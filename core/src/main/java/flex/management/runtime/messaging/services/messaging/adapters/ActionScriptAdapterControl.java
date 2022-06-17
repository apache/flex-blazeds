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
package flex.management.runtime.messaging.services.messaging.adapters;

import flex.messaging.services.messaging.adapters.ActionScriptAdapter;
import flex.management.BaseControl;
import flex.management.runtime.messaging.services.ServiceAdapterControl;

/**
 * The <code>ActionScriptAdapterControl</code> class is the MBean implemenation
 * for monitoring and managing <code>ActionScriptAdapter</code>s at runtime.
 */
public class ActionScriptAdapterControl extends ServiceAdapterControl implements ActionScriptAdapterControlMBean {
    private static final String TYPE = "ActionScriptAdapter";

    /**
     * Constructs a <code>ActionScriptAdapterControl</code>, assigning its id, managed
     * <code>ActionScriptAdapter</code> and parent MBean.
     *
     * @param serviceAdapter The <code>ActionScriptAdapter</code> managed by this MBean.
     * @param parent         The parent MBean in the management hierarchy.
     */
    public ActionScriptAdapterControl(ActionScriptAdapter serviceAdapter, BaseControl parent) {
        super(serviceAdapter, parent);
    }

    /**
     * {@inheritDoc}
     */
    public String getType() {
        return TYPE;
    }


}
