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

import java.io.IOException;
import java.io.UTFDataFormatException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import flex.messaging.io.PropertyProxy;
import flex.messaging.io.SerializationContext;
import flex.messaging.io.SerializationException;
import flex.messaging.io.UnknownTypeException;
import flex.messaging.util.ClassUtil;

/**
 * An Amf0 input object.
 * @exclude
 */
public class Amf0Input extends AbstractAmfInput implements AmfTypes
{
    /**
     * Unfortunately the Flash Player starts AMF 3 messages off with the legacy
     * AMF 0 format and uses a type, AmfTypes.kAvmPlusObjectType, to indicate
     * that the next object in the stream is to be deserialized differently. The
     * original hope was for two independent encoding versions... but for now
     * we just keep a reference to objectInput here.
     * @exclude
     */
    protected ActionMessageInput avmPlusInput;

    /**
     * @exclude
     */
    protected List objectsTable;

    public Amf0Input(SerializationContext context)
    {
        super(context);

        objectsTable = new ArrayList(64);
    }

    /**
     * Clear all object reference information so that the instance
     * can be used to deserialize another data structure.
     *
     * Reset should be called before reading a top level object,
     * such as a new header or a new body.
     */
    @Override
    public void reset()
    {
        super.reset();

        objectsTable.clear();

        if (avmPlusInput != null)
            avmPlusInput.reset();
    }


    //
    // java.io.ObjectInput SERIALIZATION IMPLEMENTATIONS
    //

    /**
     * Public entry point to read a top level AMF Object, such as
     * a header value or a message body.
     */
    public Object readObject() throws ClassNotFoundException, IOException
    {
        int type = in.readByte();

        Object value = readObjectValue(type);
        return value;
    }

    protected Object readObjectValue(int type) throws ClassNotFoundException, IOException
    {
        Object value = null;
        switch (type)
        {
            case kNumberType:
                value = Double.valueOf(readDouble());
                break;

            case kBooleanType:
                value = Boolean.valueOf(readBoolean());
                break;

            case kStringType:
                value = readString();
                break;

            case kAvmPlusObjectType:

                if (avmPlusInput == null)
                {
                    avmPlusInput = new Amf3Input(context);
                    avmPlusInput.setDebugTrace(trace);
                    avmPlusInput.setInputStream(in);
                }
                value = avmPlusInput.readObject();
                break;

            case kStrictArrayType:
                value = readArrayValue();
                break;

            case kTypedObjectType:
                String typeName = in.readUTF();
                value = readObjectValue(typeName);
                break;

            case kLongStringType:
                ClassUtil.validateCreation(String.class);

                value = readLongUTF();
                if (isDebug)
                    trace.writeString((String)value);
                break;

            case kObjectType:
                value = readObjectValue(null);
                break;

            case kXMLObjectType:
                value = readXml();
                break;

            case kNullType:
                if (isDebug)
                    trace.writeNull();
                break;

            case kDateType:
                value = readDate();
                break;

            case kECMAArrayType:
                value = readECMAArrayValue();
                break;

            case kReferenceType:
                int refNum = in.readUnsignedShort();

                if (isDebug)
                    trace.writeRef(refNum);

                value = objectsTable.get(refNum);
                break;

            case kUndefinedType:

                if (isDebug)
                    trace.writeUndefined();
                break;

            case kUnsupportedType:

                if (isDebug)
                    trace.write("UNSUPPORTED");

                //Unsupported type found in AMF stream.
                UnknownTypeException ex = new UnknownTypeException();
                ex.setMessage(10302);
                throw ex;

            case kObjectEndType:

                if (isDebug)
                    trace.write("UNEXPECTED OBJECT END");

                //Unexpected object end tag in AMF stream.
                UnknownTypeException ex1 = new UnknownTypeException();
                ex1.setMessage(10303);
                throw ex1;

            case kRecordsetType:

                if (isDebug)
                    trace.write("UNEXPECTED RECORDSET");

                //AMF Recordsets are not supported.
                UnknownTypeException ex2 = new UnknownTypeException();
                ex2.setMessage(10304);
                throw ex2;

            default:

                if (isDebug)
                    trace.write("UNKNOWN TYPE");

                UnknownTypeException ex3 = new UnknownTypeException();
                ex3.setMessage(10301, new Object[]{new Integer(type)});
                throw ex3;
        }
        return value;
    }

    protected Date readDate() throws IOException
    {
        ClassUtil.validateCreation(Date.class);

        long time = (long)in.readDouble();
        /*
            We read in the timezone but do nothing with the value as
            we expect dates to be written in the UTC timezone. Client
            and servers are responsible for applying their own
            timezones.
        */
        in.readShort();

        Date d = new Date(time);

        if (isDebug)
            trace.write(d.toString());

        return d;
    }

    /** {@inheritDoc} */
    @Override
    public boolean readBoolean() throws IOException
    {
        ClassUtil.validateCreation(Boolean.class);

        boolean b = super.readBoolean();
        if (isDebug)
            trace.write(b);
        return b;
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
     * Deserialize the bits of an ECMA array w/o a prefixing type byte.
     */
    protected Map readECMAArrayValue() throws ClassNotFoundException, IOException
    {
        ClassUtil.validateCreation(HashMap.class);

        int size = in.readInt();
        HashMap h;
        if (size == 0)
        {
            h = new HashMap();
        }
        else
        {
            int initialCapacity = size < INITIAL_COLLECTION_CAPACITY? size : INITIAL_COLLECTION_CAPACITY;
            h = new HashMap(initialCapacity);
        }

        rememberObject(h);

        if (isDebug)
            trace.startECMAArray(objectsTable.size() - 1);

        String name = in.readUTF();
        int type = in.readByte();
        while (type != kObjectEndType)
        {
            if (type != kObjectEndType)
            {
                if (isDebug)
                    trace.namedElement(name);

                // Always read value but be careful to ignore erroneous 'length' prop that is sometimes sent by the player.
                Object value = readObjectValueOneLevelDown(type, true);
                if (!name.equals("length"))
                {
                    ClassUtil.validateAssignment(h, name, value);
                    h.put(name, value);
                }
            }

            name = in.readUTF();
            type = in.readByte();
        }

        if (isDebug)
            trace.endAMFArray();

        return h;
    }

    protected String readString() throws IOException
    {
        ClassUtil.validateCreation(String.class);

        String s = readUTF();
        if (isDebug)
            trace.writeString(s);
        return s;
    }


    /**
     * Deserialize the bits of an array w/o a prefixing type byte.
     */
    protected Object readArrayValue() throws ClassNotFoundException, IOException
    {
        int size = in.readInt();
        // Don't instantiate List/Array right away with the supplied size if it is more than
        // INITIAL_COLLECTION_CAPACITY in case the supplied size has been tampered.
        boolean useListTemporarily = false;
        Object l;
        if (context.legacyCollection || size > INITIAL_COLLECTION_CAPACITY)
        {
            useListTemporarily = !context.legacyCollection;
            ClassUtil.validateCreation(ArrayList.class);
            int initialCapacity = size < INITIAL_COLLECTION_CAPACITY? size : INITIAL_COLLECTION_CAPACITY;
            l = new ArrayList(initialCapacity);
        }
        else
        {
            ClassUtil.validateCreation(Object[].class);
            l = new Object[size];
        }
        int objectId = rememberObject(l); // Remember the List/Object[].

        if (isDebug)
            trace.startAMFArray(objectsTable.size() - 1);

        for (int i = 0; i < size; ++i)
        {
            if (isDebug)
                trace.arrayElement(i);

            // Add value to the array
            int type = in.readByte();
            Object value = readObjectValueOneLevelDown(type, true);
            ClassUtil.validateAssignment(l, i, value);
            if (l instanceof ArrayList)
                ((ArrayList)l).add(value);
            else
                Array.set(l, i, value);
        }

        if (isDebug)
            trace.endAMFArray();

        if (useListTemporarily)
        {
            l = ((ArrayList)l).toArray();
            objectsTable.set(objectId, l);
        }

        return l;
    }

    /**
     * Deserialize the bits of a map w/o a prefixing type byte.
     */
    protected Object readObjectValue(String className) throws ClassNotFoundException, IOException
    {
        // Prepare the parameters for createObjectInstance(). Use an array as a holder
        // to simulate two 'by-reference' parameters className and (initially null) proxy
        Object[] params = new Object[] {className, null};
        Object object = createObjectInstance(params);

        // Retrieve any changes to the className and the proxy parameters
        className = (String)params[0];
        PropertyProxy proxy = (PropertyProxy)params[1];

        int objectId = rememberObject(object);

        if (isDebug)
            trace.startAMFObject(className, objectsTable.size() - 1);

        boolean isCollectionClass = isCollectionClass(object);
        String propertyName = in.readUTF();
        int type = in.readByte();
        while (type != kObjectEndType)
        {
            if (isDebug)
                trace.namedElement(propertyName);
            Object value = readObjectValueOneLevelDown(type, isCollectionClass);
            proxy.setValue(object, propertyName, value);
            propertyName = in.readUTF();
            type = in.readByte();
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
            objectsTable.set(objectId, newObj);
            object = newObj;
        }

        return object;
    }

    /**
     * This code borrows heavily from DataInputStreat.readUTF().
     * However, it uses a 32-bit string length.
     *
     * @return the read String
     * @throws java.io.UTFDataFormatException if the UTF-8 encoding is incorrect
     * @throws IOException            if an I/O error occurs.
     */
    protected String readLongUTF() throws IOException
    {
        int utflen = in.readInt();
        checkUTFLength(utflen);

        int c, char2, char3;
        char[] charr = getTempCharArray(utflen);
        byte bytearr [] = getTempByteArray(utflen);
        int count = 0;
        int chCount = 0;

        in.readFully(bytearr, 0, utflen);

        while (count < utflen)
        {
            c = (int)bytearr[count] & 0xff;
            switch (c >> 4)
            {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    /* 0xxxxxxx*/
                    count++;
                    charr[chCount] = (char)c;
                    break;
                case 12:
                case 13:
                    /* 110x xxxx   10xx xxxx*/
                    count += 2;
                    if (count > utflen)
                        throw new UTFDataFormatException();
                    char2 = (int)bytearr[count - 1];
                    if ((char2 & 0xC0) != 0x80)
                        throw new UTFDataFormatException();
                    charr[chCount] = (char)(((c & 0x1F) << 6) | (char2 & 0x3F));
                    break;
                case 14:
                    /* 1110 xxxx  10xx xxxx  10xx xxxx */
                    count += 3;
                    if (count > utflen)
                        throw new UTFDataFormatException();
                    char2 = (int)bytearr[count - 2];
                    char3 = (int)bytearr[count - 1];
                    if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
                        throw new UTFDataFormatException();
                    charr[chCount] = (char)
                        (((c & 0x0F) << 12) |
                         ((char2 & 0x3F) << 6) |
                         ((char3 & 0x3F) << 0));
                    break;
                default:
                    /* 10xx xxxx,  1111 xxxx */
                    throw new UTFDataFormatException();
            }
            chCount++;
        }
        // The number of chars produced may be less than utflen
        return new String(charr, 0, chCount);
    }

    protected Object readXml() throws IOException
    {
        String xml = readLongUTF();

        if (isDebug)
            trace.write(xml);

        return stringToDocument(xml);
    }


    /**
     * Remember a deserialized object so that you can use it later through a reference.
     */
    protected int rememberObject(Object obj)
    {
        int id = objectsTable.size();
        objectsTable.add(obj);
        return id;
    }

    protected Object readObjectValueOneLevelDown(int type, boolean nestCollectionLevelDown) throws ClassNotFoundException, IOException
    {
        increaseNestObjectLevel();
        if (nestCollectionLevelDown)
            increaseNestCollectionLevel();
        Object value = readObjectValue(type);
        decreaseNestObjectLevel();
        if (nestCollectionLevelDown)
            decreaseNestCollectionLevel();
        return value;
    }
}