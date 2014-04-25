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
package flex.messaging.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The ConfigMap class is a helper implementation of Map that makes it easier
 * to handle properties that can appear one or more times. If a property is set
 * more than once, it is converted to a List and added as another property
 * rather than replacing the existing property. It also provides utility APIs
 * for getting properties from the Map, cast to a certain type and allows a
 * default to be specified in the event that the property is missing.
 *
 * @author Peter Farland
 */
public class ConfigMap extends LinkedHashMap
{
    /**
     * This number was generated using the 'serialver' command line tool.
     * This number should remain consistent with the version used by
     * ColdFusion to communicate with the message broker over RMI.
     */
    private static final long serialVersionUID = 8913604659150919550L;

    /**
     * This error is thrown when a property unexpectedly contains multiple values.
     */
    private static final int UNEXPECTED_MULTIPLE_VALUES = 10169;

    /**
     * An *undocumented* system property can be used to revert to legacy config property handling:
     *   Legacy behavior - config property values were not trimmed, retaining leading/trailing whitespace which
     *                     proved problematic for customers.
     *   New default behavior - config property values are trimmed.
     */
    private static final String SYSPROPNAME_TRIM_CONFIG_PROPERTY_VALUES = "flex.trim-config-property-values";    
    private static final boolean TRIM_CONFIG_PROPERTY_VALUES = Boolean.valueOf(System.getProperty(SYSPROPNAME_TRIM_CONFIG_PROPERTY_VALUES, "true")).booleanValue();
    
    /**
     * Map to keep track of accessed properties.
     */
    private HashSet accessedKeys = new HashSet();

    /**
     * Constructs an empty <code>ConfigMap</code> with the default initial
     * capacity of 10.
     */
    public ConfigMap()
    {
        super();
    }

    /**
     * Constructs a new <code>ConfigMap</code> with the initial
     * capacity specified.
     *
     * @param  initialCapacity the initial capacity.
     */
    public ConfigMap(int initialCapacity)
    {
        super(initialCapacity);
    }

    /**
     * Constructs a new <code>ConfigMap</code> and copies the values
     * from the supplied map to this map.
     *
     * @param m a <code>ConfigMap</code> whose properties are to be added to
     * this <code>ConfigMap</code>.
     */
    public ConfigMap(ConfigMap m)
    {
        this();
        addProperties(m);
    }

    /**
     * Adds all properties from a map to this map.
     *
     * @param p a <code>ConfigMap</code> whose properties are to be added to
     * this <code>ConfigMap</code>.
     */
    public void addProperties(ConfigMap p)
    {
        Iterator it = p.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry entry = (Map.Entry) it.next();
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof ValueList)
            {
                addProperties(key, (ValueList) value);
            }
            else
            {
                addPropertyLogic(key, value);
            }
        }
    }

    /**
     * Helper method to add a list of values under a key.
     *
     * @param key The key to add the values under.
     * @param values The list of values to add.
     */
    private void addProperties(Object key, ValueList values)
    {
        ValueList list = getValueList(key);
        if (list == null)
        {
            put(key, values.clone());
        }
        else
        {
            list.addAll(values);
        }
    }

    /**
     * Helper method to add a value under a key.
     *
     * @param key The key to add the value under.
     * @param value The value to add.
     */
    private void addPropertyLogic(Object key, Object value)
    {
        ValueList list = getValueList(key);
        if (list == null)
        {
            put(key, value);
        }
        else
        {
            list.add(value);
        }
    }

    private static class ValueList extends ArrayList
    {
        /**
         * Serial version id.
         */
        static final long serialVersionUID = -5637755312744414675L;
    }

    /**
     * Given a key, returns a list of values associated with that key.
     *
     * @param key The key.
     * @return A list of values associated with the key.
     */
    private ValueList getValueList(Object key)
    {
        ValueList list;
        Object old = super.get(key);
        if (old instanceof ValueList)
        {
            list = (ValueList) old;
        }
        else if (old != null)
        {
            list = new ValueList();
            list.add(old);
            put(key, list);
        }
        else
        {
            list = null;
        }
        return list;
    }

    /**
     * Adds a <code>String</code> value to this map for the given property
     * name.
     *
     * @param name  the property name
     * @param value the property value
     */
    public void addProperty(String name, String value)
    {
        addPropertyLogic(name, TRIM_CONFIG_PROPERTY_VALUES && value != null ? value.trim() : value);
    }

    /**
     * Adds a <code>ConfigMap</code> value to this map for the given property
     * name.
     *
     * @param name  the property name
     * @param value the property value
     */
    public void addProperty(String name, ConfigMap value)
    {
        addPropertyLogic(name, value);
    }

    /**
     * Gets the set of property names contained in this map.
     *
     * @return a <code>Set</code> of property name <code>String</code>s.
     */
    public Set propertyNames()
    {
        return keySet();
    }

    /**
     * Sets a property name as allowed without needing to access the property
     * value. This marks a property as allowed for validation purposes.
     *
     * @param name  the property name to allow
     */
    public void allowProperty(String name)
    {
        accessedKeys.add(name);
    }

    /**
     * Gets the value for the given property name. Also records that this
     * property was accessed.
     *
     * @param name  the property name
     * @return the value for the property, or null if property does not exist
     * in this map.
     */
    public Object get(Object name)
    {
        accessedKeys.add(name);
        return super.get(name);
    }

    /**
     * Helper method to get the property with the specified name as a string if possible.
     *
     * @param name The property name.
     * @return The property object.
     */
    private Object getSinglePropertyOrFail(Object name)
    {
        Object result = get(name);
        if (result instanceof ValueList)
        {
            ConfigurationException exception = new ConfigurationException();
            exception.setMessage
                (UNEXPECTED_MULTIPLE_VALUES, new Object[] {name});
            throw exception;
        }
        return result;
    }

    /**
     * Gets the property with the specified name as a string if possible.
     *
     * @param name The property name.
     * @return The property name.
     */
    public String getProperty(String name)
    {
        return getPropertyAsString(name, null);
    }

    /**
     * Gets the property with the specified name as a ConfigMap if possible,
     * or returns the default value if the property is undefined.
     * <p/>
     * @param name the property name
     * @param defaultValue the default value
     * @return ConfigMap the ConfigMap object of the property
     */
    public ConfigMap getPropertyAsMap(String name, ConfigMap defaultValue)
    {
        Object prop = getSinglePropertyOrFail(name);
        if (prop instanceof ConfigMap)
        {
            return (ConfigMap)prop;
        }
        return defaultValue;
    }

    /**
     * Gets the property with the specified name as a String if possible,
     * or returns the default value if the property is undefined.
     * <p/>
     * @param name the property name
     * @param defaultValue the default value
     * @return String the String value of the property
     */
    public String getPropertyAsString(String name, String defaultValue)
    {
        Object prop = getSinglePropertyOrFail(name);
        if (prop instanceof String)
        {
            return (String)prop;
        }
        return defaultValue;
    }

    /**
     * Gets a property (or set of properties) as a List. If only one
     * property exists it is added as the only entry to a new List.
     *
     * @param name  the property name
     * @param defaultValue  the value to return if the property is not found
     * @return the value for the property as a List if it exists in this map,
     * otherwise the defaultValue is returned.
     */
    public List getPropertyAsList(String name, List defaultValue)
    {
        Object prop = get(name);
        if (prop != null)
        {
            if (prop instanceof List)
            {
                return (List) prop;
            }
            else
            {
                List list = new ArrayList();
                list.add(prop);
                return list;
            }
        }
        return defaultValue;
    }

    /**
     * Gets the property with the specified name as a boolean if possible,
     * or returns the default value if the property is undefined.
     * <p/>
     * @param name the property name
     * @param defaultValue the default value
     * @return boolean the boolean value of the property
     */
    public boolean getPropertyAsBoolean(String name, boolean defaultValue)
    {
        Object prop = getSinglePropertyOrFail(name);
        if (prop instanceof String)
        {
            return Boolean.valueOf((String)prop).booleanValue();
        }
        return defaultValue;
    }

    /**
     * Gets the property with the specified name as an int if possible,
     * or returns the default value if the property is undefined.
     * <p/>
     * @param name the property name
     * @param defaultValue the default value
     * @return int the int value of the property
     */
    public int getPropertyAsInt(String name, int defaultValue)
    {
        Object prop = getSinglePropertyOrFail(name);
        if (prop instanceof String)
        {
            try
            {
                return Integer.parseInt((String)prop);
            }
            catch (NumberFormatException ex)
            {
            }
        }
        return defaultValue;
    }

    /**
     * Gets the property with the specified name as a long if possible,
     * or returns the default value if the property is undefined.
     * <p/>
     * @param name the property name
     * @param defaultValue the default value
     * @return long the long value of the property
     */
    public long getPropertyAsLong(String name, long defaultValue)
    {
        Object prop = getSinglePropertyOrFail(name);
        if (prop instanceof String)
        {
            try
            {
                return Long.parseLong((String)prop);
            }
            catch (NumberFormatException ex)
            {
            }
        }
        return defaultValue;
    }

    /**
     * Returns a list of qualified property names that have not been accessed
     * by one of the get*() methods.
     * <p/>
     * @return List a list of unused properties
     */
    public List findAllUnusedProperties()
    {
        List result = new ArrayList();
        findUnusedProperties("", true, result);
        return result;
    }

    /**
     * Gathers a collection of properties that exist in the map but have not
     * been explicitly accessed nor marked as allowed. This list is helpful
     * in validating a set of properties as one can detect those that are
     * unknown or unexpected.
     *
     * @param parentPath Used to track the depth of property in a potential
     * hierarchy of <code>ConfigMap</code>s.
     * @param recurse Whether sub maps should be recursively searched.
     * @param result the collection of unused properties in this map.
     */
    public void findUnusedProperties
        (String parentPath, boolean recurse, Collection result)
    {
        Iterator itr = entrySet().iterator();
        while (itr.hasNext())
        {
            Map.Entry entry = (Map.Entry) itr.next();
            Object key = entry.getKey();
            String currentPath = parentPath + '/' + String.valueOf(key);
            if (!accessedKeys.contains(key))
            {
                result.add(currentPath);
            }
            else if (recurse)
            {
                Object value = entry.getValue();
                List values = value instanceof List ?
                              (List) value : Collections.singletonList(value);
                for (int i = 0; i < values.size(); i++)
                {
                    Object child = values.get(i);
                    if (child instanceof ConfigMap)
                    {
                        ((ConfigMap) child).findUnusedProperties
                            (currentPath, recurse, result);
                    }
                }
            }
        }
    }
}
