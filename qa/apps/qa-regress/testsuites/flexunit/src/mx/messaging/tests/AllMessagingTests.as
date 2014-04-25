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

import flexunit.framework.*;
import mx.rpc.tests.*;
/**
 * TestSuite used to run all the messaging tests. 
 */ 
public class AllMessagingTests
{
	////////////////////////////////////////////////////////////////////////////
    //
    // TestSuite
    //
    ////////////////////////////////////////////////////////////////////////////   
   
    public static function suite():TestSuite
    {
        var tests:TestSuite = new TestSuite();
        tests.addTest(AMF3Test.suite());
        tests.addTest(AMF0Test.suite());
        tests.addTest(URLUtilTest.suite());
        tests.addTest(HexEncoderDecoderTest.suite());
        tests.addTest(Base64EncoderDecoderTest.suite());
        tests.addTest(AMFChannelTest.suite());
        tests.addTest(ServerConfigTest.suite());
        tests.addTest(ChannelSetTest.suite());
        tests.addTest(MessageAgentTest.suite());
        tests.addTest(ConsumerWithServerTest.suite());
        tests.addTest(ConsumerTest.suite());
        tests.addTest(RemoteObjectTest.suite());
        tests.addTest(HTTPServiceNoProxyTest.suite());
        tests.addTest(HTTPServiceTest.suite());
        tests.addTest(HTTPServiceAsMXMLTest.suite());
        tests.addTest(MXMLHTTPServiceTest.suite());
        tests.addTest(WebServiceTest.suite());
        tests.addTest(WebServiceAsMXMLTest.suite());
        tests.addTest(MXMLWebServiceTest.suite());
        tests.addTest(WebServiceNoProxyTest.suite());
        return tests;
    }
}

}
