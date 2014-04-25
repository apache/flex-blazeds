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
package flex.messaging.util;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * Prettifies the representation of an Object as a String. Complex
 * types are not traversed.
 *
 * @exclude
 */
public class BasicPrettyPrinter implements PrettyPrinter
{
    protected ObjectTrace trace;

    public BasicPrettyPrinter()
    {
    }

    /**
     * Prettifies the representation of an Object as a String.
     * <ul>
     *   <li>Simple types are simply toString'ed.</li>
     *   <li>XML strings are formatted with line feeds and indentations.</li>
     *   <li>Complex types report their class names.</li>
     *   <li>Collections, Maps and native Arrays also report their size/length.</li>
     * </ul>
     * @return A prettified version of an Object as a String.
     */
    public String prettify(Object o)
    {
        try
        {
            trace = new ObjectTrace();
            internalPrettify(o);
            return trace.toString();
        }
        catch (Throwable t)
        {
            return trace.toString();
        }
        finally
        {
            trace = null;
        }
    }

    protected void internalPrettify(Object o)
    {
        if (o == null)
        {
            trace.writeNull();
        }
        else if (o instanceof String)
        {
            String string = (String)o;
            if (string.startsWith("<?xml"))
            {
                trace.write(StringUtils.prettifyXML(string));
            }
            else
            {
                trace.write(string);
            }
        }
        else if (o instanceof Number || o instanceof Boolean || o instanceof Date
                || o instanceof Calendar || o instanceof Character)
        {
            trace.write(o);
        }
        else
        {
            prettifyComplexType(o);
        }
    }

    protected void prettifyComplexType(Object o)
    {
        StringBuffer header = new StringBuffer();

        if (o instanceof PrettyPrintable)
        {
            PrettyPrintable pp = (PrettyPrintable)o;
            header.append(pp.toStringHeader());
        }

        Class c = o.getClass();
        String className = c.getName();

        if (o instanceof Collection)
        {
            header.append(className).append(" (Collection size:").append(((Collection)o).size()).append(")");
        }
        else if (o instanceof Map)
        {
            header.append(className).append(" (Map size:").append(((Map)o).size()).append(")");
        }
        else if (c.isArray() && c.getComponentType() != null)
        {
            Class componentType = c.getComponentType();
            className = componentType.getName();
            header.append(className).append("[] (Array length:").append(Array.getLength(o)).append(")");
        }
        else
        {
            header.append(className);
        }

        trace.startObject(header.toString());
        trace.endObject();
    }

    /**
     * If the definition of toString is not from java.lang.Object or any class in the
     * java.util.* package then we consider it a custom implementation in which case
     * we'll use it instead of introspecting the class.
     *
     * @param c The class to check for a custom toString definition.
     * @return Whether this class declares a custom toString() method.
     */
    protected boolean hasCustomToStringMethod(Class c)
    {
        try
        {
            Method toStringMethod = c.getMethod("toString", (Class[])null);
            Class declaringClass = toStringMethod.getDeclaringClass();
            if (declaringClass != Object.class
                    && !declaringClass.getName().startsWith("java.util"))
            {
                return true;
            }
        }
        catch (Throwable t)
        {
        }

        return false;
    }

    public Object copy()
    {
        return new BasicPrettyPrinter();
    }
}
