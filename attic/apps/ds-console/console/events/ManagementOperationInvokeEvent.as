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

package console.events
{

import flash.events.Event;

/**
 * Used to request that an MBean operation be invoked.
 */
public class ManagementOperationInvokeEvent extends Event
{
    /**
     * Constructs an instance of this event with the specified type, target,
     * and message.
     */
    public function ManagementOperationInvokeEvent(mbean:String, name:String, values:Array, signature:Array)
    {
        super(INVOKE, false, false);
        _mbean = mbean;
        _name = name;
        _values = values;
        _signature = signature;
    }
    
    /**
     * The mbean of the operation to invoke.
     */
    public function get mbean():String
    {
        return _mbean;
    }
    
    /**
     * The name of the operation to invoke.
     */
    public function get name():String
    {
        return _name;
    }
    
    /**
     * The argument values for the operation.
     */
    public function get values():Array
    {
    	return _values;	
    }
    
    /**
     * The type signature for operation arguments.
     */
    public function get signature():Array
    {
    	return _signature;
    }

	/**
     * Because this event can be re-dispatched we have to implement clone to
     * return the appropriate type, otherwise we will get just the standard
     * event type.
	 *
     * @return Clone of this <code>ManagementOperationInvokeEvent</code>
	 */
	override public function clone():Event
	{
	    return new ManagementOperationInvokeEvent(_mbean, _name, _values, _signature);
	}

    /**
     * The event type.
     */
    public static const INVOKE:String = "invoke";
	
    // private members    
    private var _name:String;
    private var _values:Array;
    private var _signature:Array;
    private var _mbean:String;
}

}
