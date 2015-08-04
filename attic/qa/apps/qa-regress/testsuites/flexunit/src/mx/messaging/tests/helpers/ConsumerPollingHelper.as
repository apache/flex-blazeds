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

public class ConsumerPollingHelper extends EventDispatcher
{
    public function ConsumerPollingHelper(cons:Consumer, pro:Producer)
    {
        super();
        _consumer = cons;
        _producer = pro;
        _consumer.addEventListener(MessageEvent.MESSAGE, messageHandler);
        _consumer.addEventListener(MessageAckEvent.ACKNOWLEDGE, ackHandler);
        _consumer.subscribe();
    }

    private function ackHandler(event:MessageAckEvent):void
    {
        if ((event.correlation is CommandMessage) && (CommandMessage(event.correlation).operation == CommandMessage.SUBSCRIBE_OPERATION))
        {
            var msg:AsyncMessage = new AsyncMessage();
            msg.body = "Polling Message 1";
            _producer.send(msg);
        }
        else // Unsubscribe ack.
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
        // Got message, go ahead and unsubscribe.        
        _consumer.unsubscribe();
    }

    private var _consumer:Consumer;
    private var _producer:Producer;
}

}