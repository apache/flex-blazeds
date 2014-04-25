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
package proxy
{
	import flash.net.Socket;
    import flash.utils.ByteArray;
    public class ResponseSerializer
    {
        private var _headers:Object;
        private var _bytes:ByteArray;
        public function ResponseSerializer()
        {

        }

        public function setHeaders(headers:Object):void
        {
            _headers = headers;
        }

        public function writeStream(s:Socket):void
        {
            if (!s.connected)
            {
                throw new Error("Socket is not connected");
            }
            if (_headers == null)
            {
                throw new Error("Headers missing ");
            }
            _bytes = new ByteArray();
            for (var prop:String in _headers)
            {
                _bytes.writeMultiByte(prop + ":" + String(_headers[prop]) + "\r\n","US-ASCII");
            }
            _bytes.writeMultiByte("\r\n","US-ASCII");
            _headers = null;
            _bytes.position = 0;
            s.writeBytes(_bytes, 0, _bytes.length);
            s.flush();
        }
	}
}