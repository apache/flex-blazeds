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
package flex.messaging.io.amfx.array_tag;

import flex.messaging.io.amfx.DeserializationConfirmation;
import flex.messaging.io.amf.ActionMessage;
import flex.messaging.io.amf.MessageBody;
import flex.messaging.MessageException;

import java.util.Map;
import java.util.HashMap;

public class Confirm2i extends DeserializationConfirmation
{
    private ActionMessage EXPECTED_VALUE;

    public Confirm2i()
    {
    }

    public ActionMessage getExpectedMessage()
    {
        if (EXPECTED_VALUE == null)
        {
            ActionMessage m = new ActionMessage();
            MessageBody body = new MessageBody();
            m.addBody(body);

            Object list = createList(2);

            Map first = new HashMap();

            Map prop0 = new HashMap();
            prop0.put("subprop0", "Quark");
            first.put("prop0", prop0);

            Object auto0 = createList(2);
            addToList(auto0, 0, Boolean.TRUE);
            addToList(auto0, 1, Boolean.FALSE);
            first.put("0", auto0);

            addToList(list, 0, first);

            Object second = createList(2);
            addToList(second, 0, null);
            addToList(second, 1, prop0);
            addToList(list, 1, second);

            body.setData(list);
            EXPECTED_VALUE = m;
        }

        return EXPECTED_VALUE;
    }

    public MessageException getExpectedException()
    {
        return null;
    }
}
