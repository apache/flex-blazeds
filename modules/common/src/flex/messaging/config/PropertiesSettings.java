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

import java.util.List;

/**
 * Holds any child element of the properties section of the services configuration.
 * <p>
 * If a property is a simple element with a text value then it is stored as a String
 * using the element name as the property name. If the same element appears again then
 * the element is converted to a List of values and further occurences are simply added
 * to the List.
 * </p>
 * <p>
 * If a property element has child elements the children are recursively processed
 * and added as a Map.
 * </p>
 *
 * @author Peter Farland
 * @exclude
 */
public abstract class PropertiesSettings
{
    protected final ConfigMap properties;

    public PropertiesSettings()
    {
        properties = new ConfigMap();
    }

    public final void addProperties(ConfigMap p)
    {
        properties.addProperties(p);
    }

    public ConfigMap getProperties()
    {
        return properties;
    }

    public final String getProperty(String name)
    {
        return getPropertyAsString(name, null);
    }

    public final void addProperty(String name, String value)
    {
        properties.addProperty(name, value);
    }

    public final void addProperty(String name, ConfigMap value)
    {
        properties.addProperty(name, value);
    }

    public final ConfigMap getPropertyAsMap(String name, ConfigMap defaultValue)
    {
        return properties.getPropertyAsMap(name, defaultValue);
    }

    public final String getPropertyAsString(String name, String defaultValue)
    {
        return properties.getPropertyAsString(name, defaultValue);
    }

    public final List getPropertyAsList(String name, List defaultValue)
    {
        return properties.getPropertyAsList(name, defaultValue);
    }

    public final int getPropertyAsInt(String name, int defaultValue)
    {
        return properties.getPropertyAsInt(name, defaultValue);
    }

    public final boolean getPropertyAsBoolean(String name, boolean defaultValue)
    {
        return properties.getPropertyAsBoolean(name, defaultValue);
    }

    public final long getPropertyAsLong(String name, long defaultValue)
    {
        return properties.getPropertyAsLong(name, defaultValue);
    }
}
