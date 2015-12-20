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

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Decodes an ActionScript object to a Java Map.
 *
 *
 */
public class MapDecoder extends ActionScriptDecoder
{
    public boolean hasShell()
    {
        return true;
    }

    protected boolean isSuitableMap(Object encodedObject, Class desiredClass)
    {
        return (encodedObject != null && encodedObject instanceof Map && desiredClass.isAssignableFrom(encodedObject.getClass()));
    }

    public Object createShell(Object encodedObject, Class desiredClass)
    {
        try
        {
            if (isSuitableMap(encodedObject, desiredClass))
            {
                return encodedObject;
            }
            else
            {
                if (desiredClass.isInterface() || !Map.class.isAssignableFrom(desiredClass))
                {
                    if (SortedMap.class.isAssignableFrom(desiredClass))
                    {
                        return new TreeMap();
                    }
                    else
                    {
                        return new HashMap();
                    }
                }
                else
                {
                    return desiredClass.newInstance();
                }
            }
        }
        catch (Exception e)
        {
            TranslationException ex = new TranslationException("Could not create Map " + desiredClass, e);
            ex.setCode("Server.Processing");
            throw ex;
        }
    }

    public Object decodeObject(Object shell, Object encodedObject, Class desiredClass)
    {
        if (shell == null || encodedObject == null)
            return null;

        // Don't decode if we already have a suitable Map.
        if (isSuitableMap(encodedObject, desiredClass))
        {
            return encodedObject;
        }

        return decodeMap((Map)shell, (Map)encodedObject);
    }

    protected Map decodeMap(Map shell, Map map)
    {
        if (shell != map)
            shell.putAll(map);
        else
            shell = map;

        return shell;
    }
}
