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
package flex.messaging.util;

import flex.messaging.MessageBrokerServlet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Simple little wrapper starting up a BlazeDS server in a separate VM useful for unit testing the
 * features that need different singletons in client and server.
 */
public class TestServer {

    public static void main(String args[]) throws Exception {
        if(args.length != 1) {
            throw new Exception("Need exactly two argument containing th path to the configuration " +
                    "followed by the port number the server should use");
        }
        final String configPath = args[0];

        // Setup a minimal servlet context for hosting our message broker servlet.
        final Server server = new Server(0);
        final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/qa-regress");
        final MessageBrokerServlet messageBrokerServlet = new MessageBrokerServlet();
        final ServletHolder servlet = new ServletHolder(messageBrokerServlet);
        servlet.setInitParameter("services.configuration.file", configPath);
        context.addServlet(servlet, "/messagebroker/amf/*");
        server.setHandler(context);
        server.setDumpAfterStart(true);
        try {
            server.start();
        } catch(Exception e) {
            e.printStackTrace();
        }

        int port = ((ServerConnector) server.getConnectors()[0]).getLocalPort();
        System.out.println("Port:" + port);
    }
}
