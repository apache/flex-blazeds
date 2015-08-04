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
    import mx.containers.Canvas;
    import console.data.Bindable3DHashMap;
    import mx.collections.ArrayCollection;
    import mx.messaging.management.ObjectName;
    
    public class UpdateListener extends Canvas
    {
        protected var _name:String;
        protected var _model:Object;

        /**
        * Invoked when the names of the mbeans are retrieved from the server.
        */
        public function mbeanModelUpdate(mbeanModel:Object):void
        {
            
        }
        
        /**
        * Invoked when the data for a given type is updated if the implementing class
        * is registered with the ConsoleManager for the type, and if the implementing class
        * is set to active in the ConsoleManager.
        */
        public function dataUpdate(type:int, data:Object):void
        {
            
        }
        
        /**
        * Invoked when the selected application has changed.  An implementing class might
        * want to clear any data it is holding onto since the data will be belonging to
        * objects in an application's MBeans that are no longer being queried.
        */
        public function appChanged(s:String):void
        {
            
        }

        /**
        * If the container only wishes to query a select number of MBeans upon dataUpdate, they should be
        * visible in a map keyed on the display type containing arrays of the MBean names.
        * 
        * If a null value, or any object that the ConsoleManager can't parse, then all
        * MBeans for all active types are returned upon dataUpdate.
        */
        public function get specificMBeansMap():Object
        {
            return null;
        }
        
        protected function traverseTreeForObject(node:Object, searchType:String = null, searchObject:String = null):Object
        {
            if (node == null)
            {
                return null;
            }
            else if (node.hasOwnProperty("type"))
            {
                // Node is a container of objects of 'type'
                if (searchType != null)
                {
                    if (node.type == searchObject)
                        return node;
                }
            }
            else if (node.hasOwnProperty("objectName"))
            {
                // Node is a specific object, an instance of parent 'type'
                if (searchObject != null)
                {
                    if ((node.objectName as ObjectName).getKeyProperty("id") == searchObject)
                        return node;
                }
            }
            
            // recur
            if (node.hasOwnProperty("children") && (node.children is Array))
            {
                for each (var child:Object in (node.children as Array))
                {
                    traverseTreeForObject(child, searchType, searchObject);
                }
            }
            
            // not found
            return null;
        }
    }
}