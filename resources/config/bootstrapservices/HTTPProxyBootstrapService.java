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
import flex.messaging.services.http.ExternalProxySettings;
import flex.messaging.services.http.HTTPConnectionManagerSettings;
import flex.messaging.services.http.HTTPProxyAdapter;
import flex.messaging.services.http.HTTPProxyDestination;
import flex.messaging.services.http.SOAPProxyAdapter;

/**
 * This BootstrapService is used to dynamicaly create a HTTPProxy Service along 
 * with its HTTPProxy Destinations without the need for any configuration files.
 */
public class HTTPProxyBootstrapService extends AbstractBootstrapService
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
        Service httpProxyService = createService();
        createDestination1(httpProxyService);
        createDestination2(httpProxyService);
        createDestination3(httpProxyService);
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
    <service id="proxy-service" class="flex.messaging.services.HTTPProxyService">

    <!-- Example proxy-config.xml -->

    <properties>
        <connection-manager>
            <max-total-connections>100</max-total-connections>
            <default-max-connections-per-host>2</default-max-connections-per-host>
        </connection-manager>

        <!-- Allow self-signed certificates; should not be used in production -->
        <allow-lax-ssl>true</allow-lax-ssl>

        <external-proxy>
            <server>10.10.10.10</server>
            <port>3128</port>
            <nt-domain>mycompany</nt-domain>
            <username>flex</username>
            <password>flex</password>
        </external-proxy>
    </properties>

    <!-- Server-side code that directly contacts a destination object or service -->
    <adapters>
        <!--
           id: a unique id specifying the adapter
           class: the Flex Enterprise class which implements the adapter
             possible values: flex.messaging.services.http.HTTPProxyAdapter, flex.messaging.services.http.SOAPProxyAdapter
           default: an optional attribute identifying the adapter to use when none is specified for the service
        -->
        <adapter-definition id="http-proxy" class="flex.messaging.services.http.HTTPProxyAdapter" default="true"/>
        <adapter-definition id="soap-proxy" class="flex.messaging.services.http.SOAPProxyAdapter"/>
    </adapters>

    <default-channels>
        <!--
           Set the ref id of the default channels to use as transport for this service.
           The channel is defined elsewhere using the channel-definition tag.
        -->
        <channel ref="my-http"/>
        <channel ref="my-amf"/>
    </default-channels>
     */
    private Service createService()
    {
        String serviceId = "proxy-service";
        String serviceClass = "flex.messaging.services.HTTPProxyService";        
        Service httpProxyService = broker.createService(serviceId, serviceClass);

        // Note that <properties> are not set on the service since they are
        // adapter related properties and will be configured at adapter level

        httpProxyService.registerAdapter("http-proxy", "flex.messaging.services.http.HTTPProxyAdapter");
        httpProxyService.registerAdapter("soap-proxy", "flex.messaging.services.http.SOAPProxyAdapter");
        httpProxyService.setDefaultAdapter("http-proxy");

        httpProxyService.addDefaultChannel("my-http");
        httpProxyService.addDefaultChannel("my-amf");

        return httpProxyService;
    }

    /*
    <!-- Example default http destination -->
    <destination id="DefaultHTTP">
        <properties>
            <dynamic-url>http://{server.name}:/{context.root}/</dynamic-url>
        </properties>
    </destination>

    <!-- Example http proxy adapter destination -->
    <destination id="myHTTPService">
        <properties>
            <!-- The endpoint available to the http proxy service -->
            <url>http://www.mycompany.com/services/myservlet</url>

            <!-- Wild card endpoints available to the http proxy services -->
            <dynamic-url>http://www.mycompany.com/services/*</dynamic-url>
        </properties>
    </destination>
     */
    public void createDestination1(Service service)
    {
        String destinationId = "DefaultHTTP";
        HTTPProxyDestination destination = (HTTPProxyDestination)service.createDestination(destinationId);

        destination.addDynamicUrl("http://{server.name}:*/{context.root}/*");

        String adapterId = "http-proxy";
        HTTPProxyAdapter adapter = (HTTPProxyAdapter)destination.createAdapter(adapterId);
        addProperties(adapter);
    }

    /*
    <!-- Example http proxy adapter destination -->
    <destination id="myHTTPService">
        <properties>
            <!-- The endpoint available to the http proxy service -->
            <url>http://www.mycompany.com/services/myservlet</url>

            <!-- Wild card endpoints available to the http proxy services -->
            <dynamic-url>http://www.mycompany.com/services/*</dynamic-url>
        </properties>
    </destination>
     */
    private void createDestination2(Service service)
    {
        String destinationId = "myHTTPService";
        HTTPProxyDestination destination = (HTTPProxyDestination)service.createDestination(destinationId);
        
        destination.setDefaultUrl("http://www.mycompany.com/services/myservlet");
        destination.addDynamicUrl("http://www.mycompany.com/services/*");

        String adapterId = "http-proxy";
        HTTPProxyAdapter adapter = (HTTPProxyAdapter)destination.createAdapter(adapterId);
        addProperties(adapter);
    }

    /*
    <!-- Example soap proxy adapter destination -->
    <destination id="echoSoapService">
        <properties>
            <!-- The location of the wsdl defined for soap proxy services -->
            <wsdl>http://{server.name}:{server.port}/myapp/echo?wsdl</wsdl>

            <!-- The soap endpoints available for access defined for soap proxy services -->
            <soap>http://{server.name}:/myapp/echo</soap>
        </properties>

        <!-- A specific adapter ref for the destination may be defined -->
        <adapter ref="soap-proxy"/>
    </destination>     
     */
    private void createDestination3(Service service)
    {
        String destinationId = "echoSoapService";
        HTTPProxyDestination destination = (HTTPProxyDestination)service.createDestination(destinationId);

        destination.setDefaultUrl("http://{server.name}:{server.port}/myapp/echo?wsdl");
        destination.addDynamicUrl("http://{server.name}:/myapp/echo");

        String adapterId = "soap-proxy";
        SOAPProxyAdapter adapter = (SOAPProxyAdapter)destination.createAdapter(adapterId);
        addProperties(adapter);
    }

    /*
    <properties>
        <connection-manager>
            <max-total-connections>100</max-total-connections>
            <default-max-connections-per-host>2</default-max-connections-per-host>
        </connection-manager>

        <!-- Allow self-signed certificates; should not be used in production -->
        <allow-lax-ssl>true</allow-lax-ssl>

        <external-proxy>
            <server>10.10.10.10</server>
            <port>3128</port>
            <nt-domain>mycompany</nt-domain>
            <username>flex</username>
            <password>flex</password>
        </external-proxy>
    </properties>     
     */
    private void addProperties(HTTPProxyAdapter adapter)
    {
        HTTPConnectionManagerSettings cms = new HTTPConnectionManagerSettings();
        cms.setMaxTotalConnections(100);
        cms.setDefaultMaxConnectionsPerHost(2);
        adapter.setConnectionManagerSettings(cms);

        adapter.setAllowLaxSSL(true);

        ExternalProxySettings eps = new ExternalProxySettings();
        eps.setProxyServer("10.10.10.10");
        eps.setProxyPort(3128);
        eps.setNTDomain("mycompany");
        eps.setUsername("flex");
        eps.setPassword("flex");
        adapter.setExternalProxySettings(eps);
    }
}