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
import flash.utils.getTimer;

import flexunit.framework.*;

import mx.controls.*;
import mx.core.mx_internal;
import mx.messaging.*;
import mx.messaging.channels.*;
import mx.messaging.config.*;
import mx.messaging.errors.*;
import mx.messaging.events.*;
import mx.messaging.messages.*;
import mx.rpc.events.*;
import mx.rpc.remoting.*;

use namespace mx_internal;

/**
 *  Tests for mx.messaging.Consumer. This test extends ConfigurationBasedTestCase which has it's own
 *  copy of the ServerConfig xml object and doesn't use the one provided by the server. Be aware that 
 *  if you add a test here that uses a new Destination or Channel defined in a server config file you 
 *  will also need to add this Destination or Channel to the ServerConfig object in 
 *  ConfigurationBasedTestCase
 */
public class ConsumerTest extends ConfigurationBasedTestCase
{
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////    
    
    public function ConsumerTest(methodName:String)
    {
        super(methodName);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    //
    // Setup
    //
    ////////////////////////////////////////////////////////////////////////////    

    /**
     *  Resets server config back to initial state.
     */
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
        tests.addTest(new ConsumerTest("testClientIdProp"));      
        tests.addTest(new ConsumerTest("testDestinationProp"));      
        tests.addTest(new ConsumerTest("testResubscribeAttemptsProp")); 
        tests.addTest(new ConsumerTest("testSetResubscribeIntervalNegativeError")); 
        tests.addTest(new ConsumerTest("testSelectorProp"));  
        tests.addTest(new ConsumerTest("testTimestampProp"));       
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

    public function testClientIdProp():void
    {
        var c:Consumer = new Consumer();
        c.setClientId("newId");
        assertEquals("ClientId set.", c.clientId, "newId");
    }
    
    public function testDestinationProp():void
    {
        var c:Consumer = new Consumer();
        c.destination = "MyTopic";
        assertEquals("Destination set.", c.destination, "MyTopic");
    }
    
    public function testResubscribeAttemptsProp():void
    {
        var c:Consumer = new Consumer();
        c.resubscribeAttempts = -1;
        assertEquals("ResubscribeAttempts set.", c.resubscribeAttempts, -1);
    }
   	
   	public function testResubscribeIntervalProp():void
    {
        var c:Consumer = new Consumer();
        c.resubscribeInterval = 1000;
        assertEquals("ResubscribeAttempts set.", c.resubscribeInterval, 1000);
    }
    
    public function testSetResubscribeIntervalNegativeError():void
    {
        var c:Consumer = new Consumer();        
        try
        {
            c.resubscribeInterval = -1000;
            fail("Didn't throw an ArgumentError");
        }
        catch (e:ArgumentError)
        {
            assertTrue("Negative resubscribeInterval throws.", true);
        }
        catch (e:Error)
        {
            fail("Didn't throw an ArgumentError");
        }
    }
    
    public function testSelectorProp():void
    {
        var c:Consumer = new Consumer();
        c.selector = "age > 50";
        assertEquals("Selector set.", c.selector, "age > 50");
    }
    
    public function testTimestampProp():void
    {
        var c:Consumer = new Consumer();
        var time:int = getTimer();
        c.timestamp = time;
        assertEquals("Timestamp set.", c.timestamp, time);
    }    
}
}