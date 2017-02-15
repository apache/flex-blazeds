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
package flex.messaging.io.amf.translator;

import flex.messaging.io.SerializationContext;
import flex.messaging.io.TypeMarshaller;
import flex.messaging.io.amf.translator.decoder.ActionScriptDecoder;
import flex.messaging.io.amf.translator.decoder.DecoderFactory;
import flex.messaging.util.ClassUtil;
import flex.messaging.util.Trace;

/**
 * ASTranslator provides the ability to convert between ASObjects used by
 * Flex and Java objects in your application.
 */
public class ASTranslator implements TypeMarshaller
{
    /** {@inheritDoc} */
    public Object createInstance(Object source, Class desiredClass)
    {
        ActionScriptDecoder decoder = DecoderFactory.getDecoderForShell(desiredClass);

        Object instance = null;
        if (decoder.hasShell())
        {
            instance = decoder.createShell(source, desiredClass);
        }
        else
        {
            instance = ClassUtil.createDefaultInstance(desiredClass, null);
        }

        return instance;
    }

    /**
     * Translate an object to another object of type class.
     * obj types should be ASObject, Boolean, String, Double, Date, ArrayList
     */
    public Object convert(Object source, Class desiredClass)
    {
        if (source == null && !desiredClass.isPrimitive())
        {
            return null;
        }

        SerializationContext serializationContext = SerializationContext.getSerializationContext();

        ActionScriptDecoder decoder;
        if (serializationContext.restoreReferences)
            decoder = DecoderFactory.getReferenceAwareDecoder(source, desiredClass);
        else
            decoder = DecoderFactory.getDecoder(source, desiredClass);

        if (Trace.remote)
        {
            Trace.trace("Decoder for " + (source == null ? "null" : source.getClass().toString()) +
                    " with desired " + desiredClass + " is " + decoder.getClass());
        }

        Object result = decoder.decodeObject(source, desiredClass);
        return result;
    }
}