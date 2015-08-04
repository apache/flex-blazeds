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

import flex.messaging.log.Log;
import flex.messaging.log.LogCategories;
import flex.messaging.util.ExceptionUtil;

import java.io.IOException;
import java.security.Principal;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.catalina.Container;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Manager;
import org.apache.catalina.Realm;
import org.apache.catalina.Session;
import org.apache.catalina.Valve;
import org.apache.catalina.authenticator.Constants;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;

/**
 *
 * A Tomcat valve for allowing programmatic login.  This valve saves the container, 
 * something not available normally to a servlet, and allows login to the current realm. 
 * The pieces interacting with Tomcat are taken from org.apache.catalina.authenticator.AuthenticatorBase.
 * It would be nice if we could just extend that class or call some of its methods, 
 * but things aren't set up in that class in such a way that this is possible
 *
 * FIXME: Doesn't support Tomcat's SingleSignOn which is a way to write custom valves that associate
 * the principal to different web apps or locations. See AuthenticatorBase for details
 */
public class TomcatValve extends ValveBase implements Lifecycle
{
    private static final String AUTH_TYPE = "flexmessaging"; // was "flashgateway"
    private static final String AMF_MATCH = "/amfgateway";
    private static final String GATEWAY_MATCH = "/flashgateway";
    private static final String MESSAGEBROKER_MATCH = "/messagebroker"; 
    private static String CUSTOM_MATCH = System.getProperty("flex.tomcatValveMatch");

    public void invoke(Request request, Response response) throws IOException, ServletException
    {
        invokeServletRequest(request);

        Valve next = getNext();
        if (next != null)
            next.invoke(request, response);
    }

    private void invokeServletRequest(Request request)
    {
        ServletRequest servRequest = request.getRequest();
        if (!(servRequest instanceof HttpServletRequest))
            return;

        // We only set the TomcatLoginImpl for gateway paths
        HttpServletRequest hrequest = (HttpServletRequest)servRequest;
        boolean match = checkIfPathMatches(hrequest.getServletPath(), hrequest.getRequestURI());
        if (match)
            handleMatch(request, hrequest.getUserPrincipal());
    }

    private void handleMatch(Request request, Principal principal)
    {
        TomcatLoginHolder.setLogin(new TomcatLoginImpl(getContainer(), request));

        // Copy over user principal and auth type values, just like in AuthenticatorBase.invoke()
        if (principal != null)
            return;

        Session session = getSession(request, false);
        if (session == null)
            return;

        principal = session.getPrincipal();
        if (principal != null) 
        {
            request.setAuthType(session.getAuthType());
            request.setUserPrincipal(principal);
        }
    }

    private boolean checkIfPathMatches(String path, String uri)
    {
        if (path == null)
        {
            // We need to use a slighly-weaker uri match for 4.1
            return (uri != null &&
                    (uri.indexOf(MESSAGEBROKER_MATCH) != -1 ||
                            uri.indexOf(AMF_MATCH) != -1 ||
                            uri.indexOf(GATEWAY_MATCH) != -1 ||
                            (CUSTOM_MATCH != null && uri.indexOf(CUSTOM_MATCH) != -1)));
        }
        else
        {
            return (path.startsWith(MESSAGEBROKER_MATCH) ||
                    path.startsWith(AMF_MATCH) ||
                    path.startsWith(GATEWAY_MATCH) ||
                    (CUSTOM_MATCH != null && path.startsWith(CUSTOM_MATCH)));
        }
    }

    public void addLifecycleListener(LifecycleListener listener)
    {
        // No-op.
    }

    public LifecycleListener[] findLifecycleListeners()
    {
        return null;
    }

    public void removeLifecycleListener(LifecycleListener listener)
    {
        // No-op.
    }

    public void start() throws LifecycleException
    {
        // RTMP may not go through invoke so we need to put at least one TomcatLoginImpl in the holder.
        TomcatLogin login = new TomcatLoginImpl(getContainer(), null);
        TomcatLoginHolder.setLogin(login);
        // To avoid the thread processes the nio based endpoints does not match the thread start the valve (which is quite possible in Tomcat)
        // We set the singleton 
        TomcatLoginHolder.setNioBasedLogin(login);
    }

    public void stop() throws LifecycleException
    {
        // No-op.
    }

    // from AuthenticatorBase.getSession()
    static Session getSession(Request request, boolean create) 
    {

        HttpServletRequest hreq = (HttpServletRequest)request.getRequest();
        HttpSession hses = hreq.getSession(create);

        if (hses == null)
            return null;

        Manager manager = request.getContext().getManager();
        if (manager == null)
            return null;

        try 
        {
            return manager.findSession(hses.getId());
        }
        catch (IOException e) 
        {
            Log.getLogger(LogCategories.SECURITY).error("Error in TomcatValve getting session id " + hses.getId() + " : " + ExceptionUtil.toString(e));
            return null;
        }
    }

    class TomcatLoginImpl implements TomcatLogin
    {
        private Container container;
        private Request request;

        TomcatLoginImpl(Container container, Request request)
        {
            this.container = container;
            this.request = request;
        }

        // Authenticate the user and associate with the current session.
        // This is taken from AuthenticatorBase.register()
        public Principal login(String username, String password, HttpServletRequest servletRequest)
        {
            Realm realm = container.getRealm();
            if (realm == null)
                return null;

            Principal principal = realm.authenticate(username, password);
            if (principal == null)
                return null;

            if (servletRequestMatches(servletRequest))
            {
                request.setAuthType(AUTH_TYPE);
                request.setUserPrincipal(principal);

                Session session = getSession(request, true);

                // Cache the authentication information in our session.
                if (session != null) 
                {
                    session.setAuthType(AUTH_TYPE);
                    session.setPrincipal(principal);

                    if (username != null)
                        session.setNote(Constants.SESS_USERNAME_NOTE, username);
                    else
                        session.removeNote(Constants.SESS_USERNAME_NOTE);

                    if (password != null)
                        session.setNote(Constants.SESS_PASSWORD_NOTE, password);
                    else
                        session.removeNote(Constants.SESS_PASSWORD_NOTE);
                }
            }

            return principal;
        }

        public boolean authorize(Principal principal, List roles)
        {
            Realm realm = container.getRealm();
            Iterator iter = roles.iterator();
            while (iter.hasNext())
            {
                String role = (String)iter.next();
                if (realm.hasRole(principal, role))
                    return true;
            }
            return false;
        }

        public boolean logout(HttpServletRequest servletRequest)
        {
            if (servletRequestMatches(servletRequest))
            {
                Session session = getSession(request, false);
                if (session != null)
                {
                    session.setPrincipal(null);
                    session.setAuthType(null);
                    session.removeNote(Constants.SESS_USERNAME_NOTE);
                    session.removeNote(Constants.SESS_PASSWORD_NOTE);
                }
                return true;
            }
            return false;
        }

        private boolean servletRequestMatches(HttpServletRequest servletRequest)
        {
            return request != null && request.getRequest() == servletRequest;
        }
        
        /** {@inheritDoc} */
        public Principal convertPrincipal(Principal principal)
        {
            return principal;
        }
    }

}
