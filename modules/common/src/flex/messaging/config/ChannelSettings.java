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
package flex.messaging.config;
import flex.messaging.util.StringUtils;

/**
 * The channel configuration is intentionally generic so that
 * other channels can be added in the future. This format also allows
 * server-endpoint specific and client code-generation specific settings
 * to be modified without affecting needing to update the configuration
 * parser.
 *
 * @author Peter Farland
 * @exclude
 */
public class ChannelSettings extends PropertiesSettings
{
    protected String id;
    protected boolean remote;
    protected String serverId;
    private String sourceFile;

    protected SecurityConstraint constraint;

    // ENDPOINT
    protected String uri;
    protected int port;
    protected String endpointType;
    protected String clientType;
    protected boolean serverOnly;

    protected String parsedUri;
    protected boolean contextParsed;
    protected String parsedClientUri;
    protected boolean clientContextParsed;

    public ChannelSettings(String id)
    {
        this.id = id;
    }

    public String getId()
    {
        return id;
    }

    public boolean isRemote()
    {
        return remote;
    }

    public void setRemote(boolean value)
    {
        remote = value;
    }

    public String getServerId()
    {
        return serverId;
    }

    public void setServerId(String value)
    {
        serverId = value;
    }

    public String getClientType()
    {
        return clientType;
    }

    public void setClientType(String type)
    {
        this.clientType = type;
    }

    public boolean getServerOnly()
    {
        return serverOnly;
    }

    public void setServerOnly(boolean serverOnly)
    {
        this.serverOnly = serverOnly;
    }

    String getSourceFile()
    {
        return sourceFile;
    }

    void setSourceFile(String sourceFile)
    {
        this.sourceFile = sourceFile;
    }

    /**
     * A return value of 0 denotes no port in channel url.
     *
     * @return the port number for this channel
     * or 0 if channel url does not contain a port number
     */
    public int getPort()
    {
        return port;
    }

    public String getUri()
    {
        return uri;
    }

    public void setUri(String uri)
    {
        this.uri = uri;
        port = parsePort(uri);
        port = (port == -1)? 0 : port; // Replace -1 with 0.
        contextParsed = false;
        clientContextParsed = false;
    }

    public String getClientParsedUri(String contextPath)
    {
        if (!clientContextParsed)
            parseClientUri(this, contextPath);

        return parsedClientUri;
    }

    public String getEndpointType()
    {
        return endpointType;
    }

    public void setEndpointType(String type)
    {
        this.endpointType = type;
    }

    public SecurityConstraint getConstraint()
    {
        return constraint;
    }

    public void setConstraint(SecurityConstraint constraint)
    {
        this.constraint = constraint;
    }

    /**
     * In this client version of the URI parser we're just looking to
     * replace the context root tokens.
     */
    private static void parseClientUri(ChannelSettings cs, String contextPath)
    {
        if (!cs.clientContextParsed)
        {
            String channelEndpoint = cs.getUri().trim();

            // either {context-root} or {context.root} is legal
            channelEndpoint = StringUtils.substitute(channelEndpoint, "{context-root}", ConfigurationConstants.CONTEXT_PATH_TOKEN);

            if ((contextPath == null) && (channelEndpoint.indexOf(ConfigurationConstants.CONTEXT_PATH_TOKEN) != -1))
            {
                // context root must be specified before it is used
                ConfigurationException e = new ConfigurationException();
                e.setMessage(ConfigurationConstants.UNDEFINED_CONTEXT_ROOT, new Object[]{cs.getId()});
                throw e;
            }

            // simplify the number of combinations to test by ensuring our
            // context path always starts with a slash
            if (contextPath != null && !contextPath.startsWith("/"))
            {
                contextPath = "/" + contextPath;
            }

            // avoid double-slashes from context root by replacing /{context.root}
            // in a single replacement step
            if (channelEndpoint.indexOf(ConfigurationConstants.SLASH_CONTEXT_PATH_TOKEN) != -1)
            {
                // but avoid double-slash for /{context.root}/etc when we have
                // the default context root
                if ("/".equals(contextPath) && !ConfigurationConstants.SLASH_CONTEXT_PATH_TOKEN.equals(channelEndpoint))
                    contextPath = "";

                channelEndpoint = StringUtils.substitute(channelEndpoint, ConfigurationConstants.SLASH_CONTEXT_PATH_TOKEN, contextPath);
            }
            // otherwise we have something like {server.name}:{server.port}{context.root}...
            else
            {
                // but avoid double-slash for {context.root}/etc when we have
                // the default context root
                if ("/".equals(contextPath) && !ConfigurationConstants.CONTEXT_PATH_TOKEN.equals(channelEndpoint))
                    contextPath = "";

                channelEndpoint = StringUtils.substitute(channelEndpoint, ConfigurationConstants.CONTEXT_PATH_TOKEN, contextPath);
            }

            cs.parsedClientUri = channelEndpoint;
            cs.clientContextParsed = true;
        }
    }
    
    /**
     * Returns the host name in the URL, or an empty <tt>String</tt> if the URL does not contain a host name.
     * 
     * @param url The URL to parse for a host name.
     * @return The host name or an empty <tt>String</tt> if the URL does not contain a host name.
     */
    // The reason for this method, rather than just using java.net.URL, is that the Java class
    // doesn't recognize many of our protocol types/schemes (e.g. rtmp, amfsocket, etc.)
    public static String parseHost(String url)
    {
        int start = url.indexOf(":/");
        if (start == -1)
            return "";
        
        start = start + 3; // Advance past all of '://' to beginning of host name.
        int end = url.indexOf('/', start);
        String hostnameWithPort = end == -1 ? url.substring(start) : url.substring(start, end);
        
        // IPv6 hostnames may contain ':' but the full value is wrapped in [] - skip past if necessary.
        int delim = hostnameWithPort.indexOf(']');
        delim = (delim != -1) ? hostnameWithPort.indexOf(':', delim) : hostnameWithPort.indexOf(':');
        if (delim == -1) // No port.
            return hostnameWithPort;
        else
            return hostnameWithPort.substring(0, delim);
    }
    
    /**
     * Returns the port number specified in the URL, or 0 if the URL
     * does not contain a port number, or -1 if the URL contains a server.port
     * token.
     *
     * @param url The URL to parse for a contained port number.
     * @return the port number in the URL, or 0 if the URL does not 
     * contain a port number, or -1 if the URL contains a server.port token.
     */
    // The reason for this method, rather than just using java.net.URL, is that the Java class
    // doesn't recognize many of our protocol types/schemes (e.g. rtmp, amfsocket, etc.)
    public static int parsePort(String url)
    {
        // rtmp://localhost:2035/foo/bar
        // Find first slash with colon
        int start = url.indexOf(":/");
        if (start == -1)
            return 0;

        // Second slash should be +1, so start 3 after for ://
        start = start + 3;
        int end = url.indexOf('/', start);

        // take everything up until the next slash for servername:port
        String snp = end == -1 ? url.substring(start) : url.substring(start, end);

        // If IPv6 is in use, start looking after the square bracket.
        int delim = snp.indexOf(']');
        delim = (delim > -1)? snp.indexOf(':', delim) : snp.indexOf(':');
        if (delim == -1)
            return 0;

        int port = 0;
        try
        {
            int p = Integer.parseInt(snp.substring(delim + 1));
            port = (p > 0)? p : 0; 
        }
        catch (Throwable t)
        {
            port = -1; // To denote that the url contained server.port token.
        }
        return port;
    }

    /**
     * Remove protocol, host, port and context-root from a given url.
     * Unlike parseClientUri, this method does not check if the channel
     * setting has been parsed before.
     *
     * @param url Original url.
     * @return Url with protocol, host, port and context-root removed.
     */
    public static String removeTokens(String url)
    {
        String channelEndpoint = url.toLowerCase().trim();

        // remove protocol and host info
        if (channelEndpoint.startsWith("http://") ||
                channelEndpoint.startsWith("https://") ||
                channelEndpoint.startsWith("rtmp://") ||
                channelEndpoint.startsWith("rtmps://")) {
            int nextSlash = channelEndpoint.indexOf('/', 8);
            // Check to see if there is a 'next slash', and also that the next
            // slash isn't the last character
            if ((nextSlash > 0) && (nextSlash != channelEndpoint.length()-1))
                channelEndpoint = channelEndpoint.substring(nextSlash);
        }

        // either {context-root} or {context.root} is legal
        channelEndpoint = StringUtils.substitute(channelEndpoint, "{context-root}", ConfigurationConstants.CONTEXT_PATH_TOKEN);

        // Remove context path info
        if (channelEndpoint.startsWith(ConfigurationConstants.CONTEXT_PATH_TOKEN))
        {
            channelEndpoint = channelEndpoint.substring(ConfigurationConstants.CONTEXT_PATH_TOKEN.length());
        }
        else if (channelEndpoint.startsWith(ConfigurationConstants.SLASH_CONTEXT_PATH_TOKEN))
        {
            channelEndpoint = channelEndpoint.substring(ConfigurationConstants.SLASH_CONTEXT_PATH_TOKEN.length());
        }

        // We also don't match on trailing slashes
        if (channelEndpoint.endsWith("/"))
        {
            channelEndpoint = channelEndpoint.substring(0, channelEndpoint.length() - 1);
        }
        return channelEndpoint;
    }
}
