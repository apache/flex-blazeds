////////////////////////////////////////////////////////////////////////////////
//
//  Licensed to the Apache Software Foundation (ASF) under one or more
//  contributor license agreements.  See the NOTICE file distributed with
//  this work for additional information regarding copyright ownership.
//  The ASF licenses this file to You under the Apache License, Version 2.0
//  (the "License"); you may not use this file except in compliance with
//  the License.  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////
package
{
    import flash.display.Sprite;
    import flash.events.Event;
    import flash.text.TextField;
    
    import mx.collections.ArrayCollection;
    import mx.collections.ArrayList;
    import mx.core.mx_internal;
    import mx.events.PropertyChangeEvent;
    import mx.messaging.ChannelSet;
    import mx.messaging.Consumer;
    import mx.messaging.Producer;
    import mx.messaging.channels.AMFChannel;
    import mx.messaging.config.ConfigMap;
    import mx.messaging.config.LoaderConfig;
    import mx.messaging.events.ChannelFaultEvent;
    import mx.messaging.events.MessageAckEvent;
    import mx.messaging.events.MessageEvent;
    import mx.messaging.events.MessageFaultEvent;
    import mx.messaging.messages.AcknowledgeMessage;
    import mx.messaging.messages.AcknowledgeMessageExt;
    import mx.messaging.messages.AsyncMessage;
    import mx.messaging.messages.AsyncMessageExt;
    import mx.messaging.messages.CommandMessage;
    import mx.messaging.messages.CommandMessageExt;
    import mx.messaging.messages.ErrorMessage;
    import mx.messaging.messages.HTTPRequestMessage;
    import mx.messaging.messages.MessagePerformanceInfo;
    import mx.utils.ObjectProxy;
    import mx.utils.ObjectUtil;
    import mx.utils.RpcClassAliasInitializer;

    use namespace mx_internal;

    /**
     * A messaging sample that does not use any Flex UI classes. To get this working,
     * first the url of LoaderConfig needs to be set due to BLZ-522 bug. Then, some
     * classes need to be registered with Flash, see the registerClassAliases method. 
     */ 
    public class messaging_withoutUI extends Sprite
    {
        private static const CHANNEL_ID:String = "my-amf-poll";
        private static const CHANNEL_URL:String = "http://localhost:8400/team/messagebroker/myamfpoll";
        private static const DESTINATION_ID:String = "messaging_AMF_Poll";

        private var channelSet:ChannelSet;
        private var producer:Producer;  
        private var consumer:Consumer;
        private var text:TextField;

        public function messaging_withoutUI()
        {
            RpcClassAliasInitializer.registerClassAliases(); 
            
            setupChannelSet();
            setupProducer();
            setupConsumer();
            consumer.subscribe();

            text = new TextField();
            text.border = true;
            text.width = 300;  
            text.text = "initializing. . .";
            addChild(text);

        }

        private function consumerMsgHandler(event:Event):void 
        {
            trace("Consumer received msg: " + ObjectUtil.toString(event));
            text.text = "Consumer received message: " + (event as MessageEvent).message.body;
        }

        private function consumerPropChangeHandler(event:PropertyChangeEvent):void
        {
            // Make sure the ack is for a subscribe operation.
            if(event.property == "subscribed" && consumer.subscribed)
            {
                // Send the message.
                var msg:AsyncMessage = new AsyncMessage();
                msg.body = "hello";
                producer.send(msg);
            }
        }       

        private function setupChannelSet():void
        {
            channelSet = new ChannelSet(); 
            var channel:AMFChannel = new AMFChannel(CHANNEL_ID, CHANNEL_URL);
            channel.pollingEnabled = true; 
            channel.pollingInterval = 2000; 
            channelSet.addChannel(channel); 
        }
        
        private function setupConsumer():void
        {
            consumer = new Consumer();
            consumer.channelSet = channelSet; 
            consumer.destination = DESTINATION_ID;
            consumer.addEventListener(MessageEvent.MESSAGE, consumerMsgHandler);
            consumer.addEventListener(ChannelFaultEvent.FAULT, faultHandler);
            consumer.addEventListener(MessageFaultEvent.FAULT, faultHandler);
            consumer.addEventListener(PropertyChangeEvent.PROPERTY_CHANGE, consumerPropChangeHandler); 
        }

        private function setupProducer():void
        {
            producer = new Producer();
            producer.channelSet = channelSet;
            producer.destination = DESTINATION_ID;
            producer.addEventListener(MessageAckEvent.ACKNOWLEDGE, producerAckHandler);
            producer.addEventListener(MessageFaultEvent.FAULT, faultHandler);
        }

        private function producerAckHandler(event:Event):void
        {
            trace("Producer received ack: " + ObjectUtil.toString(event));
        }

        private function faultHandler(event:Event):void 
        {
            trace("Server fault: " + ObjectUtil.toString(event));
        }

    }
}