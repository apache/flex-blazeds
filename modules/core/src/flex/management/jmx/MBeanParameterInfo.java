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
 * Remotable MBeanParameterInfo class that complies with Flash serialization requirements.

 */
public class MBeanParameterInfo
{
    /**
     * The name of the parameter.
     */
    public String name;
    
    /**
     * The Java type for the parameter.
     */
    public String type;
    
    /**
     * The description for the parameter.
     */
    public String description;
    
    /**
     * Constructs an empty <code>MBeanParameterInfo</code> instance.
     */
    public MBeanParameterInfo()
    {}
    
    /**
     * Constructs a <code>MBeanParameterInfo</code> instance based upon a
     * <code>javax.management.MBeanParameterInfo</code> instance.
     * 
     * @param mbeanParameterInfo The JMX <code>MBeanParameterInfo</code> instance to base this instance on.
     */
    public MBeanParameterInfo(javax.management.MBeanParameterInfo mbeanParameterInfo)
    {
        name = mbeanParameterInfo.getName();
        type = mbeanParameterInfo.getType();
        description = mbeanParameterInfo.getDescription();
    }
    
    /**
     * Utility method to convert this <code>MBeanParameterInfo</code> to a
     * <code>javax.management.MBeanParameterInfo</code> instance.
     * 
     * @return A JMX <code>MBeanParameterInfo</code> based upon this instance.
     */
    public javax.management.MBeanParameterInfo toMBeanParameterInfo()
    {
        return new javax.management.MBeanParameterInfo(name,
                                                       type,
                                                       description);
    }

}
