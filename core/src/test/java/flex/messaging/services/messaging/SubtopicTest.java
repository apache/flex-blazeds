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
package flex.messaging.services.messaging;

import org.junit.Assert;
import org.junit.Test;

public class SubtopicTest {
    private static final String DEFAULT_SEPERATOR = ".";
    private static final String ANOTHER_SEPERATOR = "*";

    @Test
    public void testMatches1() {
        Subtopic s1 = new Subtopic("foo", DEFAULT_SEPERATOR);
        Subtopic s2 = new Subtopic("foo", DEFAULT_SEPERATOR);
        boolean result = s1.matches(s2);
        Assert.assertTrue(result);
    }

    @Test
    public void testMatches2() {
        Subtopic s1 = new Subtopic("foo", DEFAULT_SEPERATOR);
        Subtopic s2 = new Subtopic("bar", DEFAULT_SEPERATOR);
        boolean result = s1.matches(s2);
        Assert.assertFalse(result);
    }

    @Test
    public void testMatches3() {
        Subtopic s1 = new Subtopic("foo.bar", DEFAULT_SEPERATOR);
        Subtopic s2 = new Subtopic("foo.bar", DEFAULT_SEPERATOR);
        boolean result = s1.matches(s2);
        Assert.assertTrue(result);
    }

    @Test
    public void testMatches4() {
        Subtopic s1 = new Subtopic("foo.bar", DEFAULT_SEPERATOR);
        Subtopic s2 = new Subtopic("foo*bar", ANOTHER_SEPERATOR);
        boolean result = s1.matches(s2);
        Assert.assertFalse(result);
    }

    @Test
    public void testMatches5() {
        Subtopic s1 = new Subtopic("*", DEFAULT_SEPERATOR);
        Subtopic s2 = new Subtopic("foo.bar.foo", DEFAULT_SEPERATOR);
        boolean result = s1.matches(s2);
        Assert.assertTrue(result);
    }

    @Test
    public void testMatches6() {
        Subtopic s1 = new Subtopic("foo.*", DEFAULT_SEPERATOR);
        Subtopic s2 = new Subtopic("foo.bar", DEFAULT_SEPERATOR);
        boolean result = s1.matches(s2);
        Assert.assertTrue(result);
    }

    @Test
    public void testMatches7() {
        Subtopic s1 = new Subtopic("foo.*", DEFAULT_SEPERATOR);
        Subtopic s2 = new Subtopic("foo.bar.foo", DEFAULT_SEPERATOR);
        boolean result = s1.matches(s2);
        Assert.assertTrue(result);
    }

    @Test
    public void testMatches8() {
        Subtopic s1 = new Subtopic("foo.bar.foo", DEFAULT_SEPERATOR);
        Subtopic s2 = new Subtopic("foo.*", DEFAULT_SEPERATOR);
        boolean result = s1.matches(s2);
        Assert.assertTrue(result);
    }

    @Test
    public void testMatches9() {
        Subtopic s1 = new Subtopic("foo.bar.*", DEFAULT_SEPERATOR);
        Subtopic s2 = new Subtopic("foo.bar.foo", DEFAULT_SEPERATOR);
        boolean result = s1.matches(s2);
        Assert.assertTrue(result);
    }

    @Test
    public void testMatches10() {
        Subtopic s1 = new Subtopic("foo.*", DEFAULT_SEPERATOR);
        Subtopic s2 = new Subtopic("foo", DEFAULT_SEPERATOR);
        boolean result = s1.matches(s2);
        Assert.assertFalse(result);
    }

}
