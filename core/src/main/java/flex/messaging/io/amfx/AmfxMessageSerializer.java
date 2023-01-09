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
package flex.messaging.io.amfx;

import flex.messaging.io.MessageSerializer;
import flex.messaging.io.amf.MessageBody;
import flex.messaging.io.amf.MessageHeader;
import flex.messaging.io.amf.ActionMessage;
import flex.messaging.io.amf.AmfTrace;
import flex.messaging.io.SerializationContext;

import java.io.OutputStream;
import java.io.IOException;

/**
 *
 */
public class AmfxMessageSerializer implements MessageSerializer, AmfxTypes {
    protected AmfxOutput amfxOut;
    protected int version;

    /*
     *  DEBUG LOGGING
     */
    protected boolean isDebug;
    protected AmfTrace debugTrace;

    public static final String XML_DIRECTIVE = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n";

    public AmfxMessageSerializer() {
    }

    /**
     * @param value - The default version of AMFX encoding to be used.
     */
    public void setVersion(int value) {
        version = value;
    }

    /**
     * Establishes the context for writing out data to the given OutputStream.
     * A null value can be passed for the trace parameter if a record of the
     * AMFX data should not be made.
     *
     * @param context The SerializationContext specifying the custom options.
     * @param out     The OutputStream to write out the AMFX data.
     * @param trace   If not null, turns on "trace" debugging for AMFX responses.
     */
    public void initialize(SerializationContext context, OutputStream out, AmfTrace trace) {
        amfxOut = new AmfxOutput(context);
        amfxOut.setOutputStream(out);
        debugTrace = trace;
        isDebug = debugTrace != null;
        amfxOut.setDebugTrace(trace);

    }

    public void writeMessage(ActionMessage m) throws IOException {
        if (isDebug)
            debugTrace.startResponse("Serializing AMFX/HTTP response");

        amfxOut.writeUTF(XML_DIRECTIVE);

        int version = m.getVersion();
        writeOpenAMFX(version);

        if (isDebug)
            debugTrace.version(version);

        // Write out headers
        int headerCount = m.getHeaderCount();
        for (int i = 0; i < headerCount; ++i) {
            MessageHeader header = m.getHeader(i);

            if (isDebug)
                debugTrace.startHeader(header.getName(), header.getMustUnderstand(), i);

            writeHeader(header);

            if (isDebug)
                debugTrace.endHeader();
        }

        // Write out the body
        int bodyCount = m.getBodyCount();
        for (int i = 0; i < bodyCount; ++i) {
            MessageBody body = m.getBody(i);

            if (isDebug)
                debugTrace.startMessage(body.getTargetURI(), body.getResponseURI(), i);

            writeBody(body);

            if (isDebug)
                debugTrace.endMessage();
        }

        writeCloseAMFX();

        if (isDebug)
            debugTrace.endMessage();
    }

    protected void writeOpenAMFX(int version) throws IOException {
        int buflen = 14; // <amfx ver="3">
        StringBuffer sb = new StringBuffer(buflen);
        sb.append("<").append(AMFX_TYPE).append(" ver=\"");
        sb.append(version);
        sb.append("\">");

        amfxOut.writeUTF(sb);
    }

    protected void writeCloseAMFX() throws IOException {
        amfxOut.writeUTF(AMFX_CLOSE_TAG);
    }

    protected void writeHeader(MessageHeader h) throws IOException {
        int buflen = 127; // <header name="..." mustUnderstand="true">
        StringBuffer sb = new StringBuffer(buflen);
        sb.append("<").append(HEADER_TYPE).append(" name=\"");
        sb.append(h.getName());
        sb.append("\"");

        if (h.getMustUnderstand()) {
            sb.append(" mustUnderstand=\"");
            sb.append(h.getMustUnderstand());
            sb.append("\"");
        }

        sb.append(">");

        amfxOut.writeUTF(sb);

        writeObject(h.getData());

        amfxOut.writeUTF(HEADER_CLOSE_TAG);
    }

    protected void writeBody(MessageBody b) throws IOException {
        if (b.getTargetURI() == null && b.getResponseURI() == null) {
            amfxOut.writeUTF(BODY_OPEN_TAG);
        } else {
            int buflen = 127; // <body targetURI="..." responseURI="...">
            StringBuffer sb = new StringBuffer(buflen);
            sb.append("<").append(BODY_TYPE);

            if (b.getTargetURI() != null)
                sb.append(" targetURI=\"").append(b.getTargetURI()).append("\"");

            if (b.getResponseURI() != null)
                sb.append(" responseURI=\"").append(b.getResponseURI()).append("\"");

            sb.append(">");
            amfxOut.writeUTF(sb);
        }

        Object data = b.getData();
        writeObject(data);

        amfxOut.writeUTF(BODY_CLOSE_TAG);
    }

    public void writeObject(Object value) throws IOException {
        amfxOut.writeObject(value);
    }

}
