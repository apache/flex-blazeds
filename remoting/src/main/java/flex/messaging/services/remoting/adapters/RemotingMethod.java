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
package flex.messaging.services.remoting.adapters;

import flex.messaging.config.SecurityConstraint;

/**
 * Used to define included and excluded methods exposed by the <tt>JavaAdapter</tt>
 * for a remoting destination.
 * This class performs no internal synchronization.
 */
public class RemotingMethod {
    //--------------------------------------------------------------------------
    //
    // Properties
    //         
    //-------------------------------------------------------------------------- 

    //----------------------------------
    //  name
    //----------------------------------

    private String name;

    /**
     * Returns the method name.
     * Because mapping ActionScript data types to Java data types is indeterminate
     * in some cases, explicit overloaded methods are not currently supported so no
     * parameter signature property is defined.
     *
     * @return method name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the method name.
     * Because mapping ActionScript data types to Java data types is indeterminate
     * in some cases, explicit overloaded methods are not currently supported so no
     * parameter signature property is defined.
     *
     * @param value method name
     */
    public void setName(String value) {
        name = value;
    }

    //----------------------------------
    //  securityConstraint
    //----------------------------------

    private SecurityConstraint constraint;

    /**
     * Returns the <tt>SecurityConstraint</tt> that will be applied to invocations
     * of the remoting method.
     *
     * @return <tt>SecurityConstraint</tt> that will be applied to invocations
     * of the remoting method.
     */
    public SecurityConstraint getSecurityConstraint() {
        return constraint;
    }

    /**
     * Sets the <tt>SecurityConstraint</tt> that will be applied to invocations of
     * the remoting method.
     *
     * @param value the <tt>SecurityConstraint</tt> that will be applied to invocations of
     *              the remoting method.
     */
    public void setSecurityConstraint(SecurityConstraint value) {
        constraint = value;
    }
}