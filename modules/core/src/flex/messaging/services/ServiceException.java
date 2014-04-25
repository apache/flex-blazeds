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
package flex.messaging.services;

import flex.messaging.MessageException;
import flex.messaging.log.LogEvent;

/**
 * Exception type for Service errors.
 *
 * @author shodgson
 * @exclude
 */
public class ServiceException extends MessageException
{
    static final long serialVersionUID = 3349730139522030203L;
    
    //--------------------------------------------------------------------------
    //
    // Properties
    //
    //--------------------------------------------------------------------------        
    
    //----------------------------------
    //  defaultLogMessageIntro
    //----------------------------------            

    /**
     * Overrides the intro text if the exception is a 'not subscribed' fault. 
     */
    public String getDefaultLogMessageIntro()
    {
        if (code != null && code.equals(MessageService.NOT_SUBSCRIBED_CODE))
            return "Client not subscribed: ";
        else
            return super.getDefaultLogMessageIntro();
    }
    
    //----------------------------------
    //  logStackTraceEnabled
    //----------------------------------            
    
    /**
     * Override to disable stack trace logging if the exception is a 'not subscribed' fault. No need for
     * a stack trace in this case.
     */
    public boolean isLogStackTraceEnabled()
    {
        if (code != null && code.equals(MessageService.NOT_SUBSCRIBED_CODE))
            return false;
        else
            return true;
    }    
    
    //----------------------------------
    //  peferredLogLevel
    //----------------------------------            
    
    /**
     * Override to lower the preferred log level to debug if the exception is a 'not subscribed' fault. 
     */
    public short getPreferredLogLevel()
    {
        String code = getCode();
        // Log not-subscribed errors at a lower level because this is a common occurance
        // following normal failover.
        if (code != null && code.equals(MessageService.NOT_SUBSCRIBED_CODE))
            return LogEvent.DEBUG;
        else
            return super.getPreferredLogLevel();
    }
}