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
package qa.messaging.throttle;

import java.util.List;

import flex.messaging.MessageBroker;
import flex.messaging.messages.AcknowledgeMessage;
import flex.messaging.messages.AsyncMessage;
import flex.messaging.util.UUIDUtils;
import qa.utils.MessageGenerator;

public class ROPushServerMessage
{
    private static final String messageOfSize10 = "0123456789";
    private static String messageOfSize100 = "";
    private static String messageOfSize1000 = "";
    private static String messageOfSize5000 = "";
    private static String messageOfSize10000;

    private MessageBroker msgBroker;
    private MessageGenerator generator;
    private volatile Thread generatorThread;

    public ROPushServerMessage()
    {
        for (int i = 0; i < 10; i++)
            messageOfSize100 += messageOfSize10;

        for (int i=0; i < 10; i++)
            messageOfSize1000 += messageOfSize100;

        for (int i=0; i < 5; i++)
            messageOfSize5000 += messageOfSize1000;

        messageOfSize10000 = messageOfSize5000 + messageOfSize5000;
    }

    public void publishMessage(String destination)
    {
        publishMessage(destination, "foo");
    }

    public void publishMessage(String destination, String message)
    {
        if (msgBroker == null)
            msgBroker = MessageBroker.getMessageBroker(null);

        String clientID = UUIDUtils.createUUID(false);

        AsyncMessage msg = new AsyncMessage();
        msg.setDestination(destination);
        msg.setClientId(clientID);
        msg.setMessageId(UUIDUtils.createUUID(false));
        msg.setTimestamp(System.currentTimeMillis());
        msg.setBody(message);
        System.out.println("Sending message to clients at: " + System.currentTimeMillis());
        msgBroker.routeMessageToService(msg, null);
    }

    public void publishAcknowledgeMessage(String destination, String message)
    {
        if (msgBroker == null)
            msgBroker = MessageBroker.getMessageBroker(null);

        String clientID = UUIDUtils.createUUID(false);

        AcknowledgeMessage msg = new AcknowledgeMessage();
        msg.setDestination(destination);
        msg.setClientId(clientID);
        msg.setMessageId(UUIDUtils.createUUID(false));
        msg.setTimestamp(System.currentTimeMillis());
        msg.setBody(message);
        msgBroker.routeMessageToService(msg, null);
    }

    public void publishMessages(String destination,int counts, int lvl)
    {
        String message;
        switch (lvl)
        {
            case 0:
                message = messageOfSize10;
                break;
            case 1:
                message = messageOfSize100;
                break;
            case 2:
                message = messageOfSize1000;
                break;
            case 3:
                message = messageOfSize5000;
                break;
            case 4:
                message = messageOfSize10000;
                break;
            default:
                throw new RuntimeException("Only have lvl 0 - 4");
        }

        for (int i=0; i<counts; i++)
            publishMessage(destination, message);
    }

    public void publishAcknowledgeMessages(String destination,int counts, int lvl)
    {
        String message;
        switch (lvl)
        {
            case 0:
                message = messageOfSize10;
                break;
            case 1:
                message = messageOfSize100;
                break;
            case 2:
                message = messageOfSize1000;
                break;
            case 3:
                message = messageOfSize5000;
                break;
            case 4:
                message = messageOfSize10000;
                break;
            default:
                throw new RuntimeException("Only have lvl 0 - 4");
        }

        for (int i=0; i<counts; i++)
            publishAcknowledgeMessage(destination, message);
    }
    public void streamMessages(String destination, Object message, int targetRate, int suggestedPasses)
    {
        if (generator == null)
        {
            // Invoke the message generator with the targetRatePerSecond, suggestedGeneratePassesPerSecond, the destination and the message.
            generator = new MessageGenerator(targetRate, suggestedPasses, destination, message);
            generatorThread = new Thread(generator);
            generatorThread.start();
        }
        else
        {
            stopStreaming();
            generator = null;
            streamMessages(destination, message, targetRate, suggestedPasses);
        }
    }
    public void stopStreaming()
    {
       if (generator != null)
       {
          generator.stop = true;
          try
          {
             generatorThread.join();
             System.out.println("Generator thread has stopped.");
          }
          catch (InterruptedException ignore)
          {}
       }
    }
    public List<Integer> getMsgSendRate()
    {
        return generator.getGenerationCountPerSecond();
    }

    public Integer getMsgTotal()
    {
        return generator.getTotal();
    }

    public Integer resetMsgTotal()
    {
        return generator.resetTotal();
    }
}
