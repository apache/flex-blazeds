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

import flex.messaging.MessageException;
import flex.messaging.io.MessageDeserializer;
import flex.messaging.io.MessageIOConstants;
import flex.messaging.io.RecoverableSerializationException;
import flex.messaging.io.SerializationContext;

import java.io.IOException;
import java.io.InputStream;

public class AmfMessageDeserializer implements MessageDeserializer {
    public static final String CODE_VERSION_MISMATCH = "VersionMismatch";
    private static final int UNSUPPORTED_AMF_VERSION = 10310;

    protected ActionMessageInput amfIn;

    protected AmfTrace debugTrace;
    protected boolean isDebug;

    public AmfMessageDeserializer() {
    }

    public void initialize(SerializationContext context, InputStream in, AmfTrace trace) {
        amfIn = new Amf0Input(context);
        amfIn.setInputStream(in);

        debugTrace = trace;
        isDebug = debugTrace != null;
        amfIn.setDebugTrace(debugTrace);
    }

    public void readMessage(ActionMessage m, ActionContext context) throws ClassNotFoundException, IOException {
        if (isDebug)
            debugTrace.startRequest("Deserializing AMF/HTTP request");

        int version = amfIn.readUnsignedShort();

        // Treat FMS's AMF1 as AMF0.
        if (version == MessageIOConstants.AMF1)
            version = MessageIOConstants.AMF0;

        if (version != MessageIOConstants.AMF0 && version != MessageIOConstants.AMF3) {
            //Unsupported AMF version {version}.
            MessageException ex = new MessageException();
            ex.setMessage(UNSUPPORTED_AMF_VERSION, new Object[]{new Integer(version)});
            ex.setCode(CODE_VERSION_MISMATCH);
            throw ex;
        }

        m.setVersion(version);
        context.setVersion(version);

        if (isDebug)
            debugTrace.version(version);

        // Read headers
        int headerCount = amfIn.readUnsignedShort();
        for (int i = 0; i < headerCount; ++i) {
            MessageHeader header = new MessageHeader();
            m.addHeader(header);
            readHeader(header, i);
        }

        // Read bodies
        int bodyCount = amfIn.readUnsignedShort();
        for (int i = 0; i < bodyCount; ++i) {
            MessageBody body = new MessageBody();
            m.addBody(body);
            readBody(body, i);
        }
    }


    /**
     * Deserialize a message header from the input stream.
     * A message header is structured as:
     * NAME kString
     * MUST UNDERSTAND kBoolean
     * LENGTH kInt
     * DATA kObject
     *
     * @param header - will hold the deserialized message header
     * @param index  header index for debugging
     * @throws IOException            thrown by the underlying stream
     * @throws ClassNotFoundException if we don't find the class for the header data.
     */
    public void readHeader(MessageHeader header, int index) throws ClassNotFoundException, IOException {
        String name = amfIn.readUTF();
        header.setName(name);
        boolean mustUnderstand = amfIn.readBoolean();
        header.setMustUnderstand(mustUnderstand);

        amfIn.readInt(); // Length

        amfIn.reset();
        Object data;

        if (isDebug)
            debugTrace.startHeader(name, mustUnderstand, index);

        try {
            data = readObject();
        } catch (RecoverableSerializationException ex) {
            ex.setCode("Client.Header.Encoding");
            data = ex;
        } catch (MessageException ex) {
            ex.setCode("Client.Header.Encoding");
            throw ex;
        }

        header.setData(data);

        if (isDebug)
            debugTrace.endHeader();
    }


    /**
     * Deserialize a message body from the input stream.
     *
     * @param body  - will hold the deserialized message body
     * @param index message index for debugging
     * @throws IOException            thrown by the underlying stream
     * @throws ClassNotFoundException if we don't find the class for the body data.
     */
    public void readBody(MessageBody body, int index) throws ClassNotFoundException, IOException {
        String targetURI = amfIn.readUTF();
        body.setTargetURI(targetURI);
        String responseURI = amfIn.readUTF();
        body.setResponseURI(responseURI);

        amfIn.readInt(); // Length

        amfIn.reset();
        Object data;

        if (isDebug)
            debugTrace.startMessage(targetURI, responseURI, index);

        try {
            data = readObject();
        } catch (RecoverableSerializationException ex) {
            ex.setCode("Client.Message.Encoding");
            data = ex;
        } catch (MessageException ex) {
            ex.setCode("Client.Message.Encoding");
            throw ex;
        }

        body.setData(data);

        if (isDebug)
            debugTrace.endMessage();
    }

    /**
     * Read Object.
     *
     * @return Object the object read from AmfInput
     * @throws ClassNotFoundException, IOException when exceptions occurs in reading the object
     */
    public Object readObject() throws ClassNotFoundException, IOException {
        return amfIn.readObject();
    }
}

