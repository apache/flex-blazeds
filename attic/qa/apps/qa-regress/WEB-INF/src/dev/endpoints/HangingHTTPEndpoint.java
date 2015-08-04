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

package dev.endpoints;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import flex.messaging.endpoints.HTTPEndpoint;

/**
 *  HTTPEndpoint varient for connect timeout and request timeout testing. 
 *  This endpoint sleeps for 3 seconds before servicing each inbound message.
 */
public class HangingHTTPEndpoint extends HTTPEndpoint
{
    public HangingHTTPEndpoint()
    {
        super();
    }
    
    public HangingHTTPEndpoint(boolean enableManagement)
    {
        super(enableManagement);
    }
    
    public void service(HttpServletRequest req, HttpServletResponse res)
    {
        try
        {
            Thread.sleep(3000);
        }
        catch (InterruptedException ie)
        {
            // Ignore.
        }
        super.service(req, res);
    }
}
