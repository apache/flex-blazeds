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
import java.io.Externalizable;
import java.io.Serializable;

import flex.messaging.MessageException;
import flex.messaging.io.amf.ASObject;
import flex.messaging.log.LogCategories;
import flex.messaging.log.Log;
import flex.messaging.log.Logger;
import flex.messaging.util.ClassUtil;

/**
 * Simple abstract implementation of PropertyProxy's common properties. Specific
 * sub-classes need to provide the full implementation focusing on the retrieval
 * of the instance traits or "list of properties" and a specific value for
 * a given property name.
 *
 * @see flex.messaging.io.PropertyProxy
 *
 */
public abstract class AbstractProxy implements PropertyProxy, Serializable
{
    protected Object defaultInstance;
    protected String alias;
    protected boolean dynamic;
    protected boolean externalizable;
    protected boolean includeReadOnly;
    protected SerializationDescriptor descriptor;
    protected SerializationContext context;

    protected static final String LOG_CATEGORY = LogCategories.ENDPOINT_TYPE;
    private static final int CONVERSION_ERROR = 10006;

    protected AbstractProxy(Object defaultInstance)
    {
        this.defaultInstance = defaultInstance;
        if (defaultInstance != null)
            alias = defaultInstance.getClass().getName();
    }

    /** {@inheritDoc} */
    public Object getDefaultInstance()
    {
        return defaultInstance;
    }

    /** {@inheritDoc} */
    public void setDefaultInstance(Object instance)
    {
        defaultInstance = instance;
    }

    /**
     * A utility method which returns the Class from the given Class name
     * using the current type context's class loader.
     *
     * @param className the class name.
     * @return a Class object for the named class.
     */
    public static Class getClassFromClassName(String className)
    {
        TypeMarshallingContext typeContext = TypeMarshallingContext.getTypeMarshallingContext();
        return ClassUtil.createClass(className, typeContext.getClassLoader());
    }

    /**
     * A utility method which creates an instance from a given class name.  It assumes
     * the class has a zero arg constructor.
     * @param className the class name
     * for a type that is missing on the server, instead of throwing a server resource not found
     * exception.
     * @return the instance of the named class.
     */
    public static Object createInstanceFromClassName(String className)
    {
        Class<?> desiredClass = getClassFromClassName(className);
        return ClassUtil.createDefaultInstance(desiredClass, null, true /*validate*/);
    }

    /** {@inheritDoc} */
    public Object createInstance(String className)
    {
        Object instance;

        if (className == null || className.length() == 0)
        {
            instance = ClassUtil.createDefaultInstance(ASObject.class, null, true /*validate*/);
        }
        else if (className.startsWith(">")) // Handle [RemoteClass] (no server alias)
        {
            instance = ClassUtil.createDefaultInstance(ASObject.class, null, true /*validate*/);
            ((ASObject)instance).setType(className);
        }
        else
        {
            if (getSerializationContext().instantiateTypes || className.startsWith("flex."))
                return createInstanceFromClassName(className);

            // Just return type info with an ASObject...
            instance = ClassUtil.createDefaultInstance(ASObject.class, null, true /*validate*/);
            ((ASObject)instance).setType(className);
        }
        return instance;
    }

    /** {@inheritDoc} */
    public List getPropertyNames()
    {
        return getPropertyNames(getDefaultInstance());
    }

    /** {@inheritDoc} */
    public Class getType(String propertyName)
    {
        return getType(getDefaultInstance(), propertyName);
    }

    /** {@inheritDoc} */
    public Object getValue(String propertyName)
    {
        return getValue(getDefaultInstance(), propertyName);
    }

    /** {@inheritDoc} */
    public void setValue(String propertyName, Object value)
    {
        setValue(getDefaultInstance(), propertyName, value);
    }

    /** {@inheritDoc} */
    public void setAlias(String value)
    {
        alias = value;
    }

    /** {@inheritDoc} */
    public String getAlias()
    {
        return alias;
    }

    /** {@inheritDoc} */
    public void setDynamic(boolean value)
    {
        dynamic = value;
    }

    /** {@inheritDoc} */
    public boolean isDynamic()
    {
        return dynamic;
    }

    /** {@inheritDoc} */
    public boolean isExternalizable()
    {
        return externalizable;
    }

    /** {@inheritDoc} */
    public void setExternalizable(boolean value)
    {
        externalizable = value;
    }

    /** {@inheritDoc} */
    public boolean isExternalizable(Object instance)
    {
        return instance instanceof Externalizable;
    }

    /** {@inheritDoc} */
    public SerializationContext getSerializationContext()
    {
        return context == null? SerializationContext.getSerializationContext() : context;
    }

    /** {@inheritDoc} */
    public void setSerializationContext(SerializationContext value)
    {
        context = value;
    }

    /** {@inheritDoc} */
    public void setIncludeReadOnly(boolean value)
    {
        includeReadOnly = value;
    }

    /** {@inheritDoc} */
    public boolean getIncludeReadOnly()
    {
        if (includeReadOnly)
        {
            return true;
        }
        return getSerializationContext().includeReadOnly;
    }

    /** {@inheritDoc} */
    public SerializationDescriptor getDescriptor()
    {
        return descriptor;
    }

    /** {@inheritDoc} */
    public void setDescriptor(SerializationDescriptor descriptor)
    {
        this.descriptor = descriptor;
    }

    /**
     * This is called after the serialization finishes.  We return the same object
     * here... this is an opportunity to replace the instance we use once we have
     * gathered all of the state into a temporary object.
     * @param instance current instance
     * @return Object the instance after complete serialization
     */
    public Object instanceComplete(Object instance)
    {
        return instance;
    }

    /**
     * Returns the instance to serialize in place of the supplied instance.
     * @param instance the instance to serialize
     * @return Object the instance
     */
    public Object getInstanceToSerialize(Object instance)
    {
        return instance;
    }

    /** {@inheritDoc} */
    @Override
    public Object clone()
    {
        try
        {
            AbstractProxy clonedProxy= (AbstractProxy) super.clone();
            clonedProxy.setCloneFieldsFrom(this);
            return clonedProxy;
        }
        catch (CloneNotSupportedException e)
        {
            if (Log.isError())
            {
                Logger log = Log.getLogger(LOG_CATEGORY);
                log.error("Failed to clone a property proxy: " + toString());
            }
            MessageException ex = new MessageException();
            ex.setMessage(CONVERSION_ERROR);
            throw ex;
        }
    }

    /**
     * A string including the default instance, class and descriptor info.
     * @return debug string.
     */
    @Override
    public String toString()
    {
        if (defaultInstance != null)
            return "[Proxy(inst=" + defaultInstance + ") proxyClass=" + getClass() + " descriptor=" + descriptor + "]";
        return "[Proxy(proxyClass=" + getClass() + " descriptor=" + descriptor + "]";
    }

    protected void setCloneFieldsFrom(AbstractProxy source)
    {
        setDescriptor(source.getDescriptor());
        setDefaultInstance(source.getDefaultInstance());
        context = source.context;
        includeReadOnly = source.includeReadOnly;
    }
}