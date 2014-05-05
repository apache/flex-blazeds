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

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import flex.messaging.Destination;
import flex.messaging.MessageBroker;
import flex.messaging.MessageDestination;
import flex.messaging.MessageException;
import flex.messaging.config.ChannelSettings;
import flex.messaging.config.ConfigurationConstants;
import flex.messaging.config.ConfigurationException;
import flex.messaging.messages.AsyncMessage;

public abstract class AbstractServiceTest extends TestCase
{
    protected AbstractService service;
    protected MessageBroker broker;
    
    public AbstractServiceTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(AbstractServiceTest.class);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        service = new MessageService();
        service.setId("message-service");
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }
    
    public void testSetMessageBroker()
    {
        broker = new MessageBroker(false);
        service.setMessageBroker(broker);

        Service actual = broker.getService(service.getId());
        Assert.assertEquals(service, actual);

        MessageBroker actual2 = service.getMessageBroker();
        Assert.assertEquals(broker, actual2);
    }
        
    public void testSetMessageBrokerTwice()
    {
        broker = new MessageBroker(false);
        service.setMessageBroker(broker);
        service.setMessageBroker(broker);
        
        Service actual = broker.getService(service.getId());
        Assert.assertEquals(service, actual);
    }

    public void testSetMessageBrokerAndAddService()
    {
        broker = new MessageBroker(false);
        service.setMessageBroker(broker);
        broker.addService(service);

        Service actual = broker.getService(service.getId());
        Assert.assertEquals(service, actual);
    }
    
    public void testAddDefaultChannelNotStarted()
    {
        String id = "default-channel";
        service.addDefaultChannel(id);
        
        boolean contains = service.getDefaultChannels().contains(id);
        Assert.assertTrue(contains);
    }
        
    public void testAddDefaultChannelStartedBrokerKnows()
    {         
        start();
        String id = "default-channel";
        Map<String, ChannelSettings> csMap = new HashMap<String, ChannelSettings>();
        csMap.put(id, null);
        broker.setChannelSettings(csMap);
        service.addDefaultChannel(id);
        
        boolean contains = service.getDefaultChannels().contains(id);
        Assert.assertTrue(contains);
    }
    
    public void testRemoveDefaultChannel()
    {
        String id = "default-channel";
        service.addDefaultChannel(id);
        service.removeDefaultChannel(id);
        
        boolean contains = service.getDefaultChannels().contains(id);
        Assert.assertFalse(contains);
        
    }
    
    public void testRemoveDefaultChannelNonexistent()
    {
        boolean actual = service.removeDefaultChannel("non-existent-id");
        Assert.assertFalse(actual);
    }
        
    public void testAddDestination()
    {
        String id = "destId";
        Destination expected = new MessageDestination();
        expected.setId(id);
        broker = new MessageBroker(false);
        broker.addService(service);        
        service.addDestination(expected);

        Destination actual = service.getDestination(id);
        Assert.assertEquals(expected, actual);
    }

    public void testAddDestinationNull()
    {
        try
        {
            service.addDestination(null);

            fail("ConfigurationException expected");
        }
        catch (ConfigurationException ce)
        {
            int error = ConfigurationConstants.NULL_COMPONENT;
            Assert.assertEquals(ce.getNumber(), error);
        }
    }

    public void testAddDestinationNullId()
    {
        try
        {
            Destination destination = new MessageDestination();
            service.addDestination(destination);

            fail("ConfigurationException expected");
        }
        catch (ConfigurationException ce)
        {
            int error = ConfigurationConstants.NULL_COMPONENT_ID;
            Assert.assertEquals(ce.getNumber(), error);
        }
    }

    public void testGetDestinationFromMessage()
    {
        String id = "destId";
        Destination expected = new MessageDestination();
        expected.setId(id);
        broker = new MessageBroker(false);
        broker.addService(service);
        service.addDestination(expected);
                
        AsyncMessage msg = new AsyncMessage();
        msg.setDestination(id);
        
        Destination actual = service.getDestination(msg);
        Assert.assertEquals(expected, actual);                
    }
    
    public void testGetDestinationFromMessageNonexistent()
    {
        String id = "destId";
        AsyncMessage msg = new AsyncMessage();
        msg.setDestination(id);
        
        try
        {
            service.getDestination(msg);
        }
        catch (MessageException me)
        {
            int error = 0;
            Assert.assertEquals(error, me.getNumber());
        }
    }
    
    public void testAddDestinationExists()
    {
        try
        {
            String id = "destId";
            Destination dest1 = new MessageDestination();
            dest1.setId(id);
            
            broker = new MessageBroker(false);
            broker.addService(service);
            service.addDestination(dest1);
            
            Destination dest2 = new MessageDestination();
            dest2.setId(id);
            service.addDestination(dest2);

            fail("ConfigurationException expected");
        }
        catch (ConfigurationException ce)
        {
            int error = ConfigurationConstants.DUPLICATE_DEST_ID;
            Assert.assertEquals(ce.getNumber(), error);
        }
    }

    public void testCreateDestination()
    {
        String id = "destId";
        broker = new MessageBroker(false);
        broker.addService(service);
        Destination expected = service.createDestination(id);

        Destination actual = service.getDestination(id);
        Assert.assertEquals(expected, actual);
    }
        
    public void testCreateDestinationWithExistingId()
    {
        String id = "destId";
        broker = new MessageBroker(false);
        broker.addService(service);
        service.createDestination(id);            
        try
        {
            service.createDestination(id);
            
            fail("ConfigurationException expected");
        }
        catch (ConfigurationException ce)
        {
            int error = ConfigurationConstants.DUPLICATE_DEST_ID; 
            Assert.assertEquals(ce.getNumber(), error);
        }        
    }

    public void testRemoveDestination()
    {
        String id = "destId";
        Destination dest = new MessageDestination();
        dest.setId(id);
        broker = new MessageBroker(false);
        broker.addService(service);
        service.addDestination(dest);
        service.removeDestination(id);
        Destination actual = service.getDestination(id);

        Assert.assertNull(actual);
    }

    public void testRemoveDestinationNonexistent()
    {
        Destination actual = service.removeDestination("non-existent");
        Assert.assertNull(actual);
    }
    
    public void testRegisterAdapter()
    {
        String id = "id";
        String expected = "adapterClass";
        service.registerAdapter(id, expected);
        Map<String, String> adapters = service.getRegisteredAdapters();

        String actual = (String) adapters.get(id);
        Assert.assertEquals(expected, actual);

    }

    public void testRegisterAdapterExisting()
    {
        String id = "id";
        service.registerAdapter(id, "adapterClass");
        String expected = "adapterClass2";
        service.registerAdapter(id, expected);
        Map<String, String> adapters = service.getRegisteredAdapters();

        String actual = (String) adapters.get(id);
        Assert.assertEquals(expected, actual);
    }

    public void testUnregisterAdapter()
    {
        String id = "id";
        service.registerAdapter(id, "adapterClass");
        service.unregisterAdapter(id);
        Map<String, String> adapters = service.getRegisteredAdapters();
        String actual = (String) adapters.get(id);

        Assert.assertNull(actual);
    }

    public void testSetDefaultAdapterWithRegisteredAdapter()
    {
        String expected = "id";
        service.registerAdapter(expected, "adapterClass");
        service.setDefaultAdapter(expected);

        String actual = service.getDefaultAdapter();
        Assert.assertEquals(expected, actual);
    }

    public void testSetDefaultAdapterWithWrongId()
    {
        try
        {
            String id = "NonExistantId";
            service.setDefaultAdapter(id);

            fail("ConfigurationException expected");
        }
        catch (ConfigurationException ce)
        {
            int error = ConfigurationConstants.UNREGISTERED_ADAPTER;
            Assert.assertEquals(ce.getNumber(), error);
        }
    }

    public void testGetDefaultAdapterAfterUnregisteringAdapter()
    {
        String id = "id";
        service.registerAdapter(id, "adapterClass");
        service.setDefaultAdapter(id);
        service.unregisterAdapter(id);
        String actual = service.getDefaultAdapter();

        String expected = null;
        Assert.assertEquals(expected, actual);
    }
    
    private void start()
    {
        broker = new MessageBroker(false);
        service.setMessageBroker(broker);    
        service.start();
    }
}

