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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Node;
import org.w3c.dom.Text;

import flex.messaging.log.Log;
import flex.messaging.log.LogCategories;
import flex.messaging.util.StringUtils;

/**
 * This class is used by configuration parser to replace tokens of the format
 * {...} with actual values. The value can either come from a JVM property 
 * (eg. -Dmy.channel.id=my-amf) or from a token properties file specified via 
 * token.file JVM property (eg. -Dtoken.file=/usr/local/tokens.properties) where
 * the token key and value are specified in the property file.
 * 
 * If a token value is both specified as a JVM property and inside the token.file,
 * JVM property takes precedence.
 *
 * @exclude
 */
public class TokenReplacer
{
    private static final String TOKEN_FILE = "token.file";
    private static final Pattern pattern = Pattern.compile("\\{(.*?)\\}");

    private final Map replacedTokens;
    private Properties tokenProperties;
    private String tokenPropertiesFilename;

    /**
     * Default constructor.
     */
    public TokenReplacer()
    {
        replacedTokens = new LinkedHashMap();
        loadTokenProperties();
    }

    /**
     * Replace any tokens in the value of the node or the text child of the node.
     *
     * @param node The node whose value will be searched for tokens.
     * @param sourceFileName The source file where the node came from.
     */
    public void replaceToken(Node node, String sourceFileName)
    {
        // Exit if we are attempting to replace one of the forbidden nodes - nodes
        // that may have curly brackets as part of their syntax
        if (ConfigurationConstants.IP_ADDRESS_PATTERN.equals(node.getNodeName()))
            return;

        // ReplacementNode is either the original node if it has a value or the text
        // child of the node if it does not have a value
        Node replacementNode;
        if (node.getNodeValue() == null)
        {
            if (node.getChildNodes().getLength() == 1 && node.getFirstChild() instanceof Text)
                replacementNode = node.getFirstChild();
            else
                return;
        }
        else
        {
            replacementNode = node;
        }

        String nodeValue = replacementNode.getNodeValue();
        Matcher matcher = pattern.matcher(nodeValue);
        while (matcher.find()) // Means the node value has token(s)
        {
            String tokenWithCurlyBraces = matcher.group();
            String tokenWithoutCurlyBraces = matcher.group(1);
            String propertyValue = getPropertyValue(tokenWithoutCurlyBraces);
            if (propertyValue != null)
            {
                nodeValue = StringUtils.substitute(nodeValue, tokenWithCurlyBraces, propertyValue);
                replacedTokens.put(tokenWithCurlyBraces, propertyValue);
            }
            // context-path, server-name and server-port tokens can be replaced
            // later, therefore, no warning is necessary if they cannot be replaced
            // at this point.
            else if (!ConfigurationConstants.CONTEXT_PATH_TOKEN.equals(tokenWithCurlyBraces)
                    && !ConfigurationConstants.CONTEXT_PATH_ALT_TOKEN.equals(tokenWithCurlyBraces)
                    && !ConfigurationConstants.SERVER_NAME_TOKEN.equals(tokenWithCurlyBraces)
                    && !ConfigurationConstants.SERVER_PORT_TOKEN.equals(tokenWithCurlyBraces))
            {
                // Token ''{0}'' in ''{1}'' was not replaced. Either supply a value to this token with a JVM option, or remove it from the configuration.
                ConfigurationException ex = new ConfigurationException();
                Object[] args = {tokenWithCurlyBraces, sourceFileName};
                ex.setMessage(ConfigurationConstants.IRREPLACABLE_TOKEN, args);
                throw ex;
            }
        }
        replacementNode.setNodeValue(nodeValue);
    }

    /**
     * See if the token have a value either provided as a JVM property or as 
     * part of the token property file. JVM property takes precedence on the token
     * property file.
     */
    private String getPropertyValue(String tokenWithoutCurlyBraces)
    {
        String propertyValue = System.getProperty(tokenWithoutCurlyBraces);
        if (propertyValue != null)
            return propertyValue;

        if (tokenProperties != null)
            propertyValue = tokenProperties.getProperty(tokenWithoutCurlyBraces);

        return propertyValue;
    }

    /**
     * Used by the parser to report the replaced tokens once logging is setup.
     */
    public void reportTokens()
    {
        if (Log.isWarn())
        {
            if (tokenProperties != null && Log.isDebug())
                Log.getLogger(LogCategories.CONFIGURATION).debug("Token replacer is using the token file '" + tokenPropertiesFilename + "'");

            for (Iterator iter = replacedTokens.entrySet().iterator(); iter.hasNext();)
            {
                Map.Entry entry = (Map.Entry)iter.next();
                String tokenWithParanthesis = (String)entry.getKey();
                String propertyValue = (String)entry.getValue();
                // Issue a special warning for context.root replacements,
                if (ConfigurationConstants.CONTEXT_PATH_TOKEN.equals(tokenWithParanthesis)
                        || ConfigurationConstants.CONTEXT_PATH_ALT_TOKEN.equals(tokenWithParanthesis))
                {
                    Log.getLogger(LogCategories.CONFIGURATION).warn("Token '{0}' was replaced with '{1}'. Note that this will apply to all applications on the JVM",
                            new Object[]{tokenWithParanthesis, propertyValue});
                }
                else if (Log.isDebug())
                {
                    Log.getLogger(LogCategories.CONFIGURATION).debug("Token '{0}' was replaced with '{1}'", new Object[]{tokenWithParanthesis, propertyValue});
                }
            }
        }
    }

    /**
     * Given a String, determines whether the string contains tokens. 
     * 
     * @param value The String to check.
     * @return True if the String contains tokens.
     */
    public static boolean containsTokens(String value)
    {
        return (value != null && value.length() > 0)? pattern.matcher(value).find() : false;
    }

    private void loadTokenProperties()
    {
        tokenPropertiesFilename = System.getProperty(TOKEN_FILE);
        if (tokenPropertiesFilename == null)
            return;

        tokenProperties = new Properties();
        try
        {
            tokenProperties.load(new FileInputStream(tokenPropertiesFilename));
        }
        catch (FileNotFoundException e)
        {
            tokenProperties = null;
        }
        catch (IOException e)
        {
            tokenProperties = null;
        }
    }
}
