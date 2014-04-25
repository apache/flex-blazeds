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
import flex.messaging.util.PrettyPrinter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;

/**
 * @exclude
 */
public class Log
{

    /** @exclude **/
    public static final String INVALID_CHARS = "[]~$^&\\/(){}<>+=`!#%?,:;\'\"@";

    // Errors
    private static final int INVALID_TARGET = 10013;
    private static final int INVALID_CATEGORY = 10014;
    private static final int INVALID_CATEGORY_CHARS = 10015;

    private static Log log;
    private static PrettyPrinter prettyPrinter;
    private static String prettyPrinterClass = "BasicPrettyPrinter";

    private static final HashSet excludedProperties = new HashSet();
    /** @exclude **/
    public static final String VALUE_SUPRESSED = "** [Value Suppressed] **";

    private volatile short targetLevel;
    private final Map loggers;
    private final List targets;
    private final Map targetMap;
    private static final Object staticLock = new Object();


    //--------------------------------------------------------------------------
    //
    // Constructor
    //
    //--------------------------------------------------------------------------

    /**
     * Private constructor.
     */
    private Log()
    {
        targetLevel = LogEvent.NONE;
        loggers = new HashMap();
        targets = new ArrayList();
        targetMap = new LinkedHashMap();
    }


    /**
     * Creates the log on first access, returns already created log on
     * subsequent calls.
     *
     * @return log.
     */
    public static Log createLog()
    {
        synchronized (staticLock)
        {
            if (log == null)
                log = new Log();

            return log;
        }
    }

    //--------------------------------------------------------------------------
    //
    // Initialize, validate, start, and stop methods.
    //
    //--------------------------------------------------------------------------

    /**
     * Initializes Log with id and properties.
     *
     * @param id Id for the Log which is ignored, though is used by the ManageableComponent superclass
     * @param properties ConfigMap of properties for the Log.
     */
    public static synchronized void initialize(String id, ConfigMap properties)
    {
        String value = properties.getPropertyAsString("pretty-printer", null);
        if (value != null)
            prettyPrinterClass = value;

        // Create a HashSet with the properties that we want to exclude from the
        // list of properties given by 'getPropertiesAsList'
        ConfigMap excludeMap = properties.getPropertyAsMap("exclude-properties", null);
        if (excludeMap != null)
        {
            if (excludeMap.getPropertyAsList("property", null) != null)
                excludedProperties.addAll(excludeMap.getPropertyAsList("property", null));
        }
    }

    //--------------------------------------------------------------------------
    //
    // Public Getters and Setters for Log properties
    //
    //--------------------------------------------------------------------------

    /**
     * Indicates whether a fatal level log event will be processed by a log target.
     * @return boolean true if it is Fatal level
     */
    public static boolean isFatal()
    {
        return log == null ? false : log.targetLevel <= LogEvent.FATAL;
    }

    /**
     * Indicates whether an error level log event will be processed by a log target.
     * @return boolean true if it is Error level
     */
    public static boolean isError()
    {
        return log == null ? false : log.targetLevel <= LogEvent.ERROR;
    }

    /**
     * Indicates whether a warn level log event will be processed by a log target.
     * @return boolean true if it is Warn level
     */
    public static boolean isWarn()
    {
        return log == null ? false : log.targetLevel <= LogEvent.WARN;
    }

    /**
     * Indicates whether an info level log event will be processed by a log target.
     * @return boolean true if it is Info level
     */
    public static boolean isInfo()
    {
        return log == null ? false : log.targetLevel <= LogEvent.INFO;
    }

    /**
     * Indicates whether a debug level log event will be processed by a log target.
     * @return boolean true if it is debug level
     */
    public static boolean isDebug()
    {
        return log == null ? false : log.targetLevel <= LogEvent.DEBUG;
    }

    /**
     * Indicates whether a log property should be excluded.
     * @param property the property to check
     * @return boolean true if the property should be excluded
     */
    public static boolean isExcludedProperty(String property)
    {
        return !excludedProperties.isEmpty() && excludedProperties.contains(property);
    }


    /**
     * Given a category, returns the logger associated with the category.
     *
     * @param category Categogry for the logger.
     * @return Logger associated with the category.
     */
    public static Logger getLogger(String category)
    {
        if (log != null)
            return getLogger(log, category);

        // Return a dummy logger?
        return new Logger(category);
    }

    /**
     * @exclude
     */
    public static Logger getLogger(Log log, String category)
    {
        checkCategory(category);

        synchronized (staticLock)
        {
            Logger result = (Logger) log.loggers.get(category);
            if (result == null)
            {
                result = new Logger(category);

                // Check to see if there are any targets for this logger.
                for (Iterator iter = log.targets.iterator(); iter.hasNext();)
                {
                    Target target = (Target) iter.next();
                    if (categoryMatchInFilterList(category, target.getFilters()))
                        target.addLogger(result);
                }

                log.loggers.put(category, result);
            }
            return result;
        }
    }

    /**
     * Returns an unmodifiable snapshot of the targets registered with this Log when the
     * method is invoked.
     * @return List the list of targets 
     */
    public static List getTargets()
    {
        if (log != null)
        {
            List currentTargets;
            // Snapshot the current target list (shallow copy) and return it.
            synchronized (staticLock)
            {
                currentTargets = Collections.unmodifiableList(new ArrayList(log.targets));
            }
            return currentTargets;
        }
        return null;
    }

    /**
     * Return the Log's map of targets keyed on their human-readable ids (e.g. ConsoleTarget0, ConsoleTarget1, etc.)
     * @return Map the target map
     */
    public static Map getTargetMap()
    {
        if (log != null)
        {
            Map currentTargets;
            synchronized (staticLock)
            {
                currentTargets = new LinkedHashMap(log.targetMap);
            }
            return currentTargets;
        }
        return null;
    }

    /**
     * Returns the target associated with the unique ID searchId.  Returns null if no
     * such target exists.
     * @param searchId the search ID
     * @return Target the associated target
     */
    public static Target getTarget(String searchId)
    {
        if (log != null)
        {
            synchronized (staticLock)
            {
                return (Target) log.targetMap.get(searchId);
            }
        }

        return null;
    }

    /**
     * Return the categories for all of the loggers
     * @return String[] the categories for all of the loggers
     */
    public String[] getLoggers()
    {
        String[] currentCategories;
        if (log != null)
        {
            synchronized (staticLock)
            {
                Object[] currentCategoryObjects = loggers.keySet().toArray();
                currentCategories = new String[currentCategoryObjects.length];
                for (int i = 0; i < currentCategoryObjects.length; i++)
                {
                    currentCategories[i] = (String)(currentCategoryObjects[i]);
                }
            }
        }
        else
        {
            currentCategories = new String[0];
        }

        return currentCategories;
    }

    /**
     * Adds a target to the log.
     *
     * @param target Target to be added.
     */
    public static void addTarget(Target target)
    {
        if (log != null)
        {
            if (target != null)
            {
                synchronized (staticLock)
                {
                    List filters = target.getFilters();

                    // need to find what filters this target matches and set the specified
                    // target as a listener for that logger.
                    Iterator it = log.loggers.keySet().iterator();
                    while (it.hasNext())
                    {
                        String key = (String) it.next();
                        if (categoryMatchInFilterList(key, filters))
                            target.addLogger((Logger) log.loggers.get(key));
                    }
                    // if we found a match all is good, otherwise we need to
                    // put the target in a waiting queue in the event that a logger
                    // is created that this target cares about.
                    if (!log.targets.contains(target))
                        log.targets.add(target);

                    if (!log.targetMap.containsValue(target))
                    {
                        String name = target.getClass().getName();

                        if (name.indexOf(".") > -1)
                        {
                            String[] classes = name.split("\\.");
                            name = classes[classes.length - 1];
                        }

                        log.targetMap.put(new String(name + log.targetMap.size()), target);
                    }

                    // update our global target log level if this target is more verbose.
                    short targetLevel = target.getLevel();
                    if (log.targetLevel == LogEvent.NONE)
                        log.targetLevel = targetLevel;
                    else if (targetLevel < log.targetLevel)
                    {
                        log.targetLevel = targetLevel;
                    }
                }
            }
            else
            {
                // Invalid target specified. Target must not be null.
                LocalizedException ex = new LocalizedException();
                ex.setMessage(INVALID_TARGET);
                throw ex;
            }
        }
    }

    /**
     * Removes a target from the log.
     *
     * @param target The target to be removed.
     */
    public static void removeTarget(Target target)
    {
        if (log != null)
        {
            if (target != null)
            {
                synchronized (staticLock)
                {
                    // Remove the target from any associated loggers.
                    List filters = target.getFilters();
                    Iterator it = log.loggers.keySet().iterator();
                    while (it.hasNext())
                    {
                        String key = (String) it.next();
                        if (categoryMatchInFilterList(key, filters))
                            target.removeLogger((Logger) log.loggers.get(key));
                    }
                    // Remove the target from the Log set.
                    log.targets.remove(target);
                    resetTargetLevel();
                }
            }
            else
            {
                // Invalid target specified. Target must not be null.
                LocalizedException ex = new LocalizedException();
                ex.setMessage(INVALID_TARGET);
                throw ex;
            }
        }
    }

    //--------------------------------------------------------------------------
    //
    // Other Public APIs
    //
    //--------------------------------------------------------------------------

    /**
     * This method removes all of the current loggers and targets from the cache.
     * and resets target level.
     */
    public static synchronized void reset()
    {
        flush();
    }

    /**
     * @exclude
     */
    public static void flush()
    {
        if (log != null)
        {
            log.loggers.clear();
            log.targets.clear();
            log.targetLevel = LogEvent.NONE;
        }
    }

    /**
     * @exclude
     */
    public static short readLevel(String l)
    {
        short lvl = LogEvent.ERROR;
        if ((l != null) && (l.length() > 0))
        {
            l = l.trim().toLowerCase();
            char c = l.charAt(0);
            switch (c)
            {
                case 'n':
                    lvl = LogEvent.NONE;
                    break;
                case 'e':
                    lvl = LogEvent.ERROR;
                    break;
                case 'w':
                    lvl = LogEvent.WARN;
                    break;
                case 'i':
                    lvl = LogEvent.INFO;
                    break;
                case 'd':
                    lvl = LogEvent.DEBUG;
                    break;
                case 'a':
                    lvl = LogEvent.ALL;
                    break;
                default:
                    lvl = LogEvent.ERROR;
            }
        }

        return lvl;
    }

    /**
     * @exclude
     * This method checks the specified string value for illegal characters.
     *
     * @param value to check for illegal characters.
     *              The following characters are not valid:
     *              []~$^&amp;\/(){}&lt;&gt;+=`!#%?,:;'"&amp;#64;
     * @return <code>true</code> if there are any illegal characters found,
     *         <code>false</code> otherwise
     */
    public static boolean hasIllegalCharacters(String value)
    {
        char[] chars = value.toCharArray();
        for (int i = 0; i < chars.length; i++)
        {
            char c = chars[i];
            if (INVALID_CHARS.indexOf(c) != -1)
            {
                return true;
            }
        }

        return false;
    }

    /**
     * @exclude
     * Returns the PrettyPrinter used by the Log.
     */
    public static PrettyPrinter getPrettyPrinter()
    {
        if (prettyPrinter == null ||
                !prettyPrinter.getClass().getName().equals(prettyPrinterClass))
        {
            try
            {
                Class c = Class.forName(prettyPrinterClass);
                prettyPrinter = (PrettyPrinter)c.newInstance();
            }
            catch (Throwable t)
            {
            }
        }
        return (PrettyPrinter)prettyPrinter.copy();
    }

    /**
     * @exclude
     * Returns the current target level for the Log.
     */
    public static short getTargetLevel()
    {
        return log == null ? LogEvent.NONE : log.targetLevel;
    }

    /**
     * @exclude
     * Sets the pretty printer class name used by the log.
     *
     * @param value Name of the pretty printer class.
     */
    public static void setPrettyPrinterClass(String value)
    {
        prettyPrinterClass = value;
    }

    //--------------------------------------------------------------------------
    //
    // Protected/private methods.
    //
    //--------------------------------------------------------------------------

    /* package */ static void resetTargetLevel()
    {
        if (log != null)
        {
            synchronized (staticLock)
            {
                short maxTargetLevel = LogEvent.NONE;
                for (Iterator iter = log.targets.iterator(); iter.hasNext();)
                {
                    short targetLevel = ((Target) iter.next()).getLevel();
                    if (maxTargetLevel == LogEvent.NONE || targetLevel < maxTargetLevel)
                        maxTargetLevel = targetLevel;
                }
                log.targetLevel = maxTargetLevel;
            }
        }
    }

    /* package */ static void processTargetFilterAdd(Target target, String filter)
    {
        if (log != null)
        {
            synchronized (staticLock)
            {
                List filters = new ArrayList();
                filters.add(filter);

                // Find the loggers this target matches and add the
                // target as a listener for log events from these loggers.
                Iterator it = log.loggers.keySet().iterator();
                while (it.hasNext())
                {
                    String key = (String) it.next();
                    if (categoryMatchInFilterList(key, filters))
                        target.addLogger((Logger) log.loggers.get(key));
                }
            }
        }
    }

    /* package */ static void processTargetFilterRemove(Target target, String filter)
    {
        if (log != null)
        {
            synchronized (staticLock)
            {
                // Remove the target from any matching loggers.
                List filters = new ArrayList();
                filters.add(filter);
                Iterator it = log.loggers.keySet().iterator();
                while (it.hasNext())
                {
                    String key = (String) it.next();
                    if (categoryMatchInFilterList(key, filters))
                        target.removeLogger((Logger) log.loggers.get(key));
                }
            }
        }
    }

    /**
     * This method checks that the specified category matches any of the filter
     * expressions provided in the filters array.
     *
     * @param category to match against
     * @param filters  - list of strings to check category against.
     * @return <code>true</code> if the specified category matches any of the
     *         filter expressions found in the filters list, <code>false</code>
     *         otherwise.
     */
    private static boolean categoryMatchInFilterList(String category, List filters)
    {
        if (filters == null)
            return false;

        for (int i = 0; i < filters.size(); i++)
        {
            String filter = (String) filters.get(i);
            // match category to filter based on the presence of a wildcard
            if (checkFilterToCategory(filter,category))
                return true;
        }
        return false;
    }

    /**
     * Check whether the category match with the filter.
     * @param filter The filter string to check against a specific category
     * @param category The category which the filter could match
     * @return whether the filter matches a specific category
     */
    public static boolean checkFilterToCategory(String filter, String category)
    {
        int index = -1;
        index = filter.indexOf("*");

        if (index == 0) // match all
        {
            return true;
        }
        else if (index < 0) // match full category to filter
        {
            if (category.equals(filter))
            {
                return true;
            }
        }
        else // match partial category to filter
        {
            if ((category.length() >= index) && category.substring(0, index).equals(filter.substring(0, index)))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * This method will ensure that a valid category string has been specified.
     * If the category is not valid an exception will be thrown.
     *
     * Categories can not contain any blanks or any of the following characters:
     * []`*~,!#$%^&amp;()]{}+=\|'";?&gt;&lt;./&amp;#64; or be less than 1 character in length.
     */
    private static void checkCategory(String category)
    {
        if (category == null || category.length() == 0)
        {
            // Categories must be at least one character in length.
            LocalizedException ex = new LocalizedException();
            ex.setMessage(INVALID_CATEGORY);
            throw ex;
        }

        if (hasIllegalCharacters(category) || (category.indexOf("*") != -1))
        {
            // Categories can not contain any of the following characters: 'INVALID_CHARS'
            LocalizedException ex = new LocalizedException();
            ex.setMessage(INVALID_CATEGORY_CHARS, new Object[]{INVALID_CHARS});
            throw ex;
        }
    }

    /**
     * Clean up static member variables.
     */
    public static void clear()
    {
        log = null;
        prettyPrinter = null;
    }

}
