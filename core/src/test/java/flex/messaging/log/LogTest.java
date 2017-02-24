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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class LogTest {

    private TestingTarget testTarget;

    @Before
    public void setUp() throws Exception {
        // Make sure the logger is reset before starting the tests.
        Log.clear();
        Log.createLog();
        // Init a ConsoleLogTarget for testing purposes.
        testTarget = new TestingTarget(); // Defaults to a log level of none with no filters.
    }

    @After
    public void tearDown() throws Exception {
        Log.flush();
    }

    @Test
    public void testInitialLogState() {
        Assert.assertEquals(Log.getTargetLevel(), LogEvent.NONE);
        Assert.assertTrue(Log.getTargets().size() == 0);
    }

    @Test
    public void testLogStateAfterAddingTarget() {
        Assert.assertEquals(testTarget.getLevel(), LogEvent.ERROR);
        testTarget.setLevel(LogEvent.FATAL);
        Assert.assertEquals(testTarget.getLevel(), LogEvent.FATAL);
        Assert.assertTrue(testTarget.getFilters().size() == 1);
        Log.addTarget(testTarget);
        Assert.assertEquals(Log.getTargetLevel(), LogEvent.FATAL);
        Assert.assertTrue(Log.getTargets().size() == 1);
    }

    @Test
    public void testLogStateAfterAddRemoveTarget() {
        testTarget.setLevel(LogEvent.FATAL);
        Log.addTarget(testTarget);
        Log.removeTarget(testTarget);
        Assert.assertEquals(Log.getTargetLevel(), LogEvent.NONE);
        Assert.assertTrue(Log.getTargets().size() == 0);
    }

    @Test
    public void testAddTargetGetLogger() {
        testTarget.setLevel(LogEvent.ALL);
        testTarget.addFilter("*");
        Assert.assertTrue(testTarget.getFilters().size() == 1);
        Log.addTarget(testTarget);
        Log.getLogger("foo");
        Assert.assertTrue(testTarget.loggerCount == 1);
        Log.getLogger("bar");
        Assert.assertTrue(testTarget.loggerCount == 2);
        Log.removeTarget(testTarget);
        Assert.assertTrue(testTarget.loggerCount == 0);
        testTarget.removeFilter("*");
        Assert.assertTrue(testTarget.getFilters().size() == 0);
    }

    @Test
    public void testAddTargetGetLoggerThenRemoveFilter() {
        testTarget.setLevel(LogEvent.ALL);
        testTarget.addFilter("foo.*");
        Assert.assertTrue(testTarget.getFilters().size() == 1);
        Log.addTarget(testTarget);
        Log.getLogger("foo.bar");
        Assert.assertTrue(testTarget.loggerCount == 1);
        testTarget.removeFilter("foo.*");
        Assert.assertTrue(testTarget.loggerCount == 0);
    }

    @Test
    public void testGetLoggerAddTarget() {
        // First, remove the default "*" filter.
        testTarget.removeFilter("*");

        Log.getLogger("foo");
        Log.getLogger("bar");
        Log.getLogger("baz"); // Shouldn't be added to the target later.

        testTarget.setLevel(LogEvent.ALL);
        Log.addTarget(testTarget);
        Assert.assertTrue(testTarget.loggerCount == 0);

        // Now add filters.
        List<String> filters = new ArrayList<String>();
        filters.add("foo");
        filters.add("bar");
        testTarget.setFilters(filters);
        Assert.assertTrue(testTarget.loggerCount == 2);
    }

    @Test
    public void testLogAddFilterNull() {
        // First, remove the default "*" filter.
        testTarget.removeFilter("*");

        Log.getLogger("foo");

        testTarget.setLevel(LogEvent.ALL);
        Log.addTarget(testTarget);
        Assert.assertTrue(testTarget.loggerCount == 0);

        // Now null filters.
        List filters = new ArrayList();
        testTarget.addFilter(null);
        testTarget.setFilters(filters);
        Assert.assertTrue(testTarget.loggerCount == 1);
    }

    @Test
    public void testLogSetFilterNull() {
        // First, remove the default "*" filter.
        testTarget.removeFilter("*");

        Log.getLogger("foo");

        testTarget.setLevel(LogEvent.ALL);
        Log.addTarget(testTarget);
        Assert.assertTrue(testTarget.loggerCount == 0);

        // Now null filters.
        List filters = new ArrayList();
        testTarget.setFilters(filters);
        Assert.assertTrue(testTarget.loggerCount == 1);
    }
}
