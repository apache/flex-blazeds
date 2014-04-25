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

import flex.messaging.log.Log;
import flex.messaging.log.LogCategories;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * This class provides a means of managing TimeoutCapable objects. It leverages
 * facilities in the the Java concurrency package to provide a common utility
 * for scheduling timeout Futures and managing the underlying worker thread pools.
 *
 * @author neville
 * @exclude
 */
public class TimeoutManager
{
    private static final String LOG_CATEGORY = LogCategories.TIMEOUT;

    private ScheduledThreadPoolExecutor timeoutService;
    
    /**
     * Default constructor calls parameterized constructor will a null factory argument.
     */
    public TimeoutManager()
    {
        this(null);
    }

    /**
     * Constructs a new TimeoutManager using the passed in factory for thread creation.
     * and a single thread of the TimeoutManager.
     * 
     * @param tf ThreadFactory
     */
    public TimeoutManager(ThreadFactory tf)
    {
        this(tf, 1);
    }

    /**
     * Constructs a new TimeoutManager using the passe din factory for thread creation
     * and the passed in number of threads for TimeoutManager to use.
     * 
     * @param tf The ThreadFactory to use.
     * @param numberOfThreads The number of threads for the ThreadFactory to use.
     */
    public TimeoutManager(ThreadFactory tf, int numberOfThreads)
    {
        if (tf == null)
        {
            tf = new MonitorThreadFactory();
        }
        if (numberOfThreads < 1)
            numberOfThreads = 1;
        timeoutService = new ScheduledThreadPoolExecutor(numberOfThreads, tf);
    }

    /**
     * Schedule a task to be executed in the future.
     * 
     * @param t task to be executed at some future time
     * @return a Future object that enables access to the value(s) returned by the task
     */
    public Future scheduleTimeout(TimeoutCapable t)
    {
        Future future = null;
        if (t.getTimeoutPeriod() > 0)
        {
            Runnable timeoutTask = new TimeoutTask(t);
            future = timeoutService.schedule(timeoutTask, t.getTimeoutPeriod(), TimeUnit.MILLISECONDS);
            t.setTimeoutFuture(future);
            if (t instanceof TimeoutAbstractObject)
            {
                TimeoutAbstractObject timeoutAbstract = (TimeoutAbstractObject)t;
                timeoutAbstract.setTimeoutManager(this);
                timeoutAbstract.setTimeoutTask(timeoutTask);
            }
            if (Log.isDebug())
                Log.getLogger(LOG_CATEGORY).debug("TimeoutManager '" + System.identityHashCode(this) + "' has scheduled instance '" +
                    System.identityHashCode(t) + "' of type '" + t.getClass().getName() + "' to be timed out in " + t.getTimeoutPeriod() + " milliseconds. Task queue size: "+ timeoutService.getQueue().size());
        }
        return future;
    }

    /**
     * Cancel the execution of a future task and remove all references to it.
     * 
     * @param timeoutAbstract the task to be canceled
     * @return true if cancellation were successful
     */
    public boolean unscheduleTimeout(TimeoutAbstractObject timeoutAbstract)
    {
        Object toRemove = timeoutAbstract.getTimeoutFuture();
        /*
         * In more recent versions of the backport, they are requiring that we
         * pass in the Future returned by the schedule method.  This should always 
         * implement Runnable even in 2.2 but I'm a little paranoid here so just
         * to be sure, if we get a future which is not a runnable we go back to the old
         * code which calls the remove on the instance we passed into the schedule method.
         */
        if (!(toRemove instanceof Runnable))
            toRemove = timeoutAbstract.getTimeoutTask();
        if (timeoutService.remove((Runnable) toRemove))
        {
            if (Log.isDebug())
                Log.getLogger(LOG_CATEGORY).debug("TimeoutManager '" + System.identityHashCode(this) + "' has removed the timeout task for instance '" +
                    System.identityHashCode(timeoutAbstract) + "' of type '" + timeoutAbstract.getClass().getName() + "' that has requested its timeout be cancelled. Task queue size: "+ timeoutService.getQueue().size());
        }
        else
        {
            Future timeoutFuture = timeoutAbstract.getTimeoutFuture();
            timeoutFuture.cancel(false); // Don't interrupt it if it's running.
            if (Log.isDebug())
                Log.getLogger(LOG_CATEGORY).debug("TimeoutManager '" + System.identityHashCode(this) + "' cancelling timeout task for instance '" +
                    System.identityHashCode(timeoutAbstract) + "' of type '" + timeoutAbstract.getClass().getName() + "' that has requested its timeout be cancelled. Task queue size: "+ timeoutService.getQueue().size());
            if (timeoutFuture.isDone())
            {
                timeoutService.purge(); // Force the service to give up refs to task immediately rather than hanging on to them.
                if (Log.isDebug())
                    Log.getLogger(LOG_CATEGORY).debug("TimeoutManager '" + System.identityHashCode(this) + "' purged queue of any cancelled or completed tasks. Task queue size: "+ timeoutService.getQueue().size());
            }
        }
        
        // to aggressively clean up memory remove the reference from the unscheduled timeout to its 
        // time out object
        Object unscheduledTimeoutTask = timeoutAbstract.getTimeoutTask();
        if (unscheduledTimeoutTask != null && unscheduledTimeoutTask instanceof TimeoutTask)
            ((TimeoutTask)timeoutAbstract.getTimeoutTask()).clearTimeoutCapable();
        
        return true;
    }
    
    /**
     * Cancel all outstanding and any future tasks.
     */
    public void shutdown()
    {
        timeoutService.shutdownNow();
        // shutdownNow() returns List<Runnable> for all unexecuted tasks
        // but we ignore these because we're only queuing dependent tasks
        // for timed execution and they can be safely discarded when the 
        // parent object goes away and shuts down the TimeoutManager it is using.
        // Also, we may need to introduce an interface that encompasses 
        // using either a ScheduledThreadPoolExecutor or a CommonJ Timer;
        // in the case of a CommonJ Timer only a stop() method is provided 
        // which does not return handles to any queued, unexecuted tasks.
    }

    class MonitorThreadFactory implements ThreadFactory
    {
        public Thread newThread(Runnable r)
        {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("TimeoutManager");
            return t;
        }
    }

    class TimeoutTask implements Runnable
    {
        private TimeoutCapable timeoutObject;
        
        
        /**
         * Removes the reference from this timeout task to the object that would 
         * have been timed out.  This is useful for memory clean up when timeouts are unscheduled. 
         */
        public void clearTimeoutCapable()
        {
            timeoutObject = null;
        }

        public TimeoutTask(TimeoutCapable timeoutObject)
        {
            this.timeoutObject = timeoutObject;
        }

        public void run()
        {
            // Because of the weird clearTimeCapable() in the middle of timeout call, we got NPE in the debug log level.
            // Now copy the reference to local varable
            TimeoutCapable timeoutObject = this.timeoutObject;
            long inactiveMillis = System.currentTimeMillis() - timeoutObject.getLastUse();
            if (inactiveMillis >= timeoutObject.getTimeoutPeriod())
            {
                try
                {
                    timeoutObject.timeout();
                    
                    if (Log.isDebug())
                        Log.getLogger(LOG_CATEGORY).debug("TimeoutManager '" + System.identityHashCode(TimeoutManager.this) + "' has run the timeout task for instance '" +
                            System.identityHashCode(timeoutObject) + "' of type '" + timeoutObject.getClass().getName() + "'. Task queue size: "+ timeoutService.getQueue().size());
                }
                catch (Throwable t)
                {
                    if (Log.isError())
                        Log.getLogger(LOG_CATEGORY).error("TimeoutManager '" + System.identityHashCode(TimeoutManager.this) + "' encountered an error running the timeout task for instance '" +
                            System.identityHashCode(timeoutObject) + "' of type '" + timeoutObject.getClass().getName() + "'. Task queue size: "+ timeoutService.getQueue().size(), t);   
                }
            }
            else
            {
                // Reschedule timeout and store new Future for cancellation.
                timeoutObject.setTimeoutFuture(timeoutService.schedule(this, (timeoutObject.getTimeoutPeriod()-inactiveMillis), TimeUnit.MILLISECONDS));
                if (Log.isDebug())
                    Log.getLogger(LOG_CATEGORY).debug("TimeoutManager '" + System.identityHashCode(TimeoutManager.this) + "' has rescheduled a timeout for the active instance '" +
                        System.identityHashCode(timeoutObject) + "' of type '" + timeoutObject.getClass().getName() + "'. Task queue size: "+ timeoutService.getQueue().size());
            }
        }
    }
}
