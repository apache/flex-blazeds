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
package flex.messaging.endpoints.amf;

import flex.messaging.FlexContext;
import flex.messaging.FlexSession;
import flex.messaging.MessageException;
import flex.messaging.endpoints.AbstractEndpoint;
import flex.messaging.io.MessageIOConstants;
import flex.messaging.io.amf.ActionContext;
import flex.messaging.io.amf.MessageBody;
import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.ErrorMessage;
import flex.messaging.messages.Message;
import flex.messaging.messages.MessagePerformanceUtils;
import flex.messaging.services.MessageService;
import flex.messaging.util.StringUtils;
import flex.messaging.log.LogCategories;
import flex.messaging.log.Log;
import flex.messaging.util.ExceptionUtil;
import flex.messaging.util.UUIDUtils;

import java.util.List;
import java.lang.reflect.Array;

/**
 * A simple bridge between the encoding/decoding functionality of
 * the AMF endpoint and the MessageBroker: this last filter in the
 * chain returns the message to the MessageBroker, which will then
 * locate the correct service to handle the message.
 */
public class MessageBrokerFilter extends AMFFilter
{
    private static final int UNHANDLED_ERROR = 10000;
    static final String LOG_CATEGORY = LogCategories.MESSAGE_GENERAL;

    protected AbstractEndpoint endpoint;

    public MessageBrokerFilter(AbstractEndpoint endpoint)
    {
        this.endpoint = endpoint;
    }

    public void invoke(final ActionContext context)
    {
        MessageBody request = context.getRequestMessageBody();
        MessageBody response = context.getResponseMessageBody();

        Message inMessage = request.getDataAsMessage();

        Object outMessage = null;

        String replyMethodName = MessageIOConstants.STATUS_METHOD;

        try
        {
            // Lookup or create the correct FlexClient.
            endpoint.setupFlexClient(inMessage);

            // Assign a clientId if necessary.
            // We don't need to assign clientIds to general poll requests.
            if (inMessage.getClientId() == null &&
                (!(inMessage instanceof CommandMessage) || ((CommandMessage)inMessage).getOperation() != CommandMessage.POLL_OPERATION))
            {
                Object clientId = UUIDUtils.createUUID();
                inMessage.setClientId(clientId);
            }

            // Messages received via the AMF channel can be batched (by NetConnection on the client) and
            // we must not put the handler thread into a poll-wait state if a poll command message is followed by
            // or preceeded by other messages in the batch; the request-response loop must complete without waiting.
            // If the poll command is the only message in the batch it's ok to wait.
            // If it isn't ok to wait, tag the poll message with a header that short-circuits any potential poll-wait.
            if (inMessage instanceof CommandMessage)
            {
                CommandMessage command = (CommandMessage)inMessage;
                if ((command.getOperation() == CommandMessage.POLL_OPERATION) && (context.getRequestMessage().getBodyCount() != 1))
                    command.setHeader(CommandMessage.SUPPRESS_POLL_WAIT_HEADER, Boolean.TRUE);
            }

            // If MPI is enabled update the MPI metrics on the object referred to by the context
            // and the messages
            if (context.isMPIenabled())
                MessagePerformanceUtils.setupMPII(context, inMessage);

            // Service the message.
            outMessage = endpoint.serviceMessage(inMessage);

            // if processing of the message resulted in an error, set up context and reply method accordingly
            if (outMessage instanceof ErrorMessage)
            {
                context.setStatus(MessageIOConstants.STATUS_ERR);
                replyMethodName = MessageIOConstants.STATUS_METHOD;
            }
            else
            {
                replyMethodName = MessageIOConstants.RESULT_METHOD;
            }
        }
        catch (MessageException e)
        {
            context.setStatus(MessageIOConstants.STATUS_ERR);
            replyMethodName = MessageIOConstants.STATUS_METHOD;

            outMessage = e.createErrorMessage();
            ((ErrorMessage)outMessage).setCorrelationId(inMessage.getMessageId());
            ((ErrorMessage)outMessage).setDestination(inMessage.getDestination());
            ((ErrorMessage)outMessage).setClientId(inMessage.getClientId());

            e.logAtHingePoint(inMessage, (ErrorMessage)outMessage, null /* Use default message intros */);
        }
        catch (Throwable t)
        {
            // Handle any uncaught failures. The normal exception path on the server
            // is to throw MessageExceptions which are handled in the catch block above,
            // so if that was skipped we have an overlooked or serious problem.
            context.setStatus(MessageIOConstants.STATUS_ERR);
            replyMethodName = MessageIOConstants.STATUS_METHOD;

            String lmeMessage = t.getMessage();
            if (lmeMessage == null)
                lmeMessage = t.getClass().getName();

            MessageException lme = new MessageException();
            lme.setMessage(UNHANDLED_ERROR, new Object[] {lmeMessage});

            outMessage = lme.createErrorMessage();
            ((ErrorMessage)outMessage).setCorrelationId(inMessage.getMessageId());
            ((ErrorMessage)outMessage).setDestination(inMessage.getDestination());
            ((ErrorMessage)outMessage).setClientId(inMessage.getClientId());

            if (Log.isError())
            {
                Log.getLogger(LOG_CATEGORY).error("Unhandled error when processing a message: " +
                        t.toString() + StringUtils.NEWLINE +
                        "  incomingMessage: " + inMessage + StringUtils.NEWLINE +
                        "  errorReply: " + outMessage + StringUtils.NEWLINE +
                        ExceptionUtil.exceptionFollowedByRootCausesToString(t) + StringUtils.NEWLINE);
            }
        }
        finally
        {
            // If MPI is enabled update the MPI metrics on the object referred to by the context
            // and the messages
            if (context.isRecordMessageSizes() || context.isRecordMessageTimes())
            {
                MessagePerformanceUtils.updateOutgoingMPI(context, inMessage, outMessage);
            }

            // If our channel-endpoint combination supports small messages, and
            // if we know the current protocol version supports small messages,
            // try to replace the message...
            FlexSession session = FlexContext.getFlexSession();
            if (session != null && session.useSmallMessages()
                    && !context.isLegacy()
                    && context.getVersion() >= MessageIOConstants.AMF3
                    && outMessage instanceof Message)
            {
                outMessage = endpoint.convertToSmallMessage((Message)outMessage);
            }

            response.setReplyMethod(replyMethodName);
            response.setData(outMessage);
        }
    }
}
