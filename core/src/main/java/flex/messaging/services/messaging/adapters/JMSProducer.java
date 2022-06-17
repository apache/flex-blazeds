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

import flex.messaging.MessageException;
import flex.messaging.config.ConfigurationException;
import flex.messaging.log.Log;
import flex.messaging.messages.MessagePerformanceUtils;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.naming.NamingException;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;

/**
 * A JMSProxy subclass for <code>javax.jms.MessageProducer</code> instances.
 */
public abstract class JMSProducer extends JMSProxy {
    /* JMS related variables */
    protected MessageProducer producer;

    protected int deliveryMode;
    protected int messagePriority;
    protected String messageType;

    //--------------------------------------------------------------------------
    //
    // Constructor
    //
    //--------------------------------------------------------------------------

    /**
     * Create a new JMSProducer with default delivery mode of <code>javax.jms.Message.DEFAULT_DELIVERY_MODE</code>
     * and default message priority of <code>javax.jms.Message.DEFAULT_PRIORITY</code>.
     */
    public JMSProducer() {
        super();
        deliveryMode = javax.jms.Message.DEFAULT_DELIVERY_MODE;
        messagePriority = javax.jms.Message.DEFAULT_PRIORITY;
    }

    //--------------------------------------------------------------------------
    //
    // Initialize, validate, start, and stop methods.
    //
    //--------------------------------------------------------------------------

    /**
     * Initialize with settings from the JMS adapter.
     *
     * @param settings JMS settings to use for initialization.
     */
    public void initialize(JMSSettings settings) {
        super.initialize(settings);

        String deliveryString = settings.getDeliveryMode();
        if (deliveryString.equals(JMSConfigConstants.DEFAULT_DELIVERY_MODE))
            deliveryMode = javax.jms.Message.DEFAULT_DELIVERY_MODE;
        else if (deliveryString.equals(JMSConfigConstants.PERSISTENT))
            deliveryMode = javax.jms.DeliveryMode.PERSISTENT;
        else if (deliveryString.equals(JMSConfigConstants.NON_PERSISTENT))
            deliveryMode = javax.jms.DeliveryMode.NON_PERSISTENT;

        messagePriority = settings.getMessagePriority();
        messageType = settings.getMessageType();
    }

    /**
     * Verifies that the <code>JMSProducer</code> is in valid state before
     * it is started. For <code>JMSProducer</code> to be in valid state, it needs
     * to have a message type assigned.
     */
    protected void validate() {
        super.validate();

        if (messageType == null || !(messageType.equals(JMSConfigConstants.TEXT_MESSAGE)
                || messageType.equals(JMSConfigConstants.OBJECT_MESSAGE)
                || messageType.equals(JMSConfigConstants.MAP_MESSAGE))) {
            // Unsupported JMS Message Type ''{0}''. Valid values are javax.jms.TextMessage, javax.jms.ObjectMessage, javax.jms.MapMessage.
            ConfigurationException ce = new ConfigurationException();
            ce.setMessage(JMSConfigConstants.INVALID_JMS_MESSAGE_TYPE, new Object[]{messageType});
            throw ce;
        }
    }

    /**
     * Starts the <code>JMSProducer</code>. Subclasses should call <code>super.start</code>.
     */
    public void start() throws NamingException, JMSException {
        super.start();

        if (Log.isInfo())
            Log.getLogger(JMSAdapter.LOG_CATEGORY).info("JMS producer for JMS destination '"
                    + destinationJndiName + "' is starting.");
    }

    /**
     * Stops the <code>JMSProducer</code> by closing its underlying
     * <code>MessageProducer</code>. It then calls <code>JMSProxy.close</code>
     * for session and connection closure.
     */
    public void stop() {
        if (Log.isInfo())
            Log.getLogger(JMSAdapter.LOG_CATEGORY).info("JMS producer for JMS destination '" +
                    destinationJndiName + "' is stopping.");

        try {
            if (producer != null)
                producer.close();
        } catch (JMSException e) {
            if (Log.isWarn())
                Log.getLogger(JMSAdapter.LOG_CATEGORY).warn("JMS producer for JMS destination '" +
                        destinationJndiName + "' received an error while closing"
                        + " its underlying MessageProducer: " + e.getMessage());
        }

        super.stop();
    }

    //--------------------------------------------------------------------------
    //
    // Public Methods
    //
    //--------------------------------------------------------------------------

    /**
     * Returns the delivery mode used by the <code>JMSProducer</code>.
     *
     * @return The delivery mode used by the <code>JMSProducer</code>.
     */
    public int getDeliveryMode() {
        return deliveryMode;
    }

    /**
     * Sets the delivery mode used by the <code>JMSProducer</code>. Valid values
     * javax.jms.DeliveryMode.PERSISTENT and javax.jms.DeliveryMode.NON_PERSISTENT.
     * This propery is optional and defaults to javax.jms.Message.DEFAULT_DELIVERY_MODE.
     *
     * @param deliveryMode
     */
    public void setDeliveryMode(int deliveryMode) {
        if (deliveryMode == javax.jms.Message.DEFAULT_DELIVERY_MODE
                || deliveryMode == javax.jms.DeliveryMode.NON_PERSISTENT
                || deliveryMode == javax.jms.DeliveryMode.PERSISTENT)
            this.deliveryMode = deliveryMode;
    }

    /**
     * Returns the message priority used by the <code>JMSProducer</code>.
     *
     * @return an int specifying the message priority.
     */
    public int getMessagePriority() {
        return messagePriority;
    }

    /**
     * Sets the message priority used by the <code>JMSProducer</code>.
     * This property is optional and defaults to <code>javax.jms.Message.DEFAULT_PRIORITY</code>.
     *
     * @param messagePriority an int specifying the message priority.
     */
    public void setMessagePriority(int messagePriority) {
        this.messagePriority = messagePriority;
    }

    /**
     * Returns the message type used by the <code>JMSProducer</code>.
     *
     * @return The message type used by the <code>JMSProducer</code>.
     */
    public String getMessageType() {
        return messageType;
    }

    /**
     * Sets the message type used by the <code>JMSProducer</code>. Supported
     * types are <code>javax.jms.TextMessage</code> and <code>javax.jms.ObjectMessage</code>.
     * This property is mandatory.
     *
     * @param messageType String representing the message type used.
     */
    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    //--------------------------------------------------------------------------
    //
    // Protected and Private Methods
    //
    //--------------------------------------------------------------------------

    protected void copyHeadersToProperties(Map properties, javax.jms.Message message) throws JMSException {
        // Generic Flex headers become JMS properties, named Flex headers become JMS headers
        for (Iterator iter = properties.keySet().iterator(); iter.hasNext(); ) {
            String propName = (String) iter.next();
            Object propValue = properties.get(propName);

            // For now, only named property is TTL.
            if (!propName.equals(JMSConfigConstants.TIME_TO_LIVE)) {
                // MPI header contains a MessagePerformaceInfo object that cannot
                // be set as a JMS header property. Instead, it is broken down
                // to its primitive types and each primitive is individually
                // set as a JMS header property.
                if (propName.equals(MessagePerformanceUtils.MPI_HEADER_IN)) {
                    Field[] fields = propValue.getClass().getFields();
                    for (int i = 0; i < fields.length; i++) {
                        Field field = fields[i];
                        // Use MPI_HEADER_IN as prefix to the property name so that
                        // they can be distinguished later when the MessagePerformanceInfo
                        // object gets built from the headers.
                        String mpiPropertyName = MessagePerformanceUtils.MPI_HEADER_IN + field.getName();
                        Object mpiPropertyValue = null;
                        try {
                            mpiPropertyValue = field.get(propValue);
                            message.setObjectProperty(mpiPropertyName, mpiPropertyValue);
                        } catch (Exception e) {
                            if (Log.isWarn())
                                Log.getLogger(JMSAdapter.LOG_CATEGORY).warn("JMSProducer could not retrieve the value of MessagePerformanceUtils property '"
                                        + propValue + "' from the Flex message, therefore it will not be set on the JMS message.");
                        }
                    }
                } else if (propValue != null) {
                    message.setObjectProperty(propName, propValue);
                }
            }
        }
    }

    protected long getTimeToLive(Map properties) throws JMSException {
        long timeToLive = producer.getTimeToLive();
        if (properties.containsKey(JMSConfigConstants.TIME_TO_LIVE)) {
            long l = ((Long) properties.get(JMSConfigConstants.TIME_TO_LIVE)).longValue();
            if (l != 0) {
                // Don't let Flex default override JMS implementation default,
                // only explicit ActionScript TTL usage overrides JMS TTL.
                timeToLive = l;
            }
        }
        return timeToLive;
    }

    void sendMessage(flex.messaging.messages.Message flexMessage) throws JMSException {
        MessagePerformanceUtils.markServerPreAdapterExternalTime(flexMessage);

        if (JMSConfigConstants.TEXT_MESSAGE.equals(messageType)) {
            sendTextMessage(flexMessage.getBody().toString(), flexMessage.getHeaders());
        } else if (JMSConfigConstants.OBJECT_MESSAGE.equals(messageType)) {
            try {
                sendObjectMessage((Serializable) flexMessage.getBody(), flexMessage.getHeaders());
            } catch (ClassCastException ce) {
                // The body of the Flex Message could not be converted to a Serializable Java Object.
                MessageException me = new MessageException();
                me.setMessage(JMSConfigConstants.NONSERIALIZABLE_MESSAGE_BODY);
                throw me;
            }
        } else if (JMSConfigConstants.MAP_MESSAGE.equals(messageType)) {
            try {
                sendMapMessage((Map<String, ?>) flexMessage.getBody(), flexMessage.getHeaders());
            } catch (ClassCastException ce) {
                // 10812=The body of the Flex message could not be converted to a Java Map object.
                MessageException me = new MessageException();
                me.setMessage(JMSConfigConstants.NONMAP_MESSAGE_BODY);
                throw me;
            }
        }
    }

    abstract void sendObjectMessage(Serializable obj, Map properties) throws JMSException;

    abstract void sendTextMessage(String text, Map properties) throws JMSException;

    abstract void sendMapMessage(Map<String, ?> map, Map properties) throws JMSException;
}
