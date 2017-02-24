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

import flex.messaging.MessageBroker;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the flex.messaging.client.FlexClientManager
 */
public class FlexClientManagerTest {

    private FlexClientManager manager;

    @Before
    public void setUp() throws Exception {
        MessageBroker broker = new MessageBroker(false);
        broker.initThreadLocals();
        manager = new FlexClientManager(broker);
    }

    /**
     * Test that FlexClientManager.createFlexClient(id) returns
     * a FlexClient instance with the correct id.
     */
    @Test
    public void testCreateFlexClientWithId() {
        String id = "abc";
        FlexClient client = manager.createFlexClient(id);
        Assert.assertEquals(id, client.getId());
    }

    /**
     * Test that calling FlexClientManager.getFlexClient(id) with
     * a new id, returns a new FlexClient instance with the correct
     * id.
     */
    @Test
    public void testGetFlexClientNewId() {
        String id = "def";
        FlexClient client = manager.getFlexClient(id);
        Assert.assertNotNull(client);
        Assert.assertEquals(id, client.getId());
    }

    /**
     * Test that calling FlexClientManager.getFlexClient(id) with
     * an id for an existing FlexClient, returns the FlexClient
     * instance with the correct id.
     */
    @Test
    public void testGetFlexClientExistingId() {
        String id = "ghi";
        manager.createFlexClient(id);
        FlexClient client = manager.getFlexClient(id);
        Assert.assertEquals(id, client.getId());
    }

    /**
     * Test that calling FlexClientManager.getFlexClient(id, createNewIfNotExist)
     * with an id for a nonexistent FlexClient and false returns null.
     */
    @Test
    public void testGetFlexClientCreateNewFalse() {
        String id = "jkl";
        FlexClient client = manager.getFlexClient(id, false);
        Assert.assertNull(client);
    }

    /**
     * Test that calling FlexClientManager.getFlexClientCount() returns
     * 2 when there are 2 FlexClients.
     */
    @Test
    public void testGetFlexClientCount() {
        manager.getFlexClient("client1");
        manager.getFlexClient("client2");
        Assert.assertEquals(2, manager.getFlexClientCount());
    }

    /**
     * Test that FlexClientManager.removeFlexClient(FlexClient) removes
     * the correct FlexClient.
     */
    @Test
    public void testRemoveFlexClient() {
        FlexClient client = manager.getFlexClient("client1");
        manager.getFlexClient("client2");
        manager.removeFlexClient(client);
        Assert.assertEquals(1, manager.getFlexClientCount());
        Assert.assertEquals("client2", manager.getClientIds()[0]);
    }

}
