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

package dev.remoting.inherit;

import java.io.Serializable;

public class ParentClass implements Serializable{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public String inheritedProp = "parentClassInheritedProp";
    public String overridedPropByProp = "parentClassOverridedPropByProp";
    public String overridedPropByGetter = "parentClassOverridedPropByGetter";
    public static String parentClassClassProp = "parentClassClassProp";
    private String parentClassPrivateProp = "parentClassPrivateProp";
    protected String parentCleassProtectedProp;

    public String getInheritedPropGetter() {
        return "parentClassInheritedPropGetter";
    }
    public void setInheritedPropGetter(String s) {

    }

    public String getOverridedGetterByGetter() {
        return "parentClassOverridedGetterByGetter";
    }
    public void setOverridedGetterByGetter(String s) {

    }

    public String getOverridedGetterByProp() {
        return "parentClassOverridedGetterByProp";
    }
    public void setOverridedGetterByProp(String s) {

    }
    
    public String toString() {
    	return "Parent Class: " + 
	    	"inheritedProp = " + inheritedProp + "\n" +
	    	"overridedPropByProp = " + overridedPropByProp + "\n" +
	    	"overridedPropByGetter = " + overridedPropByGetter + "\n" +
	    	"parentClassClassProp = " + parentClassClassProp + "\n" +
	    	"parentClassPrivateProp = " + parentClassPrivateProp + "\n" +
	    	"parentCleassProtectedProp = " + parentCleassProtectedProp + "\n";
    }
}
