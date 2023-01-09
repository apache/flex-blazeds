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
package flex.management.runtime.messaging.services.messaging;

import java.io.IOException;

import flex.management.BaseControlMBean;

/**
 * Defines the runtime monitoring and management interface for
 * <code>SubscriptionManager</code>s.
 */
public interface SubscriptionManagerControlMBean extends BaseControlMBean {
    /**
     * Returns the count of active subscribers.
     *
     * @return The count of active subscribers.
     * @throws IOException Throws IOException.
     */
    Integer getSubscriberCount() throws IOException;

    /**
     * Returns the ids for all active subscribers.
     *
     * @return The ids for all active subscribers.
     * @throws IOException Throws IOException.
     */
    String[] getSubscriberIds() throws IOException;

    /**
     * Unsubscribes the target subscriber.
     *
     * @param subscriberId The id for the subscriber to unsubscribe.
     * @throws IOException Throws IOException.
     */
    void removeSubscriber(String subscriberId) throws IOException;

    /**
     * Unsubscribes all active subscribers.
     *
     * @throws IOException Throws IOException.
     */
    void removeAllSubscribers() throws IOException;
}
