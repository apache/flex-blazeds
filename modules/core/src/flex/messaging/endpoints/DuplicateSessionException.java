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
package flex.messaging.endpoints;

import flex.messaging.MessageException;
import flex.messaging.log.LogEvent;

/**
 * Exception class used to indicate duplicate client sessions were detected.
 */
public class DuplicateSessionException extends MessageException
{
    /**
     *
     */
    public static final String DUPLICATE_SESSION_DETECTED_CODE = "Server.Processing.DuplicateSessionDetected";

    /**
     *
     */
    private static final long serialVersionUID = -741704726700619666L;

    //--------------------------------------------------------------------------
    //
    // Constructors
    //
    //--------------------------------------------------------------------------

    /**
     * Default constructor.
     * Sets the code to a default value of <code>DUPLICATE_SESSION_DETECTED_CODE</code>.
     */
    public DuplicateSessionException()
    {
        setCode(DUPLICATE_SESSION_DETECTED_CODE);
    }

    //--------------------------------------------------------------------------
    //
    // Properties
    //
    //--------------------------------------------------------------------------

    //----------------------------------
    //  preferredLogLevel
    //----------------------------------

    /**
     * Override to log at the DEBUG level.
     */
    @Override public short getPreferredLogLevel()
    {
        return LogEvent.DEBUG;
    }

    //----------------------------------
    //  logStackTraceEnabled
    //----------------------------------

    /**
     * Override to suppress stack trace logging.
     */
    @Override public boolean isLogStackTraceEnabled()
    {
        return false;
    }
}
