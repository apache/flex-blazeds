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

import oracle.security.jazn.JAZNConfig;
import oracle.security.jazn.callback.JAZNCallbackHandler;

import java.security.Principal;
import java.util.*;
import javax.security.auth.callback.*;
import javax.security.auth.login.*;

/**
 * A Oracle specific implementation of LoginCommand to manually authenticate
 * a user with the current web-app container.
 */
public class OracleLoginCommand extends AppServerLoginCommand
{
    /** {@inheritDoc} */
    public Principal doAuthentication(String username, Object credentials)
        throws SecurityException
    {
        OracleUser user;
        try
        {
            CallbackHandler callbackHandler = new JAZNCallbackHandler
                (JAZNConfig.getJAZNConfig(), null, 
                 username, extractPassword(credentials));
            LoginContext context = new LoginContext
                ("oracle.security.jazn.oc4j.JAZNUserManager", callbackHandler);
            user = new OracleUser(context);
        }
        catch (LoginException loginException)
        {
            throw wrapLoginException(loginException);
        }
        return user;
    }

    /** {@inheritDoc} */
    public boolean doAuthorization(Principal principal, List roles) 
        throws SecurityException
    {
        boolean result = false;
        if (principal instanceof OracleUser)
        {
            OracleUser user = (OracleUser) principal;
            result = user.isMemberOf(roles);
        }        
        return result;
    }

    /** {@inheritDoc} */
    public boolean logout(Principal principal) throws SecurityException
    {
        boolean result = false;
        if (principal instanceof OracleUser)
        {
            OracleUser user = (OracleUser) principal;
            try
            {
                user.logout();
                result = true;
            }
            catch (LoginException loginException)
            {
                throw wrapLoginException(loginException);
            }
        }
        return result;
    }

    private SecurityException wrapLoginException(LoginException exception)
    {
        SecurityException result = new SecurityException();
        result.setRootCause(exception);
        return result;
    }
}
