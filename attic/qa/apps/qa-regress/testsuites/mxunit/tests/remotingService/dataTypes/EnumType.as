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
package {

/* 
 * Strongly typed enum class with one limitation - you must use the equals method
 * to do the comparison
 */
[RemoteClass(alias="remoting.datatype.EnumType")]
public class EnumType
{
    public static const APPLE:EnumType = new EnumType("APPLE");
    public static const ORANGE:EnumType = new EnumType("ORANGE");
    public static const BANANA:EnumType = new EnumType("BANANA");

    public var fruit:String;

    public function EnumType(v:String="unset")
    {
        fruit = v;
    }

    public function equals(other:EnumType):Boolean
    {
        return other.fruit == fruit;
    }

    public function toString():String
    {
        return fruit;
    }
}

}

