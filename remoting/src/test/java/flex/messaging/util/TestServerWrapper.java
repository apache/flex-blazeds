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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Simple little wrapper starting up a BlazeDS server in a separate VM useful for unit testing the
 * features that need different singletons in client and server.
 */
public class TestServerWrapper {

    private Process serverProcess;

    public int startServer(String configPath) {
        // We can only start one server per instance of TestServer.
        if(serverProcess != null) {
            return -1;
        }

        final String separator = System.getProperty("file.separator");
        final String classpath = System.getProperty("java.class.path");
        final String path = System.getProperty("java.home") + separator + "bin" + separator + "java";
        System.out.print("Starting test-server");
        final ProcessBuilder processBuilder = new ProcessBuilder(path,
                //"-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005",
                "-cp", /*"\"" +*/ classpath /*+ "\""*/,
                TestServer.class.getCanonicalName(), /*"\"" +*/ configPath /*+ "\""*/);
        processBuilder.redirectErrorStream(true);
        try {
            serverProcess = processBuilder.start();

            BufferedReader in = new BufferedReader(new InputStreamReader(serverProcess.getInputStream()));

            String line;
            while((line = in.readLine()) != null) {
                if(line.startsWith("Port:")) {
                    // Read the process output and extract the port
                    // number the server started on.
                    int port = Integer.parseInt(line.substring(5));
                    System.out.println("STARTED on port " + port);
                    return port;
                }
            }

            return -1;
        } catch (IOException e) {
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

}
