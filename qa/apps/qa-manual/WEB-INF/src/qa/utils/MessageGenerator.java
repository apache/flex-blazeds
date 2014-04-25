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
package qa.utils;

/* This class was adapted from a Generator class was written by Seth as a necessity for testing and simulating a steady 
 * stream of messages at regulated intervals and at at a given rate regardless of the threading and processing model 
 * for different channels/endpoints being used by the test applications
 * 
 * This Generator class is instantiated by passing in how many generate calls should happen per second (targetRatePerSecond),
 * and how many CPU time slices (runs) per second you’d like it to shoot for (suggestedGeneratePassesPerSecond). It sleeps
 * between batches/runs so you don’t want so many batches that it’s giving up its CPU time
 * too quickly and isn’t able to get any work done within a second, but not so few that it
 * just generates a big burst of messages once a second up front rather than a stream.
 * Each generate call (either the doDataMessageGenerate(flex.messaging.io.amf.ASObject) or the doAsyncMessageGenerate() method) 
 * would create and send a message in an actual test. All in all, gives us the adaptive behavior
 * we need to better simulate a steady state generation of messages on the server regardless of what the
 * threading and processing model for different channels/endpoints is.
 */
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


import flex.messaging.MessageBroker;
import flex.messaging.io.MapProxy;
import flex.messaging.io.amf.ASObject;
import flex.messaging.messages.AsyncMessage;
import flex.messaging.util.UUIDUtils;

public class MessageGenerator implements Runnable
{
    private MessageBroker msgBroker;
    private int counter=0;
    
    public MessageGenerator(final int targetRatePerSecond, final int suggestedGeneratePassesPerSecond, final String destination, final Object message)
    {
        this.targetRatePerSecond = targetRatePerSecond;
        this.suggestedGeneratePassesPerSecond = suggestedGeneratePassesPerSecond;
        this.destination = destination;
        this.message = message;
        //set this value to null when processing an AsyncMessage Type
        this.originalValue = null;
        averageGeneratesPerPass = targetRatePerSecond / suggestedGeneratePassesPerSecond;
        suggestedPauseMillis = 1000 / suggestedGeneratePassesPerSecond;
        if (msgBroker == null) 
        {
            msgBroker = MessageBroker.getMessageBroker(null);
        }        
    }
    public MessageGenerator(final int targetRatePerSecond, final int suggestedGeneratePassesPerSecond, final String destination, final ASObject originalValue)
    {
        this.targetRatePerSecond = targetRatePerSecond;
        this.suggestedGeneratePassesPerSecond = suggestedGeneratePassesPerSecond;
        this.destination = destination;
        this.originalValue = originalValue;
        //set this value to null when processing a DataMessage Type
        this.message = null;
        averageGeneratesPerPass = targetRatePerSecond / suggestedGeneratePassesPerSecond;
        suggestedPauseMillis = 1000 / suggestedGeneratePassesPerSecond;
        if (msgBroker == null) 
        {
            msgBroker = MessageBroker.getMessageBroker(null);
        }        
    }
    
    private final int targetRatePerSecond;
    private final int suggestedPauseMillis;
    private final int suggestedGeneratePassesPerSecond;
    private final int averageGeneratesPerPass;
    private final String destination;
    private final Object message;
    private final ASObject originalValue;
    private final ArrayList<Integer> generationCountPerSecond = new ArrayList<Integer>();
    public volatile boolean stop;   
    
    public List<Integer> getGenerationCountPerSecond()
    {
        //System.out.println("messages : total sent " + counter);
        return Collections.unmodifiableList(generationCountPerSecond);
    }
    public Integer getTotal()
    {
        System.out.println("current total messages count: " + counter);
        //current total messages count
        return counter;
    }
    public Integer resetTotal()
    {
        counter = 0;
        System.out.println("reset total messages count: " + counter);
        //current total messages count
        return counter;
    }
    public void run()
    {
        final long startTimeMillis = System.currentTimeMillis();
        long currentSecond = 0;
        int generatesThisSecond = 0; 
        boolean missedTarget = false;
        
        while (!stop)
        {
            //System.out.println("---> Inside while loop. Execution millis =" + (System.currentTimeMillis()-startTimeMillis));              
            for (int i = 0; i < suggestedGeneratePassesPerSecond; ++i)
            {
                if (stop)
                {
                    return;  //exit, don't get caught in this loop
                }                 
                int generatesForThisPass = averageGeneratesPerPass;
                if ((generatesThisSecond + averageGeneratesPerPass) > targetRatePerSecond)
                {
                    generatesForThisPass = targetRatePerSecond - generatesThisSecond; // Finish the remainder.
                }
                else // Determine whether we need to catch up.
                {
                    int expectedCountThisSecond = i * averageGeneratesPerPass;
                    if (expectedCountThisSecond > generatesThisSecond)
                    {
                        generatesForThisPass += expectedCountThisSecond - generatesThisSecond;
                    }
                }
                
                for (int j = 0; j < generatesForThisPass; ++j)
                {                        
                    doAsyncMessageGenerate();
                    ++generatesThisSecond;
                    long deltaSeconds = (System.currentTimeMillis() - startTimeMillis) / 1000;
                    if (deltaSeconds != currentSecond)
                    {
                        missedTarget = true;
                        break;
                    }
                }
                
                if (!missedTarget)
                {
                    try
                    {
                        long currentTimeMillis = System.currentTimeMillis();
                        long pauseMillis = suggestedPauseMillis;
                        if (((currentTimeMillis + suggestedPauseMillis - startTimeMillis) / 1000) != currentSecond)
                        {
                            // Can't fit a sleep in; if we haven't hit the target yet continue into another generate pass immediately.
                            if (generatesThisSecond < targetRatePerSecond)
                                continue;
                            else // Done generating. Sleep only as long as we need to.
                                pauseMillis = (currentSecond + 1) - ((currentTimeMillis - startTimeMillis) / 1000);
                        }
                        
                        Thread.sleep(pauseMillis);
                    }
                    catch (InterruptedException e)
                    {
                        return; // Exit.
                    }
                }
                
                long deltaSeconds = (System.currentTimeMillis() - startTimeMillis) / 1000;
                if (deltaSeconds != currentSecond)
                {
                    // Store metrics for this second, reset and advance.
                    generationCountPerSecond.add(generatesThisSecond);
                    currentSecond = deltaSeconds;
                    //System.out.println("**** Current second = " + currentSecond);
                    //System.out.println("**** How many generated this second = " + generatesThisSecond);
                    // Make sure you reset this only once in the whole loop
                    generatesThisSecond = 0;                    
                }
            }
        }
    }
    
    protected void doAsyncMessageGenerate()
    {
        ++counter;
       
        //System.out.println("Sending msg " + counter);
        AsyncMessage msg = new AsyncMessage();
        String clientID = UUIDUtils.createUUID(false);        
        msg.setDestination(destination);
        msg.setClientId(clientID);
        msg.setMessageId(UUIDUtils.createUUID(false));
        msg.setTimestamp(System.currentTimeMillis());
        msg.setBody(message);
        msgBroker.routeMessageToService(msg, null); 
    }

}
   