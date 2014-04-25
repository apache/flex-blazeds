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

import java.util.HashMap;
import java.util.Map;

/**
 * A simple registry that maps an alias to a concrete class name. This registry
 * mimics the ActionScript 3 flash.net.registerClassAlias() functionality
 * of the Flash Player. The registry is checked when deserializing AMF object
 * types.
 */
public class ClassAliasRegistry
{
    private Map aliasRegistry = new HashMap();
    private static final ClassAliasRegistry registry = new ClassAliasRegistry();

    /**
     * Constructs an empty registry.
     */
    private ClassAliasRegistry()
    {
    }

    /**
     * Returns the registry singleton.
     */
    public static ClassAliasRegistry getRegistry()
    {
        return registry;
    }

    /**
     * Looks for a concrete class name for an alias.
     * 
     * @param alias The alias used to search the registry. 
     * @return a concrete class name, if registered for this alias, otherwise
     * null.
     */
    public String getClassName(String alias)
    {
        return (String)aliasRegistry.get(alias);
    }

    /**
     * Clears all items from the registry.
     */
    public void clear()
    {
        synchronized(aliasRegistry)
        {
            aliasRegistry.clear();
        }
    }

    /**
     * Registers a custom alias for a class name.
     * 
     * @param alias The alias for the class name.
     * @param className The concrete class name.
     */
    public void registerAlias(String alias, String className)
    {
        synchronized(aliasRegistry)
        {
            aliasRegistry.put(alias, className);
        }
    }

    /**
     * Removes a class alias from the registry.
     * 
     * @param alias The alias to be removed from the registry.
     */
    public void unregisterAlias(String alias)
    {
        synchronized(aliasRegistry)
        {
            aliasRegistry.remove(alias);
        }
    }
}
