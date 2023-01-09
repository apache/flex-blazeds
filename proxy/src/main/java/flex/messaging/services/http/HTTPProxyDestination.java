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

import flex.management.runtime.messaging.services.http.HTTPProxyDestinationControl;
import flex.messaging.Destination;
import flex.messaging.config.ConfigMap;
import flex.messaging.log.LogCategories;
import flex.messaging.services.HTTPProxyService;
import flex.messaging.services.Service;
import flex.messaging.util.ClassUtil;
import flex.messaging.util.SettingsReplaceUtil;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

/**
 * Subclass of Destination which provides HTTP Proxy-specific destination functionality.
 */
public class HTTPProxyDestination extends Destination {
    static final long serialVersionUID = -5749492520894791206L;

    /**
     * Log category for <code>HTTPProxyDestination</code>.
     **/
    public static final String LOG_CATEGORY = LogCategories.SERVICE_HTTP;

    // ConfigMap keys from XML based services configuration.
    private static final String URL = "url";
    private static final String WSDL = "wsdl";
    private static final String DYNAMIC_URL = "dynamic-url";
    private static final String SOAP = "soap";
    private static final String REMOTE_USERNAME = "remote-username";
    private static final String REMOTE_PASSWORD = "remote-password";
    private static final String USE_CUSTOM_AUTH = "use-custom-auth";
    private static final String ID = "id";
    private static final String CLASS = "class";
    private static final String PROPERTIES = "properties";

    // HTTPProxyDestination's properties
    protected String defaultUrl;
    protected final List dynamicUrls;
    protected String remoteUsername;
    protected String remotePassword;
    protected boolean useCustomAuthentication;
    protected ProtocolFactory protocolFactory;

    // HTTPProxyDestination internal
    protected boolean allowsDynamicAuthentication;
    protected boolean dynamicParsed;
    protected String parsedDefaultUrl;
    protected List parsedDynamicUrls;

    private HTTPProxyDestinationControl controller;

    //--------------------------------------------------------------------------
    //
    // Constructor
    //
    //--------------------------------------------------------------------------

    /**
     * Constructs an unmanaged <code>HTTPProxyDestination</code> instance.
     */
    public HTTPProxyDestination() {
        this(false);
    }

    /**
     * Constructs a <code>HTTPProxyDestination</code> with the indicated management.
     *
     * @param enableManagement <code>true</code> if the <code>HTTPProxyDestination</code>
     *                         is manageable; otherwise <code>false</code>.
     */
    public HTTPProxyDestination(boolean enableManagement) {
        super(enableManagement);

        dynamicUrls = new ArrayList();
    }

    //--------------------------------------------------------------------------
    //
    // Initialize, validate, start, and stop methods.
    //
    //--------------------------------------------------------------------------

    /**
     * Initializes the <code>HTTPProxyDestination</code> with the properties.
     *
     * <pre>
     * &lt;url&gt;...&lt;/url&gt; (HTTP)
     *   or
     * &lt;wsdl&gt;...&lt;/wsdl&gt; (SOAP)
     *
     * &lt;dynamic-url&gt;...&lt;/dynamic-url&gt;* (HTTP)
     *   or
     * &lt;soap&gt;...&lt;/soap&gt;* (SOAP)
     *
     * &lt;remote-username&gt;...&lt;/remote-username&gt;
     * &lt;remote-password&gt;...&lt;/remote-password&gt;
     * &lt;use-custom-authentication&gt;true&lt;/use-custom-authentication&gt;
     *
     * &lt;protocol-factory class="flex.messaging.services.http.ProtocolFactory"&gt;
     *     &lt;properties&gt;...&lt;/properties&gt;
     * &lt;/protocol-factory&gt;
     * </pre>
     *
     * @param id         The id of the destination.
     * @param properties Properties for the <code>HTTPProxyDestination</code>.
     */
    public void initialize(String id, ConfigMap properties) {
        super.initialize(id, properties);

        if (properties == null || properties.size() == 0)
            return;

        // Custom protocol-factory
        ConfigMap factoryMap = properties.getPropertyAsMap(HostConfigurationSettings.PROTOCOL_FACFORY, null);
        if (factoryMap != null) {
            String className = factoryMap.getPropertyAsString(CLASS, null);
            if (className != null) {
                Class factoryClass = ClassUtil.createClass(className);
                protocolFactory = (ProtocolFactory) ClassUtil.createDefaultInstance(factoryClass, ProtocolFactory.class);
                String factoryId = factoryMap.getPropertyAsString(ID, getId() + "_protocol_factory");
                ConfigMap protocolProperties = factoryMap.getPropertyAsMap(PROPERTIES, null);
                protocolFactory.initialize(factoryId, protocolProperties);
            }
        }

        // Default URL or WSDL
        defaultUrl = properties.getPropertyAsString(URL, null);

        if (defaultUrl == null) {
            defaultUrl = properties.getPropertyAsString(WSDL, null);
        } else {
            properties.allowProperty(WSDL);
        }

        // Dynamic URL or SOAP Endpoint patterns
        List dynUrls = properties.getPropertyAsList(DYNAMIC_URL, null);
        if (dynUrls != null) {
            dynamicUrls.addAll(dynUrls);
        }

        List soapUrls = properties.getPropertyAsList(SOAP, new ArrayList());
        if (soapUrls != null) {
            dynamicUrls.addAll(soapUrls);
        }

        remoteUsername = properties.getPropertyAsString(REMOTE_USERNAME, null);
        remotePassword = properties.getPropertyAsString(REMOTE_PASSWORD, null);
        useCustomAuthentication = properties.getPropertyAsBoolean(USE_CUSTOM_AUTH, true);
    }

    //--------------------------------------------------------------------------
    //
    // Public Getters and Setters for Destination properties
    //
    //--------------------------------------------------------------------------

    /**
     * Returns the <code>url</code> (or <code>wsdl</code> if using the
     * SOAP) property.
     *
     * @return The <code>url</code> or <code>wsdl</code> property.
     */
    public String getDefaultUrl() {
        return defaultUrl;
    }

    /**
     * Sets the <code>url</code> or <code>wsdl</code> property.
     *
     * @param defaultUrl The <code>url</code> or <code>wsdl</code> property.
     */
    public void setDefaultUrl(String defaultUrl) {
        this.defaultUrl = defaultUrl;
    }

    /**
     * Returns the list of <code>dynamic-url</code> (or <code>soap</code>
     * if using SOAP) properties.
     *
     * @return The list of <code>dynamic-url</code> or <code>soap</code>
     * properties.
     */
    public List getDynamicUrls() {
        return dynamicUrls;
    }

    /**
     * Adds a <code>dynamic-url</code> or <code>soap</code> property.
     * The developer configures a list of dynamic URLs that are
     * allowed for Proxy Service destinations. The dynamic URL
     * may contain * and ? wildcards, and must start with either
     * &quot;http://&quot; or &quot;https://&quot;. Dynamic URLs
     * are compared in a case insensitive manner.
     *
     * @param dynamicUrl - A wildcard pattern used to match dynamic URLs
     */
    public void addDynamicUrl(String dynamicUrl) {
        if (dynamicUrl != null) {
            dynamicUrls.add(dynamicUrl);
            dynamicParsed = false;

            // FIXME: Why do we enforce this? What about {context.root} based relative URLs?
            /*
            if (u.startsWith("http://") || u.startsWith("https://"))
            {
                Don't convert to chars here, we need to translate {context.root} when we know it later...
                char[] urlChars = u.toLowerCase().toCharArray();
                parsedDynamicUrls.add(u);
                parsed = false;
            }
            else
            {
                throw new MessageException("Dynamic URL patterns must start with 'http://' or 'https://'");
            }
            */
        }
    }

    /**
     * Adds a list of <code>dynamic-url</code> or <code>soap</code> properties
     * to the existing list.
     *
     * @param dynamicUrls A list of <code>dynamic-url</code> or <code>soap</code>
     *                    properties.
     */
    public void addDynamicUrls(List dynamicUrls) {
        this.dynamicUrls.addAll(dynamicUrls);
        dynamicParsed = false;
    }

    /**
     * Returns the <code>protocol-factory</code> property. A ProtocolFactory
     * implementation allows the developer to customize how the HTTP Proxy
     * Service communicates with a 3rd party endpoint.
     *
     * @return The <code>protocol-factory</code> property.
     */
    public ProtocolFactory getProtocolFactory() {
        return protocolFactory;
    }

    /**
     * Sets the <code>protocol-factory</code> property.
     *
     * @param protocolFactory The <code>protocol-factory</code> property.
     */
    public void setProtocolFactory(ProtocolFactory protocolFactory) {
        this.protocolFactory = protocolFactory;
    }

    /**
     * Returns the <code>remote-password</code> property.
     *
     * @return The <code>remote-password</code> property.
     */
    public String getRemotePassword() {
        return remotePassword;
    }

    /**
     * Sets the <code>remote-password</code> property.
     *
     * @param remotePassword The <code>remote-password</code> property.
     */
    public void setRemotePassword(String remotePassword) {
        this.remotePassword = remotePassword;
    }

    /**
     * Gets the <code>remote-username</code> property.
     *
     * @return The <code>remote-username</code> property.
     */
    public String getRemoteUsername() {
        return remoteUsername;
    }

    /**
     * Sets the <code>remote-username</code> property.
     *
     * @param remoteUsername The <code>remote-username</code> property.
     */
    public void setRemoteUsername(String remoteUsername) {
        this.remoteUsername = remoteUsername;
    }

    /**
     * Casts the <code>Service</code> into <code>HTTPProxyService</code>
     * and calls super.setService.
     *
     * @param service The HTTP proxy service.
     */
    public void setService(Service service) {
        HTTPProxyService proxyService = (HTTPProxyService) service;
        super.setService(proxyService);
    }

    /**
     * Returns the <code>use-custom-auth</code> property.
     *
     * @return <code>true</code> if use-custom-auth is enabled;
     * otherwise <code>false</code>.
     */
    public boolean isUseCustomAuthentication() {
        return useCustomAuthentication;
    }

    /**
     * Sets the <code>use-custom-auth</code> property.
     *
     * @param useCustomAuthentication The <code>use-custom-auth</code> property.
     */
    public void setUseCustomAuthentication(boolean useCustomAuthentication) {
        this.useCustomAuthentication = useCustomAuthentication;
    }

    //--------------------------------------------------------------------------
    //
    // Other public APIs
    //
    //--------------------------------------------------------------------------

    /**
     * This method replaces the dynamic tokens of the default url with the specified
     * values and returns the resulting url.
     *
     * @param contextPath    The context path to be used in dynamic url replacement.
     * @param serverName     The server name to be used in dynamic url replacement.
     * @param serverPort     The server port to be used in dynamic url replacement.
     * @param serverProtocol The server protocol to be used in dynamic url replacement.
     * @return The fully parsed url where the dynamic tokens have been replaced.
     */
    public String getParsedDefaultUrl(String contextPath, String serverName, String serverPort, String serverProtocol) {
        if (defaultUrl != null) {
            parsedDefaultUrl = SettingsReplaceUtil.replaceAllTokensGivenServerName(defaultUrl, contextPath, serverName, serverPort, serverProtocol);
        }

        return parsedDefaultUrl;
    }

    /**
     * This method replaces all the dynamic tokens of dynamic urls using the specified
     * context path, when necessary.
     *
     * @param contextPath The context path to be used in dynamic url replacement.
     * @return List List of fully parsed urls where the dynamic tokens have been replaced.
     */
    public List getParsedDynamicUrls(String contextPath) {
        if (!dynamicParsed || parsedDynamicUrls == null) {
            parseDynamicUrls(this, contextPath);
        }

        return parsedDynamicUrls;
    }

    private static void parseDynamicUrls(HTTPProxyDestination dest, String contextPath) {
        List dynamicUrls = dest.getDynamicUrls();

        dest.parsedDynamicUrls = new ArrayList();
        dest.allowsDynamicAuthentication = true;

        String lastDomainAndPort = null;
        boolean computeAuth = true;

        Set parsedUrls = SettingsReplaceUtil.replaceAllTokensCalculateServerName(dynamicUrls, contextPath);
        for (Iterator iter = parsedUrls.iterator(); iter.hasNext(); ) {
            String url = (String) iter.next();
            dest.parsedDynamicUrls.add(url.toCharArray());
            if (computeAuth) {
                boolean fail = false;
                try {
                    URL urlObj = new URL(url);
                    String host = urlObj.getHost();
                    if (host.indexOf('*') > -1) {
                        fail = true;
                    } else {
                        String domainAndPort = host + ":" + urlObj.getPort();
                        if (lastDomainAndPort != null && !lastDomainAndPort.equalsIgnoreCase(domainAndPort))
                            fail = true;
                        lastDomainAndPort = domainAndPort;
                    }
                } catch (MalformedURLException e) {
                    //probably due to the port being *
                    fail = true;
                }

                if (fail) {
                    computeAuth = false;
                    dest.allowsDynamicAuthentication = false;
                }
            }
        }

        dest.dynamicParsed = true;
    }

    /**
     * Returns whether dynamic authentication is allowed.
     *
     * @return Whether dynamic authentication is allowed.
     */
    public boolean allowsDynamicAuthentication() {
        if (!dynamicParsed) {
            //not using proxy exception because this is really a coding issue
            throw new RuntimeException("Cannot compute authentication if dynamic urls aren't parsed");
        }
        return allowsDynamicAuthentication;
    }

    //--------------------------------------------------------------------------
    //
    // Protected/private APIs
    //
    //--------------------------------------------------------------------------

    /**
     * Returns the log category of the <code>HTTPProxyDestination</code>.
     *
     * @return The log category of the component.
     */
    protected String getLogCategory() {
        return LOG_CATEGORY;
    }

    /**
     * Invoked automatically to allow the <code>HTTPProxyDestination</code> to setup its corresponding
     * MBean control.
     *
     * @param service The <code>Service</code> that manages this <code>HTTPProxyDestination</code>.
     */
    protected void setupDestinationControl(Service service) {
        controller = new HTTPProxyDestinationControl(this, service.getControl());
        controller.register();
        setControl(controller);
    }
}
