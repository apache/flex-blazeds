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

import flex.messaging.util.StringUtils;
import flex.messaging.util.URLDecoder;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * An HTTPMessage specifies a destination that
 * needs to be resolved into a String
 * representation of an HTTP or HTTPS URI
 * endpoint.
 * <p>
 * The method takes values such as GET, POST,
 * HEAD etc.
 * </p>
 *
 *
 */
public class HTTPMessage extends RPCMessage
{
    public HTTPMessage()
    {
    }

    /**
     * This number was generated using the 'serialver' command line tool.
     * This number should remain consistent with the version used by
     * ColdFusion to communicate with the message broker over RMI.
     */
    private static final long serialVersionUID = 5954910346466323369L;

    protected String contentType;
    protected String method;
    protected String url;
    protected Map httpHeaders;
    protected boolean recordHeaders;

    public String getContentType()
    {
        return contentType;
    }

    public void setContentType(String type)
    {
        contentType = type;
    }

    public String getMethod()
    {
        return method;
    }

    public void setMethod(String m)
    {
        if (m != null)
        {
            method = m.trim().toUpperCase();
        }
        else
        {
            method = m;
        }
    }

    public Map getHttpHeaders()
    {
        return httpHeaders;
    }

    public void setHttpHeaders(Map h)
    {
        httpHeaders = h;
    }

    public void setUrl(String s)
    {
        try
        {
            url = URLDecoder.decode(s, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            url = s;
        }
    }

    public String getUrl()
    {
        return url;
    }

    public boolean getRecordHeaders()
    {
        return recordHeaders;
    }

    public void setRecordHeaders(boolean recordHeaders)
    {
        this.recordHeaders = recordHeaders;
    }

    protected String toStringFields(int indentLevel)
    {
        String sep = getFieldSeparator(indentLevel);
        StringBuilder sb = new StringBuilder();
        sb.append(sep).append("method = ").append(getMethod()).
           append(sep).append("url = ").append(getUrl()).
           append(sep).append("headers = ").append(getHeaders());
        sb.append(super.toStringFields(indentLevel));
        return sb.toString();
    }

    protected String internalBodyToString(Object body, int indentLevel) 
    {
        return body instanceof String ?
            StringUtils.prettifyString((String) body) :
            super.internalBodyToString(body, indentLevel);
    }
}
