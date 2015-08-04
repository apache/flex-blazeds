/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.adobe.samples.hsbc;

import java.util.Date;

import flex.messaging.MessageBroker;
import flex.messaging.messages.AsyncMessage;

public class MessageSender 
{
	public void sendMessageToClients(String messageBody, String dst)
	{
		AsyncMessage msg = new AsyncMessage();

		msg.setClientId("Java-Based-Producer-For-Messaging");
		msg.setTimestamp(new Date().getTime());
		//you can create a unique id
		msg.setMessageId("Java-Based-Producer-For-Messaging-ID");
		//destination to which the message is to be sent
		msg.setDestination(dst);	
System.out.println("ok1");	
		//set message body
		msg.setBody(messageBody != null?messageBody:"");
		//set message header
		msg.setHeader("sender", "From the server");
System.out.println("ok2");
		//send message to destination
		MessageBroker.getMessageBroker(null).routeMessageToService(msg, null);
System.out.println("ok3" + msg.getBody());				
	}

}

