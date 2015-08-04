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
package flex.messaging.io.amfx.object_tag;

import flex.messaging.io.amfx.DeserializationConfirmation;
import flex.messaging.io.amf.ActionMessage;
import flex.messaging.io.amf.MessageBody;
import flex.messaging.io.amf.ASObject;
import flex.messaging.MessageException;

public class Confirm10b extends DeserializationConfirmation
{
    private ActionMessage EXPECTED_VALUE;

    public Confirm10b()
    {
        ActionMessage m = new ActionMessage();
        MessageBody body = new MessageBody();
        m.addBody(body);

        ASObject aso = new ASObject();

        ASObject prop0 = new ASObject();
        prop0.put("subprop0", "200");
        prop0.put("subprop1", new Double(200.0));
        aso.put("prop0", prop0);

        ASObject prop1 = new ASObject();
        prop1.put("subprop0", "200");
        prop1.put("subprop1", new Double(200.0));
        aso.put("prop1", prop1);

        ASObject prop2 = new ASObject();

        ASObject subprop0 = new ASObject();
        subprop0.put("subprop0", "200");
        subprop0.put("subprop1", new Double(200.0));

        ASObject subprop1 = new ASObject();
        subprop1.put("subprop0", "200");
        subprop1.put("subprop1", new Double(200.0));
        prop2.put("subprop0", subprop0);
        prop2.put("subprop1", subprop1);

        aso.put("prop2", prop2);

        body.setData(aso);
        EXPECTED_VALUE = m;
    }

    public ActionMessage getExpectedMessage()
    {
        return EXPECTED_VALUE;
    }

    public MessageException getExpectedException()
    {
        return null;
    }
}
