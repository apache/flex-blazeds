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

import org.apache.commons.httpclient.HostConfiguration;

import java.net.URL;

/**
 *
 * Encapsulates information about a proxy target.
 */
public class Target
{
    // FIXME: this class turned out not to be as useful as originally thought.  Should move this information
    // directly into ProxyContext

    private URL url;
    private boolean useCustomAuthentication = true;
    private boolean isHTTPS;
    private String encodedPath;
    private String remoteUsername;
    private String remotePassword;
    private HostConfiguration hostConfig;

    public URL getUrl()
    {
        return url;
    }

    public void setUrl(URL url)
    {
        this.url = url;
    }

    public boolean isHTTPS()
    {
        return isHTTPS;
    }

    public void setHTTPS(boolean HTTPS)
    {
        isHTTPS = HTTPS;
    }

    public String getEncodedPath()
    {
        return encodedPath;
    }

    public void setEncodedPath(String encodedPath)
    {
        this.encodedPath = encodedPath;
    }

    public HostConfiguration getHostConfig()
    {
        return hostConfig;
    }

    public void setHostConfig(HostConfiguration hostConfig)
    {
        this.hostConfig = hostConfig;
    }

    public String getRemoteUsername()
    {
        return remoteUsername;
    }

    public void setRemoteUsername(String name)
    {
        remoteUsername = name;
    }

    public String getRemotePassword()
    {
        return remotePassword;
    }

    public void setRemotePassword(String pass)
    {
        remotePassword = pass;
    }

    public boolean useCustomAuthentication()
    {
        return useCustomAuthentication;
    }

    public void setUseCustomAuthentication(boolean b)
    {
        useCustomAuthentication = b;
    }
}
