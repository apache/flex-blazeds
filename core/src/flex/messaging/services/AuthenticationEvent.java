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
package flex.messaging.services;

import flex.messaging.FlexSession;
import flex.messaging.client.FlexClient;
import java.security.Principal;
import java.util.EventObject;

/**
 * An event that indicates a user has either logged in or logged out successfully.
 * The event object provides access to the <tt>AuthenticationService</tt> that handled the
 * login or logout, which is the event source, as well as the <tt>Principal</tt>, <tt>FlexSession</tt>,
 * and <tt>FlexClient</tt> for the user. Following a logout, these objects may have been invalidated
 * so exercise caution with accessing and using them.
 */
public class AuthenticationEvent extends EventObject
{
    private static final long serialVersionUID = 6002063582698638736L;

    //--------------------------------------------------------------------------
    //
    // Constructor
    //
    //--------------------------------------------------------------------------

    /**
     * Constructs an <tt>AuthenticationEvent</tt>.
     * 
     * @param source The <tt>AuthenticationService</tt> dispatching this event.
     * @param username The username used to authenticate
     * @param credentials The password or secret used to authenticate.
     * @param principal The user's <tt>Principal</tt>.
     * @param flexSession The user's <tt>FlexSession</tt>.
     * @param flexClient The user's <tt>FlexClient</tt>.
     */
    public AuthenticationEvent(final AuthenticationService source, final String username, final Object credentials, final Principal principal, final FlexSession flexSession, final FlexClient flexClient)
    {
        super(source);
        this.username = username;
        this.credentials = credentials;
        this.principal = principal;
        this.flexSession = flexSession;
        this.flexClient = flexClient;
    }

    //--------------------------------------------------------------------------
    //
    // Properties
    //
    //--------------------------------------------------------------------------

    //----------------------------------
    //  credentials
    //----------------------------------    
    
    private final Object credentials;
    
    /**
     * Returns the credentials used for authentication, <code>null</code> for logout events.
     * 
     * @return The credentials used for authentication, <code>null</code> for logout events.
     */
    public Object getCredentials()
    {
        return credentials;
    }
    
    //----------------------------------
    //  flexClient
    //----------------------------------

    private final FlexClient flexClient;
    
    /**
     * Returns the <tt>FlexClient</tt> associated with this event.
     * 
     * @return The <tt>FlexClient</tt> associated with this event.
     */
    public FlexClient getFlexClient()
    {
        return flexClient;
    }

    //----------------------------------
    //  flexSession
    //----------------------------------

    private final FlexSession flexSession;
    
    /**
     * Returns the <tt>FlexSession</tt> associated with this event.
     * 
     * @return The <tt>FlexSession</tt> associated with this event.
     */
    public FlexSession getFlexSession()
    {
        return flexSession;
    }    
    
    //----------------------------------
    //  principal
    //----------------------------------

    private final Principal principal;
    
    /**
     * Returns the <tt>Principal</tt> associated with this event.
     * 
     * @return The <tt>Principal</tt> associated with this event.
     */
    public Principal getPrincipal()
    {
        return principal;
    }
    
    //----------------------------------
    //  source
    //----------------------------------
    
    public AuthenticationService getSource()
    {
        return AuthenticationService.class.cast(super.getSource());
    }
    
    //----------------------------------
    //  username
    //----------------------------------
    
    private String username;
    
    /**
     * Returns the username for authentication, <code>null</code> for logout events.
     * 
     * @return The username for authentication, <code>null</code> for logout events.
     */
    public String getUsername()
    {
        return username;
    }
}
