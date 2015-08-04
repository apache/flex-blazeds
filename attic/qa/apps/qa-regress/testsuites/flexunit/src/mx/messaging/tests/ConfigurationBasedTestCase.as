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
import mx.messaging.config.*;
/**
 * This custom TestCase class is primarily intended for use in configuration testing client side APIs that 
 * use the ServerConfig xml object. Tests that actually make requests to or receive responses
 * from the server should stay away from using this class because it has it's own copy of the 
 * ServerConfig and won't match up with the server. 
 */ 
public class ConfigurationBasedTestCase extends TestCase
{
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////    
    public function ConfigurationBasedTestCase(methodName:String)
    {
        super(methodName);
    }
	////////////////////////////////////////////////////////////////////////////
    //
    // SetUp
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * This SetUp method creates it's own ServerConfig xml object. Test classes that
     * extend this one will get this ServerConfig object rather than the one from the 
     * server.
     */  
    
    override public function setUp():void
    {
        super.setUp();
        ServerConfig.xml = <services>
                                <service id="remoting-service" messageTypes="flex.messaging.messages.RemotingMessage">
                                    <destination id="RuntimeManagement">
								        <channels>
								        	<channel ref="qa-amf" />
								        </channels>
								    </destination>
								    <destination id="ServerPush">
                                        <channels>
                                            <channel ref="qa-amf" />
                                        </channels>
                                    </destination>  
                                    <destination id="WeatherService">
                                        <channels>
                                            <channel ref="qa-amf" />
                                        </channels>
                                    </destination>  
                                    <destination id="filteredAck" parent="service">
                                        <channels>
                                            <channel ref="qa-amf" />
                                        </channels>
                                    </destination>
                                    <destination id="filteredFault" parent="service">
                                        <channels>
                                            <channel ref="qa-amf" />
                                        </channels>
                                    </destination>                                
                                </service>
                                <service id="proxy-service" messageTypes="flex.messaging.messages.HTTPMessage,flex.messaging.messages.SOAPMessage">
                                    <destination id="DefaultHTTP">
                                        <channels>
                                            <channel ref="qa-amf" />
                                        </channels>
                                    </destination>
                                    <destination id="DefaultHTTPS">
                                        <channels>
                                            <channel ref="qa-secure-amf"/>
                                        </channels>
                                    </destination>
                                    <destination id="basic.xml">
                                        <channels>
                                            <channel ref="qa-http" />
                                        </channels>
                                    </destination>
                                    <destination id="echoParams">
                                        <channels>
                                            <channel ref="qa-amf" />
                                        </channels>
                                    </destination>
                                    <destination id="echoParamsAsFlashvars">
                                        <channels>
                                            <channel ref="qa-amf" />
                                        </channels>
                                    </destination>
                                    <destination id="echoParamsExtraOnUrl">
                                        <channels>
                                            <channel ref="qa-amf" />
                                        </channels>
                                    </destination>
                                    <destination id="echoXml">
                                        <channels>
                                            <channel ref="qa-amf" />
                                        </channels>
                                    </destination>
                                    <destination id="decodeURL">
                                        <channels>
                                            <channel ref="qa-amf" />
                                        </channels>
                                    </destination>
                                    <destination id="WeatherService_proxy">
                                        <channels>
                                            <channel ref="qa-amf" />
                                        </channels>
                                    </destination>
                                </service>
                                <service id="message-service" messageTypes="flex.messaging.messages.AsyncMessage">
                                    <destination id="MyTopic">
                                        <channels>
                                            <channel ref="qa-polling-amf" />
                                        </channels>
                                    </destination>
                                    <destination id="MyHangingHTTPTopic">
                                        <channels>                                            
                                            <channel ref="qa-hanging-amf"/>
                                        </channels>
                                    </destination>
                                    <destination id="MyHangingAMFTopic">
                                        <channels>
                                            <channel ref="qa-hanging-http"/>
                                        </channels>
                                    </destination>
                                    <destination id="HuntingTest">
                                        <channels>
                                            <channel ref="my-bad-amf" />
                                            <channel ref="qa-amf" />
                                        </channels>
                                    </destination>
                                    <destination id="MyJMSTopic">
                                        <channels>
                                            <channel ref="qa-polling-amf" />
                                        </channels>
                                    </destination>
                                    <destination id="OneBadChannelOneGoodAMF">
                                        <channels>
                                            <channel ref="my-bad-amf" />
                                            <channel ref="qa-polling-amf" />
                                        </channels>
                                    </destination>
                                    <destination id="AllBadChannels">
                                        <channels>
                                            <channel ref="my-bad-amf" />
                                            <channel ref="my-bad-http" />
                                        </channels>
                                    </destination>
                                    <destination id="NoChannels">
                                    </destination>
                                    <destination id="filteredChat">
                                        <channels>
                                            <channel ref="qa-polling-amf" />
                                        </channels>
                                    </destination>
                                </service>
                                <channels>
                                    <channel id="my-bad-http" type="mx.messaging.channels.HTTPChannel">
                                        <endpoint uri="http://localhost:1999/qa-regress/messagebroker/http" />
                                    </channel>
                                    <channel id="my-bad-amf" type="mx.messaging.channels.AMFChannel">
                                        <endpoint uri="http://localhost:2000/qa-regress/messagebroker/amf" />
                                    </channel>
                                    <channel id="qa-amf" type="mx.messaging.channels.AMFChannel">
                                        <endpoint uri="http://localhost:8400/qa-regress/messagebroker/amf" />
                                        <properties>
                                            <polling-enabled>false</polling-enabled>
                                        </properties>
                                    </channel>
                                    <channel id="qa-hanging-amf" type="mx.messaging.channels.AMFChannel">
                                        <endpoint uri="http://localhost:8400/qa-regress/messagebroker/hangingamf" />
                                        <properties>
                                            <connect-timeout-seconds>1</connect-timeout-seconds>
                                        </properties>
                                    </channel>
                                    <channel id="qa-secure-amf" type="mx.messaging.channels.SecureAMFChannel">
                                        <endpoint uri="http://localhost:8400/qa-regress/messagebroker/amfsecure"  />
                                        <properties>
                                            <polling-enabled>false</polling-enabled>
                                        </properties>
                                    </channel>
                                    <channel id="qa-polling-amf" type="mx.messaging.channels.AMFChannel">
                                        <endpoint uri="http://localhost:8400/qa-regress/messagebroker/amfpolling"  />
                                        <properties>
                                            <polling-enabled>true</polling-enabled>
                                            <polling-interval-millis>250</polling-interval-millis>
                                        </properties>
                                    </channel>
                                    <channel id="qa-http" type="mx.messaging.channels.HTTPChannel">
                                        <endpoint uri="http://localhost:8400/qa-regress/messagebroker/http" />
                                    </channel>
                                    <channel id="qa-hanging-http" type="mx.messaging.channels.HTTPChannel">
                                        <endpoint uri="http://localhost:8400/qa-regress/messagebroker/hanginghttp" />
                                        <properties>
                                            <connect-timeout-seconds>1</connect-timeout-seconds>
                                        </properties>
                                    </channel>
                                </channels>
                            </services>;
    }
}

}