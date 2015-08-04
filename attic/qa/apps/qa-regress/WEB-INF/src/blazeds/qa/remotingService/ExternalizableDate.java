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
package blazeds.qa.remotingService;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Date;
import java.util.Map;
import java.util.Calendar;

public class ExternalizableDate extends Date implements Externalizable
{
    private static final long serialVersionUID = 8037877279661457358L;
    

    public void readExternal(ObjectInput input) throws IOException,
            ClassNotFoundException
    {
        Object source = input.readObject();
        if (source instanceof Map)
        {
            Map map = (Map) source;
            int year = Integer.valueOf((String) map.get("year"));
            int month = Integer.valueOf((String) map.get("month"));
            int date = Integer.valueOf((String) map.get("date"));
            Calendar calendar = Calendar.getInstance();
            calendar.clear();
            calendar.set( year, month, date );            
            setTime( calendar.getTimeInMillis() );
        }
        else
        {
            throw new IOException("Cannot find expected value type");
        }
    }

    public void writeExternal(ObjectOutput output) throws IOException
    {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setTime(this);
        Map map = new HashMap();
        map.put("year", new Integer(calendar.get(Calendar.YEAR)));
        map.put("month", new Integer(calendar.get(Calendar.MONTH)));
        map.put("date", new Integer(calendar.get(Calendar.DATE)));
        output.writeObject(map);
    }

}
