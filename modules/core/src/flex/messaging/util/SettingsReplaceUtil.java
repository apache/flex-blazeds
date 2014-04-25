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
package flex.messaging.util;

import flex.messaging.MessageException;
import flex.messaging.FlexContext;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Methods for replacing tokens in config files
 * {context.root}, {context-root}, {server.name}, {server-name}, {server.port}, {server-port}.
 * 
 * @exclude
 */
public class SettingsReplaceUtil
{
    private static final int TOKEN_NOT_SUPPORTED = 10129;
    private static final int TOKEN_NOT_SUPPORTED_ANY = 10130;
    private static final int PARSE_ERROR_DYNAMIC_URL = 10131;

    public static final String SLASH_CONTEXT_PATH_TOKEN = "/{context.root}";
    public static final String CONTEXT_PATH_TOKEN = "{context.root}";
    public static final String CONTEXT_PATH_ALT_TOKEN = "{context-root}";
    public static final String SERVER_NAME_TOKEN = "{server.name}";
    public static final String SERVER_NAME_ALT_TOKEN = "{server-name}";
    public static final String SERVER_PORT_TOKEN = "{server.port}";
    public static final String SERVER_PORT_ALT_TOKEN = "{server-port}";

    /**
     * Replace {context.root} in the url string.
     * @param url the url string
     * @param contextPath the context path
     * @return String the url string after replacement
     */
    public static String replaceContextPath(String url, String contextPath)
    {
        String token = CONTEXT_PATH_TOKEN;
        int contextIndex = url.indexOf(CONTEXT_PATH_ALT_TOKEN);
        if (contextIndex != -1)
        {
            token = CONTEXT_PATH_ALT_TOKEN;
            url = StringUtils.substitute(url, CONTEXT_PATH_ALT_TOKEN, CONTEXT_PATH_TOKEN);
        }
        contextIndex = url.indexOf(CONTEXT_PATH_TOKEN);

        if ((contextPath == null) && (contextIndex != -1))
        {
            MessageException me = new MessageException();
            if (FlexContext.getHttpRequest() == null)
                me.setMessage(TOKEN_NOT_SUPPORTED, "0", new Object[] {token});
            else
                me.setMessage(TOKEN_NOT_SUPPORTED, new Object[] {token});
            throw me;
        }
        else if (contextPath != null)
        {
            if (contextIndex == 0)
            {
                url = contextPath + url.substring(CONTEXT_PATH_TOKEN.length());
            }
            else if (contextIndex > 0)
            {
                // Avoid adding //contextPath to URLs that have a /{context.root} pattern
                if (url.indexOf(SLASH_CONTEXT_PATH_TOKEN) != -1)
                {
                    url = StringUtils.substitute(url, SLASH_CONTEXT_PATH_TOKEN, contextPath);
                }
                else
                {
                    url = StringUtils.substitute(url, CONTEXT_PATH_TOKEN, contextPath);
                }
            }
        }

        return url;
    }

    public static String replaceAllTokensGivenServerName(String url, String contextPath, String serverName,
                                                         String serverPort, String serverProtocol)
    {
        if (url.startsWith("/"))
        {
            url = serverProtocol + "://{server.name}:{server.port}" + url;
        }
        url = SettingsReplaceUtil.replaceContextPath(url, contextPath);

        String token = SERVER_NAME_TOKEN;
        int serverNameIndex = url.indexOf(SERVER_NAME_ALT_TOKEN);
        if (serverNameIndex != -1)
        {
            token = SERVER_NAME_ALT_TOKEN;
            url = StringUtils.substitute(url, SERVER_NAME_ALT_TOKEN, SERVER_NAME_TOKEN);
        }

        serverNameIndex = url.indexOf(SERVER_NAME_TOKEN);
        if ((serverName == null) && (serverNameIndex != -1))
        {
            MessageException me = new MessageException();
            me.setMessage(TOKEN_NOT_SUPPORTED, new Object[] {token});
            throw me;
        }
        else if ((serverName != null) && (serverNameIndex != -1))
        {
            url = StringUtils.substitute(url, SERVER_NAME_TOKEN, serverName);
        }

        token = SERVER_PORT_TOKEN;
        int serverPortIndex = url.indexOf(SERVER_PORT_ALT_TOKEN);
        if (serverPortIndex != -1)
        {
            token = SERVER_PORT_ALT_TOKEN;
            url = StringUtils.substitute(url, SERVER_PORT_ALT_TOKEN, SERVER_PORT_TOKEN);
        }

        serverPortIndex = url.indexOf(SERVER_PORT_TOKEN);
        if ((serverPort == null) && (serverPortIndex != -1))
        {
            MessageException me = new MessageException();
            me.setMessage(TOKEN_NOT_SUPPORTED, new Object[] {token});
            throw me;
        }
        else if ((serverPort != null) && (serverPortIndex != -1))
        {
            url = StringUtils.substitute(url, SERVER_PORT_TOKEN, serverPort);
        }

        return updateIPv6(url);
    }

    /**
     * Replace all tokens in the urls.
     * @param urls a list of urls
     * @param contextPath the context path
     * @return Set a set of urls after replacement
     */
    public static Set replaceAllTokensCalculateServerName(List urls, String contextPath)
    {
        List contextParsedUrls = new ArrayList(urls.size());
        Set newURLs = new HashSet(urls.size());

        // first replace context path
        for (int i = 0; i < urls.size(); i++)
        {
            String url = (String)urls.get(i);
            url = url.toLowerCase().trim();
            url = SettingsReplaceUtil.replaceContextPath(url, contextPath);
            contextParsedUrls.add(url);

        }
        // then replace {server.name}
        replaceServerNameWithLocalHost(contextParsedUrls, newURLs);

        return newURLs;
    }


    /**
     * Replace {server.name} a horribly complicated way.  This is needed to support relative
     * URLs.  I would like for us to rethink this someday and find a better way to do this
     * <p/>
     * @param urls the list of urls to replace
     * @param newURLs the set of new urls
     */
    public static void replaceServerNameWithLocalHost(List urls, Set newURLs)
    {
        for (Iterator iterator = urls.iterator(); iterator.hasNext();)
        {
            String url = (String) iterator.next();
            url = url.toLowerCase().trim();

            String token = SERVER_PORT_TOKEN;
            int serverPortIndex = url.indexOf(SERVER_PORT_ALT_TOKEN);
            if (serverPortIndex != -1)
            {
                token = SERVER_PORT_ALT_TOKEN;
                url = StringUtils.substitute(url, SERVER_PORT_ALT_TOKEN, SERVER_PORT_TOKEN);
            }

            serverPortIndex = url.indexOf(SERVER_PORT_TOKEN);
            if (serverPortIndex != -1)
            {
                MessageException me = new MessageException();
                me.setMessage(TOKEN_NOT_SUPPORTED_ANY, new Object[] {token});
                throw me;
            }

            if (url.indexOf(SERVER_NAME_ALT_TOKEN) != 0)
            {
                StringUtils.substitute(url, SERVER_NAME_ALT_TOKEN, SERVER_NAME_TOKEN);
            }

            if (url.indexOf(SERVER_NAME_TOKEN) != 0)
            {

                try
                {
                    addLocalServerURL(url, "localhost", newURLs);
                    addLocalServerURL(url, "127.0.0.1", newURLs);
                    addLocalServerURL(url, "[::1]", newURLs); // for IPv6
                    
                    InetAddress local  = InetAddress.getLocalHost();
                    addInetAddress(local, url, newURLs);

                    // if we're using JDK 1.4 or higher, we use NetworkInterface to get the list of hostnames
                    // and IP addresses.
                    Enumeration e = NetworkInterface.getNetworkInterfaces();
                    while (e.hasMoreElements())
                    {
                        NetworkInterface address = (NetworkInterface)e.nextElement();
                        Enumeration e2 = (Enumeration)address.getInetAddresses();
                        while (e2.hasMoreElements())
                        {
                            local = (InetAddress) e2.nextElement();
                            addInetAddress(local, url, newURLs);
                        }
                    }
                }
                catch(Exception e)
                {
                    MessageException me = new MessageException();
                    me.setMessage(PARSE_ERROR_DYNAMIC_URL);
                    throw me;
                }
            }
            else {
                addParsedURL(url, newURLs);
            }
        }
    }

    private static void addInetAddress(InetAddress local, String url, Set newURLs) throws Exception
    {
        String localHostAddress = local.getHostAddress();
        if (localHostAddress != null)
        {
            addLocalServerURL(url, localHostAddress, newURLs);
        }

        String localHostName = local.getHostName();
        if (localHostName != null)
        {
            addLocalServerURL(url, localHostName, newURLs);

            InetAddress[] addrs = InetAddress.getAllByName(localHostName);
            for (int i = 0; i < addrs.length; i++)
            {
                InetAddress addr = addrs[i];
                String hostName = addr.getHostName();
                if (! hostName.equals(localHostName))
                {
                    addLocalServerURL(url, hostName, newURLs);
                }
                String hostAddress = addr.getHostAddress();
                if (! hostAddress.equals(localHostAddress))
                {
                    addLocalServerURL(url, hostAddress, newURLs);
                }
            }
        }
    }

    private static void addLocalServerURL(String url, String sub, Set newURLs)
    {
        String toSub = null;
        
        // if ipv6, then add square brackets
        if (sub.indexOf(":") != -1)
        {
            StringBuffer ipv6Sub = new StringBuffer("[");
            ipv6Sub.append(sub);
            ipv6Sub.append("]");
            toSub = ipv6Sub.toString();
        }
        else
        {
            toSub = sub;
        }
            
        String newUrl = StringUtils.substitute(url, SERVER_NAME_TOKEN, toSub);
        addParsedURL(newUrl, newURLs);
    }

    private static void addParsedURL(String url, Set newURLs)
    {
        if (! newURLs.contains(url))
        {
            newURLs.add(updateIPv6(url));
        }
    }
    
    /**
     * Update IPv6 IP address.
     * @param src the source IP address string
     * @return String the updated IP address string
     */
    public static String updateIPv6(String src)
    {
        // if the ip address has "[" and "]" then it's IPv6, check that it's long form as well
        if ((src != null) && (src.indexOf('[') != -1) && (src.indexOf(']') != -1))
        {
            // then, it's IPv6 and remove the square brackets and update to long form if required    
            int start = src.indexOf('[');
            int end = src.indexOf(']');

            StringBuffer updated = new StringBuffer(src.substring(0, start + 1));
            updated.append(updateToLongForm(src.substring(start + 1, end)));
            updated.append(src.substring(end));
            
            return updated.toString();
        }
        else
        {
            return src;
        }
    }

    protected static String updateToLongForm(String src)
    {
        // Let's see if the short form is in use.
        int numberOfTokens = 0;
        int doubleColonIndex = src.indexOf("::", 0);
        if (doubleColonIndex != -1)
        {
            String[] hexTokens = src.split("\\:");
            for (int i = 0; i < hexTokens.length; i++) 
            {
                if (!hexTokens[i].equals(""))
                    numberOfTokens++;
            }               

            // Replace missing zeros
            int numberOfMissingZeros = 8 - numberOfTokens;
            if (numberOfMissingZeros > 0)
            {
                String replacement = "";
                if (!src.startsWith("::"))
                    replacement = ":";
                while (numberOfMissingZeros-- > 0)
                    replacement += "0:";                
                src = src.replaceFirst("\\::", replacement);
            }
        }

        return src;
    }
    
}
