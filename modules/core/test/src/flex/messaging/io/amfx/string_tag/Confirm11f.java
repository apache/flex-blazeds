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
package flex.messaging.io.amfx.string_tag;

import flex.messaging.io.amfx.DeserializationConfirmation;
import flex.messaging.io.amf.ActionMessage;
import flex.messaging.io.amf.MessageBody;
import flex.messaging.MessageException;

public class Confirm11f extends DeserializationConfirmation
{
    private ActionMessage EXPECTED_VALUE;

    public Confirm11f()
    {
    }

    public ActionMessage getExpectedMessage()
    {
        if (EXPECTED_VALUE == null)
        {
            ActionMessage m = new ActionMessage();
            MessageBody body = new MessageBody();
            m.addBody(body);

            String s = "Locus classicus";
            Object list = createList(5);
            addToList(list, 0, "");
            addToList(list, 1, "");
            addToList(list, 2, s);
            addToList(list, 3, s);
            addToList(list, 4, s);

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

        String s11 = (String)getFromList(list1, 0);
        String s12 = (String)getFromList(list1, 1);
        String s13 = (String)getFromList(list1, 2);
        String s14 = (String)getFromList(list1, 3);
        String s15 = (String)getFromList(list1, 4);

        if (s11.length() > 0 || s12.length() > 0)
            return false;

        if (s13 != s14 || s14 != s15)
            return false;

        String s21 = (String)getFromList(list2, 0);
        String s22 = (String)getFromList(list2, 1);
        String s23 = (String)getFromList(list2, 2);
        String s24 = (String)getFromList(list2, 3);
        String s25 = (String)getFromList(list2, 4);

        if (s21.length() > 0 || s22.length() > 0)
                    return false;

        if (s23 != s24 || s24 != s25)
            return false;

        return match;
    }
}
