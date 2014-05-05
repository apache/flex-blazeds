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
 * This class defines the default implementation of TimeoutCapable,
 * providing the default behavior for an object that is capable of timing
 * out where that time out mechanism is managed by TimeoutManager.
 *
 * @exclude
 */
public abstract class TimeoutAbstractObject implements TimeoutCapable
{
    private long lastUse;
    private volatile boolean timeoutCanceled;
    private TimeoutManager timeoutManager;
    private Runnable timeoutTask;
    private Future timeoutFuture;
    private long timeoutPeriod;
    private final Object lock = new Object();

    /** {@inheritDoc} */
    public void cancelTimeout()
    {
        if (timeoutCanceled)
            return;

        boolean purged = false;
        if ((timeoutManager != null) && (timeoutTask != null) && (timeoutFuture != null))
            purged = timeoutManager.unscheduleTimeout(this);

        if (!purged && (timeoutFuture != null))
        {
            timeoutFuture.cancel(false);
        }

        timeoutCanceled = true;
    }

    /** {@inheritDoc} */
    public long getLastUse()
    {
        synchronized (lock)
        {
            return lastUse;
        }
    }

    /**
     * Updates the time this object was last used.
     * @param lastUse time this object was last used
     */
    public void setLastUse(long lastUse)
    {
        synchronized (lock)
        {
            this.lastUse = lastUse;
        }
    }

    /**
     * Updates the time this object was last used to be the current time.
     */
    public void updateLastUse()
    {
        synchronized (lock)
        {
            this.lastUse = System.currentTimeMillis();
        }
    }

    /**
     * Returns manager responsible for this object.
     * @return manager responsible for this object
     */
    public TimeoutManager getTimeoutManager()
    {
        synchronized (lock)
        {
            return timeoutManager;
        }
    }

    /**
     * Sets the manager responsible for this object.
     * @param timeoutManager manager responsible for this object
     */
    public void setTimeoutManager(TimeoutManager timeoutManager)
    {
        synchronized (lock)
        {
            this.timeoutManager = timeoutManager;
        }
    }

    /**
     * Returns the runnable task that will be executed once this object times out.
     * @return the runnable task that will be executed once this object times out
     */
    public Runnable getTimeoutTask()
    {
        synchronized (lock)
        {
            return timeoutTask;
        }
    }

    /**
     * Sets the runnable task that will be executed once this object times out.
     * @param timeoutTask the runnable task that will be executed once this object times out
     */
    public void setTimeoutTask(Runnable timeoutTask)
    {
        synchronized (lock)
        {
            this.timeoutTask = timeoutTask;
        }
    }

    /**
     * Return the object encapsulating result of the execution of this object once it has timed out.
     * @return the object encapsulating result of the execution of this object once it has timed out
     */
    public Future getTimeoutFuture()
    {
        synchronized (lock)
        {
            return timeoutFuture;
        }
    }

    /** {@inheritDoc} */
    public void setTimeoutFuture(Future timeoutFuture)
    {
        synchronized (lock)
        {
            this.timeoutFuture = timeoutFuture;
        }
    }

    /** {@inheritDoc} */
    public long getTimeoutPeriod()
    {
        synchronized (lock)
        {
            return timeoutPeriod;
        }
    }

    /**
     * Set the time to be elapsed before this object times out and its associated task gets executed.
     * @param timeoutPeriod the time to be elapsed before this object times out and its associated task gets executed
     */
    public void setTimeoutPeriod(long timeoutPeriod)
    {
        synchronized (lock)
        {
            this.timeoutPeriod = timeoutPeriod;
        }
    }
}
