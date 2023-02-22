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
package flex.messaging.services;

import flex.messaging.Destination;
import flex.messaging.MessageBroker;
import flex.messaging.log.Log;
import flex.messaging.log.LogCategories;
import flex.messaging.messages.Message;
import flex.messaging.services.http.HTTPProxyDestination;

/**
 * The HttpProxyService replaces the Flex 1.5 Proxy. It decouples
 * the details of how the client contacts the message broker
 * by accepting an HTTPMessage type which can be sent over
 * any channel.
 *
 * @see flex.messaging.messages.HTTPMessage
 */
public class HTTPProxyService extends AbstractService {
    /**
     * Log category for <code>HTTPProxyService</code>.
     */
    public static final String LOG_CATEGORY = LogCategories.SERVICE_HTTP;

    //--------------------------------------------------------------------------
    //
    // Constructor
    //
    //--------------------------------------------------------------------------

    /**
     * Constructs an unmanaged <code>HTTPProxyService</code>.
     */
    public HTTPProxyService() {
        this(false);
    }

    /**
     * Constructs a <code>HTTPProxyService</code> with the indicated management.
     *
     * @param enableManagement <code>true</code> if the <code>HTTPProxyService</code>
     *                         is manageable; otherwise <code>false</code>.
     */
    public HTTPProxyService(boolean enableManagement) {
        super(enableManagement);
        Log.getLogger(getLogCategory()).error("flex.messaging.services.HTTPProxyService is no longer supported by BlazeDS");
    }

    //--------------------------------------------------------------------------
    //
    // Public Getters and Setters for HTTPProxyService properties
    //
    //--------------------------------------------------------------------------

    /**
     * Creates a <code>HTTPProxyDestination</code> instance, sets its id,
     * sets it manageable if the <code>AbstractService</code> that created it is
     * manageable, and sets its <code>Service</code> to the <code>AbstractService</code>
     * that created it.
     *
     * @param id The id of the <code>HTTPProxyDestination</code>.
     * @return The <code>Destination</code> instanced created.
     */
    @Override
    public Destination createDestination(String id) {
        HTTPProxyDestination destination = new HTTPProxyDestination();
        destination.setId(id);
        destination.setManaged(isManaged());
        destination.setService(this);

        return destination;
    }

    /**
     * Casts the <code>Destination</code> into <code>HTTPProxyDestination</code>
     * and calls super.addDestination.
     *
     * @param destination The <code>Destination</code> instance to be added.
     */
    @Override
    public void addDestination(Destination destination) {
        HTTPProxyDestination proxyDestination = (HTTPProxyDestination) destination;
        super.addDestination(proxyDestination);
    }

    //--------------------------------------------------------------------------
    //
    // Other Public APIs
    //
    //--------------------------------------------------------------------------

    /**
     * Processes messages of type <code>HTTPMessage</code> by invoking the
     * requested destination's adapter.
     *
     * @param msg The message sent by the MessageBroker.
     * @return The result of the service.
     */
    @Override
    public Object serviceMessage(Message msg) {
        ServiceException e = new ServiceException();
        e.setMessage("flex.messaging.services.HTTPProxyService is no longer supported by BlazeDS");
        throw e;
    }

    //--------------------------------------------------------------------------
    //
    // Protected/private APIs
    //
    //--------------------------------------------------------------------------

    /**
     * Returns the log category of the <code>HTTPProxyService</code>.
     *
     * @return The log category of the component.
     */
    @Override
    protected String getLogCategory() {
        return LOG_CATEGORY;
    }

    /**
     * This method is invoked to allow the <code>HTTPProxyService</code> to instantiate and register its
     * MBean control.
     *
     * @param broker The <code>MessageBroker</code> to pass to the <code>HTTPProxyServiceControl</code> constructor.
     */
    @Override
    protected void setupServiceControl(MessageBroker broker) {
    }
}
