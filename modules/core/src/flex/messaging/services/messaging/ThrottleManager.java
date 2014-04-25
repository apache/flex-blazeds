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

import java.util.HashMap;
import java.util.Map;

import flex.management.ManageableComponent;
import flex.management.runtime.messaging.services.messaging.ThrottleManagerControl;
import flex.messaging.MessageException;
import flex.messaging.config.ConfigurationException;
import flex.messaging.config.ThrottleSettings;
import flex.messaging.config.ThrottleSettings.Policy;
import flex.messaging.log.Log;
import flex.messaging.log.LogCategories;
import flex.messaging.messages.Message;
import flex.messaging.services.messaging.ThrottleManager.ThrottleResult.Result;

/**
 * @exclude
 *
 * The ThrottleManager provides functionality to limit the frequency of messages
 * routed through the system in message/second terms. Message frequency can be managed
 * on a per-client basis and also on a per-destination basis by tweaking different
 * parameters. Each MessageDestination has one ThrottleManager.
 *
 * Message frequency can be throttled differently for incoming messages, which are messages
 * published by Flash/Flex producers, and for outgoing messages, which are messages
 * consumed by Flash/Flex subscribers that may have been produced by either Flash clients
 * or external message producers (such as data feeds, JMS publishers, etc).
 *
 */
public class ThrottleManager extends ManageableComponent
{
    //--------------------------------------------------------------------------
    //
    // Public Static Constants
    //
    //--------------------------------------------------------------------------

    public static final String LOG_CATEGORY = LogCategories.TRANSPORT_THROTTLE;
    public static final String TYPE = "ThrottleManager";

    //--------------------------------------------------------------------------
    //
    // Private Static Constants
    //
    //--------------------------------------------------------------------------

    private static final Object classMutex = new Object();

    //--------------------------------------------------------------------------
    //
    // Private Static Variables
    //
    //--------------------------------------------------------------------------

    private static int instanceCount = 0;

    //--------------------------------------------------------------------------
    //
    // Constructor
    //
    //--------------------------------------------------------------------------

    /**
     * Constructs an unmanaged <code>ThrottleManager</code> instance.
     */
    public ThrottleManager()
    {
        this(false);
    }

    /**
     * Constructs a <code>ThrottleManager</code> with the indicated management.
     *
     * @param enableManagement <code>true</code> if the <code>ThrottleManager</code>
     * is manageable; otherwise <code>false</code>.
     */
    public ThrottleManager(boolean enableManagement)
    {
        super(enableManagement);
        synchronized (classMutex)
        {
            super.setId(TYPE + ++instanceCount);
        }
    }

    //--------------------------------------------------------------------------
    //
    // Variables
    //
    //--------------------------------------------------------------------------

    protected ThrottleSettings settings;
    private Map<String, MessageFrequency> inboundClientMarks;
    private MessageFrequency inboundDestinationMark;
    private MessageFrequency outboundDestinationMark;

    //--------------------------------------------------------------------------
    //
    // Initialize, validate, start, and stop methods.
    //
    //--------------------------------------------------------------------------

    /**
     * Starts the throttle manager.
     */
    @Override
    public void start()
    {
        // Use the default ThrottleSettings if one is not set already.
        if (settings == null)
            settings = new ThrottleSettings();

        if (settings.isDestinationThrottleEnabled())
        {
            inboundDestinationMark = new MessageFrequency(settings.getIncomingDestinationFrequency());
            outboundDestinationMark = new MessageFrequency(settings.getOutgoingDestinationFrequency());
        }

        if (settings.isInboundClientThrottleEnabled())
            inboundClientMarks = new HashMap<String, MessageFrequency>();
    }


    /**
     * Stops the throttle manager.
     */
    @Override
    public void stop()
    {
        super.stop();

        // Remove management.
        if (isManaged() && getControl() != null)
        {
            getControl().unregister();
            setControl(null);
            setManaged(false);
        }
    }

    //--------------------------------------------------------------------------
    //
    // Public Methods
    //
    //--------------------------------------------------------------------------

    /**
     * Given a policy, returns the result for that policy.
     *
     * @param policy The policy.
     * @return The result for the policy.
     */
    public static Result getResult(Policy policy)
    {
        if (Policy.IGNORE == policy)
            return Result.IGNORE;
        else if (Policy.ERROR == policy)
            return Result.ERROR;
        else if (Policy.BUFFER == policy)
            return Result.BUFFER;
        else if (Policy.CONFLATE == policy)
            return Result.CONFLATE;
        return Result.OK;
    }

    /**
     * Returns the outbound policy being used by the throttle manager.
     *
     * @return The outbound policy for the throttle manager.
     */
    public Policy getOutboundPolicy()
    {
        return settings == null? null : settings.getOutboundPolicy();
    }


    /**
     * This is a no-op because throttle manager's id is generated internally.
     *
     * @param id The id.
     */
    @Override
    public void setId(String id)
    {
        // No-op
    }

    /**
     * @exclude
     * Used by the MessageClient in its cleanup process.
     *
     * @param clientId The id of the MessageClient.
     */
    public void removeClientThrottleMark(Object clientId)
    {
        if (inboundClientMarks != null)
            inboundClientMarks.remove(clientId);
        // Note that the outBoundClientMarks that is maintained by the FlexClientOutboundQueueProcessor
        // is cleaned up by FlexClient when MessageClient is unregistered with it.
    }

    /**
     * Sets the throttling settings of the throttle manager.
     *
     * @param throttleSettings The throttling settings for the throttle manager.
     */
    public void setThrottleSettings(ThrottleSettings throttleSettings)
    {
        // Make sure that we have valid outbound policies.
        Policy outPolicy = throttleSettings.getOutboundPolicy();
        if (outPolicy != Policy.NONE && outPolicy != Policy.IGNORE)
        {
            ConfigurationException ex = new ConfigurationException();
            ex.setMessage("Invalid outbound throttle policy '" + outPolicy
                    + "' for destination '" + throttleSettings.getDestinationName()
                    + "'. Valid values are 'NONE' and 'IGNORE'.");
            throw ex;
        }
        settings = throttleSettings;
    }

    /**
     * Attempts to throttle the incoming message at the destination and the client level.
     *
     * @param message Message to be throttled.
     * @return True if the message was throttled; otherwise false.
     */
    public boolean throttleIncomingMessage(Message message)
    {
        // destination-level throttling comes before client-level, because if it
        // fails then it doesn't matter what the client-level throttle reports.
        ThrottleResult throttleResult = throttleDestinationLevel(message, true);
        if (throttleResult.getResult() == Result.OK)
        {
            // client-level throttling allows the system to further refine a
            // different throttle for individual clients, which may be a subset
            // but never a superset of destination-level throttle settings
            throttleResult = throttleIncomingClientLevel(message);
            handleIncomingThrottleResult(message, throttleResult, true /*isClientLevel*/);
            boolean throttled = throttleResult.getResult() != Result.OK;
            if (!throttled)
            {
                updateMessageFrequencyDestinationLevel(true /* incoming */);
                updateMessageFrequencyIncomingClientLevel(message);
            }
            return throttled;
        }

        handleIncomingThrottleResult(message, throttleResult, false /*isClientLevel*/);
        boolean throttled = throttleResult.getResult() != Result.OK;
        if (!throttled)
        {
            updateMessageFrequencyDestinationLevel(true /* incoming */);
            updateMessageFrequencyIncomingClientLevel(message);
        }
        return throttled;
    }

    /**
     * Attempts to throttle the outgoing message at the destination level only.
     * Client level throttling is enforced at FlexClientOutboundQueueProcessor.
     *
     * @param message The message to be throttled.
     * @return The result of throttling attempt.
     */
    public ThrottleResult throttleOutgoingMessage(Message message)
    {
        ThrottleResult throttleResult = throttleDestinationLevel(message, false);
        // Outbound client-level throttling happens in FlexClientOutboundQueueProcessor.
        handleOutgoingThrottleResult(message, throttleResult, false /*isClientLevel*/);
        return throttleResult;
    }

    /**
     * A utility method to handle outgoing throttling results in a common way.
     *
     * @param message The message that is being throttled.
     * @param throttleResult The throttling result.
     * @param isClientLevel Whether the message is being throttled at the client level
     * or not.
     */
    public void handleOutgoingThrottleResult(Message message, ThrottleResult throttleResult, boolean isClientLevel)
    {
        Result result = throttleResult.getResult();

        // Update the management metrics.
        if (result != Result.OK && isManaged())
        {
            if (isClientLevel)
                ((ThrottleManagerControl)getControl()).incrementClientOutgoingMessageThrottleCount();
            else
                ((ThrottleManagerControl)getControl()).incrementDestinationOutgoingMessageThrottleCount();
        }

        // Result can only be IGNORE (or NONE which means no throttling)
        if (result == Result.IGNORE)
        {
            // Improve the detail message for IGNORE.
            if (isClientLevel)
                throttleResult.setDetail("Message '" + message.getMessageId() + "' ignored: Too many messages sent to client '"
                        + message.getClientId() + "' in too small of a time interval " + throttleResult.getDetail());
            else
                throttleResult.setDetail("Message '" + message.getMessageId() + "' throttled: Too many messages routed by destination '"
                        + message.getDestination() + "' in too small of a time interval " + throttleResult.getDetail());

            if (Log.isInfo())
                Log.getLogger(LOG_CATEGORY).info(throttleResult.getDetail());
        }
    }

    /**
     * Attempts to throttle destination-level incoming and outgoing messages.
     *
     * @param message Message to throttle.
     * @param incoming Whether the message is incoming or outgoing.
     * @return The result of the throttling attempt.
     */
    public ThrottleResult throttleDestinationLevel(Message message, boolean incoming)
    {
        if (incoming && settings.isInboundDestinationThrottleEnabled())
        {
            ThrottleResult result = inboundDestinationMark.checkLimit(settings.getIncomingDestinationFrequency(), settings.getInboundPolicy());
            return result;
        }
        else if (!incoming && settings.isOutboundDestinationThrottleEnabled())
        {
            ThrottleResult result = outboundDestinationMark.checkLimit(settings.getOutgoingDestinationFrequency(), settings.getOutboundPolicy());
            return result;
        }
        // Return the default OK result.
        return new ThrottleResult();
    }

    /**
     * Updates the destination level message frequency.
     *
     * param incoming Whether the message is incoming or outgoing.
     */
    public void updateMessageFrequencyDestinationLevel(boolean incoming)
    {
        if (incoming && settings.isInboundDestinationThrottleEnabled())
            inboundDestinationMark.updateMessageFrequency();
        else if (!incoming && settings.isOutboundDestinationThrottleEnabled())
            outboundDestinationMark.updateMessageFrequency();
    }

    /**
     * Updates the incoming client level message frequency.
     */
    public void updateMessageFrequencyIncomingClientLevel(Message message)
    {
        String clientId = (String)message.getClientId();
        if (settings.isInboundClientThrottleEnabled())
        {
            MessageFrequency clientLevelMark = inboundClientMarks.get(clientId);
            if (clientLevelMark != null)
                clientLevelMark.updateMessageFrequency();
        }
    }

    //--------------------------------------------------------------------------
    //
    // Protected and private methods.
    //
    //--------------------------------------------------------------------------

    /**
     * Returns the log category for the throttle manager.
     */
    @Override
    protected String getLogCategory()
    {
        return LOG_CATEGORY;
    }

    /**
     * A utility method to handle incoming throttling results in a common way.
     *
     * @param message The message that is being throttled.
     * @param throttleResult The throttling result.
     * @param isClientLevel Whether the message is being throttled at the client level
     * or not.
     */
    protected void handleIncomingThrottleResult(Message message, ThrottleResult throttleResult, boolean isClientLevel)
    {
        Result result = throttleResult.getResult();

        // Update the management metrics.
        if (result != Result.OK && isManaged())
        {
            if (isClientLevel)
                ((ThrottleManagerControl)getControl()).incrementClientIncomingMessageThrottleCount();
            else
                ((ThrottleManagerControl)getControl()).incrementDestinationIncomingMessageThrottleCount();
        }

        // Result can be IGNORE or ERROR (or NONE which means no throttling).
        if (result == Result.IGNORE || result == Result.ERROR)
        {
            if (isClientLevel)
                throttleResult.setDetail("Message '" + message.getMessageId() + "' throttled: Too many messages sent by the client '"
                        + message.getClientId() + "' in too small of a time interval " + throttleResult.getDetail());
            else
                throttleResult.setDetail("Message '" + message.getMessageId() + "' throttled: Too many messages sent to destination '"
                    + message.getDestination() + "' in too small of a time interval " + throttleResult.getDetail());

            String detail = throttleResult.getDetail();
            if (result == Result.ERROR)
            {
                if (Log.isError())
                    Log.getLogger(LOG_CATEGORY).error(detail);
                // And, throw an exception, so the client gets the error.
                MessageException me = new MessageException(detail);
                throw me;
            }
            // Otherwise, just log it.
            if (Log.isInfo())
                Log.getLogger(LOG_CATEGORY).info(detail);
        }
    }

    /**
     * Attempts to throttle client-level incoming messages only. Client-level
     * outgoing messages are throttled at the FlexClientOutboundQueueProcessor.
     *
     * @param message Message to throttle.
     * @return The result of the throttling attempt.
     */
    protected ThrottleResult throttleIncomingClientLevel(Message message)
    {
        String clientId = (String)message.getClientId();
        if (settings.isInboundClientThrottleEnabled())
        {
            MessageFrequency clientLevelMark;
            clientLevelMark = inboundClientMarks.get(clientId);
            if (clientLevelMark == null)
                clientLevelMark = new MessageFrequency(settings.getIncomingClientFrequency());

            ThrottleResult result = clientLevelMark.checkLimit(settings.getIncomingClientFrequency(), settings.getInboundPolicy());
            inboundClientMarks.put(clientId, clientLevelMark);
            return result;
        }
        // Return the default OK result.
        return new ThrottleResult();
    }

    //--------------------------------------------------------------------------
    //
    // Nested Classes
    //
    //--------------------------------------------------------------------------

    /**
     * This class is used to keep track of throttling results.
     */
    public static class ThrottleResult
    {
        /**
         * Result enum.
         */
        public enum Result
        {
            OK, IGNORE, ERROR, BUFFER, CONFLATE
        };

        private String detail;
        private Result result;

        /**
         * Creates a ThrottleResult with Result.OK.
         */
        public ThrottleResult()
        {
            this(Result.OK);
        }

        /**
         * Creates a ThrottleResult with the passed in Result.
         *
         * @param result The Result.
         */
        public ThrottleResult(Result result) // FIXME
        {
            this.result = result;
        }

        /**
         * Creates a ThrottleResult with the passed in Result and detail.
         *
         * @param result The Result.
         * @param detail The detail.
         */
        public ThrottleResult(Result result, String detail) // FIXME
        {
            this(result);
            this.detail = detail;
        }

        /**
         * Returns the detail.
         *
         * @return The detail.
         */
        public String getDetail()
        {
            return detail;
        }

        /**
         * Sets the detail.
         *
         * @param detail The detail.
         */
        public void setDetail(String detail)
        {
            this.detail = detail;
        }

        /**
         * Returns the result.
         *
         * @return The result.
         */
        public Result getResult()
        {
            return result;
        }

        /**
         * Sets the result.
         *
         * @param result The result.
         */
        public void setResult(Result result)
        {
            this.result = result;
        }
    }
}