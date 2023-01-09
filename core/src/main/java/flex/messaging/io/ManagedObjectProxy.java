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
package flex.messaging.io;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Iterator;

/**
 *
 */
public class ManagedObjectProxy extends ObjectProxy {
    static final long serialVersionUID = 255140415084514484L;

    public ManagedObjectProxy() {
        super();
    }

    public ManagedObjectProxy(int initialCapacity) {
        super(initialCapacity);
    }

    public ManagedObjectProxy(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        int count = this.size();
        out.writeInt(count);

        // TODO: QUESTION: Jeff, We could copy the client approach to check a destination
        // for lazy associations to exclude them from serialization.

        Iterator it = keySet().iterator();
        while (it.hasNext()) {
            Object key = it.next();
            out.writeObject(key);
            out.writeObject(get(key));
        }
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int count = in.readInt();

        for (int i = 0; i < count; i++) {
            Object key = in.readObject();
            Object value = in.readObject();
            put(key, value);
        }
    }
}
