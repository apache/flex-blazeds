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
 *
 * Contants related to the proxy (shared with .NET).
 */
public class ProxyConstants
{
    public static final String METHOD_GET = "GET";
    public static final String METHOD_HEAD = "HEAD";
    public static final String METHOD_OPTIONS = "OPTIONS";
    public static final String METHOD_DELETE = "DELETE";
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_TRACE = "TRACE";
    public static final String METHOD_CONNECT = "CONNECT";

    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String COOKIE_PREFIX = "FLEX";
    public static final String COOKIE_SEPARATOR = "_";
    public static String HEADER_CREDENTIALS = "credentials";
    public static String HEADER_AUTHENTICATE = "WWW-Authenticate";

    public static final String HTTP_AUTHENTICATION_ERROR = "%%401%%";
    public static final String HTTP_AUTHORIZATION_ERROR = "%%403%%Authorization failed at the remote url.";
    public static final String DOMAIN_ERROR = "The Flex proxy and the specified endpoint do not have the same domain, " +
            "and so basic authentication cannot be used.  Please specify use-custom-authentication or run-as for services not located " +
            "on the same domain as the Flex proxy.";

    public static final String PROXY_SECURITY = "PROXY SECURITY : ";
    public static final String NO_HTTPS_VIA_HTTP = "Invalid URL - can't access HTTPS URLs when accessing proxy via HTTP.";
    public static final String ONLY_HTTP_HTTPS = "Invalid URL - only HTTP or HTTPS URLs allowed";
}
