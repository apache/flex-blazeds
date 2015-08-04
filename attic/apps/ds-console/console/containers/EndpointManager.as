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
    
    public class EndpointManager extends UpdateListener
    {
        private static const pollableTypes:Array = [
            ConsoleManager.ENDPOINT_POLLABLE ];
            
        private var _manager:ConsoleManager;
        private var display:EndpointManagerDisplay;
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
            
            for (var name:String in data)
            {

                var propertyArrayForMBean:Array = data[name];
                
                for (var i:int = 0; i < propertyArrayForMBean.length; i++)
                {
                    dataCache.update(name, propertyArrayForMBean[i].name, propertyArrayForMBean[i].value);
                }
            }
            
            if (pollableTypes.indexOf(type) >= 0)
                pollableAttributes[type] = data;
        }
        
        public override function mbeanModelUpdate(model:Object):void
        {
            refreshEndpoints();
        }

        public function EndpointManager():void
        {
            super();
            display = new EndpointManagerDisplay;
            this.addChild(display);
            
            this.label = "Endpoint Management";
            
            _manager = ConsoleManager.getInstance();
            var registerTypes:Array = [
                    {type: ConsoleManager.ENDPOINT_POLLABLE, poll: true},
                    {type: ConsoleManager.ENDPOINT_SCALAR, poll: false}
                ]
            _manager.registerListener(this, registerTypes);
            display.rtmpEndpointsList.addEventListener(ListEvent.CHANGE, selectedEndpoint);
            display.amfEndpointsList.addEventListener(ListEvent.CHANGE, selectedEndpoint);
            display.httpEndpointsList.addEventListener(ListEvent.CHANGE, selectedEndpoint);
            
            display.endpointAttributes.addEventListener(ListEvent.CHANGE, selectedAttribute);

            pollableAttributes = new Object;
            dataCache = new Bindable3DHashMap();
            
            selectedBeanProperties = new ArrayCollection();
            selectedPropertyValues = new ArrayCollection();
        }
        
        private function selectedAttribute(e:Event):void
        {
            var property:String = e.currentTarget.selectedItem.Property as String;
            for (var type:* in pollableAttributes)
            {
                for each (var pollable:Object in pollableAttributes[type][selectedMBean])
                {
                    if (pollable.name == property) 
                    {
                        display.attrgraph.dataProvider = dataCache.getBindableDataArray(selectedMBean, property);
                        display.graphLabel.text = property;
                        return;
                    }
                }
            }
        }
        
        private function selectedEndpoint(e:Event):void
        {
            selectedMBean = ((e.currentTarget.selectedItem.objectName.canonicalName as String).split(":") as Array)[1];
            display.endpointAttributes.dataProvider = dataCache.getBindableKeyArray(selectedMBean);
            display.selectedProperty.text = e.currentTarget.selectedItem.label;
            display.graphLabel.text = null;       
            display.attrgraph.dataProvider = null;   
            
            if (e.currentTarget != display.amfEndpointsList)
                display.amfEndpointsList.selectedIndex = -1;
            if (e.currentTarget != display.httpEndpointsList)
                display.httpEndpointsList.selectedIndex = -1;
            if (e.currentTarget != display.rtmpEndpointsList)
                display.rtmpEndpointsList.selectedIndex = -1;                
        }

        public override function appChanged(s:String):void
        {
            dataCache.clearData();
            refreshEndpoints();
            display.endpointAttributes.dataProvider = null;
            display.selectedProperty.text = null;
            display.graphLabel.text = null;       
            display.attrgraph.dataProvider = null;     
        }
        
        private function refreshEndpoints():void
        {
            display.httpEndpointsList.dataProvider = _manager.getChildTrees("HTTPEndpoint", "StreamingHTTPEndpoint", "NIOHTTPEndpoint", "StreamingNIOHTTPEndpoint");
            display.rtmpEndpointsList.dataProvider = _manager.getChildTree("RTMPEndpoint");
            display.amfEndpointsList.dataProvider = _manager.getChildTrees("AMFEndpoint", "StreamingAMFEndpoint", "NIOAMFEndpoint", "StreamingNIOAMFEndpoint");
        }
    }
}