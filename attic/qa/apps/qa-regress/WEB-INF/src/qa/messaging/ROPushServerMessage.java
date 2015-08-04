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
package qa.messaging;

import remoting.datatype.EnumApple;
import flex.messaging.FlexContext;
import flex.messaging.MessageBroker;
import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.AcknowledgeMessage;
import flex.messaging.util.UUIDUtils;

public class ROPushServerMessage
{
    private String messageOfSize10 = "0123456789";
    private String messageOfSize100 =   "01234567890123456789012345678901234567890123456789" +
                                    "01234567890123456789012345678901234567890123456789";
    private String messageOfSize1000 = "";
    private String messageOfSize5000 = "";
    private String messageOfSize10000;
    public ROPushServerMessage()
    {
        for (int i=0; i<10; i++)
        {
            messageOfSize1000 += messageOfSize100;
        }

        for (int i=0; i<5; i++)
        {
            messageOfSize5000 += messageOfSize1000;
        }
        messageOfSize10000 = messageOfSize5000 + messageOfSize5000;
    }

    public void publishMessage(String destination)
    {
        publishMessage(destination, "foo");
    }

    public void publishMessage(String destination, String message)
    {
        MessageBroker msgBroker = getMessageBroker();
        String clientID = UUIDUtils.createUUID(false);
        AsyncMessage msg = new AsyncMessage();
        msg.setDestination(destination);
        msg.setClientId(clientID);
        msg.setMessageId(UUIDUtils.createUUID(false));
        msg.setTimestamp(System.currentTimeMillis());
        msg.setBody(message);
        msgBroker.routeMessageToService(msg, null);         
    }

    public void publishAcknowledgeMessage(String destination, String message)
    {
        MessageBroker msgBroker = getMessageBroker();
        String clientID = UUIDUtils.createUUID(false);

        AcknowledgeMessage msg = new AcknowledgeMessage();
        msg.setDestination(destination);
        msg.setClientId(clientID);
        msg.setMessageId(UUIDUtils.createUUID(false));
        msg.setTimestamp(System.currentTimeMillis());
        msg.setBody(message);
        msgBroker.routeMessageToService(msg, null);
    }

    public void publishMessageWithHeader(String destination, String message, String headerName, String headerValue)
    {
        MessageBroker msgBroker = getMessageBroker();
        String clientID = UUIDUtils.createUUID(false);
        AsyncMessage msg = new AsyncMessage();
        msg.setDestination(destination);
        msg.setClientId(clientID);
        msg.setMessageId(UUIDUtils.createUUID(false));
        msg.setTimestamp(System.currentTimeMillis());
        msg.setBody(message);
        msg.setHeader(headerName, headerValue);
        msgBroker.routeMessageToService(msg, null);         
    }
    private MessageBroker getMessageBroker() 
    {
        return FlexContext.getMessageBroker();
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
        {
            publishMessage(destination, message);

        }
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
        {
            publishAcknowledgeMessage(destination, message);

        }
    }
    public void publishEnumMessage(String destination)
    {
        MessageBroker msgBroker = getMessageBroker();
        String clientID = UUIDUtils.createUUID(false);
        AsyncMessage msg = new AsyncMessage();
        msg.setDestination(destination);
        msg.setClientId(clientID);
        msg.setMessageId(UUIDUtils.createUUID(false));
        msg.setTimestamp(System.currentTimeMillis());
        msg.setBody(EnumApple.A);
        msgBroker.routeMessageToService(msg, null);         
    }
    
}