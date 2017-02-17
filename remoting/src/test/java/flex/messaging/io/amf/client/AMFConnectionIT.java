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

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;

import flex.messaging.util.TestServerWrapper;
import junit.extensions.TestSetup;
import org.junit.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import remoting.amfclient.ClientCustomType;

import flex.messaging.MessageException;
import flex.messaging.messages.RemotingMessage;
import flex.messaging.io.amf.ASObject;
import flex.messaging.io.amf.AmfTrace;
import flex.messaging.io.amf.client.AMFConnection.HttpResponseInfo;
import flex.messaging.io.amf.client.exceptions.ClientStatusException;
import flex.messaging.io.amf.client.exceptions.ServerStatusException;
import flex.messaging.io.MessageIOConstants;


/**
 * JUnit tests for AMFConnection. Note that most of the tests require a running
 * server with the specified destination.
 */
public class AMFConnectionIT extends TestCase
{
    private static final String DEFAULT_DESTINATION_ID = "amfConnectionTestService";
    private static final String DEFAULT_METHOD_NAME = "echoString";
    private static final String DEFAULT_METHOD_ARG = "echo me";
    private static final String DEFAULT_URL = "http://localhost:%s/qa-regress/messagebroker/amf";
    private static final String DEFAULT_AMF_OPERATION = getOperationCall(DEFAULT_METHOD_NAME);
    private static final String FOO_STRING = "foo";
    private static final String BAR_STRING = "bar";
    private static final String UNEXPECTED_EXCEPTION_STRING = "Unexpected exception: ";

    private static TestServerWrapper serverWrapper;
    private static int serverPort;

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


    public AMFConnectionIT(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        //TestSuite suite = new TestSuite(AMFConnectionIT.class);
        TestSuite suite = new TestSuite();
        suite.addTest(new AMFConnectionIT("testConnect"));
        suite.addTest(new AMFConnectionIT("testConnectAndClose"));
        suite.addTest(new AMFConnectionIT("testConnectBadUrl"));
        suite.addTest(new AMFConnectionIT("testCallMultipleTimes"));
        suite.addTest(new AMFConnectionIT("testCallNoConnect"));
        suite.addTest(new AMFConnectionIT("testCallNoConnectStringMsg"));
        suite.addTest(new AMFConnectionIT("testCallUnreachableConnectUrl"));
        suite.addTest(new AMFConnectionIT("testCallNonexistantMethod"));
        suite.addTest(new AMFConnectionIT("testHttpResponseInfoWithNonexistantMethod"));
        suite.addTest(new AMFConnectionIT("testCloseNoConnect"));
        suite.addTest(new AMFConnectionIT("testSetGetObjectEncoding"));
        suite.addTest(new AMFConnectionIT("testSetGetDefaultObjectEncoding"));
        suite.addTest(new AMFConnectionIT("testSetGetAMFHeaderProcessor"));
        suite.addTest(new AMFConnectionIT("testAddRemoveAMFHeaderTwoParam"));
        suite.addTest(new AMFConnectionIT("testAddRemoveAMFHeader"));
        suite.addTest(new AMFConnectionIT("testAddRemoveAllAMFHeaders"));
        suite.addTest(new AMFConnectionIT("testAddRemoveHTTPRequestHeader"));
        suite.addTest(new AMFConnectionIT("testAddRemoveAllHTTPRequestHeaders"));
        suite.addTest(new AMFConnectionIT("testRemoveAMFHeader"));
        suite.addTest(new AMFConnectionIT("testRemoveAllAMFHeaders"));
        suite.addTest(new AMFConnectionIT("testRemoveHTTPRequestHeader"));
        suite.addTest(new AMFConnectionIT("testRemoveAllHTTPRequestHeaders"));
        suite.addTest(new AMFConnectionIT("testInstantiateTypes"));
        suite.addTest(new AMFConnectionIT("testSetGetAMFTrace"));
        suite.addTest(new AMFConnectionIT("testHTTPProxy"));

        return new TestSetup(suite) {
            protected void setUp() throws Exception {
                serverWrapper = new TestServerWrapper();
                serverPort = serverWrapper.startServer("classpath:/WEB-INF/flex/services-config.xml");
                if(serverPort == -1) {
                    Assert.fail("Couldn't start server process");
                }
                AMFConnection.registerAlias(
                        "remoting.amfclient.ServerCustomType" /* server type */,
                        "remoting.amfclient.ClientCustomType" /* client type */);
            }
            protected void tearDown() throws Exception {
                serverWrapper.stopServer();
                serverWrapper = null;
            }
        };
    }

    // Not a test, just an example to show how to use AMFConnection.
    public void example()
    {
        // Create the AMF connection.
        AMFConnection amfConnection = new AMFConnection();

        // Connect to the remote url.
        try
        {
            amfConnection.connect(getConnectionUrl());
        }
        catch (ClientStatusException cse)
        {
            return;
        }

        // Make a remoting call and retrieve the result.
        try
        {
            Object result = amfConnection.call(DEFAULT_AMF_OPERATION, DEFAULT_METHOD_ARG);
            Assert.assertEquals(DEFAULT_METHOD_ARG, result);
        }
        catch (ClientStatusException cse)
        {
            // Ignore.
        }
        catch (ServerStatusException sse)
        {
            // Ignore.
        }

        // Close the connection.
        amfConnection.close();
    }

    public void testConnect()
    {
        AMFConnection amfConnection = new AMFConnection();
        try
        {
            amfConnection.connect(getConnectionUrl());
            Assert.assertEquals(getConnectionUrl(), amfConnection.getUrl());
        }
        catch (ClientStatusException cse)
        {
            fail(UNEXPECTED_EXCEPTION_STRING + cse);
        }
        finally
        {
            amfConnection.close();
        }
    }

    public void testConnectAndClose()
    {
        AMFConnection amfConnection = new AMFConnection();
        try
        {
            amfConnection.connect(getConnectionUrl());
            Assert.assertEquals(getConnectionUrl(), amfConnection.getUrl());
        }
        catch (ClientStatusException cse)
        {
            fail(UNEXPECTED_EXCEPTION_STRING + cse);
        }
        finally
        {
            amfConnection.close();
            Assert.assertEquals(null, amfConnection.getUrl());
        }
    }

    public void testConnectBadUrl()
    {
        String badUrl = "badUrl";
        AMFConnection amfConnection = new AMFConnection();
        try
        {
            amfConnection.connect(badUrl);
            fail("ClientStatusException expected");
        }
        catch (ClientStatusException cse)
        {
            Assert.assertEquals(ClientStatusException.AMF_CONNECT_FAILED_CODE, cse.getCode());
        }
        finally
        {
            amfConnection.close();
        }
    }

    public void testCallMultipleTimes()
    {
        AMFConnection amfConnection = new AMFConnection();
        try
        {
            amfConnection.connect(getConnectionUrl());
        }
        catch (ClientStatusException cse)
        {
            fail(UNEXPECTED_EXCEPTION_STRING + cse);
        }
        // Make a remoting call and retrieve the result.
        try
        {
            for (int i = 1; i < 4; i++)
            {
                String stringToEcho = DEFAULT_METHOD_ARG + i;
                Object result = amfConnection.call(DEFAULT_AMF_OPERATION, stringToEcho);
                Assert.assertEquals(stringToEcho, result);
            }
        }
        catch (Exception e)
        {
            fail(UNEXPECTED_EXCEPTION_STRING + e);
        }
        finally
        {
            amfConnection.close();
        }
    }

    public void testCallNoConnect()
    {
        AMFConnection amfConnection = new AMFConnection();
        // Make a remoting call without connect.
        try
        {
            Object result = amfConnection.call(DEFAULT_AMF_OPERATION, DEFAULT_METHOD_ARG);
            Assert.assertEquals(DEFAULT_METHOD_ARG, result);
        }
        catch (ClientStatusException cse)
        {
            Assert.assertEquals(ClientStatusException.AMF_CALL_FAILED_CODE, cse.getCode());
        }
        catch (Exception e)
        {
            fail(UNEXPECTED_EXCEPTION_STRING + e);
        }
        finally
        {
            amfConnection.close();
        }
    }

    public void testCallNoConnectStringMsg()
    {
        AMFConnection amfConnection = new AMFConnection();
        // Make a remoting call without connect.
        try
        {
            Object result = amfConnection.call(DEFAULT_AMF_OPERATION, DEFAULT_METHOD_ARG);
            Assert.assertEquals(DEFAULT_METHOD_ARG, result);
        }
        catch (ClientStatusException cse)
        {
            Assert.assertEquals(ClientStatusException.AMF_CALL_FAILED_CODE, cse.getCode());
        }
        catch (Exception e)
        {
            fail(UNEXPECTED_EXCEPTION_STRING + e);
        }
        finally
        {
            amfConnection.close();
        }
    }

    public void testCallUnreachableConnectUrl()
    {
        String unreachableUrl = "http://localhost:8400/team/messagebroker/unreachable";
        AMFConnection amfConnection = new AMFConnection();
        try
        {
            // Connect does not actually connect but simply sets the url.
            amfConnection.connect(unreachableUrl);
        }
        catch (ClientStatusException cse)
        {
            fail(UNEXPECTED_EXCEPTION_STRING + cse);
        }
        // Make a remoting call and retrieve the result.
        try
        {
            Object result = amfConnection.call(DEFAULT_AMF_OPERATION, DEFAULT_METHOD_ARG);
            Assert.assertEquals(DEFAULT_METHOD_ARG, result);
        }
        catch (ClientStatusException cse)
        {
            Assert.assertEquals(ClientStatusException.AMF_CALL_FAILED_CODE, cse.getCode());
        }
        catch (Exception e)
        {
            fail(UNEXPECTED_EXCEPTION_STRING + e);
        }
        finally
        {
            amfConnection.close();
        }
    }

    public void testCallNonexistantMethod()
    {
        String method = "nonExistantMethod";
        try
        {
            internalTestCall(getOperationCall(method), "Wombat", new CallResultHandler(){
                public void onResult(Object result)
                {
                    fail("Unexcepted result: " + result);
                }
            });
        }
        catch (ServerStatusException sse)
        {
            ASObject status = (ASObject)sse.getData();
            String code = (String)status.get("code");
            Assert.assertEquals(MessageException.CODE_SERVER_RESOURCE_UNAVAILABLE, code);
            HttpResponseInfo info = sse.getHttpResponseInfo();
            // AMF status messages are reported as HTTP_OK still.
            Assert.assertEquals(HttpURLConnection.HTTP_OK, info.getResponseCode());
            Assert.assertEquals("OK", info.getResponseMessage());
        }
        catch (Exception e)
        {
            fail(UNEXPECTED_EXCEPTION_STRING + e);
        }
    }

    public void testHttpResponseInfoWithNonexistantMethod()
    {
        String method = "nonExistantMethod";
        final ClientCustomType methodArg = new ClientCustomType();
        methodArg.setId(1);
        try
        {
            internalTestCall(getOperationCall(method), methodArg, new CallResultHandler(){
                public void onResult(Object result)
                {
                    fail("Unexcepted result: " + result);
                }
            });
        }
        catch (ServerStatusException sse)
        {
            HttpResponseInfo info = sse.getHttpResponseInfo();
            // AMF status messages are reported as HTTP_OK still.
            Assert.assertEquals(HttpURLConnection.HTTP_OK, info.getResponseCode());
            Assert.assertEquals("OK", info.getResponseMessage());
        }
        catch (Exception e)
        {
            fail(UNEXPECTED_EXCEPTION_STRING + e);
        }
    }

    public void testCloseNoConnect()
    {
        AMFConnection amfConnection = new AMFConnection();
        // Closing with no connection or call.
        try
        {
            amfConnection.close();
            Assert.assertEquals(null, amfConnection.getUrl());
        }
        catch (Exception e)
        {
            fail(UNEXPECTED_EXCEPTION_STRING + e);
        }
    }

    public void testSetGetObjectEncoding()
    {
        int retAMF;
        AMFConnection amfConnection = new AMFConnection();
        try
        {
            amfConnection.connect(getConnectionUrl());
            Assert.assertEquals(getConnectionUrl(), amfConnection.getUrl());
            amfConnection.setObjectEncoding(MessageIOConstants.AMF0);
            retAMF = amfConnection.getObjectEncoding();
            Assert.assertEquals(MessageIOConstants.AMF0, retAMF);
        }
        catch (ClientStatusException cse)
        {
            fail(UNEXPECTED_EXCEPTION_STRING + cse);
        }
        finally
        {
            amfConnection.close();
        }
    }

    public void testSetGetDefaultObjectEncoding()
    {
        int retAMF;
        AMFConnection amfConnection = new AMFConnection();
        try
        {
            amfConnection.connect(getConnectionUrl());
            Assert.assertEquals(getConnectionUrl(), amfConnection.getUrl());
            AMFConnection.setDefaultObjectEncoding(MessageIOConstants.AMF3);
            retAMF = AMFConnection.getDefaultObjectEncoding();
            Assert.assertEquals(MessageIOConstants.AMF3, retAMF);
        }
        catch (ClientStatusException cse)
        {
            fail(UNEXPECTED_EXCEPTION_STRING + cse);
        }
        finally
        {
            amfConnection.close();
        }
    }

    /**
     * There doesn't seem to be a single implementation of AMFHeaderProcessor therefore this test
     * is pretty useless.
     */
    public void testSetGetAMFHeaderProcessor()
    {
        AMFHeaderProcessor setAMF = null;
        AMFHeaderProcessor retAMF;
        AMFConnection amfConnection = new AMFConnection();
        try
        {
            amfConnection.connect(getConnectionUrl());
            Assert.assertEquals(getConnectionUrl(), amfConnection.getUrl());
            amfConnection.setAMFHeaderProcessor(setAMF);
            retAMF = amfConnection.getAMFHeaderProcessor();
            Assert.assertEquals(setAMF, retAMF);
        }
        catch (ClientStatusException cse)
        {
            fail(UNEXPECTED_EXCEPTION_STRING + cse);
        }
        finally
        {
            amfConnection.close();
        }
    }

    public void testAddRemoveAMFHeaderTwoParam()
    {
        boolean retAMF;
        Object val = 1;
        AMFConnection amfConnection = new AMFConnection();
        try
        {
            amfConnection.connect(getConnectionUrl());
            Assert.assertEquals(getConnectionUrl(), amfConnection.getUrl());
            amfConnection.addAmfHeader(FOO_STRING,val);
            retAMF = amfConnection.removeAmfHeader(FOO_STRING);
            Assert.assertTrue(retAMF);
        }
        catch (ClientStatusException cse)
        {
            fail(UNEXPECTED_EXCEPTION_STRING + cse);
        }
        finally
        {
            amfConnection.close();
        }
    }

    public void testAddRemoveAMFHeader()
    {
        boolean retAMF;
        Object val = 1;
        AMFConnection amfConnection = new AMFConnection();
        try
        {
            amfConnection.connect(getConnectionUrl());
            Assert.assertEquals(getConnectionUrl(), amfConnection.getUrl());
            amfConnection.addAmfHeader(FOO_STRING,true,val);
            retAMF = amfConnection.removeAmfHeader(FOO_STRING);
            Assert.assertTrue(retAMF);
        }
        catch (ClientStatusException cse)
        {
            fail(UNEXPECTED_EXCEPTION_STRING + cse);
        }
        finally
        {
            amfConnection.close();
        }
    }

    public void testAddRemoveAllAMFHeaders()
    {
        Object val1 = 1;
        Object val2 = 2;
        AMFConnection amfConnection = new AMFConnection();
        try
        {
            amfConnection.connect(getConnectionUrl());
            Assert.assertEquals(getConnectionUrl(), amfConnection.getUrl());
            amfConnection.addAmfHeader(FOO_STRING,true,val1);
            amfConnection.addAmfHeader(BAR_STRING,true,val2);
            amfConnection.removeAllAmfHeaders();
            Assert.assertTrue(true);
        }
        catch (ClientStatusException cse)
        {
            fail(UNEXPECTED_EXCEPTION_STRING + cse);
        }
        finally
        {
            amfConnection.close();
        }
    }

    public void testAddRemoveHTTPRequestHeader()
    {
        boolean retHttp;
        AMFConnection amfConnection = new AMFConnection();
        try
        {
            amfConnection.connect(getConnectionUrl());
            Assert.assertEquals(getConnectionUrl(), amfConnection.getUrl());
            amfConnection.addHttpRequestHeader(FOO_STRING,BAR_STRING);
            retHttp = amfConnection.removeHttpRequestHeader(FOO_STRING);
            Assert.assertTrue(retHttp);
        }
        catch (ClientStatusException cse)
        {
            fail(UNEXPECTED_EXCEPTION_STRING + cse);
        }
        finally
        {
            amfConnection.close();
        }
    }

    public void testAddRemoveAllHTTPRequestHeaders()
    {
        AMFConnection amfConnection = new AMFConnection();
        try
        {
            amfConnection.connect(getConnectionUrl());
            Assert.assertEquals(getConnectionUrl(), amfConnection.getUrl());
            amfConnection.addHttpRequestHeader(FOO_STRING,BAR_STRING);
            amfConnection.addHttpRequestHeader(BAR_STRING,FOO_STRING);
            amfConnection.removeAllHttpRequestHeaders();
            Assert.assertTrue(true);
        }
        catch (ClientStatusException cse)
        {
            fail(UNEXPECTED_EXCEPTION_STRING + cse);
        }
        finally
        {
            amfConnection.close();
        }
    }

    public void testRemoveAMFHeader()
    {
        boolean retAMF;
        AMFConnection amfConnection = new AMFConnection();
        try
        {
            amfConnection.connect(getConnectionUrl());
            Assert.assertEquals(getConnectionUrl(), amfConnection.getUrl());
            retAMF = amfConnection.removeAmfHeader(FOO_STRING);
            Assert.assertFalse(retAMF);
        }
        catch (ClientStatusException cse)
        {
            fail(UNEXPECTED_EXCEPTION_STRING + cse);
        }
        finally
        {
            amfConnection.close();
        }
    }

    public void testRemoveAllAMFHeaders()
    {
        AMFConnection amfConnection = new AMFConnection();
        try
        {
            amfConnection.connect(getConnectionUrl());
            Assert.assertEquals(getConnectionUrl(), amfConnection.getUrl());
            amfConnection.removeAllAmfHeaders();
            Assert.assertTrue(true);
        }
        catch (ClientStatusException cse)
        {
            fail(UNEXPECTED_EXCEPTION_STRING + cse);
        }
        finally
        {
            amfConnection.close();
        }
    }

    public void testRemoveHTTPRequestHeader()
    {
        boolean retHttp;
        AMFConnection amfConnection = new AMFConnection();
        try
        {
            amfConnection.connect(getConnectionUrl());
            Assert.assertEquals(getConnectionUrl(), amfConnection.getUrl());
            retHttp = amfConnection.removeHttpRequestHeader(FOO_STRING);
            Assert.assertFalse(retHttp);
        }
        catch (ClientStatusException cse)
        {
            fail(UNEXPECTED_EXCEPTION_STRING + cse);
        }
        finally
        {
            amfConnection.close();
        }
    }

    public void testRemoveAllHTTPRequestHeaders()
    {
        AMFConnection amfConnection = new AMFConnection();
        try
        {
            amfConnection.connect(getConnectionUrl());
            Assert.assertEquals(getConnectionUrl(), amfConnection.getUrl());
            amfConnection.removeAllHttpRequestHeaders();
            Assert.assertTrue(true);
        }
        catch (ClientStatusException cse)
        {
            fail(UNEXPECTED_EXCEPTION_STRING + cse);
        }
        finally
        {
            amfConnection.close();
        }
    }


    public void testInstantiateTypes()
    {
        String method = "getObject2";
        try
        {
            AMFConnection amfConnection = new AMFConnection();
            amfConnection.connect(getConnectionUrl());

            // First, make sure we get the strong type.
            Object result = amfConnection.call(getOperationCall(method));
            Assert.assertTrue(result instanceof ClientCustomType);

            // Now, call again with instantiateTypes=false and expect an Object.
            amfConnection.setInstantiateTypes(false);
            result = amfConnection.call(getOperationCall(method));
            Assert.assertTrue(!(result instanceof ClientCustomType));
            amfConnection.close();
        }
        catch (Exception e)
        {
            fail(UNEXPECTED_EXCEPTION_STRING + e);
        }
    }

    public void testSetGetAMFTrace()
    {
        AMFConnection amfConnection = new AMFConnection();
        try
        {
            AmfTrace trace = new AmfTrace();
            amfConnection.connect(getConnectionUrl());
            amfConnection.setAmfTrace(trace);

            String stringToEcho = DEFAULT_METHOD_ARG + 1;
            Object result = amfConnection.call(DEFAULT_AMF_OPERATION, stringToEcho);
            Assert.assertEquals(stringToEcho, result);

            if (trace.toString().length() > 0) Assert.assertTrue(true);
            else fail("AmfTrace did not get anything: " + trace.toString() + " " + trace.toString().length());

            amfConnection.close();

        }
        catch (ClientStatusException cse)
        {
            fail(UNEXPECTED_EXCEPTION_STRING + cse);
        }
        catch (Exception e)
        {
            fail(UNEXPECTED_EXCEPTION_STRING + e);
        }
        finally
        {
            amfConnection.close();
        }
    }

    public void testHTTPProxy()
    {
        AMFConnection amfconn = new AMFConnection();
        try
        {
            amfconn.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8888)));
            amfconn.connect("http://localhost:8400/team/messagebroker/amf");
            RemotingMessage call = new RemotingMessage();
            call.setHeader("DSId", "" + System.identityHashCode(amfconn));
            call.setClientId("ro");
            call.setDestination("remoting_AMF");
            call.setMessageId("12345");
            call.setOperation("echo");
            call.setBody("hello");
            amfconn.call("foo", call);
            fail("ClientStatusException expected");
        }
        catch (ClientStatusException cse)
        {
            Assert.assertEquals(ClientStatusException.AMF_CALL_FAILED_CODE, cse.getCode());
        }
        catch (Exception e)
        {
            fail(UNEXPECTED_EXCEPTION_STRING + e);
        }
        finally
        {
            amfconn.close();
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
