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

import flex.management.runtime.messaging.services.http.SOAPProxyAdapterControl;
import flex.messaging.messages.Message;
import flex.messaging.messages.SOAPMessage;
import flex.messaging.messages.HTTPMessage;
import flex.messaging.services.http.proxy.ProxyContext;
import flex.messaging.Destination;
import flex.messaging.MessageException;

/**
 * A Soap specific subclass of HttpProxyAdapter to
 * allow for future web services features.
 */
public class SOAPProxyAdapter extends HTTPProxyAdapter
{
    private SOAPProxyAdapterControl controller;
    
    //--------------------------------------------------------------------------
    //
    // Constructor
    //
    //--------------------------------------------------------------------------
    
    /**
     * Constructs an unmanaged <code>SOAPProxyAdapter</code> instance.
     */
    public SOAPProxyAdapter()
    {
        this(false);
    }
    
    /**
     * Constructs a <code>SOAPProxyAdapter</code> instance.
     * 
     * @param enableManagement <code>true</code> if the <code>SOAPProxyAdapter</code> has a
     * corresponding MBean control for management; otherwise <code>false</code>.
     */
    public SOAPProxyAdapter(boolean enableManagement)
    {
        super(enableManagement);
    }
    
    //--------------------------------------------------------------------------
    //
    // Other Public APIs
    //                 
    //-------------------------------------------------------------------------- 

    /** {@inheritDoc} */    
    public Object invoke(Message msg)
    {
        HTTPMessage message = (HTTPMessage)msg;
        ProxyContext context = new ProxyContext();

        if (message instanceof SOAPMessage)
        {
            context.setSoapRequest(true);
        }

        setupContext(context, message);

        try
        {
            filterChain.invoke(context);
            return context.getResponse();
        }
        catch (MessageException ex)
        {
            throw ex;
        }
        catch (Throwable t)
        {
            // this should never happen- ErrorFilter should catch everything
            t.printStackTrace();
            throw new MessageException(t.toString());
        }
    }
    
    //--------------------------------------------------------------------------
    //
    // Protected/private APIs
    //             
    //--------------------------------------------------------------------------
    
    /**
     * Invoked automatically to allow the <code>SOAPProxyAdapter</code> to setup its corresponding
     * MBean control.
     * 
     * @param broker The <code>Destination</code> that manages this <code>SOAPProxyAdapter</code>.
     */
    protected void setupAdapterControl(Destination destination)
    {
        controller = new SOAPProxyAdapterControl(this, destination.getControl());        
        controller.register();
        setControl(controller);
    }
}
