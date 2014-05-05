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

import flex.messaging.security.SecurityException;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Peter Farland
 * @exclude
 */
public class SecuritySettings
{
    // Exception/error message numbers.
    private static final int NO_SEC_CONSTRAINT = 10062;

    private String serverInfo;
    private Map loginCommandSettings;
    private Map constraints;
    private boolean recreateHttpSessionAfterLogin;

    public SecuritySettings()
    {
        constraints = new HashMap();
        loginCommandSettings = new HashMap();
    }

    public void addConstraint(SecurityConstraint sc)
    {
        constraints.put(sc.getId(), sc);
    }

    public SecurityConstraint getConstraint(String ref)
    {
        // If an attempt is made to use a constraint that we do not know about,
        // do not let the authorization succeed
        if (constraints.get(ref) == null)
        {
            // Security constraint {0} is not defined.
            SecurityException se = new SecurityException();
            se.setMessage(NO_SEC_CONSTRAINT, new Object[] {ref});
            throw se;
        }
        return (SecurityConstraint)constraints.get(ref);
    }

    public void addLoginCommandSettings(LoginCommandSettings lcs)
    {
        loginCommandSettings.put(lcs.getServer(), lcs);
    }

    public Map getLoginCommands()
    {
        return loginCommandSettings;
    }

    /**
     * Returns a read-only set of constraints.
     *
     * @return the set of security constraints
     */
    public Collection<SecurityConstraint> getConstraints()
    {
        return Collections.unmodifiableCollection(constraints.values());
    }

    public void setServerInfo(String s)
    {
        serverInfo = s;
    }

    public String getServerInfo()
    {
        return serverInfo;
    }

    public boolean isRecreateHttpSessionAfterLogin()
    {
        return recreateHttpSessionAfterLogin;
    }

    public void setRecreateHttpSessionAfterLogin(boolean value)
    {
        recreateHttpSessionAfterLogin = value;
    }
}
