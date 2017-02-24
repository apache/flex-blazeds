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

package flex.messaging.services;

import flex.messaging.Destination;
import flex.messaging.MessageDestination;
import flex.messaging.config.ConfigurationException;
import flex.messaging.services.messaging.adapters.ActionScriptAdapter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ServiceAdapterTest {

    protected ServiceAdapter adapter;
    protected Destination destination;

    @Before
    public void setUp() throws Exception {
        adapter = new ActionScriptAdapter();
        adapter.setId("as-adapter");
    }

    @Test
    public void testSetDestination() {
        destination = new MessageDestination();
        adapter.setDestination(destination);

        Destination actual = adapter.getDestination();
        Assert.assertEquals(destination, actual);

        ServiceAdapter actual2 = destination.getAdapter();
        Assert.assertEquals(adapter, actual2);
    }

    @Test
    public void testSetDestinationNull() {
        try {
            adapter.setDestination(null);

            Assert.fail("ConfigurationException expected");
        } catch (ConfigurationException ce) {
            int error = 11116; // ManageableComponent.NULL_COMPONENT_PROPERTY;
            Assert.assertEquals(ce.getNumber(), error);
        }
    }

    @Test
    public void testSetDestinationWrongType() {
        destination = new Destination();

        try {
            adapter.setDestination(destination);

            Assert.fail("ClassCastException expected");
        } catch (ClassCastException e) {
            // Ignore this as we are expecting this.
        }
    }

    @Test
    public void testSetManagedParentUnmanaged() {
        destination = new MessageDestination();
        destination.setManaged(false);
        destination.setAdapter(adapter);
        adapter.setManaged(true);

        boolean managed = adapter.isManaged();
        Assert.assertFalse(managed);

    }

    @Test
    public void testSetManaged() {
        adapter.setManaged(true);

        boolean managed = adapter.isManaged();
        Assert.assertTrue(managed);
    }

}

