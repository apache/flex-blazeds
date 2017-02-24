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

package flex.messaging.io.amf.validators;

import flex.messaging.config.ConfigMap;
import flex.messaging.io.MessageDeserializer;
import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.ActionContext;
import flex.messaging.io.amf.ActionMessage;
import flex.messaging.io.amf.AmfMessageDeserializer;
import flex.messaging.io.amf.AmfTrace;
import flex.messaging.io.amf.MessageGenerator;
import flex.messaging.validators.DeserializationValidator;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import macromedia.util.UnitTrace;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;
import java.net.URL;

public class AmfDeserializationValidatorTest extends TestCase
{
    public static Test suite()
    {
        return new TestSuite(AmfDeserializationValidatorTest.class);
    }

    public void testDeserializationValidator()
    {
        deserializateRequest(null);

        long duration1 = 0, duration2 = 0;
        DeserializationValidator validator = new TestDeserializationValidator();
        int n = 100; // Number of times to run the tests, use an even number.

        for (int i = 0; i < n/2; i++)
        {
            long start1 = System.currentTimeMillis();
            deserializateRequest(null);
            duration1 += (System.currentTimeMillis() - start1);

            long start2 = System.currentTimeMillis();
            deserializateRequest(validator);
            duration2 += (System.currentTimeMillis() - start2);
        }

        for (int i = 0; i < n/2; i++)
        {
            long start2 = System.currentTimeMillis();
            deserializateRequest(validator);
            duration2 += (System.currentTimeMillis() - start2);

            long start1 = System.currentTimeMillis();
            deserializateRequest(null);
            duration1 += (System.currentTimeMillis() - start1);
        }

        if (UnitTrace.debug) // Print trace output.
        {
            System.out.println("AMF Deserialization Time w/o validator: " + duration1 + "ms");
            System.out.println("AMF Deserialization Time w validator: " + duration2 + "ms");
        }
    }

    private void deserializateRequest(DeserializationValidator validator)
    {
        try
        {
            // Find sample AMF data, or read the default file.
            String sample = System.getProperty("AMF_SAMPLE_FILE");
            if (sample == null || sample.length() < 1)
                sample = "amf_request.xml";

            URL resource = ClassLoader.getSystemResource(sample);
            URI uri = new URI(resource.toString());
            File testData = new File(uri.getPath());
            String testDataLocation = testData.getCanonicalPath();

            // Generate sample AMF request from the data file.
            PipedOutputStream pout = new PipedOutputStream();
            DataOutputStream dout = new DataOutputStream(pout);

            DataInputStream din = new DataInputStream(new PipedInputStream(pout));

            AmfTrace trace = new AmfTrace();
            trace.startResponse("Serializing AMF/HTTP response");

            MessageGenerator gen = new MessageGenerator();
            gen.setDebugTrace(trace);
            gen.setOutputStream(dout);
            gen.parse(testDataLocation);
            trace.endMessage();
            trace.newLine();

            // Create a deserializer for sample AMF request.
            ActionContext context = new ActionContext();
            ActionMessage message = new ActionMessage();
            context.setRequestMessage(message);

            SerializationContext dsContext = SerializationContext.getSerializationContext();
            if (validator != null)
                dsContext.setDeserializationValidator(validator);
            MessageDeserializer deserializer = new AmfMessageDeserializer();
            deserializer.initialize(dsContext, din, trace);
            deserializer.readMessage(message, context);
            trace.endMessage();
            trace.newLine();

            //if (UnitTrace.debug) // Print trace output.
            //    System.out.print(trace.toString());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail();
        }
    }

    // A simple deserialization validator that simply returns true for all creation
    // and assignments validations.
    class TestDeserializationValidator implements DeserializationValidator
    {
        public boolean validateAssignment(Object instance, int index, Object value)
        {
            //System.out.println("validateAssign: [" + (instance == null? "null" : instance.getClass().getName()) + "," + index + "," + value + "]");
            return true;
        }

        public boolean validateAssignment(Object instance, String propertyName, Object value)
        {
            //System.out.println("validateAssign: [" + (instance == null? "null" : instance.getClass().getName()) + "," + propertyName + "," + value + "]");
            return true;
        }

        public boolean validateCreation(Class<?> c)
        {
            //System.out.println("validateCreate: " + (c == null? "null" : c.getName()));
            return true;
        }

        public void initialize(String id, ConfigMap configMap)
        {
            // No-op
        }
    }
}
