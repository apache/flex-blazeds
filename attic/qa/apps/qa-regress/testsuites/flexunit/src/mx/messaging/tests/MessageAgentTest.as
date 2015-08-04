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

package mx.messaging.tests {

import flexunit.framework.*;

import mx.core.mx_internal;
import mx.messaging.*;
import mx.messaging.channels.*;
import mx.messaging.config.*;
import mx.messaging.errors.*;
import mx.messaging.events.*;
import mx.messaging.messages.*;
import mx.messaging.tests.helpers.MockMessageAgent;

use namespace mx_internal;

/**
 *  Tests for base (shared) MessageAgent functionality.
 */
public class MessageAgentTest extends ConfigurationBasedTestCase
{
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////

    public function MessageAgentTest(methodName:String)
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
        tests.addTest(new MessageAgentTest("testChannelSetProp"));
        tests.addTest(new MessageAgentTest("testClientIdProp"));
        tests.addTest(new MessageAgentTest("testConnectedProp"));
        tests.addTest(new MessageAgentTest("testDestinationProp"));
        tests.addTest(new MessageAgentTest("testDestinationAssignmentNullsConfiguredChannelSet"));
        tests.addTest(new MessageAgentTest("testIdProp"));
        tests.addTest(new MessageAgentTest("testMessageAckEventAndChannelConnectEvent"));
        tests.addTest(new MessageAgentTest("testMessageFaultEventAndChannelFaultEvent"));
        tests.addTest(new MessageAgentTest("testInitialized"));
        tests.addTest(new MessageAgentTest("testInternalSendErrorWhenDestinationNotSet"));
        tests.addTest(new MessageAgentTest("testChannelSetNotNullAfterDisconnect"));
        return tests;
   	}

    ////////////////////////////////////////////////////////////////////////////
    //
    // Tests
    //
    ////////////////////////////////////////////////////////////////////////////

   	public function testChannelSetProp():void
   	{
        var a:MockMessageAgent = new MockMessageAgent();
        assertEquals("Agent's initial channelset is null.", a.channelSet, null);
        var cs:ChannelSet = new ChannelSet();
        a.channelSet = cs;
        assertEquals("Assigned channelset is equal.", a.channelSet, cs);
        a.channelSet = null;
        assertEquals("Nulled out channelset works.", a.channelSet, null);
   	}

    public function testClientIdProp():void
    {
        var a:MockMessageAgent = new MockMessageAgent();
        assertEquals("Initial clientId is null.", a.clientId, null);
        a.setClientId("testId");
        assertEquals("Assigned clientId is equal.", a.clientId, "testId");
        a.setClientId( null);
        assertEquals("Nulled out clientId works.", a.clientId, null);
    }

    public function testConnectedProp():void
    {
        var a:MockMessageAgent = new MockMessageAgent();
        assertTrue("Agent is not connected.", !a.connected);
   	    var msg:AsyncMessage = new AsyncMessage();
   	    msg.destination = "MyTopic";
   	    var cs:ChannelSet = ServerConfig.getChannelSet(msg.destination);
        a.channelSet = cs;
        a.addEventListener(ChannelEvent.CONNECT, addAsync(checkConnected, 6000, {"a":a}));
        a.send(msg);
    }
    private function checkConnected(event:ChannelEvent, params:Object):void
    {
        var a:MockMessageAgent = params.a as MockMessageAgent;
        assertTrue("Agent is now connected.", a.connected);
    }

    public function testDestinationProp():void
    {
        var a:MockMessageAgent = new MockMessageAgent();
        assertEquals("Agent's initial destination is empty string.", a.destination, "");
        a.destination = "MyTopic";
        assertEquals("Assigned destination is equal.", a.destination, "MyTopic");
    }

    public function testDestinationAssignmentNullsConfiguredChannelSet():void
    {
        var a:MockMessageAgent = new MockMessageAgent();
        a.destination = "MyTopic";
   	    var msg:AsyncMessage = new AsyncMessage();
        a.addEventListener(ChannelEvent.CONNECT, addAsync(checkNullChannelSet, 6000, {"a":a}));
        a.send(msg);
    }
    private function checkNullChannelSet(event:ChannelEvent, params:Object):void
    {
        var a:MockMessageAgent = params.a as MockMessageAgent;
        assertTrue("Agent's channel set is not null.", a.channelSet != null);
        a.destination = "NewDestination";
        assertEquals("Updating destination nulls out configured channelset.", a.channelSet, null);
    }

    public function testIdProp():void
    {
        var a:MockMessageAgent = new MockMessageAgent();
        // Test that the default id is a UID string.
        assertTrue("Id is a string.", a.id is String);
        assertTrue("Id is not null.", a.id != null);
        assertTrue("Id has non-zero length.", a.id.length > 0);
    }

    public function testMessageAckEventAndChannelConnectEvent():void
    {
        var a:MockMessageAgent = new MockMessageAgent();
        a.destination = "MyTopic";
   	    var msg:AsyncMessage = new AsyncMessage();
   	    a.addEventListener(ChannelEvent.CONNECT, addAsync(verifyChannelConnectEvent, 6000, {"a":a}));
        a.addEventListener(MessageAckEvent.ACKNOWLEDGE, addAsync(verifyMessageAckEvent, 6000, {"msg":msg}));
        a.send(msg);
    }
    private function verifyChannelConnectEvent(event:ChannelEvent, params:Object):void
    {
        assertTrue("Got channel connect event.", true);
        var a:MessageAgent = params.a as MessageAgent;
        assertTrue("Agent connected.", a.connected);
    }
    private function verifyMessageAckEvent(event:MessageAckEvent, params:Object):void
    {
        var msg:AsyncMessage = params.msg as AsyncMessage;
        var ackMsg:AsyncMessage = event.message as AsyncMessage;
        assertEquals("Ack for sent message.", msg.messageId, ackMsg.correlationId);
    }

    public function testMessageFaultEventAndChannelFaultEvent():void
    {
        var a:MockMessageAgent = new MockMessageAgent();
        a.destination = "MyTopic";
        var cs:ChannelSet = new ChannelSet();
        var c:Channel = new AMFChannel("dead", "");
        cs.addChannel(c);
        a.channelSet = cs;
   	    var msg:AsyncMessage = new AsyncMessage();
   	    a.addEventListener(ChannelFaultEvent.FAULT, addAsync(verifyChannelFaultEvent, 6000, {"a":a}));
        a.addEventListener(MessageFaultEvent.FAULT, addAsync(verifyMessageFaultEvent, 6000, {"msg":msg}));
        a.send(msg);
    }
    private function verifyChannelFaultEvent(event:ChannelFaultEvent, params:Object):void
    {
        assertTrue("Got a channel fault event.", true);
        var a:MessageAgent = params.a as MessageAgent;
        assertTrue("Agent not connected.", !a.connected);
    }
    private function verifyMessageFaultEvent(event:MessageFaultEvent, params:Object):void
    {
        var msg:AsyncMessage = params.msg as AsyncMessage;
        var ackMsg:AsyncMessage = event.message as AsyncMessage;
        assertEquals("Fault for sent message.", msg.messageId, ackMsg.correlationId);
    }

    public function testInitialized():void
    {
        var a:MockMessageAgent = new MockMessageAgent();
        a.initialized(null, "mxmlId");
        assertEquals("Initialized assigned agent id.", a.id, "mxmlId");
    }

    public function testInternalSendErrorWhenDestinationNotSet():void
    {
        var a:MockMessageAgent = new MockMessageAgent();
        var msg:AsyncMessage = new AsyncMessage();
        try
        {
            a.send(msg);
            fail("Didn't throw an InvalidDestinationError");
        }
        catch (e:InvalidDestinationError)
        {
            assertTrue("Send with no destination set throws.",
                                                e is InvalidDestinationError);
        }
        catch (e:Error)
        {
            fail("Didn't throw an InvalidDestinationError");
        }
    }
    public function testChannelSetNotNullAfterDisconnect():void
    {
        var a:MockMessageAgent = new MockMessageAgent();
        a.destination = "MyTopic";
        var channelSet:ChannelSet = new ChannelSet(["qa-polling-amf"],false);
        a.channelSet = channelSet;
        var msg:AsyncMessage = new AsyncMessage();
        a.addEventListener(ChannelEvent.CONNECT, addAsync(checkConnectThenDisconnect, 6000, {"a":a}));
        a.send(msg);
    }
    private function checkConnectThenDisconnect(event:ChannelEvent, params:Object):void
    {
        var a:MockMessageAgent = params.a as MockMessageAgent;
        assertTrue("Message agent should be connected", a.connected);
        a.disconnect();
        assertTrue("After disconnect channelSet should not be null.", a.channelSet != null);
    }
}

}