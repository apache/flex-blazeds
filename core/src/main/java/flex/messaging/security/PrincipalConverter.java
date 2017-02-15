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

import java.security.Principal;

/**
 * The principal Converter interface.
 */
public interface PrincipalConverter
{
    /**
     * Classes that implement the flex.messaging.security.PrinciplaConverter interface, to convert a J2EE Principal to a
     * Flex Principal impl. A Flex Principal impl is specific to different Application Servers and will be used by Flex to 
     * do security authorization check, which calls security framework API specific to Application Servers.
     */
    Principal convertPrincipal(Principal principal);

}
