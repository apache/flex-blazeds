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

import flex.messaging.util.ClassUtil;
import flex.messaging.io.PropertyProxy;
import flex.messaging.io.PropertyProxyRegistry;
import flex.messaging.io.TypeMarshallingContext;
import flex.messaging.io.amf.translator.TranslationException;

/**
 * Decodes an ASObject to a Java object based on the
 * type information returned from the ASObject.getType().
 *
 * If the TranslationContext has been set up to support
 * _remoteClass then this property may be used as a back up.
 *
 * @exclude
 */
public class TypedObjectDecoder extends ActionScriptDecoder
{
    public boolean hasShell()
    {
        return true;
    }

    public Object createShell(Object encodedObject, Class desiredClass)
    {
        Object shell = null;

        Class cls;
        String type = TypeMarshallingContext.getType(encodedObject);

        if (type != null)
        {
            TypeMarshallingContext context = TypeMarshallingContext.getTypeMarshallingContext();
             cls = ClassUtil.createClass(type, context.getClassLoader());
        }
        else
        {
            cls = desiredClass;
        }

        shell = ClassUtil.createDefaultInstance(cls, null);

        return shell;
    }

    public Object decodeObject(Object shell, Object encodedObject, Class desiredClass)
    {
        Object bean = shell;
        if (bean == null)
            return null;

        return decodeTypedObject(bean, encodedObject);
    }

    protected Object decodeTypedObject(Object bean, Object encodedObject)
    {
        PropertyProxy beanProxy = PropertyProxyRegistry.getProxyAndRegister(bean);
        PropertyProxy encodedProxy = PropertyProxyRegistry.getProxyAndRegister(encodedObject);

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
                        // We may need to honor our loose-typing rules for individual types as,
                        // unlike a Collection, an Array has a fixed element type. We'll use our handy
                        // decoder suite again to find us the right decoder...
                        ActionScriptDecoder decoder = DecoderFactory.getDecoder(value, wClass);
                        decodedObject = decoder.decodeObject(value, wClass);
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
