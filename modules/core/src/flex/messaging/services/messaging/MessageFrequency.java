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

import flex.messaging.config.ThrottleSettings.Policy;
import flex.messaging.services.messaging.ThrottleManager.ThrottleResult;
import flex.messaging.services.messaging.ThrottleManager.ThrottleResult.Result;

/**
 * This class is used by ThrottleManager and FlexClientOutboundQueueProcessor
 * to keep track of inbound and outbound message rates per destination and
 * per client-subscription.
 */
public class MessageFrequency
{
    public final int messageHistorySize;
    private int messageCount;
    private long [] previousMessageTimes;

    /**
     * Creates a new MessageFrequency with the specified id.
     *
     * @param messageHistorySize The number of messages to use in calculating message rates.
     */
    public MessageFrequency(int messageHistorySize)
    {
        this.messageHistorySize = messageHistorySize;
        messageCount = 0;
        previousMessageTimes = new long[messageHistorySize];
    }

    /**
     * Given the current time and a maximum frequency, checks that the message
     * is not exceeding the max frequency limit. If message exceeds the limit,
     * returns a throttle result object that is appropriate for the passed in policy.
     *
     * Callers of checkLimit method should call updateMessageFrequency method
     * once a message is successfully sent to the client.
     *
     * @param maxFrequency The maximum frequency to enforce. If maxFrequency is
     * zero, the message frequencies are being kept track but no check happens.
     * @param policy The throttling policy.
     * @return The ThrottleResult.
     */
    public ThrottleResult checkLimit(int maxFrequency, Policy policy)
    {
        long messageTimestamp = System.currentTimeMillis();
        // If we have enough messages to start testing.
        if (maxFrequency > -1 && messageCount >= messageHistorySize)
        {
            // Find the interval between this message and the last n messages.
            int index = messageCount % messageHistorySize;
            long intervalMillis = messageTimestamp - previousMessageTimes[index];
            double intervalSeconds = intervalMillis / 1000d;
            // Calculate the message rate in seconds.
            double actualFrequency;
            if (intervalSeconds > 0)
            {
                actualFrequency = messageHistorySize / intervalSeconds;
                actualFrequency = Math.round(actualFrequency * 100d) / 100d;
            }
            // If the interval is zero, it means all the messages were sent
            // in the same instant. In that case, there's no frequency to
            // calculate really, so set it to one more than the limit.
            else
            {
                actualFrequency = maxFrequency + 1;
            }
            // If the rate is too high, toss this message and do not record it,
            // so the history represents the rate of messages actually delivered.
            if (maxFrequency > 0 && actualFrequency > maxFrequency)
            {
                Result result = ThrottleManager.getResult(policy);
                String detail = "[actual-frequency=" + actualFrequency + ", max-frequency=" + maxFrequency + "]";
                return new ThrottleResult(result, detail);
            }
        }
        // Return the default OK result.
        return new ThrottleResult();
    }

    /**
     * Increases the messageCount by one and updates the message time array by the
     * current time. This method should be used by callers of checkLimit method
     * once a message is successfully sent to the client.
     */
    public void updateMessageFrequency()
    {
        // Handle integer wrap
        messageCount = messageCount == Integer.MAX_VALUE? 0 : messageCount;

        // Increase the messageCount and update the message times.
        previousMessageTimes[messageCount++ % messageHistorySize] = System.currentTimeMillis();
    }
}
