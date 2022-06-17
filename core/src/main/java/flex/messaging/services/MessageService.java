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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import flex.management.runtime.messaging.MessageDestinationControl;
import flex.management.runtime.messaging.services.MessageServiceControl;
import flex.messaging.Destination;
import flex.messaging.FlexContext;
import flex.messaging.MessageBroker;
import flex.messaging.MessageClient;
import flex.messaging.MessageDestination;
import flex.messaging.MessageException;
import flex.messaging.MessageRoutedNotifier;
import flex.messaging.client.FlushResult;
import flex.messaging.cluster.Cluster;
import flex.messaging.cluster.ClusterManager;
import flex.messaging.config.ConfigurationConstants;
import flex.messaging.config.ConfigurationException;
import flex.messaging.config.ServerSettings;
import flex.messaging.config.ServerSettings.RoutingMode;
import flex.messaging.log.Log;
import flex.messaging.log.LogCategories;
import flex.messaging.messages.AcknowledgeMessage;
import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.Message;
import flex.messaging.messages.MessagePerformanceUtils;
import flex.messaging.services.messaging.MessagingConstants;
import flex.messaging.services.messaging.RemoteSubscriptionManager;
import flex.messaging.services.messaging.SubscriptionManager;
import flex.messaging.services.messaging.Subtopic;
import flex.messaging.services.messaging.ThrottleManager;
import flex.messaging.services.messaging.adapters.MessagingAdapter;
import flex.messaging.services.messaging.adapters.MessagingSecurityConstraintManager;
import flex.messaging.util.StringUtils;

/**
 * The MessageService class is the Service implementation that manages point-to-point
 * and publish-subscribe messaging.
 */
public class MessageService extends AbstractService implements MessagingConstants {
    /**
     * Log category for <code>MessageService</code>.
     */
    public static final String LOG_CATEGORY = LogCategories.SERVICE_MESSAGE;
    /**
     * Log category for <code>MessageService</code> that captures message timing.
     */
    public static final String TIMING_LOG_CATEGORY = LogCategories.MESSAGE_TIMING;


    public static final String NOT_SUBSCRIBED_CODE = "Server.Processing.NotSubscribed";

    // Errors
    private static final int BAD_SELECTOR = 10550;
    private static final int NOT_SUBSCRIBED = 10551;
    private static final int UNKNOWN_COMMAND = 10552;

    private boolean debug;
    private MessageServiceControl controller;

    private ReadWriteLock subscribeLock = new ReentrantReadWriteLock();

    //--------------------------------------------------------------------------
    //
    // Constructor
    //
    //--------------------------------------------------------------------------

    /**
     * Constructs an unmanaged <code>MessageService</code>.
     */
    public MessageService() {
        super(false);
    }

    /**
     * Constructs an <code>MessageService</code> with the indicated management.
     *
     * @param enableManagement <code>true</code> if the <code>MessageService</code>
     *                         is manageable; otherwise <code>false</code>.
     */
    public MessageService(boolean enableManagement) {
        super(enableManagement);
    }

    //--------------------------------------------------------------------------
    //
    // Initialize, validate, start, and stop methods.
    //
    //--------------------------------------------------------------------------

    @Override
    public void start() {
        String serviceType = getClass().getName();
        ClusterManager clm = getMessageBroker().getClusterManager();

        super.start();

        /*
         * For any destinations which are not using broadcast mode,
         * we need to init the remote subscriptions.  First we send out
         * the requestSubscription messages, then we wait for the sendSubscriptions
         * messages to come in.
         */
        for (String destName : destinations.keySet()) {
            MessageDestination dest = (MessageDestination) getDestination(destName);
            if (dest.getServerSettings().getRoutingMode() == RoutingMode.SERVER_TO_SERVER && dest.isClustered()) {
                initRemoteSubscriptions(destName);
            }
        }

        /* Now go through and wait for the response to these messages... */
        for (String destName : destinations.keySet()) {
            MessageDestination dest = (MessageDestination) getDestination(destName);
            if (dest.getServerSettings().getRoutingMode() == RoutingMode.SERVER_TO_SERVER && dest.isClustered()) {
                List members = clm.getClusterMemberAddresses(serviceType, destName);
                for (Object addr : members) {
                    if (!clm.getLocalAddress(serviceType, destName).equals(addr)) {
                        RemoteSubscriptionManager subMgr = dest.getRemoteSubscriptionManager();
                        subMgr.waitForSubscriptions(addr);
                    }
                }
            }
        }
        debug = Log.isDebug();
    }

    //--------------------------------------------------------------------------
    //
    // Public Getters and Setters for AbstractService properties
    //
    //--------------------------------------------------------------------------

    /**
     * Creates a <code>MessageDestination</code> instance, sets its id, sets it manageable
     * if the <code>AbstractService</code> that created it is manageable,
     * and sets its <code>Service</code> to the <code>AbstractService</code> that
     * created it.
     *
     * @param id The id of the <code>MessageDestination</code>.
     * @return The <code>Destination</code> instanced created.
     */
    @Override
    public Destination createDestination(String id) {
        if (id == null) {
            // Cannot add ''{0}'' with null id to the ''{1}''
            ConfigurationException ex = new ConfigurationException();
            ex.setMessage(ConfigurationConstants.NULL_COMPONENT_ID, new Object[]{"Destination", "Service"});
            throw ex;
        }

        // check with the message broker to make sure that no destination with the id already exists
        getMessageBroker().isDestinationRegistered(id, getId(), true);

        MessageDestination destination = new MessageDestination();
        destination.setId(id);
        destination.setManaged(isManaged());
        destination.setService(this);

        return destination;
    }

    /**
     * Casts the <code>Destination</code> into <code>MessageDestination</code>
     * and calls super.addDestination.
     *
     * @param destination The <code>Destination</code> instance to be added.
     */
    @Override
    public void addDestination(Destination destination) {
        MessageDestination messageDestination = (MessageDestination) destination;
        super.addDestination(messageDestination);
    }

    //--------------------------------------------------------------------------
    //
    // Other Public APIs
    //
    //--------------------------------------------------------------------------

    @Override
    public Object serviceMessage(Message message) {
        return serviceMessage(message, true);
    }

    /**
     *
     */

    public Object serviceMessage(Message message, boolean throttle) {
        return serviceMessage(message, throttle, null);
    }

    /**
     *
     */
    public Object serviceMessage(Message message, boolean throttle, MessageDestination dest) {
        if (managed)
            incrementMessageCount(false, message);

        if (throttle) {
            // Throttle the inbound message; also attempts to prevent duplicate messages sent by a client.
            dest = (MessageDestination) getDestination(message);
            ThrottleManager throttleManager = dest.getThrottleManager();
            if (throttleManager != null && throttleManager.throttleIncomingMessage(message))
                return null; // Message throttled.
        }

        // Block any sent messages that have a subtopic header containing
        // wildcards - wildcards are only supported in subscribe/unsubscribe
        // commands (see serviceCommand() and manageSubscriptions()).
        Object subtopicObj = message.getHeader(AsyncMessage.SUBTOPIC_HEADER_NAME);
        List<Subtopic> subtopics = null;
        if (subtopicObj != null) {
            if (subtopicObj instanceof Object[])
                subtopicObj = Arrays.asList((Object[]) subtopicObj);

            if (subtopicObj instanceof String) {
                String subtopicString = (String) subtopicObj;
                if (subtopicString != null && subtopicString.length() > 0) {
                    if (dest == null)
                        dest = (MessageDestination) getDestination(message);
                    Subtopic subtopic = testProducerSubtopic(dest.getServerSettings().getSubtopicSeparator(), subtopicString);
                    if (subtopics == null)
                        subtopics = new ArrayList<Subtopic>();
                    subtopics.add(subtopic);
                }
            } else if (subtopicObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> subtopicList = (List<String>) subtopicObj;
                String subtopicSeperator = null;
                for (String subtopicString : subtopicList) {
                    if (subtopicString != null && subtopicString.length() > 0) {
                        if (dest == null)
                            dest = (MessageDestination) getDestination(message);
                        subtopicSeperator = dest.getServerSettings().getSubtopicSeparator();
                        Subtopic subtopic = testProducerSubtopic(subtopicSeperator, subtopicString);
                        if (subtopics == null)
                            subtopics = new ArrayList<Subtopic>();
                        subtopics.add(subtopic);
                    }
                }
            }
        }

        // Override TTL if there was one specifically configured for this destination
        if (dest == null)
            dest = (MessageDestination) getDestination(message);
        ServerSettings destServerSettings = dest.getServerSettings();
        if (destServerSettings.getMessageTTL() >= 0)
            message.setTimeToLive(destServerSettings.getMessageTTL());

        long start = 0;
        if (debug)
            start = System.currentTimeMillis();

        // Give MessagingAdapter a chance to block the send.
        ServiceAdapter adapter = dest.getAdapter();
        if (adapter instanceof MessagingAdapter) {
            MessagingSecurityConstraintManager manager = ((MessagingAdapter) adapter).getSecurityConstraintManager();
            if (manager != null)
                manager.assertSendAuthorization();
        }

        MessagePerformanceUtils.markServerPreAdapterTime(message);
        Object result = adapter.invoke(message);
        MessagePerformanceUtils.markServerPostAdapterTime(message);

        if (debug) {
            long end = System.currentTimeMillis();
            Log.getLogger(TIMING_LOG_CATEGORY).debug("After invoke service: " + getId() + "; execution time = " + (end - start) + "ms");
        }

        return result;
    }

    /**
     *
     */
    @Override
    public Object serviceCommand(CommandMessage message) {
        if (managed)
            incrementMessageCount(true, message);

        Object commandResult = super.serviceCommonCommands(message);
        if (commandResult == null)
            commandResult = manageSubscriptions(message);

        return commandResult;
    }

    /**
     * This method is called from a messaging adapter to handle the delivery
     * of messages to one or more clients.
     * If you pass in the <code>sendToAllSubscribers</code> parameter as <code>true</code>, the message is
     * routed to all clients who are subscribed to receive messages
     * from this destination.  When you use this method, the selector expressions
     * for all subscribing clients are not evaluated. If you want the selector
     * expressions to be evaluated, use a combination of the <code>pushMessageToClients</code>
     * method and the <code>sendPushMessageFromPeer</code> methods.
     *
     * @param message              The <code>Message</code> to send.
     * @param sendToAllSubscribers If <code>true</code>, send this message to all clients
     *                             subscribed to the destination of this message.  If <code>false</code>, send the message
     *                             only to the clientId specified in the message.
     */
    public void serviceMessageFromAdapter(Message message, boolean sendToAllSubscribers) {
        // Update management metrics.
        if (managed) {
            MessageDestination destination = (MessageDestination) getDestination(message.getDestination());
            if (destination != null && destination.isManaged()) {
                MessageDestinationControl destinationControl = (MessageDestinationControl) destination.getControl();
                if (destinationControl != null) // Should not happen but just in case.
                    destinationControl.incrementServiceMessageFromAdapterCount();
            }
        }

        // in this service's case, this invocation occurs when an adapter has asynchronously
        // received a message from one of its adapters acting as a consumer
        if (sendToAllSubscribers) {
            pushMessageToClients(message, false);
            sendPushMessageFromPeer(message, false);
        } else {
            // TODO - need to do something here to locate the proper qualified client.
            // the adapter has already processed the subscribers
            Set subscriberIds = new TreeSet();
            subscriberIds.add(message.getClientId());
            pushMessageToClients(subscriberIds, message, false);
        }
    }

    /**
     * Send the passed message to clients connected to other server peer nodes in the cluster.
     * If you are using broadcast cluster-messaging-routing mode, the message is broadcast
     * through the cluster. If you are using the server-to-server mode, the message is sent only
     * to servers from which we have received a matching subscription request.
     *
     * @param message      The <code>Message</code> to push to peer server nodes in the cluster.
     * @param evalSelector <code>true</code> to evaluate each remote subscriber's selector
     *                     before pushing the message to them; <code>false</code> to skip selector evaluation.
     */
    public void sendPushMessageFromPeer(Message message, boolean evalSelector) {
        sendPushMessageFromPeer(message, (MessageDestination) getDestination(message), evalSelector);
    }

    /**
     * Same as the previous method but it accepts a destination parameter as well to avoid
     * potentially costly destination lookup.
     *
     * @param message      The <code>Message</code> to push to peer server nodes in the cluster.
     * @param destination  The destination to push the message to.
     * @param evalSelector <code>true</code> to evaluate each remote subscriber's selector
     *                     before pushing the message to them; <code>false</code> to skip selector evaluation.
     */
    public void sendPushMessageFromPeer(Message message, MessageDestination destination, boolean evalSelector) {
        if (!destination.isClustered())
            return;

        if (destination.getServerSettings().getRoutingMode() == RoutingMode.NONE)
            return;

        ClusterManager clm = getMessageBroker().getClusterManager();
        if (destination.getServerSettings().isBroadcastRoutingMode()) {
            if (debug)
                Log.getLogger(LOG_CATEGORY).debug("Broadcasting message to peer servers: " + message + " evalSelector: " + evalSelector);
            // tell the message service on other nodes to push the message
            clm.invokeServiceOperation(getClass().getName(), message.getDestination(),
                    ClusterManager.OPERATION_PUSH_MESSAGE_FROM_PEER, new Object[]{message, evalSelector});
        } else {
            RemoteSubscriptionManager mgr = destination.getRemoteSubscriptionManager();
            Set serverAddresses = mgr.getSubscriberIds(message, evalSelector);

            if (debug)
                Log.getLogger(LOG_CATEGORY).debug("Sending message to peer servers: " + serverAddresses + StringUtils.NEWLINE + " message: " + message + StringUtils.NEWLINE + " evalSelector: " + evalSelector);

            for (Object remoteAddress : serverAddresses) {
                clm.invokePeerToPeerOperation(getClass().getName(), message.getDestination(),
                        ClusterManager.OPERATION_PUSH_MESSAGE_FROM_PEER_TO_PEER, new Object[]{message, evalSelector}, remoteAddress);
            }
        }
    }

    /**
     * This method is provided for a cluster peer broadcast from a single remote node.  Because the
     * cluster handling code adds the remote server's address as a paramter when you call invokePeerToPeerOperation
     * we need a new variant of this method which takes the remote node's address.
     */
    public void pushMessageFromPeerToPeer(AsyncMessage message, Boolean evalSelector, Object address) {
        pushMessageFromPeer(message, evalSelector);
    }

    /**
     * This method is provided for a cluster peer broadcast, it is not intended to be
     * invoked locally.
     */
    public void pushMessageFromPeer(AsyncMessage message, Boolean evalSelector) {
        if (!isStarted()) {
            Log.getLogger(LOG_CATEGORY).debug("Received message from peer server before server is started - ignoring: " + message + " evalSelector: " + evalSelector);
            return;
        }
        if (debug)
            Log.getLogger(LOG_CATEGORY).debug("Received message from peer server: " + message + " evalSelector: " + evalSelector);

        // Update the FlexContext for this thread to indicate we're processing a message from
        // a server peer.
        FlexContext.setMessageFromPeer(true);
        // we are not confirming that replication is enabled again here, so if the remote
        // peer has replication enabled and therefore broadcast to this peer, then this peer
        // will complete the operation even if it locally does not have replication enabled
        pushMessageToClients(message, evalSelector);
        // And unset.
        FlexContext.setMessageFromPeer(false);
    }

    /**
     * Pushes a message to all clients that are subscribed to the destination targeted by this message.
     *
     * @param message      The <code>Message</code> to push to the destination's subscribers.
     * @param evalSelector <code>true</code> to evaluate each subscriber's selector before pushing
     *                     the message to them; <code>false</code> to skip selector evaluation.
     */
    public void pushMessageToClients(Message message, boolean evalSelector) {
        MessageDestination destination = (MessageDestination) getDestination(message);
        SubscriptionManager subscriptionManager = destination.getSubscriptionManager();
        Set subscriberIds = subscriptionManager.getSubscriberIds(message, evalSelector);

        if (debug)
            Log.getLogger(LOG_CATEGORY).debug("Sending message: " + message + StringUtils.NEWLINE + "    to subscribed clientIds: " + subscriberIds);

        if ((subscriberIds != null) && !subscriberIds.isEmpty()) {
            /* We have already filtered based on the selector and so pass false below */
            pushMessageToClients(destination, subscriberIds, message, false);
        }
    }

    /**
     * Returns a Set of clientIds of the clients subscribed to receive this message.
     * If the message has a subtopic header, the subtopics are used to gather the
     * subscribers.  If there is no subtopic header, subscribers to the destination
     * with no subtopic are used.  If a subscription has a selector expression associated
     * with it and evalSelector is true, the subscriber is only returned if the selector
     * expression evaluates to true.
     * <p>
     * In normal usage, you can use the pushMessageToClients(message, evalSelector) method
     * to both find the subscribers and send the message.  You use this method only if
     * you want to do additional processing to the subscribers list - for example, merging
     * it into another list of subscribers or logging the ids of the subscribers who should
     * receive the message.  Once this method returns, you can use the pushMessageToClients
     * variant which takes the set of subscriber ids to deliver these messages.
     * </p><p>
     * This method only returns subscriptions maintained by the current server instance.
     * It does not return any information for subscribers that might be connected to
     * remote instances.  To send the message to remotely connected servers, use the
     * sendPushMessageFromPeer method.
     * </p>
     *
     * @param message      The <code>Messsage</code>
     *                     Typically
     * @param evalSelector whether we should evaluate the selector
     * @return Set the set of the subscriber's IDs
     */
    public Set getSubscriberIds(Message message, boolean evalSelector) {
        MessageDestination destination = (MessageDestination) getDestination(message);
        SubscriptionManager subscriptionManager = destination.getSubscriptionManager();
        return subscriptionManager.getSubscriberIds(message, evalSelector);
    }

    /**
     * Returns the set of subscribers for the specified destination, subtopic/subtopic pattern
     * and message headers.  The message headers can be null.  If specified, they are used
     * to match against any selector patterns that were used for subscribers.
     *
     * @param destinationId   the destination ID
     * @param subtopicPattern the subtopic pattern
     * @param messageHeaders  the map of the message headers
     * @return Set the set of the subscriber's IDs
     */
    public Set getSubscriberIds(String destinationId, String subtopicPattern, Map messageHeaders) {
        MessageDestination destination = (MessageDestination) getDestination(destinationId);
        SubscriptionManager subscriptionManager = destination.getSubscriptionManager();
        return subscriptionManager.getSubscriberIds(subtopicPattern, messageHeaders);
    }

    /**
     * This method is not invoked across a cluster, it is always locally invoked.
     * The passed message will be pushed to the subscribers in the passed set, conditionally depending
     * upon the execution of their selector expressions.
     *
     * @param subscriberIds The set of subscribers to push the message to.
     * @param message       The <code>Message</code> to push.
     * @param evalSelector  <code>true</code> to evaluate each subscriber's selector before pushing
     *                      the message to them; <code>false</code> to skip selector evaluation.
     */
    public void pushMessageToClients(Set subscriberIds, Message message, boolean evalSelector) {
        MessageDestination destination = (MessageDestination) getDestination(message);
        pushMessageToClients(destination, subscriberIds, message, evalSelector);
    }

    /**
     * This method is used by messaging adapters to send a message to a specific
     * set of clients that are directly connected to this server.  It does not
     * propagate the message to other servers in the cluster.
     */
    public void pushMessageToClients(MessageDestination destination, Set subscriberIds, Message message, boolean evalSelector) {
        if (subscriberIds != null) {
            try {
                // Place notifier in thread-local scope.
                MessageRoutedNotifier routingNotifier = new MessageRoutedNotifier(message);
                FlexContext.setMessageRoutedNotifier(routingNotifier);

                SubscriptionManager subscriptionManager = destination.getSubscriptionManager();
                // There is a deadlock potential here, as route message could involve a FlexClient.push(), outbound message queue process could end up with managing subscription
                // See bug watson 2769398
                subscribeLock.readLock().lock();
                for (Object clientId : subscriberIds) {
                    MessageClient client = subscriptionManager.getSubscriber(clientId);

                    // Skip if the client is null or invalidated.
                    if (client == null || !client.isValid()) {
                        if (debug)
                            Log.getLogger(MessageService.LOG_CATEGORY).debug("Warning: could not find MessageClient for clientId in pushMessageToClients: " + clientId + " for destination: " + destination.getId());
                        continue;
                    }

                    pushMessageToClient(client, destination, message, evalSelector);
                }

                // Done with the push, notify any listeners.
                routingNotifier.notifyMessageRouted();
            } finally {
                subscribeLock.readLock().unlock();
                // Unset the notifier for this message.
                FlexContext.setMessageRoutedNotifier(null);
            }
        }
    }

    void pushMessageToClient(MessageClient client, MessageDestination destination, Message message,
                             boolean evalSelector) {
        // Normally we'll process the message selector criteria as part of fetching the
        // clients which should receive this message. However, because the API exposed the evalSelecor flag
        // in pushMessageToClients(Set, Message, boolean), we need to run the client.testMessage() method
        // here to make sure subtopic and selector expressions are evaluated correctly in this case.
        // The general code path passes evalSelector as false, so the testMessage() method is not generally
        // invoked as part of a message push operation.
        if (evalSelector && !client.testMessage(message, destination)) {
            return;
        }

        // Push the message to the client. Note that client level outbound throttling
        // might still happen at the FlexClientOutboundQueueProcessor level.
        try {
            // Only update client last use if the message is not a pushed server command.
            if (!(message instanceof CommandMessage))
                client.updateLastUse();

            // Remove any data in the base message that should not be included in the multicast copies.
            Map messageHeaders = message.getHeaders();
            messageHeaders.remove(Message.FLEX_CLIENT_ID_HEADER);
            messageHeaders.remove(Message.ENDPOINT_HEADER);

            // Add the default message priority headers, if it's not already set.
            int priority = destination.getServerSettings().getPriority();
            if (priority != -1) {
                Object header = message.getHeader(Message.PRIORITY_HEADER);
                if (header == null)
                    message.setHeader(Message.PRIORITY_HEADER, priority);
            }

            // FIXME: [Pete] Investigate whether this is a performance issue.
            // We also need to ensure message ids do not expose FlexClient ids
            //message.setMessageId(UUIDUtils.createUUID());

            // We need a unique instance of the message for each client; both to prevent
            // outbound queue processing for various clients from interfering with each other
            // as well as needing to target the copy of the message to a specific MessageAgent
            // instance on the client.
            Message messageForClient = (Message) message.clone();

            // the MPIUTil call will be a no-op if MPI is not enabled.  Otherwise it will add
            // a server pre-push processing timestamp to the MPI object
            MessagePerformanceUtils.markServerPrePushTime(message);
            MessagePerformanceUtils.markServerPostAdapterTime(message);
            MessagePerformanceUtils.markServerPostAdapterExternalTime(message);

            // Target the message to a specific MessageAgent on the client.
            messageForClient.setClientId(client.getClientId());

            if (debug)
                Log.getLogger(MessageService.LOG_CATEGORY).debug("Routing message to FlexClient id:" + client.getFlexClient().getId() + "', MessageClient id: " + client.getClientId());

            getMessageBroker().routeMessageToMessageClient(messageForClient, client);
        } catch (MessageException ignore) {
            // Client is subscribed but has disconnected or the network failed.
            // There's nothing we can do to correct this so just continue server processing.
        }
    }

    /**
     * Issue messages to request the remote subscription table from each server in the cluster (except this one).
     *
     * @param destinationId the destination ID
     */
    public void initRemoteSubscriptions(String destinationId) {
        ClusterManager clm = getMessageBroker().getClusterManager();
        String serviceType = getClass().getName();
        MessageDestination dest = (MessageDestination) getDestination(destinationId);

        Cluster cluster = clm.getCluster(serviceType, destinationId);
        if (cluster != null)
            cluster.addRemoveNodeListener(dest.getRemoteSubscriptionManager());

        List members = clm.getClusterMemberAddresses(serviceType, destinationId);
        for (int i = 0; i < members.size(); i++) {
            Object addr = members.get(i);
            if (!clm.getLocalAddress(serviceType, destinationId).equals(addr))
                requestSubscriptions(destinationId, addr);
        }

    }

    /**
     * This method is provided for a clustered messaging with the routing-mode set to point-to-point.
     * On startup, a server invokes this method for each server to request its local subscription state.
     */
    public void requestSubscriptions(String destinationId, Object remoteAddress) {
        ClusterManager clm = getMessageBroker().getClusterManager();
        clm.invokePeerToPeerOperation(getClass().getName(), destinationId,
                ClusterManager.OPERATION_SEND_SUBSCRIPTIONS, new Object[]{destinationId}, remoteAddress);
    }

    /**
     * This method is invoked remotely via jgroups.  It builds a snapshot of the local
     * subscription state and sends it back to the requesting server by calling its
     * receiveSubscriptions method.
     */
    public void sendSubscriptions(String destinationId, Object remoteAddress) {
        MessageDestination destination = (MessageDestination) getDestination(destinationId);
        Object subscriptions;

        /*
         * Avoid trying to use the cluster stuff if this destination does not
         * exist or is not clustered on this server.
         */
        if (destination == null) {
            if (Log.isError())
                Log.getLogger(LOG_CATEGORY).error("Destination: " + destinationId + " does not exist on this server but we received a request for the subscription info from a peer server where the destination exists as clustered.  Check the cluster configuration for this destination and make sure it matches on all servers.");
            return;
        } else if (!destination.isClustered()) {
            if (Log.isError())
                Log.getLogger(LOG_CATEGORY).error("Destination: " + destinationId + " is not clustered on this server but we received a request for the subscription info from a peer server which is clustered.  Check the cluster configuration for this destination and make sure it matches on all servers.");
            return;
        }

        RemoteSubscriptionManager subMgr = destination.getRemoteSubscriptionManager();

        /*
         * The remote server has no subscriptions initially since it has not
         * started yet.  We initialize the server here so that when it sends
         * the first add subscription request, we'll receive it.  This is because
         * servers will not process remote add/remove subscribe requests until
         * they have received the subscription state from each server.
         */
        subMgr.setSubscriptionState(Collections.EMPTY_LIST, remoteAddress);

        /*
         * To ensure that we send the remote server a clean copy of the subscription
         * table we need to block out the code which adds/removes subscriptions and sends
         * them to remote servers between here...
         */
        try {
            subscribeLock.writeLock().lock();
            subscriptions = destination.getSubscriptionManager().getSubscriptionState();
            ClusterManager clm = getMessageBroker().getClusterManager();
            clm.invokePeerToPeerOperation(getClass().getName(), destinationId,
                    ClusterManager.OPERATION_RECEIVE_SUBSCRIPTIONS, new Object[]{destinationId, subscriptions}, remoteAddress);
        } finally {
            /* ... And here */
            subscribeLock.writeLock().unlock();
        }
    }

    /**
     * This method is provided for a cluster peer broadcast, it is not invoked locally.  It is used
     * by remote clients to send their subscription table to this server.
     */
    public void receiveSubscriptions(String destinationId, Object subscriptions, Object senderAddress) {
        Destination destination = getDestination(destinationId);
        if (destination instanceof MessageDestination)
            ((MessageDestination) destination).getRemoteSubscriptionManager().setSubscriptionState(subscriptions, senderAddress);
        else if (subscriptions != null && Log.isError())
            Log.getLogger(LOG_CATEGORY).error("receiveSubscriptions called with non-null value but destination: " + destinationId + " is not a MessageDestination");
    }

    /**
     * Called when we need to push a local subscribe/unsubscribe to all of the remote
     * servers.
     */
    public void sendSubscribeFromPeer(String destinationId, Boolean subscribe, String selector, String subtopic) {
        ClusterManager clm = getMessageBroker().getClusterManager();

        String serviceType = getClass().getName();

        clm.invokeServiceOperation(serviceType, destinationId,
                ClusterManager.OPERATION_SUBSCRIBE_FROM_PEER, new Object[]{destinationId, subscribe, selector, subtopic, clm.getLocalAddress(serviceType, destinationId)});
    }

    /**
     * This is called remotely from other cluster members when a new remote subscription is identified.
     * <p>
     * We add or remove a remote subscription...
     *
     * @param destinationId the destination ID
     * @param subscribe     whehter it is a subscribe or unsubscribe
     * @param selector      the selector string
     * @param subtopic      the subtopic string
     * @param remoteAddress the remote node address in the cluster
     */
    public void subscribeFromPeer(String destinationId, Boolean subscribe, String selector, String subtopic, Object remoteAddress) {
        Destination destination = getDestination(destinationId);

        RemoteSubscriptionManager subMgr = ((MessageDestination) destination).getRemoteSubscriptionManager();

        if (destination instanceof MessageDestination) {
            if (debug)
                Log.getLogger(MessageService.LOG_CATEGORY).debug("Received subscription from peer: " + remoteAddress + " subscribe? " + subscribe + " selector: " + selector + " subtopic: " + subtopic);
            if (subscribe)
                subMgr.addSubscriber(remoteAddress, selector, subtopic, null);
            else
                subMgr.removeSubscriber(remoteAddress, selector, subtopic, null);
        } else if (Log.isError())
            Log.getLogger(LOG_CATEGORY).error("subscribeFromPeer called with destination: " + destinationId + " that is not a MessageDestination");
    }

    //--------------------------------------------------------------------------
    //
    // Protected/private APIs
    //
    //--------------------------------------------------------------------------

    /**
     * Used to increment the message count metric for the <code>MessageService</code>. This value is
     * stored in the corresponding MBean. The <code>MessageService</code> already invokes this method
     * in its <code>serviceMessage()</code> and <code>serviceCommand()</code> implementations, but if
     * a subclass overrides these methods completely it should invoke this method appropriately as
     * it processes messages.
     *
     * @param commandMessage Pass <code>true</code> if the message being processed is a <code>CommandMessage</code>;
     *                       otherwise <code>false</code>.
     */
    protected void incrementMessageCount(boolean commandMessage, Message message) {
        if (managed) // Update management metrics.
        {
            MessageDestination destination = (MessageDestination) getDestination(message.getDestination());
            if (destination != null && destination.isManaged()) {
                MessageDestinationControl destinationControl = (MessageDestinationControl) destination.getControl();
                if (destinationControl != null) // Should not happen but just in case.
                {
                    if (commandMessage)
                        destinationControl.incrementServiceCommandCount();
                    else
                        destinationControl.incrementServiceMessageCount();
                }
            }
        }
    }

    /**
     * Processes subscription related <code>CommandMessage</code>s. Subclasses that perform additional
     * custom subscription management should invoke <code>super.manageSubscriptions()</code> if they
     * choose to override this method.
     *
     * @param command The <code>CommandMessage</code> to process.
     */
    protected Message manageSubscriptions(CommandMessage command) {
        Message replyMessage = null;

        MessageDestination destination = (MessageDestination) getDestination(command);
        SubscriptionManager subscriptionManager = destination.getSubscriptionManager();

        Object clientId = command.getClientId();
        String endpointId = (String) command.getHeader(Message.ENDPOINT_HEADER);

        String subtopicString = (String) command.getHeader(AsyncMessage.SUBTOPIC_HEADER_NAME);

        ServiceAdapter adapter = destination.getAdapter();

        if (command.getOperation() == CommandMessage.SUBSCRIBE_OPERATION) {
            String selectorExpr = (String) command.getHeader(CommandMessage.SELECTOR_HEADER);

            getMessageBroker().inspectChannel(command, destination);

            // Give MessagingAdapter a chance to block the subscribe.
            if ((adapter instanceof MessagingAdapter)) {
                MessagingSecurityConstraintManager manager = ((MessagingAdapter) adapter).getSecurityConstraintManager();
                if (manager != null)
                    manager.assertSubscribeAuthorization();
            }

            try {
                /*
                 * This allows parallel add/remove subscribe calls (protected by the
                 * concurrent hash table) but prevents us from doing any table mods
                 * when the getSubscriptionState method is active
                 */
                subscribeLock.readLock().lock();

                if (adapter.handlesSubscriptions()) {
                    replyMessage = (Message) adapter.manage(command);
                } else {
                    testSelector(selectorExpr, command);
                }
                /*
                 * Even if the adapter is managing the subscription, we still need to
                 * register this with the subscription manager so that we can match the
                 * endpoint with the clientId.  I am not sure I like this though because
                 * now the subscription is registered both with the adapter and with our
                 * system so keeping them in sync is potentially problematic.   Also, it
                 * seems like the adapter should have the option to manage endpoints themselves?
                 */

                // Extract the maxFrequency that might have been specified by the client.
                int maxFrequency = processMaxFrequencyHeader(command);
                subscriptionManager.addSubscriber(clientId, selectorExpr, subtopicString, endpointId, maxFrequency);
            } finally {
                subscribeLock.readLock().unlock();
            }

            if (replyMessage == null)
                replyMessage = new AcknowledgeMessage();
        } else if (command.getOperation() == CommandMessage.UNSUBSCRIBE_OPERATION) {
            // Give MessagingAdapter a chance to block the unsubscribe, as long as
            // the subscription has not been invalidated.
            if ((adapter instanceof MessagingAdapter) && command.getHeader(CommandMessage.SUBSCRIPTION_INVALIDATED_HEADER) == null) {
                MessagingSecurityConstraintManager manager = ((MessagingAdapter) adapter).getSecurityConstraintManager();
                if (manager != null)
                    manager.assertSubscribeAuthorization();
            }

            String selectorExpr = (String) command.getHeader(CommandMessage.SELECTOR_HEADER);

            try {
                subscribeLock.readLock().lock();

                if (adapter.handlesSubscriptions()) {
                    replyMessage = (Message) adapter.manage(command);
                }
                subscriptionManager.removeSubscriber(clientId, selectorExpr, subtopicString, endpointId);
            } finally {
                subscribeLock.readLock().unlock();
            }

            if (replyMessage == null)
                replyMessage = new AcknowledgeMessage();
        } else if (command.getOperation() == CommandMessage.MULTI_SUBSCRIBE_OPERATION) {
            getMessageBroker().inspectChannel(command, destination);

            // Give MessagingAdapter a chance to block the multi subscribe.
            if ((adapter instanceof MessagingAdapter)) {
                MessagingSecurityConstraintManager manager = ((MessagingAdapter) adapter).getSecurityConstraintManager();
                if (manager != null)
                    manager.assertSubscribeAuthorization();
            }

            try {
                /*
                 * This allows parallel add/remove subscribe calls (protected by the
                 * concurrent hash table) but prevents us from doing any table mods
                 * when the getSubscriptionState method is active
                 */
                subscribeLock.readLock().lock();

                if (adapter.handlesSubscriptions()) {
                    replyMessage = (Message) adapter.manage(command);
                }

                // Deals with legacy collection setting
                Object[] adds = getObjectArrayFromHeader(command.getHeader(CommandMessage.ADD_SUBSCRIPTIONS));
                Object[] rems = getObjectArrayFromHeader(command.getHeader(CommandMessage.REMOVE_SUBSCRIPTIONS));

                if (adds != null) {
                    // Extract the maxFrequency that might have been specified
                    // by the client for every subscription (selector/subtopic).
                    int maxFrequency = processMaxFrequencyHeader(command);
                    for (int i = 0; i < adds.length; i++) {
                        // Use the maxFrequency by default.
                        int maxFrequencyPerSubscription = maxFrequency;
                        String ss = (String) adds[i];
                        int ix = ss.indexOf(CommandMessage.SUBTOPIC_SEPARATOR);
                        if (ix != -1) {
                            String subtopic = (ix == 0 ? null : ss.substring(0, ix));
                            String selector = null;
                            String selectorAndMaxFrequency = ss.substring(ix + CommandMessage.SUBTOPIC_SEPARATOR.length());
                            if (selectorAndMaxFrequency.length() != 0) {
                                int ix2 = selectorAndMaxFrequency.indexOf(CommandMessage.SUBTOPIC_SEPARATOR);
                                if (ix2 != -1) {
                                    selector = (ix2 == 0 ? null : selectorAndMaxFrequency.substring(0, ix2));
                                    String maxFrequencyString = selectorAndMaxFrequency.substring(ix2 + CommandMessage.SUBTOPIC_SEPARATOR.length());
                                    if (maxFrequencyString.length() != 0) {
                                        // Choose the minimum of Consumer level maxFrequency and subscription level maxFrequency.
                                        int maxFrequencyCandidate = Integer.parseInt(maxFrequencyString);
                                        maxFrequencyPerSubscription = maxFrequencyPerSubscription == 0 ? maxFrequencyCandidate : Math.min(maxFrequencyPerSubscription, maxFrequencyCandidate);
                                    }
                                } else {
                                    selector = selectorAndMaxFrequency;
                                }
                            }
                            subscriptionManager.addSubscriber(clientId, selector, subtopic, endpointId, maxFrequencyPerSubscription);
                        }
                        // invalid message
                    }
                }

                if (rems != null) {
                    for (int i = 0; i < rems.length; i++) {
                        String ss = (String) rems[i];
                        int ix = ss.indexOf(CommandMessage.SUBTOPIC_SEPARATOR);
                        if (ix != -1) {
                            String subtopic = (ix == 0 ? null : ss.substring(0, ix));
                            String selector = null;
                            String selectorAndMaxFrequency = ss.substring(ix + CommandMessage.SUBTOPIC_SEPARATOR.length());
                            if (selectorAndMaxFrequency.length() != 0) {
                                int ix2 = selectorAndMaxFrequency.indexOf(CommandMessage.SUBTOPIC_SEPARATOR);
                                if (ix2 != -1)
                                    selector = ix2 == 0 ? null : selectorAndMaxFrequency.substring(0, ix2);
                                else
                                    selector = selectorAndMaxFrequency;
                            }
                            subscriptionManager.removeSubscriber(clientId, selector, subtopic, endpointId);
                        }
                    }
                }
            } finally {
                subscribeLock.readLock().unlock();
            }

            if (replyMessage == null)
                replyMessage = new AcknowledgeMessage();
        } else if (command.getOperation() == CommandMessage.POLL_OPERATION) {
            // This code path handles poll messages sent by Consumer.receive().
            // This API should not trigger server side waits, so we invoke poll
            // and if there are no queued messages for this Consumer instance we
            // return an empty acknowledgement immediately.
            MessageClient client = null;
            try {
                client = subscriptionManager.getMessageClient(clientId, endpointId);

                if (client != null) {
                    if (adapter.handlesSubscriptions()) {
                        List missedMessages = (List) adapter.manage(command);
                        if (missedMessages != null && !missedMessages.isEmpty()) {
                            MessageBroker broker = getMessageBroker();
                            for (Iterator iter = missedMessages.iterator(); iter.hasNext(); )
                                broker.routeMessageToMessageClient((Message) iter.next(), client);
                        }
                    }
                    FlushResult flushResult = client.getFlexClient().poll(client);
                    List messagesToReturn = (flushResult != null) ? flushResult.getMessages() : null;
                    if (messagesToReturn != null && !messagesToReturn.isEmpty()) {
                        replyMessage = new CommandMessage(CommandMessage.CLIENT_SYNC_OPERATION);
                        replyMessage.setBody(messagesToReturn.toArray());
                    } else {
                        replyMessage = new AcknowledgeMessage();
                    }
                    // Adaptive poll wait is never used in responses to Consumer.receive() calls.
                } else {
                    ServiceException se = new ServiceException();
                    se.setCode(NOT_SUBSCRIBED_CODE);
                    se.setMessage(NOT_SUBSCRIBED, new Object[]{destination.getId()});
                    throw se;
                }
            } finally {
                subscriptionManager.releaseMessageClient(client);
            }
        } else {
            ServiceException se = new ServiceException();
            se.setMessage(UNKNOWN_COMMAND, new Object[]{new Integer(command.getOperation())});
            throw se;
        }

        return replyMessage;
    }

    /**
     * Returns the log category of the <code>MessageService</code>.
     *
     * @return The log category of the component.
     */
    @Override
    protected String getLogCategory() {
        return LOG_CATEGORY;
    }

    /**
     * This method is invoked to allow the <code>MessageService</code> to instantiate and register its
     * MBean control.
     *
     * @param broker The <code>MessageBroker</code> to pass to the <code>MessageServiceControl</code> constructor.
     */
    @Override
    protected void setupServiceControl(MessageBroker broker) {
        controller = new MessageServiceControl(this, broker.getControl());
        controller.register();
        setControl(controller);
    }

    /**
     * Tests a selector in an attempt to avoid runtime errors that we could catch at startup.
     *
     * @param selectorExpression The expression to test.
     * @param msg                A test message.
     */
    private void testSelector(String selectorExpression, Message msg) {
        /*try
        {
            JMSSelector selector = new JMSSelector(selectorExpression);
            selector.match(msg);
        }
        catch (Exception e)
        {*/
        ServiceException se = new ServiceException();
        se.setMessage(BAD_SELECTOR, new Object[]{selectorExpression});
        se.setRootCause(new RuntimeException("JMSSelector removed"));
        throw se;
        //}
    }

    private int processMaxFrequencyHeader(CommandMessage command) {
        Object maxFrequencyHeader = command.getHeader(CommandMessage.MAX_FREQUENCY_HEADER);
        if (maxFrequencyHeader != null)
            return ((Integer) maxFrequencyHeader).intValue();
        return 0;
    }

    private Subtopic testProducerSubtopic(String subtopicSeparator, String subtopicString) {
        Subtopic subtopic = new Subtopic(subtopicString, subtopicSeparator);
        if (subtopic.containsSubtopicWildcard()) {
            ServiceException se = new ServiceException();
            se.setMessage(10556, new Object[]{subtopicString});
            throw se;
        }
        return subtopic;
    }

    private Object[] getObjectArrayFromHeader(Object header) {
        if (header instanceof Object[])
            return (Object[]) header;
        else if (header instanceof List)
            return ((List) header).toArray();
        else if (header == null)
            return null;

        ServiceException se = new ServiceException();
        se.setMessage("Invalid header: " + header + " in message.  expected array or list and found: " + header.getClass().getName());
        throw se;
    }

    /**
     * This is override the method stop().
     * It is needed to provide locking of MessageService.subcribeLock first
     */
    @Override
    public void stop() {
        try {
            subscribeLock.readLock().lock();
            super.stop();
        } finally {
            subscribeLock.readLock().unlock();
        }
    }

}
