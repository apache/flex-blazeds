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

import flex.messaging.services.http.httpclient.FlexGetMethod;
import flex.messaging.services.http.httpclient.FlexPostMethod;
import flex.messaging.util.Assert;
import flex.messaging.util.Trace;
import flex.messaging.MessageException;

/**
 *
 * Wraps filters with exception handling.
 */
public class ErrorFilter extends ProxyFilter
{
    /**
     * Invokes the filter with the context.
     * 
     * @param context The proxy context.
     */
    public void invoke(ProxyContext context)
    {
        try
        {
            if (next != null)
            {
                next.invoke(context);
            }
        }
        catch (MessageException ex)
        {
            throw ex;
        }
        catch (Throwable ex)
        {
            throw new MessageException(ex);
        }
        finally
        {
            try
            {
                if (context.getHttpMethod() != null)
                {

                    // we don't want to keep the connection open if authentication info was sent
                    if (context.hasAuthorization())
                    {
                        if (context.getHttpMethod() instanceof FlexGetMethod)
                        {
                            ((FlexGetMethod)context.getHttpMethod()).setConnectionForced(true);
                        }
                        else if (context.getHttpMethod() instanceof FlexPostMethod)
                        {
                            ((FlexPostMethod)context.getHttpMethod()).setConnectionForced(true);
                        }
                        else
                        {
                            Assert.testAssertion(false, "Should have custom Flex method: " + context.getHttpMethod().getClass());
                        }
                    }
                    context.getHttpMethod().releaseConnection();
                }
            }
            catch (Exception e)
            {
                if (Trace.error)
                    e.printStackTrace();
            }
        }
    }
}
