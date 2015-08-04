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

package dev.calendar;

public class CalendarService 
{

    public String getEvents(String idName)
    {
        String events = "No events scheduled.";
                
        int id = Double.valueOf(idName).intValue();            
        switch (id)
        {           
            case 1:
            case 12:
            case 25:
            {                
                events = "Swimming 7:00 pm";
                break;
            }
            case 2:
            case 15:
            case 26:
            {
                events = "Lunch with Friend 12:00 pm";
                break;
            }
            case 3:
            case 18:
            case 29:
            case 30:
            {
                events = "Cooking class 7:00 pm";
                break;
            }
            case 4:
            case 5:
            {
                events = "Vacation";
                break;
            }
            case 6:
            case 19:
            case 28:
            {
                events = "Skiing";
                break;
            }
            case 7:
            case 14:
            case 23:
            {
                events = "Class 7-9 pm";
                break;
            }
            case 9:
            {
                events = "Holiday Party 8:00 pm";
                break;
            }
            case 10:
            case 16:
            case 22:
            {
                events = "Help friend move";
                break;
            }
        }
        
        return events;
    }
}