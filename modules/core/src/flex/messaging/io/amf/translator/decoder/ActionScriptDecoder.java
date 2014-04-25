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

import java.util.Calendar;
import java.util.Date;

/**
 * Decode an ActionScript object (of some type) to a Java object (of some type).
 *
 * @exclude
 */
public abstract class ActionScriptDecoder
{
    /**
     * Does this type have a placeholder shell?
     * @return boolean true if there is a placeholder shell
     */
    public boolean hasShell()
    {
        return false;
    }

    /**
     * Used for calls only interested in creating a placeholder shell for a type.
     * @param encodedObject the encoded object
     * @param desiredClass the desired class for the decoded object
     * @return Object the shell placeholder object
     */
    public Object createShell(Object encodedObject, Class desiredClass)
    {
        return null;
    }

    /**
     * Used by calls to decode an object has a shell placeholder.
     * @param shell the placeholder shell
     * @param encodedObject the encoded object
     * @param desiredClass the desired class for the decoded object
     * @return Object the decoded object
     */
    public abstract Object decodeObject(Object shell, Object encodedObject, Class desiredClass);

    /**
     * Used by calls wanted to decode an object. If the decoder requires a place holder shell one is created
     * and then the encodedObject is decoded to fill the object shell.
     * @param encodedObject the encoded object
     * @param desiredClass the desire class for the decoded object
     * @return Object the decoded object
     */
    public Object decodeObject(Object encodedObject, Class desiredClass)
    {
        Object shell = null;

        if (hasShell())
        {
            shell = createShell(encodedObject, desiredClass);
        }

        return decodeObject(shell, encodedObject, desiredClass);
    }

    protected boolean canUseByReference(Object o)
    {
        if (o == null)
            return false;

        else if (o instanceof String)
            return false;

        else if (o instanceof Number)
            return false;

        else if (o instanceof Boolean)
            return false;

        else if (o instanceof Date)
        {
            if (SerializationContext.getSerializationContext().supportDatesByReference)
                return true;
            else
                return false;
        }

        else if (o instanceof Calendar)
            return false;

        else if (o instanceof Character)
            return false;

        return true;
    }

    protected static Object getDefaultPrimitiveValue(Class type)
    {
        if (type == Boolean.TYPE)
            return Boolean.FALSE;
        else if (type == Integer.TYPE)
            return new Integer(0);
        else if (type == Double.TYPE)
            return new Double(0);
        else if (type == Long.TYPE)
            return new Long(0);
        else if (type == Float.TYPE)
            return new Float(0);
        else if (type == Character.TYPE)
            return new Character(Character.MIN_VALUE);
        else if (type == Short.TYPE)
            return new Short((short)0);
        else if (type == Byte.TYPE)
            return new Byte((byte)0);

        return null;
    }
}
