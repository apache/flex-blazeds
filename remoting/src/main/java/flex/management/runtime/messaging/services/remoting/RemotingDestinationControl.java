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
package flex.management.runtime.messaging.services.remoting;

import java.io.IOException;

import flex.management.BaseControl;
import flex.management.runtime.AdminConsoleTypes;
import flex.management.runtime.messaging.DestinationControl;
import flex.messaging.services.remoting.RemotingDestination;

/**
 * The <code>RemotingDestinationControl</code> class is the MBean implementation for
 * monitoring and managing a <code>RemotingDestination</code> at runtime.
 *
 * This class performs no internal synchronization, so the statistics it tracks may differ slightly from
 * the true values but they don't warrant the cost full synchronization.
 */
public class RemotingDestinationControl extends DestinationControl implements
        RemotingDestinationControlMBean
{
    private static final String TYPE = "RemotingDestination";

    /**
     * Constructs a new <code>RemotingDestinationControl</code> instance.
     *
     * @param destination The <code>RemotingDestination</code> managed by this MBean.
     * @param parent The parent MBean in the management hierarchy.
     */
    public RemotingDestinationControl(RemotingDestination destination, BaseControl parent)
    {
        super(destination, parent);
    }

    private int invocationSuccessCount = 0;
    private int invocationFaultCount = 0;
    private int totalProcessingTimeMillis = 0;
    private int averageProcessingTimeMillis = 0;

    /** {@inheritDoc} */
    @Override
    public String getType()
    {
        return TYPE;
    }

    /** {@inheritDoc} */
    public Integer getInvocationSuccessCount() throws IOException
    {
        return Integer.valueOf(invocationSuccessCount);
    }

    /**
     * Increments the invocation success count by one.
     *
     * @param processingTimeMillis The processing duration of the invocation success.
     */
    public void incrementInvocationSuccessCount(int processingTimeMillis)
    {
        try
        {
            invocationSuccessCount++;
            totalProcessingTimeMillis += processingTimeMillis;
            averageProcessingTimeMillis = totalProcessingTimeMillis / (invocationSuccessCount + invocationFaultCount);
        }
        catch (Exception needsReset)
        {
            reset();
        }
    }

    /** {@inheritDoc} */
    public Integer getInvocationFaultCount() throws IOException
    {
        return Integer.valueOf(invocationFaultCount);
    }

    /**
     * Increments the invocation fault count by one.
     *
     * @param processingTimeMillis The processing duration of the invocation fault.
     */
    public void incrementInvocationFaultCount(int processingTimeMillis)
    {
        try
        {
            invocationFaultCount++;
            totalProcessingTimeMillis += processingTimeMillis;
            averageProcessingTimeMillis = totalProcessingTimeMillis / (invocationSuccessCount + invocationFaultCount);
        }
        catch (Exception needsReset)
        {
            reset();
        }
    }

    /** {@inheritDoc} */
    public Integer getAverageInvocationProcessingTimeMillis() throws IOException
    {
        return Integer.valueOf(averageProcessingTimeMillis);
    }

    /**
     * Callback used to register properties for display in the admin application.
     */
    @Override
    protected void onRegistrationComplete()
    {
        String name = this.getObjectName().getCanonicalName();

        String[] pollablePerInterval = { "InvocationSuccessCount", "InvocationFaultCount",
                "AverageInvocationProcessingTimeMillis" };

        getRegistrar().registerObjects(
                new int[] {AdminConsoleTypes.DESTINATION_POLLABLE, AdminConsoleTypes.GRAPH_BY_POLL_INTERVAL},
                name, pollablePerInterval);
    }

    /**
     * Helper method to reset state in the case of errors updating statistics.
     */
    private void reset()
    {
        invocationSuccessCount = 0;
        invocationFaultCount = 0;
        totalProcessingTimeMillis = 0;
        averageProcessingTimeMillis = 0;
    }
}
