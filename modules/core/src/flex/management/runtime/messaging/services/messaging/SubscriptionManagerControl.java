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
package flex.management.runtime.messaging.services.messaging;

import flex.management.BaseControl;
import flex.messaging.MessageClient;
import flex.messaging.services.messaging.SubscriptionManager;

import java.util.Set;

/**
 * The <code>SubscriptionManagerControl</code> class is the MBean implementation for
 * monitoring and managing a <code>SubscriptionManager</code> at runtime.
 */
public class SubscriptionManagerControl extends BaseControl implements
        SubscriptionManagerControlMBean
{
    private SubscriptionManager subscriptionManager;
    
    /**
     * Constructs a new <code>SubscriptionManagerControl</code> instance, assigning its
     * backing <code>SubscriptionManager</code>.
     * 
     * @param subscriptionManager The <code>SubscriptionManager</code> managed by this MBean.
     * @param parent The parent MBean in the management hierarchy.
     */
    public SubscriptionManagerControl(SubscriptionManager subscriptionManager, BaseControl parent)
    {
        super(parent);
        this.subscriptionManager = subscriptionManager;
    }
    
    /*
     *  (non-Javadoc)
     * @see flex.management.BaseControlMBean#getId()
     */
    public String getId()
    {
        return subscriptionManager.getId();
    }
    
    /*
     *  (non-Javadoc)
     * @see flex.management.BaseControlMBean#getType()
     */
    public String getType()
    {
        return SubscriptionManager.TYPE;
    }
    
    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.SubscriptionManagerControlMBean#getSubscriberCount()
     */
    public Integer getSubscriberCount()
    {
        Set subscriberIds = subscriptionManager.getSubscriberIds();
        if (subscriberIds != null)
        {
            return new Integer(subscriberIds.size());            
        }
        else
        {
            return new Integer(0);
        }
    }
    
    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.SubscriptionManagerControlMBean#getSubscriberIds()
     */
    public String[] getSubscriberIds()
    {
        Set subscriberIds = subscriptionManager.getSubscriberIds();
        if (subscriberIds != null)
        {
            String[] ids = new String[subscriberIds.size()];
            return (String[])subscriberIds.toArray(ids);
        }
        else
        {
            return new String[0];
        }                
    }
    
    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.SubscriptionManagerControlMBean#removeSubscriber(java.lang.String)
     */
    public void removeSubscriber(String subscriberId)
    {
        MessageClient subscriber = subscriptionManager.getSubscriber(subscriberId);
        if (subscriber != null)
        {
            subscriptionManager.removeSubscriber(subscriber);
        }
    }
    
    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.SubscriptionManagerControlMBean#removeAllSubscribers()
     */
    public void removeAllSubscribers()
    {
        String[] subscriberIds = getSubscriberIds();
        int length = subscriberIds.length;
        for (int i = 0; i < length; ++i)
        {
            removeSubscriber(subscriberIds[i]);
        }
    }
    
}
