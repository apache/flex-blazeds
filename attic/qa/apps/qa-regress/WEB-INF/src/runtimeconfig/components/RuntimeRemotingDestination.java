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
import flex.messaging.services.RemotingService;
import flex.messaging.services.remoting.RemotingDestination;

/*
 * The purpose of this class is to duplicate the WeatherService remoting destination 
 * using runtime configuration.  This service creates two destinations, one during server startup,
 * the other after the server has started (at runtime).
 */
public class RuntimeRemotingDestination extends AbstractBootstrapService
{

    public void initialize(String id, ConfigMap properties)
    {
        //Create destination and add to the Service
        RemotingService service = (RemotingService) broker.getService("remoting-service");
        String dest = "RemotingDest_startup";
        createDestination(dest, service);
    }

    /*
     * The following is a destination in the eqa application, under messaging-config.xml
     * The method below implements it at runtime
     * 
         <destination id="WeatherService">
            <properties>
                <source>dev.weather.WeatherService</source>
            </properties>
         </destination>
    */

    private RemotingDestination createDestination(String id, RemotingService messageService)
    {
        RemotingDestination remoteDest = (RemotingDestination)messageService.createDestination(id);
        remoteDest.setSource("dev.weather.WeatherService");
        
        return remoteDest;
    }
    
    public void start()
    {
        RemotingService service = (RemotingService) broker.getService("remoting-service");
        String id = "RemotingDest_runtime";
        RemotingDestination destination = createDestination(id, service);
        destination.start();
        
    }
    
    public void stop()
    {
        // No-op
    }
    
}


