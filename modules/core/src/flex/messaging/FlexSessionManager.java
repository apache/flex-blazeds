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

import flex.management.ManageableComponent;
import flex.messaging.log.LogCategories;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @exclude
 * Manages FlexSession instances for a MessageBroker. 
 */
public class FlexSessionManager extends ManageableComponent
{
    public static final String TYPE = "FlexSessionManager";
    
    private static final long MILLIS_IN_HOUR = 3600000;
    
    //--------------------------------------------------------------------------
    //
    // Constructors
    //
    //--------------------------------------------------------------------------
    
    /**
     * @exclude
     * Constructs a <tt>FlexSessionManager</tt> for the passed <tt>MessageBroker</tt>.
     * 
     * @param broker The root <tt>MessageBroker</tt> using this <tt>FlexSessionManager</tt>.
     */
    public FlexSessionManager(MessageBroker broker)
    {
        this(false, broker);
    }
    
    /**
     * @exclude
     * Constructs a <tt>FlexSessionManager</tt> for the passed <tt>MessageBroker</tt> and optionally enables management.
     * 
     * @param enableManagement <code>true</code> if the <tt>FlexSessionManager</tt>
     * is manageable; otherwise <code>false</code>.
     */
    public FlexSessionManager(boolean enableManagement, MessageBroker broker)
    {
        super(enableManagement);
        
        super.setId(TYPE);
        
        this.broker = broker;
        
        this.setParent(broker);
    }

    //--------------------------------------------------------------------------
    //
    // Variables
    //
    //--------------------------------------------------------------------------

    /**
     * Instance-level lock.
     */
    private final Object lock = new Object();
    
    //--------------------------------------------------------------------------
    //
    // Properties
    //
    //--------------------------------------------------------------------------
    
    //----------------------------------
    //  logCategory
    //----------------------------------
    
    /**
     * Returns the log category for this component.
     * 
     * @return The log category for this component.
     */
    @Override
    protected String getLogCategory()
    {
        return LogCategories.ENDPOINT_FLEXSESSION;
    }
    
    //----------------------------------
    //  flexSessionCount
    //----------------------------------
    
    private int flexSessionCount;
    
    /**
     * Returns the total count of active FlexSessions.
     * 
     * @return The total count of active FlexSessions.
     */
    public int getFlexSessionCount()
    {
        synchronized (lock)
        {
            return flexSessionCount;
        }
    }

    //----------------------------------
    //  flexSessionProviders
    //----------------------------------

    private final ConcurrentHashMap<Class<? extends FlexSession>, AbstractFlexSessionProvider> providers = new ConcurrentHashMap<Class<? extends FlexSession>, AbstractFlexSessionProvider>();
    
    /**
     * Returns the registered <tt>FlexSessionProvider</tt> implementation for the specified <tt>FlexSession</tt> type.
     * 
     * @param sessionClass The specific <tt>FlexSession</tt> type to get a provider for.
     * @return The registered <tt>FlexSessionProvider</tt> or <code>null</code> if no provider is registered.
     */
    public AbstractFlexSessionProvider getFlexSessionProvider(Class<? extends FlexSession> sessionClass)
    {
        return providers.get(sessionClass);
    }
    
    /**
     * Registers a <tt>FlexSessionProvider</tt> implementation for a specified <tt>FlexSession</tt> type.
     * 
     * @param sessionClass The specific <tt>FlexSession</tt> type to register a provider for.
     * @param provider The corresponding <tt>FlexSessionProvider</tt> to register.
     * @return The previously registered provider, or <code>null</code> if no provider was registered for this session type.
     */
    public AbstractFlexSessionProvider registerFlexSessionProvider(Class<? extends FlexSession> sessionClass, AbstractFlexSessionProvider provider)    
    {
        provider.setFlexSessionManager(this);
        AbstractFlexSessionProvider previousProvider = providers.putIfAbsent(sessionClass, provider);
        
        if (previousProvider != null)
        {
            previousProvider.stop();
            previousProvider.setFlexSessionManager(null);
        }
        
        if (isStarted())
            provider.start();

        return previousProvider;
    }
    
    /**
     * Unregisters a <tt>FlexSessionProvider</tt> implementation for a specified <tt>FlexSession</tt> type.
     * 
     * @param sessionClass The specific <tt>FlexSession</tt> type to unregister a provider for.
     */
    public void unregisterFlexSessionProvider(Class<? extends FlexSession> sessionClass)
    {
        AbstractFlexSessionProvider provider = providers.remove(sessionClass);
        if (provider != null)
        {
            provider.stop();
            provider.setFlexSessionManager(null);
        }
    }
    
    //----------------------------------
    //  flexSessions
    //----------------------------------
    
    /**
     * Registers a new <tt>FlexSession</tt> with the <tt>FlexSessionManager</tt>.
     * 
     * @param session The new <tt>FlexSession</tt>.
     */
    public void registerFlexSession(FlexSession session)
    {
        synchronized (lock)
        {
            ++flexSessionCount;            
            resetMaxFlexSessionsInCurrentHour(flexSessionCount);
        }
    }
    
    /**
     * Unregisters an invalidated <tt>FlexSession</tt> from the <tt>FlexSessionManager</tt>.
     * 
     * @param session The invalidated <tt>FlexSession</tt>.
     */
    public void unregisterFlexSession(FlexSession session)
    {
        synchronized (lock)
        {
            --flexSessionCount;
            resetMaxFlexSessionsInCurrentHour(flexSessionCount);            
        }
    }
    
    //----------------------------------
    //  maxFlexSessionsInCurrentHour
    //----------------------------------
    
    private int maxSessionCountInCurrentHour;
    private long currentHourStartTimestamp = System.currentTimeMillis();
    
    public int getMaxFlexSessionsInCurrentHour()
    {
        synchronized (lock)
        {            
            // Make sure we report the correct value if the system has been idle across an hour transition.
            resetMaxFlexSessionsInCurrentHour(flexSessionCount);
            
            return maxSessionCountInCurrentHour;
        }
    }
    
    /* Must be called within a synchronized block. */
    private void resetMaxFlexSessionsInCurrentHour(int currentCount)
    {
        long offset = (System.currentTimeMillis() - currentHourStartTimestamp) / MILLIS_IN_HOUR;
        if (offset > 0) // Shift to the current hour and reset to the current session count.
        {
            currentHourStartTimestamp += (MILLIS_IN_HOUR * offset);
            maxSessionCountInCurrentHour = currentCount;
        }
        else if (maxSessionCountInCurrentHour < currentCount)
        {
            maxSessionCountInCurrentHour = currentCount;
        }
    }
    
    //----------------------------------
    //  messageBroker
    //----------------------------------

    private final MessageBroker broker;

    /**
     * Returns the <tt>MessageBroker</tt> instance that owns this <tt>FlexSessionManager</tt>.
     *
     * @return The parent <tt>MessageBroker</tt> instance.
     */
    public MessageBroker getMessageBroker()
    {
        return broker;
    }    
    
    //--------------------------------------------------------------------------
    //
    // Public Methods
    //
    //--------------------------------------------------------------------------
    
    /**
     * Starts the <tt>FlexSessionManager</tt>.
     * Any registered <tt>FlexSession</tt>s providers are also started.
     */
    @Override
    public void start()
    {
        if (isStarted())
            return;
        
        for (AbstractFlexSessionProvider provider : providers.values())
        {
            if (!provider.isStarted())
                provider.start();
        }           
        
        super.start();
    }
    
    /**
     * Stops the <tt>FlexSessionManager</tt>.
     * Any registered <tt>FlexSession</tt> providers are stopped and unregistered. 
     */
    @Override
    public void stop()
    {
        if (!isStarted())
            return;
        
        super.stop();
        
        for (Class<? extends FlexSession> sessionClass : providers.keySet())
        {
            unregisterFlexSessionProvider(sessionClass);
        }
        providers.clear();
    }
}
