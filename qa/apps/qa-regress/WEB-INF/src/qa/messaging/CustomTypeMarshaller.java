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
package qa.messaging;

import flex.messaging.io.amf.translator.ASTranslator;
/**
 * The <code>CustomTypeMarshaller</code> class is used to test that 
 * if a channel specifies a type-marshaller in it's serialization settings
 * the class that is specified is used. This class doesn't actually do anything
 * special it just calls super on the methods that it overrides in <code>ASTranslator</code>.
 * This class is just used to make sure that if a type-marshaller is specified that it
 * gets used.
 */
public class CustomTypeMarshaller extends ASTranslator {
	/**
	 * Override createInstance in ASTranslator. Just pass params along 
	 * to the superclass.
	 */
	public Object createInstance(Object source, Class desiredClass)
    {
        
	    return super.createInstance(source, desiredClass);
      
    }
	/**
	 * Override convert in ASTranslator. Just pass params along 
	 * to the superclass.
	 */
    public Object convert(Object source, Class desiredClass)
    {
        return super.convert(source, desiredClass);
    }
}
