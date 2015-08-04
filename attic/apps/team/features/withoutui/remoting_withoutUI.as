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
    import mx.rpc.events.FaultEvent;
    import mx.rpc.events.ResultEvent;
    import mx.rpc.remoting.RemoteObject;
    import mx.utils.ObjectProxy;
    import mx.utils.ObjectUtil;
    import mx.utils.RpcClassAliasInitializer;

    use namespace mx_internal;

    /**
     * A messaging sample that does not use any Flex UI classes. To get this working,
     * first the url of LoaderConfig needs to be set due to BLZ-522 bug. Then, some
     * classes need to be registered with Flash, see the registerClassAliases method. 
     */ 
    public class remoting_withoutUI extends Sprite
    {
        private static const CHANNEL_ID:String = "my-amf";
        private static const CHANNEL_URL:String = "http://localhost:8400/team/messagebroker/amf";
        private static const DESTINATION_ID:String = "remoting_AMF";

        private var channelSet:ChannelSet;
        private var ro:RemoteObject;          
        private var text:TextField;

        public function remoting_withoutUI()
        {
            RpcClassAliasInitializer.registerClassAliases(); 
            
            setupChannelSet();
            setupRO();
            
            text = new TextField();
            text.border = true;
            text.width = 300;  
            text.text = "initializing. . .";
            addChild(text);

            ro.echo("hello");
        }

        private function resultHandler(event:Event):void 
        {
            trace("RemoteObject received result: " + ObjectUtil.toString(event));
            text.text = "RemoteObject receives result: " + (event as ResultEvent).message.body;
        }       

        private function setupChannelSet():void
        {
            channelSet = new ChannelSet(); 
            var channel:AMFChannel = new AMFChannel(CHANNEL_ID, CHANNEL_URL);
            channelSet.addChannel(channel); 
        }
        
        private function setupRO():void
        {
            ro = new RemoteObject();
            ro.channelSet = channelSet; 
            ro.destination = DESTINATION_ID;
            ro.addEventListener(ResultEvent.RESULT, resultHandler);
            ro.addEventListener(ChannelFaultEvent.FAULT, faultHandler);
            ro.addEventListener(FaultEvent.FAULT, faultHandler);             
        }
        
        private function faultHandler(event:Event):void 
        {
            trace("Server fault: " + ObjectUtil.toString(event));
        }
    }
}