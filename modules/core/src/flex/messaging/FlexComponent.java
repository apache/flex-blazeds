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
 * Defines the lifecycle interface for FlexComponents, allowing
 * the server to manage the running state of server components
 * through a consistent interface.
 */
public interface FlexComponent extends FlexConfigurable
{
    /**
     * Invoked to start the component.
     * The {@link FlexConfigurable#initialize(String, flex.messaging.config.ConfigMap)} method inherited 
     * from the {@link FlexConfigurable} interface must be invoked before this method is invoked.
     * Once this method returns, {@link #isStarted()} must return true.
     */
    void start();

    /**
     * Invoked to stop the component.
     * Once this method returns, {@link #isStarted()} must return false.
     */
    void stop();

    /**
     * Indicates whether the component is started and running.
     * 
     * @return <code>true</code> if the component has started; 
     *         otherwise <code>false</code>.
     */
    boolean isStarted();   
}
