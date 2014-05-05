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

import flex.management.BaseControlMBean;

import java.io.IOException;
import java.util.Date;
import javax.management.ObjectName;

/**
 * Defines the runtime monitoring and management interface for managed services.
 *
 * @author shodgson
 */
public interface ServiceControlMBean extends BaseControlMBean
{

    /**
     * Returns <code>true</code> if the <code>Service</code> is running.
     *
     * @return <code>true</code> if the <code>Service</code> is running.
     * @throws IOException Throws IOException.
     */
    Boolean isRunning() throws IOException;


    /**
     * Returns the start timestamp for the <code>Service</code>.
     *
     * @return The start timestamp for the <code>Service</code>.
     * @throws IOException Throws IOException.
     */
    Date getStartTimestamp() throws IOException;

    /**
     * Returns the <code>ObjectName</code>s of all destinations registered with the
     * managed service.
     *
     * @return The <code>ObjectName</code>s of all destinations registered with the
     * managed service.
     * @throws IOException Throws IOException.
     */
    ObjectName[] getDestinations() throws IOException;
}
