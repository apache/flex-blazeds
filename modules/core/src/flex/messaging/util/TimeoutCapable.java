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
package flex.messaging.util;

import java.util.concurrent.Future;

/**
 * This interface defines the contract for an object that can time
 * out, where the timeout mechanism is provided by the TimeoutManager
 * class.
 *
 * @exclude
 */
public interface TimeoutCapable
{
    /**
     * Revoke the timeout task, removing it from future evaluation and execution.
     */
    void cancelTimeout();

    /**
     * Determine the timestamp of this object's last use, where "last use" should
     * denote all tasks that eliminate idleness.
     * 
     * @return last used time
     */
    long getLastUse();

    /**
     * Determine the time, in milliseconds, that this object is allowed to idle
     * before having its timeout method invoked.
     * 
     * @return timeout period
     */
    long getTimeoutPeriod();

    /**
     * Set the Future used to provide access to the Runnable task that invokes
     * the timeout method. A Future is used instead of a Runnable so that it may
     * be canceled according to the Java concurrency standard.
     * 
     * @param future Future used to provide access to the Runnable task that invokes the timeout method.
     */
    void setTimeoutFuture(Future future);

    /**
     * Inform the object that it has timed out.
     */
    void timeout();
}
