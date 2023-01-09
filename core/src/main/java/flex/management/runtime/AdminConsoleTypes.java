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
package flex.management.runtime;

/**
 * The interface defines a number of constants for admin console types.
 */
public interface AdminConsoleTypes {
    static final int GENERAL_SERVER = 1;
    static final int GENERAL_POLLABLE = 2;
    static final int GENERAL_OPERATION = 3;

    static final int GRAPH_BY_POLL_INTERVAL = 50;

    static final int ENDPOINT_SCALAR = 100;
    static final int ENDPOINT_POLLABLE = 101;

    static final int DESTINATION_GENERAL = 150;
    static final int DESTINATION_POLLABLE = 151;
}
