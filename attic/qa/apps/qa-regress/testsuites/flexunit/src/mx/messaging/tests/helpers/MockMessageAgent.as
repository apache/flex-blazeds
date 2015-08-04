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

package mx.messaging.tests.helpers {

import mx.logging.*;
import mx.messaging.*;
import mx.messaging.channels.*
import mx.messaging.config.*;
import mx.messaging.errors.*;
import mx.messaging.events.*;
import mx.messaging.messages.*;

/**
 *  Simple MockMessageAgent to test base functionality.
 */
public class MockMessageAgent extends MessageAgent
{
    public function MockMessageAgent()
    {
        super();
        _log = Log.getLogger("ChannelSetTest");
		_agentType = "stub-agent";
    }
    
    override public function acknowledge(ackMsg:AcknowledgeMessage, msg:IMessage):void
    {
        dispatchEvent(MessageAckEvent.createEvent(ackMsg, msg));
    }
    
    override public function fault(errMsg:ErrorMessage, msg:IMessage):void
    {
        dispatchEvent(MessageFaultEvent.createEvent(errMsg));
    }
    
    public function send(message:IMessage):void
    {
        internalSend(message);
    }
}

}