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

package blazeds.qa.remotingService.inherit;

import java.io.Serializable;

public class ChildClass extends ParentClass implements Serializable{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public static String childClassClassProp = "childClassClassProp";
    public String childClassOnlyProp = "childClassOnlyProp";
    public String overridedPropByProp = "childClassOverridedPropByProp";
    public String overridedGetterByProp = "childClassOverridedGetterByProp";
    private String childClassPrivateProp = "childClassPrivateProp";
    protected String childClassProtectedProp = "childClassProtectedProp";
    
    public String getOverridedPropByGetter() {
        return "childClassOverridedPropByGetter";
    }
    public void setOverridedPropByGetter(String s) {

    }


    public String getOverridedGetterByGetter() {
        return "childClassOverridedGetterByGetter";
    }
    public void setOverridedGetterByGetter(String s) {

    }

    public static ChildClass getChildClass() {
        return new ChildClass();
    }

    public static ParentClass getParentClass() {
        return new ParentClass();
    }
    
    public String toString() {
    	return "ChildClass: " + 
	    	"childClassClassProp = " + childClassClassProp + "\n" +
	    	"childClassOnlyProp = " + childClassOnlyProp + "\n" +
	    	"overridedPropByProp = " + overridedPropByProp + "\n" +
	    	"overridedGetterByProp = " + overridedGetterByProp + "\n" +
    		"childClassPrivateProp = " + childClassPrivateProp + "\n" +
    		"childClassProtectedProp = " + childClassProtectedProp;    		

    }
}
