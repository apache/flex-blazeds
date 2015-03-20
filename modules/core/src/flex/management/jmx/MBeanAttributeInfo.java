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
 * Remotable <code>MBeanAttributeInfo</code> class that complies with Flash serialization requirements. 
 * The <code>isIs</code> property is not named <code>is</code> because <code>is</code> is 
 * an ActionScript keyword.
 */
public class MBeanAttributeInfo
{
    /**
     * The name of the attribute.
     */
    public String name;
    
    /**
     * The class name of the attribute.
     */
    public String type;
    
    /**
     * The description of the attribute.
     */
    public String description;

    /**
     * Whether the attribute can be read.
     */
    public boolean readable;
    
    /**
     * Whether the attribute can be written.
     */
    public boolean writable; 
    
    /**
     * Whether the attribute has an "is" getter.
     */
    public boolean isIs;
    
    /**
     * Constructs an empty <code>MBeanAttributeInfo</code> instance.
     */
    public MBeanAttributeInfo()
    {}
    
    /**
     * Constructs a <code>MBeanAttributeInfo</code> instance based upon a
     * <code>javax.management.MBeanAttributeInfo</code> instance.
     * 
     * @param mbeanAttributeInfo The JMX <code>MBeanAttributeInfo</code> instance to base this instance on.
     */
    public MBeanAttributeInfo(javax.management.MBeanAttributeInfo mbeanAttributeInfo)
    {
        name = mbeanAttributeInfo.getName();
        type = mbeanAttributeInfo.getType();
        description = mbeanAttributeInfo.getDescription();
        readable = mbeanAttributeInfo.isReadable();
        writable = mbeanAttributeInfo.isWritable();
        isIs = mbeanAttributeInfo.isIs();
    }
    
    /**
     * Utility method to convert this <code>MBeanAttributeInfo</code> to a
     * <code>javax.management.MBeanAttributeInfo</code> instance.
     * 
     * @return A JMX <code>MBeanAttributeInfo</code> based upon this instance.
     */
    public javax.management.MBeanAttributeInfo toMBeanAttributeInfo()
    {
        return new javax.management.MBeanAttributeInfo(name,
                                                       type,
                                                       description,
                                                       readable,
                                                       writable,
                                                       isIs);
    }

}
