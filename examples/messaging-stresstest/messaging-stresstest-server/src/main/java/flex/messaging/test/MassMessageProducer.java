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
package flex.messaging.test;

import flex.messaging.MessageBroker;
import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.Message;
import flex.messaging.services.ServiceAdapter;
import flex.messaging.util.UUIDUtils;

import java.util.Date;

/**
 * Simple Message producer that continuously produces messaged containing random Double values.
 * The delay between postings is currently set to 1ms.
 */
public class MassMessageProducer extends ServiceAdapter {

    protected boolean running = false;

    // Send 100 messages per second.
    protected long delayMillis = 1;

    public void start() {
        super.start();

        Thread messageGenerator = new Thread(){
            public void run(){
                running = true;
                while(running){
                    Message message = generateMessage();
                    sendMessage(message);
                    try{
                        Thread.sleep(delayMillis);
                    }catch(InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        };
        messageGenerator.start();
    }

    public void stop() {
        running = false;
        super.stop();
    }


    @Override
    public Object invoke(Message message) {
        // Ignore any type of message ...
        return null;
    }

    protected Message generateMessage() {
        // Just generate some dummy payload.
        Double payload = Math.random();

        // Create an AsyncMessage to send to the client.
        AsyncMessage msg = new AsyncMessage();
        msg.setDestination("MassMessageDestination");
        msg.setClientId("MassMessageProducer-Java");
        msg.setTimestamp(new Date().getTime());
        msg.setMessageId(UUIDUtils.createUUID());
        msg.setTimeToLive(100000);
        msg.setBody(payload);

        return msg;
    }

    protected void sendMessage(Message message) {
        MessageBroker.getMessageBroker(null).routeMessageToService(message, null);
    }

}
