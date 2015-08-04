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

package dev.httpservice;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

//import flex.webtier.util.J2EEUtil;
//import flex.webtier.server.j2ee.wrappers.J2EEWrapper;
import flex.messaging.util.Base64;

public class HttpServiceTest extends HttpServlet
{
   
    private static final long serialVersionUID = 1L;
    public static final String FORMAT_XML = "xml";
    public static final String FORMAT_OBJECT = "object";
    public static final String FORMAT_TEXT = "text";
    public static final String FORMAT_FLASHVARS = "flashvars";

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        writeResponse(request, response, "GET");
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        writeResponse(request, response, "POST");
    }

    private void writeResponse(HttpServletRequest request, HttpServletResponse response, String method) throws IOException
    {
        String desiredStatusStr = request.getParameter("desiredStatus");
        if (desiredStatusStr != null && !"".equals(desiredStatusStr))
        {
            int desiredStatus = Integer.parseInt(desiredStatusStr);
            if (desiredStatus != 200)
            {
                if (desiredStatus != HttpServletResponse.SC_UNAUTHORIZED)
                {
                    response.sendError(desiredStatus, "I am a non-200 message!");
                    return;
                }
                String authorization = request.getHeader("Authorization");
                boolean authorized = false;
                if (authorization != null)
                {
                    String encoded = authorization.substring(HttpServletRequest.BASIC_AUTH.length());
                    Base64.Decoder base64Decoder = new Base64.Decoder();
                    base64Decoder.decode(encoded);
                    String decoded = new String(base64Decoder.drain());
                    String username = decoded.substring(0, decoded.indexOf(":"));
                    String password = decoded.substring(decoded.indexOf(":") + 1);
                    authorized = password.equals(username);
                }
                if (!authorized)
                {
                    response.setHeader("WWW-Authenticate", "BASIC realm=\"\"");
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
            }
        }

        String resultFormat = request.getParameter("resultFormat");
        PrintWriter pw = response.getWriter();
        //J2EEUtil.setServerInfo(getServletContext().getServerInfo());
        //request = (HttpServletRequest)J2EEWrapper.getRequest(request);
        if (FORMAT_XML.equals(resultFormat) || FORMAT_OBJECT.equals(resultFormat))
        {
            response.setContentType("text/xml");
            if (FORMAT_XML.equals(resultFormat))
            {
                pw.println("<response>");
            }
            for (Iterator iterator = request.getParameterMap().keySet().iterator(); iterator.hasNext();)
            {
                String name = (String)iterator.next();
                String value = request.getParameter(name);
                pw.print("<");
                pw.print(name);
                pw.print(">");
                pw.print(value);
                pw.print("</");
                pw.print(name);
                pw.println(">");
            }
            pw.print("<method>");
            pw.print(method);
            pw.println("</method>");
            if (FORMAT_XML.equals(resultFormat))
            {
                pw.println("</response>");
            }
        }
        else
        {
            response.setContentType("text/plain");
            String separator = (FORMAT_FLASHVARS.equals(resultFormat)) ? "&" : "\n";
            pw.print("method=");
            pw.print(method);
            pw.print(separator);
            for (Iterator iterator =  request.getParameterMap().keySet().iterator(); iterator.hasNext();)
            {
                String name = (String)iterator.next();
                String value = request.getParameter(name);
                pw.print(name);
                pw.print("=");
                pw.print(value);
                if (iterator.hasNext())
                {
                    pw.print(separator);
                }
            }
        }

        pw.close();
    }

}
