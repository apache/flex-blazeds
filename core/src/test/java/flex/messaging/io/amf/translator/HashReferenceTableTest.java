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

import flex.messaging.io.amf.TraitsInfo;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple test to check that the HashReferenceTableTest
 * auto increments correctly, does not try to access items
 * out of bounds, and correct auto generates index-based
 * and incremented Integer values for added keys.
 * <p>
 * This test also checks that the condition for equality
 * is enforced, despite look ups being optimized with
 * a hashcode. The most important class to test is
 * TraitsInfo as this type is stored in a HashReferenceTable
 * for our AMF3 serialization code.
 *
 * @see flex.messaging.io.amf.TraitsInfo;
 */
public class HashReferenceTableTest {
    private Map<Object, Object> table;

    private static Object ONE = 1;
    private static Object ONEPRIME = 1;
    private static Object TWO = 2.567;
    private static Object TWOPRIME = 2.567;
    private static Object THREE = "Hello";
    private static Object THREEPRIME = "Hello";
    private static TraitsInfo FOUR = new TraitsInfo("flex.messaging.messages.RemotingMessage", true, false, 2);

    static {
        String prop1 = "prop1";
        FOUR.addProperty(prop1);
        String prop2 = "prop2";
        FOUR.addProperty(prop2);
    }

    private static TraitsInfo FOURPRIME = new TraitsInfo("flex.messaging.messages.RemotingMessage", true, false, 2);

    static {
        String prop1 = "prop1";
        FOURPRIME.addProperty(prop1);
        String prop2 = "prop2";
        FOURPRIME.addProperty(prop2);
    }

    private static Object FIVE = "Apples";
    private static Object FIVEPRIME = "Oranges";
    private static TraitsInfo SIX = new TraitsInfo("flex.messaging.messages.RemotingMessage", true, false, 2);

    static {
        String prop1 = "propA";
        SIX.addProperty(prop1);
        String prop2 = "propB";
        SIX.addProperty(prop2);
    }

    private static TraitsInfo SIXPRIME = new TraitsInfo("flex.messaging.messages.RemotingMessage", true, false, 2);

    static {
        String prop1 = "propA";
        SIXPRIME.addProperty(prop1);
        String prop2 = "propZ";
        SIXPRIME.addProperty(prop2);
    }


    private static Object[] keys = new Object[]{ONE, TWO, THREE, FOUR, FIVE, SIX};
    private static Object[] values = new Object[]{ONEPRIME, TWOPRIME, THREEPRIME, FOURPRIME, FIVEPRIME, SIXPRIME};

    private static final int length = 2;

    @Before
    public void setUp() throws Exception {
        table = new HashMap<Object, Object>(length);
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
            table.put(keys[i], values[i]);
        }

        // Add slightly different TraitInfo test too
        table.put(SIX, SIXPRIME);

        Object one = table.get(ONE);
        if (one != ONEPRIME) {
            Assert.fail();
        }

        one = table.get(ONEPRIME);
        if (one != ONEPRIME) {
            Assert.fail();
        }

        Object two = table.get(TWO);
        if (two != TWOPRIME) {
            Assert.fail();
        }

        two = table.get(TWOPRIME);
        if (two != TWOPRIME) {
            Assert.fail();
        }

        Object three = table.get(THREE);
        if (three != THREEPRIME) {
            Assert.fail();
        }

        three = table.get(THREEPRIME);
        if (three != THREEPRIME) {
            Assert.fail();
        }

        Object four = table.get(FOUR);
        if (four != FOURPRIME) {
            Assert.fail();
        }

        four = table.get(FOURPRIME);
        if (four != FOURPRIME) {
            Assert.fail();
        }

        Object five = table.get(FIVE);
        if (five == FIVEPRIME) {
            Assert.fail();
        }

        five = table.get(FIVEPRIME);
        if (five == FIVEPRIME) {
            Assert.fail();
        }

        Object six = table.get(SIX);
        if (six != SIXPRIME) {
            Assert.fail();
        }

        six = table.get(SIXPRIME);
        if (six != null) {
            Assert.fail();
        }


        if (table.size() != 5) {
            Assert.fail();
        }
    }

    @Test
    public void testIndex() {
        table.clear();
        int goal = length * 2;

        for (int i = 0; i < goal; i++) {
            table.put(keys[i], table.size());
        }

        Integer first = (Integer) table.get(ONE);
        if (first != 0) {
            Assert.fail();
        }

        first = (Integer) table.get(ONEPRIME);
        if (first != 0) {
            Assert.fail();
        }

        Integer second = (Integer) table.get(TWO);
        if (second != 1) {
            Assert.fail();
        }

        second = (Integer) table.get(TWOPRIME);
        if (second != 1) {
            Assert.fail();
        }

        Integer third = (Integer) table.get(THREE);
        if (third != 2) {
            Assert.fail();
        }

        third = (Integer) table.get(THREEPRIME);
        if (third != 2) {
            Assert.fail();
        }

        Integer four = (Integer) table.get(FOUR);
        if (four != 3) {
            Assert.fail();
        }

        four = (Integer) table.get(FOURPRIME);
        if (four != 3) {
            Assert.fail();
        }

        Integer five = (Integer) table.get(FIVE);
        if (five != null) {
            Assert.fail();
        }

        five = (Integer) table.get(FIVEPRIME);
        if (five != null) {
            Assert.fail();
        }

        if (table.size() != 4) {
            Assert.fail();
        }
    }
}
