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

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class LogTest extends TestCase
{
    public LogTest(String name)
    {
        super(name);
    }
    
    private TestingTarget testTarget;
    
    public static Test suite()
    {
        return new TestSuite(LogTest.class);
    }
    
    protected void setUp() throws Exception
    {
        Log.createLog();
        // Init a ConsoleLogTarget for testing purposes.        
        testTarget = new TestingTarget(); // Defaults to a log level of none with no filters.
        super.setUp();
    }

    protected void tearDown() throws Exception
    {
        Log.flush();
        super.tearDown();
    }
    
    public void testInitialLogState()
    {
        Assert.assertEquals(Log.getTargetLevel(), LogEvent.NONE);
        Assert.assertTrue(Log.getTargets().size() == 0); 
    }
    
    public void testLogStateAfterAddingTarget()
    {        
        Assert.assertEquals(testTarget.getLevel(), LogEvent.ERROR);
        testTarget.setLevel(LogEvent.FATAL);
        Assert.assertEquals(testTarget.getLevel(), LogEvent.FATAL);        
        Assert.assertTrue(testTarget.getFilters().size() == 1);
        Log.addTarget(testTarget);
        Assert.assertEquals(Log.getTargetLevel(), LogEvent.FATAL);
        Assert.assertTrue(Log.getTargets().size() == 1);
    }
    
    public void testLogStateAfterAddRemoveTarget()
    {
        testTarget.setLevel(LogEvent.FATAL);
        Log.addTarget(testTarget);
        Log.removeTarget(testTarget);
        Assert.assertEquals(Log.getTargetLevel(), LogEvent.NONE);
        Assert.assertTrue(Log.getTargets().size() == 0);
    }
    
    public void testAddTargetGetLogger()
    {
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
    
    public void testAddTargetGetLoggerThenRemoveFilter()
    {
        testTarget.setLevel(LogEvent.ALL);
        testTarget.addFilter("foo.*");
        Assert.assertTrue(testTarget.getFilters().size() == 1);
        Log.addTarget(testTarget);
        Log.getLogger("foo.bar");
        Assert.assertTrue(testTarget.loggerCount == 1);
        testTarget.removeFilter("foo.*");
        Assert.assertTrue(testTarget.loggerCount == 0);
    }
    
    public void testGetLoggerAddTarget()
    {
        // First, remove the default "*" filter.
        testTarget.removeFilter("*");
        
        Log.getLogger("foo");
        Log.getLogger("bar");
        Log.getLogger("baz"); // Shouldn't be added to the target later.
        
        testTarget.setLevel(LogEvent.ALL);
        Log.addTarget(testTarget);        
        Assert.assertTrue(testTarget.loggerCount == 0);
        
        // Now add filters.
        List filters = new ArrayList();
        filters.add("foo");
        filters.add("bar");        
        testTarget.setFilters(filters);        
        Assert.assertTrue(testTarget.loggerCount == 2);
    }
        
    public void testLogAddFilterNull()
    {
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
        
    public void testLogSetFilterNull()
    {
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
