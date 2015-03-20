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

/**
 * Converts java.util.Date, java.sql.Date, java.util.Calendar or
 * java.lang.Number (via longValue) instances to a java.util.Date,
 * taking into consideration the SQL specific java.sql.Date type
 * which is required by Hibernate users.
 *
 * If the incoming type was an AMF 3 Date, we remember the translation
 * to Calendar in our list of known objects as Dates are considered
 * complex objects and can be sent by reference. We want to retain
 * pointers to Date instances in our representation of an ActionScript
 * object graph.
 *
 * @exclude
 */
public class DateDecoder extends ActionScriptDecoder
{
    public Object decodeObject(Object shell, Object encodedObject, Class desiredClass)
    {
        java.util.Date result = null;

        if (java.sql.Date.class.isAssignableFrom(desiredClass))
        {
            if (encodedObject instanceof java.util.Date)
            {
                java.util.Date date = (java.util.Date)encodedObject;
                result = new java.sql.Date(date.getTime());
            }
            else if (encodedObject instanceof Calendar)
            {
                Calendar calendar = (Calendar)encodedObject;
                result = new java.sql.Date(calendar.getTimeInMillis());
            }
            else if (encodedObject instanceof Number)
            {
                Number number = (Number)encodedObject;
                result = new java.sql.Date(number.longValue());
            }
        }
        else if (java.sql.Timestamp.class.isAssignableFrom(desiredClass))
        {
            if (encodedObject instanceof java.util.Date)
            {
                java.util.Date date = (java.util.Date)encodedObject;
                result = new java.sql.Timestamp(date.getTime());
            }
            else if (encodedObject instanceof Calendar)
            {
                Calendar calendar = (Calendar)encodedObject;
                result = new java.sql.Timestamp(calendar.getTimeInMillis());
            }
            else if (encodedObject instanceof Number)
            {
                Number number = (Number)encodedObject;
                result = new java.sql.Timestamp(number.longValue());
            }
        }
        else if (java.sql.Time.class.isAssignableFrom(desiredClass))
        {
            if (encodedObject instanceof java.util.Date)
            {
                java.util.Date date = (java.util.Date)encodedObject;
                result = new java.sql.Time(date.getTime());
            }
            else if (encodedObject instanceof Calendar)
            {
                Calendar calendar = (Calendar)encodedObject;
                result = new java.sql.Time(calendar.getTimeInMillis());
            }
            else if (encodedObject instanceof Number)
            {
                Number number = (Number)encodedObject;
                result = new java.sql.Time(number.longValue());
            }
        }
        else if (java.util.Date.class.isAssignableFrom(desiredClass))
        {
            if (encodedObject instanceof java.util.Date)
            {
                result = (java.util.Date)encodedObject;
            }
            else if (encodedObject instanceof Calendar)
            {
                Calendar calendar = (Calendar)encodedObject;
                result = calendar.getTime();
            }
            else if (encodedObject instanceof Number)
            {
                Number number = (Number)encodedObject;
                result = new java.util.Date(number.longValue());
            }
        }

        if (result == null)
        {
            DecoderFactory.invalidType(encodedObject, desiredClass);
        }

        return result;
    }
}
