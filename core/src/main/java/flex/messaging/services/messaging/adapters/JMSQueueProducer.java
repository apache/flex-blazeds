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

import java.io.Serializable;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.TextMessage;
import javax.naming.NamingException;

import flex.messaging.MessageException;

/**
 * A <code>JMSProducer</code> subclass specifically for JMS Queue senders.
 */
public class JMSQueueProducer extends JMSProducer {
    /* JMS related variables */
    private QueueSender sender;

    /**
     * Starts <code>JMSQueueProducer</code>.
     */
    public void start() throws NamingException, JMSException {
        super.start();

        // Establish queue
        Queue queue = null;
        try {
            queue = (Queue) destination;
        } catch (ClassCastException cce) {
            // JMS queue proxy for JMS destination ''{0}'' has a destination type of ''{1}'' which is not Queue.
            MessageException me = new MessageException();
            me.setMessage(JMSConfigConstants.NON_QUEUE_DESTINATION, new Object[]{destinationJndiName, destination.getClass().getName()});
            throw me;
        }

        // Create connection
        try {
            QueueConnectionFactory queueFactory = (QueueConnectionFactory) connectionFactory;
            if (connectionCredentials != null)
                connection = queueFactory.createQueueConnection(connectionCredentials.getUsername(), connectionCredentials.getPassword());
            else
                connection = queueFactory.createQueueConnection();
        } catch (ClassCastException cce) {
            // JMS queue proxy for JMS destination ''{0}'' has a connection factory type of ''{1}'' which is not QueueConnectionFactory.
            MessageException me = new MessageException();
            me.setMessage(JMSConfigConstants.NON_QUEUE_FACTORY, new Object[]{destinationJndiName, connectionFactory.getClass().getName()});
            throw me;
        }

        // Create queue session on the connection
        QueueConnection queueConnection = (QueueConnection) connection;
        session = queueConnection.createQueueSession(false, getAcknowledgeMode());

        // Create sender on the queue session
        QueueSession queueSession = (QueueSession) session;
        sender = queueSession.createSender(queue);
        producer = sender;

        // Start the connection
        connection.start();
    }

    @Override
    void sendTextMessage(String text, Map properties) throws JMSException {
        if (text == null)
            return;

        TextMessage message = session.createTextMessage();
        message.setText(text);
        copyHeadersToProperties(properties, message);
        sender.send(message, getDeliveryMode(), messagePriority, getTimeToLive(properties));
    }

    @Override
    void sendObjectMessage(Serializable obj, Map properties) throws JMSException {
        if (obj == null)
            return;

        ObjectMessage message = session.createObjectMessage();
        message.setObject(obj);
        copyHeadersToProperties(properties, message);
        sender.send(message, getDeliveryMode(), messagePriority, getTimeToLive(properties));
    }

    @Override
    void sendMapMessage(Map<String, ?> map, Map properties) throws JMSException {
        if (map == null)
            return;

        MapMessage message = session.createMapMessage();
        for (Map.Entry<String, ?> entry : map.entrySet())
            message.setObject(entry.getKey(), entry.getValue());
        copyHeadersToProperties(properties, message);
        sender.send(message, getDeliveryMode(), messagePriority, getTimeToLive(properties));
    }
}
