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

package flex.messaging.services.messaging.selector;

import javax.jms.InvalidSelectorException;
import javax.jms.JMSException;

import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.filter.BooleanExpression;
import org.apache.activemq.filter.MessageEvaluationContext;
import org.apache.activemq.selector.SelectorParser;

import flex.messaging.log.Log;
import flex.messaging.log.LogCategories;
import flex.messaging.messages.Message;

/**
 * The JMSSelector is used to determine whether a message matches a given
 * SQL-92 selector expression.
 * 
 * A prior implementation of this class borrowed heavily from Sun's
 * proprietary code, that eventually found its way into Glassfish. The license
 * was still CDDL (or CDDL+GPL) and therefore unfit for donation to Apache.
 * 
 * The current implementation relies on Apache ActiveMQ to do the same. Since
 * the selector is evaluated against the message headers (called properties in
 * ActiveMQ parlance), and both ActiveMQ messages and Flex messages use a Map<String, Object>
 * to store the header-value pair, it is easy to create a minimally sufficient
 * ActiveMQMessage from a Flex message.
 * 
 * Possible performance improvement: prevent creation of an ActiveMQMessage for each selector.
 * This isn't exactly a lightweight object but there seem to be only two choices at the moment:
 * a. Use a static field and synchronize access to it
 * 						OR
 * b. Instantiate a new ActiveMQMessage for each selector
 * 
 * Re-using the same instance (approach a.) would entail calling clearProperties() followed
 * by setProperties() and that too by one thread at a time, which is why approach b. has been
 * chosen for now.
 *
 */
public class JMSSelector
{
    public static final String LOG_CATEGORY = LogCategories.MESSAGE_SELECTOR; // Because we're not always JMS-specific.

    private String pattern = null;

    /**
     * Class Constructor.
     *
     * @param pattern selector pattern
     */
    public JMSSelector(String pattern)
    {
        if (pattern == null)
            pattern = "";

        this.pattern = pattern;
    }

    /**
     * Matches the message against the selector expression.
     *
     * @param msg The message to match against.
     * @return true if the message headers match the selector; otherwise false.
     * @exception JMSSelectorException
     */
    public boolean match(Message msg) throws JMSSelectorException
    {
        if (pattern.equals(""))
            return true; // No selector

        boolean matched = false;

        try
        {    	
            // Parse selector pattern
            BooleanExpression expr = SelectorParser.parse(pattern);	
			
			//Create a disposable ActiveMQMessage
            ActiveMQMessage dummyMessage = new ActiveMQMessage();

            // Populate dummyMessage from Flex message
            dummyMessage.setProperties(msg.getHeaders());

            // Set up an evaluation context
            MessageEvaluationContext context = new MessageEvaluationContext();
            context.setMessageReference(dummyMessage);

            // Check whether message (headers) matches selector expression
            matched = expr.matches(context);

        }
        catch (InvalidSelectorException e)
        {
            throw new JMSSelectorException(e);
        }
        catch (JMSException e)
        {
            throw new JMSSelectorException(e);
        }
        Log.getLogger(LOG_CATEGORY).debug("Selector: " + pattern + (matched ? " matched " : " did not match ") + " message with id: " + msg.getMessageId());
        return matched;
    }
}