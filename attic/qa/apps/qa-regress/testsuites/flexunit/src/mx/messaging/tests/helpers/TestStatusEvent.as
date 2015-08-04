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

/**
 * A helper class for raw NetConnection tests so that
 * a Responder's status object can be treated like a status (fault)
 * event in the flexunit framework.
 *
 * @private
 */
public class TestStatusEvent extends Event
{
    public static const STATUS:String = "status";

    public function TestStatusEvent(s:Object)
    {
        super(STATUS);
        _status = s;
    }

    public function get status():Object
    {
        return _status;
    }

    private var _status:Object;
}

}