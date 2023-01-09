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
package flex.messaging.client;

/**
 * Extends <tt>FlushResult</tt> and adds additional properties for controlling
 * client polling behavior.
 */
public class PollFlushResult extends FlushResult {
    //--------------------------------------------------------------------------
    //
    // Properties
    //
    //--------------------------------------------------------------------------

    //----------------------------------
    //  avoidBusyPolling
    //----------------------------------

    private boolean avoidBusyPolling;

    /**
     * Indicates whether the handling of this result should attempt to avoid
     * potential busy-polling cycles.
     * This will be set to <code>true</code> in the case of two clients that are both
     * long-polling the server over the same session.
     *
     * @return <code>true</code> if the handling of this result should attempt to avoid potential
     * busy-polling cycles.
     */
    public boolean isAvoidBusyPolling() {
        return avoidBusyPolling;
    }

    /**
     * Set to <code>true</code> to signal that handling for this result should attempt to avoid
     * potential busy-polling cycles.
     *
     * @param value <code>true</code> to signal that handling for this result should attempt to
     *              avoid potential busy-polling cycles.
     */
    public void setAvoidBusyPolling(boolean value) {
        avoidBusyPolling = value;
    }

    //----------------------------------
    //  clientProcessingSuppressed
    //----------------------------------

    private boolean clientProcessingSuppressed;

    /**
     * Indicates whether client processing of this result should be
     * suppressed.
     * This should be <code>true</code> for results generated for poll requests
     * that arrive while a long-poll request from the same client is being serviced
     * to avoid a busy polling cycle.
     *
     * @return <code>true</code> if client processing of this result should be suppressed;
     * otherwise <code>false</code>.
     */
    public boolean isClientProcessingSuppressed() {
        return clientProcessingSuppressed;
    }

    /**
     * Set to <code>true</code> to suppress client processing of this result.
     * Default is <code>false</code>.
     * This should be set to <code>true</code> for results generated for poll requests
     * that arrive while a long-poll request from the same client is being serviced
     * to avoid a busy polling cycle.
     *
     * @param value <code>true</code> to suppress client processing of the result.
     */
    public void setClientProcessingSuppressed(boolean value) {
        clientProcessingSuppressed = value;
    }
}
