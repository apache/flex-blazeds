package utils;

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

import java.io.*;
import java.util.*;

import org.w3c.dom.Document;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.apache.axis.utils.XMLUtils;

//import org.apache.xml.serialize.XMLSerializer;
//import org.apache.xml.serialize.OutputFormat;
//import org.apache.axis.utils.XMLUtils;


public class XMLMerge {
    private String PARENT_NODE_NAME="parent";
    private String ACTION="action";
    private String ACTION_REPLACE="replace"; // Default
    private String ACTION_ADD="add";
    private String ACTION_DELETE="delete";
    public boolean debug=false;

    public static void main (String args[]){

        String baseFilename="c:/working/web.xml";
        String importFilename="c:/working/webBasic-Merge.xml";
        String outputFilename="c:/working/out.xml";

        if (args.length == 3) {
            baseFilename=args[0];
            importFilename=args[1];
            outputFilename=args[2];
        } else if (args.length != 0) {
            System.out.println("Usage: XMLTest baseFilename importFilename outputFilename " + args.length);
        }

        XMLMerge xmlMerge=new XMLMerge();
        xmlMerge.mergeDocs(baseFilename, importFilename,  outputFilename);

    }

    public void mergeDocs(String baseFilename, String importFilename, String outputFilename) {

        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse (new File(baseFilename));
            Document newXML = docBuilder.parse(new File(importFilename));

            doc.getDocumentElement().normalize();
            newXML.getDocumentElement().normalize();
            Node importedXML=doc.importNode(newXML.getFirstChild(), true);

            // Get the name, parent and index of each new node and store them in a vector.
            Vector newNodeVector=new Vector();
            NodeList newNodes=importedXML.getChildNodes();
            debug("Making vector- " + newNodes.getLength());
            for(int i=0; i<newNodes.getLength(); i++) {

                if (newNodes.item(i).getNodeName() !="#text" && newNodes.item(i).getNodeName() !="#comment") {
                    try {
                        String name=newNodes.item(i).getNodeName();
                        NamedNodeMap attributes=newNodes.item(i).getAttributes();
                        String parent=attributes.getNamedItem(PARENT_NODE_NAME).getNodeValue();
                        String action=attributes.getNamedItem(ACTION)==null?ACTION_REPLACE:attributes.getNamedItem(ACTION).getNodeValue();
                        attributes.removeNamedItem(PARENT_NODE_NAME);
                        if (attributes.getNamedItem(ACTION)!=null)
                            attributes.removeNamedItem(ACTION);

                        newNodeVector.add(new NamedNode(name, parent, action, newNodes.item(i)));
                    } catch (NullPointerException npe) {
                        throw new Exception("The current node (" + newNodes.item(i).getNodeName() + ") likely is missing a 'parent' or 'action' attribute!", npe);
                    }
                }
            }

            // Loop over the new nodes
            Enumeration newNodeEnumeration=newNodeVector.elements();
            while (newNodeEnumeration.hasMoreElements()) {
                NamedNode nodeToAdd=(NamedNode) newNodeEnumeration.nextElement();

                debug("");

                if (nodeToAdd.theNode.getFirstChild() != null) {
                    debug("Node to " + nodeToAdd.action + ": " + nodeToAdd.name + ", " + nodeToAdd.theNode.getNodeType() + "(" + nodeToAdd.theNode.getFirstChild().getNodeValue() + ")");
                } else {
                    debug("Node to " + nodeToAdd.action + ": " + nodeToAdd.name + ", no children.");
                }

                Node target=findNode(nodeToAdd.parent, doc);
                debug("found node: " + target.getNodeName());
                NodeList targetChildren=target.getChildNodes();
                debug("target children: " + targetChildren.getLength());
                for (int i=0; i<targetChildren.getLength(); i++) {
                    Node currentChild=targetChildren.item(i);
                    debug ("Next source doc node: " + currentChild.getNodeName());
                    if (! currentChild.getNodeName().equals("#text") && ! currentChild.getNodeName().equals("#comment")) {
                        debug("Comparing " + nodeToAdd.name + " to " + nodeToAdd.parent + " " +  currentChild.getNodeName());
                        if (nodeToAdd.name.equals(currentChild.getNodeName()) && currentChild.hasAttributes()) {
                            debug(".. node has attributes.");
                            NamedNodeMap attributes=currentChild.getAttributes();
                            int matchingAttributes=0;
                            for (int j=0; j<attributes.getLength(); j++) {
                                String existingAttributeName=attributes.item(j).getNodeName();
                                String existingAttributeValue=attributes.item(j).getNodeValue();
                                if (nodeToAdd.theNode.getAttributes().getNamedItem(existingAttributeName) != null) {
                                    debug(".. existing: " + existingAttributeName + "=" + existingAttributeValue + ".. target: " + existingAttributeName + "=" + nodeToAdd.theNode.getAttributes().getNamedItem(existingAttributeName).getNodeValue());
                                    if (nodeToAdd.theNode.getAttributes().getNamedItem(existingAttributeName).getNodeValue().equals(existingAttributeValue))
                                        matchingAttributes++;
                                }
                            }

                            // allow addition of duplicates
                            if (matchingAttributes==attributes.getLength() && !nodeToAdd.action.equals(ACTION_ADD)) {
                                debug("NODE REMOVED - " + currentChild.getNodeName());
                                target.removeChild(currentChild);
                            }
                        } else if (nodeToAdd.name.equals(currentChild.getNodeName()) && nodeToAdd.action.equals(ACTION_REPLACE)) {
                            debug("NODE REMOVED - REPLACE - " + currentChild.getNodeName());
                            target.removeChild(currentChild);
                        } else if (nodeToAdd.action.equals(ACTION_DELETE)) {
                            if (nodeToAdd.name.equals(currentChild.getNodeName()) &&
                                    (currentChild.getNodeType() != Node.ELEMENT_NODE ||
                                    (currentChild.getNodeType() == Node.ELEMENT_NODE && currentChild.getFirstChild().getNodeValue().equals(nodeToAdd.theNode.getFirstChild().getNodeValue()))) ) {
                                debug("NODE REMOVED - DELETE - " + currentChild.getNodeName() + (currentChild.getNodeType() == Node.ELEMENT_NODE) + " " + currentChild.getNodeType() + " - " + currentChild.getFirstChild().getNodeValue());
                                target.removeChild(currentChild);
                            } else {
                                debug("Not removing node. " + currentChild.getNodeName() + " " + currentChild.getNodeType() + " - " + currentChild.getFirstChild().getNodeValue());
                                debug("  nodeToAdd.name.equals(currentChild.getNodeName()? " + nodeToAdd.name.equals(currentChild.getNodeName()));
                                debug("  currentChild.getNodeType() == Node.ELEMENT_NODE? " + (currentChild.getNodeType() == Node.ELEMENT_NODE));
                                debug("  currentChild.getFirstChild().getNodeValue() = " + currentChild.getFirstChild().getNodeValue());
                                debug("  nodeToAdd.theNode.getFirstChild().getNodeValue() = " + nodeToAdd.theNode.getFirstChild().getNodeValue());
                                debug("  equal? " + currentChild.getFirstChild().getNodeValue().equals(nodeToAdd.theNode.getFirstChild().getNodeValue()));

                            }
                        } else {
                            debug("! No action taken.  " + nodeToAdd.name + " delete?" +  nodeToAdd.action.equals(ACTION_DELETE) + " '" +  nodeToAdd.action + "'");
                        }

                        target.normalize();
                    }
                }

                if (! nodeToAdd.action.equals(ACTION_DELETE)) {
                    Node placeToAddTo = target;

                    if (nodeToAdd.theNode.getNodeName().equals(target.getNodeName())) {
                        debug("Trying to add " + nodeToAdd.theNode.getNodeName() + " to " + target.getNodeName());
                    } else {
                        StringTokenizer tokenizer = new StringTokenizer(nodeToAdd.parent,  "/");
                        debug("No exact match.  Finding true parent." + nodeToAdd.theNode.getNodeName() + " : " + target.getNodeName());

                        boolean found = false;

                        while (tokenizer.hasMoreElements()) {
                            String current = (String)tokenizer.nextElement();
                            debug("- add missing element loop.  current=" + current  + ", target.getNodeName()=" + target.getNodeName());

                            if (found) {
                                debug("- adding <" + current + "/> to " + placeToAddTo.getNodeName());

                                placeToAddTo = placeToAddTo.appendChild(placeToAddTo.getOwnerDocument().createElement(current));

                            } else if (current.equals(target.getNodeName())) {
                                found = true;
                            }
                        }
                        debug("Done adding missing elements");
                    }

                    debug("attempting to add to " + placeToAddTo.getNodeName());
                    placeToAddTo.appendChild(nodeToAdd.theNode);

                    // Hack for a prettier print.  Without this all new elements are put on the same line.
                    placeToAddTo.appendChild(placeToAddTo.getOwnerDocument().createTextNode("\n"));
                }
            }

            // store the file
            writeXMLToFile(doc, outputFilename);

        } catch (SAXParseException err) {
            System.out.println ("** Parsing error" + ", line " + err.getLineNumber () + ", uri " + err.getSystemId ());
            System.out.println(" " + err.getMessage ());

        } catch (SAXException e) {
            Exception x = e.getException ();
            ((x == null) ? e : x).printStackTrace ();

        } catch (Throwable t) {
            t.printStackTrace ();
        }
    }


    public void writeXMLToFile(Document doc, String filename) {
        try {
            File file=new File(filename);
            FileWriter  fileWriter = new FileWriter(file);

            /*
			// As stolen from http://forum.java.sun.com/thread.jspa?threadID=552012&messageID=2699430
			// link is now: https://forum.java.sun.com/message/7647472#7647472
			// Adobe used this, but doesn't own it so can't donate it to Apache
            XMLSerializer xmlWriter=new XMLSerializer();
            xmlWriter.serializeXmlDoc(doc, file);
            */

            // Alternative #1 - everything on its own line including text.  Some extra spaces.  Looks strange.
            /*
            FileOutputStream outputStream = new FileOutputStream(file);
            XMLUtils.PrettyDocumentToStream(doc, outputStream);
            outputStream.close();
            */

            // Alternative #2 - everything on its own line including text.  Some extra spaces.  Looks strange.
            XMLUtils.PrettyDocumentToWriter(doc, fileWriter);
            fileWriter.close();
            


            // Alternative #3 - but without setPreserveSpace() it's all bunched together and
            // with setPreserveSpace() it's the same as the original.
			/*
            OutputFormat format = new OutputFormat(doc);
            format.setLineWidth(65);
            format.setIndenting(true);
            format.setIndent(2);
            //format.setPreserveSpace();
            org.apache.xml.serialize.XMLSerializer serializer = new XMLSerializer(fileWriter, format);
            serializer.serialize(doc);
			*/

        } catch (FileNotFoundException fnf) {
            System.out.println("FILE NOT FOUND!!!" + filename);
        } catch (IOException ioe) {
            System.out.println("UNABLE TO CREATE NEW FILE. " + filename);
        }
    }


    public Node findNode(String path, Document doc) {
        StringTokenizer targetTokenizer = new StringTokenizer(path, "/");

        Node targetNode=doc;

        while (targetTokenizer.hasMoreTokens()) {
            String currentTargetString= targetTokenizer.nextToken();
            NodeList nodes=targetNode.getChildNodes();
            boolean found = false;
            for (int i=0;i<nodes.getLength(); i++) {
                if (nodes.item(i).getNodeName().equals(currentTargetString)) {
                    targetNode=nodes.item(i);
                    found=true;
                    break;
                }
            }

            if (!found ) {
                debug("Node was not found. Attempting to add " + currentTargetString + " to " + targetNode.getNodeName());
                targetNode = targetNode.appendChild(doc.createElement(currentTargetString));
            }
        }

        debug("targetNode.getNodeName() = " + targetNode.getNodeName());
        return targetNode;
    }


    public void debug(String value) {
        if (debug)
            System.out.println(value);
    }
}


class NamedNode {
    String name;
    String parent;
    String action;
    Node theNode;


    NamedNode(String name, String parent, String action, Node theNode) {
        this.name=name;
        this.parent=parent;
        this.action=action;
        this.theNode=theNode;
    }

}