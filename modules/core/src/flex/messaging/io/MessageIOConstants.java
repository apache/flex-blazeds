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
package flex.messaging.io;

/**
 * @exclude
 */
public interface MessageIOConstants
{
    int AMF0 = 0;
    int AMF1 = 1; // There is no AMF1 but FMS uses it for some reason, hence special casing.
    int AMF3 = 3;
    Double AMF3_INFO_PROPERTY = new Double(3);

    String CONTENT_TYPE_XML = "text/xml; charset=utf-8";
    String AMF_CONTENT_TYPE = "application/x-amf";
    String CONTENT_TYPE_PLAIN = "text/plain";
    String XML_CONTENT_TYPE = "application/xml";

    String RESULT_METHOD = "/onResult";
    String STATUS_METHOD = "/onStatus";

    int STATUS_OK = 0;
    int STATUS_ERR = 1;
    int STATUS_NOTAMF = 2;

    String SECURITY_HEADER_NAME = "Credentials";
    String SECURITY_PRINCIPAL = "userid";
    String SECURITY_CREDENTIALS = "password";

    String URL_APPEND_HEADER = "AppendToGatewayUrl";
    String SERVICE_TYPE_HEADER = "ServiceType";

    String REMOTE_CLASS_FIELD = "_remoteClass";
    String SUPPORT_REMOTE_CLASS = "SupportRemoteClass";
    String SUPPORT_DATES_BY_REFERENCE = "SupportDatesByReference";

    String METHOD_POST = "POST";
    String HEADER_SOAP_ACTION = "SOAPAction";
}
