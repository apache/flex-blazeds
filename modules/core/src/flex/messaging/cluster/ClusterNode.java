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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @exclude
 * ClusterNode is an encapsulation for pairing a physical host and a logical
 * software group, which is in effect a mapping between a physical address used
 * by the cluster infrastructure and a service destination used by the message
 * infrastructure.
 *
 * This class is specific to the <code>JGroupsCluster</code> implementation.
 */
public class ClusterNode
{
    /**
     * The name of the host for this cluster node.
     */
    private final String host;

    /**
     * Mapping between clustered destinations and the
     * clustered endpoint.
     * key = destination key (String)
     * value = Map of channel-id to endpoint-url mappings.
     */
    private final Map<String,Map<String,String>> destKeyToChannelMap;

    /**
     * Constructor.
     */
    ClusterNode(String host)
    {
        this.host = host;
        destKeyToChannelMap = new HashMap<String,Map<String,String>>();
    }

    /**
     * Returns the name of the host for this cluster node.
     *
     * @return The name of the host.
     */
    String getHost()
    {
        return host;
    }

    /**
     * Returns a map of clustered destination to clustered
     * endpoint mappings.
     *
     * @return Map of clustered destination to clustered
    *  endpoint mappings.
     */
    Map<String,Map<String,String>> getDestKeyToChannelMap()
    {
        return destKeyToChannelMap;
    }

    /**
     * Returns a map of clustered endpoints for the specified
     * clustered destination. If there is not currently a
     * map for the destination, an empty mapping is created
     * and returned.
     *
     * The endpoint map is indexed by channel id.
     * The endpoint map contains endpoint urls.
     *
     * @param serviceType The service type of the clustered destination.
     * @param destName The destination name of the clustered destination.
     * @return Map of clustered endpoints.
     */
    Map<String,String> getEndpoints(String serviceType, String destName)
    {
        String destKey = serviceType + ":" + destName;
        synchronized (destKeyToChannelMap)
        {
            Map<String,String> channelEndpoints = destKeyToChannelMap.get(destKey);
            if (channelEndpoints == null)
            {
                channelEndpoints = new HashMap<String,String>();
                destKeyToChannelMap.put(destKey, channelEndpoints);
            }
            return channelEndpoints;
        }
    }

    /**
     * This method adds an endpoint to the list of endpoints for the clustered
     * destination, identified by service type and destination name.
     *
     * @param serviceType The service type of the clustered destination.
     * @param destName The destination name of the clustered destination.
     * @param channelId The channel id to be added to the channel endpoint mapping.
     * @param endpointUrl The endpoint url to be added to the endpoint url mapping.
     */
    void addEndpoint(String serviceType, String destName, String channelId, String endpointUrl)
    {
        synchronized (destKeyToChannelMap)
        {
            Map<String,String> channelEndpoints = getEndpoints(serviceType, destName);
            channelEndpoints.put(channelId, endpointUrl);
        }
    }

    /**
     * Returns whether the endpoint, specified by channel id and endpoint url,
     * is included in the list of endpoints in the clustered destination.
     *
     * @param serviceType The service type of the clustered destination.
     * @param destName The destination name of the clustered destination.
     * @param channelId The channel id to find in the list of endpoints.
     * @param endpointUrl The endpoint url to find in the list of endpoints.
     * @return Whether the endpoint is included in the list for the clustered destination.
     */
    boolean containsEndpoint(String serviceType, String destName, String channelId, String endpointUrl)
    {
        Map<String,String> channelEndpoints = getEndpoints(serviceType, destName);
        return channelEndpoints.containsKey(channelId) && channelEndpoints.get(channelId).equals(endpointUrl);
    }

    /**
     * Returns a description of the clustered node including details
     * on the mapping between the clustered destinations on this node
     * and their endpoint mappings.
     *
     * @return Description of the clustered node.
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer("ClusterNode[");
        synchronized (destKeyToChannelMap)
        {
            for (Map.Entry<String,Map<String,String>> entry : destKeyToChannelMap.entrySet())
            {
                sb.append(" channels for ");
                sb.append(entry.getKey());
                sb.append('(');
                for (Iterator<Map.Entry<String,String>> iter = entry.getValue().entrySet().iterator(); iter.hasNext();)
                {
                    Map.Entry<String,String> channelMapEntry = iter.next();
                    sb.append(channelMapEntry.getKey());
                    sb.append('=');
                    sb.append(channelMapEntry.getValue());
                    if (iter.hasNext())
                        sb.append(", ");
                }
                sb.append(')');
            }
        }
        sb.append(" ]");
        return sb.toString();
    }
}
