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

package flex.messaging;

/**
 * A wrapper object used for holding onto remote credentials.  When you are using
 * the proxy service, the remote credentials are used for authenticating against
 * the proxy server.  The remote credentials are distinct from the local credentials
 * used to authenticate against the local server.  You use this class along with
 * the FlexSession methods getRemoteCredentials and putRemoteCredentials to associate
 * the remote credentials with a specific destination.
 */
public class FlexRemoteCredentials
{
    private String service;

    private String destination;

    private String username;

    private Object credentials;

    /**
     * Normally you do not have to create the FlexRemoteCredentials as they are
     * created automatically when the client specifies them via the setRemoteCredentials
     * method in ActionScript.  You'd use this if you wanted to set your remote credentials
     * on the server and not have them specified on the client.
     * @param service the service id
     * @param destination the destination id
     * @param username the user name
     * @param credentials the user credentials
     */
    public FlexRemoteCredentials(String service, String destination, 
            String username, Object credentials)
    {
        super();
        this.service = service;
        this.destination = destination;
        this.username = username;
        this.credentials = credentials;
    }

    /**
     * Returns the user name from the remote credentials.
     * @return String the user name
     */
    public String getUsername()
    {
        return username;
    }

    /**
     * Returns the credentials themselves (usually a password).
     * @return Object the credentials object
     */
    public Object getCredentials()
    {
        return credentials;
    }

    /**
     * Returns the id of the service these credentials are registered for.
     * @return String the service id
     */
    public String getService()
    {
        return service;
    }

    /**
     * Returns the destination for the service.
     * @return String the destination id
     */
    public String getDestination()
    {
        return destination;
    }
}
