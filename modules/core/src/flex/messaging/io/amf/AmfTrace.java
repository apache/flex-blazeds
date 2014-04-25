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

import flex.messaging.util.ObjectTrace;
import flex.messaging.log.Log;

/**
 * The AMFTrace class is an AMF extension to the ObjectTrace utility to format
 * AMF input/output in a similar manner to the client-side
 * NetConnection Debugger.
 *
 * Note that in this version new lines are added after the
 * individual values in complex type properties automatically.
 */
public class AmfTrace extends ObjectTrace
{
    /**
     * Default constructor.
     */
    public AmfTrace()
    {
        buffer = new StringBuffer(4096);
    }

    /**
     * Starts a request with the message.
     *
     * @param message start message string
     */
    public void startRequest(String message)
    {
        m_indent = 0;
        buffer.append(message);
        m_indent++;
    }

    /**
     * Starts a response with the message.
     *
     * @param message start response string
     */
    public void startResponse(String message)
    {
        m_indent = 0;
        buffer.append(message);
        m_indent++;
    }

    /**
     * Adds the version to the buffer.
     *
     * @param version the current version
     */
    public void version(int version)
    {
        newLine();
        buffer.append("Version: ").append(version);
    }

    /**
     * Starts a header.
     *
     * @param name header name
     * @param mustUnderstand mustUnderstand value
     * @param index header number
     */
    public void startHeader(String name, boolean mustUnderstand, int index)
    {
        newLine();
        buffer.append(indentString());
        buffer.append("(Header #").append(index).append(" name=").append(name);
        buffer.append(", mustUnderstand=").append(mustUnderstand).append(")");
        newLine();
        m_indent++;
    }

    /**
     * Ends the header.
     */
    public void endHeader()
    {
        m_indent--;
    }

    /**
     * Starts a command.
     *
     * @param cmd command object
     * @param iCmd command index
     * @param trxId transaction id
     */
    public void startCommand(Object cmd, int iCmd, Object trxId)
    {
        newLine();
        buffer.append(indentString());
        buffer.append("(Command method=").append(cmd).append(" (").append(iCmd).append(")");
        buffer.append(" trxId=").append(trxId).append(")");
        newLine();
        m_indent++;
    }

    /**
     * Ends the command.
     */
    public void endCommand()
    {
        m_indent--;
    }

    /**
     * Starts a message.
     *
     * @param targetURI the target URI
     * @param responseURI the response URI
     * @param index the message number
     */
    public void startMessage(String targetURI, String responseURI, int index)
    {
        newLine();
        buffer.append(indentString());
        buffer.append("(Message #").append(index).append(" targetURI=").append(targetURI);
        buffer.append(", responseURI=").append(responseURI).append(")");
        newLine();
        m_indent++;
    }

    /**
     * Ends the message.
     */
    public void endMessage()
    {
        m_indent--;
    }

    /**
     * Checks to make sure that this property is allowed to be written.
     * @return true if caller should not print value
     */
    private boolean isExcluded()
    {
        if (nextElementExclude)
        {
            nextElementExclude = false;
            buffer.append(Log.VALUE_SUPRESSED);
            newLine();
            return true;
        }
        return false;
    }

    /**
     * Writes an Object type.
     */
    @Override public void write(Object o)
    {
        if (isExcluded())
            return;

        if (m_nested <= 0)
            buffer.append(indentString());

        buffer.append(String.valueOf(o));
        newLine();
    }

    /**
     * Writes a boolean type.
     * @param b value to write
     */
    public void write(boolean b)
    {
        if (isExcluded())
            return;

        if (m_nested <= 0)
            buffer.append(indentString());

        buffer.append(b);
        newLine();
    }

    /**
     * Writes a double type.
     * @param d value to write
     */
    public void write(double d)
    {
        if (isExcluded())
            return;

        if (m_nested <= 0)
            buffer.append(indentString());

        buffer.append(d);
        newLine();
    }

    /**
     * Writes a float type.
     * @param f value to write
     */
    public void write(float f)
    {
        if (isExcluded())
            return;

        if (m_nested <= 0)
            buffer.append(indentString());

        buffer.append(f);
        newLine();
    }

    /**
     * Writes an integer type.
     * @param i value to write
     */
    public void write(int i)
    {
        if (isExcluded())
            return;

        if (m_nested <= 0)
            buffer.append(indentString());

        buffer.append(i);
        newLine();
    }

    /**
     * Writes a long type.
     * @param l value to write
     */
    public void write(long l)
    {
        if (isExcluded())
            return;

        if (m_nested <= 0)
            buffer.append(indentString());

        buffer.append(l);
        newLine();
    }

    /**
     * Writes a short type.
     * @param s value to write
     */
    public void write(short s)
    {
        if (isExcluded())
            return;

        if (m_nested <= 0)
            buffer.append(indentString());

        buffer.append(s);
        newLine();
    }

    /**
     * Writes a byte type.
     * @param b value to write.
     */
    public void write(byte b)
    {
        if (isExcluded())
            return;

        if (m_nested <= 0)
            buffer.append(indentString());

        buffer.append(b);
        newLine();
    }

    /**
     * Writes a null type.
     */
    @Override public void writeNull()
    {
        if (isExcluded())
            return;

        if (m_nested <= 0)
            buffer.append(indentString());

        buffer.append("null");
        newLine();
    }

    /**
     * Writes a ref type.
     */
    @Override public void writeRef(int ref)
    {
        if (isExcluded())
            return;

        if (m_nested <= 0)
            buffer.append(indentString());

        buffer.append("(Ref #").append(ref).append(")");
        newLine();
    }

    /**
     * Writes a String type.
     */
    @Override public void writeString(String s)
    {
        if (isExcluded())
            return;

        if (m_nested <= 0)
            buffer.append(indentString());

        buffer.append("\"").append(s).append("\"");

        newLine();
    }

    /**
     * Writes a String ref type.
     * @param ref string reference number
     */
    public void writeStringRef(int ref)
    {
        if (isExcluded())
            return;

        buffer.append(" (String Ref #").append(ref).append(")");
    }

    /**
     * Writes a traits info type.
     * @param ref trait reference number
     */
    public void writeTraitsInfoRef(int ref)
    {
        if (isExcluded())
            return;

        buffer.append(" (Traits Ref #").append(ref).append(")");
    }

    /**
     * Writes an undefined type.
     */
    public void writeUndefined()
    {
        if (isExcluded())
            return;

        if (m_nested <= 0)
            buffer.append(indentString());

        buffer.append("undefined");
        newLine();
    }

    /**
     * Starts an AMF array.
     *
     * @param ref the array reference number
     */
    public void startAMFArray(int ref)
    {
        if (isExcluded())
            return;

        if (m_nested <= 0)
            buffer.append(indentString());

        buffer.append("(Array #").append(ref).append(")").append(newLine);
        m_indent++;
        m_nested++;
    }

    /**
     * Starts an ECMA array.
     *
     * @param ref the array reference number
     */
    public void startECMAArray(int ref)
    {
        if (isExcluded())
            return;

        if (m_nested <= 0)
            buffer.append(indentString());

        buffer.append("(ECMA Array #").append(ref).append(")").append(newLine);
        m_indent++;
        m_nested++;
    }

    /**
     * Starts a ByteArrray.
     *
     * @param ref array reference number
     * @param length length of the array
     */
    public void startByteArray(int ref, int length)
    {
        if (isExcluded())
            return;

        if (m_nested <= 0)
            buffer.append(indentString());

        buffer.append("(Byte Array #").append(ref).append(", Length ").append(length).append(")").append(newLine);
    }

    /**
     * Ends a ByteArray.
     */
    public void endAMFArray()
    {
        m_indent--;
        m_nested--;
    }

    /**
     * Starts an ExternalizableObject.
     *
     * @param type the type of the object
     * @param ref the object number
     */
    public void startExternalizableObject(String type, int ref)
    {
        if (isExcluded())
            return;

        if (m_nested <= 0)
            buffer.append(indentString());

        buffer.append("(Externalizable Object #").append(ref).append(" \'").append(type).append("\')").append(newLine);

        m_indent++;
        m_nested++;

        buffer.append(indentString());
    }

    /**
     * Starts an AMFObject.
     *
     * @param type the type of the object
     * @param ref the reference number
     */
    public void startAMFObject(String type, int ref)
    {
        if (isExcluded())
            return;

        if (m_nested <= 0)
            buffer.append(indentString());

        if (type != null && type.length() > 0)
            buffer.append("(Typed Object #").append(ref).append(" \'").append(type).append("\')").append(newLine);
        else
            buffer.append("(Object #").append(ref).append(")").append(newLine);

        m_indent++;
        m_nested++;
    }

    /**
     * Ends an AMFObject.
     */
    public void endAMFObject()
    {
        m_indent--;
        m_nested--;
    }

    /**
     * Starts an AMF Dictionary
     *
     * @param ref the reference number
     */
    public void startAMFDictionary(int ref)
    {
        if (isExcluded())
            return;

        if (m_nested <= 0)
            buffer.append(indentString());

        buffer.append("(Dictionary #").append(ref).append(")").append(newLine);

        m_indent++;
        m_nested++;
    }

    /**
     * Starts an AMF Dictionary element.
     */
    public void startDictionaryElement()
    {
        buffer.append(indentString());
    }

    /**
     * Adds an equals sign after a dictionary key.
     */
    public void addDictionaryEquals()
    {
        buffer.append(indentString()).append("  ").append(" = ");
    }

    /**
     * Ends an AMF Dictionary.
     */
    public void endAMFDictionary()
    {
        m_indent--;
        m_nested--;
    }

    /**
     * Enum for different Vector types.
     */
    public enum VectorType
    {
        INT, UINT, DOUBLE, OBJECT;

        @Override public String toString()
        {
            return super.toString().toLowerCase();
        }
    };

    /**
     * Starts an AMF Vector.
     *
     * @param ref The reference number.
     */
    public void startAMFVector(int ref, VectorType vectorType)
    {
        if (isExcluded())
            return;

        if (m_nested <= 0)
            buffer.append(indentString());

        buffer.append("(Vector-" + vectorType + " #").append(ref).append(")").append(newLine);
        m_indent++;
        m_nested++;
    }

    /**
     * Ends an AMF Dictionary.
     */
    public void endAMFVector()
    {
        m_indent--;
        m_nested--;
    }
}