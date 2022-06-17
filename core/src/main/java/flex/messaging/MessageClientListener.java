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
package flex.messaging;

/**
 * Interface to be notified when a MessageClient is created or destroyed. Implementations of this interface
 * may add themselves as listeners statically via <code>MessageClient.addMessageClientCreatedListener()</code>.
 * To listen for MessageClient destruction, the implementation class instance must add itself as a listener to
 * a specific MessageClient instance via the <code>addMessageClientDestroyedListener()</code> method.
 */
public interface MessageClientListener {
    /**
     * Notification that a MessageClient was created.
     *
     * @param messageClient The MessageClient that was created.
     */
    void messageClientCreated(MessageClient messageClient);

    /**
     * Notification that a MessageClient is about to be destroyed.
     *
     * @param messageClient The MessageClient that will be destroyed.
     */
    void messageClientDestroyed(MessageClient messageClient);
}
