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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * @exclude
 */
public class LocalFileResolver implements ConfigurationFileResolver
{
    public static final int CLIENT = 0;
    public static final int SERVER = 1;
    public static final int LIVECYCLE = 2;

    private static final int ERR_MSG_INVALID_PATH_CLIENT = 11106;
    private static final int ERR_MSG_INVALID_PATH_SERVER = 11108;
    private static final int ERR_MSG_INVALID_PATH_LIVECYCLE = 11122;

    private Stack configurationPathStack = new Stack();
    int version = CLIENT;

    public LocalFileResolver()
    {
    }

    public LocalFileResolver(int version)
    {
        this.version = version;
    }

    public void setErrorMessage(ConfigurationException e, String path)
    {
        if (version == LIVECYCLE)
        {
            // Invalid location: ''{0}''. Please specify a valid LiveCycle Data Services Configuration file via the LiveCycle Admin UI.
            e.setMessage(ERR_MSG_INVALID_PATH_LIVECYCLE, new Object[] {path});
        }
        else if (version == SERVER)
        {
            // Please specify a valid ''services.configuration.file'' in web.xml. You specified ''{0}''. This is not a valid file system path reachable via the app server and is also not a path to a resource in your J2EE application archive.
            e.setMessage(ERR_MSG_INVALID_PATH_SERVER, new Object[]{path});
        }
        else
        {
            // Please specify a valid <services/> file path in flex-config.xml.
            e.setMessage(ERR_MSG_INVALID_PATH_CLIENT);
        }
    }

    public InputStream getConfigurationFile(String path)
    {
        File f = new File(path);
        try
        {
            if (f != null && f.exists() && f.isAbsolute())
            {
                FileInputStream fin = new FileInputStream(f);
                pushConfigurationFile(f.getParent());
                return fin;
            }
            else
            {
                ConfigurationException e = new ConfigurationException();
                setErrorMessage(e, path);
                throw e;
            }
        }
        catch (FileNotFoundException ex)
        {
            ConfigurationException e = new ConfigurationException();
            setErrorMessage(e, path);
            e.setRootCause(ex);
            throw e;
        }
        catch (SecurityException se)
        {
            ConfigurationException e = new ConfigurationException();
            setErrorMessage(e, path);
            e.setRootCause(se);
            throw e;
        }
    }

    public InputStream getIncludedFile(String src)
    {
        String path = configurationPathStack.peek() + File.separator + src;
        File f = new File(path);
        try
        {
            if (f != null && f.exists() && f.isAbsolute())
            {
                FileInputStream fin = new FileInputStream(f);
                pushConfigurationFile(f.getParent());
                return fin;
            }
            else
            {
                // Please specify a valid include file. ''{0}'' is invalid.
                ConfigurationException e = new ConfigurationException();
                e.setMessage(11107, new Object[] {path});
                throw e;
            }
        }
        catch (FileNotFoundException ex)
        {
            // Please specify a valid include file. ''{0}'' is invalid.
            ConfigurationException e = new ConfigurationException();
            e.setMessage(11107, new Object[] {path});
            e.setRootCause(ex);
            throw e;
        }
        catch (SecurityException se)
        {
            // Please specify a valid include file. ''{0}'' is invalid.
            ConfigurationException e = new ConfigurationException();
            e.setMessage(11107, new Object[] {path});
            e.setRootCause(se);
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
     *
     * @param dir a directory relative to the current configuration file
     * @return a (possibly empty) list of file names
     */
    public List getFiles(String dir)
    {
        List result = new ArrayList();
        File f = new File(configurationPathStack.peek().toString(), dir);
        if (f.exists() && f.isDirectory())
        {
            String[] xmlFiles = f.list(new FilenameFilter()
            {
                public boolean accept(File dir, String name)
                {
                    return name.endsWith(".xml");
                }
            });

            // prepend the directory to each filename
            for (int i = 0; i < xmlFiles.length; i++)
            {
                String file = xmlFiles[i];
                result.add(dir +  File.separator + file);
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

    private void pushConfigurationFile(String topLevelPath)
    {
        configurationPathStack.push(topLevelPath);
    }

    public String getIncludedPath(String src)
    {
        return configurationPathStack.peek() + File.separator + src;
    }

    public long getIncludedLastModified(String src)
    {
        String path = configurationPathStack.peek() + File.separator + src;
        File f = new File(path);
        return f.lastModified();
    }
}