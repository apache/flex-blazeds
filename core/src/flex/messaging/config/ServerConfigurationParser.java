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

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import flex.messaging.config.ThrottleSettings.Policy;
import flex.messaging.util.LocaleUtils;

/**
 * Processes DOM representation of a messaging configuration file.
 * <p>
 * Note: Since reference ids are used between elements, certain
 * sections of the document need to be parsed first.
 * </p>
 *
 *
 */
public abstract class ServerConfigurationParser extends AbstractConfigurationParser
{
    /**
     * Used to verify that advanced messaging support has been registered if necessary.
     * If other configuration requires it, but it was not registered a ConfigurationException is thrown.
     */
    private boolean verifyAdvancedMessagingSupport = false;
    private boolean advancedMessagingSupportRegistered = false;

    @Override
    protected void parseTopLevelConfig(Document doc)
    {
        Node root = selectSingleNode(doc, "/" + SERVICES_CONFIG_ELEMENT);

        if (root != null)
        {
            allowedChildElements(root, SERVICES_CONFIG_CHILDREN);

            securitySection(root); // Parse security before channels.

            serversSection(root);

            channelsSection(root);

            services(root);

            clusters(root);

            logging(root);

            system(root);

            flexClient(root);

            factories(root);

            messageFilters(root);

            validators(root);

            // Validate that any dependencies on advanced messaging support can be satisified at runtime.
            if (verifyAdvancedMessagingSupport && !advancedMessagingSupportRegistered)
            {
                ConfigurationException e = new ConfigurationException();
                e.setMessage(REQUIRE_ADVANCED_MESSAGING_SUPPORT);
                throw e;
            }
        }
        else
        {
            // The services configuration root element must be '{SERVICES_CONFIG_ELEMENT}'.
            ConfigurationException e = new ConfigurationException();
            e.setMessage(INVALID_SERVICES_ROOT, new Object[]{SERVICES_CONFIG_ELEMENT});
            throw e;
        }
    }

    private void clusters(Node root)
    {
        Node clusteringNode = selectSingleNode(root, CLUSTERS_ELEMENT);
        if (clusteringNode != null)
        {
            allowedAttributesOrElements(clusteringNode, CLUSTERING_CHILDREN);

            NodeList clusters = selectNodeList(clusteringNode, CLUSTER_DEFINITION_ELEMENT);
            for (int i = 0; i < clusters.getLength(); i++)
            {
                Node cluster = clusters.item(i);
                requiredAttributesOrElements(cluster, CLUSTER_DEFINITION_CHILDREN);
                String clusterName = getAttributeOrChildElement(cluster, ID_ATTR);
                if (!isValidID(clusterName))
                    continue;

                String propsFileName = getAttributeOrChildElement(cluster, CLUSTER_PROPERTIES_ATTR);
                ClusterSettings clusterSettings = new ClusterSettings();
                clusterSettings.setClusterName(clusterName);
                clusterSettings.setPropsFileName(propsFileName);
                String className = getAttributeOrChildElement(cluster, CLASS_ATTR);
                if (className != null && className.length() > 0)
                    clusterSettings.setImplementationClass(className);

                String defaultValue = getAttributeOrChildElement(cluster, ClusterSettings.DEFAULT_ELEMENT);
                if (defaultValue != null && defaultValue.length() > 0)
                {
                    if (defaultValue.equalsIgnoreCase(TRUE_STRING))
                        clusterSettings.setDefault(true);
                    else if (!defaultValue.equalsIgnoreCase(FALSE_STRING))
                    {
                        ConfigurationException e = new ConfigurationException();
                        e.setMessage(10215, new Object[] {clusterName, defaultValue});
                        throw e;
                    }
                }
                String ulb = getAttributeOrChildElement(cluster, ClusterSettings.URL_LOAD_BALANCING);
                if (ulb != null && ulb.length() > 0)
                {
                    if (ulb.equalsIgnoreCase(FALSE_STRING))
                    {
                        clusterSettings.setURLLoadBalancing(false);
                    }
                    else if (!ulb.equalsIgnoreCase(TRUE_STRING))
                    {
                        ConfigurationException e = new ConfigurationException();
                        e.setMessage(10216, new Object[] {clusterName, ulb});
                        throw e;
                    }
                }

                NodeList properties = selectNodeList(cluster, PROPERTIES_ELEMENT + "/*");
                if (properties.getLength() > 0)
                {
                    ConfigMap map = properties(properties, getSourceFileOf(cluster));
                    clusterSettings.addProperties(map);
                }

                ((MessagingConfiguration)config).addClusterSettings(clusterSettings);

            }
        }
    }


    private void securitySection(Node root)
    {
        Node security = selectSingleNode(root, SECURITY_ELEMENT);

        if (security == null)
            return;

        allowedChildElements(security, SECURITY_CHILDREN);

        NodeList list = selectNodeList(security, SECURITY_CONSTRAINT_DEFINITION_ELEMENT);
        for (int i = 0; i < list.getLength(); i++)
        {
            Node constraint = list.item(i);
            securityConstraint(constraint, false);
        }

        list = selectNodeList(security, CONSTRAINT_INCLUDE_ELEMENT);
        for (int i = 0; i < list.getLength(); i++)
        {
            Node include = list.item(i);
            securityConstraintInclude(include);
        }

        list = selectNodeList(security, LOGIN_COMMAND_ELEMENT);
        for (int i = 0; i < list.getLength(); i++)
        {
            Node login = list.item(i);
            LoginCommandSettings loginCommandSettings= new LoginCommandSettings();
            requiredAttributesOrElements(login, LOGIN_COMMAND_REQ_CHILDREN);
            allowedAttributesOrElements(login, LOGIN_COMMAND_CHILDREN);

            String server = getAttributeOrChildElement(login, SERVER_ATTR);
            if (server.length() == 0)
            {
                // Attribute '{SERVER_ATTR}' must be specified for element '{LOGIN_COMMAND_ELEMENT}'
                ConfigurationException e = new ConfigurationException();
                e.setMessage(MISSING_ATTRIBUTE, new Object[]{SERVER_ATTR, LOGIN_COMMAND_ELEMENT});
                throw e;
            }
            loginCommandSettings.setServer(server);

            String loginClass = getAttributeOrChildElement(login, CLASS_ATTR);
            if (loginClass.length() == 0)
            {
                // Attribute '{CLASS_ATTR}' must be specified for element '{LOGIN_COMMAND_ELEMENT}'
                ConfigurationException e = new ConfigurationException();
                e.setMessage(MISSING_ATTRIBUTE, new Object[]{CLASS_ATTR, LOGIN_COMMAND_ELEMENT});
                throw e;
            }
            loginCommandSettings.setClassName(loginClass);

            boolean isPerClientAuth = Boolean.valueOf(getAttributeOrChildElement(login, PER_CLIENT_AUTH));
            loginCommandSettings.setPerClientAuthentication(isPerClientAuth);

            ((MessagingConfiguration)config).getSecuritySettings().addLoginCommandSettings(loginCommandSettings);
        }

        boolean recreateHttpSessionAfterLogin = Boolean.valueOf(getAttributeOrChildElement(security, RECREATE_HTTPSESSION_AFTER_LOGIN_ELEMENT));
        ((MessagingConfiguration)config).getSecuritySettings().setRecreateHttpSessionAfterLogin(recreateHttpSessionAfterLogin);
    }

    private SecurityConstraint securityConstraint(Node constraint, boolean inline)
    {
        SecurityConstraint sc;

        // Validation
        allowedAttributesOrElements(constraint, SECURITY_CONSTRAINT_DEFINITION_CHILDREN);

        // Constraint by reference
        String ref = getAttributeOrChildElement(constraint, REF_ATTR);
        if (ref.length() > 0)
        {
            allowedAttributesOrElements(constraint, new String[] {REF_ATTR});

            sc = ((MessagingConfiguration)config).getSecuritySettings().getConstraint(ref);
            if (sc == null)
            {
                // {SECURITY_CONSTRAINT_DEFINITION_ELEMENT} not found for reference '{ref}'.
                ConfigurationException e = new ConfigurationException();
                e.setMessage(REF_NOT_FOUND, new Object[]{SECURITY_CONSTRAINT_DEFINITION_ELEMENT, ref});
                throw e;
            }
        }
        else
        {
            // New security constraint
            String id = getAttributeOrChildElement(constraint, ID_ATTR);

            // If not inline, we must have a valid id to register the constraint!
            if (inline)
            {
                sc = new SecurityConstraint("");
            }
            else if (isValidID(id))
            {
                sc = new SecurityConstraint(id);
                ((MessagingConfiguration)config).getSecuritySettings().addConstraint(sc);
            }
            else
            {
                //Invalid {SECURITY_CONSTRAINT_DEFINITION_ELEMENT} id '{id}'.
                ConfigurationException ex = new ConfigurationException();
                ex.setMessage(INVALID_ID, new Object[]{SECURITY_CONSTRAINT_DEFINITION_ELEMENT, id});
                ex.setDetails(INVALID_ID);
                throw ex;
            }

            // Authentication Method
            String method = getAttributeOrChildElement(constraint, AUTH_METHOD_ELEMENT);
            sc.setMethod(method);

            // Roles
            Node rolesNode = selectSingleNode(constraint, ROLES_ELEMENT);
            if (rolesNode != null)
            {
                allowedChildElements(rolesNode, ROLES_CHILDREN);
                NodeList roles = selectNodeList(rolesNode, ROLE_ELEMENT);
                for (int r = 0; r < roles.getLength(); r++)
                {
                    Node roleNode = roles.item(r);
                    String role = evaluateExpression(roleNode, ".").toString().trim();
                    if (role.length() > 0)
                    {
                        sc.addRole(role);
                    }
                }
            }
        }

        return sc;
    }

    private void securityConstraintInclude(Node constraintInclude)
    {
        // Validation
        allowedAttributesOrElements(constraintInclude, CONSTRAINT_INCLUDE_CHILDREN);

        String src = getAttributeOrChildElement(constraintInclude, SRC_ATTR);
        String dir = getAttributeOrChildElement(constraintInclude, DIRECTORY_ATTR);
        if (src.length() > 0)
        {
            constraintIncludeFile(src);
        }
        else if (dir.length() > 0)
        {
            constraintIncludeDirectory(dir);
        }
        else
        {
            // The include element ''{0}'' must specify either the ''{1}'' or ''{2}'' attribute.
            ConfigurationException ex = new ConfigurationException();
            ex.setMessage(MISSING_INCLUDE_ATTRIBUTES, new Object[]{constraintInclude.getNodeName(), SRC_ATTR, DIRECTORY_ATTR});
            throw ex;
        }
    }

    private void constraintIncludeFile(String src)
    {
        Document doc = loadDocument(src, fileResolver.getIncludedFile(src));
        doc.getDocumentElement().normalize();

        // Check for <security-constraints> wrapping more than one definition
        Node servicesNode = selectSingleNode(doc, SECURITY_CONSTRAINTS_ELEMENT);
        if (servicesNode != null)
        {
            allowedChildElements(servicesNode, SECURITY_CONSTRAINTS_CHILDREN);
            NodeList constraints = selectNodeList(servicesNode, SECURITY_CONSTRAINT_ELEMENT);
            for (int a = 0; a < constraints.getLength(); a++)
            {
                Node constraint = constraints.item(a);
                securityConstraint(constraint, false);
            }
            fileResolver.popIncludedFile();
        }
        else // Check for single <security-constraint>
        {
            Node constraint = selectSingleNode(doc, "/" + SECURITY_CONSTRAINT_ELEMENT);
            if (constraint != null)
            {
                securityConstraint(constraint, false);
                fileResolver.popIncludedFile();
            }
            else
            {
                // The {0} root element in file {1} must be ''{2}'' or ''{3}''.
                ConfigurationException ex = new ConfigurationException();
                ex.setMessage(INVALID_INCLUDE_ROOT, new Object[]{CONSTRAINT_INCLUDE_ELEMENT, src, SECURITY_CONSTRAINTS_ELEMENT, SECURITY_CONSTRAINT_ELEMENT});
                throw ex;
            }
        }
    }

    private void constraintIncludeDirectory(String dir)
    {
        List files = fileResolver.getFiles(dir);
        for (int i = 0; i < files.size(); i++)
        {
            String src = (String) files.get(i);
            constraintIncludeFile(src);
        }
    }

    private void serversSection(Node root)
    {
        // Only MessagingConfiguration supports the servers element configuration.
        // The general ServicesConfiguration interface does not.
        if (!(config instanceof MessagingConfiguration))
            return;

        Node serversNode = selectSingleNode(root, SERVERS_ELEMENT);
        if (serversNode != null)
        {
            // Validation
            allowedAttributesOrElements(serversNode, SERVERS_CHILDREN);

            NodeList servers = selectNodeList(serversNode, SERVER_ELEMENT);
            for (int i = 0; i < servers.getLength(); i++)
            {
                Node server = servers.item(i);
                serverDefinition(server);
            }
        }
    }

    private void serverDefinition(Node server)
    {
        // Validation
        requiredAttributesOrElements(server, SERVER_REQ_CHILDREN);
        allowedAttributesOrElements(server, SERVER_CHILDREN);

        String id = getAttributeOrChildElement(server, ID_ATTR);
        if (isValidID(id))
        {
            SharedServerSettings settings = new SharedServerSettings();
            settings.setId(id);
            settings.setSourceFile(getSourceFileOf(server));
            String className = getAttributeOrChildElement(server, CLASS_ATTR);
            if (className.length() > 0)
            {
                settings.setClassName(className);
                // Custom server properties.
                NodeList properties = selectNodeList(server, PROPERTIES_ELEMENT + "/*");
                if (properties.getLength() > 0)
                {
                    ConfigMap map = properties(properties, getSourceFileOf(server));
                    settings.addProperties(map);
                }
                ((MessagingConfiguration)config).addSharedServerSettings(settings);
            }
            else
            {
                // Class not specified for {MESSAGE_FILTER_ELEMENT} '{id}'.
                ConfigurationException ex = new ConfigurationException();
                ex.setMessage(CLASS_NOT_SPECIFIED, new Object[]{SERVER_ELEMENT, id});
                throw ex;
            }
        }
    }

    private void channelsSection(Node root)
    {
        Node channelsNode = selectSingleNode(root, CHANNELS_ELEMENT);
        if (channelsNode != null)
        {
            // Validation
            allowedAttributesOrElements(channelsNode, CHANNELS_CHILDREN);

            NodeList channels = selectNodeList(channelsNode, CHANNEL_DEFINITION_ELEMENT);
            for (int i = 0; i < channels.getLength(); i++)
            {
                Node channel = channels.item(i);
                channelDefinition(channel);
            }

            NodeList includes = selectNodeList(channelsNode, CHANNEL_INCLUDE_ELEMENT);
            for (int i = 0; i < includes.getLength(); i++)
            {
                Node include = includes.item(i);
                channelInclude(include);
            }
        }
    }

    private void channelDefinition(Node channel)
    {
        // Validation
        requiredAttributesOrElements(channel, CHANNEL_DEFINITION_REQ_CHILDREN);
        allowedAttributesOrElements(channel, CHANNEL_DEFINITION_CHILDREN);

        String id = getAttributeOrChildElement(channel, ID_ATTR);
        if (isValidID(id))
        {
            // Don't allow multiple channels with the same id
            if (config.getChannelSettings(id) != null)
            {
                // Cannot have multiple channels with the same id ''{0}''.
                ConfigurationException e = new ConfigurationException();
                e.setMessage(DUPLICATE_CHANNEL_ERROR, new Object[]{id});
                throw e;
            }

            ChannelSettings channelSettings = new ChannelSettings(id);
            channelSettings.setSourceFile(getSourceFileOf(channel));

            // Note whether the channel-definition was for a remote endpoint
            String remote = getAttributeOrChildElement(channel, REMOTE_ATTR);
            channelSettings.setRemote(Boolean.valueOf(remote));

            // Endpoint
            Node endpoint = selectSingleNode(channel, ENDPOINT_ELEMENT);
            if (endpoint != null)
            {
                // Endpoint Validation
                allowedAttributesOrElements(endpoint, ENDPOINT_CHILDREN);

                String type = getAttributeOrChildElement(endpoint, CLASS_ATTR);
                channelSettings.setEndpointType(type);

                // The url attribute may also be specified by the deprecated uri attribute
                String uri = getAttributeOrChildElement(endpoint, URL_ATTR);
                if (uri == null || EMPTY_STRING.equals(uri))
                    uri = getAttributeOrChildElement(endpoint, URI_ATTR);
                channelSettings.setUri(uri);

                config.addChannelSettings(id, channelSettings);
            }

            channelServerOnlyAttribute(channel, channelSettings);

            // Server reference
            Node server = selectSingleNode(channel, SERVER_ELEMENT);
            if (server != null)
            {
                requiredAttributesOrElements(server, CHANNEL_DEFINITION_SERVER_REQ_CHILDREN);

                String serverId = getAttributeOrChildElement(server, REF_ATTR);
                channelSettings.setServerId(serverId);
            }

            // Channel Properties
            NodeList properties = selectNodeList(channel, PROPERTIES_ELEMENT + "/*");
            if (properties.getLength() > 0)
            {
                ConfigMap map = properties(properties, getSourceFileOf(channel));
                channelSettings.addProperties(map);

                // Sniff for adaptive-frequency under flex-client-queue-processor hich requires advanced messaging support.
                if (!verifyAdvancedMessagingSupport)
                {
                    ConfigMap outboundQueueProcessor = map.getPropertyAsMap(FLEX_CLIENT_OUTBOUND_QUEUE_PROCESSOR_ELEMENT, null);
                    if (outboundQueueProcessor != null)
                    {
                        // Flex client queue processor properties
                        ConfigMap queueProcessorProperties = outboundQueueProcessor.getPropertyAsMap(PROPERTIES_ELEMENT, null);
                        if (queueProcessorProperties != null)
                        {
                            // Sniff for adaptive-frequency which requires advanced messaging support.
                            boolean adaptiveFrequency = queueProcessorProperties.getPropertyAsBoolean(ADAPTIVE_FREQUENCY, false);
                            if (adaptiveFrequency)
                                verifyAdvancedMessagingSupport = true;
                        }
                    }
                }
            }

            // Channel Security

            // Security-constraint short-cut attribute
            String ref = evaluateExpression(channel, "@" + SECURITY_CONSTRAINT_ATTR).toString().trim();
            if (ref.length() > 0)
            {
                SecurityConstraint sc = ((MessagingConfiguration)config).getSecuritySettings().getConstraint(ref);
                if (sc != null)
                {
                    channelSettings.setConstraint(sc);
                }
                else
                {
                    // {SECURITY_CONSTRAINT_ELEMENT} not found for reference '{ref}' in channel '{id}'.
                    ConfigurationException ex = new ConfigurationException();
                    ex.setMessage(REF_NOT_FOUND_IN_CHANNEL, new Object[]{SECURITY_CONSTRAINT_ATTR, ref, id});
                    throw ex;
                }
            }
            else
            {
                // Inline security element
                Node security = selectSingleNode(channel, SECURITY_ELEMENT);
                if (security != null)
                {
                    allowedChildElements(security, EMBEDDED_SECURITY_CHILDREN);
                    Node constraint = selectSingleNode(security, SECURITY_CONSTRAINT_ELEMENT);
                    if (constraint != null)
                    {
                        SecurityConstraint sc = securityConstraint(constraint, true);
                        channelSettings.setConstraint(sc);
                    }
                }
            }
        }
        else
        {
            // Invalid {CHANNEL_DEFINITION_ELEMENT} id '{id}'.
            ConfigurationException ex = new ConfigurationException();
            ex.setMessage(INVALID_ID, new Object[]{CHANNEL_DEFINITION_ELEMENT, id});
            ex.setDetails(INVALID_ID);
            throw ex;
        }
    }

    private void channelServerOnlyAttribute(Node channel, ChannelSettings channelSettings)
    {
        String clientType = getAttributeOrChildElement(channel, CLASS_ATTR);
        clientType = clientType.length() > 0? clientType : null;

        String serverOnlyString = getAttributeOrChildElement(channel, SERVER_ONLY_ATTR);
        boolean serverOnly = serverOnlyString.length() > 0? Boolean.valueOf(serverOnlyString) : false;

        if (clientType == null && !serverOnly) // None set.
        {
            String url = channelSettings.getUri();
            boolean serverOnlyProtocol = (url.startsWith("samfsocket") || url.startsWith("amfsocket") || url.startsWith("ws"));
            if (!serverOnlyProtocol)
            {
                // Endpoint ''{0}'' needs to have either class or server-only attribute defined.
                ConfigurationException ce = new ConfigurationException();
                ce.setMessage(CLASS_OR_SERVER_ONLY_ERROR, new Object[]{channelSettings.getId()});
                throw ce;
            }
            channelSettings.setServerOnly(true);
        }
        else if (clientType != null && serverOnly) // Both set.
        {
            // Endpoint ''{0}'' cannot have both class and server-only attribute defined.
            ConfigurationException ce = new ConfigurationException();
            ce.setMessage(CLASS_AND_SERVER_ONLY_ERROR, new Object[]{channelSettings.getId()});
            throw ce;
        }
        else // One of them set.
        {
            if (serverOnly)
                channelSettings.setServerOnly(true);
            else
                channelSettings.setClientType(clientType);
        }
    }

    private void channelInclude(Node channelInclude)
    {
        // Validation
        allowedAttributesOrElements(channelInclude, CHANNEL_INCLUDE_CHILDREN);

        String src = getAttributeOrChildElement(channelInclude, SRC_ATTR);
        String dir = getAttributeOrChildElement(channelInclude, DIRECTORY_ATTR);
        if (src.length() > 0)
        {
            channelIncludeFile(src);
        }
        else if (dir.length() > 0)
        {
            channelIncludeDirectory(dir);
        }
        else
        {
            // The include element ''{0}'' must specify either the ''{1}'' or ''{2}'' attribute.
            ConfigurationException ex = new ConfigurationException();
            ex.setMessage(MISSING_INCLUDE_ATTRIBUTES, new Object[]{channelInclude.getNodeName(), SRC_ATTR, DIRECTORY_ATTR});
            throw ex;
        }
    }

    private void channelIncludeFile(String src)
    {
        Document doc = loadDocument(src, fileResolver.getIncludedFile(src));
        doc.getDocumentElement().normalize();

        // Check for multiple channels in a single file.
        Node channelsNode = selectSingleNode(doc, CHANNELS_ELEMENT);
        if (channelsNode != null)
        {
            allowedChildElements(channelsNode, CHANNELS_CHILDREN);
            NodeList channels = selectNodeList(channelsNode, CHANNEL_DEFINITION_ELEMENT);
            for (int a = 0; a < channels.getLength(); a++)
            {
                Node service = channels.item(a);
                channelDefinition(service);
            }
            fileResolver.popIncludedFile();
        }
        else // Check for single channel in the file.
        {
            Node channel = selectSingleNode(doc, "/" + CHANNEL_DEFINITION_ELEMENT);
            if (channel != null)
            {
                channelDefinition(channel);
                fileResolver.popIncludedFile();
            }
            else
            {
                // The {0} root element in file {1} must be '{CHANNELS_ELEMENT}' or '{CHANNEL_ELEMENT}'.
                ConfigurationException ex = new ConfigurationException();
                ex.setMessage(INVALID_INCLUDE_ROOT, new Object[]{CHANNEL_INCLUDE_ELEMENT, src, CHANNELS_ELEMENT, CHANNEL_DEFINITION_ELEMENT});
                throw ex;
            }
        }
    }

    private void channelIncludeDirectory(String dir)
    {
        List files = fileResolver.getFiles(dir);
        for (int i = 0; i < files.size(); i++)
        {
            String src = (String) files.get(i);
            channelIncludeFile(src);
        }
    }

    private void services(Node root)
    {
        Node servicesNode = selectSingleNode(root, SERVICES_ELEMENT);
        if (servicesNode != null)
        {
            // Validation
            allowedChildElements(servicesNode, SERVICES_CHILDREN);

            // Default Channels for the application
            Node defaultChannels = selectSingleNode(servicesNode, DEFAULT_CHANNELS_ELEMENT);
            if (defaultChannels != null)
            {
                allowedChildElements(defaultChannels, DEFAULT_CHANNELS_CHILDREN);
                NodeList channels = selectNodeList(defaultChannels, CHANNEL_ELEMENT);
                for (int c = 0; c < channels.getLength(); c++)
                {
                    Node chan = channels.item(c);
                    allowedAttributes(chan, new String[] {REF_ATTR});
                    defaultChannel(chan);
                }
            }

            // Service Includes
            NodeList services = selectNodeList(servicesNode, SERVICE_INCLUDE_ELEMENT);
            for (int i = 0; i < services.getLength(); i++)
            {
                Node service = services.item(i);
                serviceInclude(service);
            }

            // Service
            services = selectNodeList(servicesNode, SERVICE_ELEMENT);
            for (int i = 0; i < services.getLength(); i++)
            {
                Node service = services.item(i);
                service(service);
            }
        }
    }

    private void serviceInclude(Node serviceInclude)
    {
        // Validation
        allowedAttributesOrElements(serviceInclude, SERVICE_INCLUDE_CHILDREN);

        String src = getAttributeOrChildElement(serviceInclude, SRC_ATTR);
        String dir = getAttributeOrChildElement(serviceInclude, DIRECTORY_ATTR);
        if (src.length() > 0)
        {
            serviceIncludeFile(src);
        }
        else if (dir.length() > 0)
        {
            serviceIncludeDirectory(dir);
        }
        else
        {
            // The include element ''{0}'' must specify either the ''{1}'' or ''{2}'' attribute.
            ConfigurationException ex = new ConfigurationException();
            ex.setMessage(MISSING_INCLUDE_ATTRIBUTES, new Object[]{serviceInclude.getNodeName(), SRC_ATTR, DIRECTORY_ATTR});
            throw ex;
        }
    }

    private void serviceIncludeFile(String src)
    {
        Document doc = loadDocument(src, fileResolver.getIncludedFile(src));
        doc.getDocumentElement().normalize();

        // Check for multiple services defined in a <services> tag
        Node servicesNode = selectSingleNode(doc, SERVICES_ELEMENT);
        if (servicesNode != null)
        {
            allowedChildElements(servicesNode, SERVICES_CHILDREN);
            NodeList services = selectNodeList(servicesNode, SERVICE_ELEMENT);
            for (int a = 0; a < services.getLength(); a++)
            {
                Node service = services.item(a);
                service(service);
            }
            fileResolver.popIncludedFile();
        }
        else // Check for single <service> definition.
        {
            Node service = selectSingleNode(doc, "/" + SERVICE_ELEMENT);
            if (service != null)
            {
                service(service);
                fileResolver.popIncludedFile();
            }
            else
            {
                // The {0} root element in file {1} must be ''{2}'' or ''{3}''.
                ConfigurationException ex = new ConfigurationException();
                ex.setMessage(INVALID_INCLUDE_ROOT, new Object[]{SERVICE_INCLUDE_ELEMENT, src, SERVICES_ELEMENT, SERVICE_ELEMENT});
                throw ex;
            }
        }
    }

    private void serviceIncludeDirectory(String dir)
    {
        List files = fileResolver.getFiles(dir);
        for (int i = 0; i < files.size(); i++)
        {
            String src = (String) files.get(i);
            serviceIncludeFile(src);
        }
    }

    private void service(Node service)
    {
        // Validation
        requiredAttributesOrElements(service, SERVICE_REQ_CHILDREN);
        allowedAttributesOrElements(service, SERVICE_CHILDREN);

        String id = getAttributeOrChildElement(service, ID_ATTR);
        if (isValidID(id))
        {
            ServiceSettings serviceSettings = config.getServiceSettings(id);
            if (serviceSettings == null)
            {
                serviceSettings = new ServiceSettings(id);
                serviceSettings.setSourceFile(getSourceFileOf(service));
                config.addServiceSettings(serviceSettings);
            }
            else
            {
                // Duplicate service definition '{0}'.
                ConfigurationException e = new ConfigurationException();
                e.setMessage(DUPLICATE_SERVICE_ERROR, new Object[]{id});
                throw e;
            }

            // Service Class Name
            String className = getAttributeOrChildElement(service, CLASS_ATTR);
            if (className.length() > 0)
            {
                serviceSettings.setClassName(className);

                // Sniff for AdvancedMessagingSupport.
                if (className.equals("flex.messaging.services.AdvancedMessagingSupport"))
                    advancedMessagingSupportRegistered = true;
            }
            else
            {
                // Class not specified for {SERVICE_ELEMENT} '{id}'.
                ConfigurationException ex = new ConfigurationException();
                ex.setMessage(CLASS_NOT_SPECIFIED, new Object[]{SERVICE_ELEMENT, id});
                throw ex;
            }

            // Service Message Types - deprecated

            // Service Properties
            NodeList properties = selectNodeList(service, PROPERTIES_ELEMENT + "/*");
            if (properties.getLength() > 0)
            {
                ConfigMap map = properties(properties, getSourceFileOf(service));
                serviceSettings.addProperties(map);
            }

            // Default Channels
            Node defaultChannels = selectSingleNode(service, DEFAULT_CHANNELS_ELEMENT);
            if (defaultChannels != null)
            {
                allowedChildElements(defaultChannels, DEFAULT_CHANNELS_CHILDREN);
                NodeList channels = selectNodeList(defaultChannels, CHANNEL_ELEMENT);
                for (int c = 0; c < channels.getLength(); c++)
                {
                    Node chan = channels.item(c);
                    allowedAttributes(chan, new String[] {REF_ATTR});
                    defaultChannel(chan, serviceSettings);
                }
            }
            // Fall back on application's default channels
            else if (config.getDefaultChannels().size() > 0)
            {
                for (Iterator iter = config.getDefaultChannels().iterator(); iter.hasNext();)
                {
                    String channelId = (String)iter.next();
                    ChannelSettings channel = config.getChannelSettings(channelId);
                    serviceSettings.addDefaultChannel(channel);
                }
            }

            // Default Security Constraint
            Node defaultSecurityConstraint = selectSingleNode(service, DEFAULT_SECURITY_CONSTRAINT_ELEMENT);
            if (defaultSecurityConstraint != null)
            {
                // Validation
                requiredAttributesOrElements(defaultSecurityConstraint, new String[] {REF_ATTR});
                allowedAttributesOrElements(defaultSecurityConstraint, new String[] {REF_ATTR});

                String ref = getAttributeOrChildElement(defaultSecurityConstraint, REF_ATTR);
                if (ref.length() > 0)
                {
                    SecurityConstraint sc = ((MessagingConfiguration)config).getSecuritySettings().getConstraint(ref);
                    if (sc == null)
                    {
                        // {SECURITY_CONSTRAINT_DEFINITION_ELEMENT} not found for reference '{ref}'.
                        ConfigurationException e = new ConfigurationException();
                        e.setMessage(REF_NOT_FOUND, new Object[]{SECURITY_CONSTRAINT_DEFINITION_ELEMENT, ref});
                        throw e;
                    }
                    serviceSettings.setConstraint(sc);
                }
                else
                {
                    //Invalid default-security-constraint reference ''{0}'' in service ''{1}''.
                    ConfigurationException ex = new ConfigurationException();
                    ex.setMessage(INVALID_SECURITY_CONSTRAINT_REF, new Object[]{ref, id});
                    throw ex;
                }
            }

            // Adapter Definitions
            Node adapters = selectSingleNode(service, ADAPTERS_ELEMENT);
            if (adapters != null)
            {
                allowedChildElements(adapters, ADAPTERS_CHILDREN);
                NodeList serverAdapters = selectNodeList(adapters, ADAPTER_DEFINITION_ELEMENT);
                for (int a = 0; a < serverAdapters.getLength(); a++)
                {
                    Node adapter = serverAdapters.item(a);
                    adapterDefinition(adapter, serviceSettings);
                }
                NodeList adapterIncludes = selectNodeList(adapters, ADAPTER_INCLUDE_ELEMENT);
                for (int a = 0; a < adapterIncludes.getLength(); a++)
                {
                    Node include = adapterIncludes.item(a);
                    adapterInclude(include, serviceSettings);
                }
            }

            // Destinations
            NodeList list = selectNodeList(service, DESTINATION_ELEMENT);
            for (int i = 0; i < list.getLength(); i++)
            {
                Node dest = list.item(i);
                destination(dest, serviceSettings);
            }

            // Destination Includes
            list = selectNodeList(service, DESTINATION_INCLUDE_ELEMENT);
            for (int i = 0; i < list.getLength(); i++)
            {
                Node dest = list.item(i);
                destinationInclude(dest, serviceSettings);
            }
        }
        else
        {
            //Invalid {SERVICE_ELEMENT} id '{id}'.
            ConfigurationException ex = new ConfigurationException();
            ex.setMessage(INVALID_ID, new Object[]{SERVICE_ELEMENT, id});
            throw ex;
        }
    }

    /**
     * A Flex application can declare default channels for its services. If a
     * service specifies its own list of channels it overrides these defaults.
     * <p>
     * &lt;default-channels&gt;<br/>
     * &lt;channel ref="channel-id"/&gt;<br/>
     * &lt;default-channels&gt;
     * </p>
     * @param chan the channel node
     */
    private void defaultChannel(Node chan)
    {
        String ref = getAttributeOrChildElement(chan, REF_ATTR);

        if (ref.length() > 0)
        {
            ChannelSettings channel = config.getChannelSettings(ref);
            if (channel != null)
            {
                config.addDefaultChannel(channel.getId());
            }
            else
            {
                // {0} not found for reference '{1}'
                ConfigurationException e = new ConfigurationException();
                e.setMessage(REF_NOT_FOUND, new Object[]{CHANNEL_ELEMENT, ref});
                throw e;
            }
        }
        else
        {
            //A default channel was specified without a reference for service '{0}'.
            ConfigurationException ex = new ConfigurationException();
            ex.setMessage(INVALID_DEFAULT_CHANNEL, new Object[]{"MessageBroker"});
            throw ex;
        }
    }

    /**
     * A service can declare default channels for its destinations. If a destination
     * specifies its own list of channels it overrides these defaults.
     * <p>
     * &lt;default-channels&gt;<br/>
     * &lt;channel ref="channel-id"/&gt;<br/>
     * &lt;default-channels&gt;
     * </p>
     * @param chan the channel node
     * @param serviceSettings service settings
     */
    private void defaultChannel(Node chan, ServiceSettings serviceSettings)
    {
        String ref = getAttributeOrChildElement(chan, REF_ATTR);

        if (ref.length() > 0)
        {
            ChannelSettings channel = config.getChannelSettings(ref);
            if (channel != null)
            {
                serviceSettings.addDefaultChannel(channel);
            }
            else
            {
                // {0} not found for reference '{1}'
                ConfigurationException e = new ConfigurationException();
                e.setMessage(REF_NOT_FOUND, new Object[]{CHANNEL_ELEMENT, ref});
                throw e;
            }
        }
        else
        {
            //A default channel was specified without a reference for service '{0}'.
            ConfigurationException ex = new ConfigurationException();
            ex.setMessage(INVALID_DEFAULT_CHANNEL, new Object[]{serviceSettings.getId()});
            throw ex;
        }
    }

    private void adapterDefinition(Node adapter, ServiceSettings serviceSettings)
    {
        // Validation
        requiredAttributesOrElements(adapter, ADAPTER_DEFINITION_REQ_CHILDREN);
        allowedChildElements(adapter, ADAPTER_DEFINITION_CHILDREN);

        String serviceId = serviceSettings.getId();

        String id = getAttributeOrChildElement(adapter, ID_ATTR);
        if (isValidID(id))
        {
            AdapterSettings adapterSettings = new AdapterSettings(id);
            adapterSettings.setSourceFile(getSourceFileOf(adapter));
            String className = getAttributeOrChildElement(adapter, CLASS_ATTR);

            if (className.length() > 0)
            {
                adapterSettings.setClassName(className);

                // Default Adapter Check
                boolean isDefault = Boolean.valueOf(getAttributeOrChildElement(adapter, DEFAULT_ATTR));
                if (isDefault)
                {
                    adapterSettings.setDefault(isDefault);

                    AdapterSettings defaultAdapter;
                    defaultAdapter = serviceSettings.getDefaultAdapter();

                    if (defaultAdapter != null)
                    {
                        // Duplicate default adapter '{0}' in service '{1}'. '{2}' has already been selected as the default.
                        ConfigurationException ex = new ConfigurationException();
                        ex.setMessage(DUPLICATE_DEFAULT_ADAPTER, new Object[]{id, serviceId, defaultAdapter.getId()});
                        throw ex;
                    }
                }

                serviceSettings.addAdapterSettings(adapterSettings);

                // Adapter Properties
                NodeList properties = selectNodeList(adapter, PROPERTIES_ELEMENT + "/*");
                if (properties.getLength() > 0)
                {
                    ConfigMap map = properties(properties, getSourceFileOf(adapter));
                    adapterSettings.addProperties(map);
                }
            }
            else
            {
                // Class not specified for {ADAPTER_DEFINITION_ELEMENT} '{id}'.
                ConfigurationException ex = new ConfigurationException();
                ex.setMessage(CLASS_NOT_SPECIFIED, new Object[]{ADAPTER_DEFINITION_ELEMENT, id});
                throw ex;
            }
        }
        else
        {
            //Invalid {ADAPTER_DEFINITION_ELEMENT} id '{id}' for service '{serviceId}'.
            ConfigurationException ex = new ConfigurationException();
            ex.setMessage(INVALID_ID_IN_SERVICE, new Object[]{ADAPTER_DEFINITION_ELEMENT, id, serviceId});
            throw ex;
        }
    }

    private void adapterInclude(Node adapterInclude, ServiceSettings serviceSettings)
    {
        // Validation
        allowedAttributesOrElements(adapterInclude, ADAPTER_INCLUDE_CHILDREN);

        String src = getAttributeOrChildElement(adapterInclude, SRC_ATTR);
        String dir = getAttributeOrChildElement(adapterInclude, DIRECTORY_ATTR);
        if (src.length() > 0)
        {
            adapterIncludeFile(serviceSettings, src);
        }
        else if (dir.length() > 0)
        {
            adapterIncludeDirectory(serviceSettings, dir);
        }
        else
        {
            // Attribute '{0}' must be specified for element '{1}'
            ConfigurationException ex = new ConfigurationException();
            ex.setMessage(MISSING_INCLUDE_ATTRIBUTES, new Object[]{adapterInclude.getNodeName(), SRC_ATTR, DIRECTORY_ATTR});
            throw ex;
        }
    }

    private void adapterIncludeDirectory(ServiceSettings serviceSettings, String dir)
    {
        List files = fileResolver.getFiles(dir);
        for (int i = 0; i < files.size(); i++)
        {
            String src = (String) files.get(i);
            adapterIncludeFile(serviceSettings, src);
        }
    }

    private void adapterIncludeFile(ServiceSettings serviceSettings, String src)
    {
        Document doc = loadDocument(src, fileResolver.getIncludedFile(src));
        doc.getDocumentElement().normalize();

        // Check for multiple adapters defined in file wrapped in an <adapters> element
        Node adaptersNode = selectSingleNode(doc, ADAPTERS_ELEMENT);
        if (adaptersNode != null)
        {
            allowedChildElements(adaptersNode, ADAPTERS_CHILDREN);
            NodeList adapters = selectNodeList(adaptersNode, ADAPTER_DEFINITION_ELEMENT);
            for (int a = 0; a < adapters.getLength(); a++)
            {
                Node adapter = adapters.item(a);
                adapterDefinition(adapter, serviceSettings);
            }
            fileResolver.popIncludedFile();
        }
        else // Check for single adapter
        {
            Node adapter = selectSingleNode(doc, "/" + ADAPTER_DEFINITION_ELEMENT);
            if (adapter != null)
            {
                adapterDefinition(adapter, serviceSettings);
                fileResolver.popIncludedFile();
            }
            else
            {
                // The {0} root element in file {1} must be ''{2}'' or ''{3}''.
                ConfigurationException ex = new ConfigurationException();
                ex.setMessage(INVALID_INCLUDE_ROOT, new Object[]{ADAPTER_INCLUDE_ELEMENT, src, ADAPTERS_ELEMENT, ADAPTER_DEFINITION_ELEMENT});
                throw ex;
            }
        }
    }

    private void destinationInclude(Node destInclude, ServiceSettings serviceSettings)
    {
        // Validation
        allowedAttributesOrElements(destInclude, DESTINATION_INCLUDE_CHILDREN);

        String src = getAttributeOrChildElement(destInclude, SRC_ATTR);
        String dir = getAttributeOrChildElement(destInclude, DIRECTORY_ATTR);
        if (src.length() > 0)
        {
            destinationIncludeFile(serviceSettings, src);
        }
        else if (dir.length() > 0)
        {
            destinationIncludeDirectory(serviceSettings, dir);
        }
        else
        {
            // The include element ''{0}'' must specify either the ''{1}'' or ''{2}'' attribute.
            ConfigurationException ex = new ConfigurationException();
            ex.setMessage(MISSING_INCLUDE_ATTRIBUTES, new Object[]{destInclude.getNodeName(), SRC_ATTR, DIRECTORY_ATTR});
            throw ex;
        }
    }

    private void destinationIncludeDirectory(ServiceSettings serviceSettings, String dir)
    {
        List files = fileResolver.getFiles(dir);
        for (int i = 0; i < files.size(); i++)
        {
            String src = (String) files.get(i);
            destinationIncludeFile(serviceSettings, src);
        }
    }

    private void destinationIncludeFile(ServiceSettings serviceSettings, String src)
    {
        Document doc = loadDocument(src, fileResolver.getIncludedFile(src));
        doc.getDocumentElement().normalize();

        // Check for multiple destination defined in file.
        Node destinationsNode = selectSingleNode(doc, DESTINATIONS_ELEMENT);
        if (destinationsNode != null)
        {
            allowedChildElements(destinationsNode, DESTINATIONS_CHILDREN);
            NodeList destinations = selectNodeList(destinationsNode, DESTINATION_ELEMENT);
            for (int a = 0; a < destinations.getLength(); a++)
            {
                Node dest = destinations.item(a);
                destination(dest, serviceSettings);
            }
            fileResolver.popIncludedFile();
        }
        else // Check for single destination definition.
        {
            Node dest = selectSingleNode(doc, "/" + DESTINATION_ELEMENT);
            if (dest != null)
            {
                destination(dest, serviceSettings);
                fileResolver.popIncludedFile();
            }
            else
            {
                // The {0} root element in file {1} must be ''{2}'' or ''{3}''.
                ConfigurationException ex = new ConfigurationException();
                ex.setMessage(INVALID_INCLUDE_ROOT, new Object[]{DESTINATION_INCLUDE_ELEMENT, src, DESTINATIONS_ELEMENT, DESTINATION_ELEMENT});
                throw ex;
            }
        }
    }

    private void destination(Node dest, ServiceSettings serviceSettings)
    {
        // Validation
        requiredAttributesOrElements(dest, DESTINATION_REQ_CHILDREN);
        allowedAttributes(dest, DESTINATION_ATTR);
        allowedChildElements(dest, DESTINATION_CHILDREN);

        String serviceId = serviceSettings.getId();

        DestinationSettings destinationSettings;
        String id = getAttributeOrChildElement(dest, ID_ATTR);
        if (isValidID(id))
        {
            destinationSettings = (DestinationSettings)serviceSettings.getDestinationSettings().get(id);
            if (destinationSettings != null)
            {
                // Duplicate destination definition '{id}' in service '{serviceId}'.
                ConfigurationException e = new ConfigurationException();
                e.setMessage(DUPLICATE_DESTINATION_ERROR, new Object[]{id, serviceId});
                throw e;
            }

            destinationSettings = new DestinationSettings(id);
            destinationSettings.setSourceFile(getSourceFileOf(dest));
            serviceSettings.addDestinationSettings(destinationSettings);
        }
        else
        {
            //Invalid {DESTINATION_ELEMENT} id '{id}' for service '{serviceId}'.
            ConfigurationException ex = new ConfigurationException();
            ex.setMessage(INVALID_ID_IN_SERVICE, new Object[]{DESTINATION_ELEMENT, id, serviceId});
            throw ex;
        }

        // Destination Properties
        NodeList properties = selectNodeList(dest, PROPERTIES_ELEMENT + "/*");
        if (properties.getLength() > 0)
        {
            ConfigMap map = properties(properties, getSourceFileOf(dest));
            destinationSettings.addProperties(map);

            // Sniff for <network><reliable>true|false</reliable></network> setting.
            // Also sniff for inbound and outbound throttle policies of buffer and conflate.
            // All these features are only supported when advanced messaging support is enabled.
            if (!verifyAdvancedMessagingSupport)
            {
                ConfigMap networkSettings = map.getPropertyAsMap(NetworkSettings.NETWORK_ELEMENT, null);
                if (networkSettings != null)
                {
                    String reliable = networkSettings.getPropertyAsString(NetworkSettings.RELIABLE_ELEMENT, null);
                    if (reliable != null && Boolean.valueOf(reliable))
                    {
                        verifyAdvancedMessagingSupport = true;
                    }
                    else
                    {
                        ConfigMap inbound = networkSettings.getPropertyAsMap(ThrottleSettings.ELEMENT_INBOUND, null);
                        if (inbound != null)
                        {
                            String policy = inbound.getPropertyAsString(ThrottleSettings.ELEMENT_POLICY, null);
                            if (policy != null && (Policy.BUFFER.toString().equalsIgnoreCase(policy)
                                    || Policy.CONFLATE.toString().equalsIgnoreCase(policy)))
                                verifyAdvancedMessagingSupport = true;
                        }
                        if (!verifyAdvancedMessagingSupport)
                        {
                            ConfigMap outbound = networkSettings.getPropertyAsMap(ThrottleSettings.ELEMENT_OUTBOUND, null);
                            if (outbound != null)
                            {
                                String policy = outbound.getPropertyAsString(ThrottleSettings.ELEMENT_POLICY, null);
                                if (policy != null && (Policy.BUFFER.toString().equalsIgnoreCase(policy)
                                        || Policy.CONFLATE.toString().equalsIgnoreCase(policy)))
                                    verifyAdvancedMessagingSupport = true;
                            }
                        }
                    }
                }
            }
        }

        // Channels
        destinationChannels(dest, destinationSettings, serviceSettings);

        // Security
        destinationSecurity(dest, destinationSettings, serviceSettings);

        // Service Adapter
        destinationAdapter(dest, destinationSettings, serviceSettings);
    }

    private void destinationChannels(Node dest, DestinationSettings destinationSettings, ServiceSettings serviceSettings)
    {
        String destId = destinationSettings.getId();

        // Channels attribute
        String channelsList = evaluateExpression(dest, "@" + CHANNELS_ATTR).toString().trim();
        if (channelsList.length() > 0)
        {
            StringTokenizer st = new StringTokenizer(channelsList, LIST_DELIMITERS);
            while (st.hasMoreTokens())
            {
                String ref = st.nextToken().trim();
                ChannelSettings channel = config.getChannelSettings(ref);
                if (channel != null)
                {
                    destinationSettings.addChannelSettings(channel);
                }
                else
                {
                    // {CHANNEL_ELEMENT} not found for reference '{ref}' in destination '{destId}'.
                    ConfigurationException ex = new ConfigurationException();
                    ex.setMessage(REF_NOT_FOUND_IN_DEST, new Object[]{CHANNEL_ELEMENT, ref, destId});
                    throw ex;
                }
            }
        }
        else
        {
            // Channels element
            Node channelsNode = selectSingleNode(dest, CHANNELS_ELEMENT);
            if (channelsNode != null)
            {
                allowedChildElements(channelsNode, DESTINATION_CHANNELS_CHILDREN);
                NodeList channels = selectNodeList(channelsNode, CHANNEL_ELEMENT);
                for (int c = 0; c < channels.getLength(); c++)
                {
                    Node chan = channels.item(c);

                    // Validation
                    requiredAttributesOrElements(chan, DESTINATION_CHANNEL_REQ_CHILDREN);

                    String ref = getAttributeOrChildElement(chan, REF_ATTR);
                    if (ref.length() > 0)
                    {
                        ChannelSettings channel = config.getChannelSettings(ref);
                        if (channel != null)
                        {
                            destinationSettings.addChannelSettings(channel);
                        }
                        else
                        {
                            // {CHANNEL_ELEMENT} not found for reference '{ref}' in destination '{destId}'.
                            ConfigurationException ex = new ConfigurationException();
                            ex.setMessage(REF_NOT_FOUND_IN_DEST, new Object[]{CHANNEL_ELEMENT, ref, destId});
                            throw ex;
                        }
                    }
                    else
                    {
                        //Invalid {0} ref '{1}' in destination '{2}'.
                        ConfigurationException ex = new ConfigurationException();
                        ex.setMessage(INVALID_REF_IN_DEST, new Object[]{CHANNEL_ELEMENT, ref, destId});
                        throw ex;
                    }
                }
            }
            else
            {
                // Finally, we fall back to the service's default channels
                List defaultChannels = serviceSettings.getDefaultChannels();
                Iterator it = defaultChannels.iterator();
                while (it.hasNext())
                {
                    ChannelSettings channel = (ChannelSettings)it.next();
                    destinationSettings.addChannelSettings(channel);
                }
            }
        }

        if (destinationSettings.getChannelSettings().size() <= 0)
        {
            // Destination '{id}' must specify at least one channel.
            ConfigurationException ex = new ConfigurationException();
            ex.setMessage(DEST_NEEDS_CHANNEL, new Object[]{destId});
            throw ex;
        }
    }

    private void destinationSecurity(Node dest, DestinationSettings destinationSettings, ServiceSettings serviceSettings)
    {
        String destId = destinationSettings.getId();

        // Security-constraint short-cut attribute
        String ref = evaluateExpression(dest, "@" + SECURITY_CONSTRAINT_ATTR).toString().trim();
        if (ref.length() > 0)
        {
            SecurityConstraint sc = ((MessagingConfiguration)config).getSecuritySettings().getConstraint(ref);
            if (sc != null)
            {
                destinationSettings.setConstraint(sc);
            }
            else
            {
                // {SECURITY_CONSTRAINT_ELEMENT} not found for reference '{ref}' in destination '{destId}'.
                ConfigurationException ex = new ConfigurationException();
                ex.setMessage(REF_NOT_FOUND_IN_DEST, new Object[]{SECURITY_CONSTRAINT_ATTR, ref, destId});
                throw ex;
            }
        }
        else
        {
            // Inline security element
            Node security = selectSingleNode(dest, SECURITY_ELEMENT);
            if (security != null)
            {
                allowedChildElements(security, EMBEDDED_SECURITY_CHILDREN);
                Node constraint = selectSingleNode(security, SECURITY_CONSTRAINT_ELEMENT);
                if (constraint != null)
                {
                    SecurityConstraint sc = securityConstraint(constraint, true);
                    destinationSettings.setConstraint(sc);
                }
            }
            else
            {
                // Finally, we fall back to the service's default security constraint
                SecurityConstraint sc = serviceSettings.getConstraint();
                if (sc != null)
                {
                    destinationSettings.setConstraint(sc);
                }
            }
        }
    }

    private void destinationAdapter(Node dest, DestinationSettings destinationSettings, ServiceSettings serviceSettings)
    {
        String destId = destinationSettings.getId();

        // Adapter attribute
        String ref = evaluateExpression(dest, "@" + ADAPTER_ATTR).toString().trim();
        if (ref.length() > 0)
        {
            adapterReference(ref, destinationSettings, serviceSettings);
        }
        else
        {
            Node adapter = selectSingleNode(dest, ADAPTER_ELEMENT);

            // Adapter element
            if (adapter != null)
            {
                allowedAttributesOrElements(adapter, DESTINATION_ADAPTER_CHILDREN);
                ref = getAttributeOrChildElement(adapter, REF_ATTR);
                adapterReference(ref, destinationSettings, serviceSettings);
            }
            else
            {
                // Default Adapter (optionally set at the service level)
                AdapterSettings adapterSettings = serviceSettings.getDefaultAdapter();
                if (adapterSettings != null)
                {
                    destinationSettings.setAdapterSettings(adapterSettings);
                }
            }
        }

        if (destinationSettings.getAdapterSettings() == null)
        {
            // Destination '{id}' must specify at least one adapter.
            ConfigurationException ex = new ConfigurationException();
            ex.setMessage(DEST_NEEDS_ADAPTER, new Object[]{destId});
            throw ex;
        }
    }

    private void adapterReference(String ref, DestinationSettings destinationSettings, ServiceSettings serviceSettings)
    {
        String destId = destinationSettings.getId();
        if (ref.length() > 0)
        {
            AdapterSettings adapterSettings = serviceSettings.getAdapterSettings(ref);
            if (adapterSettings != null)
            {
                destinationSettings.setAdapterSettings(adapterSettings);
            }
            else
            {
                // {ADAPTER_ELEMENT} not found for reference '{ref}' in destination '{destId}'.
                ConfigurationException ex = new ConfigurationException();
                ex.setMessage(REF_NOT_FOUND_IN_DEST, new Object[]{ADAPTER_ELEMENT, ref, destId});
                throw ex;
            }
        }
        else
        {
            //Invalid {ADAPTER_ELEMENT} ref '{ref}' in destination '{destId}'.
            ConfigurationException ex = new ConfigurationException();
            ex.setMessage(INVALID_REF_IN_DEST, new Object[]{ADAPTER_ELEMENT, ref, destId});
            throw ex;
        }
    }

    private void logging(Node root)
    {
        Node logging = selectSingleNode(root, LOGGING_ELEMENT);
        if (logging != null)
        {
            // Validation
            allowedAttributesOrElements(logging, LOGGING_CHILDREN);

            LoggingSettings settings = new LoggingSettings();

            // Log Properties
            NodeList properties = selectNodeList(logging, PROPERTIES_ELEMENT + "/*");
            if (properties.getLength() > 0)
            {
                ConfigMap map = properties(properties, getSourceFileOf(logging));
                settings.addProperties(map);
            }

            NodeList targets = selectNodeList(logging, TARGET_ELEMENT);
            for (int i = 0; i < targets.getLength(); i++)
            {
                Node targetNode = targets.item(i);

                // Target Validation
                requiredAttributesOrElements(targetNode, TARGET_REQ_CHILDREN);
                allowedAttributesOrElements(targetNode, TARGET_CHILDREN);

                String className = getAttributeOrChildElement(targetNode, CLASS_ATTR);

                if (className.length() > 0)
                {
                    TargetSettings targetSettings = new TargetSettings(className);
                    String targetLevel = getAttributeOrChildElement(targetNode, LEVEL_ATTR);

                    if (targetLevel.length() > 0)
                        targetSettings.setLevel(targetLevel);

                    // Filters
                    Node filtersNode = selectSingleNode(targetNode, FILTERS_ELEMENT);
                    if (filtersNode != null)
                    {
                        allowedChildElements(filtersNode, FILTERS_CHILDREN);
                        NodeList filters = selectNodeList(filtersNode, PATTERN_ELEMENT);
                        for (int f = 0; f < filters.getLength(); f++)
                        {
                            Node pattern = filters.item(f);
                            String filter = evaluateExpression(pattern, ".").toString().trim();
                            targetSettings.addFilter(filter);
                        }
                    }

                    // Target Properties
                    properties = selectNodeList(targetNode, PROPERTIES_ELEMENT + "/*");
                    if (properties.getLength() > 0)
                    {
                        ConfigMap map = properties(properties, getSourceFileOf(targetNode));
                        targetSettings.addProperties(map);
                    }

                    settings.addTarget(targetSettings);
                }
            }

            config.setLoggingSettings(settings);
        }
    }

    private void system(Node root)
    {
        Node system = selectSingleNode(root, SYSTEM_ELEMENT);
        if (system == null)
        {
            // Create a default instance of SystemSettings which by default has setManagable as true
            // and has setRedeployEnabled as false.
            ((MessagingConfiguration)config).setSystemSettings(new SystemSettings());
            return;
        }

        allowedAttributesOrElements(system, SYSTEM_CHILDREN);

        SystemSettings settings = new SystemSettings();

        settings.setEnforceEndpointValidation(getAttributeOrChildElement(system, ENFORCE_ENDOINT_VALIDATION));
        locale(system, settings);
        settings.setManageable(getAttributeOrChildElement(system, MANAGEABLE_ELEMENT));
        settings.setDotNetFrameworkVersion(getAttributeOrChildElement(system, DOTNET_FRAMEWORK_VERSION));
        redeploy(system, settings);
        uuidGenerator(system, settings);

        ((MessagingConfiguration)config).setSystemSettings(settings);
    }

    private void redeploy(Node system, SystemSettings settings)
    {
        Node redeployNode = selectSingleNode(system, REDEPLOY_ELEMENT);
        if (redeployNode == null)
            return;

        allowedAttributesOrElements(redeployNode, REDEPLOY_CHILDREN);

        String enabled = getAttributeOrChildElement(redeployNode, ENABLED_ELEMENT);
        settings.setRedeployEnabled(enabled);

        String interval = getAttributeOrChildElement(redeployNode, WATCH_INTERVAL_ELEMENT);
        if (interval.length() > 0)
        {
            settings.setWatchInterval(interval);
        }

        NodeList watches = selectNodeList(redeployNode, WATCH_FILE_ELEMENT);
        for (int i = 0; i < watches.getLength(); i++)
        {
            Node watchNode = watches.item(i);
            String watch = evaluateExpression(watchNode, ".").toString().trim();
            if (watch.length() > 0)
            {
                settings.addWatchFile(watch);
            }
        }

        NodeList touches = selectNodeList(redeployNode, TOUCH_FILE_ELEMENT);
        for (int i = 0; i < touches.getLength(); i++)
        {
            Node touchNode = touches.item(i);
            String touch = evaluateExpression(touchNode, ".").toString().trim();
            if (touch.length() > 0)
            {
                settings.addTouchFile(touch);
            }
        }
    }

    private void locale(Node system, SystemSettings settings)
    {
        Node localeNode = selectSingleNode(system, LOCALE_ELEMENT);
        if (localeNode == null)
            return;

        allowedAttributesOrElements(localeNode, LOCALE_CHILDREN);

        String defaultLocaleString = getAttributeOrChildElement(localeNode, DEFAULT_LOCALE_ELEMENT);
        Locale defaultLocale = defaultLocaleString.length() > 0? LocaleUtils.buildLocale(defaultLocaleString) : LocaleUtils.buildLocale(null);
        settings.setDefaultLocale(defaultLocale);
    }

    private void uuidGenerator(Node system, SystemSettings settings)
    {
        Node uuidGenerator = selectSingleNode(system, UUID_GENERATOR_ELEMENT);
        if (uuidGenerator == null)
            return;

        requiredAttributesOrElements(uuidGenerator, UUID_GENERATOR_REQ_CHILDREN);

        String className = getAttributeOrChildElement(uuidGenerator, CLASS_ATTR);
        if (className.length() == 0)
        {
            // Class not specified for {UUID_GENERATOR_ELEMENT} '{id}'.
            ConfigurationException ex = new ConfigurationException();
            ex.setMessage(CLASS_NOT_SPECIFIED, new Object[]{UUID_GENERATOR_ELEMENT, ""});
            throw ex;
        }

        settings.setUUIDGeneratorClassName(className);
    }

    private void flexClient(Node root)
    {
        Node flexClient = selectSingleNode(root, FLEX_CLIENT_ELEMENT);
        if (flexClient != null)
        {
            // Validation
            allowedChildElements(flexClient, FLEX_CLIENT_CHILDREN);

            FlexClientSettings flexClientSettings = new FlexClientSettings();

            // Timeout
            String timeout = getAttributeOrChildElement(flexClient, FLEX_CLIENT_TIMEOUT_MINUTES_ELEMENT);
            if (timeout.length() > 0)
            {
                try
                {
                    long timeoutMinutes = Long.parseLong(timeout);
                    if (timeoutMinutes < 0)
                    {
                        // Invalid timeout minutes value ''{0}'' in the <flex-client> configuration section. Please specify a positive value or leave the element undefined in which case flex client instances on the server will be timed out when all associated sessions/connections have shut down.
                        ConfigurationException e = new ConfigurationException();
                        e.setMessage(INVALID_FLEX_CLIENT_TIMEOUT, new Object[]{timeout});
                        throw e;
                    }
                    flexClientSettings.setTimeoutMinutes(timeoutMinutes);
                }
                catch (NumberFormatException nfe)
                {
                    // Invalid timeout minutes value ''{0}'' in the <flex-client> configuration section. Please specify a positive value or leave the element undefined in which case flex client instances on the server will be timed out when all associated sessions/connections have shut down.
                    ConfigurationException e = new ConfigurationException();
                    e.setMessage(INVALID_FLEX_CLIENT_TIMEOUT, new Object[]{timeout});
                    throw e;
                }
            }
            else
            {
                flexClientSettings.setTimeoutMinutes(0); // Default to 0; in this case FlexClients are invalidated when all associated sessions have been invalidated.
            }

            // Flex client queue processor
            Node outboundQueueProcessor = selectSingleNode(flexClient, FLEX_CLIENT_OUTBOUND_QUEUE_PROCESSOR_ELEMENT);
            if (outboundQueueProcessor != null)
            {
                // Validation
                requiredAttributesOrElements(outboundQueueProcessor, FLEX_CLIENT_OUTBOUND_QUEUE_PROCESSOR_REQ_CHILDREN);

                // Flex client queue processor class
                String outboundQueueProcessClass = getAttributeOrChildElement(outboundQueueProcessor, CLASS_ATTR);
                if (outboundQueueProcessClass.length() > 0)
                {
                    flexClientSettings.setFlexClientOutboundQueueProcessorClassName(outboundQueueProcessClass);
                }
                else
                {
                    // Class not specified for {FLEX_CLIENT_OUTBOUND_QUEUE_PROCESSOR_ELEMENT} '{id}'.
                    ConfigurationException ex = new ConfigurationException();
                    ex.setMessage(CLASS_NOT_SPECIFIED, new Object[]{FLEX_CLIENT_OUTBOUND_QUEUE_PROCESSOR_ELEMENT, ""});
                    throw ex;
                }

                // Flex client queue processor properties
                NodeList properties = selectNodeList(outboundQueueProcessor, PROPERTIES_ELEMENT + "/*");
                if (properties.getLength() > 0)
                {
                    ConfigMap map = properties(properties, getSourceFileOf(outboundQueueProcessor));
                    flexClientSettings.setFlexClientOutboundQueueProcessorProperties(map);
                    // Sniff for adaptive-frequency which requires advanced messaging support.
                    boolean adaptiveFrequency = map.getPropertyAsBoolean(ADAPTIVE_FREQUENCY, false);
                    if (adaptiveFrequency)
                        verifyAdvancedMessagingSupport = true;
                }
            }
            ((MessagingConfiguration)config).setFlexClientSettings(flexClientSettings);
        }
    }

    private void factories(Node root)
    {
        Node factories = selectSingleNode(root, FACTORIES_ELEMENT);
        if (factories != null)
        {
            // Validation
            allowedAttributesOrElements(factories, FACTORIES_CHILDREN);

            NodeList factoryList = selectNodeList(factories, FACTORY_ELEMENT);
            for (int i = 0; i < factoryList.getLength(); i++)
            {
                Node factory = factoryList.item(i);
                factory(factory);
            }
        }
    }

    private void factory(Node factory)
    {
        // Validation
        requiredAttributesOrElements(factory, FACTORY_REQ_CHILDREN);

        String id = getAttributeOrChildElement(factory, ID_ATTR);
        String className = getAttributeOrChildElement(factory, CLASS_ATTR);
        if (isValidID(id))
        {
            FactorySettings factorySettings = new FactorySettings(id, className);

            // Factory Properties
            NodeList properties = selectNodeList(factory, PROPERTIES_ELEMENT + "/*");
            if (properties.getLength() > 0)
            {
                ConfigMap map = properties(properties, getSourceFileOf(factory));
                factorySettings.addProperties(map);
            }
            ((MessagingConfiguration)config).addFactorySettings(id, factorySettings);
        }
        else
        {
            // Invalid {FACTORY_ELEMENT} id '{id}'.
            ConfigurationException ex = new ConfigurationException();
            ex.setMessage(INVALID_ID, new Object[]{FACTORY_ELEMENT, id});
            ex.setDetails(INVALID_ID);
            throw ex;
        }
    }

    private void messageFilters(Node root)
    {
        typedMessageFilters(root, ASYNC_MESSAGE_FILTERS_ELEMENT, ASYNC_MESSAGE_FILTERS_ELEMENT_CHILDREN);
        typedMessageFilters(root, SYNC_MESSAGE_FILTERS_ELEMENT, SYNC_MESSAGE_FILTERS_ELEMENT_CHILDREN);
    }

    private void typedMessageFilters(Node root, String filterTypeElement, String[] childrenElements)
    {
        Node messageFiltersNode = selectSingleNode(root, filterTypeElement);
        if (messageFiltersNode == null)
            return;

        // Validation
        allowedChildElements(messageFiltersNode, childrenElements);

        // Message filter
        NodeList messageFilters = selectNodeList(messageFiltersNode, FILTER_ELEMENT);
        for (int i = 0; i < messageFilters.getLength(); i++)
        {
            Node messageFilter = messageFilters.item(i);
            messageFilter(messageFilter, filterTypeElement);
        }
    }

    private void messageFilter(Node messageFilter, String filterType)
    {
        // Validation
        requiredAttributesOrElements(messageFilter, FILTER_REQ_CHILDREN);
        allowedAttributesOrElements(messageFilter, FILTER_CHILDREN);

        String id = getAttributeOrChildElement(messageFilter, ID_ATTR);
        if (isValidID(id))
        {
            // Message filter class name
            String className = getAttributeOrChildElement(messageFilter, CLASS_ATTR);
            if (className.length() > 0)
            {
                MessageFilterSettings messageFilterSettings = new MessageFilterSettings();
                messageFilterSettings.setId(id);
                messageFilterSettings.setClassName(className);
                // Record type of filter.
                MessageFilterSettings.FilterType type = filterType.equals(ASYNC_MESSAGE_FILTERS_ELEMENT) ?
                                                        MessageFilterSettings.FilterType.ASYNC :
                                                        MessageFilterSettings.FilterType.SYNC;
                messageFilterSettings.setFilterType(type);
                // Custom server properties.
                NodeList properties = selectNodeList(messageFilter, PROPERTIES_ELEMENT + "/*");
                if (properties.getLength() > 0)
                {
                    ConfigMap map = properties(properties, getSourceFileOf(messageFilter));
                    messageFilterSettings.addProperties(map);
                }
                ((MessagingConfiguration)config).addMessageFilterSettings(messageFilterSettings);
            }
            else
            {
                // Class not specified for {FILTER_ELEMENT} '{id}'.
                ConfigurationException ex = new ConfigurationException();
                ex.setMessage(CLASS_NOT_SPECIFIED, new Object[]{FILTER_ELEMENT, id});
                throw ex;
            }
        }
    }

    private void validators(Node root)
    {
        Node validatorsNode = selectSingleNode(root, VALIDATORS_ELEMENT);
        if (validatorsNode == null)
            return;

        // Validation
        allowedChildElements(validatorsNode, VALIDATORS_CHILDREN);

        // Validator
        NodeList validators = selectNodeList(validatorsNode, VALIDATOR_ELEMENT);
        for (int i = 0; i < validators.getLength(); i++)
        {
            Node validator = validators.item(i);
            validator(validator);
        }
    }

    private void validator(Node validator)
    {
        // Validation
        requiredAttributesOrElements(validator, VALIDATOR_REQ_CHILDREN);
        allowedAttributesOrElements(validator, VALIDATOR_CHILDREN);

        ValidatorSettings validatorSettings = new ValidatorSettings();

        // Validator class name
        String className = getAttributeOrChildElement(validator, CLASS_ATTR);
        if (className.length() > 0)
        {
            validatorSettings.setClassName(className);
        }
        else
        {
            // Class not specified for {VALIDATOR_ELEMENT} '{id}'.
            ConfigurationException ex = new ConfigurationException();
            ex.setMessage(CLASS_NOT_SPECIFIED, new Object[]{VALIDATOR_ELEMENT, ""});
            throw ex;
        }

        // Validator type
        String type = getAttributeOrChildElement(validator, TYPE_ATTR);
        if (type.length() > 0)
            validatorSettings.setType(type);

        // Validator properties
        NodeList properties = selectNodeList(validator, PROPERTIES_ELEMENT + "/*");
        if (properties.getLength() > 0)
        {
            ConfigMap map = properties(properties, getSourceFileOf(validator));
            validatorSettings.addProperties(map);
        }

        ((MessagingConfiguration)config).addValidatorSettings(validatorSettings);
    }
}
