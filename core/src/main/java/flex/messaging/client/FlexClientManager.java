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
package flex.messaging.client;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;

import flex.management.ManageableComponent;
import flex.management.runtime.messaging.client.FlexClientManagerControl;
import flex.messaging.FlexContext;
import flex.messaging.MessageBroker;
import flex.messaging.MessageException;
import flex.messaging.config.FlexClientSettings;
import flex.messaging.endpoints.AbstractEndpoint;
import flex.messaging.endpoints.Endpoint;
import flex.messaging.log.Log;
import flex.messaging.log.LogCategories;
import flex.messaging.util.ClassUtil;
import flex.messaging.util.TimeoutAbstractObject;
import flex.messaging.util.TimeoutManager;

/**
 * Manages FlexClient instances for a MessageBroker.
 */
public class FlexClientManager extends ManageableComponent {
    public static final String TYPE = "FlexClientManager";

    //--------------------------------------------------------------------------
    //
    // Constructor
    //
    //--------------------------------------------------------------------------

    /**
     *
     */
    public FlexClientManager() {
        this(MessageBroker.getMessageBroker(null));
    }

    /**
     * Constructs a FlexClientManager for the passed MessageBroker.
     *
     * @param broker The MessageBroker that the Flex client manager is associated with.
     */
    public FlexClientManager(MessageBroker broker) {
        this(broker.isManaged(), broker);
    }

    /**
     *
     */
    public FlexClientManager(boolean enableManagement, MessageBroker mbroker) {
        super(enableManagement);

        super.setId(TYPE);

        // Ensure that we have a message broker:
        broker = (mbroker != null) ? mbroker : MessageBroker.getMessageBroker(null);

        FlexClientSettings flexClientSettings = broker.getFlexClientSettings();
        if (flexClientSettings != null && flexClientSettings.getTimeoutMinutes() != -1) {
            // Convert from minutes to millis.
            setFlexClientTimeoutMillis(flexClientSettings.getTimeoutMinutes() * 60 * 1000);
        }

        this.setParent(broker);
    }

    //--------------------------------------------------------------------------
    //
    // Variables
    //
    //--------------------------------------------------------------------------

    /**
     * The MessageBroker that owns this manager.
     */
    private final MessageBroker broker;

    /**
     * The Mbean controller for this manager.
     */
    private FlexClientManagerControl controller;

    /**
     * Table to store FlexClients by id.
     */
    private final Map<String, FlexClient> flexClients = new ConcurrentHashMap<String, FlexClient>();


    /**
     * Manages time outs for FlexClients.
     * This currently includes timeout of FlexClient instances, timeouts for async
     * long-poll handling, and scheduling delayed flushes of outbound messages.
     */
    private volatile TimeoutManager flexClientTimeoutManager;

    //--------------------------------------------------------------------------
    //
    // Properties
    //
    //--------------------------------------------------------------------------

    //----------------------------------
    //  clientIds
    //----------------------------------

    /**
     * Returns a string array of the client IDs.
     *
     * @return A string array of the client IDs.
     */
    public String[] getClientIds() {
        String[] ids = new String[flexClients.size()];
        ArrayList<String> idList = new ArrayList<String>(flexClients.keySet());

        for (int i = 0; i < flexClients.size(); i++)
            ids[i] = idList.get(i);

        return ids;
    }

    //----------------------------------
    //  flexClientCount
    //----------------------------------

    /**
     * Returns the number of FlexClients in use.
     *
     * @return The number of FlexClients in use.
     */
    public int getFlexClientCount() {
        return flexClients.size();
    }

    //----------------------------------
    //  flexClientTimeoutMillis
    //----------------------------------

    private volatile long flexClientTimeoutMillis;

    /**
     * Returns the idle timeout in milliseconds to apply to new FlexClient instances.
     *
     * @return The idle timeout in milliseconds to apply to new FlexClient instances.
     */
    public long getFlexClientTimeoutMillis() {
        return flexClientTimeoutMillis;
    }

    /**
     * Sets the idle timeout in milliseconds to apply to new FlexClient instances.
     *
     * @param value The idle timeout in milliseconds to apply to new FlexClient instances.
     */
    public void setFlexClientTimeoutMillis(long value) {
        if (value < 1)
            value = 0;

        synchronized (this) {
            flexClientTimeoutMillis = value;
        }
    }

    //----------------------------------
    //  messageBroker
    //----------------------------------

    /**
     * Returns the MessageBroker instance that owns this FlexClientManager.
     *
     * @return The parent MessageBroker instance.
     */
    public MessageBroker getMessageBroker() {
        return broker;
    }

    //--------------------------------------------------------------------------
    //
    // Public Methods
    //
    //--------------------------------------------------------------------------

    /**
     * Get FlexClient with the specified id or a new one will be created.
     * This method will return a valid existing FlexClient for the specific Id,
     * or a new FlexClient will created
     *
     * @param id The id of the Flex client.
     * @return FlexClient the FlexClient with the specified id
     */
    public FlexClient getFlexClient(String id) {
        return getFlexClient(id, true);
    }

    /**
     * Get the FlexClient with the specified id.
     *
     * @param id                  The id of the Flex client.
     * @param createNewIfNotExist if true, a new FlexClient will be created if not exist
     * @return FlexClient the FlexClient with the specified id
     */
    public FlexClient getFlexClient(String id, boolean createNewIfNotExist) {
        FlexClient flexClient = null;
        // Try to lookup an existing instance if we receive an id.
        if (id != null) {
            flexClient = flexClients.get(id);
            if (flexClient != null) {
                if (flexClient.isValid() && !flexClient.invalidating) {
                    flexClient.updateLastUse();
                    return flexClient;
                }
                // Invalid, remove it - it will be replaced below.
                flexClients.remove(id);
            }
        }
        // Use a manager-level lock (this) when creating/recreating a new FlexClient.
        synchronized (this) {
            if (id != null) {
                flexClient = flexClients.get(id);
                if (flexClient != null) {
                    flexClient.updateLastUse();
                    return flexClient;
                } else {
                    if (!createNewIfNotExist) {
                        return null;
                    }
                }
            }

            flexClient = createFlexClient(id);
            checkForNullAndDuplicateId(flexClient.getId());
            flexClients.put(flexClient.getId(), flexClient);
            if (flexClientTimeoutMillis > 0)
                flexClientTimeoutManager.scheduleTimeout(flexClient);
            flexClient.notifyCreated();
            return flexClient;
        }
    }

    /**
     * Creates a FlexClientOutboundQueueProcessor instance and hooks it up to the passed
     * FlexClient.
     *
     * @param flexClient The FlexClient to equip with a queue processor.
     * @param endpointId The Id of the endpoint the queue processor is used for.
     * @return The FlexClient with a configured queue processor.
     */
    public FlexClientOutboundQueueProcessor createOutboundQueueProcessor(FlexClient flexClient, String endpointId) {
        // First, try to create a custom outbound queue processor, if one exists.
        FlexClientOutboundQueueProcessor processor = createCustomOutboundQueueProcessor(flexClient, endpointId);

        // If no custom processor, then try to create default queue processor.
        if (processor == null)
            processor = createDefaultOutboundQueueProcessor(flexClient, endpointId);

        // If MessageBroker's default queue processor fails, use the default processor.
        if (processor == null) {
            processor = new FlexClientOutboundQueueProcessor();
            processor.setFlexClient(flexClient);
            processor.setEndpointId(endpointId);
        }

        return processor;
    }

    /**
     * Monitors an async poll for a FlexClient for timeout.
     *
     * @param asyncPollTimeout The async poll task to monitor for timeout.
     */
    public void monitorAsyncPollTimeout(TimeoutAbstractObject asyncPollTimeout) {
        flexClientTimeoutManager.scheduleTimeout(asyncPollTimeout);
    }

    /**
     * Monitors a scheduled flush for a FlexClient for timeout.
     *
     * @param scheduledFlushTimeout The schedule flush task to monitor for timeout.
     */
    public void monitorScheduledFlush(TimeoutAbstractObject scheduledFlushTimeout) {
        flexClientTimeoutManager.scheduleTimeout(scheduledFlushTimeout);
    }

    /**
     * Starts the Flex client manager.
     *
     * @see flex.management.ManageableComponent#start()
     */
    @Override
    public void start() {
        if (isManaged()) {
            controller = new FlexClientManagerControl(getParent().getControl(), this);
            setControl(controller);
            controller.register();
        }

        final String baseId = getId();
        flexClientTimeoutManager = new TimeoutManager(new ThreadFactory() {
            int counter = 1;

            public synchronized Thread newThread(Runnable runnable) {
                Thread t = new Thread(runnable);
                t.setName(baseId + "-FlexClientTimeoutThread-" + counter++);
                return t;
            }
        });
    }

    /**
     * @see flex.management.ManageableComponent#stop()
     */
    public void stop() {
        if (controller != null) {
            controller.unregister();
        }

        if (flexClientTimeoutManager != null)
            flexClientTimeoutManager.shutdown();
    }

    //--------------------------------------------------------------------------
    //
    // Protected Methods
    //
    //--------------------------------------------------------------------------

    /**
     * Hook method invoked when a new <tt>FlexClient</tt> instance is created.
     *
     * @param id The id the client provided, which was previously assigned by this server,
     *           or another server in a cluster. New clients will pass a <code>null</code>
     *           value in which case this server must generate a unique id.
     */
    protected FlexClient createFlexClient(String id) {
        return (id == null) ? new FlexClient(this) : new FlexClient(this, id);
    }

    /* (non-Javadoc)
     * @see flex.management.ManageableComponent#getLogCategory()
     */
    protected String getLogCategory() {
        return LogCategories.CLIENT_FLEXCLIENT;
    }

    /**
     * Removes a FlexClient from being managed by this manager.
     * This method is invoked by FlexClients when they are invalidated.
     *
     * @param flexClient The id of the FlexClient being invalidated.
     */
    protected void removeFlexClient(FlexClient flexClient) {
        if (flexClient != null) {
            String id = flexClient.getId();
            synchronized (id) {
                FlexClient storedClient = flexClients.get(id);
                // If the stored instance is the same as the invalidating instance based upon identity,
                // remove it.
                if (storedClient == flexClient)
                    flexClients.remove(id);
            }
        }
    }

    //--------------------------------------------------------------------------
    //
    // Private Methods
    //
    //--------------------------------------------------------------------------

    private void checkForNullAndDuplicateId(String id) {
        if (id == null) {
            // Cannot create ''{0}'' with null id.
            MessageException me = new MessageException();
            me.setMessage(10039, new Object[]{"FlexClient"});
            me.setCode("Server.Processing.NullId");
            throw me;
        }

        if (flexClients.containsKey(id)) {
            // Cannot create ''{0}'' with id ''{1}''; another ''{0}'' is already registered with the same id.
            MessageException me = new MessageException();
            me.setMessage(10040, new Object[]{"FlexClient", id});
            me.setCode("Server.Processing.DuplicateId");
            throw me;
        }
    }

    private FlexClientOutboundQueueProcessor createDefaultOutboundQueueProcessor(
            FlexClient flexClient, String endpointId) {
        FlexClientSettings flexClientSettings = broker.getFlexClientSettings();
        if (flexClientSettings == null)
            return null;

        String queueProcessorClassName = flexClientSettings.getFlexClientOutboundQueueProcessorClassName();
        if (queueProcessorClassName == null)
            return null;

        FlexClientOutboundQueueProcessor processor = null;
        try {
            Class queueProcessorClass = createClass(queueProcessorClassName);
            Object instance = ClassUtil.createDefaultInstance(queueProcessorClass, null);
            processor = (FlexClientOutboundQueueProcessor) instance;
            processor.setFlexClient(flexClient);
            processor.setEndpointId(endpointId);
            processor.initialize(flexClientSettings.getFlexClientOutboundQueueProcessorProperties());
        } catch (Throwable t) {
            String message = "Failed to create MessageBroker's outbound queue processor for FlexClient with id '" + flexClient.getId() + "'.";
            if (Log.isWarn())
                Log.getLogger(FlexClient.FLEX_CLIENT_LOG_CATEGORY).warn(message, t);

            MessageException me = new MessageException(message, t);
            throw me;
        }

        return processor;
    }

    private FlexClientOutboundQueueProcessor createCustomOutboundQueueProcessor(
            FlexClient flexClient, String endpointId) {
        FlexClientOutboundQueueProcessor processor = null;
        Endpoint endpoint = broker.getEndpoint(endpointId);
        if (endpoint instanceof AbstractEndpoint) {
            Class processorClass = ((AbstractEndpoint) endpoint).getFlexClientOutboundQueueProcessorClass();
            if (processorClass != null) {
                try {
                    Object instance = ClassUtil.createDefaultInstance(processorClass, null);
                    if (instance instanceof FlexClientOutboundQueueProcessor) {
                        processor = (FlexClientOutboundQueueProcessor) instance;
                        processor.setFlexClient(flexClient);
                        processor.setEndpointId(endpointId);
                        processor.initialize(((AbstractEndpoint) endpoint).getFlexClientOutboundQueueProcessorConfig());
                    }
                } catch (Throwable t) {
                    if (Log.isWarn())
                        Log.getLogger(FlexClient.FLEX_CLIENT_LOG_CATEGORY).warn("Failed to create custom outbound queue processor for FlexClient with id '" + flexClient.getId() + "'. Using MessageBroker's default queue processor.", t);
                }
            }
        }
        return processor;
    }

    private Class createClass(String className) {
        Class c = ClassUtil.createClass(className, FlexContext.getMessageBroker() == null ? null :
                FlexContext.getMessageBroker().getClassLoader());

        return c;
    }
}
