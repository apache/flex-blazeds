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

package flex.messaging.io.amf.client.exceptions;
import flex.messaging.io.amf.client.AMFConnection.HttpResponseInfo;

/**
 * Server status exceptions are thrown by the AMF connection when a server side
 * error is encountered.
 */
public class ServerStatusException extends Exception
{
    private static final long serialVersionUID = -5441048669770997132L;

    private Object data;
    private HttpResponseInfo httpResponseInfo;

    /**
     * Creates a server status exception with the supplied message and data.
     *
     * @param message The message of the exception.
     * @param data The data of the exception which is usually an AMF result or
     * status message.
     */
    public ServerStatusException(String message, Object data)
    {
        this(message, data, null);
    }

    /**
     * Creates a server status exception with the supplied message, data, and
     * HTTP response info object.
     *
     * @param message The message of the exception.
     * @param data The data of the exception which is usually an AMF result or
     * status message.
     * @param httpResponseInfo The HTTP response info object that represents
     * the HTTP response returned with the exception.
     */
    public ServerStatusException(String message, Object data, HttpResponseInfo httpResponseInfo)
    {
        super(message);
        this.data = data;
        this.httpResponseInfo = httpResponseInfo;
    }

    /**
     * Returns the data of the exception.
     *
     * @return The data of the exception.
     */
    public Object getData()
    {
        return data;
    }

    /**
     * Returns the HTTP response info of the exception.
     *
     * @return The HTTP response info of the exception.
     */
    public HttpResponseInfo getHttpResponseInfo()
    {
        return httpResponseInfo;
    }

    /**
     * Returns a String representation of the exception.
     *
     * @return A String that represents the exception.
     */
    @Override
    public String toString()
    {
        String temp = "ServerStatusException " + "\n\tdata: " + data;
        if (httpResponseInfo != null)
            temp += "\n\tHttpResponseInfo: " + httpResponseInfo;
        return temp;
    }
}
