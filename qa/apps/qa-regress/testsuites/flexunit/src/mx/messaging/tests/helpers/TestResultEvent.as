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
 * a Responder's result can be treated like a result
 * event in the flexunit framework.
 *
 * @private
 */
public class TestResultEvent extends Event
{
    public static const RESULT:String = "result";

    public function TestResultEvent(r:Object)
    {
        super(TestResultEvent.RESULT);
        _result = r;
    }

    public function get result():Object
    {
        return _result;
    }

    private var _result:Object;
}

}