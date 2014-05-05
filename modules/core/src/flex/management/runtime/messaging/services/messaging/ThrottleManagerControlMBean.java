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

import flex.management.BaseControlMBean;

import java.io.IOException;
import java.util.Date;

/**
 * Defines the runtime monitoring and management interface for
 * <code>ThrottleManager</code>s.
 *
 * @author shodgson
 */
public interface ThrottleManagerControlMBean extends BaseControlMBean
{
    /**
     * Returns the number of incoming client messages that have been
     * throttled.
     *
     * @return The number of incoming client messages that have been
     * throttled.
     * @throws IOException Throws IOException.
     */
    Integer getClientIncomingMessageThrottleCount() throws IOException;

    /**
     * Resets the number of throttled incoming client messages to 0.
     *
     * @throws IOException Throws IOException.
     */
    void resetClientIncomingMessageThrottleCount() throws IOException;

    /**
     * Returns the timestamp when an incoming client message was
     * most recently throttled.
     *
     * @return The timestamp when an incoming client message was
     * most recently throttled.
     * @throws IOException Throws IOException.
     */
    Date getLastClientIncomingMessageThrottleTimestamp() throws IOException;

    /**
     * Returns the number of incoming client messages that have been
     * throttled per minute.
     *
     * @return The number of incoming client messages that have been
     * throttled per minute.
     * @throws IOException Throws IOException.
     */
    Double getClientIncomingMessageThrottleFrequency() throws IOException;

    /**
     * Returns the number of outgoing client messages that have been
     * throttled.
     *
     * @return The number of outgoing client messages that have been
     * throttled.
     * @throws IOException Throws IOException.
     */
    Integer getClientOutgoingMessageThrottleCount() throws IOException;

    /**
     * Resets the number of throttled outgoing client messages to 0.
     *
     * @throws IOException Throws IOException.
     */
    void resetClientOutgoingMessageThrottleCount() throws IOException;

    /**
     * Returns the timestamp when an outgoing client message was most
     * recently throttled.
     *
     * @return The timestamp when an outgoing client message was most
     * recently throttled.
     * @throws IOException Throws IOException.
     */
    Date getLastClientOutgoingMessageThrottleTimestamp() throws IOException;

    /**
     * Returns the number of outgoing client messages that have been
     * throttled per minute.
     *
     * @return The number of outgoing client messages that have been
     * throttled per minute.
     * @throws IOException Throws IOException.
     */
    Double getClientOutgoingMessageThrottleFrequency() throws IOException;

    /**
     * Returns the number of incoming destination messages that have
     * been throttled.
     *
     * @return The number of incoming destination messages that have
     * been throttled.
     * @throws IOException Throws IOException.
     */
    Integer getDestinationIncomingMessageThrottleCount() throws IOException;

    /**
     * Resets the number of throttled incoming destination messages to 0.
     *
     * @throws IOException Throws IOException.
     */
    void resetDestinationIncomingMessageThrottleCount() throws IOException;

    /**
     * Returns the timestamp when an incoming destination message was
     * most recently throttled.
     *
     * @return The timestamp when an incoming destination message was
     * most recently throttled.
     * @throws IOException Throws IOException.
     */
    Date getLastDestinationIncomingMessageThrottleTimestamp() throws IOException;

    /**
     * Returns the number of incoming destination messages that have
     * been throttled per minute.
     *
     * @return The number of incoming destination messages that have
     * been throttled per minute.
     * @throws IOException Throws IOException.
     */
    Double getDestinationIncomingMessageThrottleFrequency() throws IOException;

    /**
     * Returns the number of outgoing destination messages that have
     * been throttled.
     *
     * @return The number of outgoing destination messages that have
     * been throttled.
     * @throws IOException Throws IOException.
     */
    Integer getDestinationOutgoingMessageThrottleCount() throws IOException;

    /**
     * Resets the number of throttled outgoing destination messages to 0.
     *
     * @throws IOException Throws IOException.
     */
    void resetDestinationOutgoingMessageThrottleCount() throws IOException;

    /**
     * Returns the timestamp when an outgoing destination message was
     * most recently throttled.
     *
     * @return The timestamp when an outgoing destination message was
     * most recently throttled.
     * @throws IOException Throws IOException.
     */
    Date getLastDestinationOutgoingMessageThrottleTimestamp() throws IOException;

    /**
     * Returns the number of outgoing destination messages that have been
     * throttled per minute.
     *
     * @return The number of outgoing destination messages that have been
     * throttled per minute.
     * @throws IOException Throws IOException.
     */
    Double getDestinationOutgoingMessageThrottleFrequency() throws IOException;
}
