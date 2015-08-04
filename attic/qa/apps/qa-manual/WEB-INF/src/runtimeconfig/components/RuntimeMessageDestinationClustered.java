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

package runtimeconfig.components;

import flex.messaging.MessageDestination;
import flex.messaging.config.ConfigMap;
import flex.messaging.config.NetworkSettings;
import flex.messaging.config.ServerSettings;
import flex.messaging.config.ThrottleSettings;
import flex.messaging.services.AbstractBootstrapService;
import flex.messaging.services.MessageService;

/*
 * The purpose of this class is to duplicate an existing clustered messaging destination 
 * using runtime configuration.  
 * This manual test requires a cluster configuration so the default-tcp-cluster is active.
 */
public class RuntimeMessageDestinationClustered extends AbstractBootstrapService
{    
    
    public void initialize(String id, ConfigMap properties)
    {
        //Create destination and add to the Service
        MessageService service = (MessageService) broker.getService("message-service");
        String dest = "MessageDest_clustered";
        createDestination(dest, service);
    }

    /*
     * The following is a destination in the qa-regress application, under messaging-config.xml
     * The method below implements it at runtime, but making it part of a cluster.
     * 
    <destination id="MyTopic">
        <properties>
            <network>
                <!-- idle time in minutes before a subscriber will be unsubscribed -->
                <!-- '0' means don't force subscribers to unsubscribe automatically -->
                <session-timeout>0</session-timeout>
    
                <!-- throttling can be set up destination-wide as well as     -->
                <!-- per-client: the inbound policy may be ERROR or IGNORE    -->
                <!-- and the outbound policy may be ERROR, IGNORE, or REPLACE -->
                <!-- all throttle frequency values are considered the maximum -->
                <!-- allowed messages per second                              -->
                <throttle-inbound max-frequency="0" policy="ERROR"></throttle-inbound>
                <throttle-outbound max-frequency="0" policy="REPLACE"></throttle-outbound>
            </network>
            <server>
                <!-- max number of messages to maintain in memory cache -->
                <max-cache-size>1000</max-cache-size>
    
                <!-- ttl of 0 means live forever -->
                <message-time-to-live>0</message-time-to-live>
    
                <!-- options to make this a durable destination -->
                <durable>false</durable>
    
                <!-- properties for durable file store manager, all optional -->
                <!--
                <file-store-root>/whereever</file-store-root>
                <max-file-size>200K</max-file-size>
                <batch-write-size>10</batch-write-size>
                -->
            </server>
    
        </properties>
    
        <channels>
            <channel ref="qa-rtmp-ac"></channel>
            <channel ref="qa-polling-amf-ac"></channel>
            <channel ref="qa-polling-amf"></channel>
            <channel ref="qa-secure-polling-amf"></channel>
        </channels>
    </destination>
    */
    private MessageDestination createDestination(String id, MessageService messageService)
    {
        MessageDestination msgDest;
        msgDest = (MessageDestination)messageService.createDestination(id);

        // <network>
        NetworkSettings ns = new NetworkSettings();
        ns.setSubscriptionTimeoutMinutes(0);
        ThrottleSettings ts = new ThrottleSettings();
        ts.setInboundPolicy(ThrottleSettings.Policy.ERROR);
        ts.setIncomingClientFrequency(0);
        ts.setOutboundPolicy(ThrottleSettings.Policy.IGNORE);
        ts.setOutgoingClientFrequency(0);
        ns.setThrottleSettings(ts);  
        ns.setClusterId("default-tcp-cluster");
        msgDest.setNetworkSettings(ns);
        
        // <server>
        ServerSettings ss = new ServerSettings();
        ss.setMessageTTL(0);
        ss.setDurable(false);
        msgDest.setServerSettings(ss);
        
        //Use a channel that does not use the {server.name}:{server.port} tokens
        //msgDest.addChannel("qa-rtmp-cluster");        
        msgDest.addChannel("qa-amf-polling-cluster");
        //msgDest.addChannel("qa-http-polling-cluster");
        
        return msgDest;
    }
    
    public void start()
    {
        // No-op
    }
    
    public void stop()
    {
        // No-op
    }
    
}


