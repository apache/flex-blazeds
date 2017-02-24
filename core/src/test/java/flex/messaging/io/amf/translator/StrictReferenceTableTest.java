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

package flex.messaging.io.amf.translator;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Simple test to check that the StrictReferenceTable
 * auto increments correctly, does not try to access items
 * out of bounds, and correct auto generates index-based
 * and incremented Integer values for added keys.
 */
public class StrictReferenceTableTest {
    private Map<Object, Object> table;

    private static Object ONE = new Object();
    private static Object TWO = new Object();
    private static Object THREE = new Object();
    private static Object FOUR = new Object();
    private static Object FIVE = new Object();
    private static Object[] objects = new Object[]{ONE, TWO, THREE, FOUR, FIVE};

    private static final int length = 2;

    @Before
    public void setUp() throws Exception {
        table = new IdentityHashMap<Object, Object>(length);
    }

    @After
    public void tearDown() throws Exception {
        table = null;
    }

    @Test
    public void testCapacity() {
        table.clear();
        int goal = length * 2;

        for (int i = 0; i < goal; i++) {
            table.put(objects[i], objects[i]);
        }

        Object one = table.get(ONE);
        if (one != ONE) {
            Assert.fail();
        }

        Object two = table.get(TWO);
        if (two != TWO) {
            Assert.fail();
        }

        Object three = table.get(THREE);
        if (three != THREE) {
            Assert.fail();
        }

        Object four = table.get(FOUR);
        if (four != FOUR) {
            Assert.fail();
        }

        Object five = table.get(FIVE);
        if (five != null) {
            Assert.fail();
        }

        if (table.size() != 4) {
            Assert.fail();
        }
    }

    @Test
    public void testIndex() {
        table.clear();
        int goal = length * 2;

        for (int i = 0; i < goal; i++) {
            table.put(objects[i], table.size());
        }

        Integer first = (Integer) table.get(ONE);
        if (first != 0) {
            Assert.fail();
        }

        Integer second = (Integer) table.get(TWO);
        if (second != 1) {
            Assert.fail();
        }

        Integer third = (Integer) table.get(THREE);
        if (third != 2) {
            Assert.fail();
        }

        Integer fourth = (Integer) table.get(FOUR);
        if (fourth != 3) {
            Assert.fail();
        }

        Object fifth = table.get(FIVE);
        if (fifth != null) {
            Assert.fail();
        }
    }
}
