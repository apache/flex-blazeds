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

import java.util.Date;

import flex.messaging.FlexComponent;
import flex.messaging.config.ConfigMap;
import flex.messaging.config.ConfigurationException;
import flex.messaging.log.Log;

/**
 * An abstract base class that implements the <code>Manageable</code> and <code>FlexComponent</code> interfaces.
 * This is an excellent starting point for a server component that may be instantiated, initialized, started and
 * stopped, as well as exposing an optional management interface via a peer MBean.
 * <p>Support for changing component properties while the component is
 * started should be determined on a per-property basis, and the started property is volatile to ensure consistent
 * reads of the start state of the component across threads. This class performs no synchronization and is not safe for modification by multiple concurrent threads
 * in the absence of external synchronization.
 * </p>
 */
public abstract class ManageableComponent implements Manageable, FlexComponent {
    //--------------------------------------------------------------------------
    //
    // Protected Static Constants
    //
    //--------------------------------------------------------------------------

    /**
     * Error code for attempting to change a property after starting.
     */
    protected static final int PROPERTY_CHANGE_AFTER_STARTUP = 11115;

    /**
     * Error code to alert the user that a required component property is null.
     */
    protected static final int NULL_COMPONENT_PROPERTY = 11116;

    //--------------------------------------------------------------------------
    //
    // Constructor
    //
    //--------------------------------------------------------------------------

    /**
     * Constructs a <code>ManageableComponent</code> instance, specifying
     * whether to enable management.
     * Enabling management will trigger the creation of a peer MBean that exposes the
     * management interface for this component.
     *
     * @param enableManagement <code>true</code> to enable management, <code>false</code> to disable
     *                         management.
     */
    public ManageableComponent(boolean enableManagement) {
        setManaged(enableManagement);
    }

    //--------------------------------------------------------------------------
    //
    // Public Properties
    //
    //--------------------------------------------------------------------------

    //----------------------------------
    //  control
    //----------------------------------

    /**
     * The peer MBean of the <code>ManageableComponent</code> that exposes a management interface.
     */
    protected BaseControl control;

    /**
     * (non-JavaDoc)
     *
     * @see Manageable#getControl()
     */
    public BaseControl getControl() {
        return control;
    }

    /**
     * (non-JavaDoc)
     *
     * @see Manageable#setControl(BaseControl)
     */
    public void setControl(BaseControl control) {
        this.control = control;
    }

    //----------------------------------
    //  id
    //----------------------------------

    /**
     * The internal id value of the <code>ManageableComponent</code>.
     */
    protected String id;

    /**
     * Returns the id of the <code>ManageableComponent</code>.
     *
     * @return The id of the <code>ManageableComponent</code>.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id of the <code>ManageableComponent</code>. The id cannot be
     * null and it cannot be changed after startup.
     *
     * @param id The id of the <code>ManageableComponent</code>.
     */
    public void setId(String id) {
        if (isStarted()) {
            blockAssignmentWhileStarted("id");
        }
        if (id == null) {
            // Id of a component cannot be null.
            blockNullAssignment("id");
        }
        this.id = id;
    }

    //----------------------------------
    //  managed
    //----------------------------------

    /**
     * The internal managed flag of the <code>ManageableComponent</code>.
     */
    protected volatile boolean managed;

    /**
     * (non-JavaDoc)
     *
     * @see Manageable#isManaged()
     */
    public boolean isManaged() {
        return managed;
    }

    /**
     * Enables or disables management for the component. Management cannot be
     * changed once the component is started and management cannot be
     * <code>true</code> if the parent of the component is not managed.
     *
     * @param enableManagement <code>true</code> to enable management, <code>false</code> to disable management.
     */
    public void setManaged(boolean enableManagement) {
        if (isStarted() && control != null) {
            blockAssignmentWhileStarted("managed");
        }
        if (enableManagement && parent != null && !parent.isManaged()) {
            if (Log.isWarn()) {
                Log.getLogger(getLogCategory()).warn("Component: '" + id + "' cannot be managed" +
                        " since its parent is unmanaged.");
            }
            return;
        }
        managed = enableManagement;
    }

    //----------------------------------
    //  parent
    //----------------------------------

    /**
     * The internal reference to the parent component (if any) of the <code>ManageableComponent</code>.
     */
    protected Manageable parent;

    /**
     * Returns the parent of the component.
     *
     * @return The parent of the component.
     */
    public Manageable getParent() {
        return parent;
    }

    /**
     * Sets the parent of the component. The parent cannot be changed
     * after component startup and it cannot be null.
     *
     * @param parent The parent of the component.
     */
    public void setParent(Manageable parent) {
        if (isStarted()) {
            blockAssignmentWhileStarted("parent");
        }
        if (parent == null) {
            // Parent of a component cannot be null.
            blockNullAssignment("parent");
        }
        if (!parent.isManaged() && isManaged()) {
            if (Log.isWarn()) {
                Log.getLogger(getLogCategory()).warn("Component: '" + id + "' cannot be managed" +
                        " since its parent is unmanaged.");
            }
            setManaged(false);
        }
        this.parent = parent;
    }

    //----------------------------------
    //  started
    //----------------------------------

    /**
     * The internal started flag of the <code>ManageableComponent</code>.
     */
    protected volatile boolean started;

    /**
     * Returns if the component is started or not.
     *
     * @return <code>true</code> if the component is started.
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * Sets if the component is started.
     *
     * @param started true if the component is started.
     */
    protected void setStarted(boolean started) {
        if (this.started != started) {
            this.started = started;
            if (started && control != null) {
                control.setStartTimestamp(new Date());
            }
        }
    }

    //----------------------------------
    //  valid
    //----------------------------------

    /**
     * The internal valid flag of the <code>ManageableComponent</code>.
     */
    protected boolean valid;

    /**
     * Returns if the component is valid.
     *
     * @return <code>true</code> if the component is valid.
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Sets if the component is valid.
     *
     * @param valid true if the comoponent is valid.
     */
    protected void setValid(boolean valid) {
        this.valid = valid;
    }

    //----------------------------------
    //  logCategory
    //----------------------------------

    /**
     * Returns the log category of the component. Subclasses must provide an
     * implementation that returns their desired log category.
     *
     * @return The log category of the component.
     */
    protected abstract String getLogCategory();

    //--------------------------------------------------------------------------
    //
    // Public Methods
    //
    //--------------------------------------------------------------------------

    /**
     * Invoked to initialize the <code>ManageableComponent</code>.
     * This base implementation calls <code>setId()</code> passing the provided
     * id and ignores the properties map argument.
     * Subclasses should call <code>super.initialize()</code>.
     *
     * @param id         Id of the <code>ManageableComponent</code>.
     * @param properties Properties for the <code>ManageableComponent</code>.
     */
    public void initialize(String id, ConfigMap properties) {
        setId(id);
    }

    /**
     * Validates and starts the component.
     * <p>
     * Subclasses should call <code>super.start()</code>.
     */
    public void start() {
        validate();
        setStarted(true);
    }

    /**
     * Invalidates and stops the component.
     * <p>
     * Subclasses should call <code>super.stop()</code>.
     */
    public void stop() {
        invalidate();
        setStarted(false);
    }

    //--------------------------------------------------------------------------
    //
    // Protocted Methods
    //
    //--------------------------------------------------------------------------

    /**
     * Convenience method that may be used to generate and throw an Exception for an attempt to set the specified property if the
     * component is started.
     *
     * @param propertyName The name of the property being incorrectly assigned; included in the Exception message.
     */
    protected void blockAssignmentWhileStarted(String propertyName) {
        ConfigurationException ce = new ConfigurationException();
        ce.setMessage(PROPERTY_CHANGE_AFTER_STARTUP, new Object[]{propertyName});
        throw ce;
    }

    /**
     * Convenience method that may be used to generate and throw an Exception for an attempt to assign a null value to a property that
     * requires non-null values.
     *
     * @param propertyName The name of the property being incorrectly assigned.
     */
    protected void blockNullAssignment(String propertyName) {
        ConfigurationException ce = new ConfigurationException();
        ce.setMessage(NULL_COMPONENT_PROPERTY, new Object[]{propertyName});
        throw ce;
    }

    /**
     * Invoked from within the <code>stop()</code> method to invalidate the component as part of shutdown.
     * This base implementation sets the valid property to false.
     * Subclasses should call <code>super.invalidate()</code>.
     */
    protected void invalidate() {
        setValid(false);
    }

    /**
     * Hook method invoked from within the <code>start()</code> method to validate that the component is in a
     * startable state.
     * This base implementation validates the component by ensuring it has an id and a parent and then sets
     * the valid property to true.
     * If the component is not in a valid, startable state an Exception is thrown.
     * Subclasses should call <code>super.validate()</code>.
     */
    protected void validate() {
        if (getId() == null) {
            // Id of a component cannot be null.
            blockNullAssignment("id");
        }
        if (getParent() == null) {
            // Parent of a component cannot be null.
            blockNullAssignment("parent");
        }
        setValid(true);
    }

}
