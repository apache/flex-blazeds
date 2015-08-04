////////////////////////////////////////////////////////////////////////////////
//
//  Licensed to the Apache Software Foundation (ASF) under one or more
//  contributor license agreements.  See the NOTICE file distributed with
//  this work for additional information regarding copyright ownership.
//  The ASF licenses this file to You under the Apache License, Version 2.0
//  (the "License"); you may not use this file except in compliance with
//  the License.  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////
// This is a hacked up version of a more complete test in \qa-regress\testsuites\mxunit\tests\messagingService
        import mx.messaging.Channel;
        import mx.messaging.ChannelSet;
        import mx.messaging.Consumer;
        import mx.messaging.Producer;
        import mx.messaging.config.*;
        import mx.messaging.events.*;
        import mx.messaging.messages.*;
                           
        //  Name of the destination to be used by the Producer and the Consumer. 
        private var destination:String = "MyTopic_MBean";          
    //     private var destination:String = "MyTopic"; 
       private var channel:Channel;
       private var pro:Producer;
       private var con:Consumer;            
      
       public function simpleMessageTest():void {  
            
            //setup the producer
            pro = new Producer();
            pro.destination = destination;           
            
            //setup the consumer
             con = new Consumer(); 
            con.destination = destination;
             
             pro.addEventListener(ChannelFaultEvent.FAULT, producerEventHandler);
             pro.addEventListener(MessageFaultEvent.FAULT, producerEventHandler);
             pro.addEventListener(MessageAckEvent.ACKNOWLEDGE, producerEventHandler);  
                                                    
             con.addEventListener(MessageFaultEvent.FAULT, consumerEventHandler);
             con.addEventListener(ChannelFaultEvent.FAULT, consumerEventHandler);
             con.addEventListener(MessageEvent.MESSAGE, consumerEventHandler);           
            /*
             * Use the chain function to setup an event handler for the consumer ack event. The message ack event needs it's own addAsync
             * method because it comes back first, as soon as the consumer has subscribed to the destination. Putting it in the same addAsync
             * call with the rest of the consumer events would cause the test to finish as soon as the message ack event was received which 
             * is not what we want to happen. We still need to have the producer send a message and make sure the consumer receives it.  
            */
                      
            con.addEventListener(MessageAckEvent.ACKNOWLEDGE, consumerAck);       		
            var cs:ChannelSet = new ChannelSet(); 
 			channel = ServerConfig.getChannel("qa-streaming-amf",false);  	
 		//	channel = ServerConfig.getChannel("qa-rtmp-ac");  			
			cs.addChannel(channel);
			
			//set the consumer to use the new channel set then subscribe the consumer
			con.channelSet = cs; 
			//set the producer to use the new channel set
            pro.channelSet = cs;              
            con.subscribe();      
          //  out.text += "\n con.subscribe \n";                                
        }
 
        //  Listen for consumer ack events so we can tell when the consumer has subscribed. 

        private function consumerAck(event:MessageAckEvent):void {
            //make sure the ack is for a subscribe operation
           // out.text += "\n begin consumerAck \n";
            if((event.correlation is CommandMessage) && (CommandMessage(event.correlation).operation == CommandMessage.SUBSCRIBE_OPERATION))
            {
                //test that the subscribe operation was for the channel we are currently testing                 
           //     Assert.assertTrue("Wrong channel: " + c.channelSet.currentChannel.id, c.channelSet.currentChannel.id == currentTest); 
                /*
                 * Since we are subscribed remove the event listener for consumer ack events. Then create a new event
                 * listener for message events using chain as we did previously. 
                 */        
                con.removeEventListener(MessageAckEvent.ACKNOWLEDGE, consumerAck);                                
                //send the message
                var msg:IMessage = new AsyncMessage();    
         //   out.text += "\n about to send message \n";
                msg.body = "hello" ;
                pro.send(msg);              
            } 
        }
        
        /**
        *  Listen for disconnect events. 
        */
        public function channelDisconnect(event:ChannelEvent):void {     
            con.removeEventListener(ChannelEvent.DISCONNECT, channelDisconnect);   
            
            Assert.hasPendingTest = false; 
        }
        /**
        *  Listen for events from the producer.    
        */
        public function producerEventHandler(event:Event):void 
        {  
            if (event is MessageAckEvent) {
                trace("Producer received ack for message");    
            } else if (event is ChannelFaultEvent) {
                con.unsubscribe();
                con.disconnect();
                var cfe:ChannelFaultEvent = event as ChannelFaultEvent;                        
                Assert.fail("Channel faulted with following error while sending message: " + cfe.faultDetail);        
            } else if (event is MessageFaultEvent) {
                con.unsubscribe();
                con.disconnect();
                var mfe:MessageFaultEvent = event as MessageFaultEvent;
                Assert.fail("Message fault while sending message: " + mfe.faultDetail);                
            }
        }
        /**
        *  Listen for events from the consumer. 
        */
        public function consumerEventHandler(event:Event):void
        {
            if (event is ChannelFaultEvent) {
                var cfe:ChannelFaultEvent = event as ChannelFaultEvent;                        
               Assert.fail("Consumer channel faulted with the following error: " + cfe.faultDetail);     
            } else if (event is MessageFaultEvent) {            
                var mfe:MessageFaultEvent = event as MessageFaultEvent;
                Assert.fail("Consumer got the following message fault: " + mfe.faultDetail);                
            } else if (event is MessageEvent) {                     
                var me:MessageEvent = event as MessageEvent;
                //get the message body from the message and store it.
                var result:String = me.message.body.toString();
                //assert we got the correct message for the channel being tested
             Assert.assertEquals(result, "hello" );
             out.text += "message result: " + result + "\n";
                /*
                 * Since we got the message remove the event listener for message events. Then create a new event
                 * listener for disconnect events using addAsync as we did previously. 
                 */ 
                con.removeEventListener(MessageEvent.MESSAGE, consumerEventHandler);   
                con.addEventListener(ChannelEvent.DISCONNECT, channelDisconnect);    
            }
            //disconnect the consumer and producer
            con.unsubscribe();
            con.disconnect();
        }

        public function disconnectProducer():void {
            pro.disconnect();
        }
