/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package flex.messaging.endpoints.amf;

import flex.messaging.endpoints.BaseHTTPEndpoint;
import flex.messaging.io.amf.ASObject;
import flex.messaging.io.amf.ActionContext;
import flex.messaging.io.amf.MessageBody;
import flex.messaging.io.amf.MessageHeader;
import flex.messaging.messages.Message;
import flex.messaging.messages.RemotingMessage;
import flex.messaging.messages.ErrorMessage;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Array;

/**
 * AMF Headers are of limited use because the apply to the entire AMF packet, which
 * may contain a batch of several requests.
 * <p>
 * Rather than relying on the Flash Player team to change the AMF specification,
 * Flex 1.5 introduced the concept of a Message Envelope that allowed them to provide
 * message level headers that apply to a single request body.
 * </p>
 * <p>
 * Essentially they introduced one more layer of indirection with an ASObject of type &quot;Envelope&quot;
 * that had two properties:<br />
 * - <i>headers</i>, which was an array of Header structures<br />
 * - <i>body</i>, which was the actual data of the request (typically an array of arguments)
 * </p>
 * <p>
 * To save space on the wire, a Header structure was simply an array. The first element was
 * the header name as a String, and was the only required field. The second element, a boolean,
 * indicated whether the header must be understood. The third element, any Object, represented
 * the header value, if required.
 * </p>
 *
 * @author Peter Farland
 */
public class LegacyFilter extends AMFFilter
{
    public static final String LEGACY_ENVELOPE_FLAG_KEY = "_flag";
    public static final String LEGACY_ENVELOPE_FLAG_VALUE = "Envelope";
    public static final String LEGACY_SECURITY_HEADER_NAME = "Credentials";
    public static final String LEGACY_SECURITY_PRINCIPAL = "userid";
    public static final String LEGACY_SECURITY_CREDENTIALS = "password";
    
    private BaseHTTPEndpoint endpoint;

    public LegacyFilter(BaseHTTPEndpoint endpoint)
    {
        this.endpoint = endpoint;
    }

    public void invoke(final ActionContext context) throws IOException
    {
        MessageBody requestBody = context.getRequestMessageBody();
        context.setLegacy(true);

        // Parameters are usually sent as an AMF Array
        Object data = requestBody.getData();
        List newParams = null;

        // Check whether we're a new Flex 2.0 Messaging request
        if (data != null)
        {
            if (data.getClass().isArray())
            {
                int paramLength = Array.getLength(data);
                if (paramLength == 1)
                {
                    Object obj = Array.get(data, 0);
                    if (obj != null && obj instanceof Message)
                    {
                        context.setLegacy(false);
                        newParams = new ArrayList();
                        newParams.add(obj);
                    }
                }

                // It was not a Flex 2.0 Message, but we have an array, use its contents as our params
                if (newParams == null)
                {
                    newParams = new ArrayList();
                    for (int i = 0; i < paramLength; i++)
                    {
                        try
                        {
                            newParams.add(Array.get(data, i));
                        }
                        catch (Throwable t)
                        {
                        }
                    }
                }
            }
            else if (data instanceof List)
            {
                List paramList = (List)data;
                if (paramList.size() == 1)
                {
                    Object obj = paramList.get(0);
                    if (obj != null && obj instanceof Message)
                    {
                        context.setLegacy(false);
                        newParams = new ArrayList();
                        newParams.add(obj);
                    }
                }

                // It was not a Flex 2.0 Message, but we have a list, so use it as our params
                if (newParams == null)
                {
                    newParams = (List)data;
                }
            }
        }

        // We still haven't found any lists of params, so create one with
        // whatever data we have.
        if (newParams == null)
        {
            newParams = new ArrayList();
            newParams.add(data);

        }

        if (context.isLegacy())
        {
            newParams = legacyRequest(context, newParams);
        }

        requestBody.setData(newParams);


        next.invoke(context);


        if (context.isLegacy())
        {
            MessageBody responseBody = context.getResponseMessageBody();
            Object response = responseBody.getData();

            if (response instanceof ErrorMessage)
            {
                ErrorMessage error = (ErrorMessage)response;
                ASObject aso = new ASObject();
                aso.put("message", error.faultString);
                aso.put("code", error.faultCode);
                aso.put("details", error.faultDetail);
                aso.put("rootCause", error.rootCause);
                response = aso;
            }
            else if (response instanceof Message)
            {
                response = ((Message)response).getBody();
            }
            responseBody.setData(response);
        }
    }

    private List legacyRequest(ActionContext context, List oldParams)
    {
        List newParams = new ArrayList(1);
        Map headerMap = new HashMap();
        Object body = oldParams;
        Message message = null;
        MessageBody requestBody = context.getRequestMessageBody();

        // Legacy Packet Security
        List packetHeaders = context.getRequestMessage().getHeaders();
        packetCredentials(packetHeaders, headerMap);
        

        // Legacy Body
        if (oldParams.size() == 1)
        {
            Object obj = oldParams.get(0);

            if (obj != null && obj instanceof ASObject)
            {
                ASObject aso = (ASObject)obj;

                // Unwrap legacy Flex 1.5 Envelope type
                if (isEnvelope(aso))
                {
                    body = aso.get("data");

                    // Envelope level headers
                    Object h = aso.get("headers");
                    if (h != null && h instanceof List)
                    {
                        readEnvelopeHeaders((List)h, headerMap);
                        envelopeCredentials(headerMap);
                    }
                }
            }
        }

        // Convert legacy body into a RemotingMessage
        message = createMessage(requestBody, body, headerMap);
        newParams.add(message);
        return newParams;
    }

    private boolean isEnvelope(ASObject aso)
    {
        String flag = null;
        Object f = aso.get(LEGACY_ENVELOPE_FLAG_KEY);
        if (f != null && f instanceof String)
            flag = (String)f;

        if (flag != null && flag.equalsIgnoreCase(LEGACY_ENVELOPE_FLAG_VALUE))
        {
            return true;
        }

        return false;
    }


    private RemotingMessage createMessage(MessageBody messageBody, Object body, Map headerMap)
    {
        RemotingMessage remotingMessage = new RemotingMessage();
        // Assigning an empty String, MessageBroker expects non-null messageId.        
        remotingMessage.setMessageId("");  
        remotingMessage.setBody(body);
        remotingMessage.setHeaders(headerMap);

        // Decode legacy target URI into destination.operation
        String targetURI = messageBody.getTargetURI();

        int dotIndex = targetURI.lastIndexOf(".");
        if (dotIndex > 0)
        {
            String destination = targetURI.substring(0, dotIndex);
            remotingMessage.setDestination(destination);
        }

        if (targetURI.length() > dotIndex)
        {
            String operation = targetURI.substring(dotIndex + 1);
            remotingMessage.setOperation(operation);
        }

        return remotingMessage;
    }


    private Map readEnvelopeHeaders(List headers, Map headerMap)
    {
        int count = headers.size();

        for (int i = 0; i < count; i++)
        {
            Object obj = headers.get(i);

            //We currently expect a plain old AS Array
            if (obj != null && obj instanceof List)
            {
                List h = (List)obj;

                Object name = null;
                //Object mustUnderstand = null;
                Object data = null;

                int numFields = h.size();

                //The array must have exactly three (3) fields
                if (numFields == 3)
                {
                    name = h.get(0);

                    if (name != null && name instanceof String)
                    {
                        //mustUnderstand = h.get(1);
                        data = h.get(2);
                        headerMap.put(name, data);
                    }
                }
            }
        }

        return headerMap;
    }

    private void envelopeCredentials(Map headers)
    {
        // Process Legacy Security Credentials
        Object obj = headers.get(LEGACY_SECURITY_HEADER_NAME);
        if (obj != null && obj instanceof ASObject)
        {
            ASObject header = (ASObject)obj;
            String principal = (String)header.get(LEGACY_SECURITY_PRINCIPAL);
            Object credentials = header.get(LEGACY_SECURITY_CREDENTIALS);
            endpoint.getMessageBroker().getLoginManager().login(principal, credentials.toString());
        }
        headers.remove(LEGACY_SECURITY_HEADER_NAME);
    }

    private void packetCredentials(List packetHeaders, Map headers)
    {
        if (packetHeaders.size() > 0)
        {
            for (Iterator iter = packetHeaders.iterator(); iter.hasNext();)
            {
                MessageHeader header = (MessageHeader)iter.next();
                if (header.getName().equals(LEGACY_SECURITY_HEADER_NAME))
                {
                    Map loginInfo = (Map)header.getData();
                    String principal = loginInfo.get(LEGACY_SECURITY_PRINCIPAL).toString();
                    Object credentials = loginInfo.get(LEGACY_SECURITY_CREDENTIALS);
                    endpoint.getMessageBroker().getLoginManager().login(principal, credentials.toString());
                    break;
                }
            }
        }
    }
}
