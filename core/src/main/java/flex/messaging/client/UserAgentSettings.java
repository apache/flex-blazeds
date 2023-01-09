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
package flex.messaging.client;

/**
 * A class to hold user agent specific properties. For example, in streaming
 * endpoints, a certain number of bytes need to be written before the
 * streaming connection can be used and this value is specific to user agents.
 * Similarly, the number of simultaneous connections a session can have is user
 * agent specific.
 */
public class UserAgentSettings {
    /**
     * The prefixes of the version token used by various browsers.
     */
    public static final String USER_AGENT_ANDROID = "Android";
    public static final String USER_AGENT_CHROME = "Chrome";
    public static final String USER_AGENT_FIREFOX = "Firefox";
    public static final String USER_AGENT_FIREFOX_1 = "Firefox/1";
    public static final String USER_AGENT_FIREFOX_2 = "Firefox/2";
    public static final String USER_AGENT_MSIE = "MSIE";
    public static final String USER_AGENT_MSIE_5 = "MSIE 5";
    public static final String USER_AGENT_MSIE_6 = "MSIE 6";
    public static final String USER_AGENT_MSIE_7 = "MSIE 7";
    public static final String USER_AGENT_OPERA = "Opera";
    public static final String USER_AGENT_OPERA_8 = "Opera 8";
    // Opera 10,11 ship as User Agent Opera/9.8.
    public static final String USER_AGENT_OPERA_10 = "Opera/9.8";
    public static final String USER_AGENT_SAFARI = "Safari";

    /**
     * Bytes needed to kickstart the streaming connections for IE.
     */
    public static final int KICKSTART_BYTES_MSIE = 2048;
    /**
     * Bytes needed to kickstart the streaming connections for SAFARI.
     */
    public static final int KICKSTART_BYTES_SAFARI = 512;
    /**
     * Bytes needs to kicksart the streaming connections for Android.
     */
    public static final int KICKSTART_BYTES_ANDROID = 4010;

    /**
     * The default number of persistent connections per session for various browsers.
     */
    private static final int MAX_PERSISTENT_CONNECTIONS_LEGACY = 1;
    public static final int MAX_PERSISTENT_CONNECTIONS_DEFAULT = 5;
    private static final int MAX_PERSISTENT_CONNECTIONS_OPERA_LEGACY = 3;
    private static final int MAX_PERSISTENT_CONNECTIONS_CHROME = MAX_PERSISTENT_CONNECTIONS_DEFAULT;
    private static final int MAX_PERSISTENT_CONNECTIONS_FIREFOX = MAX_PERSISTENT_CONNECTIONS_DEFAULT;
    private static final int MAX_PERSISTENT_CONNECTIONS_MSIE = MAX_PERSISTENT_CONNECTIONS_DEFAULT;
    private static final int MAX_PERSISTENT_CONNECTIONS_OPERA = 7;
    private static final int MAX_PERSISTENT_CONNECTIONS_SAFARI = 3;

    private String matchOn;
    private int kickstartBytes;
    private int maxPersistentConnectionsPerSession = MAX_PERSISTENT_CONNECTIONS_DEFAULT;

    /**
     * Static method to retrieve pre-initialized user agents which are as follows:
     * <p>
     * In Chrome 0, 1, 2, the limit is 6:
     * match-on="Chrome" max-persistent-connections-per-session="5"
     * <p>
     * In Firefox 1, 2, the limit is 2:
     * match-on="Firefox" max-persistent-connections-per-session="1"
     * <p>
     * In Firefox 3, the limit is 6:
     * match-on="Firefox/3" max-persistent-connections-per-session="5"
     * <p>
     * In MSIE 5, 6, 7, the limit is 2 with kickstart bytes of 2K:
     * match-on="MSIE" max-persistent-connections-per-session="1" kickstart-bytes="2048"
     * <p>
     * In MSIE 8, the limit is 6 with kickstart bytes of 2K:
     * match-on="MSIE 8" max-persistent-connections-per-session="5" kickstart-bytes="2048"
     * <p>
     * In Opera 7, 9, the limit is 4:
     * match-on="Opera" max-persistent-connections-per-session="3"
     * <p>
     * In Opera 8, the limit is 8:
     * match-on="Opera 8" max-persistent-connections-per-session="7"
     * <p>
     * In Opera 10, the limit is 8.
     * match-on="Opera 10" max-persistent-connections-per-session="7"
     * <p>
     * In Safari 3, 4, the limit is 4.
     * match-on="Safari" max-persistent-connections-per-session="3"
     *
     * @param matchOn String to use match the agent.
     */
    public static UserAgentSettings getAgent(String matchOn) {
        UserAgentSettings userAgent = new UserAgentSettings();
        userAgent.setMatchOn(matchOn);

        if (USER_AGENT_ANDROID.equals(matchOn)) {
            userAgent.setKickstartBytes(KICKSTART_BYTES_ANDROID);
            userAgent.setMaxPersistentConnectionsPerSession(MAX_PERSISTENT_CONNECTIONS_SAFARI);
        }
        if (USER_AGENT_CHROME.equals(matchOn)) {
            userAgent.setMaxPersistentConnectionsPerSession(MAX_PERSISTENT_CONNECTIONS_CHROME);
        } else if (USER_AGENT_FIREFOX.equals(matchOn)) {
            userAgent.setMaxPersistentConnectionsPerSession(MAX_PERSISTENT_CONNECTIONS_FIREFOX);
        } else if (USER_AGENT_FIREFOX_1.equals(matchOn)) {
            userAgent.setMaxPersistentConnectionsPerSession(MAX_PERSISTENT_CONNECTIONS_LEGACY);
        } else if (USER_AGENT_FIREFOX_2.equals(matchOn)) {
            userAgent.setMaxPersistentConnectionsPerSession(MAX_PERSISTENT_CONNECTIONS_LEGACY);
        } else if (USER_AGENT_MSIE.equals(matchOn)) {
            userAgent.setKickstartBytes(KICKSTART_BYTES_MSIE);
            userAgent.setMaxPersistentConnectionsPerSession(MAX_PERSISTENT_CONNECTIONS_MSIE);
        } else if (USER_AGENT_MSIE_5.equals(matchOn)) {
            userAgent.setKickstartBytes(KICKSTART_BYTES_MSIE);
            userAgent.setMaxPersistentConnectionsPerSession(MAX_PERSISTENT_CONNECTIONS_LEGACY);
        } else if (USER_AGENT_MSIE_6.equals(matchOn)) {
            userAgent.setKickstartBytes(KICKSTART_BYTES_MSIE);
            userAgent.setMaxPersistentConnectionsPerSession(MAX_PERSISTENT_CONNECTIONS_LEGACY);
        } else if (USER_AGENT_MSIE_7.equals(matchOn)) {
            userAgent.setKickstartBytes(KICKSTART_BYTES_MSIE);
            userAgent.setMaxPersistentConnectionsPerSession(MAX_PERSISTENT_CONNECTIONS_LEGACY);
        } else if (USER_AGENT_OPERA.equals(matchOn)) {
            userAgent.setMaxPersistentConnectionsPerSession(MAX_PERSISTENT_CONNECTIONS_OPERA_LEGACY);
        } else if (USER_AGENT_OPERA_8.equals(matchOn)) {
            userAgent.setMaxPersistentConnectionsPerSession(MAX_PERSISTENT_CONNECTIONS_OPERA);
        } else if (USER_AGENT_OPERA_10.equals(matchOn)) {
            userAgent.setMaxPersistentConnectionsPerSession(MAX_PERSISTENT_CONNECTIONS_OPERA);
        } else if (USER_AGENT_SAFARI.equals(matchOn)) {
            userAgent.setKickstartBytes(KICKSTART_BYTES_SAFARI);
            userAgent.setMaxPersistentConnectionsPerSession(MAX_PERSISTENT_CONNECTIONS_SAFARI);
        }
        return userAgent;
    }

    /**
     * Returns the String to use to match the agent.
     *
     * @return The String to use to match the agent.
     */
    public String getMatchOn() {
        return matchOn;
    }

    /**
     * Sets the String to use to match the agent.
     *
     * @param matchOn The String to use to match the agent.
     */
    public void setMatchOn(String matchOn) {
        this.matchOn = matchOn;
    }

    /**
     * Returns the number of bytes needed to kickstart the streaming connections
     * for the user agent.
     *
     * @return The number of bytes needed to kickstart the streaming connections
     * for the user agent.
     */
    public int getKickstartBytes() {
        return kickstartBytes;
    }

    /**
     * Sets the number of bytes needed to kickstart the streaming connections
     * for the user agent.
     *
     * @param kickstartBytes The number of bytes needed to kickstart the streaming
     *                       connections for the user agent.
     */
    public void setKickstartBytes(int kickstartBytes) {
        if (kickstartBytes < 0)
            kickstartBytes = 0;
        this.kickstartBytes = kickstartBytes;
    }

    /**
     * @deprecated Use {@link UserAgentSettings#getMaxPersistentConnectionsPerSession()} instead.
     */
    public int getMaxStreamingConnectionsPerSession() {
        return getMaxPersistentConnectionsPerSession();
    }

    /**
     * @deprecated Use {@link UserAgentSettings#setMaxPersistentConnectionsPerSession(int)} instead.
     */
    public void setMaxStreamingConnectionsPerSession(int maxStreamingConnectionsPerSession) {
        setMaxPersistentConnectionsPerSession(maxStreamingConnectionsPerSession);
    }

    /**
     * Returns the number of simultaneous streaming connections per session
     * the user agent supports.
     *
     * @return The number of streaming connections per session the user agent supports.
     */
    public int getMaxPersistentConnectionsPerSession() {
        return maxPersistentConnectionsPerSession;
    }

    /**
     * Sets the number of simultaneous streaming connections per session
     * the user agent supports.
     *
     * @param maxStreamingConnectionsPerSession The number of simultaneous
     *                                          streaming connections per session the user agent supports.
     */
    public void setMaxPersistentConnectionsPerSession(int maxStreamingConnectionsPerSession) {
        this.maxPersistentConnectionsPerSession = maxStreamingConnectionsPerSession;
    }

}
