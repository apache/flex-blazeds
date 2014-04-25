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
package flex.messaging.log;

/**
 * @exclude
 *
 * This class contains all the log categories used in our classes. When adding
 * a new log category, make sure the sample configuration file is updated
 * as well.
 *
 */
public interface LogCategories
{
    String CLIENT_FLEXCLIENT = "Client.FlexClient";
    String CLIENT_FLEXCLIENT_ADAPTIVE = "Client.FlexClient.Adaptive";
    String CLIENT_MESSAGECLIENT = "Client.MessageClient";

    String CONFIGURATION = "Configuration";
    String CONFIGURATION_SPRING = "Configuration.Spring";

    String ENDPOINT_GENERAL = "Endpoint.General";
    String ENDPOINT_AMF = "Endpoint.AMF";
    String ENDPOINT_NIO_AMF = "Endpoint.NIOAMF";
    String ENDPOINT_FLEXSESSION = "Endpoint.FlexSession";
    String ENDPOINT_GATEWAY = "Endpoint.Gateway";
    String ENDPOINT_HTTP = "Endpoint.HTTP";
    String ENDPOINT_NIO_HTTP = "Endpoint.NIOHTTP";
    String ENDPOINT_RTMP = "Endpoint.RTMP";
    String ENDPOINT_STREAMING_AMF = "Endpoint.StreamingAMF";
    String ENDPOINT_STREAMING_NIO_AMF = "Endpoint.StreamingNIOAMF";
    String ENDPOINT_STREAMING_HTTP = "Endpoint.StreamingHTTP";
    String ENDPOINT_STREAMING_NIO_HTTP = "Endpoint.StreamingNIOHTTP";
    String ENDPOINT_WEBSOCKET_NIO_AMF = "Endpoint.WebSocketNIOAMF";
    String ENDPOINT_TYPE = "Endpoint.Type";

    String EXECUTOR = "Executor";

    String MANAGEMENT_GENERAL = "Management.General";
    String MANAGEMENT_MBEANSERVER = "Management.MBeanServer";

    String MESSAGE_GENERAL = "Message.General";
    String MESSAGE_COMMAND = "Message.Command";
    String MESSAGE_DATA = "Message.Data";
    String MESSAGE_FILTER = "Message.Filter";
    String MESSAGE_REMOTING = "Message.Remoting";
    String MESSAGE_RPC = "Message.RPC";
    String MESSAGE_SELECTOR = "Message.Selector";
    String MESSAGE_TIMING = "Message.Timing";

    String PROTOCOL_AMFSOCKET = "Protocol.AMFSocket";
    String PROTOCOL_HTTP = "Protocol.HTTP";
    String PROTOCOL_RTMP = "Protocol.RTMP";
    String PROTOCOL_RTMPT = "Protocol.RTMPT";

    String RESOURCE = "Resource";

    String SERVICE_GENERAL = "Service.General";
    String SERVICE_CLUSTER = "Service.Cluster";
    String SERVICE_COLLABORATION = "Service.Collaboration";
    String SERVICE_DATA = "Service.Data"; // Not a category but used by TargetSettings to replace DataService
    String SERVICE_DATA_GENERAL = "Service.Data.General";
    String SERVICE_DATA_HIBERNATE = "Service.Data.Hibernate";
    String SERVICE_DATA_SQL = "Service.Data.SQL";
    String SERVICE_DATA_TRANSACTION = "Service.Data.Transaction";
    String SERVICE_ADVANCED_MESSAGING = "Service.AdvancedMessaging";
    String SERVICE_NOTIFICATION = "Service.Notification";
    String SERVICE_GATEWAY = "Service.Gateway";
    String SERVICE_GATEWAY_CONNECTOR = "Service.Gateway.Connector";
    String SERVICE_HTTP = "Service.HTTP";
    String SERVICE_MESSAGE = "Service.Message";
    String SERVICE_MESSAGE_JMS = "Service.Message.JMS";
    String SERVICE_REMOTING = "Service.Remoting";

    String SECURITY = "Security";

    String SOCKET_SERVER_GENERAL = "SocketServer.General";
    String SOCKET_SERVER_BYTE_BUFFER_MANAGEMENT = "SocketServer.ByteBufferManagement";

    String SSL = "SSL";

    String STARTUP_MESSAGEBROKER = "Startup.MessageBroker";
    String STARTUP_SERVICE = "Startup.Service";
    String STARTUP_DESTINATION = "Startup.Destination";

    String TIMEOUT = "Timeout";

    String TRANSPORT_RELIABLE = "Transport.Reliable";
    String TRANSPORT_THROTTLE = "Transport.Throttle";
    String TRANSPORT_THROTTLE_BUFFER = "Transport.Throttle.Buffer";
    String TRANSPORT_THROTTLE_CONFLATE = "Transport.Throttle.Conflate";

    String WSRP_GENERAL = "WSRP.General";

    String RDS = "RDS";

    String FBSERVICES_INTROSPECTION = "FBServices.Introspection";
}
