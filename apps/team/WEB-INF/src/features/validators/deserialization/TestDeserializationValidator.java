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
package features.validators.deserialization;

import flex.messaging.config.ConfigMap;
import flex.messaging.validators.DeserializationValidator;

public class TestDeserializationValidator implements DeserializationValidator
{
    /**
     * Simply prints the assignment and always returns true.
     */
    public boolean validateAssignment(Object instance, int index, Object value)
    {
        System.out.println("validateAssign1: [" + (instance == null? "null" : instance.getClass().getName()) + "," + index + "," + value + "]");
        return true;
    }

    /**
     * Simply prints the assignment and always returns true.
     */
    public boolean validateAssignment(Object instance, String propertyName, Object value)
    {
        System.out.println("validateAssign2: [" + (instance == null? "null" : instance.getClass().getName()) + "," + propertyName + "," + value + "]");
        return true;
    }

    /**
     * Simply prints the creation and always returns true.
     */
    public boolean validateCreation(Class<?> c)
    {
        System.out.println("validateCreate: " + (c == null? "null" : c.getName()));
        return true;
    }

    /* (non-Javadoc)
     * @see flex.messaging.FlexConfigurable#initialize(java.lang.String, flex.messaging.config.ConfigMap)
     */
    public void initialize(String id, ConfigMap configMap)
    {
        // No-op.
    }
}