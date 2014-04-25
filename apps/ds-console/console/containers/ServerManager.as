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
    import console.ConsoleManager;
    import mx.collections.ArrayCollection;
    import mx.events.ListEvent;
    import flash.events.Event;
    import mx.charts.LineChart;
    import mx.charts.series.LineSeries;
    import mx.utils.ObjectProxy;
    import flash.utils.setInterval;
    import mx.collections.ICollectionView;
    import console.data.Bindable3DHashMap;
    import flash.events.TextEvent;
    import console.events.ManagementOperationInvokeEvent;
    import mx.controls.DataGrid;
    import mx.events.DataGridEvent;
    import mx.events.FlexEvent;
    
    public class ServerManager extends UpdateListener
    {
        private var _manager:ConsoleManager;
        private var display:ServerManagerDisplay;
        private var dataCache:Bindable3DHashMap;
        private var pollableAttributes:Object;
        private var visibleTypes:Array;
        
        public function ServerManager():void
        {
            super();
            display = new ServerManagerDisplay;
            this.addChild(display);
            
            this.label = "Server Management";
            display.scalarProperties.addEventListener(ListEvent.CHANGE, selectedScalar);
            display.pollableProperties.addEventListener(ListEvent.CHANGE, selectedPollable);
            _manager = ConsoleManager.getInstance();
            _manager.registerListener(this, [{type: ConsoleManager.GENERAL_OPERATION, poll: false},
                                             {type: ConsoleManager.GENERAL_SERVER, poll: false},
                                             {type: ConsoleManager.GENERAL_POLLABLE, poll: true}]);
            setupDataCache();
        }
        
        public override function dataUpdate(type:int, data:Object):void
        {
            var dataArray:ArrayCollection = new ArrayCollection;
            
            for (var name:String in data)
            {

                var propertyArrayForMBean:Array = data[name];
                
                for (var i:int = 0; i < propertyArrayForMBean.length; i++)
                {
                    dataCache.update(String(type), propertyArrayForMBean[i].name, propertyArrayForMBean[i].value);
                }
            }
            
            if (type == ConsoleManager.GENERAL_POLLABLE)
                pollableAttributes = data;
        }
              
        private function setupDataCache():void
        {
            dataCache = new Bindable3DHashMap();
            dataCache.update(String(ConsoleManager.GENERAL_OPERATION),null,null);
            dataCache.update(String(ConsoleManager.GENERAL_POLLABLE),null,null);
            dataCache.update(String(ConsoleManager.GENERAL_SERVER),null,null);
            
            display.scalarProperties.dataProvider = dataCache.getBindableKeyArray(String(ConsoleManager.GENERAL_SERVER));
            display.pollableProperties.dataProvider = dataCache.getBindableKeyArray(String(ConsoleManager.GENERAL_POLLABLE));
        }
        
        private function invokeOp(mbean:String, operation:String, value:String):void
        {
            _manager.invokeOperation(
                new ManagementOperationInvokeEvent(mbean, operation, [value], ["java.lang.String"]),
                function (e:Event):void {
                    
                }
            );
        }
        
        private function selectedScalar(e:Event):void
        {
            // It's possible that the data has changed since it was clicked because of polling
            // if the selected item is null, then return.
            // TODO: Handle this more gracefully.
            if (display.scalarProperties.selectedItem == -1) return;
            
            var attr:String = display.scalarProperties.selectedItem.propertyName;
            var mbean:String = display.scalarProperties.selectedItem.mbeanName;
            display.selectedProperty.text = attr;
            display.pollableProperties.selectedIndex = -1;
        }
        
        private function selectedPollable(e:Event):void
        {
            // It's possible that the data has changed since it was clicked because of polling
            // if the selected item is null, then return.
            // TODO: Handle this more gracefully.
            if (display.pollableProperties.selectedItem == null) return;
            
            var attr:String = display.pollableProperties.selectedItem.Property
            display.selectedProperty.text = attr;
            
            display.attrgraph.dataProvider = dataCache.getBindableDataArray(String(ConsoleManager.GENERAL_POLLABLE), attr);
            display.attrgraphSeries.dataProvider = dataCache.getBindableDataArray(String(ConsoleManager.GENERAL_POLLABLE), attr);
            display.scalarProperties.selectedIndex = -1;
        }
        
        public override function appChanged(s:String):void
        {
            display.attrgraph.dataProvider = null;
            display.attrgraphSeries.dataProvider = null;
            display.selectedProperty.text = "None";
            setupDataCache();
            _manager.updateData(this);
        }                
    }
}