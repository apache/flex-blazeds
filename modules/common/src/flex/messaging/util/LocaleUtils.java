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

/**
 * @exclude
 */
public class LocaleUtils
{
    /**
     * Builds a <code>Locale</code> instance from the passed string. If the string
     * is <code>null</code> this method will return the default locale for the JVM.
     *
     * @param locale The locale as a string.
     * @return The Locale instance built from the passed string.
     */
    public static Locale buildLocale(String locale)
    {
        if (locale == null)
        {
            return Locale.getDefault();
        }
        else
        {
            int index = locale.indexOf('_');
            if (index == -1)
            {
                return new Locale(locale);
            }
            String language = locale.substring(0, index);
            String rest = locale.substring(index + 1);
            index = rest.indexOf('_');
            if (index == -1)
            {
                return new Locale(language, rest);
            }
            String country = rest.substring(0, index);
            rest = rest.substring(index + 1);
            return new Locale(language, country, rest);
        }
    }
}
