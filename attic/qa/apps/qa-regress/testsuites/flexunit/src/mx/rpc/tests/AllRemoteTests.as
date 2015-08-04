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

public class AllRemoteTests
{

    public static function allTestsSuite():TestSuite
    {
        var suite:TestSuite = new TestSuite();
        suite.addTest(allSchemaTests());
        suite.addTest(allHTTPServiceTests());
        suite.addTest(allRemoteObjectTests());
        suite.addTest(allWebServiceTests());
        return suite;
    }
    
    public static function allSchemaTests():TestSuite
    {
        var suite:TestSuite = new TestSuite();
        suite.addTest(WalmsleySchemaTest.suite());
        return suite;
    }

    public static function allHTTPServiceTests():TestSuite
    {
        var suite:TestSuite = new TestSuite();
        suite.addTest(HTTPServiceTest.suite());
        suite.addTest(HTTPServiceNoProxyTest.suite());
        suite.addTest(HTTPServiceAsMXMLTest.suite());
        suite.addTest(MXMLHTTPServiceTest.suite());
        suite.addTest(URLUtilTest.suite());
        return suite;
    }

    public static function allRemoteObjectTests():TestSuite
    {
        var suite:TestSuite = new TestSuite();
        suite.addTest(RemoteObjectTest.suite());
        suite.addTest(RemoteObjectAsMXMLTest.suite());
        suite.addTest(MXMLRemoteObjectTest.suite());
        return suite;
    }

    public static function allWebServiceTests():TestSuite
    {
        var suite:TestSuite = new TestSuite();
        suite.addTest(WebServiceTest.suite());
        suite.addTest(WebServiceAsMXMLTest.suite());
        suite.addTest(MXMLWebServiceTest.suite());
        suite.addTest(WebServiceNoProxyTest.suite());
        return suite;
    }

}

}