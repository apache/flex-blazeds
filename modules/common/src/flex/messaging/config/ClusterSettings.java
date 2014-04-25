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

/**
 * @exclude
 */
public class ClusterSettings extends PropertiesSettings
{
    public static final String CLUSTER_ELEMENT = "cluster";
    public static final String REF_ATTR = "ref";
    public static final String SHARED_BACKEND_ATTR = "shared-backend";
    public static final String DEFAULT_ELEMENT = "default";
    public static final String URL_LOAD_BALANCING = "url-load-balancing";
    public static final String IMPLEMENTATION_CLASS = "class";

    public static final String JGROUPS_CLUSTER = "flex.messaging.cluster.JGroupsCluster";

    private String clusterName;
    private String propsFileName;
    private String implementationClass;
    private boolean def;
    private boolean urlLoadBalancing;

    /**
     * Creates a new <code>ClusterSettings</code> with default settings.
     */
    public ClusterSettings()
    {
        def = false;
        urlLoadBalancing = true;
        implementationClass = JGROUPS_CLUSTER;
    }

    /**
     * Returns the name of the cluster.
     *
     * @return The name of the cluster.
     */
    public String getClusterName()
    {
        return clusterName;
    }

    /**
     * Sets the name of the cluster.
     *
     * @param clusterName The name of the cluster.
     */
    public void setClusterName(String clusterName)
    {
        this.clusterName = clusterName;
    }

    /**
     * Returns whether the cluster is default or not.
     *
     * @return <code>true</code> is the cluster is default; otherwise <code>false</code>.
     */
    public boolean isDefault()
    {
        return def;
    }

    /**
     * Sets whether the cluster is default or not.
     *
     * @param def <code>true</code> is the cluster is default; otherwise <code>false</code>.
     */
    public void setDefault(boolean def)
    {
        this.def = def;
    }

    /**
     * Returns the properties file of the cluster.
     *
     * @return The properties file of the cluster.
     */
    public String getPropsFileName()
    {
        return propsFileName;
    }

    /**
     * Sets the properties file of the cluster.
     *
     * @param propsFileName The properties file of the cluster.
     */
    public void setPropsFileName(String propsFileName)
    {
        this.propsFileName = propsFileName;
    }

    /**
     * Returns whether url load balancing is enabled or not.
     *
     * @return <code>true</code> if the url load balancing is enabled; otherwise <code>false</code>.
     */
    public boolean getURLLoadBalancing()
    {
        return urlLoadBalancing;
    }

    /**
     * Sets whether url load balancing is enabled or not.
     *
     * @param ulb <code>true</code> if the url load balancing is enabled; otherwise <code>false</code>.
     */
    public void setURLLoadBalancing(boolean ulb)
    {
        urlLoadBalancing = ulb;
    }

    /**
     * Sets the name of the cluster implementation class.
     * The default is 'flex.messaging.cluster.JGroupsCluster'.
     *
     * @param className
     * @exclude
     */
    public void setImplementationClass(String className)
    {
        this.implementationClass = className;
    }

    /**
     * Get the name of the cluster implementation class.
     * The class must support the flex.messaging.cluster.Cluster interface.
     *
     * @return The implementation class name.
     * @exclude
     */
    public String getImplementationClass()
    {
        return implementationClass;
    }
}
