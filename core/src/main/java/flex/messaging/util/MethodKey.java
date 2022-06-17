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
package flex.messaging.util;

import java.util.Arrays;

/**
 * Utility class used as a key into collections of Remote Object methods.  The
 * key consists of the Remote Object class containing this method, the method name,
 * and the classes representing the parameters in the signature of this method.
 */
public class MethodKey {
    private Class enclosingClass;
    private String methodName;
    private Class[] parameterTypes;

    /**
     * Constructor.
     *
     * @param enclosingClass remote Ooject class containing this method
     * @param methodName     method name
     * @param parameterTypes classes representing the parameters in the signature of this method
     */
    public MethodKey(Class enclosingClass, String methodName, Class[] parameterTypes) {
        this.enclosingClass = enclosingClass;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object object) {
        boolean result;
        if (this == object) {
            result = true;
        } else if (object instanceof MethodKey) {
            MethodKey other = (MethodKey) object;
            result =
                    other.methodName.equals(this.methodName) &&
                            other.parameterTypes.length == this.parameterTypes.length &&
                            other.enclosingClass == this.enclosingClass &&
                            Arrays.equals(other.parameterTypes, this.parameterTypes);
        } else {
            result = false;
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        // Don't consider parameter types in hashcode to speed up
        // calculation.
        return enclosingClass.hashCode() * 10003 +
                methodName.hashCode();
    }
}
