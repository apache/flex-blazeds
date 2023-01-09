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
package flex.messaging.messages;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectOutput;

import flex.messaging.io.ClassAlias;

/**
 *
 */
public class CommandMessageExt extends CommandMessage implements Externalizable, ClassAlias {
    private static final long serialVersionUID = -5371460213241777011L;
    public static final String CLASS_ALIAS = "DSC";

    public CommandMessageExt() {
        super();
    }

    public CommandMessageExt(CommandMessage message) {
        super();
        _message = message;
    }

    public String getAlias() {
        return CLASS_ALIAS;
    }

    public void writeExternal(ObjectOutput output) throws IOException {
        if (_message != null)
            _message.writeExternal(output);
        else
            super.writeExternal(output);
    }

    private CommandMessage _message;
}
