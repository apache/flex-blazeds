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
package flex.messaging.validators;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import flex.messaging.config.ConfigMap;

/**
 * The <code>ClassDeserializationValidator</code> is provided as a default
 * implementation of <code>DeserializationValidator</code> and it simply
 * validates the creation of allowed and disallowed classes as specified in
 * the configuration.
 */
public class ClassDeserializationValidator implements DeserializationValidator
{
    //--------------------------------------------------------------------------
    //
    // Public Static Constants
    //
    //--------------------------------------------------------------------------

    public static final String PROPERTY_ALLOW_CLASSES = "allow-classes";
    public static final String PROPERTY_DISALLOW_CLASSES = "disallow-classes";
    public static final String PROPERTY_CLASS_ATTR = "class";
    public static final String PROPERTY_NAME_ATTR = "name";
    
    private static final String [] DEFAULT_ALLOW_CLASSES = {
        "flex.messaging.*",  
        "flex.data.*",  
        "java.lang.Boolean",  
        "java.lang.Byte",  
        "java.lang.Character",  
        "java.lang.Double",  
        "java.lang.Float",  
        "java.lang.Integer",  
        "java.lang.Long",  
        "java.lang.Object",  
        "java.lang.Short",  
        "java.lang.String",  
        "java.io.Externalizable",  
        "java.util.*",
        "org.w3c.dom.Document",
        "\\[B",  
        "\\[Ljava.lang.Object;"  
    };

    //--------------------------------------------------------------------------
    //
    // Variables
    //
    //--------------------------------------------------------------------------

    /**
     * Instance level lock for thread-safe state changes.
     */
    protected final Object lock = new Object();

    /**
     * Used to keep track of encountered allow and disallow classes for faster lookup.
     */
    private Set<String> allowClasses;
    private Set<String> disallowClasses;

    /**
     * Used to keep track of allow and disallow class patterns.
     */
    private Map<String, Pattern> allowClassPatterns;
    private Map<String, Pattern> disallowClassPatterns;

    //--------------------------------------------------------------------------
    //
    // Public Methods
    //
    //--------------------------------------------------------------------------

    /**
     * Adds a class pattern that should be allowed to be created.
     *
     * @param classNamePattern The name of the class which can be a regular expression.
     */
    public void addAllowClassPattern(String classNamePattern)
    {
        synchronized (lock)
        {
            if (allowClassPatterns == null)
                allowClassPatterns = new HashMap<String, Pattern>();

            allowClassPatterns.put(classNamePattern, Pattern.compile(classNamePattern));

            clearClassCache();
        }
    }

    /**
     * Removes a class pattern that should be allowed to be created..
     *
     * @param classNamePattern The name of the class which can be a regular expression.
     */
    public void removeAllowClassPattern(String classNamePattern)
    {
        synchronized (lock)
        {
            if (allowClassPatterns != null)
                allowClassPatterns.remove(classNamePattern);

            clearClassCache();
        }
    }

    /**
     * Adds a class pattern that should be disallowed to be created.
     *
     * @param classNamePattern The name of the class which can be a regular expression.
     */
    public void addDisallowClassPattern(String classNamePattern)
    {
        synchronized (lock)
        {
            if (disallowClassPatterns == null)
                disallowClassPatterns = new HashMap<String, Pattern>();

            disallowClassPatterns.put(classNamePattern, Pattern.compile(classNamePattern));

            clearClassCache();
        }
    }

    /**
     * Removes a class pattern that should be disallowed to be created..
     *
     * @param classNamePattern The name of the class which can be a regular expression.
     */
    public void removeDisallowClassPattern(String classNamePattern)
    {
        synchronized (lock)
        {
            if (disallowClassPatterns != null)
                disallowClassPatterns.remove(classNamePattern);

            clearClassCache();
        }
    }

    /**
     * This method is meant to validate the assignment of a value to an index of
     * an Array or List instance but this class only deals with class creations,
     * therefore this method always returns true.
     *
     * @param instance The Array or List instance.
     * @param index The index at which the value is being assigned.
     * @param value The value that is assigned to the index.
     * @return True.
     */
    public boolean validateAssignment(Object instance, int index, Object value)
    {
        return true;
    }

    /**
     * This method is meant to validate the assignment of a property of an instance
     * to a value but this class only deals with class creations, therefore this
     * method always returns true.
     *
     * @param instance The instance with the property that is being assigned a new value.
     * @param propertyName The name of the property that is being assigned.
     * @param value The value that the property is being assigned to.
     * @return True.
     */
    public boolean validateAssignment(Object instance, String propertyName, Object value)
    {
        return true;
    }

    /**
     * Validates the creation of a class as indicated by the allow and disallow
     * classes list. If the class is in both allow and disallow classes,
     * disallow classes list take precedence.
     *
     * @param c The class that is being created.
     * @return True if the creation is valid.
     */
    public boolean validateCreation(Class<?> c)
    {
        String className = c == null? null : c.getName();
        if (className == null)
            return true;

        // First, check against the encountered disallow-classes list.
        if (disallowClasses != null && disallowClasses.contains(className))
            return false;

        // Then, check against the encountered allow-classes list.
        if (allowClasses != null && allowClasses.contains(className))
            return true;

        // Otherwise, the class was encountered for the first time, need to
        // go through the disallow and allow class patterns list.

        // Disallow the class if there's a disallow-classes list, and the class is in that list.
        if (disallowClassPatterns != null && !disallowClassPatterns.isEmpty())
        {
            for (Pattern pattern : disallowClassPatterns.values())
            {
                if (pattern.matcher(className).matches())
                {
                    addDisallowClass(className);
                    return false;
                }
            }
        }

        // Disallow the class if there's an allowed-classes list, and the class is NOT in that list.
        if (allowClassPatterns != null && !allowClassPatterns.isEmpty())
        {
            for (Pattern pattern : allowClassPatterns.values())
            {
                if (pattern.matcher(className).matches())
                {
                    addAllowClass(className);
                    return true;
                }
            }
            // Disallow the class as it's not in allow-classes list.
            addDisallowClass(className);
            return false;
        }

        // Otherwise allow the class.
        addAllowClass(className);
        return true;
    }

    /** {@inheritDoc} */
    public void initialize(String id, ConfigMap properties)
    {
        // Apply default allow classes
        for (String defaultAllowClassPattern : DEFAULT_ALLOW_CLASSES)
        {
            addAllowClassPattern(defaultAllowClassPattern);
        }
        
        if (properties == null || properties.size() == 0)
            return;

        // Process allow-classes.
        ConfigMap allowedClassesMap = properties.getPropertyAsMap(PROPERTY_ALLOW_CLASSES, null);
        if (allowedClassesMap != null && !allowedClassesMap.isEmpty())
        {
            List<?> names = allowedClassesMap.getPropertyAsList(PROPERTY_CLASS_ATTR, null);
            if (names != null && !names.isEmpty())
            {
                for (Object element : names)
                {
                    String name = ((ConfigMap)element).getProperty(PROPERTY_NAME_ATTR);
                    addAllowClassPattern(name);
                }
            }
        }

        // Process disallow-classes.
        ConfigMap disallowedClassesMap = properties.getPropertyAsMap(PROPERTY_DISALLOW_CLASSES, null);
        if (disallowedClassesMap != null && !disallowedClassesMap.isEmpty())
        {
            List<?> names = disallowedClassesMap.getPropertyAsList(PROPERTY_CLASS_ATTR, null);
            if (names != null && !names.isEmpty())
            {
                for (Object element : names)
                {
                    String name = ((ConfigMap)element).getProperty(PROPERTY_NAME_ATTR);
                    addDisallowClassPattern(name);
                }
            }
        }
    }

    //--------------------------------------------------------------------------
    //
    // Protected Methods
    //
    //--------------------------------------------------------------------------

    protected void addAllowClass(String className)
    {
        synchronized (lock)
        {
            if (allowClasses == null)
                allowClasses = new HashSet<String>();

            if (!allowClasses.contains(className))
                allowClasses.add(className);
        }
    }

    protected void addDisallowClass(String className)
    {
        synchronized (lock)
        {
            if (disallowClasses == null)
                disallowClasses = new HashSet<String>();

            if (!disallowClasses.contains(className))
                disallowClasses.add(className);
        }
    }
    
    protected void clearClassCache()
    {
        if (allowClasses != null)
        {
            allowClasses.clear();
        }
        if (disallowClasses != null)
        {
            disallowClasses.clear();
        }
    }
}