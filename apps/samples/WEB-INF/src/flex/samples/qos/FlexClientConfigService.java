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
package flex.samples.qos;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import flex.messaging.FlexContext;
import flex.messaging.client.FlexClient;

public class FlexClientConfigService 
{

	public void setAttribute(String name, Object value) 
	{
		FlexClient flexClient = FlexContext.getFlexClient();
		flexClient.setAttribute(name, value);
	}

	public List getAttributes() 
	{
		FlexClient flexClient = FlexContext.getFlexClient();
		List attributes = new ArrayList();
		Enumeration attrNames = flexClient.getAttributeNames();
		while (attrNames.hasMoreElements())
		{
			String attrName = (String) attrNames.nextElement();
			attributes.add(new Attribute(attrName, flexClient.getAttribute(attrName)));
		}

		return attributes;
		
	}
	
	public class Attribute {
		
		private String name;
		private Object value;

		public Attribute(String name, Object value) {
			this.name = name;
			this.value = value;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public Object getValue() {
			return value;
		}
		public void setValue(Object value) {
			this.value = value;
		}
		
	}
	
}
