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
package flex.messaging.endpoints;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import flex.management.ManageableComponent;
import flex.management.runtime.messaging.MessageBrokerControl;
import flex.management.runtime.messaging.endpoints.EndpointControl;
import flex.messaging.FlexContext;
import flex.messaging.FlexSession;
import flex.messaging.MessageBroker;
import flex.messaging.MessageException;
import flex.messaging.Server;
import flex.messaging.client.FlexClient;
import flex.messaging.client.FlexClientOutboundQueueProcessor;
import flex.messaging.client.FlushResult;
import flex.messaging.client.PollFlushResult;
import flex.messaging.client.UserAgentSettings;
import flex.messaging.config.ChannelSettings;
import flex.messaging.config.ConfigMap;
import flex.messaging.config.ConfigurationConstants;
import flex.messaging.config.ConfigurationException;
import flex.messaging.config.SecurityConstraint;
import flex.messaging.io.ClassAliasRegistry;
import flex.messaging.io.SerializationContext;
import flex.messaging.io.TypeMarshaller;
import flex.messaging.io.TypeMarshallingContext;
import flex.messaging.io.amf.translator.ASTranslator;
import flex.messaging.log.Log;
import flex.messaging.log.LogCategories;
import flex.messaging.log.Logger;
import flex.messaging.messages.AcknowledgeMessage;
import flex.messaging.messages.AcknowledgeMessageExt;
import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.AsyncMessageExt;
import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.CommandMessageExt;
import flex.messaging.messages.Message;
import flex.messaging.messages.SmallMessage;
import flex.messaging.security.SecurityException;
import flex.messaging.util.ClassUtil;
import flex.messaging.util.StringUtils;
import flex.messaging.util.UserAgentManager;
import flex.messaging.validators.DeserializationValidator;

/**
 * This is the default implementation of Endpoint, which provides a convenient
 * base for behavior and associations common to all endpoints.
 *
 * These properties that appear in the endpoint configuration are only used by the
 * client, therefore they have to be set on the appropriate client classes: connect-timeout-seconds set on Channel.
 *
 * @see flex.messaging.endpoints.Endpoint
 */
public abstract class AbstractEndpoint extends ManageableComponent
        implements Endpoint2, ConfigurationConstants
{
    /** Log category for <code>AbstractEndpoint</code>. */
    public static final String LOG_CATEGORY = LogCategories.ENDPOINT_GENERAL;

    /**
     * HTTP header field names.
     */
    public static final String HEADER_NAME_CACHE_CONTROL = "Cache-Control";
    public static final String HEADER_NAME_EXPIRES = "Expires";
    public static final String HEADER_NAME_PRAGMA = "Pragma";

    // Errors
    private static final int NONSECURE_PROTOCOL = 10066;
    private static final int REQUIRES_FLEXCLIENT_SUPPORT = 10030;
    private static final int ERR_MSG_INVALID_URL_SCHEME = 11100;

    // XML Configuration Properties
    private static final String SERIALIZATION = "serialization";
    private static final String CREATE_ASOBJECT_FOR_MISSING_TYPE = "create-asobject-for-missing-type";
    private static final String CUSTOM_DESERIALIZER = "custom-deserializer";
    private static final String CUSTOM_SERIALIZER = "custom-serializer";
    private static final String ENABLE_SMALL_MESSAGES = "enable-small-messages";
    private static final String TYPE_MARSHALLER = "type-marshaller";
    private static final String RESTORE_REFERENCES = "restore-references";
    private static final String INSTANTIATE_TYPES = "instantiate-types";
    private static final String SUPPORT_REMOTE_CLASS = "support-remote-class";
    private static final String LEGACY_COLLECTION = "legacy-collection";
    private static final String LEGACY_DICTIONARY = "legacy-dictionary";
    private static final String LEGACY_MAP = "legacy-map";
    private static final String LEGACY_XML = "legacy-xml";
    private static final String LEGACY_XML_NAMESPACES = "legacy-xml-namespaces";
    private static final String LEGACY_THROWABLE = "legacy-throwable";
    private static final String LEGACY_BIG_NUMBERS = "legacy-big-numbers";
    private static final String LEGACY_EXTERNALIZABLE = "legacy-externalizable";
    private static final String ALLOW_XML_EXTERNAL_ENTITY_EXPANSION = "allow-xml-external-entity-expansion";

    private static final String LOG_PROPERTY_ERRORS = "log-property-errors";
    private static final String IGNORE_PROPERTY_ERRORS = "ignore-property-errors";
    private static final String INCLUDE_READ_ONLY = "include-read-only";
    private static final String GLOBAL_INCLUDE_READ_ONLY = "global-include-read-only";
    private static final String FLEX_CLIENT_OUTBOUND_QUEUE_PROCESSOR = "flex-client-outbound-queue-processor";
    private static final String SHOW_STACKTRACES = "show-stacktraces";
    private static final String MAX_OBJECT_NEST_LEVEL = "max-object-nest-level";
    private static final String MAX_COLLECTION_NEST_LEVEL = "max-collection-nest-level";
    private static final String PREFER_VECTORS = "prefer-vectors";

    // Endpoint properties
    protected Set<String> clientLoadBalancingUrls;
    protected String clientType;
    protected int connectTimeoutSeconds;
    protected int requestTimeoutSeconds;
    protected FlexClientOutboundQueueProcessor flexClientOutboundQueueProcessor;
    protected SerializationContext serializationContext;
    protected Class<?> deserializerClass;
    protected Class<?> serializerClass;
    protected TypeMarshaller typeMarshaller;
    protected int port;
    private SecurityConstraint securityConstraint;
    protected String url;
    protected boolean recordMessageSizes;
    protected boolean recordMessageTimes;
    protected boolean remote;
    protected Server server;
    protected boolean serverOnly;

    // Endpoint internal
    protected String parsedUrl;
    // Keeps track of what context path parsedUrl has been parsed for. If it is
    // null, means parsedUrl has not been parsed already.
    protected String parsedForContext;
    protected boolean clientContextParsed;
    protected String parsedClientUrl;
    protected Logger log;

    protected Class<?> flexClientOutboundQueueProcessClass;
    protected ConfigMap flexClientOutboundQueueProcessorConfig;

    // Supported messaging version
    protected double messagingVersion = 1.0;

    //--------------------------------------------------------------------------
    //
    // Constructor
    //
    //--------------------------------------------------------------------------

    /**
     * Constructs an unmanaged <code>AbstractEndpoint</code>.
     */
    public AbstractEndpoint()
    {
        this(false);
    }

    /**
     * Constructs an <code>AbstractEndpoint</code> with the indicated management.
     *
     * @param enableManagement <code>true</code> if the <code>AbstractEndpoint</code>
     * is manageable; <code>false</code> otherwise.
     */
    public AbstractEndpoint(boolean enableManagement)
    {
        super(enableManagement);
        this.log = Log.getLogger(getLogCategory());
        serializationContext = new SerializationContext();
    }

    //--------------------------------------------------------------------------
    //
    // Initialize, validate, start, and stop methods.
    //
    //--------------------------------------------------------------------------

    /**
     * Initializes the <code>Endpoint</code> with the properties.
     * If subclasses override this method, they must call <code>super.initialize()</code>.
     *
     * @param id The ID of the <code>Endpoint</code>.
     * @param properties Properties for the <code>Endpoint</code>.
     */
    @Override
    public void initialize(String id, ConfigMap properties)
    {
        super.initialize(id, properties);

        if (properties == null || properties.size() == 0)
            return;

        // Client-targeted <client-load-balancing>
        initializeClientLoadBalancing(id, properties);

        // Client-targeted <connect-timeout-seconds/>
        connectTimeoutSeconds = properties.getPropertyAsInt(CONNECT_TIMEOUT_SECONDS_ELEMENT, 0);

        // Client-targeted <request-timeout-seconds/>
        requestTimeoutSeconds = properties.getPropertyAsInt(REQUEST_TIMEOUT_SECONDS_ELEMENT, 0);

        // Check for a custom FlexClient outbound queue processor.
        ConfigMap outboundQueueConfig = properties.getPropertyAsMap(FLEX_CLIENT_OUTBOUND_QUEUE_PROCESSOR, null);
        if (outboundQueueConfig != null)
        {
            // Get nested props for the processor.
            flexClientOutboundQueueProcessorConfig = outboundQueueConfig.getPropertyAsMap(PROPERTIES_ELEMENT, null);

            String pClassName = outboundQueueConfig.getPropertyAsString(CLASS_ATTR, null);
            if (pClassName != null)
            {
                try
                {
                    flexClientOutboundQueueProcessClass = createClass(pClassName);
                    // And now create an instance and initialize to make sure the properties are valid.
                    setFlexClientOutboundQueueProcessorConfig(flexClientOutboundQueueProcessorConfig);
                }
                catch (Throwable t)
                {
                    if (Log.isWarn())
                        log.warn("Cannot register custom FlexClient outbound queue processor class {0}", new Object[]{pClassName}, t);
                }
            }
        }

        ConfigMap serialization = properties.getPropertyAsMap(SERIALIZATION, null);
        if (serialization != null)
        {
            // Custom deserializers
            List<?> deserializers = serialization.getPropertyAsList(CUSTOM_DESERIALIZER, null);
            if (deserializers != null && Log.isWarn())
                log.warn("Endpoint <custom-deserializer> functionality is no longer available. Please remove this entry from your configuration.");

            // Custom serializers
            List<?> serializers = serialization.getPropertyAsList(CUSTOM_SERIALIZER, null);
            if (serializers != null && Log.isWarn())
                log.warn("Endpoint <custom-serializer> functionality is no longer available. Please remove this entry from your configuration.");

            // Type Marshaller implementation
            String typeMarshallerClassName = serialization.getPropertyAsString(TYPE_MARSHALLER, null);
            if (typeMarshallerClassName != null && typeMarshallerClassName.length() > 0)
            {
                try
                {
                    Class<?> tmc = createClass(typeMarshallerClassName);
                    typeMarshaller = (TypeMarshaller)ClassUtil.createDefaultInstance(tmc, TypeMarshaller.class);
                }
                catch (Throwable t)
                {
                    if (Log.isWarn())
                        log.warn("Cannot register custom type marshaller for type {0}", new Object[]{typeMarshallerClassName}, t);
                }
            }

            // Boolean Serialization Flags
            serializationContext.createASObjectForMissingType = serialization.getPropertyAsBoolean(CREATE_ASOBJECT_FOR_MISSING_TYPE, false);
            serializationContext.enableSmallMessages = serialization.getPropertyAsBoolean(ENABLE_SMALL_MESSAGES, true);
            serializationContext.instantiateTypes = serialization.getPropertyAsBoolean(INSTANTIATE_TYPES, true);
            serializationContext.supportRemoteClass = serialization.getPropertyAsBoolean(SUPPORT_REMOTE_CLASS, false);
            serializationContext.legacyCollection = serialization.getPropertyAsBoolean(LEGACY_COLLECTION, false);
            serializationContext.legacyDictionary = serialization.getPropertyAsBoolean(LEGACY_DICTIONARY, false);
            serializationContext.legacyMap = serialization.getPropertyAsBoolean(LEGACY_MAP, false);
            serializationContext.legacyXMLDocument = serialization.getPropertyAsBoolean(LEGACY_XML, false);
            serializationContext.legacyXMLNamespaces = serialization.getPropertyAsBoolean(LEGACY_XML_NAMESPACES, false);
            serializationContext.legacyThrowable = serialization.getPropertyAsBoolean(LEGACY_THROWABLE, false);
            serializationContext.legacyBigNumbers = serialization.getPropertyAsBoolean(LEGACY_BIG_NUMBERS, false);
            serializationContext.legacyExternalizable = serialization.getPropertyAsBoolean(LEGACY_EXTERNALIZABLE, false);
            serializationContext.allowXmlExternalEntityExpansion = serialization.getPropertyAsBoolean(ALLOW_XML_EXTERNAL_ENTITY_EXPANSION, false);
            serializationContext.maxObjectNestLevel = (int)serialization.getPropertyAsLong(MAX_OBJECT_NEST_LEVEL, 512);
            serializationContext.maxCollectionNestLevel = (int)serialization.getPropertyAsLong(MAX_COLLECTION_NEST_LEVEL, 15);
            serializationContext.preferVectors = serialization.getPropertyAsBoolean(PREFER_VECTORS, false);

            boolean showStacktraces = serialization.getPropertyAsBoolean(SHOW_STACKTRACES, false);
            if (showStacktraces && Log.isWarn())
                log.warn("The " + SHOW_STACKTRACES + " configuration option is deprecated and non-functional. Please remove this from your configuration file.");
            serializationContext.restoreReferences = serialization.getPropertyAsBoolean(RESTORE_REFERENCES, false);
            serializationContext.logPropertyErrors = serialization.getPropertyAsBoolean(LOG_PROPERTY_ERRORS, false);
            serializationContext.ignorePropertyErrors = serialization.getPropertyAsBoolean(IGNORE_PROPERTY_ERRORS, true);
            serializationContext.includeReadOnly = serialization.getPropertyAsBoolean(INCLUDE_READ_ONLY, false);
        }

        recordMessageSizes = properties.getPropertyAsBoolean(ConfigurationConstants.RECORD_MESSAGE_SIZES_ELEMENT, false);

        if (recordMessageSizes && Log.isWarn())
            log.warn("Setting <record-message-sizes> to true affects application performance and should only be used for debugging");

        recordMessageTimes = properties.getPropertyAsBoolean(ConfigurationConstants.RECORD_MESSAGE_TIMES_ELEMENT, false);
    }

    /**
     * Starts the endpoint if its associated <code>MessageBroker</code> is started,
     * and if the endpoint is not already running. If subclasses override this method,
     * they must call <code>super.start()</code>.
     */
    @Override
    public void start()
    {
        if (isStarted())
            return;

        // Check if the MessageBroker is started
        MessageBroker broker = getMessageBroker();
        if (!broker.isStarted())
        {
            if (Log.isWarn())
            {
                Log.getLogger(getLogCategory()).warn("Endpoint with id '{0}' cannot be started" +
                        " when the MessageBroker is not started.",
                        new Object[]{getId()});
            }
            return;
        }

        // Set up management
        if (isManaged() && broker.isManaged())
        {
            setupEndpointControl(broker);
            MessageBrokerControl controller = (MessageBrokerControl)broker.getControl();
            if (getControl() != null)
                controller.addEndpoint(this);
        }

        // Setup Deserializer and Serializer for the SerializationContext
        if (deserializerClass == null)
            deserializerClass = createClass(getDeserializerClassName());

        if (serializerClass == null)
            serializerClass = createClass(getSerializerClassName());

        serializationContext.setDeserializerClass(deserializerClass);
        serializationContext.setSerializerClass(serializerClass);

        // Setup endpoint features
        ClassAliasRegistry registry = ClassAliasRegistry.getRegistry();
        registry.registerAlias(AsyncMessageExt.CLASS_ALIAS, AsyncMessageExt.class.getName());
        registry.registerAlias(AcknowledgeMessageExt.CLASS_ALIAS, AcknowledgeMessageExt.class.getName());
        registry.registerAlias(CommandMessageExt.CLASS_ALIAS, CommandMessageExt.class.getName());
        super.start();
    }

    /**
     * Stops the endpoint if it is running. If subclasses override this method, they must
     * call <code>super.stop()</code>.
     */
    @Override
    public void stop()
    {
        if (!isStarted())
            return;

        super.stop();

        // Remove management
        if (isManaged() && getMessageBroker().isManaged())
        {
            if (getControl() != null)
            {
                getControl().unregister();
                setControl(null);
            }
            setManaged(false);
        }
    }

    //--------------------------------------------------------------------------
    //
    // Public Getters and Setters for AbstractEndpoint properties
    //
    //--------------------------------------------------------------------------

    /**
     * Adds a client-load-balancing URL.
     *
     * @param url A client-load-balancing URL.
     * @return <code>false</code> if the set already contains the URL, <code>true</code> otherwise.
     *
     */
    public boolean addClientLoadBalancingUrl(String url)
    {
        if (clientLoadBalancingUrls == null)
            clientLoadBalancingUrls = new HashSet<String>();

        if (url == null || url.length() == 0)
        {
            // Invalid {0} configuration for endpoint ''{1}''; cannot add empty url.
            ConfigurationException  ce = new ConfigurationException();
            ce.setMessage(ERR_MSG_EMTPY_CLIENT_LOAD_BALACNING_URL, new Object[]{CLIENT_LOAD_BALANCING_ELEMENT, getId()});
            throw ce;
        }

        return clientLoadBalancingUrls.add(url);
    }

    /**
     * Removes the client-load-balancing URL.
     *
     * @param url The URL to remove.
     * @return <code>true</code> if the set contained the URL, <code>false</code> otherwise.
     */
    public boolean removeClientLoadBalancingUrl(String url)
    {
        if (clientLoadBalancingUrls != null)
            return clientLoadBalancingUrls.remove(url);
        return false;
    }

    /**
     * Retrieves a snapshot of the current client-load-balancing URLs.
     *
     * @return A snapshot of the current client-load-balancing URLs, or <code>null</code> if
     * no URL exists.
     */
    public Set<String> getClientLoadBalancingUrls()
    {
        return clientLoadBalancingUrls == null? null : new HashSet<String>(clientLoadBalancingUrls);
    }

    /**
     * Retrieves the corresponding client channel type for the endpoint.
     *
     * @return The corresponding client channel type for the endpoint.
     */
    public String getClientType()
    {
        return clientType;
    }

    /**
     * Sets the corresponding client channel type for the endpoint.
     *
     * @param type The corresponding client channel type for the endpoint.
     */
    public void setClientType(String type)
    {
        this.clientType = type;
    }

    /**
     * Retrieves the <code>FlexClientOutboundQueueProcessorClass</code> of the endpoint.
     *
     * @return The <code>FlexClientOutboundQueueProcessorClass</code> of the endpoint.
     */
    public Class<?> getFlexClientOutboundQueueProcessorClass()
    {
        return flexClientOutboundQueueProcessClass;
    }

    /**
     * Sets the the <code>FlexClientOutboundQueueProcessor</code> of the endpoint.
     *
     * @param flexClientOutboundQueueProcessorClass the Class of the Flex client outbound queue processor.
     */
    public void setFlexClientOutboundQueueProcessorClass(Class<?> flexClientOutboundQueueProcessorClass)
    {
        this.flexClientOutboundQueueProcessClass = flexClientOutboundQueueProcessorClass;
        if (flexClientOutboundQueueProcessClass != null && flexClientOutboundQueueProcessorConfig != null)
        {
            FlexClientOutboundQueueProcessor processor = (FlexClientOutboundQueueProcessor)ClassUtil.createDefaultInstance(flexClientOutboundQueueProcessClass, null);
            processor.initialize(flexClientOutboundQueueProcessorConfig);
        }
    }

    /**
     * Retrieves the properties for the <code>FlexClientOutboundQueueProcessor</code> of the endpoint.
     *
     * @return The properties for the <code>FlexClientOutboundQueueProcessor</code> of the endpoint.
     */
    public ConfigMap getFlexClientOutboundQueueProcessorConfig()
    {
        return flexClientOutboundQueueProcessorConfig;
    }

    /**
     * Sets the properties for the <code>FlexClientOutboundQueueProcessor</code> of the endpoint.
     *
     * @param flexClientOutboundQueueProcessorConfig The configuration map.
     */
    public void setFlexClientOutboundQueueProcessorConfig(ConfigMap flexClientOutboundQueueProcessorConfig)
    {
        this.flexClientOutboundQueueProcessorConfig = flexClientOutboundQueueProcessorConfig;
        if (flexClientOutboundQueueProcessorConfig != null && flexClientOutboundQueueProcessClass != null)
        {
            FlexClientOutboundQueueProcessor processor = (FlexClientOutboundQueueProcessor)ClassUtil.createDefaultInstance(flexClientOutboundQueueProcessClass, null);
            processor.initialize(flexClientOutboundQueueProcessorConfig);
        }
    }

    /**
     * Sets the ID of the <code>AbstractEndpoint</code>. If the <code>AbstractEndpoint</code>
     * has a <code>MessageBroker</code> assigned, it also updates the ID in the
     * <code>MessageBroker</code>.
     *
     * @param id The endpoint ID.
     */
    @Override
    public void setId(String id)
    {
        String oldId = getId();

        if (oldId != null && oldId.equals(id))
            return;

        super.setId(id);

        // Update the endpoint id in the broker
        MessageBroker broker = getMessageBroker();
        if (broker != null)
        {
            // broker must have the endpoint then
            broker.removeEndpoint(oldId);
            broker.addEndpoint(this);
        }
    }

    /**
     * Retrieves the <code>MessageBroker</code> of the <code>AbstractEndpoint</code>.
     *
     * @return The <code>MessageBroker</code> of the <code>AbstractEndpoint</code>.
     */
    public MessageBroker getMessageBroker()
    {
        return (MessageBroker)getParent();
    }

    /**
     * Sets the <code>MessageBroker</code> of the <code>AbstractEndpoint</code>.
     * Removes the <code>AbstractEndpoint</code> from the old broker
     * (if there was one) and adds to the list of endpoints in the new broker.
     *
     * @param broker The <code>MessageBroker</code> of the <code>AbstractEndpoint</code>.
     */
    public void setMessageBroker(MessageBroker broker)
    {
        MessageBroker oldBroker = getMessageBroker();

        setParent(broker);

        if (oldBroker != null)
            oldBroker.removeEndpoint(getId());

        // Add endpoint to the new broker if needed
        if (broker.getEndpoint(getId()) != this)
            broker.addEndpoint(this);
    }

    /**
     * Return the highest messaging version currently available via this
     * endpoint.
     * @return double the messaging version
     */
    public double getMessagingVersion()
    {
        return messagingVersion;
    }

    /**
     * Retrieves the port of the URL of the endpoint.
     * A return value of 0 denotes no port in the channel URL.
     *
     * @return The port of the URL of the endpoint, or 0 if the URL does not contain
     * a port number.
     */
    public int getPort()
    {
        return port;
    }

    /**
     * Determines whether the endpoint is secure.
     *
     * @return <code>false</code> by default.
     */
    public boolean isSecure()
    {
        return false;
    }

    /**
     * Determines if the endpoint clients connect to directly is mirrored and running
     * on a remote host, in which case this local instance is not started and will service no direct
     * client connections.
     *
     * @return <code>true</code> if this endpoint will not process direct client connections and is just
     * a local representation of a symmetric endpoint on a remote host that will, <code>false</code> otherwise.
     */
    public boolean isRemote()
    {
        return remote;
    }

    /**
     * Sets the remote status for this endpoint.
     *
     * @param value <code>true</code> if this endpoint will not process direct client connections and is just
     * a local representation of a symmetric endpoint on a remote host that will, <code>false</code> otherwise.
     */
    public void setRemote(boolean value)
    {
        remote = value;
    }

    /**
     * Retrieves the <tt>Server</tt> that the endpoint is using, or <code>null</code> if
     * no server has been assigned.
     * @return Server The Server object the endpoint is using.
     */
    public Server getServer()
    {
        return server;
    }

    /**
     * Sets the <tt>Server</tt> that the endpoint will use.
     * @param server The Server object.
     */
    public void setServer(Server server)
    {
        this.server = server;
    }

    /**
     * Determines whether the endpoint is server only.
     *
     * @return <code>true</code> if the endpoint is server only, <code>false</code> otherwise.
     */
    public boolean getServerOnly()
    {
        return serverOnly;
    }

    /**
     * Sets whether the endpoint is server only.
     *
     * @param serverOnly <code>true</code> if the endpoint is server only, <code>false</code> otherwise.
     */
    public void setServerOnly(boolean serverOnly)
    {
        this.serverOnly = serverOnly;
    }

    /**
     * Retrieves the <code>SecurityConstraint</code> of the <code>Endpoint</code>.
     *
     * @return The <code>SecurityConstraint</code> of the <code>Endpoint</code>.
     */
    public SecurityConstraint getSecurityConstraint()
    {
        return securityConstraint;
    }

    /**
     * Sets the <code>SecurityConstraint</code> of the <code>Endpoint</code>.
     *
     * @param securityConstraint The SecurityContraint object.
     */
    public void setSecurityConstraint(SecurityConstraint securityConstraint)
    {
        this.securityConstraint = securityConstraint;
    }

    /**
     * Retrieves the <code>SerializationContext</code> of the endpoint.
     *
     * @return The <code>SerializationContext</code> of the endpoint.
     */
    public SerializationContext getSerializationContext()
    {
        return serializationContext;
    }

    /**
     * Sets the <code>SerializationContext</code> of the endpoint.
     *
     * @param serializationContext The SerializationContext object.
     */
    public void setSerializationContext(SerializationContext serializationContext)
    {
        this.serializationContext = serializationContext;
    }

    /**
     * Retrieves the <code>TypeMarshaller</code> of the endpoint.
     *
     * @return The <code>TypeMarshaller</code> of the endpoint.
     */
    public TypeMarshaller getTypeMarshaller()
    {
        if (typeMarshaller == null)
            typeMarshaller = new ASTranslator();

        return typeMarshaller;
    }

    /**
     * Sets the <code>TypeMarshaller</code> of the endpoint.
     *
     * @param typeMarshaller The TypeMarshaller object.
     */
    public void setTypeMarshaller(TypeMarshaller typeMarshaller)
    {
        this.typeMarshaller = typeMarshaller;
    }

    /**
     * Retrieves the URL of the endpoint.
     *
     * @return The URL of the endpoint.
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * Sets the URL of the endpoint.
     *
     * @param url The URL of the endpoint.
     */
    public void setUrl(String url)
    {
        this.url = url;
        port = internalParsePort(url);
        parsedForContext = null;
        clientContextParsed = false;
    }

    /**
     *
     * Returns the url of the endpoint parsed for the client.
     *
     * @return The url of the endpoint parsed for the client.
     */
    public String getUrlForClient()
    {
        if (!clientContextParsed)
        {
            HttpServletRequest req = FlexContext.getHttpRequest();
            if (req != null)
            {
                String contextPath = req.getContextPath();
                parseClientUrl(contextPath);
            }
            else
            {
                return url;
            }
        }
        return parsedClientUrl;
    }

    /**
     *
     * Returns the total throughput for the endpoint.
     *
     * @return The total throughput for the endpoint.
     */
    public long getThroughput()
    {
        EndpointControl control = (EndpointControl)getControl();

        return control.getBytesDeserialized().longValue() + control.getBytesSerialized().longValue();
    }

    //--------------------------------------------------------------------------
    //
    // Other Public APIs
    //
    //--------------------------------------------------------------------------

    /** @exclude **/
    public static void addNoCacheHeaders(HttpServletRequest req, HttpServletResponse res)
    {
        String userAgent = req.getHeader(UserAgentManager.USER_AGENT_HEADER_NAME);

        // For MSIE over HTTPS, set additional Cache-Control values.
        if (req.isSecure() && userAgent != null && userAgent.indexOf(UserAgentSettings.USER_AGENT_MSIE) != -1)
            res.addHeader(HEADER_NAME_CACHE_CONTROL, "no-store, no-cache, must-revalidate, post-check=0, pre-check=0, no-transform, private");
        else // For the rest, set no-cache header value only.
            res.addHeader(HEADER_NAME_CACHE_CONTROL, "no-cache");

        // Set an expiration date in the past as well.
        res.setDateHeader(HEADER_NAME_EXPIRES, 946080000000L); //Approx Jan 1, 2000

        // Set Pragma no-cache header if we're not MSIE over HTTPS
        if (!(req.isSecure() && userAgent != null && userAgent.indexOf(UserAgentSettings.USER_AGENT_MSIE) != -1))
            res.setHeader(HEADER_NAME_PRAGMA, "no-cache");
    }

    /**
     *
     */
    public Message convertToSmallMessage(Message message)
    {
        if (message instanceof SmallMessage)
        {
            Message smallMessage = ((SmallMessage)message).getSmallMessage();
            if (smallMessage != null)
                message = smallMessage;
        }

        return message;
    }

    /**
     * Retrieves a <code>ConfigMap</code> of the endpoint properties the client
     * needs. Subclasses should add additional properties to <code>super.describeDestination</code>,
     * or return <code>null</code> if they must not send their properties to the client.
     *
     * @return ConfigMap The ConfigMap object.
     */
    public ConfigMap describeEndpoint()
    {
        ConfigMap channelConfig = new ConfigMap();

        if (serverOnly) // Client does not need server only endpoints.
            return channelConfig;

        channelConfig.addProperty(ID_ATTR, getId());
        channelConfig.addProperty(TYPE_ATTR, getClientType());

        ConfigMap properties = new ConfigMap();

        boolean containsClientLoadBalancing = clientLoadBalancingUrls != null && !clientLoadBalancingUrls.isEmpty();
        if (containsClientLoadBalancing)
        {
            ConfigMap clientLoadBalancing = new ConfigMap();
            for (Iterator<String> iterator = clientLoadBalancingUrls.iterator(); iterator.hasNext();)
            {
                ConfigMap url = new ConfigMap();
                // Adding as a value rather than attribute to the parent.
                url.addProperty(EMPTY_STRING, iterator.next());
                clientLoadBalancing.addProperty(URL_ATTR, url);
            }
            properties.addProperty(CLIENT_LOAD_BALANCING_ELEMENT, clientLoadBalancing);
        }

        // Add endpoint uri only if no client-load-balancing urls are defined.
        if (!containsClientLoadBalancing)
        {
            ConfigMap endpointConfig = new ConfigMap();
            endpointConfig.addProperty(URI_ATTR, getUrlForClient());
            channelConfig.addProperty(ENDPOINT_ELEMENT, endpointConfig);
        }

        if (connectTimeoutSeconds > 0)
        {
            ConfigMap connectTimeoutConfig = new ConfigMap();
            connectTimeoutConfig.addProperty(EMPTY_STRING, String.valueOf(connectTimeoutSeconds));
            properties.addProperty(CONNECT_TIMEOUT_SECONDS_ELEMENT, connectTimeoutConfig);
        }

        if (requestTimeoutSeconds > 0)
        {
            ConfigMap requestTimeoutSeconds = new ConfigMap();
            requestTimeoutSeconds.addProperty(EMPTY_STRING, String.valueOf(requestTimeoutSeconds));
            properties.addProperty(REQUEST_TIMEOUT_SECONDS_ELEMENT, requestTimeoutSeconds);
        }

        if (recordMessageTimes)
        {
            ConfigMap recordMessageTimesMap = new ConfigMap();
            // Adding as a value rather than attribute to the parent
            recordMessageTimesMap.addProperty(EMPTY_STRING, TRUE_STRING);
            properties.addProperty(RECORD_MESSAGE_TIMES_ELEMENT, recordMessageTimesMap);
        }

        if (recordMessageSizes)
        {
            ConfigMap recordMessageSizesMap = new ConfigMap();
            // Adding as a value rather than attribute to the parent
            recordMessageSizesMap.addProperty(EMPTY_STRING, TRUE_STRING);
            properties.addProperty(RECORD_MESSAGE_SIZES_ELEMENT, recordMessageSizesMap);
        }

        ConfigMap serialization = new ConfigMap();
        serialization.addProperty(ENABLE_SMALL_MESSAGES_ELEMENT, Boolean.toString(serializationContext.enableSmallMessages));
        properties.addProperty(SERIALIZATION_ELEMENT, serialization);

        if (properties.size() > 0)
            channelConfig.addProperty(PROPERTIES_ELEMENT, properties);

        return channelConfig;
    }

    /**
     *
     * Make sure this matches with ChannelSettings.getParsedUri.
     */
    public String getParsedUrl(String contextPath)
    {
        parseUrl(contextPath);
        return parsedUrl;
    }

    /**
     *
     */
    public void handleClientMessagingVersion(Number version)
    {
        if (version != null)
        {
            boolean clientSupportsSmallMessages = version.doubleValue() >= messagingVersion;
            if (clientSupportsSmallMessages && getSerializationContext().enableSmallMessages)
            {
                FlexSession session = FlexContext.getFlexSession();
                if (session != null)
                    session.setUseSmallMessages(true);
            }
        }
    }

    /**
     * Default implementation of the Endpoint <code>service</code> method.
     * Subclasses should call <code>super.service</code> before their custom
     * code.
     *
     * @param req The HttpServletRequest object.
     * @param res The HttpServletResponse object.
     */
    public void service(HttpServletRequest req, HttpServletResponse res)
    {
        validateRequestProtocol(req);
    }

    /**
     * Typically invoked by subclasses, this method transforms decoded message data
     * into the appropriate Message object and routes the Message to the endpoint's broker.
     *
     * @param message The decoded message data.
     * @return Message The transformed message.
     */
    public Message serviceMessage(Message message)
    {
        if (isManaged())
        {
            ((EndpointControl) getControl()).incrementServiceMessageCount();
        }

        try
        {
            FlexContext.setThreadLocalEndpoint(this);
            Message ack = null;

            // Make sure this message is timestamped.
            if (message.getTimestamp() == 0)
            {
                message.setTimestamp(System.currentTimeMillis());
            }

            // Reset the endpoint header for inbound messages to the id for this endpoint
            // to guarantee that it's correct. Don't allow clients to spoof this.
            // However, if the endpoint id is passed as null we need to tag the message to
            // skip channel/endpoint validation at the destination level (MessageBroker.inspectChannel()).
            if (message.getHeader(Message.ENDPOINT_HEADER) != null)
                message.setHeader(Message.VALIDATE_ENDPOINT_HEADER, Boolean.TRUE);
            message.setHeader(Message.ENDPOINT_HEADER, getId());

            if (message instanceof CommandMessage)
            {
                CommandMessage command = (CommandMessage)message;

                // Apply channel endpoint level constraint; always allow login commands through.
                int operation = command.getOperation();
                if (operation != CommandMessage.LOGIN_OPERATION)
                    checkSecurityConstraint(message);

                // Handle general (not Consumer specific) poll requests here.
                // We need to fetch all outbound messages for client subscriptions over this endpoint.
                // We identify these general poll messages by their operation and a null clientId.
                if (operation == CommandMessage.POLL_OPERATION && message.getClientId() == null)
                {
                    verifyFlexClientSupport(command);


                    FlexClient flexClient = FlexContext.getFlexClient();
                    ack = handleFlexClientPollCommand(flexClient, command);
                }
                else if (operation == CommandMessage.DISCONNECT_OPERATION)
                {
                    ack = handleChannelDisconnect(command);
                }
                else if (operation == CommandMessage.TRIGGER_CONNECT_OPERATION)
                {
                    ack = new AcknowledgeMessage();
                    ((AcknowledgeMessage)ack).setCorrelationId(message.getMessageId());

                    boolean needsConfig = false;
                    if (command.getHeader(CommandMessage.NEEDS_CONFIG_HEADER) != null)
                        needsConfig = ((Boolean)(command.getHeader(CommandMessage.NEEDS_CONFIG_HEADER)));

                    // Send configuration information only if the client requested.
                    if (needsConfig)
                    {
                        ConfigMap serverConfig = getMessageBroker().describeServices(this);
                        if (serverConfig.size() > 0)
                            ack.setBody(serverConfig);
                    }
                }
                else
                {
                    // Block a subset of commands for legacy clients that need to be recompiled to
                    // interop with a 2.5+ server.
                    if (operation == CommandMessage.SUBSCRIBE_OPERATION || operation == CommandMessage.POLL_OPERATION)
                        verifyFlexClientSupport(command);

                    ack = getMessageBroker().routeCommandToService((CommandMessage) message, this);

                    // Look for client advertised features on initial connect.
                    if (operation == CommandMessage.CLIENT_PING_OPERATION || operation == CommandMessage.LOGIN_OPERATION)
                    {
                        Number clientVersion = (Number)command.getHeader(CommandMessage.MESSAGING_VERSION);
                        handleClientMessagingVersion(clientVersion);

                        // Also respond by advertising the messaging version on the
                        // acknowledgement.
                        ack.setHeader(CommandMessage.MESSAGING_VERSION, new Double(messagingVersion));
                    }
                }
            }
            else
            {
                // Block any AsyncMessages from a legacy client.
                if (message instanceof AsyncMessage)
                    verifyFlexClientSupport(message);

                // Apply channel endpoint level constraint.
                checkSecurityConstraint(message);

                ack = getMessageBroker().routeMessageToService(message, this);
            }

            return ack;
        }
        finally
        {
            FlexContext.setThreadLocalEndpoint(null);
        }
    }

   /**
    * Utility method that endpoint implementations (or associated classes)
    * should invoke when they receive an incoming message from a client but before
    * servicing it. This method looks up or creates the proper FlexClient instance
    * based upon the client the message came from and places it in the FlexContext.
    *
    * @param message The incoming message to process.
    *
    * @return The FlexClient, or <code>null</code> if the message did not contain a FlexClient ID value.
    */
   public FlexClient setupFlexClient(Message message)
   {
       FlexClient flexClient = null;
       if (message.getHeaders().containsKey(Message.FLEX_CLIENT_ID_HEADER))
       {
           String id = (String)message.getHeaders().get(Message.FLEX_CLIENT_ID_HEADER);
           // If the id is null, reset to the special token value that let's us differentiate
           // between legacy clients and 2.5+ clients.
           if (id == null)
               id = FlexClient.NULL_FLEXCLIENT_ID;
           flexClient = setupFlexClient(id);
       }
       return flexClient;
   }

   /**
    * Utility method that endpoint implementations (or associated classes)
    * should invoke when they receive an incoming message from a client but before
    * servicing it. This method looks up or creates the proper FlexClient instance
    * based upon the FlexClient ID value received from the client.
    * It also associates this FlexClient instance with the current FlexSession.
    *
    * @param id The FlexClient ID value from the client.
    *
    * @return The FlexClient or null if the provided ID was <code>null</code>.
    */
   public FlexClient setupFlexClient(String id)
   {
       FlexClient flexClient = null;
       if (id != null)
       {
           // This indicates that we're dealing with a non-legacy client that hasn't been
           // assigned a FlexClient Id yet. Reset to null to generate a fresh Id.
           if (id.equals(FlexClient.NULL_FLEXCLIENT_ID))
               id = null;

           flexClient = getMessageBroker().getFlexClientManager().getFlexClient(id);
           // Make sure the FlexClient and FlexSession are associated.
           FlexSession session = FlexContext.getFlexSession();
           flexClient.registerFlexSession(session);
           // And place the FlexClient in FlexContext for this request.
           FlexContext.setThreadLocalFlexClient(flexClient);
       }
       return flexClient;
   }

   /**
    *
    * Performance metrics gathering property
    */
    public boolean isRecordMessageSizes()
    {
        return recordMessageSizes;
    }

   /**
    *
    * Performance metrics gathering property
    */
    public boolean isRecordMessageTimes()
    {
        return recordMessageTimes;
    }

    /**
     *
     */
    public void setThreadLocals()
    {
        if (serializationContext != null)
        {
            SerializationContext context = (SerializationContext)serializationContext.clone();
            // Get the latest deserialization validator from the broker.
            MessageBroker broker = getMessageBroker();
            DeserializationValidator validator = broker == null? null : broker.getDeserializationValidator();
            context.setDeserializationValidator(validator);
            SerializationContext.setSerializationContext(context);
        }

        TypeMarshallingContext.setTypeMarshaller(getTypeMarshaller());
    }

    /**
     *
     */
    public void clearThreadLocals()
    {
        SerializationContext.clearThreadLocalObjects();
        TypeMarshallingContext.clearThreadLocalObjects();
    }

    //--------------------------------------------------------------------------
    //
    // Protected/private methods.
    //
    //--------------------------------------------------------------------------

    /**
     * Returns the log category of the <code>AbstractEndpoint</code>. Subclasses
     * can override to provide a more specific logging category.
     *
     * @return The log category.
     */
    @Override
    protected String getLogCategory()
    {
        return LOG_CATEGORY;
    }

    /**
     * Hook method invoked when a disconnect command is received from a client channel.
     * The response returned by this method is not guaranteed to get to the client, which
     * is free to terminate its physical connection at any point.
     *
     * @param disconnectCommand The disconnect command.
     * @return The response; by default an empty <tt>AcknowledgeMessage</tt>.
     */
    protected Message handleChannelDisconnect(CommandMessage disconnectCommand)
    {
        return new AcknowledgeMessage();
    }

    /**
     * Hook method for varying poll reply strategies for synchronous endpoints.
     * The default behavior performs a non-waited, synchronous poll for the FlexClient
     * and if any messages are currently queued they are returned immediately. If no
     * messages are queued an empty response is returned immediately.
     *
     * @param flexClient The FlexClient that issued the poll request.
     * @param pollCommand The poll command from the client.
     * @return The FlushResult response.
     */
    protected FlushResult handleFlexClientPoll(FlexClient flexClient, CommandMessage pollCommand)
    {
        return flexClient.poll(getId());
    }

    /**
     * Handles a general poll request from a FlexClient to this endpoint.
     * Subclasses may override to implement different poll handling strategies.
     *
     * @param flexClient The FlexClient that issued the poll request.
     * @param pollCommand The poll command from the client.
     * @return The poll response message; either for success or fault.
     */
    protected Message handleFlexClientPollCommand(FlexClient flexClient, CommandMessage pollCommand)
    {
        if (Log.isDebug())
            Log.getLogger(getMessageBroker().getLogCategory(pollCommand)).debug(
                 "Before handling general client poll request. " + StringUtils.NEWLINE +
                 "  incomingMessage: " + pollCommand + StringUtils.NEWLINE);

        FlushResult flushResult = handleFlexClientPoll(flexClient, pollCommand);
        Message pollResponse = null;

        // Generate a no-op poll response if necessary; prevents a single client from busy polling when the server
        // is doing wait()-based long-polls.
        if ((flushResult instanceof PollFlushResult) && ((PollFlushResult)flushResult).isClientProcessingSuppressed())
        {
            pollResponse = new CommandMessage(CommandMessage.CLIENT_SYNC_OPERATION);
            pollResponse.setHeader(CommandMessage.NO_OP_POLL_HEADER, Boolean.TRUE);
        }

        if (pollResponse == null)
        {
            List<Message> messagesToReturn = (flushResult != null) ? flushResult.getMessages() : null;
            if (messagesToReturn != null && !messagesToReturn.isEmpty())
            {
                pollResponse = new CommandMessage(CommandMessage.CLIENT_SYNC_OPERATION);
                pollResponse.setBody(messagesToReturn.toArray());
            }
            else
            {
                pollResponse = new AcknowledgeMessage();
            }
        }

        // Set the adaptive poll wait time if necessary.
        if (flushResult != null)
        {
            int nextFlushWaitTime = flushResult.getNextFlushWaitTimeMillis();
            if (nextFlushWaitTime > 0)
                pollResponse.setHeader(CommandMessage.POLL_WAIT_HEADER, new Integer(nextFlushWaitTime));
        }

        if (Log.isDebug())
        {
            String debugPollResult = Log.getPrettyPrinter().prettify(pollResponse);
            Log.getLogger(getMessageBroker().getLogCategory(pollCommand)).debug(
                 "After handling general client poll request. " + StringUtils.NEWLINE +
                 "  reply: " + debugPollResult + StringUtils.NEWLINE);
        }

        return pollResponse;
    }

    /**
     * Initializes the <code>Endpoint</code> with the client-load-balancing urls.
     *
     * @param id Id of the <code>Endpoint</code>.
     * @param properties Properties for the <code>Endpoint</code>.
     */

    protected void initializeClientLoadBalancing(String id, ConfigMap properties)
    {
        if (!properties.containsKey(CLIENT_LOAD_BALANCING_ELEMENT))
            return;

        ConfigMap clientLoadBalancing = properties.getPropertyAsMap(CLIENT_LOAD_BALANCING_ELEMENT, null);
        if (clientLoadBalancing == null)
        {
            // Invalid {0} configuration for endpoint ''{1}''; no urls defined.
            ConfigurationException ce = new ConfigurationException();
            ce.setMessage(ERR_MSG_EMPTY_CLIENT_LOAD_BALANCING_ELEMENT, new Object[]{CLIENT_LOAD_BALANCING_ELEMENT, getId()});
            throw ce;
        }

        @SuppressWarnings("unchecked")
        List<String> urls = clientLoadBalancing.getPropertyAsList(URL_ATTR, null);
        if (urls == null || urls.isEmpty())
        {
            // Invalid {0} configuration for endpoint ''{1}''; no urls defined.
            ConfigurationException ce = new ConfigurationException();
            ce.setMessage(ERR_MSG_EMPTY_CLIENT_LOAD_BALANCING_ELEMENT, new Object[]{CLIENT_LOAD_BALANCING_ELEMENT, getId()});
            throw ce;
        }

        for (Iterator<String> iterator = urls.iterator(); iterator.hasNext();)
        {
            String url = iterator.next();
            if (!addClientLoadBalancingUrl(url) && Log.isWarn())
                log.warn("Endpoint '{0}' is ignoring the url '{1}' as it's already in the set of client-load-balancing urls.", new Object[]{id, url});
        }
    }

    protected void checkSecurityConstraint(Message message)
    {
        if (securityConstraint != null)
        {
            getMessageBroker().getLoginManager().checkConstraint(securityConstraint);
        }
    }

    /**
     * Returns the deserializer class name used by the endpoint.
     *
     * @return The deserializer class name used by the endpoint.
     */
    protected abstract String getDeserializerClassName();

    /**
     * Returns the serializer class name used by the endpoint.
     *
     * @return The serializer class name used by the endpoint.
     */
    protected abstract String getSerializerClassName();

    /**
     * Returns the secure protocol scheme for the endpoint.
     *
     * @return The secure protocol scheme for the endpoint.
     */
    protected abstract String getSecureProtocolScheme();

    /**
     * Returns the insecure protocol scheme for the endpoint.
     *
     * @return The insecure protocol scheme for the endpoint.
     */
    protected abstract String getInsecureProtocolScheme();

    /**
     * Invoked automatically to allow the <code>AbstractEndpoint</code> to setup
     * its corresponding MBean control. Subclasses should override to setup and
     * register their MBean control. Manageable subclasses should override this
     * template method.
     *
     * @param broker The <code>MessageBroker</code> that manages this
     * <code>AbstractEndpoint</code>.
     */
    protected abstract void setupEndpointControl(MessageBroker broker);

    /**
     * Validates the endpoint url scheme.
     */
    protected void validateEndpointProtocol()
    {
        String scheme = isSecure()? getSecureProtocolScheme() : getInsecureProtocolScheme();
        if (!url.startsWith(scheme))
        {
            ConfigurationException ce = new ConfigurationException();
            ce.setMessage(ERR_MSG_INVALID_URL_SCHEME, new Object[] {url, scheme});
            throw ce;
        }
    }

    protected void validateRequestProtocol(HttpServletRequest req)
    {
        // Secure url can talk to secure or non-secure endpoint.
        // Non-secure url can only talk to non-secure endpoint.
        boolean secure = req.isSecure();
        if (!secure && isSecure())
        {
            // Secure endpoints must be contacted via a secure protocol.
            String endpointPath = req.getServletPath() + req.getPathInfo();
            SecurityException se = new SecurityException();
            se.setMessage(NONSECURE_PROTOCOL, new Object[]{endpointPath});
            throw se;
        }
    }

    /**
     *
     * Verifies that the remote client supports the FlexClient API.
     * Legacy clients that do not support this receive a message fault for any messages they send.
     *
     * @param message The message to verify.
     */
    protected void verifyFlexClientSupport(Message message)
    {
        if (FlexContext.getFlexClient() == null)
        {
            MessageException me = new MessageException();
            me.setMessage(REQUIRES_FLEXCLIENT_SUPPORT, new Object[] {message.getDestination()});
            throw me;
        }
    }

    /**
     *
     */
    protected Class<?> createClass(String className)
    {
        return ClassUtil.createClass(className, FlexContext.getMessageBroker() == null ? null :
                    FlexContext.getMessageBroker().getClassLoader());
    }

    // This should match with ChannelSetting.parseClientUri
    private void parseClientUrl(String contextPath)
    {
        if (!clientContextParsed)
        {
            String channelEndpoint = url.trim();

            // either {context-root} or {context.root} is legal
            channelEndpoint = StringUtils.substitute(channelEndpoint, "{context-root}", ConfigurationConstants.CONTEXT_PATH_TOKEN);

            if ((contextPath == null) && (channelEndpoint.indexOf(ConfigurationConstants.CONTEXT_PATH_TOKEN) != -1))
            {
                // context root must be specified before it is used
                ConfigurationException e = new ConfigurationException();
                e.setMessage(ConfigurationConstants.UNDEFINED_CONTEXT_ROOT, new Object[]{getId()});
                throw e;
            }

            // simplify the number of combinations to test by ensuring our
            // context path always starts with a slash
            if (contextPath != null && !contextPath.startsWith("/"))
            {
                contextPath = "/" + contextPath;
            }

            // avoid double-slashes from context root by replacing /{context.root}
            // in a single replacement step
            if (channelEndpoint.indexOf(ConfigurationConstants.SLASH_CONTEXT_PATH_TOKEN) != -1)
            {
                // but avoid double-slash for /{context.root}/etc when we have
                // the default context root
                if ("/".equals(contextPath) && !ConfigurationConstants.SLASH_CONTEXT_PATH_TOKEN.equals(channelEndpoint))
                    contextPath = "";

                channelEndpoint = StringUtils.substitute(channelEndpoint, ConfigurationConstants.SLASH_CONTEXT_PATH_TOKEN, contextPath);
            }
            // otherwise we have something like {server.name}:{server.port}{context.root}...
            else
            {
                // but avoid double-slash for {context.root}/etc when we have
                // the default context root
                if ("/".equals(contextPath) && !ConfigurationConstants.CONTEXT_PATH_TOKEN.equals(channelEndpoint))
                    contextPath = "";

                channelEndpoint = StringUtils.substitute(channelEndpoint, ConfigurationConstants.CONTEXT_PATH_TOKEN, contextPath);
            }

            parsedClientUrl = channelEndpoint;
            clientContextParsed = true;
        }
    }

    private int internalParsePort(String url)
    {
        int port = ChannelSettings.parsePort(url);
        // If there is no specified port, log an info message as urls without ports are supported
        if (port == 0 && Log.isInfo())
            log.info("No port specified in channel URL:  {0}", new Object[]{url});

        return port == -1? 0 : port; // Replace -1 with 0.
    }

    private void parseUrl(String contextPath)
    {
        // Parse again only if never parsed before or parsed for a different contextPath.
        if (parsedForContext == null || !parsedForContext.equals(contextPath))
        {
            String channelEndpoint = url.toLowerCase().trim();

            // Remove protocol and host info
            String insecureProtocol = getInsecureProtocolScheme() + "://";
            String secureProtocol = getSecureProtocolScheme() + "://";
            if (channelEndpoint.startsWith(secureProtocol) || channelEndpoint.startsWith(insecureProtocol))
            {
                int nextSlash = channelEndpoint.indexOf('/', 8);
                if (nextSlash > 0)
                {
                    channelEndpoint = channelEndpoint.substring(nextSlash);
                }
            }

            // either {context-root} or {context.root} is legal
            channelEndpoint = StringUtils.substitute(channelEndpoint, "{context-root}", ConfigurationConstants.CONTEXT_PATH_TOKEN);

            // Remove context path info
            if (channelEndpoint.startsWith(ConfigurationConstants.CONTEXT_PATH_TOKEN))
            {
                channelEndpoint = channelEndpoint.substring(ConfigurationConstants.CONTEXT_PATH_TOKEN.length());
            }
            else if (channelEndpoint.startsWith(ConfigurationConstants.SLASH_CONTEXT_PATH_TOKEN))
            {
                channelEndpoint = channelEndpoint.substring(ConfigurationConstants.SLASH_CONTEXT_PATH_TOKEN.length());
            }
            else if (contextPath.length() > 0)
            {
                if (channelEndpoint.startsWith(contextPath.toLowerCase()))
                {
                    channelEndpoint = channelEndpoint.substring(contextPath.length());
                }
            }

            // We also don't match on trailing slashes
            if (channelEndpoint.endsWith("/"))
            {
                channelEndpoint = channelEndpoint.substring(0, channelEndpoint.length() - 1);
            }

            parsedUrl = channelEndpoint;
            parsedForContext = contextPath;
        }
    }
}
