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
package flex.messaging.endpoints;

/**
 * Secure version of StreamingHTTPEndpoint.
 */
public class SecureStreamingHTTPEndpoint extends StreamingHTTPEndpoint
{
    //--------------------------------------------------------------------------
    //
    // Constructors
    //
    //--------------------------------------------------------------------------

    /**
     * Constructs an unmanaged <code>SecureStreamingHTTPEndpoint</code>.
     */
    public SecureStreamingHTTPEndpoint()
    {
        this(false);
    }

    /**
     * Constructs a <code>SecureStreamingHTTPEndpoint</code> with the indicated management.
     *
     * @param enableManagement <code>true</code> if the <code>SecureHTTPEndpoint</code>
     * is manageable; <code>false</code> otherwise.
     */
    public SecureStreamingHTTPEndpoint(boolean enableManagement)
    {
        super(enableManagement);
    }

    //--------------------------------------------------------------------------
    //
    // Public Methods
    //
    //--------------------------------------------------------------------------

    /**
     * Determines whether the endpoint is secure.
     *
     * @return <code>true</code> if the endpoint is secure, <code>false</code> otherwise.
     */
    public boolean isSecure()
    {
        return true;
    }
}
