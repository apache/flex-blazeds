<!--

Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

-->
For this test you need to setup a custom login-command.
<login-command class="security.PerClientCommand" server="Tomcat" per-client-authentication="false"/>

The sources for the java classes being used are located at 
//depot/flex/server-ce/qa/apps/qa-manual/WEB-INF/src/security/*.java

Make sure using the Firefox browser for the following test since IE doesn't share session across browsers. 

This part of the test shows what will (should) happen when the per-client-authentication attribute is set to "false".
Reproduction:
1) Load application in a Firefox browse (#1) - http://localhost:8400/qa-manual/bugs/182101/PerClientAuthenticationTest.mxml
2) Click "Login" button and set the credentials.
3) Click on the "echoBooleans()" button which calls the RO's method. You will see 'Passed' in log text area
4) Open up a new Firefox browser (#2) and browes to the same app
5) Click on the "echoBooleans()" button which calls the RO's method without setting any credential. You will see 'Passed' in log text area
6) Switch back to browser #1 and click on the logout button.
7) Switch back to browse #2 and click on the "echoBooleans()" button which calls the RO's method.  You will see 'Falied' in the log text area.


This part of the test shows what will (should) happen when the per-client-authentication attribute is set to "true".
Reproduction:
1) Load application in a Firefox browse (#1) - http://localhost:8400/qa-manual/bugs/182101/PerClientAuthenticationTest.mxml
2) lick "Login" button and set the credentials.
3) Click on the "echoBooleans()" button which calls the RO's method. You will see 'Passed' in log text area
4) Open up a new Firefox browser (#2) and browes to the same app
5) Click on the "echoBooleans()" button, without setting any credential,which calls the RO's method. You will see 'Failed' in log text area

