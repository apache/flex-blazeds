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
package flex.management.runtime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import flex.management.BaseControl;

/**
 * @exclude
 */
public class AdminConsoleDisplayRegistrar extends BaseControl implements AdminConsoleDisplayRegistrarMBean
{
    public static final String ID = "AdminConsoleDisplay";
    
    private HashMap registeredExposedObjects;
    
    public AdminConsoleDisplayRegistrar(BaseControl parent)
    {
        super(parent);
        registeredExposedObjects = new HashMap();
        register();
    }

    public void registerObject(int type, String beanName, String propertyName)
    {
        Object objects = registeredExposedObjects.get(new Integer(type));
        if (objects != null)
        {
            ((ArrayList)objects).add(beanName + ":" + propertyName);
        }
        else
        {
            if (type < 1)
                return;
            
            objects = new ArrayList();
            ((ArrayList)objects).add(beanName + ":" + propertyName);
            registeredExposedObjects.put(new Integer(type), objects);
        }
    }
    
    public void registerObjects(int type, String beanName, String[] propertyNames)
    {
        for (int i = 0; i < propertyNames.length; i++)
        {
            registerObject(type, beanName, propertyNames[i]);
        }
    }
    
    public void registerObjects(int[] types, String beanName, String[] propertyNames)
    {
        for (int j = 0; j < types.length; j++)
        {
            registerObjects(types[j], beanName, propertyNames);
        }
    }
    
    public Integer[] getSupportedTypes() throws IOException
    {
        Object[] array = registeredExposedObjects.keySet().toArray();
        Integer[] types = new Integer[array.length];
        for (int i = 0; i < array.length; i++)
        {
            types[i] = (Integer)array[i];
        }
        return types;
    }

    public String[] listForType(int type) throws IOException
    {
        Object list = registeredExposedObjects.get(new Integer(type));
        
        return (list != null) ? (String[]) ((ArrayList)list).toArray(new String[0]) : new String[0];
    }

    public String getId()
    {
        return ID;
    }

    public String getType()
    {
        return ID;
    }


}
