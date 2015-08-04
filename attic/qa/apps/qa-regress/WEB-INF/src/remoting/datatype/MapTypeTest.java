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

package remoting.datatype;

import java.util.*;

public class MapTypeTest {

	private HashMap mapStringKey;
	private HashMap mapIntegerKey; 
	
	public MapTypeTest(){	
		mapStringKey = new HashMap(); 
		mapStringKey.put("one", "value1");
		mapStringKey.put("two", "value2");
		
		mapIntegerKey = new HashMap();
		mapIntegerKey.put(new Integer(1), "value1");
		mapIntegerKey.put(new Integer(2), "value2");
	}
		
	public Map getMapStringKey(){
		return mapStringKey; 
	}
	
	public Map getMapIntegerKey() {
		return mapIntegerKey;
	}	
}
