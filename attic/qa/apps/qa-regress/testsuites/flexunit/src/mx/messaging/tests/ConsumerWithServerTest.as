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

package mx.messaging.tests
{

import flash.events.TimerEvent;
import flash.utils.Timer;

import mx.messaging.ChannelSet;
import mx.messaging.Consumer;
import mx.messaging.Producer;
import mx.messaging.messages.AsyncMessage;
import mx.messaging.messages.CommandMessage;
import mx.messaging.messages.IMessage;
import mx.messaging.events.ChannelEvent;
import mx.messaging.events.MessageEvent;
import mx.messaging.events.MessageAckEvent;
import mx.messaging.events.MessageFaultEvent;
import mx.rpc.remoting.RemoteObject;

import flexunit.framework.TestCase;
import flexunit.framework.TestSuite;

/**
 *  Tests for mx.messaging.Producer and mx.messaging.Consumer.
 */
public class ConsumerWithServerTest extends TestCase
{
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////    
    
    public function ConsumerWithServerTest(methodName:String)
    {
        super(methodName);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    //
    // Setup
    //
    ////////////////////////////////////////////////////////////////////////////    

    /**
     *  Does setup before running each test method
     */
    override public function setUp():void
    {
    
    }     

    ////////////////////////////////////////////////////////////////////////////
    //
    // TestSuite
    //
    ////////////////////////////////////////////////////////////////////////////    
    
    public static function suite():TestSuite
    {
        var tests:TestSuite = new TestSuite();
        tests.addTest(new ConsumerWithServerTest("testSubscribeAndUnsubscribe"));
        tests.addTest(new ConsumerWithServerTest("testSubscribeMessageFaultEvent"));
        tests.addTest(new ConsumerWithServerTest("testMessageEvent"));
        tests.addTest(new ConsumerWithServerTest("testChannelSetNotNullAfterDisconnect"));
        tests.addTest(new ConsumerWithServerTest("testNoDSHeadersOnMessage"));
        tests.addTest(new ConsumerWithServerTest("testNoDSHeadersOnPushedMessage"));
        return tests;
   	}
   	
    ////////////////////////////////////////////////////////////////////////////
    //
    // Variables
    //
    ////////////////////////////////////////////////////////////////////////////    
    
    /**
     *  Allows handlers to be removed, queued, etc. across async response handling.
     */
    public static var handler:Function;   	
   	
    ////////////////////////////////////////////////////////////////////////////
    //
    // Tests
    //
    ////////////////////////////////////////////////////////////////////////////    

    
    public function testSubscribeAndUnsubscribe():void
    {
        var c:Consumer = new Consumer();
        c.destination = "MyTopic";
        handler = addAsync(verifySubscribe, 6000, {"c":c});
        c.addEventListener(MessageAckEvent.ACKNOWLEDGE, handler);
        assertTrue("Consumer not subscribed.", !c.subscribed);
        c.subscribe();
    }
    private function verifySubscribe(event:MessageAckEvent, params:Object):void
    {
        var c:Consumer = params.c as Consumer;
        assertTrue("Consumer subscribed.", c.subscribed); 
        assertEquals("Consumer's clientId matches ack.", c.clientId, event.message.clientId);
        c.removeEventListener(MessageAckEvent.ACKNOWLEDGE, handler);
        handler = addAsync(verifyUnsubscribe, 6000, {"c":c});
        c.addEventListener(MessageAckEvent.ACKNOWLEDGE, handler);
        c.unsubscribe();   
    }
    private function verifyUnsubscribe(event:MessageAckEvent, params:Object):void
    {
        var c:Consumer = params.c as Consumer;
        assertTrue("Consumer no longer subscribed.", !c.subscribed);
        assertTrue("Consumer's clientId is null.", c.clientId == null); 
        handler = null;  
        c.disconnect();    
    }
    
    private function verifyConsumerIsDisconnected(event:ChannelEvent, params:Object):void
    {
    	var c:Consumer = params.c as Consumer;
    	assertTrue("Channel is not reconnecting.", !event.reconnecting);
    	assertTrue("Disconnect was due to rejection.", event.rejected);
    	assertTrue("Consumer no longer connected.", !c.connected);
        assertTrue("Consumer no longer subscribed.", !c.subscribed); 
        handler = null;	
        c.disconnect();
    }       

    public static var msg:AsyncMessage;
    public function testMessageEvent():void
    {
        msg = new AsyncMessage();
        var c:Consumer = new Consumer();
        c.destination = "MyTopic";
        var complete:Function = addAsync(function(event:MessageAckEvent):void { c.disconnect(); }, 6000);        
        var messageReceived:Function = addAsync(function(event:MessageEvent):void
                                                 {                                                     
                                                     assertEquals("Sent message received.", event.message.messageId, msg.messageId);                                                     
                                                     c.unsubscribe();
                                                     c.addEventListener(MessageAckEvent.ACKNOWLEDGE, complete);
                                                 },5000);
        c.addEventListener(MessageEvent.MESSAGE, messageReceived);
        c.subscribe();
        var timer:Timer = new Timer(5, 1);
        timer.addEventListener(TimerEvent.TIMER, sendMessage);
        timer.start();        
    }
    private function sendMessage(event:TimerEvent):void
    {
        var p:Producer = new Producer();
        p.destination = "MyTopic";
        p.send(msg);    
    } 

    public function testChannelSetNotNullAfterDisconnect():void
    {
        var c:Consumer = new Consumer();
        c.destination = "MyTopic";
        var channelSet:ChannelSet = new ChannelSet(["qa-polling-amf"],false);
        c.channelSet = channelSet;        
        handler = addAsync(verifyChannelSetSubscribe, 15000, {"c":c});
        c.addEventListener(MessageAckEvent.ACKNOWLEDGE, handler);
        c.subscribe();
    }
    private function verifyChannelSetSubscribe(event:MessageAckEvent, params:Object):void
    {
        var c:Consumer = params.c as Consumer;
        assertTrue("Consumer subscribed.", c.subscribed); 
        assertEquals("Consumer's clientId matches ack.", c.clientId, event.message.clientId);
        c.removeEventListener(MessageAckEvent.ACKNOWLEDGE, handler);        
        c.unsubscribe();
        c.disconnect();
        assertNotNull("Consumer's ChannelSet should not be null after disconnect.", c.channelSet);            
    }
    
    public function testSubscribeMessageFaultEvent():void
    {
        var c:Consumer = new Consumer();
        c.destination = "AllBadChannels";
        c.resubscribeAttempts = 0;
        c.addEventListener(MessageFaultEvent.FAULT, addAsync(verifySubscribeFault, 15000, {"c":c}));
        c.subscribe();
    }
    private function verifySubscribeFault(event:MessageFaultEvent, params:Object):void
    {
        var c:Consumer = params.c as Consumer;
        assertTrue("Consumer not subscribed.", !c.subscribed);
        c.disconnect();
    }
    
    public function testNoDSHeadersOnMessage():void
    {
        var c:Consumer = new Consumer();
        c.destination = "MyTopic";
        var consumerSubscribed:Function = addAsync(function(event:MessageAckEvent):void 
                                           {
                                                if((event.correlation is CommandMessage) && (CommandMessage(event.correlation).operation == CommandMessage.SUBSCRIBE_OPERATION))
                                                {
                                                    var message:IMessage = new AsyncMessage(); 
                                                    message.body = "hello";
                                                    var p:Producer = new Producer();
                                                    p.destination = "MyTopic";
                                                    p.send(message);    
                                                }   
                                               
                                           },10000);     
        var receiveMessage:Function  = addAsync(function(event:MessageEvent):void
                                                 {                                                     
                                                     assertEquals("Sent message received.", "hello", event.message.body.toString());
                                                     assertNull("DSId should be null.", event.message.headers.DSId);     
                                                     assertNull("DSEndpoint should be null.", event.message.headers.DSEndpoint);                                                     
                                                     c.unsubscribe();
                                                     c.disconnect(); 
                                                 },10000);
        c.addEventListener(MessageEvent.MESSAGE,receiveMessage);
        c.addEventListener(MessageAckEvent.ACKNOWLEDGE, consumerSubscribed); 
        c.subscribe();
    }
   
    public function testNoDSHeadersOnPushedMessage():void
    {
        var c:Consumer = new Consumer();
        c.destination = "MyTopic";        
        var consumerSubscribed:Function = addAsync(function(event:MessageAckEvent):void 
                                           {
                                                if((event.correlation is CommandMessage) && (CommandMessage(event.correlation).operation == CommandMessage.SUBSCRIBE_OPERATION))
                                                {
                                                    var ro:RemoteObject = new RemoteObject("ServerPush");
                                                    ro.publishMessage("MyTopic", "hello");    
                                                }   
                                               
                                           },10000);
        var messageReceived:Function = addAsync(function(event:MessageEvent):void
                                                 {                                                     
                                                     assertEquals("Sent message received.", "hello",event.message.body.toString());
                                                     assertNull("DSId should be null.", event.message.headers.DSId);                                                     
                                                     assertNull("DSEndpoint should be null.", event.message.headers.DSEndpoint);
                                                     c.unsubscribe();
                                                     c.disconnect();
                                                 },10000); 
        
        c.addEventListener(MessageEvent.MESSAGE, messageReceived);
        c.addEventListener(MessageAckEvent.ACKNOWLEDGE, consumerSubscribed); 
        c.subscribe();
    }
}

}
