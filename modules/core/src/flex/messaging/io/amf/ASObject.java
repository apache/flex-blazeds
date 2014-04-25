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
package flex.messaging.io.amf;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ASObject extends HashMap
{
    static final long serialVersionUID = 1613529666682805692L;
    private boolean inHashCode = false;
    private boolean inToString = false;

    String namedType = null;

    public ASObject()
    {
        super();
    }

    public ASObject(String name)
    {
        super();
        namedType = name;
    }

    public String getType()
    {
        return namedType;
    }

    public void setType(String type)
    {
        namedType = type;
    }

    public int hashCode()
    {
        int h = 0;
        if (!inHashCode)
        {
            inHashCode = true;
            try
            {
                Iterator i = entrySet().iterator();
                while (i.hasNext())
                {
                    h += i.next().hashCode();
                }
            }
            finally
            {
                inHashCode = false;
            }
        }
        return h;
    }

    public String toString()
    {
        String className = getClass().getName();
        int dotIndex = className.lastIndexOf('.');

        StringBuffer buffer = new StringBuffer();
        buffer.append(className.substring(dotIndex + 1));
        buffer.append('(').append(System.identityHashCode(this)).append(')');
        buffer.append('{');
        if (!inToString)
        {
            inToString = true;
            try
            {
                boolean pairEmitted = false;

                Iterator i = entrySet().iterator();
                while (i.hasNext())
                {
                    if (pairEmitted)
                    {
                        buffer.append(", ");
                    }
                    Map.Entry e = (Map.Entry) (i.next());
                    buffer.append(e.getKey()).append('=').append(e.getValue());
                    pairEmitted = true;
                }
            }
            finally
            {
                inToString = false;
            }
        }
        else
        {
            buffer.append("...");
        }
        buffer.append('}');
        return buffer.toString();
    }
}
