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

package remoting.amfclient;

/**
 * A class used as a remoting destination source for AMF connection JUnit tests.
 */
 import java.util.Date;
 
public class AMFConnectionTestService
{
    public String echoString(String text)
    {
        return text;
    }

    public int echoInt(int value)
    {
        return value;
    }

    public boolean echoBoolean(boolean value)
    {
        return value;
    }

    public Date echoDate(Date value)
    {
        return value;
    }

    public short echoShort(short value)
    {
        return value;
    }

    public double echoDouble(double value)
    {
        return value;
    }

    // Object argument, Object return.
    public Object echoObject1(Object customType)
    {
        return customType;
    }
    
    // Object argument, CustomType return.
    public ServerCustomType echoObject2(Object customType)
    {
        return (ServerCustomType)customType;
    }
    
    // CustomType argument, Object return
    public Object echoObject3(ServerCustomType customType)
    {
        return customType;
    }

    // CustomType argument, CustomType return
    public ServerCustomType echoObject4(ServerCustomType customType)
    {
        return customType;
    }

    // Object argument, Object return.
    public Object[] echoObject5(Object[] customType)
    {
        return customType;
    }

    // No argument, Object return
    public Object getObject1()
    {
        ServerCustomType ct = new ServerCustomType();
        ct.setId(1);
        return ct;
    }

    // No argument, CustomType return.
    public ServerCustomType getObject2()
    {
        ServerCustomType ct = new ServerCustomType();
        ct.setId(1);
        return ct;
    }
    
    // No argument, an Array of Objects.
    public Object[] getObjectArray1()
    {
        Object[] customTypes = new Object[10];
        for (int i = 0; i < customTypes.length; i++)
        {
            ServerCustomType sct = new ServerCustomType();
            sct.setId(i);
            customTypes[i] = sct;
        }
        return customTypes;
    }
/*
    public Document echoXML(Document d)
    {
        return d;
    }
    */
}
