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
package flex.messaging.services.remoting.adapters;

import flex.management.runtime.messaging.services.remoting.adapters.JavaAdapterControl;
import flex.messaging.FlexComponent;
import flex.messaging.Destination;
import flex.messaging.FactoryInstance;
import flex.messaging.FlexFactory;
import flex.messaging.MessageException;
import flex.messaging.config.ConfigMap;
import flex.messaging.config.ConfigurationConstants;
import flex.messaging.config.ConfigurationException;
import flex.messaging.config.SecurityConstraint;
import flex.messaging.messages.Message;
import flex.messaging.messages.RemotingMessage;
import flex.messaging.security.SecurityException;
import flex.messaging.services.ServiceAdapter;
import flex.messaging.services.remoting.RemotingDestination;
import flex.messaging.util.MethodMatcher;
import flex.messaging.util.MethodMatcher.Match;
import flex.messaging.util.ExceptionUtil;
import flex.messaging.util.StringUtils;

import flex.messaging.log.LogCategories;
import flex.messaging.log.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JavaAdapter extends ServiceAdapter
{
    static final String LOG_CATEGORY = LogCategories.MESSAGE_REMOTING;

    public static final String[] PROTECTED_PACKAGES = new String[]{"jrun", "jrunx", "macromedia",
                                                                   "flex", "flex2", "coldfusion",
                                                                   "allaire", "com.allaire", "com.macromedia"};

    private static final int REMOTING_METHOD_NULL_NAME_ERRMSG = 10658;
    private static final int REMOTING_METHOD_REFS_UNDEFINED_CONSTRAINT_ERRMSG = 10659;
    private static final int REMOTING_METHOD_NOT_DEFINED_ERRMSG = 10660;

    private static final String PROPERTY_INCLUDE_METHODS = "include-methods";
    private static final String PROPERTY_EXCLUDE_METHODS = "exclude-methods";
    private static final String METHOD_ELEMENT = "method";
    private static final String NAME_ELEMENT = "name";

    //--------------------------------------------------------------------------
    //
    // Constructor
    //
    //--------------------------------------------------------------------------

    /**
     * Constructs an unmanaged <code>JavaAdapter</code> instance.
     */
    public JavaAdapter()
    {
        this(false);
    }

    /**
     * Constructs a <code>JavaAdapter</code> instance.
     *
     * @param enableManagement <code>true</code> if the <code>JavaAdapter</code> has a
     * corresponding MBean control for management; otherwise <code>false</code>.
     */
    public JavaAdapter(boolean enableManagement)
    {
        super(enableManagement);
    }

    //--------------------------------------------------------------------------
    //
    // Variables
    //
    //--------------------------------------------------------------------------

    /**
     * The MBean control for this adapter.
     */
    private JavaAdapterControl controller;

    //--------------------------------------------------------------------------
    //
    // Properties
    //
    //--------------------------------------------------------------------------

    //----------------------------------
    //  destination
    //----------------------------------

    /**
     * Casts the <code>Destination</code> into <code>RemotingDestination</code>
     * and calls super.setDestination.
     *
     * @param destination remoting destination to associate with this adapter
     */
    @Override public void setDestination(Destination destination)
    {
        Destination dest = (RemotingDestination)destination;
        super.setDestination(dest);
    }

    //----------------------------------
    //  excludeMethods
    //----------------------------------

    private Map excludeMethods;

    /**
     * Returns an <tt>Iterator</tt> over the currently registered exclude methods.
     *
     * @return an <tt>Iterator</tt> over the currently registered exclude methods
     */
    public Iterator getExcludeMethodIterator()
    {
        return excludeMethods == null? Collections.EMPTY_LIST.iterator() : excludeMethods.values().iterator();
    }

    /**
     * Adds a method to the list of excluded methods for the adapter.
     * Invocations of excluded methods are blocked.
     *
     * @param value method to exclude
     */
    public void addExcludeMethod(RemotingMethod value)
    {
        String name = value.getName();
        if (name == null)
        {
            ConfigurationException ce = new ConfigurationException();
            ce.setMessage(REMOTING_METHOD_NULL_NAME_ERRMSG, new Object[] {getDestination().getId()});
            throw ce;
        }

        // Validate that a method with this name is defined on the source class.
        if (!isMethodDefinedBySource(name))
        {
            ConfigurationException ce = new ConfigurationException();
            ce.setMessage(REMOTING_METHOD_NOT_DEFINED_ERRMSG, new Object[] {name, getDestination().getId()});
            throw ce;
        }

        if (excludeMethods == null)
        {
            excludeMethods = new HashMap();
            excludeMethods.put(name, value);
        }
        else if (!excludeMethods.containsKey(name))
        {
            excludeMethods.put(name, value);
        }
    }

    /**
     * Removes a method from the list of excluded methods for the adapter.
     *
     * @param value method to remove from exlcuded methods list
     */
    public void removeExcludeMethod(RemotingMethod value)
    {
        excludeMethods.remove(value.getName());
    }

    //----------------------------------
    //  includeMethods
    //----------------------------------

    private Map includeMethods;

    /**
     * Returns an <tt>Iterator</tt> over the currently registered include methods.
     *
     * @return an <tt>Iterator</tt> over the currently registered include methods
     */
    public Iterator getIncludeMethodIterator()
    {
        return includeMethods == null? Collections.EMPTY_LIST.iterator() : includeMethods.values().iterator();
    }

    /**
     * Adds a method to the list of included methods for the adapter.
     * Invocations of included methods are allowed, and invocations of any non-included methods will be blocked.
     *
     * @param value method to include
     */
    public void addIncludeMethod(RemotingMethod value)
    {
        String name = value.getName();
        if (name == null)
        {
            ConfigurationException ce = new ConfigurationException();
            ce.setMessage(REMOTING_METHOD_NULL_NAME_ERRMSG, new Object[] {getDestination().getId()});
            throw ce;
        }

        // Validate that a method with this name is defined on the source class.
        if (!isMethodDefinedBySource(name))
        {
            ConfigurationException ce = new ConfigurationException();
            ce.setMessage(REMOTING_METHOD_NOT_DEFINED_ERRMSG, new Object[] {name, getDestination().getId()});
            throw ce;
        }

        if (includeMethods == null)
        {
            includeMethods = new HashMap();
            includeMethods.put(name, value);
        }
        else if (!includeMethods.containsKey(name))
        {
            includeMethods.put(name, value);
        }
    }

    /**
     * Removes a method from the list of included methods for the adapter.
     *
     * @param value method to remove from the included methods list
     */
    public void removeIncludeMethod(RemotingMethod value)
    {
        includeMethods.remove(value.getName());
    }

    //--------------------------------------------------------------------------
    //
    // Initialize, validate, start, and stop methods.
    //
    //--------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override public void initialize(String id, ConfigMap properties)
    {
        ConfigMap methodsToInclude = properties.getPropertyAsMap(PROPERTY_INCLUDE_METHODS, null);
        if (methodsToInclude != null)
        {
            List methods = methodsToInclude.getPropertyAsList(METHOD_ELEMENT, null);
            if ((methods != null) && !methods.isEmpty())
            {
                int n = methods.size();
                for (int i = 0; i < n; i++)
                {
                    ConfigMap methodSettings = (ConfigMap)methods.get(i);
                    String name = methodSettings.getPropertyAsString(NAME_ELEMENT, null);
                    RemotingMethod method = new RemotingMethod();
                    method.setName(name);
                    // Check for security constraint.
                    String constraintRef = methodSettings.getPropertyAsString(ConfigurationConstants.SECURITY_CONSTRAINT_ELEMENT, null);
                    if (constraintRef != null)
                    {
                        try
                        {
                            method.setSecurityConstraint(getDestination().getService().getMessageBroker().getSecurityConstraint(constraintRef));
                        }
                        catch (SecurityException se)
                        {
                            // Rethrow with a more descriptive message.
                            ConfigurationException ce = new ConfigurationException();
                            ce.setMessage(REMOTING_METHOD_REFS_UNDEFINED_CONSTRAINT_ERRMSG, new Object[] {name, getDestination().getId(), constraintRef});
                            throw ce;
                        }
                    }
                    addIncludeMethod(method);
                }
            }
        }
        ConfigMap methodsToExclude = properties.getPropertyAsMap(PROPERTY_EXCLUDE_METHODS, null);
        if (methodsToExclude != null)
        {
            // Warn that <exclude-properties> will be ignored.
            if (includeMethods != null)
            {
                RemotingDestination dest = (RemotingDestination)getDestination();
                if (Log.isWarn())
                    Log.getLogger(LogCategories.CONFIGURATION).warn("The remoting destination '" + dest.getId() + "' contains both <include-methods/> and <exclude-methods/> configuration. The <exclude-methods/> block will be ignored.");
            }
            // Excludes must be processed regardless of whether we add them or not to avoid 'Unused tags in <properties>' exceptions.
            List methods = methodsToExclude.getPropertyAsList(METHOD_ELEMENT, null);
            if ((methods != null) && !methods.isEmpty())
            {
                int n = methods.size();
                for (int i = 0; i < n; i++)
                {
                    ConfigMap methodSettings = (ConfigMap)methods.get(i);
                    String name = methodSettings.getPropertyAsString(NAME_ELEMENT, null);
                    RemotingMethod method = new RemotingMethod();
                    method.setName(name);
                    // Check for security constraint.
                    String constraintRef = methodSettings.getPropertyAsString(ConfigurationConstants.SECURITY_CONSTRAINT_ELEMENT, null);
                    // Conditionally add, only if include methods are not defined.
                    if (includeMethods == null)
                    {
                        if (constraintRef != null)
                        {
                            RemotingDestination dest = (RemotingDestination)getDestination();
                            if (Log.isWarn())
                                Log.getLogger(LogCategories.CONFIGURATION).warn("The method '" + name + "' for remoting destination '" + dest.getId() + "' is configured to use a security constraint, but security constraints are not applicable for excluded methods.");
                        }
                        addExcludeMethod(method);
                    }
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override public void start()
    {
        if (isStarted())
        {
            return;
        }
        super.start();
        validateInstanceSettings();

        RemotingDestination remotingDestination = (RemotingDestination) getDestination();
        if (FlexFactory.SCOPE_APPLICATION.equals(remotingDestination.getScope()))
        {
            FactoryInstance factoryInstance = remotingDestination.getFactoryInstance();
            createInstance(factoryInstance.getInstanceClass());
        }
    }

    //--------------------------------------------------------------------------
    //
    // Other public APIs
    //
    //--------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override public Object invoke(Message message)
    {
        RemotingDestination remotingDestination = (RemotingDestination)getDestination();
        RemotingMessage remotingMessage = (RemotingMessage)message;
        FactoryInstance factoryInstance = remotingDestination.getFactoryInstance();

        // We don't allow the client to specify the source for
        // Java based services.
        String className = factoryInstance.getSource();
        remotingMessage.setSource(className);

        String methodName = remotingMessage.getOperation();
        List parameters = remotingMessage.getParameters();
        Object result = null;

        try
        {
            // Test that the target method may be invoked based upon include/exclude method settings.
            validateAgainstMethodFilters(methodName);

            // Lookup and invoke.
            Object instance = createInstance(factoryInstance.getInstanceClass());
            if (instance == null)
            {
                MessageException me = new MessageException("Null instance returned from: " + factoryInstance);
                me.setCode("Server.Processing");
                throw me;
            }
            Class c = instance.getClass();

            MethodMatcher methodMatcher = remotingDestination.getMethodMatcher();
            Method method = methodMatcher.getMethod(c, methodName, parameters);
            result = method.invoke(instance, parameters.toArray());

            saveInstance(instance);
        }
        catch (InvocationTargetException ex)
        {
            /*
             * If the invocation exception wraps a message exception, unwrap it and
             * rethrow the nested message exception. Otherwise, build and throw a new
             * message exception.
             */
            Throwable cause = ex.getCause();
            if ((cause != null) && (cause instanceof MessageException))
            {
                throw (MessageException) cause;
            }
            else if (cause != null)
            {
                // Log a warning for this client's selector and continue
                if (Log.isError())
                {
                    Log.getLogger(LOG_CATEGORY).error("Error processing remote invocation: " +
                         cause.toString() + StringUtils.NEWLINE +
                         "  incomingMessage: " + message + StringUtils.NEWLINE +
                         ExceptionUtil.toString(cause));
                }
                MessageException me = new MessageException(cause.getClass().getName() + " : " + cause.getMessage());
                me.setCode("Server.Processing");
                me.setRootCause(cause);
                throw me;
            }
            else
            {
                MessageException me = new MessageException(ex.getMessage());
                me.setCode("Server.Processing");
                throw me;
            }
        }
        catch (IllegalAccessException ex)
        {
            MessageException me = new MessageException(ex.getMessage());
            me.setCode("Server.Processing");
            throw me;
        }

        return result;
    }

    //--------------------------------------------------------------------------
    //
    // Protected/private APIs
    //
    //--------------------------------------------------------------------------

    /**
     * Checks if the method is allowed to be invoked, i.e., if it has been
     * explicitly excluded, or if inclusions have been specified and it is not
     * on the inclusion list.
     *
     * @throw MessageException if method is not allowed.
     */
    protected void validateAgainstMethodFilters(String methodName)
    {
        if (includeMethods != null)
        {
            RemotingMethod method = (RemotingMethod)includeMethods.get(methodName);
            if (method == null)
                MethodMatcher.methodNotFound(methodName, null, new Match(null));

            // Check method-level security constraint, if defined.
            SecurityConstraint constraint = method.getSecurityConstraint();
            if (constraint != null)
                getDestination().getService().getMessageBroker().getLoginManager().checkConstraint(constraint);
        }
        else if ((excludeMethods != null) && excludeMethods.containsKey(methodName))
            MethodMatcher.methodNotFound(methodName, null, new Match(null));
    }

    /**
     * This method returns the instance of the given class.  You can override this in
     * your subclass to control how the instance is constructed.  Note that you can
     * can more general control how components are created by implementing the
     * flex.messaging.FlexFactory interface.
     *
     * @see flex.messaging.FlexFactory
     */
    protected Object createInstance(Class cl)
    {
        RemotingDestination remotingDestination = (RemotingDestination) getDestination();
        // Note: this breaks the admin console right now as we use this to call
        // mbean methods.  Might have performance impact as well?
        //assertAccess(cl.getName());
        FactoryInstance factoryInstance = remotingDestination.getFactoryInstance();
        Object instance = factoryInstance.lookup();
        if (isStarted() && instance instanceof FlexComponent
                && !((FlexComponent)instance).isStarted())
        {
            ((FlexComponent)instance).start();
        }
        return instance;
    }

    /**
     * This method is called by the adapter after the remote method has been invoked.
     * For session scoped components, by default FlexFactory provides an
     * operationComplete method to implement this operation.  For the JavaFactory,
     * this sets the attribute in the FlexSession to trigger sesison replication
     * for this attribute.
     */
    protected void saveInstance(Object instance)
    {
        RemotingDestination remotingDestination = (RemotingDestination) getDestination();
        FactoryInstance factoryInstance = remotingDestination.getFactoryInstance();
        factoryInstance.operationComplete(instance);
    }

    protected void assertAccess(String serviceClass)
    {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
        {
            // if there is a SecurityManager, check for specific access privileges on this class
            if (serviceClass.indexOf('.') != -1)
            {
                StringBuffer permissionData = new StringBuffer("accessClassInPackage.");
                permissionData.append(serviceClass.substring(0, serviceClass.lastIndexOf('.')));
                RuntimePermission perm = new RuntimePermission(permissionData.toString());
                AccessController.checkPermission(perm);
            }
        }
        else
        {
            // even without a SecurityManager, protect server packages
            for (int i = 0; i < PROTECTED_PACKAGES.length; i++)
            {
                if (serviceClass.startsWith(PROTECTED_PACKAGES[i]))
                {
                    StringBuffer permissionData = new StringBuffer("accessClassInPackage.");
                    permissionData.append(PROTECTED_PACKAGES[i].substring(0, PROTECTED_PACKAGES[i].length()));
                    RuntimePermission perm = new RuntimePermission(permissionData.toString());
                    AccessController.checkPermission(perm);
                }
            }
        }
    }

    protected void validateInstanceSettings()
    {
        RemotingDestination remotingDestination = (RemotingDestination) getDestination();
        // This will validate that we have a valid factory instance and accesses
        // any constructor properties needed for our factory so they do not give
        // startup warnings.
        remotingDestination.getFactoryInstance();
    }

    /**
     * Returns the log category of the <code>JavaAdapter</code>.
     *
     * @return The log category.
     */
    @Override protected String getLogCategory()
    {
        return LOG_CATEGORY;
    }

    /**
     * Invoked automatically to allow the <code>JavaAdapter</code> to setup its corresponding
     * MBean control.
     *
     * @param broker The <code>Destination</code> that manages this <code>JavaAdapter</code>.
     */
    @Override protected void setupAdapterControl(Destination destination)
    {
        controller = new JavaAdapterControl(this, destination.getControl());
        controller.register();
        setControl(controller);
    }

    /**
     * Tests whether the backing source class for this adapter defines a method with the specified name.
     *
     * @param methodName The method name.
     * @return <code>true</code> if the method is defined; otherwise <code>false</code>.
     */
    private boolean isMethodDefinedBySource(String methodName)
    {
        RemotingDestination remotingDestination = (RemotingDestination)getDestination();
        FactoryInstance factoryInstance = remotingDestination.getFactoryInstance();
        Class c = factoryInstance.getInstanceClass();
        if (c == null)
            return true; // No source class; ignore validation and generate an error at runtime.
        Method[] methods = c.getMethods();
        int n = methods.length;
        for (int i = 0; i < n; i++)
        {
            if (methods[i].getName().equals(methodName))
                return true;
        }
        return false;
    }
}
