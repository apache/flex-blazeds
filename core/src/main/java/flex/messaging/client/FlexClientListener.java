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
 * Interface to be notified when a FlexClient is created or destroyed. Implementations of this interface
 * may add themselves as created listeners statically via <code>FlexClient.addClientCreatedListener()</code>.
 * To listen for FlexClient destruction, the implementation instance must add itself as a listener to
 * a specific FlexClient instance via the <code>addClientDestroyedListener()</code> method.
 */
public interface FlexClientListener {
    /**
     * Notification that a FlexClient was created.
     *
     * @param client The FlexClient that was created.
     */
    void clientCreated(FlexClient client);

    /**
     * Notification that a FlexClient is about to be destroyed.
     *
     * @param client The FlexClient that will be destroyed.
     */
    void clientDestroyed(FlexClient client);
}
