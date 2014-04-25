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

import flex.messaging.client.UserAgentSettings;
import flex.messaging.config.ConfigMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages user agent information.
 */
public class UserAgentManager
{
    /**
     * The name of the HTTP header that transports the user agent value.
     */
    public static final String USER_AGENT_HEADER_NAME = "User-Agent";

    /*
    * Configuration constants
    */
    private static final String KICKSTART_BYTES = "kickstart-bytes";
    private static final String MATCH_ON = "match-on";
    // max-streaming-connections-per-session is deprecated; use max-persistent-connections-per-session instead.
    public static final String MAX_STREAMING_CONNECTIONS_PER_SESSION = "max-streaming-connections-per-session";
    public static final String MAX_PERSISTENT_CONNECTIONS_PER_SESSION = "max-persistent-connections-per-session";
    private static final String USER_AGENT = "user-agent";
    private static final String USER_AGENT_SETTINGS = "user-agent-settings";

    /**
     * Used to keep track of the mapping between user agent match strings and
     * the settings object.
     */
    private Map<String, UserAgentSettings> userAgentSettingsMap = new HashMap<String, UserAgentSettings>();

    /**
     * Default settings, where match="*".
     */
    private UserAgentSettings defaultSettings;

    /**
     * Given a match-on string, returns the settings for that user agent, or
     * null if no user agent settings exist for that match-on string.
     *
     * @param matchOn The match-on string used to match a specific user agent.
     * @return The settings for that user agent, or null if no user agent settings
     *         exist for that match-on string.
     */
    public UserAgentSettings getUserAgentSettings(String matchOn)
    {
        return userAgentSettingsMap.get(matchOn);
    }

    /**
     * Returns the collection of user agent settings.
     * Collection can be empty.
     *
     * @return The (possibly empty) collection of user agent settings.
     */
    public Collection<UserAgentSettings> getUserAgentSettings()
    {
        return userAgentSettingsMap.values();
    }

    /**
     * Puts a new user agent to the existing list of user agents. If an existing
     * user agent with the same match-on property as the new user agent exists,
     * it is simply overwritten.
     * @param userAgent the UserAgentSettings
     */
    public void putUserAgentSettings(UserAgentSettings userAgent)
    {
        userAgentSettingsMap.put(userAgent.getMatchOn(), userAgent);
    }

    /**
     * Removes the user agent with the same match-on property from the list of
     * existing user agents.
     * @param useAgent the UserAgentSettings object
     */
    public void removeUserAgentSettings(UserAgentSettings userAgent)
    {
        if (userAgent != null)
            userAgentSettingsMap.remove(userAgent.getMatchOn());
    }

    /**
     * Set the default settings to return when there is no match found.
     * @param settings the UserAgentSettings object
     */
    public void setDefaultUserAgentSettings(UserAgentSettings settings)
    {
        defaultSettings = settings;
    }

    /**
     * Look for the best match (based on longest match) for a user agent.
     * If no match is found, the default settings are returned if set.
     *
     * @param userAgent a user agent to look for
     * @return settings or null if no match and no default.
     * @see UserAgentManager#setDefaultUserAgentSettings(flex.messaging.client.UserAgentSettings)
     */
    public UserAgentSettings match(String userAgent)
    {
        // Dearch for the longest match
        if (userAgent != null && userAgentSettingsMap.size() > 0)
        {
            // Search for the best match based upon length.
            int bestMatchLength = 0;
            String matchedAgent = null;
            for (String key : userAgentSettingsMap.keySet())
            {
                if (userAgent.indexOf(key) != -1)
                {
                    int matchLength = key.length();
                    if (matchLength > bestMatchLength)
                    {
                        bestMatchLength = matchLength;
                        matchedAgent = key;
                    }
                }
            }

            if (matchedAgent != null)
                return userAgentSettingsMap.get(matchedAgent);
        }

        // Return default if we have one
        return defaultSettings;
    }

    /**
     * Initializes the provided manager with settings from the property map.
     * Sets default settings if it encounters a match="*" entry.
     *
     * @param properties configuration properties, possibly containing "user-agent-settings" element
     * @param manager    the UserAgentManager to configure
     */
    public static void setupUserAgentManager(ConfigMap properties, UserAgentManager manager)
    {
        // Add default entries for user agents.
        manager.putUserAgentSettings(UserAgentSettings.getAgent(UserAgentSettings.USER_AGENT_ANDROID));
        manager.putUserAgentSettings(UserAgentSettings.getAgent(UserAgentSettings.USER_AGENT_CHROME));
        manager.putUserAgentSettings(UserAgentSettings.getAgent(UserAgentSettings.USER_AGENT_FIREFOX));
        manager.putUserAgentSettings(UserAgentSettings.getAgent(UserAgentSettings.USER_AGENT_FIREFOX_1));
        manager.putUserAgentSettings(UserAgentSettings.getAgent(UserAgentSettings.USER_AGENT_FIREFOX_2));
        manager.putUserAgentSettings(UserAgentSettings.getAgent(UserAgentSettings.USER_AGENT_MSIE));
        manager.putUserAgentSettings(UserAgentSettings.getAgent(UserAgentSettings.USER_AGENT_MSIE_5));
        manager.putUserAgentSettings(UserAgentSettings.getAgent(UserAgentSettings.USER_AGENT_MSIE_6));
        manager.putUserAgentSettings(UserAgentSettings.getAgent(UserAgentSettings.USER_AGENT_MSIE_7));
        manager.putUserAgentSettings(UserAgentSettings.getAgent(UserAgentSettings.USER_AGENT_OPERA));
        manager.putUserAgentSettings(UserAgentSettings.getAgent(UserAgentSettings.USER_AGENT_OPERA_8));
        manager.putUserAgentSettings(UserAgentSettings.getAgent(UserAgentSettings.USER_AGENT_OPERA_10));
        manager.putUserAgentSettings(UserAgentSettings.getAgent(UserAgentSettings.USER_AGENT_SAFARI));

        if (properties == null)
            return;

        ConfigMap userAgents = properties.getPropertyAsMap(USER_AGENT_SETTINGS, null);
        if (userAgents == null)
            return;

        List userAgent = userAgents.getPropertyAsList(USER_AGENT, null);
        if (userAgent == null || userAgent.size() == 0)
            return;

        for (Object anUserAgent : userAgent)
        {
            ConfigMap agent = (ConfigMap)anUserAgent;
            String matchOn = agent.getPropertyAsString(MATCH_ON, null);
            if (matchOn == null)
                continue;

            int kickstartBytes = agent.getPropertyAsInt(KICKSTART_BYTES, 0);
            int connectionsPerSession = agent.getPropertyAsInt(MAX_PERSISTENT_CONNECTIONS_PER_SESSION,
                    agent.getPropertyAsInt(MAX_STREAMING_CONNECTIONS_PER_SESSION, UserAgentSettings.MAX_PERSISTENT_CONNECTIONS_DEFAULT));

            UserAgentSettings ua = UserAgentSettings.getAgent(matchOn);
            ua.setKickstartBytes(kickstartBytes);
            ua.setMaxPersistentConnectionsPerSession(connectionsPerSession);
            if (matchOn.equals("*"))
                manager.setDefaultUserAgentSettings(ua);
            else
                manager.putUserAgentSettings(ua);
        }
    }

}
