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

    public class Book extends SuperBook
    {
        public var title:String;
        public var titleCopy:String;
        public var author:String;
        public var published:Date;
        public var pages:uint=3000;
        private var _ISBN:String;
        //turn on debug log to check, and this field should not be contained in the object
        private var shouldNotSerialized:String="shouldNotSerialized!!";

        public function Book()
        {
            registerClassAlias("blazeds.qa.remotingService.Book",Book);
        }

        public function set ISBN(isbn:String):void
        {
            if (isbn != null)
            {
                _ISBN = isbn;
            }
        }

        public function get ISBN():String
        {
            return _ISBN;
        }
    }
}