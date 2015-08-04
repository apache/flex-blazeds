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
import flex.messaging.LocalizedException;

public class Confirm1b extends ConfigurationConfirmation
{
    private MessagingConfiguration EXPECTED_VALUE;

    public Confirm1b()
    {
        MessagingConfiguration config = new MessagingConfiguration();

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

        AdapterSettings fooAdapter = new AdapterSettings("foo");
        fooAdapter.setClassName("flex.messaging.services.foo.FooAdapter");
        fooService.addAdapterSettings(fooAdapter);

        fooService.addDefaultChannel(fooChannel);


        ServiceSettings barService = new ServiceSettings("bar-service");
        barService.setClassName("flex.messaging.services.BarService");
        config.addServiceSettings(barService);

        AdapterSettings bar1Adapter = new AdapterSettings("bar1");
        bar1Adapter.setClassName("flex.messaging.services.bar.BarAdapter");
        barService.addAdapterSettings(bar1Adapter);

        AdapterSettings bar2Adapter = new AdapterSettings("bar2");
        bar2Adapter.setClassName("flex.messaging.services.bar.BarAdapter");
        barService.addAdapterSettings(bar2Adapter);

        barService.addDefaultChannel(barChannel);
        barService.addDefaultChannel(fooChannel);

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

