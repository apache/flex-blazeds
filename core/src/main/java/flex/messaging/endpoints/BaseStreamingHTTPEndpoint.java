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

import flex.messaging.FlexContext;
import flex.messaging.FlexSession;
import flex.messaging.HttpFlexSession;
import flex.messaging.MessageException;
import flex.messaging.client.EndpointPushNotifier;
import flex.messaging.client.FlexClient;
import flex.messaging.client.FlushResult;
import flex.messaging.client.UserAgentSettings;
import flex.messaging.config.ConfigMap;
import flex.messaging.log.Log;
import flex.messaging.messages.AcknowledgeMessage;
import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.Message;
import flex.messaging.messages.MessagePerformanceInfo;
import flex.messaging.messages.MessagePerformanceUtils;
import flex.messaging.util.TimeoutManager;
import flex.messaging.util.UserAgentManager;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;

/**
 * Base class for HTTP-based endpoints that support streaming HTTP connections to
 * connected clients.
 * Each streaming connection managed by this endpoint consumes one of the request
 * handler threads provided by the servlet container, so it is not highly scalable
 * but offers performance advantages over client polling for clients receiving
 * a steady, rapid stream of pushed messages.
 * This endpoint does not support polling clients and will fault any poll requests
 * that are received. To support polling clients use subclasses of
 * BaseHTTPEndpoint instead.
 */
public abstract class BaseStreamingHTTPEndpoint extends BaseHTTPEndpoint {
    //--------------------------------------------------------------------------
    //
    // Private Static Constants
    //
    //--------------------------------------------------------------------------

    /**
     * This token is used in chunked HTTP responses frequently so initialize it statically for general use.
     */
    private static final byte[] CRLF_BYTES = {(byte) 13, (byte) 10};

    /**
     * This token is used for the terminal chunk within a chunked response.
     */
    private static final byte ZERO_BYTE = (byte) 48;

    /**
     * This token is used to signal that a chunk of data should be skipped by the client.
     */
    private static final byte NULL_BYTE = (byte) 0;

    /**
     * Parameter name for 'command' passed in a request for a new streaming connection.
     */
    private static final String COMMAND_PARAM_NAME = "command";

    /**
     * This is the token at the end of the HTTP request line that indicates that it is
     * a stream connection that should be held open to push data back to the client,
     * as opposed to a regular request-response message.
     */
    private static final String OPEN_COMMAND = "open";

    /**
     * This is the token at the end of the HTTP request line that indicates that it is
     * a stream connection that should be closed.
     */
    private static final String CLOSE_COMMAND = "close";

    /**
     * Parameter name for the stream ID; passed with commands for an existing streaming connection.
     */
    private static final String STREAM_ID_PARAM_NAME = "streamId";

    /**
     * Constant for HTTP/1.0.
     */
    private static final String HTTP_1_0 = "HTTP/1.0";

    /**
     * Thread name suffix for request threads that are servicing a pinned open streaming connection.
     */
    private static final String STREAMING_THREAD_NAME_EXTENSION = "-in-streaming-mode";

    /**
     * Configuration constants.
     */
    private static final String PROPERTY_CONNECTION_IDLE_TIMEOUT_MINUTES = "connection-idle-timeout-minutes";
    private static final String PROPERTY_LEGACY_CONNECTION_IDLE_TIMEOUT_MINUTES = "idle-timeout-minutes";
    private static final String MAX_STREAMING_CLIENTS = "max-streaming-clients";
    private static final String SERVER_TO_CLIENT_HEARTBEAT_MILLIS = "server-to-client-heartbeat-millis";
    private static final String PROPERTY_INVALIDATE_MESSAGECLIENT_ON_STREAMING_CLOSE = "invalidate-messageclient-on-streaming-close";

    /**
     * Defaults.
     */
    private static final boolean DEFAULT_INVALIDATE_MESSAGECLIENT_ON_STREAMING_CLOSE = false;
    private static final int DEFAULT_SERVER_TO_CLIENT_HEARTBEAT_MILLIS = 5000;
    private static final int DEFAULT_MAX_STREAMING_CLIENTS = 10;

    /**
     * Errors.
     */
    public static final String POLL_NOT_SUPPORTED_CODE = "Server.PollNotSupported";
    public static final int POLL_NOT_SUPPORTED_MESSAGE = 10034;


    //--------------------------------------------------------------------------
    //
    // Constructor
    //
    //--------------------------------------------------------------------------

    /**
     * Constructs an unmanaged <code>BaseStreamingHTTPEndpoint</code>.
     */
    public BaseStreamingHTTPEndpoint() {
        this(false);
    }

    /**
     * Constructs an <code>BaseStreamingHTTPEndpoint</code> with the indicated management.
     *
     * @param enableManagement <code>true</code> if the <code>BaseStreamingHTTPEndpoint</code>
     *                         is manageable; <code>false</code> otherwise.
     */
    public BaseStreamingHTTPEndpoint(boolean enableManagement) {
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
     * @param id         The ID of the <code>Endpoint</code>.
     * @param properties Properties for the <code>Endpoint</code>.
     */
    @Override
    public void initialize(String id, ConfigMap properties) {
        super.initialize(id, properties);

        if (properties == null || properties.size() == 0) {
            // Initialize default user agent manager settings.
            UserAgentManager.setupUserAgentManager(null, userAgentManager);

            return; // Nothing else to initialize.
        }

        // The interval that the server will check if the client is still available.
        serverToClientHeartbeatMillis = properties.getPropertyAsLong(SERVER_TO_CLIENT_HEARTBEAT_MILLIS, DEFAULT_SERVER_TO_CLIENT_HEARTBEAT_MILLIS);
        setServerToClientHeartbeatMillis(serverToClientHeartbeatMillis);

        setInvalidateMessageClientOnStreamingClose(properties.getPropertyAsBoolean(PROPERTY_INVALIDATE_MESSAGECLIENT_ON_STREAMING_CLOSE, DEFAULT_INVALIDATE_MESSAGECLIENT_ON_STREAMING_CLOSE));

        // Number of minutes a client can remain idle before the server times the connection out.
        int connectionIdleTimeoutMinutes = properties.getPropertyAsInt(PROPERTY_CONNECTION_IDLE_TIMEOUT_MINUTES, getConnectionIdleTimeoutMinutes());
        if (connectionIdleTimeoutMinutes != 0) {
            setConnectionIdleTimeoutMinutes(connectionIdleTimeoutMinutes);
        } else {
            connectionIdleTimeoutMinutes = properties.getPropertyAsInt(PROPERTY_LEGACY_CONNECTION_IDLE_TIMEOUT_MINUTES, getConnectionIdleTimeoutMinutes());
            if (connectionIdleTimeoutMinutes != 0)
                setConnectionIdleTimeoutMinutes(connectionIdleTimeoutMinutes);
        }

        // User-agent configuration for kick-start bytes and max streaming connections per session.
        UserAgentManager.setupUserAgentManager(properties, userAgentManager);

        // Maximum number of clients allowed to have streaming HTTP connections with the endpoint.
        maxStreamingClients = properties.getPropertyAsInt(MAX_STREAMING_CLIENTS, DEFAULT_MAX_STREAMING_CLIENTS);

        // Set initial state for the canWait flag based on whether we allow waits or not.
        canStream = (maxStreamingClients > 0);
    }


    @Override
    public void start() {
        if (isStarted())
            return;

        super.start();

        if (connectionIdleTimeoutMinutes > 0) {
            pushNotifierTimeoutManager = new TimeoutManager(new ThreadFactory() {
                int counter = 1;

                public synchronized Thread newThread(Runnable runnable) {
                    Thread t = new Thread(runnable);
                    t.setName(getId() + "-StreamingConnectionTimeoutThread-" + counter++);
                    return t;
                }
            });
        }

        currentStreamingRequests = new ConcurrentHashMap<String, EndpointPushNotifier>();
    }

    /**
     * (non-JavaDoc)
     *
     * @see flex.messaging.endpoints.AbstractEndpoint#stop()
     */
    @Override
    public void stop() {
        if (!isStarted())
            return;

        // Shutdown the timeout manager for streaming connections cleanly.
        if (pushNotifierTimeoutManager != null) {
            pushNotifierTimeoutManager.shutdown();
            pushNotifierTimeoutManager = null;
        }

        // Shutdown any currently open streaming connections.
        for (EndpointPushNotifier notifier : currentStreamingRequests.values())
            notifier.close();

        currentStreamingRequests = null;

        super.stop();
    }

    //--------------------------------------------------------------------------
    //
    // Variables
    //
    //--------------------------------------------------------------------------

    /**
     * Used to synchronize sets and gets to the number of streaming clients.
     */
    protected final Object lock = new Object();

    /**
     * Used to keep track of the mapping between user agent match strings and
     * the bytes needed to kickstart their streaming connections.
     */
    protected UserAgentManager userAgentManager = new UserAgentManager();

    /**
     * This flag is volatile to allow for consistent reads across thread without
     * needing to pay the cost for a synchronized lock for each read.
     */
    private volatile boolean canStream = true;

    /**
     * Manages timing out EndpointPushNotifier instances.
     */
    private volatile TimeoutManager pushNotifierTimeoutManager;

    /**
     * A Map(EndpointPushNotifier, Boolean.TRUE) containing all currently open streaming notifiers
     * for this endpoint.
     * Used for clean shutdown.
     */
    private ConcurrentHashMap<String, EndpointPushNotifier> currentStreamingRequests;

    //--------------------------------------------------------------------------
    //
    // Properties
    //
    //--------------------------------------------------------------------------

    //------------------------------------------
    //  invalidateMessageClientOnStreamingClose
    //-----------------------------------------

    private volatile boolean invalidateMessageClientOnStreamingClose = DEFAULT_INVALIDATE_MESSAGECLIENT_ON_STREAMING_CLOSE;

    /**
     * Returns whether invalidate-messageclient-on-streaming-close is enabled.
     * See {@link BaseStreamingHTTPEndpoint#setInvalidateMessageClientOnStreamingClose(boolean)}
     * for details.
     *
     * @return <code>true</code> if the invalidate-messageclient-on-streaming-close is enabled, <code>false</code> otherwise.
     */
    public boolean isInvalidateMessageClientOnStreamingClose() {
        return invalidateMessageClientOnStreamingClose;
    }

    /**
     * Sets the invalidate-messageclient-on-streaming close property. If enabled,
     * when the streaming connection is closed for whatever reason (for example, the client is gone),
     * the client's associated MessageClient on the server is invalidated immediately.
     * This is useful in scenarios where there is a constant stream of messages, the client is gone,
     * and the streaming connection is closed, but the session has not timed out on the server yet.
     * In that case, enabling this property will prevent messages accumulating on the session on behalf
     * of the MessageClient that will invalidate.
     * <p>
     * Important: Do not enable this property when reliable messaging is used, otherwise
     * reliable reconnect attempts will not happen correctly.</p>
     *
     * @param value The property value.
     */
    public void setInvalidateMessageClientOnStreamingClose(boolean value) {
        invalidateMessageClientOnStreamingClose = value;
    }

    //----------------------------------
    //  serverToClientHeartbeatMillis
    //----------------------------------

    private long serverToClientHeartbeatMillis = DEFAULT_SERVER_TO_CLIENT_HEARTBEAT_MILLIS;

    /**
     * Retrieves the number of milliseconds the server will wait before writing a
     * single <code>null</code> byte to the streaming connection, to ensure the client is
     * still available.
     *
     * @return The server-to-client heartbeat time in milliseconds.
     */
    public long getServerToClientHeartbeatMillis() {
        return serverToClientHeartbeatMillis;
    }

    /**
     * Retrieves the number of milliseconds the server will wait before writing a
     * single <code>null</code> byte to the streaming connection to ensure the client is
     * still available when there are no new messages for the client.
     * A non-positive value means the server will wait forever for new messages and
     * it will not write the <code>null</code> byte to determine if the client is available.
     *
     * @param serverToClientHeartbeatMillis The server-to-client heartbeat time in milliseconds.
     */
    public void setServerToClientHeartbeatMillis(long serverToClientHeartbeatMillis) {
        if (serverToClientHeartbeatMillis < 0)
            serverToClientHeartbeatMillis = 0;
        this.serverToClientHeartbeatMillis = serverToClientHeartbeatMillis;
    }

    //----------------------------------
    //  connectionIdleTimeoutMinutes
    //----------------------------------

    private int connectionIdleTimeoutMinutes = 0;

    /**
     * Retrieves the number of minutes a client can remain idle before the server
     * times the connection out. The default value is 0, indicating that connections
     * will not be timed out and must be closed by the client or server, either explicitly
     * or by either process terminating.
     *
     * @return The number of minutes a client can remain idle before the server
     * times the connection out.
     */
    public int getConnectionIdleTimeoutMinutes() {
        return connectionIdleTimeoutMinutes;
    }

    /**
     * Sets the number of minutes a client can remain idle before the server
     * times the connection out. A value of 0 or below indicates that
     * connections will not be timed out.
     *
     * @param value The number of minutes a client can remain idle
     *              before the server times the connection out.
     */
    public void setConnectionIdleTimeoutMinutes(int value) {
        if (value < 0)
            value = 0;

        this.connectionIdleTimeoutMinutes = value;
    }

    /**
     * (non-JavaDoc)
     *
     * @deprecated Use {@link BaseStreamingHTTPEndpoint#getConnectionIdleTimeoutMinutes()} instead.
     */
    public int getIdleTimeoutMinutes() {
        return getConnectionIdleTimeoutMinutes();
    }

    /**
     * (non-JavaDoc)
     *
     * @deprecated Use {@link BaseStreamingHTTPEndpoint#setConnectionIdleTimeoutMinutes(int)} instead.
     */
    public void setIdleTimeoutMinutes(int value) {
        setConnectionIdleTimeoutMinutes(value);
    }

    //----------------------------------
    //  maxStreamingClients
    //----------------------------------

    private int maxStreamingClients = DEFAULT_MAX_STREAMING_CLIENTS;

    /**
     * Retrieves the maximum number of clients that will be allowed to establish
     * a streaming HTTP connection with the endpoint.
     *
     * @return The maximum number of clients that will be allowed to establish
     * a streaming HTTP connection with the endpoint.
     */
    public int getMaxStreamingClients() {
        return maxStreamingClients;
    }

    /**
     * Sets the maximum number of clients that will be allowed to establish
     * a streaming HTTP connection with the server.
     *
     * @param maxStreamingClients The maximum number of clients that will be allowed
     *                            to establish a streaming HTTP connection with the server.
     */
    public void setMaxStreamingClients(int maxStreamingClients) {
        this.maxStreamingClients = maxStreamingClients;
        canStream = (streamingClientsCount < maxStreamingClients);
    }

    //----------------------------------
    //  streamingClientsCount
    //----------------------------------

    protected int streamingClientsCount;

    /**
     * Retrieves the the number of clients that are currently in the streaming state.
     *
     * @return The number of clients that are currently in the streaming state.
     */
    public int getStreamingClientsCount() {
        return streamingClientsCount;
    }

    //--------------------------------------------------------------------------
    //
    // Public Methods
    //
    //--------------------------------------------------------------------------

    /**
     * Handles HTTP requests targetting this endpoint.
     * Two types or requests are supported. If the request is a regular request-response AMF/AMFX
     * message it is handled by the base logic in BaseHTTPEndpoint.service. However, if it is a
     * request to open a streaming HTTP connection to the client this endpoint performs some
     * validation checks and then holds the connection open to stream data back to the client
     * over.
     *
     * @param req The original servlet request.
     * @param res The active servlet response.
     */
    @Override
    public void service(HttpServletRequest req, HttpServletResponse res) {
        String command = req.getParameter(COMMAND_PARAM_NAME);
        if (command != null)
            serviceStreamingRequest(req, res);
        else // Let BaseHTTPEndpoint logic handle regular request-response messaging.
            super.service(req, res);
    }

    //--------------------------------------------------------------------------
    //
    // Protected Methods
    //
    //--------------------------------------------------------------------------

    /**
     * If the message has MPI enabled, this method adds all the needed performance
     * headers to the message.
     *
     * @param message Message to add performance headers to.
     */
    protected void addPerformanceInfo(Message message) {
        MessagePerformanceInfo mpiOriginal = getMPI(message);
        if (mpiOriginal == null)
            return;

        MessagePerformanceInfo mpip = (MessagePerformanceInfo) mpiOriginal.clone();
        try {
            // Set the original message info as the pushed causer info.
            MessagePerformanceUtils.setMPIP(message, mpip);
            MessagePerformanceUtils.setMPII(message, null);
        } catch (Exception e) {
            if (Log.isDebug())
                log.debug("MPI exception while streaming the message: " + e.toString());
        }

        // Overhead only used when MPI is enabled for sizing
        MessagePerformanceInfo mpio = new MessagePerformanceInfo();
        if (mpip.recordMessageTimes) {
            mpio.sendTime = System.currentTimeMillis();
            mpio.infoType = "OUT";
        }
        mpio.pushedFlag = true;
        MessagePerformanceUtils.setMPIO(message, mpio);

        // If MPI sizing information is enabled serialize again so that we know size
        if (mpip.recordMessageSizes) {
            try {
                // Each subclass serializes the message in their own format to
                // get the message size for the MPIO.
                long serializationOverhead = System.currentTimeMillis();
                mpio.messageSize = getMessageSizeForPerformanceInfo(message);

                // Set serialization overhead to the time calculated during serialization above
                if (mpip.recordMessageTimes) {
                    serializationOverhead = System.currentTimeMillis() - serializationOverhead;
                    mpip.addToOverhead(serializationOverhead);
                    mpiOriginal.addToOverhead(serializationOverhead);
                    mpio.sendTime = System.currentTimeMillis();
                }
            } catch (Exception e) {
                log.debug("MPI exception while streaming the message: " + e.toString());
            }
        }
    }

    /**
     * Utility method to convert streamed push messages to their small versions
     * if the channel-endpoint combination supports small messages.
     *
     * @param message The regular message.
     * @return The small message if the message has a small version, or regular message
     * if it doesn't .
     */
    protected Message convertPushMessageToSmall(Message message) {
        FlexSession session = FlexContext.getFlexSession();
        if (session != null && session.useSmallMessages())
            return convertToSmallMessage(message);
        return message;
    }

    /**
     * Used internally for performance information gathering; not intended for
     * public use. The default implementation of this method returns zero.
     * Subclasses should overwrite if they want to accurately report message
     * size information in performance information gathering.
     *
     * @param message Message to get the size for.
     * @return The size of the message after message is serialized.
     */
    protected long getMessageSizeForPerformanceInfo(Message message) {
        return 0;
    }

    /**
     * This streaming endpoint does not support polling clients.
     *
     * @param flexClient  The FlexClient that issued the poll request.
     * @param pollCommand The poll command from the client.
     * @return The flush info used to build the poll response.
     */
    @Override
    protected FlushResult handleFlexClientPoll(FlexClient flexClient, CommandMessage pollCommand) {
        MessageException me = new MessageException();
        me.setMessage(POLL_NOT_SUPPORTED_MESSAGE);
        me.setDetails(POLL_NOT_SUPPORTED_MESSAGE);
        me.setCode(POLL_NOT_SUPPORTED_CODE);
        throw me;
    }

    /**
     * Handles streaming connection open command sent by the FlexClient.
     *
     * @param req        The <code>HttpServletRequest</code> to service.
     * @param res        The <code>HttpServletResponse</code> to be used in case an error
     *                   has to be sent back.
     * @param flexClient FlexClient that requested the streaming connection.
     */
    protected void handleFlexClientStreamingOpenRequest(HttpServletRequest req, HttpServletResponse res, FlexClient flexClient) {
        FlexSession session = FlexContext.getFlexSession();
        if (canStream && session.canStream) {
            // If canStream/session.canStream is true it means we currently have
            // less than the max number of allowed streaming threads, per endpoint/session.

            // We need to protect writes/reads to the stream count with the endpoint's lock.
            // Also, we have to be careful to handle the case where two threads get to this point when only
            // one streaming spot remains; one thread will win and the other needs to fault.
            boolean thisThreadCanStream;
            synchronized (lock) {
                ++streamingClientsCount;
                if (streamingClientsCount == maxStreamingClients) {
                    thisThreadCanStream = true; // This thread got the last spot.
                    canStream = false;
                } else if (streamingClientsCount > maxStreamingClients) {
                    thisThreadCanStream = false; // This thread was beaten out for the last spot.
                    --streamingClientsCount; // Decrement the count because we're not going to grant the streaming right to the client.
                } else {
                    // We haven't hit the limit yet, allow this thread to stream.
                    thisThreadCanStream = true;
                }
            }

            // If the thread cannot wait due to endpoint streaming connection
            // limit, inform the client and return.
            if (!thisThreadCanStream) {
                String errorMessage = "Endpoint with id '" + getId() + "' cannot grant streaming connection to FlexClient with id '"
                        + flexClient.getId() + "' because " + MAX_STREAMING_CLIENTS + " limit of '"
                        + maxStreamingClients + "' has been reached.";
                if (Log.isError())
                    log.error(errorMessage);
                try {
                    errorMessage = "Endpoint with id '" + getId() + "' cannot grant streaming connection to FlexClient with id '"
                            + flexClient.getId() + "' because " + MAX_STREAMING_CLIENTS + " limit has been reached.";
                    res.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, errorMessage);
                } catch (IOException ignore) {
                }
                return;
            }

            // Setup for specific user agents.
            byte[] kickStartBytesToStream = null;
            String userAgentValue = req.getHeader(UserAgentManager.USER_AGENT_HEADER_NAME);
            UserAgentSettings agentSettings = userAgentManager.match(userAgentValue);
            if (agentSettings != null) {
                synchronized (session) {
                    session.maxConnectionsPerSession = agentSettings.getMaxPersistentConnectionsPerSession();
                }

                int kickStartBytes = agentSettings.getKickstartBytes();
                if (kickStartBytes > 0) {
                    // Determine the minimum number of actual bytes that need to be sent to
                    // kickstart, taking into account transfer-encoding overhead.
                    try {
                        int chunkLengthHeaderSize = Integer.toHexString(kickStartBytes).getBytes("ASCII").length;
                        int chunkOverhead = chunkLengthHeaderSize + 4; // 4 for the 2 wrapping CRLF tokens.
                        int minimumKickstartBytes = kickStartBytes - chunkOverhead;
                        kickStartBytesToStream = new byte[(minimumKickstartBytes > 0) ? minimumKickstartBytes :
                                kickStartBytes];
                    } catch (UnsupportedEncodingException ignore) {
                        kickStartBytesToStream = new byte[kickStartBytes];
                    }
                    Arrays.fill(kickStartBytesToStream, NULL_BYTE);
                }
            }

            // Now, check with the session before granting the streaming connection.
            synchronized (session) {
                ++session.streamingConnectionsCount;
                if (session.maxConnectionsPerSession == FlexSession.MAX_CONNECTIONS_PER_SESSION_UNLIMITED) {
                    thisThreadCanStream = true;
                } else if (session.streamingConnectionsCount == session.maxConnectionsPerSession) {
                    thisThreadCanStream = true; // This thread got the last spot in the session.
                    session.canStream = false;
                } else if (session.streamingConnectionsCount > session.maxConnectionsPerSession) {
                    thisThreadCanStream = false; // This thread was beaten out for the last spot.
                    --session.streamingConnectionsCount;
                    synchronized (lock) {
                        // Decrement the endpoint count because we're not going to grant the streaming right to the client.
                        --streamingClientsCount;
                    }
                } else {
                    // We haven't hit the limit yet, allow this thread to stream.
                    thisThreadCanStream = true;
                }
            }

            // If the thread cannot wait due to session streaming connection
            // limit, inform the client and return.
            if (!thisThreadCanStream) {
                if (Log.isError())
                    log.error("Endpoint with id '" + getId() + "' cannot grant streaming connection to FlexClient with id '"
                            + flexClient.getId() + "' because " + UserAgentManager.MAX_PERSISTENT_CONNECTIONS_PER_SESSION + " limit of '" + session.maxConnectionsPerSession
                            + ((agentSettings != null) ? "' for user-agent '" + agentSettings.getMatchOn() + "'" : "") + " has been reached.");
                try {
                    // Return an HTTP status code 400.
                    String errorMessage = "The server cannot grant streaming connection to this client because limit has been reached.";
                    res.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, errorMessage);
                } catch (IOException ignore) {
                    // NOWARN
                }
                return;
            }

            Thread currentThread = Thread.currentThread();
            String threadName = currentThread.getName();
            EndpointPushNotifier notifier = null;
            boolean suppressIOExceptionLogging = false; // Used to suppress logging for IO exception.
            try {
                currentThread.setName(threadName + STREAMING_THREAD_NAME_EXTENSION);

                // Open and commit response headers and get output stream.
                if (addNoCacheHeaders)
                    addNoCacheHeaders(req, res);
                res.setContentType(getResponseContentType());
                res.setHeader("Transfer-Encoding", "chunked");
                res.setHeader("Connection", "close");
                ServletOutputStream os = res.getOutputStream();
                res.flushBuffer();

                // If kickstart-bytes are specified, stream them.
                if (kickStartBytesToStream != null) {
                    if (Log.isDebug())
                        log.debug("Endpoint with id '" + getId() + "' is streaming " + kickStartBytesToStream.length
                                + " bytes (not counting chunk encoding overhead) to kick-start the streaming connection for FlexClient with id '"
                                + flexClient.getId() + "'.");

                    streamChunk(kickStartBytesToStream, os, res);
                }

                // Setup serialization and type marshalling contexts
                setThreadLocals();

                // Activate streaming helper for this connection.
                // Watch out for duplicate stream issues.
                try {
                    notifier = new EndpointPushNotifier(this, flexClient);
                } catch (MessageException me) {
                    if (me.getNumber() == 10033) // It's a duplicate stream request from the same FlexClient. Leave the current stream in place and fault this.
                    {
                        if (Log.isWarn())
                            log.warn("Endpoint with id '" + getId() + "' received a duplicate streaming connection request from, FlexClient with id '"
                                    + flexClient.getId() + "'. Faulting request.");

                        // Rollback counters and send an error response.
                        synchronized (lock) {
                            --streamingClientsCount;
                            canStream = (streamingClientsCount < maxStreamingClients);
                            synchronized (session) {
                                --session.streamingConnectionsCount;
                                session.canStream = (session.maxConnectionsPerSession == FlexSession.MAX_CONNECTIONS_PER_SESSION_UNLIMITED
                                        || session.streamingConnectionsCount < session.maxConnectionsPerSession);
                            }
                        }
                        try {
                            res.sendError(HttpServletResponse.SC_BAD_REQUEST);
                        } catch (IOException ignore) {
                            // NOWARN
                        }
                        return; // Exit early.
                    }
                }
                if (connectionIdleTimeoutMinutes > 0)
                    notifier.setIdleTimeoutMinutes(connectionIdleTimeoutMinutes);
                notifier.setLogCategory(getLogCategory());
                monitorTimeout(notifier);
                currentStreamingRequests.put(notifier.getNotifierId(), notifier);

                // Push down an acknowledgement for the 'connect' request containing the unique id for this specific stream.
                AcknowledgeMessage connectAck = new AcknowledgeMessage();
                connectAck.setBody(notifier.getNotifierId());
                connectAck.setCorrelationId(BaseStreamingHTTPEndpoint.OPEN_COMMAND);
                ArrayList toPush = new ArrayList(1);
                toPush.add(connectAck);
                streamMessages(toPush, os, res);

                // Output session level streaming count.
                if (Log.isDebug())
                    Log.getLogger(FlexSession.FLEX_SESSION_LOG_CATEGORY).info("Number of streaming clients for FlexSession with id '" + session.getId() + "' is " + session.streamingConnectionsCount + ".");

                // Output endpoint level streaming count.
                if (Log.isDebug())
                    log.debug("Number of streaming clients for endpoint with id '" + getId() + "' is " + streamingClientsCount + ".");

                // And cycle in a wait-notify loop with the aid of the helper until it
                // is closed, we're interrupted or the act of streaming data to the client fails.
                while (!notifier.isClosed()) {
                    try {
                        // Drain any messages that might have been accumulated
                        // while the previous drain was being processed.
                        List<AsyncMessage> messages = null;
                        synchronized (notifier.pushNeeded) {
                            messages = notifier.drainMessages();
                        }
                        streamMessages(messages, os, res);

                        synchronized (notifier.pushNeeded) {
                            notifier.pushNeeded.wait(serverToClientHeartbeatMillis);

                            messages = notifier.drainMessages();
                        }
                        // If there are no messages to send to the client, send an null
                        // byte as a heartbeat to make sure the client is still valid.
                        if (messages == null && serverToClientHeartbeatMillis > 0) {
                            try {
                                os.write(NULL_BYTE);
                                res.flushBuffer();
                            } catch (IOException e) {
                                if (Log.isWarn())
                                    log.warn("Endpoint with id '" + getId() + "' is closing the streaming connection to FlexClient with id '"
                                            + flexClient.getId() + "' because endpoint encountered a socket write error" +
                                            ", possibly due to an unresponsive FlexClient.", e);
                                break; // Exit the wait loop.
                            }
                        }
                        // Otherwise stream the messages to the client.
                        else {
                            // Update the last time notifier was used to drain messages.
                            // Important for idle timeout detection.
                            notifier.updateLastUse();

                            streamMessages(messages, os, res);
                        }
                    } catch (InterruptedException e) {
                        if (Log.isWarn())
                            log.warn("Streaming thread '" + threadName + "' for endpoint with id '" + getId() + "' has been interrupted and the streaming connection will be closed.");
                        os.close();
                        break; // Exit the wait loop.
                    }

                    // Update the FlexClient last use time to prevent FlexClient from
                    // timing out when the client is still subscribed. It is important
                    // to do this outside synchronized(notifier.pushNeeded) to avoid
                    // thread deadlock!
                    flexClient.updateLastUse();
                }
                if (Log.isDebug())
                    log.debug("Streaming thread '" + threadName + "' for endpoint with id '" + getId() + "' is releasing connection and returning to the request handler pool.");
                suppressIOExceptionLogging = true;
                // Terminate the response.
                streamChunk(null, os, res);
            } catch (IOException e) {
                if (Log.isWarn() && !suppressIOExceptionLogging)
                    log.warn("Streaming thread '" + threadName + "' for endpoint with id '" + getId() + "' is closing connection due to an IO error.", e);
            } finally {
                currentThread.setName(threadName);

                // We're done so decrement the counts for streaming threads,
                // and update the canStream flag if necessary.
                synchronized (lock) {
                    --streamingClientsCount;
                    canStream = (streamingClientsCount < maxStreamingClients);
                    synchronized (session) {
                        --session.streamingConnectionsCount;
                        session.canStream = (session.maxConnectionsPerSession == FlexSession.MAX_CONNECTIONS_PER_SESSION_UNLIMITED
                                || session.streamingConnectionsCount < session.maxConnectionsPerSession);
                    }
                }

                if (notifier != null && currentStreamingRequests != null) {
                    currentStreamingRequests.remove(notifier.getNotifierId());
                    notifier.close();
                }

                // Output session level streaming count.
                if (Log.isDebug())
                    Log.getLogger(FlexSession.FLEX_SESSION_LOG_CATEGORY).info("Number of streaming clients for FlexSession with id '" + session.getId() + "' is " + session.streamingConnectionsCount + ".");

                // Output endpoint level streaming count.
                if (Log.isDebug())
                    log.debug("Number of streaming clients for endpoint with id '" + getId() + "' is " + streamingClientsCount + ".");
            }
        }
        // Otherwise, client's streaming connection open request could not be granted.
        else {
            if (Log.isError()) {
                String logString = null;
                if (!canStream) {
                    logString = "Endpoint with id '" + getId() + "' cannot grant streaming connection to FlexClient with id '"
                            + flexClient.getId() + "' because " + MAX_STREAMING_CLIENTS + " limit of '"
                            + maxStreamingClients + "' has been reached.";
                } else if (!session.canStream) {
                    logString = "Endpoint with id '" + getId() + "' cannot grant streaming connection to FlexClient with id '"
                            + flexClient.getId() + "' because " + UserAgentManager.MAX_STREAMING_CONNECTIONS_PER_SESSION + " limit of '"
                            + session.maxConnectionsPerSession + "' has been reached.";
                }
                if (logString != null)
                    log.error(logString);
            }

            try {
                // Return an HTTP status code 400 to indicate that client request can't be processed.
                String errorMessage = null;
                if (!canStream) {
                    errorMessage = "Endpoint with id '" + getId() + "' cannot grant streaming connection to FlexClient with id '"
                            + flexClient.getId() + "' because " + MAX_STREAMING_CLIENTS + " limit has been reached.";
                } else if (!session.canStream) {
                    errorMessage = "Endpoint with id '" + getId() + "' cannot grant streaming connection to FlexClient with id '"
                            + flexClient.getId() + "' because " + UserAgentManager.MAX_STREAMING_CONNECTIONS_PER_SESSION + " limit has been reached.";
                }
                res.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, errorMessage);
            } catch (IOException ignore) {
            }
        }
    }

    /**
     * Handles streaming connection close command sent by the FlexClient.
     *
     * @param req        The <code>HttpServletRequest</code> to service.
     * @param res        The <code>HttpServletResponse</code> to be used in case an error
     *                   has to be sent back.
     * @param flexClient FlexClient that requested the streaming connection.
     * @param streamId   The id for the stream to close.
     */
    protected void handleFlexClientStreamingCloseRequest(HttpServletRequest req, HttpServletResponse res, FlexClient flexClient, String streamId) {
        if (streamId != null) {
            EndpointPushNotifier notifier = (EndpointPushNotifier) flexClient.getEndpointPushHandler(getId());
            if ((notifier != null) && notifier.getNotifierId().equals(streamId))
                notifier.close();
        }
    }

    /**
     * Service streaming connection commands.
     *
     * @param req The <code>HttpServletRequest</code> to service.
     * @param res The <code>HttpServletResponse</code> to be used in case an error
     *            has to be sent back.
     */
    protected void serviceStreamingRequest(HttpServletRequest req, HttpServletResponse res) {
        // If this is a request for a streaming connection, make sure it's for a valid FlexClient
        // and that the FlexSession doesn't already have a streaming connection open.
        // Streaming requests are POSTs (to help prevent the possibility of caching) that carry the
        // following parameters:
        // command - Indicating a custom command for the endpoint; currently 'open' to request a new
        //           streaming connection be opened, and 'close' to request the streaming connection
        //           to close.
        // version - Indicates the streaming connection 'version' to use; it's here for backward comp. support
        //           if we need to change how commands are handled in a future product release.
        // DSId - The FlexClient id value that uniquely identifies the swf making the request.
        String command = req.getParameter(COMMAND_PARAM_NAME);

        // Only HTTP 1.1 is supported, disallow HTTP 1.0.
        if (req.getProtocol().equals(HTTP_1_0)) {
            if (Log.isError())
                log.error("Endpoint with id '" + getId() + "' cannot service the streaming request made with " +
                        " HTTP 1.0. Only HTTP 1.1 is supported.");

            try {
                // Return an HTTP status code 400 to indicate that the client's request was syntactically invalid (bad command).
                res.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } catch (IOException ignore) {
            }
            return; // Abort further server processing.
        }

        if (!(command.equals(OPEN_COMMAND) || command.equals(CLOSE_COMMAND))) {
            if (Log.isError())
                log.error("Endpoint with id '" + getId() + "' cannot service the streaming request as the supplied command '"
                        + command + "' is invalid.");

            try {
                // Return an HTTP status code 400 to indicate that the client's request was syntactically invalid (bad command).
                res.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } catch (IOException ignore) {
            }
            return; // Abort further server processing.
        }

        String flexClientId = req.getParameter(Message.FLEX_CLIENT_ID_HEADER);
        if (flexClientId == null) {
            if (Log.isError())
                log.error("Endpoint with id '" + getId() + "' cannot service the streaming request as no FlexClient id"
                        + " has been supplied in the request.");

            try {
                // Return an HTTP status code 400 to indicate that the client's request was syntactically invalid (missing id).
                res.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } catch (IOException ignore) {
            }
            return; // Abort further server processing.
        }

        // Validate that the provided FlexClient id exists and is associated with the current session.
        // We don't do this validation with CLOSE_COMMAND because CLOSE_COMMAND can come in on a
        // different session. For example, when the session expires due to timeout, the streaming client
        // using that session sends a CLOSE_COMMAND on a new session to let the server know to clean client's
        // corresponding server constructs. In that case, server already knows that session has expired so
        // we can simply omit this validation.
        FlexClient flexClient = null;
        List<FlexClient> flexClients = FlexContext.getFlexSession().getFlexClients();
        boolean validFlexClientId = false;
        for (Iterator<FlexClient> iter = flexClients.iterator(); iter.hasNext(); ) {
            flexClient = iter.next();
            if (flexClient.getId().equals(flexClientId) && flexClient.isValid()) {
                validFlexClientId = true;
                break;
            }
        }
        if (!command.equals(CLOSE_COMMAND) && !validFlexClientId) {
            if (Log.isError())
                log.error("Endpoint with id '" + getId() + "' cannot service the streaming request as either the supplied"
                        + " FlexClient id '" + flexClientId + " is not valid, or the FlexClient with that id is not valid.");

            try {
                // Return an HTTP status code 400 to indicate that the client's request was syntactically invalid (invalid id).
                res.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } catch (IOException ignore) {
            }
            return; // Abort further server processing.
        }

        // If a close command is received and we don't have any flex clients registered simply invalidate 
        // the Flex Session. This will take care of the Flex Session that got created when the MB servlet 
        // was processing the CLOSE request.
        if (command.equals(CLOSE_COMMAND) && flexClients.size() == 0) {
            FlexSession flexSession = FlexContext.getFlexSession();
            if (flexSession instanceof HttpFlexSession) {
                ((HttpFlexSession) flexSession).invalidate(false);
            }
            return;
        }

        if (flexClient != null) {
            if (command.equals(OPEN_COMMAND))
                handleFlexClientStreamingOpenRequest(req, res, flexClient);
            else if (command.equals(CLOSE_COMMAND))
                handleFlexClientStreamingCloseRequest(req, res, flexClient, req.getParameter(STREAM_ID_PARAM_NAME));
        }
    }

    /**
     * Helper method to write a chunk of bytes to the output stream in an HTTP
     * "Transfer-Encoding: chunked" format.
     * If the bytes array is null or empty, a terminal chunk will be written to
     * signal the end of the response.
     * Once the chunk is written to the output stream, the stream will be flushed immediately (no buffering).
     *
     * @param bytes    The array of bytes to write as a chunk in the response; or if null, the signal to write the final chunk to complete the response.
     * @param os       The output stream the chunk will be written to.
     * @param response The HttpServletResponse, used to flush the chunk to the client.
     * @throws IOException if writing the chunk to the output stream fails.
     */
    protected void streamChunk(byte[] bytes, ServletOutputStream os, HttpServletResponse response) throws IOException {
        if ((bytes != null) && (bytes.length > 0)) {
            byte[] chunkLength = Integer.toHexString(bytes.length).getBytes("ASCII");
            os.write(chunkLength);
            os.write(CRLF_BYTES);
            os.write(bytes);
            os.write(CRLF_BYTES);
            response.flushBuffer();
        } else // Send final 'EOF' chunk for the response.
        {
            os.write(ZERO_BYTE);
            os.write(CRLF_BYTES);
            response.flushBuffer();
        }
    }

    /**
     * Helper method invoked by the endpoint request handler thread cycling in wait-notify.
     * Serializes messages and streams each to the client as a response chunk using streamChunk().
     *
     * @param messages The messages to serialize and push to the client.
     * @param os       The output stream the chunk will be written to.
     * @param response The HttpServletResponse, used to flush the chunk to the client.
     */
    protected abstract void streamMessages(List messages, ServletOutputStream os, HttpServletResponse response) throws IOException;

    /**
     * Given a message, returns the MessagePerformanceInfo object if the message
     * performance gathering is enabled, returns null otherwise.
     *
     * @param message The message.
     * @return MessagePerformanceInfo if the message performance gathering is enabled,
     * null otherwise.
     */
    protected MessagePerformanceInfo getMPI(Message message) {
        return (isRecordMessageSizes() || isRecordMessageTimes()) ?
                MessagePerformanceUtils.getMPII(message) : null;
    }

    //--------------------------------------------------------------------------
    //
    // Private methods.
    //
    //--------------------------------------------------------------------------

    /**
     * Utility method used at EndpointPushNotifier construction to monitor it for timeout.
     *
     * @param notifier The EndpointPushNotifier to monitor.
     */
    private void monitorTimeout(EndpointPushNotifier notifier) {
        if (pushNotifierTimeoutManager != null)
            pushNotifierTimeoutManager.scheduleTimeout(notifier);
    }
}
