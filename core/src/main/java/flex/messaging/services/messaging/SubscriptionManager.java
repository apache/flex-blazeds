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
package flex.messaging.services.messaging;

import flex.management.ManageableComponent;
import flex.messaging.FlexContext;
import flex.messaging.MessageClient;
import flex.messaging.MessageDestination;
import flex.messaging.MessageException;
import flex.messaging.client.FlexClient;
import flex.messaging.config.ServerSettings.RoutingMode;
import flex.messaging.log.Log;
import flex.messaging.log.LogCategories;
import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.Message;
import flex.messaging.security.MessagingSecurity;
import flex.messaging.services.MessageService;
import flex.messaging.services.ServiceAdapter;
import flex.messaging.services.ServiceException;
import flex.messaging.util.StringUtils;
import flex.messaging.util.TimeoutManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;

/**
 * The SubscriptionManager monitors subscribed clients for MessageService
 * and its subclasses, such as DataService.
 */
public class SubscriptionManager extends ManageableComponent {
    public static final String TYPE = "SubscriptionManager";
    private static final int SUBTOPICS_NOT_SUPPORTED = 10553;
    private static final int WILDCARD_SUBTOPICS_NOT_ALLOWED = 10560;
    private static final Object classMutex = new Object();
    private static int instanceCount = 0;

    /**
     * clientId to MessageClient Map for any subscriber. Note that clientId is tracked as
     * Object instead of String because in clustering, clientId is not guaranteed to be String.
     */
    protected final Map<Object, MessageClient> allSubscriptions = new ConcurrentHashMap<Object, MessageClient>();
    // This lock protects allSubscriptions as synchronizing on a Concurrent class does not work.
    private final Object allSubscriptionsLock = new Object();

    /**
     * Subscriptions with no subtopic.
     */
    private final TopicSubscription globalSubscribers = new TopicSubscription();

    /**
     * Subscriptions with a simple subtopic.
     */
    private final Map<Subtopic, TopicSubscription> subscribersPerSubtopic = new ConcurrentHashMap<Subtopic, TopicSubscription>();

    /**
     * Subscriptions with a wildcard subtopic.
     */
    private final Map<Subtopic, TopicSubscription> subscribersPerSubtopicWildcard = new ConcurrentHashMap<Subtopic, TopicSubscription>();

    protected final MessageDestination destination;
    // We can either timeout subscriptions by session expiration (idleSubscriptionTimeout=0) or by an explicit
    // timeout.  If we time them out by timeout, this refers to the TimeoutManager
    // we use to monitor session timeouts.
    private TimeoutManager subscriberSessionManager;
    private long subscriptionTimeoutMillis;

    /**
     * Construct a subscription manager for a destination.
     *
     * @param destination the destination
     */
    public SubscriptionManager(MessageDestination destination) {
        this(destination, false);
    }

    /**
     * Construct a subscription manager for a destination.
     *
     * @param destination      the destination
     * @param enableManagement turn on management?
     */
    public SubscriptionManager(MessageDestination destination, boolean enableManagement) {
        super(enableManagement);
        synchronized (classMutex) {
            super.setId(TYPE + ++instanceCount);
        }
        this.destination = destination;

        subscriptionTimeoutMillis = 0;
    }

    // This component's id should never be changed as it's generated internally

    /**
     * {@inheritDoc}
     */
    @Override
    public void setId(String id) {
        // No-op
    }

    /**
     * Stops the subscription manager.
     */
    @Override
    public void stop() {
        super.stop();

        // Remove management.
        if (isManaged() && getControl() != null) {
            getControl().unregister();
            setControl(null);
            setManaged(false);
        }

        // Destroy subscriptions
        synchronized (this) {
            if (subscriberSessionManager != null) {
                subscriberSessionManager.shutdown();
                subscriberSessionManager = null;
            }
        }

        synchronized (allSubscriptionsLock) {
            if (!allSubscriptions.isEmpty()) {
                for (Map.Entry<Object, MessageClient> objectMessageClientEntry : allSubscriptions.entrySet())
                    removeSubscriber(objectMessageClientEntry.getValue());
            }
        }
    }

    /**
     * Set the timeout value.  Creates a timeout manager thread if needed.
     *
     * @param value the timeout value in milliseconds
     */
    public void setSubscriptionTimeoutMillis(long value) {
        subscriptionTimeoutMillis = value;
        if (subscriptionTimeoutMillis > 0) {
            subscriberSessionManager = new TimeoutManager(new ThreadFactory() {
                int counter = 1;

                public synchronized Thread newThread(Runnable runnable) {
                    Thread t = new Thread(runnable);
                    t.setName(destination.getId() + "-SubscriptionTimeoutThread-" + counter++);
                    return t;
                }
            });
        }
    }

    /**
     * Returns the subscription timeout.
     *
     * @return the timeout in milliseconds
     */
    public long getSubscriptionTimeoutMillis() {
        return subscriptionTimeoutMillis;
    }

    /**
     * Implement a serializer instance which wraps the subscription
     * manager in a transient variable.  It will need to block out
     * all sub/unsub messages before they are broadcast to the
     * remote server, iterate through the maps of subscriptions and
     * for each "unique" subscription it writes the selector and
     * subtopic.
     * <p>
     * synchronization note: this assumes no add/remove subscriptions
     * are occurring while this method is called.
     *
     * @return a List of subscriptions selectors and subtopics
     */
    public Object getSubscriptionState() {
        ArrayList<String> subState = new ArrayList<String>();

        if (globalSubscribers.defaultSubscriptions != null &&
                !globalSubscribers.defaultSubscriptions.isEmpty()) {
            subState.add(null); // selector string
            subState.add(null); // subtopic string
        }
        if (globalSubscribers.selectorSubscriptions != null) {
            for (String s : globalSubscribers.selectorSubscriptions.keySet()) {
                subState.add(s);
                subState.add(null); // subtopic
            }
        }
        addSubscriptionState(subState, subscribersPerSubtopic);
        addSubscriptionState(subState, subscribersPerSubtopicWildcard);

        if (Log.isDebug())
            Log.getLogger(MessageService.LOG_CATEGORY).debug("Retrieved subscription state to send to new cluster member for destination: " + destination.getId() + ": " + StringUtils.NEWLINE + subState);

        return subState;
    }

    private void addSubscriptionState(List<String> subState, Map<Subtopic, TopicSubscription> subsPerSubtopic) {
        for (Map.Entry<Subtopic, TopicSubscription> entry : subsPerSubtopic.entrySet()) {
            Subtopic subtopic = entry.getKey();
            TopicSubscription tc = entry.getValue();

            if (tc.defaultSubscriptions != null && !tc.defaultSubscriptions.isEmpty()) {
                subState.add(null);
                subState.add(subtopic.toString());
            }
            if (tc.selectorSubscriptions != null) {
                for (String s : tc.selectorSubscriptions.keySet()) {
                    subState.add(s);
                    subState.add(subtopic.toString()); // subtopic
                }
            }
        }

    }

    /**
     * Get a string representation of the subscription state.
     *
     * @return the string
     */
    protected String getDebugSubscriptionState() {
        StringBuffer sb = new StringBuffer(100);

        sb.append(" global subscriptions: ").append(globalSubscribers).append(StringUtils.NEWLINE);
        sb.append(" regular subtopic subscriptions: ").append(subscribersPerSubtopic).append(StringUtils.NEWLINE);
        sb.append(" wildcard subtopic subscriptions: ").append(subscribersPerSubtopicWildcard).append(StringUtils.NEWLINE);
        return sb.toString();
    }

    @Override
    protected String getLogCategory() {
        return MessageService.LOG_CATEGORY;
    }

    /**
     * Return the ids of our subscribers.
     *
     * @return a set of subscriber ids
     */
    public Set<Object> getSubscriberIds() {
        return allSubscriptions.keySet();
    }

    /**
     * Return the set of subscribers for a message.
     *
     * @param message      the message
     * @param evalSelector should the selector be evaluated?
     * @return the set of subscribers
     */
    public Set<Object> getSubscriberIds(Message message, boolean evalSelector) {
        Set<Object> ids = new LinkedHashSet<Object>();

        Object subtopicObj = message.getHeader(AsyncMessage.SUBTOPIC_HEADER_NAME);

        if (subtopicObj instanceof Object[])
            subtopicObj = Arrays.asList((Object[]) subtopicObj);

        if (subtopicObj instanceof String) {
            String subtopicString = (String) subtopicObj;

            if (subtopicString.length() > 0)
                addSubtopicSubscribers(subtopicString, message, ids, evalSelector);
            else
                addTopicSubscribers(globalSubscribers, message, ids, evalSelector);
        } else if (subtopicObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> subtopicList = (List<String>) subtopicObj;
            for (String aSubtopicList : subtopicList) {
                addSubtopicSubscribers(aSubtopicList, message, ids, evalSelector);
            }
        } else
            addTopicSubscribers(globalSubscribers, message, ids, evalSelector);

        return ids;
    }

    /**
     * Return the set of subscribers for a message.
     *
     * @param message      the message
     * @param evalSelector hould the selector be evaluated?
     * @param subtopics    the subtopics to use
     * @return the set of subscribers
     */
    public Set<Object> getSubscriberIds(Message message, boolean evalSelector, List<Subtopic> subtopics) {
        Set<Object> ids = new LinkedHashSet<Object>();

        if (subtopics == null || subtopics.isEmpty()) {
            addTopicSubscribers(globalSubscribers, message, ids, evalSelector);
        } else {
            for (Subtopic subtopic : subtopics) {
                addSubtopicSubscribers(subtopic, message, ids, evalSelector);
            }
        }
        return ids;
    }

    /**
     * Return the set of subscribers for a subtopic pattern.
     * Constructs a message and calls {@link #getSubscriberIds(flex.messaging.messages.Message, boolean)}.
     *
     * @param subtopicPattern the pattern to match
     * @param messageHeaders  the message headers
     * @return the set of subscribers
     */
    public Set<Object> getSubscriberIds(String subtopicPattern, Map messageHeaders) {
        // This could be more efficient but we'd have to change the SQLParser to accept a map.
        Message msg = new AsyncMessage();
        msg.setHeader(AsyncMessage.SUBTOPIC_HEADER_NAME, subtopicPattern);
        if (messageHeaders != null)
            msg.setHeaders(messageHeaders);
        return getSubscriberIds(msg, true);
    }

    void addSubtopicSubscribers(String subtopicString, Message message, Set<Object> ids, boolean evalSelector) {
        Subtopic subtopic = getSubtopic(subtopicString);
        addSubtopicSubscribers(subtopic, message, ids, evalSelector);
    }

    void addSubtopicSubscribers(Subtopic subtopic, Message message, Set<Object> ids, boolean evalSelector) {
        // If we have a subtopic, we need to route the message only to that
        // subset of subscribers.
        if (!destination.getServerSettings().getAllowSubtopics()) {
            // Throw an error - the destination doesn't allow subtopics.
            ServiceException se = new ServiceException();
            se.setMessage(SUBTOPICS_NOT_SUPPORTED, new Object[]{subtopic.getValue(), destination.getId()});
            throw se;
        }

        // Give a MessagingAdapter a chance to block the send to this subtopic.
        ServiceAdapter adapter = destination.getAdapter();
        if (adapter instanceof MessagingSecurity) {
            if (!((MessagingSecurity) adapter).allowSend(subtopic)) {
                ServiceException se = new ServiceException();
                se.setMessage(10558, new Object[]{subtopic.getValue()});
                throw se;
            }
        }

        TopicSubscription ts;
        if (subscribersPerSubtopic.containsKey(subtopic)) {
            ts = subscribersPerSubtopic.get(subtopic);
            addTopicSubscribers(ts, message, ids, evalSelector);
        }

        /*
         * TODO: performance - organize these into a tree so we can find consumers via
         * a hashtable lookup rather than a linear search
         */
        Set<Subtopic> subtopics = subscribersPerSubtopicWildcard.keySet();
        if (!subtopics.isEmpty()) {
            for (Subtopic st : subtopics) {
                if (st.matches(subtopic)) {
                    ts = subscribersPerSubtopicWildcard.get(st);
                    addTopicSubscribers(ts, message, ids, evalSelector);
                }
            }
        }
    }

    void addTopicSubscribers(TopicSubscription ts, Message message, Set<Object> ids, boolean evalSelector) {
        if (ts == null)
            return;

        Map<Object, MessageClient> subs = ts.defaultSubscriptions;
        if (subs != null)
            ids.addAll(subs.keySet());

        if (ts.selectorSubscriptions == null)
            return;

        for (Map.Entry<String, Map<Object, MessageClient>> entry : ts.selectorSubscriptions.entrySet()) {
            String selector = entry.getKey();
            subs = entry.getValue();

            /*if (!evalSelector)
            {*/
            ids.addAll(subs.keySet());
            /*}
            else
            {
                JMSSelector jmsSel = new JMSSelector(selector);
                try
                {
                    if (jmsSel.match(message))
                    {
                        ids.addAll(subs.keySet());
                    }
                }
                catch (JMSSelectorException jmse)
                {
                    if (Log.isWarn())
                    {
                        Log.getLogger(JMSSelector.LOG_CATEGORY).warn("Error processing message selector: " +
                                jmsSel + StringUtils.NEWLINE +
                                "  incomingMessage: " + message + StringUtils.NEWLINE +
                                "  selector: " + selector);
                    }
                }
            }*/
        }
    }

    /**
     * Returns the requested subscriber.
     * If the subscriber exists it is also registered for subscription timeout if necessary.
     * If the subscriber is not found this method returns null.
     *
     * @param clientId The clientId of the target subscriber.
     * @return The subscriber, or null if the subscriber is not found.
     */
    public MessageClient getSubscriber(Object clientId) {
        MessageClient client = allSubscriptions.get(clientId);
        if (client != null && !client.isTimingOut())
            monitorTimeout(client);
        return client;
    }

    /**
     * Removes the subscriber, unsubscribing it from all current subscriptions.
     * This is used by the admin UI.
     *
     * @param client the client
     */
    public void removeSubscriber(MessageClient client) {
        // Sends unsub messages for each subscription for this MessageClient which
        // should mean we remove the client at the end.
        client.invalidate();

        if (getSubscriber(client.getClientId()) != null)
            Log.getLogger(MessageService.LOG_CATEGORY).error("Failed to remove client: " + client.getClientId());
    }

    /**
     * Add a subscriber.
     *
     * @param clientId       the client id
     * @param selector       the selector
     * @param subtopicString the subtopic
     * @param endpointId     the endpoint
     */
    public void addSubscriber(Object clientId, String selector, String subtopicString, String endpointId) {
        addSubscriber(clientId, selector, subtopicString, endpointId, 0);
    }

    /**
     * Add a subscriber.
     *
     * @param clientId       the client id
     * @param selector       the selector
     * @param subtopicString the subtopic
     * @param endpointId     the endpoint
     * @param maxFrequency   maximum frequency
     */
    public void addSubscriber(Object clientId, String selector, String subtopicString, String endpointId, int maxFrequency) {
        Subtopic subtopic = getSubtopic(subtopicString);
        MessageClient client = null;
        TopicSubscription topicSub;
        Map<Object, MessageClient> subs;
        Map<Subtopic, TopicSubscription> map;

        try {
            // Handle resubscribes from the same client and duplicate subscribes from different clients
            boolean subscriptionAlreadyExists = (getSubscriber(clientId) != null);
            client = getMessageClient(clientId, endpointId);

            FlexClient flexClient = FlexContext.getFlexClient();
            if (subscriptionAlreadyExists) {
                // Block duplicate subscriptions from multiple FlexClients if they
                // attempt to use the same clientId.  (when this is called from a remote
                // subscription, there won't be a flex client so skip this test).
                if (flexClient != null && !flexClient.getId().equals(client.getFlexClient().getId())) {
                    ServiceException se = new ServiceException();
                    se.setMessage(10559, new Object[]{clientId});
                    throw se;
                }

                // It's a resubscribe. Reset the endpoint push state for the subscription to make sure its current
                // because a resubscribe could be arriving over a new endpoint or a new session.
                client.resetEndpoint(endpointId);
            }

            ServiceAdapter adapter = destination.getAdapter();
            client.updateLastUse();

            if (subtopic == null) {
                topicSub = globalSubscribers;
            } else {
                if (!destination.getServerSettings().getAllowSubtopics()) {
                    // Throw an error - the destination doesn't allow subtopics.
                    ServiceException se = new ServiceException();
                    se.setMessage(SUBTOPICS_NOT_SUPPORTED, new Object[]{subtopicString, destination.getId()});
                    throw se;
                }

                if (subtopic.containsSubtopicWildcard() && destination.getServerSettings().isDisallowWildcardSubtopics()) {
                    // Attempt to subscribe to the subtopic, ''{0}'', on destination, ''{1}'', that does not allow wilcard subtopics failed.
                    ServiceException se = new ServiceException();
                    se.setMessage(WILDCARD_SUBTOPICS_NOT_ALLOWED, new Object[]{subtopicString, destination.getId()});
                    throw se;
                }

                // Give a MessagingAdapter a chance to block the subscribe.
                if ((adapter instanceof MessagingSecurity) && (subtopic != null)) {
                    if (!((MessagingSecurity) adapter).allowSubscribe(subtopic)) {
                        ServiceException se = new ServiceException();
                        se.setMessage(10557, new Object[]{subtopicString});
                        throw se;
                    }
                }

                /*
                 * If there is a wildcard, we always need to match that subscription
                 * against the producer.  If it has no wildcard, we can do a quick
                 * lookup to find the subscribers.
                 */
                if (subtopic.containsSubtopicWildcard())
                    map = subscribersPerSubtopicWildcard;
                else
                    map = subscribersPerSubtopic;

                synchronized (this) {
                    topicSub = map.get(subtopic);
                    if (topicSub == null) {
                        topicSub = new TopicSubscription();
                        map.put(subtopic, topicSub);
                    }
                }
            }

            /* Subscribing with no selector */
            if (selector == null) {
                subs = topicSub.defaultSubscriptions;
                if (subs == null) {
                    synchronized (this) {
                        if ((subs = topicSub.defaultSubscriptions) == null)
                            topicSub.defaultSubscriptions = subs = new ConcurrentHashMap<Object, MessageClient>();
                    }
                }
            }
            /* Subscribing with a selector - store all subscriptions under the selector key */
            else {
                synchronized (this) {
                    if (topicSub.selectorSubscriptions == null)
                        topicSub.selectorSubscriptions = new ConcurrentHashMap<String, Map<Object, MessageClient>>();
                }

                subs = topicSub.selectorSubscriptions.get(selector);
                if (subs == null) {
                    synchronized (this) {
                        if ((subs = topicSub.selectorSubscriptions.get(selector)) == null)
                            topicSub.selectorSubscriptions.put(selector, subs = new ConcurrentHashMap<Object, MessageClient>());
                    }
                }
            }

            if (subs.containsKey(clientId)) {
                /* I'd rather this be an error but in 2.0 we allowed this without error */
                if (Log.isWarn())
                    Log.getLogger(LogCategories.MESSAGE_SELECTOR).warn("Client: " + clientId + " already subscribed to: " + destination.getId() + " selector: " + selector + " subtopic: " + subtopicString);
            } else {
                client.addSubscription(selector, subtopicString, maxFrequency);
                synchronized (this) {
                    /*
                     * Make sure other members of the cluster know that we are subscribed to
                     * this info if we are in server-to-server mode
                     *
                     * This has to be done in the synchronized section so that we properly
                     * order subscribe and unsubscribe messages for our peers so their
                     * subscription state matches the one in the local server.
                     */
                    if (subs.isEmpty() && destination.isClustered() &&
                            destination.getServerSettings().getRoutingMode() == RoutingMode.SERVER_TO_SERVER)
                        sendSubscriptionToPeer(true, selector, subtopicString);
                    subs.put(clientId, client);
                }
                monitorTimeout(client); // local operation, timeouts on remote host are not started until failover

                // Finally, if a new MessageClient was created, notify its created
                // listeners now that MessageClient's subscription state is setup.
                if (!subscriptionAlreadyExists)
                    client.notifyCreatedListeners();
            }
        } finally {
            releaseMessageClient(client);
        }

    }

    /**
     * Remove a subscriber.
     *
     * @param clientId       the client id
     * @param selector       the selector
     * @param subtopicString the subtopic
     * @param endpointId     the endpoint
     */
    public void removeSubscriber(Object clientId, String selector, String subtopicString, String endpointId) {
        MessageClient client = null;
        try {
            synchronized (allSubscriptionsLock) {
                // Do a simple lookup first to avoid the creation of a new MessageClient instance
                // in the following call to getMessageClient() if the subscription is already removed.
                client = allSubscriptions.get(clientId);
                if (client == null) // Subscription was already removed.
                    return;

                // Re-get in order to track refs correctly.
                client = getMessageClient(clientId, endpointId);
            }

            Subtopic subtopic = getSubtopic(subtopicString);
            TopicSubscription topicSub;
            Map<Object, MessageClient> subs;
            Map<Subtopic, TopicSubscription> map = null;

            if (subtopic == null) {
                topicSub = globalSubscribers;
            } else {
                if (subtopic.containsSubtopicWildcard())
                    map = subscribersPerSubtopicWildcard;
                else
                    map = subscribersPerSubtopic;

                topicSub = map.get(subtopic);

                if (topicSub == null)
                    throw new MessageException("Client: " + clientId + " not subscribed to subtopic: " + subtopic);
            }

            if (selector == null)
                subs = topicSub.defaultSubscriptions;
            else
                subs = topicSub.selectorSubscriptions.get(selector);

            if (subs == null || subs.get(clientId) == null)
                throw new MessageException("Client: " + clientId + " not subscribed to destination with selector: " + selector);

            synchronized (this) {
                subs.remove(clientId);
                if (subs.isEmpty() &&
                        destination.isClustered() && destination.getServerSettings().getRoutingMode() == RoutingMode.SERVER_TO_SERVER)
                    sendSubscriptionToPeer(false, selector, subtopicString);

                if (subs.isEmpty()) {
                    if (selector != null) {
                        if (topicSub.selectorSubscriptions != null && !topicSub.selectorSubscriptions.isEmpty())
                            topicSub.selectorSubscriptions.remove(selector);
                    }

                    if (subtopic != null &&
                            (topicSub.selectorSubscriptions == null || topicSub.selectorSubscriptions.isEmpty()) &&
                            (topicSub.defaultSubscriptions == null || topicSub.defaultSubscriptions.isEmpty())) {
                        if ((topicSub.selectorSubscriptions == null || topicSub.selectorSubscriptions.isEmpty()) &&
                                (topicSub.defaultSubscriptions == null || topicSub.defaultSubscriptions.isEmpty()))
                            map.remove(subtopic);
                    }
                }
            }

            if (client.removeSubscription(selector, subtopicString)) {
                allSubscriptions.remove(clientId);
                client.invalidate(); // Destroy the MessageClient.
            }
        } finally {
            if (client != null)
                releaseMessageClient(client);
        }
    }

    /**
     * Create a new MessageClient object.
     *
     * @param clientId   the client id
     * @param endpointId the endpoint
     * @return constructed MessageClient
     */
    protected MessageClient newMessageClient(Object clientId, String endpointId) {
        return new MessageClient(clientId, destination, endpointId, true);
    }

    /**
     * This method is used for subscribers who maintain client ids in their
     * own subscription tables.  It ensures we have the MessageClient for
     * a given clientId for as long as this session is valid (or the
     * subscription times out).
     *
     * @param clientId   the client id
     * @param endpointId the endpoint
     * @return registered MessageClient
     */
    public MessageClient registerMessageClient(Object clientId, String endpointId) {
        MessageClient client = getMessageClient(clientId, endpointId);

        monitorTimeout(client);

        /*
         * There is only one reference to the MessageClient for the
         * registered flag.  If someone happens to register the
         * same client more than once, just allow that to add one reference.
         */
        if (client.isRegistered())
            releaseMessageClient(client);
        else
            client.setRegistered(true);

        return client;
    }

    /**
     * Return a message client, creating it if needed.
     *
     * @param clientId   the client if
     * @param endpointId the endpoint
     * @return the MessageClient
     */
    public MessageClient getMessageClient(Object clientId, String endpointId) {
        synchronized (allSubscriptionsLock) {
            MessageClient client = allSubscriptions.get(clientId);
            if (client == null) {
                client = newMessageClient(clientId, endpointId);
                allSubscriptions.put(clientId, client);
            }

            client.incrementReferences();
            return client;
        }
    }

    /**
     * Release a client.
     *
     * @param client the client to release
     */
    public void releaseMessageClient(MessageClient client) {
        if (client == null)
            return;

        synchronized (allSubscriptionsLock) {
            if (client.decrementReferences()) {
                allSubscriptions.remove(client.getClientId());
                client.invalidate(); // Destroy the MessageClient.
            }
        }
    }

    /**
     * Turn on timeout for the provided client.
     *
     * @param client the client
     */
    protected void monitorTimeout(MessageClient client) {
        if (subscriberSessionManager != null) {
            synchronized (client) {
                if (!client.isTimingOut()) {
                    subscriberSessionManager.scheduleTimeout(client);
                    client.setTimingOut(true);
                }
            }
        }
    }

    private Subtopic getSubtopic(String subtopic) {
        return subtopic != null ?
                new Subtopic(subtopic, destination.getServerSettings().getSubtopicSeparator()) : null;
    }

    /**
     * Broadcast this subscribe/unsubscribe message to the cluster so everyone is aware
     * of this server's interest in messages matching this selector and subtopic.
     *
     * @param subscribe are we subscribing?
     * @param selector  the selector
     * @param subtopic  the subtopic
     */
    protected void sendSubscriptionToPeer(boolean subscribe, String selector, String subtopic) {
        if (Log.isDebug())
            Log.getLogger(MessageService.LOG_CATEGORY).debug("Sending subscription to peers for subscribe? " + subscribe + " selector: " + selector + " subtopic: " + subtopic);

        ((MessageService) destination.getService()).sendSubscribeFromPeer(destination.getId(), subscribe, selector, subtopic);
    }

    static class TopicSubscription {
        /**
         * This is the Map of clientId to MessageClient for each client subscribed to this topic with no selector.
         */
        Map<Object, MessageClient> defaultSubscriptions;

        /**
         * A map of selector string to Map of clientId to MessageClient.
         */
        Map<String, Map<Object, MessageClient>> selectorSubscriptions;

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer(100);

            sb.append("default subscriptions: ").append(defaultSubscriptions).append(StringUtils.NEWLINE);
            sb.append("selector subscriptions: ").append(selectorSubscriptions).append(StringUtils.NEWLINE);
            return sb.toString();
        }
    }
}
