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

/**
 * Remotable <code>Attribute</code> class that complies with Flash serialization requirements.
 *
 * @author shodgson
 */
public class Attribute
{
    /**
     * The name of the attribute.
     */
    public String name;
    
    /**
     * The value of the attribute.
     */
    public Object value;
    
    /**
     * Constructs an empty <code>Attribute</code> instance.
     *
     */
    public Attribute()
    {}
    
    /**
     * Constructs an <code>Attribute</code> instance based upon a <code>javax.management.Attribute</code> instance.
     * 
     * @param attribute The JMX <code>Attribute</code> to base this instance on.
     */
    public Attribute(javax.management.Attribute attribute)
    {
        name = attribute.getName();
        value = attribute.getValue();
    }
    
    /**
     * Utility method to convert this <code>Attribute</code> instance to a <code>javax.management.Attribute</code> instance.
     * 
     * @return A JMX <code>Attribute</code> based upon this instance.
     */
    public javax.management.Attribute toAttribute()
    {
        return new javax.management.Attribute(name, value);
    }
}
