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

import java.util.Hashtable;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import flex.messaging.config.ConfigurationException;
import flex.messaging.log.Log;

/**
 * The JMSProxy is the superclass for all producers and consumers
 * on both topics and queues. This class contains shared behavior
 * between producers and consumers.
 */
public abstract class JMSProxy
{
    /* JMS related variables */
    protected Connection connection;
    protected ConnectionCredentials connectionCredentials;
    protected ConnectionFactory connectionFactory;
    protected Session session;
    protected Destination destination;
    protected Context jndiContext;

    protected int acknowledgeMode;
    protected String connectionFactoryName;
    protected String destinationJndiName;
    protected Hashtable initialContextEnvironment;

    //--------------------------------------------------------------------------
    //
    // Constructor
    //
    //--------------------------------------------------------------------------

    /**
     * Creates a new <code>JMSProxy</code> default default acknowledge mode of
     * <code>javax.jms.Session.AUTO_ACKNOWLEDGE</code>.
     */
    public JMSProxy()
    {
        acknowledgeMode = javax.jms.Session.AUTO_ACKNOWLEDGE;
    }

    //--------------------------------------------------------------------------
    //
    // Initialize, validate, start, and stop methods.
    //
    //--------------------------------------------------------------------------

    /**
     * Initialize with settings from the JMS adapter.
     *
     * @param settings JMS settings to use for initialization.
     */
    public void initialize(JMSSettings settings)
    {
        String ackString = settings.getAcknowledgeMode();
        if (ackString.equals(JMSConfigConstants.AUTO_ACKNOWLEDGE))
            acknowledgeMode = Session.AUTO_ACKNOWLEDGE;
        else if (ackString.equals(JMSConfigConstants.CLIENT_ACKNOWLEDGE))
            acknowledgeMode = Session.CLIENT_ACKNOWLEDGE;
        else if (ackString.equals(JMSConfigConstants.DUPS_OK_ACKNOWLEDGE))
            acknowledgeMode = Session.DUPS_OK_ACKNOWLEDGE;

        connectionFactoryName = settings.getConnectionFactory();
        String username = settings.getConnectionUsername();
        String password = settings.getConnectionPassword();
        if (username != null || password != null)
        {
            connectionCredentials = new ConnectionCredentials(username, password);
        }
        destinationJndiName = settings.getDestinationJNDIName();
        initialContextEnvironment = settings.getInitialContextEnvironment();
    }

    /**
     * Verifies that the <code>JMSProxy</code> is in valid state before
     * it is started. For <code>JMSProxy</code> to be in valid state, it needs
     * to have a connection factory name and destination jndi name assigned.
     */
    protected void validate()
    {
        if (connectionFactoryName == null)
        {
            // JMS connection factory of message destinations with JMS Adapters must be specified.
            ConfigurationException ce = new ConfigurationException();
            ce.setMessage(JMSConfigConstants.MISSING_CONNECTION_FACTORY);
            throw ce;
        }

        if (destinationJndiName == null)
        {
            // JNDI names for message destinations with JMS Adapters must be specified.
            ConfigurationException ce = new ConfigurationException();
            ce.setMessage(JMSConfigConstants.MISSING_DESTINATION_JNDI_NAME);
            throw ce;
        }
    }

    /**
     * Starts the <code>JMSProxy</code>. The default implementation verifies
     * that <code>JMSProxy</code> is in a valid state to be started and then
     * initializes JNDI context and connection factory for JMS. Subclasses
     * should call <code>super.start</code>.
     *
     * @throws NamingException The thrown naming exception.
     * @throws JMSException The thrown JMS exception.
     */
    public void start() throws NamingException, JMSException
    {
        validate();
        initializeJndiContext();
        initializeConnectionFactory();
        initializeDestination();
    }

    /**
     * Stops the <code>JMSProxy</code> by stopping its associated session
     * and connection.
     */
    public void stop()
    {
        try
        {
            if (session != null)
                session.close();
        }
        catch (JMSException e)
        {
            if (Log.isWarn())
                Log.getLogger(JMSAdapter.LOG_CATEGORY).warn("JMS proxy for JMS destination '"
                        + destinationJndiName + "' received an error while closing"
                        + " its underlying Session: " + e.getMessage());
        }

        try
        {
            if (connection != null)
                connection.close();
        }
        catch (JMSException e)
        {
            if (Log.isWarn())
                Log.getLogger(JMSAdapter.LOG_CATEGORY).warn("JMS proxy for JMS destination '"
                        + destinationJndiName + "' received an error while closing"
                        + " its underlying Connection: " + e.getMessage());
        }

        try
        {
            if (jndiContext != null)
                jndiContext.close();
        }
        catch (NamingException e)
        {
            if (Log.isWarn())
                Log.getLogger(JMSAdapter.LOG_CATEGORY).warn("JMS proxy for JMS destination '"
                        + destinationJndiName + "' received an error while closing"
                        + " its underlying JNDI context: " + e.getMessage());
        }
    }

    //--------------------------------------------------------------------------
    //
    // Public Methods
    //
    //--------------------------------------------------------------------------

    /**
     * Returns the acknowledge mode used by the <code>JMSProxy</code>.
     *
     * @return The acknowledge mode used by the <code>JMSProxy</code>.
     */
    public int getAcknowledgeMode()
    {
        return acknowledgeMode;
    }

    /**
     * Sets the acknowledge mode used by the <code>JMSProxy</code>. Valid values
     * are javax.jms.Session.AUTO_ACKNOWLEDGE, javax.jms.Session.CLIENT_ACKNOWLEDGE,
     * javax.jms.Session.DUPS_OK_ACNOWLEDGE. This property is optional and
     * defaults to javax.jms.Session.AUTO_ACKNOWLEDGE.
     *
     * @param acknowledgeMode An int representing the acknowledge mode used.
     */
    public void setAcknowledgeMode(int acknowledgeMode)
    {
        if (acknowledgeMode == Session.AUTO_ACKNOWLEDGE
                || acknowledgeMode == Session.CLIENT_ACKNOWLEDGE
                || acknowledgeMode == Session.DUPS_OK_ACKNOWLEDGE)
            this.acknowledgeMode = acknowledgeMode;
    }

    /**
     * Returns the connection factory name.
     *
     * @return The connection factory name.
     */
    public String getConnectionFactoryName()
    {
        return connectionFactoryName;
    }

    /**
     * Sets the connection factory name. This property should not changed
     * after startup.
     *
     * @param connectionFactoryName The connection factory name.
     */
    public void setConnectionFactoryName(String connectionFactoryName)
    {
        this.connectionFactoryName = connectionFactoryName;
    }

    /**
     * Returns the connection credentials used while creating JMS connections.
     *
     * @return The connection credentials used while creating JMS connections.
     */
    public ConnectionCredentials getConnectionCredentials()
    {
        return connectionCredentials;
    }

    /**
     * Sets the connection credentials. Connections credentials are passed to JMS
     * connection factory when a JMS connection is created.
     *
     * @param connectionCredentials The connection credentials.
     */
    public void setConnectionCredentials(ConnectionCredentials connectionCredentials)
    {
        this.connectionCredentials = connectionCredentials;
    }

    /**
     * Returns the JNDI name of the JMS destination that <code>JMSProxy</code> talks to.
     *
     * @return The JNDI name of the JMS destination.
     */
    public String getDestinationJndiName()
    {
        return destinationJndiName;
    }

    /**
     * Sets the JNDI name of the JMS destination that <code>JMSProxy</code> talks to.
     *
     * @param destinationJndiName The JNDI name of the JMS destination.
     */
    public void setDestinationJndiName(String destinationJndiName)
    {
        this.destinationJndiName = destinationJndiName;
    }

    /**
     * Returns the <code>initial-context-environment</code> property.
     *
     * @return a Hashtable of the <code>initial-context-environment</code>.
     */
    public Hashtable getInitialContextEnvironment()
    {
        return initialContextEnvironment;
    }

    /**
     * Sets the <code>initial-context-environment</code> property. This property
     * is optional. This property should be change after startup.
     *
     * @param env A Hashtable of the <code>initial-context-environment</code>.
     */
    public void setInitialContextEnvironment(Hashtable env)
    {
        initialContextEnvironment = env;
    }

    //--------------------------------------------------------------------------
    //
    // Protected and Private Methods
    //
    //--------------------------------------------------------------------------

    /**
     * Initializes the connection factory needed for JMS.
     */
    protected ConnectionFactory initializeConnectionFactory() throws NamingException
    {
        if (connectionFactory == null)
            connectionFactory = (ConnectionFactory)jndiContext.lookup(connectionFactoryName);

        return connectionFactory;
    }

    /**
     * Initializes the destination (topic and queue) used by JMS.
     */
    protected Destination initializeDestination() throws NamingException
    {
        if (destination == null)
            destination = (Destination)jndiContext.lookup(destinationJndiName);

        return destination;
    }

    /**
     * Initializes the JNDI context needed by JMS. This should be called before
     * any other initialize methods.
     */
    protected Context initializeJndiContext() throws NamingException
    {
        if (jndiContext != null)
            stop();

        if (initialContextEnvironment != null)
            jndiContext = new InitialContext(initialContextEnvironment);
        else
            jndiContext = new InitialContext();

        return jndiContext;
    }

    //--------------------------------------------------------------------------
    //
    // Nested Classes
    //
    //--------------------------------------------------------------------------

    /**
     * A static inner class for connection credentials that is passed to JMS
     * connection factory when a JMS connection is created.
     */
    public static class ConnectionCredentials
    {
        private String username;
        private String password;

        /**
         * Creates a <code>ConnectionCredentials</code> instance with the supplied
         * username and password.
         *
         * @param username Username of the credential.
         * @param password Password of the credential.
         */
        public ConnectionCredentials(String username, String password)
        {
            this.username = username;
            this.password = password;
        }

        /**
         * Returns the username being used.
         *
         * @return The username being used.
         */
        public String getUsername()
        {
            return username;
        }

        /**
         * Returns the password being used.
         *
         * @return The password being used.
         */
        public String getPassword()
        {
            return password;
        }
    }
}
