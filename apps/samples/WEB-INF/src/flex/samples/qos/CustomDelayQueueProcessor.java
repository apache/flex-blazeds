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
package flex.samples.qos;

import java.util.ArrayList;
import java.util.List;

import flex.messaging.client.FlexClient;
import flex.messaging.client.FlexClientOutboundQueueProcessor;
import flex.messaging.client.FlushResult;
import flex.messaging.config.ConfigMap;
import flex.messaging.MessageClient;

/**
 * Per client queue processor that applies custom quality of services parameters (in this case: delay).
 * Custom quality of services parameters are read from the client FlexClient instance.
 * In this sample, these parameters are set in the FlexClient instance by the client application 
 * using the flex.samples.qos.FlexClientConfigService RemoteObject.  
 * 
 * This class is used in the channel definition (see services-config.xml)that the 'market-data-feed' 
 * message destination (see messaging-config.xml) references.
 * 
 * Each client that connects to this channel's endpoint gets a unique instance of this class to manage
 * its specific outbound queue of messages.
 */
public class CustomDelayQueueProcessor extends FlexClientOutboundQueueProcessor 
{
    /**
     * Used to store the last time this queue was flushed.
     * Starts off with an initial value of the construct time for the instance.
     */
    private long lastFlushTime = System.currentTimeMillis();

    /**
     * Driven by configuration, this is the configurable delay time between flushes.
     */
    private int delayTimeBetweenFlushes;

    public CustomDelayQueueProcessor() {
	}

	/**
     * Sets up the default delay time between flushes. This default is used if a client-specific
     * value has not been set in the FlexClient instance.
     * 
     * @param properties A ConfigMap containing any custom initialization properties.
     */
    public void initialize(ConfigMap properties) 
    {
        delayTimeBetweenFlushes = properties.getPropertyAsInt("flush-delay", -1);
        if (delayTimeBetweenFlushes < 0)
            throw new RuntimeException("Flush delay time for DelayedDeliveryQueueProcessor must be a positive value.");
    }

    /**
     * This flush implementation delays flushing messages from the queue until 3 seconds
     * have passed since the last flush. 
     * 
     * @param outboundQueue The queue of outbound messages.
     * @return An object containing the messages that have been removed from the outbound queue
     *         to be written to the network and a wait time for the next flush of the outbound queue
     *         that is the default for the underlying Channel/Endpoint.
     */
    public FlushResult flush(List outboundQueue)
    {
    	int delay = delayTimeBetweenFlushes;
    	// Read custom delay from client's FlexClient instance
    	FlexClient flexClient = getFlexClient();
    	if (flexClient != null)
    	{
			Object obj = flexClient.getAttribute("market-data-delay");
	    	if (obj != null)
	    	{
				try {
					delay = Integer.parseInt((String) obj);
				} catch (NumberFormatException ignore) {
				}    		
	    	}
    	}
    	
        long currentTime = System.currentTimeMillis();
    	if ((currentTime - lastFlushTime) < delay)
        {
            // Delaying flush. No messages will be returned at this point
            FlushResult flushResult = new FlushResult();
            // Don't return any messages to flush.
            // And request that the next flush doesn't occur until 3 seconds since the previous.
            flushResult.setNextFlushWaitTimeMillis((int)(delay - (currentTime - lastFlushTime)));
            return flushResult;
        }
        else // OK to flush.
        {
            // Flushing. All queued messages will now be returned
            lastFlushTime = currentTime;    
            FlushResult flushResult = new FlushResult();
            flushResult.setNextFlushWaitTimeMillis(delay);
            flushResult.setMessages(new ArrayList(outboundQueue));
            outboundQueue.clear();        
            return flushResult;
        }
    }

    public FlushResult flush(MessageClient client, List outboundQueue) {
        return super.flush(client, outboundQueue);
    }

}