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
 * Stores configuration settings for a shared server instance.
 * <tt>ServerConfigurationParser</tt> will generate an instance for each shared server
 * defined in the configuration file.
 * The <tt>MessagingConfiguration</tt> instance using the parser will store these and
 * use them to configure the <tt>MessageBroker</tt> with shared server instances.
 *
 *
 */
public class SharedServerSettings extends PropertiesSettings
{
    private String id;

    public String getId()
    {
        return id;
    }

    public void setId(String value)
    {
        id = value;
    }

    private String className;

    public String getClassName()
    {
        return className;
    }

    public void setClassName(String value)
    {
        className = value;
    }
    
    private String sourceFile;
    
    public String getSourceFile()
    {
        return sourceFile;
    }
    
    public void setSourceFile(String value)
    {
        sourceFile = value;
    }
}
