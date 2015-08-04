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

import console.events.ManagementOperationInvokeEvent;

import mx.containers.*;
import mx.messaging.management.MBeanInfo;

//--------------------------------------
//  Events
//--------------------------------------

/**
 * Broadcast when a request has been made to invoke an Operation within this OperationSet.
 */
[Event(name="invoke", type="console.events.ManagementOperationInvokeEvent")]

/**
 * The OperationSet is a VBox containing an Operation for each operation exposed by a
 * MBean. When the mbeanInfo property is set, the current UI of the OperationSet is torn
 * down and new UI is built based upon the MBeanInfo metadata provided.
 *
 * <p><b>MXML Syntax</b></p>
 *
 * <p>The <code>&lt;mx:OperationSet&gt;</code> tag inherits all the properties
 * of its parent classes and adds the following properties:</p>
 *
 * <p>
 * <pre>
 * &lt;mx:Button
 *   mbeanInfo="<i>No default</i>."
 * /&gt;
 * </pre>
 * </p>
 */
public class OperationSet extends VBox
{
	//--------------------------------------------------------------------------
	//
	//  Constructor
	//
	//--------------------------------------------------------------------------

	/**
	 *  @private
	 *  Constructor.
	 */
	public function OperationSet()
	{
		super();
	}




	//----------------------------------
	//  MBeanOperationInfo
	//----------------------------------
	
	private var _mbeanName:String;
	public function set mbeanName(name:String):void
	{
	    _mbeanName = name;
	}

	/**
	 *  @private
	 *  Storage for the Mbean info.
	 */
	private var _mbeanInfo:MBeanInfo;

	/**
	 *  MBean metadata to drive the UI for this component.
	 */
	public function get mbeanInfo():MBeanInfo
	{
		return _mbeanInfo;
	}
	
		
	private var _tabnav:TabNavigator;
	public function set tabnav(p:TabNavigator):void
	{
	    _tabnav = p;
	}

	public function set mbeanInfo(value:MBeanInfo):void
	{
                var i:int;

		_mbeanInfo = value;
		// Remove any existing children.
		for (i = numChildren - 1; i >= 0; --i)
		{
			removeChildAt(i);
		}
		if (value != null)
		{
			var n:int = value.operations.length;

            // If there are no operations for this MBean, disable the Operations tab.
            if (n == 0)
                _tabnav.getTabAt(1).enabled = false;

            // Otherwise, build UI for the set of operations exposed by this MBean.
            else
            {
                _tabnav.getTabAt(1).enabled = true;
                for (i = 0; i < n; ++i)
                {
                    var op:Operation = new Operation(this);
                    addChild(op);
                    op.operationInfo = value.operations[i];
                }
            }
		}
	}

	/**
	 *  Raises an operation invoke event for a nested Operation.
	 */
	public function dispatchInvokeEvent(name:String, values:Array, signature:Array):void
	{
		var event:ManagementOperationInvokeEvent = new ManagementOperationInvokeEvent(_mbeanName, name, values, signature);
		dispatchEvent(event);
	}

}

}