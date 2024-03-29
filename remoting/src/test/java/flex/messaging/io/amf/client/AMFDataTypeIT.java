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
package flex.messaging.io.amf.client;

import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.client.exceptions.ClientStatusException;
import flex.messaging.io.amf.client.exceptions.ServerStatusException;
import flex.messaging.util.TestServerWrapper;
import flex.messaging.util.XMLUtil;
import flex.messaging.validators.ClassDeserializationValidator;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import remoting.amfclient.ClientCustomType;

import java.util.Date;
import java.util.List;

/**
 * JUnit tests for AMFConnection. Note that most of the tests require a running
 * server with the specified destination.
 */
public class AMFDataTypeIT {
    private static final String DEFAULT_DESTINATION_ID = "amfConnectionTestService";
    private static final String DEFAULT_METHOD_NAME = "echoString";
    private static final String DEFAULT_METHOD_ARG = "echo me";
    private static final String DEFAULT_URL = "http://localhost:%s/qa-regress/messagebroker/amf";
    private static final String DEFAULT_AMF_OPERATION = getOperationCall(DEFAULT_METHOD_NAME);
    private static final String UNEXPECTED_EXCEPTION_STRING = "Unexpected exception: ";
    private static final String UNEXPECTED_SUCCESS_STRING = "Unexpected success of previous operation";

    private static TestServerWrapper standardValidationServerWrapper;
    private static int standardValidationServerPort;
    private static TestServerWrapper customValidationServerWrapper;
    private static int customValidationServerPort;
    private static SerializationContext serializationContext;

    @BeforeClass
    public static void setup() {
        standardValidationServerWrapper = new TestServerWrapper();
        standardValidationServerPort = standardValidationServerWrapper.startServer("classpath:/WEB-INF/flex/services-config.xml");
        if (standardValidationServerPort == -1) {
            Assert.fail("Couldn't start server (standard validation) process");
        }

        customValidationServerWrapper = new TestServerWrapper();
        customValidationServerPort = customValidationServerWrapper.startServer("classpath:/WEB-INF/flex/services-config-customized-validation.xml");
        if (customValidationServerPort == -1) {
            Assert.fail("Couldn't start server (custom validation) process");
        }

        AMFConnection.registerAlias(
                "remoting.amfclient.ServerCustomType" /* server type */,
                "remoting.amfclient.ClientCustomType" /* client type */);

        serializationContext = SerializationContext.getSerializationContext();
        ClassDeserializationValidator deserializationValidator =
                (ClassDeserializationValidator) serializationContext.getDeserializationValidator();
        deserializationValidator.addAllowClassPattern("remoting.amfclient.*");
        serializationContext.createASObjectForMissingType = true;
        // Make sure collections are written out as Arrays (vs. ArrayCollection),
        // in case the server does not recognize ArrayCollections.
        serializationContext.legacyCollection = true;
        // When legacyMap is true, Java Maps are serialized as ECMA arrays
        // instead of anonymous Object.
        serializationContext.legacyMap = true;
        // Disable serialization of xml documents.
        serializationContext.allowXml = false;
    }

    @AfterClass
    public static void teardown() {
        standardValidationServerWrapper.stopServer();
        standardValidationServerWrapper = null;
        customValidationServerWrapper.stopServer();
        customValidationServerWrapper = null;
    }

    @Test
    public void testCallStringArgStringReturn() {
        try {
            internalTestCall(DEFAULT_AMF_OPERATION, DEFAULT_METHOD_ARG, new CallResultHandler() {
                public void onResult(Object result) {
                    Assert.assertEquals(DEFAULT_METHOD_ARG, result);
                }
            }, false);
        } catch (Exception e) {
            Assert.fail(UNEXPECTED_EXCEPTION_STRING + e);
        }
    }

    @Test
    public void testCallIntArgIntReturn() {
        String method = "echoInt";
        final int methodArg = 1;
        try {
            internalTestCall(getOperationCall(method), methodArg, new CallResultHandler() {
                public void onResult(Object result) {
                    Assert.assertEquals(methodArg, ((Double) result).intValue());
                }
            }, false);
        } catch (Exception e) {
            Assert.fail(UNEXPECTED_EXCEPTION_STRING + e);
        }
    }

    @Test
    public void testCallBooleanArgBooleanReturn() {
        try {
            String method = "echoBoolean";
            final boolean methodArg = true;
            internalTestCall(getOperationCall(method), methodArg, new CallResultHandler() {
                public void onResult(Object result) {
                    Assert.assertEquals(methodArg, result);
                }
            }, false);
        } catch (Exception e) {
            Assert.fail(UNEXPECTED_EXCEPTION_STRING + e);
        }
    }

    @Test
    public void testCallDateArgDateReturn() {
        String method = "echoDate";
        final Date methodArg = new Date(999991);
        try {
            internalTestCall(getOperationCall(method), methodArg, new CallResultHandler() {
                public void onResult(Object result) {
                    Assert.assertEquals(methodArg, result);
                }
            }, false);
        } catch (Exception e) {
            Assert.fail(UNEXPECTED_EXCEPTION_STRING + e);
        }
    }

    @Test
    public void testCallShortArgShortReturn() {
        String method = "echoShort";
        final short methodArg = 32000;
        try {
            internalTestCall(getOperationCall(method), methodArg, new CallResultHandler() {
                public void onResult(Object result) {
                    Assert.assertEquals(methodArg, ((Double) result).shortValue());
                }
            }, false);
        } catch (Exception e) {
            Assert.fail(UNEXPECTED_EXCEPTION_STRING + e);
        }
    }

    @Test
    public void testCallDoubleArgDoubleReturn() {
        String method = "echoDouble";
        final double methodArg = -95.25;
        try {
            internalTestCall(getOperationCall(method), methodArg, new CallResultHandler() {
                public void onResult(Object result) {
                    Assert.assertEquals(methodArg, result);
                }
            }, false);
        } catch (Exception e) {
            Assert.fail(UNEXPECTED_EXCEPTION_STRING + e);
        }
    }

    @Test
    public void testCallObjectArgObjectReturn() {
        String method = "echoObject1";
        ClientCustomType temp = new ClientCustomType();
        temp.setId(1);
        final Object methodArg = temp;
        try {
            internalTestCall(getOperationCall(method), methodArg, new CallResultHandler() {
                public void onResult(Object result) {
                    ClientCustomType temp2 = (ClientCustomType) result;
                    Assert.assertEquals(1, temp2.getId());
                }
            }, false);
            Assert.fail(UNEXPECTED_SUCCESS_STRING);
        } catch (Exception e) {
            // An exception is what we expect here.
        }
    }

    @Test
    public void testCallObjectArgObjectReturnCustomizedValidation() {
        String method = "echoObject1";
        ClientCustomType temp = new ClientCustomType();
        temp.setId(1);
        final Object methodArg = temp;
        try {
            internalTestCall(getOperationCall(method), methodArg, new CallResultHandler() {
                public void onResult(Object result) {
                    ClientCustomType temp2 = (ClientCustomType) result;
                    Assert.assertEquals(1, temp2.getId());
                }
            }, true);
        } catch (Exception e) {
            Assert.fail(UNEXPECTED_EXCEPTION_STRING + e);
        }
    }

    @Test
    public void testCallObjectArgCustomReturn() {
        String method = "echoObject2";
        ClientCustomType temp = new ClientCustomType();
        temp.setId(1);
        final Object methodArg = temp;
        try {
            internalTestCall(getOperationCall(method), methodArg, new CallResultHandler() {
                public void onResult(Object result) {
                    ClientCustomType temp2 = (ClientCustomType) result;
                    Assert.assertEquals(1, temp2.getId());
                }
            }, false);
            Assert.fail(UNEXPECTED_SUCCESS_STRING);
        } catch (Exception e) {
            // An exception is what we expect here.
        }
    }

    @Test
    public void testCallObjectArgCustomReturnCustomizedValidation() {
        String method = "echoObject2";
        ClientCustomType temp = new ClientCustomType();
        temp.setId(1);
        final Object methodArg = temp;
        try {
            internalTestCall(getOperationCall(method), methodArg, new CallResultHandler() {
                public void onResult(Object result) {
                    ClientCustomType temp2 = (ClientCustomType) result;
                    Assert.assertEquals(1, temp2.getId());
                }
            }, true);
        } catch (Exception e) {
            Assert.fail(UNEXPECTED_EXCEPTION_STRING + e);
        }
    }

    @Test
    public void testCallCustomArgObjectReturn() {
        String method = "echoObject3";
        final ClientCustomType methodArg = new ClientCustomType();
        methodArg.setId(1);
        try {
            internalTestCall(getOperationCall(method), methodArg, new CallResultHandler() {
                public void onResult(Object result) {
                    ClientCustomType temp2 = (ClientCustomType) result;
                    Assert.assertEquals(1, temp2.getId());
                }
            }, false);
            Assert.fail(UNEXPECTED_SUCCESS_STRING);
        } catch (Exception e) {
            // An exception is what we expect here.
        }
    }

    @Test
    public void testCallCustomArgObjectReturnCustomizedValidation() {
        String method = "echoObject3";
        final ClientCustomType methodArg = new ClientCustomType();
        methodArg.setId(1);
        try {
            internalTestCall(getOperationCall(method), methodArg, new CallResultHandler() {
                public void onResult(Object result) {
                    ClientCustomType temp2 = (ClientCustomType) result;
                    Assert.assertEquals(1, temp2.getId());
                }
            }, true);
        } catch (Exception e) {
            Assert.fail(UNEXPECTED_EXCEPTION_STRING + e);
        }
    }

    @Test
    public void testCallCustomArgCustomReturn() {
        String method = "echoObject4";
        final ClientCustomType methodArg = new ClientCustomType();
        methodArg.setId(1);
        try {
            internalTestCall(getOperationCall(method), methodArg, new CallResultHandler() {
                public void onResult(Object result) {
                    ClientCustomType temp2 = (ClientCustomType) result;
                    Assert.assertEquals(1, temp2.getId());
                }
            }, false);
            Assert.fail(UNEXPECTED_SUCCESS_STRING);
        } catch (Exception e) {
            // An exception is what we expect here.
        }
    }

    @Test
    public void testCallCustomArgCustomReturnCustomizedValidation() {
        String method = "echoObject4";
        final ClientCustomType methodArg = new ClientCustomType();
        methodArg.setId(1);
        try {
            internalTestCall(getOperationCall(method), methodArg, new CallResultHandler() {
                public void onResult(Object result) {
                    ClientCustomType temp2 = (ClientCustomType) result;
                    Assert.assertEquals(1, temp2.getId());
                }
            }, true);
        } catch (Exception e) {
            Assert.fail(UNEXPECTED_EXCEPTION_STRING + e);
        }
    }

    @Test
    public void testCallNoArgObjectReturn() {
        String method = "getObject1";
        try {
            internalTestCall(getOperationCall(method), null, new CallResultHandler() {
                public void onResult(Object result) {
                    ClientCustomType temp2 = (ClientCustomType) result;
                    Assert.assertEquals(1, temp2.getId());
                }
            }, false);
        } catch (Exception e) {
            Assert.fail(UNEXPECTED_EXCEPTION_STRING + e);
        }
    }

    @Test
    public void testCallNoArgCustomReturn() {
        String method = "getObject2";
        try {
            internalTestCall(getOperationCall(method), null, new CallResultHandler() {
                public void onResult(Object result) {
                    ClientCustomType temp2 = (ClientCustomType) result;
                    Assert.assertEquals(1, temp2.getId());
                }
            }, false);
        } catch (Exception e) {
            Assert.fail(UNEXPECTED_EXCEPTION_STRING + e);
        }
    }

    @Test
    public void testCallNoArgObjectArrayReturn() {
        String method = "getObjectArray1";
        try {
            internalTestCall(getOperationCall(method), null, new CallResultHandler() {
                public void onResult(Object result) {
                    List temp = (List) result;
                    for (int i = 0; i < temp.size(); i++) {
                        ClientCustomType temp2 = (ClientCustomType) temp.get(i);
                        Assert.assertEquals(i, temp2.getId());
                    }
                }
            }, false);
        } catch (Exception e) {
            Assert.fail(UNEXPECTED_EXCEPTION_STRING + e);
        }
    }

    @Test
    public void testCallIntArrayArgIntArrayReturn() {
        String method = "echoObject5";
        final int[] methodArg = new int[]{0, 1, 2, 3};
        try {
            internalTestCall(getOperationCall(method), methodArg, new CallResultHandler() {
                public void onResult(Object result) {
                    List temp = (List) result;
                    for (int i = 0; i < temp.size(); i++) {
                        Assert.assertEquals(i, ((Integer) temp.get(i)).intValue());
                    }
                }
            }, false);
        } catch (Exception e) {
            Assert.fail(UNEXPECTED_EXCEPTION_STRING + e);
        }
    }

    @Test
    public void testCallObjectArrayArgObjectArrayReturn() {
        String method = "echoObject1";
        Object[] temp = new Object[3];
        for (int i = 0; i < temp.length; i++) {
            ClientCustomType cct = new ClientCustomType();
            cct.setId(i);
            temp[i] = cct;
        }
        final Object[] methodArg = temp;
        try {
            internalTestCall(getOperationCall(method), methodArg, new CallResultHandler() {
                public void onResult(Object result) {
                    List temp = (List) result;
                    for (int i = 0; i < temp.size(); i++) {
                        ClientCustomType temp2 = (ClientCustomType) temp.get(i);
                        Assert.assertEquals(i, temp2.getId());
                    }
                }
            }, false);
            Assert.fail(UNEXPECTED_SUCCESS_STRING);
        } catch (Exception e) {
            // An exception is what we expect here.
        }
    }

    @Test
    public void testCallObjectArrayArgObjectArrayReturnCustomizedValidation() {
        String method = "echoObject1";
        Object[] temp = new Object[3];
        for (int i = 0; i < temp.length; i++) {
            ClientCustomType cct = new ClientCustomType();
            cct.setId(i);
            temp[i] = cct;
        }
        final Object[] methodArg = temp;
        try {
            internalTestCall(getOperationCall(method), methodArg, new CallResultHandler() {
                public void onResult(Object result) {
                    List temp = (List) result;
                    for (int i = 0; i < temp.size(); i++) {
                        ClientCustomType temp2 = (ClientCustomType) temp.get(i);
                        Assert.assertEquals(i, temp2.getId());
                    }
                }
            }, true);
        } catch (Exception e) {
            Assert.fail(UNEXPECTED_EXCEPTION_STRING + e);
        }
    }

    @Test
    public void testXMLDocumentEnabledXml() {
        try {
            // Temporarily enable xml serialization/deserialization.
            serializationContext.allowXml = true;

            String method = "echoObject1";
            final StringBuffer xml = new StringBuffer(512);
            xml.append("<test>    <item id=\"1\">        <sweet/>    </item></test>");

            Document xmlDoc = XMLUtil.stringToDocument(xml.toString());
            final Object methodArg = xmlDoc;
            internalTestCall(getOperationCall(method), methodArg, new CallResultHandler() {
                public void onResult(Object result) {
                    try {
                        Document retXmlDoc = (Document) result;
                        String retXML = XMLUtil.documentToString(retXmlDoc);
                        Assert.assertEquals(xml.toString(), retXML);
                    } catch (Exception e) {
                        Assert.fail(UNEXPECTED_EXCEPTION_STRING + e);
                    }
                }
            }, false);
        } catch (Exception e) {
            Assert.fail(UNEXPECTED_EXCEPTION_STRING + e);
        } finally {
            // Disable xml serialization/deserialization again.
            serializationContext.allowXml = false;
        }
    }

    @Test
    public void testXMLDocumentDisabledXml() {
        try {
            String method = "echoObject1";
            final StringBuffer xml = new StringBuffer(512);
            xml.append("<test>    <item id=\"1\">        <sweet/>    </item></test>");

            Document xmlDoc = XMLUtil.stringToDocument(xml.toString());
            final Object methodArg = xmlDoc;
            internalTestCall(getOperationCall(method), methodArg, new CallResultHandler() {
                public void onResult(Object result) {
                    try {
                        Document retXmlDoc = (Document) result;
                        String retXML = XMLUtil.documentToString(retXmlDoc);
                        Assert.assertEquals("", retXML);
                    } catch (Exception e) {
                        Assert.fail(UNEXPECTED_EXCEPTION_STRING + e);
                    }
                }
            }, false);
        } catch (Exception e) {
            Assert.fail(UNEXPECTED_EXCEPTION_STRING + e);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////
    // Utility methods
    ///////////////////////////////////////////////////////////////////////////////

    /**
     * Given a remote method name, returns the AMF connection call needed using
     * the default destination id.
     */
    private static String getOperationCall(String method) {
        return DEFAULT_DESTINATION_ID + "." + method;
    }

    private static String getStandardValidationConnectionUrl() {
        return String.format(DEFAULT_URL, standardValidationServerPort);
    }

    private static String getCustomValidationConnectionUrl() {
        return String.format(DEFAULT_URL, customValidationServerPort);
    }

    // A simple interface to handle AMF call results.
    private interface CallResultHandler {
        void onResult(Object result);
    }

    // Helper method used by JUnit tests to pass in an operation and method argument
    // When the AMF call returns, CallResultHandler.onResult is called to Assert things.
    private void internalTestCall(String operation, Object methodArg, CallResultHandler resultHandler, boolean customizedValidation) throws ClientStatusException, ServerStatusException {
        AMFConnection amfConnection = new AMFConnection();
        // Connect.
        if (customizedValidation) {
            amfConnection.connect(getCustomValidationConnectionUrl(), serializationContext);
        } else {
            amfConnection.connect(getStandardValidationConnectionUrl(), serializationContext);
        }
        // Make a remoting call and retrieve the result.
        Object result;
        if (methodArg == null)
            result = amfConnection.call(operation);
        else
            result = amfConnection.call(operation, methodArg);
        resultHandler.onResult(result);
        amfConnection.close();
    }
}
