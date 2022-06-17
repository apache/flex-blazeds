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

import flex.management.runtime.messaging.endpoints.AMFEndpointControl;
import flex.messaging.MessageBroker;
import flex.messaging.io.MessageIOConstants;
import flex.messaging.endpoints.amf.AMFFilter;
import flex.messaging.endpoints.amf.BatchProcessFilter;
import flex.messaging.endpoints.amf.LegacyFilter;
import flex.messaging.endpoints.amf.MessageBrokerFilter;
import flex.messaging.endpoints.amf.SerializationFilter;
import flex.messaging.endpoints.amf.SessionFilter;
import flex.messaging.log.LogCategories;

/**
 * AMF based endpoint for Flex Messaging. Based on the Flash Remoting gateway servlet.
 */
public class AMFEndpoint extends BasePollingHTTPEndpoint {
    /**
     * The log category for this endpoint.
     */
    public static final String LOG_CATEGORY = LogCategories.ENDPOINT_AMF;

    //--------------------------------------------------------------------------
    //
    // Constructors
    //
    //--------------------------------------------------------------------------

    /**
     * Constructs an unmanaged <code>AMFEndpoint</code>.
     */
    public AMFEndpoint() {
        this(false);
    }

    /**
     * Constructs an <code>AMFEndpoint</code> with the indicated management.
     *
     * @param enableManagement <code>true</code> if the <code>AMFEndpoint</code>
     *                         is manageable; <code>false</code> otherwise.
     */
    public AMFEndpoint(boolean enableManagement) {
        super(enableManagement);
    }

    //--------------------------------------------------------------------------
    //
    // Protected/Private Methods
    //
    //--------------------------------------------------------------------------

    /**
     * Create the gateway filters that transform action requests
     * and responses.
     */
    @Override
    protected AMFFilter createFilterChain() {
        AMFFilter serializationFilter = new SerializationFilter(getLogCategory());
        AMFFilter batchFilter = new BatchProcessFilter();
        AMFFilter sessionFilter = sessionRewritingEnabled ? new SessionFilter() : null;
        AMFFilter envelopeFilter = new LegacyFilter(this);
        AMFFilter messageBrokerFilter = new MessageBrokerFilter(this);

        serializationFilter.setNext(batchFilter);
        if (sessionFilter != null) {
            batchFilter.setNext(sessionFilter);
            sessionFilter.setNext(envelopeFilter);
        } else {
            batchFilter.setNext(envelopeFilter);
        }
        envelopeFilter.setNext(messageBrokerFilter);

        return serializationFilter;
    }

    /**
     * Returns MessageIOConstants.AMF_CONTENT_TYPE.
     *
     * @return MessageIOConstants.AMF_CONTENT_TYPE
     */
    @Override
    protected String getResponseContentType() {
        return MessageIOConstants.AMF_CONTENT_TYPE;
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
        return "flex.messaging.io.amf.AmfMessageDeserializer";
    }

    /**
     * Returns the serializer class name used by the endpoint.
     *
     * @return The serializer class name used by the endpoint.
     */
    @Override
    protected String getSerializerClassName() {
        return "flex.messaging.io.amf.AmfMessageSerializer";
    }


    /**
     * Invoked automatically to allow the <code>AMFEndpoint</code> to setup its
     * corresponding MBean control.
     *
     * @param broker The <code>MessageBroker</code> that manages this
     *               <code>AMFEndpoint</code>.
     */
    @Override
    protected void setupEndpointControl(MessageBroker broker) {
        controller = new AMFEndpointControl(this, broker.getControl());
        controller.register();
        setControl(controller);
    }
}
