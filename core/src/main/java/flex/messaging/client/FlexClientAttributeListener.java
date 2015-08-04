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
 * Interface for FlexClient attribute listeners.
 */
public interface FlexClientAttributeListener 
{
    /**
     * Callback invoked after an attribute is added to the FlexClient.
     * 
     * @param event The event containing the associated FlexClient and attribute
     *              information.
     */
    void attributeAdded(FlexClientBindingEvent event);
    
    /**
     * Callback invoked after an attribute is removed from the FlexClient.
     * 
     * @param event The event containing the associated FlexClient and attribute
     *              information.
     */
    void attributeReplaced(FlexClientBindingEvent event);
    
    /**
     * Callback invoked after an attribute has been replaced with a new value.
     * 
     * @param event The event containing the associated FlexClient and attribute
     *              information.
     */
    void attributeRemoved(FlexClientBindingEvent event);
}
