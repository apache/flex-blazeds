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
package flex.messaging.io.amf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * AVM+ Serialization optimizes object serialization by
 * serializing the traits of a type once, and then
 * sending only the values of each instance of the type
 * as it occurs in the stream.
 *
 * @exclude
 */
public class TraitsInfo
{
    private final String className;
    private final boolean dynamic;
    private final boolean externalizable;
    private List<String> properties;

    public TraitsInfo(String className)
    {
        this(className, false, false, 10);
    }

    public TraitsInfo(String className, int initialCount)
    {
        this(className, false, false, initialCount);
    }

    public TraitsInfo(String className, boolean dynamic, boolean externalizable, int initialCount)
    {
        this(className, dynamic, externalizable, new ArrayList<String>(initialCount));
    }
    
    public TraitsInfo(String className, boolean dynamic, boolean externalizable, List<String> properties)
    {
        if (className == null)
            className = "";

        this.className = className;
        this.properties = properties;
        this.dynamic = dynamic;
        this.externalizable = externalizable;
    }

    public boolean isDynamic()
    {
        return dynamic;
    }

    public boolean isExternalizable()
    {
        return externalizable;
    }

    public int length()
    {
        return properties != null? properties.size() : 0;
    }

    public String getClassName()
    {
        return className;
    }

    public void addProperty(String name)
    {
        if (properties == null)
            properties = new ArrayList<String>();
        properties.add(name);
    }
    
    public void addAllProperties(Collection props)
    {
        if (properties == null)
            properties = new ArrayList<String>();
        properties.addAll(props);
    }

    public String getProperty(int i)
    {
        return properties != null? properties.get(i) : null;
    }
    
    public List<String> getProperties()
    {
        return properties;
    }

    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }

        if (obj instanceof TraitsInfo)
        {
            TraitsInfo other = (TraitsInfo)obj;

            if (!this.className.equals(other.className))
            {
                return false;
            }

            if (!(this.dynamic == other.dynamic))
            {
                return false;
            }

            List thisProperties = this.properties;
            List otherProperties = other.properties;
            
            if (thisProperties != otherProperties)
            {
                int thisCount = thisProperties.size();

                if (thisCount != otherProperties.size())
                {
                    return false;
                }

                for (int i = 0; i < thisCount; i++)
                {
                    Object thisProp = thisProperties.get(i);
                    Object otherProp = otherProperties.get(i);
                    if (thisProp != null && otherProp != null && !thisProp.equals(otherProp))
                        return false;
                }
            }

            return true;
        }

        return false;
    }

    /**
     * Instances of types with the same classname and number of properties may
     * return the same hash code, however, an equality test will fully
     * test whether they match exactly on individual property names.
     * @return int the hash code of the TraitsInfo object
     */
    public int hashCode()
    {
        int size = properties != null? properties.size() : 0;
        int c = className.hashCode();
        c = dynamic ? c << 2 : c << 1;
        c = c | (size << 24);
        return c;
    }
}
