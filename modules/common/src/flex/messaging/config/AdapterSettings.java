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

/**
 * A service must register the adapters that it will use
 * to process messages. Each destination selects an adapter
 * that processes the request by referring to it by id.
 * <p>
 * Adapters can also be configured with initialization
 * properties.
 * </p>
 *
 * @see flex.messaging.services.ServiceAdapter
 * @author Peter Farland
 * @exclude
 */
public class AdapterSettings extends PropertiesSettings
{
    private final String id;
    private String sourceFile;
    private String className;
    private boolean defaultAdapter;

    /**
     * Used to construct a new set of properties to describe an adapter. Note
     * that an identity is required in order for destinations to refer to this
     * adapter.
     *
     * @param id the <code>String</code> representing the unique identity for
     * this adapter.
     */
    public AdapterSettings(String id)
    {
        super();
        this.id = id;
    }

    /**
     * The identity that destinations will refer to when assigning and adapter.
     *
     * @return the adapter identity as a <code>String</code>.
     */
    public String getId()
    {
        return id;
    }

    /**
     * Gets the name of the Java class implementation for this adapter.
     *
     * @return String The name of the adapter implementation.
     * @see flex.messaging.services.ServiceAdapter
     */
    public String getClassName()
    {
        return className;
    }

    /**
     * Sets name of the Java class implementation for this adapter. The
     * implementation is resolved from the current classpath and must extend
     * <code>flex.messaging.services.ServiceAdapter</code>.
     *
     * @param name the <code>String</code>
     */
    public void setClassName(String name)
    {
        className = name;
    }

    /**
     * Returns a boolean flag that determines whether this adapter is the
     * default for a service's destinations. Only one default adapter can be
     * set for a given service.
     *
     * @return boolean true if this adapter is the default.
     */
    public boolean isDefault()
    {
        return defaultAdapter;
    }

    /**
     * Sets a flag to determine whether an adapter will be used as the default
     * (for example, in the event that a destination did not specify an adapter
     * explicitly).
     *
     * Only one default can be set for a given service.
     *
     * @param b a <code>boolean</code> flag, true if this adapter should be
     * used as the default for the service.
     */
    public void setDefault(boolean b)
    {
        defaultAdapter = b;
    }

    /**
     * Internal use only.
     * @exclude
     */
    String getSourceFile()
    {
        return sourceFile;
    }

    /**
     * Internal use only.
     * @exclude
     */
    void setSourceFile(String file)
    {
        this.sourceFile = file;
    }
}
