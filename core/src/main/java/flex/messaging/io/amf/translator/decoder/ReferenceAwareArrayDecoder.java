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

import java.lang.reflect.Array;
import java.util.Collection;

import flex.messaging.io.TypeMarshallingContext;

/**
 *
 */
public class ReferenceAwareArrayDecoder extends ArrayDecoder {
    public Object decodeObject(Object shell, Object encodedObject, Class desiredClass) {
        if (shell == null || encodedObject == null)
            return null;

        Class arrayElementClass = desiredClass.getComponentType();

        if (encodedObject instanceof Collection) {
            return decodeArray(shell, (Collection) encodedObject, arrayElementClass);
        } else if (encodedObject.getClass().isArray()) {
            return decodeArray(shell, encodedObject, arrayElementClass);
        } else if (encodedObject instanceof String && Character.class.equals(arrayElementClass)) {
            return decodeArray(shell, (String) encodedObject, arrayElementClass);
        } else {
            return shell;
        }
    }

    protected Object decodeArray(Object shellArray, Collection collection, Class arrayElementClass) {
        Object[] array = collection.toArray();
        TypeMarshallingContext.getTypeMarshallingContext().getKnownObjects().put(array, shellArray);
        return decodeArray(shellArray, array, arrayElementClass);
    }

    protected Object decodeArray(Object shellArray, Object array, Class arrayElementClass) {
        Object encodedValue = null;
        Object decodedValue = null;
        TypeMarshallingContext context = TypeMarshallingContext.getTypeMarshallingContext();

        ActionScriptDecoder decoder = null;
        int n = 0;
        int len = Array.getLength(array);
        for (int i = 0; i < len; i++) {
            encodedValue = Array.get(array, i);

            if (encodedValue == null) {
                Array.set(shellArray, n, null);
            } else {
                //Check whether we need to restore a client
                //side reference to a known object
                Object ref = null;

                if (canUseByReference(encodedValue))
                    ref = context.getKnownObjects().get(encodedValue);

                if (ref == null) {
                    decoder = DecoderFactory.getReferenceAwareDecoder(encodedValue, arrayElementClass);
                    decodedValue = decoder.decodeObject(encodedValue, arrayElementClass);

                    if (canUseByReference(decodedValue)) {
                        context.getKnownObjects().put(encodedValue, decodedValue);
                    }
                } else {
                    decodedValue = ref;
                }

                try {
                    Array.set(shellArray, n, decodedValue);
                } catch (IllegalArgumentException ex) {
                    Array.set(shellArray, n, null);
                }
            }
            n++;
        }

        return shellArray;
    }
}
