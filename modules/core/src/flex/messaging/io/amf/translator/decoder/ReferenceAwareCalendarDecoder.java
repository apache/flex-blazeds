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
package flex.messaging.io.amf.translator.decoder;

import flex.messaging.io.SerializationContext;
import flex.messaging.io.TypeMarshallingContext;

/**
 * @exclude
 */
public class ReferenceAwareCalendarDecoder extends CalendarDecoder
{
    public Object decodeObject(Object shell, Object encodedObject, Class desiredClass)
    {
        Object result = super.decodeObject(shell, encodedObject, desiredClass);

        // Only AMF 3 Dates can be sent by reference so we only
        // need to remember this translation to re-establish pointers
        // to the encodedObject if the incoming type was a Date object.
        if (result != null
                && SerializationContext.getSerializationContext().supportDatesByReference
                && encodedObject instanceof java.util.Date)
        {
            TypeMarshallingContext context = TypeMarshallingContext.getTypeMarshallingContext();
            context.getKnownObjects().put(encodedObject, result);
        }

        return result;
    }
}
