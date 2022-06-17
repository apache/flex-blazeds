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

import flex.messaging.io.amf.translator.TranslationException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Decodes a java.lang.reflect.Array, java.util.Collection,
 * java.lang.String (using toCharArray), to a java.util.Collection
 * instance.
 * <p>
 * If the desired Collection class is an interface then an instance
 * of a standard implementation will be created.
 * </p>
 * <p>
 * If java.util.SortedSet is desired, then a java.util.TreeSet will be created.
 * </p>
 * <p>
 * If a java.util.Set is desired, then a java.util.HashSet will be created.
 * </p>
 * <p>
 * If a java.util.List is desired, then a java.util.ArrayList will be created.
 * </p>
 * <p>
 * If a java.util.Collection is desired, then a java.util.ArrayList will be created.
 * </p>
 *
 * @see java.util.Collection
 */
public class CollectionDecoder extends ActionScriptDecoder {
    public boolean hasShell() {
        return true;
    }

    protected boolean isSuitableCollection(Object encodedObject, Class desiredClass) {
        return (encodedObject instanceof Collection && desiredClass.isAssignableFrom(encodedObject.getClass()));
    }

    public Object createShell(Object encodedObject, Class desiredClass) {
        Collection col = null;

        try {
            if (encodedObject != null) {
                if (isSuitableCollection(encodedObject, desiredClass)) {
                    col = (Collection) encodedObject;
                } else {
                    if (desiredClass.isInterface()) {
                        if (List.class.isAssignableFrom(desiredClass)) {
                            col = new ArrayList();
                        } else if (SortedSet.class.isAssignableFrom(desiredClass)) {
                            col = new TreeSet();
                        } else if (Set.class.isAssignableFrom(desiredClass)) {
                            col = new HashSet();
                        } else if (Collection.class.isAssignableFrom(desiredClass)) {
                            col = new ArrayList();
                        }
                    } else {
                        col = (Collection) desiredClass.newInstance();
                    }
                }
            } else {
                col = (Collection) desiredClass.newInstance();
            }
        } catch (Exception e) {
            TranslationException ex = new TranslationException("Could not create Collection " + desiredClass, e);
            ex.setCode("Server.Processing");
            throw ex;
        }

        if (col == null) {
            DecoderFactory.invalidType(encodedObject, desiredClass);
        }

        return col;
    }

    public Object decodeObject(Object shell, Object encodedObject, Class desiredClass) {
        if (shell == null || encodedObject == null)
            return null;

        // Don't decode if we already have a suitable Collection. 
        if (isSuitableCollection(encodedObject, desiredClass)) {
            return encodedObject;
        }

        return decodeCollection((Collection) shell, encodedObject);
    }

    protected Collection decodeCollection(Collection collectionShell, Object encodedObject) {
        Object obj = null;

        if (encodedObject instanceof String) {
            encodedObject = ((String) encodedObject).toCharArray();
        } else if (encodedObject instanceof Collection) {
            encodedObject = ((Collection) encodedObject).toArray();
        }

        int len = Array.getLength(encodedObject);

        for (int i = 0; i < len; i++) {
            obj = Array.get(encodedObject, i);
            collectionShell.add(obj);
        }

        return collectionShell;
    }
}
