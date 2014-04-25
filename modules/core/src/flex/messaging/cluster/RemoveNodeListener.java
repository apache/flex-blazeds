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


/**
 * @exclude
 * Called when a node leaves the cluster.  Note that for JGroups at least, this
 * callback should not execute any "long running" operations.  This is indirectly
 * called from the MembershipListener interface in JGroups.
 */
public interface RemoveNodeListener
{
    /**
     * Callback that the clustering subsystem uses to notify that a
     * node has been removed from the cluster.
     *
     * @address The node that was removed from the cluster.
     */
    void removeClusterNode(Object address);
}
