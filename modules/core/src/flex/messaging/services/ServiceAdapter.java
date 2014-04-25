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

import flex.management.ManageableComponent;
import flex.management.runtime.messaging.DestinationControl;
import flex.messaging.Destination;
import flex.messaging.log.Log;
import flex.messaging.messages.AcknowledgeMessage;
import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.Message;

/**
 * The ServiceAdapter class is the base definition of a service adapter.
 *
 * @author neville
 */
public abstract class ServiceAdapter extends ManageableComponent
{
    /** Log category for <code>ServiceAdapter</code>. */
    public static final String LOG_CATEGORY = Destination.LOG_CATEGORY;

    //--------------------------------------------------------------------------
    //
    // Constructor
    //
    //--------------------------------------------------------------------------

    /**
     * Constructs an unmanaged <code>ServiceAdapter</code> instance.
     */
    public ServiceAdapter()
    {
        this(false);
    }

    /**
     * Constructs a <code>ServiceAdapter</code> instance.
     *
     * @param enableManagement <code>true</code> if the <code>ServiceAdapter</code> has a
     * corresponding MBean control for management; otherwise <code>false</code>.
     */
    public ServiceAdapter(boolean enableManagement)
    {
        super(enableManagement);
    }

    //--------------------------------------------------------------------------
    //
    // Initialize, validate, start, and stop methods.
    //
    //--------------------------------------------------------------------------

    /**
     * Verifies that the <code>ServiceAdapter</code> is in valid state before
     * it is started. If subclasses override, they must call <code>super.validate()</code>.
     *
     */
    protected void validate()
    {
        if (isValid())
            return;

        super.validate();
    }

    /**
     * Starts the adapter if its associated <code>Destination</code> is started
     * and if the adapter is not already running. If subclasses override, they
     * must call <code>super.start()</code>.
     */
    public void start()
    {
        if (isStarted())
        {
            return;
        }

        // Check if the Destination is started
        Destination destination = getDestination();
        if (!destination.isStarted())
        {
            if (Log.isWarn())
            {
                Log.getLogger(getLogCategory()).warn("Adapter with id '{0}' cannot be started" +
                        " when its Destination with id '{1}' is not started.",
                        new Object[]{getId(), destination.getId()});
            }
            return;
        }

        // Set up management
        if (isManaged() && destination.isManaged())
        {
            setupAdapterControl(destination);
            DestinationControl controller = (DestinationControl)destination.getControl();
            if (getControl() != null)
                controller.setAdapter(getControl().getObjectName());

        }

        super.start();
    }

    /**
     * Stops the <code>ServiceAdapter</code>.
     * If subclasses override, they must call <code>super.start()</code>.
     *
     */
    public void stop()
    {
        if (!isStarted())
        {
            return;
        }

        super.stop();

        // Remove management
        if (isManaged() && getDestination().isManaged())
        {
            if (getControl() != null)
            {
                getControl().unregister();
                setControl(null);
            }
            setManaged(false);
        }

    }

    //--------------------------------------------------------------------------
    //
    // Public Getters and Setters for AbstractService properties
    //
    //--------------------------------------------------------------------------

    /**
     * Returns the <code>Destination</code> of the <code>ServiceAdapter</code>.
     *
     * @return The <code>Destination</code> of the <code>ServiceAdapter</code>.
     */
    public Destination getDestination()
    {
        return (Destination)getParent();
    }

    /**
     * Sets the <code>Destination</code> of the <code>ServiceAdapter</code>.
     * Also sets the <code>ServiceAdapter</code> of the <code>Destination</code>
     * if needed.
     *
     * @param destination The <code>Destination</code> of the <code>ServiceAdapter</code>.
     */
    public void setDestination(Destination destination)
    {
        Destination oldDestination = getDestination();

        setParent(destination);

        if (oldDestination != null)
            oldDestination.setAdapter(null);

        // Set destination's adapter if needed
        if (destination.getAdapter() != this)
        {
            destination.setAdapter(this);
        }
    }

    //--------------------------------------------------------------------------
    //
    // Other Public APIs
    //
    //--------------------------------------------------------------------------

    /**
     * Handle a data message intended for this adapter.  This method is responsible
     * for handling the message and returning a result (if any).  The return value
     * of this message is used as the body of the acknowledge message returned to
     * the client.  It may be null if there is no data being returned for this message.
     * <p>
     * Typically the data content for the message is stored in the body property
     * of the message.  The headers of the message are used to store fields which relate
     * to the transport of the message.  The type of operation is stored as the
     * operation property of the message.
     * </p>
     *
     * @param message the message as sent by the client intended for this adapter
     * @return the body of the acknowledge message (or null if there is no body)
     *
     * @see flex.messaging.messages.Message
     * @see flex.messaging.messages.AsyncMessage
     */
    public abstract Object invoke(Message message);


    /**
     * Accept a command from the adapter's service and perform some
     * internal action based upon it.  CommandMessages are used for messages
     * which control the state of the connection between the client and
     * the server.  For example, this lets the adapter perform processing with
     * subscribe, unsubscribe, and poll operations.  For subscribe and unsubscribe,
     * this method is only called if handlesSubscriptions returns true.
     * <p>
     * The service will perform some processing on the message before and after it
     * calls this method.  For subscribe messages the MessageService
     * will register a subscription after this method returns successfully.
     * For unsubscribe messages, the MessageService will unsubscribe after this
     * method returns successfully.  For both of these messages, this method
     * can return null or it can return the AcknowledgeMessage to send to the client
     * for the reply to this operation.  If a MultiTopicConsumer is used on the
     * client, this method will receive a MULTI_SUBSCRIBE message.
     * </p><p>
     * For POLL operations, this method can return a list of messages to be
     * added to the set returned to the client for this poll.  If it returns
     * null, it means no messages are to be added to the set already queued
     * up for this client.
     * </p>
     *
     * @see flex.messaging.messages.CommandMessage
     * @see flex.messaging.messages.AsyncMessage
     *
     * @param commandMessage The command message to manage.
     * @return The result of manage. The default implementation returns null.
     */
    public Object manage(CommandMessage commandMessage)
    {
        return null;
    }

    /**
     * Return an object, usually a Collection, representing any
     * shared state for the adapter. If adapters have shared state,
     * they should override this method, as the default implementation
     * throws an UnsupportedOperationException.
     *
     * @return The state of the adapter. The default implementations throws
     * <code>UnsupportedOperationException</code>.
     */
    public Object getAdapterState()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Set an object, usually a Collection, to represent shared
     * state for the adapter. If adapters have shared state,
     * they should override this method, as the default implementation
     * throws an UnsupportedOperationException.
     *
     * @param adapterState The object representing the adapter state.
     */
    public void setAdapterState(Object adapterState)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns <code>true</code> if the adapter performs custom subscription management.
     * The default return value is <code>false</code>, and subclasses should override this
     * method as necessary.
     *
     * @return <code>true</code> if the adapter performs custom subscription management.
     * The default return value is <code>false</code>.
     */
    public boolean handlesSubscriptions()
    {
        return false;
    }
    
    //--------------------------------------------------------------------------
    //
    // Protected/private APIs
    //
    //--------------------------------------------------------------------------

    /**
     * Returns the log category of the <code>ServiceAdapter</code>. Subclasses
     * can override to provide a more specific logging category.
     *
     * @return The log category.
     */
    protected String getLogCategory()
    {
        return LOG_CATEGORY;
    }

    /**
     * Managed subclasses should override this method to setup and
     * register their corresponding MBean control.
     *
     * @param destination The associated <code>Destination</code> for the adapter.
     */
    protected void setupAdapterControl(Destination destination)
    {
        setManaged(false);
    }

}
