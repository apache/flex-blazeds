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
 * Implementors of <code>FlexFactory</code> should also implement this interface
 * if their factory has custom destruction behavior.
 */
public interface DestructibleFlexFactory 
{    
    /**
     * This method is called when a component that uses this factory is removed.
     * This method gives the factory a chance to clean up resources that may have
     * been allocated for the component and may now be ready for destruction.
     * 
     * @param instanceInfo The FactoryInstance to be destroyed
     *
     */
    void destroyFactoryInstance(FactoryInstance instanceInfo);
}
