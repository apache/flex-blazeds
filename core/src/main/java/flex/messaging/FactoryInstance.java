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
import flex.messaging.config.ConfigurationException;

/**
 * This class is used by the FlexFactory to store the configuration
 * for an instance created by the factory.  There is one of these for
 * each destination currently since only destinations create these components.
 *
 * @see flex.messaging.FlexFactory
 */
public class FactoryInstance 
{
    private static final int INVALID_SCOPE = 10653;

    private FlexFactory factory;
    private String id;
    private String scope = FlexFactory.SCOPE_REQUEST;
    private String source;
    private ConfigMap properties;

    /**
     * Normally FactoryInstances are constructed by Data Services during startup so you
     * do not need to use this method.  It is typically called from the 
     * FlexFactory.createFactoryInstance method as Data Services is parsing
     * the destination configuration information for a given destination.  
     * You can override this method to extract additional configuration for 
     * your component from the properties argument.
     *
     * @param factory the FlexFactory this FactoryInstance is created from
     * @param id the Destination's id
     * @param properties the configuration properties for this destination.  
     *
     * @see flex.messaging.config.ConfigMap
     */
    public FactoryInstance(FlexFactory factory, String id, ConfigMap properties)
    {
        this.factory = factory;
        this.id = id;
        this.properties = properties;
    }

    /**
     * Get the factory instance ID.
     * @return The destination's id that this FactoryInstance is associated with.
     */
    public String getId()
    {
        return id;
    }

    /**
     * Since many factories may provide components in different
     * scopes, this is abstracted in the base factory instance class.
     * @param scope the scope
     */
    public void setScope(String scope)
    {
        this.scope = scope;

        if (!FlexFactory.SCOPE_SESSION.equals(scope)
                && !FlexFactory.SCOPE_APPLICATION.equals(scope)
                && !FlexFactory.SCOPE_REQUEST.equals(scope))
        {
            // Invalid scope setting for RemotingService destination '{id}'.
            // Valid options are 'request', 'session', or 'application'.
            ConfigurationException ex = new ConfigurationException();
            ex.setMessage(INVALID_SCOPE, new Object[] {id, "\'request\', \'session\', or \'application\'"});
            throw ex;
        }

    }
    
    /**
     * Get the scope.
     * @return String the scope
     */
    public String getScope()
    {
        return scope;
    }

    /**
     * This is by convention the main property for the defining the 
     * instance we create with this factory.  It may be the class name
     * for the JavaFactory or the id for a factory that uses ids.
     *
     * @param source source
     */
    public void setSource(String source)
    {
        this.source = source;
    }
    
    /**
     * Get the source.
     * @return String the source string
     */
    public String getSource()
    {
        return source;
    }

    /**
     * If possible, returns the class for the underlying configuration.  
     * This method can return null if the class is not known until the lookup
     * method is called.  The goal is so the factories which know the class
     * at startup time can provide earlier error detection.  If the class is not
     * known, this method can return null and validation will wait until the
     * first lookup call.
     *
     * @return the class for this configured instance or null if the class
     * is not known until lookup time.
     */
    public Class getInstanceClass()
    {
        return null;
    }

    /**
     * Returns the ConfigMap that this factory instance was created with.  You can
     * use this ConfigMap to retrieve additional properties which this factory
     * instance is configured with.  For example, if you are defining a remote object
     * destination, your FactoryInstance can be configured with additional XML tags
     * underneath the properties tag for your destination.  It is important that
     * if you expect additional properties that you access in the ConfigMap or call 
     * allowProperty on that property when the FactoryInstance is created.  Otherwise,
     * these properties can generate warnings about "unexpected" configuration.
     * @return ConfigMap the ConfigMap that this factory was created with
     * @see flex.messaging.config.ConfigMap
     */
    public ConfigMap getProperties()
    {
        return properties;
    }

    /**
     * Return an instance as appropriate for this instance of the given
     * factory.  This just calls the lookup method on the factory that this
     * instance was created on.  You override this method to return the
     * specific component for this destination.
     * @return Object the object lookup
     */
    public Object lookup()
    {
        return factory.lookup(this);
    }

    /**
     * When the caller is done with the instance, this method is called.  For
     * session scoped components, this gives you the opportunity to update
     * any state modified in the instance in a remote persistence store.
     * This method is not called when the object should be destroyed.
     * To get a destroy notification, you should register for the appropriate
     * events via the FlexContext.
     *
     * @param instance the instance returned via the lookup method for this
     * destination for this operation.
     */
    public void operationComplete(Object instance)
    {
    }
}
