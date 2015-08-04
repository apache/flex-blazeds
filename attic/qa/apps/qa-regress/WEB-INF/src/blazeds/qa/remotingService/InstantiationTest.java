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
package blazeds.qa.remotingService;

import flex.messaging.io.amf.ASObject;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.ArrayList;


public class InstantiationTest
{
    public String getASObjectType(ASObject obj)
    {
        return obj.getType();
    }

    public String getClassName(Object obj)
    {
        return obj.getClass().getName();
    }
    public Map getTypedObjectASObjectTypes(ASObject aso)
    {
        Map typeMap = new HashMap();
        typeMap.put("main", aso.getType());
        Object obj = aso.get("theCollection");
        Object[] theCollection = (Object[]) obj;
        typeMap.put("theCollection0", ((ASObject)theCollection[0]).getType());
        typeMap.put("theCollection1", ((ASObject)theCollection[1]).getType());
        typeMap.put("theCollection2.0", ((ASObject)((ArrayList)theCollection[2]).get(0)).getType());
        typeMap.put("theCollection2.1", ((ASObject)((ArrayList)theCollection[2]).get(1)).getType());
        typeMap.put("mapbook", ((ASObject)((ASObject) aso.get("map")).get("book")).getType());
        typeMap.put("hashmap", ((ASObject)((ASObject) aso.get("map")).get("hashmap")).getType());
        typeMap.put("me", ((ASObject) aso.get("me")).getType());
		typeMap.put("map", ((ASObject) aso.get("map")).getType());
        return typeMap;
    }

}
