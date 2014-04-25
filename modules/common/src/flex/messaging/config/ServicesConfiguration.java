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

import java.util.List;
import java.util.Map;

/**
 * Base interface for Flex Data Services configuration.
 *
 * Implementations may have different levels of detail
 * based on how much of the configuration is supported.
 *
 * @author pfarland
 * @exclude
 */
public interface ServicesConfiguration
{
    /*
     * CHANNEL CONFIGURATION
     */

    void addChannelSettings(String id, ChannelSettings settings);

    ChannelSettings getChannelSettings(String ref);

    Map getAllChannelSettings();

    /*
     * DEFAULT CHANNELS CONFIGURATION
     */
    void addDefaultChannel(String id);

    List getDefaultChannels();

    /*
     * SERVICE CONFIGURATION
     */

    void addServiceSettings(ServiceSettings settings);

    ServiceSettings getServiceSettings(String id);

    List getAllServiceSettings();

    /*
     * LOGGING CONFIGURATION
     */
    void setLoggingSettings(LoggingSettings settings);

    LoggingSettings getLoggingSettings();

    /* CLUSTER CONFIGURATION */
    ClusterSettings getClusterSettings(String clusterId);

    ClusterSettings getDefaultCluster();
}
