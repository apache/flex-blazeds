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
package flex.messaging;

import flex.management.MBeanLifecycleManager;
import flex.management.MBeanServerLocatorFactory;
import flex.messaging.config.ConfigurationManager;
import flex.messaging.config.FlexConfigurationManager;
import flex.messaging.config.MessagingConfiguration;
import flex.messaging.endpoints.Endpoint;
import flex.messaging.io.SerializationContext;
import flex.messaging.io.TypeMarshallingContext;
import flex.messaging.log.HTTPRequestLog;
import flex.messaging.log.Log;
import flex.messaging.log.LogCategories;
import flex.messaging.log.Logger;
import flex.messaging.log.LoggingHttpServletRequestWrapper;
import flex.messaging.log.ServletLogTarget;
import flex.messaging.services.AuthenticationService;
import flex.messaging.util.ClassUtil;
import flex.messaging.util.ExceptionUtil;
import flex.messaging.util.Trace;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The MessageBrokerServlet bootstraps the MessageBroker,
 * adds endpoints to it, and starts the broker. The servlet
 * also acts as a facade for all http-based endpoints, in that
 * the servlet receives the http request and then delegates to
 * an endpoint that can handle the request's content type. This
 * does not occur for non-http endpoints, such as the rtmp endpoint.
 *
 * @see flex.messaging.MessageBroker
 */
public class MessageBrokerServlet extends HttpServlet {
    static final long serialVersionUID = -5293855229461612246L;

    public static final String LOG_CATEGORY_STARTUP_BROKER = LogCategories.STARTUP_MESSAGEBROKER;
    private static final String STRING_UNDEFINED_APPLICATION = "undefined";

    private MessageBroker broker;
    private HttpFlexSessionProvider httpFlexSessionProvider;
    private static String FLEXDIR = "/WEB-INF/flex/";
    private boolean log_errors = false;

    /**
     * Initializes the servlet in its web container, then creates
     * the MessageBroker and adds Endpoints and Services to that broker.
     * This servlet may keep a reference to an endpoint if it needs to
     * delegate to it in the <code>service</code> method.
     */
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        // allocate thread local variables
        createThreadLocals();

        // Set the servlet config as thread local
        FlexContext.setThreadLocalObjects(null, null, null, null, null, servletConfig);

        ServletLogTarget.setServletContext(servletConfig.getServletContext());

        ClassLoader loader = getClassLoader();

        if ("true".equals(servletConfig.getInitParameter("useContextClassLoader"))) {
            loader = Thread.currentThread().getContextClassLoader();
        }

        // Should we wrap http request for later error logging?
        log_errors = HTTPRequestLog.init(getServletContext());

        // Start the broker
        try {
            // Get the configuration manager
            ConfigurationManager configManager = loadMessagingConfiguration(servletConfig);

            // Load configuration
            MessagingConfiguration config = configManager.getMessagingConfiguration(servletConfig);

            // Set up logging system ahead of everything else.
            config.createLogAndTargets();

            // Create broker.
            broker = config.createBroker(servletConfig.getInitParameter("messageBrokerId"), loader);

            // Set the servlet config as thread local
            FlexContext.setThreadLocalObjects(null, null, broker, null, null, servletConfig);

            setupPathResolvers();

            // Set initial servlet context on broker
            broker.setServletContext(servletConfig.getServletContext());

            Logger logger = Log.getLogger(ConfigurationManager.LOG_CATEGORY);
            if (Log.isInfo()) {
                logger.info(VersionInfo.buildMessage());
            }

            // Create endpoints, services, security, and logger on the broker based on configuration
            config.configureBroker(broker);

            long timeBeforeStartup = 0;
            if (Log.isDebug()) {
                timeBeforeStartup = System.currentTimeMillis();
                Log.getLogger(LOG_CATEGORY_STARTUP_BROKER).debug("MessageBroker with id '{0}' is starting.",
                        new Object[]{broker.getId()});
            }

            //initialize the httpSessionToFlexSessionMap
            synchronized (HttpFlexSession.mapLock) {
                if (servletConfig.getServletContext().getAttribute(HttpFlexSession.SESSION_MAP) == null)
                    servletConfig.getServletContext().setAttribute(HttpFlexSession.SESSION_MAP, new ConcurrentHashMap());
            }

            broker.start();

            if (Log.isDebug()) {
                long timeAfterStartup = System.currentTimeMillis();
                Long diffMillis = timeAfterStartup - timeBeforeStartup;
                Log.getLogger(LOG_CATEGORY_STARTUP_BROKER).debug("MessageBroker with id '{0}' is ready (startup time: '{1}' ms)",
                        new Object[]{broker.getId(), diffMillis});
            }

            // Report replaced tokens
            configManager.reportTokens();

            // Report any unused properties.
            config.reportUnusedProperties();

            // Setup provider for FlexSessions that wrap underlying J2EE HttpSessions.
            httpFlexSessionProvider = new HttpFlexSessionProvider();
            broker.getFlexSessionManager().registerFlexSessionProvider(HttpFlexSession.class, httpFlexSessionProvider);

            // clear the broker and servlet config as this thread is done
            FlexContext.clearThreadLocalObjects();
        } catch (Throwable t) {
            // On any unhandled exception destroy the broker, log it and rethrow.
            String applicationName = servletConfig.getServletContext().getServletContextName();
            if (applicationName == null)
                applicationName = STRING_UNDEFINED_APPLICATION;

            System.err.println("**** MessageBrokerServlet in application '" + applicationName
                    + "' failed to initialize due to runtime exception: "
                    + ExceptionUtil.exceptionFollowedByRootCausesToString(t));
            destroy();
            // We used to throw  UnavailableException, but Weblogic didn't mark the webapp as failed. See bug FBR-237
            throw new ServletException(t);
        }
    }

    private void setupPathResolvers() {
        setupExternalPathResolver();
        setupInternalPathResolver();
    }

    private void setupExternalPathResolver() {
        broker.setExternalPathResolver(
                new MessageBroker.PathResolver() {
                    public InputStream resolve(String filename) throws FileNotFoundException {
                        return new FileInputStream(new File(filename));
                    }
                }
        );
    }

    private void setupInternalPathResolver() {
        broker.setInternalPathResolver(
                new MessageBroker.InternalPathResolver() {
                    public InputStream resolve(String filename) {
                        return getServletContext().getResourceAsStream(FLEXDIR + filename);
                    }
                }
        );
    }

    private static ConfigurationManager loadMessagingConfiguration(ServletConfig servletConfig) {
        ConfigurationManager manager = null;
        Class managerClass;
        String className;

        // Check for Custom Configuration Manager Specification
        if (servletConfig != null) {
            String p = servletConfig.getInitParameter("services.configuration.manager");
            if (p != null) {
                className = p.trim();
                try {
                    managerClass = ClassUtil.createClass(className);
                    manager = (ConfigurationManager) managerClass.newInstance();
                } catch (Throwable t) {
                    if (Trace.config) // Log is not initialized yet.
                        Trace.trace("Could not load configuration manager as: " + className);
                }
            }
        }

        if (manager == null) {
            manager = new FlexConfigurationManager();
        }

        return manager;
    }

    /**
     * Stops all endpoints in the MessageBroker, giving them a chance
     * to perform any endpoint-specific clean up.
     */
    public void destroy() {
        if (broker != null) {
            broker.stop();
            if (broker.isManaged()) {
                MBeanLifecycleManager.unregisterRuntimeMBeans(broker);
            }
            // release static thread locals
            destroyThreadLocals();
        }
    }

    /**
     * Handle an incoming request, and delegate to an endpoint based on
     * content type, if appropriate. The content type mappings for endpoints
     * are not externally configurable, and currently the AmfEndpoint
     * is the only delegate.
     */
    public void service(HttpServletRequest req, HttpServletResponse res) {
        if (log_errors) {
            // Create a wrapper for the request object so we can save the body content
            LoggingHttpServletRequestWrapper wrapper = new LoggingHttpServletRequestWrapper(req);
            req = wrapper;

            try {
                // Read the body content
                wrapper.doReadBody();
            } catch (IOException ignore) {
                // ignore, the wrapper will preserve what content we were able to read.
            }
        }

        try {
            // Update thread locals
            broker.initThreadLocals();
            // Set this first so it is in place for the session creation event.  The
            // current session is set by the FlexSession stuff right when it is available.
            // The threadlocal FlexClient is set up during message deserialization in the
            // MessageBrokerFilter.
            FlexContext.setThreadLocalObjects(null, null, broker, req, res, getServletConfig());

            HttpFlexSession fs = httpFlexSessionProvider.getOrCreateSession(req);
            Principal principal;
            if (FlexContext.isPerClientAuthentication()) {
                principal = FlexContext.getUserPrincipal();
            } else {
                principal = fs.getUserPrincipal();
            }

            if (principal == null && req.getHeader("Authorization") != null) {
                String encoded = req.getHeader("Authorization");
                if (encoded.indexOf("Basic") > -1) {
                    encoded = encoded.substring(6); //Basic.length()+1
                    try {
                        ((AuthenticationService) broker.getService(AuthenticationService.ID)).decodeAndLogin(encoded, broker.getLoginManager());
                    } catch (Exception e) {
                        if (Log.isDebug())
                            Log.getLogger(LogCategories.SECURITY).info("Authentication service could not decode and login: " + e.getMessage());
                    }
                }
            }

            String contextPath = req.getContextPath();
            String pathInfo = req.getPathInfo();
            String endpointPath = req.getServletPath();
            if (pathInfo != null)
                endpointPath = endpointPath + pathInfo;

            Endpoint endpoint;
            try {
                endpoint = broker.getEndpoint(endpointPath, contextPath);
            } catch (MessageException me) {
                if (Log.isInfo())
                    Log.getLogger(LogCategories.ENDPOINT_GENERAL).info("Received invalid request for endpoint path '{0}'.", new Object[]{endpointPath});

                if (!res.isCommitted()) {
                    try {
                        res.sendError(HttpServletResponse.SC_NOT_FOUND);
                    } catch (IOException ignore) {
                    }
                }

                return;
            }

            try {
                if (Log.isInfo()) {
                    Log.getLogger(LogCategories.ENDPOINT_GENERAL).info("Channel endpoint {0} received request.",
                            new Object[]{endpoint.getId()});
                }
                endpoint.service(req, res);
            } catch (UnsupportedOperationException ue) {
                if (Log.isInfo()) {
                    Log.getLogger(LogCategories.ENDPOINT_GENERAL).info("Channel endpoint {0} received request for an unsupported operation.",
                            new Object[]{endpoint.getId()},
                            ue);
                }

                if (!res.isCommitted()) {
                    try {
                        res.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                    } catch (IOException ignore) {
                    }
                }
            }
        } catch (Throwable t) {
            // Final resort catch block as recommended by Fortify as a potential System info leak
            try {
                Log.getLogger(LogCategories.ENDPOINT_GENERAL).error("Unexpected error encountered in Message Broker servlet", t);
                res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (IOException ignore) {
                // ignore
            }

        } finally {
            if (log_errors) {
                String info = (String) req.getAttribute(HTTPRequestLog.HTTP_ERROR_INFO);
                if (info != null) {
                    // Log the HttpRequest data
                    System.out.println("Exception occurred while processing HTTP request: " + info + ", request details logged in " + HTTPRequestLog.getFileName());
                    HTTPRequestLog.outputRequest(info, req);
                }
            }

            FlexContext.clearThreadLocalObjects();
        }
    }

    /**
     * Hook for subclasses to override the class loader to use for loading user defined classes.
     *
     * @return the class loader for this class
     */
    protected ClassLoader getClassLoader() {
        return this.getClass().getClassLoader();
    }


    // Call ONLY on servlet startup
    public static void createThreadLocals() {
        // allocate static thread local objects
        FlexContext.createThreadLocalObjects();
        SerializationContext.createThreadLocalObjects();
        TypeMarshallingContext.createThreadLocalObjects();
    }


    // Call ONLY on servlet shutdown
    protected static void destroyThreadLocals() {
        // clear static member variables
        Log.clear();
        MBeanServerLocatorFactory.clear();

        // Destroy static thread local objects
        FlexContext.releaseThreadLocalObjects();
        SerializationContext.releaseThreadLocalObjects();
        TypeMarshallingContext.releaseThreadLocalObjects();
    }

}
