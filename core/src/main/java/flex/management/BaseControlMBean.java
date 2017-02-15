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
package flex.management;

import java.io.IOException;
import javax.management.ObjectName;

/**
 * The base MBean interface for management beans that control aspects of
 * Flex behavior on the server.
 */
public interface BaseControlMBean
{
    /**
     * Returns the id for this MBean. This is the value that is set for the
     * <code>id</code> key in the <code>ObjectName</code> for this MBean.
     *
     * @return The MBean instance id.
     * @throws IOException Throws IOException.
     */
    String getId() throws IOException;

    /**
     * Returns the type for this MBean. This is the value that is set for the
     * <code>type</code> key in the <code>ObjectName</code> for this MBean.
     *
     * @return The MBean instance type.
     * @throws IOException Throws IOException.
     */
    String getType() throws IOException;

    /**
     * Returns the parent for this MBean. The value is the <code>ObjectName</code>
     * for the parent MBean that conceptually contains this MBean instance. If no
     * parent exists, this method returns <code>null</code>.
     *
     * @return The <code>ObjectName</code> for the parent of this MBean instance.
     * @throws IOException Throws IOException.
     */
    ObjectName getParent() throws IOException;
}
