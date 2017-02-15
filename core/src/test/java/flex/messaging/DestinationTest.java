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

package flex.messaging;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import flex.messaging.config.ChannelSettings;
import flex.messaging.config.ConfigurationConstants;
import flex.messaging.config.ConfigurationException;
import flex.messaging.config.NetworkSettings;
import flex.messaging.config.SecurityConstraint;
import flex.messaging.services.AbstractService;
import flex.messaging.services.MessageService;
import flex.messaging.services.ServiceAdapter;
import flex.messaging.services.messaging.adapters.ActionScriptAdapter;
import org.junit.Assert;

/**
 * Important: While adding new tests, make sure you don't create dependencies
 * to other modules. (eg. don't use RemotingDestination as that would create
 * dependency on remoting module; instead add the test in remoting module).
 */
public class DestinationTest extends TestCase 
{
    protected Destination destination;
    protected MessageBroker broker;

    public DestinationTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(DestinationTest.class);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        destination = new MessageDestination();
        destination.setId("destId");
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testSetService()
    {
        MessageBroker broker = new MessageBroker(false);
        broker.initThreadLocals();

        AbstractService service = new MessageService();
        service.setId("dummy-service");
        broker.addService(service);
        destination.setService(service);

        MessageService actualSvc = (MessageService)destination.getService();

        Assert.assertEquals(service, actualSvc);

        Destination actualDest = service.getDestination(destination.getId());
        Assert.assertEquals(destination, actualDest);
    }

    public void testSetServiceNull()
    {
        try
        {
            destination.setService(null);

            fail("ConfigurationException expected");
        }
        catch (ConfigurationException ce)
        {
            int error = 11116; //ManageableComponent.NULL_COMPONENT_PROPERTY;
            Assert.assertEquals(ce.getNumber(), error);
        }
    }

    public void testSetAdapter()
    {
        ServiceAdapter expected = new ActionScriptAdapter();
        expected.setId("adapterId");
        destination.setAdapter(expected);

        ServiceAdapter actual = destination.getAdapter();
        Assert.assertEquals(expected, actual);
    }


    public void testSetAdapterNull()
    {
        destination.setAdapter(null);

        ServiceAdapter actual = destination.getAdapter();
        Assert.assertNull(actual);
    }

    public void testSetAdapterNullId()
    {
        ServiceAdapter adapter = new ActionScriptAdapter();
        destination.setAdapter(adapter);

        ServiceAdapter actual = destination.getAdapter();
        Assert.assertEquals(adapter, actual);

    }

    public void testAddChannelNotStarted()
    {
        String id = "default-channel";
        destination.addChannel(id);

        boolean contains = destination.getChannels().contains(id);
        Assert.assertTrue(contains);
    }

    /*
     * In running code, the channels would be added from the services-config.xml.
     * Here, we have to force them into the broker.
     */
    public void testAddChannelStartedBrokerKnows()
    {
        start();
        String id = "default-channel";
        Map<String, ChannelSettings> csMap = new HashMap<String, ChannelSettings>();
        csMap.put(id, null);
        broker.setChannelSettings(csMap);
        destination.addChannel(id);

        boolean contains = destination.getChannels().contains(id);
        Assert.assertTrue(contains);
    }

    public void testCreateAdapterRegisteredWithService ()
    {
        MessageService service = new MessageService();
        service.setId("dummy-service");
        broker = new MessageBroker(false);
        broker.addService(service);

        String adapterId = "id";
        String adapterClass = ActionScriptAdapter.class.getName();
        service.registerAdapter(adapterId, adapterClass);
        destination.setService(service);

        ServiceAdapter expected = destination.createAdapter(adapterId);

        ServiceAdapter actual = destination.getAdapter();
        Assert.assertEquals(expected, actual);
    }

    public void testCreateAdapterWithoutService()
    {
        try
        {
            destination.createAdapter("id");

            fail("ConfigurationException expected");
        }
        catch (ConfigurationException ce)
        {
            int error = 11117; // Destination.NO_SERVICE;
            Assert.assertEquals(ce.getNumber(), error);
        }
    }

    public void testCreateAdapterUnregisteredWithService()
    {
        MessageService service = new MessageService();
        service.setId("dummy-service");
        broker = new MessageBroker(false);
        broker.addService(service);
        destination.setService(service);

        try
        {
            destination.createAdapter("id");

            fail("ConfigurationException expected");
        }
        catch (ConfigurationException ce)
        {
            int error = ConfigurationConstants.UNREGISTERED_ADAPTER;
            Assert.assertEquals(ce.getNumber(), error);
        }

    }

    public void testCreateAdapterWithExistingId()
    {
        String id = "java-adapter";

        start();

        try
        {
            destination.createAdapter(id);

            fail("ConfigurationException expected");
        }
        catch (ConfigurationException ce)
        {
            int error = ConfigurationConstants.UNREGISTERED_ADAPTER;
            Assert.assertEquals(error, ce.getNumber());
        }
    }


    public void testSetNetworkSettings()
    {
        NetworkSettings ns = new NetworkSettings();
        ns.setSubscriptionTimeoutMinutes(1);
        destination.setNetworkSettings(ns);

        NetworkSettings actual = destination.getNetworkSettings();
        Assert.assertEquals(ns, actual);
    }

    public void testSetSecurityConstraint()
    {
        SecurityConstraint sc = new SecurityConstraint();
        destination.setSecurityConstraint(sc);

        SecurityConstraint actual = destination.getSecurityConstraint();
        Assert.assertEquals(sc, actual);
    }

    public void testSetSecurityConstraintRefNotStarted()
    {
        String ref = "sample-security";
        destination.setSecurityConstraint(ref);

        SecurityConstraint sc = destination.getSecurityConstraint();
        Assert.assertNull(sc);
    }

    public void testStop()
    {
        start();
        destination.stop();

        boolean started = destination.isStarted();
        Assert.assertFalse(started);
    }

    public void testSetManaged()
    {
        destination.setManaged(true);

        boolean managed = destination.isManaged();
        Assert.assertTrue(managed);
    }

    public void testSetManagedParentUnmanaged()
    {
        MessageService service = new MessageService();
        service.setId("dummy-service");
        service.setManaged(false);
        broker = new MessageBroker(false);
        broker.addService(service);

        destination.setService(service);
        destination.setManaged(true);

        boolean managed = destination.isManaged();
        Assert.assertFalse(managed);
    }

    public void testGetLogCategory()
    {
        String logCat = destination.getLogCategory();
        String logCat2 = destination.getLogCategory();
        Assert.assertEquals(logCat, logCat2);
    }

    public void testExtraProperties()
    {
        String propertyName = "extraProperty";
        String propertyValue = "extraValue";


        MessageBroker broker = new MessageBroker(false);
        broker.initThreadLocals();

        MessageService service = new MessageService();
        service.setId("dummy-service");
        broker.addService(service);
        destination.setService(service);
        destination.addExtraProperty(propertyName, propertyValue);

        // retrieve the destination and see if the property values still match
        Destination actualDest = service.getDestination(destination.getId());
        Assert.assertEquals(actualDest.getExtraProperty(propertyName), propertyValue);
    }

    private void start()
    {
        broker = new MessageBroker(false);
        MessageService service = new MessageService();
        service.setId("dummy-service");
        service.setMessageBroker(broker);
        ServiceAdapter adapter = new ActionScriptAdapter();
        adapter.setId("dummy-adapter");
        destination = new MessageDestination();
        destination.setId("http-proxy-dest");
        destination.setAdapter(adapter);
        destination.addChannel("some-Channel");
        destination.setService(service);
        destination.start();
    }
}
