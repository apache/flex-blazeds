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

import flexunit.framework.*;
import mx.rpc.remoting.*;
import mx.rpc.remoting.mxml.*;

public class RemoteObjectAsMXMLTest extends RemoteObjectTest
{

    public static function suite() : TestSuite
    {
        var suite : TestSuite = new TestSuite();
        suite.addTest(new RemoteObjectAsMXMLTest("testDefaultResultEvent"));
        suite.addTest(new RemoteObjectAsMXMLTest("testDefaultFaultEvent"));
        suite.addTest(new RemoteObjectAsMXMLTest("testResultProperty"));
        suite.addTest(new RemoteObjectAsMXMLTest("testOperationResultEvent"));
        suite.addTest(new RemoteObjectAsMXMLTest("testOperationFaultEvent"));
        suite.addTest(new RemoteObjectAsMXMLTest("testOperationResultNotDefault"));
        suite.addTest(new RemoteObjectAsMXMLTest("testOperationFaultNotDefault"));
        suite.addTest(new RemoteObjectAsMXMLTest("testSendNoArgumentsParams"));
        suite.addTest(new RemoteObjectAsMXMLTest("testSendArgumentsNoParams"));
        suite.addTest(new RemoteObjectAsMXMLTest("testSendArgumentsParams"));
        suite.addTest(new RemoteObjectAsMXMLTest("testArgumentsAsArray"));
        suite.addTest(new RemoteObjectAsMXMLTest("testResultCall"));
        suite.addTest(new RemoteObjectAsMXMLTest("testFaultCall"));
        suite.addTest(new RemoteObjectAsMXMLTest("testResultMessage"));
        suite.addTest(new RemoteObjectAsMXMLTest("testFaultMessage"));
        suite.addTest(new RemoteObjectAsMXMLTest("testCancel"));
        return suite;
    }

    public function RemoteObjectAsMXMLTest(name : String)
    {
        super(name);
    }

    override public function getRemoteObject() : mx.rpc.remoting.RemoteObject
    {
        return new mx.rpc.remoting.mxml.RemoteObject();
    }

}

}