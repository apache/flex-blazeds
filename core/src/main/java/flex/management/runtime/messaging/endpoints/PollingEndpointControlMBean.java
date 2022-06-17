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
package flex.management.runtime.messaging.endpoints;

import java.io.IOException;

/**
 * Defines the runtime monitoring and management interface for managed polling
 * endpoints.
 */
public interface PollingEndpointControlMBean extends EndpointControlMBean {
    /**
     * Returns the maximum number of server poll response threads that will be
     * waiting for messages to arrive for clients.
     *
     * @return The maximum number of server poll response threads that will be
     * waiting for messages to arrive for clients.
     * @throws IOException Throws IOException.
     */
    Integer getMaxWaitingPollRequests() throws IOException;

    /**
     * Returns the number of request threads that are currently in the wait state
     * (including those on their way into or out of it).
     *
     * @return The number of request threads that are currently in the wait state.
     * @throws IOException Throws IOException.
     */
    Integer getWaitingPollRequestsCount() throws IOException;
}
