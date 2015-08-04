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

import java.util.ArrayList;
import java.util.List;

import flex.messaging.config.ConfigMap;
import flex.messaging.services.AbstractBootstrapService;
import flex.messaging.services.HTTPProxyService;
import flex.messaging.services.http.HTTPProxyAdapter;
import flex.messaging.services.http.HTTPProxyDestination;
import flex.messaging.services.http.HTTPConnectionManagerSettings;

/*
 * The purpose of this class is to create runtime-configured Proxy destinations
 * that are part of qa-regress automated testing.
 */
public class RuntimeHttpProxyDestination extends AbstractBootstrapService
{
    
   public void initialize(String id, ConfigMap properties)
    {
        // Get the instance of the proxy-service from MessageBroker
        HTTPProxyService proxyService = (HTTPProxyService)getMessageBroker().getService("proxy-service");
        
        // Create destination and add to the Service
        String dest = "HTTPProxyDest_startup";
        HTTPProxyDestination proxyDest = createProxyDestination(dest, proxyService);                   
        
        createAdapter(proxyDest); 
    }

    /**
     *  This destination duplicates the echoParams_amf destination used by defined
     *  in the proxy-config.xml.  
     *  <destination id="echoParams_amf">
     *       <channels>
     *          <channel ref="qa-amf"></channel>
     *      </channels>
     *  
     *      <properties>
     *          <url>http://10.60.144.65:8080/services/httpservice/echoParams.jsp</url>
     *      </properties>
     *  </destination> 
     **/   
    private HTTPProxyDestination createProxyDestination(String id, HTTPProxyService proxyService)
    {
        HTTPProxyDestination proxyDest = new HTTPProxyDestination(true);
        proxyDest.setId(id);
        proxyDest.setService(proxyService);
       
        proxyDest.addChannel("qa-amf");
        
        // set destination properties
        proxyDest.addDynamicUrl("http://10.60.144.65:8080/services/httpservice/echoParams.jsp");
        List list = new ArrayList();
        list.add("http://10.60.144.65:8080/services/httpservice/echoParams.jsp");
        proxyDest.addDynamicUrls(list);
        proxyDest.setDefaultUrl("http://10.60.144.65:8080/services/httpservice/echoParams.jsp");
        proxyDest.getDefaultUrl();
        proxyDest.setRemoteUsername(null);
        proxyDest.setRemotePassword(null);
        proxyDest.getRemotePassword();
        proxyDest.setUseCustomAuthentication(false);
        proxyDest.isUseCustomAuthentication();
                       
        return proxyDest;
    }
   
    
    /* This method defines the following properties in the adapter itself, which are NOT part
     * of the service, as the proxy-config.xml would have you think.
        <properties>
            <connection-manager>
                <max-total-connections>100</max-total-connections>
                <default-max-connections-per-host>2</default-max-connections-per-host>
            </connection-manager>
            <allow-lax-ssl>true</allow-lax-ssl>
        </properties>

     */
    /**
     * @param proxyDest
     */
    private HTTPProxyAdapter createAdapter(HTTPProxyDestination proxyDest)
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
      
        HTTPConnectionManagerSettings connectionParams = new HTTPConnectionManagerSettings();
        connectionParams.setMaxTotalConnections(100);
        connectionParams.setDefaultMaxConnectionsPerHost(2);   
        proxyAdapter.setConnectionManagerSettings(connectionParams);
        
        return proxyAdapter;
    }     
    
    
    //Modify the adapter used by earlier destination and use it for a second one
    private void modifyAdapter(HTTPProxyDestination proxyDest)
    {
        // Create an adapter for the destination
        HTTPProxyAdapter proxyAdapter = (HTTPProxyAdapter) proxyDest.getAdapter();
       
        // Set some adapter properties
        proxyAdapter.isAllowLaxSSL();
    }     
    

    public void start()
    {
        HTTPProxyService proxyService = (HTTPProxyService)getMessageBroker().getService("proxy-service");
        String id = "HTTPProxyDest_runtime";
        HTTPProxyDestination proxyDest = createProxyDestination(id, proxyService); 
        //Use the following method to test more methods on HttpProxyAdapter
        createAdapter(proxyDest);
        proxyDest.start();
        modifyAdapter(proxyDest);
    }
    
    public void stop()
    {
        // No-op
    }
}


