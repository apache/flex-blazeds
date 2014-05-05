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
package flex.management.runtime.messaging.services;

import flex.management.BaseControl;
import flex.messaging.services.MessageService;

/**
 * The <code>MessageServiceControl</code> class is the MBean implemenation
 * for monitoring and managing a <code>MessageService</code> at runtime.
 *
 * @author shodgson
 */
public class MessageServiceControl extends ServiceControl implements
        MessageServiceControlMBean
{
    private static final String TYPE = "MessageService";

    /**
     * Constructs a <code>MessageServiceControl</code>, assigning its id, managed
     * message service and parent MBean.
     *
     * @param service The <code>MessageService</code> managed by this MBean.
     * @param parent The parent MBean in the management hierarchy.
     */
    public MessageServiceControl(MessageService service, BaseControl parent)
    {
        super(service, parent);
    }

    /** {@inheritDoc} */
    public String getType()
    {
        return TYPE;
    }

}
