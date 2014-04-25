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

import java.util.Iterator;
import java.util.List;

import flex.messaging.io.PropertyProxy;
import flex.messaging.io.PropertyProxyRegistry;
import flex.messaging.io.TypeMarshallingContext;

import flex.messaging.io.amf.translator.TranslationException;

/**
 * @exclude
 */
public class ReferenceAwareTypedObjectDecoder extends TypedObjectDecoder
{
    protected Object decodeTypedObject(Object bean, Object encodedObject)
    {
        TypeMarshallingContext context = TypeMarshallingContext.getTypeMarshallingContext();
        context.getKnownObjects().put(encodedObject, bean);

        PropertyProxy beanProxy = PropertyProxyRegistry.getProxy(bean);
        PropertyProxy encodedProxy = PropertyProxyRegistry.getProxy(encodedObject);

        List propertyNames = beanProxy.getPropertyNames(bean);
        if (propertyNames != null)
        {
            Iterator it = propertyNames.iterator();
            while (it.hasNext())
            {
                String propName = (String)it.next();

                Class wClass = beanProxy.getType(bean, propName);

                // get property value from encodedObject
                Object value = encodedProxy.getValue(encodedObject, propName);

                Object decodedObject = null;
                try
                {
                    if (value != null)
                    {
                        //Check whether we need to restore a client
                        //side reference to a known object
                        Object ref = null;
    
                        if (canUseByReference(value))
                            ref = context.getKnownObjects().get(value);
    
                        if (ref == null)
                        {
                            ActionScriptDecoder decoder = DecoderFactory.getReferenceAwareDecoder(value, wClass);
                            decodedObject = decoder.decodeObject(value, wClass);
    
                            if (canUseByReference(decodedObject))
                            {
                                context.getKnownObjects().put(value, decodedObject);
                            }
                        }
                        else
                        {
                            decodedObject = ref;
                        }
                    }
    
                    // TODO: Perhaps we could update NumberDecoder, CharacterDecoder and
                    // BooleanDecoder to do this for us?
                    if (decodedObject == null && wClass.isPrimitive())
                    {
                        decodedObject = getDefaultPrimitiveValue(wClass);
                    }
    
                    beanProxy.setValue(bean, propName, decodedObject);
                }
                catch (Exception e)
                {
                    TranslationException ex = new TranslationException("Could not set object " + decodedObject + " on " + bean.getClass() + "'s " + propName);
                    ex.setCode("Server.Processing");
                    ex.setRootCause(e);
                    throw ex;
                }
            }
        }

        return bean;
    }


}
