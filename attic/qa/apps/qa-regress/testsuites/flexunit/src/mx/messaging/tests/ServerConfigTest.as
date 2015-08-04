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

import flash.utils.*;
import flexunit.framework.*;
import mx.messaging.config.*;
import mx.messaging.channels.*;
import mx.messaging.*;
import mx.messaging.messages.*;
import mx.messaging.errors.*;

public class ServerConfigTest extends ConfigurationBasedTestCase
{
	////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    //////////////////////////////////////////////////////////////////////////// 
    public function ServerConfigTest(methodName:String)
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
        var result:TestSuite = new TestSuite();
        result.addTest(new ServerConfigTest("testInvalidChannelError"));
        result.addTest(new ServerConfigTest("testGetThenGetChannel"));
        result.addTest(new ServerConfigTest("testGetChannelSet"));
        result.addTest(new ServerConfigTest("testGetChannelSetErrorForBadMessage"));
        result.addTest(new ServerConfigTest("testGetChannelSetErrorForBadDestination"));
        return result;
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
    public function testInvalidChannelError():void
    {
        try
        {
            ServerConfig.getChannel("invalid-id");
            fail("Didn't throw an exception.");
        }
        catch(e:InvalidChannelError)
        {
            assertTrue("e is InvalidChannelError", e is InvalidChannelError);
        }
        catch(e:Error)
        {
            fail("e is not InvalidChannelError: " + e.toString());
        }
    }

    public function testGetThenGetChannel():void
    {
        var channel:Channel = ServerConfig.getChannel("qa-amf");
        assertTrue("ServerConfig.getChannel('qa-amf') is AMFChannel", channel is AMFChannel);
        assertEquals("channel.uri == 'http://localhost:8400/qa-regress/messagebroker/amf'", "http://localhost:8400/qa-regress/messagebroker/amf", channel.uri);
        var channel2:Channel = ServerConfig.getChannel("qa-amf");
        assertEquals("channel == channel2", channel, channel2);
    }
    
    public function testGetChannelSet():void
    {
        var asyncMsg:AsyncMessage = new AsyncMessage();
        var cs:ChannelSet = ServerConfig.getChannelSet("MyTopic");
        assertTrue("ServerConfig.getChannelSet('MyTopic') is ChannelSet", cs is ChannelSet);
    }
    
    public function testGetChannelSetErrorForBadMessage():void
    {
        try
        {
            var cs:ChannelSet = ServerConfig.getChannelSet("MyTopic");
        }
        catch (e:InvalidDestinationError)
        {
            assertTrue("e is InvalidDestinationError", e is InvalidDestinationError);
        }
        catch (e:Error)
        {
            fail("e is not InvalidDestinationError: " + e.toString());
        }
    }
    
    public function testGetChannelSetErrorForBadDestination():void
    {
        try
        {
            var cs:ChannelSet = ServerConfig.getChannelSet("bad-destination");
        }
        catch (e:InvalidDestinationError)
        {
            assertTrue("e is InvalidDestinationError", e is InvalidDestinationError);
        }
        catch (e:Error)
        {
            fail("e is not InvalidDestinationError: " + e.toString());
        }
    }
}

}
