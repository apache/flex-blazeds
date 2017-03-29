[//]: # Licensed to the Apache Software Foundation (ASF) under one or more
[//]: # contributor license agreements.  See the NOTICE file distributed with
[//]: # this work for additional information regarding copyright ownership.
[//]: # The ASF licenses this file to You under the Apache License, Version 2.0
[//]: # (the "License"); you may not use this file except in compliance with
[//]: # the License.  You may obtain a copy of the License at
[//]: # 
[//]: #     http://www.apache.org/licenses/LICENSE-2.0
[//]: # 
[//]: # Unless required by applicable law or agreed to in writing, software
[//]: # distributed under the License is distributed on an "AS IS" BASIS,
[//]: # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
[//]: # See the License for the specific language governing permissions and
[//]: # limitations under the License.

# Usage

## Create a new project using the blazeds-spring-boot-archetype

Using the maven-archetype-plugin it is easy to generate a new project utilizing Spring-Boot to setup a BlazeDS server in order to get started with BlazeDS almost instantly.

In order to creat such a project, you nee to execute the following command:
```
mvn archetype:generate -DarchetypeGroupId=org.apache.flex.blazeds -DarchetypeArtifactId=blazeds-spring-boot-example-archetype -DarchetypeVersion=4.7.3-SNAPSHOT
```
The archetype-plugin will ask you for a groupId, artifactId, version and package name.

The first are used to fill the maven coordinates in the generated pom and the package will be used to set the package the generated classes are located in.

## Run the generated application

After having generated your application, just start it by executing the following command:
```
mvn spring-boot:run
```
The output should be something like this:
```
[INFO] Scanning for projects...
[WARNING] 
[WARNING] Some problems were encountered while building the effective model for mygroup:myartifact:war:1.0-SNAPSHOT
[WARNING] 'build.plugins.plugin.version' for org.apache.maven.plugins:maven-war-plugin is missing. @ line 50, column 15
[WARNING] 
[WARNING] It is highly recommended to fix these problems because they threaten the stability of your build.
[WARNING] 
[WARNING] For this reason, future Maven versions might no longer support building such malformed projects.
[WARNING] 
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building myartifact 1.0-SNAPSHOT
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] >>> spring-boot-maven-plugin:1.3.3.RELEASE:run (default-cli) @ myartifact >>>
[INFO] 
[INFO] --- maven-resources-plugin:2.6:resources (default-resources) @ myartifact ---
[WARNING] Using platform encoding (UTF-8 actually) to copy filtered resources, i.e. build is platform dependent!
[INFO] Copying 1 resource
[INFO] 
[INFO] --- maven-compiler-plugin:2.5.1:compile (default-compile) @ myartifact ---
[WARNING] File encoding has not been set, using platform encoding UTF-8, i.e. build is platform dependent!
[INFO] Compiling 2 source files to /Users/christoferdutz/Temp/myartifact/target/classes
[INFO] 
[INFO] --- maven-resources-plugin:2.6:testResources (default-testResources) @ myartifact ---
[WARNING] Using platform encoding (UTF-8 actually) to copy filtered resources, i.e. build is platform dependent!
[INFO] skip non existing resourceDirectory /Users/christoferdutz/Temp/myartifact/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.5.1:testCompile (default-testCompile) @ myartifact ---
[INFO] No sources to compile
[INFO] 
[INFO] <<< spring-boot-maven-plugin:1.3.3.RELEASE:run (default-cli) @ myartifact <<<
[INFO] 
[INFO] --- spring-boot-maven-plugin:1.3.3.RELEASE:run (default-cli) @ myartifact ---

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v1.4.1.RELEASE)

2017-02-17 09:48:50.970  INFO 85498 --- [           main] o.a.m.BlazeDSSpringBootApplication       : Starting BlazeDSSpringBootApplication on Christofers-MacBook-Pro.local with PID 85498 (/Users/christoferdutz/Temp/myartifact/target/classes started by christoferdutz in /Users/christoferdutz/Temp/myartifact)
2017-02-17 09:48:50.972  INFO 85498 --- [           main] o.a.m.BlazeDSSpringBootApplication       : No active profile set, falling back to default profiles: default
2017-02-17 09:48:51.021  INFO 85498 --- [           main] ationConfigEmbeddedWebApplicationContext : Refreshing org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext@6e372704: startup date [Fri Feb 17 09:48:51 CET 2017]; root of context hierarchy
2017-02-17 09:48:52.057  INFO 85498 --- [           main] org.eclipse.jetty.util.log               : Logging initialized @4173ms
2017-02-17 09:48:52.128  INFO 85498 --- [           main] e.j.JettyEmbeddedServletContainerFactory : Server initialized with port: 8080
2017-02-17 09:48:52.129  INFO 85498 --- [           main] org.eclipse.jetty.server.Server          : jetty-9.3.11.v20160721
2017-02-17 09:48:52.207  INFO 85498 --- [           main] application                              : Initializing Spring embedded WebApplicationContext
2017-02-17 09:48:52.207  INFO 85498 --- [           main] o.s.web.context.ContextLoader            : Root WebApplicationContext: initialization completed in 1189 ms
2017-02-17 09:48:52.292  INFO 85498 --- [           main] o.s.b.w.servlet.ServletRegistrationBean  : Mapping servlet: 'dispatcherServlet' to [/]
2017-02-17 09:48:52.293  INFO 85498 --- [           main] o.s.b.w.servlet.FilterRegistrationBean   : Mapping filter: 'characterEncodingFilter' to: [/*]
2017-02-17 09:48:52.293  INFO 85498 --- [           main] o.s.b.w.servlet.FilterRegistrationBean   : Mapping filter: 'hiddenHttpMethodFilter' to: [/*]
2017-02-17 09:48:52.294  INFO 85498 --- [           main] o.s.b.w.servlet.FilterRegistrationBean   : Mapping filter: 'httpPutFormContentFilter' to: [/*]
2017-02-17 09:48:52.294  INFO 85498 --- [           main] o.s.b.w.servlet.FilterRegistrationBean   : Mapping filter: 'requestContextFilter' to: [/*]
2017-02-17 09:48:52.434  INFO 85498 --- [           main] o.e.jetty.server.handler.ContextHandler  : Started o.s.b.c.e.j.JettyEmbeddedWebAppContext@7949ef4a{/,file:///private/var/folders/tv/wtkmxql91m1dm2rxr_htl7wr0000gn/T/jetty-docbase.1578549724746013089.8080/,AVAILABLE}
2017-02-17 09:48:52.434  INFO 85498 --- [           main] org.eclipse.jetty.server.Server          : Started @4552ms
2017-02-17 09:48:52.611  INFO 85498 --- [           main] s.w.s.m.m.a.RequestMappingHandlerAdapter : Looking for @ControllerAdvice: org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext@6e372704: startup date [Fri Feb 17 09:48:51 CET 2017]; root of context hierarchy
2017-02-17 09:48:52.664  INFO 85498 --- [           main] s.w.s.m.m.a.RequestMappingHandlerMapping : Mapped "{[/error]}" onto public org.springframework.http.ResponseEntity<java.util.Map<java.lang.String, java.lang.Object>> org.springframework.boot.autoconfigure.web.BasicErrorController.error(javax.servlet.http.HttpServletRequest)
2017-02-17 09:48:52.665  INFO 85498 --- [           main] s.w.s.m.m.a.RequestMappingHandlerMapping : Mapped "{[/error],produces=[text/html]}" onto public org.springframework.web.servlet.ModelAndView org.springframework.boot.autoconfigure.web.BasicErrorController.errorHtml(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)
2017-02-17 09:48:52.686  INFO 85498 --- [           main] o.s.w.s.handler.SimpleUrlHandlerMapping  : Mapped URL path [/webjars/**] onto handler of type [class org.springframework.web.servlet.resource.ResourceHttpRequestHandler]
2017-02-17 09:48:52.686  INFO 85498 --- [           main] o.s.w.s.handler.SimpleUrlHandlerMapping  : Mapped URL path [/**] onto handler of type [class org.springframework.web.servlet.resource.ResourceHttpRequestHandler]
2017-02-17 09:48:52.716  INFO 85498 --- [           main] o.s.w.s.handler.SimpleUrlHandlerMapping  : Mapped URL path [/**/favicon.ico] onto handler of type [class org.springframework.web.servlet.resource.ResourceHttpRequestHandler]
2017-02-17 09:48:52.794  INFO 85498 --- [           main] o.s.f.config.FlexConfigurationManager    : Loading Flex services configuration from: class path resource [META-INF/flex/services-config.xml]
2017-02-17 09:48:53.012  INFO 85498 --- [           main] o.s.flex.core.MessageBrokerFactoryBean   : flex-messaging-core: 4.7.3-SNAPSHOT
2017-02-17 09:48:53.034  INFO 85498 --- [           main] o.s.flex.core.MessageBrokerFactoryBean   : MessageBroker with id '_messageBroker' is starting.
2017-02-17 09:48:53.043  INFO 85498 --- [           main] o.s.flex.core.MessageBrokerFactoryBean   : MessageBroker with id '_messageBroker' is ready (startup time: '9' ms)
2017-02-17 09:48:53.048  INFO 85498 --- [           main] o.s.w.s.handler.SimpleUrlHandlerMapping  : Mapped URL path [/messagebroker/*] onto handler '_messageBroker'
2017-02-17 09:48:53.115  INFO 85498 --- [           main] o.s.f.r.RemotingDestinationExporter      : Created remoting destination with id 'exampleService'
2017-02-17 09:48:53.119  INFO 85498 --- [           main] o.s.f.r.RemotingDestinationExporter      : Remoting destination 'exampleService' has been started started successfully.
2017-02-17 09:48:53.169  INFO 85498 --- [           main] o.s.j.e.a.AnnotationMBeanExporter        : Registering beans for JMX exposure on startup
2017-02-17 09:48:53.182  INFO 85498 --- [           main] application                              : Initializing Spring FrameworkServlet 'dispatcherServlet'
2017-02-17 09:48:53.182  INFO 85498 --- [           main] o.s.web.servlet.DispatcherServlet        : FrameworkServlet 'dispatcherServlet': initialization started
2017-02-17 09:48:53.192  INFO 85498 --- [           main] o.s.web.servlet.DispatcherServlet        : FrameworkServlet 'dispatcherServlet': initialization completed in 10 ms
2017-02-17 09:48:53.227  INFO 85498 --- [           main] o.e.jetty.server.AbstractConnector       : Started ServerConnector@7eea1892{HTTP/1.1,[http/1.1]}{0.0.0.0:8080}
2017-02-17 09:48:53.231  INFO 85498 --- [           main] .s.b.c.e.j.JettyEmbeddedServletContainer : Jetty started on port(s) 8080 (http/1.1)
2017-02-17 09:48:53.236  INFO 85498 --- [           main] o.a.m.BlazeDSSpringBootApplication       : Started BlazeDSSpringBootApplication in 2.593 seconds (JVM running for 5.354)
```

## Communicating with the BlazeDS server from a Flex client

In order to communicate with the BlazeDS server, you need to define your Remote object by providing the `endpoint` and `destination` attributes.
```
<fx:Declarations>
    <s:RemoteObject id="exampleService"
                    destination="exampleService"
                    endpoint="http://localhost:8080/messagebroker/short-polling-amf"
                    fault="onFault(event)">
        <s:method name="echo" result="onEchoResult(event)"/>
    </s:RemoteObject>
</fx:Declarations>
```
Take care that the `destination` attribute matches exaclty the name you gave the service using the `@Service` annotation and that the service is also annotated with `@RemotingDestination`

The `@Service` annotation gives the service a name in the Spring context and would be available under that name inside the Spring context. The `@RemotingDestination` annotation explicitly registers the service with BlazeDS. This is a security measure as this way only explicitly exported services are available remotely.