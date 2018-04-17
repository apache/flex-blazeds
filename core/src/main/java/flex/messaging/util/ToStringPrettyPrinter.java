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

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;

import flex.messaging.io.PropertyProxy;
import flex.messaging.io.PropertyProxyRegistry;
import flex.messaging.log.Log;

/**
 * Recursively convert an Object graph into a String
 * for logging/debugging purposes. Cyclical references are
 * handled by tracking known objects.
 * 
 *  TODO: Remove check for custom toString implementations... we
 *  should be able to extend the PrettyPrintable interface to handle
 *  any custom toString requirements without needing to actually call
 *  toString on a custom object.
 */
public class ToStringPrettyPrinter extends BasicPrettyPrinter
{
    private IdentityHashMap knownObjects;
    private int knownObjectsCount;

    /**
     * Default constructor.
     */
    public ToStringPrettyPrinter()
    {
        super();
    }
    
    /** {@inheritDoc} */
    public String prettify(Object o)
    {
        try
        {
            knownObjects = new IdentityHashMap();
            knownObjectsCount = 0;
            return super.prettify(o);    
        }
        finally
        {
            knownObjects = null;
            knownObjectsCount = 0;
        }
    }
    
    /** {@inheritDoc} */
    public Object copy()
    {
        return new ToStringPrettyPrinter();
    }

    protected void prettifyComplexType(Object o)
    {
        // Avoid circular references
        if (!isKnownObject(o))
        {
            StringBuffer header = new StringBuffer(); 

            Class c = o.getClass();

            if (hasCustomToStringMethod(c))
            {
                trace.write(String.valueOf(o));
            }
            else if (o instanceof Collection)
            {
                Collection col = ((Collection)o);
                header.append(c.getName()).append(" (Collection size:").append(col.size()).append(")");
                trace.startArray(header.toString());

                Iterator it = col.iterator();
                int i = 0;
                while (it.hasNext())
                {
                    trace.arrayElement(i);
                    internalPrettify(it.next());
                    trace.newLine();
                    i++;
                }
                
                trace.endArray();
            }
            else if (c.isArray())
            {
                Class componentType = c.getComponentType();
                int count = Array.getLength(o);

                header.append(componentType.getName()).append("[] (Array length:").append(count).append(")");
                trace.startArray(header.toString());
                
                // Check whether it is primitive array case, we omit printing the content of primitive array
                // To avoid cases like large byte array. 
                if (!componentType.isPrimitive())
                {
                    for (int i = 0; i < count; i++)
                    {
                        trace.arrayElement(i);
                        internalPrettify(Array.get(o, i));
                        trace.newLine();
                    }
                }

                trace.endArray();
            }
            else if (o instanceof Document)
            {
                try
                {
                    String xml = XMLUtil.documentToString((Document)o);
                    trace.write(xml);
                }
                catch (IOException ex)
                {
                    trace.write("(Document not printable)");
                }
            }
            else
            {
                PropertyProxy proxy = PropertyProxyRegistry.getProxy(o);
                
                if (o instanceof PrettyPrintable)
                {
                    PrettyPrintable pp = (PrettyPrintable)o;
                    header.append(pp.toStringHeader()); 
                }
                else
                {
                    header.append(c.getName());
                    if (o instanceof Map)
                    {
                        header.append(" (Map size:").append(((Map)o).size()).append(")");
                    }
                }

                trace.startObject(header.toString());

                List propertyNames = proxy.getPropertyNames();
                if (propertyNames != null)
                {
                    Iterator it = propertyNames.iterator();
                    while (it.hasNext())
                    {
                        String propName = (String)it.next();
                        trace.namedElement(propName);
                        
                        Object value = null;
                        if (trace.nextElementExclude)
                        {
                            trace.nextElementExclude = false;
                            value = Log.VALUE_SUPRESSED;
                        }
                        else
                        {
                            if (o instanceof PrettyPrintable)
                            {
                                String customToString = ((PrettyPrintable)o).toStringCustomProperty(propName);
                                if (customToString != null)
                                {
                                    value = customToString;
                                }
                            }


                            if (value == null)
                            {
                                value = proxy.getValue(propName);
                            }
                        }

                        internalPrettify(value);
                        trace.newLine();
                    }
                }

                trace.endObject();
            }
        }
    }
    
    private boolean isKnownObject(Object o)
    {
        Object ref = knownObjects.get(o);
        if (ref != null)
        {
            try
            {
                int refNum = ((Integer)ref).intValue();
                trace.writeRef(refNum);
            }
            catch (ClassCastException e)
            {
                // Ignore
            }
        }
        else
        {
        	addObject(o);
        }
        return (ref != null);
    }
    
    private void addObject(Object o)
    {
        knownObjects.put(o, new Integer(knownObjectsCount++));
    }
}
