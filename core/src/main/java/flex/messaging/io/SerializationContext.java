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

import flex.messaging.util.ClassUtil;
import flex.messaging.validators.DeserializationValidator;

import java.io.Serializable;

/**
 * A simple context to get settings from an endpoint to a deserializer
 * or serializer.
 */
public class SerializationContext implements Serializable, Cloneable
{
    static final long serialVersionUID = -3020985035377116475L;

    // Endpoint serialization configuration flags
    public boolean legacyXMLDocument;
    public boolean legacyXMLNamespaces;
    public boolean legacyCollection;
    public boolean legacyDictionary;
    public boolean legacyMap;
    public boolean legacyThrowable;
    public boolean legacyBigNumbers;
    public boolean legacyExternalizable;
    public boolean restoreReferences;
    public boolean supportRemoteClass;
    public boolean supportDatesByReference; // Typically used by AMF Version 3 requests

    /**
     * Determines whether an ASObject is created by default for a type that is
     * missing on the server, instead of throwing a server resource not found
     * exception.
     */
    public boolean createASObjectForMissingType = false;

    /**
     * Provides a way to control whether small messages should be sent even
     * if the client can support them. If set to false, small messages
     * will not be sent.
     *
     * The default is true.
     */
    public boolean enableSmallMessages = true;

    /**
     * Determines whether type information will be used to instantiate a new instance.
     * If set to false, types will be deserialized as flex.messaging.io.ASObject instances
     * with type information retained but not used to create an instance.
     *
     * Note that types in the flex.* package (and any subpackage) will always be
     * instantiated.
     *
     * The default is true.
     */
    public boolean instantiateTypes = true;
    public boolean ignorePropertyErrors = true;
    public boolean logPropertyErrors = false;
    public boolean includeReadOnly = false;

    // How deep level of nest object in the object graph that we support
    public int maxObjectNestLevel = 512;
    
    // How deep level of nest collection objects in the object graph that we support
    // Similarly like how many dimensional matrix that we support for serialization.
    public int maxCollectionNestLevel = 15;

    public boolean allowXml = false;
    public boolean allowXmlDoctypeDeclaration = false;
    public boolean allowXmlExternalEntityExpansion = false;

    /**
     * In server-to-client serialization, determines whether Java Arrays and Lists
     * should be serialized as Flash Vectors, rather than Flash Array, and Flex
     * ArrayCollection respectively.
     */
    public boolean preferVectors = false;

    private Class deserializer;
    private Class serializer;
    private DeserializationValidator deserializationValidator;

    /**
     * The default constructor.
     */
    public SerializationContext()
    {
    }

    /**
     * Returns the deserializer class.
     *
     * @return The deserializer class.
     */
    public Class getDeserializerClass()
    {
        return deserializer;
    }

    /**
     * Sets the deserializer class.
     *
     * @param c The deserializer class.
     */
    public void setDeserializerClass(Class c)
    {
        deserializer = c;
    }

    /**
     * Returns the serializer class.
     *
     * @return The serializer class.
     */
    public Class getSerializerClass()
    {
        return serializer;
    }

    /**
     * Sets the serializer class.
     *
     * @param c The serializer class.
     */
    public void setSerializerClass(Class c)
    {
        serializer = c;
    }

    /**
     * Instantiates a new message deserializer.
     *
     * @return A new message deserializer instance.
     */
    public MessageDeserializer newMessageDeserializer()
    {
        Class deserializerClass = getDeserializerClass();
        if (deserializerClass == null)
        {
            deserializerClass = ClassUtil.createClass("flex.messaging.io.amf.AmfMessageDeserializer");
            this.setDeserializerClass(deserializerClass);
        }
        MessageDeserializer deserializer = (MessageDeserializer)ClassUtil.createDefaultInstance(deserializerClass, MessageDeserializer.class);
        return deserializer;
    }

    /**
     * Instantiates a new message serializer.
     *
     * @return A new message serializer instance.
     */
    public MessageSerializer newMessageSerializer()
    {
        Class serializerClass = getSerializerClass();
        if (serializerClass == null)
        {
            serializerClass = ClassUtil.createClass("flex.messaging.io.amf.AmfMessageSerializer");
            this.setSerializerClass(serializerClass);
        }
        MessageSerializer serializer = (MessageSerializer)ClassUtil.createDefaultInstance(serializerClass, MessageSerializer.class);
        return serializer;
    }

    /**
     * Returns the deserialization validator.
     *
     * @return The deserialization validator.
     */
    public DeserializationValidator getDeserializationValidator()
    {
        return deserializationValidator;
    }

    /**
     * Sets the deserialization validator.
     *
     * @param deserializationValidator The deserialization validator.
     */
    public void setDeserializationValidator(DeserializationValidator deserializationValidator)
    {
        this.deserializationValidator = deserializationValidator;
    }

    @Override
    public Object clone()
    {
        try
        {
            return super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            // this should never happen since this class extends object
            // but just in case revert to manual clone
            SerializationContext context = new SerializationContext();
            context.createASObjectForMissingType = createASObjectForMissingType;
            context.legacyXMLDocument = legacyXMLDocument;
            context.legacyXMLNamespaces = legacyXMLNamespaces;
            context.legacyCollection = legacyCollection;
            context.legacyDictionary = legacyDictionary;
            context.legacyMap = legacyMap;
            context.legacyThrowable = legacyThrowable;
            context.legacyBigNumbers = legacyBigNumbers;
            context.legacyExternalizable = legacyExternalizable;
            context.restoreReferences = restoreReferences;
            context.supportRemoteClass = supportRemoteClass;
            context.supportDatesByReference = supportDatesByReference; // Typically used by AMF Version 3 requests
            context.instantiateTypes = instantiateTypes;
            context.ignorePropertyErrors = ignorePropertyErrors;
            context.includeReadOnly = includeReadOnly;
            context.logPropertyErrors = logPropertyErrors;
            context.deserializer = deserializer;
            context.serializer = serializer;
            context.deserializationValidator = deserializationValidator;
            context.maxObjectNestLevel = maxObjectNestLevel;
            context.maxCollectionNestLevel = maxCollectionNestLevel;
            context.allowXml = allowXml;
            context.allowXmlDoctypeDeclaration = allowXmlDoctypeDeclaration;
            context.allowXmlExternalEntityExpansion = allowXmlExternalEntityExpansion;
            context.preferVectors = preferVectors;
            return context;
        }

    }

    private static ThreadLocal<SerializationContext> contexts = new ThreadLocal<SerializationContext>();

    /**
     * Establishes a SerializationContext for the current thread.
     * Users are not expected to call this function.
     * @param context The current SerializationContext.
     */
    public static void setSerializationContext(SerializationContext context)
    {
        if (context == null)
            contexts.remove();
        else
            contexts.set(context);
    }

    /**
     * @return The current thread's SerializationContext.
     */
    public static SerializationContext getSerializationContext()
    {
        SerializationContext sc = contexts.get();
        if (sc == null)
        {
            sc = new SerializationContext();
            SerializationContext.setSerializationContext(sc);
        }
        return sc;
    }
    /**
     * Clears out the thread local state after the request completes.
     */
    public static void clearThreadLocalObjects()
    {
        if (contexts != null)
        {
            contexts.remove();
        }
    }

    /**
     *
     * Create thread local storage.
     */
    public static void createThreadLocalObjects()
    {
        if (contexts == null)
            contexts = new ThreadLocal();
    }

    /**
     *
     * Destroy thread local storage.
     * Call ONLY on shutdown.
     */
    public static void releaseThreadLocalObjects()
    {
        clearThreadLocalObjects();

        contexts = null;
    }
}
