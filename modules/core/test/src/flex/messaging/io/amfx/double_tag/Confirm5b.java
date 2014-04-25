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
package flex.messaging.io.amfx.double_tag;

import flex.messaging.io.amfx.DeserializationConfirmation;
import flex.messaging.io.amf.ActionMessage;
import flex.messaging.io.amf.MessageBody;
import flex.messaging.io.amf.ASObject;
import flex.messaging.MessageException;

public class Confirm5b extends DeserializationConfirmation
{
    private ActionMessage EXPECTED_VALUE;

    public Confirm5b()
    {
        ActionMessage m = new ActionMessage();
        MessageBody body = new MessageBody();
        m.addBody(body);

        ASObject aso = new ASObject();
        aso.put("prop0", new Double(-Double.MAX_VALUE)); // -1.7976931348623157e+308
        aso.put("prop1", new Double(-Double.MIN_VALUE)); // -4.9e-324
        aso.put("prop2", new Double(0));
        aso.put("prop3", new Double(0.0));
        aso.put("prop4", new Double(Double.MIN_VALUE)); // 4.9e-324
        aso.put("prop5", new Double(Double.MAX_VALUE)); // 1.7976931348623157e+308

        aso.put("prop6", new Double(Double.NaN));
        aso.put("prop7", new Double(Double.POSITIVE_INFINITY));
        aso.put("prop8", new Double(Double.NEGATIVE_INFINITY));
        aso.put("prop9", new Double(Double.POSITIVE_INFINITY));
        aso.put("prop10", new Double(Double.NEGATIVE_INFINITY));

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
