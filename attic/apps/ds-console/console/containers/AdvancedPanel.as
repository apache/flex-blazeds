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
    import console.containers.*;
    import console.events.*;
    import mx.collections.ListCollectionView;
    import mx.rpc.events.ResultEvent;
    import mx.messaging.management.MBeanInfo;
    import mx.messaging.management.MBeanAttributeInfo;
    import mx.events.*;
    import mx.controls.Tree;
    import mx.controls.DataGrid;
    import flash.events.Event;
    import flash.events.MouseEvent;
    import mx.containers.Box;

    public class AdvancedPanel extends UpdateListener
    {
        private var display:AdvancedPanelDisplay;
        private var _manager:ConsoleManager;
        private var _currentMBeanName:String;
        private var _currentMBeanInfo:MBeanInfo;
        
        public function AdvancedPanel():void
        {
            super();
            initialize();

            percentWidth = 100;
            percentHeight = 100;
            
            display = new AdvancedPanelDisplay();
            this.addChild(display);
            
            label = "Generic Administration View";
            
            display.mbeanTree.addEventListener(ListEvent.CHANGE, manageMBean);
            display.attributeGrid.addEventListener(ListEvent.CHANGE, showSelectedValue);
            display.refreshButton.addEventListener(MouseEvent.CLICK, handleRefreshClick);
            display.operationsUI.tabnav = display.main;
            
            _manager = ConsoleManager.getInstance();
            _manager.registerListener(this, []);
            display.operationsUI.addEventListener(ManagementOperationInvokeEvent.INVOKE, _manager.invokeOperation);
        }
        
        public override function mbeanModelUpdate(model:Object):void
        {
            display.mbeanTree.dataProvider = model;
        }
         
       /**
         * Exposes a selected MBean for management.
         */
        private function manageMBean(e:Event):void
        {
            var node:Object = display.mbeanTree.selectedItem;
            if (node && node.hasOwnProperty("objectName"))
            {
                _currentMBeanName = node["objectName"].canonicalName;
                display.mbeanNameText.text = _currentMBeanName;
                _manager.getMBeanInfo(_currentMBeanName, this, handleMBeanInfo);
            }
        }

        /**
         * Handles the result of getMBeanInfo().
         */
        public function handleMBeanInfo(e:ResultEvent):void
        {
            _currentMBeanInfo = MBeanInfo(e.result);
            // Update string descriptions for the selected MBean.
            display.mbeanClassText.text = _currentMBeanInfo.className;
            display.mbeanDescriptionText.text = _currentMBeanInfo.description;
            display.mbeanInfoOutput.text = _currentMBeanInfo.toString();
            // Update the Operations tab.
            display.operationsUI.mbeanInfo = _currentMBeanInfo;
            display.operationsUI.mbeanName = _currentMBeanName;
            // Fetch current attribute values for the selected MBean.
            // Clear out currently selected attribute value.
            display.selectedValueText.text = "";
            
            if (_currentMBeanInfo.attributes.length)
            {
                _manager.getAllAttributes(_currentMBeanName, _currentMBeanInfo, this, handleAttributes);
            }
        }

        /**
         * Handles the refresh button click.
         */ 
        private function handleRefreshClick(event:MouseEvent):void
        {
            refreshAttributes();    
        }   
         
        /**
         * Refreshes the displayed attribute values for the currently selected MBean.
         */
        private function refreshAttributes():void
        {
            if (_currentMBeanInfo != null)
            {
                _manager.getAllAttributes(_currentMBeanName, _currentMBeanInfo, this, handleAttributes);
            }
        }

        /**
         * Handles the result of getAttributes().
         */
        private function handleAttributes(e:ResultEvent):void
        {
            var attribs:Array = e.result as Array;
            var attribLength:int = attribs.length;
            display.attributeGrid.dataProvider = e.result;
            // Show selected item value.
            var index:int = display.attributeGrid.selectedIndex;
            if (index != -1)
            {
		        var item:Object = ListCollectionView(display.attributeGrid.dataProvider).getItemAt(index);
                if ((item != null) && (item.value != null))
                {
                    display.selectedValueText.text =  item.value.toString();
                }
            }
            else
            {
                display.selectedValueText.text = "";
            }
        }
        
        
        /**
         * Hack function to show the selected grid value - we should get rid of this with
         * a nice attributes grid that renders values nicely in a selectable fashion.
         */
        private function showSelectedValue(e:Event):void
        {
            var value:Object = DataGrid(e.target).selectedItem.value;
            if (value != null)
            {
                display.selectedValueText.text =  value.toString();
            }
            else
            {
                display.selectedValueText.text = "null";
            }
        }
        
    }
}