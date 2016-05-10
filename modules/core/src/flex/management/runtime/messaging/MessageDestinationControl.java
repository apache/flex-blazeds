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
package flex.management.runtime.messaging;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import flex.management.BaseControl;
import flex.management.runtime.AdminConsoleTypes;
import flex.messaging.Destination;

import javax.management.ObjectName;

/**
 * The <code>MessageDestinationControl</code> class is the MBean implementation for
 * monitoring and managing a <code>MessageDestination</code> at runtime.
 */
public class MessageDestinationControl extends DestinationControl implements
        MessageDestinationControlMBean
{
    private static final String TYPE = "MessageDestination";
    private ObjectName messageCache;
    private ObjectName throttleManager;
    private ObjectName subscriptionManager;

    private AtomicInteger serviceMessageCount = new AtomicInteger(0);
    private Date lastServiceMessageTimestamp;
    private long serviceMessageStart;
    private AtomicInteger serviceCommandCount = new AtomicInteger(0);
    private Date lastServiceCommandTimestamp;
    private long serviceCommandStart;
    private AtomicInteger serviceMessageFromAdapterCount = new AtomicInteger(0);
    private Date lastServiceMessageFromAdapterTimestamp;
    private long serviceMessageFromAdapterStart;   
    /**
     * Constructs a new <code>MessageDestinationControl</code> instance.
     * 
     * @param destination The destination managed by this MBean.
     * @param parent The parent MBean in the management hierarchy.
     */
    public MessageDestinationControl(Destination destination, BaseControl parent)
    {
        super(destination, parent);          
        serviceMessageStart = System.currentTimeMillis();
        serviceCommandStart = serviceMessageStart;
        serviceMessageFromAdapterStart = serviceMessageStart;             
    }
    
    protected void onRegistrationComplete()
    {
        String name = this.getObjectName().getCanonicalName();
        
        String[] pollablePerInterval = { "ServiceCommandCount", "ServiceMessageCount",
                "ServiceMessageFromAdapterCount" };
        String[] pollableGeneral = { "ServiceCommandFrequency", "ServiceMessageFrequency",
                "ServiceMessageFromAdapterFrequency", "LastServiceCommandTimestamp", 
                "LastServiceMessageTimestamp", "LastServiceMessageFromAdapterTimestamp"};
        
        getRegistrar().registerObjects(
                new int[] {AdminConsoleTypes.DESTINATION_POLLABLE, AdminConsoleTypes.GRAPH_BY_POLL_INTERVAL},
                name, pollablePerInterval);
        getRegistrar().registerObjects(AdminConsoleTypes.DESTINATION_POLLABLE, name,
                pollableGeneral);
    }
    
    /*
     *  (non-Javadoc)
     * @see flex.management.BaseControlMBean#getType()
     */
    public String getType()
    {
        return TYPE;
    }
    
    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.MessageDestinationControlMBean#getMessageCache()
     */
    public ObjectName getMessageCache()
    {
        return messageCache;
    }
    
    /**
     * Sets the <code>ObjectName</code> for the message cache used by the managed destination.
     * 
     * @param value The <code>ObjectName</code> for the message cache.
     */
    public void setMessageCache(ObjectName value)
    {
        messageCache = value;
    }
    
    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.MessageDestinationControlMBean#getThrottleManager()
     */
    public ObjectName getThrottleManager()
    {
        return throttleManager;
    }
    
    /**
     * Sets the <code>ObjectName</code> for the throttle manager used by the managed destination.
     * 
     * @param value The <code>ObjectName</code> for the throttle manager.
     */
    public void setThrottleManager(ObjectName value)
    {
        throttleManager = value;
    }
    
    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.MessageDestinationControlMBean#getSubscriptionManager()
     */
    public ObjectName getSubscriptionManager()
    {
        return subscriptionManager;
    }
    
    /**
     * Sets the <code>ObjectName</code> for the subscription manager used by the managed destination.
     * 
     * @param value The <code>ObjectName</code> for the subscription manager.
     */
    public void setSubscriptionManager(ObjectName value)
    {
        subscriptionManager = value;
    }
    
    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.messaging.MessageDestinationControlMBean#getServiceMessageCount()
     */
    public Integer getServiceMessageCount()
    {
        return Integer.valueOf(serviceMessageCount.get());
    }
    
    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.messaging.MessageDestinationControlMBean#resetServiceMessageCount()
     */
    public void resetServiceMessageCount()
    {
        serviceMessageStart = System.currentTimeMillis();
        serviceMessageCount = new AtomicInteger(0);
        lastServiceMessageTimestamp = null;
    }
    
    /**
     * Increments the count of messages serviced.
     */
    public void incrementServiceMessageCount()
    {
        serviceMessageCount.incrementAndGet();
        lastServiceMessageTimestamp = new Date();
    }
    
    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.messaging.MessageDestinationControlMBean#getLastServiceMessageTimestamp()
     */
    public Date getLastServiceMessageTimestamp()
    {
        return lastServiceMessageTimestamp;
    }
    
    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.messaging.MessageDestinationControlMBean#getServiceMessageFrequency()
     */
    public Double getServiceMessageFrequency()
    {
        if (serviceMessageCount.get() > 0)
        {
            double runtime = differenceInMinutes(serviceMessageStart, System.currentTimeMillis());
            return new Double(serviceMessageCount.get()/runtime);
        }
        else
        {
            return new Double(0);
        }
    }

    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.messaging.MessageDestinationControlMBean#getServiceCommandCount()
     */
    public Integer getServiceCommandCount()
    {        
        return Integer.valueOf(serviceCommandCount.get());
    }
    
    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.messaging.MessageDestinationControlMBean#resetServiceCommandCount()
     */
    public void resetServiceCommandCount()
    {
        serviceCommandStart = System.currentTimeMillis();
        serviceCommandCount = new AtomicInteger(0);
        lastServiceCommandTimestamp = null;
    }
    
    /**
     * Increments the count of command messages serviced.
     */
    public void incrementServiceCommandCount()
    {
        serviceCommandCount.incrementAndGet();
        lastServiceCommandTimestamp = new Date();
    }
    
    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.messaging.MessageDestinationControlMBean#getLastServiceCommandTimestamp()
     */
    public Date getLastServiceCommandTimestamp()
    {
        return lastServiceCommandTimestamp;
    }
    
    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.messaging.MessageDestinationControlMBean#getServiceCommandFrequency()
     */
    public Double getServiceCommandFrequency()
    {
        if (serviceCommandCount.get() > 0)
        {
            double runtime = differenceInMinutes(serviceCommandStart, System.currentTimeMillis());
            return new Double(serviceCommandCount.get()/runtime);
        }
        else
        {
            return new Double(0);
        }
    }

    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.messaging.MessageDestinationControlMBean#getServiceMessageFromAdapterCount()
     */
    public Integer getServiceMessageFromAdapterCount()
    {
        return Integer.valueOf(serviceMessageFromAdapterCount.get());
    }
    
    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.messaging.MessageDestinationControlMBean#resetServiceMessageFromAdapterCount()
     */
    public void resetServiceMessageFromAdapterCount()
    {
        serviceMessageFromAdapterStart = System.currentTimeMillis();
        serviceMessageFromAdapterCount = new AtomicInteger(0);
        lastServiceMessageFromAdapterTimestamp = null;
    }
    
    /**
     * Increments the count of messages from adapters processed.
     */
    public void incrementServiceMessageFromAdapterCount()
    {
        serviceMessageFromAdapterCount.incrementAndGet();
        lastServiceMessageFromAdapterTimestamp = new Date();
    }
    
    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.messaging.MessageDestinationControlMBean#getLastServiceMessageFromAdapterTimestamp()
     */
    public Date getLastServiceMessageFromAdapterTimestamp()
    {
        return lastServiceMessageFromAdapterTimestamp;
    }
    
    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.messaging.MessageDestinationControlMBean#getServiceMessageFromAdapterFrequency()
     */
    public Double getServiceMessageFromAdapterFrequency()
    {
        if (serviceMessageFromAdapterCount.get() > 0)
        {
            double runtime = differenceInMinutes(serviceMessageFromAdapterStart, System.currentTimeMillis());
            return new Double(serviceMessageFromAdapterCount.get()/runtime);
        }
        else
        {
            return new Double(0);
        }
    }    
}
