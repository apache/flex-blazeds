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

package flex.messaging.factories;

import flex.messaging.Destination;
import flex.messaging.FactoryInstance;
import flex.messaging.FlexConfigurable;
import flex.messaging.FlexContext;
import flex.messaging.FlexFactory;
import flex.messaging.FlexSession;
import flex.messaging.MessageBroker;
import flex.messaging.config.ConfigMap;
import flex.messaging.util.ClassUtil;

/**
 * This class is used by the <code>JavaFactory</code> to store the configuration
 * for an instance created by the <code>JavaFactory</code>.  There is one of these for
 * each destination currently since only destinations create these components.
 *
 * @see flex.messaging.factories.JavaFactory
 */

public class JavaFactoryInstance extends FactoryInstance
{
    Object applicationInstance = null;
    Class javaClass = null;
    String attributeId;

    /**
     * Constructs a <code>JavaFactoryInstance</code>, assigning its factory, id,
     * and properties.
     *
     * @param factory The <code>JavaFactory</code> that created this instance.
     * @param id The id for the <code>JavaFactoryInstance</code>.
     * @param properties The properties for the <code>JavaFactoryInstance</code>.
     */
    public JavaFactoryInstance(JavaFactory factory, String id, ConfigMap properties)
    {
        super(factory, id, properties);
    }

    /**
     * Sets the attribute id for the <code>JavaFactoryInstance</code>.
     *
     * @param attributeId The attribute id for the <code>JavaFactoryInstance</code>.
     */
    public void setAttributeId(String attributeId)
    {
        this.attributeId = attributeId;
    }

    /**
     * Returns the attribute id for the <code>JavaFactoryInstance</code>.
     *
     * @return attributeId The attribute id for the <code>JavaFactoryInstance</code>.
     */
    public String getAttributeId()
    {
        return attributeId;
    }

    /**
     * Sets the instance class to null, in addition to updating the
     * <code>source</code> property.
     */
    @Override public void setSource(String source)
    {
        super.setSource(source);
        if (javaClass != null)
            javaClass = null;
    }

    /**
     * Creates an instance from specified <code>source</code> and initializes
     * the instance if it is of <code>FlexConfigurable</code> type.
     *
     * @return the instance
     */
    public Object createInstance()
    {
        Object inst = ClassUtil.createDefaultInstance(getInstanceClass(), null);

        MessageBroker mb = FlexContext.getMessageBroker();
        if (mb != null)
        {
            Destination destination = mb.getRegisteredDestination(getId());
            if (destination != null && destination.isInitialized())
            {
                if (inst instanceof FlexConfigurable)
                    ((FlexConfigurable) inst).initialize(getId(), getProperties());
            }
        }

        return inst;
    }

    /**
     * Creates an instance class from specified <code>source</code>.
     */
    @Override public Class getInstanceClass()
    {
        if (javaClass == null)
            javaClass = ClassUtil.createClass(getSource(),
                     FlexContext.getMessageBroker() == null ? this.getClass().getClassLoader() :
                     FlexContext.getMessageBroker().getClassLoader());

        return javaClass;
    }

    /**
     * Updates the session so that these values get replicated to other nodes
     * in the cluster.  Possibly we should make this configurable?
     */
    @Override public void operationComplete(Object instance)
    {
        if (getScope().equalsIgnoreCase(FlexFactory.SCOPE_SESSION))
        {
            FlexSession session = FlexContext.getFlexSession();
            if (session != null && session.isValid())
            {
                session.setAttribute(getAttributeId(), instance);
            }
        }
    }

    /** @exclude **/
    @Override public String toString()
    {
        return "JavaFactory instance for id=" + getId() + " source=" + getSource() + " scope=" + getScope();
    }
}
