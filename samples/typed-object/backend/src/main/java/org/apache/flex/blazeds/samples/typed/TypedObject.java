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
package org.apache.flex.blazeds.samples.typed;

import java.io.Serializable;

public class TypedObject implements Serializable {
    
    static final long serialVersionUID = 1L;
    
    private String str;
    private boolean bool;
    private double num;
    
    public TypedObject() {
        
    }
    
    public TypedObject(String str, boolean bool, double num) {
        this.str = str;
        this.bool = bool;
        this.num = num;
    }

    public String getString() {
        return str;
    }
    public void setString(String str) {
        this.str = str;
    }

    public boolean getBoolean() {
        return bool;
    }
    public void setBoolean(boolean bool) {
        this.bool = bool;
    }

    public double getNumber() {
        return num;
    }
    public void setNumber(double num) {
        this.num = num;
    }
}
