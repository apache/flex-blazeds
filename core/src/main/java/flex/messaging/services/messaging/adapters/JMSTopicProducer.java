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

import jakarta.jms.JMSException;
import jakarta.jms.MapMessage;
import jakarta.jms.ObjectMessage;
import jakarta.jms.TextMessage;
import jakarta.jms.Topic;
import jakarta.jms.TopicConnection;
import jakarta.jms.TopicConnectionFactory;
import jakarta.jms.TopicPublisher;
import jakarta.jms.TopicSession;
import javax.naming.NamingException;
import java.io.Serializable;
import java.util.Map;

/**
 * A <code>JMSProducer</code> subclass specifically for JMS Topic publishers.
 */
public class JMSTopicProducer extends JMSProducer {
    /* JMS related variables */
    private TopicPublisher publisher;

    /**
     * Starts <code>JMSTopicProducer</code>.
     */
    public void start() throws NamingException, JMSException {
        super.start();

        // Establish topic
        Topic topic = null;
        try {
            topic = (Topic) destination;
        } catch (ClassCastException cce) {
            // JMS topic proxy for JMS destination ''{0}'' has a destination type of ''{1}'' which is not Topic.
            MessageException me = new MessageException();
            me.setMessage(JMSConfigConstants.NON_TOPIC_DESTINATION, new Object[]{destinationJndiName, destination.getClass().getName()});
            throw me;
        }

        // Create connection
        try {
            TopicConnectionFactory topicFactory = (TopicConnectionFactory) connectionFactory;
            if (connectionCredentials != null)
                connection = topicFactory.createTopicConnection(connectionCredentials.getUsername(), connectionCredentials.getPassword());
            else
                connection = topicFactory.createTopicConnection();
        } catch (ClassCastException cce) {
            // JMS topic proxy for JMS destination ''{0}'' has a connection factory of type ''{1}'' which is not TopicConnectionFactory.
            MessageException me = new MessageException();
            me.setMessage(JMSConfigConstants.NON_TOPIC_FACTORY, new Object[]{destinationJndiName, connectionFactory.getClass().getName()});
            throw me;
        }

        // Create topic session on the connection
        TopicConnection topicConnection = (TopicConnection) connection;
        session = topicConnection.createTopicSession(false /* Always nontransacted */, getAcknowledgeMode());

        // Create publisher on the topic session
        TopicSession topicSession = (TopicSession) session;
        publisher = topicSession.createPublisher(topic);
        producer = publisher;

        // Start the connection
        connection.start();
    }

    void sendObjectMessage(Serializable obj, Map properties) throws JMSException {
        if (obj == null)
            return;

        ObjectMessage message = session.createObjectMessage();
        message.setObject(obj);
        copyHeadersToProperties(properties, message);
        publisher.publish(message, getDeliveryMode(), messagePriority, getTimeToLive(properties));
    }

    void sendTextMessage(String text, Map properties) throws JMSException {
        if (text == null)
            return;

        TextMessage message = session.createTextMessage();
        message.setText(text);
        copyHeadersToProperties(properties, message);
        publisher.publish(message, getDeliveryMode(), messagePriority, getTimeToLive(properties));
    }

    @Override
    void sendMapMessage(Map<String, ?> map, Map properties) throws JMSException {
        if (map == null)
            return;

        MapMessage message = session.createMapMessage();
        for (Map.Entry<String, ?> entry : map.entrySet())
            message.setObject(entry.getKey(), entry.getValue());
        copyHeadersToProperties(properties, message);
        publisher.publish(message, getDeliveryMode(), messagePriority, getTimeToLive(properties));
    }
}
