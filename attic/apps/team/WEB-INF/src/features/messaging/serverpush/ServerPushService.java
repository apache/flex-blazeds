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
package features.messaging.serverpush;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import flex.messaging.FlexContext;
import flex.messaging.MessageBroker;
import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.Message;
import flex.messaging.util.UUIDUtils;

/**
 * This class can be used as a remote object by Flex client to start/stop pushing
 * messages from the server.
 */
public class ServerPushService
{
    static int INFINITE_RUN = -1;

    ScheduledExecutorService messagePushService;
    MessageBroker broker;
    int messageCount;
    int currentRunCount;
    int runCount;

    public ServerPushService()
    {
        broker = FlexContext.getMessageBroker();
    }

    /**
     * Push the specified number of messages per the specified number of millis,
     * runCount number of times (-1 means indefinitely) to the specified destination.
     */
    public void startPush(String destinationId, int numberOfMessages, long numberOfMillis, int runCount)
    {
        startPush(destinationId, null, numberOfMessages, numberOfMillis, runCount);
    }

    /**
     * Push the specified number of messages per the specified number of millis,
     * runCount number of times (-1 means indefinitely) to the specified destination
     * and subtopic.
     */
    public void startPush(String destinationId, String subtopic, int numberOfMessages, long numberOfMillis, int runCount)
    {
        if (messagePushService == null)
            messagePushService = Executors.newScheduledThreadPool(10);

        this.runCount = runCount;
        currentRunCount = 0;

        System.out.println("*** ServerPushService started: sending '" + numberOfMessages
                + "' messages in '" + numberOfMillis + "ms' to destination '" + destinationId + "'[" + subtopic + "].");

        MessageSenderCallable msc = new MessageSenderCallable(destinationId, subtopic, numberOfMessages, numberOfMillis);
        messagePushService.schedule(msc, numberOfMillis, TimeUnit.MILLISECONDS);
    }

    public void stopPush()
    {
        if (messagePushService != null)
        {
            System.out.println("*** ServerPushService stopped.");
            messagePushService.shutdown();
            messagePushService = null;
        }
    }

    class MessageSenderCallable implements Callable<Object>
    {
        private String destinationId;
        private int numberOfMessages;
        private long numberOfMillis;
        private String subtopic;

        public MessageSenderCallable(String destinationId, String subtopic, int numberOfMessages, long numberOfMillis)
        {
            this.destinationId = destinationId;
            this.numberOfMessages = numberOfMessages;
            this.numberOfMillis = numberOfMillis;
            this.subtopic = subtopic;
        }

        public Object call() throws Exception
        {
            for (int i = 0; i < numberOfMessages; i++)
            {
                Message message = createMessage();
                broker.routeMessageToService(message, null);
            }
            if (runCount == INFINITE_RUN || ++currentRunCount < runCount)
            {
                MessageSenderCallable messageSenderCallable = new MessageSenderCallable(destinationId, subtopic, numberOfMessages, numberOfMillis);
                messagePushService.schedule(messageSenderCallable, numberOfMillis, TimeUnit.MILLISECONDS);
            }
            return null;
        }

        private Message createMessage()
        {
            AsyncMessage msg = new AsyncMessage();
            msg.setDestination(destinationId);
            msg.setMessageId(UUIDUtils.createUUID(false));
            msg.setTimestamp(System.currentTimeMillis());
            msg.setBody("Foo" + messageCount++);
            if (subtopic != null)
                msg.setHeader(AsyncMessage.SUBTOPIC_HEADER_NAME, subtopic);
            return msg;
        }
    }
}