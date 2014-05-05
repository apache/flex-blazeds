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
package flex.messaging.cluster;

import flex.messaging.io.SerializationProxy;
import flex.messaging.io.amf.ASObject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import junit.framework.TestCase;


public class SerializationProxyTest extends TestCase
{
    public SerializationProxyTest(String valueExpression)
    {
        super(valueExpression);
    }

    /**
     * For clustering, SerializationProxys must be serialized and
     * deserialized.  This is because any message with a SerializationProxy,
     * for example referencedIds, on deleteItem must be passed via
     * jgroups, which uses Java serialization, to the next node in the
     * cluster.
     * Bug: LCDS-910
     */
    public void testRoundTripSerialization() throws Exception
    {
        ASObject as = new ASObject();
        as.put("name", "cathy");
        SerializationProxy proxy = new SerializationProxy(as);

        // serialize
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(out);
        oos.writeObject(proxy);
        oos.close();

        //deserialize
        byte[] bytes = out.toByteArray();
        InputStream in = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(in);
        Object o = ois.readObject();

        assertNotNull("The object should be correctly serialized/deserialized.", o);
    }
}
