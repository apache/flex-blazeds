/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package flex.messaging.io.amfx;

import flex.messaging.MessageException;
import flex.messaging.io.amf.ASObject;
import flex.messaging.io.amf.ActionMessage;
import flex.messaging.io.amf.MessageBody;
import flex.messaging.io.amf.MessageHeader;
import flex.messaging.io.SerializationContext;

import java.util.Date;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.lang.reflect.Array;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.apache.xpath.CachedXPathAPI;

/**
 * Verifies that a deserialized ActionMessage
 * from an AMFX request matches the expected value.
 *
 * @author Peter Farland
 */
public abstract class DeserializationConfirmation
{
    public static final int EXPECTED_VERSION = 3;

    private IdentityHashMap knownObjects;

    protected SerializationContext context;

    protected DeserializationConfirmation()
    {
    }

    private void init()
    {
        knownObjects = new IdentityHashMap(64);
    }

    public void setContext(SerializationContext context)
    {
        this.context = context;
    }

    public boolean isNegativeTest()
    {
        return false;
    }

    public boolean successMatches(ActionMessage m)
    {
        init();

        try
        {
            boolean match = messagesMatch(m, getExpectedMessage());
            return match;
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    public abstract ActionMessage getExpectedMessage();

    public boolean negativeMatches(ActionMessage m)
    {
        init();

        MessageException ex = null;
        MessageBody body = m.getBody(0);

        if ((body == null) || (body.getData() == null) || (!(body.getData() instanceof MessageException)))
            return false;
        else
            ex = (MessageException)body.getData();

        String message1 = ex.getMessage();
        String message2 = getExpectedException().getMessage();

        if (message1 == null && message2 == null)
            return true;

        if ((message1 == null && message2 != null) || (message1 != null && message2 == null))
            return false;

        return message1.equalsIgnoreCase(message2);
    }

    public abstract MessageException getExpectedException();


    protected boolean headersMatch(MessageHeader header1, MessageHeader header2)
    {
        if ((header1 == null && header2 != null) || (header1 != null && header2 == null))
            return false;

        if (!header1.getName().equalsIgnoreCase(header2.getName()))
            return false;

        if (header1.getMustUnderstand() != header2.getMustUnderstand())
            return false;

        Object data1 = header1.getData();
        Object data2 = header2.getData();

        if (!headerValuesMatch(data1, data2))
            return false;

        return true;
    }

    protected boolean headerValuesMatch(Object o1, Object o2)
    {
        return valuesMatch(o1, o2);
    }

    protected boolean bodiesMatch(MessageBody body1, MessageBody body2)
    {
        if ((body1 == null && body2 != null) || (body1 != null && body2 == null))
            return false;

        if (!body1.getTargetURI().equalsIgnoreCase(body2.getTargetURI()))
            return false;

        if (!body1.getResponseURI().equalsIgnoreCase(body2.getResponseURI()))
            return false;

        Object data1 = body1.getData();
        Object data2 = body2.getData();

        if (!bodyValuesMatch(data1, data2))
            return false;

        return true;
    }

    protected boolean bodyValuesMatch(Object o1, Object o2)
    {
        return valuesMatch(o1, o2);
    }

    protected boolean messagesMatch(ActionMessage m1, ActionMessage m2)
    {
        if ((m1 == null && m2 != null) || (m2 == null && m1 != null))
            return false;

        if (m1.getVersion() != m2.getVersion())
            return false;

        int headerCount1 = m1.getHeaderCount();
        int headerCount2 = m2.getHeaderCount();

        if (headerCount1 != headerCount2)
            return false;

        for (int i = 0; i < headerCount1; i++)
        {
            MessageHeader header1 = m1.getHeader(i);
            MessageHeader header2 = m2.getHeader(i);
            if (!headersMatch(header1, header2))
                return false;
        }

        int bodyCount1 = m1.getBodyCount();
        int bodyCount2 = m2.getBodyCount();

        if (bodyCount1 != bodyCount2)
            return false;

        for (int i = 0; i < bodyCount1; i++)
        {
            MessageBody body1 = m1.getBody(i);
            MessageBody body2 = m2.getBody(i);

            if (!bodiesMatch(body1, body2))
                return false;
        }

        return true;
    }

    protected boolean valuesMatch(Object o1, Object o2)
    {
        if (o1 == null && o2 == null)
            return true;

        if (o1 instanceof ASObject)
        {
            return objectsMatch((ASObject)o1, (Map)o2);
        }
        else if (o1 instanceof Map)
        {
            return mapsMatch((Map)o1, (Map)o2);
        }
        else if (o1 instanceof List)
        {
            return listsMatch((List)o1, (List)o2);
        }
        else if (o1.getClass().isArray())
        {
            return arraysMatch(o1, o2);
        }
        else if (o1 instanceof Document)
        {
            return documentsMatch((Document)o1, (Document)o2);
        }
        else if (o1 != null)
        {
            if (hasComplexChildren(o1))
            {
                // We avoid checking a complex/custom types twice in
                // case of circular dependencies
                Object known = knownObjects.get(o1);
                if (known != null)
                {
                    return true;
                }
                else
                {
                    knownObjects.put(o1, o2);
                }
            }

            // Special case Double and Integer
            if (o1 instanceof Double && o2 instanceof Integer)
                return new Integer(((Double)o1).intValue()).equals(o2);

            return o1.equals(o2);
        }

        return false;
    }

    protected boolean documentsMatch(Document doc1, Document doc2)
    {
        boolean match = false;

        try
        {
            CachedXPathAPI xpath1 = new CachedXPathAPI();
            CachedXPathAPI xpath2 = new CachedXPathAPI();
            Node root1 = xpath1.selectSingleNode(doc1, "/");
            Node root2 = xpath2.selectSingleNode(doc2, "/");

            if (!nodesMatch(xpath1, root1, xpath2, root2))
            {
                return false;
            }

            match = true;
        }
        catch (Throwable t)
        {
            throw new MessageException("Error comparing XML Documents: " + t.getMessage(), t);
        }

        return match;

    }

    protected boolean nodesMatch(CachedXPathAPI xpath1, Node node1, CachedXPathAPI xpath2, Node node2)
    {
        boolean match = false;

        try
        {
            NodeList list1 = xpath1.selectNodeList(node1, "*");
            NodeList list2 = xpath2.selectNodeList(node2, "*");

            if (list1.getLength() == list2.getLength())
            {
                for (int i = 0; i < list1.getLength(); i++)
                {
                    Node n1 = list1.item(i);
                    Node n2 = list2.item(i);

                    NodeList attributes1 = xpath1.selectNodeList(n1, "@*");
                    NodeList attributes2 = xpath2.selectNodeList(n2, "@*");

                    if (!attributesMatch(attributes1, attributes2))
                    {
                        return false;
                    }

                    if (!nodesMatch(xpath1, n1, xpath2, n2))
                    {
                        return false;
                    }
                }

                match = true;
            }
        }
        catch (Throwable t)
        {
            throw new MessageException("Error comparing XML nodes: " + t.getMessage(), t);
        }

        return match;

    }

    protected boolean attributesMatch(NodeList attributes1, NodeList attributes2)
    {
        if (attributes1 == null && attributes2 == null)
            return true;

        boolean match = false;

        if (attributes1.getLength() == attributes2.getLength())
        {
            for (int i = 0; i < attributes1.getLength(); i++)
            {
                Node a1 = attributes1.item(i);
                Node a2 = attributes2.item(i);

                if (!stringValuesMatch(a1.getNodeName(), a2.getNodeName()))
                    return false;

                if (!stringValuesMatch(a1.getNodeValue(), a2.getNodeValue()))
                    return false;
            }

            match = true;
        }

        return match;
    }


    protected boolean objectsMatch(ASObject aso1, Map aso2)
    {
        String type1 = aso1.getType();

        // We may have an anonymous ASObject and an ECMA Array
        // which get serialized in the same way, i.e. a Map,
        // so we ignore this difference
        if (type1 != null)
        {
            String type2 = ((ASObject)aso2).getType();

            if (!stringValuesMatch(type1, type2))
                return false;
        }

        return mapsMatch(aso1, aso2);
    }

    protected boolean stringValuesMatch(String str1, String str2)
    {
        if (str1 == null && str2 == null)
            return true;

        if (str1 != null && !str1.equals(str2))
            return false;

        return true;
    }

    protected boolean mapsMatch(Map map1, Map map2)
    {
        if (map1.size() != map2.size())
            return false;

        // We avoid checking a Map twice in case
        // of circular dependencies
        Object known = knownObjects.get(map1);
        if (known != null)
        {
            return true;
        }
        else
        {
            knownObjects.put(map1, map2);
        }

        Iterator it = map1.keySet().iterator();
        while (it.hasNext())
        {
            Object next = it.next();
            if (next instanceof String)
            {
                String key = (String)next;
                Object val1 = map1.get(key);
                Object val2 = map2.get(key);

                if (!valuesMatch(val1, val2))
                    return false;
            }
            else
            {
                return false;
            }
        }

        return true;
    }

    protected boolean listsMatch(List list1, List list2)
    {
        if (list1.size() != list2.size())
            return false;

        // We avoid checking a List twice in case
        // of circular dependencies
        Object known = knownObjects.get(list1);
        if (known != null)
        {
            return true;
        }
        else
        {
            knownObjects.put(list1, list2);
        }

        for (int i = 0; i < list1.size(); i++)
        {
            Object o1 = list1.get(i);
            Object o2 = list2.get(i);

            if (!valuesMatch(o1, o2))
                return false;
        }

        return true;
    }

    protected boolean arraysMatch(Object array1, Object array2)
    {
        int len1 = Array.getLength(array1);
        int len2 = Array.getLength(array2);

        if (len1 != len2)
            return false;

        // We avoid checking an Array twice in case
        // of circular dependencies
        Object known = knownObjects.get(array1);
        if (known != null)
        {
            return true;
        }
        else
        {
            knownObjects.put(array1, array2);
        }

        for (int i = 0; i < len1; i++)
        {
            Object o1 = Array.get(array1, i);
            Object o2 = Array.get(array2, i);

            if (!valuesMatch(o1, o2))
                return false;
        }

        return true;
    }

    protected void addToList(Object list, int index, Object value)
    {
        if (list instanceof List)
            ((List)list).add(value);
        else if (list.getClass().isArray())
            Array.set(list, index, value);
    }

    protected Object getFromList(Object list, int index)
    {
        if (list instanceof List)
            return ((List)list).get(index);
        else
            return Array.get(list, index);
    }

    protected Object createList(int length)
    {
        if (context.legacyCollection)
            return new ArrayList(length);
        else
            return new Object[length];
    }

    private static boolean hasComplexChildren(Object o)
    {
        if (o instanceof String
                || o instanceof Boolean
                || o instanceof Number
                || o instanceof Character
                || o instanceof Date)
        {
            return false;
        }

        return true;
    }

}
