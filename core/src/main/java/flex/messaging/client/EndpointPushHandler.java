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

import flex.messaging.MessageClient;
import flex.messaging.messages.Message;

/**
 * Defines the interface for a handler that may be registered by an endpoint with a <tt>FlexClient</tt> in order
 * to push messages to a connected client.
 */
public interface EndpointPushHandler {
    /**
     * Invoked to shut down the handler.
     * It may be invoked by the endpoint when the underlying connection it manages to the client closes,
     * or by the <tt>FlexClient</tt> if it is invalidated.
     * The implementation of this method should release any resources, and should not attempt to notify the
     * client of an explicit disconnect.
     *
     * @see #close(boolean)
     */
    void close();

    /**
     * Invoked to shut down the handler.
     * It may be invoked by the endpoint when the underlying connection it manages to the client closes,
     * or by the <tt>FlexClient</tt> if it is invalidated.
     * The implementation of this method should release any resources, and may attempt to notify the client
     * Channel that it has been disconnected in order to suppress automatic reconnect behavior.
     *
     * @param disconnectChannel True to attempt to notify the client of an explicit disconnect in order to
     *                          suppress automatic reconnect behavior.
     */
    void close(boolean disconnectChannel);

    /**
     * Invoked by the <tt>FlexClient</tt> when it has messages to push to
     * the client.
     *
     * @param messagesToPush The list of messages to push.
     */
    void pushMessages(List<Message> messagesToPush);

    /**
     * Invoked to notify the handler that the <tt>MessageClient</tt> subscription is using this handler.
     * If subscriptions should be invalidated if the handler is closed, it should retain references to
     * all registered <tt>MessageClient</tt> instances and invalidate them when it closes.
     *
     * @param messageClient The <tt>MessageClient</tt> subscription using this handler.
     */
    void registerMessageClient(MessageClient messageClient);

    /**
     * Invoked to notify the handler that a <tt>MessageClient</tt> subscription that was using it has
     * been invalidated.
     * If the handler is tracking the set of <tt>MessageClient</tt> instances that are using it, the handler should
     * remove the instance from its set.
     *
     * @param messageClient The <tt>MessageClient</tt> subscription no longer using this handler.
     */
    void unregisterMessageClient(MessageClient messageClient);
}
