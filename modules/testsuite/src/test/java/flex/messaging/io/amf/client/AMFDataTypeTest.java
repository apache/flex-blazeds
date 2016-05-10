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

import java.util.Date;
import java.util.List;

import org.w3c.dom.Document;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import amfclient.ClientCustomType;

import flex.messaging.util.TestServer;
import flex.messaging.io.amf.client.exceptions.ClientStatusException;
import flex.messaging.io.amf.client.exceptions.ServerStatusException;
import flex.messaging.util.XMLUtil;

/**
 * JUnit tests for AMFConnection. Note that most of the tests require a running
 * server with the specified destination. 
 */
public class AMFDataTypeTest extends TestCase
{
    private static final String DEFAULT_DESTINATION_ID = "amfConnectionTestService";
    private static final String DEFAULT_METHOD_NAME = "echoString";
    private static final String DEFAULT_METHOD_ARG = "echo me";
    private static final String DEFAULT_URL = "http://localhost:%s/qa-regress/messagebroker/amf";
    private static final String DEFAULT_AMF_OPERATION = getOperationCall(DEFAULT_METHOD_NAME);
    private static final String UNEXPECTED_EXCEPTION_STRING = "Unexpected exception: ";

    private TestServer server;
    private int serverPort;

    /**
     * Given a remote method name, returns the AMF connection call needed using
     * the default destination id.
     */
    private static String getOperationCall(String method)
    {
        return DEFAULT_DESTINATION_ID + "." + method;
    }

    protected String getConnectionUrl() {
        return String.format(DEFAULT_URL, serverPort);
    }


    public AMFDataTypeTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        //TestSuite suite = new TestSuite(AMFDataTypeTest.class);
        TestSuite suite = new TestSuite();
        suite.addTest(new AMFDataTypeTest("testCallStringArgStringReturn"));
        suite.addTest(new AMFDataTypeTest("testCallIntArgIntReturn"));
        suite.addTest(new AMFDataTypeTest("testCallBooleanArgBooleanReturn"));
        suite.addTest(new AMFDataTypeTest("testCallObjectArgObjectReturn"));
        suite.addTest(new AMFDataTypeTest("testCallObjectArgCustomReturn"));
        suite.addTest(new AMFDataTypeTest("testCallCustomArgObjectReturn"));
        suite.addTest(new AMFDataTypeTest("testCallCustomArgCustomReturn"));
        suite.addTest(new AMFDataTypeTest("testCallNoArgObjectReturn"));
        suite.addTest(new AMFDataTypeTest("testCallNoArgCustomReturn"));
        suite.addTest(new AMFDataTypeTest("testCallNoArgObjectArrayReturn"));
        suite.addTest(new AMFDataTypeTest("testCallDateArgDateReturn"));
        suite.addTest(new AMFDataTypeTest("testCallShortArgShortReturn"));
        suite.addTest(new AMFDataTypeTest("testCallDoubleArgDoubleReturn"));
        suite.addTest(new AMFDataTypeTest("testCallIntArrayArgIntArrayReturn"));
        suite.addTest(new AMFDataTypeTest("testCallObjectArrayArgObjectArrayReturn"));
        suite.addTest(new AMFDataTypeTest("testXMLDocument"));
        return suite;
    }

    protected void setUp() throws Exception
    {
        server = new TestServer();
        serverPort = server.startServer("classpath:/WEB-INF/flex/services-config.xml");
        if(serverPort == -1) {
            Assert.fail("Couldn't start server process");
        }
        // Give the "server" some time to startup.
        Thread.sleep(400L);

        AMFConnection.registerAlias(
                "remoting.amfclient.ServerCustomType" /* server type */,
                "amfclient.ClientCustomType" /* client type */);
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        server.stopServer();
        server = null;

        super.tearDown();
    }

    public void testCallStringArgStringReturn()
    {
        try
        {
            internalTestCall(DEFAULT_AMF_OPERATION, DEFAULT_METHOD_ARG, new CallResultHandler(){
                public void onResult(Object result)
                {
                    Assert.assertEquals(DEFAULT_METHOD_ARG, result);
                }
            });
        }
        catch (Exception e)
        {
            fail(UNEXPECTED_EXCEPTION_STRING + e);
        }
    }

    public void testCallIntArgIntReturn()
    {
        String method = "echoInt";
        final int methodArg = 1;
        try
        {
            internalTestCall(getOperationCall(method), methodArg, new CallResultHandler(){
                public void onResult(Object result)
                {
                    Assert.assertEquals(methodArg, ((Double)result).intValue());
                }
            });
        }
        catch (Exception e)
        {
            fail(UNEXPECTED_EXCEPTION_STRING + e);
        }
    }

    public void testCallBooleanArgBooleanReturn()
    {
        try
        {
            String method = "echoBoolean";
            final boolean methodArg = true;
            internalTestCall(getOperationCall(method), methodArg, new CallResultHandler(){
                public void onResult(Object result)
                {
                    Assert.assertEquals(methodArg, result);
                }
            });
        }
        catch (Exception e)
        {
            fail(UNEXPECTED_EXCEPTION_STRING + e);
        }
    }

    public void testCallDateArgDateReturn()
    {
        String method = "echoDate";
        final Date methodArg = new Date(999991);
        try
        {
            internalTestCall(getOperationCall(method), methodArg, new CallResultHandler(){
                public void onResult(Object result)
                {
                    Assert.assertEquals(methodArg, result);
                }
            });
        }
        catch (Exception e)
        {
            fail(UNEXPECTED_EXCEPTION_STRING + e);
        }
    }

    public void testCallShortArgShortReturn()
    {
        String method = "echoShort";
        final short methodArg = 32000;
        try
        {
            internalTestCall(getOperationCall(method), methodArg, new CallResultHandler(){
                public void onResult(Object result)
                {
                    Assert.assertEquals(methodArg, ((Double)result).shortValue());
                }
            });
        }
        catch (Exception e)
        {
            fail(UNEXPECTED_EXCEPTION_STRING + e);
        }
    }

    public void testCallDoubleArgDoubleReturn()
    {
        String method = "echoDouble";
        final double methodArg = -95.25;
        try
        {
            internalTestCall(getOperationCall(method), methodArg, new CallResultHandler(){
                public void onResult(Object result)
                {
                    Assert.assertEquals(methodArg, result);
                }
            });
        }
        catch (Exception e)
        {
            fail(UNEXPECTED_EXCEPTION_STRING + e);
        }
    }

    public void testCallObjectArgObjectReturn()
    {
        String method = "echoObject1";
        ClientCustomType temp = new ClientCustomType();
        temp.setId(1);
        final Object methodArg = temp;
        try
        {
            internalTestCall(getOperationCall(method), methodArg, new CallResultHandler(){
                public void onResult(Object result)
                {
                    ClientCustomType temp2 = (ClientCustomType)result;
                    Assert.assertEquals(1, temp2.getId());
                }
            });
        }
        catch (Exception e)
        {
            fail(UNEXPECTED_EXCEPTION_STRING + e);
        }
    }

    public void testCallObjectArgCustomReturn()
    {
        String method = "echoObject2";
        ClientCustomType temp = new ClientCustomType();
        temp.setId(1);
        final Object methodArg = temp;
        try
        {
            internalTestCall(getOperationCall(method), methodArg, new CallResultHandler(){
                public void onResult(Object result)
                {
                    ClientCustomType temp2 = (ClientCustomType)result;
                    Assert.assertEquals(1, temp2.getId());
                }
            });
        }
        catch (Exception e)
        {
            fail(UNEXPECTED_EXCEPTION_STRING + e);
        }
    }

    public void testCallCustomArgObjectReturn()
    {
        String method = "echoObject3";
        final ClientCustomType methodArg = new ClientCustomType();
        methodArg.setId(1);
        try
        {
            internalTestCall(getOperationCall(method), methodArg, new CallResultHandler(){
                public void onResult(Object result)
                {
                    ClientCustomType temp2 = (ClientCustomType)result;
                    Assert.assertEquals(1, temp2.getId());
                }
            });
        }
        catch (Exception e)
        {
            fail(UNEXPECTED_EXCEPTION_STRING + e);
        }
    }

    public void testCallCustomArgCustomReturn()
    {
        String method = "echoObject4";
        final ClientCustomType methodArg = new ClientCustomType();
        methodArg.setId(1);
        try
        {
            internalTestCall(getOperationCall(method), methodArg, new CallResultHandler(){
                public void onResult(Object result)
                {
                    ClientCustomType temp2 = (ClientCustomType)result;
                    Assert.assertEquals(1, temp2.getId());
                }
            });
        }
        catch (Exception e)
        {
            fail(UNEXPECTED_EXCEPTION_STRING + e);
        }
    }

    public void testCallNoArgObjectReturn()
    {
        String method = "getObject1";
        try
        {
            internalTestCall(getOperationCall(method), null, new CallResultHandler(){
                public void onResult(Object result)
                {
                    ClientCustomType temp2 = (ClientCustomType)result;
                    Assert.assertEquals(1, temp2.getId());
                }
            });
        }
        catch (Exception e)
        {
            fail(UNEXPECTED_EXCEPTION_STRING + e);
        }
    }

    public void testCallNoArgCustomReturn()
    {
        String method = "getObject2";
        try
        {
            internalTestCall(getOperationCall(method), null, new CallResultHandler(){
                public void onResult(Object result)
                {
                    ClientCustomType temp2 = (ClientCustomType)result;
                    Assert.assertEquals(1, temp2.getId());
                }
            });
        }
        catch (Exception e)
        {
            fail(UNEXPECTED_EXCEPTION_STRING + e);
        }
    }

    public void testCallNoArgObjectArrayReturn()
    {
        String method = "getObjectArray1";
        try
        {
            internalTestCall(getOperationCall(method), null, new CallResultHandler(){
                public void onResult(Object result)
                {
                    List temp = (List)result;
                    for (int i = 0; i < temp.size(); i++)
                    {
                        ClientCustomType temp2 = (ClientCustomType)temp.get(i);
                        Assert.assertEquals(i, temp2.getId());
                    }
                }
            });
        }
        catch (Exception e)
        {
            fail(UNEXPECTED_EXCEPTION_STRING + e);
        }
    }

    public void testCallIntArrayArgIntArrayReturn()
    {
        String method = "echoObject5";
        final int[] methodArg = new int[] {0,1,2,3};
        try
        {
            internalTestCall(getOperationCall(method), methodArg, new CallResultHandler(){
                public void onResult(Object result)
                {
                    List temp = (List)result;
                    for (int i = 0; i < temp.size(); i++)
                    {
                        Assert.assertEquals(i, ((Integer)temp.get(i)).intValue());
                    }
                }
            });
        }
        catch (Exception e)
        {
            fail(UNEXPECTED_EXCEPTION_STRING + e);
        }
    }

    public void testCallObjectArrayArgObjectArrayReturn()
    {
        String method = "echoObject1";
        Object[] temp = new Object[3];
        for (int i = 0; i < temp.length; i++)
        {
            ClientCustomType cct = new ClientCustomType();
            cct.setId(i);
            temp[i] = cct;
        }
        final Object[] methodArg = temp;
        try
        {
            internalTestCall(getOperationCall(method), methodArg, new CallResultHandler(){
                public void onResult(Object result)
                {
                    List temp = (List)result;
                    for (int i = 0; i < temp.size(); i++)
                    {
                        ClientCustomType temp2 = (ClientCustomType)temp.get(i);
                        Assert.assertEquals(i, temp2.getId());
                    }
                }
            });
        }
        catch (Exception e)
        {
            fail(UNEXPECTED_EXCEPTION_STRING + e);
        }
    }

  
    public void testXMLDocument()
    {
        try
        {
            String method = "echoObject1";
            final StringBuffer xml = new StringBuffer(512);
            xml.append("<test>    <item id=\"1\">        <sweet/>    </item></test>");

            Document xmlDoc = XMLUtil.stringToDocument(xml.toString());
            final Object methodArg = xmlDoc;
            internalTestCall(getOperationCall(method), methodArg, new CallResultHandler(){
                public void onResult(Object result)
                {
                    try
                    {
                        Document retXmlDoc = (Document)result;
                        String retXML = XMLUtil.documentToString(retXmlDoc);
                        Assert.assertEquals(xml.toString(), retXML);
                    }
                    catch (Exception e)
                    {
                        fail(UNEXPECTED_EXCEPTION_STRING + e);
                    }
                }
            });
        }
        catch (Exception e)
        {
            fail(UNEXPECTED_EXCEPTION_STRING + e);
        }
    }

    // A simple interface to handle AMF call results.
    private interface CallResultHandler
    {
        void onResult(Object result);
    }

    // Helper method used by JUnit tests to pass in an operation and method argument
    // When the AMF call returns, CallResultHandler.onResult is called to Assert things.
    private void internalTestCall(String operation, Object methodArg, CallResultHandler resultHandler) throws ClientStatusException, ServerStatusException
    {
        AMFConnection amfConnection = new AMFConnection();
        // Connect.
        amfConnection.connect(getConnectionUrl());
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
