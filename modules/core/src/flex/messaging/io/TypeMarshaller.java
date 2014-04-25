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
package flex.messaging.io;

/**
 * A utility to convert between data types, useful for mapping
 * loosely typed client classes to more strongly typed server classes.
 */
public interface TypeMarshaller
{
    /**
     * Creates an instance of the desired class without populating the type.
     * 
     * @param source The raw <tt>Object</tt> to be converted into an instance of the desired class.
     * @param desiredClass The type to which the source needs to be converted.
     * @return An instance of the desired class.
     */
    Object createInstance(Object source, Class desiredClass);

    /**
     * Converts the supplied source instance to an instance of the desired <tt>Class</tt>.
     * 
     * @param source The source instance.
     * @param desiredClass The type to which the source needs to be converted.
     * @return The converted instance of the desired class.
     */
    Object convert(Object source, Class desiredClass);

}
