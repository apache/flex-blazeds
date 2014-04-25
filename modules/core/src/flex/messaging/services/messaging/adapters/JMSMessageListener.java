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

import java.util.EventListener;

/**
 * An interface to be notified when a JMS message is received by the JMS
 * consumer. Implementations of this interface may add themselves as listeners
 * via <code>JMSConsumer.addJMSMessageListener</code>.
 */
public interface JMSMessageListener extends EventListener
{
    /**
     * Notification that a JMS message was received.
     *
     * @param evt JMSMessageEvent to dispatch.
     */
    void messageReceived(JMSMessageEvent evt);
}