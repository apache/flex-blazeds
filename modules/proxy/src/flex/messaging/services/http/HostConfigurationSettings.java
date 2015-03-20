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
 * @exclude
 * Collects the properties needed to create an Apache Commons HTTPClient
 * HostConfiguration. Holds all of the variables needed to describe an HTTP
 * connection to a host: remote host, port and protocol, proxy host and port,
 * local address, and virtual host.
 *  
 * @see org.apache.commons.httpclient.HostConfiguration
 */
public class HostConfigurationSettings
{
    /** @exclude */
    public static final String HOST = "host";
    
    /** @exclude */
    public static final String PORT = "port";
    
    /** @exclude */
    public static final String PROTOCOL = "protocol";
    
    /** @exclude */
    public static final String PROTOCOL_FACFORY = "protocol-factory";
    
    /** @exclude */
    public static final String LOCAL_ADDRESS = "local-address";
    
    /** @exclude */
    public static final String MAX_CONNECTIONS = "max-connections";
    
    /** @exclude */
    public static final String PROXY = "proxy";
    
    /** @exclude */
    public static final String VIRTUAL_HOST = "virtual-host";

    private String host;    
    private int port;
    private String protocol;
    private ProtocolFactory protocolFactory;
    private String localAddress;
    private int maximumConnections;
    private String proxyHost;
    private int proxyPort;
    private String virtualHost;

    /**
     * Creates a default <code>HostConfigurationSettings</code> instance.
     */
    public HostConfigurationSettings()
    {
        maximumConnections = HTTPConnectionManagerSettings.DEFAULT_MAX_CONNECTIONS_HOST;
    }

    /**
     * Returns the <code>host</code> property.
     *
     * @return The host name for the connection.
     */
    public String getHost()
    {
        return host;
    }

    /**
     * Sets the <code>host</code> property.
     *
     * @param host The host name for the connection.
     */
    public void setHost(String host)
    {
        this.host = host;
    }

    /**
     * Returns the <code>local-address</code> property.     
     *
     * @return The local address for the connection.
     */
    public String getLocalAddress()
    {
        return localAddress;
    }

    /**
     * Sets the <code>local-address</code> property.
     *
     * @param localAddress The local address for the connection.
     */
    public void setLocalAddress(String localAddress)
    {
        this.localAddress = localAddress;
    }

    /**
     * Returns the <code>max-connections</code> property.
     *
     * @return The maximum connections.
     */
    public int getMaximumConnections()
    {
        return maximumConnections;
    }

    /**
     * Sets the <code>maximum-connections</code> property.
     *
     * @param maximumConnections The maximum connections.
     */
    public void setMaximumConnections(int maximumConnections)
    {
        this.maximumConnections = maximumConnections;
    }

    /**
     * Returns the <code>port</code> property.
     *
     * @return The port for the connection.
     */
    public int getPort()
    {
        return port;
    }

    /**
     * Sets the <code>port</code> property.
     *
     * @param port The port for the connection.
     */
    public void setPort(int port)
    {
        this.port = port;
    }

    /**
     * Returns the protocol for the connection.
     *
     * @return The protocol for the connection.
     */
    public String getProtocol()
    {
        return protocol;
    }

    /**
     * Sets the <code>protocol</code> property.
     *
     * @param protocol The protocol for the connection.
     */
    public void setProtocol(String protocol)
    {
        this.protocol = protocol;
    }

    /**
     * Returns the protocol factory for the connection.
     *
     * @return The protocol factory for the connection.
     */
    public ProtocolFactory getProtocolFactory()
    {
        return protocolFactory;
    }

    /**
     * Set the protocol factory for the connection.
     *
     * @param protocolFactory The protocol factory for the connection.
     */
    public void setProtocolFactory(ProtocolFactory protocolFactory)
    {
        this.protocolFactory = protocolFactory;
    }

    /**
     * Returns the proxy host name for the connection.
     *
     * @return The proxy host name for the connection.
     */
    public String getProxyHost()
    {
        return proxyHost;
    }

    /**
     * Sets the proxy host name for the connection.
     *
     * @param proxyHost The proxy host name for the connection.
     */     
    public void setProxyHost(String proxyHost)
    {
        this.proxyHost = proxyHost;
    }

    /**
     * Returns the proxy port for the connection.
     *
     * @return The proxy port for the connection. 
     */
    public int getProxyPort()
    {
        return proxyPort;
    }

    /**
     * Sets the proxy port for the connection.
     *
     * @param proxyPort The proxy port for the connection.
     */
    public void setProxyPort(int proxyPort)
    {
        this.proxyPort = proxyPort;
    }

    /**
     * Returns the virtual host for the connection.
     *
     * @return The virtual host for the connection.
     */
    public String getVirtualHost()
    {
        return virtualHost;
    }

    /**
     * Sets the virtual host for the connection.
     *
     * @param virtualHost The virtual host for the connection.
     */
    public void setVirtualHost(String virtualHost)
    {
        this.virtualHost = virtualHost;
    }
}
