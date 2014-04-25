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

import flex.messaging.Server;

/**
 * Extension interface for <tt>Endpoint</tt> that adds support for a 
 * referenced <tt>Server</tt> that the endpoint may use.
 */
public interface Endpoint2 extends Endpoint
{
   /**
    * Retrieves the <tt>Server</tt> that the endpoint is using; <code>null</code> if
    * no server has been assigned.
    */
   Server getServer();
   
   /**
    * Sets the <tt>Server</tt> that the endpoint will use.
    */
   void setServer(Server server);
}
