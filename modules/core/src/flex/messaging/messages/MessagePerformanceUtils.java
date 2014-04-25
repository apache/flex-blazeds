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

import flex.messaging.FlexContext;
import flex.messaging.endpoints.AbstractEndpoint;
import flex.messaging.io.amf.ActionContext;
import flex.messaging.log.Log;
import flex.messaging.log.LogCategories;

/**
 * @exclude
 *
 * Utility class for populating  MessagePerformanceInformation objects at various stages of
 * server processing.  A given message may have three MPI headers populated (naming convention
 * of these headers is from the perspective of the server):
 *
 * DSMPII - incoming message, message sent from client to server
 * DSMPIO - outgoing message, response/acknowledgement message sent from server back to client
 * DSMPIP - only populated for a pushed message, this is information for the incoming message
 * that caused the pushed message
 */
public class MessagePerformanceUtils
{

    static final String LOG_CATEGORY = LogCategories.MESSAGE_GENERAL;

    public static final String MPI_HEADER_IN = "DSMPII";
    public static final String MPI_HEADER_OUT = "DSMPIO";
    public static final String MPI_HEADER_PUSH = "DSMPIP";

    public static int MPI_NONE = 0;
    public static int MPI_TIMING = 1;
    public static int MPI_TIMING_AND_SIZING = 2;

    /**
     * @exclude
     *
     * Clones the MPI object for the incoming message from client to server
     * from the batch wrapper to all messages included in it, keeping track of
     * overhead time spent doing this.
     *
     * @param message The message whose MPI should be propogated
     */
    public static void propogateMPIDownBatch(Message message)
    {
        long overhead = System.currentTimeMillis();
        if(message instanceof BatchableMessage)
        {
            BatchableMessage dm = (BatchableMessage)message;
            if(dm.isBatched())
            {
                 Object[] batchedMessages = (Object[])message.getBody();
                 int batchedLength = batchedMessages.length;
                 for(int a=0;a<batchedLength;a++)
                 {
                     Message currentMess = (Message)batchedMessages[a];
                     MessagePerformanceInfo mpi = MessagePerformanceUtils.getMPII(message);
                     MessagePerformanceUtils.setMPII(currentMess, (MessagePerformanceInfo)mpi.clone());
                     propogateMPIDownBatch(currentMess);
                 }
            }
        }
        overhead = System.currentTimeMillis() - overhead;
        MessagePerformanceUtils.getMPII(message).addToOverhead(overhead);
   }

    /**
     * @exclude
     *
     * This method finalizes the incoming MPI instance.  It is necessary because the client
     * send time is stored on the incoming MPII and the server receive time as well as message
     * size are populated in the MPI object stored on the ActionContext, this method combines
     * the information into one MPI instance.
     *
     * @param context - The action context used to deserialize the incoming message, it will have the
     * server receive time and message size information
     * @param inMessage - The incoming message, its MPI will have the client send time
     */
    public static void setupMPII(ActionContext context, Message inMessage)
    {
        try
        {
            // the MPI from the incoming message will have the client-send timestamp
            MessagePerformanceInfo mpii= MessagePerformanceUtils.getMPII(inMessage);

            // this is the MPI that we want to propogate
            MessagePerformanceInfo contextMPI = context.getMPII();
            if(contextMPI!=null && mpii!=null){
                contextMPI.sendTime = mpii.sendTime;
                MessagePerformanceUtils.setMPII(inMessage, (MessagePerformanceInfo)contextMPI.clone());
                propogateMPIDownBatch(inMessage);
            }
        }
        catch(Exception e)
        {
            if (Log.isDebug())
                Log.getLogger(LOG_CATEGORY).error("MPI error: setting up response MPI : " +
                     e.toString());
        }
    }

    /**
     * @exclude
     *
     * This method sets up the outgoing MPI object for a response/acknowledgement message.
     * The outgoing message should also have a copy of the incoming MPI so that when the client
     * receives the response it has access to all information
     *
     * @param context - The context used to deserialize the incoming message
     * @param inMessage - The incoming message
     * @param outMessage - The response message
     */
    public static void updateOutgoingMPI(ActionContext context, Message inMessage, Object outMessage)
    {
        try
        {
            MessagePerformanceInfo mpio=null;
            if(context != null)
            {
                mpio = context.getMPIO();
                if(mpio == null)
                {
                    mpio = new MessagePerformanceInfo();
                    if(MessagePerformanceUtils.getMPII(inMessage)!=null && MessagePerformanceUtils.getMPII(inMessage).sendTime!=0)
                        mpio.infoType="OUT";
                }
                Message mess = (Message)outMessage;
                if(MessagePerformanceUtils.getMPII(inMessage)!=null)
                    MessagePerformanceUtils.setMPII(mess, (MessagePerformanceInfo)MessagePerformanceUtils.getMPII(inMessage).clone());
                MessagePerformanceUtils.setMPIO(mess, mpio);
                context.setMPIO(mpio);
            }

            if(outMessage instanceof CommandMessage &&
                    ((CommandMessage)outMessage).getOperation() == CommandMessage.CLIENT_SYNC_OPERATION)
            {
                // pushed message case
                CommandMessage cmd = (CommandMessage)outMessage;
                Object[] cmdBody = (Object[])cmd.getBody();
                // in the pushed message case we want the outgoing metrics of the message to be updated
                // however, we want the incoming metrics to be that of the poll message and
                // the incoming push metrics to be that of the original message
                int batchedLength = cmdBody.length;
                for(int i = 0; i < batchedLength; i++)
                {
                    Message currentMess = (Message)cmdBody[i];
                    MessagePerformanceInfo origMPII =  MessagePerformanceUtils.getMPII(currentMess);

                    if (origMPII == null || MessagePerformanceUtils.getMPII(inMessage) == null)
                    {
                        // this can happen if the server has MPI enabled but the producing client does not
                        // log a warning for this and break out of here as MPI requires all participating
                        // parties to have the same settings
                        if (Log.isError())
                        {
                            Log.getLogger(LOG_CATEGORY).error
                            (
                                    "MPI is enabled but could not get message performance information " +
                                            "for incoming MPI instance from client message.  The client "+
                                            "might have created a new channel not configured to send message" +
                                            " performance after declaring a different destination which does." +
                                            "  The client channel should either be configured to add MPI " +
                                            "properties, or a server destion which has the same MPI " +
                                            "properties as the client channel should be used.");
                        }
                        return;
                    }

                    MessagePerformanceUtils.setMPIP(currentMess, (MessagePerformanceInfo)origMPII.clone());
                    MessagePerformanceInfo newMPII = (MessagePerformanceInfo)MessagePerformanceUtils.getMPII(inMessage).clone();
                    mpio.pushedFlag=true;
                    MessagePerformanceUtils.setMPII(currentMess, newMPII);
                    MessagePerformanceUtils.setMPIO(currentMess, mpio);
                }
            }
        }
        catch(Exception e)
        {
            if (Log.isDebug())
                Log.getLogger(LOG_CATEGORY).error("MPI error: setting up response MPI : " +
                     e.toString());
        }
    }

    /**
     * @exclude
     *
     * Convenience method for setting the incoming MPI object on a message.
     *
     * @param message The message whose MPI header will be set
     * @param mpi The incoming MPI instance
     */
    public static void setMPII(Message message, MessagePerformanceInfo mpi)
    {
        message.setHeader(MPI_HEADER_IN, mpi);
    }

    /**
     * @exclude
     * The server generated messages do not have MPII object (which is normally set 
     * by the client) which prevents the usual performance gathering. This convenience method
     * can be invoked to set the MPII object (as if it's generated by the client) once the 
     * message is generated on the server but before it is routed to the MessageBroker 
     * to be delivered to the client. This way performance gathering can proceed as expected.
     * 
     * @param message The server generated message.
     */
    public static void setMPIIForServerGeneratedMessage(Message message)
    {
        MessagePerformanceInfo mpii = new MessagePerformanceInfo();
        AbstractEndpoint endpoint = (AbstractEndpoint)FlexContext.getEndpoint();
        mpii.recordMessageSizes = endpoint != null? endpoint.isRecordMessageSizes() : false;
        mpii.recordMessageTimes = endpoint != null? endpoint.isRecordMessageTimes() : false;
        mpii.sendTime = System.currentTimeMillis();
        MessagePerformanceUtils.setMPII(message, mpii);
    }

    /**
     * @exclude
     *
     * Convenience method for setting the outgoing MPI object on a message.
     *
     * @param message The message whose MPI header will be set
     * @param mpi The outgoing MPI instance
     */
    public static void setMPIO(Message message, MessagePerformanceInfo mpi)
    {
        message.setHeader(MPI_HEADER_OUT, mpi);
    }

    /**
     * @exclude
     *
     * Convenience method for setting the pushed MPI object on a message.
     *
     * @param message The message whose MPI header will be set
     * @param mpi The pushed MPI instance (this is the incoming MPI instance
     * of the message that caused the push)
     */
    public static void setMPIP(Message message, MessagePerformanceInfo mpi)
    {
        message.setHeader(MPI_HEADER_PUSH, mpi);
    }

    /**
     * @exclude
     *
     * Convenience method for retrieving the incoming MPI object from a message.
     *
     * @param message The message whose MPI header will be retrieved
     * @return mpi Incoming MPI instance
     */
    public static MessagePerformanceInfo getMPII(Message message)
    {
        return (MessagePerformanceInfo)message.getHeader(MPI_HEADER_IN);
    }

    /**
     * @exclude
     *
     * Convenience method for retrieving the outgoing MPI object from a message.
     *
     * @param message The message whose MPI header will be retrieved
     * @return mpi Outgoing MPI instance
     */
    public static MessagePerformanceInfo getMPIO(Message message)
    {
        return (MessagePerformanceInfo)message.getHeader(MPI_HEADER_OUT);
    }

    /**
     * @exclude
     *
     * Convenience method for retrieving the pushed MPI object from a message.
     *
     * @param message The message whose MPI header will be retrieved
     * @return mpi Pushed MPI instance (this is the incoming MPI instance
     * of the message that caused the push)
     */
    public static MessagePerformanceInfo getMPIP(Message message)
    {
        return (MessagePerformanceInfo)message.getHeader(MPI_HEADER_PUSH);
    }

    /**
     * @exclude
     *
     * Convenience method for setting server pre-push processing time on a message.
     * No-op if record-message-times is false
     *
     * @param message The message whose MPI header will be updated
     */
    public static void markServerPrePushTime(Message message)
    {
        // If the message does not have an MPI header then we are not recording message times
        // and we have nothing to do here
        if (getMPII(message) == null || getMPII(message).sendTime == 0)
            return;

        MessagePerformanceInfo mpi = getMPII(message);
        mpi.serverPrePushTime = System.currentTimeMillis();
    }

    /**
     * @exclude
     *
     * Convenience method for setting server pre-adapter timestamp on a message.
     * No-op if record-message-times is false
     *
     * @param message The message whose MPI header will be updated
     */
    public static void markServerPreAdapterTime(Message message)
    {
        // If the message does not have an MPI header then we are not recording message times
        // and we have nothing to do here
        if (getMPII(message) == null || getMPII(message).sendTime == 0)
            return;

        MessagePerformanceInfo mpi = getMPII(message);

        // it is possible that a batched message will have this called multiple times,
        // do not reset stamp once it has been set
        if (mpi.serverPreAdapterTime != 0)
            return;

        mpi.serverPreAdapterTime = System.currentTimeMillis();
    }

    /**
     * @exclude
     *
     * Convenience method for setting server post-adapter timestamp on a message.
     * No-op if record-message-times is false
     *
     * @param message The message whose MPI header will be updated
     */
    public static void markServerPostAdapterTime(Message message)
    {
        // If the message does not have an MPI header then we are not recording message times
        // and we have nothing to do here
        if (getMPII(message) == null || getMPII(message).sendTime == 0 || getMPII(message).serverPostAdapterTime != 0)
            return;

        MessagePerformanceInfo mpi = getMPII(message);
        mpi.serverPostAdapterTime = System.currentTimeMillis();
    }

    /**
     *
     * Method may be called from a custom adapter to mark the beginning of processing that occurs
     * outside of the adapter for a particular message.  Use this method in conjunction with
     * <code>markServerPostAdapterExternalTime</code> to mark the amount of time spent when your
     * adapter must make a call to an external component.  If <code>record-message-times</code> is
     * <code>true</code> for the communication channel, the server processing time external to the
     * adapter may be retrieved via MessagePerformanceUtils.serverAdapterExternalTime on the client
     * once it receives the message.
     *
     * If <code>record-message-times</code> is <code>false</code> for the communication channel,
     * calling this method will have no effect.
     *
     * @param message The message being processed
     */
    public static void markServerPreAdapterExternalTime(Message message)
    {
        // If the message does not have an MPI header then we are not recording message times
        // and we have nothing to do here
        if (getMPII(message) == null || getMPII(message).sendTime == 0)
            return;

        MessagePerformanceInfo mpi = getMPII(message);
        mpi.serverPreAdapterExternalTime = System.currentTimeMillis();
    }

    /**
     *
     * Method may be called from a custom adapter to mark the end of processing that occurs
     * outside of the adapter for a particular message.  Use this method in conjunction with
     * <code>markServerPreAdapterExternalTime</code> to mark the amount of time spent when your
     * adapter must make a call to an external component.  If <code>record-message-times</code> is
     * <code>true</code> for the communication channel, the server processing time external to the
     * adapter may be retrieved via MessagePerformanceUtils.serverAdapterExternalTime on the client
     * once it receives the message.
     *
     * If <code>record-message-times</code> is <code>false</code> for the communication channel,
     * calling this method will have no effect.
     *
     * @param message The message being processed
     */
    public static void markServerPostAdapterExternalTime(Message message)
    {
        // If the message does not have an MPI header then we are not recording message times
        // and we have nothing to do here
        if (getMPII(message) == null || getMPII(message).sendTime == 0 || getMPII(message).serverPostAdapterExternalTime != 0)
            return;

        MessagePerformanceInfo mpi = getMPII(message);
        mpi.serverPostAdapterExternalTime = System.currentTimeMillis();
    }

}
