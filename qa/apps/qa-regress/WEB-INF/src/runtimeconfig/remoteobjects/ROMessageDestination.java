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

package runtimeconfig.remoteobjects;

import flex.messaging.MessageBroker;
import flex.messaging.MessageDestination;
import flex.messaging.config.NetworkSettings;
import flex.messaging.config.ServerSettings;
import flex.messaging.config.ThrottleSettings;
import flex.messaging.services.MessageService;
import flex.messaging.FlexContext;

/*
 * The purpose of this remote object is to allow a client to remove a destination and recreate it.
 * The original destination 
 * The roMessageDestinationTest invokes this object passing the destination id.
 */
public class ROMessageDestination
{    
    private MessageService service;
    
    public ROMessageDestination()
    {
        MessageBroker broker = FlexContext.getMessageBroker();
        //Get the service
        service = (MessageService) broker.getService("message-service");        
    }

    // Remove the destination and check that its ThrottleManager and SubscriptionManager are removed (use ds-console app for this)
    public void removeDestination(String id)
    {
        //Remove the destination 
        service.removeDestination(id);
    }   
     
    public void createDestination(String id)
    {
        MessageDestination msgDest = (MessageDestination)service.createDestination(id);
        
        // <network>
        NetworkSettings ns = new NetworkSettings();
        ns.setSubscriptionTimeoutMinutes(30);
        ns.setSharedBackend(true);
        ns.setClusterId(null);
        ThrottleSettings ts = new ThrottleSettings();
        ts.setInboundPolicy(ThrottleSettings.Policy.ERROR);
        ts.setIncomingClientFrequency(0);
        ts.setOutboundPolicy(ThrottleSettings.Policy.NONE);
        ts.setOutgoingClientFrequency(0);
        ns.setThrottleSettings(ts);   
        msgDest.setNetworkSettings(ns);
        
        // <server>
        ServerSettings ss = new ServerSettings();
        ss.setMessageTTL(100);
        ss.setDurable(false);
        ss.setAllowSubtopics(true);
        msgDest.setServerSettings(ss);
        
        // <channels>
        msgDest.addChannel("qa-http-polling");
        
        msgDest.start();  
    }
}


