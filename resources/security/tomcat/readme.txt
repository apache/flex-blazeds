You must perform the following configuration steps to use custom authentication with BlazeDS on Tomcat:

1. Put flex-tomcat-common.jar in tomcat/lib/blazeds.
2. Place flex-tomcat-server.jar in tomcat/lib/blazeds. (For Tomcat 7, the file name is flex-tomcat-server-708.jar)
3. Edit the catalina.properties file in the tomcat/conf directory. Find the common.loader property and add the following path to the end of the list: ${catalina.home}/lib/blazeds/*.jar
4. Add <Valve className="flex.messaging.security.TomcatValve"/> tag to the Context Descriptors. (For Tomcat 7, the class name is "flex.messaging.security.Tomcat7Valve")
5. Restart Tomcat.

You will now be authenticated against the current Tomcat realm. For this authentication, the user information is usually in the conf/tomcat-users.xml file. See the Tomcat documentation for more information on realms. See the BlazeDS documentation for more information on custom authentication.
