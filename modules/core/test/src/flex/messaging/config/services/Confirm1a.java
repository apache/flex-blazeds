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
import flex.messaging.LocalizedException;

public class Confirm1a extends ConfigurationConfirmation
{
    private MessagingConfiguration EXPECTED_VALUE;

    public Confirm1a()
    {
        MessagingConfiguration config = new MessagingConfiguration();

        ServiceSettings service = new ServiceSettings("foo-service");
        service.setClassName("flex.messaging.services.FooService");
        config.addServiceSettings(service);

        service = new ServiceSettings("bar-service");
        service.setClassName("flex.messaging.services.BarService");
        config.addServiceSettings(service);

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

