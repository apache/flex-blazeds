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
    public dynamic class TestCase203212
    {
        public var lastName:String = "Anonymous";
        public var firstName:String = "Anonymous";
        public var phoneNumber:String = "000-000-0000";
        private var _dynamicProps:Object;
        private var _anotherProp:String = "";
        
        public function TestCase203212(dynProps:Object, anotherProp:String)
        {
            _dynamicProps = dynProps;
            _anotherProp = anotherProp;
        }
        public function get dynamicProps():Object
        {
            return _dynamicProps;
        }

        public function set dynamicProps(value:Object):void
        {
            _dynamicProps = value;
        }
        public function get anotherProp():String
        {
            return _anotherProp;
        }

        public function set anotherProp(value:String):void
        {
            _anotherProp = value;
        }

    }
}