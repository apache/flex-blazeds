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
package flex.samples.runtimeconfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import flex.messaging.MessageBroker;
import flex.messaging.MessageDestination;
//import flex.messaging.config.ServerSettings;
import flex.messaging.services.MessageService;

/**
 * Simplistic implementation of a chat room management service. Clients can add rooms,
 * and obtain a list of rooms. The interesting part of this example is the "on-the-fly" 
 * creation of a message destination. The same technique can be used to create DataService
 * and Remoting destinations. 
 */
public class ChatRoomService {

	private List rooms;
	
	public ChatRoomService()
	{
		rooms = Collections.synchronizedList(new ArrayList());
	}

	public List getRoomList()
	{
		return rooms;
	}
	
	public void createRoom(String id) {

		if (roomExists(id))
		{
			throw new RuntimeException("Room already exists");
		}
		
		// Create a new Message destination dynamically
		String serviceId = "message-service";
		MessageBroker broker = MessageBroker.getMessageBroker(null);
		MessageService service = (MessageService) broker.getService(serviceId);
		MessageDestination destination = (MessageDestination) service.createDestination(id);

		if (service.isStarted())
		{
			destination.start();
		}

		rooms.add(id);
		
	}
	
	public boolean roomExists(String id)
	{
		int size = rooms.size();
		for (int i=0; i<size; i++)
		{
			if ( ((String)rooms.get(i)).equals(id) ) 
			{
				return true;
			}
		}
		return false;
	}
}