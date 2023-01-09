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
package flex.management.runtime.messaging.services.messaging.adapters;

import java.io.IOException;

import flex.management.runtime.messaging.services.ServiceAdapterControlMBean;

/**
 * Defines the runtime monitoring and management interface for managed JMS adapters.
 */
public interface JMSAdapterControlMBean extends ServiceAdapterControlMBean {
    /**
     * Returns the number of topic producers for the adapter.
     *
     * @return The number of topic producers for the adapter.
     * @throws IOException Throws IOException.
     */
    Integer getTopicProducerCount() throws IOException;

    /**
     * Returns the number of topic consumers for the adapter.
     *
     * @return The number of topic consumers for the adapter.
     * @throws IOException Throws IOException.
     */
    Integer getTopicConsumerCount() throws IOException;

    /**
     * Returns the ids of all topic consumers.
     *
     * @return The ids of all topic consumers.
     * @throws IOException Throws IOException.
     */
    String[] getTopicConsumerIds() throws IOException;

    /**
     * Returns the number of queue producers for the adapter.
     *
     * @return The number of queue producers for the adapter.
     * @throws IOException Throws IOException.
     */
    Integer getQueueProducerCount() throws IOException;

    /**
     * Returns the number of queue consumers for the adapter.
     *
     * @return The number of queue consumers for the adapter.
     * @throws IOException Throws IOException.
     */
    Integer getQueueConsumerCount() throws IOException;

    /**
     * Returns the ids of all queue consumers.
     *
     * @return The ids of all queue consumers.
     * @throws IOException Throws IOException.
     */
    String[] getQueueConsumerIds() throws IOException;

    /**
     * Unsubscribes the consumer (for either a topic or queue).
     *
     * @param consumerId The id of the consumer to unsubscribe.
     * @throws IOException Throws IOException.
     */
    void removeConsumer(String consumerId) throws IOException;
}
