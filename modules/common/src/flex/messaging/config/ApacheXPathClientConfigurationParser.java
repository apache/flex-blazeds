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
package flex.messaging.config;

import org.apache.xpath.CachedXPathAPI;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.transform.TransformerException;

/**
 * Uses Apache XPath on a DOM representation of a messaging configuration
 * file.
 * <p>
 * NOTE: Since reference ids are used between elements, certain
 * sections of the document need to be parsed first.
 * </p>
 *
 * @author Peter Farland
 * @exclude
 */
public class ApacheXPathClientConfigurationParser extends ClientConfigurationParser
{
    private CachedXPathAPI xpath;

    protected void initializeExpressionQuery()
    {
        this.xpath = new CachedXPathAPI();
    }

    protected Node selectSingleNode(Node source, String expression)
    {
        try
        {
            return xpath.selectSingleNode(source, expression);
        }
        catch (TransformerException transformerException)
        {
            throw wrapException(transformerException);
        }
    }

    protected NodeList selectNodeList(Node source, String expression)
    {
        try
        {
            return xpath.selectNodeList(source, expression);
        }
        catch (TransformerException transformerException)
        {
            throw wrapException(transformerException);
        }
    }

    protected Object evaluateExpression(Node source, String expression)
    {
        try
        {
            return xpath.eval(source, expression);
        }
        catch (TransformerException transformerException)
        {
            throw wrapException(transformerException);
        }
    }

    private ConfigurationException wrapException(TransformerException exception)
    {
       ConfigurationException result = new ConfigurationException();
       result.setDetails(PARSER_INTERNAL_ERROR);
       result.setRootCause(exception);
       return result;
    }
}
