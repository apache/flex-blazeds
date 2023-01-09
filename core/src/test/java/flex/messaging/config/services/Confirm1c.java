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
import flex.messaging.config.SecurityConstraint;
import flex.messaging.config.SecuritySettings;
import flex.messaging.config.ServiceSettings;
import flex.messaging.config.ChannelSettings;
import flex.messaging.config.AdapterSettings;
import flex.messaging.config.DestinationSettings;
import flex.messaging.LocalizedException;

import java.util.List;
import java.util.Iterator;

public class Confirm1c extends ConfigurationConfirmation {
    protected MessagingConfiguration EXPECTED_VALUE;

    public Confirm1c() {
        MessagingConfiguration config = new MessagingConfiguration();

        ChannelSettings fooChannel = new ChannelSettings("foo-channel");
        fooChannel.setUri("/foo");
        fooChannel.setEndpointType("flex.messaging.endpoints.FooEndpoint");
        config.addChannelSettings("foo-channel", fooChannel);

        ServiceSettings fooService = new ServiceSettings("foo-service");
        fooService.setClassName("flex.messaging.services.FooService");
        config.addServiceSettings(fooService);

        AdapterSettings fooAdapter = new AdapterSettings("foo");
        fooAdapter.setClassName("flex.messaging.services.foo.FooAdapter");
        fooAdapter.setDefault(true);
        fooService.addAdapterSettings(fooAdapter);

        fooService.addDefaultChannel(fooChannel);

        DestinationSettings fooDest = new DestinationSettings("foo-dest");
        fooService.addDestinationSettings(fooDest);

        // Destination Channels (from default)
        List defaultChannels = fooService.getDefaultChannels();
        Iterator it = defaultChannels.iterator();
        while (it.hasNext()) {
            ChannelSettings c = (ChannelSettings) it.next();
            fooDest.addChannelSettings(c);
        }

        // Destination Adapters (from default)
        AdapterSettings defaultAdapter = fooService.getDefaultAdapter();
        if (fooDest.getAdapterSettings() == null && defaultAdapter != null) {
            fooDest.setAdapterSettings(defaultAdapter);
        }

        // Destination Properties
        fooDest.addProperty("fooString", "fooValue");

        // Security
        SecuritySettings security = config.getSecuritySettings();
        SecurityConstraint constraint = new SecurityConstraint("test-constraint");
        constraint.addRole("test");
        constraint.setMethod(SecurityConstraint.CUSTOM_AUTH_METHOD);
        security.addConstraint(constraint);

        EXPECTED_VALUE = config;
    }

    public MessagingConfiguration getExpectedConfiguration() {
        return EXPECTED_VALUE;
    }

    public LocalizedException getExpectedException() {
        return null;
    }
}

