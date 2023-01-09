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

import javax.servlet.ServletConfig;

import flex.messaging.log.LogCategories;

/**
 * ConfigurationManager interface
 * <p>
 * The default implementation of the configuration manager is
 * FlexConfigurationManager.  However, this value be specified in
 * a servlet init-param &quot;services.configuration.manager&quot;
 * to the MessageBrokerServlet.
 */
public interface ConfigurationManager {
    String LOG_CATEGORY = LogCategories.CONFIGURATION;

    MessagingConfiguration getMessagingConfiguration(ServletConfig servletConfig);

    void reportTokens();
}
