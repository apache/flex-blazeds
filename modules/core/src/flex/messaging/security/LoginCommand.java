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

import javax.servlet.ServletConfig;

import java.security.Principal;
import java.util.List;

/**
 * The class name of the implementation of this interface is configured in the
 * gateway configuration's security section and is instantiated using reflection
 * on servlet initialization.
 */
public interface LoginCommand
{
    /**
     * Called to initialize a login command prior to authentication/authorization requests.
     * 
     * @param config The servlet configuration for MessageBrokerServlet.  
     */
    void start(ServletConfig config);

    /**
     * Called to free up resources used by the login command.
     */
    void stop();

    /**
     * The gateway calls this method to perform programmatic, custom authentication.
     * <p>
     * The credentials are passed as a Map to allow for extra properties to be
     * passed in the future. For now, only a "password" property is sent.
     * </p>
     *
     * @param username    The principal being authenticated
     * @param credentials A map, typically with string keys and values - holds, for example, a password
     * @return principal for the authenticated user when authentication is successful; null otherwise 
     */
    Principal doAuthentication(String username, Object credentials);

    /**
     * The gateway calls this method to perform programmatic authorization.
     * <p>
     * A typical implementation would simply iterate over the supplied roles and
     * check that atleast one of the roles returned true from a call to
     * HttpServletRequest.isUserInRole(String role).
     * </p>
     *
     * @param principal The principal being checked for authorization
     * @param roles    A List of role names to check, all members should be strings
     * @return true if the principal is authorized given the list of roles
     */
    boolean doAuthorization(Principal principal, List roles);

    /**
     * Attempts to log a user out from their session.
     *
     * NOTE: May not be possible on all application servers.
     * @param principal The principal to logout.
     * @return true when logout is successful
     */
    boolean logout(Principal principal);
}
