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

BlazeDS Build readme file

BlazeDS build script requires that the following products are installed.
    - ANT 1.7.0
    - ANT-CONTRIB-1.0b2
    - Sun JDK 5
    - JUnit (required in ANT_HOME/lib for "unit" target)

BlazeDS build script requires that the following environment variable are set properly.
    - JAVA_HOME
    - ANT_HOME 
    - JAVA_HOME\bin and ANT_HOME\bin must be on the path.

The build.xml at the top level of the installed BlazeDS will build the product.
The following steps will create a new version of BlazeDS and the sample applications.
    - ant clean
    - ant main
    - ant checkintests

NOTE: In order to build optional modules you need the following jars: 
      (1) For weblogic: weblogic.jar

The ant checkintests will verify if the build was successful. This step runs all unit tests developed.

