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
import flex.messaging.endpoints.BasePollingHTTPEndpoint;

/**
 * The <tt>PollingEndpointControl</tt> class is the base MBean implementation
 * for monitoring and managing a <tt>BasePollingHTTPEndpoint</tt> at runtime.
 */
public abstract class PollingEndpointControl extends EndpointControl implements
        PollingEndpointControlMBean
{
    /**
     * Constructs a <tt>PollingEndpointControl</tt>, assigning managed message
     * endpoint and parent MBean.
     *
     * @param endpoint The <code>BasePollingHTTPEndpoint</code> managed by this MBean.
     * @param parent The parent MBean in the management hierarchy.
     */
    public PollingEndpointControl(BasePollingHTTPEndpoint endpoint, BaseControl parent)
    {
        super(endpoint, parent);
    }

    protected void onRegistrationComplete()
    {
        super.onRegistrationComplete();

        String name = this.getObjectName().getCanonicalName();
        String[] generalPollables = {"WaitingPollRequestsCount"};

        getRegistrar().registerObjects(AdminConsoleTypes.ENDPOINT_POLLABLE, name, generalPollables);
        getRegistrar().registerObject(AdminConsoleTypes.ENDPOINT_SCALAR, name, "MaxWaitingPollRequests");
    }

    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.messaging.endpoints.PollingEndpointControlMBean#getMaxWaitingPollRequests()
     */
    public Integer getMaxWaitingPollRequests()
    {
        int maxWaitingPollRequests = ((BasePollingHTTPEndpoint)endpoint).getMaxWaitingPollRequests();
        return new Integer(maxWaitingPollRequests);
    }

    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.messaging.endpoints.PollingEndpointControlMBean#getWaitingPollRequestsCount()
     */
    public Integer getWaitingPollRequestsCount()
    {
        int waitingPollRequestsCount = ((BasePollingHTTPEndpoint)endpoint).getWaitingPollRequestsCount();
        return new Integer(waitingPollRequestsCount);
    }
}
