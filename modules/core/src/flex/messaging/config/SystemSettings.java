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

import flex.messaging.log.Log;
import flex.messaging.log.Logger;
import flex.messaging.util.PropertyStringResourceLoader;
import flex.messaging.util.ResourceLoader;
import flex.messaging.util.WatchedObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletContext;

/**
 * @exclude
 */
public class SystemSettings
{
    private ResourceLoader resourceLoader;
    private Locale defaultLocale;
    private boolean enforceEndpointValidation;
    private boolean manageable;
    private boolean redeployEnabled;
    private int watchInterval;
    private List watches;
    private List touches;
    private String uuidGeneratorClassName;
    private String dotNetFrameworkVersion;

    public SystemSettings()
    {
        enforceEndpointValidation = false;
        manageable = true;
        redeployEnabled = false;
        resourceLoader = new PropertyStringResourceLoader();
        touches = new ArrayList();
        watches = new ArrayList();
        watchInterval = 20;
        dotNetFrameworkVersion = null;
    }

    public void setDefaultLocale(Locale locale)
    {
        defaultLocale = locale;
        resourceLoader.setDefaultLocale(defaultLocale);
    }

    public Locale getDefaultLocale()
    {
        return defaultLocale;
    }

    public boolean isManageable()
    {
        return manageable;
    }

    public void setManageable(String manageable)
    {
        manageable = manageable.toLowerCase();
        if (manageable.startsWith("f"))
            this.manageable = false;
    }

    public boolean isEnforceEndpointValidation()
    {
        return enforceEndpointValidation;
    }

    public void setEnforceEndpointValidation(String enforceEndpointValidation)
    {
        if (enforceEndpointValidation == null || enforceEndpointValidation.length() == 0)
            return;
        if (enforceEndpointValidation.toLowerCase().startsWith("t"))
            this.enforceEndpointValidation = true;
    }

    public ResourceLoader getResourceLoader()
    {
        return resourceLoader;
    }

    public void setResourceLoader(ResourceLoader resourceLoader)
    {
        this.resourceLoader = resourceLoader;
    }

    public void setRedeployEnabled(String enabled)
    {
        enabled = enabled.toLowerCase();
        if (enabled.startsWith("t"))
            this.redeployEnabled = true;
    }

    public boolean getRedeployEnabled()
    {
        return redeployEnabled;
    }

    public void setWatchInterval(String interval)
    {
        this.watchInterval = Integer.parseInt(interval);
    }

    public int getWatchInterval()
    {
        return watchInterval;
    }

    public void addWatchFile(String watch)
    {
        this.watches.add(watch);
    }

    public List getWatchFiles()
    {
        return watches;
    }

    public void addTouchFile(String touch)
    {
        this.touches.add(touch);
    }

    public List getTouchFiles()
    {
        return touches;
    }

    public void setPaths(ServletContext context)
    {
        if (redeployEnabled)
        {
            List resolvedWatches = new ArrayList();
            for (int i = 0; i < watches.size(); i++)
            {
                String path = (String)watches.get(i);
                String resolvedPath = null;
                if (path.startsWith("{context.root}") || path.startsWith("{context-root}"))
                {
                    path = path.substring(14);
                    resolvedPath = context.getRealPath(path);

                    if (resolvedPath != null)
                    {
                        try
                        {
                            resolvedWatches.add(new WatchedObject(resolvedPath));
                        }
                        catch (FileNotFoundException fnfe)
                        {
                            Logger logger = Log.getLogger(ConfigurationManager.LOG_CATEGORY);
                            if (logger != null)
                            {
                                logger.warn("The watch-file, " + path + ", could not be found and will be ignored.");
                            }
                        }
                    }
                    else
                    {
                        Logger logger = Log.getLogger(ConfigurationManager.LOG_CATEGORY);
                        logger.warn("The watch-file, " + path + ", could not be resolved to a path and will be ignored.");
                    }
                }
                else
                {
                    try
                    {
                        resolvedWatches.add(new WatchedObject(path));
                    }
                    catch (FileNotFoundException fnfe)
                    {
                        Logger logger = Log.getLogger(ConfigurationManager.LOG_CATEGORY);
                        if (logger != null)
                        {
                            logger.warn("The watch-file, " + path + ", could not be found and will be ignored.");
                        }
                    }
                }
            }
            watches = resolvedWatches;

            List resolvedTouches = new ArrayList();
            for (int i = 0; i < touches.size(); i++)
            {
                String path = (String)touches.get(i);
                String resolvedPath = null;
                if (path.startsWith("{context.root}") || path.startsWith("{context-root}"))
                {
                    path = path.substring(14);
                    resolvedPath = context.getRealPath(path);

                    if (resolvedPath != null)
                    {
                        File file = new File(resolvedPath);
                        if (!file.exists() || (!file.isFile() && !file.isDirectory()) || (!file.isAbsolute()))
                        {
                            Logger logger = Log.getLogger(ConfigurationManager.LOG_CATEGORY);
                            logger.warn("The touch-file, " + path + ", could not be found and will be ignored.");
                        }
                        else
                        {
                            resolvedTouches.add(resolvedPath);
                        }
                    }
                    else
                    {
                        Logger logger = Log.getLogger(ConfigurationManager.LOG_CATEGORY);
                        logger.warn("The touch-file, " + path + ", could not be resolved to a path and will be ignored.");
                    }
                }
                else
                {
                    try
                    {
                        resolvedTouches.add(new WatchedObject(path));
                    }
                    catch (FileNotFoundException fnfe)
                    {
                        Logger logger = Log.getLogger(ConfigurationManager.LOG_CATEGORY);
                        if (logger != null)
                        {
                            logger.warn("The touch-file, " + path + ", could not be found and will be ignored.");
                        }
                    }
                }
            }
            touches = resolvedTouches;
        }
    }

    /**
     * Returns the UUID generator class name.
     * 
     * @return The UUID generator class name.
     */
    public String getUUIDGeneratorClassName()
    {
        return uuidGeneratorClassName;
    }

    /**
     * Sets the UUID generator class name.
     * 
     * @param value The UUID generator class name.
     */
    public void setUUIDGeneratorClassName(String value)
    {
        uuidGeneratorClassName = value;
    }

    /**
     * Set the dotnet framework version to use.
     * @param version the configured dotnet framework version
     */
    public void setDotNetFrameworkVersion(String version)
    {
        dotNetFrameworkVersion = version;
    }
    
    /**
     * Get the dotnet framework version.
     * @return String the dotnet framework version
     */
    public String getDotNetFrameworkVersion()
    {
        return dotNetFrameworkVersion;
    }
    /**
     * Clean up static member variables.
     */
    public void clear()
    {
        resourceLoader = null;
        defaultLocale = null;
        watches = null;
        touches = null;
        dotNetFrameworkVersion = null;
    }

}
