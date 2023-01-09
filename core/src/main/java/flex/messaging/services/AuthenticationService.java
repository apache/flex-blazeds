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
package flex.messaging.services;

import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.concurrent.CopyOnWriteArrayList;

import flex.messaging.FlexContext;
import flex.messaging.FlexSession;
import flex.messaging.MessageBroker;
import flex.messaging.MessageException;
import flex.messaging.client.FlexClient;
import flex.messaging.config.ConfigMap;
import flex.messaging.endpoints.Endpoint;
import flex.messaging.log.Log;
import flex.messaging.log.LogCategories;
import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.Message;
import flex.messaging.security.LoginManager;
import flex.messaging.security.SecurityException;
import flex.messaging.util.Base64;

/**
 * Core service that is automatically created and registered with a <tt>MessageBroker</tt>.
 * It handles login and logout commands from clients.
 * The service implementation is internal, but customer code may look up the service by id
 * using <code>MessageBroker#getService(String)</code>, and register as an <tt>AuthenticationListener</tt> for
 * <tt>AuthenticationEvent</tt>s. An authentication event is dispatched following successful and after
 * successful logout.
 */
public class AuthenticationService extends AbstractService {
    private static final int INVALID_CREDENTIALS_ERROR = 10064;

    /**
     * The well-known id that the <tt>AuthenticationService</tt> is bound to the <tt>MessageBroker</tt> under.
     */
    public static final String ID = "authentication-service";

    /**
     *
     */
    public AuthenticationService() {
        this(false);
    }

    /**
     *
     */
    public AuthenticationService(boolean enableManagement) {
        // this service can never be managed
        super(false);
        super.setId(ID);
    }

    /**
     * Internal thread-safe storage for AuthenticationListeners.
     */
    private final CopyOnWriteArrayList<AuthenticationListener> authenticationListeners = new CopyOnWriteArrayList<AuthenticationListener>();

    /**
     * Registers an <tt>AuthenticationListener</tt> to receive <tt>AuthenticationEvent</tt>s.
     *
     * @param listener The <tt>AuthenticationListener</tt> to register.
     */
    public void addAuthenticationListener(final AuthenticationListener listener) {
        authenticationListeners.addIfAbsent(listener);
    }

    /**
     * Unregisters an <tt>AuthenticationListener</tt>.
     *
     * @param listener The <tt>AuthenticationListener</tt> to unregister.
     */
    public void removeAuthenticationListener(final AuthenticationListener listener) {
        authenticationListeners.remove(listener);
    }

    // This service's id should never be changed

    /**
     *
     */
    public void setId(String id) {
        // No-op
    }

    // This service should not be visible to the client

    /**
     *
     */
    public ConfigMap describeService(Endpoint endpoint) {
        return null;
    }

    /**
     *
     */
    public Object serviceMessage(Message message) {
        return null;
    }

    /**
     *
     */
    public Object serviceCommand(CommandMessage msg) {
        LoginManager lm = getMessageBroker().getLoginManager();
        switch (msg.getOperation()) {
            case CommandMessage.LOGIN_OPERATION:
                if (msg.getBody() instanceof String) {
                    String encoded = (String) msg.getBody();
                    Object charsetHeader = msg.getHeader(CommandMessage.CREDENTIALS_CHARSET_HEADER);
                    if (charsetHeader instanceof String)
                        decodeAndLoginWithCharset(encoded, lm, (String) charsetHeader);
                    else
                        decodeAndLoginWithCharset(encoded, lm, null);
                }
                break;
            case CommandMessage.LOGOUT_OPERATION:
                // Generate event first, to capture refs to Principal/FlexSession/etc.
                AuthenticationEvent logoutEvent = buildAuthenticationEvent(null, null); // null username and creds.
                lm.logout();
                // Success - notify listeners.
                for (AuthenticationListener listener : authenticationListeners) {
                    try {
                        listener.logoutSucceeded(logoutEvent);
                    } catch (Throwable t) {
                        if (Log.isError())
                            Log.getLogger(LogCategories.SECURITY).error("AuthenticationListener {0} threw an exception handling a logout event.", new Object[]{listener}, t);
                    }
                }
                break;
            default:
                throw new MessageException("Service Does Not Support Command Type " + msg.getOperation());
        }
        return "success";
    }

    /**
     *
     */
    @Override
    public void stop() {
        super.stop();
        authenticationListeners.clear();
    }

    /**
     *
     */
    public void decodeAndLogin(String encoded, LoginManager lm) {
        decodeAndLoginWithCharset(encoded, lm, null);
    }

    /**
     *
     */
    private void decodeAndLoginWithCharset(String encoded, LoginManager lm, String charset) {
        String username = null;
        String password = null;
        Base64.Decoder decoder = new Base64.Decoder();
        decoder.decode(encoded);
        String decoded = "";

        // Charset-aware decoding of the credentials bytes 
        if (charset != null) {
            try {
                decoded = new String(decoder.drain(), charset);
            } catch (UnsupportedEncodingException ex) {
            }
        } else {
            decoded = new String(decoder.drain());
        }

        int colon = decoded.indexOf(":");
        if (colon > 0 && colon < decoded.length() - 1) {
            username = decoded.substring(0, colon);
            password = decoded.substring(colon + 1);
        }

        if (username != null && password != null) {
            lm.login(username, password);

            // Success - notify listeners.
            AuthenticationEvent loginEvent = buildAuthenticationEvent(username, password);
            for (AuthenticationListener listener : authenticationListeners) {
                try {
                    listener.loginSucceeded(loginEvent);
                } catch (Throwable t) {
                    if (Log.isError())
                        Log.getLogger(LogCategories.SECURITY).error("AuthenticationListener {0} threw an exception handling a login event.", new Object[]{listener}, t);
                }
            }
        } else {
            SecurityException se = new SecurityException();
            se.setCode(SecurityException.CLIENT_AUTHENTICATION_CODE);
            se.setMessage(INVALID_CREDENTIALS_ERROR);
            throw se;
        }
    }

    /**
     *
     */
    protected void setupServiceControl(MessageBroker broker) {
        // not doing anything
    }

    /**
     * Utility method to build an <tt>AuthenticationEvent</tt> based on the current
     * thread-local state for the remote user.
     *
     * @return An <tt>AuthenticationEvent</tt> that captures references to the server state for the remote user.
     */
    private AuthenticationEvent buildAuthenticationEvent(String username, Object credentials) {
        Principal principal = FlexContext.getUserPrincipal();
        FlexClient flexClient = FlexContext.getFlexClient();
        FlexSession flexSession = FlexContext.getFlexSession();
        return new AuthenticationEvent(this, username, credentials, principal, flexSession, flexClient);
    }
}
