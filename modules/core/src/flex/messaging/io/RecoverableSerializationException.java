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

/**
 * This exception class should be used by the deserializers to indicate
 * that a non fatal exception occurred during serialization.
 * The exception is such that the message body can still be created
 * and the message can be processed in the usual stream.
 * The BatchProcessFilter will add an error message to the response
 * of any messages which have a recoverable serialization exception.
 */
public class RecoverableSerializationException extends SerializationException
{
    static final long serialVersionUID = 2671402324412964558L;

    public RecoverableSerializationException()
    {
        super();
    }
}
