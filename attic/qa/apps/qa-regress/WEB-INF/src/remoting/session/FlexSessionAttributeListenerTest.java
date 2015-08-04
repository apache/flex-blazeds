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
package remoting.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

import flex.messaging.FlexContext;
import flex.messaging.FlexSession;
import flex.messaging.FlexSessionAttributeListener;
import flex.messaging.FlexSessionBindingEvent;

public class FlexSessionAttributeListenerTest implements FlexSessionAttributeListener, Serializable
{

    public void attributeAdded(FlexSessionBindingEvent event)
    {
        System.out.println("<attributeAdded>(id=" + event.getSession().getId() + ") " + event.getName() + ":" + event.getValue());        
    }

    public void attributeRemoved(FlexSessionBindingEvent event) {
        System.out.println("<attributeRemoved>(id=" + event.getSession().getId() + ") " + event.getName() + ":" + event.getValue());
    }

    public void attributeReplaced(FlexSessionBindingEvent event) {
        System.out.println("<attributeReplaced>(id=" + event.getSession().getId() + ") " + event.getName() + ":" + event.getValue()+ "==>" + event.getSession().getAttribute(event.getName()));
    }
    
    public void addListener()
    {
        FlexSession session = FlexContext.getFlexSession();
        if (session != null)
        {
            FlexSessionAttributeListenerTest listener = (FlexSessionAttributeListenerTest)session.getAttribute("listener");
            if(listener == null)
            {
                listener = this;
                session.setAttribute("listener", listener);
            }
            session.addSessionAttributeListener(listener);
        }
    }
    
    public void removeListener()
    {
        FlexSession session = FlexContext.getFlexSession();
        if (session != null)
        {
            FlexSessionAttributeListenerTest listener = (FlexSessionAttributeListenerTest)session.getAttribute("listener");
            if(listener != null)
                session.removeSessionAttributeListener(this);
        }
    }
    
    public void setAttribute(String name, String value)    
    {
        FlexSession session = FlexContext.getFlexSession();
        if (session != null)
            session.setAttribute(name, value);
    }
   
    public void removeAttribute(String name)
    {
        FlexSession session = FlexContext.getFlexSession();
        if (session != null)
            session.removeAttribute(name);
    }
    
    public Collection getAttributeNames()
    {
        Collection c = new ArrayList();
        FlexSession session = FlexContext.getFlexSession();
        if (session != null)
        {
            Enumeration names = session.getAttributeNames();
            while(names.hasMoreElements())
            {
                c.add(names.nextElement());
            }
        }            
        return c;
    }
    
}
