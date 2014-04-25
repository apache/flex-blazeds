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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * This class represents an already serialized "pass thru" value, whose bytes
 * need to be passed "as is" to the output stream.
 *
 * <p>The scenario that drove this need is to process message return values
 * from non-Java method implementations (e.g .Net), that have already been
 * serialized on the non-Java side.</p>
 */
public class SerializedObject implements Externalizable
{
    protected byte[] objectBytes;
    protected int offset;
    
    /**
     * Constructor. 
     * Construct a SerializedObject with specified object bytes

     * @param objectBytes the actual bytes to write to the output stream.
     */
    public SerializedObject(byte[] objectBytes)
    {
        this(objectBytes, 0);
    }

    /**
     * Constructor. 
     * Construct a SerializedObject with specified object bytes and offset
     * @param objectBytes the actual bytes to write to the output stream.
     * @param offset the offset into the byte array from which to start writing.
     */
    public SerializedObject(byte[] objectBytes, int offset)
    {
        this.objectBytes = objectBytes;
        this.offset = offset;
    }

    /**
     * Get the object bytes.
     * @return the object bytes being held.
     */
    public byte[] getObjectBytes()
    {
        return objectBytes;
    }

    /**
     * Not supported. Serialized objects are intended to be "write only" values.
     * @param in the ObjectInput object
     * @throws IOException, ClassNotFoundException if the read failed
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
    {
        throw new UnsupportedOperationException("serialized values can only be written, not read.");
    }

    /**
     * Writes the object bytes directly to the output stream.
     * @param out the ObjectOutput object
     * @throws IOException when the write failed
     */
    public void writeExternal(ObjectOutput out) throws IOException
    {
        /**
         * So far that the pass through is not working as it will mess up the AMF format
         * We yet to find a viable solution for pass through to work
         * throw Exception to provent the useage of pass through
        for(int i = offset; i < objectBytes.length; i++)
        {
            byte b = objectBytes[i];
            out.writeByte(b);
        }
        */
        throw new UnsupportedOperationException("Pass through does not work, throw exception to provent us using the mechanism.");
    }
}
