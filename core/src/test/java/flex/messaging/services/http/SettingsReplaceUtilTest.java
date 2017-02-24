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

package flex.messaging.services.http;

import flex.messaging.MessageBroker;
import flex.messaging.MessageException;
import flex.messaging.util.SettingsReplaceUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Set;

public class SettingsReplaceUtilTest {

    @Before
    public void setUp() throws Exception {
        MessageBroker broker = new MessageBroker(false);
        broker.initThreadLocals();
    }

    @Test
    public void testReplaceContextPathDash() {
        String actual = SettingsReplaceUtil.replaceContextPath("http://localhost:8100/{context-root}/foo.mxml", "/dev");
        String expected = "http://localhost:8100/dev/foo.mxml";
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testReplaceContextPathDot() {
        String actual = SettingsReplaceUtil.replaceContextPath("http://localhost:8100/{context.root}/foo.mxml", "/dev");
        String expected = "http://localhost:8100/dev/foo.mxml";
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testReplaceContextPathBegin() {
        String actual = SettingsReplaceUtil.replaceContextPath("{context.root}/foo.mxml", "/dev");
        String expected = "/dev/foo.mxml";
        Assert.assertEquals(expected, actual);

    }

    @Test
    public void testReplaceContextPathEnd() {
        String actual = SettingsReplaceUtil.replaceContextPath("http://localhost:8100/{context.root}", "/dev");
        String expected = "http://localhost:8100/dev";
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testReplaceContextPathNull() {
        try {
            String actual = SettingsReplaceUtil.replaceContextPath("http://localhost:8100/{context.root}", null);
            Assert.fail("MessageException expected. Result was " + actual);
        } catch (MessageException me) {
            String error = "{context.root} token cannot";
            Assert.assertTrue(me.getMessage().contains(error));
        }
    }

    @Test
    public void testReplaceGivenServerDash() {
        String actual = SettingsReplaceUtil.replaceAllTokensGivenServerName("http://{server-name}:{server-port}/dev/foo.mxml",
                "/dev", "10.1.1.1", "80", "http");
        String expected = "http://10.1.1.1:80/dev/foo.mxml";
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testReplaceGivenServerDot() {
        String actual = SettingsReplaceUtil.replaceAllTokensGivenServerName("http://{server.name}:{server.port}/dev/foo.mxml",
                "/dev", "10.1.1.1", "80", "http");
        String expected = "http://10.1.1.1:80/dev/foo.mxml";
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testReplaceGivenServerNullPort() {
        try {
            String actual = SettingsReplaceUtil.replaceAllTokensGivenServerName("http://{server.name}:{server.port}/dev/foo.mxml",
                    "/dev", "10.1.1.1", null, "http");
            Assert.fail("MessageException expected. Result was " + actual);
        } catch (MessageException me) {
            String error = "{server.port} token cannot";
            Assert.assertTrue(me.getMessage().contains(error));
        }
    }

    @Test
    public void testReplaceGivenServerStarPort() {
        String actual = SettingsReplaceUtil.replaceAllTokensGivenServerName("http://{server.name}:*/dev/foo.mxml",
                "/dev", "10.1.1.1", "80", "http");
        String expected = "http://10.1.1.1:*/dev/foo.mxml";
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testReplaceGivenServerContextRoot() {
        String actual = SettingsReplaceUtil.replaceAllTokensGivenServerName("http://{server.name}:{server.port}/{context.root}/foo.mxml",
                "/dev", "10.1.1.1", "80", "http");
        String expected = "http://10.1.1.1:80/dev/foo.mxml";
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testReplaceGivenServerNoPort() {
        String actual = SettingsReplaceUtil.replaceAllTokensGivenServerName("http://{server.name}/dev/foo.mxml",
                "/dev", "10.1.1.1", "80", "http");
        String expected = "http://10.1.1.1/dev/foo.mxml";
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testReplaceGivenServerNull() {
        try {
            String actual = SettingsReplaceUtil.replaceAllTokensGivenServerName("http://{server.name}:{server.port}/{context.root}/foo.mxml",
                    null, null, null, "http");
            Assert.fail("MessageException expected. Result was " + actual);
        } catch (MessageException me) {
            String error = "{context.root} token cannot";
            Assert.assertTrue(me.getMessage().contains(error));
        }
    }

    @Test
    public void testReplaceGivenServerRelative() {
        String actual = SettingsReplaceUtil.replaceAllTokensGivenServerName("/dev/foo.mxml",
                "/dev", "10.1.1.1", "80", "http");
        String expected = "http://10.1.1.1:80/dev/foo.mxml";
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testReplaceCalculateServer() {
        ArrayList<String> urls = new ArrayList<String>();
        urls.add("http://{server.name}:*/dev/foo.mxml");
        Set updatedUrls = SettingsReplaceUtil.replaceAllTokensCalculateServerName(urls, "/dev");

        Assert.assertTrue(updatedUrls.size() >= 4); // at least, localhost, 127.0.0.1, ip num, ip name
        Assert.assertTrue(updatedUrls.contains("http://localhost:*/dev/foo.mxml"));
        Assert.assertTrue(updatedUrls.contains("http://127.0.0.1:*/dev/foo.mxml"));
    }

    @Test
    public void testReplaceCalculateServerPort() {
        try {
            ArrayList<String> urls = new ArrayList<String>();
            urls.add("http://{server.name}:{server.port}/dev/foo.mxml");
            Set updatedUrls = SettingsReplaceUtil.replaceAllTokensCalculateServerName(urls, "/dev");
            Assert.fail("MessageException expected. Instead result was " + updatedUrls);
        } catch (MessageException me) {
            String error = "{server.port} token cannot";
            Assert.assertTrue(me.getMessage().contains(error));
        }
    }

    @Test
    public void testReplaceCalculateServerMultiple() {
        ArrayList<String> urls = new ArrayList<String>();
        urls.add("http://{server.name}:*/dev/foo.mxml");
        Set updatedUrls = SettingsReplaceUtil.replaceAllTokensCalculateServerName(urls, "/dev");
        int count = updatedUrls.size();

        urls.add("http://{server.name}:*/dev/foo.mxml");
        Assert.assertTrue(updatedUrls.size() == count); // should result in the same list
    }

    @Test
    public void testIPv6ShortForm() {
        String src = "http://[fe80::20d:60ff:fef9:8757]:8400/qa-regress/basic.html";
        String expected = "http://[fe80:0:0:0:20d:60ff:fef9:8757]:8400/qa-regress/basic.html";
        Assert.assertEquals(expected, SettingsReplaceUtil.updateIPv6(src));

        src = "http://[fe80::20d:60ff:fef9:8757]:8400/*";
        expected = "http://[fe80:0:0:0:20d:60ff:fef9:8757]:8400/*";
        Assert.assertEquals(expected, SettingsReplaceUtil.updateIPv6(src));

        src = "http://[::1]:8400/*";
        expected = "http://[0:0:0:0:0:0:0:1]:8400/*";
        Assert.assertEquals(expected, SettingsReplaceUtil.updateIPv6(src));
    }
}