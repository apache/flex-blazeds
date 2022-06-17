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

/**
 * This configuration class is derived from optional properties that
 * may be supplied in the &lt;properties&gt; section of a destination.
 * It exists to capture properties related to message throttling in a way
 * that simplifies the ThrottleManager's usage of the configuration.
 */
public class ThrottleSettings {
    /**
     * The Policy enum.
     */
    public enum Policy {
        NONE,
        ERROR,
        IGNORE,
        BUFFER,
        CONFLATE
    }

    ;


    public static final String ELEMENT_INBOUND = "throttle-inbound";

    public static final String ELEMENT_OUTBOUND = "throttle-outbound";

    public static final String ELEMENT_POLICY = "policy";

    public static final String ELEMENT_DEST_FREQ = "max-frequency";

    public static final String ELEMENT_CLIENT_FREQ = "max-client-frequency";

    // Errors
    private static final int ERR_MSG_INVALID_INBOUND_POLICY = 11130;
    private static final int ERR_MSG_INVALID_INCOMING_CLENT_FREQ = 11131;
    private static final int ERR_MSG_INVALID_INCOMING_DEST_FREQ = 11132;
    private static final int ERR_MSG_INVALID_OUTGOING_CLIENT_FREQ = 11133;
    private static final int ERR_MSG_INVALID_OUTGOING_DEST_FREQ = 11134;
    private static final int ERR_MSG_INVALID_NEGATIVE_VALUE = 11135;

    private String destinationName;
    private int inClientMessagesPerSec;
    private int inDestinationMessagesPerSec;
    private int outClientMessagesPerSec;
    private int outDestinationMessagesPerSec;
    private Policy inPolicy;
    private Policy outPolicy;

    /**
     * Creates a <code>ThrottleSettings</code> instance with default settings.
     */
    public ThrottleSettings() {
        inPolicy = Policy.NONE;
        outPolicy = Policy.NONE;
    }

    /**
     * Parses the throttle policy out of the given string.
     *
     * @param policy The string policy to parse.
     * @return The Policy.
     */
    public static Policy parsePolicy(String policy) {
        if (Policy.NONE.toString().equalsIgnoreCase(policy))
            return Policy.NONE;
        else if (Policy.IGNORE.toString().equalsIgnoreCase(policy))
            return Policy.IGNORE;
        else if (Policy.ERROR.toString().equalsIgnoreCase(policy))
            return Policy.ERROR;
        else if (Policy.BUFFER.toString().equalsIgnoreCase(policy))
            return Policy.BUFFER;
        else if (Policy.CONFLATE.toString().equalsIgnoreCase(policy))
            return Policy.CONFLATE;

        ConfigurationException ex = new ConfigurationException();
        // Message will be set by the caller.
        throw ex;
    }

    /**
     * Returns true if inbound or outbound client throttling is enabled.
     *
     * @return True if the incoming client frequency or outgoing
     * client frequency is enabled; otherwise false.
     */
    public boolean isClientThrottleEnabled() {
        return isInboundClientThrottleEnabled() || isOutboundClientThrottleEnabled();
    }

    /**
     * Returns true if inbound client throttling is enabled.
     *
     * @return True if the inbound client throttling is enabled.
     */
    public boolean isInboundClientThrottleEnabled() {
        return inPolicy != Policy.NONE && getIncomingClientFrequency() > 0;
    }

    /**
     * Returns true if outbound client throttling is enabled.
     *
     * @return True if the outbound client throttling is enabled.
     */
    public boolean isOutboundClientThrottleEnabled() {
        return outPolicy != Policy.NONE && getOutgoingClientFrequency() > 0;
    }

    /**
     * Returns true if inbound or outbound destination throttling is enabled.
     *
     * @return true if incoming or outbound destination throttling is enabled;
     * otherwise false.
     */
    public boolean isDestinationThrottleEnabled() {
        return isInboundDestinationThrottleEnabled() || isOutboundDestinationThrottleEnabled();
    }

    /**
     * Returns true if inbound destination throttling is enabled.
     *
     * @return true if inbound destination throttling is enabled.
     */
    public boolean isInboundDestinationThrottleEnabled() {
        return inPolicy != Policy.NONE && getIncomingDestinationFrequency() > 0;
    }

    /**
     * Returns true if outbound destination throttling is enabled.
     *
     * @return true if outbound destination throttling is enabled.
     */
    public boolean isOutboundDestinationThrottleEnabled() {
        return outPolicy != Policy.NONE && getOutgoingDestinationFrequency() > 0;
    }

    /**
     * Returns the inbound throttle policy.
     *
     * @return the inbound throttle policy.
     */
    public Policy getInboundPolicy() {
        return inPolicy;
    }

    /**
     * Sets inbound throttle policy. The inbound policy may be NONE, ERROR, or IGNORE.
     *
     * @param inPolicy The inbound policy.
     */
    public void setInboundPolicy(Policy inPolicy) {
        if (inPolicy != Policy.NONE && inPolicy != Policy.ERROR && inPolicy != Policy.IGNORE) {
            ConfigurationException ex = new ConfigurationException();
            // Invalid inbound throttle policy ''{0}'' for destination ''{1}''. Valid values are 'NONE', 'ERROR', and 'IGNORE'.
            ex.setMessage(ERR_MSG_INVALID_INBOUND_POLICY, new Object[]{inPolicy, destinationName});
            throw ex;
        }
        this.inPolicy = inPolicy;
    }

    /**
     * Returns the outbound throttle policy.
     *
     * @return the outbound throttle policy.
     */
    public Policy getOutboundPolicy() {
        return outPolicy;
    }

    /**
     * Sets the outbound throttle policy. The outbound policy can be NONE, IGNORE,
     * BUFFER, or CONFLATE.
     *
     * @param outPolicy The outbound policy.
     */
    public void setOutboundPolicy(Policy outPolicy) {
        // Policy is checked at throttle manager.
        this.outPolicy = outPolicy;
    }

    /**
     * Returns the destination name for <code>ThrottleSettings</code>.
     *
     * @return the destination name for <code>ThrottleSettings</code>.
     */
    public String getDestinationName() {
        return destinationName;
    }

    /**
     * Sets the destination name for <code>ThrottleSettings</code>. This is set
     * automatically when <code>NetworkSettings</code> is assigned to a destination.
     *
     * @param destinationName The destination name.
     */
    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    /**
     * Returns the incoming client frequency (max-client-frequency).
     *
     * @return The incoming client frequency (max-client-frequency).
     */
    public int getIncomingClientFrequency() {
        return inClientMessagesPerSec;
    }

    /**
     * Sets the incoming client frequency (max-client-frequency). Optional and the
     * default value is 0. Note that the incoming client frequency cannot be more
     * than the incoming destination frequency.
     *
     * @param n The incoming client frequency.
     */
    public void setIncomingClientFrequency(int n) {
        String name = "incoming client frequency";
        blockNegative(n, name);

        if (inDestinationMessagesPerSec > 0 && n > inDestinationMessagesPerSec) {
            ConfigurationException ex = new ConfigurationException();
            // Invalid {0} for destination ''{1}''. {0} ''{2}'' cannot be more than the incoming destination frequency ''{3}''.
            ex.setMessage(ERR_MSG_INVALID_INCOMING_CLENT_FREQ, new Object[]{name, destinationName,
                    Integer.valueOf(n), Integer.valueOf(inDestinationMessagesPerSec)});
            throw ex;
        }
        this.inClientMessagesPerSec = n;
    }

    /**
     * Returns the incoming destination frequency (max-frequency).
     *
     * @return The incoming destination frequency (max-frequency).
     */
    public int getIncomingDestinationFrequency() {
        return inDestinationMessagesPerSec;
    }

    /**
     * Sets the incoming destination frequency (max-frequency). Optional and the
     * default value is 0. Note that the incoming destination frequency cannot be
     * less than the incoming client frequency.
     *
     * @param n The incoming destination frequency.
     */
    public void setIncomingDestinationFrequency(int n) {
        String name = "The incoming destination frequency";
        blockNegative(n, name);

        if (inClientMessagesPerSec > 0 && n < inClientMessagesPerSec) {
            ConfigurationException ex = new ConfigurationException();
            // Invalid {0} for destination ''{1}''. {0} ''{2}'' cannot be less than the incoming client frequency ''{3}''.
            ex.setMessage(ERR_MSG_INVALID_INCOMING_DEST_FREQ, new Object[]{name, destinationName,
                    Integer.valueOf(n), Integer.valueOf(inClientMessagesPerSec)});
            throw ex;
        }
        this.inDestinationMessagesPerSec = n;
    }

    /**
     * Returns the outgoing client frequency (max-client-frequency).
     *
     * @return The outgoing client frequency (max-client-frequency).
     */
    public int getOutgoingClientFrequency() {
        return outClientMessagesPerSec;
    }

    /**
     * Sets the outgoing client frequency (max-client-frequency). Optional and the
     * default value is 0. Note that the outgoing client frequency cannot be
     * more than the outgoing destination frequency.
     *
     * @param n The outgoing client frequency.
     */
    public void setOutgoingClientFrequency(int n) {
        String name = "The outgoing client frequency";
        blockNegative(n, name);

        if (outDestinationMessagesPerSec > 0 && n > outDestinationMessagesPerSec) {
            ConfigurationException ex = new ConfigurationException();
            // Invalid {0} for destination ''{1}''. {0} ''{2}'' cannot be more than the outgoing destination frequency ''{3}''.
            ex.setMessage(ERR_MSG_INVALID_OUTGOING_CLIENT_FREQ, new Object[]{name, destinationName,
                    Integer.valueOf(n), Integer.valueOf(outDestinationMessagesPerSec)});
            throw ex;
        }
        this.outClientMessagesPerSec = n;
    }

    /**
     * Returns the outgoing destination frequency (max-frequency).
     *
     * @return The outgoing destination frequency (max-frequency).
     */
    public int getOutgoingDestinationFrequency() {
        return outDestinationMessagesPerSec;
    }

    /**
     * Sets the outgoing destination frequency (max-frequency). Optional and the
     * default value is 0. Note that the outgoing destination frequency cannot
     * be less than the outgoing client frequency.
     *
     * @param n The outgoing destination frequency.
     */
    public void setOutgoingDestinationFrequency(int n) {
        String name = "The outgoing destination frequency";
        blockNegative(n, name);

        if (outClientMessagesPerSec > 0 && n < outClientMessagesPerSec) {
            ConfigurationException ex = new ConfigurationException();
            // Invalid {0} for destination ''{1}''. {0} ''{2}'' cannot be less than the outgoing client frequency ''{3}''.
            ex.setMessage(ERR_MSG_INVALID_OUTGOING_DEST_FREQ, new Object[]{name, destinationName, Integer.valueOf(n),
                    Integer.valueOf(outClientMessagesPerSec)});
            throw ex;
        }
        this.outDestinationMessagesPerSec = n;
    }

    protected void blockNegative(int n, String name) {
        if (n < 0) {
            ConfigurationException ex = new ConfigurationException();
            // Invalid {0} for destination ''{1}''. {0} cannot be negative.
            ex.setMessage(ERR_MSG_INVALID_NEGATIVE_VALUE, new Object[]{name, destinationName});
            throw ex;
        }
    }
}
