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
package console.data
{
    import mx.collections.ArrayCollection;
    import mx.utils.ObjectProxy;
    import flash.events.Event;
    import mx.events.PropertyChangeEvent;
    import mx.events.CollectionEvent;
    import mx.controls.listClasses.ListBase;
    
    public class Bindable3DHashMap
    {
        public var objects:ObjectProxy;
        public var objectsForKeyArray:ObjectProxy;
        
        public function Bindable3DHashMap()
        {
            objects = new ObjectProxy;
            objectsForKeyArray = new ObjectProxy;
        }
        
        public function updateNoPollable(object:String, key:String, value:*):void
        {
		    if (!objectsForKeyArray.hasOwnProperty(object))            
		        objectsForKeyArray[object] = new ArrayCollection;
		        
		    if (!key)
		        return;
		        
		    var keysForKeyArray:ArrayCollection = objectsForKeyArray[object] as ArrayCollection;
            
			var foundKey:Boolean = false;
			for (var i:int = 0; i < keysForKeyArray.length; i++)
			{
			    if ((keysForKeyArray[i].hasOwnProperty("Property")) && (keysForKeyArray[i]["Property"] == key))
			    {
			        keysForKeyArray[i]["Value"] = value;
			        foundKey = true;
			        break;
			    }
			}
			
			if (!foundKey)
			    keysForKeyArray.addItem({Property: key, Value:value});
			    
			keysForKeyArray.dispatchEvent(new CollectionEvent(CollectionEvent.COLLECTION_CHANGE));
        }
        
		public function update(object:String, key:String, value:*):void
		{
		    if (!objects.hasOwnProperty(object))
		    {
		        objects[object] = new ObjectProxy;
		    }
		    
		    if (!key)
		        return;
		        
		    var keys:ObjectProxy = objects[object] as ObjectProxy;
		    
			if (!keys.hasOwnProperty(key))
			    keys[key] = new ArrayCollection;

			(keys[key] as ArrayCollection).addItem({Name: key, Value: value});
			(keys[key] as ArrayCollection).dispatchEvent(new CollectionEvent(CollectionEvent.COLLECTION_CHANGE));
			
			updateNoPollable(object, key, value);
		}
		
		public function getBindableKeyArray(object:String):ArrayCollection
		{
		    if (!objectsForKeyArray.hasOwnProperty(object))            
		        objectsForKeyArray[object] = new ArrayCollection;
		        
		    return (objectsForKeyArray[object] as ArrayCollection);
		}
		
		public function getBindableDataArray(object:String, key:String):ArrayCollection
		{
		    if (!objects.hasOwnProperty(object))
		        objects[object] = new ObjectProxy;
		        
		    var keys:ObjectProxy = objects[object] as ObjectProxy;
		    return keys[key] as ArrayCollection;
		}
		
		public function clearData():void
		{
		    objects = new ObjectProxy;
		    objectsForKeyArray = new ObjectProxy;
		}
    }
}