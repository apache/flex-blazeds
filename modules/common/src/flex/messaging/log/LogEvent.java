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

/**
 * @exclude
 */
public class LogEvent
{
    public static final short NONE = 2000;
    public static final short FATAL = 1000;
    public static final short ERROR = 8;
    public static final short WARN = 6;
    public static final short INFO = 4;
    public static final short DEBUG = 2;
    public static final short ALL = 0;

    /**
     * Provides access to the level for this log event.
     * Valid values are:
     * <ul>
     * <li><code>LogEvent.DEBUG</code> designates informational
     * level messages that are fine grained and most helpful when
     * debugging an application.</li>
     *
     * <li><code>LogEvent.INFO</code> designates informational messages
     * that highlight the progress of the application at
     * coarse-grained level.</li>
     *
     * <li><code>LogEvent.WARN</code> designates events that could be
     * harmful to the application operation.</li>
     *
     * <li><code>LogEvent.ERROR</code> designates error events that might
     * still allow the application to continue running.</li>
     *
     * <li><code>LogEvent.FATAL</code> designates events that are very
     * harmful and will eventually lead to application failure.</li>
     *
     * </ul>
     */
    public short level;

    /**
     * Provides access to the message that was logged.
     */
    public String message;

    /**
     * Logger instance that raised the log event.
     */
    public Logger logger;

    /**
     * Related exception, if applicable.
     */
    public Throwable throwable;

    /**
     * Constructor.
     *
     * @param lgr Logger instance that raised the log event.
     * @param msg Message that was logged.
     * @param lvl The level for the log event.
     * @param t Related exception, if applicable.
     */
    public LogEvent(Logger lgr, String msg, short lvl, Throwable t)
    {
        logger = lgr;
        message = msg;
        level = lvl;
        throwable = t;
    }

    /**
     * Returns a string value representing the level specified.
     *
     * @param value the level a string is desired for.
     * @return the level specified in english
     */
    public static String getLevelString(short value)
    {
        switch (value)
        {
            case NONE:
                return ("NONE");
            case FATAL:
                return ("FATAL");
            case ERROR:
                return ("ERROR");
            case WARN:
                return ("WARN");
            case INFO:
                return ("INFO");
            case DEBUG:
                return ("DEBUG");
            case ALL:
                return ("ALL");
            default:
                return ("UNKNOWN");
        }
    }
}
