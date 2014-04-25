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
package flex.messaging.endpoints;

import flex.management.runtime.messaging.endpoints.EndpointControl;
import flex.messaging.FlexContext;
import flex.messaging.FlexSession;
import flex.messaging.HttpFlexSession;
import flex.messaging.MessageClient;
import flex.messaging.client.FlexClient;
import flex.messaging.config.ConfigMap;
import flex.messaging.config.ConfigurationConstants;
import flex.messaging.endpoints.amf.AMFFilter;
import flex.messaging.io.MessageIOConstants;
import flex.messaging.io.amf.ActionContext;
import flex.messaging.log.HTTPRequestLog;
import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.Message;
import flex.messaging.util.SettingsReplaceUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for all the HTTP-based endpoints.
 */
public abstract class BaseHTTPEndpoint extends AbstractEndpoint
{
    //--------------------------------------------------------------------------
    //
    // Public Static Constants
    //
    //--------------------------------------------------------------------------

    /**
     * The secure and insecure URL schemes for the HTTP endpoint.
     */
    public static final String HTTP_PROTOCOL_SCHEME = "http";
    public static final String HTTPS_PROTOCOL_SCHEME = "https";

    //--------------------------------------------------------------------------
    //
    // Private Static Constants
    //
    //--------------------------------------------------------------------------

    private static final String ADD_NO_CACHE_HEADERS = "add-no-cache-headers";
    private static final String REDIRECT_URL = "redirect-url";
    private static final String INVALIDATE_SESSION_ON_DISCONNECT = "invalidate-session-on-disconnect";
    private static final String HTTP_RESPONSE_HEADERS = "http-response-headers";
    private static final String HEADER_ATTR = "header";

    private static final String HEADER_NAME_ORIGIN = "Origin";
    private static final String ACCESS_CONTROL = "Access-Control-";
    private static final String SESSION_REWRITING_ENABLED = "session-rewriting-enabled";

    private static final int ERR_MSG_DUPLICATE_SESSIONS_DETECTED = 10035;
    private static final String REQUEST_ATTR_DUPLICATE_SESSION_FLAG = "flex.messaging.request.DuplicateSessionDetected";

    //--------------------------------------------------------------------------
    //
    // Constructor
    //
    //--------------------------------------------------------------------------

    /**
     * Constructs an unmanaged <code>BaseHTTPEndpoint</code>.
     */
    public BaseHTTPEndpoint()
    {
        this(false);
    }

    /**
     * Constructs a <code>BaseHTTPEndpoint</code> with the specified management setting.
     *
     * @param enableManagement <code>true</code> if the <code>BaseHTTPEndpoint</code>
     * is manageable; otherwise <code>false</code>.
     */
    public BaseHTTPEndpoint(boolean enableManagement)
    {
        super(enableManagement);
    }

    //--------------------------------------------------------------------------
    //
    // Initialize, validate, start, and stop methods.
    //
    //--------------------------------------------------------------------------

    /**
     * Initializes the <code>Endpoint</code> with the properties.
     * If subclasses override this method, they must call <code>super.initialize()</code>.
     *
     * @param id The ID of the <code>Endpoint</code>.
     * @param properties Properties for the <code>Endpoint</code>.
     */
    @Override public void initialize(String id, ConfigMap properties)
    {
        super.initialize(id, properties);

        if (properties == null || properties.size() == 0)
            return;

        // General HTTP props.
        addNoCacheHeaders = properties.getPropertyAsBoolean(ADD_NO_CACHE_HEADERS, true);
        redirectURL = properties.getPropertyAsString(REDIRECT_URL, null);
        invalidateSessionOnDisconnect = properties.getPropertyAsBoolean(INVALIDATE_SESSION_ON_DISCONNECT, false);
        loginAfterDisconnect = properties.getPropertyAsBoolean(ConfigurationConstants.LOGIN_AFTER_DISCONNECT_ELEMENT, false);
        sessionRewritingEnabled = properties.getPropertyAsBoolean(SESSION_REWRITING_ENABLED, true);
        initializeHttpResponseHeaders(properties);
        validateEndpointProtocol();
    }

    /**
     * Starts the <code>Endpoint</code> by creating a filter chain and setting
     * up serializers and deserializers.
     */
    @Override public void start()
    {
        if (isStarted())
            return;

        super.start();

        filterChain = createFilterChain();
    }

    //--------------------------------------------------------------------------
    //
    // Variables
    //
    //--------------------------------------------------------------------------

    /**
     * Controller used to manage this endpoint.
     */
    protected EndpointControl controller;

    /**
     * AMF processing filter chain used by this endpoint.
     */
    protected AMFFilter filterChain;

    /**
     * Headers to add to the HTTP response.
     */
    protected List<HttpHeader> httpResponseHeaders;

    //--------------------------------------------------------------------------
    //
    // Properties
    //
    //--------------------------------------------------------------------------

    //----------------------------------
    //  addNoCacheHeaders
    //----------------------------------

    protected boolean addNoCacheHeaders = true;

    /**
     * Retrieves the <code>add-no-cache-headers</code> property.
     *
     * @return <code>true</code> if <code>add-no-cache-headers</code> is enabled;
     * <code>false</code> otherwise.
     */
    public boolean isAddNoCacheHeaders()
    {
        return addNoCacheHeaders;
    }

    /**
     * Sets the <code>add-no-cache-headers</code> property.
     *
     * @param addNoCacheHeaders The <code>add-no-cache-headers</code> property.
     */
    public void setAddNoCacheHeaders(boolean addNoCacheHeaders)
    {
        this.addNoCacheHeaders = addNoCacheHeaders;
    }

    //----------------------------------
    //  loginAfterDisconnect
    //----------------------------------

    /**
     * @exclude
     * This is a property used on the client.
     */
    protected boolean loginAfterDisconnect;

    //----------------------------------
    //  invalidateSessionOnDisconnect
    //----------------------------------

    protected boolean invalidateSessionOnDisconnect;

    /**
     * Indicates whether the server session will be invalidated
     * when a client channel disconnects.
     * The default is <code>false</code>.
     *
     * @return <code>true</code> if the server session will be invalidated
     *         when a client channel disconnects, <code>false</code> otherwise.
     */
    public boolean isInvalidateSessionOnDisconnect()
    {
        return invalidateSessionOnDisconnect;
    }

    /**
     * Determines whether to invalidate the server session for a client
     * that disconnects its channel.
     * The default is <code>false</code>.
     *
     * @param value <code>true</code> to invalidate the server session for a client
     *              that disconnects its channel, <code>false</code> otherwise.
     */
    public void setInvalidateSessionOnDisconnect(boolean value)
    {
        invalidateSessionOnDisconnect = value;
    }

    //----------------------------------
    //  redirectURL
    //----------------------------------

    protected String redirectURL;

    /**
     * Retrieves the <code>redirect-url</code> property.
     *
     * @return The <code>redirect-url</code> property.
     */
    public String getRedirectURL()
    {
        return redirectURL;
    }

    /**
     * Sets the <code>redirect-url</code> property.
     *
     * @param redirectURL The <code>redirect-url</code> property.
     */
    public void setRedirectURL(String redirectURL)
    {
        this.redirectURL = redirectURL;
    }

    //----------------------------------
    //  sessionRewritingEnabled
    //----------------------------------

    protected boolean sessionRewritingEnabled = true;

    /**
     * Indicates whether the server will fall back on rewriting URLs to include
     * session identifiers in the URL when HTTP session cookies are not allowed
     * on the client. The default is <code>true</code>.
     *
     * @return <code>true</code> if the session rewriting is enabled.
     */
    public boolean isSessionRewritingEnabled()
    {
        return sessionRewritingEnabled;
    }

    /**
     * Sets whether the session rewriting is enabled.
     *
     * @param value The session writing enabled value.
     */
    public void setSessionRewritingEnabled(boolean value)
    {
        sessionRewritingEnabled = value;
    }

    //--------------------------------------------------------------------------
    //
    // Public Methods
    //
    //--------------------------------------------------------------------------

    /**
     * Handle AMF/AMFX encoded messages sent over HTTP.
     *
     * @param req The original servlet request.
     * @param res The active servlet response.
     */
    @Override
    public void service(HttpServletRequest req, HttpServletResponse res)
    {
        super.service(req, res);

        try
        {
            // Setup serialization and type marshalling contexts
            setThreadLocals();

            // Create a context for this request
            ActionContext context = new ActionContext();

            // Pass endpoint's mpi settings to the context so that it knows what level of
            // performance metrics should be gathered during serialization/deserialization
            context.setRecordMessageSizes(isRecordMessageSizes());
            context.setRecordMessageTimes(isRecordMessageTimes());

            // Send invocation through filter chain, which ends at the MessageBroker
            filterChain.invoke(context);

            // After serialization completes, increment endpoint byte counters,
            // if the endpoint is managed
            if (isManaged())
            {
                controller.addToBytesDeserialized(context.getDeserializedBytes());
                controller.addToBytesSerialized(context.getSerializedBytes());
            }

            if (context.getStatus() != MessageIOConstants.STATUS_NOTAMF)
            {
                if (addNoCacheHeaders)
                    addNoCacheHeaders(req, res);

                addHeadersToResponse(req, res);

                ByteArrayOutputStream outBuffer = context.getResponseOutput();

                res.setContentType(getResponseContentType());

                res.setContentLength(outBuffer.size());
                outBuffer.writeTo(res.getOutputStream());
                res.flushBuffer();
            }
            else
            {
                // Not an AMF request, probably viewed in a browser
                if (redirectURL != null)
                {
                    try
                    {
                        //Check for redirect URL context-root token
                        redirectURL = SettingsReplaceUtil.replaceContextPath(redirectURL, req.getContextPath());
                        res.sendRedirect(redirectURL);
                    }
                    catch (IllegalStateException alreadyFlushed)
                    {
                        // ignore
                    }
                }
            }
        }
        catch (IOException ioe)
        {
            // This happens when client closes the connection, log it at info level
            log.info(ioe.getMessage());
            // Store exception information for latter logging
            req.setAttribute(HTTPRequestLog.HTTP_ERROR_INFO, ioe.toString());
        }
        catch (Throwable t)
        {
            log.error(t.getMessage(), t);
            // Store exception information for latter logging
            req.setAttribute(HTTPRequestLog.HTTP_ERROR_INFO, t.toString());
        }
        finally
        {
            clearThreadLocals();
        }
    }


    /**
     * @exclude
     * Returns a <code>ConfigMap</code> of endpoint properties that the client
     * needs. This includes properties from <code>super.describeEndpoint</code>
     * and additional <code>BaseHTTPEndpoint</code> specific properties under
     * "properties" key.
     */
    @Override
    public ConfigMap describeEndpoint()
    {
        ConfigMap endpointConfig = super.describeEndpoint();

        if (loginAfterDisconnect)
        {
            ConfigMap loginAfterDisconnect = new ConfigMap();
            // Adding as a value rather than attribute to the parent
            loginAfterDisconnect.addProperty(EMPTY_STRING, TRUE_STRING);

            ConfigMap properties = endpointConfig.getPropertyAsMap(PROPERTIES_ELEMENT, null);
            if (properties == null)
            {
                properties = new ConfigMap();
                endpointConfig.addProperty(PROPERTIES_ELEMENT, properties);
            }
            properties.addProperty(ConfigurationConstants.LOGIN_AFTER_DISCONNECT_ELEMENT, loginAfterDisconnect);
        }

        return endpointConfig;
    }

    /**
     * Overrides to guard against duplicate HTTP-based sessions for the same FlexClient
     * which will occur if the remote host has disabled session cookies.
     *
     * @see AbstractEndpoint#setupFlexClient(String)
     */
    @Override
    public FlexClient setupFlexClient(String id)
    {
        FlexClient flexClient = super.setupFlexClient(id);

        // Scan for duplicate HTTP-sessions and if found, invalidate them and throw a MessageException.
        // A request attribute is used to deal with batched AMF messages that arrive in a single request by trigger multiple passes through this method.
        boolean duplicateSessionDetected = (FlexContext.getHttpRequest().getAttribute(REQUEST_ATTR_DUPLICATE_SESSION_FLAG) != null);

        List<FlexSession> sessions = null;
        if (!duplicateSessionDetected)
        {
            sessions = flexClient.getFlexSessions();
            int n = sessions.size();
            if (n > 1)
            {
                List<HttpFlexSession> httpFlexSessions = new ArrayList<HttpFlexSession>();
                for (int i = 0; i < n; i++)
                {
                    FlexSession currentSession = sessions.get(i);
                    if (currentSession instanceof HttpFlexSession)
                        httpFlexSessions.add((HttpFlexSession)currentSession);
                    if (httpFlexSessions.size() > 1)
                    {
                        FlexContext.getHttpRequest().setAttribute(REQUEST_ATTR_DUPLICATE_SESSION_FLAG, httpFlexSessions);
                        duplicateSessionDetected = true;
                        break;
                    }
                }
            }
        }

        // If more than one was found, remote host isn't using session cookies. Kill all duplicate sessions and return an error.
        // Simplest to just re-scan the list given that it will be very short, but use an iterator for concurrent modification.
        if (duplicateSessionDetected)
        {
            Object attributeValue = FlexContext.getHttpRequest().getAttribute(REQUEST_ATTR_DUPLICATE_SESSION_FLAG);
            String newSessionId = null;
            String oldSessionId = null;
            if (attributeValue != null)
            {
                @SuppressWarnings("unchecked")
                List<HttpFlexSession> httpFlexSessions = (List<HttpFlexSession>)attributeValue;
                oldSessionId = httpFlexSessions.get(0).getId();
                newSessionId = httpFlexSessions.get(1).getId();
            }

            if (sessions != null)
            {
                for (FlexSession session : sessions)
                {
                    if (session instanceof HttpFlexSession)
                    {
                        session.invalidate();
                    }
                }
            }

            // Return an error to the client.

            DuplicateSessionException e = new DuplicateSessionException();
            // Duplicate HTTP-based FlexSession error: A request for FlexClient ''{0}'' arrived over a new FlexSession ''{1}'', but FlexClient is already associated with FlexSession ''{2}'', therefore it cannot be associated with the new session.
            e.setMessage(ERR_MSG_DUPLICATE_SESSIONS_DETECTED, new Object[]{flexClient.getId(), newSessionId, oldSessionId});
            throw e;
        }

        return flexClient;
    }

    //--------------------------------------------------------------------------
    //
    // Protected Methods
    //
    //--------------------------------------------------------------------------

    /**
     * Adds custom headers specified in the config to the HTTP response. The only
     * exception is that access control headers (Access-Control-*) are sent only
     * if there is an Origin header in the request.
     *
     * @param request The HTTP request.
     * @param response The HTTP response.
     */
    protected void addHeadersToResponse(HttpServletRequest request, HttpServletResponse response)
    {
        if (httpResponseHeaders == null || httpResponseHeaders.isEmpty())
            return;

        String origin = request.getHeader(HEADER_NAME_ORIGIN);
        boolean originHeaderExists = origin != null && origin.length() != 0;

        for (HttpHeader header : httpResponseHeaders)
        {
            if (header.name.startsWith(ACCESS_CONTROL) && !originHeaderExists)
                continue;

            response.addHeader(header.name, header.value);
        }
    }

    /**
     * Create the gateway filters that transform action requests
     * and responses.
     */
    protected abstract AMFFilter createFilterChain();

    /**
     * Returns the content type used by the connection handler to set on the
     * HTTP response. Subclasses should either return MessageIOConstants.AMF_CONTENT_TYPE
     * or MessageIOConstants.XML_CONTENT_TYPE.
     */
    protected abstract String getResponseContentType();

    /**
     * Returns https which is the secure protocol scheme for the endpoint.
     *
     * @return https.
     */
    @Override protected String getSecureProtocolScheme()
    {
        return HTTPS_PROTOCOL_SCHEME;
    }

    /**
     * Returns http which is the insecure protocol scheme for the endpoint.
     *
     * @return http.
     */
    @Override protected String getInsecureProtocolScheme()
    {
        return HTTP_PROTOCOL_SCHEME;
    }

    /**
     * @see flex.messaging.endpoints.AbstractEndpoint#handleChannelDisconnect(CommandMessage)
     */
    @Override protected Message handleChannelDisconnect(CommandMessage disconnectCommand)
    {
        HttpFlexSession session = (HttpFlexSession)FlexContext.getFlexSession();
        FlexClient flexClient = FlexContext.getFlexClient();

        // Shut down any subscriptions established over this channel/endpoint
        // for this specific FlexClient.
        if (flexClient.isValid())
        {
            String endpointId = getId();
            List<MessageClient> messageClients = flexClient.getMessageClients();
            for (MessageClient messageClient : messageClients)
            {
                if (messageClient.getEndpointId().equals(endpointId))
                {
                    messageClient.setClientChannelDisconnected(true);
                    messageClient.invalidate();
                }
            }
        }

        // And optionally invalidate the session.
        if (session.isValid() && isInvalidateSessionOnDisconnect())
            session.invalidate(false /* don't recreate */);

        return super.handleChannelDisconnect(disconnectCommand);
    }

    protected void initializeHttpResponseHeaders(ConfigMap properties)
    {
        if (!properties.containsKey(HTTP_RESPONSE_HEADERS))
            return;

        ConfigMap httpResponseHeaders = properties.getPropertyAsMap(HTTP_RESPONSE_HEADERS, null);
        if (httpResponseHeaders == null)
            return;

        @SuppressWarnings("unchecked")
        List<String> headers = httpResponseHeaders.getPropertyAsList(HEADER_ATTR, null);
        if (headers == null || headers.isEmpty())
            return;

        if (this.httpResponseHeaders == null)
            this.httpResponseHeaders = new ArrayList<HttpHeader>();

        for (String header : headers)
        {
            int colonIndex = header.indexOf(":");
            String name = header.substring(0, colonIndex).trim();
            String value = header.substring(colonIndex + 1).trim();
            this.httpResponseHeaders.add(new HttpHeader(name, value));
        }
    }

    //--------------------------------------------------------------------------
    //
    // Nested Classes
    //
    //--------------------------------------------------------------------------

    /**
     * Helper class used for headers in the HTTP request/response.
     */
    static class HttpHeader
    {
        public HttpHeader(String name, String value)
        {
            this.name = name;
            this.value = value;
        }
        public final String name;
        public final String value;
    }
}