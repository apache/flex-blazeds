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

import java.util.Date;

import flex.management.BaseControl;
import flex.management.runtime.AdminConsoleTypes;
import flex.messaging.endpoints.BaseStreamingHTTPEndpoint;

/**
 * The <code>StreamingEndpointControl</code> class is the base MBean implementation
 * for monitoring and managing a <code>BaseStreamingHTTPEndpoint</code> at runtime.
 */
public abstract class StreamingEndpointControl extends EndpointControl implements
        StreamingEndpointControlMBean
{   
    private int pushCount;
    private Date lastPushTimeStamp;
    private long pushStart;
    
    /**
     * Constructs a <code>StreamingEndpointControl</code>, assigning managed message 
     * endpoint and parent MBean.
     * 
     * @param endpoint The <code>BaseStreamingHTTPEndpoint</code> managed by this MBean.
     * @param parent The parent MBean in the management hierarchy.
     */
    public StreamingEndpointControl(BaseStreamingHTTPEndpoint endpoint, BaseControl parent)
    {
        super(endpoint, parent);
    }
        
    protected void onRegistrationComplete()
    {
        super.onRegistrationComplete();
        
        String name = this.getObjectName().getCanonicalName();
        String[] generalPollables = { "LastPushTimestamp", "PushCount", "PushFrequency", "StreamingClientsCount"};
        
        getRegistrar().registerObjects(AdminConsoleTypes.ENDPOINT_POLLABLE, name, generalPollables);
        getRegistrar().registerObject(AdminConsoleTypes.ENDPOINT_SCALAR, name, "MaxStreamingClients");
    }
    
    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.messaging.endpoints.StreamingEndpointControlMBean#getMaxStreamingClients()
     */
    public Integer getMaxStreamingClients() 
    {
        int maxStreamingClientsCount = ((BaseStreamingHTTPEndpoint)endpoint).getMaxStreamingClients();
        return new Integer(maxStreamingClientsCount);
    }

    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.messaging.endpoints.StreamingEndpointControlMBean#getPushCount()
     */
    public Integer getPushCount()
    {
        return new Integer(pushCount);
    }
    
    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.messaging.endpoints.StreamingEndpointControlMBean#resetPushCount()
     */
    public void resetPushCount()
    {
        pushStart = System.currentTimeMillis();
        pushCount = 0;
        lastPushTimeStamp = null;
    }
    
    /**
     * Increments the count of messages pushed by the endpoint.
     */
    public void incrementPushCount()
    {
        ++pushCount;
        lastPushTimeStamp = new Date();
    }    
    
    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.messaging.endpoints.StreamingEndpointControlMBean#getLastPushTimestamp()
     */
    public Date getLastPushTimestamp()
    {
        return lastPushTimeStamp;
    }
    
    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.messaging.endpoints.StreamingEndpointControlMBean#getPushFrequency()
     */
    public Double getPushFrequency()
    {
        if (pushCount > 0)
        {
            double runtime = differenceInMinutes(pushStart, System.currentTimeMillis());
            return new Double(pushCount/runtime);
        }
        else
        {
            return new Double(0);
        }
    }
    
    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.messaging.endpoints.StreamingEndpointControlMBean#isRunning()
     */
    public Integer getStreamingClientsCount()
    {
        int streamingClientsCount = ((BaseStreamingHTTPEndpoint)endpoint).getStreamingClientsCount();
        return new Integer(streamingClientsCount);
    }
}
