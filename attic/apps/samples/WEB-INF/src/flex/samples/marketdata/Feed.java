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
package flex.samples.marketdata;

import java.util.*;

import flex.messaging.MessageBroker;
import flex.messaging.messages.AsyncMessage;
import flex.messaging.util.UUIDUtils;

public class Feed {

	public static void main(String args[]) {
		Feed feed = new Feed();
		feed.start();
	}
	
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

		private Random random;

		public void run() {

			MessageBroker msgBroker = MessageBroker.getMessageBroker(null);
			String clientID = UUIDUtils.createUUID();

			Portfolio portfolio = new Portfolio();
			List stocks = portfolio.getStocks();
			int size = stocks.size();
			int index = 0;

			random = new Random();
			
			Stock stock;

			while (running) {

				stock = (Stock) stocks.get(index);
				simulateChange(stock);

				index++;
				if (index >= size) {
					index = 0;
				}

				AsyncMessage msg = new AsyncMessage();
				msg.setDestination("market-data-feed");
				msg.setHeader("DSSubtopic", stock.getSymbol());
				msg.setClientId(clientID);
				msg.setMessageId(UUIDUtils.createUUID());
				msg.setTimestamp(System.currentTimeMillis());
				msg.setBody(stock);
				msgBroker.routeMessageToService(msg, null);

				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
				}

			}
		}

		private void simulateChange(Stock stock) {

			double maxChange = stock.open * 0.005;
			double change = maxChange - random.nextDouble() * maxChange * 2;
			stock.change = change;
			double last = stock.last + change;

			if (last < stock.open + stock.open * 0.15
					&& last > stock.open - stock.open * 0.15) {
				stock.last = last;
			} else {
				stock.last = stock.last - change;
			}

			if (stock.last > stock.high) {
				stock.high = stock.last;
			} else if (stock.last < stock.low) {
				stock.low = stock.last;
			}
			stock.date = new Date();

		}

	}

}
