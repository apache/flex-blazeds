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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import flex.messaging.log.Log;

/**
 * Provider implementation for <code>HttpFlexSession</code>s.
 * Not intended for public use.
 */
public class HttpFlexSessionProvider extends AbstractFlexSessionProvider
{
    //--------------------------------------------------------------------------
    //
    // Public Methods
    //
    //--------------------------------------------------------------------------

    /**
     * Factory method to get an existing <tt>HttpFlexSession</tt> for the current request,
     * or create and return a new <code>HttpFlexSession</code> if necessary.
     * The <code>HttpFlexSession</code> wraps the underlying J2EE <code>HttpSession</code>.
     * Not intended for public use.
     * 
     * @param request The current <tt>HttpServletRequest</tt>.
     * @return A <tt>HttpFlexSession</tt>.
     */
    public HttpFlexSession getOrCreateSession(HttpServletRequest request)
    {
        HttpFlexSession flexSession;
        HttpSession httpSession = request.getSession(true);

        if (!HttpFlexSession.isHttpSessionListener && !HttpFlexSession.warnedNoEventRedispatch)
        {
            HttpFlexSession.warnedNoEventRedispatch = true;
            if (Log.isWarn())
                Log.getLogger(HttpFlexSession.WARN_LOG_CATEGORY).warn("HttpFlexSession has not been registered as a listener in web.xml for this application so no events will be dispatched to FlexSessionAttributeListeners or FlexSessionBindingListeners. To correct this, register flex.messaging.HttpFlexSession as a listener in web.xml.");
        }

        boolean isNew = false;
        synchronized (httpSession)
        {
            flexSession = (HttpFlexSession)httpSession.getAttribute(HttpFlexSession.SESSION_ATTRIBUTE);
            if (flexSession == null)
            {
                flexSession = new HttpFlexSession(this);
                // Correlate this FlexSession to the HttpSession before triggering any listeners.
                FlexContext.setThreadLocalSession(flexSession);
                httpSession.setAttribute(HttpFlexSession.SESSION_ATTRIBUTE, flexSession);
                flexSession.setHttpSession(httpSession);
                isNew = true;
            }
            else
            {
                FlexContext.setThreadLocalSession(flexSession);
                if (flexSession.httpSession == null)
                {
                    // httpSession is null if the instance is new or is from
                    // serialization.
                    flexSession.setHttpSession(httpSession);
                    isNew = true;
                }
            }
        }

        if (isNew)
        {
            getFlexSessionManager().registerFlexSession(flexSession);
            flexSession.notifyCreated();

            if (Log.isDebug())
                Log.getLogger(HttpFlexSession.FLEX_SESSION_LOG_CATEGORY).debug("FlexSession created with id '" + flexSession.getId() + "' for an Http-based client connection.");
        }

        return flexSession;
    }
    }
