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
package flex.messaging.io.amf.translator.decoder;

import java.util.Calendar;
import java.util.Date;

/**
 * Converts instances of java.util.Date, java.util.Calendar, and
 * java.lang.Number to instances of java.util.Calendar. If the incoming
 * object was not a Calendar, we create a new Calendar instance using the
 * default timezone and locale.
 *
 * If the incoming type was an AMF 3 Date we remember the translation
 * to Calendar in our list of known objects as Dates are considered
 * complex objects and can be sent by reference. We want to retain
 * pointers to Date instances in our representation of an ActionScript
 * object graph.
 *
 * @author Peter Farland
 *
 * @exclude
 */
public class CalendarDecoder extends ActionScriptDecoder
{
    public Object decodeObject(Object shell, Object encodedObject, Class desiredClass)
    {
        Object result = null;

        if (encodedObject instanceof Date)
        {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime((Date)encodedObject);
            result = calendar;
        }
        else if (encodedObject instanceof Calendar)
        {
            result = encodedObject;
        }
        else if (encodedObject instanceof Number)
        {
            Calendar calendar = Calendar.getInstance();
            Number number = (Number)encodedObject;
            calendar.setTimeInMillis(number.longValue());
            result = calendar;
        }
        else
        {
            DecoderFactory.invalidType(encodedObject, desiredClass);
        }


        return result;
    }
}
