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
import flash.net.*;
import flash.utils.*;

public class AMFTestResponder extends Responder implements IEventDispatcher
{
    public function AMFTestResponder()
    {
        super(result, status);
        eventDispatcher = new EventDispatcher(this);
    }

    public function result(r:Object):void
    {
        var event:TestResultEvent = new TestResultEvent(r);
        dispatchEvent(event);
    }

    public function status(s:Object):void
    {
        var event:TestStatusEvent = new TestStatusEvent(s);
        dispatchEvent(event);
    }

    public function addEventListener(type:String, listener:Function, useCapture:Boolean=false, priority:int=0, weakRef:Boolean=false):void
    {
        eventDispatcher.addEventListener(type, listener, useCapture, priority);
    }

    public function dispatchEvent(event:flash.events.Event):Boolean
    {
        return eventDispatcher.dispatchEvent(event);
    }

    public function hasEventListener(type:String):Boolean
    {
        return eventDispatcher.hasEventListener(type);
    }

    public function removeEventListener(type:String, listener:Function, useCapture:Boolean = false):void
    {
        eventDispatcher.removeEventListener(type, listener, useCapture);
    }

    public function willTrigger(type:String):Boolean
    {
        return eventDispatcher.willTrigger(type);
    }

    private var eventDispatcher:EventDispatcher;
}

}