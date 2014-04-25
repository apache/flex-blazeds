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

import java.io.Serializable;
import java.util.ArrayList;


public class ActionMessage implements Serializable
{
    static final long serialVersionUID = 7970778672727624188L;

    public static final int CURRENT_VERSION = 3;

    private int version;

    private ArrayList headers = null;

    private ArrayList bodies = null;


    public ActionMessage()
    {
        version = CURRENT_VERSION;
        headers = new ArrayList();
        bodies = new ArrayList();
    }


    public ActionMessage(int version)
    {
        this.version = version;
        headers = new ArrayList();
        bodies = new ArrayList();
    }


    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }


    public int getHeaderCount()
    {
        return headers.size();
    }


    public MessageHeader getHeader(int pos)
    {
        return (MessageHeader)headers.get(pos);
    }

    public ArrayList getHeaders()
    {
        return headers;
    }

    public void addHeader(MessageHeader h)
    {
        headers.add(h);
    }


    public int getBodyCount()
    {
        return bodies.size();
    }


    public MessageBody getBody(int pos)
    {
        return (MessageBody)bodies.get(pos);
    }


    public ArrayList getBodies()
    {
        return bodies;
    }

    public void addBody(MessageBody b)
    {
        bodies.add(b);
    }
}

