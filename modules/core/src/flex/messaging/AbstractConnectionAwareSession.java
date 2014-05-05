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

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @exclude
 * Abstract base class for <tt>ConnectionAwareSession</tt> implementations.
 * Provides support for registering <tt>FlexSessionConnectivityListener</tt>s
 * along with protected methods to notify registered listeners of <tt>FlexSessionConnectivityEvent</tt>s.
 */
public abstract class AbstractConnectionAwareSession extends FlexSession implements ConnectionAwareSession
{
    //--------------------------------------------------------------------------
    //
    // Properties
    //
    //--------------------------------------------------------------------------

    /**
     * @exclude
     * Constructs a new instance.
     *
     * @param sessionProvider The provider that instantiated this instance.
     */
    public AbstractConnectionAwareSession(AbstractFlexSessionProvider sessionProvider)
    {
        super(sessionProvider);
    }
    
    //--------------------------------------------------------------------------
    //
    // Properties
    //
    //--------------------------------------------------------------------------

    //----------------------------------
    //  connected
    //----------------------------------
    
    /**
     * Connected flag for the session.
     */
    private boolean connected;

    /**
     * Returns whether the session is connected.
     * 
     * @return true if the session is connected; otherwise false.
     */
    public boolean isConnected()
    {
        synchronized (lock)
        {
            return connected;
        }
    }
    
    /**
     * Sets the connected state for the session.
     * 
     * @param value true for a connected session; false for a disconnected session.
     */
    public void setConnected(boolean value)
    {
        boolean notify = false;
        synchronized (lock)
        {
            if (connected != value)
            {
                connected = value;
                notify = true;
            }
        }
        if (notify)
        {
            if (!value)
                notifySessionDisconnected();
            else
                notifySessionConnected();
        }
    }
    
    //----------------------------------
    //  connectivityListeners
    //----------------------------------
    
    /**
     * The list of connectivity listeners for the session.
     */
    private volatile CopyOnWriteArrayList<FlexSessionConnectivityListener> connectivityListeners;
    
    /**
     * (non-JavaDoc)
     * @see flex.messaging.ConnectionAwareSession#addConnectivityListener(FlexSessionConnectivityListener)
     */
    public void addConnectivityListener(FlexSessionConnectivityListener listener)
    {
        if (connectivityListeners == null)
        {
            synchronized (lock)
            {
                if (connectivityListeners == null)
                    connectivityListeners = new CopyOnWriteArrayList<FlexSessionConnectivityListener>(); 
            }
        }
        if (connectivityListeners.addIfAbsent(listener) && isConnected())
        {
            // If the listener is added when the session has already connected, notify it at add time.
            FlexSessionConnectivityEvent event = new FlexSessionConnectivityEvent(this);
            listener.sessionConnected(event);
        }
    }

    /**
     * (non-JavaDoc)
     * @see flex.messaging.ConnectionAwareSession#removeConnectivityListener(FlexSessionConnectivityListener)
     */
    public void removeConnectivityListener(FlexSessionConnectivityListener listener)
    {
        if (connectivityListeners == null) return;
        connectivityListeners.remove(listener);
    }
    
    //--------------------------------------------------------------------------
    //
    // Protected Methods
    //
    //--------------------------------------------------------------------------    
    
    /**
     * Notifies registered <tt>FlexSessionConnectivityListener</tt>s that the session has connected.
     */
    protected void notifySessionConnected()
    {
        if (connectivityListeners != null)
        {
            FlexSessionConnectivityEvent event = new FlexSessionConnectivityEvent(this);
            for (FlexSessionConnectivityListener listener : connectivityListeners)
                listener.sessionDisconnected(event);
        }
    }
    
    /**
     * Notifies registered <tt>FlexSessionConnectivityListener</tt>s that the session has disconnected.
     */
    protected void notifySessionDisconnected()
    {
        if (connectivityListeners != null)
        {
            FlexSessionConnectivityEvent event = new FlexSessionConnectivityEvent(this);
            for (FlexSessionConnectivityListener listener : connectivityListeners)
                listener.sessionDisconnected(event);            
        }
    }    
}