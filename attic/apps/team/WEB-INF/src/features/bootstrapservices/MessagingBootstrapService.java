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

package features.bootstrapservices;

import java.util.Hashtable;

import flex.messaging.MessageDestination;
import flex.messaging.config.ConfigMap;
import flex.messaging.config.NetworkSettings;
import flex.messaging.config.ServerSettings;
import flex.messaging.config.ThrottleSettings;
import flex.messaging.config.ThrottleSettings.Policy;
import flex.messaging.services.AbstractBootstrapService;
import flex.messaging.services.Service;
import flex.messaging.services.messaging.adapters.JMSAdapter;
import flex.messaging.services.messaging.adapters.JMSSettings;

/**
 * This BootstrapService is used to dynamicaly create a Messaging Service along 
 * with its Messaging Destinations without the need for any configuration files.
 */
public class MessagingBootstrapService extends AbstractBootstrapService
{
    
    /**
     * Called by the <code>MessageBroker</code> after all of the server 
     * components are created but right before they are started. This is 
     * usually the place to create dynamic components.
     * 
     * @param id Id of the <code>AbstractBootstrapService</code>.
     * @param properties Properties for the <code>AbstractBootstrapService</code>. 
     */
    public void initialize(String id, ConfigMap properties)
    {
        Service messagingService = createService();
        createDestination1(messagingService);
        createDestination2(messagingService);
    }

    /**
     * Called by the <code>MessageBroker</code> as server starts. Useful for
     * custom code that needs to run after all the components are initialized
     * and the server is starting up. 
     */    
    public void start()
    {
        // No-op.
    }

    /**
     * Called by the <code>MessageBroker</code> as server stops. Useful for 
     * custom code that needs to run as the server is shutting down.
     */
    public void stop()
    {
        // No-op.
    }

    /*
    <?xml version="1.0" encoding="UTF-8"?>
    <service id="message-service" class="flex.messaging.services.MessageService">

    <!-- Example messaging-config.xml -->

    <adapters>
        <!--
           id: a unique id specifying the adapter
           class: the Flex Enterprise class which implements the adapter
             possible values: flex.messaging.services.messaging.adapters.ActionScriptAdapter
                              flex.messaging.services.messaging.adapters.JMSAdapter
                              coldfusion.flex.CFEventGatewayAdapter
           default: an optional attribute identifying the adapter to use when none is specified
        -->
        <adapter-definition id="actionscript" class="flex.messaging.services.messaging.adapters.ActionScriptAdapter" default="true" />
        <adapter-definition id="jms" class="flex.messaging.services.messaging.adapters.JMSAdapter"/>
     </adapters>
     */
    private Service createService()
    {
        String serviceId = "messaging-service";
        String serviceClass = "flex.messaging.services.MessageService";
        Service messageService = broker.createService(serviceId, serviceClass);

        messageService.registerAdapter("actionscript", "flex.messaging.services.messaging.adapters.ActionScriptAdapter");
        messageService.registerAdapter("jms", "flex.messaging.services.messaging.adapters.JMSAdapter");
        messageService.setDefaultAdapter("actionscript");

        return messageService;
    }

    /*
    <!-- Example ActionScriptAdapter destination -->
    <destination id="MyTopic">
        <properties>
            <network>
                <!--
                   Idle time in minutes for a subscriber to receive no messages
                   that triggers it to be automatically unsubscribed.
                   0 means don't force subscribers to unsubscribe automatically.
                   Default value is 0.
                -->
                <subscription-timeout-minutes>0</subscription-timeout-minutes>

                <!--
                   Throttling can be set up destination-wide as well as per-client. 
                   The inbound policy may be NONE, ERROR or IGNORE and the outbound policy may be NONE and IGNORE.
                   All throttle frequency values are considered the maximum allowed messages per second.
                   A frequency of 0 disables throttling altogether.
                -->
                <throttle-inbound policy="ERROR" max-frequency="0"/>
                <throttle-outbound policy="IGNORE" max-frequency="0"/>
            </network>

            <server>
                <!-- Max number of messages to maintain in memory cache -->
                <max-cache-size>1000</max-cache-size>

                <!-- message-time-to-live of 0 means live forever -->
                <message-time-to-live>0</message-time-to-live>

                <!-- 
                   The subtopic feature lets you divide the messages that a Producer component sends to a destination 
                   into specific categories at the destination.  You can configure a Consumer component that subscribes to 
                   the destination to receive only messages sent to a specific subtopic or set of subtopics.  You use 
                   wildcard characters (*) to send or receive messages from more than one subtopic.  The subtopic-separator 
                   element is optional; the default value is period
                -->
                <allow-subtopics>true</allow-subtopics>
                <subtopic-separator>.</subtopic-separator>

                <!-- 
                    Used to choose the algorithm for routing messages in a cluster.
                    When set to server-to-server (the default), subscriptions are 
                    broadcast through the cluster to ensure each server knows which 
                    destinations, subtopics, and selector expressions define subscriptions
                    for clients connected to other servers.  When a data message
                    arrives, it is then only sent to servers who have clients interested
                    in that message.  The other value for this setting "broadcast"
                    will simply broadcast all data messages to all servers.  In this
                    mode, subscribe/unsubscribe messages are not sent across the cluster
                -->
                <cluster-message-routing>server-to-server</cluster-message-routing>
            </server>
        </properties>

        <channels>
            <!--
               Set the ref id of the default channels to use as transport for this service.
               The channel is defined elsewhere using the channel-definition tag.
            -->
            <channel ref="my-polling-amf"/>
        </channels>
    </destination>
    */
    private void createDestination1(Service service)
    {
        String destinationId = "MyTopic";
        MessageDestination destination = (MessageDestination)service.createDestination(destinationId);
        
        NetworkSettings ns = new NetworkSettings();
        ns.setSubscriptionTimeoutMinutes(0);
        ThrottleSettings ts = new ThrottleSettings();
        ts.setInboundPolicy(Policy.ERROR);
        ts.setIncomingDestinationFrequency(0);
        ts.setOutboundPolicy(Policy.IGNORE);
        ts.setOutgoingDestinationFrequency(0);
        ns.setThrottleSettings(ts);
        destination.setNetworkSettings(ns);
        
        ServerSettings ss = new ServerSettings();
        ss.setMessageTTL(0);
        ss.setBroadcastRoutingMode("server-to-server");
        destination.setServerSettings(ss);

        destination.addChannel("my-polling-amf");
    }

    /*
    <!-- Example JMSAdapter destination -->
    <destination id="MyJMSTopic">
        <properties>
            <server>
                <!-- Optional. Default is false. This option is currently only used by JMS
                     adapter when the destination-type is Topic. In that case, durable JMS
                     consumers will be used by the JMS adapter. Note that this does not 
                     guarantee durability between Flex clients and JMS adapter but rather
                     between JMS adapter and JMS server.
                <durable>false</durable>
            </server>

            <!-- For specifics on JMS, please reference the Java Message Service specification or your J2EE server documentation -->
            <jms>
                <!--
                   This determines whether the adapter is performing topic (pub/sub) or queue (point-to-point) messaging.
                   This element is optional and defaults to Topic.

                <destination-type>Topic</destination-type>
                -->

                <!--
                   The name of the destination in JMS
                   This element is optional and defaults to the destination id

                <destination-name>FlexTopic</destination-name>
                -->

                <!--
                   The javax.jms.Message type which the adapter should use for this destination.
                   Supported types: javax.jms.TextMessage, javax.jms.ObjectMessage
                -->
                <message-type>javax.jms.TextMessage</message-type>

                <!-- The name of the JMS connection factory in JNDI -->
                <connection-factory>jms/flex/TopicConnectionFactory</connection-factory>

                <!-- The name of the destination in JNDI -->
                <destination-jndi-name>jms/topic/flex/simpletopic</destination-jndi-name>

                <!-- The JMS DeliveryMode for producers -->
                <delivery-mode>NON_PERSISTENT</delivery-mode>

                <!-- The JMS priority for messages sent by Flash producers -->
                <message-priority>DEFAULT_PRIORITY</message-priority>

                <!--
                   The message acknowledgement mode for the JMS adapter
                   None of these modes require any action on the part of the Flex messaging client.
                   Supported modes:
                     AUTO_ACKNOWLEDGE - the JMS provider client runtime automatically acknowledges the messages
                     DUPS_OK_ACKNOWLEDGE - auto-acknowledgement of the messages is not required
                     CLIENT_ACKNOWLEDGE - the JMS adapter should acknowledge that the message was received
                -->
                <acknowledge-mode>AUTO_ACKNOWLEDGE</acknowledge-mode>

                <!-- The JMS session transaction mode -->
                <transacted-sessions>false</transacted-sessions>
                
                <!--
                    The maximum number of producer proxies that this destination
                    should use when communicating with the JMS Server. The default
                    is 1 which implies all clients using this destinatin will
                    share the same connection to the JMS server.
                -->
                <max-producers>1</max-producers>
                
                <!-- (Optional) JNDI environment. Use when using JMS on a remote JNDI server. 
                Used to specify the JNDI environment to access an external JMS provider. 
                -->
                <initial-context-environment>
                    <property>
                        <name>Context.SECURITY_PRINCIPAL</name>
                        <value>anonymous</value>
                    </property>
                    <property>
                        <name>Context.SECURITY_CREDENTIALS</name>
                        <value>anonymous</value>
                    </property>
                    <property>
                        <name>Context.PROVIDER_URL</name>
                        <value>http://{server.name}:1856</value>
                    </property>
                    <property>
                        <name>Context.INITIAL_CONTEXT_FACTORY</name>
                        <value>fiorano.jms.runtime.naming.FioranoInitialContextFactory</value>
                    </property>
                </initial-context-environment>
            </jms>
        </properties>

        <channels>
            <channel ref="my-polling-amf"/>
        </channels>

        <security>
            <security-constraint ref="sample-users"/>
        </security>

        <adapter ref="jms"/>
    </destination>
     */
    private void createDestination2(Service service)
    {
        String destinationId = "MyJMSTopic";
        MessageDestination destination = (MessageDestination)service.createDestination(destinationId);

        ServerSettings ss = new ServerSettings();
        ss.setDurable(false);
        destination.setServerSettings(ss);

        String adapterId = "jms";
        JMSAdapter adapter = (JMSAdapter)destination.createAdapter(adapterId);

        // JMS settings are set at the adapter level
        JMSSettings jms = new JMSSettings();
        jms.setDestinationType("Topic");
        jms.setMessageType("javax.jms.TextMessage");
        jms.setConnectionFactory("jms/flex/TopicConnectionFactory");
        jms.setDestinationJNDIName("jms/topic/flex/simpletopic");
        jms.setDeliveryMode("NON_PERSISTENT");
        //jms.setMessagePriority(javax.jms.Message.DEFAULT_PRIORITY);
        jms.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
        jms.setMaxProducers(1);
        Hashtable envProps = new Hashtable();
        envProps.put("Context.SECURITY_PRINCIPAL", "anonymous");
        envProps.put("Context.SECURITY_CREDENTIALS", "anonymous");
        envProps.put("Context.PROVIDER_URL", "http://{server.name}:1856");
        envProps.put("Context.INITIAL_CONTEXT_FACTORY", "fiorano.jms.runtime.naming.FioranoInitialContextFactory");
        jms.setInitialContextEnvironment(envProps);
        adapter.setJMSSettings(jms);

        destination.setSecurityConstraint("sample-users");

        destination.addChannel("my-polling-amf");
    }
}
