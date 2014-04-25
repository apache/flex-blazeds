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

import flex.management.BaseControlMBean;


/**
 * Defines the exposed properties and operations of the LogControl.
 */
public interface LogControlMBean extends BaseControlMBean
{
    /**
     * Returns the array of log targets.
     *
     * @return The array of log targets.
     */
    String[] getTargets();

    /**
     * Returns the array of log target filters.
     *
     * @param targetId The target id.
     * @return The array of log target filters.
     */
    String[] getTargetFilters(String targetId);

    /**
     * Returns the array of log categories.
     *
     * @return The array of log categories.
     */
    String[] getCategories();

    /**
     * Returns the target level.
     *
     * @param targetId The target id.
     * @return The target level.
     */
    Integer getTargetLevel(String targetId);

    /**
     * Changes the target level.
     *
     * @param targetId The target id.
     * @param level The target level.
     */
    void changeTargetLevel(String targetId, String level);

    /**
     * Adds a filter for the target.
     *
     * @param filter The filter.
     * @param targetId The target id.
     */
    void addFilterForTarget(String filter, String targetId);

    /**
     * Removes a filter from the target.
     *
     * @param filter The filter.
     * @param targetId The target id.
     */
    void removeFilterForTarget(String filter, String targetId);
}
