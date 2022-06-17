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

import flex.messaging.io.amf.ActionContext;
import flex.messaging.io.amf.MessageBody;
import flex.messaging.io.MessageIOConstants;
import flex.messaging.io.RecoverableSerializationException;

/**
 * Filter that breaks down the batched message buffer into individual invocations.
 */
public class BatchProcessFilter extends AMFFilter {
    public BatchProcessFilter() {
    }

    public void invoke(final ActionContext context) {
        // Process each action in the body
        int bodyCount = context.getRequestMessage().getBodyCount();

        // Report batch size in Debug mode
        //gateway.getLogger().logDebug("Processing batch of " + bodyCount + " request(s)");

        for (context.setMessageNumber(0); context.getMessageNumber() < bodyCount; context.incrementMessageNumber()) {
            try {
                // create the response body
                MessageBody responseBody = new MessageBody();
                responseBody.setTargetURI(context.getRequestMessageBody().getResponseURI());

                // append the response body to the output message
                context.getResponseMessage().addBody(responseBody);

                //Check that deserialized message body data type was valid. If not, skip this message.
                Object o = context.getRequestMessageBody().getData();

                if (o != null && o instanceof RecoverableSerializationException) {
                    context.getResponseMessageBody().setData(((RecoverableSerializationException) o).createErrorMessage());
                    context.getResponseMessageBody().setReplyMethod(MessageIOConstants.STATUS_METHOD);
                    continue;
                }

                // invoke next filter in the chain
                next.invoke(context);
            } catch (Exception e) {
                // continue invoking on next message body despite error
            }
        }
    }
}
