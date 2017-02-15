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

import flex.management.BaseControl;
import flex.management.runtime.messaging.services.ServiceControl;
import flex.messaging.Destination;
import flex.messaging.services.ServiceAdapter;

import javax.management.ObjectName;

/**
 * The <code>DestinationControl</code> class is the MBean implementation for
 * monitoring and managing a <code>Destination</code> at runtime.
 */
public abstract class DestinationControl extends BaseControl implements
        DestinationControlMBean
{
    protected Destination destination;
    private ObjectName adapter;
        
    /**
     * Constructs a new <code>DestinationControl</code> instance.
     * 
     * @param destination The <code>Destination</code> managed by this MBean.
     * @param parent The parent MBean in the management hierarchy.
     */
    public DestinationControl(Destination destination, BaseControl parent)
    {
        super(parent);
        this.destination = destination;
    }
        
    /*
     *  (non-Javadoc)
     * @see flex.management.BaseControlMBean#getId()
     */
    public String getId()
    {
        return destination.getId();
    }

    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.DestinationControlMBean#getAdapter()
     */
    public ObjectName getAdapter()
    {
        return adapter;
    }
    
    /**
     * Sets the <code>ObjectName</code> for the adapter associated with the managed destination.
     * 
     * @param value The <code>ObjectName</code> for the adapter.
     */
    public void setAdapter(ObjectName value)
    {
        adapter = value;
    }
    
    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.DestinationControlMBean#isRunning()
     */
    public Boolean isRunning()
    {
        return Boolean.valueOf(destination.isStarted());
    }
    
    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.DestinationControlMBean#getStartTimestamp()
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
        ServiceControl parent = (ServiceControl)getParentControl();
        parent.removeDestination(getObjectName());
        
        // Unregister adapter of the destination
        ServiceAdapter child = destination.getAdapter();
        if (child.getControl() != null)
        {
            child.getControl().unregister();
            child.setControl(null);
            child.setManaged(false);
        }
        
        super.preDeregister();
    }

}
