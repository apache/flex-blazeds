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
import flex.messaging.log.Log;

public abstract class FactoryDestination extends Destination
{   
    private static final String FACTORY = "factory";       
    private static final String DEFAULT_FACTORY = "java";
    
    // Errors
    private static int INVALID_FACTORY = 11103;
    private static int FACTORY_CANNOT_BE_RETURNED = 11118;
    
    // FactoryDestination's properties 
    private FlexFactory factory;
    private String source;
    private String scope = FlexFactory.SCOPE_REQUEST;
    
    // FactoryDestination internal
    private String factoryId = DEFAULT_FACTORY;
    private FactoryInstance factoryInstance; 
    private ConfigMap factoryProperties;
    
    //--------------------------------------------------------------------------
    //
    // Constructor
    //
    //--------------------------------------------------------------------------
    
    /**
     * Constructs an unmanaged <code>FactoryDestination</code> instance.
     */
    public FactoryDestination()
    {
        this(false);
    }
    
    /**
     * Constructs a <code>FactoryDestination</code> with the indicated management.
     * 
     * @param enableManagement <code>true</code> if the <code>FactoryDestination</code>
     * is manageable; otherwise <code>false</code>.
     */
    public FactoryDestination(boolean enableManagement)
    {
        super(enableManagement);
    }
    
    //--------------------------------------------------------------------------
    //
    // Initialize, validate, start, and stop methods. 
    //
    //--------------------------------------------------------------------------
    
    /**
     * Initializes the <code>FactoryDestination</code> with the properties. 
     * @param id the factory id
     * @param properties Properties for the <code>FactoryDestination</code>.
     */
    public void initialize(String id, ConfigMap properties)
    {  
        super.initialize(id, properties);
        
        if (properties == null || properties.size() == 0)
            return;

        // Need to cache this for later. TODO: We shouldn't need to do this.
        factoryProperties = properties;

        factoryId = properties.getPropertyAsString(FACTORY, factoryId);        
        scope = properties.getPropertyAsString(FlexFactory.SCOPE, scope);
        source = properties.getPropertyAsString(FlexFactory.SOURCE, source);

        if (source == null)
            source = getId();
        
        if (factory != null)
            factory.initialize(getId(), factoryProperties);
    }

    /**
     * Verifies that the <code>FactoryDestination</code> is in valid state before
     * it is started.
     */
    protected void validate()
    {               
        if (isValid())
            return; 
       
        super.validate();

        if (factory == null)
        {
            if (factoryId == null)
            {
                factoryId = DEFAULT_FACTORY;
            }
            MessageBroker broker = getService().getMessageBroker();
            FlexFactory f = broker.getFactory(factoryId);
            if (f == null)
            {
                ConfigurationException ex = new ConfigurationException();
                ex.setMessage(INVALID_FACTORY, new Object[] {getId(), factoryId});
                throw ex;
            }
            factory = f;
        }
        
        if (scope == null)
            scope = FlexFactory.SCOPE_REQUEST;
        
        if (source == null)        
            source = getId();
    }

    //--------------------------------------------------------------------------
    //
    // Public Getters and Setters for Destination properties
    //         
    //--------------------------------------------------------------------------    
    
    /**
     * Returns the factory of the <code>FactoryDestination</code>. Before a valid
     * <code>FlexFactory</code> can be returned, <code>MessageBroker</code> and
     * hence <code>Service</code> of the <code>Destination</code> has to be set.
     * @return FlexFactory the FlexFactory object
     */
    public FlexFactory getFactory()
    {
        if (factory == null)
        {
            if (factoryId == null)
            {
                factoryId = DEFAULT_FACTORY;
            }
            if (getService() == null)
            {
                // Factory cannot be returned without ''{0}'' set.
                ConfigurationException ex = new ConfigurationException();
                ex.setMessage(FACTORY_CANNOT_BE_RETURNED, new Object[] {"Service"});
                throw ex;
            }
            if (getService().getMessageBroker() == null)
            {
                // Factory cannot be returned without ''{0}'' set.
                ConfigurationException ex = new ConfigurationException();
                ex.setMessage(FACTORY_CANNOT_BE_RETURNED, new Object[] {"MessageBroker"});
                throw ex;
            }
            MessageBroker broker = getService().getMessageBroker();
            FlexFactory f = broker.getFactory(factoryId);
            if (f == null)
            {
                ConfigurationException ex = new ConfigurationException();
                ex.setMessage(INVALID_FACTORY, new Object[] {getId(), factoryId});
                throw ex;
            }
            factory = f;
        }
        return factory;
    }

    /**
     * Sets the factory of the <code>FactoryDestination</code>. 
     * <code>MessageBroker</code> has to know the factory before it can be
     * assigned to the destination. 
     * 
     * @param id The id of the factory.
     */    
    public void setFactory(String id)
    {        
        if (isStarted())
        {
            MessageBroker broker = getService().getMessageBroker();
            FlexFactory factory = broker.getFactory(id);
            if (factory == null)
            {
                ConfigurationException ex = new ConfigurationException();
                ex.setMessage(INVALID_FACTORY, new Object[] {getId(), factory});
                throw ex;
            }
            setFactory(factory);
        }
        factoryId = id;
    }
    
    /**
     * Sets the factory of the <code>FactoryDestination</code>. 
     * 
     * @param factory the FlexFactory object
     */
    public void setFactory(FlexFactory factory)
    {
        this.factory = factory;
    }
    
    /**
     * Returns the <code>FactoryInstance</code>. <code>FactoryInstance</code> 
     * stores configuration state used for retrieving an instance from
     * the factory. This needs to be called after calling <code>setSource</code>
     * and <code>setScope</code> methods.
     * @return FactoryInstance current FactoryInstance object 
     */
    public FactoryInstance getFactoryInstance()
    {
        // This is needed for HibernateAssembler
        return getFactoryInstance(factoryProperties);
    }
    
    /**
     * Returns a <code>FactoryInstance</code> using the properties passed in.
     * 
     * @param properties Properties to be used while creating the <code>FactoryInstance</code>. 
     */
    private FactoryInstance getFactoryInstance(ConfigMap properties)
    {
        // Automatically create a factory instance if not already set  
        if (factoryInstance == null)
            factoryInstance = createFactoryInstance(properties);

        return factoryInstance;
    }

    /**
     * Creates a factory instance using the properties passed in.
     * 
     * @param properties Properties to be used while creating the <code>FactoryInstance</code>. 
     */
    private FactoryInstance createFactoryInstance(ConfigMap properties)
    {   
        if (properties == null)
            properties = new ConfigMap();
        
        properties.put(FlexFactory.SOURCE, source);
        properties.put(FlexFactory.SCOPE, scope);
        FactoryInstance factoryInstance = getFactory().createFactoryInstance(getId(), properties);
        return factoryInstance;
    }
    
    /**
     * Returns the scope of the <code>FactoryDestination</code>.
     * 
     * @return scope of the <code>FactoryDestination</code>.
     */
    public String getScope()
    {
        return scope;
    }

    /**
     * Sets the scope of the <code>FactoryDestination</code> that is used
     * in <code>FactoryInstance</code> creation. Scope cannot be changed to and
     * from application scope once <code>FactoryInstance</code> is initialized.
     * 
     * @param scope the scope
     */
    public void setScope(String scope)
    {        
        if (factoryInstance != null)
        {
            if (FlexFactory.SCOPE_APPLICATION.equals(this.scope) 
                    && !FlexFactory.SCOPE_APPLICATION.equals(scope))
            {
                if (Log.isWarn())
                    Log.getLogger(getLogCategory()).warn(
                            "Current scope is "+FlexFactory.SCOPE_APPLICATION
                            +" and it cannot be changed to "+scope
                            +" once factory instance is initialized.");
                return;
            }
            else if (!FlexFactory.SCOPE_APPLICATION.equals(this.scope) 
                        && FlexFactory.SCOPE_APPLICATION.equals(scope))
            {
                if (Log.isWarn())
                    Log.getLogger(getLogCategory()).warn(
                            "Current scope is "+this.scope
                            +" and it cannot be changed to "+FlexFactory.SCOPE_APPLICATION
                            +" once factory instance is initialized.");
                return;
            }
            factoryInstance.setScope(scope);
        }
        this.scope = scope;
    }
    
    /**
     * Gets the source of the <code>FactoryDestination</code>. 
     * 
     * @return the source of the <code>FactoryDestination</code>. 
     */
    public String getSource()
    {
        return source;
    }

    /**
     * Sets the source of the <code>FactoryDestination</code> that is used
     * in <code>FactoryInstance</code> creation. Source cannot be changed once  
     * <code>FactoryInstance</code> is initialized and the scope is application.
     * 
     * @param source the source string
     */
    public void setSource(String source)
    {   
        if (factoryInstance != null)     
        {
            if (FlexFactory.SCOPE_APPLICATION.equals(scope))
            {
                if (Log.isWarn())
                    Log.getLogger(getLogCategory()).warn(
                            "Source of the destination cannot be changed once "
                            + "factory instance is already initialized and it has "
                            + FlexFactory.SCOPE_APPLICATION +" scope");
                return;
            }            
            factoryInstance.setSource(source);
        }
        this.source = source;
    }   
    
    /**
     * This method first calls stop on its superclass <code>Destination</code> and then
     * removes any assemblers from the ServletContext or Session that are ready for removal.
     * If an assembler is only used by a single destination (attribute-id==destination-id) then
     * it is removed.  If an assembler is shared across destinations, (attribute-id&lt;&gt;destination-id)
     * then it is only removed if its reference count (maintained in <code>MessageBroker</code>) is
     * down to zero
     */
    public void stop()
    {
        if (isStarted())
        {
            super.stop();
            // destroy factory instance to free up resources
            if (factory != null && (factory instanceof DestructibleFlexFactory))
                ((DestructibleFlexFactory)factory).destroyFactoryInstance(factoryInstance);    
        }
        else
        {
            super.stop();
        }
    }    
}
