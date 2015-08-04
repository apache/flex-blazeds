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
package flex.management.runtime.messaging.services.remoting;

import java.io.IOException;

import flex.management.runtime.messaging.DestinationControlMBean;

/**
 * Defines the runtime monitoring and management interface for managed
 * <code>RemotingDestination</code>s.
 */
public interface RemotingDestinationControlMBean extends
        DestinationControlMBean
{
    /**
     * Returns the count of successful invocations for the destination.
     *
     * @return The number of successful invocations for the destination.
     * @throws IOException Throws IOException.
     */
    Integer getInvocationSuccessCount() throws IOException;

    /**
     * Returns the count of faulted invocations for the destination.
     *
     * @return The number of successful invocations for the destination.
     * @throws IOException Throws IOException.
     */
    Integer getInvocationFaultCount() throws IOException;

    /**
     * Returns the average invocation processing time in milliseconds for the destination.
     *
     * @return The average invocation processing time in milliseconds for the destination.
     * @throws IOException Throws IOException.
     */
    Integer getAverageInvocationProcessingTimeMillis() throws IOException;
}
