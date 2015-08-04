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
 * The purpose of this class is to duplicate the static MyTopic messaging destination,
 * It is used to verify that runtime-configured Messaging destinations work.
 */
public class RuntimeUnmanagedMessageDestination extends AbstractBootstrapService
{    
    
    public void initialize(String id, ConfigMap properties)
    {
        //Create destination and add to the Service
        MessageService service = (MessageService) broker.getService("message-service");
        String dest = "MessageDest_Unmanaged_startup";
        createDestination(dest, service);
    }

    /*
     * The following is a destination in the qa-regress application, under messaging-config.xml
     * The method below implements it at runtime.
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
        
        // <server>
        ServerSettings ss = new ServerSettings();
        ss.setMessageTTL(0);
        ss.setDurable(false);
        msgDest.setServerSettings(ss);
      
        // switch managed on / off
        msgDest.setManaged(false);
        
        msgDest.addChannel("qa-polling-amf");
        
        return msgDest;
    }
    
    
    public void start()
    {
        //Create destination and add to the Service
     //   MessageService service = (MessageService) broker.getService("message-service");
      //  String id = "MessageDest_runtime";
      //  MessageDestination dest = createDestination(id, service);
      //  dest.start();
    }

    
    public void stop()
    {
    }
    
}


