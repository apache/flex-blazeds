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

import flex.messaging.MessageException;
import flex.messaging.log.Log;

import java.util.Map;

/**
 * A message describing a MessageException.
 *
 * @author neville
 * @exclude
 */
public class ErrorMessage extends AcknowledgeMessage
{
    /**
     * This number was generated using the 'serialver' command line tool.
     * This number should remain consistent with the version used by
     * ColdFusion to communicate with the message broker over RMI.
     */
    private static final long serialVersionUID = -9069412644250075809L;

    public String faultCode;
    public String faultString;
    public String faultDetail;
    public Object rootCause;
    public Map extendedData;

    public ErrorMessage(MessageException mxe)
    {
        faultCode = mxe.getCode();
        faultString = mxe.getMessage();
        faultDetail = mxe.getDetails();
        if (mxe.getRootCause() != null)
        {
            rootCause = mxe.getRootCauseErrorMessage();
        }
        Map extendedData = mxe.getExtendedData();
        if (extendedData != null)
        {
            this.extendedData = extendedData;
        }
    }

    public ErrorMessage()
    {
    }

    /**
     * @exclude
     */
    public Message getSmallMessage()
    {
        return null;
    }

    protected String toStringFields(int indentLevel) 
    {
        String sep = getFieldSeparator(indentLevel);
        String s = super.toStringFields(indentLevel);
        s += sep + "code =  " + faultCode;
        s += sep + "message =  " + faultString;
        s += sep + "details =  " + faultDetail;
        s += sep + "rootCause =  ";
        if (rootCause == null) s += "null";
        else s += rootCause.toString();
        if (Log.isExcludedProperty("body"))
            s += sep + "body = " + Log.VALUE_SUPRESSED;
        else
            s += sep + "body =  " + bodyToString(body, indentLevel);
        s += sep + "extendedData =  " + bodyToString(extendedData, indentLevel);
        return s;
    }
}
