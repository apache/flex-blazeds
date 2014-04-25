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
    import flash.utils.IDataInput;
    import flash.utils.IDataOutput;
    import flash.utils.IExternalizable;

    /**
     * A simple class that uses IExternalizable interface to read and write its properties.
     */ 
    [RemoteClass(alias="features.remoting.externalizable.ExternalizableClass")]
    public class ExternalizableClass implements IExternalizable
    {
        public var property1:String;
        public var property2:String;

        public function ExternalizableClass()
        {
        }

        public function readExternal(input:IDataInput):void
        {
            property1 = input.readObject() as String;
            property2 = input.readObject() as String;
        }

        public function writeExternal(output:IDataOutput):void
        {
            output.writeObject(property1);
            output.writeObject(property2);
        }

        public function toString():String
        {
            return "ExternalizableClass [property1: " + property1 + ", property2: " + property2 + "]";
        }
        
    }
}