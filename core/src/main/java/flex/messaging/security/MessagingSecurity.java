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
package flex.messaging.security;

import flex.messaging.services.messaging.Subtopic;

/**
 * This is an interface which can be implemented by the MessageAdapter or
 * by the DataManagement Assembler instance.  If it is implemented, this
 * class is used to do security filtering of subscribe and send operations.
 */
public interface MessagingSecurity {
    /**
     * This method is invoked before a client subscribe request is processed,
     * so that custom application logic can determine whether the client
     * should be allowed to subscribe to the specified subtopic. You can access
     * the current user via
     * <code>FlexContext.getUserPrincipal()</code>.
     *
     * @param subtopic The subtopic the client is attempting to subscribe to.
     * @return true to allow the subscription, false to prevent it.
     */
    boolean allowSubscribe(Subtopic subtopic);

    /**
     * This method is invoked before a client message is sent to a subtopic,
     * so that custom application logic can determine whether the client
     * should be allowed to send to the specified subtopic. You can access
     * the current user via
     * <code>FlexContext.getUserPrincipal()</code>.
     *
     * @param subtopic The subtopic the client is attempting to send a message to.
     * @return true to allow the message to be sent, false to prevent it.
     */
    boolean allowSend(Subtopic subtopic);
}
