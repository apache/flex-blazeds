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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import flex.messaging.config.ConfigMap;
import flex.messaging.messages.Message;
import flex.messaging.services.ServiceAdapter;
import flex.messaging.services.ServiceException;
import flex.messaging.util.ClassUtil;

/**
 * Adapter class for proxy services.
 */
public class HTTPProxyAdapter extends ServiceAdapter {
    // NOTE: any changes to this class should also be made to the corresponding version in the .NET.
    // The corresponding class is at src/dotNet/libs/FlexASPlib/Aspx/Proxy/ServiceProxyModule.cs


    public static final String CONTENT_TYPE_XML = "application/xml";

    public static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";

    public static final int DEFAULT_COOKIE_LIMIT = 200;

    private static final String COOKIE_LIMIT = "cookie-limit";
    private static final String ALLOW_LAX_SSL = "allow-lax-ssl";
    private static final String CONTENT_CHUNKED = "content-chunked";
    private static final String ID = "id";
    private static final String CLASS = "class";
    private static final String PROPERTIES = "properties";
    private static final String REQUEST_HEADERS = "requestHeaders";
    private static final String RESPONSE_HEADERS = "responseHeaders";

    // HTTPProxyAdapter's properties
    protected boolean allowLaxSSL = false;
    protected boolean contentChunked = false;
    protected int cookieLimit = DEFAULT_COOKIE_LIMIT;
    protected ExternalProxySettings externalProxy;
    protected HTTPConnectionManagerSettings connectionManagerSettings;

    //--------------------------------------------------------------------------
    //
    // Constructor
    //
    //--------------------------------------------------------------------------

    /**
     * Constructs an unmanaged <code>HTTPProxyAdapter</code> instance.
     */
    public HTTPProxyAdapter() {
        this(false);
    }

    /**
     * Constructs a <code>HTTPProxyAdapter</code> instance.
     *
     * @param enableManagement <code>true</code> if the <code>HTTPProxyAdapter</code> has a
     *                         corresponding MBean control for management; otherwise <code>false</code>.
     */
    public HTTPProxyAdapter(boolean enableManagement) {
        super(enableManagement);

        externalProxy = new ExternalProxySettings();
        connectionManagerSettings = new HTTPConnectionManagerSettings();
    }

    //--------------------------------------------------------------------------
    //
    // Initialize, validate, start, and stop methods.
    //
    //--------------------------------------------------------------------------

    /**
     * Initializes the <code>HTTPProxyAdapter</code> with the properties.
     *
     * <pre>
     * &lt;connection-manager&gt;
     *     &lt;cookie-policy&gt;rfc2109&lt;/cookie-policy&gt;
     *     &lt;max-total-connections&gt;100&lt;/max-total-connections&gt;
     *     &lt;default-max-connections-per-host&gt;2&lt;/default-max-connections-per-host&gt;
     *     &lt;connection-timeout&gt;0&lt;/connection-timeout&gt;
     *     &lt;socket-timeout&gt;&lt;/socket-timeout&gt;
     *     &lt;stale-checking-enabled&gt;&lt;/stale-checking-enabled&gt;
     *     &lt;send-buffer-size&gt;&lt;/send-buffer-size&gt;
     *     &lt;receive-buffer-size&gt;&lt;/receive-buffer-size&gt;
     *     &lt;tcp-no-delay&gt;true&lt;/tcp-no-delay&gt;
     *     &lt;linger&gt;-1&lt;/linger&gt;
     *     &lt;max-per-host&gt;
     *           &lt;host&gt;...&lt;/host&gt;
     *           &lt;port&gt;80&lt;/port&gt;
     *           &lt;protocol&gt;http&lt;/protocol&gt;
     *           &lt;protocol-factory class="flex.messaging.services.http.ProtocolFactory"&gt;
     *               &lt;properties&gt;...&lt;/properties&gt;
     *           &lt;/protocol-factory&gt;
     *           &lt;max-connections&gt;2&lt;/max-connections&gt;
     *           &lt;proxy&gt;
     *               &lt;host&gt;...&lt;/host&gt;
     *               &lt;port&gt;80&lt;/port&gt;
     *           &lt;/proxy&gt;
     *           &lt;local-address&gt;...&lt;/local-address&gt;
     *           &lt;virtual-host&gt;...&lt;/virtual-host&gt;
     *     &lt;/max-per-host&gt;
     *  &lt;/connection-manager&gt;
     *  &lt;cookie-limit&gt;200&lt;/cookie-limit&gt;
     *  &lt;allow-lax-ssl&gt;false&lt;/allow-lax-ssl&gt;
     *  &lt;content-chunked&gt;false&lt;/content-chunked&gt;
     *  &lt;external-proxy&gt;
     *      &lt;server&gt;...&lt;/server&gt;
     *      &lt;port&gt;80&lt;/port&gt;
     *      &lt;nt-domain&gt;...&lt;/nt-domain&gt;
     *      &lt;username&gt;...&lt;/username&gt;
     *      &lt;password&gt;...&lt;/password&gt;
     *  &lt;/external-proxy&gt;
     *  </pre>
     *
     * @param id         The id of the destination.
     * @param properties Properties for the <code>Destination</code>.
     */
    public void initialize(String id, ConfigMap properties) {
        super.initialize(id, properties);

        if (properties == null || properties.size() == 0)
            return;

        // Connection Manager
        ConfigMap conn = properties.getPropertyAsMap(HTTPConnectionManagerSettings.CONNECTION_MANAGER, null);
        if (conn != null) {
            // Cookie policy.
            if (conn.getProperty(HTTPConnectionManagerSettings.COOKIE_POLICY) != null) {
            }

            // Max Connections Total
            if (conn.getProperty(HTTPConnectionManagerSettings.MAX_TOTAL_CONNECTIONS) != null) {
            }

            // Default Max Connections Per Host
            if (conn.getProperty(HTTPConnectionManagerSettings.DEFAULT_MAX_CONNECTIONS_PER_HOST) != null) {
            }

            // Connection Timeout
            if (conn.getProperty(HTTPConnectionManagerSettings.CONNECTION_TIMEOUT) != null) {
            }

            // Socket Timeout
            if (conn.getProperty(HTTPConnectionManagerSettings.SOCKET_TIMEOUT) != null) {
            }

            // Stale Checking
            if (conn.getProperty(HTTPConnectionManagerSettings.STALE_CHECKING_ENABLED) != null) {
            }

            // Send Buffer Size
            if (conn.getProperty(HTTPConnectionManagerSettings.SEND_BUFFER_SIZE) != null) {
            }

            // Send Receive Size
            if (conn.getProperty(HTTPConnectionManagerSettings.RECEIVE_BUFFER_SIZE) != null) {
            }

            // TCP No Delay (Nagel's Algorithm)
            if (conn.getProperty(HTTPConnectionManagerSettings.TCP_NO_DELAY) != null) {
            }

            // Linger
            if (conn.getProperty(HTTPConnectionManagerSettings.LINGER) != null) {
            }

            // Max Connections Per Host
            List hosts = conn.getPropertyAsList(HTTPConnectionManagerSettings.MAX_PER_HOST, null);
            if (hosts != null) {
                List hostSettings = new ArrayList();
                Iterator it = hosts.iterator();
                while (it.hasNext()) {
                    ConfigMap maxPerHost = (ConfigMap) it.next();
                    HostConfigurationSettings hostConfig = new HostConfigurationSettings();

                    // max-connections
                    if (maxPerHost.getProperty(HostConfigurationSettings.MAX_CONNECTIONS) != null) {
                    }

                    // host
                    if (maxPerHost.getProperty(HostConfigurationSettings.HOST) != null) {
                        String host = maxPerHost.getPropertyAsString(HostConfigurationSettings.HOST, null);
                        hostConfig.setHost(host);
                        if (host != null) {
                            // port
                            int port = maxPerHost.getPropertyAsInt(HostConfigurationSettings.PORT, 0);
                            hostConfig.setPort(port);

                            // protocol-factory
                            ConfigMap factoryMap = maxPerHost.getPropertyAsMap(HostConfigurationSettings.PROTOCOL_FACFORY, null);
                            if (factoryMap != null) {
                                String className = factoryMap.getPropertyAsString(CLASS, null);
                                if (className != null) {
                                    Class factoryClass = ClassUtil.createClass(className);
                                    ProtocolFactory protocolFactory = (ProtocolFactory) ClassUtil.createDefaultInstance(factoryClass, ProtocolFactory.class);
                                    String factoryId = factoryMap.getPropertyAsString(ID, host + "_protocol_factory");
                                    ConfigMap protocolProperties = factoryMap.getPropertyAsMap(PROPERTIES, null);
                                    protocolFactory.initialize(factoryId, protocolProperties);
                                }
                            }
                            // protocol
                            else {
                                String protocol = maxPerHost.getPropertyAsString(HostConfigurationSettings.PROTOCOL, null);
                                hostConfig.setProtocol(protocol);
                            }
                        }
                    }

                    // proxy
                    ConfigMap proxy = maxPerHost.getPropertyAsMap(HostConfigurationSettings.PROXY, null);
                    if (proxy != null) {
                        // host
                        String proxyHost = proxy.getPropertyAsString(HostConfigurationSettings.HOST, null);
                        hostConfig.setProxyHost(proxyHost);
                        if (proxyHost != null) {
                            // port
                            int port = proxy.getPropertyAsInt(HostConfigurationSettings.PORT, 0);
                            hostConfig.setProxyPort(port);
                        }
                    }

                    // local-address
                    if (maxPerHost.getProperty(HostConfigurationSettings.LOCAL_ADDRESS) != null) {
                        String localAddress = maxPerHost.getPropertyAsString(HostConfigurationSettings.LOCAL_ADDRESS, null);
                        hostConfig.setLocalAddress(localAddress);
                    }

                    // virtual-host
                    if (maxPerHost.getProperty(HostConfigurationSettings.VIRTUAL_HOST) != null) {
                        String virtualHost = maxPerHost.getPropertyAsString(HostConfigurationSettings.VIRTUAL_HOST, null);
                        hostConfig.setVirtualHost(virtualHost);
                    }
                    hostSettings.add(hostConfig);
                }

                if (hostSettings.size() > 0)
                    connectionManagerSettings.setMaxConnectionsPerHost(hostSettings);
            }
        }

        // Cookie Limit
        if (properties.getProperty(COOKIE_LIMIT) != null) {
        }

        // Allow Lax SSL
        if (properties.getProperty(ALLOW_LAX_SSL) != null) {
        }

        // Content Chunked
        if (properties.getProperty(CONTENT_CHUNKED) != null) {
        }

        // External Proxy
        ConfigMap extern = properties.getPropertyAsMap(ExternalProxySettings.EXTERNAL_PROXY, null);
        if (extern != null) {
        }
    }


    //--------------------------------------------------------------------------
    //
    // Other public APIs
    //
    //--------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public Object invoke(Message msg) {
        ServiceException e = new ServiceException();
        e.setMessage("flex.messaging.services.http.HTTPProxyAdapter is no longer supported by BlazeDS");
        throw e;
    }
}
