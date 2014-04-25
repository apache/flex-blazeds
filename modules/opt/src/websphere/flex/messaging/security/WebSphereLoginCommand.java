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

import com.ibm.websphere.security.UserRegistry;
import com.ibm.websphere.security.WSSecurityException;
import com.ibm.websphere.security.auth.WSLoginFailedException;
import com.ibm.ws.security.core.ContextManager;
import com.ibm.ws.security.core.ContextManagerFactory;
import flex.messaging.FlexContext;
import flex.messaging.log.Log;
import flex.messaging.log.LogCategories;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/*
 * To setup WebSphere 5.1 for authentication testing:
 *
 * 1) Install WebSphere 5.1
 * 2) Create two files, users.props and groups.props
 * (examples in resources/security/websphere) and place them in a directory
 * under your WS install.
 * 3) Using the Admin webapp:
 *
 *  Security > Global Security
 *  Check Enabled
 *  Check Enforce Java 2 Security
 *  Set Active User Registry to Custom
 *  Click OK
 *
 *  Either the admin app will tell you to setup your Custom or you should
 *  go to Security > User Registries > Custom
 *
 *  Server User ID should be one of your users from your users.props
 *  Server User Password should be the matching password from users.props
 *  Customer Registry Classname by default is com.ibm.websphere.security.FileRegistrySample
 *  Go to Custom Properties
 *  Add a prop "groupsFile" that points to your groups.props: e.g., c:/websphere5.1/AppServer/security/groups.props
 *  Add a prop "usersFile" that points to your users.props: e.g., c:/websphere5.1/AppServer/security/users.props
 *
 *  Click OK
 *
 *  4) Install your Flex EAR.  You may need to go into its Session Settings
 *  page and enable session security there?
 *
 *  5) In <websphere_dir>/java/jre/lib/security edit java.policy and add something
 *  like the following:
 *
grant codeBase "file:${was.install.root}/installedApps/MCHOTIN03/Flex2Ear.ear/secure.war/-" {
  permission java.security.AllPermission;
};

 * This gives your webapp all the permissions it needs (possible that it could have
 * been narrowed down further).
 *
 * 6) Edit java.security in teh same directory to add the following entries
security.provider.1=com.sun.net.ssl.internal.ssl.Provider
security.provider.2=sun.security.provider.Sun
 * Update the entries below it so they're ordered right.
 * Copy jsse.jar and jcert.jar into java/jre/lib/ext (I think)
 * This will get the Flex Proxy to start correctly
 *
 * Restart your WebSphere, cross your fingers!!!
 *
 */

/**
 * Authenticates against WebSphere but does not store the authenticated
 * user in the HttpServletRequest for http attempts due to the container
 * not providing a mechanism for access.
 *
 * @author Paul Reilly
 * @author Matt Chotin
 */
public class WebSphereLoginCommand extends AppServerLoginCommand implements PrincipalConverter
{

    /** {@inheritDoc} */
    public Principal doAuthentication(String username, Object credentials)
    {
        Principal principal = null;
        try
        {
            String password = extractPassword(credentials);

            if (password != null)
            {
                ContextManager contextManager = ContextManagerFactory.getInstance();

                Subject subject =
                    contextManager.login(contextManager.getDefaultRealm(),
                            username, password);

                if (subject != null)
                {
                    //setting the caller subject really doesn't apply for long
                    //it appears to be removed later as each call to
                    //ContextManagerFactory.getInstance()
                    //returns a new instance and we cannot get the real context
                    //and assign values that will be re-used.
                    //this also means that the HttpServletRequest will not have the
                    //information that we've assigned, hence we store this contextManager
                    //in the Principal for later use

                    contextManager.setCallerSubject(subject);
                    principal = new WSLCPrincipal(username, contextManager, subject);
                }
            }
        }
        catch (WSLoginFailedException wsLoginFailedException)
        {
            if (Log.isDebug())
            {
                Log.getLogger(LogCategories.SECURITY).debug("WebSphereLoginCommand#doAuthentication() failed: " + wsLoginFailedException.toString(), wsLoginFailedException); 
            }
        }
        catch (WSSecurityException wsSecurityException)
        {
            if (Log.isDebug())
            {
                Log.getLogger(LogCategories.SECURITY).debug("WebSphereLoginCommand#doAuthentication() failed: " + wsSecurityException.toString(), wsSecurityException); 
            }
        }

        if (Log.isDebug()  && principal != null)
        {
            Log.getLogger(LogCategories.SECURITY).debug("WebSphereLoginCommand#doAuthentication(). Principal: " + principal + ", Principal class: " + principal.getClass().getName()
                    + ", Principal identity: " + System.identityHashCode(principal));
        }
        
        return principal;
    }

    /** {@inheritDoc} */
    public boolean doAuthorization(Principal principal, List roles)
    {
        //unfortunately we cannot seem to get the user stored
        //in the context so the request will never have the information
        //that we've assigned, therefore we have to do this
        //every time
        
        if (principal == null)
            return false;
        
        if (Log.isDebug())
            Log.getLogger(LogCategories.SECURITY).debug("WebSphereLoginCommand#doAuthorization(). Principal: " + principal + ", Principal class: " + principal.getClass().getName()
                    + ", Principal identity: " + System.identityHashCode(principal));
        
        if (principal instanceof WSLCPrincipal) // This code path is hit if this login command handled authentication.
        {
            ContextManager contextManager = ((WSLCPrincipal)principal).getContextManager();
            UserRegistry registry = contextManager.getRegistry(contextManager.getDefaultRealm());
            
            try
            {
                List groups = new ArrayList(registry.getGroupsForUser(principal.getName()));

                groups.retainAll(roles);
               
                // if authorization succeeds, set the user's Subject on this invocation context
                // so that the rest of the Thread is executed in the context of the appropriate Subject
                if (groups.size() > 0)
                    ContextManagerFactory.getInstance().setCallerSubject(((WSLCPrincipal)principal).getSubject());

                return groups.size() > 0;
            }
            catch (Exception e)
            {
            }            
        }
        else // This code path is hit if this login command didn't handle authentication.
        {
            // The Principal was not null, meaning we have a WAS Principal in the current HttpServletRequest.
            // Use that for the authorization check.
            HttpServletRequest request = FlexContext.getHttpRequest();
            for (Iterator iter = roles.iterator(); iter.hasNext(); )
            {
                if (request.isUserInRole((String)iter.next()))
                    return true;
            }
        }

        return false;
    }

    /** {@inheritDoc} */
    public boolean logout(Principal principal)
    {
        //as long as credentials are nulled since we can't store
        //the authenticated user there's nothing to do
        return true;
    }

    private class WSLCPrincipal implements Principal
    {
        private String username;
        private ContextManager contextManager;
        private Subject subject;

        public WSLCPrincipal(String username, ContextManager contextManager, Subject subject)
        {
            this.username = username;
            this.contextManager = contextManager;
            this.subject = subject;
        }

        public String getName()
        {
            return username;
        }

        public ContextManager getContextManager()
        {
            return contextManager;
        }
        
        public Subject getSubject()
        {
            return subject;
        }
    }
    
    /** {@inheritDoc} */
    public Principal convertPrincipal(Principal principal)
    {
        if (principal instanceof WSLCPrincipal)
        {
            // We are good
            return principal;
        }
        else
        {
            // we need the converting

            ContextManager contextManager = ContextManagerFactory.getInstance();

            Subject subject = null;
            try
            {
                subject = contextManager.getCallerSubject();
            }
            catch (WSSecurityException e)
            {
                
            }
            
            if (subject != null)
            {
                return new WSLCPrincipal(principal.getName(), contextManager, subject);
            }
            else
                // Just return the old one
                return principal;
            
        }
    }
}
