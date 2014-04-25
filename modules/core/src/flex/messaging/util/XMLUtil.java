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
package flex.messaging.util;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import flex.messaging.MessageException;

/**
 * Utility class for converting strings to XML documents and
 * vice versa.
 *
 * @exclude
 */
public class XMLUtil
{
    public static String INDENT_XML = "no";
    public static String OMIT_XML_DECLARATION = "yes";

    private XMLUtil()
    {
    }

    /**
     * Uses a TransformerFactory with an identity transformation to convert a
     * Document into a String representation of the XML.
     *
     * @param document Document.
     * @return An XML String.
     * @throws IOException if an error occurs during transformation.
     */
    public static String documentToString(Document document) throws IOException
    {
        String xml = null;

        try
        {
            DOMSource dom = new DOMSource(document);
            StringWriter writer = new StringWriter();
            StreamResult output = new StreamResult(writer);

            // Use Transformer to serialize a DOM
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();

            // No need for pretty printing
            transformer.setOutputProperty(OutputKeys.INDENT, INDENT_XML);

            // XML Declarations unexpected whitespace for legacy AS XMLDocument type,
            // so we always omit it. We can't tell whether one was present when
            // constructing the Document in the first place anyway...
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, OMIT_XML_DECLARATION);

            transformer.transform(dom, output);

            xml = writer.toString();
        }
        catch (TransformerException te)
        {
            throw new IOException("Error serializing Document as String: " + te.getMessageAndLocation());
        }
        return xml;
    }

    /**
     * Uses the current DocumentBuilderFactory to converts a String
     * representation of XML into a Document.
     *
     * @param xml XML serialized as a String
     * @return Document
     */
    public static Document stringToDocument(String xml)
    {
        return stringToDocument(xml, true);
    }

    /**
     * Uses the current DocumentBuilderFactory to converts a String
     * representation of XML into a Document.
     *
     * @param xml XML serialized as a String
     * @param nameSpaceAware determines whether the constructed Document
     * is name-space aware
     * @return Document
     */
    public static Document stringToDocument(String xml, boolean nameSpaceAware)
    {
        ClassUtil.validateCreation(Document.class);

        Document document = null;
        try
        {
            if (xml != null)
            {
                StringReader reader = new StringReader(xml);
                InputSource input = new InputSource(reader);
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(nameSpaceAware);
                factory.setValidating(false);
                DocumentBuilder builder = factory.newDocumentBuilder();

                document = builder.parse(input);
            }
        }
        catch (Exception ex)
        {
            throw new MessageException("Error deserializing XML type " + ex.getMessage());
        }

        return document;
    }
}
