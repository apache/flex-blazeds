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

package flex.messaging.config;

import flex.messaging.LocalizedException;
import flex.messaging.MessageBroker;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;

public class ConfigurationParserTest {
    private String resourceBase = "/flex/messaging/config/";
    private String baseFilePath;

    @Before
    public void setUp() throws Exception {
        MessageBroker broker = new MessageBroker(false);
        broker.initThreadLocals();
    }

    /*
     *  TESTS
     */
    @Test
    public void testConfiguration() {
        try {
            URL url = this.getClass().getResource(resourceBase + "ConfigurationParserTest.class");
            File file = new File(url.getFile());
            File dir = new File(file.getParent());

            baseFilePath = dir.getAbsolutePath();

            if (dir.isDirectory()) {
                String resourcePath = getResourcePath(baseFilePath);
                processDirectory(dir, resourcePath);
            }
        } catch (IOException ioe) {
            Assert.fail("Configuration test failed: " + ioe.getMessage());
        }
    }

    private void processDirectory(File dir, String resourcePath) throws IOException {
        File[] files = dir.listFiles(new XMLFilenameFilter());

        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    String dirResourcePath = getResourcePath(f.getAbsolutePath());
                    processDirectory(f, dirResourcePath);
                } else {
                    String testName = getTestName(f.getName());
                    System.out.println("Running test: " + testName);
                    processRequest(f.toString(), testName, resourcePath);
                }
            }
        }
    }

    private String getResourcePath(String path) {
        if (path.startsWith(baseFilePath)) {
            path = path.substring(baseFilePath.length());
            path = path.replace('\\', '/');
            return path;
        }

        return null;
    }

    private String getTestName(String fileName) {
        fileName = fileName.toLowerCase();
        int pos = fileName.indexOf(".xml");
        return fileName.substring(0, pos);
    }

    private void processRequest(String filename, String testName, String resourcePath) throws IOException {
        MessagingConfiguration config = new MessagingConfiguration();
        ConfigurationParser parser = new XPathServerConfigurationParser();
        try {
            parser.parse(filename, new LocalFileResolver(), config);

            ConfigurationConfirmation confirmation = getConfirmation(resourcePath, testName);

            if (!confirmation.matches(config)) {
                Assert.fail("MessagingConfiguration did not match test: " + testName);
            }
        } catch (LocalizedException ex) {
            ConfigurationConfirmation confirmation = getConfirmation(resourcePath, testName);

            if (confirmation.isNegativeTest()) {
                if (!confirmation.negativeMatches(ex)) {
                    Assert.fail("MessageException did not match negative test " + testName);
                }
            } else {
                throw ex;
            }
        }
    }

    private ConfigurationConfirmation getConfirmation(String resourcePath, String testName) throws IOException {
        try {
            if (resourceBase.endsWith("/") && resourcePath.startsWith("/")) {
                resourcePath = resourcePath.substring(1);
            }

            String resource = resourceBase + resourcePath;
            if (resource.startsWith("/")) {
                resource = resource.substring(1);
            }

            String packageName = resource.replace('/', '.');

            if (packageName.length() > 0 && !packageName.endsWith(".")) {
                packageName = packageName + ".";
            }

            String className = packageName + "Confirm" + testName;
            Class c = Class.forName(className);

            Object obj = c.newInstance();
            return (ConfigurationConfirmation) obj;
        } catch (Exception e) {
            throw new IOException("Unable to load confirmation for testName: " + testName + " in " + resourcePath);
        }
    }

    static class XMLFilenameFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            String child = dir.getAbsolutePath() + File.separator + name;
            File f = new File(child);

            return f.isDirectory() || name.toLowerCase().endsWith(".xml") && !name.contains("include");
        }
    }

}