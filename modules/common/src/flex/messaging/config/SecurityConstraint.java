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
package flex.messaging.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Security constraints are used by the login manager to secure access to
 * destinations and endpoints.
 */
public class SecurityConstraint
{
    /**
     * String constant for basic authentication.
     */
    public static final String BASIC_AUTH_METHOD = "Basic";

    /**
     * String constant for custom authentication.
     */
    public static final String CUSTOM_AUTH_METHOD = "Custom";

    private final String id;
    private String method;
    private List roles;

    /**
     * Creates an anonymous <code>SecurityConstraint</code> instance.
     */
    public SecurityConstraint()
    {
        this(null);
    }

    /**
     * Creates a <code>SecurityConstraint</code> instance with an id.
     *
     * @param id The id of the <code>SecurityConstraint</code> instance.
     */
    public SecurityConstraint(String id)
    {
        this.id = id;
        method = CUSTOM_AUTH_METHOD;
    }

    /**
     * Returns a list of roles of the <code>SecurityConstraint</code>.
     *
     * @return List of roles.
     */
    public List getRoles()
    {
        return roles;
    }

    /**
     * Adds a role to the list of roles of the <code>SecurityConstraint</code>.
     *
     * @param role New role to add to the list of roles.
     */
    public void addRole(String role)
    {
        if (role == null)
            return;

        if (roles == null)
            roles = new ArrayList();

            roles.add(role);
    }

    /**
     * Returns the id of the <code>SecurityConstraint</code>.
     *
     * @return The id of the <code>SecurityConstraint</code>.
     */
    public String getId()
    {
        return id;
    }

    /**
     * Returns the authorization method of the <code>SecurityConstraint</code>.
     *
     * @return Authorization method.
     */
    public String getMethod()
    {
        return method;
    }

    /**
     * Sets the authorization method of the <code>SecurityConstraint</code>.
     * Valid values are Basic and Custom.
     *
     * @param method The authentication method to set which can be custom or basic.
     */
    public void setMethod(String method)
    {
        if (method == null)
            return;

        if (CUSTOM_AUTH_METHOD.equalsIgnoreCase(method))
            this.method = CUSTOM_AUTH_METHOD;
        else if (BASIC_AUTH_METHOD.equalsIgnoreCase(method))
            this.method = BASIC_AUTH_METHOD;
    }
}
