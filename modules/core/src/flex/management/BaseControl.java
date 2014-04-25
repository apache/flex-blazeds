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

import flex.management.runtime.AdminConsoleDisplayRegistrar;
import flex.messaging.FlexContext;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanRegistration;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.servlet.ServletConfig;

/**
 * The implementation of the <code>BaseControlMBean</code> interface. This
 * abstract class provides the core functionality that all Flex control MBeans
 * require.
 * <p>
 * Defining concrete implementations of <code>getId()</code> and
 * <code>getType()</code> are left to subclasses, but this base class does
 * provide access to the parent MBean for each instance. This class also
 * implements the <code>MBeanRegistration</code> interface, and it
 * automatically stores a reference to the MBean server in each instance upon
 * registration. Subclasses may choose to override none, any, or all of the
 * methods defined by the <code>MBeanRegistration</code> interface, but any
 * overrides should be sure to invoke the overridden method with a call to their
 * superclass.
 * </p><p>
 * The <code>register()</code> method provides a simple and consistent way to
 * register instances with the MBean server, and the
 * <code>getObjectName()</code> method gaurantees consistent, well-formed
 * <code>ObjectName</code>s for all MBean instances.</p>
 *
 * @author shodgson
 */
public abstract class BaseControl implements BaseControlMBean,
        MBeanRegistration
{
    /**
     * The prefix used for the domain part of control MBean names.
     */
    public static final String DOMAIN_PREFIX = "flex.runtime";
    private static final int MALFORMED_OBJECTNAME = 10400;
    private static final int UNREG_EXCEPTION = 10401;
    private static final int UNREG_NOTFOUND = 10402;
    private static final int REG_EXCEPTION = 10403;
    private static final int REG_ALREADYEXISTS = 10404;
    private static final int REG_NOTCOMPLIANT = 10405;
    private static final int DISABLE_MANAGEMENT = 10426;    

    protected Date startTimestamp;
    private BaseControl parent;
    private ObjectName objectName;
    private ObjectName registeredObjectName;
    private MBeanServer server;
    private boolean registered = false;

    private AdminConsoleDisplayRegistrar registrar;

    // Implements flex.management.BaseControlMBean.getId; inherits javadoc
    // specification.
    public abstract String getId();

    // Implements flex.management.BaseControlMBean.getType; inherits javadoc
    // specification.
    public abstract String getType();

    // Implements flex.management.BaseControlMBean.getParent; inherits javadoc
    // specification.
    public final ObjectName getParent()
    {
        return (parent != null) ? parent.getObjectName() : null;
    }

    /**
     * Returns an identifier for the application that hosts the component that
     * this control manages.
     *
     * @return An identifier for the application that hosts the component this
     *         control manages.
     */
    public String getApplicationId()
    {
        String id = null;
        // Our base implementation attempts to use the current servlet context
        // name as our application identifier.
        ServletConfig config = FlexContext.getServletConfig();
        if (config != null)
        {
            id = config.getServletContext().getServletContextName();
        }
        return (id != null) ? id.replace(":", "") : "";
    }

    /**
     * Set the register object. 
     * @param registar the registrar to set
     */
    protected void setRegistrar(AdminConsoleDisplayRegistrar registrar)
    {
        this.registrar = registrar;
    }

    /**
     * Return the registar object.
     * @return the registrar
     */
    public AdminConsoleDisplayRegistrar getRegistrar()
    {
        if ((parent == null) && (this.registrar == null))
        {
            return new AdminConsoleDisplayRegistrar(null);
        }

        return (this.registrar != null) ? this.registrar : parent.getRegistrar();
    }

    /**
     * Constructs a <code>BaseControl</code> instance that references its
     * parent; the parent may be null for root control MBeans.
     *
     * @param parent The parent <code>BaseControl</code> for this instance or
     *        null if this instance is the root of a control hierarchy.
     */
    public BaseControl(BaseControl parent)
    {
        this.parent = parent;
    }

    /**
     * Returns the parent <code>BaseControl</code> of this instance.
     *
     * @return The parent <code>BaseControl</code>.
     */
    public final BaseControl getParentControl()
    {
        return parent;
    }

    /**
     * The <code>MBeanServer</code> that this instance is registered with. If
     * this instance has not been registered this method returns
     * <code>null</code>.
     *
     * @return The <code>MBeanServer</code> that this instance is registered
     *         with.
     */
    public final MBeanServer getMBeanServer()
    {
        return server;
    }

    /**
     * Registers this instance with the MBean server.
     *
     * It may throw ManagementException If an <code>MBeanRegistrationException</code>
     *         or <code>InstanceAlreadyExistsException</code> is thrown while
     *         registering this MBean, the typed exception is wrapped in a
     *         runtime <code>ManagementException</code> and rethrown.
     */
    public final void register()
    {
        if (!registered)
        {
            MBeanServer server = MBeanServerLocatorFactory.getMBeanServerLocator().getMBeanServer();
            ObjectName name = getObjectName();
            try
            {
                if (server.isRegistered(name))
                {
                    server.unregisterMBean(name);
                }

                registeredObjectName = server.registerMBean(this, name).getObjectName();
                registered = true;
                onRegistrationComplete();

            }
            catch (ManagementException me)
            {
                throw me;
            }
            catch (MBeanRegistrationException mre)
            {
                // Rethrow with useful message if this ever happens.
                ManagementException me = new ManagementException();
                me.setMessage(REG_EXCEPTION, new Object[] {name.toString()});
                me.setRootCause(mre);
                throw me;
            }
            catch (InstanceAlreadyExistsException iaee)
            {
                // If registration is not working at all, inform the user that
                // they may
                // work around the issue by disabling management (no MBeans will
                // be registered).
                if (!server.isRegistered(name))
                {
                    ManagementException me = new ManagementException();
                    me.setMessage(DISABLE_MANAGEMENT, new Object[] {name.toString()});
                    throw me;
                }
                else
                {
                    // Rethrow with useful message if this ever happens.
                    ManagementException me = new ManagementException();
                    me.setMessage(REG_ALREADYEXISTS, new Object[] {name.toString()});
                    throw me;
                }
            }
            catch (NotCompliantMBeanException ncme)
            {
                // Rethrow with useful message if this ever happens.
                ManagementException me = new ManagementException();
                me.setMessage(REG_NOTCOMPLIANT, new Object[] {name.toString()});
                throw me;
            }
            catch (InstanceNotFoundException infe)
            {
                // Rethrow with useful message if this ever happens.
                ManagementException me = new ManagementException();
                me.setMessage(UNREG_NOTFOUND, new Object[] {name.toString()});
                throw me;
            }
        }
    }

    /**
     * This method is called after the MBean has been registered and after the
     * MBean server has returned the registeredObjectName. Classes that need
     * access to the actual Object name should override this method rather than
     * the postRegister method.
     */
    protected void onRegistrationComplete()
    {

    }

    /**
     * Unregisters this instance from the MBean server if it has been registered
     * previously.
     */
    public final void unregister()
    {
        if (registered)
        {
            // This method may be called when the JVM is being unloaded, so if
            // our
            // external error strings are loaded as missing, fall back to
            // hard-coded
            // strings.
            try
            {
                if (server.isRegistered(registeredObjectName))
                {
                    server.unregisterMBean(registeredObjectName);
                }

                registeredObjectName = null;
                registered = false;
            }
            catch (ManagementException me)
            {
                throw me;
            }
            catch (MBeanRegistrationException mre)
            {
                // Rethrow with useful message if this ever happens.
                ManagementException me = new ManagementException();
                me.setMessage(UNREG_EXCEPTION, new Object[] {registeredObjectName.toString()});
                if (me.getMessage().indexOf(Integer.toString(UNREG_EXCEPTION)) != -1)
                {
                    me.setMessage("The MBean named, '" + registeredObjectName.toString() + "', could not be unregistered because its preDeregister() method threw an exception.");
                }
                me.setRootCause(mre);
                throw me;
            }
            catch (InstanceNotFoundException infe)
            {
                // Rethrow with useful message if this ever happens.
                ManagementException me = new ManagementException();
                me.setMessage(UNREG_NOTFOUND, new Object[] {registeredObjectName.toString()});
                if (me.getMessage().indexOf(Integer.toString(UNREG_NOTFOUND)) != -1)
                {
                    me.setMessage("The MBean named, '" + registeredObjectName.toString() + "', could not be unregistered because it is not currently registered.");
                }
                throw me;
            }
        }
    }

    /**
     * Returns the <code>ObjectName</code> for this instance, according to the
     * following format:
     * <code>{domain}[&amp;#46;{appId}]:type={type}[&amp;#44;{parent type}={parent id}]*[&amp;#44;server={server}]&amp;#63;&amp;#44;id={id}</code>.
     * <ul>
     * <li><code>domain</code>: The domain specified by the DOMAIN_PREFIX
     * constant followed by the application identifier if one is available.</li>
     * <li><code>type</code>: The short type name of the resource managed by
     * the MBean.<br /> - The <code>MessageBrokerControlMBean</code> manages
     * the <code>flex.messaging.MessageBroker</code> so:
     * <code>type=MessageBroker</code> </li>
     * <li><code>id</code>: The id value for the resource managed by this
     * MBean. If no name or id is available on the resource, an id will be
     * fabricated according to this strategy:<br />
     * <em>id = {type} + N</em> (where N is a numeric increment for instances
     * of this type) </li>
     * <li>* optional containment keys</li>
     * </ul>
     * The runtime MBean model is hierarchical, with all MBeans ultimately
     * contained by the root <code>MessageBrokerControlMBean</code>. The 
     * <code>ObjectName</code>s used for these MBeans describe this
     * containment in the following fashion. First, the 'type' key for a
     * contained MBean indicates the containment hierarchy for the bean. So, the
     * <code>ObjectName</code> for an <code>RTMPEndpointControlMBean</code>
     * would be: <code>type=MessageBroker.RTMPEndpoint</code><br />
     * In addition to the hierarchical 'type' key, the full
     * <code>ObjectName</code> for this <code>RTMPEndpointControlMBean</code>
     * also contains a containment key:
     * <code>MessageBroker=MessageBroker1</code><br />
     * Optional containment keys have the format:
     * <em>{parent type}={parent name}</em>. A containment key is added for
     * each ancestor up to the root of the hierarchy and these keys allow the
     * <code>ObjectName</code> for any MBean instance to fully describe its
     * specific location in the hierarchy. To complete the example, the full
     * <code>ObjectName</code> for the example
     * <code>RTMPEndpointControlMBean</code> would be:
     * <code>flex:type=MessageBroker.RTMPEndpoint,MessageBroker=MessageBroker1,id=RTMPEndpoint1</code>
     * <p>
     * If the MBean is registered with the MBean server, this method returns the
     * <code>ObjectName</code> that the MBean was registered under and this
     * value may contain additional key-value pairs injected by the container or
     * MBean server.
     * </p>
     *
     * @return The <code>ObjectName</code> for this instance.
     */
    public final ObjectName getObjectName()
    {
        if (registered)
            return registeredObjectName;

        if (objectName == null)
        {
            StringBuffer buffer = new StringBuffer();
            buffer.append(DOMAIN_PREFIX);
            String appId = getApplicationId();
            if (appId != null && appId.length() > 0)
            {
                buffer.append('.');
                buffer.append(appId);
            }
            buffer.append(":type=");
            // Build hierarchical type value.
            List types = new ArrayList();
            List ids = new ArrayList();
            types.add(getType());
            ids.add(getId());
            BaseControl ancestor = parent;
            while (ancestor != null)
            {
                types.add(ancestor.getType());
                ids.add(ancestor.getId());
                ancestor = ancestor.getParentControl();
            }
            for (int i = types.size() - 1; i >= 0; --i)
            {
                buffer.append((String)types.get(i));
                if (i > 0)
                {
                    buffer.append('.');
                }
            }
            buffer.append(',');
            // Add containment keys.
            for (int i = ids.size() - 1; i >= 1; --i)
            {
                buffer.append((String)types.get(i));
                buffer.append('=');
                buffer.append((String)ids.get(i));
                buffer.append(',');
            }
            buffer.append("id=");
            buffer.append(getId());
            String name = buffer.toString();
            // TODO: Seth: add server identifier key if we're running in a
            // cluster?
            try
            {
                objectName = new ObjectName(name);
            }
            catch (MalformedObjectNameException mone)
            {
                // Rethrow with useful message if this ever happens.
                ManagementException me = new ManagementException();
                me.setMessage(MALFORMED_OBJECTNAME, new Object[] {name});
                throw me;
            }
        }
        return objectName;
    }

    /**
     * Implements <code>javax.management.MBeanRegistration.preRegister</code>.
     * Allows the MBean to perform any operations it needs before being
     * registered in the MBean server. This base implementation stores a
     * reference to the MBean server that may be accessed via
     * <code>getMBeanServer()</code>. If subclasses override, they must call
     * <code>super.preRegister()</code>.
     *
     * @param server The Mbean server in which the MBean will be registered.
     * @param name The object name of the MBean.
     * @return The name the MBean will be registered under.
     * @throws Exception when the process failed
     */
    public ObjectName preRegister(MBeanServer server, ObjectName name)
            throws Exception
    {
        this.server = server;
        return (name == null) ? getObjectName() : name;
    }

    /**
     * Implements <code>javax.management.MBeanRegistration.postRegister</code>.
     * Allows the MBean to perform any operations needed after having been
     * registered in the MBean server or after the registration has failed. This
     * base implementation is a no-op that may be overridden.
     *
     * @param registrationDone Indicates whether or not the MBean was
     *        successfully registered in the MBean server.
     */
    public void postRegister(Boolean registrationDone)
    {
        // No-op.
    }

    /**
     * Implements <code>javax.management.MBeanRegistration.preDeregister</code>.
     * Allows the MBean to perform any operations needed after having been
     * unregistered in the MBean server. This base implementation is a no-op
     * that may be overridden.
     * @throws Exception when the process failed
     */
    public void preDeregister() throws Exception
    {
        // No-op.
    }

    /**
     * Implements <code>javax.management.MBeanRegistration.postDeregister</code>.
     * Allows the MBean to perform any operations it needs before being
     * unregistered by the MBean server. This base implementation is a no-op
     * that may be overridden.
     */
    public void postDeregister()
    {
        // No-op.
    }

    /**
     * Sets the start timestamp for the managed component.
     *
     * @param value The start timestamp for the managed component.
     */
    public void setStartTimestamp(Date value)
    {
        startTimestamp = value;
    }

    /**
     * @exclude Returns the difference between a start and end timestamps in
     *          minutes. Differences of less than one minute are rounded up to
     *          one minute to avoid frequency calculations reporting infinite
     *          message frequencies.
     * @param startTime The start timestamp in milliseconds.
     * @param endTime The end timestamp in milliseconds.
     * @return The difference between a start and end timestamps in minutes.
     */
    protected double differenceInMinutes(long startTime, long endTime)
    {
        double minutes = (endTime - startTime) / 60000d;
        if (minutes > 1d)
        {
            return minutes;
        }
        else
        {
            return 1d;
        }
    }
}
