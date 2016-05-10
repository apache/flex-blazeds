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
import flex.messaging.endpoints.HTTPEndpoint;

/**
 * The <code>HTTPEndpointControl</code> class is the MBean implemenation
 * for monitoring and managing a <code>HTTPEndpoint</code> at runtime.
 */
public class HTTPEndpointControl extends PollingEndpointControl implements
    HTTPEndpointControlMBean
{
    private static final String TYPE = "HTTPEndpoint";

    /**
     * Constructs a <code>HTTPEndpointControl</code>, assigning managed message
     * endpoint and parent MBean.
     *
     * @param endpoint The <code>HTTPEndpoint</code> managed by this MBean.
     * @param parent The parent MBean in the management hierarchy.
     */
    public HTTPEndpointControl(HTTPEndpoint endpoint, BaseControl parent)
    {
        super(endpoint, parent);
    }

    /** {@inheritDoc} */
    public String getType()
    {
        return TYPE;
    }
}
