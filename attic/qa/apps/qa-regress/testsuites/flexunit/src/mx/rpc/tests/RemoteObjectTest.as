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

import mx.logging.Log;

import flexunit.framework.*;
import mx.messaging.*;
import mx.messaging.errors.*;
import mx.messaging.tests.*;
import mx.rpc.events.*;
import mx.rpc.remoting.*;
import mx.rpc.*;

public class RemoteObjectTest extends ConfigurationBasedTestCase
{

    public static function suite() : TestSuite
    {
        var suite : TestSuite = new TestSuite();
        suite.addTest(new RemoteObjectTest("testDefaultResultEvent"));
        suite.addTest(new RemoteObjectTest("testDefaultFaultEvent"));
        suite.addTest(new RemoteObjectTest("testResultProperty"));
        suite.addTest(new RemoteObjectTest("testOperationResultEvent"));
        suite.addTest(new RemoteObjectTest("testOperationFaultEvent"));
        suite.addTest(new RemoteObjectTest("testOperationResultNotDefault"));
        suite.addTest(new RemoteObjectTest("testOperationFaultNotDefault"));
        suite.addTest(new RemoteObjectTest("testSendNoArgumentsParams"));
        suite.addTest(new RemoteObjectTest("testSendArgumentsNoParams"));
        suite.addTest(new RemoteObjectTest("testSendArgumentsParams"));
        suite.addTest(new RemoteObjectTest("testArgumentsAsArray"));
        suite.addTest(new RemoteObjectTest("testResultCall"));
        suite.addTest(new RemoteObjectTest("testFaultCall"));
        suite.addTest(new RemoteObjectTest("testResultMessage"));
        suite.addTest(new RemoteObjectTest("testFaultMessage"));
        suite.addTest(new RemoteObjectTest("testFaultMessageWithExtendedData"));
        suite.addTest(new RemoteObjectTest("testCancel"));
        return suite;
    }

    public function RemoteObjectTest(name : String)
    {
        super(name);
    }

    override public function setUp() :void
    {
        super.setUp();
        ro = getRemoteObject();
    }

    override public function tearDown() :void
    {
        ro = null;
    }

    public function getRemoteObject() : RemoteObject
    {
        return new RemoteObject();
    }

    public var ro : RemoteObject;

    /**
    *  Test valid destination and operation, remoteObject should dispatch result event
    **/
    public function testDefaultResultEvent() :void
    {
        ro.destination = "WeatherService";
        var helper : Function = addAsync(resultExpected, 5000);
        ro.addEventListener("result", helper);
        ro.addEventListener("fault", helper);
        ro.getWeatherMap("55555");
    }

    private function resultExpected(event : AbstractEvent) :void
    {
        assertEquals("result", event.type);
        assertTrue("event should be ResultEvent", event is ResultEvent);
    }

    /**
    *  Test invalid destination, remoteObject should dispatch fault event
    **/
    public function testDefaultFaultEvent() :void
    {
        ro.destination = "bogus";
        var helper : Function = addAsync(faultExpected, 5000);
        ro.addEventListener("result", helper);
        ro.addEventListener("fault", helper);
        ro.getWeatherMap("55555");
    }

    private function faultExpected(event : AbstractEvent) :void
    {
        assertEquals("fault", event.type);
        assertTrue("event should be FaultEvent", event is FaultEvent);
    }
    
    /**
    *     Test valid destination and operation, remoteObject should dispatch result event,
    *     and operation.lastResult should have value 
    **/
    public function testResultProperty() :void
    {
        ro.destination = "WeatherService";
        var helper : Function = addAsync(resultPropertyExpected, 5000, ro.getWeatherMap);
        ro.addEventListener("result", helper);
        ro.addEventListener("fault", helper);
        ro.getWeatherMap("55555");
    }
    
    /**
    *     Test valid destination and operation, remoteObject should dispatch result event,
    *     and operation.lastResult should equal to event.result  
    **/
    private function resultPropertyExpected(event : AbstractEvent, operation : Operation) :void
    {
        assertTrue("event si ResultEvent", event is ResultEvent);
        assertNotNull("event.result should exist", ResultEvent(event).result);
        assertNotNull("operation.lastResult should exist", operation.lastResult);
        assertEquals("operation.lastResult should equal event.result", operation.lastResult, ResultEvent(event).result);
    }
    
    /**
    *     Test valid destination and operation, remoteObject should dispatch result event ,
    *     and operation.lastResult should equal to event.result  
    **/
    public function testOperationResultEvent() :void
    {
        ro.destination = "WeatherService";
        var helper : Function = addAsync(resultExpected, 5000);
        ro.getWeatherMap.addEventListener("result", helper);
        ro.getWeatherMap.addEventListener("fault", helper);
        ro.getWeatherMap("55555");
    }
    
    /**
    *     Test invalid destination, operation should dispatch fault event.
    **/
    public function testOperationFaultEvent() :void
    {
        ro.destination = "bogus";
        var helper : Function = addAsync(faultExpected, 5000);
        ro.getWeatherMap.addEventListener("result", helper);
        ro.getWeatherMap.addEventListener("fault", helper);
        ro.getWeatherMap("55555");
    }
    
    /**
    *     Test valid destination and operation. 
    *     Operation should dispatch result event, but not remoteobject
    **/
    public function testOperationResultNotDefault() :void
    {
        ro.destination = "WeatherService";
        var helper : Function = addAsync(opResultEventExpected, 5000);
        ro.addEventListener("result", helper);
        ro.addEventListener("fault", helper);
        var op : Operation = ro.getWeatherMap;
        op.addEventListener("result", helper);
        op.addEventListener("fault", helper);
        ro.getWeatherMap("55555");
    }

    private function opResultEventExpected(event : AbstractEvent) :void
    {
        resultExpected(event);
        assertTrue("event.target is Operation", event.target is Operation);
    }
    
    /**
    *     Test invalid destination and operation. 
    *     Operation should dispatch fault event, but not remoteobject
    **/
    public function testOperationFaultNotDefault() :void
    {
        ro.destination = "bogus";
        var helper : Function = addAsync(opFaultEventExpected, 5000);
        ro.addEventListener("result", helper);
        ro.addEventListener("fault", helper);
        var op : Operation = ro.getWeatherMap;
        op.addEventListener("result", helper);
        op.addEventListener("fault", helper);
        ro.getWeatherMap("55555");
    }

    private function opFaultEventExpected(event : AbstractEvent) :void
    {
        faultExpected(event);
        assertTrue("event.target is Operation", event.target is Operation);
    }
    
    /**
    *     Test valid destination and operation passing parameter
    *  
    **/
    public function testSendNoArgumentsParams() :void
    {
        ro.destination = "WeatherService";
        var helper : Function = addAsync(verifyTemperature, 5000, "50");
        ro.addEventListener("result", helper);
        ro.addEventListener("fault", helper);
        ro.getWeatherMap("94103");
    }

    private function verifyTemperature(event : ResultEvent, temp : String) :void
    {
        assertEquals(temp, event.result.temperature);
    }
    
    /**
    *     Test valid destination and operation passing operation.arguments
    *  
    **/
    public function testSendArgumentsNoParams() :void
    {
        ro.destination = "WeatherService";
        var helper : Function = addAsync(verifyTemperature, 5000, "50");
        ro.addEventListener("result", helper);
        ro.addEventListener("fault", helper);
        var op : Operation = ro.getWeatherMap;
        op.arguments = {arg1: "94103"};
        op.argumentNames = ["arg1"];
        op.send();
    }
    
    /**
    *     Test valid destination and operation passing both operation.arguments and parameter
    *  
    **/
    public function testSendArgumentsParams() :void
    {
        ro.destination = "WeatherService";
        var helper : Function = addAsync(verifyTemperature, 5000, "50");
        ro.addEventListener("result", helper);
        ro.addEventListener("fault", helper);
        var op : Operation = ro.getWeatherMap;
        op.arguments = {arg1: "02912"};
        op.argumentNames = ["arg1"];
        ro.getWeatherMap("94103");
    }
    
    /**
    *     Test valid destination and operation passing operation.arguments as array
    *  
    **/
    public function testArgumentsAsArray() :void
    {
        ro.destination = "WeatherService";
        var helper : Function = addAsync(verifyTemperature, 5000, "50");
        ro.addEventListener("result", helper);
        ro.addEventListener("fault", helper);
        var op : Operation = ro.getWeatherMap;
        op.arguments = ["94103"];
        op.send();
    }
    
    /**
    *     Test assigning property to AsyncToken and retrieving the value in result event
    *    
    **/
    public function testResultCall() :void
    {
        ro.destination = "WeatherService";
        var helper : Function = addAsync(verifyCall, 5000, "resultCall");
        ro.addEventListener("result", helper);
        ro.addEventListener("fault", helper);
        var call : Object = ro.getWeatherMap("94103");
        assertNotNull("Call object from execute is null", call);
        call.param = "resultCall";
    }
    
    /**
    *     Test assigning property to AsyncToken and retrieving the value in fault event
    *    
    **/
    public function testFaultCall() :void
    {
        ro.destination = "WeatherService";
        var helper : Function = addAsync(verifyCall, 5000, "faultCall");
        ro.addEventListener("result", helper);
        ro.addEventListener("fault", helper);
        var call : Object = ro.getWeatherMap();
        assertNotNull("Call object from execute is null", call);
        call.param = "faultCall";
    }

    private function verifyCall(event : Object, expected : String):void
    {
        var call : Object = event.token;
        assertNotNull("Call object from event is null", call);
        assertEquals(expected, call.param);
    }
    
    /**
    *     Test response message has same ids as request message in result event
    *    
    **/
    public function testResultMessage() :void
    {
        ro.destination = "WeatherService";
        var call : Object = ro.getWeatherMap("55555");
        assertNotNull(call.message);
        var helper : Function = addAsync(verifyMessage, 5000, call.message.messageId);
        ro.addEventListener("result", helper);
        ro.addEventListener("fault", helper);
    }
    
    /**
    *     Test response message has same ids as request message in fault event
    *    
    **/
    public function testFaultMessage() :void
    {
        ro.destination = "WeatherService";
        var call : Object = ro.getWeatherMap();
        assertNotNull(call.message);
        var helper : Function = addAsync(verifyMessage, 5000, call.message.messageId);
        ro.addEventListener("result", helper);
        ro.addEventListener("fault", helper);
    }

    private function verifyMessage(event : Object, expectedId : String) :void
    {
        assertNotNull(event.message);
        assertNotNull(event.token.message);
        assertEquals(expectedId, event.token.message.messageId);
        assertEquals(expectedId, event.message.correlationId);
    }
    
    /**
    *     Test MessageException extended Data. 
    *    
    **/
    public function testFaultMessageWithExtendedData() :void
    {
        ro.destination = "WeatherService";
        var data : String = "Extra data.";
        var call : Object = ro.generateMessageExceptionWithExtendedData(data);
        assertNotNull(call.message);
        var helper : Function = addAsync(verifyExtendedData, 5000, data);
        ro.addEventListener("result", helper);
        ro.addEventListener("fault", helper);
    }

    private function verifyExtendedData(event : Object, expectedData: String) :void
    {
        assertNotNull(event.message);
        assertNotNull(event.token.message);
        assertEquals(expectedData, event.message.extendedData.extraData);
    }

    private var cancelTestCount : int;
    /**
    *     Test cancel remote call. When second call is cancel, should not have result nor fault
    **/
    public function testCancel() :void
    {
        ro.destination = "WeatherService";
        cancelTestCount = 0;
        var helper : Function = addAsync(verifyCancel, 5000);
        ro.addEventListener("result", helper);
        ro.addEventListener("fault", helper);
        var call : Object = ro.getWeatherMap("55555");
        call.callPos = "first";
        call = ro.getWeatherMap("55555");
        call.callPos = "second";
        ro.getWeatherMap.cancel();
    }

    private function verifyCancel(event : ResultEvent) :void
    {
        cancelTestCount++;
        if (cancelTestCount > 1)
        {
            fail("Cancel did not occur, verifyCancel called more than once");
        }
        assertEquals("Cancel should cancel the 'second'", "first", event.token.callPos);
    }
}

}
