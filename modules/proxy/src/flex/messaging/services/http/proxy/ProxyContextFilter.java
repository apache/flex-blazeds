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

import flex.messaging.services.http.httpclient.EasySSLProtocolSocketFactory;
import flex.messaging.services.HTTPProxyService;
import flex.messaging.FlexContext;
import flex.messaging.log.Log;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.httpclient.util.URIUtil;

import javax.servlet.http.HttpServletRequest;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

/**
 *
 * Fills in ProxyContext data for use by ProxyFilters within HttpProxyAdapter.
 */
public class ProxyContextFilter extends ProxyFilter
{
    private static final int RELATIVE_NOT_SUPPORTED = 10704;
    private static final int INVALID_TARGET = 10705;

    private static final String STRING_LOCALHOST = "localhost";

    private Protocol myhttps = new Protocol("https", (ProtocolSocketFactory)(new EasySSLProtocolSocketFactory()), 443);

    /**
     * Invokes the filter with the context.
     *
     * @param context The proxy context.
     */
    public void invoke(ProxyContext context)
    {
        setupInitialProperties(context);
        setupTarget(context);
        logInfo(context);

        if (next != null)
        {
            next.invoke(context);
        }
    }

    protected void setupInitialProperties(ProxyContext context)
    {
        HttpServletRequest clientRequest = FlexContext.getHttpRequest();
        if (clientRequest != null)
        {
            String reqURL = clientRequest.getRequestURL().toString();
            int idx = reqURL.indexOf(':');
            String reqProto = reqURL.substring(0, idx);
            context.setClientHttps(reqProto.equalsIgnoreCase("https"));

            // set up disableCaching variable since its used in sendException
            String userAgent = clientRequest.getHeader("User-Agent");
            context.setDisableCaching(context.isClientHttps() && userAgent != null && userAgent.indexOf("MSIE") != -1);
        }
    }

    protected void setupTarget(ProxyContext context)
    {
        Target target = context.getTarget();
        String source = context.getUrl();
        HttpServletRequest clientRequest = FlexContext.getHttpRequest();

        try
        {
            target.setUrl(new URL(source));
        }
        catch (MalformedURLException e)
        {
            try
            {
                //[Pete] Enhancement Req. 80172 - relative URLs from webroot (not
                //       webapp context root)
                if (clientRequest != null)
                {
                    String baseurl = "http" + (clientRequest.isSecure() ? "s" : "") + "://"
                            + clientRequest.getServerName() + ":" + clientRequest.getServerPort();

                    target.setUrl(new URL(baseurl + source));
                }
                else
                {
                    ProxyException pe = new ProxyException();
                    pe.setMessage(RELATIVE_NOT_SUPPORTED, new Object[] { source });
                    throw pe;
                }
            }
            catch (MalformedURLException ex)
            {
                target.setUrl(null);
            }
        }

        if (target.getUrl() == null)
        {
            ProxyException pe = new ProxyException();
            pe.setMessage(INVALID_TARGET, new Object[] { source });
            throw pe;
        }

        target.setHTTPS(target.getUrl().getProtocol().equalsIgnoreCase("https"));
        target.setEncodedPath(target.getUrl().getPath());
        String queryStr = target.getUrl().getQuery();
        if (queryStr != null)
        {
            target.setEncodedPath(target.getEncodedPath() + ("?" + queryStr));
        }
        try {
            target.setEncodedPath(URIUtil.encodePathQuery(target.getEncodedPath()));
        } catch (URIException e) {
            // exception is thrown if the default charset is not supported.
            // proceed with the provided URL.
        }

        target.setHostConfig(new HostConfiguration());
        String targetHost = target.getUrl().getHost();
        int targetPort = target.getUrl().getPort();

        // Check for a custom protocol
        Protocol customProtocol = context.getProtocol();
        if (customProtocol != null)
        {
            target.getHostConfig().setHost(targetHost, targetPort, customProtocol);
        }
        else if (target.isHTTPS() && context.allowLaxSSL())
        {
            target.getHostConfig().setHost(targetHost, targetPort, myhttps);
        }
        else
        {
            String targetProtocol = target.getUrl().getProtocol();
            target.getHostConfig().setHost(targetHost, targetPort, targetProtocol);
        }

        if (context.getConnectionManager() != null)
        {
            context.setHttpClient(new HttpClient(context.getConnectionManager()));
        }
        else
        {
            context.setHttpClient(new HttpClient());
        }

        // Determine if target domain matches this proxy's domain and port
        boolean localDomain = false;
        boolean localPort = false;
        if (clientRequest != null)
        {
            String proxyDomain = clientRequest.getServerName().contains(STRING_LOCALHOST)?
                    getResolvedLocalhost() : clientRequest.getServerName();
            String resolvedTargetHost = targetHost.contains(STRING_LOCALHOST)? getResolvedLocalhost() : targetHost;
            if (proxyDomain.equalsIgnoreCase(resolvedTargetHost))
            {
                localDomain = true;
                int proxyPort = clientRequest.getServerPort();
                localPort = proxyPort == targetPort;
            }
        }
        context.setLocalDomain(localDomain);
        context.setLocalPort(localPort);
    }

    protected void logInfo(ProxyContext context)
    {
        if (Log.isInfo())
        {
            Target target = context.getTarget();
            String prefix = "-- " + context.getMethod() + " : ";
            int targetPort = target.getUrl().getPort();
            String targetURL = target.getUrl().getProtocol() + "://" + target.getUrl().getHost() +
                    (targetPort == -1 ? "" : ":" + targetPort) + target.getEncodedPath();

            Log.getLogger(HTTPProxyService.LOG_CATEGORY).info(prefix + targetURL);
        }
    }

    /**
     * Returns the IP of the localhost.
     *
     * @return The IP of the localhost.
     */
    private String getResolvedLocalhost()
    {
        try
        {
            return InetAddress.getLocalHost().getHostAddress();
        }
        catch (UnknownHostException e)
        {
            // NOWARN
        }
        return null;
    }
}
