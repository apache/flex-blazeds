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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A special mxmlc compiler specific implentation of the configuration
 * parser for JDK 1.4. Only a small subset of the configuration is
 * processed to generate the information that the client needs at runtime,
 * such as channel definitions and service destination properties.
 *
 * @author Peter Farland
 * @exclude
 */
public abstract class ClientConfigurationParser extends AbstractConfigurationParser
{
    protected void parseTopLevelConfig(Document doc)
    {
        Node root = selectSingleNode(doc, "/" + SERVICES_CONFIG_ELEMENT);

        if (root != null)
        {
            // Validation
            allowedChildElements(root, SERVICES_CONFIG_CHILDREN);

            // Channels (parse before services)
            channelsSection(root);

            // Services
            services(root);

            // Clustering
            clusters(root);

            // FlexClient
            flexClient(root);
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

        String id = getAttributeOrChildElement(channel, ID_ATTR).trim();
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

            // Endpoint
            Node endpoint = selectSingleNode(channel, ENDPOINT_ELEMENT);
            if (endpoint != null)
            {
                // Endpoint Validation
                allowedAttributesOrElements(endpoint, ENDPOINT_CHILDREN);

                // The url attribute may also be specified by the deprecated uri attribute
                String uri = getAttributeOrChildElement(endpoint, URL_ATTR);
                if (uri == null || EMPTY_STRING.equals(uri))
                    uri = getAttributeOrChildElement(endpoint, URI_ATTR);
                channelSettings.setUri(uri);

                config.addChannelSettings(id, channelSettings);
            }

            channelServerOnlyAttribute(channel, channelSettings);

            // Add the channel properties that the client needs namely polling-enabled,
            // polling-interval-millis, piggybacking-enabled, login-after-disconnect,
            // record-message-sizes, record-message-times, connect-timeout-seconds,
            // polling-interval-seconds (deprecated), and client-load-balancing.
            addProperty(channel, channelSettings, POLLING_ENABLED_ELEMENT);
            addProperty(channel, channelSettings, POLLING_INTERVAL_MILLIS_ELEMENT);
            addProperty(channel, channelSettings, PIGGYBACKING_ENABLED_ELEMENT);
            addProperty(channel, channelSettings, LOGIN_AFTER_DISCONNECT_ELEMENT);
            addProperty(channel, channelSettings, RECORD_MESSAGE_SIZES_ELEMENT);
            addProperty(channel, channelSettings, RECORD_MESSAGE_TIMES_ELEMENT);
            addProperty(channel, channelSettings, CONNECT_TIMEOUT_SECONDS_ELEMENT);
            addProperty(channel, channelSettings, POLLING_INTERVAL_SECONDS_ELEMENT); // deprecated.
            addProperty(channel, channelSettings, CLIENT_LOAD_BALANCING_ELEMENT);
            addProperty(channel, channelSettings, REQUEST_TIMEOUT_SECONDS_ELEMENT);

            // enable-small-messages.
            NodeList properties = selectNodeList(channel, PROPERTIES_ELEMENT + "/" + SERIALIZATION_ELEMENT);
            if (properties.getLength() > 0)
            {
                ConfigMap map = properties(properties, getSourceFileOf(channel));
                ConfigMap serialization = map.getPropertyAsMap(SERIALIZATION_ELEMENT, null);
                if (serialization != null)
                {
                    // enable-small-messages.
                    String enableSmallMessages = serialization.getProperty(ENABLE_SMALL_MESSAGES_ELEMENT);
                    if (enableSmallMessages != null)
                    {
                        ConfigMap clientMap = new ConfigMap();
                        clientMap.addProperty(ENABLE_SMALL_MESSAGES_ELEMENT, enableSmallMessages);
                        channelSettings.addProperty(SERIALIZATION_ELEMENT, clientMap);
                    }
                }
            }
        }
        else
        {
            // Invalid {CHANNEL_DEFINITION_ELEMENT} id '{id}'.
            ConfigurationException ex = new ConfigurationException();
            ex.setMessage(INVALID_ID, new Object[]{CHANNEL_DEFINITION_ELEMENT, id});
            String details = "An id must be non-empty and not contain any list delimiter characters, i.e. commas, semi-colons or colons.";
            ex.setDetails(details);
            throw ex;
        }
    }

    private void channelServerOnlyAttribute(Node channel, ChannelSettings channelSettings)
    {
        String clientType = getAttributeOrChildElement(channel, CLASS_ATTR);
        clientType = clientType.length() > 0? clientType : null;

        String serverOnlyString = getAttributeOrChildElement(channel, SERVER_ONLY_ATTR);
        boolean serverOnly = serverOnlyString.length() > 0 && Boolean.valueOf(serverOnlyString).booleanValue();

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

    private void addProperty(Node channel, ChannelSettings channelSettings, String property)
    {
        NodeList properties = selectNodeList(channel, PROPERTIES_ELEMENT + "/" + property);
        if (properties.getLength() > 0)
        {
            ConfigMap map = properties(properties, getSourceFileOf(channel));
            if (CLIENT_LOAD_BALANCING_ELEMENT.equals(property))
            {
                ConfigMap clientLoadBalancingMap = map.getPropertyAsMap(CLIENT_LOAD_BALANCING_ELEMENT, null);
                if (clientLoadBalancingMap == null)
                {
                    // Invalid {0} configuration for endpoint ''{1}''; no urls defined.
                    ConfigurationException ce = new ConfigurationException();
                    ce.setMessage(ERR_MSG_EMPTY_CLIENT_LOAD_BALANCING_ELEMENT, new Object[]{CLIENT_LOAD_BALANCING_ELEMENT, channelSettings.getId()});
                    throw ce;
                }
                List urls = clientLoadBalancingMap.getPropertyAsList(URL_ATTR, null);
                addClientLoadBalancingUrls(urls, channelSettings.getId());
            }
            channelSettings.addProperties(map);
        }
    }

    // Add client load balancing urls after necessary validation checks.
    private void addClientLoadBalancingUrls(List urls, String endpointId)
    {
        if (urls == null || urls.isEmpty())
        {
            // Invalid {0} configuration for endpoint ''{1}''; no urls defined.
            ConfigurationException ce = new ConfigurationException();
            ce.setMessage(ERR_MSG_EMPTY_CLIENT_LOAD_BALANCING_ELEMENT, new Object[]{CLIENT_LOAD_BALANCING_ELEMENT, endpointId});
            throw ce;
        }

        Set clientLoadBalancingUrls = new HashSet();
        for (Iterator iterator = urls.iterator(); iterator.hasNext();)
        {
            String url = (String) iterator.next();
            if (url == null || url.length() == 0)
            {
                // Invalid {0} configuration for endpoint ''{1}''; cannot add empty url.
                ConfigurationException  ce = new ConfigurationException();
                ce.setMessage(ERR_MSG_EMTPY_CLIENT_LOAD_BALACNING_URL, new Object[]{CLIENT_LOAD_BALANCING_ELEMENT, endpointId});
                throw ce;
            }

            if (TokenReplacer.containsTokens(url))
            {
                // Invalid {0} configuration for endpoint ''{1}''; cannot add url with tokens.
                ConfigurationException  ce = new ConfigurationException();
                ce.setMessage(ERR_MSG_CLIENT_LOAD_BALANCING_URL_WITH_TOKEN, new Object[]{CLIENT_LOAD_BALANCING_ELEMENT, endpointId});
                throw ce;
            }

            if (clientLoadBalancingUrls.contains(url))
                iterator.remove();
            else
                clientLoadBalancingUrls.add(url);
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
        if (fileResolver instanceof LocalFileResolver)
        {
            LocalFileResolver local = (LocalFileResolver)fileResolver;
            ((ClientConfiguration)config).addConfigPath(local.getIncludedPath(src), local.getIncludedLastModified(src));
        }

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
                if (isValidID(clusterName))
                {
                    String propsFileName = getAttributeOrChildElement(cluster, CLUSTER_PROPERTIES_ATTR);
                    ClusterSettings clusterSettings = new ClusterSettings();
                    clusterSettings.setClusterName(clusterName);
                    clusterSettings.setPropsFileName(propsFileName);
                    String defaultValue = getAttributeOrChildElement(cluster, ClusterSettings.DEFAULT_ELEMENT);
                    if (defaultValue != null && defaultValue.length() > 0)
                    {
                        if (defaultValue.equalsIgnoreCase("true"))
                            clusterSettings.setDefault(true);
                        else if (!defaultValue.equalsIgnoreCase("false"))
                        {
                            ConfigurationException e = new ConfigurationException();
                            e.setMessage(10215, new Object[] {clusterName, defaultValue});
                            throw e;
                        }
                    }
                    String ulb = getAttributeOrChildElement(cluster, ClusterSettings.URL_LOAD_BALANCING);
                    if (ulb != null && ulb.length() > 0)
                    {
                        if (ulb.equalsIgnoreCase("false"))
                            clusterSettings.setURLLoadBalancing(false);
                        else if (!ulb.equalsIgnoreCase("true"))
                        {
                            ConfigurationException e = new ConfigurationException();
                            e.setMessage(10216, new Object[] {clusterName, ulb});
                            throw e;
                        }
                    }
                    ((ClientConfiguration)config).addClusterSettings(clusterSettings);
                }
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
        if (fileResolver instanceof LocalFileResolver)
        {
            LocalFileResolver local = (LocalFileResolver)fileResolver;
            ((ClientConfiguration)config).addConfigPath(local.getIncludedPath(src), local.getIncludedLastModified(src));
        }

        doc.getDocumentElement().normalize();

        // Check for multiple services defined in file.
        Node servicesNode = selectSingleNode(doc, SERVICES_ELEMENT);
        if (servicesNode != null)
        {
            allowedChildElements(servicesNode, SERVICES_CHILDREN);
            NodeList services = selectNodeList(servicesNode, SERVICES_ELEMENT);
            for (int a = 0; a < services.getLength(); a++)
            {
                Node service = services.item(a);
                service(service);
            }
            fileResolver.popIncludedFile();
        }
        else // Check for single service in file.
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
                // Service Properties
                NodeList properties = selectNodeList(service, PROPERTIES_ELEMENT + "/*");
                if (properties.getLength() > 0)
                {
                    ConfigMap map = properties(properties, getSourceFileOf(service));
                    serviceSettings.addProperties(map);
                }
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
            }
            else
            {
                // Class not specified for {SERVICE_ELEMENT} '{id}'.
                ConfigurationException ex = new ConfigurationException();
                ex.setMessage(CLASS_NOT_SPECIFIED, new Object[]{SERVICE_ELEMENT, id});
                throw ex;
            }

            //Service Message Types - deprecated

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
     * Flex application can declare default channels for its services. If a
     * service specifies its own list of channels it overrides these defaults.
     * <p>
     * &lt;default-channels&gt;<br />
     * ;&lt;channel ref="channel-id" /&gt;<br />
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
     * &lt;default-channels&gt;<br />
     * &lt;channel ref="channel-id" /&gt;<br />
     * &lt;default-channels&gt;
     * </p>
     * @param chan the channel node
     * @param serviceSettings service settings
     */
    private void defaultChannel(Node chan, ServiceSettings serviceSettings)
    {
        String ref = getAttributeOrChildElement(chan, REF_ATTR).trim();

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
        if (fileResolver instanceof LocalFileResolver)
        {
            LocalFileResolver local = (LocalFileResolver)fileResolver;
            ((ClientConfiguration)config).addConfigPath(local.getIncludedPath(src), local.getIncludedLastModified(src));
        }

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
        }

        // Channels
        destinationChannels(dest, destinationSettings, serviceSettings);

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
                allowedChildElements
                    (channelsNode, DESTINATION_CHANNELS_CHILDREN);
                NodeList channels = selectNodeList(channelsNode, CHANNEL_ELEMENT);
                if (channels.getLength() > 0)
                {
                    for (int c = 0; c < channels.getLength(); c++)
                    {
                        Node chan = channels.item(c);

                        // Validation
                        requiredAttributesOrElements(chan, DESTINATION_CHANNEL_REQ_CHILDREN);

                        String ref = getAttributeOrChildElement(chan, REF_ATTR).trim();
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

    private void flexClient(Node root)
    {
        Node flexClient = selectSingleNode(root, FLEX_CLIENT_ELEMENT);
        if (flexClient != null)
        {
            FlexClientSettings flexClientSettings = new FlexClientSettings();
            // Reliable reconnect duration millis
            String reliableReconnectDurationMillis = getAttributeOrChildElement(flexClient, FLEX_CLIENT_RELIABLE_RECONNECT_DURATION_MILLIS);
            if (reliableReconnectDurationMillis.length() > 0)
            {
                try
                {
                    int millis = Integer.parseInt(reliableReconnectDurationMillis);
                    if (millis < 0)
                    {
                        ConfigurationException e = new ConfigurationException();
                        e.setMessage(INVALID_FLEX_CLIENT_RELIABLE_RECONNECT_DURATION_MILLIS, new Object[]{reliableReconnectDurationMillis});
                        throw e;
                    }
                    flexClientSettings.setReliableReconnectDurationMillis(millis);
                }
                catch (NumberFormatException nfe)
                {
                    ConfigurationException e = new ConfigurationException();
                    e.setMessage(INVALID_FLEX_CLIENT_RELIABLE_RECONNECT_DURATION_MILLIS, new Object[]{reliableReconnectDurationMillis});
                    throw e;
                }
            }
            else
            {
                flexClientSettings.setReliableReconnectDurationMillis(0); // Default is 0.
            }
            // heartbeat interval millis
            String heartbeatIntervalMillis = getAttributeOrChildElement(flexClient, FLEX_CLIENT_HEARTBEAT_INTERVAL_MILLIS);
            if (heartbeatIntervalMillis.length() > 0)
            {
                try
                {
                    int millis = Integer.parseInt(heartbeatIntervalMillis);
                    if (millis < 0)
                    {
                        ConfigurationException e = new ConfigurationException();
                        e.setMessage(INVALID_FLEX_CLIENT_HEARTBEAT_INTERVAL_MILLIS, new Object[] {heartbeatIntervalMillis});
                        throw e;
                    }
                    flexClientSettings.setHeartbeatIntervalMillis(millis);
                }
                catch (NumberFormatException nfe)
                {
                    ConfigurationException e = new ConfigurationException();
                    e.setMessage(INVALID_FLEX_CLIENT_HEARTBEAT_INTERVAL_MILLIS, new Object[] {heartbeatIntervalMillis});
                    throw e;
                }
            }
            ((ClientConfiguration)config).setFlexClientSettings(flexClientSettings);
        }
    }
}
