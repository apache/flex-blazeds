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


/*
 * The intent of this class is to test a dependency between bootstrap services:
 * First load the RuntimeHttpProxyService followed by the RuntimeHttpProxyDestination.
 * The destination depends on the service creation.
 * You have to use the following in the services-config.xml before you start:
 *      <service class="runtimeconfig.listeners.RuntimeHttpProxyService" id="proxyDest1"></service>
 *      <service class="runtimeconfig.listeners.RuntimeHttpProxyDestination" id="proxyDest2"></service>    
 */
public class RuntimeHttpProxyService extends AbstractBootstrapService
{
    
    public void initialize(String id, ConfigMap properties)
    {
         // Create Service and add to the MessageBroker
        HTTPProxyService proxyService = createProxyService();
        getMessageBroker().addService(proxyService);
    }

    /**
     * <?xml version="1.0"?>
       <service class="flex.messaging.services.HTTPProxyService" id="proxy-service" messageTypes="flex.messaging.messages.HTTPMessage,flex.messaging.messages.SOAPMessage">
    
        //This section to be done on the adapter in RuntimeHttpProxyDestination.java fiel
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
    
    public void start()
    {
    }
    
    public void stop()
    {
    }
    
}


