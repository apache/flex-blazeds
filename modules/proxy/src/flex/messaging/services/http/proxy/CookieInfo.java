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
 * @exclude
 */
public class CookieInfo
{
    public String clientName;
    public String domain;
    public String name;
    public String value;
    public String path;
    // for Java
    public int maxAge;
    // for .NET
    public Object maxAgeObj;
    public boolean secure;

    public CookieInfo(String clientName, String domain, String name, String value, String path,
                      int maxAge, Object maxAgeObj, boolean secure)
    {
        this.clientName = clientName;
        this.domain = domain;
        this.name = name;
        this.value = value;
        this.path = path;
        this.maxAge = maxAge;
        this.maxAgeObj = maxAgeObj;
        this.secure = secure;
    }

    public String toString()
    {
        return "domain = '" + domain +
                "', path = '" + path +
                "', client name = '" + clientName +
                "', endpoint name = '" + name +
                "', value = '" + value;
    }
}
