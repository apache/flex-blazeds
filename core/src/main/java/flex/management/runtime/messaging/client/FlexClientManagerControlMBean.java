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
package flex.management.runtime.messaging.client;

import java.io.IOException;

import flex.management.BaseControlMBean;

/**
 * Defines the runtime monitoring and management interface for managed flex client managers.
 */
public interface FlexClientManagerControlMBean extends BaseControlMBean {
    /**
     * Returns ids of managed clients.
     *
     * @return An array of client ids.
     * @throws IOException Throws IOException.
     */
    String[] getClientIds() throws IOException;

    /**
     * Returns the number of subscriptions for the client with the clientId.
     *
     * @param clientId The client id.
     * @return The number of subscriptions for the client with the cliendId
     * @throws IOException Throws IOException.
     */
    Integer getClientSubscriptionCount(String clientId) throws IOException;

    /**
     * Returns the number of sessiosn for the client with the clientId.
     *
     * @param clientId The client id.
     * @return The number of sessions for the client with the cliendId
     * @throws IOException Throws IOException.
     */
    Integer getClientSessionCount(String clientId) throws IOException;

    /**
     * Returns the last use by the client with the clientId.
     *
     * @param clientId The client id.
     * @return The last use by the client with the clientId
     * @throws IOException Throws IOException.
     */
    Long getClientLastUse(String clientId) throws IOException;

    /**
     * Returns the number of clients.
     *
     * @return The number of clients.
     * @throws IOException Throws IOException.
     */
    Integer getFlexClientCount() throws IOException;
}
