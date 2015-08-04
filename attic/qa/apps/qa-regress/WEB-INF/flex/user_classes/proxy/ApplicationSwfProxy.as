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

    import proxy.ResponseSerializer;

    import flash.net.Socket;
	import flash.utils.ByteArray;
    import flash.events.Event;

    import mx.core.Application;
	public class ApplicationSwfProxy
	{
		private var socket:Socket;
		
		public function ApplicationSwfProxy(host:String="localhost",port:uint=4321)
		{
			socket = new Socket();
            socket.addEventListener("connect", connect);
            socket.addEventListener("socketData", readStream);
            socket.addEventListener("close", connectionError);
            socket.addEventListener("ioError", connectionError);
            try
            {
                socket.connect(host,port);
            }
            catch (e:Error)
            {
                trace("Cannot connect to >" + host + ":" + port);
            }
		}

		public function connectionError(event:Event):void
		{
		    trace(event);
		}
		
		public function connect(event:Event):void
		{
			var headers:Object = new Object();
			headers.url = Application.application.url;
			headers.proxyid = Application.application.parameters.proxyid;
			var response:ResponseSerializer = new ResponseSerializer();
			response.setHeaders(headers);
			response.writeStream(socket);
		}
		
		private var commandHeader:String = "";
		public function readStream(event:Event):void
		{
			var bytes:ByteArray = new ByteArray();
			var availBytes:uint = socket.bytesAvailable;
            var offset:uint = 0;
            socket.readBytes(bytes, offset, availBytes);
            bytes.position = 0;
            for (var i:uint=0; i<bytes.length; i++)
            {
            	commandHeader += bytes.readMultiByte(1,"US-ASCII");
            }
            trace(commandHeader);
            if (commandHeader.indexOf("\r\n\r\n") > -1)
            {
           		if (commandHeader.indexOf("invoke:") != 0)
           		{
           			socket.close();
           			throw new Error("Unknown command");
           		}

           		var commandStr:String = commandHeader.substring(7,commandHeader.length-4); //strip invoke: + crlfx2
           		trace(commandStr);
           		var command:String = commandStr.substring(0,commandStr.indexOf(":"));  //method or property
           		trace("command: " + command);
           		commandStr = commandStr.substring(commandStr.indexOf(":"));
           		var parameterStr:String = commandStr.substring(commandStr.indexOf(":")+1); //methodname + parameters
           		trace("parameterStr: " + parameterStr);
           		var methodName:String;
           		var returnValue:*;
           		commandHeader = "";
           		
           		if (command == "method")
           		{
           			var parameters:Array = new Array();
           			var index:int = parameterStr.indexOf(",");
           			while (index > -1)
           			{
           				trace(index);
           				parameters.push(parameterStr.substring(0, index));
           				parameterStr = parameterStr.substring(index +1);
           				index = parameterStr.indexOf(",");
           			}
           			parameters.push(parameterStr);
           			methodName = parameters.shift();
           			trace("methodName: " + methodName);
           			returnValue = Application.application[methodName].apply(Application.application, parameters);

           		}
           		else if (command == "property")
           		{
           			trace("property:"+ parameterStr);
           			returnValue = Application.application[parameterStr];
           		}
           		//implicitly cast returnValue to String
           		returnValue = "" + returnValue;
           		trace("return:"+returnValue);
           		var headers:Object = new Object();
				headers["return"] = returnValue;
				var response:ResponseSerializer = new ResponseSerializer();
				response.setHeaders(headers);
				response.writeStream(socket);
            }
            	
		}
		
		
		
		
		
		
	}
}