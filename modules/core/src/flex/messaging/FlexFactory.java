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

import flex.messaging.config.ConfigMap;

/**
 * The FlexFactory interface is implemented by factory components that provide
 * instances to the Flex messaging framework.  You can implement this interface
 * if you want to tie together Flex Data Services with another system which maintains
 * component instances (often called the "services layer" in a typical enterprise
 * architecture).  By implementing FlexFactory, you can configure a Flex
 * RemoteObject destination or a Flex Data Management Services assembler which
 * uses a Java object instance in your services layer rather than having Data Services
 * create a new component instance.  In some cases, this means you avoid writing
 * glue code for each service you want to expose to flex clients.
 */
public interface FlexFactory extends FlexConfigurable
{
    /** Request scope string. */
    String SCOPE_REQUEST = "request";
    /** Session scope string. */
    String SCOPE_SESSION = "session";
    /** Application scope string .*/
    String SCOPE_APPLICATION = "application";
    /** Scope string. */
    String SCOPE = "scope";
    /** Source string. */
    String SOURCE = "source";

    /**
     * Called when the
     * definition of an instance that this factory looks up is initialized.
     * It should validate that
     * the properties supplied are valid to define an instance
     * and returns an instance of the type FactoryInstance
     * that contains all configuration necessary to construct
     * an instance of this object.  If the instance is application
     * scoped, the FactoryInstance may contain a reference to the
     * instance directly.
     * <p>
     * Any valid properties used for this configuration
     * must be accessed to avoid warnings about unused configuration
     * elements.  If your factory is only used for application
     * scoped components, you do not need to implement
     * this method as the lookup method itself can be used
     * to validate its configuration.
     * </p><p>
     * The id property is used as a name to help you identify
     * this factory instance for any errors it might generate.
     * </p>
     *
     */
    FactoryInstance createFactoryInstance(String id, ConfigMap properties);

    /**
     * This method is called by the default implementation of FactoryInstance.lookup.
     * When Data Services wants an instance of a given factory destination, it calls the
     * FactoryInstance.lookup to retrieve that instance.  That method in turn
     * calls this method by default.
     *
     * For simple FlexFactory implementations which do not need to
     * add additional configuration properties or logic to the FactoryInstance class,
     * by implementing this method you can avoid having to add an additional subclass of
     * FactoryInstance for your factory.  If you do extend FactoryInstance, it is
     * recommended that you just override FactoryInstance.lookup and put your logic
     * there to avoid the extra level of indirection.
     *
     * @param instanceInfo The FactoryInstance for this destination
     * @return the Object instance to use for the given operation for this destination.
     */
    Object lookup(FactoryInstance instanceInfo);
}
