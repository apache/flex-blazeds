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

import flex.messaging.FlexContext;
import flex.messaging.io.amf.ActionContext;
import flex.messaging.io.amf.MessageHeader;
import flex.messaging.io.MessageIOConstants;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This filter detects whether a request URL is decorated with a ;jessionid token
 * in the event that the client does not support cookies. In that case, an AppendToGatewayUrl
 * header with jsessionid as its value is added to the response message.
 */
public class SessionFilter extends AMFFilter
{
    public SessionFilter()
    {
    }

    public void invoke(final ActionContext context) throws IOException
    {
        next.invoke(context);

        try
        {
            HttpServletRequest request = FlexContext.getHttpRequest();
            HttpServletResponse response = FlexContext.getHttpResponse();

            StringBuffer reqURL = request.getRequestURL();

            if (reqURL != null)
            {
                if (request.getQueryString() != null)
                    reqURL.append('?').append(request.getQueryString());

                String oldFullURL = reqURL.toString().trim();
                String encFullURL = response.encodeURL(oldFullURL).trim();

                String sessionSuffix = null;

                // It's ok to lower case here as URLs must be in ASCII
                int pos = encFullURL.toLowerCase().indexOf(";jsessionid");
                if (pos > 0)
                {
                    StringBuffer sb = new StringBuffer();
                    sb.append(encFullURL.substring(pos));
                    sessionSuffix = sb.toString();
                }

                if (sessionSuffix != null && oldFullURL.indexOf(sessionSuffix) < 0)
                {
                    context.getResponseMessage().addHeader(new MessageHeader(MessageIOConstants.URL_APPEND_HEADER, true /*mustUnderstand*/, sessionSuffix));
                }
            }
        }
        catch (Throwable t)
        {
            //Nothing more we can do... don't send 'URL Append' AMF header.
        }
    }
}
