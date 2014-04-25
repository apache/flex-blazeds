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
 * @exclude
 */
public interface ConfigurationConstants
{
    String CONTEXT_PATH_TOKEN = "{context.root}";
    String CONTEXT_PATH_ALT_TOKEN = "{context-root}";
    String SLASH_CONTEXT_PATH_TOKEN = "/{context.root}";
    String EMPTY_STRING = "";
    String TRUE_STRING = "true";
    String FALSE_STRING = "false";
    String SERVER_NAME_TOKEN = "{server.name}";
    String SERVER_PORT_TOKEN = "{server.port}";

    // ELEMENTS

    // Top Level
    String SERVICES_CONFIG_ELEMENT = "services-config";

    // Services
    String SERVICES_ELEMENT = "services";
    String SERVICE_ELEMENT = "service";
    String SERVICE_INCLUDE_ELEMENT = "service-include";

    String SRC_ATTR = "file-path";
    String DIRECTORY_ATTR = "directory-path";
    String ID_ATTR = "id";
    String CLASS_ATTR = "class";
    String PER_CLIENT_AUTH="per-client-authentication";
    String MESSAGE_TYPES_ATTR = "messageTypes";

    String PROPERTIES_ELEMENT = "properties";

    String METADATA_ELEMENT = "metadata";

    String ADAPTERS_ELEMENT = "adapters";
    String ADAPTER_DEFINITION_ELEMENT = "adapter-definition";
    String ADAPTER_INCLUDE_ELEMENT = "adapter-include";
    String DEFAULT_ATTR = "default";

    String DEFAULT_CHANNELS_ELEMENT = "default-channels";
    String CHANNEL_ELEMENT = "channel";
    String REF_ATTR = "ref";

    String DEFAULT_SECURITY_CONSTRAINT_ELEMENT = "default-security-constraint";

    String DESTINATION_ELEMENT = "destination";
    String DESTINATIONS_ELEMENT = "destinations";
    String DESTINATION_INCLUDE_ELEMENT = "destination-include";
    String ADAPTER_ELEMENT = "adapter";
    String ADAPTER_ATTR = "adapter";
    String CHANNELS_ATTR = "channels";
    String SECURITY_CONSTRAINT_ELEMENT = "security-constraint";
    String SECURITY_CONSTRAINT_ATTR = "security-constraint";
    String SECURITY_CONSTRAINTS_ELEMENT = "security-constraints";  // for includes only

    // Security
    String SECURITY_ELEMENT = "security";
    String SECURITY_CONSTRAINT_DEFINITION_ELEMENT = "security-constraint";
    String CONSTRAINT_INCLUDE_ELEMENT = "constraint-include";
    String AUTH_METHOD_ELEMENT = "auth-method";
    String ROLES_ELEMENT = "roles";
    String ROLE_ELEMENT = "role";
    String LOGIN_COMMAND_ELEMENT = "login-command";
    String SERVER_ATTR = "server";
    String RECREATE_HTTPSESSION_AFTER_LOGIN_ELEMENT = "recreate-httpsession-after-login";

    // SocketServers
    String SERVERS_ELEMENT = "servers";
    String SERVER_ELEMENT = "server";
    String IP_ADDRESS_PATTERN = "ip-address-pattern";

    // Channels
    String CHANNELS_ELEMENT = "channels";
    String CHANNEL_DEFINITION_ELEMENT = "channel-definition";
    String CHANNEL_INCLUDE_ELEMENT = "channel-include";
    String REMOTE_ATTR = "remote";
    String SERVER_ONLY_ATTR = "server-only";
    String ENDPOINT_ELEMENT = "endpoint";
    // Deprecated, use URL_ATTR instead.
    String URI_ATTR = "uri";
    String URL_ATTR = "url";
    String POLLING_ENABLED_ELEMENT = "polling-enabled";
    String POLLING_INTERVAL_MILLIS_ELEMENT = "polling-interval-millis";
    String PIGGYBACKING_ENABLED_ELEMENT = "piggybacking-enabled";
    String LOGIN_AFTER_DISCONNECT_ELEMENT = "login-after-disconnect";
    String RECORD_MESSAGE_SIZES_ELEMENT = "record-message-sizes";
    String RECORD_MESSAGE_TIMES_ELEMENT = "record-message-times";
    String SERIALIZATION_ELEMENT = "serialization";
    String ENABLE_SMALL_MESSAGES_ELEMENT = "enable-small-messages";
    // Deprecated, use POLLING_INTERVAL_MILLIS_ELEMENT instead.
    String POLLING_INTERVAL_SECONDS_ELEMENT = "polling-interval-seconds";
    String CONNECT_TIMEOUT_SECONDS_ELEMENT = "connect-timeout-seconds";
    String CLIENT_LOAD_BALANCING_ELEMENT = "client-load-balancing";
    String REQUEST_TIMEOUT_SECONDS_ELEMENT = "request-timeout-seconds";

    // Clusters
    String CLUSTERS_ELEMENT = "clusters";
    String CLUSTER_DEFINITION_ELEMENT = "cluster";
    String CLUSTER_PROPERTIES_ATTR = "properties";

    // Logging
    String LOGGING_ELEMENT = "logging";
    String TARGET_ELEMENT = "target";
    String FILTERS_ELEMENT = "filters";
    String PATTERN_ELEMENT = "pattern";
    String LEVEL_ATTR = "level";

    // System
    String SYSTEM_ELEMENT = "system";
    String ENFORCE_ENDOINT_VALIDATION ="enforce-endpoint-validation";
    String LOCALE_ELEMENT = "locale";
    String MANAGEABLE_ELEMENT = "manageable";
    String DEFAULT_LOCALE_ELEMENT = "default-locale";
    String REDEPLOY_ELEMENT = "redeploy";
    String ENABLED_ELEMENT = "enabled";
    String WATCH_INTERVAL_ELEMENT = "watch-interval";
    String WATCH_FILE_ELEMENT = "watch-file";
    String TOUCH_FILE_ELEMENT = "touch-file";
    String FACTORIES_ELEMENT = "factories";
    String FACTORY_ELEMENT = "factory";
    String UUID_GENERATOR_ELEMENT = "uuid-generator";
    String DOTNET_FRAMEWORK_VERSION = "dotnet-framework-version";

    // FlexClient
    String FLEX_CLIENT_ELEMENT = "flex-client";
    String FLEX_CLIENT_TIMEOUT_MINUTES_ELEMENT = "timeout-minutes";
    String FLEX_CLIENT_OUTBOUND_QUEUE_PROCESSOR_ELEMENT = "flex-client-outbound-queue-processor";
    String ADAPTIVE_FREQUENCY = "adaptive-frequency";
    String FLEX_CLIENT_RELIABLE_RECONNECT_DURATION_MILLIS = "reliable-reconnect-duration-millis";
    String FLEX_CLIENT_HEARTBEAT_INTERVAL_MILLIS = "heartbeat-interval-millis";

    // Message filters
    String ASYNC_MESSAGE_FILTERS_ELEMENT = "async-message-filters";
    String SYNC_MESSAGE_FILTERS_ELEMENT = "sync-message-filters";
    String FILTER_ELEMENT = "filter";

    // Validators
    String VALIDATORS_ELEMENT = "validators";
    String VALIDATOR_ELEMENT = "validator";
    String TYPE_ATTR = "type";

    // CHILD ELEMENTS

    // Top Level
    String[] SERVICES_CONFIG_CHILDREN = {
        SERVICES_ELEMENT, SECURITY_ELEMENT, SERVERS_ELEMENT, CHANNELS_ELEMENT, LOGGING_ELEMENT,
        SYSTEM_ELEMENT, CLUSTERS_ELEMENT, FACTORIES_ELEMENT, FLEX_CLIENT_ELEMENT,
        ASYNC_MESSAGE_FILTERS_ELEMENT, SYNC_MESSAGE_FILTERS_ELEMENT, VALIDATORS_ELEMENT
    };

    // Services
    String[] SERVICES_CHILDREN = {
        SERVICE_ELEMENT, SERVICE_INCLUDE_ELEMENT, DEFAULT_CHANNELS_ELEMENT
    };

    String[] SERVICE_INCLUDE_CHILDREN = {
        SRC_ATTR, DIRECTORY_ATTR
    };

    String[] SERVICE_CHILDREN = {
        ID_ATTR, CLASS_ATTR, MESSAGE_TYPES_ATTR, PROPERTIES_ELEMENT,
        ADAPTERS_ELEMENT, DEFAULT_CHANNELS_ELEMENT, DEFAULT_SECURITY_CONSTRAINT_ELEMENT,
        DESTINATION_INCLUDE_ELEMENT, DESTINATION_ELEMENT
    };

    String[] SERVICE_REQ_CHILDREN = {
        ID_ATTR, CLASS_ATTR
    };

    String[] ADAPTER_DEFINITION_CHILDREN = {
        ID_ATTR, CLASS_ATTR, DEFAULT_ATTR, PROPERTIES_ELEMENT
    };

    String[] ADAPTER_DEFINITION_REQ_CHILDREN = {
        ID_ATTR, CLASS_ATTR
    };

    String[] DESTINATION_INCLUDE_CHILDREN = {
        SRC_ATTR, DIRECTORY_ATTR
    };

    String[] ADAPTERS_CHILDREN = {
        ADAPTER_DEFINITION_ELEMENT, ADAPTER_INCLUDE_ELEMENT
    };

    String[] ADAPTER_INCLUDE_CHILDREN = {
        SRC_ATTR, DIRECTORY_ATTR
    };

    String[] DEFAULT_CHANNELS_CHILDREN = {
        CHANNEL_ELEMENT
    };

    String[] FLEX_CLIENT_CHILDREN = {
        FLEX_CLIENT_TIMEOUT_MINUTES_ELEMENT, FLEX_CLIENT_OUTBOUND_QUEUE_PROCESSOR_ELEMENT,
        ADAPTIVE_FREQUENCY, FLEX_CLIENT_RELIABLE_RECONNECT_DURATION_MILLIS, FLEX_CLIENT_HEARTBEAT_INTERVAL_MILLIS
    };

    String[] FLEX_CLIENT_OUTBOUND_QUEUE_PROCESSOR_REQ_CHILDREN = {
        CLASS_ATTR
    };

    // Security
    String[] SECURITY_CHILDREN = {
        SECURITY_CONSTRAINT_DEFINITION_ELEMENT, LOGIN_COMMAND_ELEMENT, RECREATE_HTTPSESSION_AFTER_LOGIN_ELEMENT,
        CONSTRAINT_INCLUDE_ELEMENT
    };

    String[] EMBEDDED_SECURITY_CHILDREN = {
        SECURITY_CONSTRAINT_DEFINITION_ELEMENT
    };

    String[] SECURITY_CONSTRAINT_DEFINITION_CHILDREN = {
        REF_ATTR, ID_ATTR, AUTH_METHOD_ELEMENT, ROLES_ELEMENT
    };

    String[] ROLES_CHILDREN = {
        ROLE_ELEMENT
    };

    String[] LOGIN_COMMAND_CHILDREN = {
        SERVER_ATTR, CLASS_ATTR, PER_CLIENT_AUTH
    };

    String[] LOGIN_COMMAND_REQ_CHILDREN = {
        SERVER_ATTR, CLASS_ATTR
    };

    String[] SECURITY_CONSTRAINTS_CHILDREN = {
        SECURITY_CONSTRAINT_ELEMENT
    };

    String[] CONSTRAINT_INCLUDE_CHILDREN = {
        SRC_ATTR, DIRECTORY_ATTR
    };

    // Servers
    String[] SERVERS_CHILDREN = {SERVER_ELEMENT};

    String[] SERVER_REQ_CHILDREN = {
        ID_ATTR, CLASS_ATTR
    };

    String [] SERVER_CHILDREN = {
        ID_ATTR, CLASS_ATTR, PROPERTIES_ELEMENT
    };

    // Channels
    String[] CHANNELS_CHILDREN = {
        CHANNEL_DEFINITION_ELEMENT, CHANNEL_INCLUDE_ELEMENT
    };

    String[] CHANNEL_DEFINITION_REQ_CHILDREN = {
        ENDPOINT_ELEMENT, ID_ATTR
    };

    String[] CHANNEL_DEFINITION_CHILDREN = {
        ENDPOINT_ELEMENT, PROPERTIES_ELEMENT, SECURITY_ELEMENT, SERVER_ELEMENT,
        SECURITY_CONSTRAINT_ATTR, CLASS_ATTR, ID_ATTR, REMOTE_ATTR, SERVER_ONLY_ATTR
    };

    String[] CHANNEL_DEFINITION_SERVER_REQ_CHILDREN = {
        REF_ATTR
    };

    String[] CHANNEL_INCLUDE_CHILDREN = {
        SRC_ATTR, DIRECTORY_ATTR
    };

    String[] ENDPOINT_CHILDREN = {
        URI_ATTR, URL_ATTR, CLASS_ATTR
    };

    String[] DESTINATION_REQ_CHILDREN = {
        ID_ATTR
    };

    String[] DESTINATION_CHILDREN = {
        ID_ATTR, PROPERTIES_ELEMENT, CHANNELS_ELEMENT, SECURITY_ELEMENT, ADAPTER_ELEMENT,
        CHANNELS_ATTR, ADAPTER_ATTR, SECURITY_CONSTRAINT_ATTR
    };

    String[] DESTINATIONS_CHILDREN = {
        DESTINATION_ELEMENT
    };

    String[] DESTINATION_ATTR = {
            ID_ATTR, PROPERTIES_ELEMENT, CHANNELS_ELEMENT, ADAPTER_ELEMENT,
            CHANNELS_ATTR, ADAPTER_ATTR, SECURITY_CONSTRAINT_ATTR
    };

    String[] DESTINATION_CHANNEL_REQ_CHILDREN = {
        REF_ATTR
    };

    String[] DESTINATION_CHANNELS_CHILDREN = {
        CHANNEL_ELEMENT
    };

    String[] DESTINATION_ADAPTER_CHILDREN = {
        REF_ATTR
    };

    // Clustering
    String[] CLUSTERING_CHILDREN = {
        CLUSTER_DEFINITION_ELEMENT
    };

    String[] CLUSTER_DEFINITION_CHILDREN = {
        ID_ATTR, CLUSTER_PROPERTIES_ATTR
    };

    // Logging

    String[] LOGGING_CHILDREN = {
        PROPERTIES_ELEMENT, LEVEL_ATTR, TARGET_ELEMENT,
    };

    String[] TARGET_CHILDREN = {
        CLASS_ATTR, LEVEL_ATTR, PROPERTIES_ELEMENT, FILTERS_ELEMENT
    };

    String[] TARGET_REQ_CHILDREN = {
        CLASS_ATTR
    };

    String[] FILTERS_CHILDREN = {
        PATTERN_ELEMENT
    };

    // System

    String[] SYSTEM_CHILDREN = {
        LOCALE_ELEMENT, ENFORCE_ENDOINT_VALIDATION, REDEPLOY_ELEMENT, MANAGEABLE_ELEMENT, UUID_GENERATOR_ELEMENT, DOTNET_FRAMEWORK_VERSION
    };

    String[] REDEPLOY_CHILDREN = {
        ENABLED_ELEMENT, WATCH_INTERVAL_ELEMENT, WATCH_FILE_ELEMENT, TOUCH_FILE_ELEMENT
    };

    String[] LOCALE_CHILDREN = {
        DEFAULT_LOCALE_ELEMENT
    };

    // Factories
    String[] FACTORIES_CHILDREN = {
        FACTORY_ELEMENT
    };

    String[] FACTORY_REQ_CHILDREN = {
        ID_ATTR, CLASS_ATTR
    };

    // Message filters
    String[] ASYNC_MESSAGE_FILTERS_ELEMENT_CHILDREN = {
        FILTER_ELEMENT
    };

    String[] SYNC_MESSAGE_FILTERS_ELEMENT_CHILDREN = {
        FILTER_ELEMENT
    };

    String[] FILTER_CHILDREN = {
        ID_ATTR, CLASS_ATTR, PROPERTIES_ELEMENT
    };

    String[] FILTER_REQ_CHILDREN = {
        ID_ATTR, CLASS_ATTR
    };

    // UUID Generator
    String[] UUID_GENERATOR_REQ_CHILDREN = {
            CLASS_ATTR
         };

    // Validators
    String[] VALIDATORS_CHILDREN = {
        VALIDATOR_ELEMENT
    };

    String[] VALIDATOR_CHILDREN = {
        CLASS_ATTR, TYPE_ATTR, PROPERTIES_ELEMENT
    };

    String[] VALIDATOR_REQ_CHILDREN = {
        CLASS_ATTR
     };

    // UTILS
    String LIST_DELIMITERS = ",;:";

    // TOKEN REPLACEMENT
    String UNKNOWN_SOURCE_FILE = "uknown file";


    // EXCEPTION MESSAGES

    int PARSER_INIT_ERROR = 10100;
    int PARSER_INTERNAL_ERROR = 10101;
    int XML_PARSER_ERROR = 10102;
    int INVALID_SERVICES_ROOT = 10103;
    int MISSING_ELEMENT = 10104;
    int MISSING_ATTRIBUTE = 10105;
    int UNEXPECTED_ELEMENT = 10106;
    int UNEXPECTED_ATTRIBUTE = 10107;
    int TOO_MANY_OCCURRENCES = 10108;
    int REF_NOT_FOUND = 10109;
    int INVALID_ID = 10110;
    int INVALID_ENDPOINT_PORT = 10111;
    int INVALID_INCLUDE_ROOT = 10112;
    int DUPLICATE_SERVICE_ERROR = 10113;
    int CLASS_NOT_SPECIFIED = 10114;
    int INVALID_DEFAULT_CHANNEL = 10116;
    int DUPLICATE_DEFAULT_ADAPTER = 10117;
    int MISSING_INCLUDE_ATTRIBUTES = 10118;
    int INVALID_ID_IN_SERVICE = 10119;
    int REF_NOT_FOUND_IN_DEST = 10120;
    int INVALID_REF_IN_DEST = 10121;
    int DUPLICATE_DESTINATION_ERROR = 10122;
    int DEST_NEEDS_CHANNEL = 10123;
    int DEST_NEEDS_ADAPTER = 10127;
    int REF_NOT_FOUND_IN_CHANNEL = 10132;
    int UNEXPECTED_TEXT = 11104;

    int NULL_COMPONENT = 11110;
    int NULL_COMPONENT_ID = 11111;
    int DUPLICATE_COMPONENT_ID = 11112;
    int UNREGISTERED_ADAPTER = 11114;
    int DUPLICATE_DEST_ID = 11119;

    int UNDEFINED_CONTEXT_ROOT = 11120;
    int INVALID_FLEX_CLIENT_TIMEOUT = 11123;
    int INVALID_SECURITY_CONSTRAINT_REF = 11124;
    int IRREPLACABLE_TOKEN = 11125;
    int INVALID_VALUE_FOR_PROPERTY_OF_COMPONENT_WITH_ID = 11126;
    int DUPLICATE_CHANNEL_ERROR = 11127;
    int INVALID_FLEX_CLIENT_RELIABLE_RECONNECT_DURATION_MILLIS = 11137;
    int INVALID_FLEX_CLIENT_HEARTBEAT_INTERVAL_MILLIS = 11146;

    int REQUIRE_ADVANCED_MESSAGING_SUPPORT = 11129;
    int CLASS_OR_SERVER_ONLY_ERROR = 11139;
    int CLASS_AND_SERVER_ONLY_ERROR = 11140;
    int ERR_MSG_EMPTY_CLIENT_LOAD_BALANCING_ELEMENT = 11141;
    int ERR_MSG_EMTPY_CLIENT_LOAD_BALACNING_URL = 11142;
    int ERR_MSG_CLIENT_LOAD_BALANCING_URL_WITH_TOKEN = 11147;

    int EXTERNAL_ENTITY_NOT_ALLOW = 11149;
}
