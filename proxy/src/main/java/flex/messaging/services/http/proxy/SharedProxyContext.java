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

/**
 * Store all the information needed for a proxy request that's used in flex.server.common.proxy.
 */
public class SharedProxyContext {
    // POST, GET, HEAD etc
    private String method;

    // often-used variables describing the type of request
    private boolean isSoapRequest;
    private boolean isHttpRequest;
    private boolean isClientHttps;

    // whether request has custom auth or Authorization header
    private boolean hasAuthorization;
    // whether endpoint is the same domain as proxy
    private boolean localDomain;
    // whether the endpoint has the same port as the proxy (always false if localDomain is false)
    private boolean localPort;
    // whether request needs browser caching disabled
    private boolean disableCaching;
    // whether target URL came from the client
    private boolean clientTarget;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public boolean isSoapRequest() {
        return isSoapRequest;
    }

    public void setSoapRequest(boolean s) {
        isSoapRequest = s;
    }

    public boolean isHttpRequest() {
        return isHttpRequest;
    }

    public void setHttpRequest(boolean h) {
        isHttpRequest = h;
    }

    public boolean isClientHttps() {
        return isClientHttps;
    }

    public void setClientHttps(boolean h) {
        isClientHttps = h;
    }

    public boolean hasAuthorization() {
        return hasAuthorization;
    }

    public void setAuthorization(boolean hasAuthorization) {
        this.hasAuthorization = hasAuthorization;
    }

    public boolean isLocalDomain() {
        return localDomain;
    }

    public void setLocalDomain(boolean localDomain) {
        this.localDomain = localDomain;
    }

    public boolean isLocalPort() {
        return localPort;
    }

    public void setLocalPort(boolean localPort) {
        this.localPort = localPort;
    }

    public boolean isLocalDomainAndPort() {
        return localDomain && localPort;
    }

    public boolean disableCaching() {
        return disableCaching;
    }

    public void setDisableCaching(boolean disableCaching) {
        this.disableCaching = disableCaching;
    }

    public boolean isClientTarget() {
        return clientTarget;
    }

    public void setClientTarget(boolean clientTarget) {
        this.clientTarget = clientTarget;
    }
}
