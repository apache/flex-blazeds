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

import java.util.Collection;
import java.lang.reflect.Array;

/**
 * A special version of CollectionDecoder than runs a new decoder
 * over every item before it is added to the decoded collection
 * to restore references to any instances that may have undergone
 * translation to another type.
 * <p>
 * Note that tracking references is an expensive exercise and will
 * scale poorly with larger amounts of data.`
 */
public class ReferenceAwareCollectionDecoder extends CollectionDecoder {
    /**
     * We want to iterate through all of the contents of the Collection
     * to check for references that need to be restored and translations
     * that may still be required (in the case of typed ASObjects).
     * <p>
     * Return false to ensure we create a new collection and copy
     * all of the contents.
     */
    protected boolean isSuitableCollection(Object encodedObject, Class desiredClass) {
        return false;
    }

    protected Collection decodeCollection(Collection shell, Object encodedObject) {
        Collection decodedCollection = shell;
        Object decodedObject = null;
        Object obj = null;

        TypeMarshallingContext context = TypeMarshallingContext.getTypeMarshallingContext();
        ActionScriptDecoder decoder = null;

        if (encodedObject instanceof String) {
            encodedObject = ((String) encodedObject).toCharArray();
        } else {
            if (encodedObject instanceof Collection) {
                encodedObject = ((Collection) encodedObject).toArray();
            }

            context.getKnownObjects().put(encodedObject, shell);
        }

        int len = Array.getLength(encodedObject);

        for (int i = 0; i < len; i++) {
            obj = Array.get(encodedObject, i);

            if (obj == null) {
                decodedCollection.add(null);
            } else {
                //Check whether we need to restore a client
                //side reference to a known object
                Object ref = null;

                if (canUseByReference(obj))
                    ref = context.getKnownObjects().get(obj);

                if (ref == null) {
                    decoder = DecoderFactory.getReferenceAwareDecoder(obj, obj.getClass());
                    decodedObject = decoder.decodeObject(obj, obj.getClass());

                    if (canUseByReference(decodedObject)) {
                        context.getKnownObjects().put(obj, decodedObject);
                    }
                } else {
                    decodedObject = ref;
                }

                decodedCollection.add(decodedObject);
            }
        }

        return decodedCollection;
    }
}
