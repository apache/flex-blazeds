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
 * This interface allows different Executor implementations to be chosen and used
 * without creating a direct dependency upon <code>java.util.concurrent.Executor</code>
 * added in Java 1.5, the Java 1.4.x-friendly backport of the <code>java.util.concurrent</code> APIs 
 * which has a different package structure, or alternative work execution frameworks such as
 * IBM WebSphere 5's <code>com.ibm.websphere.asynchbeans.WorkManager</code> or the 
 * <code>commonj.work.WorkManager</code> available in IBM WebSphere 6, BEA WebLogic 9 or
 * other application servers that support the <code>commonj</code> API.
 * Implementations should notify clients of any failure with executing a command by invoking 
 * the callback on the <code>FailedExecutionHandler</code> if one has been set.
 * 
 * @see java.util.concurrent.Executor
 * @exclude
 */ 
public interface Executor
{
    /**
     * Executes the given command at some time in the future.
     * The command may execute in a new thread, in a pooled thread, or in the calling thread, at the 
     * discretion of the <code>Executor</code> implementation.
     * Implementation classes are free to throw a <code>RuntimeException</code> if the command can not
     * be executed.
     * 
     * @param command The command to execute.
     */
    void execute(Runnable command);
    
    /**
     * Returns the current handler for failed executions.
     * 
     * @return The current handler.
     */
    FailedExecutionHandler getFailedExecutionHandler();
    
    /**
     * Sets the handler for failed executions.
     * 
     * @param handler The new handler.
     */
    void setFailedExecutionHandler(FailedExecutionHandler handler);
}
