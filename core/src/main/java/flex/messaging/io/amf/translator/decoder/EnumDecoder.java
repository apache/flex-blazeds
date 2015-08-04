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

/**
 * Decode an ActionScript enumeration object (generally a string) to a Java enum.
 *
 */
public class EnumDecoder extends ActionScriptDecoder
{
    /**
     * Does this type have a placeholder shell?
     * True for Enumerations.
     * @return boolean true if it has a shell
     */
    @Override
    public boolean hasShell()
    {
        return true;
    }

    /**
     * Create the enumeration object based on the string.
     *
     * @param encodedObject the object
     * @param desiredClass ignored
     * @return Object the created shell object
     */
    @Override
    public Object createShell(Object encodedObject, Class desiredClass)
    {
        if (encodedObject instanceof Enum)
            return encodedObject;

        if (encodedObject == null)
            return null;

        @SuppressWarnings("unchecked")
        Enum value = Enum.valueOf(desiredClass, encodedObject.toString());
        return value;
    }

    /**
     * Decode an object.
     * For the enum type, the createShell has already done the work, so we just
     * return the shell itself.
     * @param shell the shell object
     * @param encodedObject the encodedObject
     * @param desiredClass the desired class for decoded object
     * @return Object the decoded object
     */
    @Override
    public Object decodeObject(Object shell, Object encodedObject, Class desiredClass)
    {
        return (shell == null || encodedObject == null)? null : shell;
    }
}