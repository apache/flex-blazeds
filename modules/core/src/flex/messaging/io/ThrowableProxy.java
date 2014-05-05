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
package flex.messaging.io;

/**
 * Throwable instances are treated as a special type of Bean as
 * usually properties are read only but need to be serialized.
 * 
 * @author Peter Farland
 */
public class ThrowableProxy extends BeanProxy
{
    static final long serialVersionUID = 6363249716988887262L;

    public ThrowableProxy()
    {
        super();
        setIncludeReadOnly(true);
    }
    
    public ThrowableProxy(Throwable defaultInstance)
    {
        super(defaultInstance);
        setIncludeReadOnly(true);
    }
}
