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
package flex.samples.feed;

import java.util.*;
import flex.messaging.MessageBroker;
import flex.messaging.messages.AsyncMessage;
import flex.messaging.util.UUIDUtils;

public class Feed {
	private static FeedThread thread;

	public Feed() {
	}

	public void start() {
		if (thread == null) {
			thread = new FeedThread();
			thread.start();
		}
	}

	public void stop() {
		thread.running = false;
		thread = null;
	}

	public static class FeedThread extends Thread {

		public boolean running = true;

		public void run() {
			MessageBroker msgBroker = MessageBroker.getMessageBroker(null);
			String clientID = UUIDUtils.createUUID();

			Random random = new Random();
			double initialValue = 35;
			double currentValue = 35;
			double maxChange = initialValue * 0.005;

			while (running) {
				double change = maxChange - random.nextDouble() * maxChange * 2;
				double newValue = currentValue + change;

				if (currentValue < initialValue + initialValue * 0.15
						&& currentValue > initialValue - initialValue * 0.15) {
					currentValue = newValue;
				} else {
					currentValue -= change;
				}

				AsyncMessage msg = new AsyncMessage();
				msg.setDestination("feed");
				msg.setClientId(clientID);
				msg.setMessageId(UUIDUtils.createUUID());
				msg.setTimestamp(System.currentTimeMillis());
				msg.setBody(new Double(currentValue));
				msgBroker.routeMessageToService(msg, null);

				System.out.println("" + currentValue);

				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
				}

			}
		}
	}

}