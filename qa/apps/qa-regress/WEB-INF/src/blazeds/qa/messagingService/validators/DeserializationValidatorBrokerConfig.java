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

package blazeds.qa.messagingService.validators;

import flex.messaging.MessageBroker;
import flex.messaging.validators.ClassDeserializationValidator;
import blazeds.qa.messagingService.validators.TestDeserializationValidator;
import flex.messaging.FlexContext;

public class DeserializationValidatorBrokerConfig
{
    /**
     * Method testGetValidator returns Validator classname if Validator exists or string indicating validator is null.
     */
    public String testGetValidator()
    {
        String validator = "validator is null!";

        MessageBroker broker = FlexContext.getMessageBroker();
        if (broker.getDeserializationValidator() == null )
        {
            validator = "Null Validator Returned";
        }
        else
        {
            validator = broker.getDeserializationValidator().getClass().getName();
        }

        return validator;
    }

    /**
     * Method testSetValidator sets Validator of classname passed.
     */
    public void testSetValidatorBookRemoved()
    {
        try
        {
            MessageBroker broker = FlexContext.getMessageBroker();
            ClassDeserializationValidator dsvalidator =(ClassDeserializationValidator) broker.getDeserializationValidator();
            dsvalidator.removeAllowClassPattern("blazeds.qa.remotingService.Book");

            //System.out.println("Book Validator had Book removed from allowed");
        }
        catch(Exception e)
        {
            System.out.println("test set validator with book removed error: " + e.toString());
        }

    }

    /**
     * Method testSetValidator returns void - creates, configures and sets ClassDeserializationValidator.
     */
    public void testSetValidator()
    {
        try
        {
            ClassDeserializationValidator dsvalidator = new ClassDeserializationValidator();
            dsvalidator.addAllowClassPattern("flex.*");
            dsvalidator.addAllowClassPattern("java.*");
            dsvalidator.addAllowClassPattern("javax.*");
            dsvalidator.addAllowClassPattern("\\[Ljava.*");
            dsvalidator.addAllowClassPattern("\\[B*");
            dsvalidator.addAllowClassPattern("blazeds.qa.remotingService.Book");
            dsvalidator.removeAllowClassPattern("blazeds.qa.remotingService.Book");
            dsvalidator.addAllowClassPattern("blazeds.qa.remotingService.Book");
            dsvalidator.addDisallowClassPattern("blazeds.qa.remotingService.Book");
            dsvalidator.removeDisallowClassPattern("blazeds.qa.remotingService.Book");
            MessageBroker broker = FlexContext.getMessageBroker();
            broker.setDeserializationValidator(dsvalidator);
            //System.out.println("Validator with Book, flex and java set!");
        }
        catch(Exception e)
        {
            System.out.println("test set validator error: " + e.toString());
        }

    }

    /**
     * Method testSetEmptyValidator returns void - creates, configures and sets 'empty' ClassDeserializationValidator.
     * Only flex.*, java.* and javax.* is allowed.
     */
    public void testSetEmptyValidator()
    {
        try
        {
            ClassDeserializationValidator dsvalidator = new ClassDeserializationValidator();
            dsvalidator.addAllowClassPattern("flex.*");
            dsvalidator.addAllowClassPattern("java.*");
            dsvalidator.addAllowClassPattern("javax.*");
            dsvalidator.addAllowClassPattern("\\[Ljava.*");
            dsvalidator.addAllowClassPattern("\\[B*");
            dsvalidator.addDisallowClassPattern("blazeds.qa.remotingService.Book");
            MessageBroker broker = FlexContext.getMessageBroker();
            broker.setDeserializationValidator(dsvalidator);
            //System.out.println("Validator with only 'flex.*' and java, set!");
        }
        catch(Exception e)
        {
            System.out.println("test set empty validator error: " + e.toString());
        }

    }

    /**
     * Method testSetNullValidator returns void - sets validator to null, basically removes validator.
     */
    public void testSetNullValidator()
    {
        try
        {
            MessageBroker broker = FlexContext.getMessageBroker();
            broker.setDeserializationValidator(null);
            //System.out.println("Validator set to null");
        }
        catch(Exception e)
        {
            System.out.println("test set null validator error (remove validator): " + e.toString());
        }

    }

    /**
     * Method testSetValidator returns void - creates, configures and sets ClassDeserializationValidator.
     */
    public void testSetTestValidator()
    {
        try
        {
            TestDeserializationValidator dsvalidator = new TestDeserializationValidator();
            MessageBroker broker = FlexContext.getMessageBroker();
            broker.setDeserializationValidator(dsvalidator);
            //System.out.println("TestValidator with class logging only set!");
        }
        catch(Exception e)
        {
            System.out.println("test set TestValidator error: " + e.toString());
        }
    }

}