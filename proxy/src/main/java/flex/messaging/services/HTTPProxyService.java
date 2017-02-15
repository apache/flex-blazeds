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
package flex.messaging.services;

import flex.management.runtime.messaging.services.HTTPProxyServiceControl;
import flex.management.runtime.messaging.services.http.HTTPProxyDestinationControl;
import flex.messaging.Destination;
import flex.messaging.FlexRemoteCredentials;
import flex.messaging.MessageBroker;
import flex.messaging.MessageException;
import flex.messaging.FlexContext;
import flex.messaging.messages.HTTPMessage;
import flex.messaging.messages.Message;
import flex.messaging.messages.SOAPMessage;
import flex.messaging.services.http.HTTPProxyDestination;
import flex.messaging.services.http.proxy.ProxyException;
import flex.messaging.util.SettingsReplaceUtil;
import flex.messaging.util.StringUtils;
import flex.messaging.log.LogCategories;
import flex.messaging.log.Log;

import java.util.List;
import javax.servlet.http.HttpServletRequest;

/**
 * The HttpProxyService replaces the Flex 1.5 Proxy. It decouples
 * the details of how the client contacts the message broker
 * by accepting an HTTPMessage type which can be sent over
 * any channel.
 *
 * @see flex.messaging.messages.HTTPMessage
 */
public class HTTPProxyService extends AbstractService
{
    /** Log category for <code>HTTPProxyService</code>. */
    public static final String LOG_CATEGORY = LogCategories.SERVICE_HTTP;

    // Errors
    private static final int DOT_DOT_NOT_ALLOWED = 10700;
    private static final int MULTIPLE_DOMAIN_PORT = 10701;
    private static final int DYNAMIC_NOT_CONFIGURED = 10702;

    private HTTPProxyServiceControl controller;

    //--------------------------------------------------------------------------
    //
    // Constructor
    //
    //--------------------------------------------------------------------------

    /**
     * Constructs an unmanaged <code>HTTPProxyService</code>.
     */
    public HTTPProxyService()
    {
        this(false);
    }

    /**
     * Constructs a <code>HTTPProxyService</code> with the indicated management.
     *
     * @param enableManagement <code>true</code> if the <code>HTTPProxyService</code>
     * is manageable; otherwise <code>false</code>.
     */
    public HTTPProxyService(boolean enableManagement)
    {
        super(enableManagement);
    }

    //--------------------------------------------------------------------------
    //
    // Public Getters and Setters for HTTPProxyService properties
    //
    //--------------------------------------------------------------------------

    /**
     * Creates a <code>HTTPProxyDestination</code> instance, sets its id,
     * sets it manageable if the <code>AbstractService</code> that created it is
     * manageable, and sets its <code>Service</code> to the <code>AbstractService</code>
     * that created it.
     *
     * @param id The id of the <code>HTTPProxyDestination</code>.
     * @return The <code>Destination</code> instanced created.
     */
    @Override
    public Destination createDestination(String id)
    {
        HTTPProxyDestination destination = new HTTPProxyDestination();
        destination.setId(id);
        destination.setManaged(isManaged());
        destination.setService(this);

        return destination;
    }

    /**
     * Casts the <code>Destination</code> into <code>HTTPProxyDestination</code>
     * and calls super.addDestination.
     *
     * @param destination The <code>Destination</code> instance to be added.
     */
    @Override
    public void addDestination(Destination destination)
    {
        HTTPProxyDestination proxyDestination = (HTTPProxyDestination)destination;
        super.addDestination(proxyDestination);
    }

    //--------------------------------------------------------------------------
    //
    // Other Public APIs
    //
    //--------------------------------------------------------------------------

    /**
     * Processes messages of type <code>HTTPMessage</code> by invoking the
     * requested destination's adapter.
     *
     * @param msg The message sent by the MessageBroker.
     * @return The result of the service.
     */
    @Override
    public Object serviceMessage(Message msg)
    {
        if (!(msg instanceof HTTPMessage))
        {
            // The 'HTTPProxy' Service can only process messages of type 'HTTPMessage'.
            ServiceException e = new ServiceException();
            e.setMessage(UNKNOWN_MESSAGE_TYPE, new Object[]{"HTTPProxy", "HTTPMessage"});
            throw e;
        }

        HTTPMessage message = (HTTPMessage)msg;

        String destination = message.getDestination();
        HTTPProxyDestination dest = (HTTPProxyDestination)destinations.get(destination);

        //use the remote settings if the message didn't specify them
        FlexRemoteCredentials remoteCredentials =
            FlexContext.getFlexSession().getRemoteCredentials(getId(), destination);
        if (remoteCredentials != null)
        {
            message.setRemoteUsername(remoteCredentials.getUsername());
            message.setRemotePassword((String)remoteCredentials.getCredentials());
        }
        else if (dest.getRemoteUsername() != null && dest.getRemotePassword() != null)
        {
            message.setRemoteUsername(dest.getRemoteUsername());
            message.setRemotePassword(dest.getRemotePassword());
        }

        ServiceAdapter adapter = dest.getAdapter();

        Object result;

        if (message instanceof SOAPMessage)
        {
            result = invokeSoap(adapter, (SOAPMessage)message, dest);
        }
        else
        {
            result = invokeHttp(adapter, message, dest);
        }

        if (Log.isDebug())
        {
            String debugResult =
                StringUtils.prettifyString(String.valueOf(result));
            Log.getLogger(getLogCategory()).debug
            ("HTTP request: " +
                    message + StringUtils.NEWLINE +
                    "  response: " + StringUtils.NEWLINE +
                    debugResult + StringUtils.NEWLINE);
        }

        return result;
    }

    //--------------------------------------------------------------------------
    //
    // Protected/private APIs
    //
    //--------------------------------------------------------------------------

    protected Object invokeSoap(ServiceAdapter adapter, SOAPMessage message, HTTPProxyDestination destination)
    {
        if (isManaged())
        {
            HTTPProxyDestinationControl destinationControl = (HTTPProxyDestinationControl)destination.getControl();
            if (destinationControl != null)
                destinationControl.incrementInvokeSOAPCount();
        }

        String dynamicUrl = message.getUrl();

        String contextPath = null;
        String serverName = null;
        String serverPort = null;
        String protocol = null;
        HttpServletRequest req = FlexContext.getHttpRequest();
        if (req != null)
        {
            contextPath = req.getContextPath();
            protocol = req.getScheme();
            serverName = req.getServerName();
            int port = req.getServerPort();
            if (port != 0)
            {
                serverPort = Integer.valueOf(req.getServerPort()).toString();
            }
        }

        if (dynamicUrl != null && dynamicUrl.length() > 0)
        {
            checkUrl(dynamicUrl, contextPath, destination, serverName, serverPort, protocol, message.getRemoteUsername() != null);
        }
        else
        {
            //TODO: QUESTION: Pete Support default soap endpoints?
            //String url = settings.getParsedDefaultUrl(contextPath);
            //message.setUrl(url);

            // FIXME: Need a better error here!
            throw new MessageException("A SOAP endpoint was not provided.");
        }

        return adapter.invoke(message);
    }

    protected void checkUrl(String url, String contextPath, HTTPProxyDestination destination, String serverName,
            String serverPort, String serverProtocol, boolean authSupplied)
    {
        String originalUrl = url;

        String defaultUrl = destination.getParsedDefaultUrl(contextPath, serverName, serverPort, serverProtocol);
        List dynamicUrls = destination.getParsedDynamicUrls(contextPath);

        //If we find ".." in a URL provided by the client, someone's likely
        //trying to trick us.  Ask them to do it another way if so.

        int i = url.indexOf("/..");
        while (i != -1)
        {
            if (i == (url.length() - 3) || url.charAt(i + 3) == '/')
            {
                throw new ProxyException(DOT_DOT_NOT_ALLOWED);
            }
            i = url.indexOf("/..", i + 1);
        }

        //Next, check if the URL is exactly the default URL
        url = url.toLowerCase();

        // In IPv6, update to long form, if required.
        url = SettingsReplaceUtil.updateIPv6(url);

        if (defaultUrl != null && defaultUrl.equalsIgnoreCase(url))
            return;

        char[] urlChars = url.toCharArray();

        // Next, check that the URL matches a dynamic URL pattern
        for (i = 0; i < dynamicUrls.size(); i++)
        {
            char[] pattern = (char[])dynamicUrls.get(i);
            boolean matches = StringUtils.findMatchWithWildcard(urlChars, pattern);

            if (matches)
            {
                if (!authSupplied || destination.allowsDynamicAuthentication())
                    return;
                throw new ProxyException(MULTIPLE_DOMAIN_PORT);
            }
        }

        ProxyException exception = new ProxyException();
        exception.setMessage
        (DYNAMIC_NOT_CONFIGURED, new Object[] {originalUrl, destination.getId()});
        throw exception;
    }

    /**
     * Returns the log category of the <code>HTTPProxyService</code>.
     *
     * @return The log category of the component.
     */
    @Override
    protected String getLogCategory()
    {
        return LOG_CATEGORY;
    }

    protected Object invokeHttp(ServiceAdapter adapter, HTTPMessage message, HTTPProxyDestination destination)
    {
        if (isManaged())
        {
            HTTPProxyDestinationControl destinationControl = (HTTPProxyDestinationControl)destination.getControl();
            if (destinationControl != null)
                destinationControl.incrementInvokeHTTPCount();
        }

        String dynamicUrl = message.getUrl();

        String contextPath = null;
        String serverName = null;
        String serverPort = null;
        String protocol = null;
        HttpServletRequest req = FlexContext.getHttpRequest();

        if (req != null)
        {
            contextPath = req.getContextPath();
            protocol = req.getScheme();
            serverName = req.getServerName();
            int port = req.getServerPort();
            if (port != 0)
            {
                serverPort = Integer.toString(req.getServerPort());
            }
        }

        if (dynamicUrl != null && !"".equals(dynamicUrl))
        {
            checkUrl(dynamicUrl, contextPath, destination, serverName, serverPort, protocol, message.getRemoteUsername() != null);
        }
        else
        {
            String url = destination.getParsedDefaultUrl(contextPath, serverName, serverPort, protocol);
            message.setUrl(url);
        }

        return adapter.invoke(message);
    }

    /**
     * This method is invoked to allow the <code>HTTPProxyService</code> to instantiate and register its
     * MBean control.
     *
     * @param broker The <code>MessageBroker</code> to pass to the <code>HTTPProxyServiceControl</code> constructor.
     */
    @Override
    protected void setupServiceControl(MessageBroker broker)
    {
        controller = new HTTPProxyServiceControl(this, broker.getControl());
        controller.register();
        setControl(controller);
    }
}
