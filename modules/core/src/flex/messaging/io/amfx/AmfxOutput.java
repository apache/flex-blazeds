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
package flex.messaging.io.amfx;

import flex.messaging.MessageException;

import flex.messaging.io.amf.ASObject;
import flex.messaging.io.amf.AbstractAmfOutput;
import flex.messaging.io.amf.Amf3Types;
import flex.messaging.io.PagedRowSet;
import flex.messaging.io.PropertyProxy;
import flex.messaging.io.PropertyProxyRegistry;
import flex.messaging.io.SerializationContext;
import flex.messaging.io.SerializationDescriptor;
import flex.messaging.io.StatusInfoProxy;
import flex.messaging.io.amf.TraitsInfo;
import flex.messaging.io.amf.Amf3Output;
import flex.messaging.io.ArrayCollection;
import flex.messaging.io.BeanProxy;
import flex.messaging.util.Hex;
import flex.messaging.util.Trace;

import org.w3c.dom.Document;

import java.io.IOException;
import java.io.Externalizable;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.sql.RowSet;

/**
 * Serializes Java types to ActionScript 3 types via AMFX, an XML
 * based representation of AMF 3.
 * <p>
 * XML is formatted using using UTF-8 encoding.
 * </p>
 *
 * @see AmfxMessageSerializer
 * @see AmfxInput
 */
public class AmfxOutput extends AbstractAmfOutput implements AmfxTypes
{
    /**
     * A mapping of object instances to their serialization numbers
     * for storing object references on the stream.
     */
    protected IdentityHashMap objectTable;
    protected HashMap traitsTable;
    protected HashMap stringTable;

    public AmfxOutput(SerializationContext context)
    {
        super(context);

        objectTable = new IdentityHashMap(64);
        traitsTable = new HashMap(10);
        stringTable = new HashMap(64);
    }

    public void reset()
    {
        super.reset();
        objectTable.clear();
        traitsTable.clear();
        stringTable.clear();
    }

    /**
     * Creates a new Amf3Output instance which is initialized with the
     * current SerializationContext, OutputStream and debug trace settings
     * to switch the version of the AMF protocol mid-stream.
     */
    protected Amf3Output createAMF3Output()
    {
        return new Amf3Output(context);
    }

    //
    // java.io.ObjectOutput IMPLEMENTATIONS
    //

    public void writeObject(Object o) throws IOException
    {
        if (o == null)
        {
            writeAMFNull();
            return;
        }

        if (!context.legacyExternalizable && o instanceof Externalizable)
        {
            writeCustomObject(o);
        }
        else if (o instanceof String || o instanceof Character)
        {
            String s = o.toString();
            writeString(s);
        }
        else if (o instanceof Number)
        {
            if (o instanceof Integer || o instanceof Short || o instanceof Byte)
            {
                int i = ((Number)o).intValue();
                writeAMFInt(i);
            }
            else if (!context.legacyBigNumbers &&
                    (o instanceof BigInteger || o instanceof BigDecimal))
            {
                // Using double to write big numbers such as BigInteger or
                // BigDecimal can result in information loss so we write
                // them as String by default...
                writeString(((Number)o).toString());
            }
            else
            {
                double d = ((Number)o).doubleValue();
                writeAMFDouble(d);
            }
        }
        else if (o instanceof Boolean)
        {
            writeAMFBoolean(((Boolean)o).booleanValue());
        }
        // We have a complex type...
        else if (o instanceof Date)
        {
            writeDate((Date)o);
        }
        else if (o instanceof Calendar)
        {
            writeDate(((Calendar)o).getTime());
        }
        else if (o instanceof Document)
        {
            String xml = documentToString(o);

            int len = xml.length() + 15; // <xml>...</xml>

            StringBuffer sb = new StringBuffer(len);
            sb.append(XML_OPEN_TAG);
            writeEscapedString(sb, xml);
            sb.append(XML_CLOSE_TAG);

            writeUTF(sb);

            if (isDebug)
                trace.writeString(xml);
        }
        // If there is a proxy for this,write it as a custom object so the default
        // behavior can be overriden.
        else if (o instanceof Enum && PropertyProxyRegistry.getRegistry().getProxy(o.getClass()) == null)
        {
            Enum<?> enumValue = (Enum<?>)o;
            writeString(enumValue.name());
        }
        else
        {
            //We have an Object or Array type...
            Class cls = o.getClass();

            if (o instanceof Map && context.legacyMap && !(o instanceof ASObject))
            {
                writeMapAsECMAArray((Map)o);
            }
            else if (!context.legacyDictionary && o instanceof Dictionary)
            {
                writeDictionary((Dictionary)o);
            }
            else if (o instanceof Collection)
            {
                if (context.legacyCollection)
                    writeCollection((Collection)o, null);
                else
                    writeArrayCollection((Collection)o, null);
            }
            else if (cls.isArray())
            {
                writeAMFArray(o, cls.getComponentType());
            }
            else
            {
                //Special Case: wrap RowSet in PageableRowSet for Serialization
                if (o instanceof RowSet)
                {
                    o = new PagedRowSet((RowSet)o, Integer.MAX_VALUE, false);
                }
                else if (o instanceof Throwable && context.legacyThrowable)
                {
                    o = new StatusInfoProxy((Throwable)o);
                }

                writeCustomObject(o);
            }
        }
    }

    public void writeObjectTraits(TraitsInfo ti) throws IOException
    {
        String className = ti.getClassName();

        if (className == null || className.length() == 0)
        {
            writeUTF(OBJECT_OPEN_TAG);
        }
        else
        {
            int len = 127; // <object type="...">
            StringBuffer sb = new StringBuffer(len);
            sb.append("<").append(OBJECT_TYPE).append(" type=\"");
            sb.append(className);
            sb.append("\">");

            writeUTF(sb);
        }

        if (isDebug)
            trace.startAMFObject(className, objectTable.size() - 1);

        // We treat an empty anonymous Object as a special case
        // of <traits/> and thus do not serialize by reference.
        if (ti.length() == 0 && className == null)
        {
            writeUTF(EMPTY_TRAITS_TAG);
        }
        else if (!byReference(ti))
        {
            // We assume all Java objects are not dynamic
            //boolean dynamic = ti.isDynamic();
            if (ti.isExternalizable())
            {
                writeUTF(TRAITS_EXTERNALIZALBE_TAG);
            }
            else
            {
                int count = ti.getProperties().size();

                if (count <= 0)
                {
                    writeUTF(EMPTY_TRAITS_TAG);
                }
                else
                {
                    writeUTF(TRAITS_OPEN_TAG);

                    for (int i = 0; i < count; i++)
                    {
                        String propName = ti.getProperty(i);
                        writeString(propName, true);
                    }

                    writeUTF(TRAITS_CLOSE_TAG);
                }
            }
        }
    }


    public void writeObjectProperty(String name, Object value) throws IOException
    {
        if (isDebug)
            trace.namedElement(name);

        writeObject(value);
    }

    public void writeObjectEnd() throws IOException
    {
        writeUTF(OBJECT_CLOSE_TAG);

        if (isDebug)
            trace.endAMFObject();
    }

    //
    // java.io.DataOutput IMPLEMENTATIONS
    //

    public void writeUTF(String s) throws IOException
    {
        byte[] bytes = s.getBytes(UTF_8);
        out.write(bytes);
    }

    //
    // AMF SPECIFIC SERIALIZATION IMPLEMENTATIONS
    //

    /**
     * @exclude
     */
    protected void writeAMFBoolean(boolean b) throws IOException
    {
        if (b)
            writeUTF(TRUE_TAG); // <true/>
        else
            writeUTF(FALSE_TAG); // <false/>

        if (isDebug)
            trace.write(b);
    }

    /**
     * @exclude
     */
    protected void writeAMFDouble(double d) throws IOException
    {
        int buflen = 40; // <double>...</double>
        StringBuffer sb = new StringBuffer(buflen);
        sb.append(DOUBLE_OPEN_TAG);
        sb.append(d);
        sb.append(DOUBLE_CLOSE_TAG);

        writeUTF(sb);

        if (isDebug)
            trace.write(d);
    }

    /**
     * @exclude
     */
    protected void writeAMFInt(int i) throws IOException
    {
        if (i >= Amf3Types.INT28_MIN_VALUE && i <= Amf3Types.INT28_MAX_VALUE)
        {
            int buflen = 25; // <int>...</int>
            StringBuffer sb = new StringBuffer(buflen);
            sb.append(INTEGER_OPEN_TAG);
            sb.append(i);
            sb.append(INTEGER_CLOSE_TAG);

            writeUTF(sb);

            if (isDebug)
                trace.write(i);
        }
        else
        {
            // Promote large int to a double; technically not needed for AMFX
            // but doing it for consistency with AMF.
            writeAMFDouble(i);
        }
    }

    /**
     * @exclude
     */
    protected void writeByteArray(byte[] ba) throws IOException
    {
        int length = ba.length * 2;

        int len = 23 + length; // <bytearray>number of bytes * 2 for encoding</bytearray>
        StringBuffer sb = new StringBuffer(len);
        sb.append(BYTE_ARRAY_OPEN_TAG);
        writeUTF(sb);

        Hex.Encoder encoder = new Hex.Encoder(ba.length * 2);
        encoder.encode(ba);
        String encoded = encoder.drain();
        writeUTF(encoded);

        writeUTF(BYTE_ARRAY_CLOSE_TAG);

        if (isDebug)
            trace.startByteArray(objectTable.size() - 1, ba.length);
    }

    /**
     * @exclude
     */
    protected void writeByteArray(Byte[] ba) throws IOException
    {
        int length = ba.length;
        byte[] bytes = new byte[length];

        for (int i = 0; i < length; i++)
        {
            Byte b = ba[i];
            if (b == null)
                bytes[i] = 0;
            else
                bytes[i] = ba[i].byteValue();
        }

        writeByteArray(bytes);
    }

    /**
     * @exclude
     */
    public void writeUTF(StringBuffer sb) throws IOException
    {
        byte[] bytes = sb.toString().getBytes(UTF_8);
        out.write(bytes);
    }

    /**
     * @exclude
     */
    protected void writeDate(Date d) throws IOException
    {
        if (!byReference(d))
        {
            int buflen = 30; // <date>...</date>
            long time = d.getTime();
            StringBuffer sb = new StringBuffer(buflen);
            sb.append(DATE_OPEN_TAG);
            sb.append(time);
            sb.append(DATE_CLOSE_TAG);

            writeUTF(sb);

            if (isDebug)
                trace.write(d);
        }
    }

    protected void writeDictionary(Dictionary dictionary) throws IOException
    {
        StringBuffer sb = new StringBuffer(25);
        sb.append("<").append(DICTIONARY_TYPE).append(" length=\"");
        sb.append(dictionary.size());
        sb.append("\">");
        writeUTF(sb);

        if (isDebug) trace.startAMFDictionary(objectTable.size() - 1);

        Enumeration keys = dictionary.keys();
        while (keys.hasMoreElements())
        {
            if (isDebug) trace.startDictionaryElement();
            Object key = keys.nextElement();
            writeObject(key);
            if (isDebug) trace.addDictionaryEquals();
            Object value = dictionary.get(key);
            writeObject(value);
        }

        writeUTF(DICTIONARY_CLOSE_TAG);

        if (isDebug)
            trace.endAMFDictionary();
    }

    /**
     * @exclude
     */
    protected void writeMapAsECMAArray(Map map) throws IOException
    {
        int len = 20; // <array ecma="true">

        StringBuffer sb = new StringBuffer(len);
        sb.append("<").append(ARRAY_TYPE).append(" ecma=\"true\">");
        writeUTF(sb);

        if (isDebug)
            trace.startAMFArray(objectTable.size() - 1);

        Iterator it = map.keySet().iterator();
        while (it.hasNext())
        {
            Object key = it.next();
            if (key != null)
            {
                String propName = key.toString();
                sb = new StringBuffer();

                // For now, all keys will be named items
                sb.append("<").append(ITEM_TYPE).append(" name=\"").append(propName).append("\">");
                writeUTF(sb);

                if (isDebug)
                    trace.namedElement(propName);

                writeObject(map.get(key));

                writeUTF(ITEM_CLOSE_TAG);
            }
        }

        writeUTF(ARRAY_CLOSE_TAG);

        if (isDebug)
            trace.endAMFArray();
    }

    /**
     * @exclude
     */
    protected void writeAMFNull() throws IOException
    {
        writeUTF(NULL_TAG);

        if (isDebug)
            trace.writeNull();
    }

    /**
     * @exclude
     */
    protected void writeString(String s) throws IOException
    {
        writeString(s, false);

        if (isDebug)
            trace.writeString(s);
    }

    //
    // PRIVATE SERIALIZATION HELPER METHODS
    //

    /**
     * @exclude
     */
    protected void writeAMFArray(Object o, Class componentType) throws IOException
    {
        if (componentType.isPrimitive())
        {
            writePrimitiveArray(o);
        }
        else if (componentType.equals(Byte.class))
        {
            writeByteArray((Byte[])o);
        }
        else if (componentType.equals(Character.class))
        {
            writeCharArrayAsString((Character[])o);
        }
        else
        {
            writeObjectArray((Object[])o, null);
        }
    }

    /**
     * @exclude
     */
    protected void writeArrayCollection(Collection col, SerializationDescriptor desc) throws IOException
    {
        if (!byReference(col))
        {
            ArrayCollection ac;

            if (col instanceof ArrayCollection)
            {
                ac = (ArrayCollection)col;
                // TODO: QUESTION: Pete, ignoring the descriptor here... not sure if
                // we should modify the user's AC as that could cause corruption?
            }
            else
            {
                // Wrap any Collection in an ArrayCollection
                ac = new ArrayCollection(col);
                if (desc != null)
                    ac.setDescriptor(desc);
            }

            // Then wrap ArrayCollection in PropertyProxy for bean-like serialization
            PropertyProxy proxy = PropertyProxyRegistry.getProxy(ac);
            writePropertyProxy(proxy, ac);
        }
    }

    /**
     * @exclude
     */
    protected void writeCustomObject(Object o) throws IOException
    {
        PropertyProxy proxy = null;

        if (o instanceof PropertyProxy)
        {
            proxy = (PropertyProxy)o;
            o = proxy.getDefaultInstance();

            // The proxy may wrap a null default instance, if so, short circuit here.
            if (o == null)
            {
                writeAMFNull();
                return;
            }
            // HACK: Short circuit and unwrap if PropertyProxy is wrapping an Array
            // or Collection type since we don't yet have the ability to proxy multiple
            // AMF types. We write an AMF Array directly instead of an AMF Object
            else if (o instanceof Collection)
            {
                if (context.legacyCollection)
                    writeCollection((Collection)o, proxy.getDescriptor());
                else
                    writeArrayCollection((Collection)o, proxy.getDescriptor());
                return;
            }
            else if (o.getClass().isArray())
            {
                writeObjectArray((Object[])o, proxy.getDescriptor());
                return;
            }
            else if (context.legacyMap && o instanceof Map && !(o instanceof ASObject))
            {
                writeMapAsECMAArray((Map)o);
                return;
            }
        }

        if (!byReference(o))
        {
            if (proxy == null)
            {
                proxy = PropertyProxyRegistry.getProxyAndRegister(o);
            }

            writePropertyProxy(proxy, o);
        }
    }

    /**
     * @exclude
     */
    protected void writePropertyProxy(PropertyProxy pp, Object instance) throws IOException
    {
        /*
         * At this point we substitute the instance we want to serialize.
         */
        Object newInst = pp.getInstanceToSerialize(instance);
        if (newInst != instance)
        {
            // We can't use writeAMFNull here I think since we already added this object
            // to the object table on the server side.  The player won't have any way
            // of knowing we have this reference mapped to null.
            if (newInst == null)
                throw new MessageException("PropertyProxy.getInstanceToSerialize class: " + pp.getClass() + " returned null for instance class: " + instance.getClass().getName());

            // Grab a new proxy if necessary for the new instance
            pp = PropertyProxyRegistry.getProxyAndRegister(newInst);
            instance = newInst;
        }

        List propertyNames = null;
        boolean externalizable = pp.isExternalizable(instance);

        if (!externalizable)
        {
            propertyNames = pp.getPropertyNames(instance);
            // filter write-only properties
            if (pp instanceof BeanProxy)
            {
                BeanProxy bp = (BeanProxy) pp;
                Iterator it = propertyNames.iterator();
                while (it.hasNext())
                {
                    String propName = (String) it.next();
                    if (bp.isWriteOnly(instance, propName))
                        it.remove();
                }
            }
        }

        TraitsInfo ti = new TraitsInfo(pp.getAlias(instance), pp.isDynamic(), externalizable, propertyNames);
        writeObjectTraits(ti);

        if (externalizable)
        {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            Amf3Output objOut = createAMF3Output();
            objOut.setOutputStream(bout);
            //objOut.setDebugTrace(trace);
            ((Externalizable)instance).writeExternal(objOut);
            writeByteArray(bout.toByteArray());
        }
        else if (propertyNames != null)
        {
            Iterator it = propertyNames.iterator();
            while (it.hasNext())
            {
                String propName = (String)it.next();
                Object value = pp.getValue(instance, propName);
                writeObjectProperty(propName, value);
            }
        }

        writeObjectEnd();
    }

    /**
     * @exclude
     */
    protected void writeString(String s, boolean isTrait) throws IOException
    {
        if (s.length() == 0)
        {
            writeUTF(EMPTY_STRING_TAG);
        }
        else if (!byReference(s))
        {
            int len = s.length() + 35; // <string>...</string> + <![CDATA[ ]]>

            StringBuffer sb = new StringBuffer(len);
            sb.append(STRING_OPEN_TAG);

            // Traits won't contain chars that need escaping
            if (!isTrait)
                writeEscapedString(sb, s);
            else
                sb.append(s);

            sb.append(STRING_CLOSE_TAG);

            writeUTF(sb);
        }
    }

    /**
     * XML defines the following set as valid characters to appear in a document:
     * U+0009, U+000A, U+000D, [U+0020-U+D7FF], [U+E000-U+FFFD], and [U+10000-U+10FFFF].
     *
     * Java only supports characters up to 0xFFFE so codepoints beyond the BMP are
     * not considered.
     *
     * Characters not in this set will be escaped using a numerical character reference
     * in hexadecimal form, i.e. &amp;#xFFFF.
     *
     * A CDATA section is not used because numerical character references cannot be
     * used in such a context.
     *
     * @param sb The StringBuffer to which the escaped String should be written.
     * @param s The source String to escape for XML.
     * @exclude
     */
    protected void writeEscapedString(StringBuffer sb, String s)
    {
        StringBuffer temp = new StringBuffer(s.length());

        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++)
        {
            char c = chars[i];
            if (c >= 0x0020)
            {
                if (c == '&')
                {
                    temp.append("&amp;");
                }
                else if (c == '<')
                {
                    temp.append("&lt;");
                }
                else if (c > 0xD7FF && (c < 0xE000 || c > 0xFFFD))
                {
                    temp.append("&#x").append(Integer.toHexString(c)).append(";");
                }
                else
                {
                    temp.append(c);
                }
            }
            else if (c == 0x0009 || c == 0x000A || c == 0x000D)
            {
                temp.append(c);
            }
            else
            {
                temp.append("&#x").append(Integer.toHexString(c)).append(";");
            }
        }

        sb.append(temp); //Use temp.toString() if JDK 1.3 or earlier
    }

    /**
     * @exclude
     */
    protected void writeCharArrayAsString(Character[] ca) throws IOException
    {
        int length = ca.length;
        char[] chars = new char[length];

        for (int i = 0; i < length; i++)
        {
            Character c = ca[i];
            if (c == null)
                chars[i] = 0;
            else
                chars[i] = ca[i].charValue();
        }
        writeCharArrayAsString(chars);
    }

    /**
     * @exclude
     */
    protected void writeCharArrayAsString(char[] ca) throws IOException
    {
        String str = new String(ca);
        writeString(str);
    }

    /**
     * @exclude
     */
    protected void writeCollection(Collection c, SerializationDescriptor descriptor) throws IOException
    {
        if (!byReference(c))
        {
            writeObjectArrayDirectly(c.toArray(), descriptor);
        }
    }

    /**
     * @exclude
     */
    protected void writeObjectArray(Object[] values, SerializationDescriptor descriptor) throws IOException
    {
        if (!byReference(values))
        {
            writeObjectArrayDirectly(values, descriptor);
        }
    }

    /**
     * @exclude
     */
    protected void writeObjectArrayDirectly(Object[] values, SerializationDescriptor descriptor) throws IOException
    {
        int len = 25; // <array length="...">

        StringBuffer sb = new StringBuffer(len);
        sb.append("<").append(ARRAY_TYPE).append(" length=\"");
        sb.append(values.length);
        sb.append("\">");
        writeUTF(sb);

        if (isDebug)
            trace.startAMFArray(objectTable.size() - 1);

        for (int i = 0; i < values.length; ++i)
        {
            if (isDebug)
                trace.arrayElement(i);

            writeObject(values[i]);
        }

        writeUTF(ARRAY_CLOSE_TAG);

        if (isDebug)
            trace.endAMFArray();
    }


    /**
     * Serialize an array of primitives.
     * <p>
     * Primitives include the following:
     * boolean, char, double, float, long, int, short, byte
     * </p>
     *
     * @param obj An array of primitives
     * @exclude
     */
    protected void writePrimitiveArray(Object obj) throws IOException
    {
        Class aType = obj.getClass().getComponentType();

        if (aType.equals(Character.TYPE))
        {
            //Treat char[] as a String
            char[] c = (char[])obj;
            writeCharArrayAsString(c);
        }
        else if (aType.equals(Byte.TYPE))
        {
            writeByteArray((byte[])obj);
        }
        else if (!byReference(obj))
        {
            int length = Array.getLength(obj);

            int buflen = 25; // <array length="...">
            StringBuffer sb = new StringBuffer(buflen);
            sb.append("<").append(ARRAY_TYPE).append(" length=\"");
            sb.append(length);
            sb.append("\">");
            writeUTF(sb);

            if (isDebug)
                trace.startAMFArray(objectTable.size() - 1);

            if (aType.equals(Boolean.TYPE))
            {
                boolean[] b = (boolean[])obj;

                for (int i = 0; i < b.length; i++)
                {
                    if (isDebug)
                        trace.arrayElement(i);

                    writeAMFBoolean(b[i]);
                }
            }
            else if (aType.equals(Integer.TYPE) || aType.equals(Short.TYPE))
            {
                //We have a primitive number, either an int or short
                //We write all of these as Integers...
                for (int i = 0; i < length; i++)
                {
                    if (isDebug)
                        trace.arrayElement(i);

                    int v = Array.getInt(obj, i);
                    writeAMFInt(v);
                }
            }
            else
            {
                //We have a primitive number, either a double, float, or long
                //We write all of these as doubles...
                for (int i = 0; i < length; i++)
                {
                    if (isDebug)
                        trace.arrayElement(i);

                    double v = Array.getDouble(obj, i);
                    writeAMFDouble(v);
                }
            }

            writeUTF(ARRAY_CLOSE_TAG);

            if (isDebug)
                trace.endAMFArray();
        }
    }


    /**
     * Attempts to serialize the object as a reference.
     * If the object cannot be serialized as a reference, it is stored
     * in the reference collection for potential future encounter.
     *
     * @return Success/failure indicator as to whether the object could be
     *         serialized as a reference.
     * @exclude
     */
    protected boolean byReference(Object o) throws IOException
    {
        Object ref = objectTable.get(o);

        if (ref != null)
        {
            try
            {
                int refNum = ((Integer)ref).intValue();

                int len = 20; // <ref id="..."/>

                StringBuffer sb = new StringBuffer(len);
                sb.append("<").append(REF_TYPE).append(" id=\"");
                sb.append(refNum);
                sb.append("\"/>");

                writeUTF(sb);

                if (isDebug)
                    trace.writeRef(refNum);
            }
            catch (ClassCastException e)
            {
                throw new IOException("Object reference is not an Integer");
            }
        }
        else
        {
            objectTable.put(o, new Integer(objectTable.size()));
        }

        return (ref != null);
    }

    /**
     * @exclude
     */
    protected boolean byReference(String s) throws IOException
    {
        Object ref = stringTable.get(s);

        if (ref != null)
        {
            try
            {
                int refNum = ((Integer)ref).intValue();

                int len = 20; // <string id="..."/>

                StringBuffer sb = new StringBuffer(len);
                sb.append("<").append(STRING_TYPE).append(" id=\"");
                sb.append(refNum);
                sb.append("\"/>");

                writeUTF(sb);

                if (Trace.amf && isDebug)
                    trace.writeStringRef(refNum);
            }
            catch (ClassCastException e)
            {
                throw new IOException("String reference is not an Integer");
            }
        }
        else
        {
            stringTable.put(s, new Integer(stringTable.size()));
        }

        return (ref != null);
    }

    /**
     * @exclude
     */
    protected boolean byReference(TraitsInfo ti) throws IOException
    {
        // We treat an empty anonymous Object as a special case
        // of <traits/> and thus do not serialize by reference.
        if (ti.length() == 0 && ti.getClassName() == null)
            return false;

        Object ref = traitsTable.get(ti);

        if (ref != null)
        {
            try
            {
                int refNum = ((Integer)ref).intValue();

                int len = 20; // <traits id="..."/>

                StringBuffer sb = new StringBuffer(len);
                sb.append("<").append(TRAITS_TYPE).append(" id=\"");
                sb.append(refNum);
                sb.append("\"/>");

                writeUTF(sb);

                if (Trace.amf && isDebug)
                    trace.writeTraitsInfoRef(refNum);
            }
            catch (ClassCastException e)
            {
                throw new IOException("Traits reference is not an Integer");
            }
        }
        else
        {
            traitsTable.put(ti, new Integer(traitsTable.size()));
        }

        return (ref != null);
    }
}
