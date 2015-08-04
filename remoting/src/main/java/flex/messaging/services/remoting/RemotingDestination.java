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
package flex.messaging.services.remoting;

import flex.management.runtime.messaging.services.remoting.RemotingDestinationControl;
import flex.messaging.FactoryDestination;
import flex.messaging.MessageBroker;
import flex.messaging.MessageException;
import flex.messaging.util.MethodMatcher;
import flex.messaging.log.LogCategories;
import flex.messaging.services.RemotingService;
import flex.messaging.services.Service;

/**
 * A logical reference to a RemotingDestination.
 */
public class RemotingDestination extends FactoryDestination
{
    static final long serialVersionUID = -8454338922948146048L;
    /** Log category for <code>RemotingDestination</code>. */
    public static final String LOG_CATEGORY = LogCategories.SERVICE_REMOTING;

    private static final String REMOTING_SERVICE_CLASS = "flex.messaging.services.RemotingService";

    // errors
    public static final int NO_MESSAGE_BROKER = 10163;
    private static final int NO_REMOTING_SERVICE = 10657;

    // RemotingDestination internal
    private MethodMatcher methodMatcher;

    private RemotingDestinationControl controller;

    //--------------------------------------------------------------------------
    //
    // Constructor
    //
    //--------------------------------------------------------------------------

    /**
     * Constructs an unmanaged <code>RemotingDestination</code> instance.
     */
    public RemotingDestination()
    {
        this(false);
    }

    /**
     * Constructs a <code>RemotingDestination</code> with the indicated management.
     *
     * @param enableManagement <code>true</code> if the <code>RemotingDestination</code>
     * is manageable; otherwise <code>false</code>.
     */
    public RemotingDestination(boolean enableManagement)
    {
        super(enableManagement);
    }

    /**
     * Retrieves the RemotingDestination for the supplied server id.  If serverId
     * is null, the default MessageBroker instance is returned.  You use this
     * version of this method to retrieve a DataDestination if you are not in the
     * context of processing a current message when you need the RemotingDestination.
     *
     * @param serverId the id of the server containing the remoting destination to be retrieved.
     * @param destinationName the name of the remoting destination to be retrieved.
     *
     * @return remoting destination corresponding to the supplied server id and destination name
     */
    public static RemotingDestination getRemotingDestination(String serverId, String destinationName)
    {
        MessageBroker broker = MessageBroker.getMessageBroker(serverId);

        if (broker == null)
        {
            // Unable to locate a MessageBroker initialized with server id ''{0}''
            MessageException me = new MessageException();
            me.setMessage(NO_MESSAGE_BROKER, new Object[] { serverId });
            throw me;
        }

        RemotingService rs = (RemotingService) broker.getServiceByType(REMOTING_SERVICE_CLASS);
        if (rs == null)
        {
            // MessageBroker with server id ''{0}'' does not contain a service with class flex.messaging.remoting.RemotingService
            MessageException me = new MessageException();
            me.setMessage(NO_REMOTING_SERVICE, new Object[] { serverId });
            throw me;
        }

        return (RemotingDestination) rs.getDestination(destinationName);
    }


    //--------------------------------------------------------------------------
    //
    // Public Getters and Setters for Destination properties
    //
    //--------------------------------------------------------------------------

    /**
     * Returns the log category of the <code>RemotingDestination</code>.
     *
     * @return The log category of the component.
     */
    public String getLogCategory()
    {
        return LOG_CATEGORY;
    }

    /**
     * Casts the <code>Service</code> into <code>RemotingService</code>
     * and calls super.setService.
     *
     * @param service the <code>RemotingService</code> to associate with this destination.
     */
    public void setService(Service service)
    {
        RemotingService remotingService = (RemotingService)service;
        super.setService(remotingService);
        setMethodMatcher(remotingService.getMethodMatcher());
    }

    //--------------------------------------------------------------------------
    //
    // Other public APIs
    //
    //--------------------------------------------------------------------------
    /**
     *
     */
    public MethodMatcher getMethodMatcher()
    {
        return methodMatcher;
    }

    /**
     *
     */
    public void setMethodMatcher(MethodMatcher matcher)
    {
        methodMatcher = matcher;
    }

    //--------------------------------------------------------------------------
    //
    // Protected/private APIs
    //
    //--------------------------------------------------------------------------

    /**
     * Invoked automatically to allow the <code>RemotingDestination</code> to setup its corresponding
     * MBean control.
     *
     * @param service The <code>Service</code> that manages this <code>RemotingDestination</code>.
     */
    protected void setupDestinationControl(Service service)
    {
        controller = new RemotingDestinationControl(this, service.getControl());
        controller.register();
        setControl(controller);
    }
}
