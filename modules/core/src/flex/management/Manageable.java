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
package flex.management;

/**
 * Manageability of a class is enabled by implementing this interface. The
 * specific level of manageability is defined by the relationship between
 * a manageable component and its corresponding control.
 * 
 * @author shodgson
 */
public interface Manageable
{
    /**
     * Returns <code>true</code> if the component is enabled for management.
     * 
     * @return <code>true</code> if the component is enabled for management.
     */
    boolean isManaged();
    
    /**
     * Enables or disables management for the component.
     * 
     * @param enableManagement <code>true</code> to enable management, <code>false</code> to disable management.
     */
    void setManaged(boolean enableManagement);
    
    /**
     * Returns the control MBean used to manage the component.
     * 
     * @return The control MBean used to manage the component.
     */
    BaseControl getControl();
    
    /**
     * Set the control MBean used to manage the component.
     * 
     * @param control The <code>BaseControl</code> MBean used to manage the component.
     */
    void setControl(BaseControl control);
}
