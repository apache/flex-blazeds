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

import flex.messaging.messages.Message;

import java.util.ArrayList;

/**
 * @exclude
 * Supports registration and notification of <tt>MessageRoutedListener</tt>s.
 * An instance of this class is exposed by <tt>FlexContext</tt> while a message is
 * being routed, and once routing of the message to the outbound messages queues for
 * target clients and registered listeners are notified.
 * This class performs no synchronization because it is only used within the context
 * of a single Thread, and only during the routing of a single message.
 */
public class MessageRoutedNotifier
{
    //--------------------------------------------------------------------------
    //
    // Constructor
    //
    //--------------------------------------------------------------------------
    
    /**
     * Constructs a <tt>MessageRoutedNotifier</tt> for the supplied source message.
     * 
     * @param The source message being routed.
     */
    public MessageRoutedNotifier(Message message)
    {
        this.message = message;
    }
    
    //--------------------------------------------------------------------------
    //
    // Variables
    //
    //--------------------------------------------------------------------------
    
    /**
     * The source message being routed.
     */
    private final Message message;
    
    //--------------------------------------------------------------------------
    //
    // Properties
    //
    //--------------------------------------------------------------------------

    //----------------------------------
    //  messageRoutedListeners
    //----------------------------------
    
    private ArrayList listeners;
    
    /**
     * Adds a <tt>MessageRoutedListener</tt>.
     */
    public void addMessageRoutedListener(MessageRoutedListener listener)
    {
        if (listener != null)
        {
            // Lazy-init only if necessary.
            if (listeners == null)
                listeners = new ArrayList();
            
            // Add if absent.
            if (!listeners.contains(listener))
                listeners.add(listener);
        }
    }
    
    /**
     * Removes a <tt>MessageRoutedListener</tt>.
     */
    public void removeMessageRoutedListener(MessageRoutedListener listener)
    {
        if ((listener != null) && (listeners != null))
            listeners.remove(listener);
    }
    
    //--------------------------------------------------------------------------
    //
    // Public Methods
    //
    //--------------------------------------------------------------------------

    /**
     * Notifies registered listeners of a routed message.
     * 
     * @param message The message that has been routed.
     */
    public void notifyMessageRouted()
    {
        if ((listeners != null) && !listeners.isEmpty())
        {
            MessageRoutedEvent event = new MessageRoutedEvent(message);
            int n = listeners.size();
            for (int i = 0; i < n; ++i)
                ((MessageRoutedListener)listeners.get(i)).messageRouted(event);
        }        
    }
}
