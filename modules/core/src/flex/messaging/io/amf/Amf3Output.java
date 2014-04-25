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

import flex.messaging.MessageException;
import flex.messaging.io.ArrayCollection;
import flex.messaging.io.BeanProxy;
import flex.messaging.io.PagedRowSet;
import flex.messaging.io.PropertyProxy;
import flex.messaging.io.PropertyProxyRegistry;
import flex.messaging.io.SerializationContext;
import flex.messaging.io.SerializationDescriptor;
import flex.messaging.io.StatusInfoProxy;
import flex.messaging.io.amf.AmfTrace.VectorType;
import flex.messaging.util.Trace;
import org.w3c.dom.Document;

import javax.sql.RowSet;
import java.io.Externalizable;
import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
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

/**
 * Serializes data to an output stream using the new
 * AMF 3 format.
 * <p>
 * This class intends to match the Flash Player 8 C++ code
 * in avmglue/DataIO.cpp
 * </p>
 *
 * @author Peter Farland
 * @exclude
 */
public class Amf3Output extends AbstractAmfOutput implements Amf3Types
{
    /**
     * @exclude
     */
    protected IdentityHashMap<Object, Integer> objectTable;

    /**
     * @exclude
     */
    protected HashMap<TraitsInfo, Integer> traitsTable;

    /**
     * @exclude
     */
    protected HashMap<String, Integer> stringTable;

    public Amf3Output(SerializationContext context)
    {
        super(context);
        context.supportDatesByReference = true;
    }

    public void reset()
    {
        super.reset();
        if (objectTable != null)
            objectTable.clear();
        if (traitsTable != null)
            traitsTable.clear();
        if (stringTable != null)
            stringTable.clear();
    }

    //
    // java.io.ObjectOutput IMPLEMENTATIONS
    //

    /**
     * Serialize an Object using AMF 3.
     * @param o the object to write
     * @throws IOException if the write failed
     */
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
            writeAMFString(s);
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
                writeAMFString(((Number)o).toString());
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
            writeAMFDate((Date)o);
        }
        else if (o instanceof Calendar)
        {
            writeAMFDate(((Calendar)o).getTime());
        }
        else if (o instanceof Document)
        {
            if (context.legacyXMLDocument)
                out.write(kXMLType); // Legacy flash.xml.XMLDocument Type
            else
                out.write(kAvmPlusXmlType); // New E4X XML Type
            if (!byReference(o))
            {
                String xml = documentToString(o);
                if (isDebug)
                    trace.write(xml);

                writeAMFUTF(xml);
            }
        }
        // If there is a proxy for this,write it as a custom object so the default
        // behavior can be overriden.
        else if (o instanceof Enum && PropertyProxyRegistry.getRegistry().getProxy(o.getClass()) == null)
        {
            Enum<?> enumValue = (Enum<?>)o;
            writeAMFString(enumValue.name());
        }
        else
        {
            // We have an Object or Array type...
            Class cls = o.getClass();

            if (context.legacyMap && o instanceof Map && !(o instanceof ASObject))
            {
                writeMapAsECMAArray((Map)o);
            }
            else if (!context.legacyDictionary && o instanceof Dictionary)
            {
                writeDictionary((Dictionary)o);
            }
            else if (o instanceof Collection)
            {
                if (o instanceof List && context.preferVectors)
                    writeListAsTypedVector((List)o);
                else if (context.legacyCollection)
                    writeCollection((Collection)o, null);
                else
                    writeArrayCollection((Collection)o, null);
            }
            else if (cls.isArray())
            {
                Class<?> componentType = cls.getComponentType();
                // Convert to vector if requested, except for character and byte arrays
                if (context.preferVectors &&
                        !(componentType.equals(Byte.class) || componentType.equals(byte.class))&&
                        !(componentType.equals(Character.class) || componentType.equals(char.class)))
                    writeArrayAsTypedVector(o, componentType);
                else
                    writeAMFArray(o, componentType);
            }
            else
            {
                //Special Case: wrap RowSet in PageableRowSet for Serialization
                if (o instanceof RowSet)
                {
                    o = new PagedRowSet((RowSet)o, Integer.MAX_VALUE, false);
                }
                else if (context.legacyThrowable && o instanceof Throwable)
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

        if (isDebug)
        {
            if (ti.isExternalizable())
                trace.startExternalizableObject(className, getObjectTableSize());
            else
                trace.startAMFObject(className, getObjectTableSize());
        }

        if (!byReference(ti))
        {
            int count = 0;
            List propertyNames = null;
            boolean externalizable = ti.isExternalizable();

            if (!externalizable)
            {
                propertyNames = ti.getProperties();
                if (propertyNames != null)
                    count = propertyNames.size();
            }

            boolean dynamic = ti.isDynamic();

            writeUInt29(3 | (externalizable ? 4 : 0) | (dynamic ? 8 : 0) | (count << 4));
            writeStringWithoutType(className);

            if (!externalizable && propertyNames != null)
            {
                for (int i = 0; i < count; i++)
                {
                    String propName = ti.getProperty(i);
                    writeStringWithoutType(propName);
                }
            }
        }
    }

    public void writeObjectProperty(String name, Object value) throws IOException
    {
        if (isDebug)
            trace.namedElement(name);
        increaseNestObjectLevel();
        writeObject(value);
        decreaseNestObjectLevel();
    }

    public void writeObjectEnd() throws IOException
    {
        // No action required for AMF 3

        if (isDebug)
            trace.endAMFObject();
    }

    //
    // AMF SPECIFIC SERIALIZATION IMPLEMENTATIONS
    //

    /**
     * @exclude
     */
    protected void writeAMFBoolean(boolean b) throws IOException
    {
        if (isDebug)
            trace.write(b);

        if (b)
            out.write(kTrueType);
        else
            out.write(kFalseType);
    }

    /**
     * @exclude
     */
    protected void writeAMFDate(Date d) throws IOException
    {
        out.write(kDateType);

        if (!byReference(d))
        {
            if (isDebug)
                trace.write(d);

            //Write out an invalid reference
            writeUInt29(1);

            // Write the time as 64bit value in ms
            out.writeDouble((double)d.getTime());
        }
    }

    /**
     * @exclude
     */
    protected void writeAMFDouble(double d) throws IOException
    {
        if (isDebug)
            trace.write(d);

        out.write(kDoubleType);
        out.writeDouble(d);
    }

    /**
     * @exclude
     */
    protected void writeAMFInt(int i) throws IOException
    {
        if (i >= INT28_MIN_VALUE && i <= INT28_MAX_VALUE)
        {
            if (isDebug)
                trace.write(i);

            // We have to be careful when the MSB is set, as (value >> 3) will sign extend.
            // We know there are only 29-bits of precision, so truncate. This requires
            // similar care when reading an integer.
            //i = ((i >> 3) & UINT29_MASK);
            i = i & UINT29_MASK; // Mask is 2^29 - 1
            out.write(kIntegerType);
            writeUInt29(i);
        }
        else
        {
            // Promote large int to a double
            writeAMFDouble(i);
        }
    }

    protected void writeDictionary(Dictionary dictionary) throws IOException
    {
        out.write(kDictionaryType);

        if (byReference(dictionary))
            return;

        writeUInt29((dictionary.size() << 1) | 1);
        writeAMFBoolean(false /*usingWeakKeys*/);

        if (isDebug) trace.startAMFDictionary(objectTable.size() - 1);

        Enumeration keys = dictionary.keys();
        while (keys.hasMoreElements())
        {
            if (isDebug) trace.startDictionaryElement();
            Object key = keys.nextElement();
            increaseNestObjectLevel();
            writeObject(key);
            decreaseNestObjectLevel();
            if (isDebug) trace.addDictionaryEquals();
            Object value = dictionary.get(key);
            increaseNestObjectLevel();
            writeObject(value);
            decreaseNestObjectLevel();
        }

        if (isDebug)
            trace.endAMFDictionary();
    }

    protected void writeArrayAsTypedVector(Object array, Class<?> componentType) throws IOException
    {
        int vecType = kTypedVectorObject;
        if (componentType.isPrimitive())
        {
            if (int.class.equals(componentType))
                vecType = kTypedVectorInt;
            else if (double.class.equals(componentType))
                vecType = kTypedVectorDouble;
        }
        else
        {
            if (Integer.class.equals(componentType))
                vecType = kTypedVectorInt;
            else if (Double.class.equals(componentType))
                vecType = kTypedVectorDouble;
        }

        out.write(vecType);

        if (byReference(array))
            return;

        int length = Array.getLength(array);
        writeUInt29((length << 1) | 1);
        writeBoolean(true /*fixed*/);

        switch (vecType)
        {
            case kTypedVectorInt:
                if (isDebug)
                    trace.startAMFVector(objectTable.size() - 1, VectorType.INT);

                for (int i = 0; i < length; i++)
                {
                    if (isDebug)
                        trace.arrayElement(i);

                    Object element = Array.get(array, i);
                    int value = ((Integer)element).intValue();

                    if (isDebug)
                        trace.write(value);

                    writeInt(value);
                }
                break;
            case kTypedVectorDouble:
                if (isDebug)
                    trace.startAMFVector(objectTable.size() - 1, VectorType.DOUBLE);

                for (int i = 0; i < length; i++)
                {
                    if (isDebug)
                        trace.arrayElement(i);

                    Object element = Array.get(array, i);
                    double value = ((Double)element).doubleValue();

                    if (isDebug)
                        trace.write(value);

                    writeDouble(value);
                }
                break;
            case kTypedVectorObject:
                if (isDebug)
                    trace.startAMFVector(objectTable.size() - 1, VectorType.OBJECT);

                // TODO - I don't think this className is used properly on the client currently.
                String className = componentType.getName();
                writeStringWithoutType(className);

                for (int i = 0; i < length; i++)
                {
                    if (isDebug)
                        trace.arrayElement(i);

                    Object element = Array.get(array, i);
                    increaseNestObjectLevel();
                    writeObject(element);
                    decreaseNestObjectLevel();
                }
                break;
            default:
                break;
        }

        if (isDebug)
            trace.endAMFVector();
    }

    protected void writeListAsTypedVector(List list) throws IOException
    {
        // Peek at the first three elements of the list to figure out what type
        // of Vector it should be sent as.
        int vecType = -1;
        Class<?> initialElementClass = null;
        int peekSize = Math.min(list.size(), 3);
        for (int i = 0; i < peekSize; i++)
        {
            Object element = list.get(i);
            if (i == 0)
            {
                initialElementClass = element != null? element.getClass() : null;
            }
            else
            {
                Class<?> currentElementClass = element != null? element.getClass() : null;
                if (initialElementClass != currentElementClass)
                {
                    vecType = kTypedVectorObject;
                    break;
                }
            }
        }

        if (vecType == -1)
        {
            if (initialElementClass == Integer.class)
                vecType = kTypedVectorInt;
            else if (initialElementClass == Double.class)
                vecType = kTypedVectorDouble;
            else
                vecType = kTypedVectorObject;

        }

        out.write(vecType);

        if (byReference(list))
            return;

        int length = list.size();
        writeUInt29((length << 1) | 1);
        writeBoolean(false /*fixed*/);

        switch (vecType)
        {
            case kTypedVectorInt:
                if (isDebug)
                    trace.startAMFVector(objectTable.size() - 1, VectorType.INT);

                for (int i = 0; i < length; i++)
                {
                    if (isDebug)
                        trace.arrayElement(i);

                    Object element = list.get(i);
                    int value = ((Integer)element).intValue();

                    if (isDebug)
                        trace.write(value);

                    writeInt(value);
                }
                break;
            case kTypedVectorDouble:
                if (isDebug)
                    trace.startAMFVector(objectTable.size() - 1, VectorType.DOUBLE);

                for (int i = 0; i < length; i++)
                {
                    if (isDebug)
                        trace.arrayElement(i);

                    Object element = list.get(i);
                    double value = ((Double)element).doubleValue();

                    if (isDebug)
                        trace.write(value);

                    writeDouble(value);
                }
                break;
            case kTypedVectorObject:
                if (isDebug)
                    trace.startAMFVector(objectTable.size() - 1, VectorType.OBJECT);

                // TODO - I don't think this className is used properly on the client currently.
                String className = initialElementClass != null? initialElementClass.getName() : "";
                writeStringWithoutType(className);

                for (int i = 0; i < length; i++)
                {
                    if (isDebug)
                        trace.arrayElement(i);

                    Object element = list.get(i);
                    increaseNestObjectLevel();
                    writeObject(element);
                    decreaseNestObjectLevel();
                }
                break;
            default:
                break;
        }

        if (isDebug)
            trace.endAMFVector();
    }

    /**
     * @exclude
     */
    protected void writeMapAsECMAArray(Map map) throws IOException
    {
        out.write(kArrayType);

        if (!byReference(map))
        {
            if (isDebug)
                trace.startECMAArray(getObjectTableSize());

            writeUInt29((0 << 1) | 1);

            Iterator it = map.keySet().iterator();
            while (it.hasNext())
            {
                Object key = it.next();
                if (key != null)
                {
                    String propName = key.toString();
                    writeStringWithoutType(propName);

                    if (isDebug)
                        trace.namedElement(propName);

                    increaseNestObjectLevel();
                    writeObject(map.get(key));
                    decreaseNestObjectLevel();
                }
            }

            writeStringWithoutType(EMPTY_STRING);

            if (isDebug)
                trace.endAMFArray();
        }
    }

    /**
     * @exclude
     */
    protected void writeAMFNull() throws IOException
    {
        if (isDebug)
            trace.writeNull();

        out.write(kNullType);
    }

    /**
     * @exclude
     */
    protected void writeAMFString(String s) throws IOException
    {
        out.write(kStringType);
        writeStringWithoutType(s);

        if (isDebug)
        {
            trace.writeString(s);
        }
    }

    /**
     * @exclude
     */
    protected void writeStringWithoutType(String s) throws IOException
    {
        if (s.length() == 0)
        {
            // don't create a reference for the empty string,
            // as it's represented by the one byte value 1
            // len = 0, ((len << 1) | 1).
            writeUInt29(1);
            return;
        }

        if (!byReference(s))
        {
            writeAMFUTF(s);
            return;
        }
    }

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
            writeAMFByteArray((Byte[])o);
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
        out.write(kObjectType);

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
            // or Collection or Map with legacyMap as true since we don't yet have
            // the ability to proxy multiple AMF types. We write an AMF Array directly
            // instead of an AMF Object...
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

        out.write(kObjectType);

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
    protected void writePropertyProxy(PropertyProxy proxy, Object instance) throws IOException
    {
        /*
         * At this point we substitute the instance we want to serialize.
         */
        Object newInst = proxy.getInstanceToSerialize(instance);
        if (newInst != instance)
        {
            // We can't use writeAMFNull here I think since we already added this object
            // to the object table on the server side.  The player won't have any way
            // of knowing we have this reference mapped to null.
            if (newInst == null)
                throw new MessageException("PropertyProxy.getInstanceToSerialize class: " + proxy.getClass() + " returned null for instance class: " + instance.getClass().getName());

            // Grab a new proxy if necessary for the new instance
            proxy = PropertyProxyRegistry.getProxyAndRegister(newInst);
            instance = newInst;
        }

        List propertyNames = null;
        boolean externalizable = proxy.isExternalizable(instance);

        if (!externalizable)
        {
            propertyNames = proxy.getPropertyNames(instance);
            // filter write-only properties
            if (proxy instanceof BeanProxy)
            {
                BeanProxy bp = (BeanProxy)proxy;
                if (propertyNames != null && !propertyNames.isEmpty())
                {
                    List<String> propertiesToRemove = null;
                    for (int i = 0; i < propertyNames.size(); i++)
                    {
                        String propName = (String)propertyNames.get(i);
                        if (bp.isWriteOnly(instance, propName))
                        {
                            if (propertiesToRemove == null)
                                propertiesToRemove = new ArrayList<String>();
                            propertiesToRemove.add(propName);
                        }
                    }
                    if (propertiesToRemove != null)
                        propertyNames.removeAll(propertiesToRemove);
                }
            }
        }


        TraitsInfo ti = new TraitsInfo(proxy.getAlias(instance), proxy.isDynamic(), externalizable, propertyNames);
        writeObjectTraits(ti);

        if (externalizable)
        {
            // Call user defined serialization
            ((Externalizable)instance).writeExternal(this);
        }
        else if (propertyNames != null && !propertyNames.isEmpty())
        {
            for (int i = 0; i < propertyNames.size(); i++)
            {
                String propName = (String)propertyNames.get(i);
                Object value = proxy.getValue(instance, propName);
                writeObjectProperty(propName, value);
            }
        }

        writeObjectEnd();
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
            writeAMFByteArray((byte[])obj);
        }
        else
        {
            out.write(kArrayType);

            if (!byReference(obj))
            {
                if (aType.equals(Boolean.TYPE))
                {
                    boolean[] b = (boolean[])obj;

                    // Write out an invalid reference, storing the length in the unused 28-bits.
                    writeUInt29((b.length << 1) | 1);

                    // Send an empty string to imply no named keys
                    writeStringWithoutType(EMPTY_STRING);

                    if (isDebug)
                    {
                        trace.startAMFArray(getObjectTableSize());

                        for (int i = 0; i < b.length; i++)
                        {
                            trace.arrayElement(i);
                            writeAMFBoolean(b[i]);
                        }

                        trace.endAMFArray();
                    }
                    else
                    {
                        for (int i = 0; i < b.length; i++)
                        {
                            writeAMFBoolean(b[i]);
                        }
                    }
                }
                else if (aType.equals(Integer.TYPE) || aType.equals(Short.TYPE))
                {
                    //We have a primitive number, either an int or short
                    //We write all of these as Integers...
                    int length = Array.getLength(obj);

                    // Write out an invalid reference, storing the length in the unused 28-bits.
                    writeUInt29((length << 1) | 1);
                    // Send an empty string to imply no named keys
                    writeStringWithoutType(EMPTY_STRING);

                    if (isDebug)
                    {
                        trace.startAMFArray(getObjectTableSize());

                        for (int i = 0; i < length; i++)
                        {
                            trace.arrayElement(i);
                            int v = Array.getInt(obj, i);
                            writeAMFInt(v);
                        }

                        trace.endAMFArray();
                    }
                    else
                    {
                        for (int i = 0; i < length; i++)
                        {
                            int v = Array.getInt(obj, i);
                            writeAMFInt(v);
                        }
                    }
                }
                else
                {
                    //We have a primitive number, either a double, float, or long
                    //We write all of these as doubles...
                    int length = Array.getLength(obj);

                    // Write out an invalid reference, storing the length in the unused 28-bits.
                    writeUInt29((length << 1) | 1);
                    // Send an empty string to imply no named keys
                    writeStringWithoutType(EMPTY_STRING);

                    if (isDebug)
                    {
                        trace.startAMFArray(getObjectTableSize());

                        for (int i = 0; i < length; i++)
                        {
                            trace.arrayElement(i);
                            double v = Array.getDouble(obj, i);
                            writeAMFDouble(v);
                        }

                        trace.endAMFArray();
                    }
                    else
                    {
                        for (int i = 0; i < length; i++)
                        {
                            double v = Array.getDouble(obj, i);
                            writeAMFDouble(v);
                        }
                    }
                }
            }
        }
    }

    /**
     * @exclude
     */
    protected void writeAMFByteArray(byte[] ba) throws IOException
    {
        out.write(kByteArrayType);

        if (!byReference(ba))
        {
            int length = ba.length;

            // Write out an invalid reference, storing the length in the unused 28-bits.
            writeUInt29((length << 1) | 1);

            if (isDebug)
            {
                trace.startByteArray(getObjectTableSize(), length);
            }

            out.write(ba, 0, length);
        }
    }

    /**
     * @exclude
     */
    protected void writeAMFByteArray(Byte[] ba) throws IOException
    {
        out.write(kByteArrayType);

        if (!byReference(ba))
        {
            int length = ba.length;

            // Write out an invalid reference, storing the length in the unused 28-bits.
            writeUInt29((length << 1) | 1);

            if (isDebug)
            {
                trace.startByteArray(getObjectTableSize(), length);
            }

            for (int i = 0; i < ba.length; i++)
            {
                Byte b = ba[i];
                if (b == null)
                    out.write(0);
                else
                    out.write(b.byteValue());
            }
        }
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
        writeAMFString(str);
    }

    /**
     * @exclude
     */
    protected void writeObjectArray(Object[] values, SerializationDescriptor descriptor) throws IOException
    {
        out.write(kArrayType);

        if (!byReference(values))
        {
            if (isDebug)
                trace.startAMFArray(getObjectTableSize());

            writeUInt29((values.length << 1) | 1);

            // Send an empty string to imply no named keys
            writeStringWithoutType(EMPTY_STRING);

            for (int i = 0; i < values.length; ++i)
            {
                if (isDebug)
                    trace.arrayElement(i);

                Object item = values[i];
                if (item != null && descriptor != null && !(item instanceof String)
                        && !(item instanceof Number) && !(item instanceof Boolean)
                        && !(item instanceof Character))
                {
                    PropertyProxy proxy = PropertyProxyRegistry.getProxy(item);
                    proxy = (PropertyProxy)proxy.clone();
                    proxy.setDescriptor(descriptor);
                    proxy.setDefaultInstance(item);
                    item = proxy;
                }
                increaseNestObjectLevel();
                writeObject(item);
                decreaseNestObjectLevel();
            }

            if (isDebug)
                trace.endAMFArray();
        }
    }

    /**
     * @exclude
     */
    protected void writeCollection(Collection c, SerializationDescriptor descriptor) throws IOException
    {
        out.write(kArrayType);

        // Note: We process Collections independently of Object[]
        // as we want the reference to be based on the actual
        // Collection.
        if (!byReference(c))
        {
            if (isDebug)
                trace.startAMFArray(getObjectTableSize());

            writeUInt29((c.size() << 1) | 1);

            // Send an empty string to imply no named keys
            writeStringWithoutType(EMPTY_STRING);

            Iterator it = c.iterator();
            int i = 0;
            while (it.hasNext())
            {
                if (isDebug)
                    trace.arrayElement(i);

                Object item = it.next();

                if (item != null && descriptor != null && !(item instanceof String)
                        && !(item instanceof Number) && !(item instanceof Boolean)
                        && !(item instanceof Character))
                {
                    PropertyProxy proxy = PropertyProxyRegistry.getProxy(item);
                    proxy = (PropertyProxy)proxy.clone();
                    proxy.setDescriptor(descriptor);
                    proxy.setDefaultInstance(item);
                    item = proxy;
                }
                increaseNestObjectLevel();
                writeObject(item);
                decreaseNestObjectLevel();

                i++;
            }

            if (isDebug)
                trace.endAMFArray();
        }
    }

    /**
     * @exclude
     */
    protected void writeUInt29(int ref) throws IOException
    {
        // Represent smaller integers with fewer bytes using the most
        // significant bit of each byte. The worst case uses 32-bits
        // to represent a 29-bit number, which is what we would have
        // done with no compression.

        // 0x00000000 - 0x0000007F : 0xxxxxxx
        // 0x00000080 - 0x00003FFF : 1xxxxxxx 0xxxxxxx
        // 0x00004000 - 0x001FFFFF : 1xxxxxxx 1xxxxxxx 0xxxxxxx
        // 0x00200000 - 0x3FFFFFFF : 1xxxxxxx 1xxxxxxx 1xxxxxxx xxxxxxxx
        // 0x40000000 - 0xFFFFFFFF : throw range exception
        if (ref < 0x80)
        {
            // 0x00000000 - 0x0000007F : 0xxxxxxx
            out.writeByte(ref);
        }
        else if (ref < 0x4000)
        {
            // 0x00000080 - 0x00003FFF : 1xxxxxxx 0xxxxxxx
            out.writeByte(((ref >> 7) & 0x7F) | 0x80);
            out.writeByte(ref & 0x7F);

        }
        else if (ref < 0x200000)
        {
            // 0x00004000 - 0x001FFFFF : 1xxxxxxx 1xxxxxxx 0xxxxxxx
            out.writeByte(((ref >> 14) & 0x7F) | 0x80);
            out.writeByte(((ref >> 7) & 0x7F) | 0x80);
            out.writeByte(ref & 0x7F);

        }
        else if (ref < 0x40000000)
        {
            // 0x00200000 - 0x3FFFFFFF : 1xxxxxxx 1xxxxxxx 1xxxxxxx xxxxxxxx
            out.writeByte(((ref >> 22) & 0x7F) | 0x80);
            out.writeByte(((ref >> 15) & 0x7F) | 0x80);
            out.writeByte(((ref >> 8) & 0x7F) | 0x80);
            out.writeByte(ref & 0xFF);

        }
        else
        {
            // 0x40000000 - 0xFFFFFFFF : throw range exception
            throw new MessageException("Integer out of range: " + ref);
        }
    }

    /**
     * @exclude
     */
    public void writeAMFUTF(String s) throws IOException
    {
        int strlen = s.length();
        int utflen = 0;
        int c, count = 0;

        char[] charr = getTempCharArray(strlen);
        s.getChars(0, strlen, charr, 0);

        for (int i = 0; i < strlen; i++)
        {
            c = charr[i];
            if (c <= 0x007F)
            {
                utflen++;
            }
            else if (c > 0x07FF)
            {
                utflen += 3;
            }
            else
            {
                utflen += 2;
            }
        }

        writeUInt29((utflen << 1) | 1);

        byte[] bytearr = getTempByteArray(utflen);

        for (int i = 0; i < strlen; i++)
        {
            c = charr[i];
            if (c <= 0x007F)
            {
                bytearr[count++] = (byte)c;
            }
            else if (c > 0x07FF)
            {
                bytearr[count++] = (byte)(0xE0 | ((c >> 12) & 0x0F));
                bytearr[count++] = (byte)(0x80 | ((c >> 6) & 0x3F));
                bytearr[count++] = (byte)(0x80 | ((c >> 0) & 0x3F));
            }
            else
            {
                bytearr[count++] = (byte)(0xC0 | ((c >> 6) & 0x1F));
                bytearr[count++] = (byte)(0x80 | ((c >> 0) & 0x3F));
            }
        }
        out.write(bytearr, 0, utflen);
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
        if (objectTable != null && objectTable.containsKey(o))
        {
            try
            {
                int refNum = objectTable.get(o).intValue();

                if (isDebug)
                    trace.writeRef(refNum);

                writeUInt29(refNum << 1);

                return true;
            }
            catch (ClassCastException e)
            {
                throw new IOException("Object reference is not an Integer");
            }
        }

        if (objectTable == null)
            objectTable = new IdentityHashMap<Object, Integer>(64);
        objectTable.put(o, Integer.valueOf(objectTable.size()));
        return false;
    }

    /**
     * @exclude
     */
    public void addObjectReference(Object o) throws IOException
    {
        byReference(o);
    }

    /**
     * @exclude
     */
    protected boolean byReference(String s) throws IOException
    {
        if (stringTable != null && stringTable.containsKey(s))
        {
            try
            {
                int refNum = stringTable.get(s).intValue();

                writeUInt29(refNum << 1);

                if (isDebug && Trace.amf)
                    trace.writeStringRef(refNum);
                return true;
            }
            catch (ClassCastException e)
            {
                throw new IOException("String reference is not an Integer");
            }
        }
        if (stringTable == null)
            stringTable = new HashMap<String, Integer>(64);
        stringTable.put(s, Integer.valueOf(stringTable.size()));
        return false;
    }

    /**
     * @exclude
     */
    protected boolean byReference(TraitsInfo ti) throws IOException
    {
        if (traitsTable != null && traitsTable.containsKey(ti))
        {
            try
            {
                int refNum = traitsTable.get(ti).intValue();

                writeUInt29((refNum << 2) | 1);

                if (isDebug && Trace.amf)
                    trace.writeTraitsInfoRef(refNum);
                return true;
            }
            catch (ClassCastException e)
            {
                throw new IOException("TraitsInfo reference is not an Integer");
            }
        }
        if (traitsTable == null)
            traitsTable = new HashMap<TraitsInfo, Integer>(10);
        traitsTable.put(ti, Integer.valueOf(traitsTable.size()));
        return false;
    }

    protected int getObjectTableSize()
    {
        return objectTable != null? objectTable.size() - 1 : 0;
    }
}
