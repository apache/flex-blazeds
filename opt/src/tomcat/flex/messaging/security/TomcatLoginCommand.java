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
package flex.messaging.security;

import java.security.Principal;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import flex.messaging.FlexContext;
import flex.messaging.util.PropertyStringResourceLoader;

/**
 * A Tomcat specific implementation of LoginCommand.
 */
public class TomcatLoginCommand extends AppServerLoginCommand implements PrincipalConverter
{
    private static final int NO_VALVE = 20000;

    /** {@inheritDoc} */
    public Principal doAuthentication(String username, Object credentials) throws SecurityException
    {
        TomcatLogin login = TomcatLoginHolder.getLogin();
        if (login == null)
        {
            SecurityException se = new SecurityException(new PropertyStringResourceLoader(PropertyStringResourceLoader.VENDORS_BUNDLE));
            se.setMessage(NO_VALVE);
            throw se;
        }

        String password = extractPassword(credentials);
        if (password != null)
        {
            HttpServletRequest request = (HttpServletRequest)FlexContext.getHttpRequest();
            return login.login(username, password, request);
        }

        return null;
    }

    /** {@inheritDoc} */
    public boolean doAuthorization(Principal principal, List roles) throws SecurityException
    {
        boolean authorized = false;

        HttpServletRequest request = FlexContext.getHttpRequest();
        // Response is null for NIO endpoints.
        HttpServletResponse response = FlexContext.getHttpResponse();

        if (responseAndRequestNotNull(response, request) 
                && principalMatchesWithRequest(principal, request))
        {
            authorized = doAuthorization(principal, roles, request);
        }
        else
        {
            TomcatLogin login = TomcatLoginHolder.getLogin();
            if (login == null)
            {
                SecurityException se =
                    new SecurityException(new PropertyStringResourceLoader(PropertyStringResourceLoader.VENDORS_BUNDLE));
                se.setMessage(NO_VALVE);
                throw se;
            }
            authorized = login.authorize(principal, roles);
        }

        return authorized;
    }

    /** {@inheritDoc} */
    public boolean logout(Principal principal) throws SecurityException
    {
        HttpServletRequest request = FlexContext.getHttpRequest();
        // Response is null for NIO endpoints.
        HttpServletResponse response = FlexContext.getHttpResponse();
        if (responseAndRequestNotNull(response, request))
        {
            TomcatLogin login = TomcatLoginHolder.getLogin();
            if (login != null)
            {
                return login.logout(request);
            }
            else
            {
                //TODO should we do this?
                //request.getSession(false).invalidate();
            }
        }
        return true;
    }

    private boolean principalMatchesWithRequest(Principal principal, HttpServletRequest request)
    {
        return principal != null && principal.equals(request.getUserPrincipal());
    }

    private boolean responseAndRequestNotNull(HttpServletResponse response, HttpServletRequest request)
    {
        return response != null && request != null;
    }
    
    /** {@inheritDoc} */
    public Principal convertPrincipal(Principal principal)
    {
        TomcatLogin login = TomcatLoginHolder.getLogin();
        return login.convertPrincipal(principal);
    }
}
