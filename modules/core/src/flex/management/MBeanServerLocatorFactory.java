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

import flex.messaging.log.Log;
import flex.messaging.log.LogCategories;
import flex.messaging.util.ClassUtil;

/**
 * Factory to get a <code>MBeanServerLocator</code>.
 */
public class MBeanServerLocatorFactory
{
    //--------------------------------------------------------------------------
    //
    // Private Static Variables
    //
    //--------------------------------------------------------------------------
    
    /**
     * The MBeanServerLocator impl to use; lazily init'ed on first access.
     */
    private static MBeanServerLocator locator;

    //--------------------------------------------------------------------------
    //
    // Public Static Methods
    //
    //--------------------------------------------------------------------------
    
    /**
     * Returns a <code>MBeanServerLocator</code> that exposes the <code>MBeanServer</code> to register MBeans with.
     * 
     * @return The <code>MBeanServerLocator</code> that exposes the <code>MBeanServer</code> to register MBeans with.
     */
    public static synchronized MBeanServerLocator getMBeanServerLocator()
    {
        if (locator == null)
        {
            // Try app-server specific locators.
            // WebSphere provides access to its MBeanServer via a custom admin API.
            // instantiateLocator("flex.management.WebSphereMBeanServerLocator", new String[] {"com.ibm.websphere.management.AdminServiceFactory"});

            // Sun JRE 1.5 based implementation
            if (locator == null)
                locator =  new PlatformMBeanServerLocator();
            
            if (Log.isDebug())
                Log.getLogger(LogCategories.MANAGEMENT_GENERAL).debug("Using MBeanServerLocator: " + locator.getClass().getName());
        }
        return locator;
    }
    
    /**
     * Release static MBeanServerLocator
     * Called on MessageBroker shutdown.
     */
    public static void clear()
    {
        locator = null;
    }
    
    //--------------------------------------------------------------------------
    //
    // Private Static Methods
    //
    //--------------------------------------------------------------------------
    
    /**
     * Helper method that attempts to load a specific MBeanServerLocator.
     * 
     * @param locatorClassName The classname of the desired MBeanServerLocator.
     * @param dependencyClassNames Any additional dependent classnames that the desired locator depends upon
     *                            that should also be tested for availability.
     */
    private static void instantiateLocator(String locatorClassName, String[] dependencyClassNames)
    {
        try
        {
            if (dependencyClassNames != null)
            {
                for (int i = 0; i < dependencyClassNames.length; i++)
                    ClassUtil.createClass(dependencyClassNames[i]);
            }
            
            Class locatorClass = ClassUtil.createClass(locatorClassName);
            locator = (MBeanServerLocator)locatorClass.newInstance();
        }
        catch (Throwable t)
        {
            if (Log.isDebug())
                Log.getLogger(LogCategories.MANAGEMENT_MBEANSERVER).debug("Not using MBeanServerLocator: " + locatorClassName + ". Reason: " + t.getMessage());
        }
    }
    
    //--------------------------------------------------------------------------
    //
    // Constructor
    //
    //--------------------------------------------------------------------------
    
    /**
     * Direct instantiation is not allowed.
     * Use <code>getMBeanServerLocator()</code> to obtain a <code>MBeanServerLocator</code> 
     * instance to lookup the proper MBean server to use.
     */
    private MBeanServerLocatorFactory() {}
    
}