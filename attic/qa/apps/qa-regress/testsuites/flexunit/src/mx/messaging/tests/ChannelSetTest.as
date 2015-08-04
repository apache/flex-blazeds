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

import flash.errors.IllegalOperationError;

import mx.core.mx_internal;
import mx.messaging.Channel;
import mx.messaging.ChannelSet;
import mx.messaging.MessageAgent;
import mx.messaging.channels.AMFChannel;
import mx.messaging.channels.HTTPChannel;
import mx.messaging.config.ServerConfig;
import mx.messaging.errors.NoChannelAvailableError;
import mx.messaging.events.ChannelEvent;
import mx.messaging.events.ChannelFaultEvent;
import mx.messaging.events.MessageAckEvent;
import mx.messaging.events.MessageFaultEvent;
import mx.messaging.messages.AsyncMessage;

import flexunit.framework.TestSuite;

import mx.messaging.tests.helpers.MockMessageAgent;

public class ChannelSetTest extends ConfigurationBasedTestCase
{
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////    
    
    public function ChannelSetTest(methodName:String)
    {
        super(methodName);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Setup
    //
    ////////////////////////////////////////////////////////////////////////////    

    override public function setUp():void
    {
        super.setUp();
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // TestSuite
    //
    ////////////////////////////////////////////////////////////////////////////    

    public static function suite():TestSuite
    {
        var tests:TestSuite = new TestSuite();
        tests.addTest(new ChannelSetTest("testAddChannel"));
        tests.addTest(new ChannelSetTest("testAddNullChannel"));
        tests.addTest(new ChannelSetTest("testDupeAddChannel"));
        tests.addTest(new ChannelSetTest("testAddChannelToConfiguredChannelSetError"));
        tests.addTest(new ChannelSetTest("testRemoveChannel"));
        tests.addTest(new ChannelSetTest("testRemoveNonExistentChannel"));
        tests.addTest(new ChannelSetTest("testRemoveChannelFromConfiguredChannelSetError"));
        tests.addTest(new ChannelSetTest("testChannelIdsForManualChannels"));
        tests.addTest(new ChannelSetTest("testChannelIdsForConfiguredChannels"));
        tests.addTest(new ChannelSetTest("testConfigured"));
        tests.addTest(new ChannelSetTest("testClustered"));
        tests.addTest(new ChannelSetTest("testConnectAddsAgent"));
        tests.addTest(new ChannelSetTest("testConnectDoesNotAddDupeAgent"));
        tests.addTest(new ChannelSetTest("testDisconnectRemovesAgent"));
        tests.addTest(new ChannelSetTest("testDisconnectIgnoresDisconnectedAgent"));
        tests.addTest(new ChannelSetTest("testChannelSetConnect"));
        tests.addTest(new ChannelSetTest("testChannelSetNoEventOnAdditionalConnects"));
        tests.addTest(new ChannelSetTest("testChannelSetDisconnect"));
        tests.addTest(new ChannelSetTest("testSetChannels"));
        tests.addTest(new ChannelSetTest("testSend"));
        tests.addTest(new ChannelSetTest("testSendWithHunting"));
        tests.addTest(new ChannelSetTest("testSendFault"));
        tests.addTest(new ChannelSetTest("testSendWithNoChannelsError"));
        return tests;
       }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Tests
    //
    ////////////////////////////////////////////////////////////////////////////    

    /**
     * Test that a manual channel add works.
     */
    public function testAddChannel():void
    {
        var cs:ChannelSet = new ChannelSet();
        var c:Channel = new AMFChannel("fake", "");
        cs.addChannel(c);
        var ids:Array = cs.channelIds;
        assertEquals("Channel added.", ids[0], "fake");
    }

    /**
     * Test that adding a null channel fails
     */ 
    public function testAddNullChannel():void
    {
        var cs:ChannelSet = new ChannelSet();
        cs.addChannel(null);
        assertEquals("Null channel add ignored.", cs.channelIds.length, 0);
    }

    /**
     * Test that a duplicate manual channel add is ignored.
     */ 
    public function testDupeAddChannel():void
    {
        var cs:ChannelSet = new ChannelSet();
        var c:Channel = new AMFChannel("fake", "");
        cs.addChannel(c);
        var ids:Array = cs.channelIds;
        assertEquals("Channel added.", ids[0], "fake");
        // Dupe add
        cs.addChannel(c);
        assertEquals("Dupe channel add ignored.", cs.channelIds.length, 1);
    }

    /**
     * Test adding a channel to a configured ChannelSet throws error.
     */ 
    public function testAddChannelToConfiguredChannelSetError():void
    {
        // Create a configured set.
        var cs:ChannelSet = new ChannelSet(["qa-amf"], false);
        // Now try to add a channel
        try
        {
            var c:Channel = new AMFChannel("fake", "");
            cs.addChannel(c);
        }
        catch (e:IllegalOperationError)
        {
            assertTrue("Channel add for configured set throws.",
                                                e is IllegalOperationError);
        }
        catch (e:Error)
        {
            fail("Didn't throw an IllegalOperationError");
        }
    }

    /**
     * Test that a manual channel remove works.
     */ 
    public function testRemoveChannel():void
    {
        var cs:ChannelSet = new ChannelSet();
        var c:Channel = new AMFChannel("fake", "");
        cs.addChannel(c);
        assertEquals("Added Channel.", cs.channelIds.length, 1);
        cs.removeChannel(c)
        assertEquals("Removed Channel.", cs.channelIds.length, 0);
    }

    /**
     * Test that a manual channel remove for non-existent channel is a no-op.
     */ 
    public function testRemoveNonExistentChannel():void
    {
        var cs:ChannelSet = new ChannelSet();
        var c:Channel = new AMFChannel("fake", "");
        assertEquals("No Channels.", cs.channelIds.length, 0);
        cs.removeChannel(c)
        assertEquals("After remove, still no channels.", cs.channelIds.length, 0);
    }

    /** 
     * Test that removing a channel from a configured channelset throws.
     */ 
    public function testRemoveChannelFromConfiguredChannelSetError():void
    {
        // Create a configured set.
        var cs:ChannelSet = new ChannelSet(["qa-amf"], false);
        // Now try to remove the channel
        try
        {
            var c:Channel = ServerConfig.getChannel("qa-amf");
            cs.removeChannel(c);
        }
        catch (e:IllegalOperationError)
        {
            assertTrue("Channel remove for configured set throws.",
                                                e is IllegalOperationError);
        }
        catch (e:Error)
        {
            fail("Didn't throw an IllegalOperationError");
        }
    }

    /** 
     * Test that the channelIds prop returns correctly for manual channels.
     */ 
    public function testChannelIdsForManualChannels():void
    {
        var cs:ChannelSet = new ChannelSet();
        var c:Channel = new AMFChannel("fake", "");
        cs.addChannel(c);
        c = new AMFChannel("fake2", "");
        cs.addChannel(c);
        var ids:Array = cs.channelIds;
        assertEquals("First channel has id 'fake'.", ids[0], "fake");
        assertEquals("Second channel has id 'fake2'.", ids[1], "fake2");
    }

    /** 
     * Test that the channelIds prop returns correctly for configured channels.
     */ 
    public function testChannelIdsForConfiguredChannels():void
    {
        var cs:ChannelSet = new ChannelSet(["fake", "fake2"], false);
        var ids:Array = cs.channelIds;
        assertEquals("First channel has id 'fake'.", ids[0], "fake");
        assertEquals("Second channel has id 'fake2'.", ids[1], "fake2");
    }

    /**
     * Test that the configured prop returns correctly for manual and configured channelsets.
     */
    public function testConfigured():void
    {
        var cs:ChannelSet = new ChannelSet();
        assertTrue("Manual channelset is not configured.", !cs.mx_internal::configured);

        var cs2:ChannelSet = new ChannelSet([], false);
        assertTrue("Configured channelset is configured.", cs2.mx_internal::configured);
    }

    /**
     * Test that the clustered prop returns correctly for clustered and non-clustered
     * channelsets.
     */ 
    public function testClustered():void
    {
        var cs:ChannelSet = new ChannelSet([], false);
        assertTrue("Channelset is not clustered.", !cs.clustered);

        var cs2:ChannelSet = new ChannelSet([], true);
        assertTrue("Channelset is clustered.", cs2.clustered);
    }

    /**
     * Test connecting a message agent to the channelset.
     */ 
    public function testConnectAddsAgent():void
    {
        var a:MockMessageAgent = new MockMessageAgent();
        var cs:ChannelSet = new ChannelSet();
        cs.connect(a);
        var agents:Array = cs.messageAgents;
        assertEquals("Channelset added agent.", a, agents[0]);
    }

    /**
     * Test connecting a dupe message agent to the channelset is a no-op.
     */ 
    public function testConnectDoesNotAddDupeAgent():void
    {
        var a:MockMessageAgent = new MockMessageAgent();
        var cs:ChannelSet = new ChannelSet();
        cs.connect(a);
        cs.connect(a);
        assertEquals("Channelset doesn't add dupe agent.", cs.messageAgents.length, 1);
    }

    /**
     *  Test disconnecting a message agent from the set.
     */
    public function testDisconnectRemovesAgent():void
    {
        var a:MockMessageAgent = new MockMessageAgent();
        var cs:ChannelSet = new ChannelSet();
        cs.connect(a);
        cs.disconnect(a);
        assertEquals("Channelset removed agent.", cs.messageAgents.length, 0);
    }

    /**
     * Test that disconnecting a disconnected agent is a no-op.
     */
    public function testDisconnectIgnoresDisconnectedAgent():void
    {
        var a:MockMessageAgent = new MockMessageAgent();
        var cs:ChannelSet = new ChannelSet();
        cs.connect(a);
        cs.disconnect(a);
        cs.disconnect(a);
        assertEquals("Channelset ignored disconnected agent.", cs.messageAgents.length, 0);
    }

    /** 
     * Test that connecting a message agent results in an async connect event for the set
     * as well as toggling the connected prop.
     */ 
    public function testChannelSetConnect():void
    {
        var msg:AsyncMessage = new AsyncMessage();
        msg.destination = "MyTopic";
        var cs:ChannelSet = ServerConfig.getChannelSet(msg.destination);
        var a:MockMessageAgent = new MockMessageAgent();
        cs.addEventListener(ChannelEvent.CONNECT, addAsync(checkChannelConnect, 6000, {"cs":cs}));
        cs.connect(a);
        cs.send(a, msg); // Must send to trigger dynamic channel connect
        assertTrue("Channelset NOT connected.", !cs.connected);
    }
    /**
     * @private
     */  
    public function checkChannelConnect(event:ChannelEvent, params:Object):void
    {
        var cs:ChannelSet = params.cs as ChannelSet;
        assertEquals("Event is CONNECT.", event.type, ChannelEvent.CONNECT);
        assertTrue("Not failing over.", !event.reconnecting);
        assertTrue("Channelset is now connected.", cs.connected);
        assertEquals("ChannelSet has one connected agent.", cs.messageAgents.length, 1);
    }
       
	/**
	 * Tests that the channelset doesn't dispatch a general connect event for
     * subsequent agent connects after it has connected once.
	 */  
    public function testChannelSetNoEventOnAdditionalConnects():void
    {
        connectEventCount = 0;
        var msg:AsyncMessage = new AsyncMessage();
        msg.destination = "MyTopic";
        var cs:ChannelSet = ServerConfig.getChannelSet(msg.destination);
        var a:MockMessageAgent = new MockMessageAgent();
        cs.addEventListener(ChannelEvent.CONNECT, addAsync(verifyFirstConnect, 6000, {"cs":cs}));
        cs.connect(a);
        cs.send(a, msg);
        assertEquals("Connected one agent to the channelset.", cs.messageAgents.length, 1);
    }
    
    /**
     * @private
     */  
    public static var connectEventCount:int;
	/**
     * @private
     */ 
    public function verifyFirstConnect(event:ChannelEvent, params:Object):void
    {
        ++connectEventCount;
        assertEquals("Connect event count for ChannelSet is 1.", connectEventCount, 1);
        var cs:ChannelSet = params.cs as ChannelSet;
        var b:MockMessageAgent = new MockMessageAgent();
        cs.addEventListener(ChannelEvent.CONNECT, addAsync(verifySecondConnect, 6000, {}, verifyNoSecondConnect));
        cs.connect(b);
        var msg:AsyncMessage = new AsyncMessage();
        msg.destination = "MyTopic";
        cs.send(b, msg);
        assertEquals("Connected second agent to the channelset.", cs.messageAgents.length, 2);
    }
	/**
     * @private
     */ 
    public function verifySecondConnect(event:ChannelEvent, params:Object):void
    {
        // No second connect event should be dispatched to trigger this handler
        fail("The Channelset dispatched a second general CONNECT event when a " +
            "second agent connected. This event should only be generally " +
            "broadcast upon initial connect.");
    }
	/**
     * @private
     */ 
    public function verifyNoSecondConnect(params:Object):void
    {
        assertTrue("No second connect event was handled.", true);
    }

    /** 
     * Test that disconnecting the only agent from a connected channelset disconnects
     * the set and toggles the connected prop
     */ 
    public function testChannelSetDisconnect():void
    {
        var msg:AsyncMessage = new AsyncMessage();
        msg.destination = "MyTopic";
        var cs:ChannelSet = ServerConfig.getChannelSet(msg.destination);
        var a:MockMessageAgent = new MockMessageAgent();
        cs.addEventListener(ChannelEvent.CONNECT, addAsync(asyncDisconnect, 6000, {"cs":cs, "a":a}));        
        cs.connect(a);
        cs.send(a, msg); // Must send to trigger dynamic channel connect
    }

    /**
     * Test that calling setChannels(), the current channels are disconnected and removed from the ChannelSet
     */ 
    public function testSetChannels():void
    {
        var cs:ChannelSet = new ChannelSet();
        var c1:Channel = new AMFChannel("old1", "");
        cs.addChannel(c1);
        assertEquals("Channels added: ", cs.channelIds.length, 1);
		// Test replacing the old channel via set channel
        var c2:Channel = new AMFChannel("new1", "");
        var c3:Channel = new HTTPChannel("new2", "");
        var channelArray:Array = new Array();
        channelArray[0] = c2;
        channelArray[1] = c3;
        cs.channels = channelArray;
        assertEquals("Channels added: ", cs.channelIds.length, 2);
        assertEquals("New channel has been added: ", cs.channelIds[0], "new1");
        assertEquals("New channel has been added: ", cs.channelIds[1], "new2");
        cs.channels = null;
        assertEquals("Channels have been deleted: ", cs.channelIds.length, 0);
    }
	/**
     * @private
     */ 
    public function asyncDisconnect(event:ChannelEvent, params:Object):void
    {
        var cs:ChannelSet = params.cs as ChannelSet;
        var a:MessageAgent = params.a as MessageAgent;
        assertTrue("Channelset is now connected.", cs.connected);
        assertEquals("ChannelSet has one connected agent.", cs.messageAgents.length, 1);
        // Now disconnect the agent
        cs.disconnect(a);
        assertEquals("No connected agents.", cs.messageAgents.length, 0);
        assertTrue("Channelset is not connected.", !cs.connected);
        // NOTE: we don't wait for an async disconnect event, because when the agent
        // disconnects the set fabricates a disconnect and dispatches just to that
        // agent. When the set has no more connected agents it disconnects from its
        // underlying channel and that call unwires the sets listeners for connect,
        // disconnect and fault on the underlying channel so there's nothing else
        // to listen for.
    }

    /**
     * Tests that a send over the channelset results in a message ack event for the 
     * agent sending the message.
     */ 
    public function testSend():void
    {
        var msg:AsyncMessage = new AsyncMessage();
        msg.destination = "MyTopic";
        var cs:ChannelSet = ServerConfig.getChannelSet(msg.destination);
        var a:MockMessageAgent = new MockMessageAgent();
        a.addEventListener(MessageAckEvent.ACKNOWLEDGE, addAsync(verifyAckMessage, 6000, {"msg":msg}));
        cs.connect(a);
        cs.send(a, msg);
    }
    /**
     * @private
     */ 
    public function verifyAckMessage(event:MessageAckEvent, params:Object):void
    {
        var msg:AsyncMessage = params.msg as AsyncMessage;
        var ackMsg:AsyncMessage = event.message as AsyncMessage;
        assertEquals("Ack correlates to sent message.", ackMsg.correlationId, msg.messageId);
    }
    
    /**
     * Tests that the channelset can hunt past two bad channels to send over a
     * third good channel
     */ 
    public function testSendWithHunting():void
    {
        var cs:ChannelSet = new ChannelSet(["my-bad-http", "my-bad-amf", "qa-polling-amf"], false);
        // Keep track of channel fault events through the hunting cycle.
        cs.addEventListener(ChannelFaultEvent.FAULT, handleHuntingFault);
        cs.addEventListener(ChannelEvent.CONNECT, addAsync(verifyConnectFailover, 10000));
        var msg:AsyncMessage = new AsyncMessage();
        msg.destination = "MyTopic";
        var a:MockMessageAgent = new MockMessageAgent();
        a.addEventListener(MessageAckEvent.ACKNOWLEDGE, addAsync(verifyHuntingAckMessage, 10000, {"msg":msg}));
        cs.connect(a);
        cs.send(a, msg);
    }
	/**
     * @private
     */
	public static var huntingFaults:Array = [];
	/**
     * @private
     */
    public function handleHuntingFault(event:ChannelFaultEvent):void
    {
        huntingFaults.push(event);
    }
	/**
     * @private
     */
    public function verifyConnectFailover(event:ChannelEvent):void
    {
        assertTrue("Channelset connect on 4th channel is result of failover.", event.reconnecting);
    }
	/**
     * @private
     */
    public function verifyHuntingAckMessage(event:MessageAckEvent, params:Object):void
    {
        var msg:AsyncMessage = params.msg as AsyncMessage;
        var ackMsg:AsyncMessage = event.message as AsyncMessage;
        assertEquals("Ack correlates to sent message.", ackMsg.correlationId, msg.messageId);
        // Now verify that we hunted through the 2 bad channels and that the event data
        // is correct.
        verifyHuntingFaults();
    }
	/**
     * @private
     */
    private function verifyHuntingFaults():void
    {
        var n:int = huntingFaults.length;
        assertEquals("Hunting generated 2 channel faults.", n, 2);
        for (var i:int = 0; i < n; i++)
        {
            var e:ChannelFaultEvent = huntingFaults.pop() as ChannelFaultEvent;
            assertTrue("Channel fault indicates set was failing over.", e.reconnecting);        
        }
    }
    
    
    /** 
     * Tests that a ChannelSet with no good channels faults the sent message back
     * to the agent.
     */ 
    public function testSendFault():void
    {
        var cs:ChannelSet = new ChannelSet(["my-bad-amf"], false);
        var msg:AsyncMessage = new AsyncMessage();
        msg.destination = "MyTopic";
        var a:MockMessageAgent = new MockMessageAgent();
        a.addEventListener(MessageFaultEvent.FAULT, addAsync(verifyFaultMessage, 10000, {"msg":msg}));
        cs.connect(a);
        cs.send(a, msg);
    }
	/**
     * @private
     */
    public function verifyFaultMessage(event:MessageFaultEvent, params:Object):void
    {
        var msg:AsyncMessage = params.msg as AsyncMessage;
        assertEquals("Fault correlates to sent message.", event.message.correlationId, msg.messageId);         
    }
    
    /** 
     * Test that sending over a channelset with no channels errors out.
     */ 
    public function testSendWithNoChannelsError():void
    {
        var cs:ChannelSet = new ChannelSet();
        var msg:AsyncMessage = new AsyncMessage();
        msg.destination = "MyTopic";
        var a:MockMessageAgent = new MockMessageAgent();
        cs.connect(a);
        try
        {
            cs.send(a, msg);
        }
        catch (e:NoChannelAvailableError)
        {
            assertTrue("No channels available for send.", 
                                                e is NoChannelAvailableError);    
        }
        catch (e:Error)
        {
            fail("Didn't throw a NoChannelAvailableError");
        }
    }
}

}