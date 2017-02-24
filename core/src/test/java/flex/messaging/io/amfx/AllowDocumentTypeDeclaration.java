/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package flex.messaging.io.amfx;

import flex.messaging.MessageException;
import flex.messaging.util.XMLUtil;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class AllowDocumentTypeDeclaration {

    @Test
    public void testDoctypeEnabled() throws Exception {
        // Start a simple server socket.
        TinyServer server = new TinyServer();
        server.start();

        // Wait till the server is up.
        while (server.port == 0) {
            Thread.sleep(100);
        }

        try {
            String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
                    "<!DOCTYPE foo PUBLIC \"-//VSR//PENTEST//EN\" \"http://localhost:" +
                    server.getPort() + "/service?ssrf\">" +
                    "<foo>Some content</foo>";
            XMLUtil.stringToDocument(xml, true, true, false);

            // The server should have been contacted.
            Assert.assertTrue(server.connected);
        } finally {
            server.kill();
        }
    }

    @Test
    public void testDoctypeDisabled() throws Exception {
        // Start a simple server socket.
        TinyServer server = new TinyServer();
        server.start();

        // Wait till the server is up.
        while (server.port == 0) {
            Thread.sleep(100);
        }

        try {
            StringBuilder xml = new StringBuilder(512);
            xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
            xml.append("<!DOCTYPE foo PUBLIC \"-//VSR//PENTEST//EN\" \"http://localhost:");
            xml.append(server.getPort()).append("/service?ssrf\">");
            xml.append("<foo>Some content</foo>");
            try {
                XMLUtil.stringToDocument(xml.toString(), true, false, false);
                Assert.fail("This should have failed.");
            } catch (MessageException me) {
                Assert.assertTrue(me.getMessage().contains("DOCTYPE"));
            }

            // The server should not have been contacted.
            Assert.assertFalse(server.connected);
        } finally {
            server.kill();
        }
    }

    private class TinyServer extends Thread {

        private int port;
        private boolean connected = false;

        private ServerSocket serverSocket;
        private Socket clientSocket;

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(0);
                port = serverSocket.getLocalPort();
                clientSocket = serverSocket.accept();
                connected = true;
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                while (reader.ready()) {
                    String line = reader.readLine();
                    System.out.println(line);
                }
                OutputStream out = clientSocket.getOutputStream();
                out.write("HTTP/1.0 200 OK".getBytes());
                out.write("Content-Type: text/plain".getBytes());
                out.write("Content-Length: 1354".getBytes());
                out.write(("<!DOCTYPE foo [" +
                        "<!ELEMENT foo>" +
                        "]>").getBytes());
                out.flush();

                // It seems we need a little sleep here or the Dom parser hangs forever.
                Thread.sleep(100);
            } catch (Exception e) {
                // Ignore.
            } finally {
                try {
                    clientSocket.getOutputStream().close();
                } catch (Exception e) {
                    // Ignore ...
                }
                try {
                    clientSocket.getOutputStream().close();
                } catch (Exception e) {
                    // Ignore ...
                }
            }
        }

        void kill() {
            try {
                serverSocket.close();
            } catch (IOException e) {
                // Ignore.
            }
        }

        int getPort() {
            return port;
        }
    }

}
