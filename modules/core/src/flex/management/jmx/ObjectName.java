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
package flex.management.jmx;

import java.util.Hashtable;
import javax.management.MalformedObjectNameException;

/**
 * Remotable ObjectName representation that complies with Flash serialization requirements. 
 * This class is JMX 1.1 compliant.
 */
public class ObjectName
{
    /**
     * String representation of the list of key properties sorted in lexical order.
     */
    public String canonicalKeyPropertyListString;
    
    /**
     * Canonical form of the name with properties sorted in lexical order.
     */
    public String canonicalName;
    
    /**
     * The domain part of the object name.
     */
    public String domain;
    
    /**
     * A Hashtable containing key-property pairs.
     */
    public Hashtable keyPropertyList;
    
    /**
     * String representation of the key properties. 
     */
    public String keyPropertyListString;    
    
    /**
     * Indicates whether the object name is a pattern.
     */
    public boolean pattern;
    
    /**
     * Indicates whether the object name is a pattern on key properties.
     */
    public boolean propertyPattern;
    
    /**
     * Constructs an empty <code>ObjectName</code> instance.
     */
    public ObjectName()
    {}
    
    /**
     * Constructs a <code>ObjectName</code> instance based upon a
     * <code>javax.management.ObjectName</code> instance.
     * 
     * @param objectName The JMX <code>ObjectName</code> instance to base this instance on.
     */
    public ObjectName(javax.management.ObjectName objectName)
    {
        canonicalKeyPropertyListString = objectName.getCanonicalKeyPropertyListString();
        canonicalName = objectName.getCanonicalName();
        domain = objectName.getDomain();
        keyPropertyList = objectName.getKeyPropertyList();
        keyPropertyListString = objectName.getKeyPropertyListString();
        pattern = objectName.isPattern();        
        propertyPattern = objectName.isPropertyPattern();
    }
    
    /**
     * Utility method to convert this <code>ObjectName</code> to a
     * <code>javax.management.ObjectName</code> instance.
     * 
     * @return A JMX <code>ObjectName</code> based upon this instance.
     */
    public javax.management.ObjectName toObjectName() throws MalformedObjectNameException
    {
        return new javax.management.ObjectName(domain, keyPropertyList);
    }

}
