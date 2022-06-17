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
package flex.messaging.io.amf;

import flex.messaging.MessageException;
import flex.messaging.io.MessageIOConstants;
import flex.messaging.messages.Message;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.List;

public class MessageBody implements Serializable {
    private static final int ERR_MSG_INVALID_REQUEST_TYPE = 10037;

    static final long serialVersionUID = 3874002169129668459L;

    private String targetURI = "";

    private String responseURI = "";

    protected Object data;

    public MessageBody() {
    }

    public MessageBody(String targetURI, String responseURI, Object data) {
        setTargetURI(targetURI);
        setResponseURI(responseURI);
        this.data = data;
    }


    public String getTargetURI() {
        return targetURI;
    }

    public void setTargetURI(String uri) {
        if (uri == null)
            uri = "";

        targetURI = uri;
    }

    public void setReplyMethod(String methodName) {
        if (targetURI.endsWith(MessageIOConstants.STATUS_METHOD) || targetURI.endsWith(MessageIOConstants.RESULT_METHOD))
            targetURI = targetURI.substring(0, targetURI.lastIndexOf('/'));

        targetURI = targetURI + methodName;
    }

    public String getReplyMethod() {
        return targetURI.substring((targetURI.lastIndexOf('/') + 1), targetURI.length());
    }


    public String getResponseURI() {
        return responseURI;
    }

    public void setResponseURI(String uri) {
        if (uri == null)
            uri = "";

        responseURI = uri;
    }

    public Object getData() {
        return data;
    }

    public Message getDataAsMessage() {
        if (data instanceof List)
            data = ((List) data).get(0);
        else if (data.getClass().isArray())
            data = Array.get(data, 0);

        if (data instanceof Message)
            return (Message) data;

        MessageException me = new MessageException();
        me.setMessage(ERR_MSG_INVALID_REQUEST_TYPE, new Object[]{data.getClass().getName()});
        throw me;
    }

    public void setData(Object data) {
        this.data = data;
    }
}

