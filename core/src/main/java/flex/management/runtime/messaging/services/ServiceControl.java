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

import flex.management.BaseControl;
import flex.management.runtime.messaging.MessageBrokerControl;
import flex.messaging.Destination;
import flex.messaging.services.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.management.ObjectName;

/**
 * The <code>ServiceControl</code> class is the MBean implementation for
 * monitoring and managing a <code>Service</code> at runtime.
 */
public abstract class ServiceControl extends BaseControl implements ServiceControlMBean {
    protected Service service;
    private List destinations;

    /**
     * Constructs a <code>ServiceControl</code>, assigning its id, managed service and
     * parent MBean.
     *
     * @param service The <code>Service</code> managed by this MBean.
     * @param parent  The parent MBean in the management hierarchy.
     */
    public ServiceControl(Service service, BaseControl parent) {
        super(parent);
        this.service = service;
        destinations = new ArrayList();
    }

    /*
     *  (non-Javadoc)
     * @see flex.management.BaseControlMBean#getId()
     */
    public String getId() {
        return service.getId();
    }

    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.ServiceControlMBean#isRunning()
     */
    public Boolean isRunning() {
        return Boolean.valueOf(service.isStarted());
    }


    /**
     * Adds the <code>ObjectName</code> of a destination registered with the managed service.
     *
     * @param value The <code>ObjectName</code> of a destination registered with the managed service.
     */
    public void addDestination(ObjectName value) {
        destinations.add(value);
    }

    /**
     * Removes the <code>ObjectName</code> of a destination registered with the managed service.
     *
     * @param value The <code>ObjectName</code> of a destination registered with the managed service.
     */
    public void removeDestination(ObjectName value) {
        destinations.remove(value);
    }

    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.ServiceControlMBean#getDestinations()
     */
    public ObjectName[] getDestinations() {
        int size = destinations.size();
        ObjectName[] destinationNames = new ObjectName[size];
        for (int i = 0; i < size; ++i) {
            destinationNames[i] = (ObjectName) destinations.get(i);
        }
        return destinationNames;
    }


    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.ServiceControlMBean#getStartTimestamp()
     */
    public Date getStartTimestamp() {
        return startTimestamp;
    }


    /*
     *  (non-Javadoc)
     * @see javax.management.MBeanRegistration#preDeregister()
     */
    public void preDeregister() throws Exception {
        MessageBrokerControl parent = (MessageBrokerControl) getParentControl();
        parent.removeService(getObjectName());

        // Unregister destinations of the service
        for (Iterator iter = service.getDestinations().values().iterator(); iter.hasNext(); ) {
            Destination child = (Destination) iter.next();
            if (child.getControl() != null) {
                child.getControl().unregister();
                child.setControl(null);
                child.setManaged(false);
            }

        }

        super.preDeregister();
    }

}
