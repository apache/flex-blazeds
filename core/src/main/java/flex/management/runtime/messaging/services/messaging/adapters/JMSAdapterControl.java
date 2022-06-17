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
package flex.management.runtime.messaging.services.messaging.adapters;

import flex.management.BaseControl;
import flex.management.runtime.messaging.services.ServiceAdapterControl;
import flex.messaging.services.messaging.adapters.JMSAdapter;

/**
 * The <code>JMSAdapterControl</code> class is the MBean implemenation
 * for monitoring and managing <code>JMSAdapter</code>s at runtime.
 */
public class JMSAdapterControl extends ServiceAdapterControl implements
        JMSAdapterControlMBean {
    private static final String TYPE = "JMSAdapter";
    private JMSAdapter jmsAdapter;

    /**
     * Constructs a <code>JMSAdapterControl</code>, assigning its id, managed
     * <code>JMSAdapter</code> and parent MBean.
     *
     * @param serviceAdapter The <code>JMSAdapter</code> managed by this MBean.
     * @param parent         The parent MBean in the management hierarchy.
     */
    public JMSAdapterControl(JMSAdapter serviceAdapter, BaseControl parent) {
        super(serviceAdapter, parent);
        jmsAdapter = serviceAdapter;
    }

    /*
     *  (non-Javadoc)
     * @see flex.management.BaseControlMBean#getType()
     */
    public String getType() {
        return TYPE;
    }

    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.JMSAdapterControlMBean#getTopicProducerCount()
     */
    public Integer getTopicProducerCount() {
        return new Integer(jmsAdapter.getTopicProducerCount());
    }

    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.JMSAdapterControlMBean#getTopicConsumerCount()
     */
    public Integer getTopicConsumerCount() {
        return new Integer(jmsAdapter.getTopicConsumerCount());
    }

    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.JMSAdapterControlMBean#getTopicConsumerIds()
     */
    public String[] getTopicConsumerIds() {
        return jmsAdapter.getTopicConsumerIds();
    }

    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.JMSAdapterControlMBean#getQueueProducerCount()
     */
    public Integer getQueueProducerCount() {
        return new Integer(jmsAdapter.getQueueProducerCount());
    }

    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.JMSAdapterControlMBean#getQueueConsumerCount()
     */
    public Integer getQueueConsumerCount() {
        return new Integer(jmsAdapter.getQueueConsumerCount());
    }

    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.JMSAdapterControlMBean#getQueueConsumerIds()
     */
    public String[] getQueueConsumerIds() {
        return jmsAdapter.getQueueConsumerIds();
    }

    /*
     *  (non-Javadoc)
     * @see flex.management.runtime.JMSAdapterControlMBean#removeConsumer(java.lang.String)
     */
    public void removeConsumer(String consumerId) {
        jmsAdapter.removeConsumer(consumerId);
    }
}
