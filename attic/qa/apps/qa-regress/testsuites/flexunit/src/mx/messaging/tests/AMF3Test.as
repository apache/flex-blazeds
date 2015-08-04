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

import flash.errors.*;
import flash.events.*;
import flash.net.*;
import flash.utils.*;
import flash.xml.*;

import flexunit.framework.*;

import mx.collections.ArrayCollection;
import mx.logging.ILogger;
import mx.logging.Log;
import mx.messaging.tests.helpers.EqualityHelper;
import mx.messaging.tests.helpers.AMFTestResponder;
import mx.messaging.tests.helpers.TestResultEvent;
import mx.messaging.tests.helpers.TestStatusEvent;
import mx.utils.ObjectUtil;
/**
 * Test datatype serialization and deserialization over AMF0. This test extends AMF3Test and 
 * uses most of the same test methods but the tests are with the ObjectEncoding for the NetConnection
 * set to AMF0 instead of AMF3.  
 */
public class AMF3Test extends TestCase
{
    ////////////////////////////////////////////////////////////////////////////
    //
    // Variables
    //
    //////////////////////////////////////////////////////////////////////////// 	
    protected var nc:NetConnection;
    protected var responder:AMFTestResponder;
    protected var service:String;
    protected var log:ILogger;
    protected var equalityHelper:EqualityHelper;
	////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    //////////////////////////////////////////////////////////////////////////// 
    public function AMF3Test(methodName:String)
    {
        super(methodName);
        log = Log.getLogger("mx.messaging.tests.AMF3Test");
    }
	////////////////////////////////////////////////////////////////////////////
    //
    // TestSuite
    //
    ////////////////////////////////////////////////////////////////////////////   
    public static function suite():TestSuite
    {
        var tests:TestSuite = new TestSuite();
        tests.addTest(new AMF3Test("testAssociativeArray"));
        tests.addTest(new AMF3Test("testEmptyArray"));
        tests.addTest(new AMF3Test("testDenseStrictArray"));
        tests.addTest(new AMF3Test("testSparseStrictArray"));
        tests.addTest(new AMF3Test("testEmptyObject"));
        tests.addTest(new AMF3Test("testBooleanTrue"));
        tests.addTest(new AMF3Test("testBooleanFalse"));
        tests.addTest(new AMF3Test("testBigDecimal"));
        tests.addTest(new AMF3Test("testBigInteger"));
        tests.addTest(new AMF3Test("testDateNull"));
        tests.addTest(new AMF3Test("testDateEpoch"));
        tests.addTest(new AMF3Test("testDate"));
        tests.addTest(new AMF3Test("testSQLDate"));
        tests.addTest(new AMF3Test("testSQLTime"));
        tests.addTest(new AMF3Test("testSQLTimestamp"));
        tests.addTest(new AMF3Test("testCalendar"));

        tests.addTest(new AMF3Test("testXML"));
        tests.addTest(new AMF3Test("testXMLDocument"));
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
        nc = createNetConnection();
        nc.addEventListener(SecurityErrorEvent.SECURITY_ERROR, handleSecurityError);
        nc.addEventListener(NetStatusEvent.NET_STATUS, handleNetStatus);
        service = "qa.echoservice.Echo";
        equalityHelper = new EqualityHelper();
    }
    ////////////////////////////////////////////////////////////////////////////
    //
    // Tear Down
    //
    ////////////////////////////////////////////////////////////////////////////   
    override public function tearDown():void
    {
        nc.removeEventListener(SecurityErrorEvent.SECURITY_ERROR, handleSecurityError);
        nc.removeEventListener(NetStatusEvent.NET_STATUS, handleNetStatus);
        nc = null;
        service = null;
        equalityHelper = null;
    }
    ////////////////////////////////////////////////////////////////////////////
    //
    // Tests
    //
    ////////////////////////////////////////////////////////////////////////////   
    public function testEmptyArray():void
    {
        var method:String = service + ".echoObject";
        var expected:Array = new Array();
        registerResponder({method:method, expected:expected});
        nc.call(method, responder, expected);
    }

    public function testDenseStrictArray():void
    {
        var method:String = service + ".echoObject";
        var expected:Array = [0, 1, "two", {value:3}];
        registerResponder({method:method, expected:expected});
        nc.call(method, responder, expected);
    }

    public function testSparseStrictArray():void
    {
        var method:String = service + ".echoObject";
        var input:Array = [];
        input[0] = 0;
        input[4] = "four";
        input[9] = {value:9};
        var expected:Object = {"0":0, "4":"four", "9":{value:9}};
        registerResponder({method:method, expected:expected});
        nc.call(method, responder, input);
    }

    public function testAssociativeArray():void
    {
        var method:String = service + ".echoObject";
        var input:Array = [];
        input["Paris"] = "France";
        input["Rome"] = "Italy";
        input["Berlin"] = "Germany";
        var expected:Object = {Paris:"France", Rome:"Italy", Berlin:"Germany"};
        registerResponder({method:method, expected:expected});
        nc.call(method, responder, input);
    }

    public function testEmptyObject():void
    {
        var method:String = service + ".echoObject";
        var expected:Object = new Object();
        registerResponder({method:method, expected:expected});
        nc.call(method, responder, expected);
    }

    public function testBooleanTrue():void
    {
        var method:String = service + ".echoBoolean";
        var expected:Boolean = true;
        registerResponder({method:method, expected:expected});
        nc.call(method, responder, expected);
    }

    public function testBooleanFalse():void
    {
        var method:String = service + ".echoBoolean";
        var expected:Boolean = false;
        registerResponder({method:method, expected:expected});
        nc.call(method, responder, expected);
    }

    public function testBigDecimal():void
    {
        var method:String = service + ".echoBigDecimal";
        var expected:String = "12345678910111213.1415161718192021222324252627282930";
        registerResponder({method:method, expected:expected});
        nc.call(method, responder, expected);
    }

    public function testBigInteger():void
    {
        var method:String = service + ".echoBigInteger";
        var expected:String = "1234567891011121314151617181920";
        registerResponder({method:method, expected:expected});
        nc.call(method, responder, expected);
    }

    public function testDateNull():void
    {
        var method:String = service + ".echoDate";
        var expected:Object = null;
        registerResponder({method:method, expected:expected});
        nc.call(method, responder, expected);
    }

    public function testDateEpoch():void
    {
        var method:String = service + ".echoDate";
        var expected:Date = new Date();
        expected.time = 0;
        registerResponder({method:method, expected:expected});
        nc.call(method, responder, expected);
    }

    public function testDate():void
    {
        var method:String = service + ".echoDate";
        var expected:Date = new Date();
        registerResponder({method:method, expected:expected});
        nc.call(method, responder, expected);
    }

    public function testSQLDate():void
    {
        var method:String = service + ".echoSQLDate";
        var expected:Date = new Date();
        registerResponder({method:method, expected:expected});
        nc.call(method, responder, expected);
    }

    public function testSQLTime():void
    {
        var method:String = service + ".echoTime";
        var expected:Date = new Date();
        registerResponder({method:method, expected:expected});
        nc.call(method, responder, expected);
    }

    public function testSQLTimestamp():void
    {
        var method:String = service + ".echoTimestamp";
        var expected:Date = new Date();
        registerResponder({method:method, expected:expected});
        nc.call(method, responder, expected);
    }

    public function testCalendar():void
    {
        var method:String = service + ".echoCalendar";
        var expected:Date = new Date();
        registerResponder({method:method, expected:expected});
        nc.call(method, responder, expected);
    }

    public function testXML():void
    {
        var method:String = service + ".echoXML";
        var expected:XML = <test xmlns:t1="http://www.macromedia.com/2005/test"><t1:one won="wunderbar!">1</t1:one><two to="too">2</two></test>;
        registerResponder({method:method, expected:expected});
        nc.call(method, responder, expected);
    }

    public function testXMLDocument():void
    {
        var method:String = service + ".echoXML";
        var expected:XMLDocument = new XMLDocument("<test xmlns:t1=\"http://www.macromedia.com/2005/test\"><t1:one won=\"wunderbar!\">1</t1:one><two to=\"too\">2</two></test>");
        registerResponder({method:method, expected:expected});
        nc.call(method, responder, expected);
    }
    ////////////////////////////////////////////////////////////////////////////
    //
    // Helper methods
    //
    ////////////////////////////////////////////////////////////////////////////       
    public function AppendToGatewayUrl(value:String):void
    {        
        // ignore
    }

    protected function createNetConnection():NetConnection
    {
        var netConnection:NetConnection = new NetConnection();
        netConnection.objectEncoding = ObjectEncoding.AMF3;
        netConnection.client = this;
        netConnection.connect("http://localhost:8400/qa-regress/messagebroker/amf");
        return netConnection;
    }

    protected function registerResponder(test:Object, timeout:int = 5000):void
    {
        responder = new AMFTestResponder();
        var handler:Function = addAsync(handleResult, timeout, test);
        responder.addEventListener(TestResultEvent.RESULT, handler);
        responder.addEventListener(TestStatusEvent.STATUS, handler);
    }

    protected function handleResult(event:Event, test:Object):void
    {
        if (event is TestResultEvent)
        {
            equalityHelper.reset();
            var result:Object = TestResultEvent(event).result;
            assertTrue(equalityHelper.testEquality(result, test.expected));
        }
        else if (event is TestStatusEvent)
        {
            handleUnexpectedStatus(TestStatusEvent(event), test);
        }
        else
        {
            fail("Test failed with an unknown event type " + ObjectUtil.toString(event));
        }
    }

    protected function handleUnexpectedStatus(event:TestStatusEvent, test:Object):void
    {
        fail("Unexpected status encountered calling method '" + test.method + "'\n" + ObjectUtil.toString(event));
    }

    protected function handleSecurityError(event:SecurityErrorEvent):void
    {
        fail("Unexpected security error encountered\n" + ObjectUtil.toString(event));
    }

    protected function handleNetStatus(event:NetStatusEvent):void
    {
        fail("Unexpected net status event encountered\n" + ObjectUtil.toString(event));
    }
}



}