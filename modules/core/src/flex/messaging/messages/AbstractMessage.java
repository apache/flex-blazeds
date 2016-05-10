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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.IdentityHashMap;

import flex.messaging.log.LogCategories;
import flex.messaging.log.Log;
import flex.messaging.util.StringUtils;
import flex.messaging.util.UUIDUtils;
import flex.messaging.util.ExceptionUtil;

/**
 * This is the default implementation of Message, which
 * provides a convenient base for behavior and associations
 * common to all endpoints. 
 */
public abstract class AbstractMessage implements Message, Cloneable
{
    /**
     * This number was generated using the 'serialver' command line tool.
     * This number should remain consistent with the version used by
     * ColdFusion to communicate with the message broker over RMI.
     */
    private static final long serialVersionUID = -834697863344344313L;

    // Serialization constants
    protected static final short HAS_NEXT_FLAG = 128;
    protected static final short BODY_FLAG = 1;
    protected static final short CLIENT_ID_FLAG = 2;
    protected static final short DESTINATION_FLAG = 4;
    protected static final short HEADERS_FLAG = 8;
    protected static final short MESSAGE_ID_FLAG = 16;
    protected static final short TIMESTAMP_FLAG = 32;
    protected static final short TIME_TO_LIVE_FLAG = 64;
    protected static final short CLIENT_ID_BYTES_FLAG = 1;
    protected static final short MESSAGE_ID_BYTES_FLAG = 2;

    protected Object clientId;
    protected String destination;
    protected String messageId;
    protected long timestamp;
    protected long timeToLive;
    
    protected Map headers;
    protected Object body;

    protected byte[] clientIdBytes;
    protected byte[] messageIdBytes;

    /**
     * Returns the client id.
     * 
     * @return The client id.
     */
    public Object getClientId()
    {
        return clientId;
    }

    /**
     * Sets the client id.
     * 
     * @param clientId The client id.
     */
    public void setClientId(Object clientId)
    {
        this.clientId = clientId;
        clientIdBytes = null;
    }    

    /**
     * Returns the message id.
     * 
     * @return The message id.
     */
    public String getMessageId()
    {
        return messageId;
    }

    /**
     * Sets the message id.
     * 
     * @param messageId The message id.
     */
    public void setMessageId(String messageId)
    {
        this.messageId = messageId;
        messageIdBytes = null;
    }

    /**
     * Returns the timestamp.
     * 
     * @return The timestamp.
     */
    public long getTimestamp()
    {
        return timestamp;
    }

    /**
     * Sets the timestamp.
     * 
     * @param timestamp The timestamp.
     */
    public void setTimestamp(long timestamp)
    {
        this.timestamp = timestamp;
    }

    /**
     * Returns the time to live.
     * 
     * @return The time to live.
     */
    public long getTimeToLive()
    {
        return timeToLive;
    }

    /**
     * Sets the time to live.
     * 
     * @param timeToLive The time to live.
     */
    public void setTimeToLive(long timeToLive)
    {
        this.timeToLive = timeToLive;
    }

    /**
     * Returns the body.
     * 
     * @return the body.
     */
    public Object getBody()
    {
        return body;
    }
    

    /**
     * Sets the body.
     * 
     * @param body The body.
     */
    public void setBody(Object body)
    {
        this.body = body;
    }

    /**
     * Returns the destination id.
     * 
     * @return The destination id.
     */
    public String getDestination()
    {
        return destination;
    }

    /**
     * Sets the destination id.
     * 
     * @param destination The destination id.
     */
    public void setDestination(String destination)
    {
        this.destination = destination;
    }

    /**
     * Returns the headers.
     * 
     * @return The headers.
     */
    public Map getHeaders()
    {
        if (headers == null)
        {
            headers = new HashMap();
        }
        return headers;
    }

    /**
     * Sets the headers.
     * 
     * @param newHeaders The new headers to set.
     */
    public void setHeaders(Map newHeaders)
    {
        for (Iterator iter = newHeaders.entrySet().iterator(); iter.hasNext(); )
        {
            Map.Entry entry = (Map.Entry) iter.next();
            String propName = (String) entry.getKey();
            setHeader(propName, entry.getValue());
        }
    }

    /**
     * Returns the header value associated with the header name, or null.
     * @param headerName the header name
     * @return The header value associaged with the header name.
     */
    public Object getHeader(String headerName)
    {
        return headers != null? headers.get(headerName) : null;
    }

    /**
     * Sets the header name and value.
     * 
     * @param headerName The header name.
     * @param value The header value.
     */
    public void setHeader(String headerName, Object value)
    {
        if (headers == null)
            headers = new HashMap();

        if (value == null)
            headers.remove(headerName);
        else
            headers.put(headerName, value);
    }

    /**
     * Determines whether the header exists.
     * @param headerName the header name
     * @return True if the header exists.
     */
    public boolean headerExists(String headerName)
    {
        return (headers != null && headers.containsKey(headerName));
    }

    public boolean equals(Object o)
    {
        if (o instanceof Message)
        {
            if (messageId == null)
                return this == o;

            Message m = (Message) o;
            if (m.getMessageId().equals(this.getMessageId()))
            {
                return true;
            }
        }
        return false;        
    }

    public int hashCode()
    {
        if (messageId == null)
            return super.hashCode();
        return messageId.hashCode();
    }

    /**
     * Returns a category to use when logging against this message type.
     * @return String the log category
     */
    public String logCategory() 
    {
       return LogCategories.MESSAGE_GENERAL;
    }

    public String toString()
    {
        return toString(1);
    }

    public String toString(int indent)
    {
        return toStringHeader(indent) + toStringFields(indent+1);
    }

    /**
     *
     * 
     * While this class itself does not implement java.io.Externalizable,
     * SmallMessage implementations will typically use Externalizable to
     * serialize themselves in a smaller form. This method supports this
     * functionality by implementing Externalizable.readExternal(ObjectInput) to
     * deserialize the properties for this abstract base class.
     */
    public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException
    {
        short[] flagsArray = readFlags(input);

        for (int i = 0; i < flagsArray.length; i++)
        {
            short flags = flagsArray[i];
            short reservedPosition = 0;

            if (i == 0)
            {
                if ((flags & BODY_FLAG) != 0)
                    readExternalBody(input);
        
                if ((flags & CLIENT_ID_FLAG) != 0)
                    clientId = input.readObject();
        
                if ((flags & DESTINATION_FLAG) != 0)
                    destination = (String)input.readObject();
        
                if ((flags & HEADERS_FLAG) != 0)
                    headers = (Map)input.readObject();
        
                if ((flags & MESSAGE_ID_FLAG) != 0)
                    messageId = (String)input.readObject();
        
                if ((flags & TIMESTAMP_FLAG) != 0)
                    timestamp = ((Number)input.readObject()).longValue();
        
                if ((flags & TIME_TO_LIVE_FLAG) != 0)
                    timeToLive = ((Number)input.readObject()).longValue();

                reservedPosition = 7;
            }
            else if (i == 1)
            {
                if ((flags & CLIENT_ID_BYTES_FLAG) != 0)
                {
                    clientIdBytes = (byte[])input.readObject();
                    clientId = UUIDUtils.fromByteArray(clientIdBytes);
                }
        
                if ((flags & MESSAGE_ID_BYTES_FLAG) != 0)
                {
                    messageIdBytes = (byte[])input.readObject();
                    messageId = UUIDUtils.fromByteArray(messageIdBytes);
                }

                reservedPosition = 2;
            }

            // For forwards compatibility, read in any other flagged objects to
            // preserve the integrity of the input stream...
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
     * 
     * While this class itself does not implement java.io.Externalizable,
     * SmallMessage implementations will typically use Externalizable to
     * serialize themselves in a smaller form. This method supports this
     * functionality by implementing Externalizable.writeExternal(ObjectOutput)
     * to efficiently serialize the properties for this abstract base class.
     */
    public void writeExternal(ObjectOutput output) throws IOException
    {
        short flags = 0;

        if (clientIdBytes == null && clientId != null && clientId instanceof String)
            clientIdBytes = UUIDUtils.toByteArray((String)clientId);

        if (messageIdBytes == null && messageId != null)
            messageIdBytes = UUIDUtils.toByteArray(messageId);

        if (body != null)
            flags |= BODY_FLAG;

        if (clientId != null && clientIdBytes == null)
            flags |= CLIENT_ID_FLAG;

        if (destination != null)
            flags |= DESTINATION_FLAG;

        if (headers != null)
            flags |= HEADERS_FLAG;

        if (messageId != null && messageIdBytes == null)
            flags |= MESSAGE_ID_FLAG;

        if (timestamp != 0)
            flags |= TIMESTAMP_FLAG;

        if (timeToLive != 0)
            flags |= TIME_TO_LIVE_FLAG;

        if (clientIdBytes != null || messageIdBytes != null)
            flags |= HAS_NEXT_FLAG;

        output.writeByte(flags);

        flags = 0;

        if (clientIdBytes != null)
            flags |= CLIENT_ID_BYTES_FLAG;

        if (messageIdBytes != null)
            flags |= MESSAGE_ID_BYTES_FLAG;

        if (flags != 0)
            output.writeByte(flags);

        if (body != null)
            writeExternalBody(output);

        if (clientId != null && clientIdBytes == null)
            output.writeObject(clientId);

        if (destination != null)
            output.writeObject(destination);

        if (headers != null)
            output.writeObject(headers);

        if (messageId != null && messageIdBytes == null)
            output.writeObject(messageId);

        if (timestamp != 0)
            output.writeObject(new Long(timestamp));

        if (timeToLive != 0)
            output.writeObject(new Long(timeToLive));

        if (clientIdBytes != null)
            output.writeObject(clientIdBytes);

        if (messageIdBytes != null)
            output.writeObject(messageIdBytes);
    }

    public Object clone() 
    {
        AbstractMessage m = null;
        try 
        {
            m = (AbstractMessage) super.clone();

            /* NOTE: this is not cloning the body - just the headers */
            if (headers != null)
                m.headers = (HashMap) ((HashMap) headers).clone();
        }
        catch (CloneNotSupportedException exc) 
        {
            // can't happen..
        }
        return m;
    }

    /**
     * Implements Comparable. Compares this message with the other message,
     * according to the message priority header value (if one exists). 
     * @param otherMessage the message to compare with
     * @return int return 1 if the priority is lower than the other message, 0 if equal and -1 if higher
     */
    public int compareTo(Message otherMessage) 
    {
        Object priorityHeader = getHeader(PRIORITY_HEADER);
        int thisPriority = priorityHeader == null? DEFAULT_PRIORITY : ((Integer)priorityHeader).intValue();
        priorityHeader = otherMessage.getHeader(PRIORITY_HEADER);
        int otherPriority = priorityHeader == null? DEFAULT_PRIORITY : ((Integer)priorityHeader).intValue();
        // Note that lower priority goes last.
        return (thisPriority < otherPriority? 1 : (thisPriority == otherPriority? 0 : -1));
    }

    static final String [] indentLevels = 
        {"", "  ", "    ", "      ", "        ","          "};

    protected String getIndent(int indentLevel) 
    {
        if (indentLevel < indentLevels.length) return indentLevels[indentLevel];
        StringBuffer sb = new StringBuffer();
        sb.append(indentLevels[indentLevels.length-1]);
        indentLevel -= indentLevels.length - 1;
        for (int i = 0; i < indentLevel; i++)
            sb.append("  ");
        return sb.toString();
    }

    protected String getFieldSeparator(int indentLevel) 
    {
        String indStr = getIndent(indentLevel);
        if (indentLevel > 0) 
            indStr = StringUtils.NEWLINE + indStr;
        else 
            indStr = " ";
        return indStr;
    }

    protected String toStringHeader(int indentLevel) 
    {
        String s = "Flex Message";
        s += " (" + getClass().getName() + ") ";
        return s;
    }

    protected String toStringFields(int indentLevel)
    {
        if (headers != null)
        {
            String sep = getFieldSeparator(indentLevel); 
            StringBuilder sb = new StringBuilder();
            for (Iterator i = headers.entrySet().iterator(); i.hasNext();)
            {
                Map.Entry e = (Map.Entry) i.next();
                String key = e.getKey().toString();
                sb.append(sep).append("hdr(").append(key).append(") = ");
                if (Log.isExcludedProperty(key))
                    sb.append(Log.VALUE_SUPRESSED);
                else
                    sb.append(bodyToString(e.getValue(), indentLevel+1));
            }
            return sb.toString();
        }
        return "";
    }

    /**
     * This is usually an array so might as well format it nicely in 
     * this case.
     */
    protected final String bodyToString(Object body, int indentLevel) 
    {
        return bodyToString(body, indentLevel, null);
    }

    /**
     * This is usually an array so might as well format it nicely in 
     * this case.
     */
    protected final String bodyToString(Object body, int indentLevel, Map visited) 
    {
        try 
        {
            indentLevel = indentLevel + 1;
            if (visited == null && indentLevel > 18)
                return StringUtils.NEWLINE + getFieldSeparator(indentLevel) + "<..max-depth-reached..>";
            return internalBodyToString(body, indentLevel, visited);
        }
        catch (RuntimeException exc) 
        {
            return "Exception in body toString: " + ExceptionUtil.toString(exc);
        }
    }

    protected String internalBodyToString(Object body, int indentLevel)     
    {
        return internalBodyToString(body, indentLevel, null);
    }

    protected String internalBodyToString(Object body, int indentLevel, Map visited)
    {
        if (body instanceof Object[]) 
        {
            if ((visited = checkVisited(visited, body)) == null)
                return "<--";

            String sep = getFieldSeparator(indentLevel);
            StringBuffer sb = new StringBuffer();
            Object [] arr = (Object[]) body;
            sb.append(getFieldSeparator(indentLevel-1));
            sb.append("[");
            sb.append(sep);
            for (int i = 0; i < arr.length; i++)
            {
                if (i != 0) 
                {
                    sb.append(",");
                    sb.append(sep);
                }
                sb.append(bodyToString(arr[i],indentLevel,visited));
            }
            sb.append(getFieldSeparator(indentLevel-1));
            sb.append("]");
            return sb.toString();
        }
        // This is here so we can format maps with Object[] as values properly
        // and with the proper indent
        else if (body instanceof Map)
        {
            if ((visited = checkVisited(visited, body)) == null)
                return "<--";
            Map bodyMap = (Map) body;
            StringBuffer buf = new StringBuffer();
            buf.append("{");
            Iterator it = bodyMap.entrySet().iterator();
            while(it.hasNext())
            {
                Map.Entry e = (Map.Entry) it.next();
                Object key = e.getKey();
                Object value = e.getValue();
                buf.append(key == this ? "(recursive Map as key)" : key);
                buf.append("=");
                if (value == this)
                    buf.append("(recursive Map as value)");
                else if (Log.isExcludedProperty(key.toString()))
                    buf.append(Log.VALUE_SUPRESSED);
                else
                    buf.append(bodyToString(value, indentLevel + 1, visited));

                if (it.hasNext())
                    buf.append(", ");
            }
            buf.append("}");
            return buf.toString();
        }
        else if (body instanceof AbstractMessage) 
        {
            return ((AbstractMessage)body).toString(indentLevel);
        }
        else if (body != null)
            return body.toString();
        else return "null";
    }

    /**
     *
     * Used by the readExtenral method to read the body.
     *  
     * @param input Object input.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    protected void readExternalBody(ObjectInput input) throws IOException, ClassNotFoundException
    {
        body = input.readObject();
    }

    /**
     *
     * To support efficient serialization for SmallMessage implementations,
     * this utility method reads in the property flags from an ObjectInput
     * stream. Flags are read in one byte at a time. Flags make use of
     * sign-extension so that if the high-bit is set to 1 this indicates that
     * another set of flags follows.
     * 
     * @return The array of property flags. 
     */
    protected short[] readFlags(ObjectInput input) throws IOException
    {
        boolean hasNextFlag = true;
        short[] flagsArray = new short[2];
        int i = 0;

        while (hasNextFlag)
        {
            short flags = (short)input.readUnsignedByte();
            if (i == flagsArray.length)
            {
                short[] tempArray = new short[i*2];
                System.arraycopy(flagsArray, 0, tempArray, 0, flagsArray.length);
                flagsArray = tempArray;
            }

            flagsArray[i] = flags;

            hasNextFlag = (flags & HAS_NEXT_FLAG) != 0;

            i++;
        }

        return flagsArray;
    }

    /**
     *
     * Used by writeExternal method to write the body.
     * 
     * @param output The object output.
     * @throws IOException
     */
    protected void writeExternalBody(ObjectOutput output) throws IOException
    {
        output.writeObject(body);
    }

    private Map checkVisited(Map visited, Object obj)
    {
        if (visited == null)
            visited = new IdentityHashMap();
        else if (visited.get(obj) != null)
            return null;

        visited.put(obj, Boolean.TRUE);

        return visited;
    }

}
