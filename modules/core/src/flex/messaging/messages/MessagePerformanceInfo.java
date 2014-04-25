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

import java.io.Serializable;

/**
 * @exclude
 *
 * The MessagePerformanceInfo class is used to capture various metrics about
 * the sizing and timing of a message sent from a client to the server and its
 * response message, as well as pushed messages from the server to the client.
 * A response message should have two instances of this class among its headers,
 * headers[MPII] - info for the client to server message,
 * headers[MPIO] - info for the response message from server to client.
 * A pushed message will have an extra headers and its headers will represent,
 * headers[MPII] - info for the client to server message poll message (non RTMP)
 * headers[MPIO] - info for the pushed message from server to client,
 * headers[MPIP] - info for the message from the client that caused the push message
 *
 * It has a symmetric AS counterpart - MessagePerformanceInfo.as
 */
public class MessagePerformanceInfo implements Serializable, Cloneable 
{
    private static final long serialVersionUID = -8556484221291213962L;

    /**
     * @exclude
     *
      * Size of message in Bytes (message types depends on what header this MPI is in)
      */
    public long messageSize;

    /**
     * @exclude
     *
      * Millisecond timestamp of when this message was sent
      * (origin depends on on what header this MPI is in)
      */
    public long sendTime;

    /**
     * @exclude
     *
      * Millisecond timestamp of when this message was received
      * (destination depends on on what header this MPI is in)
      */
    public long receiveTime;

    /**
     * @exclude
     *
      * Amount of time in milliseconds that this message was being processed on the server
      * in order to calculate and populate MPI metrics
      */
    public long overheadTime;

    /**
     * @exclude
     *
      * "OUT" when this message originated on the server
      */
    public String infoType;

    /**
     * @exclude
     *
      * True if this is info for a message that was pushed from server to client
      */
    public boolean pushedFlag;

    /**
     * @exclude
     *
     * Flag is true when record-message-sizes is enabled for the communication channel
      */
    public boolean recordMessageSizes;

    /**
     * @exclude
     *
     * Flag is true when record-message-times is enabled for the communication channel
      */
    public boolean recordMessageTimes;

    /**
     * @exclude
     *
     * Millisecond timestamp of when the server became ready to push this message out
     * to clients
      */
    public long serverPrePushTime;

    /**
     * @exclude
     *
     * Millisecond timestamp of when the server called into the adapter associated with the
     * destination of this message
      */
    public long serverPreAdapterTime;

    /**
     * @exclude
     *
     * Millisecond timestamp of when server processing returned from the adapater associated
     * with the destination of this message
      */
    public long serverPostAdapterTime;

    /**
     * @exclude
     *
     * Millisecond timestamp of when the adapter associated with the destination of this message
     * made a call to an external component (for example a JMS server)
      */
    public long serverPreAdapterExternalTime;

    /**
     * @exclude
     *
     * Millisecond timestamp of when processing came back to the adapter associated with the destination
     * of this message from a call to an external component (for example a JMS server)
      */
    public long serverPostAdapterExternalTime;

    /**
     * @exclude
     *
      * Copies the immutable fields of this MPI isntance over to create a new one
      * @return cloned instance of this MessagePerformanceInfo instance
      */
    public Object clone()
    {
        try
        {
            return super.clone();
        }        
        catch (CloneNotSupportedException e)
        {
            // This cannot happen since the super class is Object but since 
            // the exception has to be caught anyway, code is left in
            MessagePerformanceInfo mpii = new MessagePerformanceInfo();
            mpii.messageSize = this.messageSize;
            mpii.sendTime = this.sendTime;
            mpii.receiveTime = this.receiveTime;
            mpii.overheadTime = this.overheadTime;
            mpii.serverPrePushTime = this.serverPrePushTime;
            mpii.serverPreAdapterTime = this.serverPreAdapterTime;
            mpii.serverPostAdapterTime = this.serverPostAdapterTime;
            mpii.serverPreAdapterExternalTime = this.serverPreAdapterExternalTime;
            mpii.serverPostAdapterExternalTime = this.serverPostAdapterExternalTime;
            mpii.recordMessageSizes = this.recordMessageSizes;
            mpii.recordMessageTimes = this.recordMessageTimes;
            return mpii;
        }

    }

    /**
     * @exclude
     *
      * Increase the overhead counter for this MPI
      * @param overhead Increment size in milliseconds
      */
    public void addToOverhead(long overhead)
    {
        overheadTime += overhead;
    }

    /**
     * @exclude
     *
     * Default constructor
      */
    public MessagePerformanceInfo()
    {

    }
}
