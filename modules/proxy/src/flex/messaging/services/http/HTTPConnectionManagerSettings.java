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

import java.util.List;

import org.apache.commons.httpclient.cookie.CookiePolicy;

/**
 * Establishes the settings used to construct an Apache Commons HTTPClient
 * HttpConnectionManager for the Proxy Service.
 */
public class HTTPConnectionManagerSettings 
{    
    /** @exclude */
    public static final String COOKIE_POLICY = "cookie-policy";

    /** @exclude */
    public static final String CONNECTION_MANAGER = "connection-manager";
    
    /** @exclude */
    public static final String CONNECTION_TIMEOUT = "connection-timeout";
    
    /** @exclude */
    public static final String DEFAULT_MAX_CONNECTIONS_PER_HOST = "default-max-connections-per-host";
    
    /** @exclude */
    public static final String LINGER = "linger";    
    
    /** @exclude */
    public static final String MAX_PER_HOST = "max-per-host";    
    
    /** @exclude */
    public static final String MAX_TOTAL_CONNECTIONS = "max-total-connections";
    
    /** @exclude */
    public static final String RECEIVE_BUFFER_SIZE = "receive-buffer-size";
    
    /** @exclude */
    public static final String SEND_BUFFER_SIZE = "send-buffer-size";
    
    /** @exclude */
    public static final String SOCKET_TIMEOUT = "socket-timeout";
    
    /** @exclude */
    public static final String STALE_CHECKING_ENABLED = "stale-checking-enabled";
    
    /** @exclude */
    public static final String TCP_NO_DELAY = "tcp-no-delay";

    /** The default maximum number of connections allowed per host. */
    public static final int DEFAULT_MAX_CONNECTIONS_HOST = 2;   // Per RFC 2616 sec 8.1.4
 
    /** The default maximum number of connections allowed overall. */
    public static final int DEFAULT_MAX_TOTAL_CONNECTIONS = 20;

    private String cookiePolicy;
    private int connectionTimeout;
    private int defaultMaxConnectionsPerHost;
    private int linger;
    private int maxTotalConnections;
    private List maxConnectionsPerHost;
    private int receiveBufferSize;
    private int sendBufferSize;
    private int socketTimeout;
    private boolean staleCheckingEnabled;
    private boolean tcpNoDelay;

    /**
     * Creates a default <code>HTTPConnectionManagerSettings</code> instance.
     */
    public HTTPConnectionManagerSettings()
    {
        cookiePolicy = CookiePolicy.DEFAULT;
        defaultMaxConnectionsPerHost = DEFAULT_MAX_CONNECTIONS_HOST;
        maxTotalConnections = DEFAULT_MAX_TOTAL_CONNECTIONS;
        linger = -1;
        staleCheckingEnabled = true;
        tcpNoDelay = true;
    }

    /**
     * Returns the cookie policy.
     * 
     * @return The cookie policy.
     */
    public String getCookiePolicy()
    {
        return cookiePolicy;
    }

    /**
     * Sets the cookie policy.
     * 
     * @param cookiePolicy The cookie policy.
     */
    public void setCookiePolicy(String cookiePolicy)
    {
        this.cookiePolicy = cookiePolicy;
    }

    /**
     * Returns the number of milliseconds to wait before a connection will
     * timeout.
     *
     * @return The number of milliseconds to wait until a connection is
     * established. The default value is 0 which means a timeout is not used. 
     */
    public int getConnectionTimeout()
    {
        return connectionTimeout;
    }

    /**
     * Sets the number of milliseconds to wait before a connection will
     * timeout.
     * 
     * @param value - timeout in milliseconds
     */
    public void setConnectionTimeout(int value)
    {
        connectionTimeout = value;
    }

    /**
     * Returns the default maximum number of connections allowed per host.
     *
     * @return The default maximum number of connections allowed per host.
     * The default is 2 as per RFC 2616 section 8.1.4.
     */
    public int getDefaultMaxConnectionsPerHost()
    {
        return defaultMaxConnectionsPerHost;
    }

    /**
     * Sets the default maximum number of connections allowed per host.
     * @param value - the maximum number of connections
     */
    public void setDefaultMaxConnectionsPerHost(int value)
    {
        defaultMaxConnectionsPerHost = value;
    }

    /**
     * Returns linger-on-close timeout in seconds.
     * @return int - A value of <code>0</code> implies that the option is
     * disabled. A value of <code>-1</code> implies that the JRE default is
     * used.
     */
    public int getLinger()
    {
        return linger;
    }

    /**
     * Sets the linger-on-close timeout (in seconds).
     * @param linger An int. Use <code>0</code> to disable. Use <code>-1</code>
     * to rely on the JRE default. 
     */
    public void setLinger(int linger)
    {
        this.linger = linger;
    }

    /**
     * Returns a collection of HostConfigurationSettings which specify
     * connection options on a host-by-host basis.
     * 
     * @return A List of HostConfigurationSettings.
     * @see flex.messaging.services.http.HostConfigurationSettings
     */
    public List getMaxConnectionsPerHost()
    {
        return maxConnectionsPerHost;
    }

    /**
     * Sets the list of HostConfigurationSettings specifying connection
     * options on a host-by-host basis.
     * 
     * @param value The list of HostConfigurationSettings.
     * @see flex.messaging.services.http.HostConfigurationSettings
     */
    public void setMaxConnectionsPerHost(List value)
    {
        maxConnectionsPerHost = value;
    }

    /**
     * Returns the maximum number of connections allowed.
     *
     * @return The maximum number of connections allowed.
     */
    public int getMaxTotalConnections()
    {
        return maxTotalConnections;
    }

    /**
     * Sets the maximum number of connections allowed.
     * @param value - the maximum number of connections
     */
    public void setMaxTotalConnections(int value)
    {
        maxTotalConnections = value;
    }

    /**
     * Returns The size of the underlying receive buffers.
     *
     * @return The hint given to the kernel about the size of the underlying
     * buffers used by the platform for incoming network I/O.
     */
    public int getReceiveBufferSize()
    {
        return receiveBufferSize;
    }

    /**
     * Sets a suggestion for kernel from the application about the size of
     * buffers to use for the data to be received over the socket.
     * @param value - the suggested receive buffer size
     */
    public void setReceiveBufferSize(int value)
    {
        receiveBufferSize = value;
    }

    /**
     * Return the size of the underlying send buffers.
     *
     * @return The hint given to the kernel about the size of the underlying
     * buffers used by the platform for outgoing network I/O.
     */
    public int getSendBufferSize()
    {
        return sendBufferSize;
    }

    /**
     * Sets a suggestion for kernel from the application about the size of
     * buffers to use for the data to be sent over the socket.
     * @param sendBufferSize the suggested send buffer size
     */
    public void setSendBufferSize(int sendBufferSize)
    {
        this.sendBufferSize = sendBufferSize;
    }

    /**
     * Return the default socket timeout in milliseconds for waiting for data.
     *
     * @return The default socket timeout in milliseconds for waiting for data.
     * The default is 0 which is interpreted as an infinite timeout.
     */
    public int getSocketTimeout()
    {
        return socketTimeout;
    }

    /**
     * Sets the socket timeout in milliseconds for waiting for data.
     * @param value - the timeout in milliseconds
     */
    public void setSocketTimeout(int value)
    {
        socketTimeout = value;
    }

    /**
     * Returns whether a check for stale connections is to be performed.
     *
     * @return Whether a check for stale connections is to be performed.
     * The default is true.
     */
    public boolean isStaleCheckingEnabled()
    {
        return staleCheckingEnabled;
    }

    /**
     * Sets whether a check should be performed for stale connections.
     * Disabling the check may result in a slight performance improvement at
     * the risk of getting an I/O error when executing a request over a
     * connection that has been closed at the server side.

     * @param value - whether stale connection checking should be performed
     */
    public void setStaleCheckingEnabled(boolean value)
    {
        staleCheckingEnabled = value;
    }

    /**
     * Return whether Nagle's algorithm should be used for this connection.
     *
     * @return Determines whether Nagle's algorithm is to be used with this
     * connection. The default is true and Nagle's algorithm is NOT to be used
     * (i.e. TCP_NODELAY is enabled).  
     */
    public boolean isTcpNoDelay()
    {
        return tcpNoDelay;
    }

    /**
     * Sets whether Nagle's algorithm should be used for this connection. If 
     * set to true, Nagle's algorithm is to NOT be used (i.e. TCP_NODELAY
     * is enabled).
     * 
     * @param value - whether Nagle's algorithm should be used
     */
    public void setTcpNoDelay(boolean value)
    {
        tcpNoDelay = value;
    }
}
