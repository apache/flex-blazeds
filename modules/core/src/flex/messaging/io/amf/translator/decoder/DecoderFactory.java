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
package flex.messaging.io.amf.translator.decoder;

import flex.messaging.io.TypeMarshallingContext;
import flex.messaging.io.amf.ASObject;
import flex.messaging.io.amf.translator.TranslationException;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * Utility class that tries to find an ActionScriptDecoder that will be able
 * to convert the encoded object into an instance of the desired class.
 *
 * @see ActionScriptDecoder
 *
 *
 */
public class DecoderFactory
{
    // The identity transform
    private static final NativeDecoder nativeDecoder = new NativeDecoder();

    // Special null transform, always returns null
    private static final NullDecoder nullDecoder = new NullDecoder();

    // Simple types (do not have the concept of a creating a placeholder or 'shell')
    private static final NumberDecoder numberDecoder = new NumberDecoder();
    private static final StringDecoder stringDecoder = new StringDecoder();
    private static final BooleanDecoder booleanDecoder = new BooleanDecoder();
    private static final CharacterDecoder characterDecoder = new CharacterDecoder();

    // Historically dates are simple types, but they are now considered complex
    // via AMF 3 (though they can not have empty placeholders or 'shells')
    private static final DateDecoder dateDecoder = new DateDecoder();
    private static final CalendarDecoder calendarDecoder = new CalendarDecoder();

    // Complex types (can also be used to create empty placeholders to be populated at decode time)
    private static final ArrayDecoder arrayDecoder = new ArrayDecoder();
    private static EnumDecoder enumDecoder = new EnumDecoder();
    private static final MapDecoder mapDecoder = new MapDecoder();
    private static final CollectionDecoder collectionDecoder = new CollectionDecoder();
    private static final TypedObjectDecoder typedObjectDecoder = new TypedObjectDecoder();

    // If we require references to be tracked and restored, we use deep, recursive decoders
    // however these are very expensive in terms of processing time
    private static final ArrayDecoder deepArrayDecoder = new ReferenceAwareArrayDecoder();
    private static final MapDecoder deepMapDecoder = new ReferenceAwareMapDecoder();
    private static final CollectionDecoder deepCollectionDecoder = new ReferenceAwareCollectionDecoder();
    private static final TypedObjectDecoder deepTypedObjectDecoder = new ReferenceAwareTypedObjectDecoder();


    /**
     * A simple method for obtaining a placeholder or 'shell' object that will be subsequently
     * populated by potentially some other deserializer. Decoders contain special logic to implement well
     * known Collections interfaces such as java.util.Map, java.util.Set, etc
     * and also contain utilities to create native Arrays, so this functionality has been
     * made available to other classes besides decoders.
     *
     * @param desiredClass the desire class for the decoded object
     * @return The <tt>ActionScriptDecoder</tt> to use for instances of the desired class.
     */
    public static ActionScriptDecoder getDecoderForShell(Class desiredClass)
    {
        if (desiredClass == null)
            return nullDecoder;

        if (Collection.class.isAssignableFrom(desiredClass))
            return collectionDecoder;

        if (Map.class.isAssignableFrom(desiredClass))
            return mapDecoder;

        if (desiredClass.isArray())
            return arrayDecoder;

        if (desiredClass.isEnum())
            return enumDecoder;

        return nativeDecoder;
    }

    /**
     * This is a faster implementation than getReferenceAwareDecoder as it doesn't track
     * or care about restoring references in the event that an instance was converted to
     * a new type. Since the all MessageDeserializes now convert directly to strongly
     * typed instances it is less likely that references will need to be tracked.
     *
     * A case where a reference may have needed to be tracked would be that a Class has
     * several properties of a subtype of Object[] (i.e. Array), Collection or Map but these
     * properties were not of the default type returned by the MessageDeserializer, that
     * is ArrayList or HashMap, and that each property had the potential to point to the same
     * instance. If we didn't track the reference, then the conversion to the subtype
     * (say HashMap -> TreeMap) would effectively clone these instances.
     *
     * @param encodedObject the encoded object
     * @param desiredClass the desire class for the decoded object
     * @return The <tt>ActionScriptDecoder</tt> to use for instances of the desired class.
     */
    public static ActionScriptDecoder getDecoder(Object encodedObject, Class desiredClass)
    {
        if (encodedObject != null)
        {
            // If we already have a suitable instance, return immediately!
            if (desiredClass.isAssignableFrom(encodedObject.getClass()))
                return nativeDecoder;

            if (String.class.equals(desiredClass))
                return stringDecoder;

            // We check Number and Boolean here as well as the encodedObejct == null case
            // as they're very common property types...
            if (isNumber(desiredClass))
                return numberDecoder;

            if (isBoolean(desiredClass))
                return booleanDecoder;

            if (Collection.class.isAssignableFrom(desiredClass))
                return collectionDecoder;

            if (Map.class.isAssignableFrom(desiredClass))
                return mapDecoder;

            if (desiredClass.isArray())
                return arrayDecoder;

            // Special Case - we have a typed ASObject and we're expecting it to
            // be converted into a new class... this would be an usual situation
            // for Mistral, however, since we now create strongly typed instances
            // from a stream
            if (isTypedObject(encodedObject))
                return typedObjectDecoder;

            if (Date.class.isAssignableFrom(desiredClass))
                return dateDecoder;

            if (Calendar.class.isAssignableFrom(desiredClass))
                return calendarDecoder;
        }

        // Null may have been sent to a primitive Java type, in which case
        // we create a default value, such as new Integer(0) for int rather
        // than create a null Integer() instance...
        if (isNumber(desiredClass))
            return numberDecoder;

        if (isBoolean(desiredClass))
            return booleanDecoder;

        if (isCharacter(desiredClass))
            return characterDecoder;

        if (encodedObject == null)
            return nullDecoder;
        
        if (desiredClass.isEnum())
            return enumDecoder;        

        DecoderFactory.invalidType(encodedObject, desiredClass);

        // Never reached...
        return nativeDecoder;
    }

    /**
     * A considerably slower entry point for decoders as it both changes the
     * assumptions we can make about type translation and also keeps track of
     * a lot of information when a complex type is converted.
     *
     * @param encodedObject the encoded object
     * @param desiredClass the desire class for the decoded object
     * @return The <tt>ActionScriptDecoder</tt> to use for instances of the desired class.
     */
    public static ActionScriptDecoder getReferenceAwareDecoder(Object encodedObject, Class desiredClass)
    {
        if (encodedObject != null)
        {
            if (String.class.equals(desiredClass))
                return stringDecoder;

            // We check Number and Boolean here as well as the encodedObejct == null case
            // as they're very common property types...
            if (isNumber(desiredClass))
                return numberDecoder;

            if (isBoolean(desiredClass))
                return booleanDecoder;

            if (Collection.class.isAssignableFrom(desiredClass))
                return deepCollectionDecoder;

            if (Map.class.isAssignableFrom(desiredClass))
                return deepMapDecoder;

            if (desiredClass.isArray())
                return deepArrayDecoder;

            // Special Case - we have a typed ASObject and we're expecting it to
            // be converted into a new class... this would be an usual situation
            // for Mistral, however, since we now create strongly typed instances
            // from a stream.
            if (isTypedObject(encodedObject))
                return deepTypedObjectDecoder;

            if (Date.class.isAssignableFrom(desiredClass))
                return dateDecoder;

            if (Calendar.class.isAssignableFrom(desiredClass))
                return calendarDecoder;

            if (isCharacter(desiredClass))
                return characterDecoder;

            // Last resort, just try and return the object undecoded if it's the right type
            // We do this last because at this stage if it is a complex object we won't catch
            // any Typed Object translations for properties on this type...
            if (desiredClass.isAssignableFrom(encodedObject.getClass()))
                return nativeDecoder;
        }

        // Null may have been sent to a primitive Java type, in which case
        // we create a default value, such as new Integer(0) for int rather than create
        // a null Integer() instance...
        if (isNumber(desiredClass))
            return numberDecoder;

        if (isBoolean(desiredClass))
            return booleanDecoder;

        if (isCharacter(desiredClass))
            return characterDecoder;

        if (encodedObject == null)
            return nullDecoder;

        DecoderFactory.invalidType(encodedObject, desiredClass);

        // Never reached...
        return nativeDecoder;
    }

    public static boolean isNumber(Class desiredClass)
    {
        boolean isNum = false;

        if (desiredClass.isPrimitive())
        {
            if (desiredClass.equals(Integer.TYPE)
                    || desiredClass.equals(Double.TYPE)
                    || desiredClass.equals(Long.TYPE)
                    || desiredClass.equals(Float.TYPE)
                    || desiredClass.equals(Short.TYPE)
                    || desiredClass.equals(Byte.TYPE))
            {
                isNum = true;
            }
        }
        else if (Number.class.isAssignableFrom(desiredClass))
        {
            isNum = true;
        }

        return isNum;
    }

    public static boolean isCharacter(Class desiredClass)
    {
        boolean isChar = false;

        if (desiredClass.isPrimitive() && desiredClass.equals(Character.TYPE))
        {
            isChar = true;
        }
        else if (desiredClass.equals(Character.class))
        {
            isChar = true;
        }

        return isChar;
    }

    public static boolean isBoolean(Class desiredClass)
    {
        boolean isBool = false;

        if (desiredClass.isPrimitive() && desiredClass.equals(Boolean.TYPE))
        {
            isBool = true;
        }
        else if (desiredClass.equals(Boolean.class))
        {
            isBool = true;
        }

        return isBool;
    }

    public static boolean isCharArray(Class desiredClass)
    {
        boolean isCharArray = false;

        if (desiredClass.isArray())
        {
            Class type = desiredClass.getComponentType();
            if (type != null && type.equals(Character.TYPE))
            {
                isCharArray = true;
            }
        }

        return isCharArray;
    }

    public static boolean isTypedObject(Object encodedObject)
    {
        boolean typed = false;

        if (encodedObject instanceof ASObject)
        {
            typed = TypeMarshallingContext.getType(encodedObject) != null;
        }

        return typed;
    }

    public static void invalidType(Object object, Class desiredClass)
    {
        String inputType = null;

        if (object != null)
        {
            inputType = object.getClass().getName();
        }

        StringBuffer message = new StringBuffer("Cannot convert ");
        if (inputType != null)
        {
            message.append("type ").append(inputType).append(" ");
        }

        if (object != null && (object instanceof String
                || object instanceof Number
                || object instanceof Boolean
                || object instanceof Date))
        {
            message.append("with value '").append(object.toString()).append("' ");
        }
        else if (object instanceof ASObject)
        {
            ASObject aso = (ASObject)object;
            message.append("with remote type specified as '").append(aso.getType()).append("' ");
        }

        message.append("to an instance of ").append(desiredClass.toString());

        TranslationException ex = new TranslationException(message.toString());
        ex.setCode("Client.Message.Deserialize.InvalidType");
        throw ex;
    }
}
