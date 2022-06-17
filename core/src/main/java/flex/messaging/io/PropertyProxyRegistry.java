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

import flex.messaging.LocalizedException;
import flex.messaging.MessageException;
import flex.messaging.io.amf.ASObject;

import javax.sql.RowSet;
import java.util.AbstractMap;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Allows custom PropertyProxy's to be registered on a Class basis.
 * <p>
 * Class hierarchies can be optionally searched with the first match winning.
 * The search starts by trying an exact Class match, then the immediate
 * interfaces are tried in the order that they are declared on the Class and
 * finally the process is repeated for the superclass, if one exists. If a
 * PropertyProxy is found in the immediate parent hierarchy (either the
 * immediate superclass or directly implemented interfaces) then the
 * implementing class is registered with the selected PropertyProxy to optimize
 * subsequent searches.
 */
public class PropertyProxyRegistry {
    private final Map<Class, PropertyProxy> classRegistry = new IdentityHashMap<Class, PropertyProxy>();

    /**
     * A global registry that maps a Class type to a PropertyProxy.
     */
    private static final PropertyProxyRegistry registry = new PropertyProxyRegistry();

    static {
        preRegister();
    }

    /**
     * Constructs an empty PropertyProxy registry.
     */
    public PropertyProxyRegistry() {
        // No-op.
    }

    /**
     * Returns the static or "application scope" PropertyProxy registry. If
     * custom sets of PropertyProxies are required in different scopes then
     * new instances of PropertyProxyRegistry should be manually created,
     * however these will not be used for serialization.
     *
     * @return The global PropertyProxy registry.
     */
    public static PropertyProxyRegistry getRegistry() {
        return registry;
    }

    /**
     * Call this on Message broker shutdown ONLY.
     * Clears the registry and removes the static global registry.
     */
    public static void release() {
        registry.clear();
        preRegister();  // init for restart
    }


    /**
     * Pre-registers a few common types that are often proxied to
     * speed up lookups.
     */
    private static void preRegister() {
        ThrowableProxy proxy = new ThrowableProxy();
        registry.register(MessageException.class, proxy);
        registry.register(LocalizedException.class, proxy);
        registry.register(Throwable.class, proxy);

        MapProxy mapProxy = new MapProxy();
        registry.register(ASObject.class, mapProxy);
        registry.register(HashMap.class, mapProxy);
        registry.register(AbstractMap.class, mapProxy);
        registry.register(Map.class, mapProxy);
    }

    /**
     * Returns a PropertyProxy suitable for the given instance and registers
     * the selected PropertyProxy for the Class of the instance. Note that
     * the PropertyProxy is not cloned so either the PropertyProxy should be
     * used as a template else you must first call clone() on the returned
     * PropertyProxy and then set the instance as the default on the resulting
     * clone.
     *
     * @param instance the type to search for a suitable PropertyProxy.
     * @return PropertyProxy suitable for the instance type.
     */
    public static PropertyProxy getProxyAndRegister(Object instance) {
        if (instance instanceof PropertyProxy)
            return (PropertyProxy) instance;

        Class c = instance.getClass();
        PropertyProxy proxy = getRegistry().getProxyAndRegister(c);

        if (proxy == null) {
            proxy = guessProxy(instance);
            getRegistry().register(c, proxy);
        }

        return proxy;
    }

    /**
     * Returns a PropertyProxy suitable for the given instance but does not
     * register the selected PropertyProxy for the Class of the instance. Note
     * that the PropertyProxy is not cloned so either the PropertyProxy should
     * be used as a template else you must first call clone() on the returned
     * PropertyProxy and then set the instance as the default on the resulting
     * clone.
     *
     * @param instance the type to search for a suitable PropertyProxy.
     * @return PropertyProxy suitable for the instance type.
     */
    public static PropertyProxy getProxy(Object instance) {
        if (instance instanceof PropertyProxy)
            return (PropertyProxy) instance;

        Class c = instance.getClass();
        PropertyProxy proxy = getRegistry().getProxy(c);

        if (proxy == null) {
            proxy = guessProxy(instance);
        }

        proxy = (PropertyProxy) proxy.clone();
        proxy.setDefaultInstance(instance);
        return proxy;
    }

    /**
     * Attempts to select a suitable proxy for the given instance
     * based on common type mappings.
     *
     * @param instance the object to examine.
     * @return a proxy for getting properties.
     */
    private static PropertyProxy guessProxy(Object instance) {
        PropertyProxy proxy;

        if (instance instanceof Map) {
            proxy = new MapProxy();
        } else if (instance instanceof Throwable) {
            proxy = new ThrowableProxy();
        } else if (instance instanceof PageableRowSet || instance instanceof RowSet) {
            proxy = new PageableRowSetProxy();
        } else if (instance instanceof Dictionary) {
            proxy = new DictionaryProxy();
        } else {
            proxy = new BeanProxy();
        }

        return proxy;
    }

    /**
     * Locates a custom PropertyProxy for the given Class. The entire class
     * hierarchy is searched. Even if a match is found in the class heirarchy
     * the PropertyProxy is not registered for the given Class.
     *
     * @param c the Class used to search the registry.
     * @return the custom PropertyProxy registered for the Class or
     * null if a PropertyProxy was not found or if the given Class is null.
     */
    public PropertyProxy getProxy(Class c) {
        return getProxy(c, true, false);
    }

    /**
     * Locates a custom PropertyProxy for the given Class. The entire class
     * hierarchy is searched. If a match is found in the class heirarchy the
     * PropertyProxy is registered for the given class.
     *
     * @param c the Class used to search the registry.
     * @return the custom PropertyProxy registered for the Class or
     * null if a PropertyProxy was not found or if the given Class is null.
     */
    public PropertyProxy getProxyAndRegister(Class c) {
        return getProxy(c, true, true);
    }

    /**
     * Locates a custom PropertyProxy for the given Class. If the
     * searchHierarchy argument is true the search starts by trying an exact
     * Class match, then the immediate interfaces are tried in the order that
     * they are declared on the Class and finally the process is repeated for
     * the superclass, if one exists.
     *
     * @param c               the Class used to search the registry.
     * @param searchHierarchy if true the entire class hierarchy is searched.
     * @param autoRegister    if true a successful match is registerd for top
     *                        level class
     * @return the custom PropertyProxy registered for the Class or
     * null if a PropertyProxy was not found or if the given Class is null.
     */
    public PropertyProxy getProxy(Class c, boolean searchHierarchy, boolean autoRegister) {
        if (c == null)
            return null;

        // Check for native Array so we can unwrap for Class component type
        if (c.isArray())
            c = c.getComponentType();

        // Locate PropertyProxy by Class reference
        PropertyProxy proxy = null;
        synchronized (classRegistry) {
            proxy = classRegistry.get(c);
        }

        if (proxy == null && searchHierarchy) {
            // Next, try matching PropertyProxy by interface
            Class[] interfaces = c.getInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
                Class interfaceClass = interfaces[i];
                synchronized (classRegistry) {
                    proxy = classRegistry.get(interfaceClass);
                }
                if (proxy != null && autoRegister) {
                    register(c, proxy);
                    break;
                } else {
                    // Recursively check super interfaces too
                    proxy = getProxy(interfaceClass, searchHierarchy, autoRegister);
                    if (proxy != null) {
                        break;
                    }
                }
            }
        }

        if (proxy == null && searchHierarchy) {
            // Finally, recursively search superclass hierarchy
            Class superclass = c.getSuperclass();
            if (superclass != null) {
                proxy = getProxy(superclass, searchHierarchy, autoRegister);
                if (proxy != null && autoRegister) {
                    register(c, proxy);
                }
            }
        }

        return proxy;
    }

    /**
     * Removes all items from the class registry.
     */
    public void clear() {
        synchronized (classRegistry) {
            classRegistry.clear();
        }
    }

    /**
     * Register a custom PropertyProxy for a Class.
     *
     * @param c     The key for the class registry.
     * @param proxy The custom PropertyProxy implementation.
     */
    public void register(Class c, PropertyProxy proxy) {
        synchronized (classRegistry) {
            classRegistry.put(c, proxy);
        }
    }

    /**
     * Removes a custom PropertyProxy from the registry.
     *
     * @param c The Class to be removed from the registry.
     */
    public void unregister(Class c) {
        synchronized (classRegistry) {
            classRegistry.remove(c);
        }
    }
}
