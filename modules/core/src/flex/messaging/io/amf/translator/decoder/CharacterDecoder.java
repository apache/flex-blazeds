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
 * Decode a java.lang.String or java.lang.Character to
 * a java.lang.Character instance.
 * <p>
 * Note that a String must be non-zero length and only the first
 * character in the String will be used.
 * </p>
 *
 * @author Peter Farland
 *
 * @exclude
 */
public class CharacterDecoder extends ActionScriptDecoder
{
    public Object decodeObject(Object shell, Object encodedObject, Class desiredClass)
    {
        Character result = null;

        if (encodedObject == null)
        {
            char c = 0;
            result = new Character(c);
        }
        else if (encodedObject instanceof String)
        {
            String str = (String)encodedObject;

            char[] chars = str.toCharArray();

            if (chars.length > 0)
            {
                result = new Character(chars[0]);
            }
        }
        else if (encodedObject instanceof Character)
        {
            result = (Character)encodedObject;
        }

        if (result == null)
        {
            DecoderFactory.invalidType(encodedObject, desiredClass);
        }

        return result;
    }
}
