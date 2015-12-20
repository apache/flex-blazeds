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

import java.util.List;

/**
 * A PropertyProxy allows customized serialization and deserialization of complex objects by 
 * providing access to each of the steps in the serialization and deserialization process.
 * A serializer asks a PropertyProxy for the class name, traits
 * and properties during serialization. A deserializer asks a PropertyProxy
 * to create a new instance and set property values. 
 * 
 * Different PropertyProxy implementations may be written for different
 * types of objects such as Map, Dictionary, Enumerable, Throwable,
 * and Beans. 
 */
public interface PropertyProxy extends Cloneable
{
    /**
     * The default instance managed by this PropertyProxy. The
     * default instance is used for one-type proxied instances.
     * 
     * @return The proxied instance.
     */
    Object getDefaultInstance();

    /**
     * Sets the default instance managed by this PropertyProxy.
     * 
     * @param defaultInstance The default instance.
     */
    void setDefaultInstance(Object defaultInstance);
    
    /**
     * Creates a new instance for the given className. ClassName is the 
     * value of the "alias" for the ActionScript class serialized.
     * If the className is invalid an anonymous ASObject is created. If the className is
     * prefixed with "&gt;" an ASObject is created with the type set however
     * a concrete instance is not instantiated.
     * @param className the class to create
     * @return an instance of className
     */
    Object createInstance(String className);

    /**
     * The List of property names as Strings that make up the traits
     * of the default instance. These traits determine which properties
     * are to be serialized.
     * 
     * @return The set of property names as Strings to be serialized.
     */
    List getPropertyNames();

    /**
     * The List of property names as Strings that make up the traits
     * of the given instance. These traits determine which properties
     * are to be serialized.
     * 
     * @param instance the object to examine
     * @return List of property names as Strings to be serialized.
     */
    List getPropertyNames(Object instance);

    /**
     * Looks up the Class type of the property by name on the default instance.
     * @param propertyName The name of the property.
     * @return The property type.
     */
    Class getType(String propertyName);
    
    /**
     * Looks up the Class type of the property by name on the given instance.
     * @param instance The instance that possesses the property.
     * @param propertyName The name of the property.
     * @return The property type.
     */
    Class getType(Object instance, String propertyName);

    /**
     * Looks up the value of the property by name from the default 
     * instance.
     * 
     * @param propertyName The name of the property.
     * @return The value of the given property.
     */
    Object getValue(String propertyName);

    /**
     * Looks up the value of a property by name for the given instance.
     * 
     * @param instance The instance that possesses the requested property.
     * @param propertyName The name of the property.
     * @return The value of the given property.
     */
    Object getValue(Object instance, String propertyName);

    /**
     * Updates the value of a propery by name for the default instance.
     * 
     * @param propertyName the property name
     * @param value the new value
     */
    void setValue(String propertyName, Object value);
    
    /**
     * Updates the value of a property by name for the given instance.
     * 
     * @param instance The instance that possesses the requested property.
     * @param propertyName The name of the property to update.
     * @param value The updated value for the property.
     */
    void setValue(Object instance, String propertyName, Object value);

    /**
     *  This is called after the deserialization of the instance is complete (i.e.
     *  after the lastSetValue call.  It has the opportunity to return an instance
     *  to use to replace the instance returned previously in createInstance.
     *  NOTE however that this approach does not support recursive references back to 
     *  this same object (i.e. if a property of this object refers back to itself).
     *  @param instance the instance being deserialized (previously returned from a
     *  createInstance call)
     *  @return possibly the same instance to use for this object.
     */
    Object instanceComplete(Object instance);
    
    /**
     * Allows an alias to be set for the instance type. By default
     * the classname of the instance is used.
     * 
     * @param value The class name alias.
     */
    void setAlias(String value);

    /**
     * The class name alias for the default instance.
     *  
     * @return The class name alias to be used in serializing the type traits.
     */
    String getAlias();

    /**
     * The class name alias for the given instance.
     *  
     * @param instance the object to get the alias of.
     * @return The class name alias to be used in serializing the type traits.
     */
    String getAlias(Object instance);

    /**
     * Dynamic is a client-only concept for types that allow for arbitrary
     * public properties to be defined at runtime. This setting isn't yet 
     * relevant for serialization but can still be set in the Object traits.
     * 
     * @param value Whether the client type is expected to be dynamic. This setting
     * is currently not used.
     */
    void setDynamic(boolean value);
    
    /**
     * The trait setting "dynamic" is a client-only concept for types that 
     * allow for arbitrary public properties to be defined at runtime.
     * The default is false and it is unlikely that it would need to be set.
     *  
     * @return The dynamic client trait setting to be used during serialization of the type.
     */
    boolean isDynamic();

    /**
     * Specifies whether the default instance manages its own serialization through the
     * Externalizable interface.
     * 
     * @return Whether the default instance implements java.io.Externalizable.
     */
    boolean isExternalizable();

    /**
     * Specifies whether the given instance manages its own serialization through the
     * Externalizable interface.
     * 
     * @param instance the object to check
     * @return Whether the given instance implements java.io.Externalizable.
     */
    boolean isExternalizable(Object instance);

    /**
     * Specifies whether the given instance manages its own serialization through the
     * Externalizable interface. If this property is set to true then the type
     * must implement java.io.Externalizable. Setting this property to false allows an 
     * otherwise Externalizable instance to be considered as a normal type for 
     * custom serialization.
     * 
     * @param value if set to true the instance must implement java.io.Externalizable, otherwise
     * if set to false the proxied type can avoid external serialization
     * for an otherwise Externalizable type.
     */
    void setExternalizable(boolean value);

    /**
     * The context holds settings that govern serialization behavior.
     * @return The current serialization context, or a new default instance if undefined.
     */
    SerializationContext getSerializationContext();
    
    /**
     * Sets the context for serialization behavior.
     * @param value the new context
     */
    void setSerializationContext(SerializationContext value);

    /**
     * If set to true, read only properties will be included
     * during default serialization. The default is false.
     * 
     * @param value whether read only properties should be included. 
     */
    void setIncludeReadOnly(boolean value);
    
    /**
     * Determines whether read only properties from the instance should be included
     * during default serialization. The default is false.
     * 
     * @return Whether read only properties should be included during default
     * serialization.
     */
    boolean getIncludeReadOnly();

    /**
     * A serialization descriptor that provides overrides to the
     * default behavior for selecting properties for serialization. At
     * any given level a list of includes and excludes can be specified.
     * Complex child properties can have their own descriptors specified
     * in a nested manner.
     * 
     * @return The serialization descriptor for custom serialization.
     */
    SerializationDescriptor getDescriptor();

    /**
     * Allows non-default inclusion/exclusion of properties for
     * serialization.
     * 
     * @param descriptor The descriptor to customize property selection for serialization.
     */
    void setDescriptor(SerializationDescriptor descriptor);

    /**
     * Returns a copy of the PropertyProxy so that it can be used as a template without 
     * modifying/creating global references to instances, descriptors etc.
     * 
     * @return A copy of the PropertyProxy.
     */
    Object clone();

    /**
     * This is called right before we are about to serialize the supplied instance.
     * You can override this method to serialize an instance which you want to serialize
     * instead of the instance encountered in the object graph.  If you return an
     * instance of a different class, we use the PropertyProxyRegistry to get a new
     * proxy for that instance.  That proxy is then used during the rest of the serialization.
     * Note that the objects returned from this method should be serialized through their
     * properties.  You cannot return a String, Integer or other primitive type.
     *
     * @param instance the instance encountered during AMF serialization
     * @return the instance you want to serialize in its place
     */
    Object getInstanceToSerialize(Object instance);
}
