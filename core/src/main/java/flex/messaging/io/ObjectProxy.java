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

import java.io.Externalizable;
import java.io.ObjectInput;
import java.io.IOException;
import java.io.ObjectOutput;
import java.util.Map;
import java.util.HashMap;

/**
 * Flex's ObjectProxy class allows an anonymous, dynamic ActionScript Object
 * to be bindable and report change events. Since ObjectProxy only wraps
 * the ActionScript Object type we can map the class to a java.util.HashMap on the
 * server, since the user would expect this type to be deserialized as a
 * java.util.HashMap as it is...
 */
public class ObjectProxy extends HashMap implements Externalizable
{
    static final long serialVersionUID = 6978936573135117900L;

    public ObjectProxy()
    {
       super();
    }

    public ObjectProxy(int initialCapacity)
    {
        super(initialCapacity);
    }

    public ObjectProxy(int initialCapacity, float loadFactor)
    {
        super(initialCapacity, loadFactor);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
    {
        Object value = in.readObject();
        if (value instanceof Map)
        {
            putAll((Map)value);
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException
    {
        // We can't output "this" to the serializer as it would
        // cause a loop back to writeExternal as this is an Externalizable
        // implementation itself!

        Map map = new HashMap();
        map.putAll(this);
        out.writeObject(map);
    }
}
