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
package flex.messaging.endpoints;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import flex.management.Manageable;
import flex.messaging.MessageBroker;
import flex.messaging.config.ConfigMap;
import flex.messaging.config.SecurityConstraint;

/**
 * An endpoint receives messages from clients and decodes them,
 * then sends them on to a MessageBroker for routing to a service.
 * The endpoint also encodes messages and delivers them to clients.
 * Endpoints are specific to a message format and network transport,
 * and are defined by the named URI path on which they are located.
 * 
 * @author neville
 */
public interface Endpoint extends Manageable
{
    /**
     * Initializes the endpoint with an ID and properties. 
     * 
     * @param id The ID of the endpoint. 
     * @param properties Properties of the endpoint. 
     */
    void initialize(String id, ConfigMap properties);
    
    /**
     * Start the endpoint. The MethodBroker invokes this
     * method in order to set the endpoint up for sending and receiving
     * messages from Flash clients.
     *
     */
    void start();
    
    /**
     * Determines whether the endpoint is started. 
     * 
     * @return <code>true</code> if the endpoint is started;  <code>false</code> otherwise. 
     */
    boolean isStarted();
    
    /**
     * Stop and destroy the endpoint. The MethodBroker invokes
     * this method in order to stop the endpoint from sending
     * and receiving messages from Flash clients.
     * 
     */
    void stop();
       
    /**
     * Retrieves the corresponding client channel type for the endpoint. 
     * 
     * @return The corresponding client channel type for the endpoint. 
     */
    String getClientType();
    
    /**
     * Sets the corresponding client channel type for the endpoint. 
     * 
     * @param clientType The corresponding client channel type for the endpoint.  
     */
    void setClientType(String clientType);
      
    /**
     * Retrieves the endpoint properties the client needs.
     * @return The endpoint properties the client needs.
     */
    ConfigMap describeEndpoint();

    /**
     * All endpoints are referenceable by an ID that is unique among
     * all the endpoints registered to a single broker instance.
     * @return The endpoint ID.
     */
    String getId();
    
    /**
     * All endpoints are referenceable by an ID that is unique among
     * all the endpoints registered to a single broker instance. The id
     * is set through this method, usually through parsed configuration.
     * <p/>
     * @param id The endpoint ID.
     */
    void setId(String id);
    
    /**
     * All endpoints must be managed by a single MessageBroker,
     * and must be capable of returning a reference to that broker.
     * This broker reference is used when the endpoint wishes to 
     * send a message to one of the broker's services.
     * 
     * @return broker The MessageBroker instance which manages this endpoint.
     */
    MessageBroker getMessageBroker();
    
    /**
     * Sets the <code>MessageBroker</code> of the endpoint. 
     * 
     * @param broker the message broker object
     */
    void setMessageBroker(MessageBroker broker);

    /**
     * Retrieves the highest messaging version currently available via this
     * endpoint.  
     * @return The messaging version number.
     */
    double getMessagingVersion();
    
    /** @exclude **/
    String getParsedUrl(String contextPath);
    
    /**
     * Retrieves the port of the URL of the endpoint. 
     * 
     * @return The port of the URL of the endpoint. 
     */
    int getPort();
       
    /**
     * Specifies whether this protocol requires the secure HTTPS protocol. 
     * @return <code>true</code> if the endpoint is a secure endpoint, <code>false</code> otherwise.
     */
    boolean isSecure();

    /**
     * Retrieves the security constraint of the endpoint. 
     * 
     * @return The security constraint of the endpoint.
     */
    SecurityConstraint getSecurityConstraint();
    
    /**
     * Sets the security constraint of the endpoint. 
     * 
     * @param constraint The security constraint of the endpoint.
     */
    void setSecurityConstraint(SecurityConstraint constraint);

    /**
     * Responds to HTTP-based messages published by a client. Endpoints which
     * do not support access over HTTP should throw an UnsupportedOperationException
     * in the implementation of htis method.
     * <p/>
     * @param req The HttpServletRequest object.
     * @param res The HttpServletResponse object.
     */
    void service(HttpServletRequest req, HttpServletResponse res);
        
    /**
     * Retrieves the URL of the endpoint. 
     * 
     * @return The URL of the endpoint. 
     */
    String getUrl();
    
    /**
     * Sets the URL of the endpoint. 
     * 
     * @param url The URL of the endpoint.
     */
    void setUrl(String url);  
    
    /**
     * @exclude
     * Returns the url of the endpoint parsed for the client. 
     *  
     * @return The url of the endpoint parsed for the client.
     */
    String getUrlForClient();
}
