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
package flex.messaging.endpoints.amf;

import flex.messaging.io.amf.ActionContext;

import java.io.IOException;

/**
 * Filters perform pre- and post-processing duties on the ActionContext,
 * which contains the message/invocation as well as conextual information
 * about it, following the standard pipe-and-filter design pattern.
 *
 * @author PS Neville
 */
public abstract class AMFFilter
{
    protected AMFFilter next;

    public AMFFilter()
    {
    }

    public void setNext(AMFFilter next)
    {
        this.next = next;
    }

    public AMFFilter getNext()
    {
        return next;
    }

    /**
     * The core business method.
     */
    public abstract void invoke(final ActionContext context) throws IOException;

}
