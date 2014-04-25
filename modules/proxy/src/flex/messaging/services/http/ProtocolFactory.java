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
package flex.messaging.services.http;

import org.apache.commons.httpclient.protocol.Protocol;

import flex.messaging.FlexConfigurable;

/**
 * Implementations of the ProtocolFactory interface allow the developer to
 * customize how the HTTP Proxy Service communicates with a 3rd party endpoint.
 * ProtocolFactory extends FlexConfigurable to allow for properties to be
 * provided directly in the services configuration.
 * <p>
 * An example of a custom protocol might be to provide client certificates
 * for two-way SSL authentication for a specific destination.
 * </p>
 * <p>
 * Implementations of this interface must provide a default, no-args
 * constructor.
 * </p>
 * 
 * @author Peter Farland
 */
public interface ProtocolFactory extends FlexConfigurable
{
    /**
     * Returns a custom implementation of Apache Commons
     * HTTPClient's Protocol interface.
     * 
     * @return An implementation of org.apache.commons.httpclient.protocol.Protocol.
     */
    Protocol getProtocol();
}
