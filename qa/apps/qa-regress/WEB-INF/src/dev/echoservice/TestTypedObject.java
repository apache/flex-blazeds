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

package dev.echoservice;

import java.util.Collection;
import java.util.HashMap;
import java.io.Serializable;

public class TestTypedObject implements Serializable {
    /**
     * Simple typed object
     */
    private static final long serialVersionUID = 1L;
    public Collection theCollection;
    public HashMap map;
    public TestTypedObject me;
    private Object _prop1;
    //public transient double myNo = 0.2;
    private String _prop2="b";

    public TestTypedObject() {
        System.out.println("Constructor Call................\n\n\n\n");
    }
    public void setProp1(Object p) {
        _prop1 = p;
    }
    public Object getProp1() {
        return _prop1;
    }

    public void setProp2(String p) {
        _prop2 = p;
    }
    public String getProp2() {
        return _prop2;
    }
    public Object getReadOnlyProp1() {
        return "This is a ServerSide ReadOnly Property";
    }
}
