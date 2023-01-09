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

/**
 * Event used to notify FlexClientAttributeListeners of changes to FlexClient
 * attributes.
 */
public class FlexClientBindingEvent {
    //--------------------------------------------------------------------------
    //
    // Constructor
    //
    //--------------------------------------------------------------------------

    /**
     * Constructs an event for an attribute that is bound or unbound from a FlexClient.
     *
     * @param client The FlexClient.
     * @param name   The attribute name.
     */
    public FlexClientBindingEvent(FlexClient client, String name) {
        this.client = client;
        this.name = name;
    }


    /**
     * Constructs an event for an attribute that is added to a FlexClient or
     * replaced by a new value.
     *
     * @param client The FlexClient.
     * @param name   The attribute name.
     * @param value  The attribute value.
     */
    public FlexClientBindingEvent(FlexClient client, String name, Object value) {
        this.client = client;
        this.name = name;
        this.value = value;
    }

    //--------------------------------------------------------------------------
    //
    // Variables
    //
    //--------------------------------------------------------------------------

    /**
     * The FlexClient that generated the event.
     */
    private FlexClient client;

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
     * Returns the FlexClient that generated the event.
     *
     * @return The FlexClient that generated the event.
     */
    public FlexClient getClient() {
        return client;
    }

    /**
     * Returns the name of the attribute associated with the event.
     *
     * @return The name of the attribute associated with the event.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the value of the attribute associated with the event.
     *
     * @return The value of the attribute associated with the event.
     */
    public Object getValue() {
        return value;
    }
}
