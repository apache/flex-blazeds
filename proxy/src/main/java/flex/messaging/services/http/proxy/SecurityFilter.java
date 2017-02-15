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
import flex.messaging.services.HTTPProxyService;
import flex.messaging.log.Log;
import flex.messaging.util.Base64;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.StatusLine;
import org.apache.commons.httpclient.UsernamePasswordCredentials;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;

/**
 *
 * Handles whitelist/access and authentication/authorization system for requests.  Checks URL to make sure we
 * accept it.  Sets credentials if needed on the request.  After the request is made, changes response for
 * security info if needed
 */
public class SecurityFilter extends ProxyFilter
{
    // NOTE: any changes to this class should also be made to the corresponding version in the .NET.
    // The corresponding class is in src/dotNet/libs/FlexASPlib/Aspx/Proxy
    
    private static final int EMPTY_ERROR = 10708;
    private static final int ONLY_HTTP_HTTPS = 10712;
    private static final int NO_HTTPS_VIA_HTTP = 10713;
    private static final int NO_BASIC_NOT_HTTP = 10714;
    private static final int NO_BASIC_FOR_SOAP = 10715;
    private static final int DOMAIN_ERROR = 10716;
    private static final int LOGIN_REQUIRED = 10717;
    private static final int UNAUTHORIZED_ERROR = 10718;

    public void invoke(ProxyContext context)
    {
        checkURL(context);
        setCredentials(context);

        if (next != null)
        {
            next.invoke(context);
        }

        sendSecurityInfo(context);
    }

    private void checkURL(ProxyContext context)
    {
        Target target = context.getTarget();

        // We only allow http type urls
        if (!context.getTarget().getUrl().getProtocol().equalsIgnoreCase("http") && !target.isHTTPS())
        {
            Log.getLogger(HTTPProxyService.LOG_CATEGORY).warn(ProxyConstants.PROXY_SECURITY + ProxyConstants.ONLY_HTTP_HTTPS);
            throw new ProxyException(ONLY_HTTP_HTTPS);
        }

        if (target.isHTTPS() && !context.isClientHttps())
        {
            // Respond with error
            Log.getLogger(HTTPProxyService.LOG_CATEGORY).warn(ProxyConstants.PROXY_SECURITY + ProxyConstants.NO_HTTPS_VIA_HTTP);

            throw new ProxyException(NO_HTTPS_VIA_HTTP);
        }
    }

    private void setCredentials(ProxyContext context)
    {
        String user = null, password = null;

        // Check for credentials in runAs
        user = context.getTarget().getRemoteUsername();
        password = context.getTarget().getRemotePassword();

        String fromRequest = null;
        HttpServletRequest clientRequest = FlexContext.getHttpRequest();
        if (clientRequest != null)
        {
            // Check for credentials in parameter/header
            fromRequest = clientRequest.getHeader(ProxyConstants.HEADER_CREDENTIALS);
        }

        // We sometimes send the credentials as a URL parameter to work around a player/Opera issue
        if (fromRequest == null)
        {
            fromRequest = context.getCredentialsHeader();
        }

        // Add authentication header for credentials
        if (fromRequest != null)
        {
            Base64.Decoder decoder = new Base64.Decoder();
            decoder.decode(fromRequest);
            String decoded = new String(decoder.drain());
            int colonIdx = decoded.indexOf(":");
            if (colonIdx != -1)
            {
                user = decoded.substring(0, colonIdx);
            }
            password = decoded.substring(colonIdx + 1);
        }

        // Check for existing authentication header
        if (clientRequest != null)
        {
            Enumeration headers = clientRequest.getHeaders("Authorization");
            if (headers != null)
            {
                while (headers.hasMoreElements())
                {
                    String value = (String)headers.nextElement();

                    if (value.startsWith("Basic"))
                    {
                        if (!context.isLocalDomainAndPort())
                        {
                            if (Log.isInfo())
                            {
                                Log.getLogger(HTTPProxyService.LOG_CATEGORY).debug("Not sending on Authentication header. Proxy domain:port of " +
                                        clientRequest.getServerName() + ":" + clientRequest.getServerPort() + " does not match target domain:port of " +
                                        context.getTarget().getUrl().getHost() + ":" + context.getTarget().getUrl().getPort());
                            }
                        }
                        else
                        {
                            // Super gross hack to work around what appears to be an commons-httpclient bug
                            // where headers are not resent after a 302.
                            Base64.Decoder decoder = new Base64.Decoder();
                            String encoded = value.substring(6);
                            decoder.decode(encoded);
                            String decoded = new String(decoder.drain());
                            int colonIdx = decoded.indexOf(":");
                            user = decoded.substring(0, colonIdx);
                            password = decoded.substring(colonIdx + 1);
                        }
                    }
                }
            }
        }

        // Set up request for authentication
        if (user != null)
        {
            UsernamePasswordCredentials cred = new UsernamePasswordCredentials(user, password);

            context.getHttpClient().getState().setCredentials(ProxyUtil.getDefaultAuthScope(), cred);
            context.setAuthorization(true);

            if (Log.isInfo())
            {
                Log.getLogger(HTTPProxyService.LOG_CATEGORY).info("-- Authentication header being sent for " + user);
            }
        }
    }

    private void sendSecurityInfo(ProxyContext context)
    {
        Target target = context.getTarget();
        String targetHost = target.getUrl().getHost();

        int statusCode = 200;

        boolean customAuth = target.useCustomAuthentication();

        StatusLine statusLine = context.getHttpMethod().getStatusLine();
        if (statusLine != null)
        {
            statusCode = statusLine.getStatusCode();
        }

        context.setStatusCode(statusCode);

        if (statusCode == 401 || statusCode == 403)
        {
            if (!customAuth)
            {
                if (!context.isHttpRequest())
                {
                    throw new ProxyException(NO_BASIC_NOT_HTTP);
                }
                else if (context.isSoapRequest())
                {
                    // Note: if we remove this error, must do the proxyDomain/targetHost check as done above
                    throw new ProxyException(NO_BASIC_FOR_SOAP);
                }
                else
                {
                    // Don't allow a 401 (and 403, although this should never happen) to be sent to the client
                    // if the service is not using custom authentication and the domains do not match

                    if (!context.isLocalDomainAndPort())
                    {
                        HttpServletRequest clientRequest = FlexContext.getHttpRequest();

                        String errorMessage = ProxyConstants.DOMAIN_ERROR + " . The proxy domain:port is " +
                                clientRequest.getServerName() + ":" + clientRequest.getServerPort() + 
                                " and the target domain:port is " + targetHost + ":" + target.getUrl().getPort();

                        Log.getLogger(HTTPProxyService.LOG_CATEGORY).error(errorMessage);

                        throw new ProxyException(DOMAIN_ERROR);
                    }
                    else
                    {
                        //For BASIC Auth, send back the status code
                        HttpServletResponse clientResponse = FlexContext.getHttpResponse();
                        clientResponse.setStatus(statusCode);
                    }
                }
            }
            else
            {
                String message = null;
                if (statusLine != null)
                    message = statusLine.toString();

                if (statusCode == 401)
                {
                    ProxyException se = new ProxyException();
                    se.setCode("Client.Authentication");
                    if (message == null)
                    {
                        se.setMessage(LOGIN_REQUIRED);
                    }
                    else
                    {
                        se.setMessage(EMPTY_ERROR, new Object[] { message });
                    }


                    Header header = context.getHttpMethod().getResponseHeader(ProxyConstants.HEADER_AUTHENTICATE);
                    if (header != null)
                        se.setDetails(header.getValue());
                    throw se;
                }
                else
                {
                    ProxyException se = new ProxyException();
                    se.setCode("Client.Authentication");
                    if (message == null)
                    {
                        se.setMessage(UNAUTHORIZED_ERROR);
                    }
                    else
                    {
                        se.setMessage(EMPTY_ERROR, new Object[] { message });
                    }
                    throw se;
                }
            }
        }
    }
}
