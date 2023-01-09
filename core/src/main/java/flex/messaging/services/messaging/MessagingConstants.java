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
package flex.messaging.services.messaging;

/**
 *
 */
public interface MessagingConstants {
    // General constants.
    /**
     * The default subtopic separator value.
     */
    String DEFAULT_SUBTOPIC_SEPARATOR = ".";

    // Configuration element constants (for properties in services-config.xml)
    /**
     * Constant for the allow-subtopics configuration element.
     */
    String ALLOW_SUBTOPICS_ELEMENT = "allow-subtopics";
    /**
     * Constant for disallow-wildcard-subtopics configuration element.
     */
    String DISALLOW_WILDCARD_SUBTOPICS_ELEMENT = "disallow-wildcard-subtopics";
    /**
     * Constant for the durable configuration element.
     */
    String IS_DURABLE_ELEMENT = "durable";
    /**
     * Constant for the <message-time-to-live/> configuration element.
     */
    String TIME_TO_LIVE_ELEMENT = "message-time-to-live";
    /**
     * Constant for the <message-priority/> configuration element.
     */
    String MESSAGE_PRIORITY = "message-priority";
    /**
     * Constant for the <subtopic-separator/> configuration element.
     */
    String SUBTOPIC_SEPARATOR_ELEMENT = "subtopic-separator";
    /**
     * Constant for the cluster message routing element.
     */
    String CLUSTER_MESSAGE_ROUTING = "cluster-message-routing";
}
