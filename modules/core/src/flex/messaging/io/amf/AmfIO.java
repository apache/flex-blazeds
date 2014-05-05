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
import flex.messaging.io.AbstractProxy;
import flex.messaging.io.SerializationContext;
import flex.messaging.io.SerializationException;
import flex.messaging.io.TypeMarshallingContext;

/**
 * Base class for Amf I/O. 
 * @exclude
 */
abstract class AmfIO
{
    protected final SerializationContext context;
    /*
     *  DEBUG LOGGING.
     */
    protected boolean isDebug;
    protected AmfTrace trace;
    
    // Nest object level, how deep the object graph is right now
    private int nestObjectLevel;
    // Nest collection level, how deep the collection nest is right now, for example 3 dimensional matrix will reach level of 3
    private int nestCollectionLevel;
    

    /*
     *  OPTIMIZATION.
     */
    private char[] tempCharArray = null;
    private byte[] tempByteArray = null;

    AmfIO(SerializationContext context)
    {
        this.context = context;
        nestObjectLevel = 0;
        nestCollectionLevel = 0;
    }

    /**
     * Turns on "trace" debugging for AMF responses.
     * @param trace the trace object
     */
    public void setDebugTrace(AmfTrace trace)
    {
        this.trace = trace;
        isDebug = this.trace != null;
    }

    /**
     * Clear all object reference information so that the instance
     * can be used to deserialize another data structure.
     * 
     * Reset should be called before reading a top level object,
     * such as a new header or a new body.
     */
    public void reset()
    {
        nestObjectLevel = 0;
        nestCollectionLevel = 0;
        TypeMarshallingContext marshallingContext = TypeMarshallingContext.getTypeMarshallingContext();
        marshallingContext.reset();
    }

    /**
     * Returns an existing array with a length of at least the specified
     * capacity.  This method is for optimization only.  Do not use the array
     * outside the context of this method and do not call this method again
     * while the array is being used.
     * @param capacity minimum length
     * @return a character array 
     */
    final char[] getTempCharArray(int capacity)
    {
        char[] result = this.tempCharArray;
        if ((result == null) || (result.length < capacity))
        {
            result = new char[capacity * 2];
            tempCharArray = result;
        }
        return result;
    }

    /**
     * Returns an existing array with a length of at least the specified
     * capacity.  This method is for optimization only.  Do not use the array
     * outside the context of this method and do not call this method again
     * while the array is being used.
     * @param capacity minimum length
     * @return a byte array
     */
    final byte[] getTempByteArray(int capacity)
    {
        byte[] result = this.tempByteArray;
        if ((result == null) || (result.length < capacity))
        {
            result = new byte[capacity * 2];
            tempByteArray = result;
        }
        return result;
    }
    
    protected void increaseNestObjectLevel()
    {
        nestObjectLevel++;
       
        if (nestObjectLevel > context.maxObjectNestLevel)
        {
            SerializationException se = new SerializationException();
            se.setMessage(10315, new Object[] {context.maxObjectNestLevel});
            throw se;
        }
    }
    
    protected void decreaseNestObjectLevel()
    {
        nestObjectLevel--;
    }
    
    protected void increaseNestCollectionLevel()
    {
        nestCollectionLevel++;
        if (nestCollectionLevel > context.maxCollectionNestLevel)
        {
            SerializationException se = new SerializationException();
            se.setMessage(10316, new Object[] {context.maxCollectionNestLevel});
            throw se;
        }
    }
    
    protected void decreaseNestCollectionLevel()
    {
        nestCollectionLevel--;
    }
    
    public static boolean isCollectionClass(Object object)
    {
        if (object == null)
            return false;
        Class clazz = object.getClass();
        if (clazz.isArray())
            return true;
        if (java.util.Collection.class.isAssignableFrom(clazz))
            return true;
        if (java.util.Map.class.isAssignableFrom(clazz))
            return true;
        if (flex.messaging.io.ArrayCollection.class.equals(clazz))
            return true;
        if (flex.messaging.io.ArrayList.class.equals(clazz))
            return true;
        return false;
    }
}
