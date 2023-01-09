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
package flex.management;

import java.lang.reflect.Method;

import javax.management.MBeanServer;

import flex.messaging.log.Log;
import flex.messaging.log.LogCategories;
import flex.messaging.util.ClassUtil;

/**
 * Custom MBeanServerLocator for use with WebSphere.
 * This class locates a MBean server instance via WebSphere's administration APIs.
 */
public class WebSphereMBeanServerLocator implements MBeanServerLocator {
    //--------------------------------------------------------------------------
    //
    // Private Static Variables
    //
    //--------------------------------------------------------------------------

    /**
     * Localized error constant.
     */
    private static final int FAILED_TO_LOCATE_MBEAN_SERVER = 10427;

    //--------------------------------------------------------------------------
    //
    // Private Variables
    //
    //--------------------------------------------------------------------------

    /**
     * Reference to MBeanServer this locator found.
     */
    private MBeanServer server;

    //--------------------------------------------------------------------------
    //
    // Public Methods
    //
    //--------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public synchronized MBeanServer getMBeanServer() {
        if (server == null) {
            Class adminServiceClass = ClassUtil.createClass("com.ibm.websphere.management.AdminServiceFactory");
            try {
                Method getMBeanFactoryMethod = adminServiceClass.getMethod("getMBeanFactory", new Class[0]);
                Object mbeanFactory = getMBeanFactoryMethod.invoke(null, new Object[0]);
                Method getMBeanServerMethod = mbeanFactory.getClass().getMethod("getMBeanServer", new Class[0]);
                server = (MBeanServer) getMBeanServerMethod.invoke(mbeanFactory, new Object[0]);
            } catch (Exception e) {
                ManagementException me = new ManagementException();
                me.setMessage(FAILED_TO_LOCATE_MBEAN_SERVER, new Object[]{getClass().getName()});
                me.setRootCause(e);
                throw me;
            }
            if (Log.isDebug())
                Log.getLogger(LogCategories.MANAGEMENT_MBEANSERVER).debug("Using MBeanServer: " + server);
        }
        return server;
    }
}