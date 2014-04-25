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

import flex.management.BaseControl;
import flex.management.runtime.AdminConsoleDisplayRegistrar;
import flex.management.runtime.AdminConsoleTypes;
import flex.messaging.MessageBroker;
import flex.messaging.endpoints.AMFEndpoint;
import flex.messaging.endpoints.AbstractEndpoint;
import flex.messaging.endpoints.Endpoint;
import flex.messaging.endpoints.HTTPEndpoint;
import flex.messaging.endpoints.StreamingAMFEndpoint;
import flex.messaging.endpoints.StreamingHTTPEndpoint;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import javax.management.ObjectName;

/**
 * The <code>MessageBrokerControl</code> class is the MBean implemenation for monitoring
 * and managing a <code>MessageBroker</code> at runtime.
 *
 * @author shodgson
 * @author majacobs
 */
public class MessageBrokerControl extends BaseControl implements MessageBrokerControlMBean
{
    private static final Object classMutex = new Object();
    private static final String TYPE = "MessageBroker";
    private static int instanceCount = 0;
    private String id;
    private MessageBroker broker;
    private List endpointNames;
    private List amfEndpoints;
    private List httpEndpoints;
    private List enterpriseEndpoints;
    private List streamingAmfEndpoints;
    private List streamingHttpEndpoints;
    private List services;

    /**
     * Constructs a new <code>MessageBrokerControl</code> instance, assigning its
     * backing <code>MessageBroker</code>.
     *
     * @param broker The <code>MessageBroker</code> managed by this MBean.
     */
    public MessageBrokerControl(MessageBroker broker)
    {
        super(null);
        this.broker = broker;
        endpointNames = new ArrayList();
        amfEndpoints = new ArrayList();
        httpEndpoints = new ArrayList();
        enterpriseEndpoints = new ArrayList();
        streamingAmfEndpoints = new ArrayList();
        streamingHttpEndpoints = new ArrayList();
        services = new ArrayList();
        synchronized (classMutex)
        {
            id = TYPE + ++instanceCount;
        }

        setRegistrar(new AdminConsoleDisplayRegistrar(this));
    }

    protected void onRegistrationComplete()
    {
        String name = this.getObjectName().getCanonicalName();
        getRegistrar().registerObject(AdminConsoleTypes.GENERAL_POLLABLE, name, "FlexSessionCount");
        getRegistrar().registerObjects(new int[] {AdminConsoleTypes.GENERAL_POLLABLE, AdminConsoleTypes.GRAPH_BY_POLL_INTERVAL },
                name, new String[] {"AMFThroughput", "HTTPThroughput", "EnterpriseThroughput"});

        getRegistrar().registerObject(AdminConsoleTypes.GENERAL_SERVER, name, "MaxFlexSessionsInCurrentHour");
    }

    /*
     *  (non-Javadoc)
     * @see flex.management.BaseControlMBean#getId()
     */
    public String getId()
    {
        return id;
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
     * @see flex.management.runtime.MessageBrokerControlMBean#isRunning()
     */
    public Boolean isRunning()
    {
        return Boolean.valueOf(broker.isStarted());
    }

    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.MessageBrokerControlMBean#getStartTimestamp()
     */
    public Date getStartTimestamp()
    {
        return startTimestamp;
    }

    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.MessageBrokerControlMBean#getEndpoints()
     */
    public ObjectName[] getEndpoints() throws IOException
    {
        int size = endpointNames.size();
        ObjectName[] endpointNameObjects = new ObjectName[size];
        for (int i = 0; i < size; ++i)
        {
            endpointNameObjects[i] = (ObjectName)endpointNames.get(i);
        }
        return endpointNameObjects;
    }

    /**
     * Adds an <code>Endpoint</code> for an endpoint registered with the backing <code>MessageBroker</code>.
     *
     * @param value The endpoint <code>Endpoint</code>.
     */
    public void addEndpoint(Endpoint value)
    {
        if (value instanceof AMFEndpoint)
            amfEndpoints.add(value);
        else if (value instanceof HTTPEndpoint)
            httpEndpoints.add(value);
        else if (value instanceof StreamingAMFEndpoint)
            streamingAmfEndpoints.add(value);
        else if (value instanceof StreamingHTTPEndpoint)
            streamingHttpEndpoints.add(value);
        else
            enterpriseEndpoints.add(value);

        endpointNames.add(value.getControl().getObjectName());
    }

    /**
     * Removes an <code>ObjectName</code> for an endpoint registered with the backing <code>MessageBroker</code>.
     *
     * @param value The endpoint <code>ObjectName</code>.
     */
    public void removeEndpoint(ObjectName value)
    {
        endpointNames.remove(value);
    }

    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.MessageBrokerControlMBean#getServices()
     */
    public ObjectName[] getServices() throws IOException
    {
        int size = services.size();
        ObjectName[] serviceNames = new ObjectName[size];
        for (int i = 0; i < size; ++i)
        {
            serviceNames[i] = (ObjectName)services.get(i);
        }
        return serviceNames;
    }

    /**
     * Adds an <code>ObjectName</code> for a service registered with the backing <code>MessageBroker</code>.
     *
     * @param value The service <code>ObjectName</code>.
     */
    public void addService(ObjectName value)
    {
        services.add(value);
    }

    /**
     * Removes an <code>ObjectName</code> for a service registered with the backing <code>MessageBroker</code>.
     *
     * @param value The service <code>ObjectName</code>.
     */
    public void removeService(ObjectName value)
    {
        services.remove(value);
    }

    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.MessageBrokerControlMBean#getFlexSessionCount()
     */
    public Integer getFlexSessionCount()
    {
        return broker.getFlexSessionManager().getFlexSessionCount();
    }

    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.MessageBrokerControlMBean#getMaxFlexSessionsInCurrentHour()
     */
    public Integer getMaxFlexSessionsInCurrentHour()
    {
        return broker.getFlexSessionManager().getMaxFlexSessionsInCurrentHour();
    }

    /* (non-Javadoc)
     * @see flex.management.runtime.messaging.MessageBrokerControlMBean#getRTMPConnectionCount()
     */
    public Integer getEnterpriseConnectionCount() throws IOException
    {
        int connections = 0;
    /*
        for (int i = 0; i < rtmpEndpoints.size(); i++)
        {
            connections += (((RTMPEndpoint)rtmpEndpoints.get(i)).getConnectionCount());
        }
    */
        return new Integer(connections);
    }

    /* (non-Javadoc)
     * @see flex.management.runtime.messaging.MessageBrokerControlMBean#getAMFThroughput()
     */
    public Long getAMFThroughput() throws IOException
    {
        return new Long(calculateEndpointThroughput(amfEndpoints));
    }

    /* (non-Javadoc)
     * @see flex.management.runtime.messaging.MessageBrokerControlMBean#getHTTPThroughput()
     */
    public Long getHTTPThroughput() throws IOException
    {
        return new Long(calculateEndpointThroughput(httpEndpoints));
    }

    /* (non-Javadoc)
     * @see flex.management.runtime.messaging.MessageBrokerControlMBean#getRTMPThroughput()
     */
    public Long getEnterpriseThroughput() throws IOException
    {
        return new Long(calculateEndpointThroughput(enterpriseEndpoints));
    }

    /* (non-Javadoc)
     * @see flex.management.runtime.messaging.MessageBrokerControlMBean#getStreamingAMFThroughput()
     */
    public Long getStreamingAMFThroughput() throws IOException
    {
        return new Long(calculateEndpointThroughput(streamingAmfEndpoints));
    }

    /* (non-Javadoc)
     * @see flex.management.runtime.messaging.MessageBrokerControlMBean#getStreamingHTTPThroughput()
     */
    public Long getStreamingHTTPThroughput() throws IOException
    {
        return new Long(calculateEndpointThroughput(streamingHttpEndpoints));
    }

    private long calculateEndpointThroughput(List endpoints)
    {
        long throughput = 0;

        for (int i = 0; i < endpoints.size(); i++)
        {
            // This method shouldn't be used with Lists containing objects that are not AbstractEndpoints
            if (endpoints.get(i) instanceof AbstractEndpoint)
                throughput += ((AbstractEndpoint)endpoints.get(i)).getThroughput();
        }

        return throughput;
    }
}
