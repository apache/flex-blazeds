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
package flex.messaging.services.http.proxy;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.StatusLine;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.OptionsMethod;
import flex.messaging.io.MessageIOConstants;

import javax.servlet.http.HttpServletResponse;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import flex.messaging.FlexContext;
import flex.messaging.services.HTTPProxyService;
import flex.messaging.log.Log;

/**
 *
 * Send the response to the client, including custom copying of headers and cookies.
 */
public class ResponseFilter extends ProxyFilter
{
    // NOTE: any changes to this class should also be made to the corresponding version in the .NET.
    // The corresponding class is in src/dotNet/libs/FlexASPlib/Aspx/Proxy

    public static int RESPONSE_CHUNK = 4096;

    private static final int STATUS_ERROR = 10708;
    private static final int NULL_RESPONSE_STREAM = 10709;
    private static final int CANNOT_STREAM_NOT_HTTP = 10710;
    private static final int ERROR_WRITING_RESPONSE = 10711;

    public void invoke(ProxyContext context)
    {
        if (next != null)
        {
            next.invoke(context);
        }

        checkStatusCode(context);
        copyCookiesFromEndpoint(context);
        copyHeadersFromEndpoint(context);
        recordResponseHeaders(context);
        setupResponse(context);
    }

    protected void checkStatusCode(ProxyContext context)
    {
        int statusCode = context.getStatusCode();
        // FIXME: Why do this only for HTTP Proxy? Why not WebServices?
        if (statusCode >= 400 && statusCode != 401 & statusCode != 403 && !context.isSoapRequest())
        {
            StatusLine statusLine = context.getHttpMethod().getStatusLine();
            String reason = null;

            if (statusLine != null)
                reason = statusLine.toString();

            if (reason == null || "".equals(reason))
                reason = String.valueOf(statusCode);

            ProxyException pe = new ProxyException();
            pe.setMessage(STATUS_ERROR, new Object[] { reason });
            pe.setCode(ProxyException.CODE_SERVER_PROXY_REQUEST_FAILED);
            pe.setDetails(STATUS_ERROR, "1", new Object[] { reason });
            pe.setStatusCode(statusCode);
            throw pe;
        }
    }

    protected void copyCookiesFromEndpoint(ProxyContext context)
    {
        HttpServletResponse clientResponse = FlexContext.getHttpResponse();

        if (clientResponse != null)
        {
            Cookie[] cookies = context.getHttpClient().getState().getCookies();
            // We need to filter out the request cookies, we don't need to send back to the client
            Set requestCookies = context.getRequestCookies();
            for (int i = 0; i < cookies.length; i++)
            {
                if (requestCookies != null && requestCookies.contains(cookies[i]) && cookies[i].getExpiryDate() == null)
                {
                    // It means it is a request cookie and nothing changed, we need to skip it 
                    continue;
                }
                // Process the cookie;
                String domain = cookies[i].getDomain();
                String path = cookies[i].getPath();
                String name = cookies[i].getName();
                String value = cookies[i].getValue();

                String clientName = ResponseUtil.getCookieName(context, path, name, domain);

                if (Log.isInfo())
                {
                    String str = "-- Cookie in response: domain = '" + domain + "', path = '" + path +
                            "', client name = '" + clientName + "', endpoint name = '" + name + "', value = '" + value;
                    Log.getLogger(HTTPProxyService.LOG_CATEGORY).debug(str);
                }

                javax.servlet.http.Cookie cookie = new javax.servlet.http.Cookie(clientName, value);

                Date expiry = cookies[i].getExpiryDate();
                if (expiry != null)
                {
                    int maxAge = (int)((expiry.getTime() - System.currentTimeMillis()) / 1000);
                    cookie.setMaxAge(maxAge);
                }
                cookie.setSecure(cookies[i].getSecure());
                cookie.setPath("/");

                clientResponse.addCookie(cookie);
            }
        }
    }

    protected void copyHeadersFromEndpoint(ProxyContext context)
    {
        HttpServletResponse clientResponse = FlexContext.getHttpResponse();

        if (clientResponse != null)
        {
            Header[] headers = context.getHttpMethod().getResponseHeaders();
            for (int i = 0; i < headers.length; i++)
            {
                Header header = headers[i];
                String name = header.getName();
                String value = header.getValue();
                if (ResponseUtil.ignoreHeader(name, context))
                {
                    continue;
                }

                if ((name != null) && (value != null))
                {
                    clientResponse.addHeader(name, value);
                    if (Log.isInfo())
                    {
                        Log.getLogger(HTTPProxyService.LOG_CATEGORY).debug("-- Header in response: " + name + " : " + value);
                    }
                }
            }
            // set Pragma needed for ATG on HTTPS
            clientResponse.setHeader("Pragma", "public");
        }
    }

    protected void recordResponseHeaders(ProxyContext context)
    {
        String method = context.getMethod();
        if (context.getRecordHeaders() || ProxyConstants.METHOD_HEAD.equals(method))
        {
            Header[] headers = context.getHttpMethod().getResponseHeaders();
            HashMap responseHeaders = new HashMap();
            for (int i = 0; i < headers.length; i++)
            {
                Header header = headers[i];
                String headerName = header.getName();
                String headerValue = header.getValue();
                Object existingHeaderValue = responseHeaders.get(headerName);
                // Value(s) already exist for the header.
                if (existingHeaderValue != null)
                {
                    ArrayList headerValues;
                    // Only a single value exists.
                    if (existingHeaderValue instanceof String)
                    {
                        headerValues = new ArrayList();
                        headerValues.add(existingHeaderValue);
                        headerValues.add(headerValue);
                        responseHeaders.put(headerName, headerValues);
                    }
                    // Multiple values exist.
                    else if (existingHeaderValue instanceof ArrayList)
                    {
                        headerValues = (ArrayList)existingHeaderValue;
                        headerValues.add(headerValue);
                    }
                }
                else
                {
                    responseHeaders.put(headerName, headerValue);
                }
            }
            context.setResponseHeaders(responseHeaders);
        }
    }


    protected void setupResponse(ProxyContext context)
    {
        String method = context.getMethod();
        HttpMethodBase httpMethod = context.getHttpMethod();
        if (MessageIOConstants.METHOD_POST.equals(method))
        {
            writeResponse(context);
        }
        else if (ProxyConstants.METHOD_GET.equals(method))
        {
            writeResponse(context);
        }
        else if (ProxyConstants.METHOD_OPTIONS.equals(method))
        {
            OptionsMethod optionsMethod = (OptionsMethod)httpMethod;
            Enumeration options = optionsMethod.getAllowedMethods();
            if (options != null)
            {
                List ops = new ArrayList();
                while (options.hasMoreElements())
                {
                    Object option = options.nextElement();
                    ops.add(option);
                }
                Object[] o = ops.toArray();
                context.setResponse(o);
            }
        }
        else if (ProxyConstants.METHOD_TRACE.equals(method))
        {
            writeResponse(context);
        }
        else if (ProxyConstants.METHOD_DELETE.equals(method))
        {
            writeResponse(context);
        }
        else if (ProxyConstants.METHOD_HEAD.equals(method))
        {
            context.setResponse(context.getResponseHeaders());
        }
        else if (ProxyConstants.METHOD_PUT.equals(method))
        {
            writeResponse(context);
        }
    }

    protected void writeResponse(ProxyContext context)
    {
        try
        {
            InputStream in = context.getHttpMethod().getResponseBodyAsStream();

            if (in == null)
            {
                throw new ProxyException(NULL_RESPONSE_STREAM);
            }

            int length = (int)context.getHttpMethod().getResponseContentLength();

            // Stream response directly to client
            if (context.streamResponseToClient())
            {
                HttpServletResponse clientResponse = FlexContext.getHttpResponse();

                if (clientResponse != null)
                {
                    OutputStream out = clientResponse.getOutputStream();
                    if (length != -1)
                    {
                        clientResponse.setContentLength(length);
                    }

                    writeStreamedResponse(in, out, context);
                }
                else
                {
                    throw new ProxyException(CANNOT_STREAM_NOT_HTTP);
                }
            }
            else
            {
                writeResponseAsString(in, length, context);
            }
        }
        catch (IOException ioe)
        {
            ProxyException pe = new ProxyException();
            pe.setMessage(ERROR_WRITING_RESPONSE, new Object[] { ioe.getMessage() });
            throw pe;
        }
    }

    protected void writeStreamedResponse(InputStream inStream, OutputStream out, ProxyContext context) throws IOException
    {
        byte[] tmp = new byte[RESPONSE_CHUNK];
        int i = 0;

        while ((i = inStream.read(tmp)) >= 0)
        {
            out.write(tmp, 0, i);
        }
    }

    protected void writeResponseAsString(InputStream inStream, int length, ProxyContext context)
            throws IOException
    {
        char[] tmp = new char[RESPONSE_CHUNK];
        //int i = 0;
        StringBuffer sb = new StringBuffer( length < 0 ? 16 : length);
        BufferedInputStream bufferedIn = new BufferedInputStream(inStream);
        String charset = context.getHttpMethod().getResponseCharSet();

        bufferedIn.mark(4);

        // Check for BOM as InputStreamReader does not strip BOM in all cases.
        boolean hasBOM = false;
        int read = bufferedIn.read();
        if (read > 0)
        {
            // UTF-8 BOM is EF BB BF
            if (0xEF == (read & 0xFF))
            {
                read = bufferedIn.read();
                if (0xBB == (read & 0xFF))
                {
                    read = bufferedIn.read();
                    if (0xBF == (read & 0xFF))
                    {
                        hasBOM = true;
                        charset = "UTF-8";
                    }
                }
            }
            // UTF-16 Little Endian BOM is FF FE
            // UTF-32 Little Endian BOM is FF FE 00 00
            else if (0xFF == (read & 0xFF))
            {
                read = bufferedIn.read();
                if (0xFE == (read & 0xFF))
                {
                    hasBOM = true;
                    charset = "UTF16-LE";

                    // Check two more bytes incase we have UTF-32
                    bufferedIn.mark(2);
                    read = bufferedIn.read();
                    if (0x00 == (read & 0xFF))
                    {
                        read = bufferedIn.read();
                        if (0x00 == (read & 0xFF))
                        {
                            charset = "UTF32-LE";
                        }
                        else
                        {
                            bufferedIn.reset();
                        }
                    }
                    else
                    {
                        bufferedIn.reset();
                    }
                }
            }
            // UTF-16 Big Endian BOM is FE FF
            else if (0xFE == (read & 0xFF))
            {
                read = bufferedIn.read();
                if (0xFF == (read & 0xFF))
                {
                    hasBOM = true;
                    charset = "UTF16-BE";
                }
            }
            // UTF-32 Big Endian BOM is 00 00 FE FF
            else if (0x00 == (read & 0xFF))
            {
                read = bufferedIn.read();
                if (0x00 == (read & 0xFF))
                {
                    read = bufferedIn.read();
                    if (0xFE == (read & 0xFF))
                    {
                        read = bufferedIn.read();
                        if (0xFF == (read & 0xFF))
                        {
                            hasBOM = true;
                            charset = "UTF32-BE";
                        }
                    }
                }
            }

            // If we didn't find a BOM, all bytes should contribute to the content
            if (!hasBOM)
                bufferedIn.reset();
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(bufferedIn, charset));
        int charactersRead = -1;
        while ((charactersRead = reader.read(tmp, 0, tmp.length)) >= 0)
        {
            sb.append(new String(tmp, 0, charactersRead));
        }

        context.setResponse(sb.toString());
    }
}
