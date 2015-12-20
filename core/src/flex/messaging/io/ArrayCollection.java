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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Used to map to client mx.collections.ArrayCollection to java.util.Lists in Java.
 */
public class ArrayCollection extends ArrayList implements Externalizable
{
    private static final long serialVersionUID = 8037277879661457358L;

    private SerializationDescriptor descriptor = null;

    /**
     * Default constructor.
     */
    public ArrayCollection()
    {
        super();
    }

    /**
     * Creates an <tt>ArrayCollection</tt> with the supplied collection.
     *
     * @param c Collection.
     */
    public ArrayCollection(Collection c)
    {
        super(c);
    }

    /**
     * Creates <tt>ArrayCollection</tt> with the supllied inital capacity.
     *
     * @param initialCapacity The initial capacity.
     */
    public ArrayCollection(int initialCapacity)
    {
        super(initialCapacity);
    }

    /**
     * Returns the backing Array of the <tt>ArrayCollection</tt>.
     *
     * @return The backing Array.
     */
    public Object[] getSource()
    {
        return toArray();
    }

    /**
     * Sets the serialization descriptor.
     *
     * @param desc The serialization descriptor.
     */
    public void setDescriptor(SerializationDescriptor desc)
    {
        this.descriptor = desc;
    }

    /**
     * Sets the source with the supplied Array.
     *
     * @param s The source Array.
     */
    public void setSource(Object[] s)
    {
        if (s == null)
        {
            clear();
            return;
        }

        if (size() > 0)
            clear();

        for (int i = 0; i < s.length; i++)
            add(s[i]);
    }

    /**
     * Sets the sources with the supplied Collection.
     *
     * @param s The source Collection.
     */
    public void setSource(Collection s)
    {
        addAll(s);
    }

    /**
     * Implements {@link Externalizable#readExternal(ObjectInput)}
     *
     * @param input The object input.
     */
    public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException
    {
        Object s = input.readObject();
        if (s instanceof Collection)
            s = ((Collection)s).toArray();
        Object[] source = (Object[])s;
        setSource(source);
    }

    /**
     * Implements {@link Externalizable#writeExternal(ObjectOutput)}
     *
     * @param output The object output.
     */
    public void writeExternal(ObjectOutput output) throws IOException
    {
        if (descriptor == null)
        {
            output.writeObject(getSource());
            return;
        }

        Object[] source = getSource();
        if (source == null)
        {
            output.writeObject(null);
            return;
        }

        for (int i = 0; i < source.length; i++)
        {
            Object item = source[i];
            if (item == null)
            {
                source[i] = null;
            }
            else
            {
                PropertyProxy proxy = PropertyProxyRegistry.getProxy(item);
                proxy = (PropertyProxy)proxy.clone();
                proxy.setDescriptor(descriptor);
                proxy.setDefaultInstance(item);
                source[i] = proxy;
            }
        }
        output.writeObject(source);
    }
}
