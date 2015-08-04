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
import java.util.Iterator;
import java.util.List;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import weblogic.security.SimpleCallbackHandler;
import weblogic.security.SubjectUtils;
import weblogic.security.services.Authentication;
import weblogic.servlet.security.ServletAuthentication;
import weblogic.security.Security;
import flex.messaging.FlexContext;

/**
 * Authenticates against WebLogic and if using an HttpServlet will store
 * the authenticated user in the request.
 */
public class WeblogicLoginCommand extends AppServerLoginCommand implements PrincipalConverter
{
    /** {@inheritDoc} */
    public Principal doAuthentication(String username, Object credentials)
    {
        Principal principal = null;

        String password = extractPassword(credentials);

        if (password != null)
        {
            // Test for the presence of a response here (rather than request) because NIO 
            // endpoints require the alternate code path and they don't populate the response
            // in FlexContext.
            HttpServletResponse response = FlexContext.getHttpResponse();
            if (response != null)
            {
                HttpServletRequest request = FlexContext.getHttpRequest();
                int result = ServletAuthentication.FAILED_AUTHENTICATION;
                try
                {
                    result = ServletAuthentication.login(username, password,
                            request);
                }
                catch (LoginException e)
                {
                }
                catch (NoSuchMethodError noSuchMethodError)
                {
                    //even though we're not supporting WebLogic 7 anymore...
                    // Weblogic 7.0.4 didn't have login(), so try weak().
                    result = ServletAuthentication.weak(username, password,
                            request);
                }

                if (result != ServletAuthentication.FAILED_AUTHENTICATION)
                {
                    // To authorize against the Groups defined via the WL console, we need
                    // to have a SubjectPrincipal.  Because we do not need a principal to authorize
                    // against web.xml / weblogic.xml, always save the SubjectPrincipal
                    principal = getSubjectPrincipal(username, password);
                }
            }
            else // Code path for NIO endpoints.
            {
                principal = getSubjectPrincipal(username, password);
            }
        }

        return principal;
    }

    /**
     * Get a SubjectPrincipal for the current user.
     * @return the generated SubjectPrincipal
     */
    private Principal getSubjectPrincipal(String username, String password)
    {
        Principal principal=null;

        SimpleCallbackHandler handler =
            new SimpleCallbackHandler(username, password);
        try
        {
            Subject subject = Authentication.login(handler);
            principal = new SubjectPrincipal(subject);
        }
        catch (LoginException e)
        {
            // let authentication fail if this fails
        }

        return principal;
    }

    /**
     * Authorize a user against the Groups defined in the WL console.
     * @param principal - Current user principal
     * @param roles - Set of roles that allow a succesfull authorization
     * @return true if the authorization were succesfull
     */
    private boolean doSubjectGroupAuthorization(Principal principal, List roles)
    {
        boolean authorized = false;

        Subject subject = null;
        if (principal instanceof SubjectPrincipal)
        {
            subject = ((SubjectPrincipal)principal).getSubject();
        }
        else
        {
            subject = Security.getCurrentSubject();
        }
        if (subject == null)
        {
            return false;
        }
        Iterator iter = roles.iterator();
        while (iter.hasNext())
        {
            String role = (String)iter.next();
            if (SubjectUtils.isUserInGroup(subject, role))
            {
                authorized = true;
                break;
            }
        }

        return authorized;
    }

    /** {@inheritDoc} */
    public boolean doAuthorization(Principal principal, List roles)
    {
        if (principal == null)
            return false; // Avoid NPEs.
        
        //NOTE: I believe that both HttpServletRequest.isUserInRole and
        //SubjectUtils.isUserInGroup returns if the user is in a Weblogic Group,
        //not necessarily the Weblogic role construct

        boolean authorized = false;

        // Test for the presence of a response here (rather than request) because NIO 
        // endpoints require the alternate code path and they don't populate the response
        // in FlexContext.
        HttpServletResponse response = FlexContext.getHttpResponse();
        if (response != null)
        {
            HttpServletRequest request = FlexContext.getHttpRequest();
            
            // This will attempt to authorize the user against roles configured
            // in web.xml and weblogic.xml.
            authorized = doAuthorization(principal, roles, request);

            // We also want to support roles defined via the WL console
            // attempt this authorization here
            if (!authorized)
            {
                authorized = doSubjectGroupAuthorization(principal, roles);
            }
        }
        else // Code path for NIO endpoints.
        {            
            authorized = doSubjectGroupAuthorization(principal, roles);
        }

        return authorized;
    }

    /** {@inheritDoc} */
    public boolean logout(Principal principal)
    {
        HttpServletResponse response = FlexContext.getHttpResponse();
        if (response != null)
        {
            // Destroy the Principal maintained by the app server.
            HttpServletRequest request = FlexContext.getHttpRequest();
            ServletAuthentication.logout(request);
        }
        // else, current non-servlet session will be automatically invalidated, destroying any active Principal.
        
        return true;
    }

    private class SubjectPrincipal implements Principal
    {
        private Subject subject;

        public SubjectPrincipal(Subject subject)
        {
            this.subject = subject;
        }

        public String getName()
        {
            return SubjectUtils.getUserPrincipal(subject).getName();
        }

        public Subject getSubject()
        {
            return subject;
        }
    }
    
    /** {@inheritDoc} */
    public Principal convertPrincipal(Principal principal)
    {
        if (principal instanceof SubjectPrincipal)
        {
            return principal;
        }
        else
        {
            // We need to do the converting
            Subject subject = Security.getCurrentSubject();
            return new SubjectPrincipal(subject);
        }
    }
}
