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
 * Client status exceptions are thrown by the AMF connection when a client side
 * error is encountered such as when a connect or call attempt fails due to
 * wrong url on the client.
 */
public class ClientStatusException extends Exception
{
    private static final long serialVersionUID = 1412675397183129614L;

    /**
     * Exception codes.
     */
    public static final String AMF_CALL_FAILED_CODE = "AMFConnection.Call.Failed";
    public static final String AMF_CONNECT_FAILED_CODE = "AMFConnection.Connect.Failed";

    private String code;
    private HttpResponseInfo httpResponseInfo;

    /**
     * Creates a client status exception with the supplied throwable and code.
     *
     * @param t The throwable instance used to create the exception.
     * @param code The code of the exception.
     */
    public ClientStatusException(Throwable t, String code)
    {
        super(t);
        this.code = code;
    }

    /**
     * Creates a client status exception with the supplied message and code.
     *
     * @param message The message of the exception.
     * @param code The code of the exception.
     */
    public ClientStatusException(String message, String code)
    {
        super(message);
        this.code = code;
    }

    /**
     * Creates a client status exception with the supplied message, code,
     * and http response info.
     *
     * @param message The message of the exception.
     * @param code The code of the exception.
     * @param httpResponseInfo The HTTP response info object that represents
     * the HTTP response returned with the exception.
     */
    public ClientStatusException(String message, String code, HttpResponseInfo httpResponseInfo)
    {
        this(message, code);
        this.httpResponseInfo = httpResponseInfo;
    }

    /**
     * Creates a client status exception with the supplied message, code,
     * and http response info.
     *
     * @param t The throwable instance used to create the exception.
     * @param code The code of the exception.
     * @param httpResponseInfo The HTTP response info object that represents
     * the HTTP response returned with the exception.
     */
    public ClientStatusException(Throwable t, String code, HttpResponseInfo httpResponseInfo)
    {
        this(t, code);
        this.httpResponseInfo = httpResponseInfo;
    }

    /**
     * Returns the code of the exception.
     *
     * @return The code of the exception.
     */
    public String getCode()
    {
        return code;
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
        return "ClientStatusException "
        + "\n\tmessage: " + getMessage()
        + "\n\tcode: " + code;
    }
}
