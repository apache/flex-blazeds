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

import flex.messaging.client.FlexClient;
import flex.messaging.client.FlexClientListener;
import flex.messaging.log.LogCategories;
import flex.messaging.messages.Message;
import flex.messaging.util.TimeoutAbstractObject;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The base for FlexSession implementations.
 */
public abstract class FlexSession extends TimeoutAbstractObject implements FlexClientListener, MessageClientListener
{
    //--------------------------------------------------------------------------
    //
    // Public Static Variables
    //
    //--------------------------------------------------------------------------

    /**
     * Log category for FlexSession related messages.
     */
    public static final String FLEX_SESSION_LOG_CATEGORY = LogCategories.ENDPOINT_FLEXSESSION;

    /**
     * @exclude
     */
    public static final int MAX_CONNECTIONS_PER_SESSION_UNLIMITED = -1;

    //--------------------------------------------------------------------------
    //
    // Private Static Variables
    //
    //--------------------------------------------------------------------------

    /**
     * The set of session created listeners to notify upon a new session creation.
     */
    private static final CopyOnWriteArrayList<FlexSessionListener> createdListeners = new CopyOnWriteArrayList<FlexSessionListener>();

    /**
     * Error string constants.
     */
    private static final int FLEX_SESSION_INVALIDATED = 10019;

    //--------------------------------------------------------------------------
    //
    // Constructor
    //
    //--------------------------------------------------------------------------

    /**
     * @exclude
     * @deprecated Post 2.6.1 releases require use of the constructor that takes an <tt>AbstractFlexSessionProvider</tt> argument.
     */
    public FlexSession()
    {
        this(null);
    }

    /**
     * @exclude
     * Constructs a new FlexSession instance.
     *
     * @param sessionProvider The provider that instantiated this instance.
     */
    public FlexSession(AbstractFlexSessionProvider sessionProvider)
    {
        this.sessionProvider = sessionProvider;
    }

    //--------------------------------------------------------------------------
    //
    // Static Methods
    //
    //--------------------------------------------------------------------------

    /**
     * Adds a session created listener that will be notified when new sessions
     * are created.
     *
     * @see flex.messaging.FlexSessionListener
     *
     * @param listener The listener to add.
     */
    public static void addSessionCreatedListener(FlexSessionListener listener)
    {
        if (listener != null)
            createdListeners.addIfAbsent(listener);
    }

    /**
     * Removes a session created listener.
     *
     * @see flex.messaging.FlexSessionListener
     *
     * @param listener The listener to remove.
     */
    public static void removeSessionCreatedListener(FlexSessionListener listener)
    {
        if (listener != null)
            createdListeners.remove(listener);
    }

    //--------------------------------------------------------------------------
    //
    // Variables
    //
    //--------------------------------------------------------------------------

    /**
     * Flag used to break cycles during invalidation.
     */
    protected boolean invalidating;

    /**
     * Instance level lock to sync for state changes.
     */
    protected final Object lock = new Object();

    /**
     * Flag indicated whether the session has been invalidated/destroyed.
     */
    protected boolean valid = true;

    /**
     * The attributes associated with this session.
     */
    private Map<String, Object> attributes;

    /**
     * Registered attribute listeners for the session.
     */
    private volatile CopyOnWriteArrayList<FlexSessionAttributeListener> attributeListeners;

    /**
     * Flag indicating whether creation notification has been completed.
     */
    private boolean creationNotified;

    /**
     * The set of session destroy listeners to notify when the session is destroyed.
     */
    private volatile CopyOnWriteArrayList<FlexSessionListener> destroyedListeners;

    /**
     * The associated FlexClients.
     */
    private final CopyOnWriteArrayList<FlexClient> flexClients = new CopyOnWriteArrayList<FlexClient>();

    /**
     * List of associated MessageClients created while this session was active (thread local).
     */
    private volatile CopyOnWriteArrayList<MessageClient> messageClients;

    /**
     * Storage for remote credentials associated with the session; used by the HTTPProxyService
     * when requests are made to a secured remote endpoint.
     */
    private volatile Map remoteCredentials;

    //--------------------------------------------------------------------------
    //
    // Properties
    //
    //--------------------------------------------------------------------------

    //----------------------------------
    //  asyncPollMap
    //----------------------------------

    /**
     * @exclude
     * Used internally to manage async long-polls; not for public use.
     *
     * A map of endpoint to async poll objects that keeps track of what
     * client is parked on a long-poll with what endpoint.
     * We only want an endpoint to have a single connection to a client.
     * Normally, the server leaves an async long-poll in place until
     * data arrives to push to the client or a timeout is reached.
     * However, if two or more browser tabs/windows are sharing the same server session and are both attempting
     * async long-polling, we alternate their parked requests to avoid locking up the browser process by holding too many
     * Http connections open at once.
     * This also aids with closing out 'orphaned' long polls following a browser page reload.
     * Generically typed as <code>Object</code>; using this reference is left up to async poll
     * implementation code.
     */
    public volatile HashMap<String, FlexClient.AsyncPollWithTimeout> asyncPollMap;

    //----------------------------------
    //  flexSessionProvider
    //----------------------------------

    private final AbstractFlexSessionProvider sessionProvider;

    /**
     * @exclude
     * Returns the session provider that created this instance.
     *
     * @return The session provider that created this instance.
     */
    public AbstractFlexSessionProvider getFlexSessionProvider()
    {
        return sessionProvider;
    }

    //----------------------------------
    //  principal
    //----------------------------------

    /**
     * The principal associated with the session.
     */
    private Principal userPrincipal;

    /**
     * This method should be called on FlexContext and not on this class.  Keeping
     * this method for backwards compatibility.  This method will produce
     * correct results when perClientAuthentication is false.  However, it will
     * not return correct results when perClientAuthentication is true.
     *
     * Returns the principal associated with the session. If the client has not
     * authenticated the principal will be null.
     *
     * @return The principal associated with the session.
     */
    public Principal getUserPrincipal()
    {
        synchronized (lock)
        {
            checkValid();
            return userPrincipal;
        }
    }

    /**
     * This method should be called on FlexContext and not on this class.  Keeping
     * this method for backwards compatibility.  Calling this when perClientAuthentication
     * is true will not correctly set the UserPrincipal.
     *
     * @param userPrincipal The principal to associate with the session.
     */
    public void setUserPrincipal(Principal userPrincipal)
    {
        synchronized (lock)
        {
            checkValid();
            this.userPrincipal = userPrincipal;
        }
    }

    //----------------------------------
    //  canStream
    //----------------------------------

    /**
     * @exclude
     * Used internally by streaming endpoints to enforce session level streaming
     * connection limits; not for public use.
     * This flag is volatile to allow for consistent reads across thread without
     * needing to pay the cost for a synchronized lock for each read.
     */
    public volatile boolean canStream = true;

    //----------------------------------
    //  maxConnectionsPerSession
    //----------------------------------

    /**
     * @exclude
     * Used internally by streaming and long polling endpoints to enforce session
     * level streaming connection limits; not for public use. Default value is -1
     * (limitless)
     */
    public int maxConnectionsPerSession = MAX_CONNECTIONS_PER_SESSION_UNLIMITED;

    //----------------------------------
    //  streamingClientsCount
    //----------------------------------

    /**
     * @exclude
     * Used internally by streaming and long polling endpoints to enforce
     * session level streaming connection limits; not for public use.
     *
     * Some browsers put limits on the number of connections per session. For
     * example, Firefox has network.http.max-connections-per-server=8 limit which
     * limits the number of streaming connections per session to 7. Similarly,
     * IE has a limit of 2 per session.
     *
     * This variable is used by streaming and long polling endpoint to keep
     * track of open connections per session and disallow them when needed.
     *
     */
    public int streamingConnectionsCount;

    //----------------------------------
    //  useSmallMessages
    //----------------------------------

    /**
     * @exclude
     */
    private boolean useSmallMessages;

    /**
     * @exclude
     * Determines whether the server can attempt to send small messages
     * for those messages that have a small form. This setting can be overridden
     * by an endpoint's enableSmallMessages switch which controls whether
     * small messages should be sent, even if they are supported.
     *
     * The default is false.
     */
    public boolean useSmallMessages()
    {
        return useSmallMessages;
    }

    /**
     * @exclude
     */
    public void setUseSmallMessages(boolean value)
    {
        useSmallMessages = value;
    }

    //----------------------------------
    //  waitMonitor
    //----------------------------------

    /**
     * @exclude
     * Used internally to manage wait()-based long-polls; not for public use.
     *
     * This is the monitor that a request handling thread associated with this
     * FlexSession is waiting on. Normally, the waiting request handling thread will wait until
     * a new message arrives that can be returned in a poll response or its wait interval times out.
     * This also aids with closing out 'orphaned' long polls following a browser page reload.
     */
    public volatile HashMap<String, FlexClient.EndpointQueue> waitMonitor;

    //--------------------------------------------------------------------------
    //
    // Methods
    //
    //--------------------------------------------------------------------------

    /**
     * Adds a session attribute listener that will be notified when an
     * attribute is added, removed or changed.
     *
     * @param listener The listener to add.
     */
    public void addSessionAttributeListener(FlexSessionAttributeListener listener)
    {
        if (listener != null)
        {
            checkValid();

            if (attributeListeners == null)
            {
                synchronized (lock)
                {
                    if (attributeListeners == null)
                        attributeListeners = new CopyOnWriteArrayList<FlexSessionAttributeListener>();
                }
            }

            attributeListeners.addIfAbsent(listener);
        }
    }

    /**
     * Adds a session destroy listener that will be notified when the session
     * is destroyed. Session destroy listeners are notified after all attributes
     * have been unbound from the session and any FlexSessionBindingListeners
     * and FlexSessionAttributeListeners have been notified.
     *
     * @see flex.messaging.FlexSessionListener
     *
     * @param listener The listener to add.
     */
    public void addSessionDestroyedListener(FlexSessionListener listener)
    {
        if (listener != null)
        {
            checkValid();

            if (destroyedListeners == null)
            {
                synchronized (lock)
                {
                    if (destroyedListeners == null)
                        destroyedListeners = new CopyOnWriteArrayList<FlexSessionListener>();
                }
            }

            destroyedListeners.addIfAbsent(listener);
        }
    }

    /**
     * Returns the attribute bound to the specified name in the session, or null
     * if no attribute is bound under the name.
     *
     * @param name The name the target attribute is bound to.
     * @return The attribute bound to the specified name.
     */
    public Object getAttribute(String name)
    {
        synchronized (lock)
        {
            checkValid();

            return (attributes == null) ? null : attributes.get(name);
        }
    }

    /**
     * Returns a snapshot of the names of all attributes bound to the session.
     *
     * @return A snapshot of the names of all attributes bound to the session.
     */
    public Enumeration<String> getAttributeNames()
    {
        synchronized (lock)
        {
            checkValid();

            if (attributes == null)
                return Collections.enumeration(Collections.<String>emptyList());

            // Return a copy so we do not run into concurrent modification problems if
            // someone adds to the attributes while iterating through the returned enumeration.
            return Collections.enumeration(new ArrayList<String>(attributes.keySet()));
        }
    }

    /**
     * @exclude
     * Implements MessageClientListener.
     * Handling created events is a no-op.
     *
     * @messageClient The new MessageClient.
     */
    public void messageClientCreated(MessageClient messageClient) {}

    /**
     * @exclude
     * Implements MessageClientListener.
     * Notification that an associated MessageClient was destroyed.
     *
     * @param messageClient The MessageClient that was destroyed.
     */
    public void messageClientDestroyed(MessageClient messageClient)
    {
        unregisterMessageClient(messageClient);
    }

    /**
     * @exclude
     * FlexClient invokes this to determine whether the session can be used to push messages
     * to the client.
     *
     * @return true if the FlexSession supports direct push; otherwise false (polling is assumed).
     */
    public abstract boolean isPushSupported();

    /**
     * @exclude
     * FlexClient invokes this to push a message to a remote client.
     *
     * @param message The message to push.
     */
    public void push(Message message)
    {
        throw new UnsupportedOperationException("Push not supported.");
    }

    /**
     * Removes the attribute bound to the specified name in the session.
     *
     * @param name The name of the attribute to remove.
     */
    public void removeAttribute(String name)
    {
        Object value; // Used for event dispatch after the attribute is removed.

        synchronized (lock)
        {
            checkValid(); // Re-enters lock but should be fast because we're already holding it.

            value = (attributes != null) ? attributes.remove(name) : null;
        }

        // If no value was bound under this name it's a no-op.
        if (value == null)
            return;

        notifyAttributeUnbound(name, value);
        notifyAttributeRemoved(name, value);
    }

    /**
     * Removes a session attribute listener.
     *
     * @param listener The listener to remove.
     */
    public void removeSessionAttributeListener(FlexSessionAttributeListener listener)
    {
        // No need to check validity; removing a listener is always ok.
        if (listener != null && attributeListeners != null)
            attributeListeners.remove(listener);
    }

    /**
     * Removes a session destroy listener.
     *
     * @see flex.messaging.FlexSessionListener
     *
     * @param listener The listener to remove.
     */
    public void removeSessionDestroyedListener(FlexSessionListener listener)
    {
        // No need to check validity; removing a listener is always ok.
        if (listener != null && destroyedListeners != null)
            destroyedListeners.remove(listener);
    }

    /**
     * Binds an attribute value to the session under the specified name.
     *
     * @param name The name to bind the attribute under.
     * @param value The value of the attribute.
     */
    public void setAttribute(String name, Object value)
    {
        // Null value is the same as removeAttribute().
        if (value == null)
        {
            removeAttribute(name);
            return;
        }

        Object oldValue; // Used to determine which events to dispatch after the set is performed.

        // Only synchronize for the attribute mutation; event dispatch doesn't require it.
        synchronized (lock)
        {
            checkValid(); // Re-enters lock but should be fast because we're already holding it.

            if (attributes == null)
                attributes = new HashMap<String, Object>();

            oldValue = attributes.put(name, value);
        }

        if (oldValue == null)
        {
            notifyAttributeBound(name, value);
            notifyAttributeAdded(name, value);
        }
        else
        {
            notifyAttributeUnbound(name, oldValue);
            notifyAttributeReplaced(name, oldValue);
            notifyAttributeBound(name, value);
        }
    }

    /**
     * Stores remote credentials in the session for proxied calls to remote systems.
     *
     * @param credentials The remote credentials.
     */
    public void putRemoteCredentials(FlexRemoteCredentials credentials)
    {
        if (credentials != null)
        {
            // We only need to hold the lock to lazy-init the remoteCredentials variable.
            if (remoteCredentials == null)
            {
                synchronized (lock)
                {
                    // Init size to 4 because that's the number of shipping service types
                    // (messaging, remoting, proxy, data management).
                    if (remoteCredentials == null)
                        remoteCredentials = new HashMap(4);
                }
            }
            synchronized (remoteCredentials)
            {
                Map serviceMap = (Map)remoteCredentials.get(credentials.getService());
                if (serviceMap == null)
                {
                    // Init size to half the normal number of buckets; most services won't have a large
                    // number of destinations with remote credentials.
                    serviceMap = new HashMap(7);
                    remoteCredentials.put(credentials.getService(), serviceMap);
                }
                serviceMap.put(credentials.getDestination(), credentials);
            }
        }
    }

    /**
     * Returns the remote credentials stored in the session for the specified service destination.
     *
     * @param serviceId The service id.
     * @param destinationId The destination id.
     * @return The stored remote credentials for the specified service destination.
     */
    public FlexRemoteCredentials getRemoteCredentials(String serviceId, String destinationId)
    {
        if (serviceId != null && destinationId != null)
        {
            if (remoteCredentials == null)
                return null;
            synchronized (remoteCredentials)
            {
                Map serviceMap = (Map)remoteCredentials.get(serviceId);
                return (serviceMap != null) ? (FlexRemoteCredentials)serviceMap.get(destinationId) : null;
            }
        }
        return null;
    }

    /**
     * Clears any stored remote credentials from the session for the specified service destination.
     *
     * @param serviceId The service Id.
     * @param destinationId The destination Id.
     */
    public void clearRemoteCredentials(String serviceId, String destinationId)
    {
        if (serviceId != null && destinationId != null)
        {
            if (remoteCredentials == null)
                return;
            synchronized (remoteCredentials)
            {
                Map serviceMap = (Map)remoteCredentials.get(serviceId);
                if (serviceMap != null)
                {
                    serviceMap.put(destinationId, null);
                }
            }
        }
    }

    /**
     * Invalidates the FlexSession.
     */
    public void invalidate()
    {
        synchronized (lock)
        {
            if (!valid || invalidating)
                return; // Already shutting down.

            invalidating = true; // This thread gets to shut the FlexSession down.
            cancelTimeout();
            if (sessionProvider != null)
                sessionProvider.removeFlexSession(this);
        }

        // Unregister all FlexClients.
        if (!flexClients.isEmpty())
        {
            for (FlexClient flexClient :  flexClients)
                unregisterFlexClient(flexClient);
        }

        // Invalidate associated MessageClient subscriptions.
        if (messageClients != null && !messageClients.isEmpty())
        {
            for (Iterator<MessageClient> iter = messageClients.iterator(); iter.hasNext();)
            {
                MessageClient messageClient = iter.next();
                messageClient.removeMessageClientDestroyedListener(this);
                messageClient.invalidate();
            }
            messageClients.clear();
        }


        // Notify destroy listeners that we're shutting the FlexSession down.
        if (destroyedListeners != null && !destroyedListeners.isEmpty())
        {
            for (FlexSessionListener destroyListener : destroyedListeners)
            {
                destroyListener.sessionDestroyed(this);
            }
            destroyedListeners.clear();
        }

        // Unbind all attributes.
        if (attributes != null && !attributes.isEmpty())
        {
            Set<String> keySet = attributes.keySet();
            String[] keys = keySet.toArray(new String[keySet.size()]);
            for (String key : keys)
                removeAttribute(key);

            attributes = null;
        }

        internalInvalidate();

        synchronized (lock)
        {
            valid = false;
            invalidating = false;
        }

        // Notify any waiting threads.
        if (waitMonitor != null)
        {
            for (FlexClient.EndpointQueue endpointQueue : waitMonitor.values())
            {
                synchronized (endpointQueue)
                {
                    endpointQueue.notifyAll();
                }
            }
        }
    }

    /**
     * Hook for subclasses to perform any custom shutdown.
     * Invoked after the FlexSession has performed generic shutdown but right before the session's valid
     * property flips to false.
     */
    protected void internalInvalidate() {}

    /**
     * Returns a snapshot of the FlexClients associated with the FlexSession
     * when this method is invoked.
     * This list is not guaranteed to remain consistent with the actual list
     * of active FlexClients associated with the FlexSession over time.
     *
     * @return A snapshot of the current list of FlexSessions associated with the FlexClient.
     */
    public List<FlexClient> getFlexClients()
    {
        List<FlexClient> currentFlexClients = null;
        synchronized (lock)
        {
            checkValid(); // Re-enters lock but should be fast because we're already holding it.

            currentFlexClients = new ArrayList<FlexClient>(flexClients); // Make a copy of the current list to return.
        }
        return currentFlexClients;
    }

    /**
     * Returns a snapshot of the MessageClients (subscriptions) associated with the FlexSession
     * when this method is invoked.
     * This list is not guaranteed to remain consistent with the actual list
     * of active MessageClients associated with the FlexSession over time.
     *
     * @return A snapshot of the current list of MessageClients associated with the FlexSession.
     */
    public List<MessageClient> getMessageClients()
    {
        List<MessageClient> currentMessageClients = null;
        synchronized (lock)
        {
            checkValid(); // Re-enters lock but should be fast because we're already holding it.

            currentMessageClients = (messageClients != null) ? new ArrayList<MessageClient>(messageClients) // Make a copy of the current list to return.
                                                             : new ArrayList<MessageClient>(); // Return an empty list.
        }
        return currentMessageClients;
    }

    /**
     * Returns the Id for the session.
     *
     * @return The Id for the session.
     */
    public abstract String getId();

    /**
     * Returns whether the current user is in the specified role.
     *
     * @param role The role to test.
     * @return true if the user is in the role; otherwise false.
     */
    public boolean isUserInRole(String role)
    {
        ArrayList list = new ArrayList();
        list.add(role);
        return FlexContext.getMessageBroker().getLoginManager().checkRoles(userPrincipal, list);
    }

    /**
     * Returns whether the session is valid.
     *
     * @return true if the session is valid; otherwise false.
     */
    public boolean isValid()
    {
        synchronized (lock)
        {
            return valid;
        }
    }

    /**
     * @exclude
     * Implements FlexClientListener interface.
     * Notification that a FlexClient was created.
     * This is a no-op because the FlexSession is never added as a static FlexClient created listener
     * but this method is required by the interface. We only listen for the destroyed event from
     * associated FlexClients.
     *
     * @param flexClient The FlexClient that was created.
     */
    public void clientCreated(FlexClient flexClient) {}

    /**
     * @exclude
     * Implements FlexClientListener interface.
     * Notification that an associated FlexClient was destroyed.
     *
     * @param flexClient The FlexClient that was destroyed.
     */
    public void clientDestroyed(FlexClient flexClient)
    {
        unregisterFlexClient(flexClient);
    }

    /**
     * @exclude
     * Used internally to associate a FlexClient with the FlexSession.
     *
     * @param flexClient The FlexClient to assocaite with the session.
     */
    public void registerFlexClient(FlexClient flexClient)
    {
        if (flexClients.addIfAbsent(flexClient))
        {
            flexClient.addClientDestroyedListener(this);
            flexClient.registerFlexSession(this);
        }
    }

    /**
     * @exclude
     * Used internally to disassociate a FlexClient from the FlexSession.
     *
     * @param flexClient The FlexClient to disassociate from the session.
     */
    public void unregisterFlexClient(FlexClient flexClient)
    {
        if (flexClients.remove(flexClient))
        {
            flexClient.removeClientDestroyedListener(this);
            flexClient.unregisterFlexSession(this);
        }
    }

    /**
     * @exclude
     * Used internally to associate a MessagClient (subscription) with the FlexSession.
     *
     * @param messageClient The MessageClient to associate with the session.
     */
    public void registerMessageClient(MessageClient messageClient)
    {
        if (messageClients == null)
        {
            synchronized (lock)
            {
                if (messageClients == null)
                    messageClients = new CopyOnWriteArrayList<MessageClient>();
            }
        }

        if (messageClients.addIfAbsent(messageClient))
            messageClient.addMessageClientDestroyedListener(this);
    }

    /**
     * @exclude
     * Used internally to disassociate a MessageClient (subscription) from a FlexSession.
     *
     * @param messageClient The MessageClient to disassociate from the session.
     */
    public void unregisterMessageClient(MessageClient messageClient)
    {
        if (messageClients != null && messageClients.remove(messageClient))
            messageClient.removeMessageClientDestroyedListener(this);
    }

    /**
     * Default implementation invokes <code>invalidate()</code> upon timeout.
     *
     * @see flex.messaging.util.TimeoutCapable#timeout()
     */
    public void timeout()
    {
        invalidate();
    }

    //--------------------------------------------------------------------------
    //
    // Protected Methods
    //
    //--------------------------------------------------------------------------

    /**
     * Ensures that the session has not been invalidated.
     */
    protected void checkValid()
    {
        synchronized (lock)
        {
            if (!valid)
            {
                LocalizedException e = new LocalizedException();
                e.setMessage(FLEX_SESSION_INVALIDATED);
                throw e;
            }
        }
    }

    /**
     * Notify attribute listeners that an attribute has been added.
     *
     * @param name The name of the attribute.
     * @param value The new value of the attribute.
     */
    protected void notifyAttributeAdded(String name, Object value)
    {
        if (attributeListeners != null && !attributeListeners.isEmpty())
        {
            FlexSessionBindingEvent event = new FlexSessionBindingEvent(this, name, value);
            // CopyOnWriteArrayList is iteration-safe from ConcurrentModificationExceptions.
            for (FlexSessionAttributeListener attribListener : attributeListeners)
                attribListener.attributeAdded(event);
        }
    }

    /**
     * Notify binding listener that it has been bound to the session.
     *
     * @param name The attribute name.
     * @param value The attribute that has been bound.
     */
    protected void notifyAttributeBound(String name, Object value)
    {
        if ((value != null) && (value instanceof FlexSessionBindingListener))
        {
            FlexSessionBindingEvent bindingEvent = new FlexSessionBindingEvent(this, name);
            ((FlexSessionBindingListener)value).valueBound(bindingEvent);
        }
    }

    /**
     * Notify attribute listeners that an attribute has been removed.
     *
     * @param name The name of the attribute.
     * @param value The previous value of the attribute.
     */
    protected void notifyAttributeRemoved(String name, Object value)
    {
        if (attributeListeners != null && !attributeListeners.isEmpty())
        {
            FlexSessionBindingEvent event = new FlexSessionBindingEvent(this, name, value);
            // CopyOnWriteArrayList is iteration-safe from ConcurrentModificationExceptions.
            for (FlexSessionAttributeListener attribListener : attributeListeners)
                attribListener.attributeRemoved(event);
        }
    }

    /**
     * Notify attribute listeners that an attribute has been replaced.
     *
     * @param name The name of the attribute.
     * @param value The previous value of the attribute.
     */
    protected void notifyAttributeReplaced(String name, Object value)
    {
        if (attributeListeners != null && !attributeListeners.isEmpty())
        {
            FlexSessionBindingEvent event = new FlexSessionBindingEvent(this, name, value);
            // CopyOnWriteArrayList is iteration-safe from ConcurrentModificationExceptions.
            for (FlexSessionAttributeListener attribListener : attributeListeners)
                attribListener.attributeReplaced(event);
        }
    }

    /**
     * Notify binding listener that it has been unbound from the session.
     *
     * @param name The attribute name.
     * @param value The attribute that has been unbound.
     */
    protected void notifyAttributeUnbound(String name, Object value)
    {
        if ((value != null) && (value instanceof FlexSessionBindingListener))
        {
            FlexSessionBindingEvent bindingEvent = new FlexSessionBindingEvent(this, name);
            ((FlexSessionBindingListener)value).valueUnbound(bindingEvent);
        }
    }

    /**
     * Invoked by subclass upon session creation to notify all registered
     * session create listeners of the event.
     * This method must be invoked in the subclass constructor.
     */
    protected void notifyCreated()
    {
        // This guard is here only to prevent duplicate notifications if there's a coding error
        // in the subclass. Not likely..
        synchronized (lock)
        {
            if (creationNotified)
                return;

            creationNotified = true;
        }

        if (!createdListeners.isEmpty())
        {
            // CopyOnWriteArrayList is iteration-safe from ConcurrentModificationExceptions.
            for (Iterator<FlexSessionListener> iter = createdListeners.iterator(); iter.hasNext();)
                iter.next().sessionCreated(this);
        }
    }
}
