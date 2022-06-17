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

import java.util.Map;
import java.util.Iterator;

/**
 *
 */
public class ReferenceAwareMapDecoder extends MapDecoder {
    public Object decodeObject(Object shell, Object encodedObject, Class desiredClass) {
        if (shell == null) return null;

        Map shellMap = (Map) shell;
        Map encodedMap = (Map) encodedObject;

        TypeMarshallingContext context = TypeMarshallingContext.getTypeMarshallingContext();
        context.getKnownObjects().put(encodedObject, shell);

        ActionScriptDecoder decoder = null;
        Object key = null;
        Object value = null;
        Object decodedValue = null;
        for (Iterator keys = encodedMap.keySet().iterator(); keys.hasNext(); ) {
            key = keys.next();
            value = encodedMap.get(key);

            if (value == null) {
                shellMap.put(key, null);
                continue;
            }

            //Check whether we need to restore a client
            //side reference to a known object
            Object ref = null;

            if (canUseByReference(value))
                ref = context.getKnownObjects().get(value);

            if (ref == null) {
                decoder = DecoderFactory.getReferenceAwareDecoder(value, value.getClass());
                decodedValue = decoder.decodeObject(value, value.getClass());

                if (canUseByReference(decodedValue)) {
                    context.getKnownObjects().put(value, decodedValue);
                }
            } else {
                decodedValue = ref;
            }

            shellMap.put(key, decodedValue);
        }

        return shellMap;
    }
}
