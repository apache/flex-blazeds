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

import java.util.List;
import flex.messaging.FlexContext;
import flex.messaging.MessageRoutedEvent;
import flex.messaging.MessageRoutedListener;
import flex.messaging.MessageRoutedNotifier;
import flex.messaging.VersionInfo;
import flex.messaging.client.FlexClientOutboundQueueProcessor;
import flex.messaging.config.ConfigMap;
import flex.messaging.messages.Message;

public class MessageRoutedListenerTest extends FlexClientOutboundQueueProcessor implements MessageRoutedListener
{
    
    public void initialize(ConfigMap properties) 
    {
        System.out.println("======== initialize custom FlexClientOutboundQueueProcessor " + VersionInfo.getBuildAsLong());
        super.initialize(properties);
    }
    
    // add event listener and route the message to the queue
    public void add(List outboundQueue, Message message)
    {
        MessageRoutedNotifier notifier = FlexContext.getMessageRoutedNotifier();
        
        if(notifier != null)
        {
            notifier.addMessageRoutedListener(this);
            System.out.println("======== MessageRoutedListener added");
            message.setBody("The original message has been removed");
        }
        
        super.add(outboundQueue, message);       
    }
    
    public void messageRouted(MessageRoutedEvent event)
    {
        Message msg = event.getMessage();
        System.out.println("========= MessageRouted: id=" + msg.getMessageId() + " destination=" + msg.getDestination() + " body=" + msg.getBody().toString());        
    }
    
}
