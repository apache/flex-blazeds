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

package flex.messaging.security;

/**
 * Extensions to the LoginCommand interface.
 */
public interface LoginCommandExt
{
    /**
     * Classes that implement the flex.messaging.security.LoginCommand interface, should also
     * implement this interface if the name stored in java.security.Principal created as a result
     * of a succesfull authentication differs from the username that is actually passed in to
     * the authentication.
     *
     * Implementing this interace gives such LoginCommand's a chance to return the resulting
     * username so that it can be compared to the one stored in Principal.
     *
     * Returns the value that would be returned by java.security.Principal.getName() if
     * username/credentials had been authenticated
     *
     * @param username - User whose comparable name will be retrieved
     * @param credentials - Credentials for user whose comparable name will be retrieved
     * @return - value that would be returned by java.security.Principal.getName() if
     *           username/credentials had been authenticated
     */
    String getPrincipalNameFromCredentials(String username, Object credentials);

}
