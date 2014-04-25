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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A service represents a high-level grouping of
 * functionality to which the message broker can
 * delegate messages. Services specify which
 * message types they're interested in and use
 * adapters to carry out a message's for a
 * destination.
 * <p>
 * A service maintains a list of destinations which
 * effectively represents a &quot;whitelist&quot;
 * of actions allowed by that service.
 * </p>
 *
 * @author Peter Farland
 * @exclude
 */
public class ServiceSettings extends PropertiesSettings
{
    private final String id;
    private String sourceFile;
    private String className;

    private AdapterSettings defaultAdapterSettings;
    private final Map adapterSettings;
    private final List defaultChannels;
    private final Map destinationSettings;
    private SecurityConstraint securityConstraint;

    public ServiceSettings(String id)
    {
        this.id = id;
        destinationSettings = new HashMap();
        adapterSettings = new HashMap(2);
        defaultChannels = new ArrayList(4);
    }

    public String getId()
    {
        return id;
    }

    String getSourceFile()
    {
        return sourceFile;
    }

    void setSourceFile(String sourceFile)
    {
        this.sourceFile = sourceFile;
    }

    public String getClassName()
    {
        return className;
    }

    public void setClassName(String name)
    {
        className = name;
    }

    /*
     *  SERVER ADAPTERS
     */
    public AdapterSettings getDefaultAdapter()
    {
        return defaultAdapterSettings;
    }

    public AdapterSettings getAdapterSettings(String id)
    {
        return (AdapterSettings)adapterSettings.get(id);
    }

    public Map getAllAdapterSettings()
    {
        return adapterSettings;
    }

    public void addAdapterSettings(AdapterSettings a)
    {
        adapterSettings.put(a.getId(), a);
        if (a.isDefault())
        {
            defaultAdapterSettings = a;
        }
    }

    /*
     *  DEFAULT CHANNELS
     */
    public void addDefaultChannel(ChannelSettings c)
    {
        defaultChannels.add(c);
    }

    public List getDefaultChannels()
    {
        return defaultChannels;
    }

    /*
     *  DEFAULT SECURITY
     */

    /**
     * Gets the <code>SecurityConstraint</code> that will be applied to all
     * destinations of the service, or <code>null</code> if no constraint has
     * been registered.
     *
     * @return the <code>SecurityConstraint</code> for this service.
     */
    public SecurityConstraint getConstraint()
    {
        return securityConstraint;
    }

    /**
     * Sets the security constraint to be applied to all destinations of the service.
     * Security constraints restrict which clients can contact this destination. Use
     * <code>null</code> to remove an existing constraint.
     *
     * @param sc the <code>SecurityConstraint</code> to apply to this
     * service.
     */
    public void setConstraint(SecurityConstraint sc)
    {
        securityConstraint = sc;
    }

    /*
     *  DESTINATIONS
     */
    public Map getDestinationSettings()
    {
        return destinationSettings;
    }

    public void addDestinationSettings(DestinationSettings dest)
    {
        destinationSettings.put(dest.getId(), dest);
    }
}
