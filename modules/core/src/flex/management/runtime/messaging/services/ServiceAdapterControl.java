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
package flex.management.runtime.messaging.services;

import java.util.Date;

import flex.management.BaseControl;
import flex.management.runtime.messaging.DestinationControl;
import flex.messaging.services.ServiceAdapter;

/**
 * The <code>ServiceAdapterControl</code> class is the base MBean implementation 
 * for monitoring and managing a <code>ServiceAdapter</code> at runtime.
 * 
 * @author shodgson
 */
public abstract class ServiceAdapterControl extends BaseControl implements
        ServiceAdapterControlMBean
{
    protected ServiceAdapter serviceAdapter;  

    /**
     * Constructs a <code>ServiceAdapterControl</code>, assigning its id, managed service 
     * adapter and parent MBean.
     * 
     * @param serviceAdapter The <code>ServiceAdapter</code> managed by this MBean.
     * @param parent The parent MBean in the management hierarchy.
     */
    public ServiceAdapterControl(ServiceAdapter serviceAdapter, BaseControl parent)
    {
        super(parent);    
        this.serviceAdapter = serviceAdapter;  
    }
    
    /*
     *  (non-Javadoc)
     * @see flex.management.BaseControlMBean#getId()
     */
    public String getId()
    {
        return serviceAdapter.getId();
    }
    
    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.ServiceAdapterControlMBean#isRunning()
     */
    public Boolean isRunning()
    {
        return Boolean.valueOf(serviceAdapter.isStarted());
    }
    
    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.ServiceAdapterControlMBean#getStartTimestamp()
     */
    public Date getStartTimestamp()
    {
        return startTimestamp;
    }
    
    /*
     *  (non-Javadoc)
     * @see javax.management.MBeanRegistration#preDeregister()
     */
    public void preDeregister() throws Exception
    {
        DestinationControl parent = (DestinationControl)getParentControl();
        parent.setAdapter(null);
       
        super.preDeregister();
    }
}
