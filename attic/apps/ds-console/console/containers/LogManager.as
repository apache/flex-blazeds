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
    import flash.events.Event;
    import mx.controls.ComboBox;
    import console.events.ManagementOperationInvokeEvent;
    import mx.events.ListEvent;
    import flash.events.MouseEvent;
    import mx.collections.ArrayCollection;
    
    public class LogManager extends UpdateListener
    {
        private var _manager:ConsoleManager;
        private var _logMBean:String;
        private var _logTree:Array;
        private var display:LogManagerDisplay;
        
        public static var targetLevels:Array = new Array(
                {label:"NONE", value:"2000"},
                {label:"FATAL", value:"1000"},
                {label:"ERROR", value:"8"},
                {label:"WARN", value:"6"},
                {label:"INFO", value:"4"},
                {label:"DEBUG", value:"2"},
                {label:"ALL", value:"0"}
            );
        
        public function LogManager():void
        {
            super();
            
            display = new LogManagerDisplay;
            this.addChild(display);
            
            this.label = "Log Manager";
            
            _manager = ConsoleManager.getInstance();
            _manager.registerListener(this, []);
            
            // On selecting a log target, invoke 'getTargetFilters' to populate the table of filters
            display.logTargetSelect.addEventListener(ListEvent.CHANGE, selectLogTarget);
            display.logTargetLevel.dataProvider = targetLevels;
            
            // Handler for removing a target
            display.deleteCategory.addEventListener(MouseEvent.CLICK,
                function(e:Event):void {
                    
                    _manager.invokeOperation(
                    
                    new ManagementOperationInvokeEvent(_logMBean, 
                        "removeFilterForTarget", 
                        [display.logTargetSelect.selectedItem, display.currentCategories.selectedItem], 
                        ["java.lang.String", "java.lang.String"]),
                        
                        function (e:Event):void { display.logTargetSelect.dispatchEvent(
                            new Event(ListEvent.CHANGE)); } );
                }
            );
            
            // Handler for adding a target
            display.addFilterButton.addEventListener(MouseEvent.CLICK,
                function(e:Event):void {
                    
                    _manager.invokeOperation(
                    
                    new ManagementOperationInvokeEvent(_logMBean, 
                        "addFilterForTarget", 
                        [display.logTargetSelect.selectedItem, display.addFilterTextInput.text], 
                        ["java.lang.String", "java.lang.String"]),
                        // Simple callback function to refresh the Log View
                        function (e:Event):void { 
                            display.logTargetSelect.dispatchEvent(new Event(ListEvent.CHANGE));
                            display.addFilterTextInput.text = "";
                        } 
                    );
                }
            );
            
            // Handler for adding a target
            display.addCommonFilterButton.addEventListener(MouseEvent.CLICK,
                function(e:Event):void {
                    
                    _manager.invokeOperation(
                    
                    new ManagementOperationInvokeEvent(_logMBean, 
                        "addFilterForTarget", 
                        [display.logTargetSelect.selectedItem, display.commonCategories.selectedItem], 
                        ["java.lang.String", "java.lang.String"]),
                        // Simple callback function to refresh the Log View
                        function (e:Event):void { 
                            display.logTargetSelect.dispatchEvent(new Event(ListEvent.CHANGE));
                            display.addFilterTextInput.text = "";
                        } 
                    );
                }
            );
            
            // Handler for changing the target's level
            display.logTargetLevel.addEventListener(ListEvent.CHANGE, function(e:Event):void
                {
                    _manager.invokeOperation(
                    
                    new ManagementOperationInvokeEvent(_logMBean, 
                        "changeTargetLevel", 
                        [display.logTargetSelect.selectedItem, display.logTargetLevel.selectedItem.value], 
                        ["java.lang.String", "java.lang.String"]),
                        new Function
                    );
                }
            );
            
        }
        
        public override function mbeanModelUpdate(model:Object):void
        {
            _logTree = _manager.getChildTree("Log");
            if (_logTree != null && _logTree.length != 0)
            {
                _logMBean = _logTree[0]["objectName"]["canonicalName"];
                // Get the targets for the current application
                _manager.getAttributes(_logMBean, ["Targets"], this, updateTargets);
                _manager.getAttributes(_logMBean, ["Categories"], this, updateCommonCats);
            }
        }
        
        public override function appChanged(s:String):void
        {
            mbeanModelUpdate(null);
        }
        
        public function updateCommonCats(e:Event):void
        {
            display.commonCategories.dataProvider = (e["result"] as Array)[0].value;
            display.commonCategories.selectedIndex = 0;
            display.commonCategories.dispatchEvent(new Event(ListEvent.CHANGE));
        }
        
        public function updateTargets(e:Event):void
        {
            var logTargets:Array = (e["result"] as Array)[0].value;
            display.logTargetSelect.dataProvider = logTargets;
            display.logTargetSelect.selectedIndex = 0;
            display.logTargetSelect.dispatchEvent(new Event(ListEvent.CHANGE));
        }
        
        public function updateFilters(e:Event):void
        {
            display.currentCategories.dataProvider = e["result"] as Array;
        }
        
        public function selectLogTarget(e:Event):void
        {
            _manager.invokeOperation(new ManagementOperationInvokeEvent(_logMBean, 
                "getTargetFilters", [display.logTargetSelect.selectedItem], ["java.lang.String"]),
                updateFilters);
            _manager.invokeOperation(new ManagementOperationInvokeEvent(_logMBean, 
                "getTargetLevel", [display.logTargetSelect.selectedItem], ["java.lang.String"]),
                handleGetTargetLevel);
        }
        
        public function handleGetTargetLevel(e:Event):void
        {
            var currentLevelInt:String = e["message"]["body"];
            for (var i:int = 0; i < targetLevels.length; i++)
            {
                if (targetLevels[i].value == currentLevelInt)
                    display.logTargetLevel.selectedIndex = i;
            }
        }
    }
}