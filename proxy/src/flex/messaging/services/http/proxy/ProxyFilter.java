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
package flex.messaging.services.http.proxy;


/**
 *
 * Base filter definition that defines the filter contract.
 * Filters perform pre- and post-processing duties on the ProxyContext
 */
public abstract class ProxyFilter
{
    protected ProxyFilter next;

    public ProxyFilter()
    {
    }

    public ProxyFilter getNext()
    {
        return next;
    }

    public void setNext(ProxyFilter next)
    {
        this.next = next;
    }

    /**
     * The core business method.
     */
    public abstract void invoke(ProxyContext context);
}
