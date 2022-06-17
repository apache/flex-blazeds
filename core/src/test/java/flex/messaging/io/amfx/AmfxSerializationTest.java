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
package flex.messaging.io.amfx;

import flex.messaging.MessageException;
import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.*;
import flex.messaging.validators.ClassDeserializationValidator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;

/**
 * Runs through an extensive suite of deserialization
 * test cases for AMFX data types and message structures.
 * <p>
 * The first step is to turn an XML file into an AMFX request.
 * If the request is not a negative test then it is reserialized
 * back to XML to test the serializer. Finally, the serialized
 * XML is re-deserialized back into a request and once more
 * tested.
 */
public class AmfxSerializationTest {
    private String resourceBase = "/flex/messaging/io/amfx/";
    private String baseFilePath;
    private SerializationContext serializationContext;
    private AmfTrace trace;

    @Before
    public void setUp() throws Exception {
        ClassDeserializationValidator classDeserializationValidator = new ClassDeserializationValidator();
        classDeserializationValidator.addAllowClassPattern("flex.messaging.io.amfx.testtypes.*");
        serializationContext = new SerializationContext();
        serializationContext.setDeserializationValidator(classDeserializationValidator);
        SerializationContext.setSerializationContext(serializationContext);
        //trace = new AmfTrace();
    }

    @After
    public void tearDown() throws Exception {
        SerializationContext.setSerializationContext(null);
        trace = null;
    }

    @Test
    public void testSerialization() {
        try {
            URL url = this.getClass().getResource(resourceBase + "AmfxSerializationTest.class");
            File file = new File(url.getFile());
            File dir = new File(file.getParent());

            baseFilePath = dir.getAbsolutePath();

            if (dir.isDirectory()) {
                String resourcePath = getResourcePath(baseFilePath);
                processDirectory(dir, resourcePath);
            }
        } catch (IOException ioe) {
            Assert.fail("AMFX deserialization test failed: " + ioe.getMessage());
        } finally {
            if (trace != null) {
                System.out.println(trace.toString());
            }
        }
    }

    private void processDirectory(File dir, String resourcePath) throws IOException {
        File[] files = dir.listFiles(new XMLFilenameFilter());

        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    resourcePath = getResourcePath(f.getAbsolutePath());
                    processDirectory(f, resourcePath);
                } else {
                    String testName = getTestName(f.getName());
                    System.out.println("Running test: " + testName);

                    FileInputStream fis = new FileInputStream(f);

                    processRequest(resourcePath, testName, fis);
                }
            }
        }
    }

    private String getResourcePath(String path) {
        if (path.startsWith(baseFilePath)) {
            path = path.substring(baseFilePath.length());
            path = path.replace('\\', '/');
            return path;
        }

        return null;
    }

    private String getTestName(String fileName) {
        fileName = fileName.toLowerCase();
        int pos = fileName.indexOf(".xml");
        return fileName.substring(0, pos);
    }

    private void processRequest(String resourcePath, String testName, InputStream is) throws IOException {
        try {
            SerializationContext.setSerializationContext((SerializationContext) serializationContext.clone());
            // STEP 1. Deserialize Message
            ActionContext context = new ActionContext();
            context.setResponseMessage(new ActionMessage());

            ActionMessage requestMessage = new ActionMessage();
            context.setRequestMessage(requestMessage);
            AmfxMessageDeserializer deserializer = new AmfxMessageDeserializer();
            deserializer.initialize(serializationContext, is, trace);

            if (trace != null) {
                trace.startRequest("Deserializing AMF/HTTP request");
            }

            DeserializationConfirmation confirm = getConfirmation(resourcePath, testName);
            confirm.setContext(serializationContext);
            try {
                deserializer.readMessage(requestMessage, context);
            } catch (MessageException me) // For negative tests that cause MessageExceptions.
            {
                if (!confirm.isNegativeTest()) {
                    Assert.fail("Only negative tests might cause MessageExceptions: " + testName);
                }
            }

            if (!confirm.isNegativeTest() && !confirm.successMatches(requestMessage)) {
                Assert.fail("ActionMessage did not match test: " + testName);
            }

            // STEP 2. Re-serialize Message (Negative tests won't make it this far...)

            // create the response body
            if (requestMessage.getBodyCount() > 0 || requestMessage.getHeaderCount() > 0) {
                ActionMessage responseMessage = context.getResponseMessage();

                if (requestMessage.getBodyCount() > 0) {
                    ArrayList bodies = requestMessage.getBodies();
                    for (Object body : bodies) {
                        MessageBody requestBody = (MessageBody) body;
                        MessageBody responseBody = new MessageBody();
                        responseBody.setData(requestBody.getData());
                        responseMessage.addBody(responseBody);
                    }
                }

                if (requestMessage.getHeaderCount() > 0) {
                    ArrayList headers = requestMessage.getHeaders();
                    for (Object header : headers) {
                        responseMessage.addHeader((MessageHeader) header);
                    }
                }

                if (trace != null) {
                    trace.startRequest("Serializing AMF/HTTP request");
                }

                ByteArrayOutputStream bout = new ByteArrayOutputStream();

                AmfxMessageSerializer serializer = new AmfxMessageSerializer();
                serializer.initialize(serializationContext, bout, trace);
                serializer.writeMessage(responseMessage);

                // STEP 3. Deserialize Message once more...
                requestMessage = new ActionMessage();
                context.setRequestMessage(requestMessage);

                ByteArrayInputStream bis = new ByteArrayInputStream(bout.toByteArray());

                deserializer = new AmfxMessageDeserializer();
                deserializer.initialize(serializationContext, bis, trace);

                if (trace != null) {
                    trace.startRequest("Deserializing AMF/HTTP request");
                }

                deserializer.readMessage(requestMessage, context);

                confirm.setContext(serializationContext);

                if (!confirm.successMatches(requestMessage)) {
                    Assert.fail("ActionMessage did not match test on deserialization after re-serialization: " + testName);
                }
            }
        } finally {
            SerializationContext.setSerializationContext(null);
        }

    }

    private DeserializationConfirmation getConfirmation(String resourcePath, String testName) throws IOException {
        try {
            if (resourceBase.endsWith("/") && resourcePath.startsWith("/")) {
                resourcePath = resourcePath.substring(1);
            }

            String resource = resourceBase + resourcePath;
            if (resource.startsWith("/")) {
                resource = resource.substring(1);
            }

            String packageName = resource.replace('/', '.');

            if (packageName.length() > 0 && !packageName.endsWith(".")) {
                packageName = packageName + ".";
            }

            String className = packageName + "Confirm" + testName;
            Class c = Class.forName(className);

            Object obj = c.newInstance();
            return (DeserializationConfirmation) obj;
        } catch (Exception e) {
            throw new IOException("Unable to load confirmation for testName: " + testName + " in " + resourcePath);
        }
    }

    static class XMLFilenameFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            String child = dir.getAbsolutePath() + File.separator + name;

            File f = new File(child);

            if (f.isDirectory() && !name.startsWith(".svn")) {
                return true;
            } else if (name.toLowerCase().endsWith(".xml")) {
                return true;
            } else {
                return false;
            }
        }
    }

}
