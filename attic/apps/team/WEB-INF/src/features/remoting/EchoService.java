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
/**
 * A simple class that simply echoes back the provided text. Used by remoting
 * samples.
 */
package features.remoting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import features.remoting.externalizable.ExternalizableClass;
import flex.messaging.io.BeanProxy;
import flex.messaging.io.PropertyProxyRegistry;

public class EchoService
{
    // Making sure the read-only properties of ReadOnly class are serialized
    // back to the client.
    static
    {
        PropertyProxyRegistry registry = PropertyProxyRegistry.getRegistry();
        BeanProxy beanProxy = new BeanProxy();
        beanProxy.setIncludeReadOnly(true);
        registry.register(ReadOnly.class, beanProxy);
    }

    public String echo(String text)
    {
        return "I received '" + text + "' from you";
    }

    public int echoInt(int value)
    {
        return value;
    }

    public boolean echoBoolean(boolean value)
    {
        return value;
    }

    public List echoDenseArray(List array)
    {
        return array;
    }

    public Map echoSparseArray(Map array)
    {
        return array;
    }

    public Map echoMap(Map map)
    {
        return map;
    }

    public Map echoDictionary(Map dict)
    {
        return dict;
    }

    public Object echoIntVector(Object vector)
    {
        return vector;
    }

    public Object echoUIntVector(Object vector)
    {
        return vector;
    }

    public Object echoDoubleVector(Object vector)
    {
        return vector;
    }

    public Object echoObjectVector(Object vector)
    {
        return vector;
    }

    public Object echoStringVector(Object vector)
    {
        return vector;
    }

    public ExternalizableClass echoExternalizableClass(ExternalizableClass value)
    {
        return value;
    }

    public ReadOnly echoReadOnly()
    {
        ReadOnly ro = new ReadOnly("property1");
        return ro;
    }

    // A Class that only has a read-only property.
    public class ReadOnly
    {
        private String property;

        public ReadOnly(String property)
        {
            this.property = property;
        }

        public String getProperty()
        {
            return property;
        }
    }
}

