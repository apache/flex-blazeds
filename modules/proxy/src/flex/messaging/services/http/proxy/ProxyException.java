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
package flex.messaging.services.http.proxy;

import flex.messaging.MessageException;

/**
 *
 * Simple exception used to get back to ErrorFilter from other filters.
 */
public class ProxyException extends MessageException
{
    static final long serialVersionUID = -6516172702871227717L;

    public static final String CODE_SERVER_PROXY_REQUEST_FAILED = "Server.Proxy.Request.Failed";

    //--------------------------------------------------------------------------
    //
    // Constructors
    //
    //--------------------------------------------------------------------------

    /**
     * Default constructor.
     */
    public ProxyException()
    {
        super();
        super.setCode(CODE_SERVER_PROXY_REQUEST_FAILED);
    }

    /**
     * Constructor with a message.
     *
     * @param message The detailed message for the exception.
     */
    public ProxyException(int message)
    {
        this();
        setMessage(message);
    }
}