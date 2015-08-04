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
package flex.messaging.services.http;

/**
 * External Proxy Settings for a proxy service.
 */
public class ExternalProxySettings
{

    public static final int DEFAULT_PROXY_PORT = 80;

    public static final String PORT = "port";

    public static final String EXTERNAL_PROXY = "external-proxy";

    public static final String SERVER = "server";

    public static final String NT_DOMAIN = "nt-domain";

    public static final String USERNAME = "username";

    public static final String PASSWORD = "password";    
    private static final String HTTP = "http://";

    private String proxyServer;
    private int proxyPort = DEFAULT_PROXY_PORT;
    private String username;
    private String password;
    private String ntDomain;

    /**
     * Creates a default <code>ExternalProxySettings</code> instance.
     */
    public ExternalProxySettings()
    {
    }

    /**
     * Returns the <code>server</code> property.
     * 
     * @return the property as a string
     */
    public String getProxyServer()
    {
        return proxyServer;
    }

    /**
     * Sets the <code>server</code> property.
     * 
     * @param s The IP or server name of the proxy server.
     */
    public void setProxyServer(String s)
    {
        if (s != null)
        {
            if (s.endsWith("/"))
                s = s.substring(0, s.length() - 1);

            boolean hasProtocol = s.indexOf("://") != -1;
            if (!hasProtocol && isDotNet())
                s = HTTP + s;

            if (!isDotNet() && hasProtocol)
            {
                if (s.startsWith(HTTP))
                    s = s.substring(HTTP.length());
                else
                    throw new IllegalArgumentException("A protocol cannot be specified for the proxy element: " + s);
                // FIXME: Should we throw an exception if a port is specified in the proxy server name?
            }
        }

        proxyServer = s;
    }

    /**
     * Returns the <code>port</code> property.
     * 
     * @return the <code>port</code>
     */
    public int getProxyPort()
    {
        return proxyPort;
    }

    /**
     * Sets the <code>port</code> property. Default is 80.
     * 
     * @param p The port number.
     */
    public void setProxyPort(int p)
    {
        if (p > 0)
        {
            proxyPort = p;
        }
    }

    /**
     * Returns the <code>username</code> property.
     * 
     * @return the <code>username</code>
     */
    public String getUsername()
    {
        return username;
    }

    /**
     * Sets the <code>username</code> property. 
     * 
     * @param username The user name for logging into the proxy server.
     */
    public void setUsername(String username)
    {
        this.username = username;
    }

    /**
     * Returns the <code>password</code> property.
     * 
     * @return the <code>password</code>
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * Sets the <code>password</code> property.
     * 
     * @param password The password for loggin into the proxy server.
     */
    public void setPassword(String password)
    {
        this.password = password;
    }

    /**
     * Returns the <code>nt-domain</code> property.
     * 
     * @return a string containing the <code>nt-domain</code>
     */
    public String getNTDomain()
    {
        return ntDomain;
    }

    /**
     * Sets the <code>nt-domain</code> property.
     * 
     * @param ntDomain The NT domain for the proxy server.
     */
    public void setNTDomain(String ntDomain)
    {
        this.ntDomain = ntDomain;
    }

    /**
     *
     */
    public static boolean isDotNet()
    {
        return System.getProperty("flex.platform.CLR") != null;
    }
}
