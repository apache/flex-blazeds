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

import flex.management.runtime.messaging.MessageDestinationControl;
import flex.management.runtime.messaging.services.messaging.SubscriptionManagerControl;
import flex.management.runtime.messaging.services.messaging.ThrottleManagerControl;
import flex.messaging.config.ConfigurationConstants;
import flex.messaging.config.ConfigurationException;
import flex.messaging.config.DestinationSettings;
import flex.messaging.config.ThrottleSettings;
import flex.messaging.config.ConfigMap;
import flex.messaging.config.NetworkSettings;
import flex.messaging.config.ServerSettings;
import flex.messaging.config.ThrottleSettings.Policy;
import flex.messaging.log.LogCategories;
import flex.messaging.services.MessageService;
import flex.messaging.services.Service;
import flex.messaging.services.messaging.SubscriptionManager;
import flex.messaging.services.messaging.RemoteSubscriptionManager;
import flex.messaging.services.messaging.ThrottleManager;
import flex.messaging.services.messaging.MessagingConstants;
import flex.messaging.util.ClassUtil;

/**
 * A logical reference to a MessageDestination.
 */
public class MessageDestination extends FactoryDestination
{
    static final long serialVersionUID = -2016911808141319012L;

    /** Log category for <code>MessageDestination</code>.*/
    public static final String LOG_CATEGORY = LogCategories.SERVICE_MESSAGE;

    // Errors
    private static final int UNSUPPORTED_POLICY = 10124;

    // Destination properties
    private transient ServerSettings serverSettings;

    // Destination internal
    private transient SubscriptionManager subscriptionManager;
    private transient RemoteSubscriptionManager remoteSubscriptionManager;
    private transient ThrottleManager throttleManager;

    private transient MessageDestinationControl controller;

    //--------------------------------------------------------------------------
    //
    // Constructor
    //
    //--------------------------------------------------------------------------

    /**
     * Constructs an unmanaged <code>MessageDestination</code> instance.
     */
    public MessageDestination()
    {
        this(false);
    }

    /**
     * Constructs a <code>MessageDestination</code> with the indicated management.
     *
     * @param enableManagement <code>true</code> if the <code>MessageDestination</code>
     * is manageable; otherwise <code>false</code>.
     */
    public MessageDestination(boolean enableManagement)
    {
        super(enableManagement);

        serverSettings = new ServerSettings();

        // Managers
        subscriptionManager = new SubscriptionManager(this);
        remoteSubscriptionManager = new RemoteSubscriptionManager(this);
    }

    //--------------------------------------------------------------------------
    //
    // Initialize, validate, start, and stop methods.
    //
    //--------------------------------------------------------------------------

    /**
     * Initializes the <code>MessageDestination</code> with the properties.
     * If subclasses override, they must call <code>super.initialize()</code>.
     *
     * @param id The id of the destination.
     * @param properties Properties for the <code>MessageDestination</code>.
     */
    @Override
    public void initialize(String id, ConfigMap properties)
    {
        super.initialize(id, properties);

        if (properties == null || properties.size() == 0)
            return;

        // Network properties
        network(properties);

        // Server properties
        server(properties);
    }

    /**
     * Sets up the throttle manager before it starts.
     */
    @Override
    public void start()
    {
        // Create the throttle manager, only if needed.
        if (networkSettings.getThrottleSettings() != null)
        {
            ThrottleSettings settings = networkSettings.getThrottleSettings();
            if (settings.isClientThrottleEnabled() || settings.isDestinationThrottleEnabled())
            {
                settings.setDestinationName(getId());
                throttleManager = createThrottleManager();
                throttleManager.setThrottleSettings(settings);
                throttleManager.start();
            }
        }
        super.start();
    }

    /**
     * Stops the subscription, remote subscription, and throttle managers and
     * then calls super class's stop.
     */
    @Override
    public void stop()
    {
        if (isStarted())
        {
            subscriptionManager.stop();
            remoteSubscriptionManager.stop();
            if (throttleManager != null)
                throttleManager.stop();
        }
        super.stop();
    }

    //--------------------------------------------------------------------------
    //
    // Public Getters and Setters for Destination properties
    //
    //--------------------------------------------------------------------------

    /**
     * Sets the <code>NetworkSettings</code> of the <code>MessageDestination</code>.
     *
     * @param networkSettings The <code>NetworkSettings</code> of the <code>MessageDestination</code>
     */
    @Override
    public void setNetworkSettings(NetworkSettings networkSettings)
    {
        super.setNetworkSettings(networkSettings);

        // Set the subscription manager settings if needed.
        if (networkSettings.getSubscriptionTimeoutMinutes() > 0)
        {
            long subscriptionTimeoutMillis = networkSettings.getSubscriptionTimeoutMinutes() * 60L * 1000L; // Convert to millis.
            subscriptionManager.setSubscriptionTimeoutMillis(subscriptionTimeoutMillis);
        }
    }

    /**
     * Returns the <code>ServerSettings</code> of the <code>MessageDestination</code>.
     *
     * @return The <code>ServerSettings</code> of the <code>MessageDestination</code>.
     */
    public ServerSettings getServerSettings()
    {
        return serverSettings;
    }

    /**
     * Sets the <code>ServerSettings</code> of the <code>MessageDestination</code>.
     *
     * @param serverSettings The <code>ServerSettings</code> of the <code>MessageDestination</code>
     */
    public void setServerSettings(ServerSettings serverSettings)
    {
        this.serverSettings = serverSettings;
    }

    /**
     * Casts the <code>Service</code> into <code>MessageService</code>
     * and calls super.setService.
     *
     * @param service The <code>Service</code> managing this <code>Destination</code>.
     */
    @Override
    public void setService(Service service)
    {
        MessageService messageService = (MessageService)service;
        super.setService(messageService);
    }

    //--------------------------------------------------------------------------
    //
    // Other Public Methods
    //
    //--------------------------------------------------------------------------

    /**
     *
     * Returns a <tt>ConfigMap</tt> of destination properties that the client
     * needs. This includes properties from <code>super{@link #describeDestination(boolean)}</code>
     * and it also includes outbound throttling policy that the edge server might need.
     *
     * @param onlyReliable Determines whether only reliable destination configuration should be returned.
     * @return A <tt>ConfigMap</tt> of destination properties that the client needs.
     */
    @Override
    public ConfigMap describeDestination(boolean onlyReliable)
    {
        ConfigMap destinationConfig = super.describeDestination(onlyReliable);
        if (destinationConfig == null)
            return null;

        if (throttleManager == null)
            return destinationConfig;

        Policy outboundPolicy = throttleManager.getOutboundPolicy();
        if (outboundPolicy == null || outboundPolicy == Policy.NONE)
            return destinationConfig;

        // Add the outbound throttle policy to network properties section as appropriate.
        ConfigMap properties = destinationConfig.getPropertyAsMap(ConfigurationConstants.PROPERTIES_ELEMENT, null);
        if (properties == null)
        {
            properties = new ConfigMap();
            destinationConfig.addProperty(ConfigurationConstants.PROPERTIES_ELEMENT, properties);
        }

        ConfigMap network = properties.getPropertyAsMap(NetworkSettings.NETWORK_ELEMENT, null);
        if (network == null)
        {
            network = new ConfigMap();
            properties.addProperty(NetworkSettings.NETWORK_ELEMENT, network);
        }

        ConfigMap throttleOutbound = new ConfigMap();
        throttleOutbound.addProperty(ThrottleSettings.ELEMENT_POLICY, throttleManager.getOutboundPolicy().toString());
        network.addProperty(ThrottleSettings.ELEMENT_OUTBOUND, throttleOutbound);

        return destinationConfig;
    }


    public SubscriptionManager getSubscriptionManager()
    {
        return subscriptionManager;
    }


    public RemoteSubscriptionManager getRemoteSubscriptionManager()
    {
        return remoteSubscriptionManager;
    }


    public ThrottleManager getThrottleManager()
    {
        return throttleManager;
    }


    @Override
    public boolean equals(Object o)
    {
        if (o instanceof Destination)
        {
            Destination d = (Destination)o;
            String serviceType1 = d.getServiceType();
            String serviceType2 = getServiceType();
            if ((serviceType1 == null && serviceType2 == null) || (serviceType1 != null && serviceType1.equals(serviceType2)))
            {
                String id1 = d.getId();
                String id2 = getId();
                if ((id1 == null && id2 == null) || (id1 != null && id1.equals(id2)))
                    return true;
            }
        }
        return false;
    }


    @Override
    public int hashCode()
    {
        return (getServiceType() == null ? 0 : getServiceType().hashCode()) * 100003 +
            (getId() == null ? 0 : getId().hashCode());
    }


    @Override
    public String toString()
    {
        return getServiceType() + "#" + getId();
    }

    //--------------------------------------------------------------------------
    //
    // Protected/Private Methods
    //
    //--------------------------------------------------------------------------

    protected ThrottleManager createThrottleManager()
    {
        Service service = getService();
        if (service == null || service.getMessageBroker() == null)
            return new ThrottleManager(); // Return the default.

        try
        {
            Class<? extends ThrottleManager> throttleManagerClass = service.getMessageBroker().getThrottleManagerClass();
            Object instance = ClassUtil.createDefaultInstance(throttleManagerClass, null);
            if (instance instanceof ThrottleManager)
                return (ThrottleManager)instance;
        }
        catch (Throwable t)
        {
            // NOWARN
        }

        return new ThrottleManager(); // Return the default.
    }

    protected void network(ConfigMap properties)
    {
        ConfigMap network = properties.getPropertyAsMap(NetworkSettings.NETWORK_ELEMENT, null);
        if (network == null)
            return;

        // Get implementation specific network settings, including subclasses!
        NetworkSettings ns = getNetworkSettings();

        // Subscriber timeout; first check for subscription-timeout-minutes and fallback to legacy session-timeout.
        int useLegacyPropertyToken = -999999;
        int subscriptionTimeoutMinutes = network.getPropertyAsInt(NetworkSettings.SUBSCRIPTION_TIMEOUT_MINUTES, useLegacyPropertyToken);
        if (subscriptionTimeoutMinutes == useLegacyPropertyToken)
            subscriptionTimeoutMinutes = network.getPropertyAsInt(NetworkSettings.SESSION_TIMEOUT, NetworkSettings.DEFAULT_TIMEOUT);
        ns.setSubscriptionTimeoutMinutes(subscriptionTimeoutMinutes);

        // Throttle Settings
        ThrottleSettings ts = ns.getThrottleSettings();
        ts.setDestinationName(getId());
        throttle(ts, network);

        setNetworkSettings(ns);
    }

    protected void throttle(ThrottleSettings ts, ConfigMap network)
    {
        ConfigMap inbound = network.getPropertyAsMap(ThrottleSettings.ELEMENT_INBOUND, null);
        if (inbound != null)
        {
            ThrottleSettings.Policy policy = getPolicyFromThrottleSettings(inbound);
            ts.setInboundPolicy(policy);
            int destFreq = inbound.getPropertyAsInt(ThrottleSettings.ELEMENT_DEST_FREQ, 0);
            ts.setIncomingDestinationFrequency(destFreq);
            int clientFreq = inbound.getPropertyAsInt(ThrottleSettings.ELEMENT_CLIENT_FREQ, 0);
            ts.setIncomingClientFrequency(clientFreq);
        }

        ConfigMap outbound = network.getPropertyAsMap(ThrottleSettings.ELEMENT_OUTBOUND, null);
        if (outbound != null)
        {
            ThrottleSettings.Policy policy = getPolicyFromThrottleSettings(outbound);
            ts.setOutboundPolicy(policy);
            int destFreq = outbound.getPropertyAsInt(ThrottleSettings.ELEMENT_DEST_FREQ, 0);
            ts.setOutgoingDestinationFrequency(destFreq);
            int clientFreq = outbound.getPropertyAsInt(ThrottleSettings.ELEMENT_CLIENT_FREQ, 0);
            ts.setOutgoingClientFrequency(clientFreq);
        }
    }

    private ThrottleSettings.Policy getPolicyFromThrottleSettings(ConfigMap settings)
    {
        String policyString = settings.getPropertyAsString(ThrottleSettings.ELEMENT_POLICY, null);
        ThrottleSettings.Policy policy = ThrottleSettings.Policy.NONE;
        if (policyString == null)
            return policy;
        try
        {
            policy = ThrottleSettings.parsePolicy(policyString);
        }
        catch (ConfigurationException exception)
        {
            ConfigurationException ce = new ConfigurationException();
            ce.setMessage(UNSUPPORTED_POLICY, new Object[] {getId(), policyString});
            throw ce;
        }
        return policy;
    }

    protected void server(ConfigMap properties)
    {
        ConfigMap server = properties.getPropertyAsMap(DestinationSettings.SERVER_ELEMENT, null);
        if (server == null)
            return;

        long ttl = server.getPropertyAsLong(MessagingConstants.TIME_TO_LIVE_ELEMENT, -1);
        serverSettings.setMessageTTL(ttl);

        boolean durable = server.getPropertyAsBoolean(MessagingConstants.IS_DURABLE_ELEMENT, false);
        serverSettings.setDurable(durable);

        boolean allowSubtopics = server.getPropertyAsBoolean(MessagingConstants.ALLOW_SUBTOPICS_ELEMENT, false);
        serverSettings.setAllowSubtopics(allowSubtopics);

        boolean disallowWildcardSubtopics = server.getPropertyAsBoolean(MessagingConstants.DISALLOW_WILDCARD_SUBTOPICS_ELEMENT, false);
        serverSettings.setDisallowWildcardSubtopics(disallowWildcardSubtopics);

        int priority = server.getPropertyAsInt(MessagingConstants.MESSAGE_PRIORITY, -1);
        if (priority != -1)
            serverSettings.setPriority(priority);

        String subtopicSeparator = server.getPropertyAsString(MessagingConstants.SUBTOPIC_SEPARATOR_ELEMENT, MessagingConstants.DEFAULT_SUBTOPIC_SEPARATOR);
        serverSettings.setSubtopicSeparator(subtopicSeparator);

        String routingMode = server.getPropertyAsString(MessagingConstants.CLUSTER_MESSAGE_ROUTING, "server-to-server");
        serverSettings.setBroadcastRoutingMode(routingMode);
    }

    /**
     * Returns the log category of the <code>MessageDestination</code>.
     *
     * @return The log category of the component.
     */
    @Override
    protected String getLogCategory()
    {
        return LOG_CATEGORY;
    }

    /**
     * Invoked automatically to allow the <code>MessageDestination</code> to setup its corresponding
     * MBean control.
     *
     * @param service The <code>Service</code> that manages this <code>MessageDestination</code>.
     */
    @Override
    protected void setupDestinationControl(Service service)
    {
        controller = new MessageDestinationControl(this, service.getControl());
        controller.register();
        setControl(controller);
        setupThrottleManagerControl(controller);
        setupSubscriptionManagerControl(controller);
    }

    protected void setupThrottleManagerControl(MessageDestinationControl destinationControl)
    {
        if (throttleManager != null)
        {
            ThrottleManagerControl throttleManagerControl = new ThrottleManagerControl(throttleManager, destinationControl);
            throttleManagerControl.register();
            throttleManager.setControl(throttleManagerControl);
            throttleManager.setManaged(true);
            destinationControl.setThrottleManager(throttleManagerControl.getObjectName());
        }
    }

    private void setupSubscriptionManagerControl(MessageDestinationControl destinationControl)
    {
        SubscriptionManagerControl subscriptionManagerControl = new SubscriptionManagerControl(getSubscriptionManager(), destinationControl);
        subscriptionManagerControl.register();
        getSubscriptionManager().setControl(subscriptionManagerControl);
        getSubscriptionManager().setManaged(true);
        destinationControl.setSubscriptionManager(subscriptionManagerControl.getObjectName());
    }
}
