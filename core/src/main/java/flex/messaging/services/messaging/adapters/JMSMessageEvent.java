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

import java.util.EventObject;
import jakarta.jms.Message;

/**
 * Event dispatched to the JMSMessageListener when a JMS message is received
 * by the source.
 *
 * @see flex.messaging.services.messaging.adapters.JMSMessageListener
 */
public class JMSMessageEvent extends EventObject {
    private Message message;

    /**
     * Create a new JMSMessageEvent with the source and message.
     *
     * @param source       The source of the message.
     * @param jmsException The actual JMS message.
     */
    JMSMessageEvent(JMSConsumer source, jakarta.jms.Message message) {
        super(source);
        this.message = message;
    }

    /**
     * Return the JMS message of the event.
     *
     * @return The JMS message of the event.
     */
    public Message getJMSMessage() {
        return message;
    }
}