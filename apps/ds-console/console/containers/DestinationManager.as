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
package console.containers
{
    import console.data.Bindable3DHashMap;
    import console.ConsoleManager;
    import mx.collections.ArrayCollection;
    import mx.events.ListEvent;
    import flash.events.Event;
    import mx.charts.LineChart;
    import mx.charts.series.LineSeries;
    import mx.controls.listClasses.ListBase;
    import mx.events.CollectionEvent;
    
    public class DestinationManager extends UpdateListener
    {
        private static const pollableTypes:Array = [
            ConsoleManager.DESTINATION_POLLABLE ];
            
        private var _manager:ConsoleManager;
        private var display:DestinationManagerDisplay;
        private var selectedDestinationId:String;
        private var selectedMBean:String;
        private var dataCache:Bindable3DHashMap;
        private var pollableAttributes:Object;
        
        [Bindable]
        private var selectedBeanProperties:ArrayCollection;
        
        [Bindable]
        private var selectedPropertyValues:ArrayCollection;
        
        public override function dataUpdate(type:int, data:Object):void
        {
            var dataArray:ArrayCollection = new ArrayCollection;
            
            // This finds any exposed properties under any destination node and
            // adds them to the proper data structures so that all data in MBeans
            // underneath destinations can be accesed by the destination's id 
            for (var name:String in data)
            {
                var mapKey:String;
                var mapValue:String;
                
                // Gets all properties for this MBean
                var propertyArrayForMBean:Array = data[name];
                
                // split the mbean name on the commas, then split on the periods and get the last
                // type.  if it contains destination, insert it into the array
                // otherwise find the destination tag and insert it into it's data cache key.
                var mbeanNames:Array = name.split(",");
                
                // find the actual type of this MBean, and if it is a Destination, we can use the id
                // as the map key, otherwise, we have to find the MBean's parent which is a Destination
                var types:Array = (mbeanNames[mbeanNames.length - 1] as String).split(".");
                if ((types[types.length - 1] as String).indexOf("Destination") >= 0)
                {
                	// we are going after the id pairing here, most often it will be the second
                	// to last pair but since WAS 6.1 inserts nodes and servers into 
                	// the MBean name it may not be the case.  For optimization we'll still
                	// go after the second to last pair, but will verify that this is indeed
                	// the id pair and if not will look for it explicitly.
                	var namePair:Array = (mbeanNames[mbeanNames.length - 2] as String).split("=");
                	                	               
                    mapKey = namePair[0];                    
                    mapValue = namePair[1];     
                    if (mapKey != "id")
                    {
                    	// did not find id where expected go through entire name pair collection
                    	// looking for the id pair
	                    for each (var mbeanNamePair:String in mbeanNames)
	                    {
	                    	var loopNamePair:Array = mbeanNamePair.split("=");
	                    	mapKey =loopNamePair[0];
	                    	mapValue = loopNamePair[1];	                    	
	                        if (mapKey == "id")
	                        {
	                            break;
	                        }
	                    }                    	                    	
                    }
                }
                else
                {
                    for each (var id:String in mbeanNames)
                    {
                        if (id.indexOf("Destination") >= 0)
                        {
                            // Get the Destination's id name as the key into the map
                            mapValue = id.split("=")[1];
                            break;
                        }
                    }
                }
                
                // For each property, add the property and value to the appropriate map
                for (var i:int = 0; i < propertyArrayForMBean.length; i++)
                {
                    if (pollableTypes.indexOf(type) >= 0)
                        dataCache.update(mapValue, propertyArrayForMBean[i].name, propertyArrayForMBean[i].value);
                    else
                        dataCache.updateNoPollable(mapValue, propertyArrayForMBean[i].name, propertyArrayForMBean[i].value)
                }
            }
        }
        
        public override function mbeanModelUpdate(model:Object):void
        {
            refreshDestinations();
        }

        public function DestinationManager():void
        {
            super();
            display = new DestinationManagerDisplay;
            this.addChild(display);
            
            this.label = "Destination Management";
            
            _manager = ConsoleManager.getInstance();
            var registerTypes:Array = [ 
                {type: ConsoleManager.DESTINATION_GENERAL, poll: false},
                {type: ConsoleManager.DESTINATION_POLLABLE, poll: true},
            ];
            
            _manager.registerListener(this, registerTypes);
            display.dataList.addEventListener(ListEvent.CHANGE, selectedService);
            display.httpList.addEventListener(ListEvent.CHANGE, selectedService);
            display.messageList.addEventListener(ListEvent.CHANGE, selectedService);
            display.remotingList.addEventListener(ListEvent.CHANGE, selectedService);
            
            display.destinationAttributes.addEventListener(ListEvent.CHANGE, selectedAttribute);

            pollableAttributes = new Object;
            dataCache = new Bindable3DHashMap();
            
            selectedBeanProperties = new ArrayCollection();
            selectedPropertyValues = new ArrayCollection();
        }
        
        private function selectedAttribute(e:Event):void
        {
            var property:String = e.target.selectedItem.Property as String;
            var ret:ArrayCollection = dataCache.getBindableDataArray(selectedDestinationId, property);
            if ((ret != null) && (ret.length >= 0))
            {
                display.attrgraph.dataProvider = ret;
                display.graphLabel.text = property;
            }
            /*
            return;

            for (var type:* in pollableAttributes)
            {
                for each (var pollable:Object in pollableAttributes[type][selectedDestinationId])
                {
                    if (pollable.name == property) 
                    {
                        display.attrgraph.dataProvider = dataCache.getBindableDataArray(selectedDestinationId, property);
                        display.graphLabel.text = property;
                        return;
                    }
                }
            }
            */
        }
        
        private function selectedService(e:Event):void
        {
            selectedDestinationId = e.currentTarget.selectedItem.label;
            selectedMBean = e.currentTarget.selectedItem.objectName.canonicalName.split(":")[1];
            display.destinationAttributes.dataProvider = dataCache.getBindableKeyArray(selectedDestinationId);
            display.selectedProperty.text = e.currentTarget.selectedItem.label;
            display.graphLabel.text = null;       
            display.attrgraph.dataProvider = null;   
            
            if (e.currentTarget != display.httpList)
                display.httpList.selectedIndex = -1;
            if (e.currentTarget != display.messageList)
                display.messageList.selectedIndex = -1;
            if (e.currentTarget != display.dataList)
                display.dataList.selectedIndex = -1;    
            if (e.currentTarget != display.remotingList)
                display.remotingList.selectedIndex = -1;  
        }

        public override function appChanged(s:String):void
        {
            dataCache.clearData();
            refreshDestinations();
            display.destinationAttributes.dataProvider = null;
            display.selectedProperty.text = null;
            display.graphLabel.text = null;       
            display.attrgraph.dataProvider = null;     
        }
        
        private function refreshDestinations():void
        {
            // TODO: Handle not having any specific services gracefully
            display.dataList.dataProvider = getChildTree("DataService");
            display.httpList.dataProvider = getChildTree("HTTPProxyService");
            display.messageList.dataProvider = getChildTree("MessageService");
            display.remotingList.dataProvider = getChildTree("RemotingService");
        }
        
        /**
        * This is just a helper method to find the proper MBeans for each Service
        * from the ConsoleManager's model.  The model is a complicated structure
        * of maps and arrays.
        */
        private function getChildTree(child:String):Array
        {
            var childArray:Array = _manager.getChildTree(child);
            if ((childArray.length > 0) && (childArray[0].hasOwnProperty("children")))
                childArray = childArray[0].children as Array;
            if ((childArray.length > 0) && (childArray[0].hasOwnProperty("children")))
                childArray = childArray[0].children as Array;            
                    
            return childArray;
        }
    }
}