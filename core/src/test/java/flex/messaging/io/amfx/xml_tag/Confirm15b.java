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
package flex.messaging.io.amfx.xml_tag;

import flex.messaging.io.amfx.DeserializationConfirmation;
import flex.messaging.io.amf.ActionMessage;
import flex.messaging.io.amf.MessageBody;
import flex.messaging.util.XMLUtil;
import flex.messaging.MessageException;

public class Confirm15b extends DeserializationConfirmation {
    private ActionMessage EXPECTED_VALUE;

    public Confirm15b() {
        ActionMessage m = new ActionMessage();
        MessageBody body = new MessageBody();
        m.addBody(body);

        try {
            StringBuffer xml = new StringBuffer(512);
            xml.append("<stock>");
            xml.append("    <item id=\"5\">");
            xml.append("        <discontinued />");
            xml.append("    </item>");
            xml.append("</stock>");

            Object data = XMLUtil.stringToDocument(xml.toString());
            body.setData(data);
        } catch (Throwable t) {
            throw new MessageException("Error creating expected message for test 15b: " + t.getMessage());
        }

        EXPECTED_VALUE = m;
    }

    public ActionMessage getExpectedMessage() {
        return EXPECTED_VALUE;
    }

    public MessageException getExpectedException() {
        return null;
    }
}
