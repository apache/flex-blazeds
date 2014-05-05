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

import flex.management.BaseControlMBean;

import java.io.IOException;
import java.util.Date;

/**
 * Defines the runtime monitoring and management interface for managed endpoints.
 *
 * @author shodgson
 */
public interface EndpointControlMBean extends BaseControlMBean
{
    /**
     * Returns <code>true</code> if the <code>Endpoint</code> is running.
     *
     * @return <code>true</code> if the <code>Endpoint</code> is running.
     * @throws IOException Throws IOException.
     */
    Boolean isRunning() throws IOException;

    /**
     * Returns the start timestamp for the <code>Endpoint</code>.
     *
     * @return The start timestamp for the <code>Endpoint</code>.
     * @throws IOException Throws IOException.
     */
    Date getStartTimestamp() throws IOException;

    /**
     * Returns the count of messages decoded by this endpoint and routed to the broker.
     *
     * @return The count of messages decoded by this endpoint and routed to the broker.
     * @throws IOException Throws IOException.
     */
    Integer getServiceMessageCount() throws IOException;

    /**
     * Resets the count of service message invocations.
     *
     * @throws IOException Throws IOException.
     */
    void resetServiceMessageCount() throws IOException;

    /**
     * Returns the timestamp for the most recent message decoded by this endpoint and
     * routed to the broker.
     *
     * @return The timestamp for the most recent message decoded by this endpoint and
     * routed to the broker.
     * @throws IOException Throws IOException.
     */
    Date getLastServiceMessageTimestamp() throws IOException;

    /**
     * Returns the number of service message invocations per minute.
     *
     * @return The number of service message invocations per minute.
     * @throws IOException Throws IOException.
     */
    Double getServiceMessageFrequency() throws IOException;

    /**
     * Returns the URI that corresponds to this endpoint.
     *
     * @return The URI that corresponds to this endpoint.
     * @throws IOException Throws IOException.
     */
    String getURI() throws IOException;

    /**
     * Returns the security constraint that is associated with this endpoint.
     *
     * @return The security constraint that is associated with this endpoint.
     * @throws IOException Throws IOException.
     */
    String getSecurityConstraint() throws IOException;

    /**
     * Returns the total Bytes that have been deserialized by this endpoint
     * during its lifetime.
     *
     * @return total Bytes deserialized.
     * @throws IOException Throws IOException.
     */
    Long getBytesDeserialized() throws IOException;

    /**
     * Returns the total Bytes that have been serialized by this endpoint
     * during its lifetime.
     *
     * @return total Bytes serialized.
     * @throws IOException Throws IOException.
     */
    Long getBytesSerialized() throws IOException;
}
