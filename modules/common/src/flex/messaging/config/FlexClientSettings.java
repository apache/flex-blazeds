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
 * @exclude
 */
public class FlexClientSettings extends PropertiesSettings
{
    //--------------------------------------------------------------------------
    //
    // Constructor
    //
    //--------------------------------------------------------------------------

    /**
     * Constructs a FlexClientSettings instance.
     */
    public FlexClientSettings()
    {
        // Empty for now.
    }

    //--------------------------------------------------------------------------
    //
    // Properties
    //
    //--------------------------------------------------------------------------

    private long timeoutMinutes = -1;

    /**
     * Returns the number of minutes before an idle FlexClient is timed out.
     *
     * @return The number of minutes before an idle FlexClient is timed out.
     */
    public long getTimeoutMinutes()
    {
        return timeoutMinutes;
    }

    /**
     * Sets the number of minutes before an idle FlexClient is timed out.
     *
     * @param value The number of minutes before an idle FlexClient is timed out.
     */
    public void setTimeoutMinutes(long value)
    {
        timeoutMinutes = value;
    }

    private String flexClientOutboundQueueProcessorClassName;

    /**
     * Returns the name of the default <code>FlexClientOutboundQueueProcessorClass</code>.
     *
     * @return The the name of the  default <code>FlexClientOutboundQueueProcessorClass</code>.
     */
    public String getFlexClientOutboundQueueProcessorClassName()
    {
        return flexClientOutboundQueueProcessorClassName;
    }

    /**
     * Sets the name of the default <code>FlexClientOutboundQueueProcessor</code>.
     *
     * @param flexClientOutboundQueueProcessorClassName The name of the default <code>FlexClientOutboundQueueProcessor</code>.
     */
    public void setFlexClientOutboundQueueProcessorClassName(String flexClientOutboundQueueProcessorClassName)
    {
        this.flexClientOutboundQueueProcessorClassName = flexClientOutboundQueueProcessorClassName;
    }

    private ConfigMap flexClientOutboundQueueProcessorProperties;

    /**
     * Returns the properties for the default <code>FlexClientOutboundQueueProcessor</code>.
     *
     * @return The properties for the default <code>FlexClientOutboundQueueProcessor</code>.
     */
    public ConfigMap getFlexClientOutboundQueueProcessorProperties()
    {
        return flexClientOutboundQueueProcessorProperties;
    }

    /**
     * Sets the properties for the default <code>FlexClientOutboundQueueProcessor</code>.
     *
     * @param flexClientOutboundQueueProcessorProperties
     */
    public void setFlexClientOutboundQueueProcessorProperties(ConfigMap flexClientOutboundQueueProcessorProperties)
    {
        this.flexClientOutboundQueueProcessorProperties = flexClientOutboundQueueProcessorProperties;
    }
    
    private int reliableReconnectDurationMillis;
    
    public int getReliableReconnectDurationMillis()
    {
        return reliableReconnectDurationMillis;    
    }
    
    public void setReliableReconnectDurationMillis(int value)
    {
        reliableReconnectDurationMillis = value;
    }
    
    private int heartbeatIntervalMillis;
    
    public int getHeartbeatIntervalMillis()
    {
        return heartbeatIntervalMillis;
    }
    
    public void setHeartbeatIntervalMillis(int value)
    {
        heartbeatIntervalMillis = value;
    }
}
