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

import flash.utils.Timer;
import flash.events.*;
import flexunit.framework.*;
import mx.core.mx_internal;
import mx.messaging.*;
import mx.messaging.messages.*;
import mx.messaging.events.*;
import mx.messaging.errors.*;
import mx.messaging.channels.*
import mx.messaging.config.*;
import mx.messaging.tests.helpers.*;

use namespace mx_internal;
/**
 * Test features of the AMFChannel. This test extends ConfigurationBasedTestCase which has it's own
 * copy of the ServerConfig xml object and doesn't use the one provided by the server. Be aware that 
 * if you add a test here that uses a new Destination or Channel defined in a server config file you 
 * will also need to add this Destination or Channel to the ServerConfig object in 
 * ConfigurationBasedTestCase
 */
public class AMFChannelTest extends ConfigurationBasedTestCase
{
	////////////////////////////////////////////////////////////////////////////
    //
    // Variables
    //
    //////////////////////////////////////////////////////////////////////////// 	
	private var amfPollingChannelSet:ChannelSet;
	private var amfHangingSet:ChannelSet;
	////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    //////////////////////////////////////////////////////////////////////////// 	
    public function AMFChannelTest(methodName:String)
    {
        super(methodName);
        
        amfPollingChannelSet = new ChannelSet();
        amfPollingChannelSet.addChannel(ServerConfig.getChannel("qa-polling-amf"));
        
        amfHangingSet = new ChannelSet();
        amfHangingSet.addChannel(ServerConfig.getChannel("qa-hanging-amf"));
    }
	////////////////////////////////////////////////////////////////////////////
    //
    // TestSuite
    //
    //////////////////////////////////////////////////////////////////////////// 
    public static function suite():TestSuite
    {
        var tests:TestSuite = new TestSuite();
        tests.addTest(new AMFChannelTest("testAMFChannelConnectTimeout"));
        tests.addTest(new AMFChannelTest("testAMFChannelConnectTimeoutWith404"));        
        tests.addTest(new AMFChannelTest("testOneConsumerOneProducerOnSameChannelPolling"));
        tests.addTest(new AMFChannelTest("testOneConsumerOneProducerOnSameChannelMultipleSendsPolling"));
        return tests;
    }
	////////////////////////////////////////////////////////////////////////////
    //
    // SetUp
    //
    //////////////////////////////////////////////////////////////////////////// 
    override public function setUp():void
    {
        super.setUp();
    }
	////////////////////////////////////////////////////////////////////////////
    //
    // Tests
    //
    //////////////////////////////////////////////////////////////////////////// 
    public function testOneConsumerOneProducerOnSameChannelPolling():void
    {
        var pro:Producer = new Producer();
        pro.channelSet = amfPollingChannelSet;
        pro.destination = "MyTopic";

        var cons:Consumer = new Consumer();
        cons.channelSet = amfPollingChannelSet;
        cons.destination = "MyTopic";

        var pollingHelper:ConsumerPollingHelper = new ConsumerPollingHelper(cons, pro);
        pollingHelper.addEventListener(TestEvent.COMPLETE, addAsync(testComplete, 6000));
    }

    public function testOneConsumerOneProducerOnSameChannelMultipleSendsPolling():void
    {
        // create an array of 5 messages that we will test for during a poll
        var msgs:Array = [];
        var msg:AsyncMessage = new AsyncMessage();
        msg.body.text = "testPolling 1";
        msgs.push(msg);
        msg = new AsyncMessage();
        msg.body.text = "testPolling 2";
        msgs.push(msg);
        msg = new AsyncMessage();
        msg.body.text = "testPolling 3";
        msgs.push(msg);
        msg = new AsyncMessage();
        msg.body.text = "testPolling 4";
        msgs.push(msg);

        var cons:Consumer = new Consumer();
        cons.channelSet = amfPollingChannelSet;
        cons.destination = "MyTopic";
        
        var pro:Producer = new Producer();
        pro.channelSet = amfPollingChannelSet;
        pro.destination = "MyTopic";

        var pollTester:PollingTester = new PollingTester(cons, pro, msgs);
        pollTester.addEventListener(TestEvent.COMPLETE, addAsync(testComplete, 10000));
    }
	
	/**
	 * Test that our channel faults connecting due to timeout.
	 */
	public function testAMFChannelConnectTimeout():void
	{
	    var pro:Producer = new Producer();
	    pro.destination = "MyHangingAMFTopic";
	    // This channel set is configured to timeout a connect attempt after 1 second.
	    // The server endpoint sleeps for 3 seconds before sending a 404 response, so
	    // we should hit the connect timeout quickly which will increment the channel fault count.
	    // The 404 should not trigger an additional channel fault event because we've already given
	    // up.
	    var hangingChannel:Channel = ServerConfig.getChannel("qa-hanging-amf");
	    var cs:ChannelSet = new ChannelSet();
        cs.addChannel(hangingChannel);
	    pro.channelSet = cs;
	    
	    // Local variable to store the count of channel faults.
	    var faultCount:int = 0;
	    
	    cs.addEventListener(ChannelFaultEvent.FAULT, function(event:ChannelFaultEvent):void
	                                                 {
	                                                     faultCount++;	                                                            
	                                                 });
	    pro.send(new AsyncMessage());
	    var testChannelFaults:Function = addAsync(function(event:Event):void
                                	             {
                                	                 // We should only have gotten one channel fault.
                                	                 assertEquals("Expecting only 1 channel fault event.", 1, faultCount);
                                	             }, 
                                	             5000 /* Trigger this in 3 seconds to give the Channel a chance to timeout */);
                                	             
        // We need to trigger this async helper in 3 seconds.
        var t:Timer = new Timer(3000, 1);
        t.addEventListener(TimerEvent.TIMER, function(event:TimerEvent):void 
                                             {
                                                testChannelFaults(event);                                                 
                                             });
        t.start();
	}
	
	/** 
	 * Test that our channel faults connecting due to timeout.
	 */
	public function testAMFChannelConnectTimeoutWith404():void
	{
	    var pro:Producer = new Producer();
	    pro.destination = "MyHangingAMFTopic";
	    // This channel set is configured to timeout a connect attempt after 1 second.
	    // The server endpoint sleeps for 3 seconds before sending a 404 response, so
	    // we should hit the connect timeout quickly which will increment the channel fault count.
	    // The 404 should not trigger an additional channel fault event because we've already given
	    // up.
	    var hangingChannel:Channel = ServerConfig.getChannel("qa-hanging-amf");
	    var originalURI:String = hangingChannel.uri;
	    hangingChannel.uri += "?404=true";
	    var cs:ChannelSet = new ChannelSet();
        cs.addChannel(hangingChannel);
	    pro.channelSet = cs;
	    
	    // Local variable to store the count of channel faults.
	    var faultCount:int = 0;
	    
	    cs.addEventListener(ChannelFaultEvent.FAULT, function(event:ChannelFaultEvent):void
	                                                 {
	                                                     faultCount++;	                                                            
	                                                 });
	    pro.send(new AsyncMessage());
	    var testChannelFaults:Function = addAsync(function(event:Event):void
                                	             {
                                	                 // We should only have gotten one channel fault.
                                	                 assertEquals("Expecting only 1 channel fault event.", 1, faultCount);
                                	             }, 
                                	             5000 /* Trigger this in 3 seconds to give the Channel a chance to timeout */);
                                	             
        // We need to trigger this async helper in 3 seconds.
        var t:Timer = new Timer(3000, 1);
        t.addEventListener(TimerEvent.TIMER, function(event:TimerEvent):void 
                                             { 
                                                hangingChannel.uri = originalURI;
                                                testChannelFaults(event);                                                 
                                             });
        t.start();
	}	

	////////////////////////////////////////////////////////////////////////////
    //
    // Helpers
    //
    //////////////////////////////////////////////////////////////////////////// 
    private function testComplete(event:TestEvent):void
    {
        assertTrue(event.passed);
    }
        
}

}