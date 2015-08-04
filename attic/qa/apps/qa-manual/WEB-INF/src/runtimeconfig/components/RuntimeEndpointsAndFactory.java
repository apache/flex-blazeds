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

import flex.messaging.FlexFactory;
import flex.messaging.config.ConfigMap;
import flex.messaging.endpoints.HTTPEndpoint;
import flex.messaging.factories.JavaFactory;
import flex.messaging.services.AbstractBootstrapService;


/** 
 * This class create runtime-configured destinations and a factory class instance.
 * These destinations are used in rtPushOverHTTP tests inside the qa-manual application.
 * TBD: create a destination that uses the factory.
 */
public class RuntimeEndpointsAndFactory extends AbstractBootstrapService
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
        createEndpoints();
        createFactories();
    }

    /**
     * Called by the <code>MessageBroker</code> as server starts. Useful for
     * custom code that needs to run after all the components are initialized
     * and the server is starting up. 
     */    
    public void start()
    {
        // No-op        
    }

    /**
     * Called by the <code>MessageBroker</code> as server stops. Useful for 
     * custom code that needs to run as the server is shutting down.
     */
    public void stop()
    {
        // No-op        
    }
    
    private void createEndpoints()
    {
    	createHttpLongPollEndpoint();
        createHttpWaitingPollRequestsEndpoint();
        createDataSecureHTTPPollingEndpoint();
    }
    
    /**
     * This endpoint is used by the Real Time Push over HTTP feature.  It is meant do duplicate the following static endpoint:
	     <channel-definition id="data-http-long-poll" class="mx.messaging.channels.HTTPChannel">
	        <endpoint url="http://{server.name}:{server.port}/qa-manual/messagebroker/testhttp1" class="flex.messaging.endpoints.HTTPEndpoint"/>  
	        <properties>
		        <polling-enabled>true</polling-enabled>                
		        <polling-interval-millis>0</polling-interval-millis>
		        <wait-interval-millis>10000</wait-interval-millis>
	            <max-waiting-poll-requests>2</max-waiting-poll-requests>                
	        </properties>
	    </channel-definition>        
     */
    private void createHttpLongPollEndpoint()
    {
        String endpointId = "data-http-long-poll";
        String endpointUrl = "http://{server.name}:{server.port}/qa-manual/messagebroker/httplongpoll";
        String endpointClass = "flex.messaging.endpoints.HTTPEndpoint";         
        HTTPEndpoint httpEndpoint = (HTTPEndpoint)broker.createEndpoint(endpointId, endpointUrl, endpointClass);  
        
        // <polling-enabled> is set on the client via the PollingChannel API
        // <polling-interval-millis> is set on the client via the PollingChannel API
        httpEndpoint.setWaitInterval(10000);
        httpEndpoint.setMaxWaitingPollRequests(2);
    }

    /**
     * This endpoint is used by the Real Time Push over HTTP feature
	    <channel-definition id="data-testhttp2" class="mx.messaging.channels.HTTPChannel" parent="services-config/channels">
	        <endpoint url="http://{server.name}:{server.port}/qa-manual/messagebroker/testhttp2" class="flex.messaging.endpoints.HTTPEndpoint"/>      
	        <properties>
	            <polling-enabled>true</polling-enabled>
		        <polling-interval-millis>0</polling-interval-millis>            
	            <wait-interval-millis>-1</wait-interval-millis>
	            <max-waiting-poll-requests>4</max-waiting-poll-requests>
	            <client-wait-interval-millis>3000</client-wait-interval-millis>
	        </properties>
	    </channel-definition> 
     */
    private void createHttpWaitingPollRequestsEndpoint()
    {
        String endpointId = "data-http-waiting-poll-requests";
        String endpointUrl = "http://{server.name}:{server.port}/qa-manual/messagebroker/httpwaitingpoll";
        String endpointClass = "flex.messaging.endpoints.HTTPEndpoint";         
        HTTPEndpoint httpEndpoint = (HTTPEndpoint)broker.createEndpoint(endpointId, endpointUrl, endpointClass);  
        
        // <polling-enabled> is set on the client via the PollingChannel API
        // <polling-interval-millis> is set on the client via the PollingChannel API
        httpEndpoint.setWaitInterval(-1);
        httpEndpoint.setMaxWaitingPollRequests(4);
        httpEndpoint.setClientWaitInterval(3000);
    }

    /**
     * This endpoint is used by the Real Time Push over HTTP feature
        <channel-definition id="data-secure-http-polling" class="mx.messaging.channels.SecureHTTPChannel" parent="services-config/channels" action="add">
            <endpoint url="https://{server.name}:9400/qa-manual/messagebroker/securehttpac" class="flex.messaging.endpoints.SecureHTTPEndpoint"/>
            <properties>
                <polling-enabled>true</polling-enabled>
                <polling-interval-seconds>2000</polling-interval-seconds>
            </properties>
        </channel-definition>
	*/
	private void createDataSecureHTTPPollingEndpoint()
	{
	    String endpointId = "data-secure-http-polling";
	    String endpointUrl = "https://{server.name}:9400/qa-manual/messagebroker/securepollinghttp";
	    String endpointClass = "flex.messaging.endpoints.SecureHTTPEndpoint";         
	    broker.createEndpoint(endpointId, endpointUrl, endpointClass);
	    
        // <polling-enabled> is set on the client via the PollingChannel API
        // <polling-interval-millis> is set on the client via the PollingChannel API
	}
    
    /**
     * This method removes and recreates the following factory, as an example of creating factories dynamically.
     * <factory class="flex.messaging.factories.JavaFactory" id="remotingTestFactory"></factory>
     */
    private void createFactories()
    {
    	String factoryId = "remotingTestFactory";
    	FlexFactory factory = new JavaFactory();
        
        broker.removeFactory(factoryId);
        broker.addFactory(factoryId, factory);
    }
}
