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
package flex.messaging.services.messaging;

import flex.messaging.MessageClient;
import flex.messaging.MessageDestination;

import java.util.Iterator;

/**
 *
 */
public class RemoteMessageClient extends MessageClient
{
    /**
     *
     */
    private static final long serialVersionUID = -4743740983792418491L;

    /**
     * Constructor.
     * 
     * @param clientId The client id.
     * @param destination The message destination.
     * @param endpointId The endpoint id.
     */
    public RemoteMessageClient(Object clientId, MessageDestination destination, String endpointId)
    {
        super(clientId, destination, endpointId, false /* do not use session */);
    }

    /**
     * Invalidates the RemoteMessageClient.
     */
    public void invalidate()
    {
        synchronized (lock)
        {
            if (!valid)
                return;
        }

        if (destination instanceof MessageDestination)
        {
            MessageDestination msgDestination = (MessageDestination)destination;
            for (Iterator it = subscriptions.iterator(); it.hasNext(); )
            {
                SubscriptionInfo si = (SubscriptionInfo)it.next();
                msgDestination.getRemoteSubscriptionManager().removeSubscriber(clientId,
                        si.selector, si.subtopic, null);
            }
        }
    }
}
