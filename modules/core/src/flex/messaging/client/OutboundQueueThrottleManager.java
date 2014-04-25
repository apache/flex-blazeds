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
package flex.messaging.client;

import java.util.concurrent.ConcurrentHashMap;

import flex.messaging.MessageClient.SubscriptionInfo;
import flex.messaging.config.ThrottleSettings.Policy;
import flex.messaging.log.Log;
import flex.messaging.messages.Message;
import flex.messaging.services.messaging.MessageFrequency;
import flex.messaging.services.messaging.ThrottleManager;
import flex.messaging.services.messaging.ThrottleManager.ThrottleResult;
import flex.messaging.util.StringUtils;


/**
 * Used to keep track of and limit outbound message rates of a single FlexClient queue.
 * An outbound FlexClient queue can contain messages from multiple MessageClients
 * across multiple destinations. It can also contain messages for multiple
 * subscriptions (for each subtopic/selector) across the same destination for
 * the same MessageClient.
 */
public class OutboundQueueThrottleManager
{
    //--------------------------------------------------------------------------
    //
    // Constructor
    //
    //--------------------------------------------------------------------------

    /**
     * Constructs a default outbound queue throttle manager.
     *
     * @param processor The outbound queue processor that is using this throttle manager.
     */
    public OutboundQueueThrottleManager(FlexClientOutboundQueueProcessor processor)
    {
        destinationFrequencies = new ConcurrentHashMap<String, DestinationFrequency>();
        this.processor = processor;
    }

    //--------------------------------------------------------------------------
    //
    // Variables
    //
    //--------------------------------------------------------------------------

    /**
     * Map of destination id and destination message frequencies.
     */
    protected final ConcurrentHashMap<String, DestinationFrequency> destinationFrequencies;

    /**
     * The parent queue processor of the throttle manager.
     */
    protected final FlexClientOutboundQueueProcessor processor;

    //--------------------------------------------------------------------------
    //
    // Public Methods
    //
    //--------------------------------------------------------------------------

    /**
     * Registers the destination with the outbound throttle manager.
     *
     * @param destinationId The id of the destination.
     * @param outboundMaxClientFrequency The outbound max-client-frequency specified
     * at the destination.
     * @param outboundPolicy The outbound throttle policy specified at the destination.
     */
    public void registerDestination(String destinationId, int outboundMaxClientFrequency, Policy outboundPolicy)
    {
        DestinationFrequency frequency = destinationFrequencies.get(destinationId);
        if (frequency == null)
        {
            frequency = new DestinationFrequency(outboundMaxClientFrequency, outboundPolicy);
            destinationFrequencies.putIfAbsent(destinationId, frequency);
        }
    }

    /**
     * Registers the subscription of a client talking to a destination with the
     * specified subscription info.
     *
     * @param destinationId The destination id.
     * @param si The subscription information.
     */
    public void registerSubscription(String destinationId, SubscriptionInfo si)
    {
        DestinationFrequency frequency = destinationFrequencies.get(destinationId);
        frequency.logMaxFrequencyDuringRegistration(frequency.outboundMaxClientFrequency, si);
    }

    /**
     * Unregisters the subscription.
     *
     * @param destinationId The destination id.
     * @param si The subscription information.
     */
    public void unregisterSubscription(String destinationId, SubscriptionInfo si)
    {
        unregisterDestination(destinationId);
    }

    /**
     * Unregisters all subscriptions of the client under the specified destination.
     *
     * @param destinationId The destination id.
     */
    public void unregisterAllSubscriptions(String destinationId)
    {
        unregisterDestination(destinationId);
    }

    /**
     * Attempts to throttle the outgoing message.
     *
     * @param message The message to consider to throttle.
     * @return True if the message was throttled; otherwise false.
     */
    public ThrottleResult throttleOutgoingClientLevel(Message message)
    {
        String destinationId = message.getDestination();
        if (isDestinationRegistered(destinationId))
        {
            DestinationFrequency frequency = destinationFrequencies.get(message.getDestination());
            int maxFrequency = frequency.getMaxFrequency(message); // Limit to check against.
            MessageFrequency messageFrequency = frequency.getMessageFrequency(message); // Message rate of the client.
            if (messageFrequency != null)
            {
                ThrottleResult result = messageFrequency.checkLimit(maxFrequency, frequency.outboundPolicy);
                return result;
            }
        }
        return new ThrottleResult(); // Otherwise, return OK result.
    }

    /**
     * Updates the outgoing client level message frequency of the message.
     *
     * @param message The message.
     */
    public void updateMessageFrequencyOutgoingClientLevel(Message message)
    {
        String destinationId = message.getDestination();
        if (isDestinationRegistered(destinationId))
        {
            DestinationFrequency frequency = destinationFrequencies.get(message.getDestination());
            MessageFrequency messageFrequency = frequency.getMessageFrequency(message);
            if (messageFrequency != null)
                messageFrequency.updateMessageFrequency();
        }
    }

    //--------------------------------------------------------------------------
    //
    // Protected Methods
    //
    //--------------------------------------------------------------------------

    /**
     * Determines whether the destination has been registered or not.
     *
     * @param destinationId The destination id.
     * @return True if the destination with the specified id has been registered.
     */
    protected boolean isDestinationRegistered(String destinationId)
    {
        return destinationFrequencies.containsKey(destinationId);
    }

    /**
     * Unregisters the destination from the outbound throttle manager.
     *
     * @param destinationId The id of the destination.
     */
    protected void unregisterDestination(String destinationId)
    {
        if (isDestinationRegistered(destinationId))
            destinationFrequencies.remove(destinationId);
    }

    //--------------------------------------------------------------------------
    //
    // Inner Classes
    //
    //--------------------------------------------------------------------------

    /**
     * Used to keep track of max-client-frequency and outgoing throttle policy
     * specified at the destination. It also keeps track of outbound message
     * rates of all MessageClient subscriptions across the destination.
     */
    class DestinationFrequency
    {
        protected final int outboundMaxClientFrequency; // destination specified client limit.
        protected final MessageFrequency outboundClientFrequency;
        protected final Policy outboundPolicy; // destination specified policy.

        /**
         * Default constructor.
         *
         * @param outboundMaxClientFrequency The outbound throttling max-client-frequency of the destination.
         * @param outboundPolicy The outbound throttling policy of the destination.
         */
        DestinationFrequency(int outboundMaxClientFrequency, Policy outboundPolicy)
        {
            this.outboundMaxClientFrequency = outboundMaxClientFrequency;
            this.outboundPolicy = outboundPolicy;
            outboundClientFrequency = new MessageFrequency(outboundMaxClientFrequency);
        }

        /**
         * Returns the max-client-frequency for the subscription the message is
         * intended for (which is simply the max-client-frequency specified at 
         * the destination).
         *
         * @param message The message.
         *
         * @return The max-frequency for the subscription.
         */
        int getMaxFrequency(Message message)
        {
            return outboundMaxClientFrequency;
        }

        /**
         * Given a subscription the message is intended to, returns the message
         * rate frequency for that subscription.
         *
         * @param message The message.
         * @return The message frequency for the subscription, if it exists; otherwise null.
         */
        MessageFrequency getMessageFrequency(Message message)
        {
            return outboundClientFrequency;
        }

        /**
         * Utility function to log the maxFrequency being used during subscription.
         *
         * @param maxFrequency The maxFrequency to log.
         */
        void logMaxFrequencyDuringRegistration(int maxFrequency, SubscriptionInfo si)
        {
            if (Log.isDebug())
                Log.getLogger(ThrottleManager.LOG_CATEGORY).debug("Outbound queue throttle manager for FlexClient '"
                        + processor.getFlexClient().getId() + "' is using '" + maxFrequency
                        + "' as the throttling limit for its subscription: "
                        +  StringUtils.NEWLINE + si);
        }
    }
}