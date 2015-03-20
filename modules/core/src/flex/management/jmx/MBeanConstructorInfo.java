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
 * Remotable <code>MBeanConstructorInfo</code> class that complies with Flash serialization requirements.
 */
public class MBeanConstructorInfo
{
    /**
     * The name of the constructor.
     */
    public String name;
    
    /**
     * The description of the constructor.
     */
    public String description;
    
    /**
     * The constructor's parameter signature.
     */
    public MBeanParameterInfo[] signature;
    
    /**
     * Constructs an empty <code>MBeanConstructorInfo</code> instance.
     *
     */
    public MBeanConstructorInfo()
    {}
    
    /**
     * Constructs a <code>MBeanConstructorInfo</code> instance based upon a
     * <code>javax.management.MBeanConstructorInfo</code> instance.
     * 
     * @param mbeanConstructorInfo The <code>javax.management.MBeanConstructorInfo</code> to base this instance on.
     */
    public MBeanConstructorInfo(javax.management.MBeanConstructorInfo mbeanConstructorInfo)
    {
        name = mbeanConstructorInfo.getName();
        description = mbeanConstructorInfo.getDescription();
        signature = convertSignature(mbeanConstructorInfo.getSignature());
    }
    
    /**
     * Utility method to convert this <code>MBeanConstructorInfo</code> instance to a
     * <code>javax.management.MBeanConstructorInfo</code> instance.
     * 
     * @return A JMX <code>MBeanConstructorInfo</code> based upon this instance.
     */
    public javax.management.MBeanConstructorInfo toMBeanConstructorInfo()
    {
        return new javax.management.MBeanConstructorInfo(name,
                                                         description,
                                                         convertSignature(signature));
    }    
    
    /**
     * Utility method to convert the JMX constructor signature to our Flash friendly param type.
     * 
     * @param source The JMX constructor signature params.
     * @return Flash friendly signature params.
     */
    private MBeanParameterInfo[] convertSignature(javax.management.MBeanParameterInfo[] source)
    {
        MBeanParameterInfo[] signature = new MBeanParameterInfo[source.length];
        for (int i = 0; i < source.length; i++)
        {
            signature[i] = new MBeanParameterInfo(source[i]);
        }
        return signature;
    }
    
    /**
     * Utility method to convert a Flash friendly construtor param signature to the JMX params.
     * 
     * @param source The Flash friendly signature params.
     * @return The JMX constructor signature params.
     */
    private javax.management.MBeanParameterInfo[] convertSignature(MBeanParameterInfo[] source)
    {
        javax.management.MBeanParameterInfo[] signature = new javax.management.MBeanParameterInfo[source.length];
        for (int i = 0; i < source.length; i++)
        {
            signature[i] = source[i].toMBeanParameterInfo();
        }
        return signature;
    }   

}
