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
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Element;

import flex.messaging.config.ConfigMap;
import flex.messaging.log.LogCategories;

/**
 * Base interface for cluster implementations.
 */
public abstract class Cluster {
    /**
     * Default log category for clustering.
     */
    public static final String LOG_CATEGORY = LogCategories.SERVICE_CLUSTER;

    /**
     * Listeners to be notified when a node is removed from the cluster.
     */
    List removeNodeListeners = Collections.synchronizedList(new ArrayList());

    /**
     * Cluster properties file.
     */
    Element clusterPropertiesFile;

    /**
     * Specifies whether or not this is the default cluster.
     */
    boolean def;

    /**
     * Specifies if this cluster is enabled for URL load balancing.
     */
    boolean urlLoadBalancing;

    /**
     * Because destinations are the constructs which become clustered, clusters
     * are identified by a unique name composed in the format
     * "serviceType:destinationId".
     *
     * @param serviceType     The name of the service for this destination.
     * @param destinationName The original name of the destination.
     * @return The unique name for the clustered destination.
     */
    static String getClusterDestinationKey(String serviceType, String destinationName) {
        StringBuffer sb = new StringBuffer();
        sb.append(serviceType);
        sb.append(':');
        sb.append(destinationName);
        return sb.toString();
    }

    /**
     * Add a listener for remove cluster node notification.
     *
     * @param listener the RemoveNodeListener to add
     */
    public void addRemoveNodeListener(RemoveNodeListener listener) {
        removeNodeListeners.add(listener);
    }

    /**
     * Send notification to remove node listeners that a node has
     * been removed from the cluster.
     *
     * @param address The node that was removed from the cluster.
     */
    protected void sendRemoveNodeListener(Object address) {
        synchronized (removeNodeListeners) {
            for (int i = 0; i < removeNodeListeners.size(); i++)
                ((RemoveNodeListener) removeNodeListeners.get(i)).removeClusterNode(address);
        }
    }

    /**
     * Initializes the Cluster with id and the map of properties. The default
     * implementation is no-op.
     *
     * @param id         The cluster id.
     * @param properties The map of properties.
     */
    public void initialize(String id, ConfigMap properties) {
        // No-op.
    }

    /**
     * Returns the cluster properties file.
     *
     * @return The cluster properties file.
     */
    public Element clusterPropertiesFile() {
        return clusterPropertiesFile;
    }

    /**
     * Sets the cluster properties file.
     *
     * @param value The cluster properties file.
     */
    public void setClusterPropertiesFile(Element value) {
        this.clusterPropertiesFile = value;
    }

    /**
     * Returns true if this is the default cluster for any destination that does not
     * specify a clustered destination.
     *
     * @return Returns true if this is the default cluster.
     */
    public boolean isDefault() {
        return def;
    }

    /**
     * When true, this is the default cluster for any destination that does not
     * specify a clustered destination.
     *
     * @param d true if this is the default cluster
     */
    public void setDefault(boolean d) {
        this.def = d;
    }

    /**
     * When true, this cluster is enabled for URL load balancing.
     *
     * @return true if this cluster enabled for load balancing.
     */
    public boolean getURLLoadBalancing() {
        return urlLoadBalancing;
    }

    /**
     * When true, the cluster is enabled for URL load balancing.
     *
     * @param u the flag to enable the URL load balancing
     */
    public void setURLLoadBalancing(boolean u) {
        urlLoadBalancing = u;
    }

    /**
     * Shutdown the cluster.
     */
    public abstract void destroy();

    /**
     * Retrieve a List of Maps, where each Map contains channel id keys
     * mapped to endpoint URLs for the given service type and destination name.
     * There is exactly one endpoint URL for each
     * channel id. This List represents all of the known endpoint URLs
     * for all of the channels in the Cluster.
     *
     * @param serviceType the service type
     * @param destName    the destination name
     * @return List of maps of channel ids to endpoint URLs for each node in
     * the cluster.
     */
    public abstract List getAllEndpoints(String serviceType, String destName);

    /**
     * Returns a list of all of the nodes of this cluster.
     *
     * @return List a list of member IP addresses in the cluster
     */
    public abstract List getMemberAddresses();

    /**
     * Returns the local cluster node.
     *
     * @return Object the Local Address object
     */
    public abstract Object getLocalAddress();

    /**
     * Broadcast a service-related operation, which usually includes a Message as a method parameter. This method
     * allows a local service to process a Message and then send the Message to the services on all peer nodes
     * so that they may perform the same processing.
     *
     * @param serviceOperation The operation to broadcast.
     * @param params           Parameters for the operation.
     */
    public abstract void broadcastServiceOperation(String serviceOperation, Object[] params);

    /**
     * Send a service-related operation in point-to-point fashion to one and only one member of the cluster.
     * This is similar to the broadcastServiceOperation except that this invocation is sent to the first
     * node among the cluster members that does not have the local node's address.
     *
     * @param serviceOperation The operation to send.
     * @param params           Parameters for the operation.
     * @param targetAddress    the target address of a remote node in the cluster
     */
    public abstract void sendPointToPointServiceOperation(String serviceOperation, Object[] params, Object targetAddress);

    /**
     * Add a local endpoint URL for a local channel. After doing so, broadcast the information to
     * peers so that they will be aware of the URL.
     *
     * @param serviceType  the service type of the endpoint
     * @param destName     the destination name
     * @param channelId    the Channel ID
     * @param endpointUrl  the endpoint URL
     * @param endpointPort the endpoint port
     */
    public abstract void addLocalEndpointForChannel(String serviceType, String destName,
                                                    String channelId, String endpointUrl, int endpointPort);
}
