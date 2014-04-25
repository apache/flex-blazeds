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

package mx.rpc.tests
{
import flash.events.Event;
import flexunit.framework.*;
import mx.messaging.*;
import mx.rpc.events.*;
import mx.rpc.*;
import mx.rpc.remoting.mxml.*;

public class MXMLRemoteObjectTest extends TestCase
{

    public static function suite() : TestSuite
    {
        var suite : TestSuite = new TestSuite();
        suite.addTest(new MXMLRemoteObjectTest("testConcurrencySingle"));
        suite.addTest(new MXMLRemoteObjectTest("testConcurrencyLast"));
        suite.addTest(new MXMLRemoteObjectTest("testConcurrencyMultiple"));
        return suite;
    }

    public function MXMLRemoteObjectTest(name : String)
    {
        super(name);
    }

    override public function setUp() :void
    {
        super.setUp();
        ro = new RemoteObject();
    }

    override public function tearDown() :void
    {
        super.tearDown();
    }

    internal var ro:mx.rpc.remoting.mxml.RemoteObject;

    //-------------------------------------------------------------------------
    //
    //            Tests
    //
    //-------------------------------------------------------------------------

    // This test originally added two async handlers at the same time but there's
    // a bug in flexunit that causes this to fail because the second once is called for either event
    // - only a single addAsync can be in play at a time for some reason. 
    // So, I'm removing the first one that verified a result because this test really just needs to 
    // verify that an immediate second call after that first is faulted.
    public function testConcurrencySingle():void
    {
        ro.destination = "WeatherService";
        ro.concurrency = "single";
        var helper:Function = addAsync(verifySingleFault, 5500);
        ro.addEventListener("fault", helper);
        
        // This first call will go out and will still have its result/fault
        // pending when we make the second call.
        var call:Object = ro.getWeatherMap("12345");
        call.param = "first";
        
        // This second call needs to fault because we have an outstanding call
        // and the RemoteObject is tagged to use single concurrency.
        call = ro.getWeatherMap("54321");
        if (call)
            call.param = "second";
    }

    // Verify that the immediate second call while the first is pending faults.
    public function verifySingleFault(event:FaultEvent):void
    {
        assertEquals("second", event.token.param);
        assertEquals("ConcurrencyError", event.fault.faultCode);        
    }

    public function testConcurrencyLast() :void
    {
        ro.destination = "WeatherService";
        ro.concurrency = "last";
        lastCount = 0;
        var helper : Function = addAsync(verifySecondResult, 5000);
        ro.addEventListener("fault", helper);
        ro.addEventListener("result", helper);
        var call : Object = ro.getWeatherMap("12345");
        call.param = "first";
        call = ro.getWeatherMap("54321");
        call.param = "second";
    }

    private var lastCount : int;
    private function verifySecondResult(event : AbstractEvent) :void
    {
        if (event.type == "fault")
        {
            var fault:Fault = FaultEvent(event).fault;
            fail("A fault was thrown. " + fault.faultCode + ". " + fault.faultString);
            return;
        }

        if (lastCount == 1)
        {
            fail("verifySecondResult called twice");
        }
        assertEquals("second", event.token.param);
        ++lastCount;
    }

    public function testConcurrencyMultiple() :void
    {
        ro.destination = "WeatherService";
        multipleCount = 0;
        multipleFirstHelper = addAsync(verifyMultiple, 5000);
        multipleSecondHelper = addAsync(verifyMultiple, 5000);
        ro.addEventListener("fault", multipleFirstHelper);
        ro.addEventListener("result", multipleFirstHelper);
        var call : Object = ro.getWeatherMap("12345");
        call.param = "first";
        call = ro.getWeatherMap("54321");
        call.param = "second";
    }

    private var multipleCount : int;
    private var multipleResults : Array = [];
    private var multipleFirstHelper : Function;
    private var multipleSecondHelper : Function;
    private function verifyMultiple(event : AbstractEvent) :void
    {
        if (event.type == "fault")
        {
            var fault:Fault = FaultEvent(event).fault;
            fail("A fault was thrown. " + fault.faultCode + ". " + fault.faultString);
            return;
        }

        ++multipleCount;
        multipleResults.push(event.token.param);

        switch (multipleCount)
        {
            case 1 :
                event.target.removeEventListener("result", multipleFirstHelper);
                event.target.removeEventListener("fault", multipleFirstHelper);
                event.target.addEventListener("fault", multipleSecondHelper);
                event.target.addEventListener("result", multipleSecondHelper);
                break;
            case 2 :
                multipleResults.sort();
                assertEquals("first call did not return", "first", multipleResults[0]);
                assertEquals("second call did not return", "second", multipleResults[1]);
                break;
            default: fail("multipleCount not 1 or 2");
        }
    }

    public function testShowBusyCursorTrue() :void
    {

    }

    public function testShowBusyCursorFalse() :void
    {

    }

    public function testShowBusyCursorTrueMultiple() :void
    {

    }

    public function testValidatedRequest() :void
    {

    }

    public function testValidatedRequestButParamsSent() :void
    {

    }

}

}