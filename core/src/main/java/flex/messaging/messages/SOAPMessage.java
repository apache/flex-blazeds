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

import flex.messaging.io.MessageIOConstants;

/**
 * A SOAP specific subclass of HTTPMessage. By default we
 * assume the content-type as &quot;text/xml; charset=utf-8&quot;
 * and the HTTP method will be POST.
 *
 *
 */
public class SOAPMessage extends HTTPMessage
{
    /**
     * This number was generated using the 'serialver' command line tool.
     * This number should remain consistent with the version used by
     * ColdFusion to communicate with the message broker over RMI.
     */
    private static final long serialVersionUID = 3706466843618325314L;

    public SOAPMessage()
    {
        contentType = MessageIOConstants.CONTENT_TYPE_XML;
        method = MessageIOConstants.METHOD_POST;
    }

    public String getAction()
    {
        Object action = httpHeaders.get(MessageIOConstants.HEADER_SOAP_ACTION);
        return action == null ? null : action.toString();
    }

    public void setAction(String action)
    {
        httpHeaders.put(MessageIOConstants.HEADER_SOAP_ACTION, action);
    }
}
