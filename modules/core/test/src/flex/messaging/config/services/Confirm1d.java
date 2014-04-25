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

package flex.messaging.config.services;

import flex.messaging.config.ConfigurationConfirmation;
import flex.messaging.config.MessagingConfiguration;
import flex.messaging.config.ServiceSettings;
import flex.messaging.config.AdapterSettings;
import flex.messaging.config.ChannelSettings;
import flex.messaging.config.SecuritySettings;
import flex.messaging.config.SecurityConstraint;
import flex.messaging.config.DestinationSettings;
import flex.messaging.LocalizedException;

public class Confirm1d extends ConfigurationConfirmation
{
    private MessagingConfiguration EXPECTED_VALUE;

    public Confirm1d()
    {
        MessagingConfiguration config = new MessagingConfiguration();

        // Security
        SecuritySettings security = config.getSecuritySettings();
        SecurityConstraint fooConstraint = new SecurityConstraint("foo-constraint");
        fooConstraint.setMethod(SecurityConstraint.CUSTOM_AUTH_METHOD);
        fooConstraint.addRole("foo-managers");
        security.addConstraint(fooConstraint);

        SecurityConstraint barConstraint = new SecurityConstraint("bar-constraint");
        barConstraint.setMethod(SecurityConstraint.CUSTOM_AUTH_METHOD);
        barConstraint.addRole("bar-managers");
        security.addConstraint(barConstraint);


        // Channels
        ChannelSettings fooChannel = new ChannelSettings("foo-channel");
        fooChannel.setUri("/foo");
        fooChannel.setEndpointType("flex.messaging.endpoints.FooEndpoint");
        config.addChannelSettings("foo-channel", fooChannel);

        ChannelSettings barChannel = new ChannelSettings("bar-channel");
        barChannel.setUri("/bar");
        barChannel.setEndpointType("flex.messaging.endpoints.BarEndpoint");
        config.addChannelSettings("bar-channel", barChannel);


        // Services
        ServiceSettings fooService = new ServiceSettings("foo-service");
        fooService.setClassName("flex.messaging.services.FooService");
        config.addServiceSettings(fooService);

        // Adapters
        AdapterSettings fooAdapter = new AdapterSettings("foo");
        fooAdapter.setClassName("flex.messaging.services.foo.FooAdapter");
        fooService.addAdapterSettings(fooAdapter);

        AdapterSettings barAdapter = new AdapterSettings("bar");
        barAdapter.setClassName("flex.messaging.services.bar.BarAdapter");
        barAdapter.setDefault(true);
        fooService.addAdapterSettings(barAdapter);

        // Default Channels
        fooService.addDefaultChannel(fooChannel);


        // Destination - foo-dest
        DestinationSettings fooDest = new DestinationSettings("foo-dest");
        fooDest.addChannelSettings(fooChannel);
        fooDest.setAdapterSettings(fooAdapter);
        fooDest.setConstraint(fooConstraint);
        fooDest.addProperty("fooString", "fooValue");
        fooService.addDestinationSettings(fooDest);

        // Destination - bar-dest
        DestinationSettings barDest = new DestinationSettings("bar-dest");
        barDest.addChannelSettings(barChannel);
        barDest.addChannelSettings(fooChannel);
        barDest.setAdapterSettings(barAdapter);
        barDest.setConstraint(barConstraint);
        barDest.addProperty("barString", "barValue");
        fooService.addDestinationSettings(barDest);

        EXPECTED_VALUE = config;
    }

    public MessagingConfiguration getExpectedConfiguration()
    {
        return EXPECTED_VALUE;
    }

    public LocalizedException getExpectedException()
    {
        return null;
    }
}

