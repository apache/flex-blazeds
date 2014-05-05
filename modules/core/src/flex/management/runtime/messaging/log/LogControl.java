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

import flex.management.BaseControl;
import flex.messaging.log.AbstractTarget;
import flex.messaging.log.Log;
import flex.messaging.log.Target;

/**
 * The <code>LogControl</code> class is the MBean implemenation
 * for monitoring and managing a <code>Log</code> at runtime through the <code>LogManager</code>.
 * @author majacobs
 *
 */
public class LogControl extends BaseControl implements
        LogControlMBean
{

    private static final String TYPE = "Log"; // The type registered with the mbean server
    private LogManager logManager; // Reference to the LogManager which interfaces with Log
        
    
    /**
     * Creates the mbean and registers it with the mbean server.
     * 
     * @param parent BaseControl
     * @param manager A reference to the LogManager
     */
    public LogControl(BaseControl parent, LogManager manager)
    {
        super(parent);
        this.logManager = manager;
        register();
    }
    
    
    /**
     * Sets the logging level for the target associated with the unique ID searchId.
     * @param searchId the search ID
     * @param level the log level
     */
    public void changeTargetLevel(String searchId, String level)
    {
        Target selectedTarget = Log.getTarget(searchId);
        if (selectedTarget != null)
        {
            selectedTarget.setLevel(new Short(level).shortValue());
        }
    }
    
    /* (non-Javadoc)
     * @see flex.management.BaseControl#getId()
     */
    public String getId()
    {
        return logManager.getId();
    }
    
    /* (non-Javadoc)
     * @see flex.management.BaseControl#getType()
     */
    public String getType()
    {
        return TYPE;
    }

    /**
     * Return a string array of the loggers.
     * @return a string array of loggers
     */
    public String[] getLoggers()
    {
        return logManager.getLoggers();
    }

    /* (non-Javadoc)
     * @see flex.management.runtime.messaging.log.LogControlMBean#getTargets()
     */
    public String[] getTargets()
    {
        return logManager.getTargetIds();
    }

    /* (non-Javadoc)
     * @see flex.management.runtime.messaging.log.LogControlMBean#addFilterForTarget(java.lang.String, java.lang.String)
     */
    public void addFilterForTarget(String targetId, String filter)
    {
        AbstractTarget target = (AbstractTarget) logManager.getTarget(targetId);
        
        if (target != null && logManager.checkFilter(filter))
            target.addFilter(filter);
    }

    /* (non-Javadoc)
     * @see flex.management.runtime.messaging.log.LogControlMBean#getTargetFilters(java.lang.String)
     */
    public String[] getTargetFilters(String targetId)
    {
        return logManager.getTargetFilters(targetId);
    }

    /* (non-Javadoc)
     * @see flex.management.runtime.messaging.log.LogControlMBean#removeFilterForTarget(java.lang.String, java.lang.String)
     */
    public void removeFilterForTarget(String targetId, String filter)
    {
        AbstractTarget target = (AbstractTarget) logManager.getTarget(targetId);
        
        if (target != null && target.containsFilter(filter))
            target.removeFilter(filter);
    }
    
    /* (non-Javadoc)
     * @see flex.management.runtime.messaging.log.LogControlMBean#getCategories()
     */
    public String[] getCategories()
    {
        return (String[]) logManager.getCategories().toArray(new String[0]);
    }


    public Integer getTargetLevel(String searchId)
    {
        AbstractTarget target = (AbstractTarget) logManager.getTarget(searchId);
        
        if (target != null)
        {
            return new Integer(target.getLevel());
        } else
            return new Integer(-1);
    }
    
}
