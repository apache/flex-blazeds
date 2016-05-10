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
package flex.messaging.io.amf;

import flex.messaging.io.MessageIOConstants;
import flex.messaging.messages.MessagePerformanceInfo;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

/**
 * A context for reading and writing messages.
 *
 */
public class ActionContext implements Serializable
{
    static final long serialVersionUID = 2300156738426801921L;
    private int messageNumber;
    private ActionMessage requestMessage;
    private ActionMessage responseMessage;
    private ByteArrayOutputStream outBuffer;

    private int status;
    private int version;

    private boolean legacy;
    public boolean isPush;
    public boolean isDebug;

    /**
     *
     * Performance metrics related field, keeps track of bytes deserialized using this context
     */
    private int deserializedBytes;

    /**
     *
     * Performance metrics related field, keeps track of bytes serialized using this context
     */
    private int serializedBytes;

    /**
     *
     * Performance metrics related field, recordMessageSizes flag
     */
    private boolean recordMessageSizes;

    /**
     *
     * Performance metrics related field, recordMessageTimes flag
     */
    private boolean recordMessageTimes;

    /**
     *
     * Performance metrics related field, incoming MPI object, will only be populated when one of
     * the record-message-* params is enabled
     */
    private MessagePerformanceInfo mpii;

    /**
     *
     * Performance metrics related field, outgoing MPI object, will only be populated when one of
     * the record-message-* params is enabled
     */
    private MessagePerformanceInfo mpio;

    public ActionContext()
    {
        status = MessageIOConstants.STATUS_OK;
    }

    public boolean isLegacy()
    {
        return legacy;
    }

    public void setLegacy(boolean legacy)
    {
        this.legacy = legacy;
    }

    public int getMessageNumber()
    {
        return messageNumber;
    }

    public void setMessageNumber(int messageNumber)
    {
        this.messageNumber = messageNumber;
    }

    public MessageBody getRequestMessageBody()
    {
        return requestMessage.getBody(messageNumber);
    }

    public ActionMessage getRequestMessage()
    {
        return requestMessage;
    }

    public void setRequestMessage(ActionMessage requestMessage)
    {
        this.requestMessage = requestMessage;
    }

    public ActionMessage getResponseMessage()
    {
        return responseMessage;
    }

    public MessageBody getResponseMessageBody()
    {
        return responseMessage.getBody(messageNumber);
    }

    public void setResponseMessage(ActionMessage responseMessage)
    {
        this.responseMessage = responseMessage;
    }

    public void setResponseOutput(ByteArrayOutputStream out)
    {
        outBuffer = out;
    }

    public ByteArrayOutputStream getResponseOutput()
    {
        return outBuffer;
    }

    public int getStatus()
    {
        return status;
    }

    public void setStatus(int status)
    {
        this.status = status;
    }

    public void setVersion(int v)
    {
        version = v;
    }

    public int getVersion()
    {
        return version;
    }

    public void incrementMessageNumber()
    {
        messageNumber++;
    }

    public int getDeserializedBytes()
    {
        return deserializedBytes;
    }

    public void setDeserializedBytes(int deserializedBytes)
    {
        this.deserializedBytes = deserializedBytes;
    }

    public int getSerializedBytes()
    {
        return serializedBytes;
    }

    public void setSerializedBytes(int serializedBytes)
    {
        this.serializedBytes = serializedBytes;
    }

    public MessagePerformanceInfo getMPII()
    {
        return mpii;
    }

    public void setMPII(MessagePerformanceInfo mpii)
    {
        this.mpii = mpii;
    }

    public MessagePerformanceInfo getMPIO()
    {
        return mpio;
    }

    public void setMPIO(MessagePerformanceInfo mpio)
    {
        this.mpio = mpio;
    }

    public boolean isRecordMessageSizes()
    {
        return recordMessageSizes;
    }

    public void setRecordMessageSizes(boolean recordMessageSizes)
    {
        this.recordMessageSizes = recordMessageSizes;
    }

    public boolean isRecordMessageTimes()
    {
        return recordMessageTimes;
    }

    public boolean isMPIenabled()
    {
        return recordMessageTimes || recordMessageSizes;
    }

    public void setRecordMessageTimes(boolean recordMessageTimes)
    {
        this.recordMessageTimes = recordMessageTimes;
    }

}
