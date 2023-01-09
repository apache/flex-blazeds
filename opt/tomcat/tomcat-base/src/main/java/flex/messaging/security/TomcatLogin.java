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

/**
 * Interface to code in the Tomcat valve. This is needed because Tomcat has a classloader system
 * where code in a valve does not appear in the classloader that is used for servlets.
 * There is a commons area that both valves and servlets share and this interface
 * needs to be placed there.
 */
public interface TomcatLogin {
    /**
     * Attempt to login user with the specified credentials.  Return a generated
     * Principal object if login were successful
     *
     * @param username username.
     * @param password credentials.
     * @param request  request via which this login attempt was made
     * @return Principal generated for user if login were successful
     */
    Principal login(String username, String password, HttpServletRequest request);

    /**
     * The gateway calls this method to perform programmatic authorization.
     * <p>
     * A typical implementation would simply iterate over the supplied roles and
     * check that atleast one of the roles returned true from a call to
     * HttpServletRequest.isUserInRole(String role).
     * </p>
     *
     * @param principal The principal being checked for authorization
     * @param roles     A List of role names to check, all members should be strings
     * @return true if the principal is authorized given the list of roles
     */
    boolean authorize(Principal principal, List roles);

    /**
     * Logs out the user associated with the passed-in request.
     *
     * @param request whose associated user is to be loged-out
     * @return true if logout were successful
     */
    boolean logout(HttpServletRequest request);

    /**
     * Classes that implement the flex.messaging.security.PrinciplaConverter interface, to convert a J2EE Principal to a
     * Flex Principal impl. A Flex Principal impl is specific to different Application Servers and will be used by Flex to
     * do security authorization check, which calls security framework API specific to Application Servers.
     */
    Principal convertPrincipal(Principal principal);
}
