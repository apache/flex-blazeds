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

import java.util.List;

import flex.messaging.messages.Message;

/**
 * Stores the messages that should be written to the network as a result of a flush
 * invocation on a FlexClient's outbound queue.
 */
public class FlushResult {
    //--------------------------------------------------------------------------
    //
    // Constructor
    //
    //--------------------------------------------------------------------------

    /**
     * Constructs a <tt>FlushResult</tt> instance to return from a
     * flush invocation on a FlexClient's outbound queue.
     * This instance stores the list of messages to write over the network to
     * the client as well as an optional wait time in milliseconds for when the
     * next flush should be invoked.
     */
    public FlushResult() {
    }

    //--------------------------------------------------------------------------
    //
    // Properties
    //
    //--------------------------------------------------------------------------

    //----------------------------------
    //  messages
    //----------------------------------

    private List<Message> messages;

    /**
     * Returns the messages to write to the network for this flush invocation.
     * This list may be null, in which case no messages are written.
     *
     * @return The messages to write to the network for this flush invocation.
     */
    public List<Message> getMessages() {
        return messages;
    }

    /**
     * Sets the messages to write to the network for this flush invocation.
     *
     * @param value The messages to write to the network for this flush invocation.
     */
    public void setMessages(List<Message> value) {
        messages = value;
    }

    //----------------------------------
    //  nextFlushWaitTimeMillis
    //----------------------------------

    private int nextFlushWaitTimeMillis = 0;

    /**
     * Returns the wait time in milliseconds for when the next flush invocation should occur.
     * If this value is 0, the default, a delayed flush is not scheduled and the next flush will
     * depend upon the underlying Channel/Endpoint.
     * For client-side polling Channels the next flush invocation will happen when the client sends
     * its next poll request at its regular interval.
     * For client-side Channels that support direct writes to the client a flush invocation is triggered
     * when the next message is added to the outbound queue.
     *
     * @return The wait time in milliseconds before flush is next invoked. A value of 0, the default,
     * indicates that the default flush behavior for the underlying Channel/Endpoint should be
     * used.
     */
    public int getNextFlushWaitTimeMillis() {
        return nextFlushWaitTimeMillis;
    }

    /**
     * Sets the wait time in milliseconds for when the next flush invocation should occur.
     * If this value is 0, the default, a delayed flush is not scheduled and the next flush will
     * depend upon the underlying Channel/Endpoint.
     * For client-side polling Channels the next flush invocation will happen when the client sends
     * its next poll request at its regular interval.
     * For client-side Channels that support direct writes to the client a flush invocation is triggered
     * when the next message is added to the outbound queue.
     * Negative value assignments are treated as 0.
     *
     * @param value The wait time in milliseconds before flush will be invoked.
     */
    public void setNextFlushWaitTimeMillis(int value) {
        nextFlushWaitTimeMillis = (value < 1) ? 0 : value;
    }
}
