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
package flex.messaging;

import flex.messaging.config.ConfigMap;

/**
 * @exclude
 * Base for FlexSessionProvider implementations.
 * Providers are protocol-specific factories for concrete FlexSession implementations.
 * They are registered with a FlexSessionManager, which acts as the central point of control
 * for tracking all active FlexSessions and for dispatching creation events to FlexSessionListeners.
 */
public abstract class AbstractFlexSessionProvider implements FlexComponent
{
    //--------------------------------------------------------------------------
    //
    // Variables
    //
    //--------------------------------------------------------------------------

    /**
     * Instance lock.
     */
    protected final Object lock = new Object();
    
    //--------------------------------------------------------------------------
    //
    // Properties
    //
    //--------------------------------------------------------------------------

    //----------------------------------
    //  flexSessionManager
    //----------------------------------

    private volatile FlexSessionManager flexSessionManager;
    
    /**
     * Returns the <tt>FlexSessionManager</tt> this provider is currently registered to.
     * 
     * @return The <tt>FlexSessionManager</tt> this provider is currently registered to.
     */
    public FlexSessionManager getFlexSessionManager()
    {
        return flexSessionManager;
    }
    
    /**
     * Sets the <tt>FlexSessionManager</tt> this provider is registered to.
     * 
     * @param value The <tt>FlexSessionManager</tt> this provider is registered to.
     */
    public void setFlexSessionManager(final FlexSessionManager value)
    {
        flexSessionManager = value;
    }
    
    //----------------------------------
    //  logCategory
    //----------------------------------

    private boolean started;    
    
    /**
     * Indicates whether the component is started and running.
     * 
     * @return <code>true</code> if the component has started; 
     *         otherwise <code>false</code>.
     */
    public boolean isStarted()
    {
        synchronized (lock)
        {
            return started;
        }
    }
    
    //--------------------------------------------------------------------------
    //
    // Public Methods
    //
    //--------------------------------------------------------------------------

    /**
     * Initializes the component with configuration information.
     *
     * @param id The id of the component.
     * @param configMap The properties for configuring component.
     */
    public void initialize(final String id, final ConfigMap configMap)
    {
        // No-op.
    }
    
    /**
     * Removes a <tt>FlexSession</tt> created by this provider.
     * This callback is invoked by <tt>FlexSession</tt>s when they are invalidated.
     *
     * @param session The <tt>FlexSession</tt> being invalidated.
     */
    public void removeFlexSession(final FlexSession session)
    {
        FlexSessionManager manager = getFlexSessionManager();
        if (manager != null)
            manager.unregisterFlexSession(session);
    }
    
    /**
     * Invoked to start the component.
     * This base implementation changes the components state such that {@link #isStarted()} returns true.
     */
    public void start()
    {
        synchronized (lock)
        {
            started = true;
        }
    }
    
    /**
     * Invoked to stop the component.
     * This base implementation changes the components state such that {@link #isStarted()} returns false.
     */
    public void stop()
    {
        synchronized (lock)
        {
            started = false;
        }
    }
}
