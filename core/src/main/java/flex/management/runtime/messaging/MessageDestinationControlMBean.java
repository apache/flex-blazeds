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

import java.io.IOException;
import java.util.Date;

import javax.management.ObjectName;


/**
 * Defines the runtime monitoring and management interface for managed
 * <code>MessageDestination</code>s.
 */
public interface MessageDestinationControlMBean extends DestinationControlMBean {
    /**
     * Returns the <code>ObjectName</code> for the message cache used by the managed
     * destination.
     *
     * @return The <code>ObjectName</code> for the message cache.
     * @throws IOException Throws IOException.
     */
    ObjectName getMessageCache() throws IOException;

    /**
     * Returns the <code>ObjectName</code> for the throttle manager used by the
     * managed destination.
     *
     * @return The <code>ObjectName</code> for the throttle manager.
     * @throws IOException Throws IOException.
     */
    ObjectName getThrottleManager() throws IOException;

    /**
     * Returns the <code>ObjectName</code> for the subscription manager used
     * by the managed destination.
     *
     * @return The <code>ObjectName</code> for the subscription manager.
     * @throws IOException Throws IOException.
     */
    ObjectName getSubscriptionManager() throws IOException;

    /**
     * Returns the number of service message invocations.
     *
     * @return The number of service message invocations.
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
     * Returns the timestamp for the most recent service message
     * invocation.
     *
     * @return The timestamp for the most recent service message invocation.
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
     * Returns the number of service command invocations.
     *
     * @return The number of service command invocations.
     * @throws IOException Throws IOException.
     */
    Integer getServiceCommandCount() throws IOException;

    /**
     * Resets the count of service command invocations.
     *
     * @throws IOException Throws IOException.
     */
    void resetServiceCommandCount() throws IOException;

    /**
     * Returns the timestamp for the most recent service command invocation.
     *
     * @return The timestamp for the most recent service command invocation.
     * @throws IOException Throws IOException.
     */
    Date getLastServiceCommandTimestamp() throws IOException;

    /**
     * Returns the number of service command invocations per minute.
     *
     * @return The number of service command invocations per minute.
     * @throws IOException Throws IOException.
     */
    Double getServiceCommandFrequency() throws IOException;

    /**
     * Returns the number of messages from an adapter that the managed service
     * has processed.
     *
     * @return The number of messages from an adapter that the managed service
     * has processed
     * @throws IOException Throws IOException.
     */
    Integer getServiceMessageFromAdapterCount() throws IOException;

    /**
     * Resets the count of service message from adapter invocations.
     *
     * @throws IOException Throws IOException.
     */
    void resetServiceMessageFromAdapterCount() throws IOException;

    /**
     * Returns the timestamp of the most recent service message from adapter invocation.
     *
     * @return The timestamp of the most recent service message from adapter invocation.
     * @throws IOException Throws IOException.
     */
    Date getLastServiceMessageFromAdapterTimestamp() throws IOException;

    /**
     * Returns the number of service message from adapter invocations per minute.
     *
     * @return The number of service message from adapter invocations per minute.
     * @throws IOException Throws IOException.
     */
    Double getServiceMessageFromAdapterFrequency() throws IOException;
}
