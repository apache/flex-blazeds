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
package mx.messaging.tests.helpers
{

import flash.events.*;
import mx.messaging.*;
import mx.messaging.events.*;
import mx.messaging.messages.*;
import mx.messaging.channels.*;

// this class is used to confirm that multiple messages sent will all be recieved
// in a polling operation
public class PollingTester extends EventDispatcher
{
    public function PollingTester(cons:Consumer, pro:Producer, messageList:Array)
    {
        super();
        _consumer = cons;
        _consumer.addEventListener(MessageAckEvent.ACKNOWLEDGE, ackHandler);
        _consumer.addEventListener(MessageEvent.MESSAGE, messageHandler);
        _consumer.subscribe();
        _producer = pro;
        _messages = messageList;
        _messageTestedCount = 0;
    }

    private function ackHandler(event:MessageAckEvent):void
    {
        if ((event.correlation is CommandMessage) && (CommandMessage(event.correlation).operation == CommandMessage.SUBSCRIBE_OPERATION))
        {
            for (var i:int=0; i<_messages.length; i++)
                _producer.send(AsyncMessage(_messages[i]));
        }
        else // Unsubscribe ack. The test is done.
        {
            var event2:TestEvent = new TestEvent(TestEvent.COMPLETE);
            event2.passed = true;
            dispatchEvent(event2);
            _consumer.disconnect();
            _producer.disconnect();
        }
    }

    private function messageHandler(event:MessageEvent):void
    {
        // see if we can find the specified message in the list
        var found:Boolean = false;
        for (var i:int=0; i<_messages.length && !found; i++)
        {
            if (_messages[i].body.text == event.message.body.text)
            {
                found = true;
            }
        }
        if (found)
            _messageTestedCount++;

        if (_messageTestedCount == _messages.length)
        {            
            _consumer.unsubscribe();
        }
    }

    private var _messages:Array;
    private var _messageTestedCount:uint;
    private var _consumer:Consumer;
    private var _producer:Producer;
}

}