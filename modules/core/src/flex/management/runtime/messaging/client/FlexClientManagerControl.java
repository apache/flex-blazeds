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
package flex.management.runtime.messaging.client;

import flex.management.BaseControl;
import flex.management.runtime.AdminConsoleTypes;
import flex.messaging.client.FlexClientManager;

/**
 * @author majacobs
 *
 * @exclude
 */
public class FlexClientManagerControl extends BaseControl implements FlexClientManagerControlMBean
{
    private FlexClientManager flexClientManager;
    
    public FlexClientManagerControl(BaseControl parent, FlexClientManager manager)
    {
        super(parent);        
        flexClientManager = manager;
    }
    
    public void onRegistrationComplete()
    {
        String name = getObjectName().getCanonicalName();
        getRegistrar().registerObject(AdminConsoleTypes.GENERAL_POLLABLE, name, "FlexClientCount");
    }

    /* (non-Javadoc)
     * @see flex.management.BaseControl#getId()
     */
    public String getId()
    {
        return flexClientManager.getId();
    }

    /* (non-Javadoc)
     * @see flex.management.BaseControl#getType()
     */
    public String getType()
    {
        return flexClientManager.getId();
    }

    /* (non-Javadoc)
     * @see flex.management.runtime.messaging.client.FlexClientManagerControlMBean#getClientIds()
     */
    public String[] getClientIds() 
    {
        return flexClientManager.getClientIds();
    }

    /* (non-Javadoc)
     * @see flex.management.runtime.messaging.client.FlexClientManagerControlMBean#getClientLastUse(java.lang.String)
     */
    public Long getClientLastUse(String clientId)
    {
        return new Long(flexClientManager.getFlexClient(clientId).getLastUse());
    }

    /* (non-Javadoc)
     * @see flex.management.runtime.messaging.client.FlexClientManagerControlMBean#getClientSessionCount(java.lang.String)
     */
    public Integer getClientSessionCount(String clientId)
    {
        return new Integer(flexClientManager.getFlexClient(clientId).getSessionCount());
    }

    /* (non-Javadoc)
     * @see flex.management.runtime.messaging.client.FlexClientManagerControlMBean#getClientSubscriptionCount(java.lang.String)
     */
    public Integer getClientSubscriptionCount(String clientId)
    {
        return new Integer(flexClientManager.getFlexClient(clientId).getSubscriptionCount());
    }
    
    /* (non-Javadoc)
     * @see flex.management.runtime.messaging.client.FlexClientManagerControlMBean#getFlexClientCount()
     */
    public Integer getFlexClientCount() 
    {
        return new Integer(flexClientManager.getFlexClientCount());
    }    
}
