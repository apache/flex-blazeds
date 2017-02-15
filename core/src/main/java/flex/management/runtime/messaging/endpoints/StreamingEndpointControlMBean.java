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
import java.util.Date;

/**
 * Defines the runtime monitoring and management interface for managed streaming
 * endpoints.
 */
public interface StreamingEndpointControlMBean extends EndpointControlMBean
{
    /**
     * Returns the maximum number of clients that will be allowed to establish
     * a streaming HTTP connection with the endpoint.
     *
     * @return The maximum number of clients that will be allowed to establish
     * a streaming HTTP connection with the endpoint.
     * @throws IOException Throws IOException.
     */
    Integer getMaxStreamingClients() throws IOException;

    /**
     * Returns the count of push invocations.
     *
     * @return The count of push invocations.
     * @throws IOException Throws IOException.
     */
    Integer getPushCount() throws IOException;

    /**
     * Resets the count of push invocations.
     *
     * @throws IOException Throws IOException.
     */
    void resetPushCount() throws IOException;

    /**
     * Returns the timestamp for the most recent push invocation.
     *
     * @return The timestamp for the most recent push invocation.
     * @throws IOException Throws IOException.
     */
    Date getLastPushTimestamp() throws IOException;

    /**
     * Returns the number of push invocations per minute.
     *
     * @return The number of push invocations per minute.
     * @throws IOException Throws IOException.
     */
    Double getPushFrequency() throws IOException;

    /**
     * Returns the the number of clients that are currently in the streaming state.
     *
     * @return The number of clients that are currently in the streaming state.
     * @throws IOException Throws IOException.
     */
    Integer getStreamingClientsCount() throws IOException;
}
