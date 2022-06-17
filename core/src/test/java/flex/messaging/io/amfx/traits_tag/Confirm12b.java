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
package flex.messaging.io.amfx.traits_tag;

import flex.messaging.io.amfx.DeserializationConfirmation;
import flex.messaging.io.amf.ActionMessage;
import flex.messaging.io.amf.MessageBody;
import flex.messaging.io.amf.ASObject;
import flex.messaging.MessageException;

public class Confirm12b extends DeserializationConfirmation {
    private ActionMessage EXPECTED_VALUE;

    public Confirm12b() {
    }

    public ActionMessage getExpectedMessage() {
        if (EXPECTED_VALUE == null) {
            ActionMessage m = new ActionMessage();
            MessageBody body = new MessageBody();
            m.addBody(body);

            Object list = createList(3);

            ASObject aso1 = new ASObject();
            aso1.put("prop0", Boolean.TRUE);
            aso1.put("prop1", Boolean.FALSE);
            aso1.put("prop2", Boolean.FALSE);
            addToList(list, 0, aso1);

            ASObject aso2 = new ASObject();
            aso2.put("prop0", Boolean.FALSE);
            aso2.put("prop1", Boolean.FALSE);
            aso2.put("prop2", Boolean.FALSE);
            addToList(list, 1, aso2);

            ASObject aso3 = new ASObject();
            aso3.put("prop0", Boolean.TRUE);
            aso3.put("prop1", Boolean.TRUE);
            aso3.put("prop2", Boolean.FALSE);
            addToList(list, 2, aso3);

            body.setData(list);
            EXPECTED_VALUE = m;
        }
        return EXPECTED_VALUE;
    }

    public MessageException getExpectedException() {
        return null;
    }
}
