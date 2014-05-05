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
package flex.management.runtime.messaging;

import flex.management.BaseControlMBean;

import java.io.IOException;
import java.util.Date;
import javax.management.ObjectName;

/**
 * Defines the runtime monitoring and management interface for managed <code>MessageBroker</code>s.
 *
 * @author shodgson
 */
public interface MessageBrokerControlMBean extends BaseControlMBean
{
    /**
     * Returns <code>true</code> if the <code>MessageBroker</code> is running.
     *
     * @return <code>true</code> if the <code>MessageBroker</code> is running.
     * @throws IOException Throws IOException.
     */
    Boolean isRunning() throws IOException;

    /**
     * Returns the start timestamp for the <code>MessageBroker</code>.
     *
     * @return The start timestamp for the <code>MessageBroker</code>.
     * @throws IOException Throws IOException.
     */
    Date getStartTimestamp() throws IOException;

    /**
     * Returns the <code>ObjectName</code>s for endpoints that are registered with the
     * managed <code>MessageBroker</code>.
     *
     * @return The <code>ObjectName</code>s for endpoints registered with the managed <code>MessageBroker</code>.
     * @throws IOException Throws IOException.
     */
    ObjectName[] getEndpoints() throws IOException;

    /**
     * Returns the <code>ObjectName</code>s for services that are registered with the
     * managed <code>MessageBroker</code>.
     *
     * @return The <code>ObjectName</code>s for services registered with the managed <code>MessageBroker</code>.
     * @throws IOException Throws IOException.
     */
    ObjectName[] getServices() throws IOException;

    /**
     * Returns Flex session count for the <code>MessageBroker</code>.
     *
     * @return Flex session count for the <code>MessageBroker</code>.
     * @throws IOException Throws IOException.
     */
    Integer getFlexSessionCount() throws IOException;

    /**
     * Returns the maximum concurrent Flex session count for the
     * <code>MessageBroker</code> in the current hour.
     *
     * @return The maximum concurrent Flex session count for the
     * <code>MessageBroker</code> over the course of the last hour.
     * @throws IOException Throws IOException.
     */
    Integer getMaxFlexSessionsInCurrentHour() throws IOException;

    /**
     * Returns the number of Enterprise Connections across all
     * Enterprise Endpoints.
     *
     * @return The number of Enterprise Connections
     * @throws IOException Throws IOException.
     */
    Integer getEnterpriseConnectionCount() throws IOException;

    /**
     * Returns the total number of bytes passing through all AMF endpoints.
     *
     * @return The total number of bytes passing through all AMF endpoints.
     * @throws IOException Throws IOException.
     */
    Long getAMFThroughput() throws IOException;

    /**
     * Returns the total number of bytes passing through all HTTP endpoints.
     *
     * @return The total number of bytes passing through all HTTP endpoints.
     * @throws IOException Throws IOException.
     */
    Long getHTTPThroughput() throws IOException;

    /**
     * Returns the total number of bytes passing through all Enterprise endpoints.
     *
     * @return The total number of bytes passing through all Enterprise endpoints.
     * @throws IOException Throws IOException.
     */
    Long getEnterpriseThroughput() throws IOException;

    /**
     * Returns the total number of bytes passing through all streaming AMF endpoints.
     *
     * @return The total number of bytes passing through all streaming AMF endpoints.
     * @throws IOException Throws IOException.
     */
    Long getStreamingAMFThroughput() throws IOException;

    /**
     * Returns the total number of bytes passing through all streaming HTTP endpoints.
     *
     * @return The total number of bytes passing through all streaming HTTP endpoints.
     * @throws IOException Throws IOException.
     */
    Long getStreamingHTTPThroughput() throws IOException;
}
