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
package flex.messaging.services.http.proxy;

import flex.messaging.FlexContext;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * Determines whether overall access to the proxy is allowed for a request.
 */
public class AccessFilter extends ProxyFilter
{
    private static final int TOO_MANY_COOKIES = 10703;

    /**
     * Invokes the filter with the context.
     * 
     * @param context The proxy context.
     */
    public void invoke(ProxyContext context)
    {
        HttpServletRequest clientRequest = FlexContext.getHttpRequest();

        // as requested by @stake, limit the number of cookies that can be sent from the endpoint to prevent
        // as denial of service attack.  It seems our processing of Flex-mangled cookies bogs down the server.
        // We set the cookie limit to 200, but it can be changed via -Dflex.cookieLimit
        if (clientRequest != null)
        {
            javax.servlet.http.Cookie[] cookies = clientRequest.getCookies();
            if (cookies != null && cookies.length > context.getCookieLimit())
            {
                ProxyException e = new ProxyException();
                e.setMessage(TOO_MANY_COOKIES, new Object[] { "" + cookies.length });
                throw e;
            }
        }

        if (next != null)
        {
            next.invoke(context);
        }
    }
}
