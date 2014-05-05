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
package flex.messaging.validators;

import flex.messaging.FlexConfigurable;

/**
 * Deserialization validator is registered with the Message broker and provide the
 * opportunity to validate the creation of classes and assignment of a property
 * of an instance to a value for incoming (client-to-server) deserialization.
 */
public interface DeserializationValidator extends FlexConfigurable
{

    /**
     * Validate the assignment of a value to an index of an Array or List instance.
     *
     * @param instance The Array or List instance.
     * @param index The index at which the value is being assigned.
     * @param value The value that is assigned to the index.
     * @return True if the assignment is valid.
     */
    boolean validateAssignment(Object instance, int index, Object value);

    /**
     * Validate the assignment of a property of an instance to a value.
     *
     * @param instance The instance with the property that is being assigned a new value.
     * @param propertyName The name of the property that is being assigned.
     * @param value The value that the property is being assigned to.
     * @return True if the assignment is valid.
     */
    boolean validateAssignment(Object instance, String propertyName, Object value);

    /**
     * Validate creation of a class.
     *
     * @param c The class that is being created.
     * @return True if the creation is valid.
     */
    boolean validateCreation(Class<?> c);
}
