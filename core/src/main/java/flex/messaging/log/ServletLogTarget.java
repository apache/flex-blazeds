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

import javax.servlet.ServletContext;

/**
 * This is a log target which uses the servlet context in order to log
 * messages.
 */
public class ServletLogTarget extends LineFormattedTarget {
    static ServletContext context;

    /**
     * This method must be called during startup to give this target a reference
     * to the ServletContext.
     *
     * @param ctx the servlet context
     */
    public static void setServletContext(ServletContext ctx) {
        context = ctx;
    }

    //--------------------------------------------------------------------------
    //
    // Constructor
    //
    //--------------------------------------------------------------------------

    /**
     * Default constructor.
     */
    public ServletLogTarget() {
        super();
    }

    boolean warned = false;

    /**
     * Log a message via the servlet context.
     *
     * @param message the message to log.
     */
    @Override
    protected void internalLog(String message) {
        if (context == null) {
            if (!warned) {
                System.out.println("**** No servlet context set in ServletLogTarget - logging disabled.");
                warned = true;
            }
        } else {
            context.log(message);
        }
    }
}