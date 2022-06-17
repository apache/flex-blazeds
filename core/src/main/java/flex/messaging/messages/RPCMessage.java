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
package flex.messaging.messages;

import flex.messaging.log.LogCategories;
import flex.messaging.log.Log;

/**
 * Base class for RPC request-styled messages, such as RemotingMessage,
 * HTTPMessage and SOAPMessage.
 */
public abstract class RPCMessage extends AbstractMessage {
    /**
     * This number was generated using the 'serialver' command line tool.
     * This number should remain consistent with the version used by
     * ColdFusion to communicate with the message broker over RMI.
     */
    private static final long serialVersionUID = -1203255926746881424L;

    private String remoteUsername;
    private String remotePassword;

    public RPCMessage() {
    }

    public String getRemoteUsername() {
        return remoteUsername;
    }

    public void setRemoteUsername(String s) {
        remoteUsername = s;
    }

    public String getRemotePassword() {
        return remotePassword;
    }

    public void setRemotePassword(String s) {
        remotePassword = s;
    }

    protected String toStringFields(int indentLevel) {
        String sp = super.toStringFields(indentLevel);
        String sep = getFieldSeparator(indentLevel);
        StringBuilder sb = new StringBuilder();
        sb.append(sep).append("clientId = ").append(Log.isExcludedProperty("clientId") ? Log.VALUE_SUPRESSED : clientId);
        sb.append(sep).append("destination = ").append(Log.isExcludedProperty("destination") ? Log.VALUE_SUPRESSED : destination);
        sb.append(sep).append("messageId = ").append(Log.isExcludedProperty("messageId") ? Log.VALUE_SUPRESSED : messageId);
        sb.append(sep).append("timestamp = ").append(Log.isExcludedProperty("timestamp") ? Log.VALUE_SUPRESSED : String.valueOf(timestamp));
        sb.append(sep).append("timeToLive = ").append(Log.isExcludedProperty("timeToLive") ? Log.VALUE_SUPRESSED : String.valueOf(timeToLive));
        sb.append(sep).append("body = ").append(Log.isExcludedProperty("body") ? Log.VALUE_SUPRESSED : bodyToString(getBody(), indentLevel) + sp);
        return sb.toString();
    }

    public String logCategory() {
        return LogCategories.MESSAGE_RPC;
    }
}
