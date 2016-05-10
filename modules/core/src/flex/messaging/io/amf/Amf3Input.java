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

import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import flex.messaging.io.PropertyProxy;
import flex.messaging.io.SerializationContext;
import flex.messaging.io.SerializationException;
import flex.messaging.io.UnknownTypeException;
import flex.messaging.io.amf.AmfTrace.VectorType;
import flex.messaging.util.ClassUtil;
import flex.messaging.util.Trace;

/**
 * Reads AMF 3 formatted data stream.
 * <p>
 * This class intends to matches the Flash Player 8 C++ code
 * in avmglue/DataIO.cpp
 * </p>
 *
 *
 */
public class Amf3Input extends AbstractAmfInput implements Amf3Types
{
    /**
     *
     */
    protected List objectTable;

    /**
     *
     */
    protected List stringTable;

    /**
     *
     */
    protected List traitsTable;

    public Amf3Input(SerializationContext context)
    {
        super(context);

        stringTable = new ArrayList(64);
        objectTable = new ArrayList(64);
        traitsTable = new ArrayList(10);
    }

    /**
     * Reset should be called before reading a top level object,
     * such as a new header or a new body.
     */
    @Override
    public void reset()
    {
        super.reset();
        stringTable.clear();
        objectTable.clear();
        traitsTable.clear();
    }

    public Object saveObjectTable()
    {
        Object table = objectTable;
        objectTable = new ArrayList(64);
        return table;
    }

    public void restoreObjectTable(Object table)
    {
        objectTable = (ArrayList) table;
    }

    public Object saveTraitsTable()
    {
        Object table = traitsTable;
        traitsTable = new ArrayList(10);
        return table;
    }

    public void restoreTraitsTable(Object table)
    {
        traitsTable = (ArrayList) table;
    }

    public Object saveStringTable()
    {
        Object table = stringTable;
        stringTable = new ArrayList(64);
        return table;
    }

    public void restoreStringTable(Object table)
    {
        stringTable = (ArrayList) table;
    }

    /**
     * Public entry point to read a top level AMF Object, such as
     * a header value or a message body.
     * @return Object the object read
     * @throws ClassNotFoundException, IOException when reading object process failed
     */
    public Object readObject() throws ClassNotFoundException, IOException
    {
        int type = in.readByte();
        Object value = readObjectValue(type);
        return value;
    }

    /**
     *
     */
    protected Object readObjectValue(int type) throws ClassNotFoundException, IOException
    {
        Object value = null;

        switch (type)
        {
            case kStringType:
                ClassUtil.validateCreation(String.class);

                value = readString();
                if (isDebug)
                    trace.writeString((String)value);
                break;

            case kObjectType:
                value = readScriptObject();
                break;

            case kArrayType:
                value = readArray();
                break;

            case kFalseType:
                ClassUtil.validateCreation(Boolean.class);

                value = Boolean.FALSE;

                if (isDebug)
                    trace.write(value);
                break;

            case kTrueType:
                ClassUtil.validateCreation(Boolean.class);

                value = Boolean.TRUE;

                if (isDebug)
                    trace.write(value);
                break;

            case kIntegerType:
                ClassUtil.validateCreation(Integer.class);

                int i = readUInt29();
                // Symmetric with writing an integer to fix sign bits for negative values...
                i = (i << 3) >> 3;
                value = new Integer(i);

                if (isDebug)
                    trace.write(value);
                break;

                case kDoubleType:
                    value = Double.valueOf(readDouble());
                    break;

                case kUndefinedType:
                    if (isDebug)
                        trace.writeUndefined();
                    break;

                case kNullType:
                    if (isDebug)
                        trace.writeNull();
                    break;

                case kXMLType:
                case kAvmPlusXmlType:
                    value = readXml();
                    break;

                case kDateType:
                    value = readDate();
                    break;

                case kByteArrayType:
                    value = readByteArray();
                    break;

                case kDictionaryType:
                    value = readDictionary();
                    break;

                case kTypedVectorInt:
                case kTypedVectorUint:
                case kTypedVectorDouble:
                case kTypedVectorObject:
                    value = readTypedVector(type);
                    break;
                default:
                    // Unknown object type tag {type}
                    UnknownTypeException ex = new UnknownTypeException();
                    ex.setMessage(10301, new Object[]{new Integer(type)});
                    throw ex;
        }

        return value;
    }

    /** {@inheritDoc} */
    @Override
    public double readDouble() throws IOException
    {
        ClassUtil.validateCreation(Double.class);

        double d = super.readDouble();
        if (isDebug)
            trace.write(d);
        return d;
    }

    /**
     *
     */
    protected String readString() throws IOException
    {
        int ref = readUInt29();
        if ((ref & 1) == 0) // This is a reference
            return getStringReference(ref >> 1);

        int len = (ref >> 1); // Read the string in

        // writeString() special cases the empty string
        // to avoid creating a reference.
        if (0 == len)
            return EMPTY_STRING;

        String str = readUTF(len);

        stringTable.add(str); // Remember String

        return str;
    }

    /**
     * Deserialize the bits of a date-time value w/o a prefixing type byte.
     */
    protected Date readDate() throws IOException
    {
        ClassUtil.validateCreation(Date.class);

        int ref = readUInt29();
        if ((ref & 1) == 0) // This is a reference
            return (Date)getObjectReference(ref >> 1);

        long time = (long)in.readDouble();

        Date d = new Date(time);

        objectTable.add(d); //Remember Date

        if (isDebug)
            trace.write(d);

        return d;
    }

    protected Object readDictionary() throws IOException, ClassNotFoundException
    {
        int ref = readUInt29();

        if ((ref & 1) == 0) // This is a reference.
            return getObjectReference(ref >> 1);

        readBoolean(); // usingWeakTypes - irrelevant in Java.
        int len = (ref >> 1);

        Dictionary dictionary = (Hashtable)ClassUtil.createDefaultInstance(Hashtable.class, null, true /*validate*/);

        objectTable.add(dictionary); // Remember the object.

        if (isDebug)
            trace.startAMFDictionary(objectTable.size() - 1);

        for (int i = 0; i < len; i++)
        {
            if (isDebug) trace.startDictionaryElement();
            Object key = readObjectOneLevelDown(true);

            if (isDebug) trace.addDictionaryEquals();
            Object value = readObjectOneLevelDown(true);
            ClassUtil.validateAssignment(dictionary, key != null? key.toString() : null, value);
            dictionary.put(key, value);
        }

        if (isDebug)
            trace.endAMFDictionary();

        return dictionary;
    }

    protected Object readTypedVector(int type) throws IOException, ClassNotFoundException
    {
        int ref = readUInt29();

        if ((ref & 1) == 0) // This is a reference.
            return getObjectReference(ref >> 1);

        int len = (ref >> 1);
        boolean fixed = readBoolean();

        Object vector = null;
        switch (type)
        {
            case kTypedVectorInt:
                vector = readTypedIntVector(len, fixed);
                break;
            case kTypedVectorUint:
                vector = readTypedUintVector(len, fixed);
                break;
            case kTypedVectorDouble:
                vector = readTypedDoubleVector(len, fixed);
                break;
            case kTypedVectorObject:
                vector = readTypedObjectVector(len, fixed);
                break;
            default:
                // Unknown object type tag {type}
                UnknownTypeException ex = new UnknownTypeException();
                ex.setMessage(10301, new Object[]{Integer.valueOf(type)});
                throw ex;
        }
        return vector;
    }

    @SuppressWarnings("unchecked")
    protected Object readTypedIntVector(int len, boolean fixed) throws IOException
    {
        // Don't instantiate Array right away with the supplied size if it is more
        // than INITIAL_ARRAY_CAPACITY in case the supplied size has been tampered.
        boolean useListTemporarily = false;

        Object vector;
        if (fixed)
        {
            useListTemporarily = len > INITIAL_COLLECTION_CAPACITY;
            if (useListTemporarily)
            {
                ClassUtil.validateCreation(ArrayList.class);
                vector = new ArrayList<Integer>(INITIAL_COLLECTION_CAPACITY);
            }
            else
            {
                ClassUtil.validateCreation(Integer[].class);
                vector = new Integer[len];
            }
        }
        else
        {
            ClassUtil.validateCreation(ArrayList.class);
            int initialCapacity = len < INITIAL_COLLECTION_CAPACITY? len : INITIAL_COLLECTION_CAPACITY;
            vector = new ArrayList<Integer>(initialCapacity);
        }

        int objectId = rememberObject(vector);

        if (isDebug)
            trace.startAMFVector(objectTable.size() - 1, VectorType.INT);

        for (int i = 0; i < len; i++)
        {
            if (isDebug)
                trace.arrayElement(i);

            ClassUtil.validateCreation(Integer.class);
            int value = readInt();

            if (isDebug)
                trace.write(value);

            Integer item = Integer.valueOf(value);

            ClassUtil.validateAssignment(vector, i, item);
            if (vector instanceof Integer[])
                Array.set(vector, i, item);
            else
                ((List<Integer>)vector).add(item);
        }

        if (useListTemporarily)
        {
            vector = ((ArrayList<Integer>)vector).toArray();
            objectTable.set(objectId, vector);
        }

        if (isDebug)
            trace.endAMFVector();

        return vector;
    }

    @SuppressWarnings("unchecked")
    protected Object readTypedUintVector(int len, boolean fixed) throws IOException
    {
        // Don't instantiate Array right away with the supplied size if it is more
        // than INITIAL_ARRAY_CAPACITY in case the supplied size has been tampered.
        boolean useListTemporarily = false;

        Object vector;
        if (fixed)
        {
            useListTemporarily = len > INITIAL_COLLECTION_CAPACITY;
            if (useListTemporarily)
            {
                ClassUtil.validateCreation(ArrayList.class);
                vector = new ArrayList<Long>(INITIAL_COLLECTION_CAPACITY);
            }
            else
            {
                ClassUtil.validateCreation(Long[].class);
                vector = new Long[len];
            }
        }
        else
        {
            ClassUtil.validateCreation(ArrayList.class);
            int initialCapacity = len < INITIAL_COLLECTION_CAPACITY? len : INITIAL_COLLECTION_CAPACITY;
            vector = new ArrayList<Long>(initialCapacity);
        }

        int objectId = rememberObject(vector);

        if (isDebug)
            trace.startAMFVector(objectTable.size() - 1, VectorType.UINT);

        for (int i = 0; i < len; i++)
        {
            if (isDebug)
                trace.arrayElement(i);

            ClassUtil.validateCreation(Long.class);

            long value = (long) (in.readByte() & 0xFF) << 24L;
            value += (long) (in.readByte() & 0xFF) << 16L;
            value += (long) (in.readByte() & 0xFF) << 8L;
            value += (in.readByte() & 0xFF);

            if (isDebug)
                trace.write(value);

            Long item = Long.valueOf(value);

            ClassUtil.validateAssignment(vector, i, item);
            if (vector instanceof Long[])
                Array.set(vector, i, item);
            else
                ((List<Long>)vector).add(item);
        }

        if (useListTemporarily)
        {
            vector = ((ArrayList<Long>)vector).toArray();
            objectTable.set(objectId, vector);
        }

        if (isDebug)
            trace.endAMFVector();

        return vector;
    }

    @SuppressWarnings("unchecked")
    protected Object readTypedDoubleVector(int len, boolean fixed) throws IOException
    {
        // Don't instantiate Array right away with the supplied size if it is more
        // than INITIAL_ARRAY_CAPACITY in case the supplied size has been tampered.
        boolean useListTemporarily = false;

        Object vector;
        if (fixed)
        {
            useListTemporarily = len > INITIAL_COLLECTION_CAPACITY;
            if (useListTemporarily)
            {
                ClassUtil.validateCreation(ArrayList.class);
                vector = new ArrayList<Double>(INITIAL_COLLECTION_CAPACITY);
            }
            else
            {
                ClassUtil.validateCreation(Double[].class);
                vector = new Double[len];
            }
        }
        else
        {
            ClassUtil.validateCreation(ArrayList.class);
            int initialCapacity = len < INITIAL_COLLECTION_CAPACITY? len : INITIAL_COLLECTION_CAPACITY;
            vector = new ArrayList<Double>(initialCapacity);
        }

        int objectId = rememberObject(vector);

        if (isDebug)
            trace.startAMFVector(objectTable.size() - 1, VectorType.DOUBLE);

        for (int i = 0; i < len; i++)
        {
            if (isDebug)
                trace.arrayElement(i);

            Double item = Double.valueOf(readDouble());

            ClassUtil.validateAssignment(vector, i, item);
            if (vector instanceof Double[])
                Array.set(vector, i, item);
            else
                ((List<Double>)vector).add(item);
        }

        if (useListTemporarily)
        {
            vector = ((ArrayList<Double>)vector).toArray();
            objectTable.set(objectId, vector);
        }

        if (isDebug)
            trace.endAMFVector();

        return vector;
    }

    @SuppressWarnings("unchecked")
    protected Object readTypedObjectVector(int len, boolean fixed) throws IOException, ClassNotFoundException
    {
        // TODO - Class name is always empty for some reason, need to figure out if this is expected.
        String className = readString();
        if (className == null || className.length() == 0 || className.equals(EMPTY_STRING))
            className = Object.class.getName();

        // Don't instantiate Array right away with the supplied size if it is more
        // than INITIAL_ARRAY_CAPACITY in case the supplied size has been tampered.
        boolean useListTemporarily = false;

        Object vector;
        if (fixed)
        {
            useListTemporarily = len > INITIAL_COLLECTION_CAPACITY;
            if (useListTemporarily)
            {
                ClassUtil.validateCreation(ArrayList.class);
                vector = new ArrayList<Object>(INITIAL_COLLECTION_CAPACITY);
            }
            else
            {
                ClassUtil.validateCreation(Object[].class);
                vector = new Object[len];
            }
        }
        else
        {
            ClassUtil.validateCreation(ArrayList.class);
            int initialCapacity = len < INITIAL_COLLECTION_CAPACITY? len : INITIAL_COLLECTION_CAPACITY;
            vector = new ArrayList<Object>(initialCapacity);
        }

        int objectId = rememberObject(vector);

        if (isDebug)
            trace.startAMFVector(objectTable.size() - 1, VectorType.OBJECT);

        for (int i = 0; i < len; i++)
        {
            if (isDebug)
                trace.arrayElement(i);
            Object item = readObjectOneLevelDown(true);
            ClassUtil.validateAssignment(vector, i, item);
            if (vector instanceof Object[])
                Array.set(vector, i, item);
            else
                ((List<Object>)vector).add(item);
        }

        if (useListTemporarily)
        {
            vector = ((ArrayList<Object>)vector).toArray();
            objectTable.set(objectId, vector);
        }

        if (isDebug)
            trace.endAMFVector();

        return vector;
    }

    /**
     *
     */
    protected Object readArray() throws ClassNotFoundException, IOException
    {
        int ref = readUInt29();

        if ((ref & 1) == 0) // This is a reference.
            return getObjectReference(ref >> 1);

        int len = (ref >> 1);
        Object array = null;

        // First, look for any string based keys. If any non-ordinal indices were used,
        // or if the Array is sparse, we represent the structure as a Map.
        Map map = null;
        for (; ;)
        {
            String name = readString();
            if (name == null || name.length() == 0)
                break;

            if (map == null)
            {
                map = (HashMap)ClassUtil.createDefaultInstance(HashMap.class, null, true /*validate*/);
                array = map;

                //Remember Object
                objectTable.add(array);

                if (isDebug)
                    trace.startECMAArray(objectTable.size() - 1);
            }

            if (isDebug)
                trace.namedElement(name);
            Object value = readObjectOneLevelDown(true);
            ClassUtil.validateAssignment(map, name, value);
            map.put(name, value);
        }

        // If we didn't find any string based keys, we have a dense Array, so we
        // represent the structure as a List.
        if (map == null)
        {
            // Don't instantiate List/Array right away with the supplied size if it is more than
            // INITIAL_ARRAY_CAPACITY in case the supplied size has been tampered. This at least
            // requires the user to pass in the actual objects for the List/Array to grow beyond.
            boolean useListTemporarily = false;

            // Legacy Flex 1.5 behavior was to return a java.util.Collection for Array
            if (context.legacyCollection || len > INITIAL_COLLECTION_CAPACITY)
            {
                useListTemporarily = !context.legacyCollection;
                ClassUtil.validateCreation(ArrayList.class);
                int initialCapacity = len < INITIAL_COLLECTION_CAPACITY? len : INITIAL_COLLECTION_CAPACITY;
                array = new ArrayList(initialCapacity);
            }
            // Flex 2+ behavior is to return Object[] for AS3 Array
            else
            {
                ClassUtil.validateCreation(Object[].class);
                array = new Object[len];
            }
            int objectId = rememberObject(array); // Remember the List/Object[].

            if (isDebug)
                trace.startAMFArray(objectTable.size() - 1);

            for (int i = 0; i < len; i++)
            {
                if (isDebug)
                    trace.arrayElement(i);
                Object item = readObjectOneLevelDown(true);
                ClassUtil.validateAssignment(array, i, item);
                if (array instanceof ArrayList)
                    ((ArrayList)array).add(item);
                else
                    Array.set(array, i, item);
            }

            if (useListTemporarily)
            {
                array = ((ArrayList)array).toArray();
                objectTable.set(objectId, array);
            }
        }
        else
        {
            for (int i = 0; i < len; i++)
            {
                if (isDebug)
                    trace.arrayElement(i);
                Object item = readObjectOneLevelDown(true);
                String key = Integer.toString(i);
                ClassUtil.validateAssignment(map, key, item);
                map.put(key, item);
            }
        }

        if (isDebug)
            trace.endAMFArray();

        return array;
    }

    /**
     *
     */
    protected Object readScriptObject() throws ClassNotFoundException, IOException
    {
        int ref = readUInt29();

        if ((ref & 1) == 0)
            return getObjectReference(ref >> 1);

        TraitsInfo ti = readTraits(ref);
        String className = ti.getClassName();
        boolean externalizable = ti.isExternalizable();

        // Prepare the parameters for createObjectInstance(). Use an array as a holder
        // to simulate two 'by-reference' parameters className and (initially null) proxy
        Object[] params = new Object[] {className, null};
        Object object = createObjectInstance(params);

        // Retrieve any changes to the className and the proxy parameters
        className = (String)params[0];
        PropertyProxy proxy = (PropertyProxy)params[1];

        // Remember our instance in the object table
        int objectId = rememberObject(object);

        if (externalizable)
        {
            readExternalizable(className, object);
        }
        else
        {
            if (isDebug)
            {
                trace.startAMFObject(className, objectTable.size() - 1);
            }

            boolean isCollectionClass = isCollectionClass(object);
            int len = ti.getProperties().size();

            for (int i = 0; i < len; i++)
            {
                String propName = ti.getProperty(i);

                if (isDebug)
                    trace.namedElement(propName);
                Object value = readObjectOneLevelDown(isCollectionClass);
                proxy.setValue(object, propName, value);
            }

            if (ti.isDynamic())
            {
                for (; ;)
                {
                    String name = readString();
                    if (name == null || name.length() == 0) break;

                    if (isDebug)
                        trace.namedElement(name);
                    Object value = readObjectOneLevelDown(isCollectionClass);
                    proxy.setValue(object, name, value);
                }
            }
        }

        if (isDebug)
            trace.endAMFObject();

        // This lets the BeanProxy substitute a new instance into the BeanProxy
        // at the end of the serialization.  You might for example create a Map, store up
        // the properties, then construct the instance based on that.  Note that this does
        // not support recursive references to the parent object however.
        Object newObj = proxy.instanceComplete(object);

        // TODO: It is possible we gave out references to the
        // temporary object.  it would be possible to warn users about
        // that problem by tracking if we read any references to this object
        // in the readObject call above.
        if (newObj != object)
        {
            objectTable.set(objectId, newObj);
            object = newObj;
        }

        return object;
    }

    /**
     *
     */
    protected void readExternalizable(String className, Object object) throws ClassNotFoundException, IOException
    {
        if (object instanceof Externalizable)
        {
            ClassUtil.validateCreation(Externalizable.class);

            if (isDebug)
                trace.startExternalizableObject(className, objectTable.size() - 1);

            ((Externalizable)object).readExternal(this);
        }
        else
        {
            //Class '{className}' must implement java.io.Externalizable to receive client IExternalizable instances.
            SerializationException ex = new SerializationException();
            ex.setMessage(10305, new Object[] {object.getClass().getName()});
            throw ex;
        }
    }

    /**
     *
     */
    protected byte[] readByteArray() throws IOException
    {
        ClassUtil.validateCreation(byte[].class);

        int ref = readUInt29();
        if ((ref & 1) == 0)
            return (byte[])getObjectReference(ref >> 1);

        int len = (ref >> 1);
        int initialCapacity = len < INITIAL_COLLECTION_CAPACITY? len : INITIAL_COLLECTION_CAPACITY;
        ByteArrayOutputStream outStream = new ByteArrayOutputStream(initialCapacity);
        for (int i = 0; i < len; i++)
            outStream.write(in.read());

        byte[] ba = outStream.toByteArray();
        objectTable.add(ba);

        if (isDebug)
            trace.startByteArray(objectTable.size() - 1, len);

        return ba;
    }

    /**
     *
     */
    protected TraitsInfo readTraits(int ref) throws IOException
    {
        if ((ref & 3) == 1) // This is a reference
            return getTraitReference(ref >> 2);

        boolean externalizable = ((ref & 4) == 4);
        boolean dynamic = ((ref & 8) == 8);
        int count = (ref >> 4); /* uint29 */
        String className = readString();

        TraitsInfo ti = new TraitsInfo(className, dynamic, externalizable, count);

        // Remember Trait Info
        traitsTable.add(ti);

        for (int i = 0; i < count; i++)
        {
            String propName = readString();
            ti.addProperty(propName);
        }

        return ti;
    }

    /**
     *
     */
    protected String readUTF(int utflen) throws IOException
    {
        checkUTFLength(utflen);
        // We should just read the bytes into a buffer
        byte[] bytearr = new byte[utflen];

        in.readFully(bytearr, 0, utflen);
        // It is UTF-8 encoding, directly use new String(bytes, "utf-8");
        String s = new String(bytearr, "utf-8");
        return s;
    }

    /**
     * AMF 3 represents smaller integers with fewer bytes using the most
     * significant bit of each byte. The worst case uses 32-bits
     * to represent a 29-bit number, which is what we would have
     * done with no compression.
     * <pre>
     * 0x00000000 - 0x0000007F : 0xxxxxxx
     * 0x00000080 - 0x00003FFF : 1xxxxxxx 0xxxxxxx
     * 0x00004000 - 0x001FFFFF : 1xxxxxxx 1xxxxxxx 0xxxxxxx
     * 0x00200000 - 0x3FFFFFFF : 1xxxxxxx 1xxxxxxx 1xxxxxxx xxxxxxxx
     * 0x40000000 - 0xFFFFFFFF : throw range exception
     * </pre>
     *
     * @return A int capable of holding an unsigned 29 bit integer.
     * @throws IOException
     *
     */
    protected int readUInt29() throws IOException
    {
        int value;

        // Each byte must be treated as unsigned
        int b = in.readByte() & 0xFF;

        if (b < 128)
            return b;

        value = (b & 0x7F) << 7;
        b = in.readByte() & 0xFF;

        if (b < 128)
            return (value | b);

        value = (value | (b & 0x7F)) << 7;
        b = in.readByte() & 0xFF;

        if (b < 128)
            return (value | b);

        value = (value | (b & 0x7F)) << 8;
        b = in.readByte() & 0xFF;

        return (value | b);
    }

    /**
     *
     */
    protected Object readXml() throws IOException
    {
        String xml = null;

        int ref = readUInt29();

        if ((ref & 1) == 0)
        {
            // This is a reference
            xml = (String)getObjectReference(ref >> 1);
        }
        else
        {
            // Read the string in
            int len = (ref >> 1);

            // writeString() special case the empty string
            // for speed.  Do add a reference
            if (0 == len)
                xml = (String)ClassUtil.createDefaultInstance(String.class, null);
            else
                xml = readUTF(len);

            //Remember Object
            objectTable.add(xml);

            if (isDebug)
                trace.write(xml);
        }

        return stringToDocument(xml);
    }

    /**
     *
     */
    protected Object getObjectReference(int ref)
    {
        if (isDebug)
            trace.writeRef(ref);

        return objectTable.get(ref);
    }

    /**
     *
     */
    protected String getStringReference(int ref)
    {
        String str = (String)stringTable.get(ref);

        if (Trace.amf && isDebug)
            trace.writeStringRef(ref);

        return str;
    }

    /**
     *
     */
    protected TraitsInfo getTraitReference(int ref)
    {
        if (Trace.amf && isDebug)
            trace.writeTraitsInfoRef(ref);

        return (TraitsInfo)traitsTable.get(ref);
    }

    /**
     * Remember a deserialized object so that you can use it later through a reference.
     */
    protected int rememberObject(Object obj)
    {
        int id = objectTable.size();
        objectTable.add(obj);
        return id;
    }


    protected Object readObjectOneLevelDown(boolean nestCollectionLevelDown) throws ClassNotFoundException, IOException
    {
        increaseNestObjectLevel();
        if (nestCollectionLevelDown)
            increaseNestCollectionLevel();
        Object value = readObject();
        decreaseNestObjectLevel();
        if (nestCollectionLevelDown)
            decreaseNestCollectionLevel();
        return value;
    }
}