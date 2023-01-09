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
package flex.management.jmx;

import javax.management.MalformedObjectNameException;

/**
 * Remotable ObjectInstance representation that complies with Flash serialization requirements.
 */
public class ObjectInstance {
    /**
     * The object name part of the <code>ObjectInstance</code>.
     */
    public ObjectName objectName;

    /**
     * The class name part of the <code>ObjectInstance</code>.
     */
    public String className;

    /**
     * Constructs an empty <code>ObjectInstance</code> instance.
     */
    public ObjectInstance() {
    }

    /**
     * Constructs a <code>ObjectInstance</code> instance based upon a
     * <code>javax.management.ObjectInstance</code> instance.
     *
     * @param objectInstance The JMX <code>ObjectInstance</code> instance to base this instance on.
     */
    public ObjectInstance(javax.management.ObjectInstance objectInstance) {
        objectName = new ObjectName(objectInstance.getObjectName());
        className = objectInstance.getClassName();
    }

    /**
     * Utility method to convert this <code>ObjectInstance</code> to a
     * <code>javax.management.ObjectInstance</code> instance.
     *
     * @return A JMX <code>ObjectInstance</code> based upon this instance.
     * @throws MalformedObjectNameException an exception
     */
    public javax.management.ObjectInstance toObjectInstance() throws MalformedObjectNameException {
        return new javax.management.ObjectInstance(objectName.toObjectName(), className);
    }
}
