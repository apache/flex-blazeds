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

import java.util.EventObject;

/**
 * An event dispatched when the connection state for a session changes.
 */
public class FlexSessionConnectivityEvent extends EventObject
{
    /**
     * @exclude
     */
    private static final long serialVersionUID = 8622680412552475829L;

    /**
     * Constructs a new <tt>FlexSessionConnectivityEvent</tt> using the supplied source <tt>ConnectionAwareSession</tt>.
     * 
     * @param session The session whose connection state has changed.
     */
    public FlexSessionConnectivityEvent(ConnectionAwareSession session)
    {
        super(session);
    }
    
    /**
     * Returns the session whose connection state has changed.
     * 
     * @return The session whose connection state has changed.
     */
    public ConnectionAwareSession getFlexSession()
    {
        return (ConnectionAwareSession)getSource();
    }
}
