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

/**
 * @exclude
 * Response methods shared by J2EE and .NET.
 *
 * @author Brian Deitte
 */
public class ResponseUtil
{
    public static String getCookieName(ProxyContext context, String path, String name, String domain)
    {
        String clientName;
        if (context.isLocalDomain() && (path == null || path.equals("/")))
        {
            clientName = name;
        }
        else
        {
            //Cookie name format: COOKIE_PREFIX[COOKIE_SEPARATOR]domain[COOKIE_SEPARATOR]path[COOKIE_SEPARATOR]name
            StringBuffer nameBuf = new StringBuffer(40); //estimated length to usually avoid the buffer needing to grow
            nameBuf.append(ProxyConstants.COOKIE_PREFIX);
            nameBuf.append(ProxyConstants.COOKIE_SEPARATOR);
            nameBuf.append(domain.hashCode());
            nameBuf.append(ProxyConstants.COOKIE_SEPARATOR);
            nameBuf.append(path.hashCode());
            nameBuf.append(ProxyConstants.COOKIE_SEPARATOR);
            nameBuf.append(name);
            clientName = nameBuf.toString();
        }
        return clientName;
    }


    public static boolean ignoreHeader(String name, ProxyContext context)
    {
        boolean ignoreHeader = false;
        if ("Content-Length".equalsIgnoreCase(name) ||
                "Set-Cookie".equalsIgnoreCase(name) ||
                "Set-Cookie2".equalsIgnoreCase(name) ||
                "Cookie".equalsIgnoreCase(name) ||
                "Transfer-Encoding".equalsIgnoreCase(name) ||
                // cmurphy - copying "Connection" was causing problems with WebLogic 8.1
                // brian- Connection header specifies what type of connection is wanted, ie keep-alive.
                // From what I've read, it is perfectly acceptible for a proxy to ignore this header
                "Connection".equalsIgnoreCase(name) ||
                // ignore caching headers if we want to stop caching on this request
                (context.disableCaching() && ("Cache-Control".equalsIgnoreCase(name) ||
                "Expires".equalsIgnoreCase(name) || "Pragma".equalsIgnoreCase(name)))
        )
        {
            ignoreHeader = true;
        }
        return ignoreHeader;
    }
}
