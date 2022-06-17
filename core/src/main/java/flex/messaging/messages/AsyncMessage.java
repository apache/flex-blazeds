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

import flex.messaging.util.UUIDUtils;
import flex.messaging.log.Log;

/**
 * This type of message contains information necessary to perform
 * point-to-point or publish-subscribe messaging.
 */
public class AsyncMessage extends AbstractMessage implements SmallMessage {
    /**
     * This number was generated using the 'serialver' command line tool.
     * This number should remain consistent with the version used by
     * ColdFusion to communicate with the message broker over RMI.
     */
    private static final long serialVersionUID = -3549535089417916783L;

    /**
     * The name for the subtopic header if the message targets a destination
     * subtopic.
     */
    public static final String SUBTOPIC_HEADER_NAME = "DSSubtopic";

    // Serialization constants
    private static byte CORRELATION_ID_FLAG = 1;
    private static byte CORRELATION_ID_BYTES_FLAG = 2;

    protected String correlationId;
    protected byte[] correlationIdBytes;

    /**
     * Default constructor for <code>AsyncMessage</code>.
     */
    public AsyncMessage() {
        // No-op.
    }

    /**
     * Gets the correlationId of the <code>AsyncMessage</code>.
     *
     * @return The correlation id.
     */
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * Sets the correlationId of the <code>AsyncMessage</code>.
     *
     * @param correlationId The correlationId for the message.
     */
    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    /**
     *
     */
    public Message getSmallMessage() {
        return getClass() == AsyncMessage.class ? new AsyncMessageExt(this) : null;
    }

    /**
     *
     */
    @Override
    public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
        super.readExternal(input);

        short[] flagsArray = readFlags(input);
        for (int i = 0; i < flagsArray.length; i++) {
            short flags = flagsArray[i];
            short reservedPosition = 0;

            if (i == 0) {
                if ((flags & CORRELATION_ID_FLAG) != 0)
                    correlationId = (String) input.readObject();

                if ((flags & CORRELATION_ID_BYTES_FLAG) != 0) {
                    correlationIdBytes = (byte[]) input.readObject();
                    correlationId = UUIDUtils.fromByteArray(correlationIdBytes);
                }

                reservedPosition = 2;
            }

            // For forwards compatibility, read in any other flagged objects
            // to preserve the integrity of the input stream...
            if ((flags >> reservedPosition) != 0) {
                for (short j = reservedPosition; j < 6; j++) {
                    if (((flags >> j) & 1) != 0)
                        input.readObject();
                }
            }
        }
    }

    /**
     *
     */
    @Override
    public void writeExternal(ObjectOutput output) throws IOException {
        super.writeExternal(output);

        if (correlationIdBytes == null && correlationId != null)
            correlationIdBytes = UUIDUtils.toByteArray(correlationId);

        short flags = 0;

        if (correlationId != null && correlationIdBytes == null)
            flags |= CORRELATION_ID_FLAG;

        if (correlationIdBytes != null)
            flags |= CORRELATION_ID_BYTES_FLAG;

        output.writeByte(flags);

        if (correlationId != null && correlationIdBytes == null)
            output.writeObject(correlationId);

        if (correlationIdBytes != null)
            output.writeObject(correlationIdBytes);
    }

    @Override
    protected String toStringFields(int indentLevel) {
        String sep = getFieldSeparator(indentLevel);
        String s = sep + "clientId = " + (Log.isExcludedProperty("clientId") ? Log.VALUE_SUPRESSED : clientId);
        s += sep + "correlationId = " + (Log.isExcludedProperty("correlationId") ? Log.VALUE_SUPRESSED : correlationId);
        s += sep + "destination = " + (Log.isExcludedProperty("destination") ? Log.VALUE_SUPRESSED : destination);
        s += sep + "messageId = " + (Log.isExcludedProperty("messageId") ? Log.VALUE_SUPRESSED : messageId);
        s += sep + "timestamp = " + (Log.isExcludedProperty("timestamp") ? Log.VALUE_SUPRESSED : String.valueOf(timestamp));
        s += sep + "timeToLive = " + (Log.isExcludedProperty("timeToLive") ? Log.VALUE_SUPRESSED : String.valueOf(timeToLive));
        s += sep + "body = " + (Log.isExcludedProperty("body") ? Log.VALUE_SUPRESSED : bodyToString(body, indentLevel));
        s += super.toStringFields(indentLevel);
        return s;
    }

}
