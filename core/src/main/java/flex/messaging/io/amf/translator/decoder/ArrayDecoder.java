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

import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.translator.TranslationException;

import java.lang.reflect.Array;
import java.util.Collection;

/**
 *
 * Decodes native Java Array, java.util.Collection, or
 * java.lang.String (to char[]) instances to a native
 * Java Array instance with desired component type.
 *
 * This class does not handle the case where the source
 * encodedObject is modified while decoding.
 */
public class ArrayDecoder extends ActionScriptDecoder
{
    @Override public boolean hasShell()
    {
        return true;
    }

    @Override public Object createShell(Object encodedObject, Class desiredClass)
    {
        Class<?> arrayElementClass = desiredClass.getComponentType();

        int size = 10;

        // If we have an encodedObject as a source then we check it's size to optimize
        // array creation. We may have been called by
        if (encodedObject != null)
        {
            if (encodedObject.getClass().isArray())
            {
                size = Array.getLength(encodedObject);
            }
            else if (encodedObject instanceof Collection)
            {
                size = ((Collection<?>)encodedObject).size();
            }
            else if (encodedObject instanceof String)
            {
                size = ((String)encodedObject).length();
            }
            else
            {
                TranslationException ex = new TranslationException("Could not create Array " + arrayElementClass);
                ex.setCode("Server.Processing");
                throw ex;
            }
        }

        Object shell = Array.newInstance(arrayElementClass, size);
        return shell;
    }

    @Override public Object decodeObject(Object shell, Object encodedObject, Class desiredClass)
    {
        if (shell == null || encodedObject == null)
            return null;

        Class<?> arrayElementClass = desiredClass.getComponentType();

        if (encodedObject instanceof Collection)
            return decodeArray(shell, (Collection<?>)encodedObject, arrayElementClass);

        if (encodedObject.getClass().isArray())
            return decodeArray(shell, encodedObject, arrayElementClass);

        if (encodedObject instanceof String && isCharacter(arrayElementClass))
            return decodeArray(shell, (String)encodedObject, arrayElementClass);

        return null; // FIXME: Throw an exception!
    }

    protected Object decodeArray(Object shellArray, String string, Class arrayElementClass)
    {
        if (char.class.equals(arrayElementClass))
            return string.toCharArray();

        if (Character.class.equals(arrayElementClass))
        {
            char[] temp = string.toCharArray();
            Character[] charArray= new Character[temp.length];
            for (int i = 0; i < temp.length; i++)
                charArray[i] = Character.valueOf(temp[i]);
            return charArray;
        }

        return null;
    }

    protected Object decodeArray(Object shellArray, Collection collection, Class arrayElementClass)
    {
        return decodeArray(shellArray, collection.toArray(), arrayElementClass);
    }

    protected Object decodeArray(Object shellArray, Object array, Class arrayElementClass)
    {
        Object encodedValue = null;
        Object decodedValue = null;

        int n = 0;
        int len = Array.getLength(array);

        for (int i = 0; i < len; i++)
        {
            encodedValue = Array.get(array, i);

            if (encodedValue == null)
            {
                Array.set(shellArray, n, null);
            }
            else
            {
                // We may need to honor our loose-typing rules for individual types as,
                // unlike a Collection, an Array has a fixed element type. We'll use our handy
                // decoder suite again to find us the right decoder...
                ActionScriptDecoder decoder;
                if (SerializationContext.getSerializationContext().restoreReferences)
                    decoder = DecoderFactory.getReferenceAwareDecoder(encodedValue, arrayElementClass);
                else
                    decoder = DecoderFactory.getDecoder(encodedValue, arrayElementClass);

                decodedValue = decoder.decodeObject(encodedValue, arrayElementClass);

                try
                {
                    Array.set(shellArray, n, decodedValue);
                }
                catch (IllegalArgumentException ex)
                {
                    // FIXME: At least log this as a error...
                    // TODO: Should we report a failed Array element set?
                    // Perhaps the action here could be configurable on the translation context?
                    Array.set(shellArray, n, null);
                }
            }
            n++;
        }

        return shellArray;
    }

    private boolean isCharacter(Class<?> classType)
    {
        return Character.class.equals(classType) || char.class.equals(classType);
    }
}
