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
import java.net.ServerSocket;

/**
 * Simple little wrapper starting up a BlazeDS server in a separate VM useful for unit testing the
 * features that need different singletons in client and server.
 */
public class TestServer {

    private Process serverProcess;

    public int startServer(String configPath) {
        // We can only start one server per instance of TestServer.
        if(serverProcess != null) {
            return -1;
        }

        final String separator = System.getProperty("file.separator");
        final String classpath = System.getProperty("java.class.path");
        final String path = System.getProperty("java.home") + separator + "bin" + separator + "java";
        final int port = findFreePort();
        System.out.print("Starting test-server on port: " + port + " ... ");
        final ProcessBuilder processBuilder = new ProcessBuilder(path,
                /*"-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005",*/
                "-cp", /*"\"" +*/ classpath /*+ "\""*/,
                TestServer.class.getCanonicalName(), /*"\"" +*/ configPath /*+ "\""*/,
                Integer.toString(port));
        processBuilder.redirectErrorStream(true);
        try {
            serverProcess = processBuilder.start();
            // Give the server some time to ramp up.
            Thread.sleep(3000);
            System.out.println("STARTED");
            return port;
        } catch (IOException e) {
            System.out.println("ERROR: " + e.toString());
            return -1;
        } catch (InterruptedException e) {
            System.out.println("ERROR: " + e.toString());
            return -1;
        }
    }

    public void stopServer() {
        if(serverProcess != null) {
            System.out.print("Stopping test-server ... ");
            // Send a signal to the server process to make itself shut down.
            serverProcess.destroy();
            System.out.println("STOPPED");
        }
    }

    public static void main(String args[]) throws Exception {
        if(args.length != 2) {
            throw new Exception("Need exactly two argument containing th path to the configuration " +
                    "followed by the port number the server should use");
        }
        final String configPath = args[0];

        // Setup a minimal servlet context for hosting our message broker servlet.
        final int port = Integer.valueOf(args[1]);
        final Server server = new Server(port);
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

    private static int findFreePort() {
        ServerSocket socket = null;
        try {
            socket = new ServerSocket(0);
            socket.setReuseAddress(true);
            int port = socket.getLocalPort();
            try {
                socket.close();
            } catch (IOException e) {
                // Ignore IOException on close()
            }
            return port;
        } catch (IOException e) {
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
        throw new IllegalStateException("Could not find a free TCP/IP port to start embedded Jetty HTTP Server on");
    }

}
