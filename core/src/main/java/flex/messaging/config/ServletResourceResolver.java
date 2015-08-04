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

import flex.messaging.io.ArrayList;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 *
 */
public class ServletResourceResolver implements ConfigurationFileResolver
{
    private ServletContext context;
    private Stack configurationPathStack = new Stack();

    /**
     * Constructor.
     *
     * @param context servlet context
     */
    public ServletResourceResolver(ServletContext context)
    {
        this.context = context;
    }

    /**
     * Is the configuration file available.
     *
     * @param path path to check
     * @param throwError true if wmethod shold throw a ConfigurationException if path no found.
     * @return true if path is available
     * @throws ConfigurationException if throwError is true and path is not available
     */
    public boolean isAvailable(String path, boolean throwError) throws ConfigurationException
    {
        boolean available = false;
        InputStream is = context.getResourceAsStream(path);
        if (is != null)
        {
            try { is.close(); } catch (IOException ignore) { /* ignore */}
            pushConfigurationFile(path);
            available = true;
        }
        else
        {
            if (throwError)
            {
                // Please specify a valid ''services.configuration.file'' in web.xml.
                ConfigurationException e = new ConfigurationException();
                e.setMessage(11108, new Object[] {path});
                throw e;
            }
        }

        return available;
    }

    public InputStream getConfigurationFile(String path)
    {
        InputStream is = context.getResourceAsStream(path);
        if (is != null)
        {
            pushConfigurationFile(path);
            return is;
        }
        else
        {
            // Please specify a valid ''services.configuration.file'' in web.xml.
            ConfigurationException e = new ConfigurationException();
            e.setMessage(11108, new Object[] {path});
            throw e;
        }
    }

    public InputStream getIncludedFile(String src)
    {
        String path = configurationPathStack.peek() + "/" + src;
        InputStream is = context.getResourceAsStream(path);

        if (is != null)
        {
            pushConfigurationFile(path);
            return is;
        }
        else
        {
            // Please specify a valid include file. ''{0}'' is invalid.
            ConfigurationException e = new ConfigurationException();
            e.setMessage(11107, new Object[] {path});
            throw e;
        }
    }

    public void popIncludedFile()
    {
        configurationPathStack.pop();
    }

    /**
     * Returns the list of XML files (denoted by .xml extension) in the directory
     * relative to the current configuration file.
     */
    public List getFiles(String dir)
    {
        List result =  new ArrayList();
        String prefix = configurationPathStack.peek() + "/";
        Set paths = context.getResourcePaths(prefix + dir);
        if (paths != null)
        {
            for (Object entry : paths)
            {
                String path = (String) entry;
                if (path.endsWith(".xml"))
                {
                    result.add(path.substring(prefix.length()));
                }
            }
            return result;
        }
        else
        {
            // Please specify a valid include directory. ''{0}'' is invalid.
            ConfigurationException e = new ConfigurationException();
            e.setMessage(11113, new Object[]{dir});
            throw e;
        }
    }

    private void pushConfigurationFile(String path)
    {
        String topLevelPath = path.substring(0, path.lastIndexOf('/'));
        configurationPathStack.push(topLevelPath);
    }
}
