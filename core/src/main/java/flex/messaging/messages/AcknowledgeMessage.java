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

/**
 *
 * This is the type of message returned by the MessageBroker
 * to endpoints after the broker has routed an endpoint's message
 * to a service.
 */
public class AcknowledgeMessage extends AsyncMessage
{
    /**
     * This number was generated using the 'serialver' command line tool.
     * This number should remain consistent with the version used by
     * ColdFusion to communicate with the message broker over RMI.
     */
    private static final long serialVersionUID = 228072709981643313L;

    /**
     * Default constructor.
     */
    public AcknowledgeMessage()
    {
        this.messageId = UUIDUtils.createUUID(false);
        this.timestamp = System.currentTimeMillis();
    }

    /**
     *
     */
    public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException
    {
        super.readExternal(input);

        short[] flagsArray = readFlags(input);
        for (int i = 0; i < flagsArray.length; i++)
        {
            short flags = flagsArray[i];
            short reservedPosition = 0;

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
     *
     */
    public Message getSmallMessage()
    {
        if (getClass() == AcknowledgeMessage.class)
            return new AcknowledgeMessageExt(this);
        return null;
    }

    /**
     *
     */
    public void writeExternal(ObjectOutput output) throws IOException
    {
        super.writeExternal(output);

        short flags = 0;
        output.writeByte(flags);
    }
}
