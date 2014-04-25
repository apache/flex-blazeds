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
package flex.management.runtime.messaging.endpoints;

import flex.management.BaseControl;
import flex.management.runtime.AdminConsoleTypes;
import flex.management.runtime.messaging.MessageBrokerControl;
import flex.messaging.config.SecurityConstraint;
import flex.messaging.endpoints.Endpoint;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The <code>EndpointControl</code> class is the MBean implementation for
 * monitoring and managing an <code>Endpoint</code> at runtime.
 *
 * @author shodgson
 */
public abstract class EndpointControl extends BaseControl implements EndpointControlMBean
{
    protected Endpoint endpoint;
    private AtomicInteger serviceMessageCount = new AtomicInteger(0);
    private Date lastServiceMessageTimestamp;
    private long serviceMessageStart;
    private AtomicLong bytesDeserialized = new AtomicLong(0);
    private AtomicLong bytesSerialized = new AtomicLong(0);

    /**
     * Constructs an <code>EndpointControl</code>, assigning its managed endpoint and
     * parent MBean.
     *
     * @param endpoint The <code>Endpoint</code> managed by this MBean.
     * @param parent The parent MBean in the management hierarchy.
     */
    public EndpointControl(Endpoint endpoint, BaseControl parent)
    {
        super(parent);
        this.endpoint = endpoint;
        serviceMessageStart = System.currentTimeMillis();
    }


    protected void onRegistrationComplete()
    {
        String name = this.getObjectName().getCanonicalName();
        String[] generalNames = { "SecurityConstraint"};
        String[] generalPollables = { "ServiceMessageCount", "LastServiceMessageTimestamp", "ServiceMessageFrequency"};
        String[] pollableGraphByInterval = {"BytesDeserialized", "BytesSerialized"};

        getRegistrar().registerObjects(AdminConsoleTypes.ENDPOINT_SCALAR,
                name, generalNames);
        getRegistrar().registerObjects(AdminConsoleTypes.ENDPOINT_POLLABLE,
                name, generalPollables);
        getRegistrar().registerObjects(new int[] {AdminConsoleTypes.GRAPH_BY_POLL_INTERVAL, AdminConsoleTypes.ENDPOINT_POLLABLE},
                name, pollableGraphByInterval);
    }

    /*
     *  (non-Javadoc)
     * @see flex.management.BaseControlMBean#getId()
     */
    public String getId()
    {
        return endpoint.getId();
    }

    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.EndpointControlMBean#isRunning()
     */
    public Boolean isRunning()
    {
        return Boolean.valueOf(endpoint.isStarted());
    }

    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.EndpointControlMBean#getStartTimestamp()
     */
    public Date getStartTimestamp()
    {
        return startTimestamp;
    }

    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.EndpointControlMBean#getServiceMessageCount()
     */
    public Integer getServiceMessageCount()
    {
        return Integer.valueOf(serviceMessageCount.get());
    }

    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.EndpointControlMBean#resetServiceMessageCount()
     */
    public void resetServiceMessageCount()
    {
        serviceMessageStart = System.currentTimeMillis();
        serviceMessageCount = new AtomicInteger(0);
        lastServiceMessageTimestamp = null;
    }

    /**
     * Increments the count of <code>serviceMessage()</code> invocations by the endpoint.
     */
    public void incrementServiceMessageCount()
    {
        serviceMessageCount.incrementAndGet();
        lastServiceMessageTimestamp = new Date();
    }

    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.EndpointControlMBean#getLastServiceMessageTimestamp()
     */
    public Date getLastServiceMessageTimestamp()
    {
        return lastServiceMessageTimestamp;
    }

    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.EndpointControlMBean#getServiceMessageFrequency()
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
     * @see javax.management.MBeanRegistration#preDeregister()
     */
    public void preDeregister() throws Exception
    {
        MessageBrokerControl parent = (MessageBrokerControl)getParentControl();
        parent.removeEndpoint(getObjectName());
    }

    public String getURI()
    {
        return endpoint.getUrl();
    }

    public String getSecurityConstraint()
    {
        return getSecurityConstraintOf(endpoint);
    }

    public static String getSecurityConstraintOf(Endpoint endpoint)
    {
        String result = "None";

        SecurityConstraint constraint = endpoint.getSecurityConstraint();
        if (constraint != null)
        {
            String authMethod = constraint.getMethod();
            if (authMethod != null)
            {
                StringBuffer buffer = new StringBuffer();
                buffer.append(authMethod);

                List roles = constraint.getRoles();
                if ((roles != null) && !roles.isEmpty())
                {
                    buffer.append(':');
                    for (int i = 0; i < roles.size(); i++)
                    {
                        if (i > 0)
                        {
                            buffer.append(',');
                        }
                        buffer.append(' ');
                        buffer.append(roles.get(i));
                    }
                }
                result = buffer.toString();
            }
        }
        return result;
    }

    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.EndpointControlMBean#getBytesDeserialized()
     */
    public Long getBytesDeserialized(){
        return Long.valueOf(bytesDeserialized.get());
    }

    /**
     * Increments the count of bytes deserialized by the endpoint.
     * @param currentBytesDeserialized the bytes is deserialized
     */
    public void addToBytesDeserialized(int currentBytesDeserialized) {
        bytesDeserialized.addAndGet(currentBytesDeserialized);
    }

    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.EndpointControlMBean#getBytesSerialized()
     */
    public Long getBytesSerialized() {
        return Long.valueOf(bytesSerialized.get());
    }

    /**
     * Increments the count of bytes serialized by the endpoint.
     * @param currentBytesSerialized the bytes is serialized
     */
    public void addToBytesSerialized(int currentBytesSerialized) {
        bytesSerialized.addAndGet(currentBytesSerialized);
    }
}
