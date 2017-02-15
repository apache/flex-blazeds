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

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.jgroups.Address;
import org.jgroups.MembershipListener;
import org.jgroups.View;

/**
 *
 * Clusters employ this Listener in order to respond to nodes which
 * join and abandon it. This class bridges the low-level protocol layer
 * to the more abstract logical cluster.
 */
class ClusterMembershipListener implements MembershipListener
{
    /**
     * The cluster implementation that owns this listener.
     */
   // TODO: The missing class JGroupsCluster seems to extend Cluster, but is missing from the repository.
//    private JGroupsCluster cluster;

    /**
     * The list of current cluster members as we know it.
     */
    private List<Address> members;

    /**
     * The list of cluster members that are not currently active.
     */
    private List<Address> zombies;

    /**
     * Our implementation of cluster membership listener.
     *
     * @param cluster The logical cluster implementation.
     */
    public ClusterMembershipListener(Cluster cluster)
    {
//        this.cluster = (JGroupsCluster)cluster;
        this.members = new ArrayList<Address>();
        this.zombies = new ArrayList<Address>();
    }

    /**
     * This method is invoked by the cluster infrastructure whenever
     * a member joins or abandons the cluster group.
     *
     * @param membershipView Snapshot of members of the cluster.
     */
    public void viewAccepted(View membershipView)
    {
        synchronized(this)
        {
            Vector<Address> currentMemberList = membershipView.getMembers();
            handleArrivingMembers(currentMemberList);
            handleDepartedMembers(membershipView, currentMemberList);
        }
    }

    /**
     * This method is invoked by the cluster infrastructure whenever
     * a member appears to have left the cluster, but before it has
     * been removed from the active member list. The Cluster treats
     * these addresses as zombies and will not use their channel and
     * endpoint information.
     *
     * @param zombieAddress The address of the suspect node.
     */
    public void suspect(Address zombieAddress)
    {
        synchronized(this)
        {
            zombies.add(zombieAddress);
        }
    }

    /**
     * This method from the core MembershipListener is a no-op for
     * the Flex destination Cluster.
     */
    public void block()
    {
        // No-op.
    }

    /**
     * Allow the Cluster to determine whether a given physical address
     * is a zombie.
     *
     * @param address The node to check.
     * @return True, if the given address is a zombie.
     */
    public boolean isZombie(Address address)
    {
        return zombies.contains(address);
    }

    private void handleDepartedMembers(View membershipView, Vector<Address> currentMemberList)
    {
        for (Address member : members)
        {
            if (!membershipView.containsMember(member))
            {
//                cluster.removeClusterNode(member);
                zombies.remove(member);
            }
        }
        members = currentMemberList;
    }

    private void handleArrivingMembers(Vector<Address> currentMemberList)
    {
        for (Address member : currentMemberList) 
        {
/*            if (!cluster.getLocalAddress().equals(member) && !members.contains(member))
                cluster.addClusterNode(member);*/
        }
    }
}