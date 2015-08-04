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
package
{
	import flash.events.Event;
	
	import mx.collections.IList;
	import mx.events.CollectionEvent;
	
	import spark.components.ComboBox;
	
	public class BindableComboBox extends ComboBox
	{
		private var _value:Object;
		
		public var valueField:String = "data";
		
		public function set value(value:Object):void 
		{
			_value = value;
			selectIndex();	
		}
		
		public function get value():Object
		{
			return selectedItem[valueField];
		}
		
		override public function set dataProvider(dataProvider:IList):void 
		{
			super.dataProvider = dataProvider;
			dataProvider.addEventListener(CollectionEvent.COLLECTION_CHANGE, 
				function(event:Event):void
				{
					selectIndex();
				});
			selectIndex();
		}
		
		private function selectIndex():void
		{
			if (!_value || !dataProvider)
			{
				return;
			}
			for (var i:int = 0; i < dataProvider.length; i++) 
			{
				if (_value == dataProvider[i][valueField])
				{
					selectedIndex = i;
					return;
				}
			}
		}
		
	}
}