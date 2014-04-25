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
 * Sessions that directly track their connection state support notifying interested
 * listeners of connectivity changes.
 */
public interface ConnectionAwareSession
{
    //----------------------------------
    //  connected
    //----------------------------------

    /**
     * Returns true if the session is connected; otherwise false.
     * 
     * @return true if the session is connected; otherwise false.
     */
    boolean isConnected();
    
    /**
     * Sets the connected state for the session.
     * 
     * @param value true if the session is connected; false if disconnected.
     */
    void setConnected(boolean value);
    
    //----------------------------------
    //  connectivityListeners
    //----------------------------------
    
    /**
     * Registers a session connectivity listener with the session.
     * This listener will be notified when the session acquires or looses connectivity
     * to the remote host.
     * 
     * @param listener The <tt>FlexSessionConnectivityListener</tt> to register with the session.
     */
    void addConnectivityListener(FlexSessionConnectivityListener listener);
    
    /**
     * Unregisters a session connectivity listener from the session.
     * The unregistered listener will no longer be notified of session connectivity changes.
     * 
     * @param listener The <tt>FlexSessionConnectivityListener</tt> to unregister from the session.
     */
    void removeConnectivityListener(FlexSessionConnectivityListener listener);
}
