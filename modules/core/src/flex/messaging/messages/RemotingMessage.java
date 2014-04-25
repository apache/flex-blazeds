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
package flex.messaging.messages;

import java.util.Arrays;
import java.util.List;

/**
 * This type of message contains information needed to perform
 * a Remoting invocation. Some of this information mirrors that
 * of the gateway's ActionContext, but the context itself cannot
 * be used as the message, because it is available only from the
 * AMF Endpoint and not to other endpoints (the RTMP Endpoint has
 * no HTTP, and therefore cannot support the request/response and
 * session properties of the ActionContext).
 *
 * @author neville
 * @exclude 
 */
public class RemotingMessage extends RPCMessage
{
    /**
     * This number was generated using the 'serialver' command line tool.
     * This number should remain consistent with the version used by
     * ColdFusion to communicate with the message broker over RMI.
     */
    private static final long serialVersionUID = 1491092800943415719L;

    private String source;
    private String operation;
    private Object[] parameters;
    private transient List parameterList;
    
    public RemotingMessage()
    {
    }

    public String getSource()
    {
        return source;
    }

    public void setSource(String s)
    {
        source = s;
    }

    public Object getBody()
    {
        if (parameters == null && parameterList != null)
            return parameterList.toArray();
        else
            return parameters;
    }

    public void setBody(Object bodyValue)
    {
        if (bodyValue instanceof List)
        {
            // some channels/endpoints may send in a list
            // and expect to keep a reference to it - amfx
            // for example works this way, so keep the list
            // around rather than making an array copy
            if (parameterList != null)
            {
                parameterList.addAll((List) bodyValue);
            }
            else
            {
                parameterList = (List) bodyValue;
            }
        }
        else if (!bodyValue.getClass().isArray())
        {
            parameters = new Object[] { bodyValue };
        }
        else
        {
            parameters = (Object[]) bodyValue;
        }
    }

    public String getOperation()
    {
        return operation;
    }

    public void setOperation(String operation)
    {
        this.operation = operation;
    }

    public List getParameters()
    {
        if (parameters == null && parameterList != null)
        {
            parameters = parameterList.toArray();
            // we can clean up the parameter list now
            parameterList = null;
        }
        return (parameters == null) ? null : Arrays.asList(parameters);            
    }

    public void setParameters(List params)
    {
        parameters = params.toArray();
    }
    
    protected String toStringFields(int indentLevel)
    {
        String s = getOperation();
        String sp = super.toStringFields(indentLevel);
        String sep = getFieldSeparator(indentLevel);
        //parameters will be showing up as in the body (getBody() is used in super.toStringField())
        //so we will skip parameters here
        return sep + "operation = " + s + sp;
    }

}
