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
package flex.messaging.io;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;

/**
 * Proxies serialization of a Dictionary and considers all keys as String based property 
 * names. Additionally, bean properties from the instance are also included and override 
 * any Dictionary entries with the same name.
 *  
 * @author Peter Farland
 */
public class DictionaryProxy extends BeanProxy
{
    static final long serialVersionUID = 1501461889185692712L;

    public DictionaryProxy()
    {
        super();
        //dynamic = true;
    }

    public DictionaryProxy(Dictionary defaultInstance)
    {
        super(defaultInstance);
    }

    public List getPropertyNames(Object instance)
    {
        if (instance == null)
            return null;

        List propertyNames = null;
        List excludes = null;

        if (descriptor != null)
        {
            excludes = descriptor.getExcludesForInstance(instance);
            if (excludes == null)
                excludes = descriptor.getExcludes();
        }

        // Add all Dictionary keys as properties
        if (instance instanceof Dictionary)
        {
            Dictionary dictionary = (Dictionary)instance;

            propertyNames = new ArrayList(dictionary.size());

            Enumeration keys = dictionary.keys();
            while (keys.hasMoreElements())
            {
                Object key = keys.nextElement();
                if (key != null)
                {
                    if (excludes != null && excludes.contains(key))
                        continue;

                    propertyNames.add(key.toString());
                }
            }
        }

        // Then, check for bean properties
        List beanProperties = super.getPropertyNames(instance);
        if (propertyNames == null)
        {
            propertyNames = beanProperties;
        }
        else
        {
            propertyNames.addAll(beanProperties);
        }

        return propertyNames;
    }

    public Object getValue(Object instance, String propertyName)
    {
        if (instance == null || propertyName == null)
            return null;

        // First, check for bean property
        Object value = super.getValue(instance, propertyName);

        // Then check for Dictionary entry
        if (value == null && instance instanceof Dictionary)
        {
            Dictionary dictionary = (Dictionary)instance;
            value = dictionary.get(propertyName);
        }

        return value; 
    }
}
