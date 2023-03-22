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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import flex.management.runtime.messaging.endpoints.HTTPEndpointControl;
import flex.messaging.MessageBroker;
import flex.messaging.endpoints.amf.AMFFilter;
import flex.messaging.endpoints.amf.BatchProcessFilter;
import flex.messaging.endpoints.amf.MessageBrokerFilter;
import flex.messaging.endpoints.amf.SerializationFilter;
import flex.messaging.endpoints.amf.SessionFilter;
import flex.messaging.io.MessageIOConstants;
import flex.messaging.log.LogCategories;
import flex.messaging.messages.Message;
import flex.messaging.security.SecurityException;

/**
 * This class replaces Flex 1.5's ProxyServlet by splitting
 * the proxy's functionality into two pieces. Requests for proxied HTTP
 * content can now be sent using a message type via any channel.
 * The message broker directs requests to the appropriate service,
 * in Flex 1.5 terms, the Proxy Service. The response from the proxy
 * request is streamed back to the client.
 */
public class HTTPEndpoint extends BasePollingHTTPEndpoint {
    public static final String LOG_CATEGORY = LogCategories.ENDPOINT_HTTP;

    private static final int IMPROPER_CONTENT_TYPE = 10068;

    //--------------------------------------------------------------------------
    //
    // Constructor
    //
    //--------------------------------------------------------------------------

    /**
     * Constructs an unmanaged <code>HTTPEndpoint</code>.
     */
    public HTTPEndpoint() {
        this(false);
    }

    /**
     * Constructs a <code>HTTPEndpoint</code> with the indicated management.
     *
     * @param enableManagement <code>true</code> if the <code>HTTPEndpoint</code>
     *                         is manageable; <code>false</code> otherwise.
     */
    public HTTPEndpoint(boolean enableManagement) {
        super(enableManagement);
    }

    /**
     * Currently this override is a no-op to disable small messages over HTTP
     * endpoints.
     */
    @Override
    public Message convertToSmallMessage(Message message) {
        return message;
    }

    /**
     * Overrides to check the request content type is application/xml.
     *
     * @param req The servlet request.
     * @param res The servlet response.
     */
    @Override
    public void service(HttpServletRequest req, HttpServletResponse res) {
        String contentType = req.getContentType();
        boolean xmlContentType = contentType == null || contentType.equals(MessageIOConstants.XML_CONTENT_TYPE);
        if (!xmlContentType) {
            // HTTP endpoint ''{0}'' must be contacted via a HTTP request with proper content type.
            SecurityException se = new SecurityException();
            se.setMessage(IMPROPER_CONTENT_TYPE, new Object[]{id});
            throw se;
        }

        super.service(req, res);
    }

    //--------------------------------------------------------------------------
    //
    // Protected/Private Methods
    //
    //--------------------------------------------------------------------------

    /**
     * Create default filter chain or return current one if already present.
     */
    @Override
    protected AMFFilter createFilterChain() {
        AMFFilter serializationFilter = new SerializationFilter(getLogCategory());
        AMFFilter batchFilter = new BatchProcessFilter();
        AMFFilter sessionFilter = sessionRewritingEnabled ? new SessionFilter() : null;
        AMFFilter messageBrokerFilter = new MessageBrokerFilter(this);

        serializationFilter.setNext(batchFilter);
        if (sessionFilter != null) {
            batchFilter.setNext(sessionFilter);
            sessionFilter.setNext(messageBrokerFilter);
        } else {
            batchFilter.setNext(messageBrokerFilter);
        }

        return serializationFilter;
    }

    /**
     * Returns MessageIOConstants.XML_CONTENT_TYPE.
     */
    @Override
    protected String getResponseContentType() {
        return MessageIOConstants.XML_CONTENT_TYPE;
    }

    /**
     * Returns the log category of the endpoint.
     *
     * @return The log category of the endpoint.
     */
    @Override
    protected String getLogCategory() {
        return LOG_CATEGORY;
    }

    /**
     * Returns the deserializer class name used by the endpoint.
     *
     * @return The deserializer class name used by the endpoint.
     */
    @Override
    protected String getDeserializerClassName() {
        return "flex.messaging.io.amfx.AmfxMessageDeserializer";
    }

    /**
     * Returns the serializer class name used by the endpoint.
     *
     * @return The serializer class name used by the endpoint.
     */
    @Override
    protected String getSerializerClassName() {
        return "flex.messaging.io.amfx.AmfxMessageSerializer";
    }

    /**
     * Invoked automatically to allow the <code>HTTPEndpoint</code> to setup its
     * corresponding MBean control.
     *
     * @param broker The <code>MessageBroker</code> that manages this
     *               <code>HTTPEndpoint</code>.
     */
    @Override
    protected void setupEndpointControl(MessageBroker broker) {
        controller = new HTTPEndpointControl(this, broker.getControl());
        controller.register();
        setControl(controller);
    }
}
