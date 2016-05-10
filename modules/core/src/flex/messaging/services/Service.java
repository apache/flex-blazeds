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

import java.util.List;
import java.util.Map;

import flex.management.Manageable;
import flex.messaging.FlexComponent;
import flex.messaging.MessageBroker;
import flex.messaging.Destination;
import flex.messaging.config.ConfigMap;
import flex.messaging.endpoints.Endpoint;
import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.Message;

/**
 * The MessageBroker has endpoints on one end and services
 * on the other. The Service interface defines the contract between
 * the MessageBroker and all Service implementations.
 */
public interface Service extends Manageable, FlexComponent
{
    /**
     * Returns the adapters registered with the <code>Service</code>.
     *
     * @return The Map of adapter id and classes.
     */
    Map<String, String> getRegisteredAdapters();

    /**
     * Registers the adapter with the <code>Service</code>.
     *
     * @param id The id of the adapter.
     * @param className The class of the adapter.
     * @return The previous adapter class that the id was associated with.
     */
    String registerAdapter(String id, String className);

    /**
     * Unregistered the adapter with the <code>Service</code> and
     * set the default adapter to <code>null</code> if needed.
     *
     * @param id The id of the adapter.
     * @return The adapter class that the id was associated with.
     */
    String unregisterAdapter(String id);

    /**
     * Returns the id of the default adapter of the <code>Service</code>.
     *
     * @return The id of the default adapter of the <code>Service</code>.
     */
    String getDefaultAdapter();

    /**
     * Sets the id of the default adapter of the <code>Service</code>.
     *
     * @param id The id of the default adapter of the <code>Service</code>.
     */
    void setDefaultAdapter(String id);

    /**
     * Returns the list of channel ids of the <code>Service</code>.
     *
     * @return The list of channel ids of the <code>Service</code>.
     */
    List<String> getDefaultChannels();

    /**
     * Adds the channel to the list of channels of the <code>Service</code>.
     * <code>MessageBroker</code> has to know the channel. Otherwise, the channel
     * should not added to the list.
     *
     * @param id The id of the channel.
     */
    void addDefaultChannel(String id);

    /**
     * Sets the channel list of the <code>Service</code>.
     * <code>MessageBroker</code> has to know the channels, otherwise they
     * should not be added to the list.
     *
     * @param ids List of channel ids.
     */
    void setDefaultChannels(List<String> ids);


    /**
     * Removes the channel from the list of channels for the <code>AbstractService</code>.
     *
     * @param id The id of the channel.
     * @return <code>true</code> if the list contained the channel id.
     */
    boolean removeDefaultChannel(String id);

    /**
     * Retrieves the destination in this service for which the given message is intended.
     *
     * @param message The message.
     * @return The <code>Destination</code> in this service for which the given message is intended.
     */
    Destination getDestination(Message message);

    /**
     * Returns the <code>Destination</code> with the specified id or null if no
     * <code>Destination</code> with id exists.
     *
     * @param id The id of the <code>Destination</code>.
     * @return The <code>Destination</code> with the specified id or null.
     */
    Destination getDestination(String id);

    /**
     * Returns the Map of <code>Destination</code> ids and instances.
     *
     * @return The Map of <code>Destination</code> ids and instances.
     */
    Map<String, Destination> getDestinations();

    /**
     * Creates a <code>Destination</code> instance, sets its id, sets it manageable
     * if the <code>Service</code> that created it is manageable,
     * and sets its <code>Service</code> to the <code>Service</code> that
     * created it.
     *
     * @param id The id of the <code>Destination</code>.
     * @return The <code>Destination</code> instanced created.
     */
    Destination createDestination(String id);

    /**
     * Adds the <code>Destination</code> to the <code>Service</code>.
     *
     * @param destination The <code>Destination</code> to be added.
     */
    void addDestination(Destination destination);

    /**
     * Remove the <code>Destination</code> from the <code>Service</code>.
     *
     * @param id The id of the <code>Destination</code>.
     * @return Previous <code>Destination</code> associated with the id.
     */
    Destination removeDestination(String id);

    /**
     * Returns the id for the service.
     *
     * @return The id for the service.
     */
    String getId();

    /**
     * Sets the id for the service.
     *
     * @param id The id for the service.
     */
    void setId(String id);

    /**
     * All services must be managed by a single MessageBroker,
     * and must be capable of returning a reference to that broker.
     * This broker is used when a service wishes to push a message
     * to one or more endpoints managed by the broker.
     *
     * @return broker The MessageBroker instance which manages this service
     */
    MessageBroker getMessageBroker();

    /**
     * Sets the <code>MessageBroker</code> of the <code>Service</code>.
     *
     * @param broker The <code>MessageBroker</code> of the <code>Service</code>.
     */
    void setMessageBroker(MessageBroker broker);

    /**
     * Describes the service for the client.
     *
     * @param endpoint Endpoint used to filter the service destinations.
     * @return ConfigMap of service properties.
     */
    ConfigMap describeService(Endpoint endpoint);

    /**
     * Handles a message routed to the service by the MessageBroker.
     *
     * @param message The message sent by the MessageBroker.
     * @return The result of the service.
     */
    Object serviceMessage(Message message);

    /**
     * Handles a command routed to the service by the MessageBroker.
     * Usually these are commands sent by one of the endpoints.
     *
     * @param message The message sent by the MessageBroker.
     * @return The result of the service.
     */
    Object serviceCommand(CommandMessage message);
}
