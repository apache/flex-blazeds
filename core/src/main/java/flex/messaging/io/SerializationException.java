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
package flex.messaging.io;

import flex.messaging.MessageException;

/**
 * Typically signifies that a fatal exception happened during deserialization or
 * serialization. The messaging framework should try to get a meaningful
 * message back to the client in a response, however this is not always possible,
 * especially for batched AMF messages, so at the very least the error should be
 * logged.
 * <p>
 * A special sub-class RecoverableSerializationException can be thrown for non-fatal
 * serialization exceptions.
 *
 * @see flex.messaging.io.RecoverableSerializationException
 */
public class SerializationException extends MessageException {
    static final long serialVersionUID = -5723542920189973518L;

    public static final String CLIENT_PACKET_ENCODING = "Client.Packet.Encoding";

    public SerializationException() {
        setCode(CLIENT_PACKET_ENCODING);
    }
}
