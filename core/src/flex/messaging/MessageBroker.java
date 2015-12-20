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
package flex.messaging;

import flex.management.ManageableComponent;
import flex.management.runtime.messaging.MessageBrokerControl;
import flex.management.runtime.messaging.log.LogManager;
import flex.messaging.client.FlexClient;
import flex.messaging.client.FlexClientManager;
import flex.messaging.cluster.ClusterManager;
import flex.messaging.config.ChannelSettings;
import flex.messaging.config.ConfigMap;
import flex.messaging.config.ConfigurationConstants;
import flex.messaging.config.ConfigurationException;
import flex.messaging.config.ConfigurationManager;
import flex.messaging.config.FlexClientSettings;
import flex.messaging.config.SecurityConstraint;
import flex.messaging.config.SecuritySettings;
import flex.messaging.config.SystemSettings;
import flex.messaging.endpoints.AbstractEndpoint;
import flex.messaging.endpoints.Endpoint;
import flex.messaging.endpoints.Endpoint2;
import flex.messaging.factories.JavaFactory;
import flex.messaging.io.BeanProxy;
import flex.messaging.io.PropertyProxyRegistry;
import flex.messaging.log.Log;
import flex.messaging.log.LogCategories;
import flex.messaging.messages.AbstractMessage;
import flex.messaging.messages.AcknowledgeMessage;
import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.Message;
import flex.messaging.security.LoginManager;
import flex.messaging.security.SecurityException;
import flex.messaging.services.AbstractService;
import flex.messaging.services.Service;
import flex.messaging.services.ServiceException;
import flex.messaging.services.messaging.ThrottleManager;
import flex.messaging.util.Base64;
import flex.messaging.util.ClassUtil;
import flex.messaging.util.ExceptionUtil;
import flex.messaging.util.RedeployManager;
import flex.messaging.util.StringUtils;
import flex.messaging.util.UUIDGenerator;
import flex.messaging.util.UUIDUtils;
import flex.messaging.validators.DeserializationValidator;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The MessageBroker is the hub of message traffic. It has a number of endpoints which send and
 * receive messages over the network, and it has a number of
 * services that are message destinations. The broker routes
 * decoded messages received by endpoints to services based
 * on the service destination specified in each message.
 * The broker also has a means of pushing messages back through
 * endpoints to clients.
 */
public class MessageBroker extends ManageableComponent
{
    //--------------------------------------------------------------------------
    //
    // Public Static Constants
    //
    //--------------------------------------------------------------------------

    /**
     * Id that the AuthenticationService uses to register itself with the broker.
     */
    public static final String AUTHENTICATION_SERVICE_ID = "authentication-service";

    /**
     * Localized error messages for <code>MessageBroker</code>.
     */
    public static final int ERR_MSG_NO_SERVICE_FOR_DEST = 10004;
    public static final int ERR_MSG_DESTINATION_UNACCESSIBLE = 10005;
    public static final int ERR_MSG_UNKNOWN_REMOTE_CREDENTIALS_FORMAT = 10020;
    public static final int ERR_MSG_NULL_MESSAGE_ID = 10029;
    public static final int ERR_MSG_CANNOT_SERVICE_STOPPED = 10038;
    public static final int ERR_MSG_NULL_ENDPOINT_URL = 10128;
    public static final int ERR_MSG_SERVICE_CMD_NOT_SUPPORTED = 10451;
    public static final int ERR_MSG_URI_ALREADY_REGISTERED = 11109;

    /**
     * Log category for <code>MessageBroker</code>.
     */
    public static final String LOG_CATEGORY = LogCategories.MESSAGE_GENERAL;

    /**
     * Log category that captures startup information for broker's destinations.
     */
    public static final String LOG_CATEGORY_STARTUP_SERVICE = LogCategories.STARTUP_SERVICE;


    public static final String TYPE = "MessageBroker";

    //--------------------------------------------------------------------------
    //
    // Package Protected Static Constants
    //
    //--------------------------------------------------------------------------
    /** The default message broker id when one is not specified in web.xml. */
    public static final String DEFAULT_BROKER_ID = "__default__";

    /** A map of currently available message brokers indexed by message broker id. */
    static final Map<String, MessageBroker> messageBrokers = new HashMap<String, MessageBroker>();

    //--------------------------------------------------------------------------
    //
    // Private Static Constants
    //
    //--------------------------------------------------------------------------

    private static final String LOG_MANAGER_ID = "log";
    private static final Integer INTEGER_ONE = 1;
    private static final String MESSAGEBROKER = "MessageBroker";
    private static final String ENDPOINT = "Endpoint";
    private static final String SERVICE = "Service";

    //--------------------------------------------------------------------------
    //
    // Constructors
    //
    //--------------------------------------------------------------------------

    /**
     *
     * Create a MessageBroker. This constructor will
     * establish collections for routers, endpoints,
     * and services.
     */
    public MessageBroker()
    {
        this(true, null);
    }


    public MessageBroker(boolean enableManagement)
    {
        this(enableManagement, null);
    }


    public MessageBroker(boolean enableManagement, String mbid)
    {
        this(enableManagement, mbid, MessageBroker.class.getClassLoader());
    }


    public MessageBroker(boolean enableManagement, String mbid, ClassLoader loader)
    {
        super(enableManagement);
        classLoader = loader;
        attributes = new ConcurrentHashMap<String, Object>();
        destinationToService = new ConcurrentHashMap<String, String>();
        endpoints = new LinkedHashMap<String, Endpoint>();
        services = new LinkedHashMap<String, Service>();
        servers = new LinkedHashMap<String, Server>();
        factories = new HashMap<String, FlexFactory>();
        registeredEndpoints = new HashMap<String, String>();

        // Add the built-in java factory
        addFactory("java", new JavaFactory());

        setId(mbid);

        log = Log.createLog();

        clusterManager = new ClusterManager(this);
        systemSettings = new SystemSettings();

        if (isManaged())
        {
            controller = new MessageBrokerControl(this);
            controller.register();
            setControl(controller);

           logManager = new LogManager();
           logManager.setLog(log);
           logManager.setParent(this);
           logManager.setupLogControl();
           logManager.initialize(LOG_MANAGER_ID, null);
        }
    }

    //--------------------------------------------------------------------------
    //
    // Variables
    //
    //--------------------------------------------------------------------------

    private Map<String, Object> attributes;
    /**
     * Map of attribute ids of Application or Session level scoped destination assemblers
     * to the number of active destinations referring to.
     */
    private final Map<String, Integer> attributeIdRefCounts = new HashMap<String, Integer>();
    private Map<String, ChannelSettings> channelSettings;
    private ClassLoader classLoader;
    private ClusterManager clusterManager;
    private MessageBrokerControl controller;
    private List<String> defaultChannels;
    private DeserializationValidator deserializationValidator;
    private Map<String, String> destinationToService; // destiantionId to serviceId map.
    private Map<String, Endpoint> endpoints;
    private boolean enforceEndpointValidation;
    private Map<String, FlexFactory> factories;
    private FlexClientManager flexClientManager;
    private FlexClientSettings flexClientSettings;
    private FlexSessionManager flexSessionManager;
    private PathResolver externalPathResolver;
    private InternalPathResolver internalPathResolver;
    private Log log;
    private LogManager logManager;
    private LoginManager loginManager;
    private RedeployManager redeployManager;
    private Map<String, String> registeredEndpoints;
    private SecuritySettings securitySettings;
    private Map<String, Service> services;
    private Map<String, Server> servers;
    private final ConcurrentHashMap<String, ServiceValidationListener> serviceValidationListeners = new ConcurrentHashMap<String, ServiceValidationListener>();
    private ServletContext servletContext;
    private SystemSettings systemSettings;
    private Class<? extends ThrottleManager> throttleManagerClass = ThrottleManager.class; // The default ThrottleManager class.
    private UUIDGenerator uuidGenerator;

    //--------------------------------------------------------------------------
    //
    // Public Methods
    //
    //--------------------------------------------------------------------------

    /**
     * Sets the id of the <code>MessageBroker</code>. If id is null, uses the
     * default broker id.
     *
     *
     */
    @Override public void setId(String id)
    {
        if (id == null)
            id = DEFAULT_BROKER_ID;

        super.setId(id);
    }

    /**
     * Retrieves a message broker with the supplied id.  This is defined via
     * the servlet init parameter messageBrokerId.  If no messageBrokerId is supplied, pass
     * in a null value for the id parameter.
     *
     * In case null is passed, the method tries to lookup the broker from current FlexContext.
     * If not available, it uses default ID to lookup the message broker.
     *
     * @param id The id of the message broker to retrieve.
     * @return The <code>MessageBroker</code> for the supplied id.
     */
    public static MessageBroker getMessageBroker(String id)
    {
        if (id == null)
        {
            // If available, return the broker from FlexContext
            MessageBroker broker = FlexContext.getMessageBroker();
            if (broker != null)
            {
                return broker;
            }

            id = DEFAULT_BROKER_ID;
        }

        return messageBrokers.get(id);
    }

    /**
     * Start the message broker's endpoints and services.
     *
     */
    @Override public void start()
    {
        if (isStarted())
            return;

        /*
         * J2EE can be a real pain in terms of getting the right class loader so dump out
         * some detailed info about what is going on.
         */
        if (Log.isDebug())
        {
            StringBuffer sb = new StringBuffer(100);
            if (classLoader == MessageBroker.class.getClassLoader())
                sb.append(" the MessageBroker's class loader");
            if (classLoader == Thread.currentThread().getContextClassLoader())
            {
                if (sb.length() > 0) sb.append(" and");
                sb.append(" the context class loader");
            }
            if (sb.length() == 0)
                sb.append(" not the context or the message broker's class loader");
            Log.getLogger(LogCategories.CONFIGURATION).debug(
                    "MessageBroker id: " + getId() + " classLoader is:" +
                    sb.toString() + " (" + "classLoader " + ClassUtil.classLoaderToString(classLoader));
        }

        // Catch any startup errors and log using our log machinery, then rethrow to trigger shutdown.
        try
        {
            // MessageBroker doesn't call super.start() because it doesn't need the
            // usual validation that other components need
            setStarted(true);

            registerMessageBroker();
            if (flexClientManager == null)
            {
                flexClientManager = new FlexClientManager(isManaged(), this);
            }
            flexClientManager.start();
            flexSessionManager = new FlexSessionManager(isManaged(), this);
            flexSessionManager.start();
            if (systemSettings == null)
            {
                systemSettings = new SystemSettings();
            }
            startServices();
            loginManager.start();
            startEndpoints();
            startServers();
            redeployManager.start();
        }
        catch (Exception e)
        {
            if (Log.isError())
                Log.getLogger(LogCategories.CONFIGURATION).error("MessageBroker failed to start: " + ExceptionUtil.exceptionFollowedByRootCausesToString(e));

            // Rethrow.
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Stop the broker's endpoints, clusters, and services.
     *
     */
    @Override public void stop()
    {
        if (!isStarted())
            return;

        if (Log.isDebug())
            Log.getLogger(LogCategories.CONFIGURATION).debug("MessageBroker stopping: " + getId());

        serviceValidationListeners.clear();

        flexSessionManager.stop();
        flexClientManager.stop();
        stopServers();
        stopEndpoints();

        // set this MB in FlexContext as it is needed for reference counts in destination stopping
        FlexContext.setThreadLocalMessageBroker(this);
        stopServices();
        FlexContext.setThreadLocalMessageBroker(null);

        if (loginManager != null)
            loginManager.stop();
        try
        {
            if (redeployManager != null)
                redeployManager.stop();
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
        clusterManager.destroyClusters();

        super.stop();
        unRegisterMessageBroker();

        // clear static proxy caches
        BeanProxy.clear();
        PropertyProxyRegistry.release();

        // clear system settings
        systemSettings.clear();
        systemSettings = null;

        if (Log.isDebug())
            Log.getLogger(LogCategories.CONFIGURATION).debug("MessageBroker stopped: " + getId());
    }

    /**
     * Returns an <tt>Iterator</tt> containing the current names that attributes have been bound
     * to the <tt>MessageBroker</tt> under.
     * Use {@link #getAttribute(String)} to retrieve an attribute value.
     *
     * @return An iterator containing the current names of the attributes.
     */
    public Iterator<String> getAttributeNames()
    {
        return attributes.keySet().iterator();
    }

    /**
     * Returns the attribute value bound to the <tt>MessageBroker</tt> under the provided name.
     *
     * @param name The attribute name.
     * @return Object the attribute object
     */
    public Object getAttribute(String name)
    {
        return attributes.get(name);
    }

    /**
     * Binds an attribute value to the <tt>MessageBroker</tt> under the provided name.
     *
     * @param name The attribute name.
     * @param value The attribute value.
     */
    public void setAttribute(String name, Object value)
    {
        if (value == null)
            removeAttribute(name);
        else
            attributes.put(name, value);
    }

    /**
     * Removes the attribute with the given name from the <tt>MessageBroker</tt>.
     *
     * @param name The attribute name.
     */
    public void removeAttribute(String name)
    {
        attributes.remove(name);
    }

    /**
     * Returns the deserialization validator of the <tt>MessageBroker</tt> or null
     * if none exists.
     *
     * @return The deserialization validator of the <tt>MessageBroker</tt> or null
     * if none exists.
     */
    public DeserializationValidator getDeserializationValidator()
    {
        return deserializationValidator;
    }

    /**
     * Sets the deserialization validator of the <tt>MessageBroker</tt>.
     *
     * @param deserializationValidator The deserialization validator.
     */
    public void setDeserializationValidator(DeserializationValidator deserializationValidator)
    {
        this.deserializationValidator = deserializationValidator;
    }

    public void setExternalPathResolver(PathResolver externalPathResolver)
    {
        this.externalPathResolver = externalPathResolver;
    }


    public void setInternalPathResolver(InternalPathResolver internalPathResolver)
    {
        this.internalPathResolver = internalPathResolver;
    }


    public InputStream resolveExternalPath(String filename) throws IOException
    {
        return externalPathResolver.resolve(filename);
    }


    public InputStream resolveInternalPath(String filename) throws IOException
    {
        return internalPathResolver.resolve(filename);
    }


    public interface PathResolver
    {
        InputStream resolve(String filename) throws IOException;
    }

    /**
     * This interface is being kept for backwards compatibility.
     *
     */
    public interface InternalPathResolver extends PathResolver
    {
        // No-op.
    }


    public ClusterManager getClusterManager()
    {
        return clusterManager;
    }

    /**
     *
     * Add a <code>Server</code> to the broker's collection.
     *
     * @param server <code>Server</code> to be added.
     */
    public void addServer(Server server)
    {
        if (server == null)
        {
            // Cannot add null ''{0}'' to the ''{1}''
            ConfigurationException ex = new ConfigurationException();
            ex.setMessage(ConfigurationConstants.NULL_COMPONENT, new Object[]{"Server", MESSAGEBROKER});
            throw ex;
        }

        String id = server.getId();

        if (id == null)
        {
            // Cannot add ''{0}'' with null id to the ''{1}''
            ConfigurationException ex = new ConfigurationException();
            ex.setMessage(ConfigurationConstants.NULL_COMPONENT_ID, new Object[]{"Server", MESSAGEBROKER});
            throw ex;
        }

        // No need to add if server is already added
        Server currentServer = getServer(id);
        if (currentServer == server)
            return;

        // Do not allow servers with the same id
        if (currentServer != null)
        {
            // Cannot add a ''{0}'' with the id ''{1}'' that is already registered with the ''{2}''
            ConfigurationException ex = new ConfigurationException();
            ex.setMessage(ConfigurationConstants.DUPLICATE_COMPONENT_ID, new Object[]{"Server", id, MESSAGEBROKER});
            throw ex;
        }

        servers.put(id, server);
    }

    /**
     *
     * Returns the <code>Server</code> with the specified id.
     *
     * @param id The id of the <code>Server</code>/
     * @return The <code>Server</code> with the specified id or null if no
     * <code>Server</code> with the id exists.
     */
    public Server getServer(String id)
    {
        return servers.get(id);
    }

    /**
     *
     * Stops and removes the <code>Server</code> from the set of shared servers managed by the <code>MessageBroker</code>.
     *
     * @param id The id of the <code>Server</code> to remove.
     * @return <code>Server</code> that has been removed or <code>null</code> if it doesn't exist.
     */
    public Server removeServer(String id)
    {
        Server server = servers.get(id);
        if (server != null)
        {
            server.stop();
            servers.remove(id);
        }
        return server;
    }

    /**
     *
     * Creates an <code>Endpoint</code> instance, sets its id and url.
     * It further sets the endpoint manageable if the <code>MessageBroker</code>
     * is manageable, and assigns its <code>MessageBroker</code> to the
     * <code>MessageBroker</code> that created it.
     *
     * @param id The id of the endpoint.
     * @param url The url of the endpoint.
     * @param className The class name of the endpoint.
     *
     * @return The created <code>Endpoint</code> instance.
     */
    public Endpoint createEndpoint(String id, String url, String className)
    {
        Class endpointClass = ClassUtil.createClass(className, getClassLoader());

        Endpoint endpoint = (Endpoint)ClassUtil.createDefaultInstance(endpointClass, Endpoint.class);
        endpoint.setId(id);
        endpoint.setUrl(url);
        endpoint.setManaged(isManaged());
        endpoint.setMessageBroker(this);

        return endpoint;
    }

    /**
     *
     * Add an endpoint to the broker's collection. Broker will accept the endpoint
     * to be added only if the endpoint is not null, it does not have null id or
     * url, and it does not have the same id or url as another endpoint.
     *
     * @param endpoint Endpoint to be added.
     */
    public void addEndpoint(Endpoint endpoint)
    {
        if (endpoint == null)
        {
            // Cannot add null ''{0}'' to the ''{1}''
            ConfigurationException ex = new ConfigurationException();
            ex.setMessage(ConfigurationConstants.NULL_COMPONENT, new Object[]{ENDPOINT, MESSAGEBROKER});
            throw ex;
        }

        String id = endpoint.getId();

        if (id == null)
        {
            // Cannot add ''{0}'' with null id to the ''{1}''
            ConfigurationException ex = new ConfigurationException();
            ex.setMessage(ConfigurationConstants.NULL_COMPONENT_ID, new Object[]{ENDPOINT, MESSAGEBROKER});
            throw ex;
        }

        // No need to add if endpoint is already added
        if (getEndpoint(id) == endpoint)
            return;

        // Do not allow endpoints with the same id
        if (getEndpoint(id) != null)
        {
            // Cannot add a ''{0}'' with the id ''{1}'' that is already registered with the ''{2}''
            ConfigurationException ex = new ConfigurationException();
            ex.setMessage(ConfigurationConstants.DUPLICATE_COMPONENT_ID, new Object[]{ENDPOINT, id, MESSAGEBROKER});
            throw ex;
        }

        // Add the endpoint only if its url is not null and it is not registered
        // by another channel
        checkEndpointUrl(id, endpoint.getUrl());

        // Finally add the endpoint to endpoints map
        endpoints.put(id, endpoint);
    }

    /**
     *
     * Returns the <code>Endpoint</code> with the specified id.
     *
     * @param id The id of the <code>Endpoint</code>/
     * @return The <code>Endpoint</code> with the specified id or null if no <code>Endpoint</code> with the id exists.
     */
    public Endpoint getEndpoint(String id)
    {
        return endpoints.get(id);
    }

    /**
     *
     * Retrieve the map of all endpoints in this broker.
     *
     * @return the map of all endpoints in this broker
     */
    public Map<String, Endpoint> getEndpoints()
    {
        return endpoints;
    }

    /**
     *
     * Retrieve an endpoint based on a requested URL path. Two endpoints should not be
     * registered to the same path.
     *
     * @param path the URL path
     * @param contextPath the context path
     * @return endpoint based on a requested URL path
     */
    public Endpoint getEndpoint(String path, String contextPath)
    {
        for (String id : endpoints.keySet())
        {
            Endpoint e = endpoints.get(id);

            if (matchEndpoint(path, contextPath, e))
            {
                return e;
            }
        }
        MessageException lme = new MessageException();
        lme.setMessage(10003, new Object[] {path});
        throw lme;
    }

    /**
     *
     * Removes an endpoint from the <code>MessageBroker</code>.
     *
     * @param id The id of the endpoint.
     * @return The removed endpoint.
     */
    public Endpoint removeEndpoint(String id)
    {
        Endpoint endpoint = getEndpoint(id);
        if (endpoint != null)
        {
            endpoint.stop();
            endpoints.remove(id);
        }
        return endpoint;
    }

    /**
     * Returns whether the endpoint validation is enforced on the server, regardless
     * of whether client requested endpoint validation or not.
     *
     * @return True if the endpoint validation is enforced on the server, regardless
     * of whether client requested endpoint validation or not.
     */
    public boolean isEnforceEndpointValidation()
    {
        return enforceEndpointValidation;
    }

    /**
     * Sets whether the endpoint validation is enforced on the server, regardless
     * of whether client requested endpoint validation or not.
     *
     * @param enforceEndpointValidation The endpoint validation flag.
     */
    public void setEnforceEndpointValidation(boolean enforceEndpointValidation)
    {
        this.enforceEndpointValidation = enforceEndpointValidation;
    }

    /**
     * Returns the <code>FlexFactory</code> with the specified id.
     *
     * @param id The id of the <code>FlexFactory</code>.
     * @return The <code>FlexFactory</code> with the specified id or null if no
     * factory with the id exists.
     */
    public FlexFactory getFactory(String id)
    {
        return factories.get(id);
    }

    /**
     * Returns the map of <code>FlexFactory</code> instances.
     *
     * @return The map of <code>FlexFactory</code> instances.
     */
    public Map<String, FlexFactory> getFactories()
    {
       return factories;
    }

    /**
     * Registers a factory with the <code>MessageBroker</code>.
     *
     * @param id The id of the factory.
     * @param factory <code>FlexFactory</code> instance.
     */
    public void addFactory(String id, FlexFactory factory)
    {
        if (id == null)
        {
            // Cannot add ''{0}'' with null id to the ''{1}''
            ConfigurationException ex = new ConfigurationException();
            ex.setMessage(ConfigurationConstants.NULL_COMPONENT_ID, new Object[]{"FlexFactory", MESSAGEBROKER});
            throw ex;
        }
        // No need to add if factory is already added
        if (getFactory(id) == factory)
        {
            return;
        }
        // Do not allow multiple factories with the same id
        if (getFactory(id) != null)
        {
            // Cannot add a ''{0}'' with the id ''{1}'' that is already registered with the ''{2}''
            ConfigurationException ex = new ConfigurationException();
            ex.setMessage(ConfigurationConstants.DUPLICATE_COMPONENT_ID, new Object[]{"FlexFactory", id, MESSAGEBROKER});
            throw ex;
        }
        factories.put(id, factory);
    }

    /**
     * Removes the <code>FlexFactory</code> from the list of factories known
     * by the <code>MessageBroker</code>.
     *
     * @param id The id of the <code>FlexFactory</code>.
     * @return <code>FlexFactory</code> that has been removed.
     */
    public FlexFactory removeFactory(String id)
    {
        FlexFactory factory = getFactory(id);
        if (factory != null)
        {
            factories.remove(id);
        }
        return factory;
    }

    /**
     * Returns the <code>Service</code> with the specified id.
     *
     * @param id The id of the <code>Service</code>/
     * @return The <code>Service</code> with the specified id or null if no
     * <code>Service</code> with the id exists.
     */
    public Service getService(String id)
    {
        return services.get(id);
    }

    /**
     * Return a service of the specific type.
     * The current list of services is searched for the specified class name.
     * If there is no service of the specified type, null is  returned.
     * If there is more than one service of the same type it is undefined which instance is returned.
     * If more than one service of a specific type is configured,
     * callers should use {@link #getService(String)} and provide the service id of the specific service,
     * or {@link #getServices()} to get access to the map of all registered services.
     *
     * @param type the fully qualified class name of the service implementation.
     * @return a service or null if not found.
     */
    public Service getServiceByType(String type)
    {
        for (Service svc : services.values())
        {
            if (svc.getClass().getName().equals(type))
            {
                return svc;
            }
        }
        return null;
    }

    /**
     * Returns the Map of <code>Service</code> instances.
     *
     * @return The Map of <code>Service</code> instances.
     */
    public Map<String, Service> getServices()
    {
        return services;
    }

    /**
     * Returns a <tt>ConfigMap</tt> of service and channel properties that the client
     * needs.
     *
     * @param endpoint Endpoint used to filter the destinations of the service;
     * no filtering is done if the endpoint is null.
     * @return ConfigMap of server properties.
     */
    public ConfigMap describeServices(Endpoint endpoint)
    {
       return describeServices(endpoint, true);
    }

    /**
     *
     * Returns a <tt>ConfigMap</tt> of service and channel properties that the client
     * needs.
     * The <tt>allDestinations</tt> flag controls whether configuration for all
     * destinations or only reliable client destinations is returned.
     *
     * @param endpoint Endpoint used to filter the destinations of the service.
     * No filtering is done if the endpoint is null.
     * @param onlyReliable When false, configuration for all destinations is
     * returned instead of only reliable destinations.
     * @return ConfigMap of service properties.
     */
    public ConfigMap describeServices(Endpoint endpoint, boolean onlyReliable)
    {
        // Let the service validation listeners know about the configuration change.
        if (!serviceValidationListeners.isEmpty())
        {
            for (Enumeration<ServiceValidationListener> iter = serviceValidationListeners.elements(); iter.hasMoreElements();)
                iter.nextElement().validateServices();
        }

        ConfigMap servicesConfig = new ConfigMap();

        // Keep track of channel ids as we encounter them so we can generate
        // the channel properties that might be needed by the client.
        ArrayList<String> channelIds = new ArrayList<String>();
        if (endpoint == null)
        {
            for (Endpoint endpointToAdd: getEndpoints().values())
                channelIds.add(endpointToAdd.getId());
        }
        else
        {
            channelIds.add(endpoint.getId());
        }

        if (defaultChannels != null)
        {
            ConfigMap defaultChannelsMap = new ConfigMap();
            for (Object defaultChannel : defaultChannels)
            {
                String id = (String) defaultChannel;
                ConfigMap channelConfig = new ConfigMap();
                channelConfig.addProperty(ConfigurationConstants.REF_ATTR, id);
                defaultChannelsMap.addProperty(ConfigurationConstants.CHANNEL_ELEMENT, channelConfig);
                if (!channelIds.contains(id))
                    channelIds.add(id);
            }
            if (defaultChannelsMap.size() > 0)
                servicesConfig.addProperty(ConfigurationConstants.DEFAULT_CHANNELS_ELEMENT, defaultChannelsMap);
        }

        for (Service service : services.values())
        {
            ConfigMap serviceConfig = service instanceof AbstractService?
                    ((AbstractService)service).describeService(endpoint, onlyReliable) : service.describeService(endpoint);
            if (serviceConfig != null && serviceConfig.size() > 0)
                servicesConfig.addProperty(ConfigurationConstants.SERVICE_ELEMENT, serviceConfig);
        }

        // Need to send channel properties again in case the client didn't
        // compile in services-config.xml and hence doesn't have channels section
        // of the configuration - but only if channel/endpoint is not tagged as "remote"!
        ConfigMap channels = new ConfigMap();
        for (String id : channelIds)
        {
            Endpoint currentEndpoint = getEndpoint(id);
            if (currentEndpoint instanceof AbstractEndpoint && ((AbstractEndpoint)currentEndpoint).isRemote())
            {
                continue; // Client already has configuration for "remote" endpoint by other means.
            }

            ConfigMap channel = currentEndpoint.describeEndpoint();
            if (channel != null && channel.size() > 0)
                channels.addProperty(ConfigurationConstants.CHANNEL_ELEMENT, channel);
        }
        if (channels.size() > 0)
            servicesConfig.addProperty(ConfigurationConstants.CHANNELS_ELEMENT, channels);

        if (Log.isDebug())
            Log.getLogger(ConfigurationManager.LOG_CATEGORY).debug(
                    "Returning service description for endpoint: " +
                    (endpoint == null? "all" : endpoint.getId()) + " config: " + servicesConfig);

        return servicesConfig;
    }

    /**
     * Add a listener for the describeServices callback.  The describeServices listener
     * is called before any execution of the describeServices method.
     *
     * @param id Identifier of the listener to add
     * @param listener The listener callback
     */
    public void addServiceValidationListener(String id, ServiceValidationListener listener)
    {
        if (listener != null)
        {
            serviceValidationListeners.putIfAbsent(id, listener);
        }
    }

    /**
     * Returns an <tt>Iterator</tt> for all <tt>ServiceValidationListeners</tt> currently
     * registered with the broker.
     *
     * @return An <tt>Iterator</tt> for all registered <tt>ServiceValidationListeners</tt>.
     */
    public Iterator<ServiceValidationListener> getServiceValidationListenerIterator()
    {
        return serviceValidationListeners.values().iterator();
    }

    /**
     * Remove a listener from the describeServices callback.
     *
     * @param id Identifier of the listener to remove
     */
    public void removeServiceValidationListener(String id)
    {
        if (serviceValidationListeners.containsKey(id))
        {
            serviceValidationListeners.remove(id);
        }
    }

    /**
     * Creates a <code>Service</code> instance, sets its id, sets it manageable
     * if the <code>MessageBroker</code> that created it is manageable,
     * and sets its <code>MessageBroker</code> to the <code>MessageBroker</code> that
     * created it.
     *
     * @param id The id of the <code>Service</code>.
     * @param className The class name of the <code>Service</code>.
     *
     * @return The <code>Service</code> instanced created.
     */
    public Service createService(String id, String className)
    {
        Class svcClass = ClassUtil.createClass(className, getClassLoader());

        Service service = (Service)ClassUtil.createDefaultInstance(svcClass, Service.class);
        service.setId(id);
        service.setManaged(isManaged());
        service.setMessageBroker(this);

        return service;
    }

    /**
     * Add a message type -to- service mapping to the broker's collection.
     * When the broker attempts to route a message to a service, it finds the first
     * service capable of handling the message type.
     *
     * Note that <code>Service</code> cannot be null, it cannot have a null
     * id, and it cannot have the same id or type of a <code>Service</code>
     * already registered with the <code>MessageBroker</code>.
     *
     * <code>Service</code> needs to be started if the <code>MessageBroker</code>
     * is already running.
     *
     * @param service The service instance used to handle the messages
     *
     */
    public void addService(Service service)
    {
        if (service == null)
        {
            // Cannot add null ''{0}'' to the ''{1}''
            ConfigurationException ex = new ConfigurationException();
            ex.setMessage(ConfigurationConstants.NULL_COMPONENT, new Object[]{SERVICE, MESSAGEBROKER});
            throw ex;
        }

        String id = service.getId();

        if (id == null)
        {
            // Cannot add ''{0}'' with null id to the ''{1}''
            ConfigurationException ex = new ConfigurationException();
            ex.setMessage(ConfigurationConstants.NULL_COMPONENT_ID, new Object[]{SERVICE, MESSAGEBROKER});
            throw ex;
        }
        // No need to add if service is already added
        if (getService(id) == service)
        {
            return;
        }
        // Do not allow multiple services with the same id
        if (getService(id) != null)
        {
            // Cannot add a ''{0}'' with the id ''{1}'' that is already registered with the ''{2}''
            ConfigurationException ex = new ConfigurationException();
            ex.setMessage(ConfigurationConstants.DUPLICATE_COMPONENT_ID, new Object[]{SERVICE, id, MESSAGEBROKER});
            throw ex;
        }
        // Not supposed to have multiple services of the same type; warn about it
        // but still allow it.
        String type = service.getClass().getName();
        if (getServiceByType(type) != null && Log.isWarn())
            Log.getLogger(LOG_CATEGORY).warn("Adding a service type '{0}' that is already registered with the MessageBroker",
                    new Object[]{type});


        services.put(id, service);

        if (service.getMessageBroker() == null || service.getMessageBroker() != this)
        {
            service.setMessageBroker(this);
        }
    }

    /**
     * Removes the <code>Service</code> from the list of services known
     * by the <code>MessageBroker</code>.
     *
     * @param id The id of the <code>Service</code>.
     * @return Previous <code>Service</code> associated with the id.
     */
    public Service removeService(String id)
    {
        Service service = getService(id);
        if (service != null)
        {
            service.stop();
            services.remove(id);
        }
        return service;
    }

    /**
     * Returns the logger of the <code>MessageBroker</code>.
     *
     * @return Logger of the <code>MessageBroker</code>.
     */
    public Log getLog()
    {
        return log;
    }


    public LogManager getLogManager()
    {
        return logManager;
    }


    public LoginManager getLoginManager()
    {
        return loginManager;
    }


    public void setLoginManager(LoginManager loginManager)
    {
        if (this.loginManager != null && this.loginManager.isStarted())
            this.loginManager.stop();

        this.loginManager = loginManager;

        if (isStarted())
            loginManager.start();
    }


    public FlexClientManager getFlexClientManager()
    {
        return flexClientManager;
    }


    public void setFlexClientManager(FlexClientManager value)
    {
        flexClientManager = value;
    }


    public FlexSessionManager getFlexSessionManager()
    {
        return flexSessionManager;
    }


    public void setFlexSessionManager(FlexSessionManager value)
    {
        flexSessionManager = value;
    }


    public RedeployManager getRedeployManager()
    {
        return redeployManager;
    }


    public void setRedeployManager(RedeployManager redeployManager)
    {
        if (this.redeployManager != null && this.redeployManager.isStarted())
            this.redeployManager.stop();

        this.redeployManager = redeployManager;

        if (isStarted())
            redeployManager.start();
    }


    public Class<? extends ThrottleManager> getThrottleManagerClass()
    {
        return throttleManagerClass;
    }


    public void setThrottleManagerClass(Class<? extends ThrottleManager> throttleManagerClass)
    {
        this.throttleManagerClass = throttleManagerClass;
    }

    /**
     * Returns a UUID either from the UUID generator assigned to <tt>MessageBroker</tt>,
     * or from the <tt>UUIDUtils#createUUID</tt> if there is no assigned UUID generator.
     *
     * @return String the UUID.
     */
    public String createUUID()
    {
        return uuidGenerator != null? uuidGenerator.createUUID() : UUIDUtils.createUUID();
    }

    /**
     * Returns the custom <tt>UUIDGenerator</tt> used by the <tt>MessageBroker</tt>
     * for NIO-HTTP session cookie value and <tt>FlexClient</tt> id generation or null if
     *  the default UUID generator, <tt>UUIDUtils</tt>, is being used.
     *
     * @return The custom <tt>UUIDGenerator</tt> used by <tt>MessageBroker</tt> or null.
     */
    public UUIDGenerator getUUIDGenerator()
    {
        return uuidGenerator;
    }


    /**
     * Sets the custom <tt>UUIDGenerator</tt> used by the <tt>MessageBroker</tt>
     * for NIO-HTTP session cookie value and <tt>FlexClient</tt> id generation.
     *
     * @param value The custom <tt>UUIDGenerator</tt>.
     */
    public void setUUIDGenerator(UUIDGenerator value)
    {
        uuidGenerator = value;
    }

    /**
     * Returns the list of channel ids known to the <code>MessageBroker</code>.
     *
     * @return The list of channel ids.
     */
    public List<String> getChannelIds()
    {
        return (endpoints != null && endpoints.size() != 0)? new ArrayList<String>(endpoints.keySet()) : null;
    }


    public ChannelSettings getChannelSettings(String ref)
    {
        return channelSettings.get(ref);
    }


    public Map<String, ChannelSettings> getAllChannelSettings()
    {
        return channelSettings;
    }


    public void setChannelSettings(Map<String, ChannelSettings> channelSettings)
    {
        this.channelSettings = channelSettings;
    }

    /**
     * Returns the default channel ids of the MessageBroker. If a service
     * specifies its own list of channels it overrides these defaults.
     *
     * @return Default channel ids of the MessageBroker.
     */
    public List<String> getDefaultChannels()
    {
        return defaultChannels;
    }

    /**
     * Adds the channel id to the list of default channel ids.
     *
     * @param id The id of the channel to add to the list of default channel ids.
     */
    public void addDefaultChannel(String id)
    {
        if (defaultChannels == null)
            defaultChannels = new ArrayList<String>();
        else if (defaultChannels.contains(id))
            return;

        List<String> channelIds = getChannelIds();
        if (channelIds == null || !channelIds.contains(id))
        {
            // No channel with id ''{0}'' is known by the MessageBroker.
            if (Log.isWarn())
            {
                Log.getLogger(LOG_CATEGORY).warn("No channel with id '{0}' is known by the MessageBroker." +
                        " Not adding the channel.",
                        new Object[]{id});
            }
            return;
        }
        defaultChannels.add(id);
    }

    /**
     * Sets the default channel ids of the MessageBroker.
     *
     * @param ids Default channel ids of the MessageBroker.
     */
    public void setDefaultChannels(List<String> ids)
    {
        if (ids != null)
        {
            List<String> channelIds = getChannelIds();
            for (Iterator<String> iter = ids.iterator(); iter.hasNext();)
            {
                String id = iter.next();
                if (channelIds == null || !channelIds.contains(id))
                {
                    iter.remove();
                    if (Log.isWarn())
                    {
                        Log.getLogger(LOG_CATEGORY).warn("No channel with id '{0}' is known by the MessageBroker." +
                                " Not adding the channel.",
                                new Object[]{id});
                    }
                }
            }
        }
        defaultChannels = ids;
    }

    /**
     * Removes the channel id from the list of default channel ids.
     *
     * @param id The id of the channel to remove from the list of default channel ids.
     * @return <code>true</code> if the list contained the channel id.
     */
    public boolean removeDefaultChannel(String id)
    {
        return defaultChannels != null && defaultChannels.remove(id);
    }

    /**
     * Returns the <code>SecurityConstraint</code> with the indicated
     * reference id.
     *
     * @param ref The reference of the <code>SecurityConstraint</code>
     * @return The <code>SecurityConstraint</code> with the indicated reference id.
     */
    public SecurityConstraint getSecurityConstraint(String ref)
    {
        return getSecuritySettings().getConstraint(ref);
    }


    public ServletContext getServletContext()
    {
        return servletContext;
    }


    public SecuritySettings getSecuritySettings()
    {
        return securitySettings;
    }


    public void setSecuritySettings(SecuritySettings securitySettings)
    {
        this.securitySettings = securitySettings;
    }


    public SystemSettings getSystemSettings()
    {
        return systemSettings;
    }


    public void setSystemSettings(SystemSettings l)
    {
        systemSettings = l;
    }


    public FlexClientSettings getFlexClientSettings()
    {
        return flexClientSettings;
    }


    public void setFlexClientSettings(FlexClientSettings value)
    {
        flexClientSettings = value;
    }


    public void initThreadLocals()
    {
        // No thread-locals anymore, so no-op.
    }

    /**
     * You can call this method in order to send a message from your code into
     * the message routing system.  The message is routed to a service that
     * is defined to handle messages of this type.  Once the service is identified,
     * the destination property of the message is used to find a destination
     * configured for that service.  The adapter defined for that destination
     * is used to handle the message.
     *
     * @param message  The message to be routed to a service
     * @param endpoint This can identify the endpoint that is sending the message
     * but it is currently not used so you may pass in null.
     * @return <code>AcknowledgeMessage</code> with result.
     */
    public AcknowledgeMessage routeMessageToService(Message message, Endpoint endpoint)
    {
        // Make sure message has a messageId
        checkMessageId(message);

        Object serviceResult = null;
        boolean serviced = false;
        Service service = null;
        String destId = message.getDestination();
        try
        {
            String serviceId = destId != null ? destinationToService.get(destId) : null;

            if ((serviceId == null) && (destId != null) && (!serviceValidationListeners.isEmpty()))
            {
                for (Enumeration<ServiceValidationListener> iter = serviceValidationListeners.elements(); iter.hasMoreElements();)
                {
                    iter.nextElement().validateDestination(destId);
                }
                serviceId = destinationToService.get(destId);
            }

            if (serviceId != null)
            {
                service = services.get(serviceId);
                serviced = true;
                Destination destination = service.getDestination(destId);
                inspectOperation(message, destination);
                // Remove the validate endpoint header if it was set.
                if (message.headerExists(Message.VALIDATE_ENDPOINT_HEADER))
                    message.getHeaders().remove(Message.VALIDATE_ENDPOINT_HEADER);

                if (Log.isDebug())
                    Log.getLogger(getLogCategory(message)).debug(
                            "Before invoke service: " + service.getId() + StringUtils.NEWLINE +
                            "  incomingMessage: " + message + StringUtils.NEWLINE);

                extractRemoteCredentials(service, message);
                serviceResult = service.serviceMessage(message);
            }

            if (!serviced)
            {
                MessageException lme = new MessageException();
                // The supplied destination id is not registered with any service.
                lme.setMessage(ERR_MSG_NO_SERVICE_FOR_DEST);
                throw lme;
            }

            if (Log.isDebug())
            {
                String debugServiceResult = Log.getPrettyPrinter().prettify(serviceResult);
                Log.getLogger(getLogCategory(message)).debug(
                     "After invoke service: " + service.getId() + StringUtils.NEWLINE +
                     "  reply: " + debugServiceResult + StringUtils.NEWLINE);
            }

            AcknowledgeMessage ack;
            if (serviceResult instanceof AcknowledgeMessage)
            {
                // service will return an ack if they need to transform it in some
                // service-specific way (paging is an example)
                ack = (AcknowledgeMessage)serviceResult;
            }
            else
            {
                // most services will return a result of some sort, possibly null,
                // and expect the broker to compose a message to deliver it
                ack = new AcknowledgeMessage();
                ack.setBody(serviceResult);
            }
            ack.setCorrelationId(message.getMessageId());
            ack.setClientId(message.getClientId());
            return ack;
        }
        catch (MessageException exc)
        {
            exc.logAtHingePoint(message,
                                null, /* No outbound error message at this point. */
                                "Exception when invoking service '" + (service == null ? "(none)" : service.getId()) + "': ");

            throw exc;
        }
        catch (RuntimeException exc)
        {
            Log.getLogger(LogCategories.MESSAGE_GENERAL).error(
                 "Exception when invoking service: " +
                 (service == null ? "(none)" : service.getId()) +
                 StringUtils.NEWLINE +
                 "  with message: " + message + StringUtils.NEWLINE +
                 ExceptionUtil.exceptionFollowedByRootCausesToString(exc) + StringUtils.NEWLINE);

            throw exc;
        }
        catch (Error exc)
        {
            Log.getLogger(LogCategories.MESSAGE_GENERAL).error(
                 "Error when invoking service: " +
                 (service == null ? "(none)" : service.getId()) +
                 StringUtils.NEWLINE +
                 "  with message: " + message + StringUtils.NEWLINE +
                 ExceptionUtil.exceptionFollowedByRootCausesToString(exc) + StringUtils.NEWLINE);

            throw exc;
        }

    }


    public AsyncMessage routeCommandToService(CommandMessage command, Endpoint endpoint)
    {
        // Make sure command has a messageId
        checkMessageId(command);

        String destId = command.getDestination();

        AsyncMessage replyMessage;
        Service service;
        String serviceId;
        Object commandResult = null;
        boolean serviced = false;
        boolean recreateHttpFlexSessionAfterLogin = false;

        // Forward login and logout commands to AuthenticationService
        int operation = command.getOperation();
        if (operation == CommandMessage.LOGIN_OPERATION || operation == CommandMessage.LOGOUT_OPERATION)
        {
            serviceId = AUTHENTICATION_SERVICE_ID;
            recreateHttpFlexSessionAfterLogin = securitySettings.isRecreateHttpSessionAfterLogin()
                &&  operation == CommandMessage.LOGIN_OPERATION && FlexContext.getFlexSession() instanceof HttpFlexSession;
        }
        else
        {
            serviceId = destId != null? destinationToService.get(destId) : null;
        }

        service = serviceId != null? services.get(serviceId) : null;
        if (service != null)
        {
            // Before passing the message to the service, need to check
            // the security constraints.
            Destination destination = service.getDestination(destId);
            if (destination != null)
                inspectOperation(command, destination);

            try
            {
                extractRemoteCredentials(service, command);
                commandResult = service.serviceCommand(command);
                serviced = true;
            }
            catch (UnsupportedOperationException e)
            {
                ServiceException se = new ServiceException();
                se.setMessage(ERR_MSG_SERVICE_CMD_NOT_SUPPORTED, new Object[] {service.getClass().getName()});
                throw se;
            }
            catch (SecurityException se)
            {
                // when a LOGIN message causes a security exception, we want to continue processing here
                // to allow metadata to be sent to clients communicating with runtime destinations.
                // The result will be an error message with a login fault message as well as the metadata
                if (AUTHENTICATION_SERVICE_ID.equals(serviceId))
                {
                    commandResult = se.createErrorMessage();
                    if (Log.isDebug())
                        Log.getLogger(LOG_CATEGORY).debug("Security error for message: " +
                                se.toString() + StringUtils.NEWLINE +
                             "  incomingMessage: " + command + StringUtils.NEWLINE +
                             "  errorReply: " + commandResult);
                    serviced = true;
                }
                else
                {
                    throw se;
                }
            }
        }

        if (recreateHttpFlexSessionAfterLogin)
            recreateHttpFlexSessionAfterLogin();

        if (commandResult == null)
        {
            replyMessage = new AcknowledgeMessage();
        }
        else if (commandResult instanceof AsyncMessage)
        {
            replyMessage = (AsyncMessage)commandResult;
        }
        else
        {
            replyMessage = new AcknowledgeMessage();
            replyMessage.setBody(commandResult);
        }

        // Update the replyMessage body with server configuration if the
        // operation is ping or login and make sure to return the FlexClient Id value.
        if (command.getOperation() == CommandMessage.CLIENT_PING_OPERATION
                || command.getOperation() == CommandMessage.LOGIN_OPERATION)
        {
            boolean needsConfig = false;
            if (command.getHeader(CommandMessage.NEEDS_CONFIG_HEADER) != null)
                needsConfig = ((Boolean)(command.getHeader(CommandMessage.NEEDS_CONFIG_HEADER)));

            // Send configuration information only if the client requested.
            if (needsConfig)
            {
                ConfigMap serverConfig = describeServices(endpoint);
                if (serverConfig.size() > 0)
                    replyMessage.setBody(serverConfig);
            }

            // Record the features available over this endpoint
            double msgVersion = endpoint.getMessagingVersion();
            if (msgVersion > 0)
                replyMessage.setHeader(CommandMessage.MESSAGING_VERSION, new Double(msgVersion));

            // Record the flex client ID
            FlexClient flexClient = FlexContext.getFlexClient();
            if (flexClient != null)
                replyMessage.setHeader(Message.FLEX_CLIENT_ID_HEADER, flexClient.getId());
        }
        else if (!serviced)
        {
            MessageException lme = new MessageException();
            // The supplied destination id is not registered with any service..
            lme.setMessage(ERR_MSG_NO_SERVICE_FOR_DEST);
            throw lme;
        }

        replyMessage.setCorrelationId(command.getMessageId());
        replyMessage.setClientId(command.getClientId());
        if (replyMessage.getBody() instanceof java.util.List)
        {
            replyMessage.setBody(((List) replyMessage.getBody()).toArray());
        }

        if (Log.isDebug())
            Log.getLogger(getLogCategory(command)).debug(
                 "Executed command: " +
                 (service == null ? "(default service)" : "service=" +
                                                          service.getId()) + StringUtils.NEWLINE +
                                                                           "  commandMessage: " + command + StringUtils.NEWLINE +
                                                                           "  replyMessage: " + replyMessage + StringUtils.NEWLINE);

        return replyMessage;
    }

    /**
     * Services call this method in order to send a message
     * to a FlexClient.
     *
     * @param message the message
     * @param messageClient the message client the message should be sent to
     */
    public void routeMessageToMessageClient(Message message, MessageClient messageClient)
    {
        // Make sure message has a messageId
        checkMessageId(message);

        // Route the message and the MessageClient (subscription) to the FlexClient to
        // queue the message for delivery to the remote client.
        // Reset the thread local FlexClient and FlexSession to be specific to the client
        // we're pushing to, and then reset the context back to its original request handling state.
        FlexClient requestFlexClient = FlexContext.getFlexClient();
        FlexSession requestFlexSession = FlexContext.getFlexSession();

        FlexClient pushFlexClient = messageClient.getFlexClient();
        FlexContext.setThreadLocalFlexClient(pushFlexClient);
        FlexContext.setThreadLocalSession(null); // Null because we don't have a currently active endpoint for the push client.
        try
        {
            pushFlexClient.push(message, messageClient);
        }
        finally // Reset thread locals.
        {
            FlexContext.setThreadLocalFlexClient(requestFlexClient);
            FlexContext.setThreadLocalSession(requestFlexSession);
        }
    }

    /**
     *
     * Check that the destination permits access over the endpoint, the security
     * constraint of the destination permits the operation, and the service and
     * the destination the message is targeting are running,
     *
     * @param message The incoming message.
     * @param destination The destination to check against.
     */
    public void inspectOperation(Message message, Destination destination)
    {
        inspectChannel(message, destination);
        loginManager.checkConstraint(destination.getSecurityConstraint());

        Service service = destination.getService();
        if (!service.isStarted())
        {
            // {0} ''{1}'' cannot service message ''{2}'' in stopped state.
            MessageException me = new MessageException();
            me.setMessage(ERR_MSG_CANNOT_SERVICE_STOPPED, new Object[]{SERVICE, service.getId(), message.getMessageId()});
            throw me;
        }

        if (!destination.isStarted())
        {
            // {0} ''{1}'' cannot service message ''{2}'' in stopped state.
            MessageException me = new MessageException();
            me.setMessage(ERR_MSG_CANNOT_SERVICE_STOPPED, new Object[]{"Destination", destination.getId(), message.getMessageId()});
            throw me;
        }
    }

    /**
     *
     * Verify that this destination permits access over this endpoint.
     *
     * @param message The incoming message.
     * @param destination The destination to check against.
     */
    public void inspectChannel(Message message, Destination destination)
    {
        if (!enforceEndpointValidation && message.getHeader(Message.VALIDATE_ENDPOINT_HEADER) == null)
            return;

        String messageChannel = (String)message.getHeader(Message.ENDPOINT_HEADER);
        for (String channelId : destination.getChannels())
        {
            if (channelId.equals(messageChannel))
                return;
        }
        MessageException lme = new MessageException();
        lme.setMessage(ERR_MSG_DESTINATION_UNACCESSIBLE, new Object[] {destination.getId(), messageChannel});
        throw lme;
    }

    /**
     *
     * Returns the logging category to use for a given message.
     *
     * @param message the message
     * @return the logging category to use for a given message
     */
    public String getLogCategory(Message message)
    {
        if (message instanceof AbstractMessage)
            return ((AbstractMessage) message).logCategory();
        return LogCategories.MESSAGE_GENERAL;
    }

    /**
     * This is the class loader used by the system to load user defined classes.
     *
     * @return <code>ClassLoader</code> the system should use to load user defined classes.
     */
    public ClassLoader getClassLoader()
    {
        return classLoader;
    }

    /**
     *
     * Sets the class loader used by the system to load user defined classes.
     *
     * @param classLoader The class loader used by the system to loader user defiend classes.
     */
    public void setClassLoader(ClassLoader classLoader)
    {
        this.classLoader = classLoader;
    }

    /**
     *
     * Used internally by AbstractService to check existence of destination and service id
     * mapping in the destinationToService map.
     *
     * @param destId the destination id
     * @param svcId the service id
     * @param throwException true if an exception should be thrown if something goes wrong
     * @return True if the destination is already registered.
     */
    public boolean isDestinationRegistered(String destId, String svcId, boolean throwException)
    {
        // Do not allow multiple destinations with the same id across services
        if (destinationToService.containsKey(destId))
        {
            if (throwException)
            {
                // Cannot add destination with id ''{0}'' to service with id ''{1}'' because another service with id ''{2}'' already has a destination with the same id.
                ConfigurationException ex = new ConfigurationException();
                ex.setMessage(ConfigurationConstants.DUPLICATE_DEST_ID, new Object[]{destId, svcId, destinationToService.get(destId)});
                throw ex;
            }
            return true;
        }
        return false;
    }

    /**
     *
     * Used internally by AbstractService to add destination and service id
     * mapping to destinationToService map.
     *
     * @param destId Destination id.
     * @param svcId Service id.
     */
    public void registerDestination(String destId, String svcId)
    {
        // Do not allow multiple destinations with the same id across services
        if (destinationToService.containsKey(destId))
        {
            // Cannot add destination with id ''{0}'' to service with id ''{1}'' because another service with id ''{2}'' already has a destination with the same id.
            ConfigurationException ex = new ConfigurationException();
            ex.setMessage(ConfigurationConstants.DUPLICATE_DEST_ID, new Object[]{destId, svcId, destinationToService.get(destId)});
            throw ex;
        }
        destinationToService.put(destId, svcId);
    }

    /**
     *
     * Used internally by AbstractService to remove destination and service id
     * mapping from destinationToService map.
     *
     * @param destId Destination id.
     */
    public void unregisterDestination(String destId)
    {
        destinationToService.remove(destId);
    }

    /**
     *
     * Looks up and returns a destination by id; removing the need to know which service
     * a destination is registered for.
     *
     * @param destId Destination id.
     * @return the Destination oblect for the given destination id
     */
    public Destination getRegisteredDestination(String destId)
    {
        String serviceId = destId != null? destinationToService.get(destId) : null;
        return serviceId != null? getService(serviceId).getDestination(destId) : null;
    }

    /**
     * Increments the count of destinations actively using an Application or Session
     * level scoped assembler identified by the passed in attributeId.
     *
     * @param attributeId Attribute id for the session or application-scoped object.
     */
    public void incrementAttributeIdRefCount(String attributeId)
    {
        synchronized (attributeIdRefCounts)
        {
            Integer currentCount = attributeIdRefCounts.get(attributeId);
            if (currentCount == null)
                attributeIdRefCounts.put(attributeId, INTEGER_ONE);
            else
                attributeIdRefCounts.put(attributeId, currentCount + 1);
        }
    }

    /**
     * Decrements the count of destinations actively using an Application or Session
     * level scoped assembler identified by the passed in attributeId.
     *
     * @param attributeId Attribute id for the session or application-scoped object.
     * @return in the attribute ID ref count after decrement
     */
    public int decrementAttributeIdRefCount(String attributeId)
    {
        synchronized (attributeIdRefCounts)
        {
            Integer currentCount = attributeIdRefCounts.get(attributeId);
            if (currentCount == null)
                return 0;

            int newValue = currentCount -1 ;
            attributeIdRefCounts.put(attributeId, newValue);
            return newValue;
        }
    }


    //--------------------------------------------------------------------------
    //
    // Protected Methods
    //
    //--------------------------------------------------------------------------

    /**
     *
     * Utility method to make sure that message has an assigned messageId.
     *
     * @param message the message that should be checked
     */
    protected void checkMessageId(Message message)
    {
        if (message.getMessageId() == null)
        {
            MessageException lme = new MessageException();
            lme.setMessage(ERR_MSG_NULL_MESSAGE_ID);
            throw lme;
        }
    }

    /**
     *
     * Check the headers for the message for the RemoteCredentials.
     *
     * @param service the service
     * @param message the message
     */
    protected void extractRemoteCredentials(Service service, Message message)
    {
        if (!message.headerExists(Message.REMOTE_CREDENTIALS_HEADER))
            return;

        boolean setting = false;
        String username = null;
        String credentials = null;
        if (message.getHeader(Message.REMOTE_CREDENTIALS_HEADER) instanceof String)
        {
            String encoded = (String)message.getHeader(Message.REMOTE_CREDENTIALS_HEADER);
            if (encoded.length() > 0) //empty string is clearing the credentials
            {
                setting = true;
                Base64.Decoder decoder = new Base64.Decoder();
                decoder.decode(encoded);
                byte[] decodedBytes = decoder.drain();
                String decoded;

                String charset = (String)message.getHeader(Message.REMOTE_CREDENTIALS_CHARSET_HEADER);
                if (charset != null)
                {
                    try
                    {
                        decoded = new String(decodedBytes, charset);
                    }
                    catch (UnsupportedEncodingException ex)
                    {
                        MessageException lme = new MessageException();
                        lme.setMessage(ERR_MSG_UNKNOWN_REMOTE_CREDENTIALS_FORMAT);
                        throw lme;
                    }
                }
                else
                {
                    decoded = new String(decodedBytes);
                }

                int colon = decoded.indexOf(':');
                if (colon > 0 && colon < decoded.length() - 1)
                {
                    username = decoded.substring(0, colon);
                    credentials = decoded.substring(colon + 1);
                }
            }
        }
        else
        {
            MessageException lme = new MessageException();
            lme.setMessage(ERR_MSG_UNKNOWN_REMOTE_CREDENTIALS_FORMAT);
            throw lme;
        }

        if (setting)
        {
            FlexContext.getFlexSession().putRemoteCredentials(
                    new FlexRemoteCredentials(service.getId(),
                            message.getDestination(), username, credentials));
        }
        else
        {
            FlexContext.getFlexSession().clearRemoteCredentials(service.getId(),
                    message.getDestination());
        }
    }

    @Override
    protected String getLogCategory()
    {
        return LOG_CATEGORY;
    }


    public void setServletContext(ServletContext servletContext)
    {
        this.servletContext = servletContext;
    }

    /**
     *
     * This method was added so that Spring-BlazeDS Integration 1.0.2 works with latest BlazeDS binaries
     * Internally, this method simply invokes the setServletContext(...) method
     *
     * @param servletContext ServletContext that should be set.
     */
    protected void setInitServletContext(ServletContext servletContext)
    {
        setServletContext(servletContext);
    }

    protected void recreateHttpFlexSessionAfterLogin()
    {
        FlexSession currentHttpFlexSession = FlexContext.getFlexSession();
        Principal principal = currentHttpFlexSession.getUserPrincipal();
        currentHttpFlexSession.invalidate(); // This will recreate a new session.

        FlexSession newHttpFlexSession = FlexContext.getFlexSession();
        newHttpFlexSession.setUserPrincipal(principal);
    }

    /**
     * Start all of the broker's endpoints.
     *
     *
     */
    protected void startEndpoints()
    {
        for (Endpoint endpoint : endpoints.values())
        {
            if (endpoint instanceof AbstractEndpoint && ((AbstractEndpoint)endpoint).isRemote())
                continue; // Local representation of remote endpoints are not started.
            endpoint.start();
        }
    }

    /**
     * Stop all of the broker's endpoints.
     */
    protected void stopEndpoints()
    {
        for (Endpoint endpoint : endpoints.values())
        {
            if (endpoint instanceof AbstractEndpoint && ((AbstractEndpoint)endpoint).isRemote())
                continue; // Local representation of remote endpoints are not stopped.
            endpoint.stop();
        }
    }

    //--------------------------------------------------------------------------
    //
    // Private Methods
    //
    //--------------------------------------------------------------------------

    /**
     *
     */
    private void checkEndpointUrl(String id, String endpointUrl)
    {
        // Do not allow endpoints with null url property.
        if (endpointUrl == null)
        {
            // Cannot add ''{0}'' with null url to the ''{1}''
            ConfigurationException ex = new ConfigurationException();
            ex.setMessage(ERR_MSG_NULL_ENDPOINT_URL, new Object[]{ENDPOINT, MESSAGEBROKER});
            throw ex;
        }

        String parsedEndpointURI = ChannelSettings.removeTokens(endpointUrl);

        // first check the original URI
        if (registeredEndpoints.containsKey(parsedEndpointURI))
        {
          ConfigurationException ce = new ConfigurationException();
          ce.setMessage(ERR_MSG_URI_ALREADY_REGISTERED, new Object[] {id, parsedEndpointURI,
                        registeredEndpoints.get(parsedEndpointURI)});
          throw ce;
        }

        // add the original URI to the registered endpoints map
        registeredEndpoints.put(parsedEndpointURI, id);

        // also need to check the URI without the context root
        int nextSlash = parsedEndpointURI.indexOf('/', 1);
        if (nextSlash > 0)
        {
            String parsedEndpointURI2 = parsedEndpointURI.substring(nextSlash);
            if (registeredEndpoints.containsKey(parsedEndpointURI2))
            {
                ConfigurationException ce = new ConfigurationException();
                ce.setMessage(ERR_MSG_URI_ALREADY_REGISTERED, new Object[] {
                        parsedEndpointURI2, id,
                        registeredEndpoints.get(parsedEndpointURI2) });
                throw ce;
            }
            registeredEndpoints.put(parsedEndpointURI2, id);
        }
    }

    /**
     *
     * Matches the current &quot;servlet + pathinfo&quot; to a list of channels registered
     * in the services configuration file, independent of context root.
     *
     * @param path        The Servlet mapping and PathInfo of the current request
     * @param contextPath The web application context root (or empty string for default root)
     * @param endpoint    The endpoint to be matched
     * @return whether the current request matches a registered endpoint URI
     *
     */
    private boolean matchEndpoint(String path, String contextPath, Endpoint endpoint)
    {
        boolean match = false;
        String channelEndpoint = endpoint.getParsedUrl(contextPath);

        if (path.endsWith("/"))
        {
            path = path.substring(0, path.length() - 1);
        }

        if (path.equalsIgnoreCase(channelEndpoint))
        {
            match = true;
        }

        return match;
    }

    private void registerMessageBroker()
    {
        String mbid = getId();

        synchronized (messageBrokers)
        {
            if (messageBrokers.get(mbid) != null)
            {
                ConfigurationException ce = new ConfigurationException();
                ce.setMessage(10137, new Object[] {getId() == null ? "(no value supplied)" : mbid});
                throw ce;
            }
            messageBrokers.put(mbid, this);
        }
    }

    private void unRegisterMessageBroker()
    {
        String mbid = getId();

        synchronized (messageBrokers)
        {
            messageBrokers.remove(mbid);
        }
    }

    /**
     * Start all of the broker's shared servers.
     */
    private void startServers()
    {
        for (Server server : servers.values())
        {
            // Validate that the server is actually referenced by an endpoint; if not, warn.
            boolean serverIsReferenced = false;
            for (Endpoint endpoint : endpoints.values())
            {
                if (endpoint instanceof Endpoint2 && server.equals(((Endpoint2)endpoint).getServer()))
                {
                    serverIsReferenced = true;
                    break;
                }
            }

            if (!serverIsReferenced && Log.isWarn())
                Log.getLogger(LogCategories.CONFIGURATION).warn("Server '" + server.getId() + "' is not referenced by any endpoints.");

            server.start();
        }
    }

    /**
     * Stop all the broker's shared servers.
     */
    private void stopServers()
    {
        for (Server server : servers.values())
            server.stop();
    }

    /**
     * Start all of the broker's services.
     *
     *
     */
    private void startServices()
    {
        for (Service svc : services.values() )
        {
            long timeBeforeStartup = 0;
            if (Log.isDebug())
            {
                timeBeforeStartup = System.currentTimeMillis();
                Log.getLogger(LOG_CATEGORY_STARTUP_SERVICE).debug("Service with id '{0}' is starting.",
                        new Object[]{svc.getId()});
            }

            svc.start();

            if (Log.isDebug())
            {
                long timeAfterStartup = System.currentTimeMillis();
                Long diffMillis = timeAfterStartup - timeBeforeStartup;
                Log.getLogger(LOG_CATEGORY_STARTUP_SERVICE).debug("Service with id '{0}' is ready (startup time: '{1}' ms)",
                        new Object[]{svc.getId(), diffMillis});
            }
        }
    }

    /**
     * Stop all of the broker's services.
     *
     *
     */
    private void stopServices()
    {
        for (Service svc : services.values())
            svc.stop();
    }
}
