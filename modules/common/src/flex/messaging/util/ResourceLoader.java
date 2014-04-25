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

import java.util.Locale;
import java.util.Map;

/**
 * The root interface for classes that provide access to localized resources.
 *
 * @author Seth Hodgson
 * @exclude
 */
public interface ResourceLoader
{
    /**
     * Initializes the <code>ResourceLoader</code> using the specified properties.
     *
     * @param properties The initialization properties.
     */
    void init(Map properties);

    /**
     * Sets the default locale to be used when locating resources. The
     * string will be converted into a Locale.
     *
     * @param locale The default locale to be used.
     */
    void setDefaultLocale(String locale);

    /**
     * Sets the default locale to be used when locating resources.
     *
     * @param locale The default locale to be used.
     */
    void setDefaultLocale(Locale locale);

    /**
     * The default locale to be used when locating resources.
     *
     * @return The default locale.
     */
    Locale getDefaultLocale();

    /**
     * Gets a string for the given key.
     *
     * @param key The key for the target string.
     * @return The string for the given key.
     */
    String getString(String key);

    /**
     * Gets a parameterized string for the given key and substitutes
     * the parameters using the passed array of arguments.
     *
     * @param key The key for the target string.
     * @param arguments The arguments to substitute into the parameterized string.
     * @return The substituted string for the given key.
     * @exception IllegalArgumentException If the parameterized string is invalid,
     *            or if an argument in the <code>arguments</code> array
     *            is not of the type expected by the format element(s)
     *            that use it.
     */
    String getString(String key, Object[] arguments);

    /**
     * Gets a string for the given key and locale.
     *
     * @param key The key for the target string.
     * @param locale The target locale for the string.
     * @return The localized string for the given key.
     */
    String getString(String key, Locale locale);

    /**
     * Gets a parameterized string for the given key and locale and substitutes the
     * parameters using the passed array of arguments.
     *
     * @param key The key for the target string.
     * @param locale The target locale for the string.
     * @param arguments The arguments to substitute into the parameterized string.
     * @return The substituted localized string for the given key.
     * @exception IllegalArgumentException If the parameterized string is invalid,
     *            or if an argument in the <code>arguments</code> array
     *            is not of the type expected by the format element(s)
     *            that use it.
     */
    String getString(String key, Locale locale, Object[] arguments);

}
