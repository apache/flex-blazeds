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

import flex.messaging.LocalizedException;
import flex.messaging.config.ConfigMap;
import flex.messaging.util.UUIDUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @exclude
 */
public abstract class AbstractTarget implements Target
{
    private static final int INVALID_FILTER_CHARS = 10016;
    private static final int INVALID_FILTER_STAR = 10017;

    protected final String id;
    protected List filters;
    protected volatile short level;
    protected volatile int loggerCount;
    private final Object lock = new Object();
    private boolean usingDefaultFilter = false;

    //--------------------------------------------------------------------------
    //
    // Constructor
    //
    //--------------------------------------------------------------------------

    /**
     * Default constructor.
     */
    public AbstractTarget()
    {
        id = UUIDUtils.createUUID();
        level = LogEvent.ERROR;
        filters = new ArrayList();
        filters.add("*");
        usingDefaultFilter = true;
    }

    //--------------------------------------------------------------------------
    //
    // Initialize, validate, start, and stop methods.
    //
    //--------------------------------------------------------------------------

    /**
     * Initializes the target with id and properties. Subclasses can overwrite.
     *
     * @param id id for the target which is ignored.
     * @param properties ConfigMap of properties for the target.
     */
    public void initialize(String id, ConfigMap properties)
    {
        // No-op
    }

    //--------------------------------------------------------------------------
    //
    // Public Getters and Setters for AbstractTarget properties
    //
    //--------------------------------------------------------------------------

    /**
     * Return a read-only snap-shot of the current filters for this target.
     *
     * @return An unmodifiable list of filters.
     */
    public List getFilters()
    {
        return Collections.unmodifiableList(new ArrayList(filters));
    }

    /**
     * Adds a filter to this target.
     *
     * @param value Filter to be added.
     */
    public void addFilter(String value)
    {
        if (value != null)
            validateFilter(value);
        else // Default to "*"
            value = "*";

        boolean filterWasAdded = false;
        synchronized (lock)
        {
            if (!filters.contains(value))
            {
                // If the default filter is being used, remove it.
                if (usingDefaultFilter)
                {
                    removeFilter("*");
                    usingDefaultFilter = false;
                }
                filters.add(value);
                filterWasAdded = true;
            }
        }
        if (filterWasAdded)
            Log.processTargetFilterAdd(this, value);
    }

    /**
     * Removes a filter from this target.
     *
     * @param value Filter to be removed.
     */
    public void removeFilter(String value)
    {
        boolean filterWasRemoved;
        synchronized (lock)
        {
            filterWasRemoved = filters.remove(value);
        }
        if (filterWasRemoved)
            Log.processTargetFilterRemove(this, value);
    }

    /**
     * Sets the list of filters for this target.
     *
     * @param value List of filters.
     */
    public void setFilters(List value)
    {
        if (value != null && value.size() > 0)
        {
            // a valid filter value will be fully qualified or have a wildcard
            // in it.  the wild card can only be located at the end of the
            // expression.  valid examples  xx*, xx.*,  *
            for (int i = 0; i < value.size(); i++)
            {
                validateFilter((String) value.get(i));
            }
        }
        else
        {
            // if null was specified then default to all
            value = new ArrayList();
            value.add("*");
        }

        Log.removeTarget(this);
        synchronized (lock)
        {
            filters = value;
            usingDefaultFilter = false;
        }
        Log.addTarget(this);
    }

    /**
     * Return the log level for this target.
     */
    public short getLevel()
    {
        return level;
    }

    /**
     * Return the target's unique ID.
     */
    public String getId()
    {
        return id;
    }

    /**
     * Sets the log level for this target. If not set, defaults to <code>LogEvent.ERROR</code>.
     */
    public void setLevel(short value)
    {
        level = value;
        Log.resetTargetLevel();
    }

    /**
     * Sets up this target with the specified logger.
     * This allows this target to receive log events from the specified logger.
     *
     * @param logger this target should listen to.
     */
    public void addLogger(Logger logger)
    {
        if (logger != null)
        {
            synchronized (lock)
            {
                loggerCount++;
            }
            logger.addTarget(this);
        }
    }

    /**
     * Stops this target from receiving events from the specified logger.
     *
     * @param logger this target should ignore.
     */
    public void removeLogger(Logger logger)
    {
        if (logger != null)
        {
            synchronized (lock)
            {
                loggerCount--;
            }
            logger.removeTarget(this);
        }
    }

    /**
     * @param filter category to check against the filters defined for this target
     * @return whether filter is defined
     */
    public boolean containsFilter(String filter)
    {
        return filters.contains(filter);
    }

    //--------------------------------------------------------------------------
    //
    // Protected/private methods.
    //
    //--------------------------------------------------------------------------

    private void validateFilter(String value)
    {
        // check for invalid characters
        if (Log.hasIllegalCharacters(value))
        {
            //Error for filter '{filter}'. The following characters are not valid: {Log.INVALID_CHARS}
            LocalizedException ex = new LocalizedException();
            ex.setMessage(INVALID_FILTER_CHARS, new Object[]{value, Log.INVALID_CHARS});
            throw ex;
        }

        int index = value.indexOf('*');
        if ((index >= 0) && (index != (value.length() - 1)))
        {
            //Error for filter '{filter}'. '*' must be the right most character.
            LocalizedException ex = new LocalizedException();
            ex.setMessage(INVALID_FILTER_STAR, new Object[]{value});
            throw ex;
        }
    }
}
