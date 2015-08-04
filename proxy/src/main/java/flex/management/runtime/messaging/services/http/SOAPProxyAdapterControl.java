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
package flex.management.runtime.messaging.services.http;

import flex.management.BaseControl;
import flex.messaging.services.http.SOAPProxyAdapter;

/**
 *
 * The <code>SOAPProxyAdapterControl</code> class is the MBean implementation
 * for monitoring and managing <code>SOAPProxyAdapter</code>s at runtime.
 */
public class SOAPProxyAdapterControl extends HTTPProxyAdapterControl implements
        SOAPProxyAdapterControlMBean
{
    private static final String TYPE = "SOAPProxyAdapter";
    
    /**
     * Constructs a <code>SOAPProxyAdapterControl</code>, assigning its id, managed
     * <code>SOAPProxyAdapter</code> and parent MBean.
     * 
     * @param serviceAdapter The <code>SOAPProxyAdapter</code> managed by this MBean.
     * @param parent The parent MBean in the management hierarchy.
     */
    public SOAPProxyAdapterControl(SOAPProxyAdapter serviceAdapter, BaseControl parent)
    {
        super(serviceAdapter, parent);
    }

    /**
     *
     *  (non-Javadoc)
     * @see flex.management.BaseControlMBean#getType()
     */
    public String getType()
    {
        return TYPE;
    }
}
