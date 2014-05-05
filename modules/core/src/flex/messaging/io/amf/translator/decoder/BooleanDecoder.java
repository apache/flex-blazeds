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
 * Translates a java.lang.Boolean or java.lang.String instances
 * into a java.lang.Boolean instance.
 * <p>
 * Note that for Strings, only &quot;true&quot;
 * will be (case insensitively) converted to a true Boolean value. All other
 * values will be interpreted as false.
 * </p>
 *
 * @author Brian Deitte
 * @author Peter Farland
 *
 * @exclude
 */
public class BooleanDecoder extends ActionScriptDecoder
{
    public Object decodeObject(Object shell, Object encodedObject, Class desiredClass)
    {
        Object result = null;

        if (encodedObject == null)
        {
            result = Boolean.FALSE;
        }
        else if (encodedObject instanceof Boolean)
        {
            result = encodedObject;
        }
        else if (encodedObject instanceof String)
        {
            String str = (String)encodedObject;
            result = Boolean.valueOf(str);
        }
        else
        {
            DecoderFactory.invalidType(encodedObject, desiredClass);
        }

        return result;
    }
}
