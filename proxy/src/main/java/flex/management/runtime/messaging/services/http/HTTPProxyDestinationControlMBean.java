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
package flex.management.runtime.messaging.services.http;

import java.io.IOException;
import java.util.Date;

import flex.management.runtime.messaging.DestinationControlMBean;

/**
 * Defines the runtime monitoring and management interface for managed
 * <code>HTTPProxyDestination</code>s.
 */
public interface HTTPProxyDestinationControlMBean extends
        DestinationControlMBean {
    /**
     * Returns the number of SOAP invocations the HTTP proxy service has processed.
     *
     * @return The number of SOAP invocations the HTTP proxy service has processed.
     * @throws IOException Throws IOException.
     */
    Integer getInvokeSOAPCount() throws IOException;

    /**
     * Resets the count of SOAP invocations.
     *
     * @throws IOException Throws IOException.
     */
    void resetInvokeSOAPCount() throws IOException;

    /**
     * Returns the timestamp of the most recent SOAP invocation processed by the
     * HTTP proxy service.
     *
     * @return The timestamp for the most recent SOAP invocation.
     * @throws IOException Throws IOException.
     */
    Date getLastInvokeSOAPTimestamp() throws IOException;

    /**
     * Returns the number of SOAP invocations per minute.
     *
     * @return The number of SOAP invocations per minute.
     * @throws IOException Throws IOException.
     */
    Double getInvokeSOAPFrequency() throws IOException;

    /**
     * Returns the number of HTTP invocations the HTTP proxy service has processed.
     *
     * @return The number of HTTP invocations the HTTP proxy service has processed.
     * @throws IOException Throws IOException.
     */
    Integer getInvokeHTTPCount() throws IOException;

    /**
     * Resets the count of HTTP invocations.
     *
     * @throws IOException Throws IOException.
     */
    void resetInvokeHTTPCount() throws IOException;

    /**
     * Returns the timestamp of the most recent HTTP invocation processed by the
     * HTTP proxy service.
     *
     * @return The timestamp for the most recent HTTP invocation.
     * @throws IOException Throws IOException.
     */
    Date getLastInvokeHTTPTimestamp() throws IOException;

    /**
     * Returns the number of HTTP invocations per minute.
     *
     * @return The number of HTTP invocations per minute.
     * @throws IOException Throws IOException.
     */
    Double getInvokeHTTPFrequency() throws IOException;
}
