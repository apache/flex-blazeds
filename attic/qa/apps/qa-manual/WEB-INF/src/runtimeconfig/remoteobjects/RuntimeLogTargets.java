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
package runtimeconfig.remoteobjects;

import flex.messaging.log.ConsoleTarget;
import flex.messaging.log.Log;
import flex.messaging.log.LogEvent;

/*
 * This class allows logging levels to be changed dynamically from Error to Debug (and vice-versa).
 */
public class RuntimeLogTargets
{    

	public String createLogTarget(String target) 
	{
		short currentLevel = ((ConsoleTarget)Log.getTargets().get(0)).getLevel();
		String retVal = "Log level did not change. Default target level: " + LogEvent.getLevelString(currentLevel);
		
		if (target.equals("Debug")) 
		{
			currentLevel = createDebugLogTarget();
			retVal = LogEvent.getLevelString(currentLevel);
		}
		else if (target.equals("Error"))
		{
			currentLevel = createErrorLogTarget();
			retVal = LogEvent.getLevelString(currentLevel);			
		}
		
		return retVal; 
	}
	
	/*
	 * The purpose of this method is to replace the default "Error" logging target
	 * with Debug log level and specific filters.  
	 * Verification of the log must be manually done against the console output.
	 *
	 	<target class="flex.messaging.log.ConsoleTarget" level="Debug">
		    <properties>
		        <prefix>[LCDS] </prefix>
		        <includeDate>false</includeDate>
		        <includeTime>true</includeTime>
		        <includeLevel>true</includeLevel>
		        <includeCategory>true</includeCategory>
		    </properties>
		    <filters>
		        <pattern>Service.Data.*</pattern>
		        <pattern>Message.*</pattern>
		    </filters>
		</target>
     */
    private short createDebugLogTarget()
    {
    	//Remove any other targets
    	Log.reset();
    	
        //Up the logging level to debug, and create a logging target dynamically
    	ConsoleTarget myTarget = new ConsoleTarget();
        myTarget.setLevel(LogEvent.DEBUG);
        myTarget.setPrefix("[LCDS] ");
        //myTarget.setIncludeDate(true);
        myTarget.setIncludeTime(true);
        myTarget.setIncludeLevel(true);
        myTarget.setIncludeCategory(true);
        //myTarget.addFilter("Startup.*");
        myTarget.addFilter("Service.Data.*");
        myTarget.addFilter("Message.*");
        myTarget.addFilter("Endpoint.*");
        
        //Add the new Debug target
        Log.addTarget(myTarget);
        
        return ((ConsoleTarget)Log.getTargets().get(0)).getLevel();
    }

	/*
	 * Use this method to return to the Error log level in qa-manual app.
	 *
	 	<target class="flex.messaging.log.ConsoleTarget" level="Error">
		    <properties>
		        <prefix>[Flex] </prefix>
		        <includeDate>false</includeDate>
		        <includeTime>true</includeTime>
		        <includeLevel>true</includeLevel>
		        <includeCategory>true</includeCategory>
		    </properties>
		    <filters>
		        <pattern>Service.*</pattern>
		        <pattern>Message.*</pattern>
		        <pattern>DataService.*</pattern>
		        <!--<pattern>Endpoint.*</pattern>-->
		    </filters>
		</target>
     */
    private short createErrorLogTarget()
    {
    	//Remove any other targets
    	Log.reset();
    	
    	//Up the logging level to debug, and create a logging target dynamically
    	ConsoleTarget myTarget = new ConsoleTarget();
        myTarget.setLevel(LogEvent.ERROR);
        myTarget.setPrefix("[Flex] ");
        myTarget.setIncludeDate(false);
        myTarget.setIncludeTime(true);
        myTarget.setIncludeLevel(true);
        myTarget.setIncludeCategory(true);
        myTarget.addFilter("Service.*");
        myTarget.addFilter("Message.*");
        myTarget.addFilter("DataService.*");        
        Log.addTarget(myTarget);
        
        return ((ConsoleTarget)Log.getTargets().get(0)).getLevel();
    }
}


