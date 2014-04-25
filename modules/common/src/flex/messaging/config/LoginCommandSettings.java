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

/**
 * Settings for <code>LoginCommand</code> class.
 *
 * @author esolovey
 * @exclude
 */
public class LoginCommandSettings
{
    public static final String SERVER_MATCH_OVERRIDE = "all";

    private String className;
    private String server;
    private boolean perClientAuthentication;

    /**
     * Create a new <code>LoginCommandSettings</code> instance with default settings.
     */
    public LoginCommandSettings()
    {
        perClientAuthentication = false;
    }

    /**
     * Returns the class name associated with the settings.
     *
     * @return The class name.
     */
    public String getClassName()
    {
        return className;
    }

    /**
     * Sets the class name associated with the settings.
     *
     * @param className The class name.
     */
    public void setClassName(String className)
    {
        this.className = className;
    }

    /**
     * Returns the server name associated with the settings.
     *
     * @return The server name.
     */
    public String getServer()
    {
        return server;
    }

    /**
     * Sets the server name associated with the settings.
     *
     * @param server The server name.
     */
    public void setServer(String server)
    {
        this.server = server;
    }

    /**
     * Returns whether per client authentication is enabled or not.
     *
     * @return <code>true</code> if per client authentication is enabled;
     * otherwise <code>false</code>.
     */
    public boolean isPerClientAuthentication()
    {
        return perClientAuthentication;
    }

    /**
     * Sets whether per client authentication is enabled or not.
     *
     * @param perClientAuthentication <code>true</code> if per client authentication
     * is enabled; otherwise <code>false</code>.
     */
    public void setPerClientAuthentication(boolean perClientAuthentication)
    {
        this.perClientAuthentication = perClientAuthentication;
    }
}
