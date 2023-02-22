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
package flex.messaging.services.http;

import flex.messaging.messages.Message;
import flex.messaging.services.ServiceException;

/**
 * A Soap specific subclass of HttpProxyAdapter to
 * allow for future web services features.
 */
public class SOAPProxyAdapter extends HTTPProxyAdapter {

    //--------------------------------------------------------------------------
    //
    // Constructor
    //
    //--------------------------------------------------------------------------

    /**
     * Constructs an unmanaged <code>SOAPProxyAdapter</code> instance.
     */
    public SOAPProxyAdapter() {
        this(false);
    }

    /**
     * Constructs a <code>SOAPProxyAdapter</code> instance.
     *
     * @param enableManagement <code>true</code> if the <code>SOAPProxyAdapter</code> has a
     *                         corresponding MBean control for management; otherwise <code>false</code>.
     */
    public SOAPProxyAdapter(boolean enableManagement) {
        super(enableManagement);
    }

    //--------------------------------------------------------------------------
    //
    // Other Public APIs
    //                 
    //-------------------------------------------------------------------------- 

    /**
     * {@inheritDoc}
     */
    public Object invoke(Message msg) {
        ServiceException e = new ServiceException();
        e.setMessage("flex.messaging.services.http.SOAPProxyAdapter is no longer supported by BlazeDS");
        throw e;
    }
}
