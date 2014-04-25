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

/**
 * Settings class for message filters.
 * 
 * @exclude
 */
public class MessageFilterSettings extends PropertiesSettings
{
    /**
     * Filters belong to one of two types; those that filter messages
     * asynchronously and those that filter messages synchronously.
     */
    public enum FilterType { ASYNC, SYNC };
    
    private String id;

    /**
     * Returns the id.
     *
     * @return The id.
     */
    public String getId()
    {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param value The id.
     */
    public void setId(String value)
    {
        id = value;
    }    
    
    private String className;

    /**
     * Returns the class name.
     *
     * @return The class name.
     */
    public String getClassName()
    {
        return className;
    }

    /**
     * Sets the class name.
     *
     * @param value The class name.
     */
    public void setClassName(String value)
    {
        className = value;
    }
    
    private FilterType filterType;
    
    /**
     * Returns the filter type.
     * @see FilterType
     * 
     * @return The filter type.
     */
    public FilterType getFilterType()
    {
        return filterType;
    }
    
    /**
     * Sets the filter type.
     * @see FilterType
     * 
     * @param value The filter type.
     */
    public void setFilterType(FilterType value)
    {
        filterType = value;
    }
}
