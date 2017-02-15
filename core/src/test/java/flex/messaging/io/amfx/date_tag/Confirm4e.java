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
package flex.messaging.io.amfx.date_tag;

import flex.messaging.io.amfx.DeserializationConfirmation;
import flex.messaging.io.amf.ActionMessage;
import flex.messaging.io.amf.MessageBody;
import flex.messaging.MessageException;

import java.util.Date;

public class Confirm4e extends DeserializationConfirmation
{
    private ActionMessage EXPECTED_VALUE;

    public Confirm4e()
    {
    }

    public ActionMessage getExpectedMessage()
    {
        if (EXPECTED_VALUE == null)
        {
            ActionMessage m = new ActionMessage();
            MessageBody body = new MessageBody();
            m.addBody(body);

            Date d = new Date(1119647239994L);
            Object list = createList(3);
            addToList(list, 0, d);
            addToList(list, 1, d);
            addToList(list, 2, d);

            body.setData(list);
            EXPECTED_VALUE = m;
        }
        return EXPECTED_VALUE;
    }

    public MessageException getExpectedException()
    {
        return null;
    }

    protected boolean bodyValuesMatch(Object o1, Object o2)
    {
        boolean match = super.bodyValuesMatch(o1, o2);

        // Also check that by-reference serialization of dates restored
        // the pointers to the original date...
        Object list1 = o1;
        Object list2 = o2;

        Date d11 = (Date)getFromList(list1, 0);
        Date d12 = (Date)getFromList(list1, 1);
        Date d13 = (Date)getFromList(list1, 2);

        if (d11 != d12 || d12 != d13)
            return false;

        Date d21 = (Date)getFromList(list2, 0);
        Date d22 = (Date)getFromList(list2, 1);
        Date d23 = (Date)getFromList(list2, 2);

        if (d21 != d22 || d22 != d23)
            return false;

        return match;
    }
}
