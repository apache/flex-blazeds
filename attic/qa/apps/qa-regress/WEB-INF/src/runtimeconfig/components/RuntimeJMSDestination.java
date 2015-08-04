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

import flex.messaging.Destination;
import flex.messaging.MessageBroker;
import flex.messaging.MessageDestination;
import flex.messaging.config.ConfigMap;
import flex.messaging.config.NetworkSettings;
import flex.messaging.config.ServerSettings;
import flex.messaging.config.ThrottleSettings;
import flex.messaging.io.ArrayList;
import flex.messaging.log.Log;
import flex.messaging.services.AbstractBootstrapService;
import flex.messaging.services.MessageService;
import flex.messaging.services.RemotingService;
import flex.messaging.services.ServiceAdapter;
import flex.messaging.services.messaging.adapters.JMSAdapter;
import flex.messaging.services.messaging.adapters.JMSSettings;
import flex.messaging.services.remoting.RemotingDestination;

/*
 * The purpose of this class is to duplicate an existing JMS destination
 * using runtime configuration.  
 */
public class RuntimeJMSDestination extends AbstractBootstrapService
{
    private final String dest_runtime = "JMSDest_runtime";
    private final String dest_startup = "JMSDest_startup";
    
    public void initialize(String id, ConfigMap properties)
    {
        if (broker != null)
        {
            MessageService service = (MessageService) broker.getService("message-service");        
            createDestination(dest_startup, service);
        }
    }

    /*
     * The following is a destination in the qa-regress application, under messaging-config.xml
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
        
        // add clusterId and sharedBackend for code coverage
        ns.setClusterId("default-tcp-cluster");
        ns.setSharedBackend(true);
        
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
        
		String jndiAppend = "";
        if (getAppServer() == "tomcat")
        {
        //  System.out.println("***** running on TomCat - appending java:comp/env/ *****)");
            jndiAppend = "java:comp/env/";
        }
  
        Log.getLogger("QE.DEBUG").debug("jndiAppend =" + jndiAppend);
        System.out.println("appServer: " +  getAppServer() );
        if (getAppServer() == "jboss") {
          //  System.out.println("****running on jboss *****)");
          //  js.setConnectionFactory(jndiAppend + "jms/flex/TopicConnectionFactory");
          //  js.setDestinationJNDIName(jndiAppend + "jms/topic/flex/simpletopic");
          // Changing this to match the names in jbossmq-destinations-service.xml, jms-ds.xml (in \default\deploy\jms directory), 
          // and messaging-config.xml on the jboss regression machine
                                                
            js.setConnectionFactory("java:XAConnectionFactory");
            js.setDestinationJNDIName("topic/FlexTopic");
        }
        else {
         //   System.out.println("***** not jboss *****)");  
           js.setConnectionFactory(jndiAppend + "jms/flex/TopicConnectionFactory");
           js.setDestinationJNDIName(jndiAppend + "jms/topic/flex/simpletopic");
        }
        
        js.setDeliveryMode("NON_PERSISTENT");
        js.setMessagePriority(javax.jms.Message.DEFAULT_PRIORITY);
        js.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
        adapter.setJMSSettings(js);
        adapter.getJMSSettings();
        adapter.setDestination(msgDest);
        
        return msgDest;
    }
    
    
    public void start()
    {        
        if(broker != null)
        {
            MessageService service = (MessageService) broker.getService("message-service");
            
            MessageDestination destination = createDestination(dest_runtime, service);
                       
            // code coverage
            destination.addChannel("qa-polling-amf");        
            assert (destination.getExtraProperty("extra-name")== null);        
            destination.addExtraProperty("extra-name", dest_runtime);        
            Log.getLogger("QE.CODE_COVERAGE").debug("Destionation " + destination.getExtraProperty("extra-name") + " isCluster=" + destination.isClustered() +  " isBackendShared=" + destination.isBackendShared());
            
            destination.start();
            
            // add a runtime remote destination to run code coverage
            RemotingService rs = (RemotingService) broker.getService("remoting-service");
            String id = "RuntimeRemotingDest_JMS";
            RemotingDestination rsd = (RemotingDestination)rs.createDestination(id);
            rsd.setSource("runtimeconfig.components.RuntimeJMSDestination");
            rsd.start();
        }
    }
    
    public void stop()
    {
        // no-op
    }
    
    // code coverage
    public void runCodeCoverage()
    {        
        Destination d = new Destination();
        
        MessageBroker mb = MessageBroker.getMessageBroker(null);
        MessageService service = (MessageService) mb.getService("message-service");
        MessageDestination destination = (MessageDestination)service.getDestination(dest_runtime);
        
        Log.getLogger("QE.CODE_COVERAGE").debug("Destination " + dest_runtime + " isCluster=" + destination.isClustered() +" isBackendShared=" + destination.isBackendShared());
        
        destination.removeChannel("qa-polling-amf");
        destination.addChannel("non-existing-channel");
        
        ArrayList ids = new ArrayList();
        ids.add("qa-polling-amf");
        ids.add("non-existing-channel2");
        destination.setChannels(ids);
        
        ConfigMap cm = destination.describeDestination().getPropertyAsMap("channels", null);        
        if (cm != null)
            Log.getLogger("QE.CODE_COVERAGE").debug("Destination " + dest_runtime + " has " + cm.size() + " channels." );
        
        ServiceAdapter adapter = destination.getAdapter();
        if (adapter != null)
        {
            destination.setAdapter(null);
            destination.setAdapter(adapter);
            destination.start();    
        }
        
        destination.setSecurityConstraint("no-existing-constraint");
    } 

/* commenting out this function, but leaving it here for future reference.
 * Replacing it with an alternative getAppServer() method which is able to get all the app servers
 */
/*	private boolean isWebLogicOrWebSphere()
    {
        try 
        {
            Class.forName("weblogic.logging.Loggable");
            Log.getLogger("QE-DEBUG").debug("Found WebLogic");
            return true;
        } 
        catch (ClassNotFoundException cnfe)
        {
              // ignore          
        }
        
        try 
        {
            Class.forName("com.ibm.ws.bootstrap.WsLogManager");
            Log.getLogger("QE-DEBUG").debug("Found WebSphere");
            return true;
        } 
        catch (ClassNotFoundException cnfe)
        {
             // ignore       
        }        
        return false;
    }
 */
	
	private String getAppServer()
	{
	    String serverInfo = broker.getSecuritySettings().getServerInfo().toLowerCase();
//	    System.out.println("serverInfo: " + serverInfo);
	    if (serverInfo.indexOf("tomcat") != -1 )
	    {
	        return "tomcat";
	    } else if (serverInfo.indexOf("jboss") != -1) 
	    {   return "jboss";
	    } else if (serverInfo.indexOf("jrun") != -1)
	    {
	        return "jrun";
	    } else if (serverInfo.indexOf("websphere") != -1)
	    {
	        return "websphere";             
	    } else if (serverInfo.indexOf("weblogic") != -1)
	    {
	        return "weblogic"  ;            
	    } else if (serverInfo.indexOf("oracle") != -1)
	    {
	        return "oracle";           
	    }
	    else return "unknown";
	}

}