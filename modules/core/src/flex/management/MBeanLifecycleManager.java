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

import flex.messaging.MessageBroker;

import java.util.Iterator;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * Helper class for managing MBean lifecycles externally from the core server
 * components where necessary.
 * 
 * @author shodgson
 */
public class MBeanLifecycleManager
{
    /**
     * Unregisters all runtime MBeans that are registered in the same domain as the 
     * MessageBrokerControl for the target MessageBroker. 
     *  
     * @param broker The MessageBroker component that has been stopped.
     */
    public static void unregisterRuntimeMBeans(MessageBroker broker)
    {        
        MBeanServer server = MBeanServerLocatorFactory.getMBeanServerLocator().getMBeanServer();
        ObjectName brokerMBean = broker.getControl().getObjectName();
        String domain = brokerMBean.getDomain();
        try
        {
            ObjectName pattern = new ObjectName(domain + ":*");
            Set names = server.queryNames(pattern, null);
            Iterator iter = names.iterator();
            while (iter.hasNext())
            {
                ObjectName on = (ObjectName)iter.next();
                server.unregisterMBean(on);
            }
        }
        catch (Exception e)
        {
            // We're generally unregistering these during shutdown (possibly JVM shutdown)
            // so there's nothing to really do here because we aren't guaranteed access to
            // resources like system log files, localized messaging, etc.
        }
    }
    
}
