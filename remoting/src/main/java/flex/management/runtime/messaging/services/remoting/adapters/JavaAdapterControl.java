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
package flex.management.runtime.messaging.services.remoting.adapters;

import flex.management.BaseControl;
import flex.management.runtime.messaging.services.ServiceAdapterControl;
import flex.messaging.services.remoting.adapters.JavaAdapter;

/**
 * The <code>JavaAdapterControl</code> class is the MBean implemenation
 * for monitoring and managing Java service adapters at runtime.
 */
public class JavaAdapterControl extends ServiceAdapterControl implements
        JavaAdapterControlMBean
{
    private static final String TYPE = "JavaAdapter";
    
    /**
     * Constructs a <code>JavaAdapterControl</code>, assigning its id, managed
     * Java service adapter and parent MBean.
     * 
     * @param serviceAdapter The <code>JavaAdapter</code> managed by this MBean.
     * @param parent The parent MBean in the management hierarchy.
     */
    public JavaAdapterControl(JavaAdapter serviceAdapter, BaseControl parent)
    {
        super(serviceAdapter, parent);
    }

    /** {@inheritDoc} */
    public String getType()
    {
        return TYPE;
    }
}
