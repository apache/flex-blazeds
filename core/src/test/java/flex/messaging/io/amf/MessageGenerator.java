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

package flex.messaging.io.amf;

import flex.messaging.io.SerializationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Test programs use this class to simulate Flash Player AMF requests.
 * Simply call <code>parse(String)</code> with the path to an AMF request sample XML file (but
 * be sure to set a DataOutputStream first!).
 * <p>
 * This class, which extends DataOutput, interprets the XML and writes an AMF request to the
 * underlying DataOutputStream.
 * </p>
 */
public class MessageGenerator extends Amf0Output {
    private DocumentBuilder docBuilder;

    public MessageGenerator() {
        super(new SerializationContext());
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        docBuilderFactory.setIgnoringComments(true);
        docBuilderFactory.setIgnoringElementContentWhitespace(true);
        try {
            docBuilder = docBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            throw new RuntimeException(ex);
        }
    }


    public void parse(String path) {
        parse(new File(path));
    }

    public void parse(File file) {
        try {
            Document doc = docBuilder.parse(file);
            doc.getDocumentElement().normalize();
            parseAmf(doc);
        } catch(XPathExpressionException ex) {
            throw new RuntimeException(ex);
        } catch (TransformerException ex) {
            throw new RuntimeException(ex);
        } catch (SAXException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }


    private void parseAmf(Document doc) throws TransformerException, IOException, XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        Node root = (Node) xpath.evaluate("/amf-request", doc, XPathConstants.NODE);

        if (root != null) {
            // messages
            NodeList list = (NodeList) xpath.evaluate("message", root, XPathConstants.NODESET);
            for (int i = 0; i < list.getLength(); i++) {
                Node message = list.item(i);
                message(xpath, message);
            }
        }
    }

    private void message(XPath xpath, Node message) throws TransformerException, IOException, XPathExpressionException {
        int version = ((Double) xpath.evaluate("version", message, XPathConstants.NUMBER)).intValue();
        out.writeShort(version);

        // Headers
        Node headers = (Node) xpath.evaluate("headers", message, XPathConstants.NODE);
        int headerCount = ((Double) xpath.evaluate("@count", headers, XPathConstants.NUMBER)).intValue();
        out.writeShort(headerCount);

        NodeList list = (NodeList) xpath.evaluate("header", headers, XPathConstants.NODESET);
        for (int i = 0; i < headerCount; i++) {
            Node header = list.item(i);
            if (header != null) {
                header(xpath, header, i);
            } else {
                throw new RuntimeException("Missing header(s). Specified " + headerCount + ", found " + (i - 1));
            }
        }

        // Bodies
        Node bodies = (Node) xpath.evaluate("bodies", message, XPathConstants.NODE);
        int bodyCount = ((Double) xpath.evaluate("@count", bodies, XPathConstants.NUMBER)).intValue();
        out.writeShort(bodyCount);

        list = (NodeList) xpath.evaluate("body", bodies, XPathConstants.NODESET);
        for (int i = 0; i < bodyCount; i++) {
            Node body = list.item(i);
            if (body != null) {
                body(xpath, body, i);
            } else {
                throw new RuntimeException("Missing body(ies). Specified " + bodyCount + ", found " + (i - 1));
            }
        }
    }

    private void header(XPath xpath, Node header, int i) throws TransformerException, IOException, XPathExpressionException {
        String name = xpath.evaluate("@name", header);
        boolean mustUnderstand = (Boolean) xpath.evaluate("@mustUnderstand", header, XPathConstants.BOOLEAN);

        if (isDebug)
            trace.startHeader(name, mustUnderstand, i);

        out.writeUTF(name);
        out.writeBoolean(mustUnderstand);

        //int length = ((Double) xpath.evaluate("@length", body, XPathConstants.NUMBER)).intValue();
        out.writeInt(-1); //Specify unknown content length

        reset();

        Node data = (Node) xpath.evaluate("*", header, XPathConstants.NODE); // Only one data item can be sent as the body...
        Object value = value(xpath, data);
        writeObject(value);

        if (isDebug)
            trace.endHeader();
    }

    private void body(XPath xpath, Node body, int i) throws TransformerException, IOException, XPathExpressionException {
        String targetUri = xpath.evaluate("@targetUri", body);
        String responseUri = xpath.evaluate("@responseUri", body);

        if (isDebug)
            trace.startMessage(targetUri, responseUri, i);

        out.writeUTF(targetUri);
        out.writeUTF(responseUri);

        //int length = ((Double) xpath.evaluate("@length", body, XPathConstants.NUMBER)).intValue();
        out.writeInt(-1); //Specify unknown content length

        reset();

        Node data = (Node) xpath.evaluate("*", body, XPathConstants.NODE); // Only one data item can be sent as the body...
        Object value = value(xpath, data);
        writeObject(value);

        if (isDebug)
            trace.endMessage();
    }

    private Object value(XPath xpath, Node node) throws TransformerException, IOException, XPathExpressionException {
        String type = node.getNodeName();
        String value = xpath.evaluate(".", node);

        if (value == null) {
            return null;
        }

        if (avmPlus) {
            if ("string".equals(type)) {
                return value;
            } else if ("boolean".equals(type)) {
                return Boolean.valueOf(value.trim());
            } else if ("integer".equals(type)) {
                return Integer.valueOf(value.trim());
            } else if ("double".equals(type)) {
                return Double.valueOf(value.trim());
            } else if ("array".equals(type)) {
                List<Object> array = new ArrayList<Object>();

                int count = ((Double) xpath.evaluate("@count", node, XPathConstants.NUMBER)).intValue();

                NodeList list = (NodeList) xpath.evaluate("*", node, XPathConstants.NODESET);
                for (int i = 0; i < count; i++) {
                    Node item = list.item(i);
                    if (item != null) {
                        array.add(value(xpath, item));
                    } else {
                        throw new RuntimeException("Missing array item(s). Specified " + count + ", found " + (i - 1));
                    }
                }

                return array;
            } else if ("object".equals(type)) {
                ASObject object = new ASObject();

                NodeList list = (NodeList) xpath.evaluate("*[not(self::property)]", node, XPathConstants.NODESET);

                List<Object> traitProperties = null;

                // TRAIT PROPERTIES
                for (int i = 0; i < list.getLength(); i++) {
                    if (i == 0) {
                        Node traits = list.item(i);

                        String className = xpath.evaluate("@classname", traits).trim();
                        int count = ((Double) xpath.evaluate("@count", traits, XPathConstants.NUMBER)).intValue();
                        // boolean dynamic = (Boolean) xpath.evaluate("@dynamic", traits, XPathConstants.BOOLEAN);

                        traitProperties = new ArrayList<Object>(count);

                        if (className.length() > 0) {
                            object.setType(className);
                        }

                        NodeList propList = (NodeList) xpath.evaluate("property", traits, XPathConstants.NODESET);
                        for (int p = 0; p < count; p++) {
                            Node prop = propList.item(p);
                            if (prop != null) {
                                String propName = xpath.evaluate("@name", prop).trim();
                                traitProperties.add(propName);
                            } else {
                                throw new RuntimeException("Missing trait property(ies). Specified " + count + ", found " + (p - 1));
                            }
                        }
                    } else {
                        Node prop = list.item(i);
                        String propName = (String) traitProperties.get(i - 1);
                        object.put(propName, value(xpath, prop));
                    }
                }

                // DYNAMIC PROPERTIES
                list = (NodeList) xpath.evaluate("property", node, XPathConstants.NODESET);
                for (int i = 0; i < list.getLength(); i++) {
                    Node prop = list.item(i);
                    String propName = xpath.evaluate("@name", prop);
                    Node propValue = (Node) xpath.evaluate("*", prop, XPathConstants.NODE);
                    object.put(propName, value(xpath, propValue));
                }


                return object;
            } else if ("xml".equals(type)) {
                return value;
            }
        } else {
            if ("avmplus".equals(type)) {
                setAvmPlus(true);
                Node data = (Node) xpath.evaluate("*", node, XPathConstants.NODE); // Only one data item can be sent as the body...
                return value(xpath, data);
            } else if ("string".equals(type)) {
                return value;
            } else if ("boolean".equals(type)) {
                return Boolean.valueOf(value.trim());
            } else if ("number".equals(type)) {
                return Double.valueOf(value.trim());
            } else if ("array".equals(type)) {
                List<Object> array = new ArrayList<Object>();

                int count = ((Double) xpath.evaluate("@count", node, XPathConstants.NUMBER)).intValue();

                NodeList list = (NodeList) xpath.evaluate("*", node, XPathConstants.NODESET);
                for (int i = 0; i < count; i++) {
                    Node item = list.item(i);
                    if (item != null) {
                        array.add(value(xpath, item));
                    } else {
                        throw new RuntimeException("Missing array item(s). Specified " + count + ", found " + (i - 1));
                    }
                }

                return array;
            } else if ("object".equals(type)) {
                ASObject object = new ASObject();
                String className = xpath.evaluate("@classname", node).trim();

                if (className.length() > 0) {
                    object.setType(className);
                }

                NodeList list = (NodeList) xpath.evaluate("property", node, XPathConstants.NODESET);
                for (int i = 0; i < list.getLength(); i++) {
                    Node prop = list.item(i);
                    String propName = xpath.evaluate("@name", prop).trim();
                    object.put(propName, value(xpath, prop));
                }

                return object;
            } else if ("xml".equals(type)) {
                return value;
            }
        }

        return null;
    }

}
