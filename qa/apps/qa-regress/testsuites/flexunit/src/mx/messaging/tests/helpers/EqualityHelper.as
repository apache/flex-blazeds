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
package mx.messaging.tests.helpers
{

import flash.xml.*;

import mx.logging.*;
import mx.collections.XMLListCollection;
import mx.utils.*;

public class EqualityHelper
{
    public var log:ILogger;
    public var known:Array;

    public function EqualityHelper()
    {
        log = Log.getLogger("mx.messaging.tests.helpers.EqualityHelper");
        reset();
    }

    public function reset():void
    {
        known = [];
    }

    public function testEquality(o1:Object, o2:Object):Boolean
    {
        try
        {
            if (o1 == null)
            {
                return (o1 == o2);
            }

            if (o1 is Boolean || o1 is String || o1 is Number || o1 is int || o1 is uint)
            {
                return (o1 == o2);
            }
            else if (o1 is Date)
            {
                return o1.time == o2.time;
            }
            else if (o1 is XML)
            {
                return testXMLEquality(XML(o1), XML(o2));
            }
            else if (o1 is XMLDocument)
            {
                return testXMLDocumentEquality(XMLDocument(o1), XMLDocument(o2));
            }
            else if (o1 is XMLNode)
            {
                return testXMLNodeEquality(XMLNode(o1), XMLNode(o2));
            }
            else
            {
                if (knownObject(o1))
                {
                    return true; // We're already testing this object, return true to avoid looping
                }

                rememberObject(o1);

                if (o1 is Array)
                {
                    return testArrayEquality(o1 as Array, o2 as Array);
                }
                else
                {
                    return testObjectEquality(o1, o2);
                }
            }
        }
        catch (e:Error)
        {
            log.error("EqualityHelper Error: o1 != o2");
            log.error(e.toString());
        }

        return false;
    }

    public function testArrayEquality(a1:Array, a2:Array):Boolean
    {
        if (a1.length != a2.length)
        {
            return false;
        }

        for (var i:uint = 0; i < a1.length; i++)
        {
             if (!testEquality(a1[i], a2[i]))
             {
                return false;
             }
        }

        return true;
    }

    public function testObjectEquality(o1:Object, o2:Object):Boolean
    {
        // ObjectUtil Class Information:
        // name - The class name
        // alias - The registered class alias
        // properties - An Array of class properties, both dynamic and sealed

        var info1:Object = ObjectUtil.getClassInfo(o1);
        var info2:Object = ObjectUtil.getClassInfo(o2);

        // NOTE: For now, allow an anonymous Objects to be compared to typed.
        if (info1.name != "Object" && info2.name != "Object" && info1.name != info2.name)
        {
            return false;
        }

        if (info1.alias != info2.alias)
        {
            return false;
        }

        var prop:QName;
        for each (prop in info1.properties)        
        {
            var val1:Object = o1[prop];
            var val2:Object = o2[prop];

            if (!testEquality(val1, val2))
            {
                return false;
            }
        }

        return true;
    }

    public function testXMLEquality(o1:XML, o2:XML):Boolean
    {
        //log.debug("Testing nodes named {0} = {1} ", o1.name(), o2.name());

        if (o1.name() != o2.name())
        {
            return false;
        }

        //"text", "comment", "processing-instruction," or "attribute".
        var kind1:String = o1.nodeKind();
        var kind2:String = o2.nodeKind();

        if (kind1 != kind2)
        {
            return false;
        }

        if (kind1 == "element")
        {
            var attributes1:XMLList = o1.attributes();
            var attributes2:XMLList = o2.attributes();

            if (attributes1.length() != attributes2.length())
            {
                return false;
            }

            for (var i:uint = 0; i < attributes1.length(); i++)
            {
                var a1:XML = attributes1[i];
                var a2:XML = attributes2[i];

                if (!testXMLEquality(a1, a2))
                {
                    return false;
                }
            }

            var elements1:XMLList = o1.elements();
            var elements2:XMLList = o2.elements();

            for (i = 0; i < elements1.length(); i++)
            {
                var e1:XML = elements1[i];
                var e2:XML = elements2[i];

                if (!testXMLEquality(e1, e2))
                {
                    return false;
                }
            }

            var text1:XMLList = o1.text();
            var text2:XMLList = o2.text();

            for (i = 0; i < text1.length(); i++)
            {
                var t1:XML = text1[i];
                var t2:XML = text1[i];

                if (!testXMLEquality(t1, t2))
                {
                    return false;
                }
            }
        }
        else
        {
            //log.debug("Testing {0} nodes, {1} = {2}", kind1, o1.toString(), o2.toString());

            if (o1.toString() != o2.toString())
            {
                return false;
            }

        }

        return true;
    }

    public function testXMLDocumentEquality(o1:XMLDocument, o2:XMLDocument):Boolean
    {
        var x1:XMLNode = o1.firstChild;
        var x2:XMLNode = o2.firstChild;
        return testXMLNodeEquality(x1, x2);
    }

    public function testXMLNodeEquality(o1:XMLNode, o2:XMLNode):Boolean
    {
        //log.debug("Testing XMLNodes named {0} = {1} ", o1.nodeName, o2.nodeName);

        if (o1.nodeName != o2.nodeName)
        {
            return false;
        }

        // nodeType of 1 is ELEMENT, nodeType of 3 is TEXT
        var kind1:uint = o1.nodeType;
        var kind2:uint = o2.nodeType;

        if (kind1 != kind2)
        {
            return false;
        }

        if (kind1 == XMLNodeType.ELEMENT_NODE)
        {
            var attributes1:Object = o1.attributes;
            var attributes2:Object = o2.attributes;

            for (var a:String in attributes1)
            {
                var a1:Object = attributes1[a];
                var a2:Object = attributes2[a];

                if (!testEquality(a1, a2))
                {
                    return false;
                }
            }

            var children1:Array = o1.childNodes;
            var children2:Array = o2.childNodes;

            for (var i:uint = 0; i < children1.length; i++)
            {
                var c1:XMLNode = children1[i];
                var c2:XMLNode = children2[i];

                if (!testXMLNodeEquality(c1, c2))
                {
                    return false;
                }
            }
        }
        else
        {
            //log.debug("Testing XMLNode Values, {1} = {2}", kind1, o1.nodeValue, o2.nodeValue);

            if (o1.nodeValue != o2.nodeValue)
            {
                return false;
            }

        }

        return true;
    }
    
    /**
     *  Tests whether two XML or XMLList instances are equivalent.
     *  Specific ordering of attributes and child elements is not taken into account
     *  but the unordered set of elements and attributes in the first instance must
     *  match the set of unordered elements and attributes in the second.
     *  Differences in the set of elements or attributes as well as differences in the
     *  value for a corresponding element or attribute make the two instances nonequivalent.
     * 
     *  Note: Legacy XMLDocument and XMLNode instances are not supported.
     */
    public function testXMLEquivalency(o1:Object, o2:Object, useFilter:Boolean = false):Boolean
    {
        if (o1 is XMLDocument || o1 is XMLNode || o2 is XMLDocument || o2 is XMLNode)
            return false;
                        
        var root:XML = coerceObjectToXMLInstance(o1);
        var documentToMatch:XML = coerceObjectToXMLInstance(o2);
        if (root == null || documentToMatch == null)
            return false;      
        
        return testEquivalence(root, documentToMatch, useFilter);
    }   
            
    private function testEquivalence(expected:XML, actual:XML, useFilter:Boolean = false):Boolean
    {
        if ((expected.nodeKind() != actual.nodeKind()) || 
            (expected.hasSimpleContent() != actual.hasSimpleContent()) || 
            (expected.hasComplexContent() != actual.hasComplexContent()) || 
            (expected.name() != actual.name())) 
        {        	
            return false;
        }
            
        // Test current node data if the node has simple content and is text.
        // Comments and processing instructions don't need to match exactly.
        if (expected.nodeKind() == "text" && expected.hasSimpleContent() && (expected.toString() != actual.toString()))
        {
            return false;
        }
        
        // Test attributes
        var i:int, j:int;
        var expectedAttribs:XMLList = expected.attributes();        
        var actualAttribs:XMLList = actual.attributes();
        
   
        if (useFilter) {
        	filterAttributeList(expectedAttribs);	
        	filterAttributeList(actualAttribs);	
    	}

  
        if (expectedAttribs.length() != actualAttribs.length()) {
            return false;
        }
        var numRequiredMatches:int = expectedAttribs.length();
        var numMatches:int = 0;
        var matchedPositions:Object = {};
        for (i = 0; i < expectedAttribs.length(); i++)
        {
            for (j = 0; j < actualAttribs.length(); j++)
            {
                // If we've already matched at this position continue.
                if (matchedPositions.hasOwnProperty(j))
                    continue;

                if ((expectedAttribs[i].name() == actualAttribs[j].name()) && (expectedAttribs[i].toString() == actualAttribs[j].toString()))
                {
                    // Match.
                    matchedPositions[j] = true;
                    ++numMatches;
                    break;
                }
            }
        }        
        if (numMatches != numRequiredMatches) {
            return false; // Unmatched attributes.   
        }
        
            
        // Test child elements
        var expectedChildren:XMLList = expected.children();
        var actualChildren:XMLList = actual.children();
        var numActualToMatch:int = 0;
        numRequiredMatches = 0;
        numMatches = 0;
        matchedPositions = {};
        var ignoredExpectedPositions:Object = {};
        var ignoredActualPositions:Object = {};
        // Ignore comments and processing instructions.
        var child:XML;
        for (i = 0; i < expectedChildren.length(); i++)
        {
            child = expectedChildren[i];
            if (child.nodeKind() == "processing-instruction" || child.nodeKind() == "comment")
                ignoredExpectedPositions[i] = true; // Ignore this child in the matching pass.                
            else
                ++numRequiredMatches;
        }
        for (i = 0; i < actualChildren.length(); i++)        
        {
            child = actualChildren[i];
            if (child.nodeKind() == "processing-instruction" || child.nodeKind() == "comment")
                ignoredActualPositions[i] = true; // Ignore this child in the matching pass.
            else
                ++numActualToMatch;
        }
        if (numRequiredMatches != numActualToMatch)
            return false;
        // Try to match all required children.
        for (i = 0; i < expectedChildren.length(); i++)
        {
            if (ignoredExpectedPositions.hasOwnProperty(i))
                continue;
            
            for (j = 0; j < actualChildren.length(); j++)
            {
                if (ignoredActualPositions.hasOwnProperty(j) || matchedPositions.hasOwnProperty(j))
                    continue; // Ignore or already matched.
                
                if (testEquivalence(expectedChildren[i], actualChildren[j]))
                {
                    // Match.
                    matchedPositions[j] = true;
                    ++numMatches;
                    break;
                }
            }
        }
        if (numMatches != numRequiredMatches)
            return false; // Unmatched children.
            
        return true;
    }
    
    private function coerceObjectToXMLInstance(o:Object):XML
    {          
        // In the XMLList case, as long as the length is 1 we can treat it as an XML.
        if (o is XMLList && XMLList(o).length() == 1)
        {
            return XMLList(o)[0];
        }
        else if (o is XML)
        {
            return o as XML;
        }
        else
        {
            return null;
        }
    }

    private function rememberObject(o:Object):void
    {
        known.push(o);
    }

    private function knownObject(o1:Object):Boolean
    {
        for (var i:uint = 0; i < known.length; i++)
        {
            if (o1 === known[i])
            {
                return true;
            }
        }

        return false;
    }
    
    // When called this will remove all attributes with xsi namespace.  
    // Others can be added here or the method could be converted to accept
    // a list of items to be filtered if needed.
    private function filterAttributeList(list:XMLList):void
    {
    	
    	var xsi:String = "http://www.w3.org/2001/XMLSchema-instance";
    	var filteredList:XMLListCollection = new XMLListCollection();
    	
    	for (var i:int = 0; i < list.length(); i++) 
    	{
    		if (list[i].namespace().toString() == xsi) {
    			delete(list[i]);
			} 
		}	
	}
	
}

}