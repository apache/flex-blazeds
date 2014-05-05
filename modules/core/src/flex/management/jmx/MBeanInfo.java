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
 * Remotable MBeanInfo class that complies with Flash serialization requirements. 
 * MBean Notifications are not currently supported.
 *
 * @author shodgson
 */
public class MBeanInfo
{
    /**
     * The Java class name for the MBean object.
     */
    public String className;
    
    /**
     * The description of the MBean.
     */
    public String description;
    
    /**
     * The attributes exposed for management.
     */
    public MBeanAttributeInfo[] attributes;
    
    /**
     * The public constructors for the MBean.
     */
    public MBeanConstructorInfo[] constructors;
    
    /**
     * The operations exposed by the MBean.
     */
    public MBeanOperationInfo[] operations;
        
    /**
     * Constructs an empty <code>MBeanInfo</code> instance.
     */
    public MBeanInfo()
    {}
    
    /**
     * Constructs a <code>MBeanInfo</code> instance based upon a
     * <code>javax.management.MBeanInfo</code> instance.
     * 
     * @param mbeanInfo The JMX <code>MBeanInfo</code> instance to base this instance on.
     */
    public MBeanInfo(javax.management.MBeanInfo mbeanInfo)
    {
        className = mbeanInfo.getClassName();
        description = mbeanInfo.getDescription();
        attributes = convertAttributes(mbeanInfo.getAttributes());
        constructors = convertConstructors(mbeanInfo.getConstructors());
        operations = convertOperations(mbeanInfo.getOperations());
    }
    
    /**
     * Utility method to convert this <code>MBeanInfo</code> to a
     * <code>javax.management.MBeanInfo</code> instance.
     * 
     * @return A JMX <code>MBeanInfo</code> based upon this instance.
     */
    public javax.management.MBeanInfo toMBeanInfo()
    {
        return new javax.management.MBeanInfo(className,
                                              description,
                                              convertAttributes(attributes),
                                              convertConstructors(constructors),
                                              convertOperations(operations),
                                              null);
    }      
    
    /**
     * Utility method to convert JMX attribute info instances to Flash friendly instances.
     * 
     * @param source JMX attribute info instances.
     * @return Flash friendly attribute info instances.
     */
    private MBeanAttributeInfo[] convertAttributes(javax.management.MBeanAttributeInfo[] source)
    {
        MBeanAttributeInfo[] attributes = new MBeanAttributeInfo[source.length];
        for (int i = 0; i < source.length; i++)
        {
            attributes[i] = new MBeanAttributeInfo(source[i]);
        }
        return attributes;
    }    
    
    /**
     * Utility method to convert Flash friendly attribute info instances to JMX attribute info instances.
     * 
     * @param source Flash friendly attribute info instances.
     * @return JMX attribute info instances.
     */
    private javax.management.MBeanAttributeInfo[] convertAttributes(MBeanAttributeInfo[] source)
    {
        javax.management.MBeanAttributeInfo[] attributes = new javax.management.MBeanAttributeInfo[source.length];
        for (int i = 0; i < source.length; i++)
        {
            attributes[i] = source[i].toMBeanAttributeInfo();
        }
        return attributes;
    }
    
    /**
     * Utility method to convert JMX constructor info instances to Flash friendly constructor info
     * instances.
     * 
     * @param source JMX constructor info instances.
     * @return Flash friendly constructor info instances.
     */
    private MBeanConstructorInfo[] convertConstructors(javax.management.MBeanConstructorInfo[] source)
    {
        MBeanConstructorInfo[] constructors = new MBeanConstructorInfo[source.length];
        for (int i = 0; i < source.length; i++)
        {
            constructors[i] = new MBeanConstructorInfo(source[i]);            
        }
        return constructors;
    }
    
    /**
     * Utility method to convert Flash friendly constructor info instances to JMX constructor info instances.
     * 
     * @param source Flash friendly constructor info instances.
     * @return JMX constructor info instances.
     */
    private javax.management.MBeanConstructorInfo[] convertConstructors(MBeanConstructorInfo[] source)
    {
        javax.management.MBeanConstructorInfo[] constructors = new javax.management.MBeanConstructorInfo[source.length];
        for (int i = 0; i < source.length; i++)
        {
            constructors[i] = source[i].toMBeanConstructorInfo();
        }
        return constructors;
    }
    
    /**
     * Utility method to convert JMX operation info instances to Flash friendly operation info instances.
     * 
     * @param source JMX opereration info instances.
     * @return Flash friendly operation info instances.
     */
    private MBeanOperationInfo[] convertOperations(javax.management.MBeanOperationInfo[] source)
    {
        MBeanOperationInfo[] operations = new MBeanOperationInfo[source.length];
        for (int i = 0; i < source.length; i++)
        {
            operations[i] = new MBeanOperationInfo(source[i]);
        }
        return operations;
    }
    
    /**
     * Utility method to convert Flash friendly operation info instances to JMX operation info instances.
     * 
     * @param source Flash friendly operation info instances. 
     * @return JMX operation info instances.
     */
    private javax.management.MBeanOperationInfo[] convertOperations(MBeanOperationInfo[] source)
    {
        javax.management.MBeanOperationInfo[] operations = new javax.management.MBeanOperationInfo[source.length];
        for (int i = 0; i < source.length; i++)
        {
            operations[i] = source[i].toMBeanOperationInfo();
        }
        return operations;
    }
    
}
