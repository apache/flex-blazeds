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
package flex.messaging.cluster;

import java.util.List;

/**
 * @exclude
 * This interface represents a handler for a message broadcast by a Cluster.
 * Clusters broadcast messages across their physical nodes, and when they
 * receive those messages they locate a BroadcastHandler capable of handling
 * the broadcast.
 */
public interface BroadcastHandler
{
    /**
     * Handle the broadcast message.
     *
     * @param sender sender of the original message
     * @param params any parameters need to handle the message
     */
    void handleBroadcast(Object sender, List<Object> params);

    /**
     * Determine whether this Handler supports a particular operation by name.
     *
     * @return whether or not this handler supports the named operation
     * @param name name of the operation
     */
    boolean isSupportedOperation(String name);
}
