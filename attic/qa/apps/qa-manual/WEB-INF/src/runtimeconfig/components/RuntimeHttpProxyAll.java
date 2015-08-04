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

import flex.messaging.config.ConfigMap;
import flex.messaging.services.AbstractBootstrapService;
import flex.messaging.services.HTTPProxyService;
import flex.messaging.services.http.HTTPProxyAdapter;
import flex.messaging.services.http.HTTPProxyDestination;
import flex.messaging.services.http.HTTPConnectionManagerSettings;

/* 
 * The purpose of this class is to recreate the proxy service and add a destination
 * To test this, the proxy-service must be manually removed from services-config.xml,
 * and the following entry added inside the <services> node instead:
 * <service class="runtimeconfig.components.RuntimeHttpProxyAll" id="proxy"></service>
 */
public class RuntimeHttpProxyAll extends AbstractBootstrapService
{
    

    public void initialize(String id, ConfigMap properties)
    {
        // Create Service and add to the MessageBroker
        HTTPProxyService proxyService = createProxyService();
        getMessageBroker().addService(proxyService);
        
        // Create destination and add to the Service
        String dest = "HTTPProxyDest_startup";
        HTTPProxyDestination proxyDest = createProxyDestination(dest, proxyService);                   
        proxyService.addDestination(proxyDest);
        
        // This is needed to set the properties on the adapter: after both service 
        // and destination exist
        createAdapter(proxyDest);        
    }

    /*
     * The following method recreates the proxy-service as defined under the qa-manual\WEB-INF\flex directory
       <?xml version="1.0"?>
       <service class="flex.messaging.services.HTTPProxyService" id="proxy-service" messageTypes="flex.messaging.messages.HTTPMessage,flex.messaging.messages.SOAPMessage">
    
        // The following <properties> node must be implemented by each adapter for this service.
        // See the createAdapter() method below.
        <properties>
            <connection-manager>
                <max-total-connections>100</max-total-connections>
                <default-max-connections-per-host>2</default-max-connections-per-host>
            </connection-manager>
            <allow-lax-ssl>true</allow-lax-ssl>
        </properties>
    
        <adapters>
            <adapter-definition class="flex.messaging.services.http.HTTPProxyAdapter" default="true" id="http-proxy"></adapter-definition>
            <adapter-definition class="flex.messaging.services.http.SOAPProxyAdapter" id="soap-proxy"></adapter-definition>
        </adapters>
        
        <default-channels>
           <channel ref="qa-http"></channel>
           <channel ref="qa-amf"></channel>
           <channel ref="qa-secure-amf"></channel>
        </default-channels>
    */
    private HTTPProxyService createProxyService()
    {
        HTTPProxyService proxyService = new HTTPProxyService(true);
        proxyService.setId("proxy-service");
        proxyService.addDefaultChannel("qa-http");
        proxyService.addDefaultChannel("qa-amf");
        proxyService.addDefaultChannel("qa-secure-amf");
        proxyService.registerAdapter("http-proxy", "flex.messaging.services.http.HTTPProxyAdapter");
        proxyService.registerAdapter("soap-proxy", "flex.messaging.services.http.SOAPProxyAdapter");
        proxyService.setDefaultAdapter("http-proxy");

        return proxyService;
    }
 
    /* This method replicates the following destination in proxy-config.xml
    <destination id="echoParams_amf">
        <channels>
            <channel ref="qa-amf"></channel>
        </channels>

        <properties>
            <url>http://10.60.144.67:8080/services/httpservice/echoParams.jsp</url>
        </properties>
    </destination>
     */
    private HTTPProxyDestination createProxyDestination(String id, HTTPProxyService proxyService)
    {
        HTTPProxyDestination proxyDest = new HTTPProxyDestination(true);
    	proxyDest.setId(id);
    	proxyDest.setService(proxyService);
       
        // we'll use the default channel - so nothing else is needed
        proxyDest.addChannel("qa-amf");
        //Use this type of code to generate the warning "No channel with id '{0}' is known by the MessageBroker. Not adding the channel.",
        
        // set destination properties
        proxyDest.setDefaultUrl("http://10.60.144.67:8080/services/httpservice/echoParams.jsp");
               
        return proxyDest;
    }
    
    /* 
     * This method creates the adapter used by the destination passed as a parameter.
     * @param proxyDest the destination to associate with this adapter
        <properties>
            <connection-manager>
                <max-total-connections>100</max-total-connections>
                <default-max-connections-per-host>2</default-max-connections-per-host>
            </connection-manager>
            <allow-lax-ssl>true</allow-lax-ssl>
        </properties>
     */
    private void createAdapter(HTTPProxyDestination proxyDest)
    {
        // Create an adapter for the destination
        HTTPProxyAdapter proxyAdapter = new HTTPProxyAdapter();
       
        // Set adapter's id
        proxyAdapter.setId("runtime-http-proxy");
       
        // Set adapter's management property
        proxyAdapter.setManaged(true);
       
        // Set adapter's parent (which also sets destination's adapter)
        proxyAdapter.setDestination(proxyDest);
       
        // Alternatively, we could have set destination's adapter
        //proxyDest.setAdapter(proxyAdapter);
       
        // Set some adapter properties
        proxyAdapter.setAllowLaxSSL(true);
       
        int maxTotal = 100;
        int defaultMaxConnsPerHost = 2;
        HTTPConnectionManagerSettings connectionParams = new HTTPConnectionManagerSettings();
        connectionParams.setMaxTotalConnections(maxTotal);
        connectionParams.setDefaultMaxConnectionsPerHost(defaultMaxConnsPerHost);   
       
        proxyAdapter.setConnectionManagerSettings(connectionParams);
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


