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
package flex.messaging.config;

import flex.messaging.FlexFactory;
import flex.messaging.util.ClassUtil;

/**
 * The factory configuration defines a single factory in the flex
 * configuration file.
 *
 * @author Jeff Vroom
 * @exclude
 */
public class FactorySettings extends PropertiesSettings
{
    protected String id;
    protected String className;

    public FactorySettings(String id, String className)
    {
        this.id = id;
        this.className = className;
    }

    public String getId()
    {
        return id;
    }

    public String getClassName()
    {
        return className;
    }

    public FlexFactory createFactory()
    {
        return createFactory(null);
    }

    public FlexFactory createFactory(ClassLoader cl)
    {
        try
        {
            Class c = ClassUtil.createClass(className, cl);
            Object f = ClassUtil.createDefaultInstance(c, FlexFactory.class);
            if (f instanceof FlexFactory)
            {
                FlexFactory ff = (FlexFactory) f;
                ff.initialize(getId(), getProperties());
                return ff;
            }
            else
            {
                ConfigurationException cx = new ConfigurationException();
                cx.setMessage(11101, new Object[] { className });
                throw cx;
            }
        }
        catch (Throwable th)
        {
            ConfigurationException cx = new ConfigurationException();
            cx.setMessage(11102, new Object[] { className, th.toString() });
            cx.setRootCause(th);
            throw cx;
        }
    }
}
