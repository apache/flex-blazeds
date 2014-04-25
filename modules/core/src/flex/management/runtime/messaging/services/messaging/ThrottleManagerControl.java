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
import flex.management.runtime.AdminConsoleTypes;
import flex.messaging.services.messaging.ThrottleManager;

import java.util.Date;

/**
 * The <code>ThrottleManagerControl</code> class is the MBean implementation for
 * monitoring and managing a <code>ThrottleManager</code> at runtime.
 *
 * @author shodgson
 */
public class ThrottleManagerControl extends BaseControl implements
        ThrottleManagerControlMBean
{
    private ThrottleManager throttleManager;
    private long clientIncomingMessageThrottleStart;
    private int clientIncomingMessageThrottleCount;
    private Date lastClientIncomingMessageThrottleTimestamp;
    private long clientOutgoingMessageThrottleStart;
    private int clientOutgoingMessageThrottleCount;
    private Date lastClientOutgoingMessageThrottleTimestamp;
    private long destinationIncomingMessageThrottleStart;
    private int destinationIncomingMessageThrottleCount;
    private Date lastDestinationIncomingMessageThrottleTimestamp;
    private long destinationOutgoingMessageThrottleStart;
    private int destinationOutgoingMessageThrottleCount;
    private Date lastDestinationOutgoingMessageThrottleTimestamp;

    /**
     * Constructs a new <code>ThrottleManagerControl</code> instance, assigning its
     * backing <code>ThrottleManager</code>.
     *
     * @param throttleManager The <code>ThrottleManager</code> managed by this MBean.
     * @param parent The parent MBean in the management hierarchy.
     */
    public ThrottleManagerControl(ThrottleManager throttleManager, BaseControl parent)
    {
        super(parent);
        this.throttleManager = throttleManager;
        clientIncomingMessageThrottleStart = System.currentTimeMillis();
        clientOutgoingMessageThrottleStart = clientIncomingMessageThrottleStart;
        destinationIncomingMessageThrottleStart = clientIncomingMessageThrottleStart;
        destinationOutgoingMessageThrottleStart = clientIncomingMessageThrottleStart;
    }

    @Override
    protected void onRegistrationComplete()
    {
        String name = this.getObjectName().getCanonicalName();
        String[] attributes = {
                "ClientIncomingMessageThrottleCount", "ClientIncomingMessageThrottleFrequency",
                "ClientOutgoingMessageThrottleCount", "ClientOutgoingMessageThrottleFrequency",
                "DestinationIncomingMessageThrottleCount", "DestinationIncomingMessageThrottleFrequency",
                "DestinationOutgoingMessageThrottleCount", "DestinationOutgoingMessageThrottleFrequency",
                "LastClientIncomingMessageThrottleTimestamp", "LastClientOutgoingMessageThrottleTimestamp",
                "LastDestinationIncomingMessageThrottleTimestamp", "LastDestinationOutgoingMessageThrottleTimestamp"
        };

        getRegistrar().registerObjects(AdminConsoleTypes.DESTINATION_POLLABLE, name, attributes);
    }

    /*
     *  (non-Javadoc)
     * @see flex.management.BaseControlMBean#getId()
     */
    @Override
    public String getId()
    {
        return throttleManager.getId();
    }

    /*
     *  (non-Javadoc)
     * @see flex.management.BaseControlMBean#getType()
     */
    @Override
    public String getType()
    {
        return ThrottleManager.TYPE;
    }

    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.ThrottleManagerControlMBean#getClientIncomingMessageThrottleCount()
     */
    public Integer getClientIncomingMessageThrottleCount()
    {
        return Integer.valueOf(clientIncomingMessageThrottleCount);
    }

    /**
     * Increments the count of throttled incoming client messages.
     */
    public void incrementClientIncomingMessageThrottleCount()
    {
        ++clientIncomingMessageThrottleCount;
        lastClientIncomingMessageThrottleTimestamp = new Date();
    }

    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.ThrottleManagerControlMBean#resetClientIncomingMessageThrottleCount()
     */
    public void resetClientIncomingMessageThrottleCount()
    {
        clientIncomingMessageThrottleStart = System.currentTimeMillis();
        clientIncomingMessageThrottleCount = 0;
        lastClientIncomingMessageThrottleTimestamp = null;
    }

    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.ThrottleManagerControlMBean#getLastClientIncomingMessageThrottleTimestamp()
     */
    public Date getLastClientIncomingMessageThrottleTimestamp()
    {
        return lastClientIncomingMessageThrottleTimestamp;
    }

    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.ThrottleManagerControlMBean#getClientIncomingMessageThrottleFrequency()
     */
    public Double getClientIncomingMessageThrottleFrequency()
    {
        if (clientIncomingMessageThrottleCount > 0)
        {
            double runtime = differenceInMinutes(clientIncomingMessageThrottleStart, System.currentTimeMillis());
            return new Double(clientIncomingMessageThrottleCount/runtime);
        }
        return new Double(0);
    }

    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.ThrottleManagerControlMBean#getClientOutgoingMessageThrottleCount()
     */
    public Integer getClientOutgoingMessageThrottleCount()
    {
        return Integer.valueOf(clientOutgoingMessageThrottleCount);
    }

    /**
     * Increments the count of throttled outgoing client messages.
     */
    public void incrementClientOutgoingMessageThrottleCount()
    {
        ++clientOutgoingMessageThrottleCount;
        lastClientOutgoingMessageThrottleTimestamp = new Date();
    }

    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.ThrottleManagerControlMBean#resetClientOutgoingMessageThrottleCount()
     */
    public void resetClientOutgoingMessageThrottleCount()
    {
        clientOutgoingMessageThrottleStart = System.currentTimeMillis();
        clientOutgoingMessageThrottleCount = 0;
        lastClientOutgoingMessageThrottleTimestamp = null;
    }

    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.ThrottleManagerControlMBean#getLastClientOutgoingMessageThrottleTimestamp()
     */
    public Date getLastClientOutgoingMessageThrottleTimestamp()
    {
        return lastClientOutgoingMessageThrottleTimestamp;
    }

    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.ThrottleManagerControlMBean#getClientOutgoingMessageThrottleFrequency()
     */
    public Double getClientOutgoingMessageThrottleFrequency()
    {
        if (clientOutgoingMessageThrottleCount > 0)
        {
            double runtime = differenceInMinutes(clientOutgoingMessageThrottleStart, System.currentTimeMillis());
            return new Double(clientOutgoingMessageThrottleCount/runtime);
        }
        return new Double(0);
    }

    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.ThrottleManagerControlMBean#getDestinationIncomingMessageThrottleCount()
     */
    public Integer getDestinationIncomingMessageThrottleCount()
    {
        return Integer.valueOf(destinationIncomingMessageThrottleCount);
    }

    /**
     * Increments the count of throttled incoming destination messages.
     */
    public void incrementDestinationIncomingMessageThrottleCount()
    {
        ++destinationIncomingMessageThrottleCount;
        lastDestinationIncomingMessageThrottleTimestamp = new Date();
    }

    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.ThrottleManagerControlMBean#resetDestinationIncomingMessageThrottleCount()
     */
    public void resetDestinationIncomingMessageThrottleCount()
    {
        destinationIncomingMessageThrottleStart = System.currentTimeMillis();
        destinationIncomingMessageThrottleCount = 0;
        lastDestinationIncomingMessageThrottleTimestamp = null;
    }

    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.ThrottleManagerControlMBean#getLastDestinationIncomingMessageThrottleTimestamp()
     */
    public Date getLastDestinationIncomingMessageThrottleTimestamp()
    {
        return lastDestinationIncomingMessageThrottleTimestamp;
    }

    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.ThrottleManagerControlMBean#getDestinationIncomingMessageThrottleFrequency()
     */
    public Double getDestinationIncomingMessageThrottleFrequency()
    {
        if (destinationIncomingMessageThrottleCount > 0)
        {
            double runtime = differenceInMinutes(destinationIncomingMessageThrottleStart, System.currentTimeMillis());
            return new Double(destinationIncomingMessageThrottleCount/runtime);
        }
        return new Double(0);
    }

    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.ThrottleManagerControlMBean#getDestinationOutgoingMessageThrottleCount()
     */
    public Integer getDestinationOutgoingMessageThrottleCount()
    {
        return Integer.valueOf(destinationOutgoingMessageThrottleCount);
    }

    /**
     * Increments the count of throttled outgoing destination messages.
     */
    public void incrementDestinationOutgoingMessageThrottleCount()
    {
        ++destinationOutgoingMessageThrottleCount;
        lastDestinationOutgoingMessageThrottleTimestamp = new Date();
    }

    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.ThrottleManagerControlMBean#resetDestinationOutgoingMessageThrottleCount()
     */
    public void resetDestinationOutgoingMessageThrottleCount()
    {
        destinationOutgoingMessageThrottleStart = System.currentTimeMillis();
        destinationOutgoingMessageThrottleCount = 0;
        lastDestinationOutgoingMessageThrottleTimestamp = null;
    }

    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.ThrottleManagerControlMBean#getLastDestinationOutgoingMessageThrottleTimestamp()
     */
    public Date getLastDestinationOutgoingMessageThrottleTimestamp()
    {
        return lastDestinationOutgoingMessageThrottleTimestamp;
    }

    public Double getDestinationOutgoingMessageThrottleFrequency()
    {
        if (destinationOutgoingMessageThrottleCount > 0)
        {
            double runtime = differenceInMinutes(destinationOutgoingMessageThrottleStart, System.currentTimeMillis());
            return new Double(destinationOutgoingMessageThrottleCount/runtime);
        }
        return new Double(0);
    }
}
