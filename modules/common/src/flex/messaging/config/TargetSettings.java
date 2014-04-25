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

import java.util.List;
import java.util.ArrayList;

import flex.messaging.log.LogCategories;

/**
 * A logging target must specify the class name
 * of the implementation, the level of logging events
 * it will accept, a list of filters for logging
 * categories it is interested in, and a collection of
 * properties required to initialize the target.
 *
 * @author Peter Farland
 * @exclude
 */
public class TargetSettings extends PropertiesSettings
{
    private String className;
    private String level;
    private List filters;

    public TargetSettings(String className)
    {
        this.className = className;
    }

    public String getClassName()
    {
        return className;
    }

    public String getLevel()
    {
        return level;
    }

    public void setLevel(String level)
    {
        this.level = level;
    }

    public List getFilters()
    {
        return filters;
    }

    public void addFilter(String filter)
    {
        if (filters == null)
            filters = new ArrayList();

        // Replace DataService with Service.Data for backwards compatibility,
        // excluding DataService.coldfusion.
        if (filter.startsWith("DataService") && !filter.equals("DataService.coldfusion"))
            filter = filter.replaceFirst("DataService", LogCategories.SERVICE_DATA);

        filters.add(filter);
    }


}
