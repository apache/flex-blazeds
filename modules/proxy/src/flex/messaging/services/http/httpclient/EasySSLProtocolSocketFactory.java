/*
 * $Header: /home/cvspublic/jakarta-commons/httpclient/src/contrib/org/apache/commons/httpclient/contrib/ssl/EasySSLProtocolSocketFactory.java,v 1.7 2004/06/11 19:26:27 olegk Exp $
 * $Revision: 1.7 $
 * $Date: 2004/06/11 19:26:27 $
 *
 * ====================================================================
 *
 *  Copyright 2002-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

/*
 * This file has been modified by Adobe (Adobe Systems Incorporated).
 * Date: Mar 27, 2008 onwards.
 * Reason(s): Fixed a few checkstyle warnings.
 */

package flex.messaging.services.http.httpclient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.HttpClientError;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.ControllerThreadSocketFactory;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import flex.messaging.util.Trace;

/**
 *
 * <p>
 * EasySSLProtocolSocketFactory can be used to creats SSL {@link Socket}s
 * that accept self-signed certificates.
 * </p>
 * <p>
 * This socket factory SHOULD NOT be used for productive systems
 * due to security reasons, unless it is a concious decision and
 * you are perfectly aware of security implications of accepting
 * self-signed certificates
 * </p>
 * <p>
 * Example of using custom protocol socket factory for a specific host:
 * <pre>
 *  Protocol easyhttps =
 *  new Protocol("https", new EasySSLProtocolSocketFactory(), 443);
 * </pre>
 * </p>
 * <p>
 * <pre>
 *     HttpClient client = new HttpClient();
 *     client.getHostConfiguration().setHost("localhost", 443, easyhttps);
 *     // use relative url only
 *     GetMethod httpget = new GetMethod("/");
 *     client.executeMethod(httpget);
 * </pre>
 * </p>
 * <p>
 * Example of using custom protocol socket factory per default instead of the standard one:
 * <pre>
 *     Protocol easyhttps = new Protocol("https", new EasySSLProtocolSocketFactory(), 443);
 *     Protocol.registerProtocol("https", easyhttps);
 *     HttpClient client = new HttpClient();
 *     GetMethod httpget = new GetMethod("https://localhost/");
 *     client.executeMethod(httpget);
 * </pre>
 * </p>
 *
 * <p>
 * DISCLAIMER: HttpClient developers DO NOT actively support this component.
 * The component is provided as a reference material, which may be inappropriate
 * for use without additional customization.
 * </p>
 */
public class EasySSLProtocolSocketFactory implements SecureProtocolSocketFactory
{
    private SSLContext sslcontext = null;

    /**
     * Constructor for EasySSLProtocolSocketFactory.
     */
    public EasySSLProtocolSocketFactory()
    {
        super();
    }

    private static SSLContext createEasySSLContext()
    {
        try
        {
            SSLContext context = SSLContext.getInstance("SSL");
            context.init(null,
                    new TrustManager[]{new EasyX509TrustManager(null)},
                    null);
            return context;
        }
        catch (Exception e)
        {
            if (Trace.ssl)
            {
                Trace.trace(e.getMessage());
            }
            throw new HttpClientError(e.toString());
        }
    }

    private SSLContext getSSLContext()
    {
        if (this.sslcontext == null)
        {
            this.sslcontext = createEasySSLContext();
        }
        return this.sslcontext;
    }

    /** {@inheritDoc} */
    public Socket createSocket(String host,
                               int port,
                               InetAddress clientHost,
                               int clientPort)
            throws IOException {

        return getSSLContext().getSocketFactory().createSocket(host,
                port,
                clientHost,
                clientPort);
    }

    /**
     * Attempts to get a new socket connection to the given host within the given time limit.
     * <p>
     * To circumvent the limitations of older JREs that do not support connect timeout a
     * controller thread is executed. The controller thread attempts to create a new socket
     * within the given limit of time. If socket constructor does not return until the
     * timeout expires, the controller terminates and throws an {@link ConnectTimeoutException}
     * </p>
     *
     * @param host         the host name/IP
     * @param port         the port on the host
     * @param localAddress the local host name/IP to bind the socket to
     * @param localPort    the port on the local machine
     * @param params       {@link HttpConnectionParams Http connection parameters}
     * @return Socket a new socket
     * @throws IOException          if an I/O error occurs while creating the socket
     * @throws UnknownHostException if the IP address of the host cannot be
     *                              determined
     */
    public Socket createSocket(final String host,
                               final int port,
                               final InetAddress localAddress,
                               final int localPort,
                               final HttpConnectionParams params) throws IOException {
        if (params == null)
        {
            throw new IllegalArgumentException("Parameters may not be null");
        }
        int timeout = params.getConnectionTimeout();
        if (timeout == 0)
        {
            return createSocket(host, port, localAddress, localPort);
        }
        else
        {
            // To be eventually deprecated when migrated to Java 1.4 or above
            return ControllerThreadSocketFactory.createSocket(this, host, port, localAddress, localPort, timeout);
        }
    }

    /** {@inheritDoc} */
    public Socket createSocket(String host, int port)
            throws IOException {
        return getSSLContext().getSocketFactory().createSocket(host,
                port);
    }

    /** {@inheritDoc} */
    public Socket createSocket(Socket socket,
                               String host,
                               int port,
                               boolean autoClose)
            throws IOException {
        return getSSLContext().getSocketFactory().createSocket(socket,
                host,
                port,
                autoClose);
    }

    /** {@inheritDoc} */
    public boolean equals(Object obj)
    {
        return ((obj != null) && obj.getClass().equals(EasySSLProtocolSocketFactory.class));
    }

    /**
     * Return hash code of this object.
     * @return int hash code of this object
     */
    public int hashCode()
    {
        return EasySSLProtocolSocketFactory.class.hashCode();
    }

}
