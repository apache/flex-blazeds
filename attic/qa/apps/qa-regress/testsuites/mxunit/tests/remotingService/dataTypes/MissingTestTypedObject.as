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
package
{

import flash.net.*;

public class MissingTestTypedObject
{
    private var _prop1:int;
    public var theCollection:Array;
    public var me:MissingTestTypedObject;
    public var prop2:String;
    internal var prop3:String="I am not public";
    public var map:Object;
    public var extra:String;  //this variable doesn't exist on server side

    public function MissingTestTypedObject()
    {
        registerClassAlias("missing.TestTypedObject",MissingTestTypedObject);
    }

    public function set prop1(value:int):void
    {
        _prop1 = value;
    }

    public function get prop1():int
    {
        return _prop1;
    }

    public function get readOnlyProp1():int
    {
        return _prop1;
    }
}
}