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

package flex.messaging.log;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class LoggerTest {

    private TestingTarget target;

    @Before
    public void setUp() throws Exception {
        Log.createLog();
        target = new TestingTarget();
        target.setLevel(LogEvent.ALL);
        target.addFilter("*");
        Log.addTarget(target);
    }

    @Test
    public void testLogDebug() {
        Logger lg1 = Log.getLogger("mx.rpc.SOAP");
        lg1.debug("testing");
        Assert.assertEquals(LogEvent.DEBUG, target.lastEvent.level);
        Assert.assertEquals("testing", target.lastEvent.message);
    }

    @Test
    public void testLogInfo() {
        Logger lg1 = Log.getLogger("mx.rpc.SOAP");
        lg1.info("testing");
        Assert.assertEquals(LogEvent.INFO, target.lastEvent.level);
        Assert.assertEquals("testing", target.lastEvent.message);
    }

    @Test
    public void testLogWarning() {
        Logger lg1 = Log.getLogger("mx.rpc.SOAP");
        lg1.warn("testing");
        Assert.assertEquals(LogEvent.WARN, target.lastEvent.level);
        Assert.assertEquals("testing", target.lastEvent.message);
    }

    @Test
    public void testLogError() {
        Logger lg1 = Log.getLogger("mx.rpc.SOAP");
        lg1.error("testing");
        Assert.assertEquals(LogEvent.ERROR, target.lastEvent.level);
        Assert.assertEquals("testing", target.lastEvent.message);
    }

    @Test
    public void testLogFatal() {
        Logger lg1 = Log.getLogger("mx.rpc.SOAP");
        lg1.fatal("testing");
        Assert.assertEquals(LogEvent.FATAL, target.lastEvent.level);
        Assert.assertEquals("testing", target.lastEvent.message);
    }

    @Test
    public void testTargetLevelDebug() {
        Logger lg1 = Log.getLogger("mx.rpc.SOAP");
        target.level = LogEvent.DEBUG;
        lg1.error("testing");
        Assert.assertNotNull(target.lastEvent);
        Assert.assertEquals(LogEvent.ERROR, target.lastEvent.level);
        Assert.assertEquals("testing", target.lastEvent.message);
    }

    @Test
    public void testTargetLevelInfo() {
        Logger lg1 = Log.getLogger("mx.rpc.SOAP");
        target.level = LogEvent.INFO;
        lg1.debug("testing");
        Assert.assertNull(target.lastEvent);
    }

    @Test
    public void testTargetLevelFatal() {
        Logger lg1 = Log.getLogger("mx.rpc.SOAP");
        target.level = LogEvent.FATAL;
        lg1.info("testing");
        Assert.assertNull(target.lastEvent);
        lg1.fatal("fatal");
        Assert.assertNotNull(target.lastEvent);
        Assert.assertEquals(LogEvent.FATAL, target.lastEvent.level);
        Assert.assertEquals("fatal", target.lastEvent.message);
    }

    @Test
    public void testTargetLevelWarn() {
        Logger lg1 = Log.getLogger("mx.rpc.SOAP");
        target.level = LogEvent.WARN;
        lg1.error("testing");
        Assert.assertNotNull(target.lastEvent);
        Assert.assertEquals(LogEvent.ERROR, target.lastEvent.level);
        Assert.assertEquals("testing", target.lastEvent.message);
        lg1.warn("warn");
        Assert.assertNotNull(target.lastEvent);
        Assert.assertEquals(LogEvent.WARN, target.lastEvent.level);
        Assert.assertEquals("warn", target.lastEvent.message);
    }

    @Test
    public void testTargetLevelError() {
        Logger lg1 = Log.getLogger("mx.rpc.SOAP");
        target.level = LogEvent.ERROR;
        lg1.info("testing");
        Assert.assertNull(target.lastEvent);
        lg1.error("error");
        Assert.assertNotNull(target.lastEvent);
        Assert.assertEquals(LogEvent.ERROR, target.lastEvent.level);
        Assert.assertEquals("error", target.lastEvent.message);
    }

    @Test
    public void testGetTargetId() {
        String tid = target.getId();
        Assert.assertNotNull(tid);
        TestingTarget target2 = new TestingTarget();
        target2.setLevel(LogEvent.ALL);
        target2.addFilter("*");
        Log.addTarget(target2);
        String tid2 = target2.getId();
        Assert.assertNotNull(tid2);
        Assert.assertNotSame(tid, tid2);
    }

}
