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
package flex.messaging.services.messaging.adapters;

import flex.messaging.FlexContext;
import flex.messaging.MessageBroker;
import flex.messaging.config.ConfigurationConstants;
import flex.messaging.config.ConfigMap;
import flex.messaging.config.SecurityConstraint;
import flex.messaging.config.SecuritySettings;
import flex.messaging.security.LoginManager;
import flex.messaging.security.SecurityException;

/**
 * Messaging security constraint managers are used by messaging destinations
 * to assert authorization of send and subscribe operations.
 */
public final class MessagingSecurityConstraintManager
{
    public static final String SEND_SECURITY_CONSTRAINT = "send-security-constraint";
    public static final String SUBSCRIBE_SECURITY_CONSTRAINT = "subscribe-security-constraint";

    private static final int NO_SEC_CONSTRAINT = 10062;

    private LoginManager loginManager;
    private SecuritySettings securitySettings;

    private SecurityConstraint sendConstraint;
    private SecurityConstraint subscribeConstraint;

    /**
     * Creates a new <code>MessagingSecurityConstraintManager</code> instance.
     *
     * @param broker Associated <code>MessageBroker</code>.
     */
    public MessagingSecurityConstraintManager(MessageBroker broker)
    {
        this.loginManager = broker.getLoginManager();
        this.securitySettings = broker.getSecuritySettings();
    }

    /**
     * Sets the send constraint which is used when to assert authorization when
     * sending a message.
     *
     * @param ref The reference id of the constraint
     */
    public void setSendConstraint(String ref)
    {
        validateConstraint(ref);
        sendConstraint = securitySettings.getConstraint(ref);
    }

    /**
     * Sets the subscribe constraint which is used to assert authorization in
     * subscribe, multi-subscribe, and unsubscribe operations.
     *
     * @param ref The reference id of the constraint
     */
    public void setSubscribeConstraint(String ref)
    {
       validateConstraint(ref);
       subscribeConstraint = securitySettings.getConstraint(ref);
    }

    /**
     *
     * Asserts send authorizations.
     */
    public void assertSendAuthorization()
    {
        checkConstraint(sendConstraint);
    }

    /**
     *
     * Asserts subscribe authorizations.
     */
    public void assertSubscribeAuthorization()
    {
        checkConstraint(subscribeConstraint);
    }

    /**
     *
     * Creates security constraints from the given server settings.
     *
     * @param serverSettings The <code>ConfigMap</code> of server settings.
     */
    public void createConstraints(ConfigMap serverSettings)
    {
        // Send constraint
        ConfigMap send = serverSettings.getPropertyAsMap(SEND_SECURITY_CONSTRAINT, null);
        if (send != null)
        {
            String ref = send.getPropertyAsString(ConfigurationConstants.REF_ATTR, null);
            createSendConstraint(ref);
        }

        // Subscribe constraint
        ConfigMap subscribe = serverSettings.getPropertyAsMap(SUBSCRIBE_SECURITY_CONSTRAINT, null);
        if (subscribe != null)
        {
            String ref = subscribe.getPropertyAsString(ConfigurationConstants.REF_ATTR, null);
            createSubscribeConstraint(ref);
        }
    }

    void createSendConstraint(String ref)
    {
        if (ref != null)
            sendConstraint = securitySettings.getConstraint(ref);
    }

    void createSubscribeConstraint(String ref)
    {
        if (ref != null)
            subscribeConstraint = securitySettings.getConstraint(ref);
    }

    private void checkConstraint(SecurityConstraint constraint)
    {
        if (constraint != null && !FlexContext.isMessageFromPeer())
        {
            try
            {
                loginManager.checkConstraint(constraint);
            }
            catch (SecurityException e)
            {
                throw e;
            }
        }
    }

    private void validateConstraint(String ref)
    {
        // If an attempt is made to use a constraint that we do not know about,
        // do not let the authorization succeed.
        if (securitySettings.getConstraint(ref) == null)
        {
            // Security constraint {0} is not defined.
            SecurityException se = new SecurityException();
            se.setMessage(NO_SEC_CONSTRAINT, new Object[] {ref});
            throw se;
        }
    }
}