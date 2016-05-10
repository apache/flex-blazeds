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
package flex.messaging.client;

import flex.messaging.MessageException;
import flex.messaging.log.LogEvent;

/**
 *
 */
public class FlexClientNotSubscribedException extends MessageException
{
    /**
     *
     */
    private static final long serialVersionUID = 773524927178340950L;

    //--------------------------------------------------------------------------
    //
    // Properties
    //
    //--------------------------------------------------------------------------        
    
    //----------------------------------
    //  defaultLogMessageIntro
    //----------------------------------            

    /**
     * Overrides the intro text for the log message. 
     */
    public String getDefaultLogMessageIntro()
    {
        return "FlexClient not subscribed: ";        
    }
    
    //----------------------------------
    //  logStackTraceEnabled
    //----------------------------------            
    
    /**
     * Override to disable stack trace logging.
     */
    public boolean isLogStackTraceEnabled()
    {
        return false;        
    }    
    
    //----------------------------------
    //  peferredLogLevel
    //----------------------------------            
    
    /**
     * Override to lower the preferred log level to debug. 
     */
    public short getPreferredLogLevel()
    {
        return LogEvent.DEBUG;        
    }
}
