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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import flex.messaging.Destination;
import flex.messaging.MessageClient;
import flex.messaging.MessageDestination;
import flex.messaging.config.ConfigMap;
import flex.messaging.messages.Message;
import flex.messaging.services.messaging.ThrottleManager;
import flex.messaging.services.messaging.ThrottleManager.ThrottleResult;
import flex.messaging.services.messaging.ThrottleManager.ThrottleResult.Result;

/**
 * The base FlexClientOutboundQueueProcessor implementation used if a custom implementation is not
 * specified. Its behavior is very simple. It adds all new messages in order to the tail
 * of the outbound queue and flushes all queued messages to the network as quickly as possible.
 * It also handles the outbound client-level throttling specified at the destination level.
 */
public class FlexClientOutboundQueueProcessor
{
    //--------------------------------------------------------------------------
    //
    // Variables
    //
    //--------------------------------------------------------------------------

    /**
     * The associated FlexClient.
     */
    private FlexClient client;

    /**
     * The last MessageClient messages were flushed to. This is mainly for faster
     * lookup.
     */
    private MessageClient lastMessageClient;

    /**
     * The associated endpoint's Id.
     */
    private String endpointId;

    /**
     * Manages throttling of outbound client level messages.
     */
    protected OutboundQueueThrottleManager outboundQueueThrottleManager;

    //--------------------------------------------------------------------------
    //
    // Public Methods
    //
    //--------------------------------------------------------------------------

    /**
     *
     * Stores the Id for the outbound queue's endpoint.
     *
     * @param value The Id for the outbound queue's endpoint.
     */
    public void setEndpointId(String value)
    {
        endpointId = value;
    }

    /**
     * Returns the Id for the outbound queue's endpoint.
     *
     * @return The Id for the outbound queue's endpoint.
     */
    public String getEndpointId()
    {
        return endpointId;
    }

    /**
     *
     * Sets the associated FlexClient.
     *
     * @param value The associated FlexClient.
     */
    public void setFlexClient(FlexClient value)
    {
        client = value;
    }

    /**
     * Returns the associated FlexClient.
     *
     * @return The associated FlexClient.
     */
    public FlexClient getFlexClient()
    {
        return client;
    }

    /**
     * Returns the outbound queue throttle manager, or null if one does not exist.
     *
     * @return The outbound queue throttle manager.
     */
    public OutboundQueueThrottleManager getOutboundQueueThrottleManager()
    {
       return outboundQueueThrottleManager;
    }

    /**
     * Utility method to initialize (if necessary) and return an outbound queue
     * throttle manager.
     *
     * @return The outbound queue throttle manager.
     */
    public OutboundQueueThrottleManager getOrCreateOutboundQueueThrottleManager()
    {
        if (outboundQueueThrottleManager == null)
            outboundQueueThrottleManager = new OutboundQueueThrottleManager(this);
        return outboundQueueThrottleManager;
    }

    /**
     * No-op; this default implementation doesn't require custom initialization.
     * Subclasses may override to process any custom initialization properties that have been
     * defined in the server configuration.
     *
     * @param properties A ConfigMap containing any custom initialization properties.
     */
    public void initialize(ConfigMap properties) {}

    /**
     * Always adds a new message to the tail of the queue.
     *
     * @param outboundQueue The queue of outbound messages.
     * @param message The new message to add to the queue.
     */
    public void add(List<Message> outboundQueue, Message message)
    {
        outboundQueue.add(message);
    }

    /**
     * Always empties the queue and returns all messages to be sent to the client.
     *
     * @param outboundQueue The queue of outbound messages.
     * @return A FlushResult containing the messages that have been removed from the outbound queue
     *         to be written to the network and a wait time for the next flush of the outbound queue
     *         that is the default for the underlying Channel/Endpoint.
     */
    public FlushResult flush(List<Message> outboundQueue)
    {
        return flush(null /* no client distinction */, outboundQueue);
    }

    /**
     * Removes all messages in the queue targeted to this specific MessageClient subscription(s) and
     * returns them to be sent to the client.
     * Overrides should be careful to only return messages for the specified MessageClient.
     *
     * @param messageClient The specific MessageClient to return messages for.
     * @param outboundQueue The queue of outbound messages.
     * @return A FlushResult containing the messages that have been removed from the outbound queue
     *         to be written to the network for this MessageClient.
     */
    public FlushResult flush(MessageClient messageClient, List<Message> outboundQueue)
    {
        FlushResult flushResult = new FlushResult();
        List<Message> messagesToFlush = null;

        for (Iterator<Message> iter = outboundQueue.iterator(); iter.hasNext();)
        {
            Message message = iter.next();
            if (messageClient == null || (message.getClientId().equals(messageClient.getClientId())))
            {
                if (isMessageExpired(message)) // Don't flush expired messages.
                {
                    iter.remove();
                    continue;
                }

                // If no message client was explicitly provided, get the message client from
                // the current message.
                MessageClient messageClientForCurrentMessage = messageClient == null ?
                        getMessageClient(message) : messageClient;

                // First, apply the destination level outbound throttling.
                ThrottleResult throttleResult =
                        throttleOutgoingDestinationLevel(messageClientForCurrentMessage, message, false);
                Result result = throttleResult.getResult();

                // No destination level throttling; check destination-client level throttling.
                if (Result.OK == result)
                {
                    throttleResult = throttleOutgoingClientLevel(messageClientForCurrentMessage, message, false);
                    result = throttleResult.getResult();
                    // If no throttling, simply add the message to the list.
                    if (Result.OK == result)
                    {
                        updateMessageFrequencyOutgoing(messageClientForCurrentMessage, message);
                        if (messagesToFlush == null)
                            messagesToFlush = new ArrayList<Message>();
                        messagesToFlush.add(message);
                    }
                    // In rest of the policies (which is NONE), simply don't
                    // add the message to the list.
                }
                iter.remove();
            }
        }

        flushResult.setMessages(messagesToFlush);
        return flushResult;
    }

    /**
     * Utility method to test whether a message has expired or not.
     * Messages with a timeToLive value that is shorter than the timespan from the message's
     * timestamp up to the current system time will cause this method to return true.
     * If there are expired messages in the outbound queue, flush implementations
     * should use this helper method to only process and return messages that have
     * not yet expired.
     *
     * @param message The message to test for expiration.
     *
     * @return true if the message has a timeToLive value that has expired; otherwise false.
     */
    public boolean isMessageExpired(Message message)
    {
        return (message.getTimeToLive() > 0 &&
                (System.currentTimeMillis() - message.getTimestamp()) >= message.getTimeToLive());
    }

    /**
     * Attempts to throttle the outgoing message at the destination level.
     *
     * @param msgClient The client the message is intended for.
     * @param message The message to consider to throttle.
     * @param buffered Whether the message has already been buffered. In that case,
     * parts of regular throttling code is skipped.
     * @return The result of throttling attempt.
     */
    protected ThrottleResult throttleOutgoingDestinationLevel(
            MessageClient msgClient, Message message, boolean buffered)
    {
        ThrottleManager throttleManager = getThrottleManager(msgClient);
        if (throttleManager != null)
        {
            // In already buffered messages, don't use ThrottleManager#throttleOutgoingMessage
            // to avoid regular throttling handling as the message has already been buffered.
            if (buffered)
                return throttleManager.throttleDestinationLevel(message, false /*incoming*/);

            // Otherwise, regular throttling.
            return throttleManager.throttleOutgoingMessage(message);
        }
        return new ThrottleResult(); // Otherwise, return OK result.
    }

    /**
     * Attempts to throttle the outgoing message at the destination-client level.
     *
     * @param msgClient The client the message is intended for.
     * @param message The message to consider to throttle.
     * @param buffered Whether the message has already been buffered. In that case,
     * parts of regular throttling code is skipped.
     * @return The result of throttling attempt.
     */
    protected ThrottleResult throttleOutgoingClientLevel(MessageClient msgClient, Message message, boolean buffered)
    {
        if (outboundQueueThrottleManager != null) // Means client level throttling enabled.
        {
            ThrottleResult throttleResult = outboundQueueThrottleManager.throttleOutgoingClientLevel(message);
            if (!buffered)
            {
                ThrottleManager throttleManager = getThrottleManager(msgClient);
                if (throttleManager != null)
                    throttleManager.handleOutgoingThrottleResult(message, throttleResult, true /*isClientLevel*/);
            }
            return throttleResult;
        }
        return new ThrottleResult(); // Otherwise, return OK result.
    }

    /**
     * Returns the message client that the message is intended to.
     *
     * @param message The message.
     * @return The message client that the message is intended to.
     */
    protected MessageClient getMessageClient(Message message)
    {
        // First try using the cached message client.
        if (lastMessageClient != null && message.getClientId().equals(lastMessageClient.getClientId()))
        {
            return lastMessageClient;
        }
        else // Go ahead with the lookup.
        {
            lastMessageClient = client.getMessageClient((String)message.getClientId());
            return lastMessageClient;
        } 
    }

    /**
     * Returns the throttle manager associated with the destination the message
     * is intended to.
     *
     * @param msgClient The message client; it can be null.
     * @return The throttle manager.
     */
    protected ThrottleManager getThrottleManager(MessageClient msgClient)
    {
        Destination destination = msgClient != null? msgClient.getDestination() : null;
        return (destination != null && destination instanceof MessageDestination)? 
                ((MessageDestination)destination).getThrottleManager() : null;
    }

    /**
     * Updates the outgoing message's message frequency.
     *
     * @param msgClient The MessageClient that might have been passed to the flush; it can be null.
     * @param message The message.
     */
    protected void updateMessageFrequencyOutgoing(MessageClient msgClient, Message message)
    {
        // Update the destination level message frequency.
        ThrottleManager throttleManager = getThrottleManager(msgClient);
        if (throttleManager != null)
            throttleManager.updateMessageFrequencyDestinationLevel(false /*incoming*/);

        // Update the client level message frequency.
        if (outboundQueueThrottleManager != null)
            outboundQueueThrottleManager.updateMessageFrequencyOutgoingClientLevel(message);
    }
}
