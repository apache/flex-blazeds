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
package flex.messaging.services.messaging.adapters;

import java.util.Set;

import flex.messaging.services.MessageService;
import flex.messaging.services.messaging.SubscriptionManager;
import flex.messaging.messages.Message;
import flex.messaging.Destination;
import flex.messaging.MessageDestination;
import flex.management.runtime.messaging.services.messaging.adapters.ActionScriptAdapterControl;

/**
 * An ActionScript object based adapter for the MessageService
 * that supports simple publish/subscribe messaging between
 * ActionScript based clients.
 */
public class ActionScriptAdapter extends MessagingAdapter
{
    private ActionScriptAdapterControl controller;

    //--------------------------------------------------------------------------
    //
    // Constructor
    //
    //--------------------------------------------------------------------------
    
    /**
     * Constructs a default <code>ActionScriptAdapter</code>.
     */
    public ActionScriptAdapter()
    {
        super();
    }
    
    //--------------------------------------------------------------------------
    //
    // Public Getters and Setters for ServiceAdapter properties
    //                             
    //--------------------------------------------------------------------------

    /**
     * Casts the <code>Destination</code> into <code>MessageDestination</code>
     * and calls super.setDestination.
     * 
     * @param destination
     */
    public void setDestination(Destination destination)
    {
        Destination dest = (MessageDestination)destination;
        super.setDestination(dest);
    }
    
    //--------------------------------------------------------------------------
    //
    // Other Public APIs
    //                 
    //--------------------------------------------------------------------------
    
    /**
     * Handle a data message intended for this adapter.
     */
    public Object invoke(Message message)
    {
        MessageDestination destination = (MessageDestination)getDestination();
        MessageService msgService = (MessageService)destination.getService();

        SubscriptionManager subscriptionManager = destination.getSubscriptionManager();
        Set subscriberIds = subscriptionManager.getSubscriberIds(message, true /*evalSelector*/);
        if (subscriberIds != null && !subscriberIds.isEmpty())
        {
            /* We have already filtered based on the selector and so pass false below */
            msgService.pushMessageToClients(destination, subscriberIds, message, false);
        }
        msgService.sendPushMessageFromPeer(message, destination, true);

        return null;
    }

    /**
     * Invoked automatically to allow the <code>ActionScriptAdapter</code> to setup its corresponding
     * MBean control.
     * 
     * @param broker The <code>Destination</code> that manages this <code>ActionScriptAdapter</code>.
     */
    protected void setupAdapterControl(Destination destination)
    {
        controller = new ActionScriptAdapterControl(this, destination.getControl());
        controller.register();
        setControl(controller);
    }
}
