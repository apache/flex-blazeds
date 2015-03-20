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

import flex.management.runtime.messaging.services.http.HTTPProxyAdapterControl;
import flex.messaging.Destination;
import flex.messaging.FlexContext;
import flex.messaging.MessageException;
import flex.messaging.config.ConfigMap;
import flex.messaging.messages.AcknowledgeMessage;
import flex.messaging.messages.HTTPMessage;
import flex.messaging.messages.Message;
import flex.messaging.messages.SOAPMessage;
import flex.messaging.services.ServiceAdapter;
import flex.messaging.services.http.proxy.AccessFilter;
import flex.messaging.services.http.proxy.ErrorFilter;
import flex.messaging.services.http.proxy.ProxyContext;
import flex.messaging.services.http.proxy.ProxyContextFilter;
import flex.messaging.services.http.proxy.ProxyFilter;
import flex.messaging.services.http.proxy.RequestFilter;
import flex.messaging.services.http.proxy.ResponseFilter;
import flex.messaging.services.http.proxy.SecurityFilter;
import flex.messaging.services.http.proxy.Target;
import flex.messaging.util.ClassUtil;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.params.HostParams;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.protocol.Protocol;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Adapter class for proxy services.
 */
public class HTTPProxyAdapter extends ServiceAdapter
{
    // NOTE: any changes to this class should also be made to the corresponding version in the .NET.
    // The corresponding class is at src/dotNet/libs/FlexASPlib/Aspx/Proxy/ServiceProxyModule.cs

    /** @exclude **/
    public static final String CONTENT_TYPE_XML = "application/xml";
    /** @exclude **/
    public static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";
    /** @exclude **/
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

    // HTTPProxyAdapter internal
    protected HttpConnectionManager connectionManager;
    protected HttpConnectionManagerParams connectionParams;
    protected ProxyFilter filterChain;
    protected UsernamePasswordCredentials proxyCredentials;

    private HTTPProxyAdapterControl controller;

    //--------------------------------------------------------------------------
    //
    // Constructor
    //
    //--------------------------------------------------------------------------

    /**
     * Constructs an unmanaged <code>HTTPProxyAdapter</code> instance.
     */
    public HTTPProxyAdapter()
    {
        this(false);
    }

    /**
     * Constructs a <code>HTTPProxyAdapter</code> instance.
     *
     * @param enableManagement <code>true</code> if the <code>HTTPProxyAdapter</code> has a
     *                         corresponding MBean control for management; otherwise <code>false</code>.
     */
    public HTTPProxyAdapter(boolean enableManagement)
    {
        super(enableManagement);

        createFilterChain();
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
     * <p/>
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
    public void initialize(String id, ConfigMap properties)
    {
        super.initialize(id, properties);

        if (properties == null || properties.size() == 0)
            return;

        // Connection Manager
        ConfigMap conn = properties.getPropertyAsMap(HTTPConnectionManagerSettings.CONNECTION_MANAGER, null);
        if (conn != null)
        {
            // Cookie policy.
            if (conn.getProperty(HTTPConnectionManagerSettings.COOKIE_POLICY) != null)
            {
                connectionManagerSettings.setCookiePolicy(conn.getPropertyAsString(HTTPConnectionManagerSettings.COOKIE_POLICY, 
                        CookiePolicy.DEFAULT));
            }

            // Max Connections Total
            if (conn.getProperty(HTTPConnectionManagerSettings.MAX_TOTAL_CONNECTIONS) != null)
            {
                int maxTotal = conn.getPropertyAsInt(HTTPConnectionManagerSettings.MAX_TOTAL_CONNECTIONS,
                        MultiThreadedHttpConnectionManager.DEFAULT_MAX_TOTAL_CONNECTIONS);
                connectionManagerSettings.setMaxTotalConnections(maxTotal);
            }

            // Default Max Connections Per Host
            int defaultMaxConnsPerHost = MultiThreadedHttpConnectionManager.DEFAULT_MAX_HOST_CONNECTIONS;
            if (conn.getProperty(HTTPConnectionManagerSettings.DEFAULT_MAX_CONNECTIONS_PER_HOST) != null)
            {
                defaultMaxConnsPerHost = conn.getPropertyAsInt(HTTPConnectionManagerSettings.DEFAULT_MAX_CONNECTIONS_PER_HOST,
                        MultiThreadedHttpConnectionManager.DEFAULT_MAX_HOST_CONNECTIONS);
                connectionManagerSettings.setDefaultMaxConnectionsPerHost(defaultMaxConnsPerHost);
            }

            // Connection Timeout
            if (conn.getProperty(HTTPConnectionManagerSettings.CONNECTION_TIMEOUT) != null)
            {
                int timeout = conn.getPropertyAsInt(HTTPConnectionManagerSettings.CONNECTION_TIMEOUT, 0);
                if (timeout >= 0)
                    connectionManagerSettings.setConnectionTimeout(timeout);
            }

            // Socket Timeout
            if (conn.getProperty(HTTPConnectionManagerSettings.SOCKET_TIMEOUT) != null)
            {
                int timeout = conn.getPropertyAsInt(HTTPConnectionManagerSettings.SOCKET_TIMEOUT, 0);
                if (timeout >= 0)
                    connectionManagerSettings.setSocketTimeout(timeout);
            }

            // Stale Checking
            if (conn.getProperty(HTTPConnectionManagerSettings.STALE_CHECKING_ENABLED) != null)
            {
                boolean staleCheck = conn.getPropertyAsBoolean(HTTPConnectionManagerSettings.STALE_CHECKING_ENABLED, true);
                connectionManagerSettings.setStaleCheckingEnabled(staleCheck);
            }

            // Send Buffer Size
            if (conn.getProperty(HTTPConnectionManagerSettings.SEND_BUFFER_SIZE) != null)
            {
                int bufferSize = conn.getPropertyAsInt(HTTPConnectionManagerSettings.SEND_BUFFER_SIZE, 0);
                if (bufferSize > 0)
                    connectionManagerSettings.setSendBufferSize(bufferSize);
            }

            // Send Receive Size
            if (conn.getProperty(HTTPConnectionManagerSettings.RECEIVE_BUFFER_SIZE) != null)
            {
                int bufferSize = conn.getPropertyAsInt(HTTPConnectionManagerSettings.RECEIVE_BUFFER_SIZE, 0);
                if (bufferSize > 0)
                    connectionManagerSettings.setReceiveBufferSize(bufferSize);
            }

            // TCP No Delay (Nagel's Algorithm)
            if (conn.getProperty(HTTPConnectionManagerSettings.TCP_NO_DELAY) != null)
            {
                boolean noNagel = conn.getPropertyAsBoolean(HTTPConnectionManagerSettings.TCP_NO_DELAY, true);
                connectionManagerSettings.setTcpNoDelay(noNagel);
            }

            // Linger
            if (conn.getProperty(HTTPConnectionManagerSettings.LINGER) != null)
            {
                int linger = conn.getPropertyAsInt(HTTPConnectionManagerSettings.LINGER, -1);
                connectionManagerSettings.setLinger(linger);
            }

            // Max Connections Per Host
            List hosts = conn.getPropertyAsList(HTTPConnectionManagerSettings.MAX_PER_HOST, null);
            if (hosts != null)
            {
                List hostSettings = new ArrayList();
                Iterator it = hosts.iterator();
                while (it.hasNext())
                {
                    ConfigMap maxPerHost = (ConfigMap) it.next();
                    HostConfigurationSettings hostConfig = new HostConfigurationSettings();

                    // max-connections
                    if (maxPerHost.getProperty(HostConfigurationSettings.MAX_CONNECTIONS) != null)
                    {
                        int maxConn = maxPerHost.getPropertyAsInt(HostConfigurationSettings.MAX_CONNECTIONS,
                                defaultMaxConnsPerHost);
                        hostConfig.setMaximumConnections(maxConn);
                    }

                    // host
                    if (maxPerHost.getProperty(HostConfigurationSettings.HOST) != null)
                    {
                        String host = maxPerHost.getPropertyAsString(HostConfigurationSettings.HOST, null);
                        hostConfig.setHost(host);
                        if (host != null)
                        {
                            // port
                            int port = maxPerHost.getPropertyAsInt(HostConfigurationSettings.PORT, 0);
                            hostConfig.setPort(port);

                            // protocol-factory
                            ConfigMap factoryMap = maxPerHost.getPropertyAsMap(HostConfigurationSettings.PROTOCOL_FACFORY, null);
                            if (factoryMap != null)
                            {
                                String className = factoryMap.getPropertyAsString(CLASS, null);
                                if (className != null)
                                {
                                    Class factoryClass = ClassUtil.createClass(className);
                                    ProtocolFactory protocolFactory = (ProtocolFactory)ClassUtil.createDefaultInstance(factoryClass, ProtocolFactory.class);
                                    String factoryId = factoryMap.getPropertyAsString(ID, host + "_protocol_factory");
                                    ConfigMap protocolProperties = factoryMap.getPropertyAsMap(PROPERTIES, null);
                                    protocolFactory.initialize(factoryId, protocolProperties);
                                }
                            }
                            // protocol
                            else
                            {
                                String protocol = maxPerHost.getPropertyAsString(HostConfigurationSettings.PROTOCOL, null);
                                hostConfig.setProtocol(protocol);
                            }
                        }
                    }

                    // proxy
                    ConfigMap proxy = maxPerHost.getPropertyAsMap(HostConfigurationSettings.PROXY, null);
                    if (proxy != null)
                    {
                        // host
                        String proxyHost = proxy.getPropertyAsString(HostConfigurationSettings.HOST, null);
                        hostConfig.setProxyHost(proxyHost);
                        if (proxyHost != null)
                        {
                            // port
                            int port = proxy.getPropertyAsInt(HostConfigurationSettings.PORT, 0);
                            hostConfig.setProxyPort(port);
                        }
                    }

                    // local-address
                    if (maxPerHost.getProperty(HostConfigurationSettings.LOCAL_ADDRESS) != null)
                    {
                        String localAddress = maxPerHost.getPropertyAsString(HostConfigurationSettings.LOCAL_ADDRESS, null);
                        hostConfig.setLocalAddress(localAddress);
                    }

                    // virtual-host
                    if (maxPerHost.getProperty(HostConfigurationSettings.VIRTUAL_HOST) != null)
                    {
                        String virtualHost = maxPerHost.getPropertyAsString(HostConfigurationSettings.VIRTUAL_HOST, null);
                        hostConfig.setVirtualHost(virtualHost);
                    }
                    hostSettings.add(hostConfig);
                }

                if (hostSettings.size() > 0)
                    connectionManagerSettings.setMaxConnectionsPerHost(hostSettings);
            }
            setConnectionManagerSettings(connectionManagerSettings);
        }

        // Cookie Limit
        if (properties.getProperty(COOKIE_LIMIT) != null)
        {
            int cl = properties.getPropertyAsInt(COOKIE_LIMIT, DEFAULT_COOKIE_LIMIT);
            setCookieLimit(cl);
        }

        // Allow Lax SSL
        if (properties.getProperty(ALLOW_LAX_SSL) != null)
        {
            boolean lax = properties.getPropertyAsBoolean(ALLOW_LAX_SSL, false);
            setAllowLaxSSL(lax);
        }

        // Content Chunked
        if (properties.getProperty(CONTENT_CHUNKED) != null)
        {
            boolean ch = properties.getPropertyAsBoolean(CONTENT_CHUNKED, false);
            setContentChunked(ch);
        }

        // External Proxy
        ConfigMap extern = properties.getPropertyAsMap(ExternalProxySettings.EXTERNAL_PROXY, null);
        if (extern != null)
        {
            ExternalProxySettings proxy = new ExternalProxySettings();

            String proxyServer = extern.getPropertyAsString(ExternalProxySettings.SERVER, null);
            proxy.setProxyServer(proxyServer);
            int proxyPort = extern.getPropertyAsInt(ExternalProxySettings.PORT, ExternalProxySettings.DEFAULT_PROXY_PORT);
            proxy.setProxyPort(proxyPort);
            String ntdomain = extern.getPropertyAsString(ExternalProxySettings.NT_DOMAIN, null);
            proxy.setNTDomain(ntdomain);
            String username = extern.getPropertyAsString(ExternalProxySettings.USERNAME, null);
            proxy.setUsername(username);
            String password = extern.getPropertyAsString(ExternalProxySettings.PASSWORD, null);
            proxy.setPassword(password);

            setExternalProxySettings(proxy);
        }
    }

    //--------------------------------------------------------------------------
    //
    // Public Getters and Setters for Destination properties
    //
    //--------------------------------------------------------------------------

    /**
     * Returns <code>allow-lax-ssl</code> property.
     *
     * @return <code>true</code> if <code>allow-lax-ssl</code> property is
     *         <code>true</code>; otherwise <code>false</code>.
     */
    public boolean isAllowLaxSSL()
    {
        return allowLaxSSL;
    }

    /**
     * Sets <code>allow-lax-ssl</code> property which determines if self-signed
     * certificates are allowed; should not be used in production.
     * Default <code>false</code>.
     *
     * @param allowLaxSSL Whether lax SSL should be allowed.
     */
    public void setAllowLaxSSL(boolean allowLaxSSL)
    {
        this.allowLaxSSL = allowLaxSSL;
    }

    /**
     * Returns the <code>content-chunked</code> property.
     *
     * @return <code>true</code> if <code>content-chunked</code> property is
     *         <code>true</code>; otherwise <code>false</code>.
     */
    public boolean isContentChunked()
    {
        return contentChunked;
    }

    /**
     * Sets the <code>content-chunked</code> property. Default <code>false</code>.
     *
     * @param contentChunked The <code>content-chunked</code> property.
     */
    public void setContentChunked(boolean contentChunked)
    {
        this.contentChunked = contentChunked;
    }

    /**
     * Returns the <code>cookie-limit</code> property.
     *
     * @return The <code>cookie-limit</code> property.
     */
    public int getCookieLimit()
    {
        return cookieLimit;
    }

    /**
     * Sets the <code>cookie-limit</code> property. Default 200.
     *
     * @param cookieLimit The cookie limit for the proxy.
     */
    public void setCookieLimit(int cookieLimit)
    {
        this.cookieLimit = cookieLimit;
    }

    /**
     * Casts the <code>Destination</code> into <code>HTTPProxyDestination</code>
     * and calls super.setDestination.
     * @param destination The HTTP proxy destination.
     */
    public void setDestination(Destination destination)
    {
        Destination dest = (HTTPProxyDestination) destination;
        super.setDestination(dest);
    }

    /**
     * Returns <code>ExternalProxySettings</code>.
     *
     * @return the <code>ExternalProxySettings</code>
     */
    public ExternalProxySettings getExternalProxySettings()
    {
        return externalProxy;
    }

    /**
     * Sets <code>ExternalProxySettings</code>.
     *
     * @param externalProxy The external proxy settings.
     */
    public void setExternalProxySettings(ExternalProxySettings externalProxy)
    {
        this.externalProxy = externalProxy;
        initExternalProxy(externalProxy);
    }

    /**
     * Returns <code>HTTPConnectionManagerSettings</code>.
     *
     * @return the <code>HTTPConnectionManagerSettings</code>
     */
    public HTTPConnectionManagerSettings getConnectionManagerSettings()
    {
        return connectionManagerSettings;
    }

    /**
     * Sets <code>HTTPConnectionManagerSettings</code>.
     *
     * @param connectionManagerSettings The connection manager settings.
     */
    public void setConnectionManagerSettings(HTTPConnectionManagerSettings connectionManagerSettings)
    {
        this.connectionManagerSettings = connectionManagerSettings;
        initHttpConnectionManagerParams(connectionManagerSettings);
        connectionManager = new MultiThreadedHttpConnectionManager();
        connectionManager.setParams(connectionParams);
    }

    //--------------------------------------------------------------------------
    //
    // Other public APIs
    //
    //--------------------------------------------------------------------------

    /** {@inheritDoc} */
    public Object invoke(Message msg)
    {
        HTTPMessage message = (HTTPMessage) msg;

        ProxyContext context = new ProxyContext();

        // SOAPMessages should be sent through the SOAPProxyAdapter, but
        // the default destination may be just to the HTTPProxyAdapter.
        // We'll update the context just in case....
        if (message instanceof SOAPMessage)
            context.setSoapRequest(true);
        else
            context.setSoapRequest(false);

        setupContext(context, message);

        try
        {
            filterChain.invoke(context);

            //TODO: Do we want a return type that encapsulates the response data?

            // OUTPUT
            AcknowledgeMessage ack = new AcknowledgeMessage();
            ack.setBody(context.getResponse());
            ack.setHeader(Message.STATUS_CODE_HEADER, context.getStatusCode());

            if (context.getRecordHeaders())
            {
                ack.setHeader(REQUEST_HEADERS, context.getRequestHeaders());
                ack.setHeader(RESPONSE_HEADERS, context.getResponseHeaders());
            }

            return ack;
        }
        catch (MessageException ex)
        {
            throw ex;
        }
        catch (Throwable t)
        {
            // this should never happen- ErrorFilter should catch everything
            t.printStackTrace();
            throw new MessageException(t.toString());
        }
    }

    //--------------------------------------------------------------------------
    //
    // Protected/private APIs
    //
    //--------------------------------------------------------------------------

    protected void setupContext(ProxyContext context, HTTPMessage message)
    {
        Target target = new Target();
        context.setTarget(target);

        context.setExternalProxySettings(externalProxy);
        context.setProxyCredentials(proxyCredentials);
        context.setConnectionManager(connectionManager);
        context.setAllowLaxSSL(allowLaxSSL);
        context.setContentChunked(contentChunked);
        context.setRecordHeaders(message.getRecordHeaders());
        context.setCookieLimit(cookieLimit);
        context.setHttpRequest(FlexContext.getHttpRequest() != null);

        //TODO: QUESTION: Pete, Send HTTPHeaders as real headers

        // INPUT
        String url = message.getUrl();
        context.setUrl(url);

        Map httpHeaders = message.getHttpHeaders();
        context.setHeaders(httpHeaders);

        String method = message.getMethod();
        context.setMethod(method);

        String contentType = message.getContentType();
        context.setContentType(contentType);

        Object body = message.getBody();
        context.setBody(body);

        target.setRemoteUsername(message.getRemoteUsername());
        target.setRemotePassword(message.getRemotePassword());

        HTTPProxyDestination destination = (HTTPProxyDestination)getDestination();
        target.setUseCustomAuthentication(destination.isUseCustomAuthentication());

        if (destination.getProtocolFactory() != null)
        {
            ProtocolFactory protocolFactory = destination.getProtocolFactory();
            context.setProtocol(protocolFactory.getProtocol());
        }
    }

    /**
     * Invoked automatically to allow the <code>HTTPProxyAdapter</code> to setup its corresponding
     * MBean control.
     *
     * @param destination The <code>Destination</code> that manages this <code>HTTPProxyAdapter</code>.
     */
    protected void setupAdapterControl(Destination destination)
    {
        controller = new HTTPProxyAdapterControl(this, destination.getControl());
        controller.register();
        setControl(controller);
    }

    /**
     * Create default filter chain or return current one if already present.
     */
    private ProxyFilter createFilterChain()
    {
        if (filterChain == null)
        {
            // catch-all error filter
            ErrorFilter errorFilter = new ErrorFilter();
            // check proxy access
            AccessFilter accessFilter = new AccessFilter();
            // set up ProxyContext
            ProxyContextFilter contextFilter = new ProxyContextFilter();
            // sends out response after further filters
            ResponseFilter responseFilter = new ResponseFilter();
            // deals with credentials
            SecurityFilter securityFilter = new SecurityFilter();
            // sends out the request
            RequestFilter requestFilter = new RequestFilter();

            errorFilter.setNext(accessFilter);
            accessFilter.setNext(contextFilter);
            contextFilter.setNext(responseFilter);
            responseFilter.setNext(securityFilter);
            securityFilter.setNext(requestFilter);

            filterChain = errorFilter;
        }
        return filterChain;
    }

    private void initExternalProxy(ExternalProxySettings ep)
    {
        if (externalProxy != null)
        {

            String proxyServer = externalProxy.getProxyServer();
            String proxyUsername = externalProxy.getUsername();

            if (proxyUsername != null)
            {
                String proxyPassword = externalProxy.getPassword();
                String proxyDomain = externalProxy.getNTDomain();
                if (proxyDomain != null)
                {
                    proxyCredentials = new NTCredentials(proxyUsername, proxyPassword, proxyServer, proxyDomain);
                }
                else
                {
                    proxyCredentials = new UsernamePasswordCredentials(proxyUsername, proxyPassword);
                }
            }
        }
    }

    private void initHttpConnectionManagerParams(HTTPConnectionManagerSettings settings)
    {
        connectionParams = new HttpConnectionManagerParams();
        connectionParams.setMaxTotalConnections(settings.getMaxTotalConnections());
        connectionParams.setDefaultMaxConnectionsPerHost(settings.getDefaultMaxConnectionsPerHost());

        if (!settings.getCookiePolicy().equals(CookiePolicy.DEFAULT))
        {
            HttpClientParams httpClientParams = (HttpClientParams) connectionParams.getDefaults();
            httpClientParams.setCookiePolicy(settings.getCookiePolicy()); 
        }

        if (settings.getConnectionTimeout() >= 0)
            connectionParams.setConnectionTimeout(settings.getConnectionTimeout());

        if (settings.getSocketTimeout() >= 0)
            connectionParams.setSoTimeout(settings.getSocketTimeout());

        connectionParams.setStaleCheckingEnabled(settings.isStaleCheckingEnabled());

        if (settings.getSendBufferSize() > 0)
            connectionParams.setSendBufferSize(settings.getSendBufferSize());

        if (settings.getReceiveBufferSize() > 0)
            connectionParams.setReceiveBufferSize(settings.getReceiveBufferSize());

        connectionParams.setTcpNoDelay(settings.isTcpNoDelay());
        connectionParams.setLinger(settings.getLinger());

        if (settings.getMaxConnectionsPerHost() != null)
        {
            Iterator it = settings.getMaxConnectionsPerHost().iterator();
            while (it.hasNext())
            {
                HostConfigurationSettings hcs = (HostConfigurationSettings)it.next();
                HostConfiguration hostConfig = new HostConfiguration();

                if (hcs.getProtocol() != null)
                {
                    Protocol protocol = Protocol.getProtocol(hcs.getProtocol());
                    hostConfig.setHost(hcs.getHost(), hcs.getPort(), protocol);
                }
                else if (hcs.getProtocolFactory() != null)
                {
                    Protocol protocol = hcs.getProtocolFactory().getProtocol();
                    if (hcs.getPort() > 0)
                        hostConfig.setHost(hcs.getHost(), hcs.getPort(), protocol);
                    else
                        hostConfig.setHost(hcs.getHost(), protocol.getDefaultPort(), protocol);
                }
                else
                {
                    if (hcs.getPort() > 0)
                        hostConfig.setHost(hcs.getHost(), hcs.getPort());
                    else
                        hostConfig.setHost(hcs.getHost());
                }

                if (hcs.getVirtualHost() != null)
                {
                    HostParams params = hostConfig.getParams();
                    if (params != null)
                        params.setVirtualHost(hcs.getVirtualHost());
                }

                if (hcs.getProxyHost() != null)
                {
                    hostConfig.setProxy(hcs.getProxyHost(), hcs.getProxyPort());
                }

                try
                {
                    InetAddress addr = InetAddress.getByName(hcs.getLocalAddress());
                    hostConfig.setLocalAddress(addr);
                }
                catch (UnknownHostException ex)
                {
                }
                connectionParams.setMaxConnectionsPerHost(hostConfig, hcs.getMaximumConnections());
            }
        }
    }
}
