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
package flex.messaging;

import flex.messaging.util.StringUtils;


/**
 * Class representing the build version of Data Services.
 */
public class VersionInfo
{
    //Cache this info as it should not change during the time class is loaded
    public static String BUILD_MESSAGE;
    public static String BUILD_NUMBER_STRING;
    public static String BUILD_TITLE;
    public static long BUILD_NUMBER;
    
    private static final String LCDS_CLASS = "flex.data.DataService";

    public static String buildMessage()
    {
        if (BUILD_MESSAGE == null)
        {
            try
            {
                //Ensure we've parsed build info
                getBuild();

                if (StringUtils.isEmpty(BUILD_NUMBER_STRING))
                {
                    BUILD_MESSAGE = BUILD_TITLE;
                }
                else
                {
                    BUILD_MESSAGE = BUILD_TITLE + ": " + BUILD_NUMBER_STRING;
                }
            }
            catch (Throwable t)
            {
                BUILD_MESSAGE = BUILD_TITLE +": information unavailable";
            }
        }

        return BUILD_MESSAGE;
    }

    public static long getBuildAsLong()
    {
        if (BUILD_NUMBER == 0)
        {
            getBuild();

            if (BUILD_NUMBER_STRING != null && !BUILD_NUMBER_STRING.equals(""))
            {
                try
                {
                    BUILD_NUMBER = Long.parseLong(BUILD_NUMBER_STRING);
                }
                catch (NumberFormatException nfe)
                {
                    // ignore, just return 0
                }
            }
        }

        return BUILD_NUMBER;
    }

    public static String getBuild()
    {
        if (BUILD_NUMBER_STRING == null)
        {
            Class classToUseForManifest;  
            
            try
            {
                classToUseForManifest = Class.forName(LCDS_CLASS);
            }
            catch (ClassNotFoundException e)
            {
                classToUseForManifest = VersionInfo.class;
            }
            
            try
            {
                BUILD_NUMBER_STRING = "";
                Package pack = classToUseForManifest.getPackage();
                BUILD_NUMBER_STRING = pack.getImplementationVersion();
                BUILD_TITLE = pack.getImplementationTitle();
            }
            catch (Throwable t)
            {
                // ignore, just return empty string
            }
        }

        return BUILD_NUMBER_STRING;
    }
}
