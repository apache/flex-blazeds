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
package flex.messaging.io;

import java.util.HashMap;
import java.util.List;

/**
 * The SerializationProxy uses this descriptor to determine which
 * fields and properties should be excluded from an object graph
 * on an instance-by-instance basis. By default, all public instance
 * variables and properties will be serialized.
 * <p>
 * If excludes need to be specified for a complex child property,
 * the property name is added to this dynamic descriptor and its value
 * set to another descriptor with its own set of excludes.
 * <p>
 * The absence of excludes implies default serialization. The absence of
 * a child property implies default serialization.
 *
 * @see flex.messaging.io.PropertyProxy
 */
public class SerializationDescriptor extends HashMap {
    static final long serialVersionUID = 1828426777611186569L;

    private List excludes;

    public SerializationDescriptor() {
        super();
    }

    public List getExcludesForInstance(Object instance) {
        return excludes;
    }

    /*
     * Deprecated in favor of getExcludesForInstance(instance).
     */
    public List getExcludes() {
        return excludes;
    }

    public void setExcludes(List excludes) {
        this.excludes = excludes;
    }

    public String toString() {
        return "[ excludes: " + excludes + "]";
    }
}
