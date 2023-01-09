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
package flex.messaging.client;

/**
 * Used alongside invocations of <code>FlexClient.pollWithWait()</code> to allow calling code to
 * maintain a record of the Objects being used to place waited poll requests into a wait
 * state. This can be used to break the threads out of their wait state separately from the
 * internal waited poll handling within <code>FlexClient</code>.
 */
public interface PollWaitListener {
    /**
     * Hook method invoked directly before a wait begins.
     *
     * @param notifier The <tt>Object</tt> being used to <code>wait()/notify()</code>.
     */
    void waitStart(Object notifier);

    /**
     * Hook method invoked directly after a wait completes.
     *
     * @param notifier The <tt>Object</tt> being used to <code>wait()/notify()</code>.
     */
    void waitEnd(Object notifier);
}
