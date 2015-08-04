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
package qa.messaging;

import flex.messaging.services.messaging.adapters.ActionScriptAdapter;
import flex.messaging.services.messaging.Subtopic;
import flex.messaging.FlexContext;
import flex.messaging.FlexSession;

import java.security.Principal;
/**
 * The <code>DynamicDestinationAdapter</code> tests adding custom logic to 
 * determine whether the client should be able to subscribe or send messages
 * to the specified subtopic.
 */
public class DynamicDestinationAdapter extends ActionScriptAdapter  {

    /**
     * Adds custom logic to determine whether the client should be able to subscribe.
     * Users guest, employee, supervisor and manager are allowed to subscribe.
     * 
     * @param subtopic The subtopic the client is attempting to send a message to.
     * @return true to allow the message to be sent, false to prevent it. 
     */
    public boolean allowSubscribe(Subtopic subtopic) {
        FlexSession session = FlexContext.getFlexSession();

        String separator = subtopic.getSeparator();

        System.out.println("DynamicDestinationAdapter.allowSubscribe()");
        System.out.println("  destination.id = " + getDestination().getId());
        System.out.println("  subtopic.getValue() = " + subtopic.getValue());
        System.out.println("  subtopic.containsSubtopicWildcard()? " + subtopic.containsSubtopicWildcard());
        System.out.println("  subtopic.isHierarchical()? " + subtopic.isHierarchical());
        System.out.println("  subtopic.getSeparator()? " + subtopic.getSeparator());

        getDestination().getId();

        Principal principal = session.getUserPrincipal();
        String principalName = principal == null? "" : principal.getName();

        // allow managers full access
        if (session.isUserInRole("managers")) {
            System.out.println("-> allowing manager access");
            return true;

        // allow supervisors to subscribe to anything but *hr*
        } else if (session.isUserInRole("supervisors") && subtopic.getValue().indexOf(separator + "hr") < 0 ) {
            System.out.println("-> allowing supervisor access to a topic that does not contain *hr*");
            return true;

        // allow supervisors and employees to subscribe to anything that does not contain wildcards or *hr*
        } else if (!subtopic.containsSubtopicWildcard() && subtopic.getValue().indexOf(separator + "hr") < 0
                && (principalName.equals("employee") || principalName.equals("supervisor"))) {
            System.out.println("-> allowing supervisor or employee access to a non-wildcarded topic that does not contain *hr*");
            return true;

        } if (!subtopic.containsSubtopicWildcard() && !subtopic.isHierarchical() && subtopic.getValue().indexOf(separator + "hr") < 0 && principalName.equals("guest") ) {
            System.out.println("-> allowing guest access to a non-wildcarded non-hierarchical topic that does not contain *hr*");
            return true;
        } else {
            return false;
        }
    }

    /**
     * Adds custom logic to determine whether the client should be able to send to the 
     * specified subtopic. 
     * 
     * @param subtopic The subtopic the client is attempting to send a message to.
     * @return true to allow the message to be sent, false to prevent it. 
     */
    public boolean allowSend(Subtopic subtopic) {
        FlexSession session = FlexContext.getFlexSession();
        String separator = subtopic.getSeparator();

        Principal principal = session.getUserPrincipal();
        String principalName = principal == null? "" : principal.getName();

        // allow managers full access
        if (principalName.equals("manager")) {
            return true;

        // allow supervisor, employee to send messages to anything but *hr*
        } else if ((session.isUserInRole("employees") || session.isUserInRole("supervisors")) && subtopic.getValue().indexOf(separator + "hr") < 0 ) {
            return true;

        // allow guests to send messages to 'sandbox' only.
        } else if (session.isUserInRole("guests") && subtopic.matches(new Subtopic("sandbox", separator))) {
            return true;
        } else {
            return false;
        }
    }

}
