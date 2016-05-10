/*
 * Copyright 2002-2009 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

 /*
  * This file has been modified by Adobe (Adobe Systems Incorporated).
  * Date: Oct 7, 2010
  * Reason(s): Fixed the following issues:
  * BLZ-573: Cannot create a new company in Company Manager Sample in Spring BlazeDS Integration Test Drive
  * BLZ-574: Error in creating new contact in insync05 sample in Spring BlazeDS Integration test drive
  */

package org.springframework.flex.samples.simplefeed;

import java.util.Random;

import org.springframework.flex.messaging.MessageTemplate;

public class SimpleFeed {

    private static FeedThread thread;

    private final MessageTemplate template;

    public SimpleFeed(MessageTemplate template) {
        this.template = template;
    }

    public void start() {
        if (thread == null) {
            thread = new FeedThread(this.template);
            thread.start();
        }
    }

    public void stop() {
        if (thread != null) {
            thread.running = false;
            thread = null;
        }
    }

    public static class FeedThread extends Thread {

        public boolean running = false;

        private final MessageTemplate template;

        public FeedThread(MessageTemplate template) {
            this.template = template;
        }

        @Override
        public void run() {
            this.running = true;
            Random random = new Random();
            double initialValue = 35;
            double currentValue = 35;
            double maxChange = initialValue * 0.005;

            while (this.running) {
                double change = maxChange - random.nextDouble() * maxChange * 2;
                double newValue = currentValue + change;

                if (currentValue < initialValue + initialValue * 0.15 && currentValue > initialValue - initialValue * 0.15) {
                    currentValue = newValue;
                } else {
                    currentValue -= change;
                }

                this.template.send("simple-feed", new Double(currentValue));

                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                }

            }
        }
    }

}
