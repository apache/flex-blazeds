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
import java.util.Map;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.http.HttpServletRequest;

import flex.messaging.FlexContext;
import flex.messaging.io.MessageIOConstants;

/**
 * This class implements LoginCommand and doAuthorization in way that should work by default if
 * authorization logged a user into the J2EE application server.  doAuthorization uses isUserInRole.
 */
public abstract class AppServerLoginCommand implements LoginCommand {

    /**
     * The gateway calls this method to perform programmatic authorization.
     * <p>
     * This implementation will simply iterate over the supplied roles and
     * check that at least one of the roles returned true from a call to
     * HttpServletRequest.isUserInRole(String role).
     * </p>
     *
     * @param principal The principal being checked for authorization
     * @param roles     A List of role names to check, all members should be strings
     * @return true if the principal belongs to at least one of the roles
     * @throws SecurityException Throws SecurityException
     */
    public boolean doAuthorization(Principal principal, List roles) throws SecurityException {
        HttpServletRequest request = FlexContext.getHttpRequest();
        return (request != null) ? doAuthorization(principal, roles, request) : false;
    }

    protected boolean doAuthorization(Principal principal, List roles, HttpServletRequest request)
            throws SecurityException {
        for (Object role : roles) {
            if (request.isUserInRole((String) role))
                return true;
        }

        return false;
    }

    protected String extractPassword(Object credentials) {
        if (credentials instanceof String)
            return (String) credentials;
        else if (credentials instanceof Map)
            return (String) ((Map) credentials).get(MessageIOConstants.SECURITY_CREDENTIALS);
        return null;
    }

    /**
     * Called to initialize a login command prior to authentication/authorization requests.
     * The default implementation is no-op but subclasses can override to provide
     * their own implementation.
     *
     * @param config The servlet configuration for MessageBrokerServlet.
     */
    public void start(ServletConfig config) {
        // No-op.
    }

    /**
     * Called to free up resources used by the login command. The default implementation
     * is no-op, subclasses can override to provide their own implementation.
     */
    public void stop() {
        // No-op.
    }
}
