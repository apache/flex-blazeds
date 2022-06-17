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

import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * A <code>MessageReceiver</code> that receives messages asynchronously from JMS.
 */
class AsyncMessageReceiver implements MessageReceiver, ExceptionListener, MessageListener {
    private JMSConsumer jmsConsumer;

    /**
     * Constructs a new AsyncMessageReceiver.
     *
     * @param jmsConsumer JMSConsumer associated with the AsyncMessageReceiver.
     */
    public AsyncMessageReceiver(JMSConsumer jmsConsumer) {
        this.jmsConsumer = jmsConsumer;
    }

    /**
     * Implements MessageReceiver.startReceive.
     */
    public void startReceive() throws JMSException {
        jmsConsumer.setMessageListener(this);
    }

    /**
     * Implements MessageReceiver.stopReceive.
     */
    public void stopReceive() {
        // Nothing to do.
    }

    /**
     * Implements javax.jms.ExceptionListener.onException.
     *
     * @param exception JMS exception received from the JMS server.
     */
    public void onException(JMSException exception) {
        jmsConsumer.onException(exception);
    }

    /**
     * Implements javax.jms.MessageListener.onMessage.
     *
     * @param message JMS message received from the JMS server.
     */
    public void onMessage(Message message) {
        jmsConsumer.onMessage(message);
    }
}
