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

package flex.messaging.io.amf;

import flex.messaging.io.MessageDeserializer;
import flex.messaging.io.SerializationContext;
import macromedia.qa.metrics.MetricsManager;
import macromedia.qa.metrics.Value;
import macromedia.util.UnitTrace;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.URI;
import java.net.URL;

public class AmfDeserializerTest {
    private MetricsManager metricsManager;

    @Before
    public void setUp() {
        try {
            String project = System.getProperty("project.name");
            String build = System.getProperty("build.number");
            String props = System.getProperty("metrics.properties");

            if (project != null && build != null
                    && !build.trim().toUpperCase().equals("N/A") && props != null) {

                File file = new File(props);

                if (file.exists()) {
                    metricsManager = new MetricsManager(project, build, file);
                    metricsManager.newRun();
                    metricsManager.newMetric("AMF Deserialization Time", "ms");
                }
            }
        } catch (Throwable t) {
            if (UnitTrace.errors) {
                t.printStackTrace();
            }
        }
    }

    @Test
    public void testDeserializeMessage() {
        try {
            String sample = System.getProperty("AMF_SAMPLE_FILE");
            if (sample == null || sample.length() < 1) {
                sample = "amf_request.xml";
            }

            URL resource = ClassLoader.getSystemResource(sample);
            URI uri = new URI(resource.toString());
            File testData = new File(uri.getPath());
            String testDataLocation = testData.getCanonicalPath();

            PipedOutputStream pout = new PipedOutputStream();
            DataOutputStream dout = new DataOutputStream(pout);
            PipedInputStream pin = new PipedInputStream(pout);
            DataInputStream din = new DataInputStream(pin);

            AmfTrace trace = new AmfTrace();
            trace.startResponse("Serializing AMF/HTTP response");

            MessageGenerator gen = new MessageGenerator();
            gen.setDebugTrace(trace);
            gen.setOutputStream(dout);
            gen.parse(testDataLocation);
            trace.endMessage();
            trace.newLine();

            ActionContext context = new ActionContext();
            ActionMessage message = new ActionMessage();
            context.setRequestMessage(message);
            trace.startRequest("Deserializing AMF/HTTP request");
            SerializationContext dsContext = SerializationContext.getSerializationContext();
            MessageDeserializer deserializer = new AmfMessageDeserializer();
            deserializer.initialize(dsContext, din, trace);

            long start = System.currentTimeMillis();

            deserializer.readMessage(message, context);

            long finish = System.currentTimeMillis();
            trace.endMessage();

            try {
                if (metricsManager != null) {
                    long duration = finish - start;
                    Value v2 = metricsManager.createValue(duration);
                    metricsManager.saveValue(v2);
                    trace.newLine();

                    if (UnitTrace.debug) {
                        System.out.print("AMF Deserialization Time: " + duration + "ms");
                    }
                }
            } catch (Throwable t) {
                if (UnitTrace.errors) {
                    t.printStackTrace();
                }
            }

            if (UnitTrace.debug) {
                System.out.print(trace.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}
