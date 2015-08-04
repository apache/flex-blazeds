////////////////////////////////////////////////////////////////////////////////
//
//  Licensed to the Apache Software Foundation (ASF) under one or more
//  contributor license agreements.  See the NOTICE file distributed with
//  this work for additional information regarding copyright ownership.
//  The ASF licenses this file to You under the Apache License, Version 2.0
//  (the "License"); you may not use this file except in compliance with
//  the License.  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////
package {

import mx.utils.ObjectUtil;

public class SortUtils
{
    //lookup the index based on month abbreviation
    static public var monthMap:Object = {
      Jan: 0,
      Feb: 1,
      Mar: 2,
      Apr: 3,
      May: 4,
      Jun: 5,
      Jul: 6,
      Aug: 7,
      Sep: 8,
      Oct: 9,
      Nov: 10,
      Dec: 11
    };

    public function SortUtils()
    {
        super();
    }

     static public function sortByDates(obj1:Object, obj2:Object, prop:String):Number
     {
         var month:String = obj1[prop].substr(0,3);
         var month1:Number = monthMap[month];
         var year1:String = "20" + obj1[prop].substr(4,2);
         month = obj2[prop].substr(0,3);
         var month2:Number = monthMap[month];
         var year2:String = "20" + obj2[prop].substr(4,2);
         var date1:Date = new Date(Number(year1), month1, 01);
         var date2:Date = new Date(Number(year2), month2, 01);

         return ObjectUtil.dateCompare(date1, date2);
    }

}

}
