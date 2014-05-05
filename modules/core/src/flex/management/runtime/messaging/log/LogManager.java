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

package flex.management.runtime.messaging.log;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.lang.reflect.Field;

import flex.management.ManageableComponent;
import flex.messaging.config.ConfigurationException;
import flex.messaging.log.Log;
import flex.messaging.log.LogCategories;
import flex.messaging.log.Target;


/**
 * The LogManager is an interface between the Log and the LogControl which exists
 * because Log lives in the common package, so it cannot extend ManageableComponent itself,
 * which is necessary for a class to be exposed through MBeans.
 *
 * @author majacobs
 *
 */
public class LogManager extends ManageableComponent
{

    private static final String LOG_CATEGORY = LogCategories.CONFIGURATION; // Log category used by LogManager (ManageableComponent)
    private static final int NULL_LOG_REF_EXCEPTION = 10031;

    private Log log;
    private String ID = "log";
    private CategoryManager categoryManager;
    private LogControl controller;

    private boolean isSetup;

    /**
     * Public constructor required by ManageableComponent.
     */
    public LogManager()
    {
        this(true);
    }

    public LogManager(boolean enableManagement)
    {
        super(enableManagement);
        setId(ID);

        categoryManager = new CategoryManager();

    }


    public void setupLogControl()
    {
        if (!isSetup)
        {
            controller = new LogControl(getParent().getControl(), this);
            setControl(controller);
            controller.register();
            isSetup = true;
        }
    }

    public void stop()
    {
        if (!isStarted())
        {
            return;
        }


        super.stop();

        // Remove management
        if (isManaged())
        {
            if (getControl() != null)
            {
                getControl().unregister();
                setControl(null);
            }
            setManaged(false);
        }
    }

    public void setLog(Log logInstance)
    {
        log = logInstance;
    }

    /* (non-Javadoc)
     * @see flex.management.ManageableComponent#getLogCategory()
     */
    protected String getLogCategory()
    {
        return LOG_CATEGORY;
    }

    /**
     * Gets the Loggers as a string array.
     * @return a String array
     */
    public String[] getLoggers()
    {
        return log.getLoggers();
    }

    /**
     * Gets the Target IDs.
     * @return a string array
     */
    public String[] getTargetIds()
    {
        return (String[]) Log.getTargetMap().keySet().toArray(new String[0]);
    }

    /**
     * Get a Target for a targetId.
     *
     * @param targetId the target ID
     * @return the target from the Log, or null if it is not found
     */
    public Target getTarget(String targetId)
    {
        return (Target) Log.getTargetMap().get(targetId);
    }

    /**
     * Gets the filters for a given target.
     * @param targetId the target ID
     * @return a string array
     */
    public String[] getTargetFilters(String targetId)
    {

        Target target = getTarget(targetId);

        if (target == null)
            return new String[0];

        List filterObjects = target.getFilters();
        String[] filters = new String[filterObjects.size()];
        for (int i = 0; i < filterObjects.size(); i++)
        {
            filters[i] = (String)filterObjects.get(i);
        }

        return filters;
    }

    /**
     * Check whether a filter is valid.
     * @param filter the filter string to check
     * @return whether the category exists in LogCategories
     */
    public boolean checkFilter(String filter)
    {
        return categoryManager.checkFilter(filter);
    }

    /**
     * Return a list of categories in LogCategories.
     * @return the list of categories in LogCategories
     */
    public List getCategories()
    {
        return categoryManager.getCategories();
    }

    protected void validate()
    {
        if (isValid())
            return;

        super.validate();

        if (log == null)
        {
            invalidate();
            ConfigurationException ex = new ConfigurationException();
            ex.setMessage(NULL_LOG_REF_EXCEPTION, new Object[]{});
            throw ex;
        }

    }

    /**
     * This private class keeps track of what categories exist in LogCategories by implementing
     * LogCategories and reflecting the interface's properties.
     *
     * @author majacobs
     *
     */
    private class CategoryManager implements LogCategories
    {
        private List categories;

        /**
         * Construct an ArrayList for each category in the reflected public properties
         * Note this will be incorrect if additional public properties are added to this class
         * or to the interface LogCategories.
         */
        public CategoryManager()
        {
            categories = new ArrayList();

            Field[] categoryFields = this.getClass().getFields();
            for (int i = 0; i < categoryFields.length; i++)
            {
                try
                {
                    categories.add((String)categoryFields[i].get(this));
                }
                catch (IllegalAccessException iae)
                {
                    // Illegal Access on reflection
                }
            }
        }


        /**
         * Check if any categories match with the filter (the filter is valid or not).
         * @param filter the filter string to check
         * @return whether the filter is valid  (with or without a trailing .*)
         */
        public boolean checkFilter(String filter)
        {

            for (int i = 0; i < categories.size(); i++)
            {
                if (Log.checkFilterToCategory((String)filter, (String)categories.get(i)))
                {
                    return true;
                }
            }
            return false;
        }

        /**
         * Return a list of log categories.
         * @return List a list of the categories
         */
        public List getCategories()
        {
            return Collections.unmodifiableList(new ArrayList(categories));
        }
    }
}
