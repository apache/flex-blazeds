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

import flex.messaging.config.ConfigMap;
import flex.messaging.services.AbstractBootstrapService;
import flex.messaging.services.Service;
import flex.messaging.services.remoting.RemotingDestination;

/**
 * This BootstrapService is used to dynamicaly create a Remoting Service along 
 * with its Remoting Destinations without the need for any configuration files.
 */
public class RemotingBootstrapService extends AbstractBootstrapService
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
        Service remotingService = createService();
        createDestination1(remotingService);
        createDestination2(remotingService);
        createDestination3(remotingService);
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
    <service id="remoting-service" class="flex.messaging.services.RemotingService">

        <!-- Example remoting-config.xml -->

        <!--
            The set of adapters available for this service. A service uses an 
            adapter to handle the implementation specifc details of a 
            destination.
        -->
        <adapters>
            <!--
                id: A unique id for this adapter-definition. Destinations use this
                    id to select which adapter should be used to process requests.
                class: The implementation class for the adapter. A single Remoting 
                    Service adapter ships with Flex 2:
                        flex.messaging.services.remoting.adapters.JavaAdapter
                default: An optional boolean attribute identifying the adapter to
                    use when none is specified for a destination.
            -->
            <adapter-definition id="java-object"
                class="flex.messaging.services.remoting.adapters.JavaAdapter"
                default="true"/>
        </adapters>

        <!--
            The set of default channels to use to transport messages to 
            remoting-service destinations.
        -->
        <default-channels>
           <!--
               ref: A reference to a channel-definition id. Channels are defined
               in the top level configuration file.
            -->
            <channel ref="my-amf"/>
            <channel ref="my-http"/>
        </default-channels>
    */
    private Service createService()
    {
        String serviceId = "remoting-service";
        String serviceClass = "flex.messaging.services.RemotingService";
        Service remotingService = broker.createService(serviceId, serviceClass);

        String adapterId = "java-object";
        String adapterClass = "flex.messaging.services.remoting.adapters.JavaAdapter";
        remotingService.registerAdapter(adapterId, adapterClass);
        remotingService.setDefaultAdapter(adapterId);

        remotingService.addDefaultChannel("my-amf");
        remotingService.addDefaultChannel("my-http");

        return remotingService;
    }

    /*
    <!-- 
        A simple example. 
        
        This destination uses the default set of channels 'my-amf' and 
        'my-http', relies on the default adapter configured for this service, 
        'java-object' (an instance of JavaAdapter), will use the default factory
        of the JavaAdapter - the flex.messaging.factories.JavaFactory, and 
        POJO instances of the class specified by the source property will be 
        created in the default scope, the 'request' scope.
    -->
    <destination id="sample">
        <properties>
            <!-- source is the Java class name of the destination -->
            <source>my.company.SampleService</source>
        </properties>
    </destination>
     */
    private void createDestination1(Service service)
    {
        String destinationId = "sample";
        RemotingDestination destination = (RemotingDestination)service.createDestination(destinationId);
        destination.setSource("my.company.SampleService");
    }

    /*
    <!-- 
        A more complex example.

        A custom factory is used to create instances of the source specified
        for this destination. Instances will be shared between requests in
        the same session. This destination also restricts access to 
        authenticated users who are in the 'sampleusers' role.
    -->
    <destination id="sampleByFactoryAndSecure">
        <security>
            <security-constraint ref="sample-users" />
        </security>
        <properties>
            <!-- 
                myJavaFactory is defined in the main configuration file. The 
                source and all other properties are used by the factory to 
                create the java class. Factory instance provides the java class
                based on the properties provided such as scope.
            -->
            <factory>myJavaFactory</factory>
            <source>my.company.SampleService</source>
            <!-- Possible scope values are request, session or application. -->
            <scope>session</scope>
        </properties>
    </destination>
     */
    private void createDestination2(Service service)
    {
        String destinationId = "sampleByFactoryAndSecure";
        RemotingDestination destination = (RemotingDestination)service.createDestination(destinationId);
        destination.setSecurityConstraint("sample-users");
        destination.setFactory("myJavaFactory");
        destination.setSource("my.company.SampleService");
        destination.setScope("session");
    }

    /*
    <!--
        A verbose example using child tags.
    -->
    <destination id="sampleVerbose">
        <channels>
            <channel ref="my-secure-amf" />
            <channel ref="my-secure-http" />
        </channels>
        <adapter ref="java-object" />
        <security>
            <security-constraint ref="sample-users" />
        </security>
        <properties>
            <source>my.company.SampleService</source>
            <scope>session</scope>
            <factory>myJavaFactory</factory>
        </properties>
    </destination>
     */
    private void createDestination3(Service service)
    {
        String destinationId = "sampleVerbose";
        RemotingDestination destination = (RemotingDestination)service.createDestination(destinationId);
        destination.addChannel("my-secure-amf");
        destination.addChannel("my-secure-http");
        
        String adapterId = "java-object";
        destination.createAdapter(adapterId);
        
        destination.setSecurityConstraint("sample-users");
        destination.setSource("my.company.SampleService");
        destination.setScope("session");
        destination.setFactory("myJavaFactory");
    }
}
