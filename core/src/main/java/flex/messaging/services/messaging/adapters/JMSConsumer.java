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

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

import jakarta.jms.ExceptionListener;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageListener;
import jakarta.jms.Session;
import javax.naming.NamingException;

import flex.messaging.MessageException;
import flex.messaging.log.Log;

/**
 * A JMSProxy subclass for <code>jakarta.jms.MessageConsumer</code> instance.
 */
public abstract class JMSConsumer extends JMSProxy implements ExceptionListener {
    /* JMS related variables */
    protected MessageConsumer consumer;

    protected MessageReceiver messageReceiver;
    protected String selectorExpression;

    // Keep track whether MessageReceiver was set manually by the user or JMSAdapter.
    // or automatically instantiated so appropriate error messages can be propagated
    // in the former and supressed in the latter case.
    private boolean messageReceiverManuallySet = false;

    /**
     * The lock to use to guard all state changes for the JMSConsumer.
     */
    protected Object lock = new Object();

    /**
     * The set of JMS message listeners to notify when a JMS message arrives.
     */
    private final CopyOnWriteArrayList jmsMessageListeners = new CopyOnWriteArrayList();

    /**
     * The set of JMS exception listeners to notify when a JMS exception is thrown.
     */
    private final CopyOnWriteArrayList jmsExceptionListeners = new CopyOnWriteArrayList();

    //--------------------------------------------------------------------------
    //
    // Initialize, validate, start, and stop methods.
    //
    //--------------------------------------------------------------------------

    /**
     * Starts the <code>JMSConsumer</code>. Subclasses should call <code>super.start</code>.
     *
     * @throws NamingException The thrown naming exception.
     * @throws JMSException    The thrown JMS exception.
     */
    public void start() throws NamingException, JMSException {
        super.start();

        if (Log.isInfo())
            Log.getLogger(JMSAdapter.LOG_CATEGORY).info("JMS consumer for JMS destination '"
                    + destinationJndiName + "' is starting.");
    }

    /**
     * Stops the <code>JMSConsumer</code> by stopping its associated receiver
     * adapter and closing the underlying <code>MessageConsumer</code>. It then
     * calls <code>JMSProxy.close</code> for session and connection closure.
     */
    public void stop() {
        if (Log.isInfo())
            Log.getLogger(JMSAdapter.LOG_CATEGORY).info("JMS consumer for JMS destination '"
                    + destinationJndiName + "' is stopping.");

        stopMessageReceiver();

        try {
            if (consumer != null)
                consumer.close();
        } catch (JMSException e) {
            if (Log.isWarn())
                Log.getLogger(JMSAdapter.LOG_CATEGORY).warn("JMS consumer for JMS destination '"
                        + destinationJndiName + "' received an error while closing its underlying MessageConsumer: "
                        + e.getMessage());
        }

        super.stop();
    }

    /**
     * Stops the <code>JMSConsumer</code> and unsubscribes a durable subscription
     * if one exists. By default this method delegates to <code>stop()</code>
     * and doesn't remove a durable subscription.
     *
     * @param unsubscribe Determines whether to unsubscribe a durable subscription
     *                    if one exists, or not.
     */
    public void stop(boolean unsubscribe) {
        stop();
    }

    //--------------------------------------------------------------------------
    //
    // Public Methods
    //
    //--------------------------------------------------------------------------

    /**
     * Adds a JMS message listener.
     *
     * @param listener The listener to add.
     * @see flex.messaging.services.messaging.adapters.JMSMessageListener
     */
    public void addJMSMessageListener(JMSMessageListener listener) {
        if (listener != null)
            jmsMessageListeners.addIfAbsent(listener);
    }

    /**
     * Removes a JMS message listener.
     *
     * @param listener The listener to remove.
     * @see flex.messaging.services.messaging.adapters.JMSMessageListener
     */
    public void removeJMSMessageListener(JMSMessageListener listener) {
        if (listener != null)
            jmsMessageListeners.remove(listener);
    }

    /**
     * Adds a JMS exception listener.
     *
     * @param listener The listener to add.
     * @see flex.messaging.services.messaging.adapters.JMSExceptionListener
     */
    public void addJMSExceptionListener(JMSExceptionListener listener) {
        if (listener != null)
            jmsExceptionListeners.addIfAbsent(listener);
    }

    /**
     * Removes a JMS exception listener.
     *
     * @param listener The listener to remove.
     * @see flex.messaging.services.messaging.adapters.JMSExceptionListener
     */
    public void removeJMSExceptionListener(JMSExceptionListener listener) {
        if (listener != null)
            jmsExceptionListeners.remove(listener);
    }

    /**
     * Sets the message listener of the underlying MessageConsumer. This method
     * is not meant to be directly called as it is used internally by
     * MessageReceivers that need to perform async message delivery. Any future
     * custom MessageReceiver implementations can use this method to set themselves
     * as MessageListeners to the underlying MessageConsumer.
     *
     * @param listener Message listener to set on the underlying MessageConsumer.
     * @return The old message listener associated with the MessageConsumer.
     * @throws JMSException The thrown JMS exception.
     */
    public MessageListener setMessageListener(MessageListener listener) throws JMSException {
        MessageListener oldListener = consumer.getMessageListener();
        consumer.setMessageListener(listener);
        return oldListener;
    }

    /**
     * Returns the <code>MessageReceiver</code> used by the consumer to retrieve
     * JMS messages.
     *
     * @return The <code>MessageReceiver</code> used.
     */
    public MessageReceiver getMessageReceiver() {
        return messageReceiver;
    }

    /**
     * Sets the <code>MessageReceiver</code> used by the consumer to retrieve
     * JMS messages. This property should not change after startup.
     *
     * @param messageReceiver The <code>MessageReceiver</code> used.
     */
    public void setMessageReceiver(MessageReceiver messageReceiver) {
        this.messageReceiver = messageReceiver;
        messageReceiverManuallySet = true;
    }

    /**
     * Returns the selector expression used when the underlying
     * <code>jakarta.jms.MessageConsumer</code> is created.
     *
     * @return The selector expression.
     */
    public String getSelectorExpression() {
        return selectorExpression;
    }

    /**
     * Sets the selector expression used when the underlying
     * <code>jakarta.jms.MessageConsumer</code> is created. This property should
     * not change after startup.
     *
     * @param selectorExpression The selector expression.
     */
    public void setSelectorExpression(String selectorExpression) {
        this.selectorExpression = selectorExpression;
    }

    /**
     * Implementation of jakarta.jms.ExceptionListener.onException.
     * Dispatches the JMS exception to registered JMS exception listeners.
     *
     * @param exception The thrown JMS exception.
     */
    public void onException(JMSException exception) {
        if (!jmsExceptionListeners.isEmpty()) {
            // CopyOnWriteArrayList is iteration-safe from ConcurrentModificationExceptions.
            for (Iterator iter = jmsExceptionListeners.iterator(); iter.hasNext(); )
                ((JMSExceptionListener) iter.next()).exceptionThrown(new JMSExceptionEvent(this, exception));
        }
    }

    /**
     * Acnowledges the receipt of the message to the JMS server and passes the
     * message to registered JMS message listeners.
     *
     * @param jmsMessage The new JMS message to acknowledge and dispatch.
     */
    public void onMessage(Message jmsMessage) {
        acknowledgeMessage(jmsMessage);

        if (!jmsMessageListeners.isEmpty()) {
            // CopyOnWriteArrayList is iteration-safe from ConcurrentModificationExceptions.
            for (Iterator iter = jmsMessageListeners.iterator(); iter.hasNext(); )
                ((JMSMessageListener) iter.next()).messageReceived(new JMSMessageEvent(this, jmsMessage));
        }
    }

    /**
     * Receive the next message from the underlying MessageConsumer or wait
     * indefinetely until a message arrives if there is no message.
     *
     * @return The received JMS message.
     * @throws JMSException The thrown JMS exception.
     */
    public Message receive() throws JMSException {
        if (Log.isInfo())
            Log.getLogger(JMSAdapter.LOG_CATEGORY).info(Thread.currentThread()
                    + " JMS consumer for JMS destination '" + destinationJndiName
                    + "' is waiting forever until a new message arrives.");

        return consumer.receive();
    }

    /**
     * Receive the next message from the underlying MessageConsumer within the
     * specified timeout interval.
     *
     * @param timeout The number of milliseconds to wait for a new message.
     * @throws JMSException The thrown JMS exception.
     */
    public Message receive(long timeout) throws JMSException {
        if (Log.isInfo())
            Log.getLogger(JMSAdapter.LOG_CATEGORY).info(Thread.currentThread()
                    + " JMS consumer for JMS destination '" + destinationJndiName
                    + "' is waiting " + timeout + " ms for new message to arrive");

        return consumer.receive(timeout);
    }

    /**
     * Receive the new message from the underlying MessageConsumer with no wait.
     *
     * @return The received JMS message.
     * @throws JMSException The thrown JMS exception.
     */
    public Message receiveNoWait() throws JMSException {
        return consumer.receiveNoWait();
    }

    //--------------------------------------------------------------------------
    //
    // Protected and Private Methods
    //
    //--------------------------------------------------------------------------

    /**
     * Start the Message Receiver of the <code>JMSConsumer</code>.
     *
     * @throws JMSException The thrown JMS exception.
     */
    void startMessageReceiver() throws JMSException {
        initializeMessageReceiver();
        messageReceiver.startReceive();
        connection.start();
    }

    /**
     * Stops the Message Receiver of the <code>JMSConsumer</code>.
     */
    void stopMessageReceiver() {
        if (messageReceiver != null)
            messageReceiver.stopReceive();
    }

    /**
     * Used internally to acknowledge the arrival of a message to the JMS server.
     *
     * @param message The JMS message to acknowledge.
     */
    protected void acknowledgeMessage(Message message) {
        if (getAcknowledgeMode() == Session.CLIENT_ACKNOWLEDGE) {
            try {
                message.acknowledge();
            } catch (JMSException e) {
                if (Log.isInfo())
                    Log.getLogger(JMSAdapter.LOG_CATEGORY).info("JMS consumer for JMS destination '"
                            + destinationJndiName + "' received an error in message acknowledgement: " + e.getMessage());
            }
        }
    }

    /**
     * Initializes the message receiver used by the <code>JMSConsumer</code>.
     * If the message receiver has been manually set, it validates the message
     * receiver. Otherwise, it initalizes an async message receiver if it can,
     * and falls back to sync message delivery if it cannot.
     * <p>
     * This method should be called by subclasses once there is an underlying
     * <code>jakarta.jms.MessageConsumer</code>.
     */
    private void initializeMessageReceiver() {
        // If an AsyncMessageReceiver is manually set, make sure the app server
        // allows MessageListener and ExceptionListener for JMS.
        if (messageReceiverManuallySet && messageReceiver != null) {
            if (messageReceiver instanceof AsyncMessageReceiver) {
                String restrictedMethod = null;
                try {
                    // Test if MessageListener is restricted.
                    restrictedMethod = "jakarta.jms.MessageConsumer.setMessageListener";
                    consumer.getMessageListener();

                    // Test if ExceptionListener is restricted.
                    restrictedMethod = "jakarta.jms.Connection.setExceptionListener";
                    connection.setExceptionListener((AsyncMessageReceiver) messageReceiver);

                    if (Log.isInfo())
                        Log.getLogger(JMSAdapter.LOG_CATEGORY).info("JMS consumer for JMS destination '"
                                + destinationJndiName + "' is using async message receiver.");
                } catch (JMSException jmsEx) {
                    // JMS consumer for JMS destination ''{0}'' is configured to use async message receiver but the application server does not allow ''{1}'' call used in async message receiver. Please switch to sync message receiver.
                    MessageException me = new MessageException();
                    me.setMessage(JMSConfigConstants.ASYNC_MESSAGE_DELIVERY_NOT_SUPPORTED, new Object[]{destinationJndiName, restrictedMethod});
                    throw me;
                }
            } else if (messageReceiver instanceof SyncMessageReceiver) {
                SyncMessageReceiver smr = (SyncMessageReceiver) messageReceiver;
                if (Log.isInfo()) {
                    Log.getLogger(JMSAdapter.LOG_CATEGORY).info("JMS consumer for JMS destination '"
                            + destinationJndiName + "' is using sync message receiver"
                            + " with sync-receive-interval-millis: " + smr.getSyncReceiveIntervalMillis()
                            + ", sync-receive-wait-millis: " + smr.getSyncReceiveWaitMillis());
                }
            }
        }
        // If no MessageReceiver was manually set, set a default MessageReceiver
        // with the following strategy: First try async message delivery. If the
        // app server doesn't allow it, switch to sync message delivery.
        else {
            try {
                messageReceiver = new AsyncMessageReceiver(this);

                // Test if MessageListener is restricted.
                consumer.getMessageListener();
                // Test if ExceptionListener is restricted.
                connection.setExceptionListener((AsyncMessageReceiver) messageReceiver);

                if (Log.isInfo())
                    Log.getLogger(JMSAdapter.LOG_CATEGORY).info("JMS consumer for JMS destination '"
                            + destinationJndiName + "' is using async message receiver.");
            } catch (JMSException e) {
                SyncMessageReceiver smr = new SyncMessageReceiver(this);
                smr.setSyncReceiveIntervalMillis(1);
                smr.setSyncReceiveWaitMillis(-1);
                messageReceiver = smr;

                if (Log.isInfo()) {
                    Log.getLogger(JMSAdapter.LOG_CATEGORY).info("JMS consumer for JMS destination '"
                            + destinationJndiName + "' is using sync message receiver"
                            + " with sync-receive-interval-millis: " + smr.getSyncReceiveIntervalMillis()
                            + ", sync-receive-wait-millis: " + smr.getSyncReceiveWaitMillis());
                }
            }
        }
    }
}
