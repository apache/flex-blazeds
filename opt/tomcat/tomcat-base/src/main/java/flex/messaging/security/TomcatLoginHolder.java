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
 * Contains the last good TomcatLogin for use by the TomcatLoginCommand.
 */
public class TomcatLoginHolder {
    private static ThreadLocal logins = new ThreadLocal();

    // We should really make this one as a singleton instead of resetting it every time we call setLogin()
    private static TomcatLogin nioBasedLogin;

    private TomcatLoginHolder() {
        // No-op.
    }

    /**
     * Saves the last valid login.
     *
     * @param login last valid login
     */
    public static void setLogin(TomcatLogin login) {
        logins.set(login);
    }

    /**
     * Retrieves the last valid login.
     *
     * @return last valid login.
     */
    public static TomcatLogin getLogin() {
        return logins.get() != null ? (TomcatLogin) logins.get() : nioBasedLogin;
    }

    /**
     * Saves the nio based login.
     *
     * @param login the valid login that nio based endpoints should use
     */
    public static void setNioBasedLogin(TomcatLogin login) {
        nioBasedLogin = login;
    }
}
