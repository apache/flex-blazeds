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

import flex.messaging.io.PropertyProxyRegistry;
import java.util.*;

public class EnumTypeTest {
	EnumType etype;

	public EnumTypeTest(){	
		PropertyProxyRegistry registry = PropertyProxyRegistry.getRegistry();
		registry.register(EnumType.class, new EnumProxy());
	}
	
	public EnumTypeTest(EnumType etype) {
		this.etype = etype;
	}
	
	public void tellColor() {
		switch (etype) {
			case APPLE: System.out.println("Apple is red.");
					     break;					
			case ORANGE: System.out.println("Orange is orange.");
					     break;					     
			case BANANA: System.out.println("Banana is yellow.");
					     break;					     
			default:	 System.out.println("No fruit?");
					     break;
		}
	}
	
	public static void main(String[] args) {
		EnumTypeTest apple = new EnumTypeTest(EnumType.APPLE);
		apple.tellColor();
		EnumTypeTest orange = new EnumTypeTest(EnumType.ORANGE);
		orange.tellColor();
		EnumTypeTest banana = new EnumTypeTest(EnumType.BANANA);
		banana.tellColor();		
		System.out.println(  "EnumType.APPLE=" + banana.echoEnum(EnumType.APPLE) + " EnumType.Apple.value=" +                                       banana.echoEnum(EnumType.APPLE).value );
		System.out.println(  "EnumType.APPLE=" + banana.getEnum("APPLE") + " EnumType.Apple.value=" + banana.getEnum("APPLE").value                                     );
		banana.getApplePrice();
	}

	public EnumType getEnum(String type){
		
		if (type.toUpperCase().equals("APPLE")){		
			 return EnumType.APPLE;			
		} else if (type.toUpperCase().equals("ORANGE")){
			return EnumType.ORANGE;
		} else {
			return EnumType.BANANA;
		}	
	}

	public EnumType echoEnum(EnumType type){
		switch (type) {
			case APPLE: return EnumType.APPLE;					    				
			case ORANGE: return EnumType.ORANGE;
			default:	return EnumType.BANANA;
		}	
	}

	public void getApplePrice() {			
		System.out.println("Apple " + getEnumApple() + " costs " + getEnumApple().getPrice());

		// Display all Apples and prices.
		System.out.println("All apple prices:");
		for (EnumApple a : EnumApple.values())
		  System.out.println(a + " costs " + a.getPrice() + " cents.");
	 }

	 public EnumApple getEnumApple(){
		return EnumApple.A;
	 }
         
         public Map enumKeyMap() {
             Map map = new HashMap();
             map.put(EnumApple.A,  "AppleA");
             map.put(EnumApple.B, "AppleB");
             return map;
         }
}
