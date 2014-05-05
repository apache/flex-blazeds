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

import flex.messaging.io.amf.ActionMessage;
import flex.messaging.io.amf.ActionContext;
import flex.messaging.io.amf.AmfTrace;

import java.io.IOException;
import java.io.InputStream;

/**
 * An interface to allow for either AMF or AMFX based deserializers
 * to process requests.
 *
 * @author Peter Farland
 */
public interface MessageDeserializer
{
    void initialize(SerializationContext context, InputStream in, AmfTrace trace);

    void readMessage(ActionMessage m, ActionContext context) throws ClassNotFoundException, IOException;

    Object readObject() throws ClassNotFoundException, IOException;
}
