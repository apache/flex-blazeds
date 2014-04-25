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
package flex.management.jmx;

import flex.management.BaseControl;
import flex.management.MBeanServerLocatorFactory;
import flex.management.ManagementException;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ReflectionException;
import javax.management.RuntimeOperationsException;

/**
 * Remoting gateway to the MBean server that hosts Flex MBeans.
 * <p>
 * Some base javax.management.MBeanServer methods are unimplemented due to the
 * fact that we're interacting with the MBean server from remote Flash clients.
 * Some methods have been modified to better suite remote Flash clients. Other
 * methods are additive, serving as a convenience for Flex applications.
 * </p><p>
 * Unimplemented methods from the base MBeanServer API:
 * <ul>
 *   <li>getDomains() - JMX 1.2</li>
 *   <li>addNotificationListener()/removeNotificationListener() - Flash objects
 *       cannot listen directly for MBean notifications.</li>
 *   <li>instantiate() - returns a reference to a Java object that is not useful
 *       to a remote Flash client.</li>
 *   <li>deserialize() - deprecated.</li>
 *   <li>getClassLoaderFor() - meaningless to a Flash client.</li>
 *   <li>getClassLoader() - meaningless to a Flash client.</li>
 *   <li>getClassLoaderRepository() - meaningless to a Flash client.</li>
 * </ul>
 * </p><p>
 * Modifications to the base MBeanServer API:
 * <ul>
 *   <li>* All ObjectName arguments are typed as String because serialization in either
 *       direction doesn't support ObjectNames that are patterns. This does not effect
 *       ObjectNames that are not patterns that are returned to the client.</li>
 *   <li>queryMBeans() - returns an Array of ObjectInstances rather than a java.util.Set
 *       and does not currently support the QueryExp argument.</li>
 *   <li>queryNames() returns an Array of ObjectNames rather than a java.util.Set
 *       and does not currently support the QueryExp argument.</li>
 *   <li>getAttributes() returns an Array of Attributes rather than an AttributeList.</li>
 *   <li>setAttributes() accepts and returns Arrays of Attributes rather than AttributeLists.</li>
 * </ul>
 * </p><p>
 * Additonal Flex-specific methods:
 * <ul>
 *   <li>getFlexMBeanCount()</li>
 *   <li>getFlexDomains()</li>
 *   <li>getFlexMBeanObjectNames()</li>
 * </ul>
 * </p>
 *
 * @author shodgson
 */
public class MBeanServerGateway
{
    // Error string constants.
    private static final int MALFORMED_OBJECTNAME = 10400;
    private static final int GETINFO_INTROSPECTION_ERR = 10406;
    private static final int MBEAN_NOTFOUND = 10407;
    private static final int GETINFO_REFLECT_ERR = 10408;
    private static final int ATTRIB_NOTFOUND = 10409;
    private static final int GETATTRIB_EXCEPTION = 10410;
    private static final int GETATTRIB_REFLECT_ERR = 10411;
    private static final int GETATTRIB_NULL_ARGUMENT = 10412;
    private static final int GETATTRIBS_REFLECT_ERR = 10413;
    private static final int GETATTRIBS_NULL_ARGUMENT = 10414;
    private static final int INVOKE_REFLECT_ERR = 10415;
    private static final int INVOKE_ERR = 10416;
    private static final int CREATE_ERR = 10417;
    private static final int INSTANCE_EXISTS = 10418;
    private static final int NOT_COMPLIANT = 10419;
    private static final int MBEAN_PREREG_ERR = 10420;
    private static final int MBEAN_PREDEREG_ERR = 10421;
    private static final int SETATTRIB_REFLECT_ERR = 10422;
    private static final int SETATTRIB_EXCEPTION = 10423;
    private static final int INVALID_ATTRIB_VALUE = 10424;
    private static final int SETATTRIBS_REFLECT_ERR = 10425;
    
    private MBeanServer server;

    /**
     * Constructs a new MBeanServerGateway. The gateway exposes the MBean server
     * that Flex MBean are registered with in a remoting-friendly fashion.
     */
    public MBeanServerGateway()
    {
        server = MBeanServerLocatorFactory.getMBeanServerLocator().getMBeanServer();
    }

    /////////////////////////////////////
    //
    // Core MBeanServer API
    //
    /////////////////////////////////////

    /**
     * Instantiates and registers an MBean with the MBean server.
     *
     * @param className The class name for the MBean to instantiate.
     * @param objectName The object name of the MBean.
     * @return An ObjectInstance containing the ObjectName and Java class name of the new MBean.
     */
    public ObjectInstance createMBean(String className, String objectName)
    {
        javax.management.ObjectName name = null;
        if (objectName != null)
            name = validateObjectName(objectName);
        try
        {
            return new ObjectInstance(server.createMBean(className, name));
        }
        catch (ReflectionException re)
        {
            ManagementException me = new ManagementException();
            me.setMessage(CREATE_ERR, new Object[] {name});
            me.setRootCause(re);
            throw me;
        }
        catch (InstanceAlreadyExistsException iaee)
        {
            ManagementException me = new ManagementException();
            me.setMessage(INSTANCE_EXISTS, new Object[] {name});
            me.setRootCause(iaee);
            throw me;
        }
        catch (MBeanException mbe)
        {
            ManagementException me = new ManagementException();
            me.setMessage(CREATE_ERR, new Object[] {name});
            me.setRootCause(mbe);
            throw me;
        }
        catch (NotCompliantMBeanException ncmbe)
        {
            ManagementException me = new ManagementException();
            me.setMessage(NOT_COMPLIANT, new Object[] {className});
            me.setRootCause(ncmbe);
            throw me;
        }
    }

    /**
     * Instantiates and registers an MBean with the MBean server. The class loader
     * to use to load the MBean class is identified by its ObjectName.
     *
     * @param className The class name for the MBean to instantiate.
     * @param objectName The object name of the MBean.
     * @param loaderName The object name of the desired class loader.
     * @return An ObjectInstance containing the ObjectName and Java class name of the new MBean.
     */
    public ObjectInstance createMBean(String className, String objectName, String loaderName)
    {
        javax.management.ObjectName name = null;
        javax.management.ObjectName loader = null;
        if (objectName != null)
            name = validateObjectName(objectName);
        if (loaderName != null)
            loader = validateObjectName(loaderName);
        try
        {
            return new ObjectInstance(server.createMBean(className, name, loader));
        }
        catch (ReflectionException re)
        {
            ManagementException me = new ManagementException();
            me.setMessage(CREATE_ERR, new Object[] {name});
            me.setRootCause(re);
            throw me;
        }
        catch (InstanceAlreadyExistsException iaee)
        {
            ManagementException me = new ManagementException();
            me.setMessage(INSTANCE_EXISTS, new Object[] {name});
            me.setRootCause(iaee);
            throw me;
        }
        catch (MBeanException mbe)
        {
            ManagementException me = new ManagementException();
            me.setMessage(CREATE_ERR, new Object[] {name});
            me.setRootCause(mbe);
            throw me;
        }
        catch (NotCompliantMBeanException ncmbe)
        {
            ManagementException me = new ManagementException();
            me.setMessage(NOT_COMPLIANT, new Object[] {className});
            me.setRootCause(ncmbe);
            throw me;
        }
        catch (InstanceNotFoundException infe)
        {
            ManagementException me = new ManagementException();
            me.setMessage(MBEAN_NOTFOUND, new Object[] {name});
            me.setRootCause(infe);
            throw me;
        }
    }

    /**
     * Instantiates and registers an MBean with the MBean server.
     *
     * @param className The class name for the MBean to instantiate.
     * @param objectName The object name of the MBean.
     * @param params An array of parameters to pass to the MBean constructor.
     * @param signature An array containing the type signature for the constructor to invoke.
     * @return An ObjectInstance containing the ObjectName and Java class name of the new MBean.
     */
    public ObjectInstance createMBean(String className, String objectName, Object[] params, String[] signature)
    {
        javax.management.ObjectName name = null;
        if (objectName != null)
            name = validateObjectName(objectName);
        try
        {
            return new ObjectInstance(server.createMBean(className, name, params, signature));
        }
        catch (ReflectionException re)
        {
            ManagementException me = new ManagementException();
            me.setMessage(CREATE_ERR, new Object[] {name});
            me.setRootCause(re);
            throw me;
        }
        catch (InstanceAlreadyExistsException iaee)
        {
            ManagementException me = new ManagementException();
            me.setMessage(INSTANCE_EXISTS, new Object[] {name});
            me.setRootCause(iaee);
            throw me;
        }
        catch (MBeanException mbe)
        {
            ManagementException me = new ManagementException();
            me.setMessage(CREATE_ERR, new Object[] {name});
            me.setRootCause(mbe);
            throw me;
        }
        catch (NotCompliantMBeanException ncmbe)
        {
            ManagementException me = new ManagementException();
            me.setMessage(NOT_COMPLIANT, new Object[] {className});
            me.setRootCause(ncmbe);
            throw me;
        }
    }

    /**
     * Instantiates and registers an MBean with the MBean server. The class loader
     * to use to load the MBean class is identified by its ObjectName.
     *
     * @param className The class name for the MBean to instantiate.
     * @param objectName The object name of the MBean.
     * @param loaderName The object name of the desired class loader.
     * @param params An array of parameters to pass to the MBean constructor.
     * @param signature An array containing the type signature for the constructor to invoke.
     * @return An ObjectInstance containing the ObjectName and Java class name of the new MBean.
     */
    public ObjectInstance createMBean(String className, String objectName, String loaderName, Object[] params, String[] signature)
    {
        javax.management.ObjectName name = null;
        javax.management.ObjectName loader = null;
        if (objectName != null)
            name = validateObjectName(objectName);
        if (loaderName != null)
            loader = validateObjectName(loaderName);
        try
        {
            return new ObjectInstance(server.createMBean(className, name, loader, params, signature));
        }
        catch (ReflectionException re)
        {
            ManagementException me = new ManagementException();
            me.setMessage(CREATE_ERR, new Object[] {name});
            me.setRootCause(re);
            throw me;
        }
        catch (InstanceAlreadyExistsException iaee)
        {
            ManagementException me = new ManagementException();
            me.setMessage(INSTANCE_EXISTS, new Object[] {name});
            me.setRootCause(iaee);
            throw me;
        }
        catch (MBeanException mbe)
        {
            ManagementException me = new ManagementException();
            me.setMessage(CREATE_ERR, new Object[] {name});
            me.setRootCause(mbe);
            throw me;
        }
        catch (NotCompliantMBeanException ncmbe)
        {
            ManagementException me = new ManagementException();
            me.setMessage(NOT_COMPLIANT, new Object[] {className});
            me.setRootCause(ncmbe);
            throw me;
        }
        catch (InstanceNotFoundException infe)
        {
            ManagementException me = new ManagementException();
            me.setMessage(MBEAN_NOTFOUND, new Object[] {name});
            me.setRootCause(infe);
            throw me;
        }
    }

    /**
     * Registers a pre-existing object as an MBean with the MBean server.
     *
     * @param object The object to register as an MBean.
     * @param objectName The object name for the MBean.
     * @return An ObjectInstance containing the ObjectName and Java class name of the new MBean.
     */
    public ObjectInstance registerMBean(Object object, String objectName)
    {
        javax.management.ObjectName name = null;
        if (objectName != null)
            name = validateObjectName(objectName);
        try
        {
            return new ObjectInstance(server.registerMBean(object, name));
        }
        catch (InstanceAlreadyExistsException iaee)
        {
            ManagementException me = new ManagementException();
            me.setMessage(INSTANCE_EXISTS, new Object[] {name});
            me.setRootCause(iaee);
            throw me;
        }
        catch (NotCompliantMBeanException ncmbe)
        {
            ManagementException me = new ManagementException();
            me.setMessage(NOT_COMPLIANT, new Object[] {object.getClass().getName()});
            me.setRootCause(ncmbe);
            throw me;
        }
        catch (MBeanRegistrationException mbre)
        {
            ManagementException me = new ManagementException();
            me.setMessage(MBEAN_PREREG_ERR, new Object[] {name});
            me.setRootCause(mbre);
            throw me;
        }
    }

    /**
     * Unregisters an MBean from the MBean server.
     *
     * @param objectName The object name of the MBean to unregister.
     */
    public void unregisterMBean(String objectName)
    {
        javax.management.ObjectName name = validateObjectName(objectName);
        try
        {
            server.unregisterMBean(name);
        }
        catch (InstanceNotFoundException infe)
        {
            ManagementException me = new ManagementException();
            me.setMessage(MBEAN_NOTFOUND, new Object[] {name});
            me.setRootCause(infe);
            throw me;
        }
        catch (MBeanRegistrationException mbre)
        {
            ManagementException me = new ManagementException();
            me.setMessage(MBEAN_PREDEREG_ERR, new Object[] {name});
            me.setRootCause(mbre);
            throw me;
        }
    }

    /**
     * Gets the ObjectInstance for the specified MBean registered with the
     * MBean server.
     *
     * @param objectName The object name of the MBean.
     * @return An ObjectInstance containing the ObjectName and Java class name of the MBean.
     */
    public ObjectInstance getObjectInstance(String objectName)
    {
        javax.management.ObjectName name = validateObjectName(objectName);
        try
        {
            return new ObjectInstance(server.getObjectInstance(name));
        }
        catch (InstanceNotFoundException infe)
        {
            ManagementException me = new ManagementException();
            me.setMessage(MBEAN_NOTFOUND, new Object[] {name});
            me.setRootCause(infe);
            throw me;
        }
    }

    /**
     * Gets MBeans controlled by the MBean server. This method allows the following to be obtained:
     * All MBeans, or a set of MBeans specified by pattern matching on the ObjectName, or a specific
     * MBean.
     * <p>
     * This method does not support a QueryExp argument for additional filtering of the queried set.
     * </p>
     *
     * @param objectName The object name pattern identifying the MBeans to retrieve.
     * @return A set of ObjectInstances for the selected MBeans.
     */
    public ObjectInstance[] queryMBeans(String objectName)
    {
        javax.management.ObjectName name = validateObjectName(objectName);
        Set result = server.queryMBeans(name, null);
        int n = result.size();
        if (n > 0)
        {
            ObjectInstance[] toReturn = new ObjectInstance[n];
            int i = 0;
            for (Iterator iter = result.iterator(); iter.hasNext();) 
            {
                toReturn[i++] = new ObjectInstance((javax.management.ObjectInstance)iter.next());
            }
            return toReturn;
        }
        else
        {
            return new ObjectInstance[0];
        }
    }

    /**
     * Gets the names of MBeans controlled by the MBean server. This method allows the following to be
     * obtained: The names of all MBeans, the names of the set of MBeans matching the ObjectName pattern,
     * a specific MBean name.
     * <p>
     * This method does not support a QueryExp argument for additional filtering of the queried set.
     * </p>
     *
     * @param objectName The object name pattern identifying the MBean names to retrieve.
     * @return A set of ObjectNames for the selected MBeans.
     */
    public ObjectName[] queryNames(String objectName)
    {
        javax.management.ObjectName name = validateObjectName(objectName);
        Set result = server.queryNames(name, null);
        int n = result.size();
        if (n > 0)
        {
            ObjectName[] toReturn = new ObjectName[n];
            int i = 0;
            for (Iterator iter = result.iterator(); iter.hasNext();)
            {
                toReturn[i++] = new ObjectName((javax.management.ObjectName)iter.next());
            }
            return toReturn;
        }
        else
        {
            return new ObjectName[0];
        }
    }

    /**
     * Checks whether an MBean, identified by its object name, is already registered with the MBean server.
     *
     * @param objectName The object name of the MBean to be checked.
     * @return True if the MBean is already registered in the MBean server, false otherwise.
     */
    public boolean isRegistered(String objectName)
    {
        javax.management.ObjectName name = validateObjectName(objectName);
        return server.isRegistered(name);
    }

    /**
     * Returns the total number of beans registered in the MBean server.
     *
     * @return The number of registered MBeans.
     */
    public Integer getMBeanCount()
    {
        return server.getMBeanCount();
    }

    /**
     * Gets the value of a specific attribute of a named MBean. The MBean is identified by its object name.
     *
     * @param objectName The object name of the MBean from which the attribute is to be retrieved.
     * @param attribute The name of the attribute to be retrieved.
     * @return The value of the retrieved attribute.
     */
    public Object getAttribute(String objectName, String attribute)
    {
        javax.management.ObjectName name = validateObjectName(objectName);
        try
        {
            return server.getAttribute(name, attribute);
        }
        catch (AttributeNotFoundException anfe)
        {
            ManagementException me = new ManagementException();
            me.setMessage(ATTRIB_NOTFOUND, new Object[] {attribute, name});
            me.setRootCause(anfe);
            throw me;
        }
        catch (MBeanException mbe)
        {
            ManagementException me = new ManagementException();
            me.setMessage(GETATTRIB_EXCEPTION, new Object[] {attribute, name});
            me.setRootCause(mbe);
            throw me;
        }
        catch (ReflectionException re)
        {
            ManagementException me = new ManagementException();
            me.setMessage(GETATTRIB_REFLECT_ERR, new Object[] {attribute, name});
            me.setRootCause(re);
            throw me;
        }
        catch (InstanceNotFoundException infe)
        {
            ManagementException me = new ManagementException();
            me.setMessage(MBEAN_NOTFOUND, new Object[] {name});
            me.setRootCause(infe);
            throw me;
        }
        catch (RuntimeOperationsException roe)
        {
            ManagementException me = new ManagementException();
            me.setMessage(GETATTRIB_NULL_ARGUMENT);
            me.setRootCause(roe);
            throw me;
        }
    }

    /**
     * Gets the values of several attributes of a named MBean.
     *
     * @param objectName The object name of the MBean to get attribute values from.
     * @param attributes The names of the attributes to get values for.
     * @return The attributes, each containing their name and value.
     */
    public Attribute[] getAttributes(String objectName, String[] attributes)
    {
        javax.management.ObjectName name = validateObjectName(objectName);
        try
        {
            AttributeList result = server.getAttributes(name, attributes);
            Attribute[] values = new Attribute[result.size()];
            for (int i = 0; i < result.size(); i++)
            {
                values[i] = new Attribute((javax.management.Attribute)result.get(i));
            }
            return values;
        }
        catch (ReflectionException re)
        {
            ManagementException me = new ManagementException();
            me.setMessage(GETATTRIBS_REFLECT_ERR, new Object[] {name});
            me.setRootCause(re);
            throw me;
        }
        catch (InstanceNotFoundException infe)
        {
            ManagementException me = new ManagementException();
            me.setMessage(MBEAN_NOTFOUND, new Object[] {name});
            me.setRootCause(infe);
            throw me;
        }
        catch (RuntimeOperationsException roe)
        {
            ManagementException me = new ManagementException();
            me.setMessage(GETATTRIBS_NULL_ARGUMENT);
            me.setRootCause(roe);
            throw me;
        }
    }

    /**
     * Sets the value of the attribute for the specified MBean.
     *
     * @param objectName The name of the MBean.
     * @param attribute The attribute to set.
     */
    public void setAttribute(String objectName, Attribute attribute)
    {
        javax.management.ObjectName name = validateObjectName(objectName);
        javax.management.Attribute attrib = validateAttribute(attribute);
        try
        {
            server.setAttribute(name, attrib);
        }
        catch (ReflectionException re)
        {
            ManagementException me = new ManagementException();
            me.setMessage(SETATTRIB_REFLECT_ERR, new Object[] {attrib.getName(), name});
            me.setRootCause(re);
            throw me;
        }
        catch (InstanceNotFoundException infe)
        {
            ManagementException me = new ManagementException();
            me.setMessage(MBEAN_NOTFOUND, new Object[] {name});
            me.setRootCause(infe);
            throw me;
        }
        catch (AttributeNotFoundException anfe)
        {
            ManagementException me = new ManagementException();
            me.setMessage(ATTRIB_NOTFOUND, new Object[] {attrib.getName(), name});
            me.setRootCause(anfe);
            throw me;
        }
        catch (MBeanException mbe)
        {
            ManagementException me = new ManagementException();
            me.setMessage(SETATTRIB_EXCEPTION, new Object[] {attrib.getName(), name});
            me.setRootCause(mbe);
            throw me;
        }
        catch (InvalidAttributeValueException iave)
        {
            ManagementException me = new ManagementException();
            me.setMessage(INVALID_ATTRIB_VALUE, new Object[] {attrib.getValue(), attrib.getName(), name});
            me.setRootCause(iave);
            throw me;
        }
    }

    /**
     * Sets the values for several attributes of the specified MBean.
     *
     * @param objectName The object name for the MBean.
     * @param attributes The attributes to set.
     * @return The attributes that were set with their new values.
     */
    public Attribute[] setAttributes(String objectName, Attribute[] attributes)
    {
        javax.management.ObjectName name = validateObjectName(objectName);
        AttributeList attribList = new AttributeList();
        for (int i = 0; i < attributes.length; i++)
        {
            attribList.add(attributes[i].toAttribute());
        }
        try
        {
            AttributeList result = server.setAttributes(name, attribList);
            Attribute[] values = new Attribute[result.size()];
            for (int i = 0; i < result.size(); i++)
            {
                values[i] = new Attribute((javax.management.Attribute)result.get(i));
            }
            return values;
        }
        catch (InstanceNotFoundException infe)
        {
            ManagementException me = new ManagementException();
            me.setMessage(MBEAN_NOTFOUND, new Object[] {name});
            me.setRootCause(infe);
            throw me;
        }
        catch (ReflectionException re)
        {
            ManagementException me = new ManagementException();
            me.setMessage(SETATTRIBS_REFLECT_ERR, new Object[] {name});
            me.setRootCause(re);
            throw me;
        }
    }

    /**
     * Invokes an operation on an MBean.
     *
     * @param objectName The object name of the MBean to invoke the operation on.
     * @param operationName The operation to invoke.
     * @param params The parameters for the operation invocation.
     * @param signature The parameter signature for the operation.
     * @return The object returned by the operation invocation.
     */
    public Object invoke(String objectName, String operationName, Object[] params, String[] signature)
    {
        javax.management.ObjectName name = validateObjectName(objectName);
        try
        {
            return server.invoke(name, operationName, params, signature);
        }
        catch (ReflectionException re)
        {
            ManagementException me = new ManagementException();
            me.setMessage(INVOKE_REFLECT_ERR, new Object[]  {operationName, objectName});
            me.setRootCause(re);
            throw me;
        }
        catch (InstanceNotFoundException infe)
        {
            ManagementException me = new ManagementException();
            me.setMessage(MBEAN_NOTFOUND, new Object[] {objectName});
            me.setRootCause(infe);
            throw me;
        }
        catch (MBeanException mbe)
        {
            ManagementException me = new ManagementException();
            me.setMessage(INVOKE_ERR, new Object[]  {operationName, objectName});
            me.setRootCause(mbe);
            throw me;
        }
    }

    /**
     * Returns the default domain used for naming MBeans.
     *
     * @return The default domain.
     */
    public String getDefaultDomain()
    {
        return server.getDefaultDomain();
    }

    /**
     * This method discovers the attributes and operations that an MBean exposes for management
     * by a Flash client.
     *
     * @param objectName The name of the MBean to get metadata for.
     * @return An MBeanInfo instance that describes the MBean.
     */
    public flex.management.jmx.MBeanInfo getMBeanInfo(String objectName)
    {
        javax.management.ObjectName name = validateObjectName(objectName);
        try
        {
            return new MBeanInfo(server.getMBeanInfo(name));
        }
        catch (IntrospectionException ie)
        {
            ManagementException me = new ManagementException();
            me.setMessage(GETINFO_INTROSPECTION_ERR, new Object[] {objectName});
            me.setRootCause(ie);
            throw me;
        }
        catch (InstanceNotFoundException infe)
        {
            ManagementException me = new ManagementException();
            me.setMessage(MBEAN_NOTFOUND, new Object[] {objectName});
            me.setRootCause(infe);
            throw me;
        }
        catch (ReflectionException re)
        {
            ManagementException me = new ManagementException();
            me.setMessage(GETINFO_REFLECT_ERR, new Object[] {objectName});
            me.setRootCause(re);
            throw me;
        }
    }


    /**
     * Returns true if the specified MBean is an instance of the specified class; otherwise false.
     *
     * @param objectName The object name of the MBean.
     * @param className The name of the class.
     * @return true if the specified MBean is an instance of the specified class; otherwise false.
     */
    public boolean isInstanceOf(String objectName, String className)
    {
        javax.management.ObjectName name = validateObjectName(objectName);
        try
        {
            return server.isInstanceOf(name, className);
        }
        catch (InstanceNotFoundException infe)
        {
            ManagementException me = new ManagementException();
            me.setMessage(MBEAN_NOTFOUND, new Object[] {objectName});
            me.setRootCause(infe);
            throw me;
        }
    }

    ////////////////////////////////
    //
    // Additive Flex-specific API
    //
    ////////////////////////////////

    /**
     * Returns all the object names for Flex related MBeans.
     *
     * @return The object names for all Flex related MBeans.
     */
    public ObjectName[] getFlexMBeanObjectNames()
    {
        javax.management.ObjectName pattern = validateObjectName(BaseControl.DOMAIN_PREFIX + "*:*");
        Set result = server.queryNames(pattern, null);
        ObjectName[] names = new ObjectName[result.size()];
        int i = 0;
        for (Iterator iter = result.iterator(); iter.hasNext(); )
        {
            names[i++] = new ObjectName((javax.management.ObjectName)iter.next());
        }
        return names;
    }

    /**
     * Returns the number of narrowed Flex MBeans registered in the MBean server.
     *
     * @return The number of narrowed Flex MBeans registered in the MBean server.
     */
    public Integer getFlexMBeanCount()
    {
        return new Integer(getFlexMBeanObjectNames().length);
    }

    /**
     * Returns the narrowed list of Flex domains in which any MBean is currently registered.
     * The domains are returned in naturally sorted order.
     *
     * @return The narrowed list of Flex domains in which any MBean is currently registered.
     */
    public String[] getFlexDomains()
    {
        ObjectName[] names = getFlexMBeanObjectNames();
        Set domains = new TreeSet();
        String name;
        String domain;
        if (names.length > 0)
        {
            for (int i = 0; i < names.length; ++i)
            {
                name = names[i].canonicalName;
                domain = name.substring(0, name.indexOf(':'));
                if (!domains.contains(domain))
                {
                    domains.add(domain);
                }
            }
        }
        return (String[])domains.toArray(new String[domains.size()]);
    }

    ///////////////////////////////
    //
    // Internal helper methods
    //
    ///////////////////////////////

    /**
     * Helper method to validate that we have a well-formed ObjectName string.
     *
     * @param objectName The object name to validate.
     * @return The valid ObjectName.
     */
    private javax.management.ObjectName validateObjectName(String objectName)
    {
        try
        {
            return new javax.management.ObjectName(objectName);
        }
        catch (MalformedObjectNameException mone)
        {
            ManagementException me = new ManagementException();
            me.setMessage(MALFORMED_OBJECTNAME, new Object[] {objectName});
            throw me;
        }
    }

    /**
     * Helper method to validate that we have a well-formed Attribute.
     *
     * @param attribute The attribute to validate.
     * @return The valid Attribute.
     */
    private javax.management.Attribute validateAttribute(Attribute attribute)
    {
        return attribute.toAttribute();
    }

}
