You need to perform the following configuration steps to use custom authentication with BlazeDS on JBoss:

JBoss EAP 4.3 

    1. Copy flex-tomcat-common.jar and flex-tomcat-server.jar from {blazeds_install}/resources/security/tomcat folder to {jboss_root}/server/default/lib folder.
    2. Add the TomcatValve to the context.xml under \server\default\deploy\jboss-web.deployer 
    
    <Valve className="flex.messaging.security.TomcatValve"/> 

    3. Restart JBoss. 

JBoss EAP 5

   1. Copy flex-tomcat-common.jar and flex-tomcat-server.jar from {blazeds_install}/resources/security/tomcat folder to {jboss_root}/server/default/lib folder.
   2. Add the TomcatValve to the context.xml under \server\default\deploy\jbossweb.sar

    <Valve className="flex.messaging.security.TomcatValve"/> 

   3. Restart JBoss. 

You will now be able to authenticate against the current JBoss Realm. 

See your JBoss documentation for Realm configuration information, including how to add users and roles to the Realm.