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

/**
 * Constants for JMSAdapter and its related classes.
 */
public interface JMSConfigConstants {
    // Values used in the messaging configuration
    String ASYNC = "async";
    String ACKNOWLEDGE_MODE = "acknowledge-mode";
    String AUTO_ACKNOWLEDGE = "auto_acknowledge";
    String CLIENT_ACKNOWLEDGE = "client_acknowledge";
    String CONNECTION_FACTORY = "connection-factory";
    String CONNECTION_CREDENTIALS = "connection-credentials";
    String DELIVERY_SETTINGS = "delivery-settings";
    String DEFAULT_DELIVERY_MODE = "default_delivery_mode";
    String DEFAULT_PRIORITY = "default-priority";
    String DELIVERY_MODE = "delivery-mode";
    String DESTINATION_JNDI_NAME = "destination-jndi-name";
    String DESTINATION_NAME = "destination-name";
    String DESTINATION_TYPE = "destination-type";
    String DUPS_OK_ACKNOWLEDGE = "dups_ok_acknowledge";
    String INITIAL_CONTEXT_ENVIRONMENT = "initial-context-environment";
    String JMS = "jms";
    String MAX_PRODUCERS = "max-producers";
    String MAP_MESSAGE = "javax.jms.MapMessage";
    String MESSAGE_TYPE = "message-type";
    String MESSAGE_PRIORITY = "message-priority";
    String MODE = "mode";
    String NAME = "name";
    String NON_PERSISTENT = "non_persistent";
    String OBJECT_MESSAGE = "javax.jms.ObjectMessage";
    String PASSWORD = "password";
    String PERSISTENT = "persistent";
    String PRESERVE_JMS_HEADERS = "preserve-jms-headers";
    String PROPERTY = "property";
    String SYNC = "sync";
    String SYNC_RECEIVE_INTERVAL_MILLIS = "sync-receive-interval-millis";
    String SYNC_RECEIVE_WAIT_MILLIS = "sync-receive-wait-millis";
    String QUEUE = "queue";
    String TEXT_MESSAGE = "javax.jms.TextMessage";
    String TOPIC = "topic";
    String TRANSACTION_MODE = "transacted-sessions";
    String USERNAME = "username";
    String VALUE = "value";

    // Flex message properties to translate to JMS
    String TIME_TO_LIVE = "timeToLive";

    // Standard JMS headers to translate to Flex
    String JMS_CORRELATION_ID = "JMSCorrelationID";
    String JMS_DELIVERY_MODE = "JMSDeliveryMode";
    String JMS_DESTINATION = "JMSDestination";
    String JMS_EXPIRATION = "JMSExpiration";
    // public static final String JMS_MESSAGE_ID = "JMSMessageID";
    String JMS_PRIORITY = "JMSPriority";
    String JMS_REDELIVERED = "JMSRedelivered";
    String JMS_REPLY_TO = "JMSReplyTo";
    // public static final String JMS_TIMESTAMP = "JMSTimestamp";
    String JMS_TYPE = "JMSType";

    // Defaults
    String defaultAcknowledgeMode = AUTO_ACKNOWLEDGE;
    String defaultDestinationType = TOPIC;
    boolean defaultPreserveJMSHeaders = true;
    long defaultSyncReceiveIntervalMillis = 100;
    long defaultSyncReceiveWaitMillis = 0;
    int defaultMaxProducers = 1;
    String defaultMode = SYNC;

    // Errors
    int MISSING_NAME_OR_VALUE = 10800;
    int INVALID_CONTEXT_NAME = 10801;
    int INACCESIBLE_CONTEXT_NAME = 10802;
    int MISSING_PROPERTY_SUBELEMENT = 10803;
    int MISSING_CONNECTION_FACTORY = 10804;
    int INVALID_DESTINATION_TYPE = 10805;
    // int CLIENT_NOT_SUBSCRIBED = 10806;
    int MISSING_DESTINATION_JNDI_NAME = 10807;
    int INVALID_ACKNOWLEDGE_MODE = 10808;
    int INVALID_DELIVERY_MODE = 10809;
    int NONSERIALIZABLE_MESSAGE_BODY = 10810;
    int INVALID_JMS_MESSAGE_TYPE = 10811;
    int NONMAP_MESSAGE_BODY = 10812;
    int NON_QUEUE_DESTINATION = 10813;
    int NON_QUEUE_FACTORY = 10814;
    int NON_TOPIC_DESTINATION = 10815;
    int NON_TOPIC_FACTORY = 10816;
    int INVALID_DELIVERY_MODE_VALUE = 10817;
    int ASYNC_MESSAGE_DELIVERY_NOT_SUPPORTED = 10818;
    int DURABLE_SUBSCRIBER_NOT_SUPPORTED = 10819;
    int CLIENT_UNSUBSCRIBE_DUE_TO_MESSAGE_DELIVERY_ERROR = 10820;
    int CLIENT_UNSUBSCRIBE_DUE_TO_CONSUMER_REMOVAL = 10821;
    int CLIENT_UNSUBSCRIBE_DUE_TO_CONSUMER_STOP = 10822;
    int MISSING_DURABLE_SUBSCRIPTION_NAME = 10823;
    int JMSINVOCATION_EXCEPTION = 10824;
}

