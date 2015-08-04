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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import flex.messaging.MessageBroker;
import flex.messaging.MessageDestination;
import flex.messaging.config.ServerSettings;
import flex.messaging.services.HTTPProxyService;
import flex.messaging.services.MessageService;
import flex.messaging.services.RemotingService;
import flex.messaging.services.Service;
import flex.messaging.services.http.HTTPProxyDestination;
import flex.messaging.services.remoting.RemotingDestination;
import flex.messaging.FlexContext;

public class RuntimeConfigurator
{

    MessageBroker msgBroker;
    
    public RuntimeConfigurator()
    {
        System.out.println("DynamicDestinationCreator - constructor");
        msgBroker = FlexContext.getMessageBroker();
    }
    
    public Map getServiceNames()
    {
        List serviceNames = new ArrayList();
        for (Iterator iter = msgBroker.getServices().values().iterator(); iter.hasNext();)
        {
            Service service = (Service) iter.next();
            serviceNames.add(service.getId());            
        }
        Map services = new HashMap();
        services.put("type", "serviceNames");
        services.put("value", serviceNames);
        return services;
    }
    
    public Map getDestinationNames(String svcId)
    {
        List destinationNames = new ArrayList();
        for (Iterator iter = msgBroker.getService(svcId).getDestinations().keySet().iterator(); iter.hasNext();)
        {
            destinationNames.add((String)iter.next());
            
        }
        Map destinations = new HashMap();
        destinations.put("type", "destinationNames");
        destinations.put("value", destinationNames);
        return destinations;
    }
    
    public String removeService(String id)
    {        
        msgBroker.removeService(id);
        
        return "Service: " + id + " removed from MessageBroker";
    }
    
    public String removeDestination(String destId, String svcId)
    {
        Service svc = msgBroker.getService(svcId);
        svc.removeDestination(destId);
        
        return "Destination: "+ destId + " removed from Service: "+svcId;
    }
      
    public String createRemotingDestination(){
        return createRemotingDestination("RC_WeatherService", "dev.weather.WeatherService", "application");
    }

    public String createRemotingDestination(String id, String src, String scope)
    {
        String dest_id;
        String dest_src;
        String dest_scope;
        
        if (id == null || src == null || scope == null) 
        {
            dest_id = "RC_WeatherService";
            dest_src = "dev.weather.WeatherService";
            dest_scope = "application";
        } 
        else 
        {
            dest_id = id;
            dest_src = src;
            dest_scope = scope;
        }

        String serviceId = "remoting-service";
                
        RemotingService service = (RemotingService)msgBroker.getService(serviceId);
        RemotingDestination destination = (RemotingDestination)service.createDestination(dest_id);
        destination.setSource(dest_src);
        destination.setScope(dest_scope);
        //destination.addChannel("my-http");
        destination.addChannel("qa-amf");
        
        if (service.isStarted())
            destination.start();
               
        return "Destination: "+ dest_id+ " source " + dest_src + " scope " + dest_scope + " created for Service: "+serviceId;   
    }
    
    public String createProxyService()
    {
        String serviceId = "proxy-service";
        
        HTTPProxyService proxyService = new HTTPProxyService();
        proxyService.setId(serviceId);
        proxyService.setManaged(true);
        proxyService.setMessageBroker(msgBroker);
        proxyService.addDefaultChannel("qa-http");
        //proxyService.addDefaultChannel("my-amf");
        proxyService.registerAdapter("http-proxy", "flex.messaging.services.http.HTTPProxyAdapter");
        proxyService.registerAdapter("soap-proxy", "flex.messaging.services.http.SOAPProxyAdapter");
        proxyService.setDefaultAdapter("http-proxy");
        msgBroker.addService(proxyService);
        if (msgBroker.isStarted())
            proxyService.start();
                        
        return "Service: "+serviceId+" created";
    }
           
    public String createProxyDestination()
    {
        String serviceId = "proxy-service";
        String id = "RC_WeatherService";
        
        HTTPProxyService proxyService = (HTTPProxyService)msgBroker.getService(serviceId); 
        HTTPProxyDestination proxyDest = new HTTPProxyDestination();
        proxyDest.setId(id);
        proxyDest.setManaged(true);
        proxyDest.setService(proxyService);
        
        // we'll use the default channel - so nothing else is needed             
        // we'll use the default adapter - so nothing else is needed
        // set destination properties
               
        proxyDest.setDefaultUrl("http://{server.name}:{server.port}/{context.root}/services/WeatherService?wsdl");
        proxyDest.addDynamicUrl("http://{server.name}:*/{context.root}/services/WeatherService");

        proxyService.addDestination(proxyDest);
        
        if (proxyService.isStarted())
            proxyDest.start();
        
        return "Destination: "+id+" created for Service: "+serviceId;
    }
            
    /*
   <destination id="MyTransientTopic">

        <properties>
            <network>
                <session-timeout>0</session-timeout>
                <!--<cluster ref="default-cluster"/>-->
            </network>
            <server>
                <!-- max number of messages to maintain in memory cache -->
                <max-cache-size>1000</max-cache-size>

                <!-- ttl of 0 means live forever -->
                <message-time-to-live>0</message-time-to-live>

                <!-- options to make this a durable destination -->
                <durable>false</durable>

            </server>
        </properties>

        <channels>
            <!-- <channel ref="my-rtmp"/> -->
        <channel ref="my-polling-amf"/>
        </channels>

    </destination>

     */
    public String createMessageDestination()
    {
        String serviceId = "message-service";
        String id = "MyTransientTopic2";
        
        MessageService msgService = (MessageService)msgBroker.getService(serviceId);
        MessageDestination msgDestination;
        msgDestination = (MessageDestination)msgService.createDestination(id);
                
        msgDestination.addChannel("qa-polling-amf");
              
        ServerSettings serverSettings = new ServerSettings();
        serverSettings.setMessageTTL(0);
        serverSettings.setDurable(false);
        msgDestination.setServerSettings(serverSettings);
        
        // we'll use the default adapter - so nothing else is needed        
        
        if (msgService.isStarted())
            msgDestination.start();
        
        return "Destination: "+id+" created for Service: "+serviceId;   
    }
    

                 
}
