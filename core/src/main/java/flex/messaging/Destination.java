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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import flex.management.ManageableComponent;
import flex.management.runtime.messaging.services.ServiceControl;
import flex.messaging.log.LogCategories;
import flex.messaging.log.Log;
import flex.messaging.services.Service;
import flex.messaging.services.ServiceAdapter;
import flex.messaging.util.ClassUtil;
import flex.messaging.cluster.ClusterManager;
import flex.messaging.config.ClusterSettings;
import flex.messaging.config.ConfigMap;
import flex.messaging.config.ConfigurationConstants;
import flex.messaging.config.ConfigurationException;
import flex.messaging.config.NetworkSettings;
import flex.messaging.config.SecurityConstraint;

/**
 * The <code>Destination</code> class is a source and sink for messages sent through
 * a service destination and uses an adapter to process messages.
 */
public class Destination extends ManageableComponent implements java.io.Serializable {
    static final long serialVersionUID = -977001797620881435L;

    /**
     * Default log category for <code>Destination</code>.
     */
    public static final String LOG_CATEGORY = LogCategories.SERVICE_GENERAL;

    /**
     * Hard coded id for the push destination
     */
    public static final String PUSH_DESTINATION_ID = "_DS_PUSH_";

    // Errors
    private static final int NO_SERVICE = 11117;

    // Destination's properties
    protected ServiceAdapter adapter;
    protected List<String> channelIds;
    protected NetworkSettings networkSettings;
    protected SecurityConstraint securityConstraint;
    protected String securityConstraintRef;
    protected HashMap<String, Object> extraProperties;
    protected boolean initialized;
    protected boolean clustered;
    protected boolean clusteredCalculated;

    //--------------------------------------------------------------------------
    //
    // Constructor
    //
    //--------------------------------------------------------------------------

    /**
     * Constructs an unmanaged <code>Destination</code> instance.
     */
    public Destination() {
        this(false);
    }

    /**
     * Constructs a <code>Destination</code> with the indicated management.
     *
     * @param enableManagement <code>true</code> if the <code>Destination</code>
     *                         is manageable; otherwise <code>false</code>.
     */
    public Destination(boolean enableManagement) {
        super(enableManagement);

        networkSettings = new NetworkSettings();
    }

    //--------------------------------------------------------------------------
    //
    // Initialize, validate, start, and stop methods.
    //
    //--------------------------------------------------------------------------

    /**
     * Initializes the <code>Destination</code> with the properties.
     * If subclasses override, they must call <code>super.initialize()</code>.
     *
     * @param id         The id of the destination.
     * @param properties Properties for the destination.
     */
    @Override
    public void initialize(String id, ConfigMap properties) {
        super.initialize(id, properties);

        if (properties == null || properties.size() == 0) {
            initialized = true;
            return;
        }

        ConfigMap network = properties.getPropertyAsMap(NetworkSettings.NETWORK_ELEMENT, null);

        if (network != null) {
            networkSettings.setReliable(network.getPropertyAsBoolean(NetworkSettings.RELIABLE_ELEMENT, false));

            ConfigMap clusterInfo = network.getPropertyAsMap(ClusterSettings.CLUSTER_ELEMENT, null);
            if (clusterInfo != null) {
                // Mark these as used so we do not get warnings about them.
                network.allowProperty(ClusterSettings.CLUSTER_ELEMENT);
                clusterInfo.allowProperty(ClusterSettings.REF_ATTR);
                clusterInfo.allowProperty(ClusterSettings.SHARED_BACKEND_ATTR);

                String clusterId = clusterInfo.getPropertyAsString(ClusterSettings.REF_ATTR, null);
                String coordinatorPolicy = clusterInfo.getPropertyAsString(ClusterSettings.SHARED_BACKEND_ATTR, null);
                if (coordinatorPolicy != null)
                    networkSettings.setSharedBackend(Boolean.valueOf(coordinatorPolicy));

                networkSettings.setClusterId(clusterId);
            }
        }

        initialized = true;
    }

    /**
     * Returns whether or not the destination has been initialized.
     *
     * @return True, if the destination has been initialized.
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Verifies that the <code>Destination</code> is in valid state before
     * it is started. If subclasses override, they must call <code>super.validate()</code>.
     */
    @Override
    protected void validate() {
        if (isValid())
            return;

        super.validate();

        if (getAdapter() == null) {
            String defaultAdapterId = getService().getDefaultAdapter();
            if (defaultAdapterId != null) {
                createAdapter(defaultAdapterId);
            } else {
                invalidate();
                // Destination '{id}' must specify at least one adapter.
                ConfigurationException ex = new ConfigurationException();
                ex.setMessage(ConfigurationConstants.DEST_NEEDS_ADAPTER, new Object[]{getId()});
                throw ex;
            }
        }

        if (channelIds != null) {
            List<String> brokerChannelIds = getService().getMessageBroker().getChannelIds();
            for (Iterator<String> iter = channelIds.iterator(); iter.hasNext(); ) {
                String id = iter.next();
                if (brokerChannelIds == null || !brokerChannelIds.contains(id)) {
                    iter.remove();
                    if (Log.isWarn()) {
                        Log.getLogger(getLogCategory()).warn("No channel with id '{0}' is known by the MessageBroker." +
                                        " Removing the channel.",
                                new Object[]{id});
                    }
                }
            }
        }

        // Set the default channels if needed
        if (channelIds == null) {
            List<String> defaultChannelIds = getService().getDefaultChannels();
            if (defaultChannelIds != null && defaultChannelIds.size() > 0) {
                setChannels(defaultChannelIds);
            } else {
                invalidate();
                // Destination '{id}' must specify at least one channel.
                ConfigurationException ex = new ConfigurationException();
                ex.setMessage(ConfigurationConstants.DEST_NEEDS_CHANNEL, new Object[]{getId()});
                throw ex;
            }
        }

        MessageBroker broker = getService().getMessageBroker();

        // Validate the security constraint
        if (securityConstraint == null && securityConstraintRef != null) {
            securityConstraint = broker.getSecurityConstraint(securityConstraintRef);
            // No need to throw an error as MessageBroker automatically throws
            // an error if no such constraint exists
        }

        ClusterManager cm = broker.getClusterManager();

        // Set clustering if needed
        if (getNetworkSettings().getClusterId() != null || cm.getDefaultClusterId() != null) {
            cm.clusterDestination(this);
        }
    }

    /**
     * Starts the destination if its associated <code>Service</code> is started
     * and if the destination is not already running. The default implementation
     * of this method starts the adapter of the destination. If subclasses
     * override, they must call <code>super.start()</code>.
     */
    @Override
    public void start() {
        if (isStarted()) {
            // Needed for adapters added after startup.
            getAdapter().start();
            return;
        }

        // Check if the Service is started
        Service service = getService();
        if (!service.isStarted()) {
            if (Log.isWarn()) {
                Log.getLogger(getLogCategory()).warn("Destination with id '{0}' cannot be started" +
                                " when its Service with id '{1}' is not started.",
                        new Object[]{getId(), service.getId()});
            }
            return;
        }

        // Set up management
        if (isManaged() && service.isManaged()) {
            setupDestinationControl(service);
            ServiceControl controller = (ServiceControl) service.getControl();
            if (getControl() != null)
                controller.addDestination(getControl().getObjectName());
        }

        super.start();

        getAdapter().start();
    }

    /**
     * The default implementation of this method stops all of the adapters
     * of the destination.
     * If subclasses override, they must call <code>super.stop()</code>.
     */
    @Override
    public void stop() {
        if (!isStarted())
            return;

        getAdapter().stop();

        super.stop();

        // Remove management
        if (isManaged() && getService().isManaged()) {
            if (getControl() != null) {
                getControl().unregister();
                setControl(null);
            }
            setManaged(false);
        }

    }

    //--------------------------------------------------------------------------
    //
    // Public Getters and Setters for Destination properties
    //
    //--------------------------------------------------------------------------

    /**
     * Returns the <code>ServiceAdapter</code> for the <code>Destination</code>.
     *
     * @return The <code>ServiceAdapter</code> for the <code>Destination</code>.
     */
    public ServiceAdapter getAdapter() {
        return adapter;
    }

    /**
     * Creates a <code>ServiceAdapter</code> instance, sets its id, sets it manageable
     * if the <code>Destination</code> that created it is manageable,
     * and set its <code>Destination</code> to the <code>Destination</code> that
     * created it.
     * <p>
     * In order to use this method, <code>Destination</code> should have an assigned
     * <code>Service</code> and the id provided for the adapter should already
     * be registered with the <code>Service</code>.
     *
     * @param id The id of the <code>ServiceAdapter</code>.
     * @return The <code>ServiceAdapter</code> instanced created.
     */
    public ServiceAdapter createAdapter(String id) {
        if (getService() == null) {
            // Destination cannot create adapter '{0}' without its Service set.
            ConfigurationException ex = new ConfigurationException();
            ex.setMessage(NO_SERVICE, new Object[]{id});
            throw ex;
        }
        Map<String, String> adapterClasses = getService().getRegisteredAdapters();
        if (!adapterClasses.containsKey(id)) {
            // No adapter with id '{0}' is registered with the service '{1}'.
            ConfigurationException ex = new ConfigurationException();
            ex.setMessage(ConfigurationConstants.UNREGISTERED_ADAPTER, new Object[]{id, getService().getId()});
            throw ex;
        }

        String adapterClassName = adapterClasses.get(id);
        Class<?> adapterClass = ClassUtil.createClass(adapterClassName,
                FlexContext.getMessageBroker() == null ?
                        null : FlexContext.getMessageBroker().getClassLoader());

        ServiceAdapter adapter = (ServiceAdapter) ClassUtil.createDefaultInstance(adapterClass, ServiceAdapter.class);
        adapter.setId(id);
        adapter.setManaged(isManaged());
        adapter.setDestination(this);

        return adapter;
    }

    /**
     * Sets the <code>ServiceAdapter</code> of the <code>Destination</code>.
     *
     * <code>ServiceAdapter</code> needs to be started if the <code>Destination</code>
     * is already running.
     *
     * @param adapter The adapter for the destination.
     */
    public void setAdapter(ServiceAdapter adapter) {
        if (getAdapter() == adapter) // No need to reset the adapter.
            return;

        if (adapter == null) {
            removeAdapter();
            return;
        }

        addAdapter(adapter);
    }

    /**
     * Used by setAdapter and it removes the old adapter of the destination
     * and adds the new adapter.
     *
     * @param adapter The adapter for the destination.
     */
    private void addAdapter(ServiceAdapter adapter) {
        removeAdapter();

        this.adapter = adapter;

        if (adapter.getDestination() == null || adapter.getDestination() != this)
            adapter.setDestination(this);
    }

    /**
     * Used by setAdapter and addAdapter. It removes the current adapter
     * of the destination
     */
    private void removeAdapter() {
        ServiceAdapter adapter = getAdapter();
        if (adapter != null) {
            adapter.stop();
        }
        this.adapter = null;
    }


    /**
     * The destination may be not clustered at all, may be clustered for channel failover and
     * destination sharing, or it may be clustered for channel failover and also have a
     * common backend, such as a common database or backend clustered JMS topic.
     * If the destination is clustered and has a common backend to coordinate the cluster,
     * this method returns true; otherwise it return false. Note that this method returns
     * <code>false</code> if the <code>Destination</code> is not runnning.
     *
     * @return <code>true</code> if the clustered <code>Destination</code> shares a common backend;
     * otherwise <code>false</code>.
     */
    public boolean isBackendShared() {
        if (!isStarted())
            return false;

        ClusterManager clm = getService().getMessageBroker().getClusterManager();
        return clm.isBackendShared(getService().getClass().getName(), getId());
    }

    /**
     * Returns the list of channel ids of the <code>Destination</code>.
     *
     * @return The list of channel ids of the <code>Destination</code>.
     */
    public List<String> getChannels() {
        return channelIds;
    }

    /**
     * Adds a channel to the list of channels known by the <code>Destination</code>.
     * <code>MessageBroker</code> has to know the channel. Otherwise, the channel
     * is not added to the list.
     *
     * @param id The id of the channel.
     */
    public void addChannel(String id) {
        if (channelIds == null)
            channelIds = new ArrayList<String>();
        else if (channelIds.contains(id))
            return;

        if (isStarted()) {
            List<String> brokerChannelIds = getService().getMessageBroker().getChannelIds();
            if (brokerChannelIds == null || !brokerChannelIds.contains(id)) {
                if (Log.isWarn()) {
                    Log.getLogger(getLogCategory()).warn("No channel with id '{0}' is known by the MessageBroker." +
                                    " Not adding the channel.",
                            new Object[]{id});
                }
                return;
            }
        }
        // Either message broker knows about the channel, or destination is not
        // running and channel will be checked before startup during validate
        channelIds.add(id);
    }

    /**
     * Removes the channel from the list of channels for the <code>Destination</code>.
     *
     * @param id The id of the channel.
     * @return <code>true</code> if the list contained the channel id.
     */
    public boolean removeChannel(String id) {
        return channelIds != null && channelIds.remove(id);
    }

    /**
     * Sets the channel list of the <code>Destination</code>.
     * <code>MessageBroker</code> has to know the channels, otherwise they
     * are not added to the list.
     *
     * @param ids List of channel ids.
     */
    public void setChannels(List<String> ids) {
        if (ids != null && isStarted()) {
            List<String> brokerChannelIds = getService().getMessageBroker().getChannelIds();
            for (Iterator<String> iter = ids.iterator(); iter.hasNext(); ) {
                String id = iter.next();
                if (brokerChannelIds == null || !brokerChannelIds.contains(id)) {
                    iter.remove();
                    if (Log.isWarn()) {
                        Log.getLogger(getLogCategory()).warn("No channel with id '{0}' is known by the MessageBroker." +
                                        " Not adding the channel.",
                                new Object[]{id});
                    }
                }
            }
        }
        // Otherwise, channels will be checked before startup during validate
        channelIds = ids;
    }

    /**
     * The destination may be not clustered at all, may be clustered for channel failover
     * only, or it may be clustered for channel failover and also have shared back ends.
     * If the destination is clustered, regardless of whether or not it relies on a shared
     * back end for cluster configuration, this method returns true. Note that this method
     * returns <code>false</code> if the <code>Destination</code> is not runnning.
     *
     * @return <code>true</code> if the <code>Destination</code> is clustered; otherwise <code>false</code>.
     */
    public boolean isClustered() {
        if (!isStarted())
            return false;

        if (!clusteredCalculated) {
            ClusterManager clm = getService().getMessageBroker().getClusterManager();
            clustered = clm.isDestinationClustered(getService().getClass().getName(), getId());
            clusteredCalculated = true;
        }
        return clustered;
    }

    /**
     * Sets the id of the <code>Destination</code>. If the <code>Destination</code>
     * has a <code>Service</code> assigned, it also updates the id in the
     * <code>Service</code>.
     *
     * @param id The id of the <code>Destination</code>.
     */
    @Override
    public void setId(String id) {
        String oldId = getId();

        super.setId(id);

        // Update the destination id in the service and MessageBroker
        Service service = getService();
        if (service != null) {
            service.removeDestination(oldId);
            service.addDestination(this);
        }
    }

    /**
     * Get the <code>NetworkSettings</code> of the <code>Destination</code>.
     *
     * @return The <code>NetworkSettings</code> of the <code>Destination</code>.
     */
    public NetworkSettings getNetworkSettings() {
        return networkSettings;
    }

    /**
     * Set the <code>NetworkSettings</code> of the <code>Destination</code>.
     *
     * @param networkSettings The <code>NetworkSettings</code> of the <code>Destination</code>.
     */
    public void setNetworkSettings(NetworkSettings networkSettings) {
        this.networkSettings = networkSettings;
    }

    /**
     * Returns the <code>Service</code> managing this <code>Destination</code>.
     *
     * @return The <code>Service</code> managing this <code>Destination</code>.
     */
    public Service getService() {
        return (Service) getParent();
    }

    /**
     * Sets the <code>Service</code> managing this <code>Destination</code>.
     * Removes the <code>Destination</code> from the old service
     * (if there was one) and adds to the list of destination in the new service.
     *
     * @param service The <code>Service</code> managing this <code>Destination</code>.
     */
    public void setService(Service service) {
        Service oldService = getService();

        setParent(service);

        if (oldService != null)
            oldService.removeDestination(getId());

        // Add the destination to the service if needed
        if (service.getDestination(getId()) != this)
            service.addDestination(this);
    }

    /**
     * Returns the Java class name for the <code>Service</code> managing this
     * <code>Destination</code>. Returns null if there is no <code>Service</code>
     * assigned to the <code>Destination</code> yet.
     *
     * @return The Java class name for the <code>Service</code> manageing this <code>Destination</code>.
     */
    public String getServiceType() {
        Service service = getService();
        return service != null ? service.getClass().getName() : null;
    }

    /**
     * Returns the <code>SecurityConstraint</code> of the <code>Destination</code>.
     * <code>SecurityConstraint</code> is constructed as the <code>Destination</code>
     * starts up. Therefore, this could return null even if the <code>SecurityConstraint</code>
     * reference is set but <code>Destination</code> is not started yet.
     *
     * @return The <code>SecurityConstraint</code> of the <code>Destination</code>.
     */
    public SecurityConstraint getSecurityConstraint() {
        return securityConstraint;
    }

    /**
     * Sets the <code>SecurityConstraint</code> of the <code>Destination</code>.
     *
     * @param securityConstraint The <code>SecurityConstraint</code> of the <code>Destination</code>.
     */
    public void setSecurityConstraint(SecurityConstraint securityConstraint) {
        this.securityConstraint = securityConstraint;
    }

    /**
     * Sets the <code>SecurityConstraint</code> reference of the <code>Destination</code>.
     * <code>MessageBroker</code> has to know the <code>SecurityConstraint</code>
     * reference. Note that <code>getSecurityConstraint</code> can return null
     * if the reference is set but the <code>Destination</code> is not started yet.
     *
     * @param ref <code>SecurityConstraint</code> reference.
     */
    public void setSecurityConstraint(String ref) {
        if (isStarted()) {
            MessageBroker msgBroker = getService().getMessageBroker();
            securityConstraint = msgBroker.getSecurityConstraint(ref);
            // No need to throw an error as MessageBroker automatically throws
            // an error if no such constraint exists
        }
        securityConstraintRef = ref;
    }

    //--------------------------------------------------------------------------
    //
    // Other public APIs
    //
    //--------------------------------------------------------------------------

    /**
     * Calls {@link Destination#describeDestination(boolean)} with true boolean value.
     *
     * @return A <tt>ConfigMap</tt> of destination properties that the client needs.
     * @see flex.messaging.Destination#describeDestination(boolean)
     */
    public ConfigMap describeDestination() {
        return describeDestination(true);
    }

    /**
     * Returns a <tt>ConfigMap</tt> of destination properties that the client
     * needs. Subclasses can add additional properties to <tt>super.describeDestination(boolean)</tt>,
     * or return null if they don't want their properties to be sent to the client.
     *
     * @param onlyReliable Determines whether only reliable destination configuration should be returned.
     * @return A <tt>ConfigMap</tt> of destination properties that the client needs.
     */
    public ConfigMap describeDestination(boolean onlyReliable) {
        boolean reliable = networkSettings != null && networkSettings.isReliable();
        if (onlyReliable && !reliable)
            return null;

        ConfigMap destinationConfig = new ConfigMap();
        destinationConfig.addProperty(ConfigurationConstants.ID_ATTR, getId());

        // Include network settings if reliability for the destination is enabled.
        if (reliable) {
            ConfigMap properties = new ConfigMap();
            ConfigMap network = new ConfigMap();

            ConfigMap reliableMap = new ConfigMap();
            // Adding as a value rather than attribute to the parent
            reliableMap.addProperty(ConfigurationConstants.EMPTY_STRING, Boolean.toString(networkSettings.isReliable()));

            network.addProperty(NetworkSettings.RELIABLE_ELEMENT, reliableMap);
            properties.addProperty(NetworkSettings.NETWORK_ELEMENT, network);

            destinationConfig.addProperty(ConfigurationConstants.PROPERTIES_ELEMENT, properties);
        }

        ConfigMap channelsConfig = new ConfigMap();
        for (String id : channelIds) {
            ConfigMap channelConfig = new ConfigMap();
            channelConfig.addProperty(ConfigurationConstants.REF_ATTR, id);
            channelsConfig.addProperty(ConfigurationConstants.CHANNEL_ELEMENT, channelConfig);
        }

        if (channelsConfig.size() > 0)
            destinationConfig.addProperty(ConfigurationConstants.CHANNELS_ELEMENT, channelsConfig);

        return destinationConfig;
    }

    /**
     * Method for setting an extra property for the destination at runtime.
     *
     * @param name  The name of the property.
     * @param value The value of the property.
     */
    public void addExtraProperty(String name, Object value) {
        if (extraProperties == null) {
            extraProperties = new HashMap<String, Object>();
        }

        extraProperties.put(name, value);
    }

    /**
     * Method for getting an extra property at runtime.
     *
     * @param name The name of the property.
     * @return The value of the property or null if the property does not exist.
     */
    public Object getExtraProperty(String name) {
        return extraProperties != null ? extraProperties.get(name) : null;
    }

    //--------------------------------------------------------------------------
    //
    // Protected/private APIs
    //
    //--------------------------------------------------------------------------

    /**
     * Returns the log category of the <code>Destination</code>. Subclasses
     * can override to provide a more specific log category.
     *
     * @return The log category.
     */
    @Override
    protected String getLogCategory() {
        return LOG_CATEGORY;
    }

    /**
     * Invoked automatically to allow the <code>Destination</code> to setup its corresponding
     * MBean control. Subclasses should override to setup and register their MBean control.
     *
     * @param service The <code>Service</code> that manages this <code>Destination</code>.
     */
    protected void setupDestinationControl(Service service) {
        // Manageable subclasses should override this template method.
        setManaged(false);
    }
}
