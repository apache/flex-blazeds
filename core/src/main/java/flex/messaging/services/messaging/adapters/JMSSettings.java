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
package flex.messaging.services.messaging.adapters;

import java.util.Hashtable;

import flex.messaging.config.ConfigurationException;

/**
 * Settings for <code>JMSAdapter</code>.
 */
public class JMSSettings
{
    private String acknowledgeMode;
    private String connectionFactory;
    private String connectionUsername;
    private String connectionPassword;
    private String deliveryMode;
    private String destinationJNDIName;
    private DeliverySettings deliverySettings;
    private String destinationType;
    private boolean durableConsumers;
    private Hashtable initialContextEnvironment;
    private int maxProducers;
    private int messagePriority;
    private String messageType;
    private boolean preserveJMSHeaders;

    /**
     * Creates a <code>JMSSettings</code> instance with the following default
     * values: acknowledge mode of AUTO_ACKNOWLEDGE, delivery  mode of
     * DEFAULT_DELIVERY_MODE, destination type of Topic, and default
     * delivery setting.
     */
    public JMSSettings()
    {
        acknowledgeMode = JMSConfigConstants.AUTO_ACKNOWLEDGE;
        deliveryMode = JMSConfigConstants.DEFAULT_DELIVERY_MODE;
        destinationType = JMSConfigConstants.TOPIC;
        deliverySettings = new DeliverySettings();
        maxProducers = JMSConfigConstants.defaultMaxProducers;
        messagePriority = javax.jms.Message.DEFAULT_PRIORITY;
        preserveJMSHeaders = JMSConfigConstants.defaultPreserveJMSHeaders;
    }

    /**
     * Returns the <code>acknowledge-mode</code> property.
     *
     * @return a String containing the <code>acknowledge-mode</code>
     */
    public String getAcknowledgeMode()
    {
        return acknowledgeMode;
    }

    /**
     * Sets the <code>acknowledge-mode</code> property which is the message
     * acknowledgement mode for the JMS adapter. None of these modes require any
     * action on the part of the Flex messaging client. This property is optional
     * and defautls to AUTO_ACKNOWLEDGE.
     *
     * @param mode Message acknowledgement mode. Supported modes are:
     * AUTO_ACKNOWLEDGE - the JMS provider client runtime automatically acknowledges the messages.
     * DUPS_OK_ACKNOWLEDGE - auto-acknowledgement of the messages is not required.
     * CLIENT_ACKNOWLEDGE - the JMS adapter should acknowledge that the message was received.
     *
     */
    public void setAcknowledgeMode(String mode)
    {
        if (mode == null)
        {
            acknowledgeMode = JMSConfigConstants.defaultAcknowledgeMode;
            return;
        }

        mode = mode.toLowerCase();

        if (!(mode.equals(JMSConfigConstants.AUTO_ACKNOWLEDGE)
                || mode.equals(JMSConfigConstants.DUPS_OK_ACKNOWLEDGE)
                || mode.equals(JMSConfigConstants.CLIENT_ACKNOWLEDGE)) )
        {
            // Invalid Acknowledge Mode ''{0}''. Valid values are AUTO_ACKNOWLEDGE, DUPS_OK_ACKNOWLEDGE, and CLIENT_ACKNOWLEDGE.
            ConfigurationException ce = new ConfigurationException();
            ce.setMessage(JMSConfigConstants.INVALID_ACKNOWLEDGE_MODE, new Object[] {mode});
            throw ce;
        }
        acknowledgeMode = mode;
    }

    /**
     * Returns the <code>connection-factory</code> property.
     *
     * @return a String containing the <code>connection-factory</code>.
     */
    public String getConnectionFactory()
    {
        return connectionFactory;
    }

    /**
     * Sets the <code>connection-factory</code> property which is the name of
     * the JMS connection factory in JNDI. This property is required and it
     * cannot be null.
     *
     * @param factory The non-null name of the JMS connection factory.
     */
    public void setConnectionFactory(String factory)
    {
        if (factory == null)
        {
            // JMS connection factory of message destinations with JMS Adapters must be specified.
            ConfigurationException ce = new ConfigurationException();
            ce.setMessage(JMSConfigConstants.MISSING_CONNECTION_FACTORY);
            throw ce;
        }
        connectionFactory = factory;
    }

    /**
     * Returns the connection username used while creating JMS connections.
     *
     * @return The connection username used while creating JMS connections.
     */
    public String getConnectionUsername()
    {
        return connectionUsername;
    }

    /**
     * Sets the connection username used while creating JMS connections.
     * This is optional and only needed when connection level JMS authentication
     * is being used.
     *
     * @param connectionUsername The connection username used while creating JMS connections.
     */
    public void setConnectionUsername(String connectionUsername)
    {
        this.connectionUsername = connectionUsername;
    }

    /**
     * Returns the connection password used while creating JMS connections.
     *
     * @return The connection password used while creating JMS connections.
     */
    public String getConnectionPassword()
    {
        return connectionPassword;
    }

    /**
     * Sets the connection password used while creating JMS connections.
     * This is optional and only needed when connection level JMS authentication
     * is being used.
     *
     * @param connectionPassword The connection password used while creating JMS connections.
     */
    public void setConnectionPassword(String connectionPassword)
    {
        this.connectionPassword = connectionPassword;
    }

    /**
     * Returns the <code>delivery-mode</code> property.
     *
     * @return a String containing the <code>delivery-mode</code>.
     */
    public String getDeliveryMode()
    {
        return deliveryMode;
    }

    /**
     * Sets the <code>delivery-mode</code> property which is the JMS DeliveryMode
     * for producers. This property optional and defaults to DEFAULT_DELIVERY_MODE.
     *
     * @param mode The delivery mode. Valid values are DEFAULT_DELIVERY_MODE,
     * PERSISTENT, and NON_PERSISTENT.
     */
    public void setDeliveryMode(String mode)
    {
        if (mode == null)
        {
            deliveryMode = JMSConfigConstants.DEFAULT_DELIVERY_MODE;
            return;
        }

        mode = mode.toLowerCase();

        if (!(mode.equals(JMSConfigConstants.DEFAULT_DELIVERY_MODE)
                || mode.equals(JMSConfigConstants.PERSISTENT)
                || mode.equals(JMSConfigConstants.NON_PERSISTENT)))
        {
            // Invalid Delivery Mode ''{0}''. Valid values are DEFAULT_DELIVERY_MODE, PERSISTENT, and NON_PERSISTENT.
            ConfigurationException ce = new ConfigurationException();
            ce.setMessage(JMSConfigConstants.INVALID_DELIVERY_MODE, new Object[] {mode});
            throw ce;
        }
        deliveryMode = mode;
    }

    /**
     * Returns the <code>delivery-settings</code> property.
     *
     * @return The <code>delivery-settings</code> property.
     */
    public DeliverySettings getDeliverySettings()
    {
        return deliverySettings;
    }

    /**
     * Sets the <code>delivery-settings</code> property. This property is
     * optional and defaults to default settings as described in
     * <code>DeliverySettings</code> inner class.
     *
     * @param deliverySettings The <code>delivery-settings</code> property.
     */
    public void setDeliverySettings(DeliverySettings deliverySettings)
    {
        this.deliverySettings = deliverySettings;
    }

    /**
     * Returns the <code>destination-jndi-name</code> property.
     *
     * @return a String containing the <code>destination-jndi-name</code>
     */
    public String getDestinationJNDIName()
    {
        return destinationJNDIName;
    }

    /**
     * Sets the <code>destination-jndi-name</code> property which is the name of
     * the destination in JNDI. This value is required and it cannot be null.
     *
     * @param name The non-null name of the destination in JNDI.
     */
    public void setDestinationJNDIName(String name)
    {
        if (name == null)
        {
            // JNDI names for message destinations with JMS Adapters must be specified.
            ConfigurationException ce = new ConfigurationException();
            ce.setMessage(JMSConfigConstants.MISSING_DESTINATION_JNDI_NAME);
            throw ce;
        }
        destinationJNDIName = name;
    }

    /**
     * Destination-name property is not used anymore.
     *
     * @deprecated
     * @return null.
     */
    public String getDestinationName()
    {
        return null;
    }

    /**
     * Destination-name property is not used anymore.
     *
     * @deprecated
     * @param name The name of the destination.
     */
    public void setDestinationName(String name)
    {
        // No-op
    }

    /**
     * Returns the <code>destination-type</code> property.
     *
     * @return a String containing the <code>destination-type</code>.
     */
    public String getDestinationType()
    {
        return destinationType;
    }

    /**
     * Sets the <code>destination-type</code> property which determines whether
     * the adapter is performing topic (pub/sub) or queue (point-to-point)
     * messaging. This element is optional and defaults to Topic.
     *
     * @param type The destination type. Valid values are Topic and Queue.
     */
    public void setDestinationType(String type)
    {
        if (type == null)
        {
            destinationType = JMSConfigConstants.defaultDestinationType;
            return;
        }

        type = type.toLowerCase();

        if (!(type.equals(JMSConfigConstants.TOPIC) || type.equals(JMSConfigConstants.QUEUE)))
        {
            // JMS Adapter destination type must be Topic or Queue.
            ConfigurationException ce = new ConfigurationException();
            ce.setMessage(JMSConfigConstants.INVALID_DESTINATION_TYPE);
            throw ce;
        }
        destinationType = type;
    }

    /**
     * Returns whether consumers are durable or not.
     *
     * @return <code>true</code> is consumers are durable, <code>false</code>
     * otherwise.
     */
    public boolean useDurableConsumers()
    {
        return durableConsumers;
    }

    /**
     * Sets whethers consumers are durable or not. This property is optional
     * and defaults to false.
     *
     * @param durable A boolean indicating whether consumers should be durable.
     */
    public void setDurableConsumers(boolean durable)
    {
        durableConsumers = durable;
    }

    /**
     * Returns the <code>initial-context-environment</code> property.
     *
     * @return a Hashtable of the <code>initial-context-environment</code>.
     */
    public Hashtable getInitialContextEnvironment()
    {
        return initialContextEnvironment;
    }

    /**
     * Sets the <code>initial-context-environment</code> property. This property
     * is optional.
     *
     * @param env A Hashtable of the <code>initial-context-environment</code>.
     */
    public void setInitialContextEnvironment(Hashtable env)
    {
        initialContextEnvironment = env;
    }

    /**
     * Returns the <code>max-producers</code> property.
     *
     * @return an int representing the <code>max-producers</code>.
     */
    public int getMaxProducers()
    {
        return maxProducers;
    }

    /**
     * Sets the <code>max-producers</code> property which is the maximum number
     * of producer proxies that this destination should use when communicating
     * with the JMS Server. This property is optional and defaults to 1 which
     * implies all clients using this destinatin will share the same connection
     * to the JMS server.
     *
     * @param value an int representing the <code>max-producers</code>.
     */
    public void setMaxProducers(int value)
    {
        if (value < 1)
            value = JMSConfigConstants.defaultMaxProducers;
        maxProducers = value;
    }

    /**
     * Returns the <code>message-priority</code> property.
     *
     * @return an int specifying the <code>message-priority</code>
     */
    public int getMessagePriority()
    {
        return messagePriority;
    }

    /**
     * Sets the <code>message-priority</code> property which is the JMS priority
     * for messages sent by Flex producers. This property is optional and
     * defaults to <code>javax.jms.Message.DEFAULT_PRIORITY</code>.
     *
     * @param priority an int specifying the <code>message-priority</code>.
     */
    public void setMessagePriority(int priority)
    {
        messagePriority = priority;
    }

    /**
     * Returns the <code>message-type</code> property.
     *
     * @return a String containing the <code>message-type</code>.
     */
    public String getMessageType()
    {
        return messageType;
    }

    /**
     * Sets the <code>message-type</code> property which is the
     * <code>javax.jms.Message</code> type which the adapter should use for the
     * destination.
     *
     * @param type The <code>message-type</code> property. Supported types are
     * <code>javax.jms.TextMessage</code>, <code>javax.jms.ObjectMessage</code>,
     * and <code>javax.jms.MapMessage</code>.
     */
    public void setMessageType(String type)
    {
        if (type == null || !(type.equals(JMSConfigConstants.TEXT_MESSAGE)
                || type.equals(JMSConfigConstants.OBJECT_MESSAGE)
                || type.equals(JMSConfigConstants.MAP_MESSAGE)) )
        {
            // Unsupported JMS Message Type ''{0}''. Valid values are javax.jms.TextMessage and javax.jms.ObjectMessage.
            ConfigurationException ce = new ConfigurationException();
            ce.setMessage(JMSConfigConstants.INVALID_JMS_MESSAGE_TYPE, new Object[] {type});
            throw ce;
        }
        messageType = type;
    }

    /**
     * Returns the <code>preserve-jms-headers</code> property.
     *
     * @return The <code>preserve-jms-headers</code> property.
     */
    public boolean isPreserveJMSHeaders()
    {
        return preserveJMSHeaders;
    }

    /**
     * Sets the <code>preserve-jms-headers</code> property. This property is
     * optional and defaults to true.
     *
     * @param preserveJMSHeaders The <code>preserve-jms-headers</code> property.
     */
    public void setPreserveJMSHeaders(boolean preserveJMSHeaders)
    {
        this.preserveJMSHeaders = preserveJMSHeaders;
    }

    /**
     * Transacted-session property is not used anymore.
     *
     * @deprecated
     * @return false.
     */
    public boolean isTransactedSessions()
    {
        return false;
    }

    /**
     * Transacted-session property is not used anymore.
     *
     * @deprecated
     * @param mode The transacted-session property.
     */
    public void setTransactedSessions(boolean mode)
    {
        // No-op
    }

    //--------------------------------------------------------------------------
    //
    // Nested Classes
    //
    //--------------------------------------------------------------------------

    /**
     * A static inner class for delivery settings.
     */
    public static class DeliverySettings
    {
        private String mode;
        private long syncReceiveIntervalMillis;
        private long syncReceiveWaitMillis;

        /**
         * Creates a default <code>DeliverySettings</code> instance with default
         * settings.
         */
        public DeliverySettings()
        {
            mode = JMSConfigConstants.SYNC;
            syncReceiveIntervalMillis = JMSConfigConstants.defaultSyncReceiveIntervalMillis;
            syncReceiveWaitMillis = JMSConfigConstants.defaultSyncReceiveWaitMillis;
        }

        /**
         * Returns the message delivery mode.
         *
         * @return The message delivery mode.
         */
        public String getMode()
        {
            return mode;
        }
        /**
         * Sets the message delivery mode. This property is optional and defaults
         * to sync.
         *
         * @param mode The message delivery mode. Valid values are async and sync.
         */
        public void setMode(String mode)
        {
            if (mode == null)
            {
                mode = JMSConfigConstants.defaultMode;
                return;
            }

            mode = mode.toLowerCase();

            if (!(mode.equals(JMSConfigConstants.ASYNC) || mode.equals(JMSConfigConstants.SYNC)))
            {
                // Invalid delivery-settings mode ''{0}''. Valid values are async and sync.
                ConfigurationException ce = new ConfigurationException();
                ce.setMessage(JMSConfigConstants.INVALID_DELIVERY_MODE_VALUE, new Object[] {mode});
                throw ce;
            }
            this.mode = mode;
        }

        /**
         * Returns the interval of the sync receive message call.
         *
         * @return The interval of the sync receive message call.
         */
        public long getSyncReceiveIntervalMillis()
        {
            return syncReceiveIntervalMillis;
        }

        /**
         * Sets the interval of the receive message call. This property
         * is optional and defaults to 100.
         *
         * @param syncReceiveIntervalMillis A positive long that indicates
         * the interval of the receive message call.
         */
        public void setSyncReceiveIntervalMillis(long syncReceiveIntervalMillis)
        {
            if (syncReceiveIntervalMillis < 1)
                syncReceiveIntervalMillis = JMSConfigConstants.defaultSyncReceiveIntervalMillis;
            this.syncReceiveIntervalMillis = syncReceiveIntervalMillis;
        }

        /**
         * Returns how long a JMS proxy waits for a message before returning.
         *
         * @return How long a JMS proxy waits for a message before returning.
         */
        public long getSyncReceiveWaitMillis()
        {
            return syncReceiveWaitMillis;
        }

        /**
         * Sets how long a JMS proxy waits for a message before returning.
         * This property is optional and defaults to zero (no wait).
         *
         * @param syncReceiveWaitMillis A non-negative value that indicates how
         * long a JMS proxy waits for a message before returning. Zero means no
         * wait, negative one means wait until a message arrives.
         */
        public void setSyncReceiveWaitMillis(long syncReceiveWaitMillis)
        {
            if (syncReceiveWaitMillis < -1)
                syncReceiveWaitMillis = JMSConfigConstants.defaultSyncReceiveWaitMillis;
            this.syncReceiveWaitMillis = syncReceiveWaitMillis;
        }
    }
}
