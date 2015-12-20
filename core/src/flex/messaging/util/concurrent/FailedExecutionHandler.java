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
package flex.messaging.util.concurrent;

/**
 * This interface defines a callback that can be registered with an {@link Executor}
 * to be notified when execution of a <code>Runnable</code> has failed.
 */
public interface FailedExecutionHandler
{
    /**
     * Invoked when a <code>Runnable</code> has failed to execute.
     * This is most commonly invoked when the <code>Runnable</code> can not be queued
     * for execution, but may also be invoked if an uncaught <code>Exception</code> was
     * thrown and the <code>Executor</code> implementation has support for catching this
     * and notifying via this callback.
     * 
     * @param command The <code>Runnable</code> command that failed to execute.
     * @param executor The <code>Executor</code> that was unable to execute the command.
     * @param exception The <code>Exception</code> identifying why the command failed to execute.
     */
    void failedExecution(Runnable command, Executor executor, Exception exception);
}
