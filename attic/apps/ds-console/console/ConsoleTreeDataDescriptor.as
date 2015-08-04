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

package console {
    
import mx.collections.ArrayCollection;
import mx.collections.ICollectionView;
import mx.controls.treeClasses.ITreeDataDescriptor;
    
public class ConsoleTreeDataDescriptor implements ITreeDataDescriptor {

        public function getChildren(node:Object, model:Object=null):ICollectionView
	{
	    if (node == null)
	        return null;
		else if (node.hasOwnProperty("children") && (node.children.length > 0))
		    return new ArrayCollection(node.children);
		else
    		return null;
	}
	
	public function hasChildren(node:Object, model:Object=null):Boolean
        {
            return true;
        }


	public function isBranch(node:Object, model:Object=null):Boolean
	{
	    if (node == null)
	        return false;
	    else
    		return (node.hasOwnProperty("children") && (node.children.length > 0)) ? true : false;
	}
	
	public function getData(node:Object, model:Object=null):Object 
	{
		return node;
	}

          
        public function addChildAt(node:Object, child:Object, index:int, model:Object=null):Boolean
	{
           return true;
	} 

       public function removeChildAt(node:Object, child:Object, index:int, model:Object=null):Boolean
       {
            return true;
       }
    
}

}