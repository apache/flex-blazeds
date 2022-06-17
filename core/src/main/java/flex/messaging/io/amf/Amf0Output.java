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
import flex.messaging.io.PagedRowSet;
import flex.messaging.io.PropertyProxy;
import flex.messaging.io.PropertyProxyRegistry;
import flex.messaging.io.SerializationContext;
import flex.messaging.io.SerializationDescriptor;
import flex.messaging.io.StatusInfoProxy;
import flex.messaging.io.BeanProxy;

import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.sql.RowSet;

/**
 * An Amf0 output object.
 */
public class Amf0Output extends AbstractAmfOutput implements AmfTypes {
    /**
     * 3-byte marker for object end; used for faster serialization
     * than a combination of writeUTF("") and writeByte(kObjectEndType).
     */
    public static final byte[] OBJECT_END_MARKER = {0, 0, kObjectEndType};

    /**
     * A mapping of object instances to their serialization numbers
     * for storing object references on the stream.
     */
    protected IdentityHashMap serializedObjects;

    /**
     * Number of serialized objects.
     */
    protected int serializedObjectCount = 0;

    /**
     * AVM+ Encoding.
     */
    protected boolean avmPlus;

    /**
     *
     */
    protected Amf3Output avmPlusOutput;

    /**
     * Construct a serializer without connecting it to an output stream.
     *
     * @param context the context to use
     */
    public Amf0Output(SerializationContext context) {
        super(context);
        context.supportDatesByReference = false;

        serializedObjects = new IdentityHashMap(64);
    }

    /**
     * Set to true if the AMF0 stream should switch to use AMF3 on encountering
     * the first complex Object during serialization.
     *
     * @param a Whether the client is running in AVMPlus and can handle AMF3 encoding.
     */
    public void setAvmPlus(boolean a) {
        avmPlus = a;
    }

    /**
     * Reset all object reference information allowing the class to be used to
     * write a "new" data structure.
     */
    public void reset() {
        super.reset();

        serializedObjects.clear();
        serializedObjectCount = 0;

        if (avmPlusOutput != null)
            avmPlusOutput.reset();
    }

    /**
     * Creates a new Amf3Output instance which is initialized with the
     * current SerializationContext, OutputStream and debug trace settings
     * to switch the version of the AMF protocol mid-stream.
     */
    protected void createAMF3Output() {
        avmPlusOutput = new Amf3Output(context);
        avmPlusOutput.setOutputStream(out);
        avmPlusOutput.setDebugTrace(trace);
    }

    //
    // java.io.ObjectOutput implementations
    //

    /**
     * Serialize an Object using AMF 0.
     */
    public void writeObject(Object o) throws IOException {
        if (o == null) {
            writeAMFNull();
            return;
        }

        if (o instanceof String) {
            writeAMFString((String) o);
        } else if (o instanceof Number) {
            if (!context.legacyBigNumbers &&
                    (o instanceof BigInteger || o instanceof BigDecimal)) {
                // Using double to write big numbers such as BigInteger or
                // BigDecimal can result in information loss so we write
                // them as String by default...
                writeAMFString((o).toString());
            } else {
                writeAMFDouble(((Number) o).doubleValue());
            }
        } else if (o instanceof Boolean) {
            writeAMFBoolean(((Boolean) o).booleanValue());
        } else if (o instanceof Character) {
            String s = o.toString();
            writeAMFString(s);
        } else if (o instanceof Date) {
            // Note that dates are not considered complex types in AMF 0
            writeAMFDate((Date) o);
        } else if (o instanceof Calendar) {
            writeAMFDate(((Calendar) o).getTime());
        }
        // If there is a proxy for this,write it as a custom object so the default
        // behavior can be overriden.
        else if (o instanceof Enum && PropertyProxyRegistry.getRegistry().getProxy(o.getClass()) == null) {
            Enum<?> enumValue = (Enum<?>) o;
            writeAMFString(enumValue.name());
        } else {
            // We have a complex object.

            // If we're using AMF 3, delegate to AVM+ encoding format
            if (avmPlus) {
                if (avmPlusOutput == null) {
                    createAMF3Output();
                }

                out.writeByte(kAvmPlusObjectType);
                avmPlusOutput.writeObject(o);
            } else {
                Class cls = o.getClass();

                if (cls.isArray()) {
                    writeAMFArray(o, cls.getComponentType());
                } else if (o instanceof Map && context.legacyMap && !(o instanceof ASObject)) {
                    writeMapAsECMAArray((Map) o);
                } else if (o instanceof Collection) {
                    if (context.legacyCollection)
                        writeCollection((Collection) o, null);
                    else
                        writeArrayCollection((Collection) o, null);
                } else if (o instanceof org.w3c.dom.Document) {
                    out.write(kXMLObjectType);
                    String xml = documentToString(o);
                    if (isDebug)
                        trace.write(xml);

                    writeUTF(xml, true, false);
                } else {
                    // Special Case: wrap RowSet in PageableRowSet for Serialization
                    if (o instanceof RowSet) {
                        o = new PagedRowSet((RowSet) o, Integer.MAX_VALUE, false);
                    } else if (o instanceof Throwable && context.legacyThrowable) {
                        o = new StatusInfoProxy((Throwable) o);
                    }

                    writeCustomObject(o);
                }
            }
        }
    }

    /**
     *
     */
    public void writeObjectTraits(TraitsInfo traits) throws IOException {
        String className = null;
        if (traits != null)
            className = traits.getClassName();

        if (isDebug)
            trace.startAMFObject(className, serializedObjectCount - 1);

        if (className == null || className.length() == 0) {
            out.write(kObjectType);
        } else {
            out.write(kTypedObjectType);
            out.writeUTF(className);
        }
    }

    /**
     *
     */
    public void writeObjectProperty(String name, Object value) throws IOException {
        if (isDebug)
            trace.namedElement(name);

        out.writeUTF(name);
        increaseNestObjectLevel();
        writeObject(value);
        decreaseNestObjectLevel();
    }

    /**
     *
     */
    public void writeObjectEnd() throws IOException {
        out.write(OBJECT_END_MARKER, 0, OBJECT_END_MARKER.length);

        if (isDebug)
            trace.endAMFObject();
    }


    //
    // AMF SPECIFIC SERIALIZATION METHODS
    //

    /**
     *
     */
    protected void writeAMFBoolean(boolean b) throws IOException {
        if (isDebug)
            trace.write(b);

        out.write(kBooleanType);

        out.writeBoolean(b);
    }

    /**
     *
     */
    protected void writeAMFDouble(double d) throws IOException {
        if (isDebug)
            trace.write(d);

        out.write(kNumberType);

        out.writeDouble(d);
    }

    /**
     *
     */
    protected void writeAMFDate(Date d) throws IOException {
        if (isDebug)
            trace.write(d);

        out.write(kDateType);
        // Write the time as 64bit value in ms
        out.writeDouble((double) d.getTime());
        int nCurrentTimezoneOffset = TimeZone.getDefault().getRawOffset();
        out.writeShort(nCurrentTimezoneOffset / 60000);
    }

    /**
     *
     */
    protected void writeAMFArray(Object o, Class componentType) throws IOException {
        if (componentType.isPrimitive()) {
            writePrimitiveArray(o);
        } else if (componentType.equals(Character.class)) {
            writeCharArrayAsString((Character[]) o);
        } else {
            writeObjectArray((Object[]) o, null);
        }
    }

    /**
     *
     */
    protected void writeArrayCollection(Collection col, SerializationDescriptor desc) throws IOException {
        if (!serializeAsReference(col)) {
            ArrayCollection ac;

            if (col instanceof ArrayCollection) {
                ac = (ArrayCollection) col;
                // TODO: QUESTION: Pete ignoring the descriptor here... not sure if
                // we should modify the user's AC as that could cause corruption?
            } else {
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
     *
     */
    protected void writeCustomObject(Object o) throws IOException {
        PropertyProxy proxy = null;

        if (o instanceof PropertyProxy) {
            proxy = (PropertyProxy) o;
            o = proxy.getDefaultInstance();

            // The proxy may wrap a null default instance, if so, short circuit here.
            if (o == null) {
                writeAMFNull();
                return;
            }
            // HACK: Short circuit and unwrap if PropertyProxy is wrapping an Array
            // or Collection type since we don't yet have the ability to proxy multiple
            // AMF types. We write an AMF Array directly instead of an AMF Object
            else if (o instanceof Collection) {
                if (context.legacyCollection)
                    writeCollection((Collection) o, proxy.getDescriptor());
                else
                    writeArrayCollection((Collection) o, proxy.getDescriptor());
                return;
            } else if (o.getClass().isArray()) {
                writeObjectArray((Object[]) o, proxy.getDescriptor());
                return;
            } else if (context.legacyMap && o instanceof Map && !(o instanceof ASObject)) {
                writeMapAsECMAArray((Map) o);
                return;
            }
        }

        if (!serializeAsReference(o)) {
            if (proxy == null) {
                proxy = PropertyProxyRegistry.getProxyAndRegister(o);
            }

            writePropertyProxy(proxy, o);
        }
    }

    /**
     *
     */
    protected void writePropertyProxy(PropertyProxy pp, Object instance) throws IOException {
        /*
         * At this point we substitute the instance we want to serialize.
         */
        Object newInst = pp.getInstanceToSerialize(instance);
        if (newInst != instance) {
            // We can't use writeAMFNull here I think since we already added this object
            // to the object table on the server side.  The player won't have any way
            // of knowing we have this reference mapped to null.
            if (newInst == null)
                throw new MessageException("PropertyProxy.getInstanceToSerialize class: " + pp.getClass() + " returned null for instance class: " + instance.getClass().getName());

            // Grab a new proxy if necessary for the new instance
            pp = PropertyProxyRegistry.getProxyAndRegister(newInst);
            instance = newInst;
        }

        //FIXME: Throw exception or use unsupported type for externalizable as it's not supported in AMF 0?
        boolean externalizable = false; //sp.isExternalizable(instance);
        List propertyNames = pp.getPropertyNames(instance);
        // filter write-only properties
        if (pp instanceof BeanProxy) {
            BeanProxy bp = (BeanProxy) pp;
            Iterator it = propertyNames.iterator();
            while (it.hasNext()) {
                String propName = (String) it.next();
                if (bp.isWriteOnly(instance, propName)) {   // do not serialize this, as we cannot get the value
                    it.remove();
                }
            }
        }
        TraitsInfo ti = new TraitsInfo(pp.getAlias(instance), pp.isDynamic(), externalizable, propertyNames);
        writeObjectTraits(ti);

        if (propertyNames != null) {
            Iterator it = propertyNames.iterator();
            while (it.hasNext()) {
                String propName = (String) it.next();
                Object value = pp.getValue(instance, propName);
                writeObjectProperty(propName, value);
            }
        }

        writeObjectEnd();
    }

    /**
     *
     */
    protected void writeMapAsECMAArray(Map m) throws IOException {
        if (!serializeAsReference(m)) {
            if (isDebug)
                trace.startECMAArray(serializedObjectCount - 1);

            out.write(kECMAArrayType);
            out.writeInt(0);

            Iterator it = m.keySet().iterator();
            while (it.hasNext()) {
                Object key = it.next();
                Object value = m.get(key);
                writeObjectProperty(key.toString(), value);
            }

            writeObjectEnd();
        }
    }

    /**
     *
     */
    protected void writeAMFNull() throws IOException {
        if (isDebug)
            trace.writeNull();

        out.write(kNullType);
    }

    /**
     *
     */
    protected void writeAMFString(String str) throws IOException {
        if (isDebug)
            trace.writeString(str);

        writeUTF(str, false, true);
    }

    /**
     *
     */
    protected void writeObjectArray(Object[] values, SerializationDescriptor descriptor) throws IOException {
        if (!serializeAsReference(values)) {
            if (isDebug)
                trace.startAMFArray(serializedObjectCount - 1);

            out.write(kStrictArrayType);
            out.writeInt(values.length);
            for (int i = 0; i < values.length; ++i) {
                if (isDebug)
                    trace.arrayElement(i);

                Object item = values[i];
                if (item != null && descriptor != null && !(item instanceof String)
                        && !(item instanceof Number) && !(item instanceof Boolean)
                        && !(item instanceof Character)) {

                    PropertyProxy proxy = PropertyProxyRegistry.getProxy(item);
                    proxy = (PropertyProxy) proxy.clone();
                    proxy.setDescriptor(descriptor);
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
     * Serialize a Collection.
     *
     * @param c Collection to be serialized as an array.
     * @throws java.io.IOException The exception can be generated by the output stream
     */
    protected void writeCollection(Collection c, SerializationDescriptor descriptor) throws IOException {
        if (!serializeAsReference(c)) {
            if (isDebug)
                trace.startAMFArray(serializedObjectCount - 1);

            out.write(kStrictArrayType);
            out.writeInt(c.size());
            Iterator it = c.iterator();
            int i = 0;
            while (it.hasNext()) {
                if (isDebug)
                    trace.arrayElement(i++);

                Object item = it.next();
                if (item != null && descriptor != null && !(item instanceof String)
                        && !(item instanceof Number) && !(item instanceof Boolean)
                        && !(item instanceof Character)) {
                    PropertyProxy proxy = PropertyProxyRegistry.getProxy(item);
                    proxy = (PropertyProxy) proxy.clone();
                    proxy.setDescriptor(descriptor);
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
     * Serialize an array of primitives.
     * <p>
     * Primitives include the following:
     * boolean, char, double, float, long, int, short, byte
     * </p>
     *
     * @param obj An array of primitives
     */
    protected void writePrimitiveArray(Object obj) throws IOException {
        Class aType = obj.getClass().getComponentType();

        //Treat char[] as a String
        if (aType.equals(Character.TYPE)) {
            char[] c = (char[]) obj;
            writeCharArrayAsString(c);
        } else if (!serializeAsReference(obj)) {
            if (aType.equals(Boolean.TYPE)) {
                out.write(kStrictArrayType);

                boolean[] b = (boolean[]) obj;
                out.writeInt(b.length);

                if (isDebug) {
                    trace.startAMFArray(serializedObjectCount - 1);

                    for (int i = 0; i < b.length; i++) {
                        trace.arrayElement(i);
                        writeAMFBoolean(b[i]);
                    }

                    trace.endAMFArray();
                } else {
                    for (int i = 0; i < b.length; i++) {
                        writeAMFBoolean(b[i]);
                    }
                }
            } else {
                //We have a primitive number, either a double, float, long, int, short or byte.
                //We write all of these as doubles...
                out.write(kStrictArrayType);

                int length = Array.getLength(obj);
                out.writeInt(length);

                if (isDebug) {
                    trace.startAMFArray(serializedObjectCount - 1);

                    for (int i = 0; i < length; i++) {
                        trace.arrayElement(i);
                        double v = Array.getDouble(obj, i);
                        writeAMFDouble(v);
                    }

                    trace.endAMFArray();
                } else {
                    for (int i = 0; i < length; i++) {
                        double v = Array.getDouble(obj, i);
                        writeAMFDouble(v);
                    }
                }
            }
        }
    }

    /**
     *
     */
    protected void writeCharArrayAsString(Character[] ca) throws IOException {
        int length = ca.length;
        char[] chars = new char[length];

        for (int i = 0; i < length; i++) {
            Character c = ca[i];
            if (c == null)
                chars[i] = 0;
            else
                chars[i] = ca[i].charValue();
        }
        writeCharArrayAsString(chars);
    }

    /**
     *
     */
    protected void writeCharArrayAsString(char[] ca) throws IOException {
        writeAMFString(new String(ca));
    }

    /**
     *
     */
    protected void writeUTF(String str, boolean forceLong, boolean writeType) throws IOException {
        int strlen = str.length();
        int utflen = 0;
        int c, count = 0;

        char[] charr = getTempCharArray(strlen);
        str.getChars(0, strlen, charr, 0);

        for (int i = 0; i < strlen; i++) {
            c = charr[i];
            if (c <= 0x007F) {
                utflen++;
            } else if (c > 0x07FF) {
                utflen += 3;
            } else {
                utflen += 2;
            }
        }

        int type;
        if (forceLong) {
            type = kLongStringType;
        } else {
            if (utflen <= 65535)
                type = kStringType;
            else
                type = kLongStringType;
        }

        byte[] bytearr;
        if (writeType) {
            bytearr = getTempByteArray(utflen + (type == kStringType ? 3 : 5));
            bytearr[count++] = (byte) (type);
        } else
            bytearr = getTempByteArray(utflen + (type == kStringType ? 2 : 4));

        if (type == kLongStringType) {
            bytearr[count++] = (byte) ((utflen >>> 24) & 0xFF);
            bytearr[count++] = (byte) ((utflen >>> 16) & 0xFF);
        }
        bytearr[count++] = (byte) ((utflen >>> 8) & 0xFF);
        bytearr[count++] = (byte) ((utflen) & 0xFF);
        for (int i = 0; i < strlen; i++) {
            c = charr[i];
            if (c <= 0x007F) {
                bytearr[count++] = (byte) c;
            } else if (c > 0x07FF) {
                bytearr[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
                bytearr[count++] = (byte) (0x80 | ((c >> 6) & 0x3F));
                bytearr[count++] = (byte) (0x80 | ((c) & 0x3F));
            } else {
                bytearr[count++] = (byte) (0xC0 | ((c >> 6) & 0x1F));
                bytearr[count++] = (byte) (0x80 | ((c) & 0x3F));
            }
        }
        out.write(bytearr, 0, count);
    }

    /**
     * Remember the object's serialization number so that it can be referred to
     * as a reference later. Only complex ojects should be stored as references.
     */
    protected void rememberObjectReference(Object obj) {
        serializedObjects.put(obj, new Integer(serializedObjectCount++));
    }

    /**
     * Attempts to serialize the object as a reference.
     * If the object cannot be serialized as a reference, it is stored
     * in the reference collection for potential future encounter.
     *
     * @return Success/failure indicator as to whether the object could be
     * serialized as a reference.
     */
    protected boolean serializeAsReference(Object obj) throws IOException {
        Object ref = serializedObjects.get(obj);
        if (ref != null) {
            try {
                int refNum = ((Integer) ref).intValue();
                out.write(kReferenceType);
                out.writeShort(refNum);

                if (isDebug)
                    trace.writeRef(refNum);
            } catch (ClassCastException e) {
                throw new IOException("Object reference value is not an Integer");
            }
        } else {
            rememberObjectReference(obj);
        }
        return (ref != null);
    }

    /**
     protected void writeUnsupported() throws IOException
     {
     if (isDebug)
     trace.write("UNSUPPORTED");

     out.write(kUnsupportedType);
     }
     */
}
