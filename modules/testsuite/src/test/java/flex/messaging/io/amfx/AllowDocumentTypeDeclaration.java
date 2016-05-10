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
import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by christoferdutz on 23.07.15.
 */

public class AllowDocumentTypeDeclaration extends TestCase {

    public void testDoctypeEnabled() throws Exception {
        // Start a simple server socket.
        TinyServer server = new TinyServer();
        server.start();

        // Sleep for half a second.
        Thread.sleep(500);

        try {
            StringBuffer xml = new StringBuffer(512);
            xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
            xml.append("<!DOCTYPE foo PUBLIC \"-//VSR//PENTEST//EN\" \"http://localhost:" + server.getPort() +
                    "/service?ssrf\">");
            xml.append("<foo>Some content</foo>");
            XMLUtil.stringToDocument(xml.toString(), true, true, false);

            // The server should have been contacted.
            Assert.assertTrue(server.connected);
        } finally {
            server.kill();
        }
    }

    public void testDoctypeDisabled() throws Exception {
        // Start a simple server socket.
        TinyServer server = new TinyServer();
        server.start();

        // Sleep for half a second.
        Thread.sleep(500);

        try {
            StringBuffer xml = new StringBuffer(512);
            xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
            xml.append("<!DOCTYPE foo PUBLIC \"-//VSR//PENTEST//EN\" \"http://localhost:" + server.getPort() +
                    "/service?ssrf\">");
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
                while(reader.ready()) {
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
            } catch (Exception e) {
                // Ignore.
            } finally {
                try {
                    clientSocket.getOutputStream().close();
                } catch(Exception e) {
                    // Ignore ...
                }
                try {
                    clientSocket.getOutputStream().close();
                } catch(Exception e) {
                    // Ignore ...
                }
            }
        }

        public void kill() {
            try {
                serverSocket.close();
            } catch (IOException e) {
                // Ignore.
            }
        }

        public int getPort() {
            return port;
        }

        public boolean isConnected() {
            return connected;
        }
    }

}
