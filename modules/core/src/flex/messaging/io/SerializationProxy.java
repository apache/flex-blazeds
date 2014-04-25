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
package flex.messaging.io;

import flex.messaging.io.amf.Amf3Input;
import flex.messaging.io.amf.Amf3Output;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * This class is the server side portion of a custom serialization
 * mechanism to support partial serialization of Objects.
 *
 * The client uses the Externalizable interface to guarantee the
 * serialization order of the properties. The name of the remote class
 * is the first property send as it is needed to construct an object shell
 * before deserializing any of the wrapped instance properties so that
 * object references are correctly restored.
 */
public class SerializationProxy extends MapProxy implements Externalizable
{
    static final long serialVersionUID = -2544463435731984479L;

    /**
     * Default constructor required for deserialization of
     * client SerializationProxy instances. A SerializationProxy
     * is merely a utility wrapper on the server side which is
     * discarded once the externalizable information has been
     * read and the wrapped instance constructed.
     */
    public SerializationProxy()
    {
        super(null);
        externalizable = false;
    }
    
    public SerializationProxy(Object defaultInstance)
    {
        super(defaultInstance);
        externalizable = false;
    }

    public boolean isExternalizable()
    {
        return false;
    }

    public boolean isExternalizable(Object instance)
    {
        return false;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
    {
        Object saveObjectTable = null;
        Object saveTraitsTable = null;
        Object saveStringTable = null;
        Amf3Input in3 = null;

        if (in instanceof Amf3Input)
            in3 = (Amf3Input) in;

        try 
        {
            if (in3 != null)
            {
                saveObjectTable = in3.saveObjectTable();
                saveTraitsTable = in3.saveTraitsTable();
                saveStringTable = in3.saveStringTable();
            }

            this.defaultInstance = in.readObject();
        }
        finally
        {
            if (in3 != null)
            {
                in3.restoreObjectTable(saveObjectTable);
                in3.restoreTraitsTable(saveTraitsTable);
                in3.restoreStringTable(saveStringTable);
            }
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException
    {
        if (out instanceof Amf3Output)
            throw new UnsupportedOperationException("This method should not be used for AMF3 serialization.");
        
        // this method is used to serialize the proxy during cluster serialization    
        out.writeObject(this.defaultInstance);
    }

    public String toString()
    {
        return "[Proxy(" + defaultInstance + ") descriptor=" + descriptor + "]";
    }
}
