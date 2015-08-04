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

package remoting.datatype;

import java.util.Collections;
import java.util.List;

import flex.messaging.io.BeanProxy;
import flex.messaging.io.AbstractProxy;
import flex.messaging.util.ClassUtil;

public class EnumProxy extends BeanProxy
{
    static final String ACTION_SCRIPT_VALUE_NAME = "fruit";
    static List propertyNames = Collections.singletonList(ACTION_SCRIPT_VALUE_NAME);

    @Override
    public Object createInstance(String className)
    {
        Class cl = AbstractProxy.getClassFromClassName(className);

        if (cl.isEnum())
        {
            return new EnumStub(cl);
        }
        throw new IllegalArgumentException("**** remoting.datatype.EnumProxy registered for a class which is not an enum: " + cl.getName());
    }

    public Object getValue(Object instance, String propertyName)
    {
        if (propertyName.equals(ACTION_SCRIPT_VALUE_NAME))
            return instance.toString();

        throw new IllegalArgumentException("No property named: " + propertyName + " on enum type");
    }

    public void setValue(Object instance, String propertyName, Object value)
    {
        ClassUtil.validateAssignment(instance, propertyName, value);
        EnumStub es = (EnumStub) instance;
        if (propertyName.equals(ACTION_SCRIPT_VALUE_NAME))
            es.value = (String) value;
        else
            throw new IllegalArgumentException("no EnumStub property: " + propertyName);
    }

    public Object instanceComplete(Object instance)
    {
        EnumStub es = (EnumStub) instance;
        return Enum.valueOf(es.cl, es.value);
    }

    public List getPropertyNames(Object instance)
    {
        if (!(instance instanceof Enum))
            throw new IllegalArgumentException("getPropertyNames called with non Enum object");
        return propertyNames;
    }

    class EnumStub {
        EnumStub(Class c)
        {
            cl = c;
        }
        Class cl;
        String value;
    }

}

