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

/*
 * used by the eqa app by using runtime configuration.  Attached is the config code 
 * as it appears in messaging-config.xml
 */
import flex.messaging.MessageDestination;
import flex.messaging.config.ConfigMap;
import flex.messaging.config.NetworkSettings;
import flex.messaging.config.ServerSettings;
import flex.messaging.config.ThrottleSettings;
import flex.messaging.services.AbstractBootstrapService;
import flex.messaging.services.MessageService;
import flex.messaging.services.messaging.adapters.JMSAdapter;
import flex.messaging.services.messaging.adapters.JMSSettings;

/* 
 * The purpose of this class is create a JMS destination without a JNDI destination name.
 * It is used in the qa-manual messaging\simpleJMSMessagingNoConnFactoryTest.
 */
public class RuntimeJMSDestinationNoJNDIDestName extends AbstractBootstrapService
{
    
    public void initialize(String id, ConfigMap properties)
    {
         // Create destination and add to the Service
        MessageService service = (MessageService) getMessageBroker().getService("message-service");
        String dest = "JMSDestNoJNDIDestName_startup";
        createDestination(dest, service);
    }

    /*
     * The following is a destination in the eqa application, under messaging-config.xml
     * The method below implements an equivalent destination at runtime
     * 
     <destination id="MyJMSTopic">

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
                <durable>false</durable>
            </server>

            <jms>
                <!-- whether the adapter is performing topic (pub/sub) or queue (point-to-point) messaging -->
                <!-- optional element, defaults to Topic -->
                <destination-type>Topic</destination-type>

                <!-- the javax.jms.Message type which the adapter should use for this destination -->
                <message-type>javax.jms.TextMessage</message-type>

                <!-- name of the JMS connection factory in JNDI -->
                <connection-factory>jms/flex/TopicConnectionFactory</connection-factory>

                <!-- name of the destination in JNDI -->
                <destination-jndi-name>jms/topic/flex/simpletopic</destination-jndi-name>

                <!-- name of the destination in JMS -->
                <!-- optional element, defaults to the destination id -->
                <destination-name>FlexTopic</destination-name>

                <!-- the JMS DeliveryMode for producers -->
                <delivery-mode>NON_PERSISTENT</delivery-mode>

                <!-- JMS priority for messages sent by Flash producers -->
                <message-priority>DEFAULT_PRIORITY</message-priority>

                <!-- the JMS message acknowledgement mode -->
                <acknowledge-mode>AUTO_ACKNOWLEDGE</acknowledge-mode>

                <!-- the JMS session transaction mode -->
                <transacted-sessions>false</transacted-sessions>

            </jms>
        </properties>

        <channels>
            <channel ref="qa-rtmp-ac"></channel>
            <channel ref="qa-polling-amf"></channel>
        </channels>

        <adapter ref="jms"></adapter>

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
        ts.setIncomingClientFrequency(0);
        ts.setInboundPolicy(ThrottleSettings.Policy.ERROR);
        ts.setOutgoingClientFrequency(0);
        ts.setOutboundPolicy(ThrottleSettings.Policy.IGNORE);
        ns.setThrottleSettings(ts);   
        msgDest.setNetworkSettings(ns);
        
        // <server>
        ServerSettings ss = new ServerSettings();
        ss.setDurable(false);
        msgDest.setServerSettings(ss);
        
        // <channels>
        msgDest.addChannel("qa-polling-amf");
        
        //Properties that appear in the destination above must "really" be set in a JMS adapter
        JMSAdapter adapter = new JMSAdapter();
        adapter.setId("jms");
        // Use JMSSettings object for the <jms> properties above
        JMSSettings js = new JMSSettings();
        js.setDestinationType("Topic");
        js.setMessageType("javax.jms.TextMessage");
        js.setConnectionFactory("java:comp/env/jms/flex/TopicConnectionFactory");
        //js.setDestinationJNDIName("java:comp/env/jms/topic/flex/simpletopic");
        js.setDeliveryMode("NON_PERSISTENT");
        js.setMessagePriority(javax.jms.Message.DEFAULT_PRIORITY);
        js.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
        adapter.setJMSSettings(js);
        adapter.getJMSSettings();
        adapter.setDestination(msgDest);
        
        return msgDest;
    }
    
    // Create a destination after server startup.
    public void start()
    {
        MessageService service = (MessageService) getMessageBroker().getService("message-service");
        String id = "JMSDestNoJNDIDestName_runtime";
        MessageDestination destination = createDestination(id, service);
        destination.start();
    }
    
    // No-op.
    public void stop()
    {
    }
}


