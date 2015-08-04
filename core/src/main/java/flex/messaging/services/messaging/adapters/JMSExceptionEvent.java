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
package flex.messaging.services.messaging.adapters;

import java.util.EventObject;
import javax.jms.JMSException;

/**
 * Event dispatched to the JMSExceptionListener when a JMS exception is encountered
 * by the source.
 * 
 * @see flex.messaging.services.messaging.adapters.JMSExceptionListener
 */
public class JMSExceptionEvent extends EventObject
{
    private JMSException jmsException;
    
    /**
     * Create a new JMSExceptionEvent with the source and exception.
     * 
     * @param source The source of the exception.
     * @param jmsException The actual JMS exception.
     */
    JMSExceptionEvent(JMSConsumer source, JMSException jmsException)
    {
        super(source);
        this.jmsException = jmsException;
    }
    
    /**
     * Return the JMS exception of the event.
     * 
     * @return The JMS exception of the event.
     */
    public JMSException getJMSException()
    {
        return jmsException;
    }    
}