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

/**
 * Event used to notify FlexSessionAttributeListeners of changes to session
 * attributes.
 */
public class FlexSessionBindingEvent
{
    //--------------------------------------------------------------------------
    //
    // Constructor
    //
    //--------------------------------------------------------------------------

    /**
     * Constructs an event for an attribute that is bound or unbound from a session.
     * 
     * @param session The associated session.
     * @param name The attribute name.
     */
    public FlexSessionBindingEvent(FlexSession session, String name)
    {
        this.session = session;
        this.name = name;
    }
    
    /**
     * Constructs an event for an attribute that is added to a session or 
     * replaced by a new value.
     * 
     * @param session The associated session.
     * @param name The attribute name.
     * @param value The attribute value.
     */
    public FlexSessionBindingEvent(FlexSession session, String name, Object value)
    {
        this.session = session;
        this.name = name;
        this.value = value;
    }

    //--------------------------------------------------------------------------
    //
    // Variables
    //
    //--------------------------------------------------------------------------

    /**
     * The session that generated the event.
     */
    private FlexSession session;
    
    /**
     * The name of the attribute associated with the event.
     */
    private String name;
    
    /**
     * The value of the attribute associated with the event.
     */
    private Object value;
    
    //--------------------------------------------------------------------------
    //
    // Methods
    //
    //--------------------------------------------------------------------------
    
    /**
     * Returns the Flex session that generated the event.
     * 
     * @return The Flex session that generated the event.
     */
    public FlexSession getSession()
    {
        return session;
    }
    
    /**
     * Returns the name of the attribute associated with the event.
     * 
     * @return The name of the attribute associated with the event.
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * Returns the value of the attribute associated with the event.
     * 
     * @return The value of the attribute associated with the event.
     */
    public Object getValue()
    {
        return value;
    }
}
