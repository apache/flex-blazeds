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
import flex.messaging.io.amf.ASObject;
import flex.messaging.MessageException;

public class Confirm2b extends DeserializationConfirmation {
    private ActionMessage EXPECTED_VALUE;

    public Confirm2b() {
    }

    public ActionMessage getExpectedMessage() {
        if (EXPECTED_VALUE == null) {
            ActionMessage m = new ActionMessage();
            MessageBody body = new MessageBody();
            m.addBody(body);

            ASObject aso = new ASObject();

            Object list1 = createList(2);
            addToList(list1, 0, new Integer(200));
            addToList(list1, 1, new Integer(400));

            Object list2 = createList(4);
            addToList(list2, 0, new Double(200.5));
            addToList(list2, 1, new Double(400.5));
            addToList(list2, 2, new Double(600.5));
            addToList(list2, 3, new Double(800.5));

            aso.put("prop0", list1);
            aso.put("prop1", list2);

            body.setData(aso);
            EXPECTED_VALUE = m;
        }

        return EXPECTED_VALUE;
    }

    public MessageException getExpectedException() {
        return null;
    }
}
