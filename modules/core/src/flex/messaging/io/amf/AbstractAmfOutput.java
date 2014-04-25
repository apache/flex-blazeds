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

import flex.messaging.io.SerializationContext;
import flex.messaging.util.XMLUtil;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.io.IOException;

import org.w3c.dom.Document;

/**
 * An abstract serializer of AMF protocol data.
 *
 * @see ActionMessageOutput
 * @exclude
 */
public abstract class AbstractAmfOutput extends AmfIO implements ActionMessageOutput
{
    /**
     * @exclude
     */
    protected DataOutputStream out;

    /**
     * Construct a serializer without connecting it to an output stream.
     * @param context serialization parameters
     */
    public AbstractAmfOutput(SerializationContext context)
    {
        super(context);
    }

    /**
     * Sets the output stream that the serializer should use.
     *
     * @param out DataOutputStream to use
     */
    public void setOutputStream(OutputStream out)
    {
        if (out instanceof DataOutputStream)
        {
            this.out = (DataOutputStream) out;
        }
        else
        {
            this.out = new DataOutputStream(out);
        }
        reset();
    }

    protected String documentToString(Object value) throws IOException
    {
        return XMLUtil.documentToString((Document)value);
    }

    //
    // java.io.ObjectOutput implementations
    //

    /** {@inheritDoc} */
    public void close() throws IOException
    {
        out.close();
    }

    /** {@inheritDoc} */
    public void flush() throws IOException
    {
        out.flush();
    }

    /** {@inheritDoc} */
    public void write(int b) throws IOException
    {
        out.write(b);
    }

    /** {@inheritDoc} */
    public void write(byte bytes[]) throws IOException
    {
        out.write(bytes);
    }

    /** {@inheritDoc} */
    public void write(byte bytes[], int offset, int length) throws IOException
    {
        out.write(bytes, offset, length);
    }


    //
    // java.io.DataOutput implementations
    //

    /** {@inheritDoc} */
    public void writeBoolean(boolean v) throws IOException
    {
        out.writeBoolean(v);
    }

    /** {@inheritDoc} */
    public void writeByte(int v) throws IOException
    {
        out.writeByte(v);
    }

    /** {@inheritDoc} */
    public void writeBytes(String s) throws IOException
    {
        out.writeBytes(s);
    }

    /** {@inheritDoc} */
    public void writeChar(int v) throws IOException
    {
        out.writeChar(v);
    }

    /** {@inheritDoc} */
    public void writeChars(String s) throws IOException
    {
        out.writeChars(s);
    }

    /** {@inheritDoc} */
    public void writeDouble(double v) throws IOException
    {
        out.writeDouble(v);
    }

    /** {@inheritDoc} */
    public void writeFloat(float v) throws IOException
    {
        out.writeFloat(v);
    }

    /** {@inheritDoc} */
    public void writeInt(int v) throws IOException
    {
        out.writeInt(v);
    }

    /** {@inheritDoc} */
    public void writeLong(long v) throws IOException
    {
        out.writeLong(v);
    }

    /** {@inheritDoc} */
    public void writeShort(int v) throws IOException
    {
        out.writeShort(v);
    }

    /** {@inheritDoc} */
    public void writeUTF(String s) throws IOException
    {
        out.writeUTF(s);
    }
}
