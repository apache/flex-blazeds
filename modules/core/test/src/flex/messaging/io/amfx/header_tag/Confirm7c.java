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
package flex.messaging.io.amfx.header_tag;

import flex.messaging.io.amfx.DeserializationConfirmation;
import flex.messaging.io.amf.ActionMessage;
import flex.messaging.io.amf.MessageHeader;
import flex.messaging.io.amf.ASObject;
import flex.messaging.MessageException;

public class Confirm7c extends DeserializationConfirmation
{
    private ActionMessage EXPECTED_VALUE;

    public Confirm7c()
    {
    }

    public ActionMessage getExpectedMessage()
    {
        if (EXPECTED_VALUE == null)
        {
            ActionMessage m = new ActionMessage();

            MessageHeader header = new MessageHeader();
            header.setName("Sample Header");
            header.setData("Sample value.");
            m.addHeader(header);

            header = new MessageHeader();
            header.setName("Another Header");
            header.setMustUnderstand(false);
            ASObject aso = new ASObject();
            aso.put("prop0", "Another sample value.");
            aso.put("prop1", new Double(400.05));
            header.setData(aso);
            m.addHeader(header);

            header = new MessageHeader();
            header.setName("Yet Another Header");
            header.setMustUnderstand(true);
            Object list = createList(3);
            addToList(list, 0, new Integer(-10));
            addToList(list, 1, new Integer(0));
            addToList(list, 2, new Integer(10));
            header.setData(list);
            m.addHeader(header);

            EXPECTED_VALUE = m;
        }
        return EXPECTED_VALUE;
    }

    public MessageException getExpectedException()
    {
        return null;
    }
}
