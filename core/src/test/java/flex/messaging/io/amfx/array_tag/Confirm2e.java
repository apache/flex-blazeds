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

public class Confirm2e extends DeserializationConfirmation {
    private ActionMessage EXPECTED_VALUE;

    public Confirm2e() {
    }

    public ActionMessage getExpectedMessage() {
        if (EXPECTED_VALUE == null) {
            ActionMessage m = new ActionMessage();
            MessageBody body = new MessageBody();
            m.addBody(body);

            Object list = createList(1);
            addToList(list, 0, list);

            body.setData(list);
            EXPECTED_VALUE = m;
        }

        return EXPECTED_VALUE;
    }

    public MessageException getExpectedException() {
        return null;
    }

    protected boolean bodyValuesMatch(Object o1, Object o2) {
        boolean match = super.bodyValuesMatch(o1, o2);

        // Also check that by-reference serialization of the array restored
        // the pointers to the original array...
        if (match) {
            Object list1 = getFromList(o1, 0);

            if (o1 != list1)
                return false;

            Object list2 = getFromList(o2, 0);

            if (o2 != list2)
                return false;
        }

        return match;
    }
}
