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
package features.messaging.customadapter;

import java.util.HashSet;
import java.util.Set;

import flex.messaging.MessageClient;
import flex.messaging.MessageClientListener;
import flex.messaging.messages.CommandMessage;
import flex.messaging.services.messaging.adapters.ActionScriptAdapter;

/**
 * A sample custom adapter that keeps track its own subscriptions.
 */
public class CustomActionscriptAdapter extends ActionScriptAdapter implements MessageClientListener
{
    /**
     * Set of subscriptions (clientIds).
     */
    private Set<String> clientIds = new HashSet<String>();

    /**
     * Default constructor adds itself as the MessageClient created listener so
     * it can get creation notifications as Consumers subscribe (and MessageClients
     * get created for them on the server).
     */
    public CustomActionscriptAdapter()
    {
        MessageClient.addMessageClientCreatedListener(this);
    }

    /**
     * Manage method is called when Consumer subscribes/unsubscribes.
     * Override to update the clientIds set.
     */
    @Override public Object manage(CommandMessage commandMessage)
    {
        int operation = commandMessage.getOperation();
        String clientId = (String)commandMessage.getClientId();
        switch (operation)
        {
            case CommandMessage.SUBSCRIBE_OPERATION:
                clientIds.add(clientId);
                break;
            case CommandMessage.UNSUBSCRIBE_OPERATION:
                clientIds.remove(clientId);
                break;
            default:
                break;
        }
        return super.manage(commandMessage);
    }

    /**
     * Return true, so manage method gets called as Consumer subscribes.
     */
    public boolean handlesSubscriptions()
    {
        return true;
    }

    /**
     * Implements {@link MessageClientListener#messageClientCreated(MessageClient)}
     */
    public void messageClientCreated(MessageClient messageClient)
    {
        // Add the adapter as MessageClient destroyed listener, so it can get
        // destruction notifications as the MessageClient gets destroyed due to
        // Consumer unsubscribe, disconnect, or session invalidation.
        messageClient.addMessageClientDestroyedListener(this);
    }

    /**
     * Implements {@link MessageClientListener#messageClientDestroyed(MessageClient)}
     */
    public void messageClientDestroyed(MessageClient messageClient)
    {
        String clientId = (String)messageClient.getClientId();
        clientIds.remove(clientId);
    }
}
