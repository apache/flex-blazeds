/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.flex.blazeds.spring;

import flex.messaging.MessageBroker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.flex.core.MessageBrokerFactoryBean;

import javax.servlet.ServletContext;

/**
 * Created by christoferdutz on 21.03.16.
 */
@ConditionalOnWebApplication
@ConditionalOnResource(resources = BlazeDsAutoConfiguration.SERVICES_CONFIG_PATH)
public class BlazeDsAutoConfiguration {

    public static final String SERVICES_CONFIG_PATH = "classpath:/META-INF/flex/services-config.xml";

    @Autowired
    private ServletContext context;

    @Autowired
    private ResourceLoader resourceLoader;

    @Bean
    public MessageBroker messageBroker() throws Exception {
        // We'll use the Spring-Flex-Integration factories.
        MessageBrokerFactoryBean factoryBean = new MessageBrokerFactoryBean();

        // TODO: Do all the special configuration magic here ...
        //factoryBean.setConfigProcessors(null);
        //factoryBean.setConfigurationManager(null);

        // Setup the Spring stuff.
        factoryBean.setResourceLoader(resourceLoader);
        factoryBean.setServletContext(context);

        // The most important option: Where the services-config.xml is located.
        factoryBean.setServicesConfigPath(SERVICES_CONFIG_PATH);

        // This actually internally creates and configures the message broker.
        factoryBean.afterPropertiesSet();

        // Return the instance.
        return factoryBean.getObject();
    }

}
