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

import flex.messaging.services.messaging.MessagingConstants;

/**
 * Server settings for a message destination.
 */
public class ServerSettings
{
    // Errors
    private static final int INVALID_CLUSTER_MESSAGE_ROUTING = 11121;

    private boolean allowSubtopics;
    private boolean disallowWildcardSubtopics;
    private long messageTTL = -1; //We need to keep track of uninitialized value
    private boolean isDurable;
    private int priority = -1;
    private String subtopicSeparator;
    private RoutingMode routingMode = RoutingMode.SERVER_TO_SERVER;
    
    public static enum RoutingMode {NONE,BROADCAST,SERVER_TO_SERVER};

    /**
     * Creates a default <code>ServerSettings</code> instance.
     */
    public ServerSettings()
    {
        isDurable = false;
        subtopicSeparator = MessagingConstants.DEFAULT_SUBTOPIC_SEPARATOR;
    }

    /**
     * Returns <code>allow-subtopics</code> property.
     *
     * @return a boolean specifying whether or not the server has the <code>allow-subtopics</code> property.
     */
    public boolean getAllowSubtopics()
    {
        return allowSubtopics;
    }

    /**
     * Sets <code>allow-subtopics</code> property.
     *
     * @param value The value for <code>allow-subtopics</code> property.
     */
    public void setAllowSubtopics(boolean value)
    {
        allowSubtopics = value;
    }

    /**
     * Returns <code>cluster-message-routing</code> property.
     *
     * @return a boolean specifying whether or not the server has the <code>cluster-message-routing</code> property.
     */
    public boolean isBroadcastRoutingMode()
    {
        return routingMode == RoutingMode.BROADCAST;
    }

    /**
     * Returns the routing mode configured.
     * 
     * @return The configured routing mode.
     */
    public RoutingMode getRoutingMode()
    {
        return routingMode;
    }
    
    /**
     * Sets the <code>cluster-message-routing</code> property.
     *
     * @param routingMode <code>server-to-server</code>(default)
     * or <code>broadcast</code>.
     */
    public void setBroadcastRoutingMode(String routingMode)
    {
        if (routingMode.equalsIgnoreCase("broadcast"))
            this.routingMode = RoutingMode.BROADCAST;
        else if (routingMode.equalsIgnoreCase("server-to-server"))
            this.routingMode = RoutingMode.SERVER_TO_SERVER;
        else if (routingMode.equalsIgnoreCase("none"))
            this.routingMode = RoutingMode.NONE;
        else
        {
            ConfigurationException ce = new ConfigurationException();
            ce.setMessage(INVALID_CLUSTER_MESSAGE_ROUTING, new Object[] {routingMode});
            throw ce;
        }
    }

    /**
     * @deprecated Not used anymore.
     */
    public int getMaxCacheSize()
    {
        return 0;
    }

    /**
     * @deprecated Not used anymore.
     */
    public void setMaxCacheSize(int size)
    {
        // No-op.
    }

    /**
     * Returns the <code>message-time-to-live</code> property.
     *
     * @return the <code>message-time-to-live</code> property.
     */
    public long getMessageTTL()
    {
        return messageTTL;
    }

    /**
     * Sets the <code>message-time-to-live</code> property. Default value is -1.
     *
     * @param ttl The value for <code>message-time-to-live</code> property.
     */
    public void setMessageTTL(long ttl)
    {
        messageTTL = ttl;
    }

    /**
     * Returns true if wildcard subtopics are disallowed.
     *
     * @return true if wilcard subtopics are disallowed.
     */
    public boolean isDisallowWildcardSubtopics()
    {
        return disallowWildcardSubtopics;
    }

    /**
     * Sets whether the wildcard subtopics are disallowed.
     *
     * @param value Whether the wildcard subtopics are disallowed.
     */
    public void setDisallowWildcardSubtopics(boolean value)
    {
        disallowWildcardSubtopics = value;
    }

    /**
     * Returns whether destination is durable.
     *
     * @return <code>true</code> if destination is durable; otherwise <code>false</code>.
     */
    public boolean isDurable()
    {
        return isDurable;
    }

    /**
     * Sets whether destination is durable. Default value is <code>false</code>.
     *
     * @param durable The value for <code>durable</code> property.
     */
    public void setDurable(boolean durable)
    {
        isDurable = durable;
    }

    /**
     * Returns the default message priority for the destination which is a numerical
     * value between 0 and 9 (and -1 means no default priority).
     * 
     * @return The default message priority for the destination.
     */
    public int getPriority()
    {
        return priority;
    }

    /**
     * Sets the default message priority of the destination which should be 
     * a numerical value between 0 and 9. Values less than 0 and greater than 9
     * are treated as 0 and 9 respectively.
     * 
     * @param priority The new value for the priority.
     */
    public void setPriority(int priority)
    {
        priority = priority < 0? 0 : priority > 9? 9 : priority;
        this.priority = priority;
    }

    
    /**
     * Returns the <code>subtopic-separator</code> property.
     *
     * @return the <code>subtopic-separator</code> property.
     */
    public String getSubtopicSeparator()
    {
        return subtopicSeparator;
    }

    /**
     * Sets the <code>subtopic-separator</code> property.
     * Optional; the default value is period.
     *
     * @param value The value for <code>subtopic-separator</code> property.
     */
    public void setSubtopicSeparator(String value)
    {
        subtopicSeparator = value;
    }
}
