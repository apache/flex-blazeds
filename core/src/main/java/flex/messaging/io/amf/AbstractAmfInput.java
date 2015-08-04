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
package flex.messaging.io.amf;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import flex.messaging.MessageException;
import flex.messaging.io.AbstractProxy;
import flex.messaging.io.BeanProxy;
import flex.messaging.io.ClassAliasRegistry;
import flex.messaging.io.PropertyProxy;
import flex.messaging.io.PropertyProxyRegistry;
import flex.messaging.io.SerializationContext;
import flex.messaging.io.SerializationException;
import flex.messaging.util.ClassUtil;
import flex.messaging.util.XMLUtil;

/**
 * A deserializer of AMF protocol data.
 *
 * @see ActionMessageOutput
 *
 */
public abstract class AbstractAmfInput extends AmfIO implements ActionMessageInput
{
    /**
     * This is the initial capacity that will be used for AMF arrays, vectors, etc.
     * that have length greater than 1024.
     */
    public static final int INITIAL_COLLECTION_CAPACITY = 1024;

    /**
     * This is the default max String length (25MB).
     */
    public static final int DEFAULT_MAX_STRING_BYTES = 26214400;
    public static final String STRING_MAX_STRING_BYTES = "max-string-bytes";

    protected BeanProxy beanProxy = new BeanProxy();
    protected DataInputStream in = null;
    protected int maxStringBytes = DEFAULT_MAX_STRING_BYTES;

    /**
     * Construct a deserializer without connecting it to an input stream.
     * @param context serialization parameters.
     */
    public AbstractAmfInput(SerializationContext context)
    {
        super(context);

        readMaxStringBytes();
    }

    /**
     * Internal use
     *
     */
    public void setInputStream(InputStream in)
    {
        this.in = new DataInputStream(in);
    }

    protected Object stringToDocument(String xml)
    {
        // FIXME: Temporary workaround for bug 194815
        if (xml != null && xml.indexOf('<') == -1)
            return xml;

        // Validation performed in XMLUtil#stringToDocument.
        return XMLUtil.stringToDocument(xml, !(context.legacyXMLNamespaces),
                context.allowXmlExternalEntityExpansion);
    }

    /**
     * A utility method that is used by subclasses to perform max-string-bytes
     * checks. By default, max-string-bytes is limited to 25MB for security
     * but this can be changed via JVM option called max-string-bytes.
     *
     * @param utflen The UTF string length to check.
     * @throws <tt>SerializationException</tt> if max-string-bytes has been exceeded.
     */
    protected void checkUTFLength(int utflen)
    {
        if (utflen > maxStringBytes)
        {
            // Error deserializing the string with length ''{0}'', it exceeds the max-string-bytes limit of ''{1}''.
            SerializationException ex = new SerializationException();
            ex.setMessage(10314, new Object[] {utflen, maxStringBytes});
            throw ex;
        }
    }

    /**
     * Internal use. Common logic for creating an object for AMF0 and AMF3. Used from
     * Amf0Input.readObjectValue() and Amf3Input.readScriptObject().
     * This method is responsible for resolving class aliases, creating and
     * registering a property proxy and instantiating an instance of the desired class.
     * The params array is used as a holder for parameter values that are updated or
     * created in this method, but are needed by its callers.
     * @param params array of values that may be updated by this method.
     *               [0] - String className - the name or alias of the class to create
     *               [1] - PropertyProxy proxy
     * @return an instance of the appropriate object for deserialization
     *
     */
    protected Object createObjectInstance(Object[] params)
    {
        String className = (String)params[0];

        // Check for any registered class aliases
        String aliasedClass = ClassAliasRegistry.getRegistry().getClassName(className);
        if (aliasedClass != null)
        {
            className = aliasedClass;
            params[0] = className; //update the params array so that callers get this change
        }

        Object object = null;
        PropertyProxy proxy = null;

        if (className != null && className.startsWith(">")) // Handle [RemoteClass] (no server alias)
        {
            object = createDefaultASObject(className);
        }
        else if (className != null && className.length() > 0 && (context.instantiateTypes || className.startsWith("flex.")))
        {
            // otherwise attempt to create an instance if we have a className
            Class<?> desiredClass = null;
            try
            {
                desiredClass = AbstractProxy.getClassFromClassName(className);
            }
            catch (MessageException me)
            {
                // Type not found and don't want to use ASObject for the missing type.
                if (!(me.getCode().startsWith(MessageException.CODE_SERVER_RESOURCE_UNAVAILABLE)
                        && context.createASObjectForMissingType))
                {
                    throw me; // Rethrow.
                }
                // if we didn't rethrow the exception, the default ASObject will be created further down.
            }

            // Type exists. Create either default instance of desiredClass or an instance from a registered proxy.
            if (desiredClass != null)
            {
                proxy = PropertyProxyRegistry.getRegistry().getProxyAndRegister(desiredClass);
                if (proxy == null)
                {
                    object = ClassUtil.createDefaultInstance(desiredClass, null, true /*validate*/);
                }
                else
                {
                    object = proxy.createInstance(className); // Validation is performed in the proxy.
                }
            }
        }

        // if we still don't have an object, create an ASObject with what we have for className (can be null)
        if (object == null)
            object = createDefaultASObject(className);

        // if proxy wasn't created, create one based on the new instance.
        if (proxy == null)
            proxy = PropertyProxyRegistry.getProxyAndRegister(object);

        params[1] = proxy; //update the params array so that callers get the proxy

        return object;
    }

    /**
     * Internal use. Convenience method for creating an ASObject and assigning it a type
     * @param type named type for the ASObject or null
     * @return a new instance of ASObject
     *
     */
    protected ASObject createDefaultASObject(String type)
    {
        ASObject object = (ASObject) ClassUtil.createDefaultInstance(ASObject.class, null, true /*validate*/);
        if (type != null && type.length() > 0)
            object.setType(type);
        return object;
    }

    /**
     *
     */
    protected void readMaxStringBytes()
    {
        // See if a JVM option is specified for max-string-bytes.
        String maxStringBytes = null;
        try
        {
            maxStringBytes = System.getProperty(STRING_MAX_STRING_BYTES);
        }
        catch (SecurityException se)
        {
            // Ignore and use the default.
        }

        if (maxStringBytes == null)
            return;

        try
        {
            this.maxStringBytes = Integer.parseInt(maxStringBytes);
        }
        catch (NumberFormatException ignore)
        {
            // Ignore and use the default.
        }
    }

    //
    // java.io.ObjectInput IMPLEMENTATIONS
    //

    /** {@inheritDoc} */
    public int available() throws IOException
    {
        return in.available();
    }

    /** {@inheritDoc} */
    public void close() throws IOException
    {
        in.close();
    }

    /** {@inheritDoc} */
    public int read() throws IOException
    {
        return in.read();
    }

    /** {@inheritDoc} */
    public int read(byte[] bytes) throws IOException
    {
        return in.read(bytes);
    }

    /** {@inheritDoc} */
    public int read(byte[] bytes, int offset, int length) throws IOException
    {
        return in.read(bytes, offset, length);
    }

    /** {@inheritDoc} */
    public long skip(long n) throws IOException
    {
        return in.skip(n);
    }

    /** {@inheritDoc} */
    public int skipBytes(int n) throws IOException
    {
        return in.skipBytes(n);
    }

    //
    // java.io.DataInput IMPLEMENTATIONS
    //

    /** {@inheritDoc} */
    public boolean readBoolean() throws IOException
    {
        return in.readBoolean();
    }

    /** {@inheritDoc} */
    public byte readByte() throws IOException
    {
        return in.readByte();
    }

    /** {@inheritDoc} */
    public char readChar() throws IOException
    {
        return in.readChar();
    }

    /** {@inheritDoc} */
    public double readDouble() throws IOException
    {
        return in.readDouble();
    }

    /** {@inheritDoc} */
    public float readFloat() throws IOException
    {
        return in.readFloat();
    }

    /** {@inheritDoc} */
    public void readFully(byte[] bytes) throws IOException
    {
        in.readFully(bytes);
    }

    /** {@inheritDoc} */
    public void readFully(byte[] bytes, int offset, int length) throws IOException
    {
        in.readFully(bytes, offset, length);
    }

    /** {@inheritDoc} */
    public int readInt() throws IOException
    {
        return in.readInt();
    }

    /**
     *  Reads the next line of text from the input stream.
     * @deprecated
     */
    public String readLine() throws IOException
    {
        return in.readLine();
    }

    /** {@inheritDoc} */
    public long readLong() throws IOException
    {
        return in.readLong();
    }

    /** {@inheritDoc} */
    public short readShort() throws IOException
    {
        return in.readShort();
    }

    /** {@inheritDoc} */
    public int readUnsignedByte() throws IOException
    {
        return in.readUnsignedByte();
    }

    /** {@inheritDoc} */
    public int readUnsignedShort() throws IOException
    {
        return in.readUnsignedShort();
    }

    /** {@inheritDoc} */
    public String readUTF() throws IOException
    {
        return in.readUTF();
    }
}