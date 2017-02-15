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

import flex.messaging.MessageException;
import flex.messaging.log.Log;
import flex.messaging.log.Logger;
import flex.messaging.util.ClassUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Proxies serialization of a Map and considers all keys as String based property
 * names. Additionally, bean properties from the instance are also included and
 * override any Map entries with the same key name.
 */
public class MapProxy extends BeanProxy
{
    static final long serialVersionUID = 7857999941099335210L;

    private static final int NULL_KEY_ERROR = 10026;

    /**
     * Constructor
     */
    public MapProxy()
    {
        super();
        //dynamic = true;
    }

    /**
     * Construct with a default instance type.
     * @param defaultInstance defines the alias if provided
     */
    public MapProxy(Object defaultInstance)
    {
        super(defaultInstance);
        //dynamic = true;
    }

    /** {@inheritDoc} */
    @Override
    public List getPropertyNames(Object instance)
    {
        if (instance == null)
            return null;

        List propertyNames = null;
        List excludes = null;

        if (descriptor != null)
        {
            excludes = descriptor.getExcludesForInstance(instance);
            if (excludes == null) // For compatibility with older implementations
                excludes = descriptor.getExcludes();
        }

        // Add all Map keys as properties
        if (instance instanceof Map)
        {
            Map map = (Map)instance;

            if (map.size() > 0)
            {
                propertyNames = new ArrayList(map.size());
                SerializationContext context = getSerializationContext();

                Iterator it = map.keySet().iterator();
                while (it.hasNext())
                {
                    Object key = it.next();
                    if (key != null)
                    {
                        if (excludes != null && excludes.contains(key))
                            continue;

                        propertyNames.add(key.toString());
                    }
                    else
                    {
                        // Log null key errors
                        if (Log.isWarn() && context.logPropertyErrors)
                        {
                            Logger log = Log.getLogger(LOG_CATEGORY);
                            log.warn("Cannot send a null Map key for type {0}.",
                                    new Object[] {map.getClass().getName()});
                        }

                        if (!context.ignorePropertyErrors)
                        {
                            // Cannot send a null Map key for type {0}.
                            MessageException ex = new MessageException();
                            ex.setMessage(NULL_KEY_ERROR, new Object[] {map.getClass().getName()});
                            throw ex;
                        }
                    }
                }
            }
        }

        // Then, check for bean properties
        List beanProperties = super.getPropertyNames(instance);
        if (beanProperties != null)
        {
            if (propertyNames == null)
                propertyNames = beanProperties;
            else
                propertyNames.addAll(beanProperties);
        }

        return propertyNames;
    }

    /** {@inheritDoc} */
    @Override
    public Object getValue(Object instance, String propertyName)
    {
        if (instance == null || propertyName == null)
            return null;

        Object value = null;

        // First, check for bean property
        BeanProperty bp = getBeanProperty(instance, propertyName);
        if (bp != null)
            value = super.getBeanValue(instance, bp);

        // Then check for Map entry
        if (value == null && instance instanceof Map)
            value = getMapValue((Map)instance, propertyName);

        return value;
    }

    /** {@inheritDoc} */
    @Override
    public void setValue(Object instance, String propertyName, Object value)
    {
        if (instance == null || propertyName == null)
            return;

        Map props = getBeanProperties(instance);
        if (props.containsKey(propertyName))
        {
            super.setValue(instance, propertyName, value);
        }
        else if (instance instanceof Map)
        {
            ClassUtil.validateAssignment(instance, propertyName, value);
            ((Map)instance).put(propertyName, value);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected boolean ignorePropertyErrors(SerializationContext context)
    {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    protected boolean logPropertyErrors(SerializationContext context)
    {
        return false;
    }

    /**
     * Return the classname of the instance, including ASObject types.
     * If the instance is a Map and is in the java.util package, we return null.
     * @param instance the object to find the class name of
     * @return the class name of the object.
     */
    @Override
    protected String getClassName(Object instance)
    {
        return (instance != null && instance instanceof Map
                && instance.getClass().getName().startsWith("java.util."))?
                        null : super.getClassName(instance);
    }

    /**
     * Given a map and a property name, returns the value keyed under that property
     * name but instead of depending on {@link Map#get(Object)}, propertyName
     * is compared against key#toString. This is due to the fact that propertyNames
     * are always stored as Strings.
     *
     * @param map The Map to check against.
     * @param propertyName The property name to check for.
     * @return The value keyed under property name or null if it does not exist.
     */
    protected Object getMapValue(Map map, String propertyName)
    {
        for (Object entry : map.entrySet())
        {
            Object key = ((Map.Entry) entry).getKey();
            if (key.toString().equals(propertyName))
                return ((Map.Entry) entry).getValue();
        }
        return null;
    }
}
