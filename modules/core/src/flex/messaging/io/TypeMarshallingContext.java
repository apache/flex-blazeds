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

import flex.messaging.FlexContext;
import flex.messaging.MessageBroker;
import flex.messaging.io.amf.ASObject;
import flex.messaging.io.amf.translator.ASTranslator;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * A simple context to hold type marshalling specific settings.
 */
public class TypeMarshallingContext
{
    private static ThreadLocal<TypeMarshallingContext> contexts = new ThreadLocal<TypeMarshallingContext>();
    private static ThreadLocal<TypeMarshaller> marshallers = new ThreadLocal<TypeMarshaller>();
    private IdentityHashMap knownObjects;
    private ClassLoader classLoader;

    /**
     * Constructs a default type marshalling context.
     */
    public TypeMarshallingContext()
    {
    }

    /**
     * Establishes a TypeMarshallingContext for the current thread.
     * Users are not expected to call this function.
     * @param context The current TypeMarshallingContext.
     */
    public static void setTypeMarshallingContext(TypeMarshallingContext context)
    {
        if (context == null)
            contexts.remove();
        else
            contexts.set(context);
    }

    /**
     * Get current TypeMarshallingContext object
     * @return The current thread's TypeMarshallingContext.
     */
    public static TypeMarshallingContext getTypeMarshallingContext()
    {
        TypeMarshallingContext context = contexts.get();
        if (context == null)
        {
            context = new TypeMarshallingContext();
            TypeMarshallingContext.setTypeMarshallingContext(context);
        }
        return context;
    }

    /**
     * Establishes a TypeMarshallingContext for the current thread.
     * Users are not expected to call this function.
     * 
     * @param marshaller The current TypeMarshaller.
     */
    public static void setTypeMarshaller(TypeMarshaller marshaller)
    {
        if (marshaller == null)
            marshallers.remove();
        else
            marshallers.set(marshaller);
    }

    /**
     * Get current TypeMarshaller.
     * @return The current thread's TypeMarshaller.
     */
    public static TypeMarshaller getTypeMarshaller()
    {
        TypeMarshaller marshaller = marshallers.get();
        if (marshaller == null)
        {
            marshaller = new ASTranslator();
            setTypeMarshaller(marshaller);
        }

        return marshaller; 
    }

    /**
     * Returns the custom ClassLoader for this type marshalling session, or
     * defaults to the current MessageBroker's ClassLoader if none has been set.
     * @return ClassLoader the ClassLoader in use
     */
    public ClassLoader getClassLoader()
    {
        if (classLoader != null)
            return classLoader;

        try
        {
            MessageBroker messageBroker = FlexContext.getMessageBroker();
            return messageBroker != null? messageBroker.getClassLoader() : null;
        }
        catch (NoClassDefFoundError exception) // Could happen in client mode.
        {
            return null;
        }
    }

    /**
     * Sets a custom classloader for this type marshalling session that will
     * be used to create new instances of strongly typed objects.
     * @param loader the ClassLoader to use
     */
    public void setClassLoader(ClassLoader loader)
    {
        classLoader = loader;
    }

    /**
     * A map of known objects already encountered in this type marshalling
     * session.
     * @return IdentityHashMap the known objects
     */
    public IdentityHashMap getKnownObjects()
    {
        if (knownObjects == null)
            knownObjects = new IdentityHashMap(64);

        return knownObjects;
    }

    /**
     * Sets the list of the objects already encountered for this type
     * marshalling session.
     * @param knownObjects the IdentityHashMap object
     */
    public void setKnownObjects(IdentityHashMap knownObjects)
    {
        this.knownObjects = knownObjects;
    }

    /**
     * Resets the list of known objects.
     */
    public void reset()
    {
        if (knownObjects != null)
            knownObjects.clear();
    }

    /**
     * A utility method to determine whether an anonymous type specifies
     * a strong type name, such as ASObject.getType() or the legacy Flash
     * Remoting convention of using a _remoteClass property.
     * @param obj the object to check
     * @return The name of the strong type, or null if none was specified.
     */
    public static String getType(Object obj)
    {
        String type = null;

        if (obj != null && obj instanceof Map)
        {
            Map map = (Map)obj;

            //Check for an Object.registerClass Typed ASObject
            if (map instanceof ASObject)
            {
                ASObject aso = (ASObject)map;
                type = aso.getType();
            }

            SerializationContext sc = SerializationContext.getSerializationContext();
            
            if (type == null && sc.supportRemoteClass)
            {
                Object registerClass = map.get(MessageIOConstants.REMOTE_CLASS_FIELD);
                if (registerClass != null && registerClass instanceof String)
                {
                    type = (String)registerClass;
                }
            }
        }

        return type;
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
        if (marshallers != null)
        {
            marshallers.remove();
        }
    }

    /**
     * @exclude
     * Destroy static thread local storage.
     * Call ONLY on shutdown.
     */
    public static void releaseThreadLocalObjects()
    {
        clearThreadLocalObjects();
        
        contexts = null;
        marshallers = null;
    }

    /**
     * @exclude
     * Create static thread local storage.
     */
    public static void createThreadLocalObjects()
    {
        if (contexts == null)
            contexts = new ThreadLocal();
        if (marshallers == null)
            marshallers = new ThreadLocal();
    }
}
