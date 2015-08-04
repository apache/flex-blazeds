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
package console.containers
{
    import mx.charts.LineChart;
    import mx.charts.series.LineSeries;

    public class PollableAttributeChart extends LineChart
    {
        function PollableAttributeChart(provider:Object):void
        {
            super();
            
            dataProvider = provider;
            this.percentHeight = 100;
            this.percentWidth = 100;
            
            var series:LineSeries = new LineSeries;
            series.dataProvider = provider;
            series.yField = "value";
            this.addChild(series);
            
            initialize();
        }
    }
}