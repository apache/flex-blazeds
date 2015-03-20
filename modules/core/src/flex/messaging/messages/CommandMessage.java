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
package flex.messaging.messages;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import flex.messaging.log.LogCategories;
import flex.messaging.util.UUIDUtils;

/**
 * A message that represents an infrastructure command passed between
 * client and server. Subscribe/unsubscribe operations result in
 * CommandMessage transmissions, as do polling operations.
 */
public class CommandMessage extends AsyncMessage
{
    /** Log category for <code>CommandMessage</code>.*/
    public static final String LOG_CATEGORY = LogCategories.MESSAGE_COMMAND;

    // THESE VALUES MUST BE THE SAME ON CLIENT AND SERVER
    /**
     *  This operation is used to subscribe to a remote destination.
     */
    public static final int SUBSCRIBE_OPERATION = 0;

    /**
     *  This operation is used to unsubscribe from a remote destination.
     */
    public static final int UNSUBSCRIBE_OPERATION = 1;

    /**
     *  This operation is used to poll a remote destination for pending,
     *  undelivered messages.
     */
    public static final int POLL_OPERATION = 2;

    /**
     *  This operation is used by a remote destination to sync missed or cached messages
     *  back to a client as a result of a client issued poll command.
     */
    public static final int CLIENT_SYNC_OPERATION = 4;

    /**
     *  This operation is used to test connectivity over the current channel to
     *  the remote endpoint.
     */
    public static final int CLIENT_PING_OPERATION = 5;

    /**
     *  This operation is used to request a list of failover endpoint URIs
     *  for the remote destination based on cluster membership.
     */
    public static final int CLUSTER_REQUEST_OPERATION = 7;

    /**
     * This operation is used to send credentials to the endpoint so that
     * the user can be logged in over the current channel.
     * The credentials need to be Base64 encoded and stored in the <code>body</code>
     * of the message.
     */
    public static final int LOGIN_OPERATION = 8;

    /**
     * This operation is used to log the user out of the current channel, and
     * will invalidate the server session if the channel is HTTP based.
     */
    public static final int LOGOUT_OPERATION = 9;

    /**
     * This operation is used to indicate that the client's subscription to a
     * remote destination has been invalidated.
     */
    public static final int SUBSCRIPTION_INVALIDATE_OPERATION = 10;

    /**
     * This operation is used by the MultiTopicConsumer to subscribe/unsubscribe
     * from multiple subtopics/selectors in the same message.
     */
    public static final int MULTI_SUBSCRIBE_OPERATION = 11;

    /**
     * This operation is used to indicate that a channel has disconnected.
     */
    public static final int DISCONNECT_OPERATION = 12;
    
    /**
     *  This operation is used to trigger a client connect attempt.
     */
    public static final int TRIGGER_CONNECT_OPERATION = 13;     

    /**
     *  This is the default operation for new CommandMessage instances.
     */
    public static final int UNKNOWN_OPERATION = 10000;

    /**
     * Endpoints can imply what features they support by reporting the
     * latest version of messaging they are capable of during the handshake of
     * the initial ping CommandMessage.
     */
    public static final String MESSAGING_VERSION = "DSMessagingVersion";

    /**
     * The name for the selector header in subscribe messages.
     */
    public static final String SELECTOR_HEADER = "DSSelector";

    /**
     * The name for the header used internaly on the server to indicate that an unsubscribe
     * message is due to a client subscription being invalidated.
     */
    public static final String SUBSCRIPTION_INVALIDATED_HEADER = "DSSubscriptionInvalidated";

    /**
     *  Durable JMS subscriptions are preserved when an unsubscribe message
     *  has this parameter set to true in its header.
     */
    public static final String PRESERVE_DURABLE_HEADER= "DSPreserveDurable";

    /**
     * Header to indicate that the Channel needs the configuration from the
     * server.
     */
    public static final String NEEDS_CONFIG_HEADER = "DSNeedsConfig";

    /**
     * Header used in a MULTI_SUBSCRIBE message to specify an Array of subtopic/selector
     * pairs to add to the existing set of subscriptions.
     */
    public static final String ADD_SUBSCRIPTIONS = "DSAddSub";

    /**
     * Like the above, but specifies the subtopic/selector array of to remove.
     */
    public static final String REMOVE_SUBSCRIPTIONS = "DSRemSub";

    /**
     * The separator used in the add and remove subscription headers for
     * multi subscribe messages.
     */
    public static final String SUBTOPIC_SEPARATOR = "_;_";

    /**
     * Header to drive an idle wait time before the next client poll request.
     */
    public static final String POLL_WAIT_HEADER = "DSPollWait";

    /**
     * Header to suppress poll response processing. If a client has a long-poll
     * parked on the server and issues another poll, the response to this subsequent poll
     * should be tagged with this header in which case the response is treated as a
     * no-op and the next poll will not be scheduled. Without this, a subsequent poll
     * will put the channel and endpoint into a busy polling cycle.
     */
    public static final String NO_OP_POLL_HEADER = "DSNoOpPoll";

    /**
     * @exclude
     * Internal header used to tag poll messages when a poll-wait must be suppressed.
     */
    public static final String SUPPRESS_POLL_WAIT_HEADER = "DSSuppressPollWait";

    /**
     * Header to specify which character set encoding was used while encoding
     * login credentials.
     */
    public static final String CREDENTIALS_CHARSET_HEADER = "DSCredentialsCharset";

    /**
     * Header to indicate the maximum number of messages a Consumer wants to 
     * receive per second.
     */
    public static final String MAX_FREQUENCY_HEADER = "DSMaxFrequency";
    
    /**
     * Header that indicates the message is a heartbeat.
     */
    public static final String HEARTBEAT_HEADER = "DS<3";

    /**
     * Header that indicates the client application has successfully registered for push notifications.
     */
    public static final String PUSH_NOTIFICATION_REGISTERED_HEADER = "DSApplicationRegisteredForPush";
    
    /**
     * Header that provides additional meta-information when the client has registered for push notifications.
     */
    public static final String PUSH_REGISTRATION_INFORMATION = "DSPushRegisteredInformation";

    /**
     * @exclude
     * The position of the operation flag within all flags.
     * Constant used during serialization.
     */
    private static byte OPERATION_FLAG = 1;

    /**
     * This number was generated using the 'serialver' command line tool.
     * This number should remain consistent with the version used by
     * ColdFusion to communicate with the message broker over RMI.
     */
    private static final long serialVersionUID = -4026438615587526303L;

    /**
     * The operation names that map to each of the operation constants above.
     * The constants in this list should remain parallel to the above constants
     */
    static final String [] operationNames  = {
        "subscribe", "unsubscribe", "poll", "unused3", "client_sync", "client_ping",
        "unused6", "cluster_request", "login", "logout", "subscription_invalidate",
        "multi_subscribe", "disconnect", "trigger_connect", "state_change"
    };

    /**
     * The operation to execute for messages of this type.
     */
    private int operation = UNKNOWN_OPERATION;

    /**
     * Constructs a <code>CommandMessage</code> instance.
     * The message id is set to a universally unique value, and the
     * timestamp for the message is set to the current system timestamp.
     * The operation is set to a default value of <code>UNKNOWN_OPERATION</code>.
     */
    public CommandMessage()
    {
        this.messageId = UUIDUtils.createUUID();
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Constructs a <code>CommandMessage</code> instance.
     * The message id is set to a universally unique value, and the
     * timestamp for the message is set to the current system timestamp.
     *
     * @param operation The operation for the <code>CommandMessage</code>; one of the operation constants.
     */
    public CommandMessage(int operation)
    {
        this();
        this.operation = operation;
    }

    /**
     * Returns the operation for this <code>CommandMessage</code>.
     *
     * @return The operation for this <code>CommandMessage</code>.
     */
    public int getOperation()
    {
        return operation;
    }

    /**
     * Sets the operation for this <code>CommandMessage</code>.
     *
     * @param operation The operation for this <code>CommandMessage</code>.
     */
    public void setOperation(int operation)
    {
        this.operation = operation;
    }

    /**
     * @exclude
     */
    public Message getSmallMessage()
    {
        // We shouldn't use small messages for PING or LOGIN operations as the
        // messaging version handshake would not yet be complete... for now just
        // optimize POLL operations.
        if (operation == POLL_OPERATION)
        {
            return new CommandMessageExt(this);
        }

        return null;
    }

    /**
     * @exclude
     * Debugging function which returns the name of the operation for
     * a given operation code.
     */
    public static String operationToString(int operation)
    {
        if (operation < 0 || operation >= operationNames.length)
            return "invalid." + operation;
        return operationNames[operation];
    }

    /**
     * @exclude
     */
    public void readExternal(ObjectInput input)throws IOException, ClassNotFoundException
    {
        super.readExternal(input);

        short[] flagsArray = readFlags(input);
        for (int i = 0; i < flagsArray.length; i++)
        {
            short flags = flagsArray[i];
            short reservedPosition = 0;

            if (i == 0)
            {
                if ((flags & OPERATION_FLAG) != 0)
                    operation = ((Number)input.readObject()).intValue();

                reservedPosition = 1;
            }

            // For forwards compatibility, read in any other flagged objects
            // to preserve the integrity of the input stream...
            if ((flags >> reservedPosition) != 0)
            {
                for (short j = reservedPosition; j < 6; j++)
                {
                    if (((flags >> j) & 1) != 0)
                    {
                        input.readObject();
                    }
                }
            }
        }
    }

    /**
     * @exclude
     * Utility method to pretty print a <code>CommandMessage</code>.
     *
     * @param indentLevel This method may be invoked recursively so this argument
     *        allows nested messages to print relative to the current print stack.
     */
    protected String toStringFields(int indentLevel)
    {
        String sep = getFieldSeparator(indentLevel);
        String s = sep + "operation = " + operationToString(operation);
        if (operation == SUBSCRIBE_OPERATION)
            s += sep + "selector = " + getHeader(SELECTOR_HEADER);
        if (operation != LOGIN_OPERATION)
        {
            s += super.toStringFields(indentLevel);
        }
        else
        {
            s += sep + "clientId =  " + clientId;
            s += sep + "destination =  " + destination;
            s += sep + "messageId =  " + messageId;
            s += sep + "timestamp =  " + timestamp;
            s += sep + "timeToLive =  " + timeToLive;
            s += sep + "***not printing credentials***";
        }
        return s;
    }

    /**
     * @exclude
     */
    public void writeExternal(ObjectOutput output) throws IOException
    {
        super.writeExternal(output);

        short flags = 0;

        if (operation != 0)
            flags |= OPERATION_FLAG;

        output.writeByte(flags);

        if (operation != 0)
            output.writeObject(new Integer(operation));
    }

    /**
     * @exclude
     * Utility method to build the log category to use for logging <code>CommandMessage</code>s.
     */
    public String logCategory()
    {
        return LOG_CATEGORY + "." + operationToString(operation);
    }
}
