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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import flex.management.runtime.messaging.endpoints.StreamingAMFEndpointControl;
import flex.messaging.MessageBroker;
import flex.messaging.endpoints.amf.AMFFilter;
import flex.messaging.endpoints.amf.BatchProcessFilter;
import flex.messaging.endpoints.amf.LegacyFilter;
import flex.messaging.endpoints.amf.MessageBrokerFilter;
import flex.messaging.endpoints.amf.SerializationFilter;
import flex.messaging.endpoints.amf.SessionFilter;
import flex.messaging.io.MessageIOConstants;
import flex.messaging.io.TypeMarshallingContext;
import flex.messaging.io.amf.Amf3Output;
import flex.messaging.log.Log;
import flex.messaging.log.LogCategories;
import flex.messaging.messages.Message;

/**
 * Extension to the AMFEndpoint to support streaming HTTP connections to connected
 * clients.
 * Each streaming connection managed by this endpoint consumes one of the request
 * handler threads provided by the servlet container, so it is not highly scalable
 * but offers performance advantages over client polling for clients receiving a steady,
 * rapid stream of pushed messages.
 * This endpoint does not support polling clients and will fault any poll requests
 * that are received. To support polling clients use AMFEndpoint instead.
 */
public class StreamingAMFEndpoint extends BaseStreamingHTTPEndpoint
{
    //--------------------------------------------------------------------------
    //
    // Public Static Constants
    //
    //--------------------------------------------------------------------------

    /**
     * The log category for this endpoint.
     */
    public static final String LOG_CATEGORY = LogCategories.ENDPOINT_STREAMING_AMF;

    //--------------------------------------------------------------------------
    //
    // Private Static Constants
    //
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    //
    // Constructors
    //
    //--------------------------------------------------------------------------

    /**
     * Constructs an unmanaged <code>StreamingAMFEndpoint</code>.
     */
    public StreamingAMFEndpoint()
    {
        this(false);
    }

    /**
     * Constructs a <code>StreamingAMFEndpoint</code> with the indicated management.
     *
     * @param enableManagement <code>true</code> if the <code>StreamingAMFEndpoint</code>
     * is manageable; <code>false</code> otherwise.
     */
    public StreamingAMFEndpoint(boolean enableManagement)
    {
        super(enableManagement);
    }

    //--------------------------------------------------------------------------
    //
    // Protected Methods
    //
    //--------------------------------------------------------------------------

    /**
     * Create the gateway filters that transform action requests
     * and responses.
     */
    @Override protected AMFFilter createFilterChain()
    {
        AMFFilter serializationFilter = new SerializationFilter(getLogCategory());
        AMFFilter batchFilter = new BatchProcessFilter();
        AMFFilter sessionFilter = sessionRewritingEnabled? new SessionFilter() : null;
        AMFFilter envelopeFilter = new LegacyFilter(this);
        AMFFilter messageBrokerFilter = new MessageBrokerFilter(this);

        serializationFilter.setNext(batchFilter);
        if (sessionFilter != null)
        {
            batchFilter.setNext(sessionFilter);
            sessionFilter.setNext(envelopeFilter);
        }
        else
        {
            batchFilter.setNext(envelopeFilter);
        }
        envelopeFilter.setNext(messageBrokerFilter);

        return serializationFilter;
    }

    /**
     * Returns MessageIOConstants.AMF_CONTENT_TYPE.
     */
    @Override protected String getResponseContentType()
    {
        return MessageIOConstants.AMF_CONTENT_TYPE;
    }

    /**
     * Returns the log category of the endpoint.
     *
     * @return The log category of the endpoint.
     */
    @Override protected String getLogCategory()
    {
        return LOG_CATEGORY;
    }

    /**
     * Used internally for performance information gathering; not intended for
     * public use. Serializes the message in AMF format and returns the size of
     * the serialized message.
     *
     * @param message Message to get the size for.
     *
     * @return The size of the message after message is serialized.
     */
    @Override protected long getMessageSizeForPerformanceInfo(Message message)
    {
        Amf3Output amfOut = new Amf3Output(serializationContext);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        DataOutputStream dataOutStream = new DataOutputStream(outStream);
        amfOut.setOutputStream(dataOutStream);
        try
        {
            amfOut.writeObject(message);
        }
        catch (IOException e)
        {
            if (Log.isDebug())
                log.debug("MPI exception while retrieving the size of the serialized message: " + e.toString());
        }
        return dataOutStream.size();
    }

    /**
     * Returns the deserializer class name used by the endpoint.
     *
     * @return The deserializer class name used by the endpoint.
     */
    @Override protected String getDeserializerClassName()
    {
        return "flex.messaging.io.amf.AmfMessageDeserializer";
    }

    /**
     * Returns the serializer class name used by the endpoint.
     *
     * @return The serializer class name used by the endpoint.
     */
    @Override protected String getSerializerClassName()
    {
        return "flex.messaging.io.amf.AmfMessageSerializer";
    }

    /**
     * Invoked automatically to allow the <code>StreamingAMFEndpoint</code> to setup its
     * corresponding MBean control.
     *
     * @param broker The <code>MessageBroker</code> that manages this
     * <code>StreamingAMFEndpoint</code>.
     */
    @Override protected void setupEndpointControl(MessageBroker broker)
    {
        controller = new StreamingAMFEndpointControl(this, broker.getControl());
        controller.register();
        setControl(controller);
    }

    /**
     * Helper method invoked by the endpoint request handler thread cycling in wait-notify.
     * Serializes messages and streams each to the client as a response chunk using streamChunk().
     *
     * @param messages The messages to serialize and push to the client.
     * @param os The output stream the chunk will be written to.
     * @param response The HttpServletResponse, used to flush the chunk to the client.
     */
    @Override protected void streamMessages(List messages, ServletOutputStream os, HttpServletResponse response) throws IOException
    {
        if (messages == null || messages.isEmpty())
            return;

        // Serialize each message as a separate chunk of bytes.
        TypeMarshallingContext.setTypeMarshaller(getTypeMarshaller());
        for (Iterator iter = messages.iterator(); iter.hasNext();)
        {
            Message message = (Message)iter.next();
            addPerformanceInfo(message);
            message = convertPushMessageToSmall(message);
            if (Log.isDebug())
                log.debug("Endpoint with id '" + getId() + "' is streaming message: " + message);

            Amf3Output amfOut = new Amf3Output(serializationContext);
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            DataOutputStream dataOutStream = new DataOutputStream(outStream);
            amfOut.setOutputStream(dataOutStream);

            amfOut.writeObject(message);
            dataOutStream.flush();
            byte[] messageBytes = outStream.toByteArray();
            streamChunk(messageBytes, os, response);

            if (isManaged())
                ((StreamingAMFEndpointControl)controller).incrementPushCount();
        }
        TypeMarshallingContext.setTypeMarshaller(null);
    }
}