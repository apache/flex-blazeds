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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Assert;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

public class DefaultThreadPoolExecutorTest extends TestCase
{
    public DefaultThreadPoolExecutorTest(String name)
    {
        super(name);
    }
    
    public static Test suite()
    {
        return new TestSuite(DefaultThreadPoolExecutorTest.class);
    }
    
    public void testSimpleExecution()
    {
        // Create a small pool with a synchronous queue (no bounding or storage).
        Executor executor = new DefaultThreadPoolExecutor(1, 1, Integer.MAX_VALUE, TimeUnit.SECONDS, new SynchronousQueue());
                
        final CountDownLatch continueSignal = new CountDownLatch(1);
        executor.execute(new Runnable(){
            public void run()
            {
                // Indicate that we've run.
                continueSignal.countDown();
            }
        });
        try
        {
            boolean success = continueSignal.await(2, TimeUnit.SECONDS);
            if (!success)
            {
                fail("Test timed out waiting for execution to complete.");
            }
        }
        catch (InterruptedException e)
        {
            fail("Test was interrupted: " + e);
        }
        
        // Shut down; because we create the thread pool in this specific impl be sure to shut it down.
        DefaultThreadPoolExecutor tpe = (DefaultThreadPoolExecutor)executor;
        tpe.shutdown();
    }
    
    public void testFailedExecutionHandling()
    {
        // Create a small pool with a bounded queue that can only contain one queued task.
        final DefaultThreadPoolExecutor executor = new DefaultThreadPoolExecutor(1, 1, Integer.MAX_VALUE, TimeUnit.SECONDS, new ArrayBlockingQueue(1));
        final CountDownLatch taskPauseSignal = new CountDownLatch(1);
        final CountDownLatch executorPauseSignal = new CountDownLatch(1);
        final ArrayList failedTasks = new ArrayList();
        executor.setFailedExecutionHandler(new FailedExecutionHandler(){
            public void failedExecution(Runnable command, Executor ex, Exception exception)
            {
                assertEquals(executor, ex);
                assertTrue(exception instanceof RejectedExecutionException);
                
                failedTasks.add(command);
                // Unpause the initial task processing.
                taskPauseSignal.countDown();
            }
        });
        
        // This first task will pause.
        executor.execute(new Task("first", this, taskPauseSignal, executorPauseSignal, executor));
        // Now queue two more tasks to overflow the executor's queue.
        executor.execute(new Task("second", this, null, null, null));
        executor.execute(new Task("third", this, null, null, null));
        // and wait.
        try
        {
            boolean success = executorPauseSignal.await(5 * 60, TimeUnit.SECONDS);
            if (!success)
            {
                fail("Test timed out waiting for execution to complete.");
            }
        }
        catch (InterruptedException e)
        {
            fail("Test was interrupted: " + e);
        }       
                
        // Test failed execution handling
        assertEquals(failedTasks.size(), 1);
        assertTrue(((Task)failedTasks.get(0)).name.equals("third"));
        
        // Shut down; because we create the thread pool in this specific impl be sure we shut it down.        
        executor.shutdown();
    }
    
    static class Task implements Runnable
    {
        public Task(String name, TestCase test, CountDownLatch taskPauseSignal, CountDownLatch executorPauseSignal, DefaultThreadPoolExecutor executor)
        {
            this.name = name;
            this.test = test;
            this.taskPauseSignal = taskPauseSignal;
            this.executorPauseSignal = executorPauseSignal;
            this.executor = executor;
        }
        
        public String name;
        private TestCase test;
        private CountDownLatch taskPauseSignal;
        private CountDownLatch executorPauseSignal;
        private DefaultThreadPoolExecutor executor;
        
        public void run()
        {
            if (taskPauseSignal != null)
            {
                try
                {
                    boolean success = taskPauseSignal.await(5 * 60, TimeUnit.SECONDS);
                    if (!success)
                    {
                        Assert.fail("Test timed out waiting for execution to complete.");
                    }
                    else
                    {
                        // At this point we should have the second task queued and the third task should have failed (overflow).
                        assertEquals(executor.getQueue().size(), 1);
                        assertTrue(((Task)executor.getQueue().peek()).name.equals("second"));
                        
                        // Unblock the executor test thread.
                        executorPauseSignal.countDown();
                    }
                }
                catch (InterruptedException e)
                {
                    Assert.fail("Test was interrupted: " + e);
                }
            }
                
        }
    }
}
