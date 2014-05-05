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
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.io.IOException;

/**
 * Simple little wrapper starting up a BlazeDS server in a separate VM useful for unit testing the
 * features that need different singletons in client and server.
 *
 * @author Christofer Dutz
 */
public class TestServer {

    private Process serverProcess;

    public boolean startServer(String configPath) {
        // We can only start one server per instance of TestServer.
        if(serverProcess != null) {
            return false;
        }

        final String separator = System.getProperty("file.separator");
        final String classpath = System.getProperty("java.class.path");
        final String path = System.getProperty("java.home") + separator + "bin" + separator + "java";
        final ProcessBuilder processBuilder = new ProcessBuilder(path, "-cp", classpath,
                TestServer.class.getCanonicalName(), "\"" + configPath + "\"");
        processBuilder.redirectErrorStream(true);
        try {
            serverProcess = processBuilder.start();
            // Give the server some time to ramp up.
            Thread.sleep(500);
        } catch (IOException e) {
            return false;
        } catch (InterruptedException e) {
            return false;
        }
        return true;
    }

    public void stopServer() {
        if(serverProcess != null) {
            // Send a signal to the server process to make itself shut down.
            serverProcess.destroy();
        }
    }

    public static void main(String args[]) throws Exception {
        if(args.length != 1) {
            throw new Exception("Need exactly one argument containing th path to the configuration");
        }
        final String configPath = args[0];

        // Setup a minimal servlet context for hosting our message broker servlet.
        final Server server = new Server(8400);
        final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/qa-regress");
        final MessageBrokerServlet messageBrokerServlet = new MessageBrokerServlet();
        final ServletHolder servlet = new ServletHolder(messageBrokerServlet);
        servlet.setInitParameter("services.configuration.file", configPath);
        context.addServlet(servlet, "/messagebroker/amf/*");
        server.setHandler(context);
        server.start();

        // Wait for the process to receive a single from the other vm.
        while(true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // Shut down the server.
                server.stop();
                return;
            }
        }
    }

}
