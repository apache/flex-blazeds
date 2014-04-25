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

import mx.messaging.tests.helpers.EqualityHelper;

import flexunit.framework.*;
/**
 * Test datatype serialization and deserialization over AMF0. This test extends AMF3Test and 
 * uses most of the same test methods but the tests are with the ObjectEncoding for the NetConnection
 * set to AMF0 instead of AMF3.  
 */
public class AMF0Test extends AMF3Test
{
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    //////////////////////////////////////////////////////////////////////////// 
    public function AMF0Test(methodName:String)
    {
        super(methodName);
    }
	////////////////////////////////////////////////////////////////////////////
    //
    // TestSuite
    //
    ////////////////////////////////////////////////////////////////////////////   
    
    public static function suite():TestSuite
    {
        var tests:TestSuite = new TestSuite();
        tests.addTest(new AMF0Test("testAssociativeArray"));
        tests.addTest(new AMF0Test("testEmptyArray"));
        tests.addTest(new AMF0Test("testDenseStrictArray"));
        tests.addTest(new AMF0Test("testSparseStrictArray"));
        tests.addTest(new AMF0Test("testEmptyObject"));
        tests.addTest(new AMF0Test("testBooleanTrue"));
        tests.addTest(new AMF0Test("testBooleanFalse"));
        tests.addTest(new AMF0Test("testBigDecimal"));
        tests.addTest(new AMF0Test("testBigInteger"));
        tests.addTest(new AMF0Test("testDateNull"));
        tests.addTest(new AMF0Test("testDateEpoch"));
        tests.addTest(new AMF0Test("testDate"));
        tests.addTest(new AMF0Test("testSQLDate"));
        tests.addTest(new AMF0Test("testSQLTime"));
        tests.addTest(new AMF0Test("testSQLTimestamp"));
        tests.addTest(new AMF0Test("testCalendar"));

        // E4X XML is not supported by AMF 0
        //tests.addTest(new AMF0Test("testXML"));
        
        // XMLDocument does not strip off whitespace that is present
        // when an XML Declaration is added above the root node.
        tests.addTest(new AMF0Test("testXMLDocument"));
        return tests;
    }
    ////////////////////////////////////////////////////////////////////////////
    //
    // Tests
    //
    ////////////////////////////////////////////////////////////////////////////   
       
    override public function testSparseStrictArray():void
    {
        var method:String = service + ".echoObject";
        var input:Array = [];
        input[0] = 0;
        input[4] = "four";
        input[9] = {value:9};
        // Java does not have the concept of undefined, so null is 
        // returned instead for the undefined elements created by
        // the sparse Array on serialization...
        //var expected:Array = [0, undefined, undefined, undefined, "four", undefined, undefined, undefined, undefined, {value:9}];
        var expected:Array = [0, null, null, null, "four", null, null, null, null, {value:9}];
        registerResponder({method:method, expected:expected});
        nc.call(method, responder, input);
    }
    ////////////////////////////////////////////////////////////////////////////
    //
    // Helper methods
    //
    ////////////////////////////////////////////////////////////////////////////   
    override protected function createNetConnection():NetConnection
    {
        var netConnection:NetConnection = new NetConnection();
        netConnection.client = this;
        netConnection.objectEncoding = ObjectEncoding.AMF0;
        netConnection.connect("http://localhost:8400/qa-regress/messagebroker/amf");
        return netConnection;
    }
}

}