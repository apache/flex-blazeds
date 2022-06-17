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
package flex.messaging.io.amf.client;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import flex.messaging.MessageException;
import flex.messaging.io.amf.ActionContext;
import flex.messaging.io.amf.ActionMessage;
import flex.messaging.io.amf.AmfTrace;
import flex.messaging.io.amf.MessageBody;
import flex.messaging.io.amf.MessageHeader;
import flex.messaging.io.ClassAliasRegistry;
import flex.messaging.io.MessageDeserializer;
import flex.messaging.io.MessageIOConstants;
import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.AmfMessageSerializer;
import flex.messaging.io.amf.AmfMessageDeserializer;
import flex.messaging.io.amf.client.exceptions.ClientStatusException;
import flex.messaging.io.amf.client.exceptions.ServerStatusException;

/**
 * A Java alternative to the native flash.net.NetConnection class for
 * sending AMF formatted requests over HTTP or HTTPS.
 * AMFConnection in Actionscript. AMF connection automatically handles cookies
 * by looking for cookie headers and setting the cookies in subsequent request.
 * <p>
 * AMF connection class is not thread safe.
 */
public class AMFConnection {
    //--------------------------------------------------------------------------
    //
    // Public Static Variables
    //
    //--------------------------------------------------------------------------

    public static final String COOKIE = "Cookie";
    public static final String COOKIE2 = "Cookie2";
    public static final String COOKIE_SEPERATOR = ";";
    public static final String COOKIE_NAMEVALUE_SEPERATOR = "=";
    public static final String SET_COOKIE = "Set-Cookie";
    public static final String SET_COOKIE2 = "Set-Cookie2";

    //--------------------------------------------------------------------------
    //
    // Private Static Variables
    //
    //--------------------------------------------------------------------------

    private static int DEFAULT_OBJECT_ENCODING = MessageIOConstants.AMF3;
    private static String HTTP_HEADER_NAME_CONTENT_TYPE = "Content-Type";

    //--------------------------------------------------------------------------
    //
    // Public Static Methods
    //
    //--------------------------------------------------------------------------

    /**
     * Registers a custom alias for a class name bidirectionally.
     *
     * @param alias     The alias for the class name.
     * @param className The concrete class name.
     */
    public static void registerAlias(String alias, String className) {
        ClassAliasRegistry registry = ClassAliasRegistry.getRegistry();
        registry.registerAlias(alias, className);
        registry.registerAlias(className, alias);
    }

    //--------------------------------------------------------------------------
    //
    // Constructor
    //
    //--------------------------------------------------------------------------

    /**
     * Creates a default AMF connection instance.
     */
    public AMFConnection() {
    }

    //--------------------------------------------------------------------------
    //
    // Private Variables
    //
    //--------------------------------------------------------------------------

    private ActionContext actionContext;
    private boolean connected;
    private boolean instantiateTypes = true;
    private Proxy proxy;
    private int objectEncoding;
    private boolean objectEncodingSet = false;
    private SerializationContext serializationContext;
    private String url;
    private URL urlObject;

    //--------------------------------------------------------------------------
    //
    // Protected Variables
    //
    //--------------------------------------------------------------------------

    /**
     * List of AMF message headers.
     */
    protected List<MessageHeader> amfHeaders;

    /**
     * An AMF connection may have an AMF header processor where AMF headers
     * can be passed to as they are encountered in AMF response messages.
     */
    protected AMFHeaderProcessor amfHeaderProcessor;

    /**
     * Used internally by AMF serialization code to log debug info.
     */
    protected AmfTrace amfTrace;

    /**
     * A map of cookie names and values that are used to keep track of cookies.
     */
    protected Map<String, String> cookies;

    /**
     * Map of Http request header names and values.
     */
    protected Map<String, String> httpRequestHeaders;

    /**
     * Sequentially incremented counter used to generate a unique responseURI
     * to match response messages to responders.
     */
    protected int responseCounter;

    /**
     * The URL connection used to make AMF formatted HTTP and HTTPS requests for
     * this connection.
     */
    protected HttpURLConnection urlConnection;

    /**
     * A buffered input stream that wraps the input stream of the underlying
     * url connection. This is returned in server and client exceptions as part
     * of the HTTP response info.
     */
    protected BufferedInputStream urlConnectionInputStream;

    //--------------------------------------------------------------------------
    //
    // Properties
    //
    //--------------------------------------------------------------------------

    //----------------------------------
    //  amfHeaderProcessor
    //----------------------------------

    /**
     * Returns the AMF header processor associated with the AMF connection. AMF
     * header processor is same as NetConnection's client property.
     * See flash.net.NetConnection#client.
     *
     * @return The AMF header processor associated with the AMF connection.
     */
    public AMFHeaderProcessor getAMFHeaderProcessor() {
        return amfHeaderProcessor;
    }

    /**
     * Sets the AMF header processor associated with the AMF connection.
     *
     * @param amfHeaderProcessor The AMF header processor to set.
     */
    public void setAMFHeaderProcessor(AMFHeaderProcessor amfHeaderProcessor) {
        this.amfHeaderProcessor = amfHeaderProcessor;
    }

    //----------------------------------
    //  amfTrace
    //----------------------------------

    /**
     * Returns the <tt>AmfTrace</tt> associated with the AMF connection.
     *
     * @return The <tt>AmfTrace</tt> associated with the AMF connection.
     */
    public AmfTrace getAmfTrace() {
        return amfTrace;
    }

    /**
     * Sets the <tt>AmfTrace</tt> associated with the AMF connection.
     *
     * @param amfTrace The <tt>AmfTrace</tt> associated with the AMF connection.
     */
    public void setAmfTrace(AmfTrace amfTrace) {
        this.amfTrace = amfTrace;
    }

    //----------------------------------
    //  defaultObjectEncoding
    //----------------------------------

    /**
     * The default object encoding for all AMFConnection instances. This
     * controls which version of AMF is used during serialization. The default
     * is AMF 3. See flash.net.ObjectEncoding#DEFAULT
     *
     * @return The default object encoding of the AMF connection.
     */
    public static int getDefaultObjectEncoding() {
        return DEFAULT_OBJECT_ENCODING;
    }

    /**
     * Sets the default object encoding of the AMF connection.
     *
     * @param value The value to set the default object encoding to.
     */
    public static void setDefaultObjectEncoding(int value) {
        DEFAULT_OBJECT_ENCODING = value;
    }

    //----------------------------------
    //  instantiateTypes
    //----------------------------------

    /**
     * Returns instantiateTypes property. InstantiateTypes property determines
     * whether type information will be used to instantiate a new instance.
     * If set to false, types will be deserialized as flex.messaging.io.ASObject
     * instances with type information retained but not used to create an instance.
     * Note that types in the flex.* package (and any subpackage) will always be
     * instantiated. The default is true.
     *
     * @return The instantitateTypes property.
     */
    public boolean isInstantiateTypes() {
        return instantiateTypes;
    }

    /**
     * Sets the instantiateTypes property.
     *
     * @param instantiateTypes The value to set the instantiateTypes property to.
     */
    public void setInstantiateTypes(boolean instantiateTypes) {
        this.instantiateTypes = instantiateTypes;
    }

    //----------------------------------
    //  objectEncoding
    //----------------------------------

    /**
     * The object encoding for this AMFConnection sets which AMF version to
     * use during serialization. If set, this version overrides the
     * defaultObjectEncoding.
     *
     * @return The object encoding for the AMF connection.
     */
    public int getObjectEncoding() {
        if (!objectEncodingSet)
            return getDefaultObjectEncoding();
        return objectEncoding;
    }

    /**
     * Sets the object encoding for the AMF connection.
     *
     * @param objectEncoding The value to set the object encoding to.
     */
    public void setObjectEncoding(int objectEncoding) {
        this.objectEncoding = objectEncoding;
        objectEncodingSet = true;
    }

    //----------------------------------
    //  proxy
    //----------------------------------

    /**
     * Returns the <tt>Proxy</tt> this AMF connection is using;
     * <code>null</code> by default.
     *
     * @return The <tt>Proxy</tt> this AMF connection is using.
     */
    public Proxy getProxy() {
        return proxy;
    }

    /**
     * Sets the <tt>Proxy</tt> that this AMF connection will use.
     * Set to <code>null</code> to clear out any existing proxy setting that
     * should no longer be used.
     *
     * @param proxy The <tt>Proxy</tt> this AMF connection will use.
     */
    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }

    //----------------------------------
    //  url
    //----------------------------------

    /**
     * Returns the HTTP or HTTPS url for the AMF connection.
     *
     * @return The HTTP or HTTPs url for the AMF connection.
     */
    public String getUrl() {
        return url;
    }

    //--------------------------------------------------------------------------
    //
    // Public Methods
    //
    //--------------------------------------------------------------------------

    /**
     * Adds an AMF packet-level header which is sent with every request for
     * the life of this AMF connection.
     *
     * @param name           The name of the header.
     * @param mustUnderstand Whether the header must be processed or not.
     * @param data           The value of the header.
     */
    public void addAmfHeader(String name, boolean mustUnderstand, Object data) {
        if (amfHeaders == null)
            amfHeaders = new ArrayList<MessageHeader>();

        MessageHeader header = new MessageHeader(name, mustUnderstand, data);
        amfHeaders.add(header);
    }

    /**
     * Add an AMF packet-level header with mustUnderstand=false, which is sent
     * with every request for the life of this AMF connection.
     *
     * @param name The name of the header.
     * @param data The value of the header.
     */
    public void addAmfHeader(String name, Object data) {
        addAmfHeader(name, false, data);
    }

    /**
     * Removes any AMF headers found with the name given.
     *
     * @param name The name of the header(s) to remove.
     * @return true if a header existed with the given name.
     */
    public boolean removeAmfHeader(String name) {
        boolean exists = false;
        if (amfHeaders != null) {
            for (Iterator<MessageHeader> iterator = amfHeaders.iterator(); iterator.hasNext(); ) {
                MessageHeader header = iterator.next();
                if (name.equals(header.getName())) {
                    iterator.remove();
                    exists = true;
                }
            }
        }
        return exists;
    }

    /**
     * Removes all AMF headers.
     */
    public void removeAllAmfHeaders() {
        if (amfHeaders != null)
            amfHeaders = null;
    }

    /**
     * Adds a Http request header to the underlying connection.
     *
     * @param name  The name of the Http header.
     * @param value The value of the Http header.
     */
    public void addHttpRequestHeader(String name, String value) {
        if (httpRequestHeaders == null)
            httpRequestHeaders = new HashMap<String, String>();

        httpRequestHeaders.put(name, value);
    }

    /**
     * Removes the Http header found with the name given.
     *
     * @param name The name of the Http header.
     * @return true if a header existed with the given name.
     */
    public boolean removeHttpRequestHeader(String name) {
        boolean exists = false;
        if (httpRequestHeaders != null) {
            Object previousValue = httpRequestHeaders.remove(name);
            exists = (previousValue != null);
        }
        return exists;
    }

    /**
     * Removes all Http request headers.
     */
    public void removeAllHttpRequestHeaders() {
        if (httpRequestHeaders != null)
            httpRequestHeaders = null;
    }

    /**
     * Makes an AMF request to the server. A connection must have been made
     * prior to making a call.
     *
     * @param command   The method to call on the server.
     * @param arguments Arguments for the method.
     * @return The result of the call.
     * @throws ClientStatusException If there is a client side exception.
     * @throws ServerStatusException If there is a server side exception.
     */
    public Object call(String command, Object... arguments) throws ClientStatusException, ServerStatusException {
        if (!connected) {
            String message = "AMF connection is not connected";
            ClientStatusException cse = new ClientStatusException(message, ClientStatusException.AMF_CALL_FAILED_CODE);
            throw cse;
        }

        String responseURI = getResponseURI();

        // TODO: Support customizable batching of messages.
        ActionMessage requestMessage = new ActionMessage(getObjectEncoding());

        if (amfHeaders != null) {
            for (MessageHeader header : amfHeaders)
                requestMessage.addHeader(header);
        }

        MessageBody amfMessage = new MessageBody(command, responseURI, arguments);
        requestMessage.addBody(amfMessage);

        // Setup for AMF message serializer
        actionContext.setRequestMessage(requestMessage);
        ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
        AmfMessageSerializer amfMessageSerializer = new AmfMessageSerializer();
        amfMessageSerializer.initialize(serializationContext, outBuffer, amfTrace);

        try {
            amfMessageSerializer.writeMessage(requestMessage);
            Object result = send(outBuffer);
            return result;
        } catch (Exception e) {
            if (e instanceof ClientStatusException)
                throw (ClientStatusException) e;
            if (e instanceof ServerStatusException)
                throw (ServerStatusException) e;
            // Otherwise, wrap into a ClientStatusException.
            throw new ClientStatusException(e, ClientStatusException.AMF_CALL_FAILED_CODE, generateHttpResponseInfo());
        } finally {
            try {
                outBuffer.close();
            } catch (IOException ignore) {
            }
        }
    }

    /**
     * Closes the underlying URL connection, sets the url to null, and clears
     * the cookies.
     */
    public void close() {
        // Clear the cookies.
        if (cookies != null)
            cookies.clear();

        // Clear the URL connection and URL.
        if (urlConnection != null) {
            urlConnection.disconnect();
            urlConnection = null;
        }
        url = null;
        urlObject = null;

        serializationContext = null;
        connected = false;
    }

    /**
     * Connects to the URL provided. Any previous connections are closed.
     *
     * @param connectUrl The url to connect to.
     * @throws ClientStatusException If there is a client side exception.
     */
    public void connect(String connectUrl) throws ClientStatusException {
        SerializationContext serializationContext = new SerializationContext();
        serializationContext.createASObjectForMissingType = true;
        // Make sure collections are written out as Arrays (vs. ArrayCollection),
        // in case the server does not recognize ArrayCollections.
        serializationContext.legacyCollection = true;
        // When legacyMap is true, Java Maps are serialized as ECMA arrays
        // instead of anonymous Object.
        serializationContext.legacyMap = true;
        connect(connectUrl, serializationContext);
    }

    /**
     * Connects to the URL provided. Any previous connections are closed.
     *
     * @param connectUrl           The url to connect to.
     * @param serializationContext The serialization context used to configure the serialization.
     * @throws ClientStatusException If there is a client side exception.
     */
    public void connect(String connectUrl, SerializationContext serializationContext) throws ClientStatusException {
        if (connected)
            close();

        url = connectUrl;

        // Try to encode the url in case it has spaces etc.
        String encodedUrl = null;
        try {
            URL raw = new URL(url);
            URI uri = new URI(raw.getProtocol(), raw.getUserInfo(), raw.getHost(), raw.getPort(), raw.getPath(), raw.getQuery(), null);
            encodedUrl = uri.toString();
        } catch (Exception e) {
            // NOWARN
        }

        try {
            urlObject = new URL(encodedUrl != null ? encodedUrl : url);
            this.serializationContext = serializationContext;
            internalConnect();
        } catch (IOException e) {
            ClientStatusException exception = new ClientStatusException(e, ClientStatusException.AMF_CONNECT_FAILED_CODE);
            throw exception;
        }
    }

    //--------------------------------------------------------------------------
    //
    // Protected Methods
    //
    //--------------------------------------------------------------------------

    /**
     * Generates the HTTP response info for the server status exception.
     *
     * @return The HTTP response info for the server status exception.
     */
    protected HttpResponseInfo generateHttpResponseInfo() {
        HttpResponseInfo httpResponseInfo = null;
        try {
            if (urlConnection != null) {
                int responseCode = urlConnection.getResponseCode();
                String responseMessage = urlConnection.getResponseMessage();
                httpResponseInfo = new HttpResponseInfo(responseCode, responseMessage, urlConnectionInputStream);
            }
        } catch (IOException ignore) {
        }
        return httpResponseInfo;
    }

    /**
     * Generates and returns the response URI.
     *
     * @return The response URI.
     */
    protected String getResponseURI() {
        String responseURI = "/" + responseCounter;
        responseCounter++;
        return responseURI;
    }

    /**
     * An internal method that sets up the underlying URL connection.
     *
     * @throws IOException If an exception is encountered during URL connection setup.
     */
    protected void internalConnect() throws IOException {
        if (proxy == null)
            urlConnection = (HttpURLConnection) urlObject.openConnection();
        else
            urlConnection = (HttpURLConnection) urlObject.openConnection(proxy);

        urlConnection.setDoOutput(true);
        setHttpRequestHeaders();
        serializationContext.instantiateTypes = instantiateTypes;
        actionContext = new ActionContext();
        connected = true;
    }

    /**
     * Processes the HTTP response headers and body.
     */
    protected Object processHttpResponse(InputStream inputStream) throws ClassNotFoundException, IOException, ClientStatusException, ServerStatusException {
        processHttpResponseHeaders();
        return processHttpResponseBody(inputStream);
    }

    /**
     * Processes the HTTP response body.
     */
    protected Object processHttpResponseBody(InputStream inputStream)
            throws ClassNotFoundException, IOException, ClientStatusException,
            ServerStatusException {
        if (urlConnectionInputStream != null)
            urlConnectionInputStream.close();
        urlConnectionInputStream = new BufferedInputStream(inputStream);
        // Mark the first 2 bytes so that the stream can be reset in case it
        // contains non-AMF data.
        urlConnectionInputStream.mark(2);
        ActionMessage message = new ActionMessage();
        actionContext.setRequestMessage(message);
        MessageDeserializer deserializer = new AmfMessageDeserializer();
        deserializer.initialize(serializationContext, urlConnectionInputStream, amfTrace);
        try {
            deserializer.readMessage(message, actionContext);
        } catch (MessageException me) {
            // Means the stream contained non-AMF data, reset the stream and throw.
            if (AmfMessageDeserializer.CODE_VERSION_MISMATCH.equals(me.getCode())) {
                urlConnectionInputStream.reset();
                String errorMessage = "Unsupported AMF version";
                throw new ClientStatusException(errorMessage, ClientStatusException.AMF_CALL_FAILED_CODE, generateHttpResponseInfo());
            }
            throw me;
        }
        return processAmfPacket(message);
    }

    /**
     * Processes the HTTP response headers.
     */
    protected void processHttpResponseHeaders() {
        Map<String, List<String>> headers = urlConnection.getHeaderFields();
        for (Map.Entry<String, List<String>> element : headers.entrySet()) {
            String headerName = element.getKey();
            List<String> headerValues = element.getValue();
            for (String headerValue : headerValues) {
                if (SET_COOKIE.equals(headerName) || COOKIE.equals(headerName)
                        || SET_COOKIE2.equals(headerName) || COOKIE2.equals(headerName))
                    processSetCookieHeader(headerValue);
            }
        }
    }

    /**
     * Processes the AMF packet.
     */
    protected Object processAmfPacket(ActionMessage packet) throws ServerStatusException {
        processAmfHeaders(packet.getHeaders());
        return processAmfBody(packet.getBodies());
    }

    /**
     * Processes the AMF headers by dispatching them to an AMF header processor,
     * if one exists.
     */
    protected void processAmfHeaders(ArrayList<MessageHeader> headers) {
        // No need to process headers if there's no AMF header processor.
        if (amfHeaderProcessor == null)
            return;

        for (MessageHeader header : headers)
            amfHeaderProcessor.processHeader(header);
    }

    /**
     * Processes the AMF body. Note that this method won't work if batching of
     * AMF messages is supported at some point but for now we are guaranteed to
     * have a single message.
     */
    protected Object processAmfBody(ArrayList<MessageBody> messages) throws ServerStatusException {
        for (MessageBody message : messages) {
            String targetURI = message.getTargetURI();

            if (targetURI.endsWith(MessageIOConstants.RESULT_METHOD)) {
                return message.getData();
            } else if (targetURI.endsWith(MessageIOConstants.STATUS_METHOD)) {
                String exMessage = "Server error";
                HttpResponseInfo responseInfo = generateHttpResponseInfo();
                ServerStatusException exception = new ServerStatusException(exMessage, message.getData(), responseInfo);
                throw exception;
            }
        }
        return null; // Should not happen.
    }

    /**
     * Writes the output buffer and processes the HTTP response.
     */
    protected Object send(ByteArrayOutputStream outBuffer) throws ClassNotFoundException, IOException, ClientStatusException, ServerStatusException {
        // Every Http request needs a new HttpURLConnection, hence the internalConnect.
        internalConnect();

        outBuffer.writeTo(urlConnection.getOutputStream());
        outBuffer.flush();
        outBuffer.close();

        // Process the response
        return processHttpResponse(urlConnection.getInputStream());
    }

    /**
     * Processes the incoming set-cookie headers.
     *
     * @param headerValue The value of the set-cookie header.
     */
    protected void processSetCookieHeader(String headerValue) {
        String cookie = headerValue;
        if (cookie.indexOf(COOKIE_SEPERATOR) > 0)
            cookie = headerValue.substring(0, cookie.indexOf(COOKIE_SEPERATOR));
        String name = cookie.substring(0, cookie.indexOf(COOKIE_NAMEVALUE_SEPERATOR));
        String value = cookie.substring(cookie.indexOf(COOKIE_NAMEVALUE_SEPERATOR) + 1, cookie.length());
        if (cookies == null)
            cookies = new HashMap<String, String>();
        cookies.put(name, value);
    }

    /**
     * Sets the Http request headers, including the cookie headers.
     */
    protected void setHttpRequestHeaders() {
        setHttpRequestCookieHeader();
        if (httpRequestHeaders != null) {
            for (Map.Entry<String, String> element : httpRequestHeaders.entrySet()) {
                String key = element.getKey();
                String value = element.getValue();
                urlConnection.setRequestProperty(key, value);
            }
        }
        // Always set valid Content-Type header (overrides any user-defined Content-Type).
        urlConnection.setRequestProperty(HTTP_HEADER_NAME_CONTENT_TYPE, flex.messaging.io.MessageIOConstants.AMF_CONTENT_TYPE);
    }

    /**
     * Sets the Http request cookie headers.
     */
    protected void setHttpRequestCookieHeader() {
        if (cookies == null)
            return;

        // Set the cookies, if any.
        StringBuffer cookieHeaderValue = null;
        for (Map.Entry<String, String> element : cookies.entrySet()) {
            String name = element.getKey();
            String value = element.getValue();
            if (cookieHeaderValue == null) // First cookie
                cookieHeaderValue = new StringBuffer(name + COOKIE_NAMEVALUE_SEPERATOR + value);
            else
                cookieHeaderValue.append(COOKIE_SEPERATOR + " " + name + COOKIE_NAMEVALUE_SEPERATOR + value);
        }
        if (cookieHeaderValue != null)
            urlConnection.setRequestProperty(COOKIE, cookieHeaderValue.toString());
    }

    //--------------------------------------------------------------------------
    //
    // Inner Classes
    //
    //--------------------------------------------------------------------------

    /**
     * An inner class to represent the HTTP response associated with the exception.
     */
    public static class HttpResponseInfo {
        private int responseCode;
        private String responseMessage;
        private InputStream responseInputStream;

        /**
         * Creates an HTTP response info with the HTTP code, message, and the
         * input stream.
         *
         * @param responseCode        The HTTP response code.
         * @param responseMessage     the HTTP message.
         * @param responseInputStream The underlying input stream.
         */
        public HttpResponseInfo(int responseCode, String responseMessage, InputStream responseInputStream) {
            this.responseCode = responseCode;
            this.responseMessage = responseMessage;
            this.responseInputStream = responseInputStream;
        }

        /**
         * Returns the HTTP response code.
         *
         * @return The HTTP response code.
         */
        public int getResponseCode() {
            return responseCode;
        }

        /**
         * Returns the HTTP response message.
         *
         * @return The HTTP response message.
         */
        public String getResponseMessage() {
            return responseMessage;
        }

        /**
         * Returns the underlying response input stream.
         *
         * @return The underlying response input stream.
         */
        public InputStream getResponseInputStream() {
            return responseInputStream;
        }

        /**
         * Returns a String representation of the HTTP response info.
         *
         * @return A String representation of the HTTP response info.
         */
        @Override
        public String toString() {
            return "HttpResponseInfo " + "\n\tcode: " + responseCode
                    + "\n\tmessage: " + responseMessage;
        }
    }
}
