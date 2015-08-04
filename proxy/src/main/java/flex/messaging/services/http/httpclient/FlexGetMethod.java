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
package flex.messaging.services.http.httpclient;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HeaderElement;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 *
 * Simple wrapper around PostMethod that exposes one method for ProxyServlet.
 */
public class FlexGetMethod extends GetMethod
{
    public FlexGetMethod(String str)
    {
        super(str);
    }

    public void setConnectionForced(boolean bool)
    {
        setConnectionCloseForced(bool);
    }

    protected String getContentCharSet(Header contentheader)
    {
        String charset = null;
        if (contentheader != null)
        {
            HeaderElement values[] = contentheader.getElements();
            if (values.length == 1)
            {
                NameValuePair param = values[0].getParameterByName("charset");
                if (param != null)
                {
                    charset = param.getValue();
                }
            }
        }
        if (charset == null)
        {
            charset = "UTF-8";
        }
        return charset;
    }
}
