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
 * Remotable MBeanOperationInfo class that complies with Flash serialization requirements.
 *
 * @author shodgson
 */
public class MBeanOperationInfo
{
    /**
     * The operation name.
     */
    public String name;
    
    /**
     * The operation description.
     */
    public String description;
    
    /**
     * The operation's argument signature.
     */
    public MBeanParameterInfo[] signature;
    
    /**
     * The operation's return type.
     */
    public String returnType;
    
    /**
     * The impact of the operation; one of <code>INFO, ACTION, ACTION_INFO, UNKNOWN</code>.
     */
    public int impact;
    
    /**
     * Constructs an empty <code>MBeanOperationInfo</code> instance.    
     */
    public MBeanOperationInfo()
    {}
    
    /**
     * Constructs a <code>MBeanOperationInfo</code> instance based upon a
     * <code>javax.management.MBeanOperationInfo</code> instance.
     * 
     * @param mbeanOperationInfo The JMX <code>MBeanOperationInfo</code> instance to base this instance on.
     */
    public MBeanOperationInfo(javax.management.MBeanOperationInfo mbeanOperationInfo)
    {
        name = mbeanOperationInfo.getName();
        description = mbeanOperationInfo.getDescription();
        signature = convertSignature(mbeanOperationInfo.getSignature());
        returnType = mbeanOperationInfo.getReturnType();
        impact = mbeanOperationInfo.getImpact();
    }
    
    /**
     * Utility method to convert this <code>MBeanOperationInfo</code> to a
     * <code>javax.management.MBeanOperationInfo</code> instance.
     * 
     * @return A JMX <code>MBeanOperationInfo</code> based upon this instance.
     */
    public javax.management.MBeanOperationInfo toMBeanOperationInfo()
    {
        return new javax.management.MBeanOperationInfo(name,
                                                       description,
                                                       convertSignature(signature),
                                                       returnType,
                                                       impact);
    }
    
    /**
     * Utility method to convert JMX parameter info instances to Flash friendly parameter info instances.
     * 
     * @param source JMX parameter info instances.
     * @return Flash friendly parameter info instances.
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
     * Utility method to convert Flash friendly parameter info instances to JMX parameter info instances.
     * 
     * @param source Flash friendly parameter info instances.
     * @return JMX parameter info instances.
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
