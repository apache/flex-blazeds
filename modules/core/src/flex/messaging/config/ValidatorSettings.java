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
package flex.messaging.config;

import flex.messaging.validators.DeserializationValidator;

/**
 * Settings class for validators.
 */
public class ValidatorSettings extends PropertiesSettings
{
    private String className;
    private String type = DeserializationValidator.class.getName();

    /**
     * Returns the class name.
     *
     * @return The class name.
     */
    public String getClassName()
    {
        return className;
    }

    /**
     * Sets the class name.
     *
     * @param className The class name.
     */
    public void setClassName(String className)
    {
        this.className = className;
    }

    /**
     * Returns the type of the validator.
     *
     * @return The type of the validator.
     */
    public String getType()
    {
        return type;
    }

    /**
     * Sets the type of the validator.
     *
     * @param type The type of the validator.
     */
    public void setType(String type)
    {
        this.type = type;
    }
}