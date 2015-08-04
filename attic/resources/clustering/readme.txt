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

Clustering readme file

Clustering support for data services is provided by JGroups 2.9.0 GA.  The source code
for JGroups 2.9.0 GA is available in the BlazeDS SVN repository or from
http://www.jgroups.org

Copy jgroups.jar to your data services web application's WEB-INF/lib directory.
Either one or both of the properties files (jgroups-tcp.xml and
jgroups-udp.xml) in {install_root}/resources/clustering directory should be
copied over to WEB-INF/flex directory. Please refer to the JGroups
documentation for more information regarding these properties files. For most
data services deployments, you should use jgroups-tcp.xml as TCP provides for an
easier deployment in networks that do not support UDP.  The TCP protocol also
may perform better in a typical data services cluster since each server will be
sending messages to every other server.  In this situation, UDP may encounter
packet collisions that require messages to be re-sent which can reduce overall
throughput in the cluster.

For usage information, see the Using Software Clustering topic in  the developer guide.
