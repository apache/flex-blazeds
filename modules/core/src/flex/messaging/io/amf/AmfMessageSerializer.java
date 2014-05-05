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
import flex.messaging.io.MessageSerializer;
import flex.messaging.io.SerializationContext;

import java.io.IOException;
import java.io.OutputStream;

public class AmfMessageSerializer implements MessageSerializer
{
    public static final int UNKNOWN_CONTENT_LENGTH = 1; //-1;

    protected Amf0Output amfOut;

    protected boolean isDebug;
    protected AmfTrace debugTrace;
    protected int version;

    public AmfMessageSerializer()
    {
    }

    public void setVersion(int value)
    {
        version = value;
    }

    public void initialize(SerializationContext context, OutputStream out, AmfTrace trace)
    {
        amfOut = new Amf0Output(context);
        amfOut.setOutputStream(out);
        amfOut.setAvmPlus(version >= MessageIOConstants.AMF3);

        debugTrace = trace;
        isDebug = trace != null;
        amfOut.setDebugTrace(debugTrace);
    }

    /**
     * Serializes a message to the output stream.
     *
     * @param m message to serialize
     * @throws IOException
     */
    public void writeMessage(ActionMessage m) throws IOException
    {
        if (isDebug)
            debugTrace.startResponse("Serializing AMF/HTTP response");

        int version = m.getVersion();

        amfOut.setAvmPlus(version >= MessageIOConstants.AMF3);

        // Write packet header
        amfOut.writeShort(version);

        if (isDebug)
            debugTrace.version(version);

        // Write out headers
        int headerCount = m.getHeaderCount();
        amfOut.writeShort(headerCount);
        for (int i = 0; i < headerCount; ++i)
        {
            MessageHeader header = m.getHeader(i);

            if (isDebug)
                debugTrace.startHeader(header.getName(), header.getMustUnderstand(), i);

            writeHeader(header);

            if (isDebug)
                debugTrace.endHeader();
        }

        // Write out the bodies
        int bodyCount = m.getBodyCount();
        amfOut.writeShort(bodyCount);
        for (int i = 0; i < bodyCount; ++i)
        {
            MessageBody body = m.getBody(i);

            if (isDebug)
                debugTrace.startMessage(body.getTargetURI(), body.getResponseURI(), i);

            writeBody(body);

            if (isDebug)
                debugTrace.endMessage();
        }
    }

    /**
     * Serializes a message header to the output stream.
     *
     * @param h header to serialize
     * @throws IOException if write fails.
     */
    public void writeHeader(MessageHeader h) throws IOException
    {
        amfOut.writeUTF(h.getName());
        amfOut.writeBoolean(h.getMustUnderstand());
        amfOut.writeInt(UNKNOWN_CONTENT_LENGTH);
        amfOut.reset();
        writeObject(h.getData());
    }

    /**
     * Serializes a message body to the output stream.
     *
     * @param b body to serialize
     * @throws IOException if write fails.
     */
    public void writeBody(MessageBody b) throws IOException
    {
        if (b.getTargetURI() == null)
            amfOut.writeUTF("null");
        else
            amfOut.writeUTF(b.getTargetURI());

        if (b.getResponseURI() == null)
            amfOut.writeUTF("null");
        else
            amfOut.writeUTF(b.getResponseURI());

        amfOut.writeInt(UNKNOWN_CONTENT_LENGTH);
        amfOut.reset();

        Object data = b.getData();
        writeObject(data);
    }

    /**
     * Serializes an Object directly to the output stream.
     *
     * @param value - the Object to write to the AMF stream.
     */
    public void writeObject(Object value) throws IOException
    {
        amfOut.writeObject(value);
    }
}