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
    import console.ConsoleManager;
    import mx.containers.Panel;
    import flash.events.Event;
    
    public class DefaultPanel extends UpdateListener
    {
        private var display:DefaultPanelDisplay;
        
        public function DefaultPanel():void
        {
            super();
            display = new DefaultPanelDisplay;
            this.addChild(display);
            label = "Console";
            
            // No data is ever used, but to ensure compatibility with the rest of
            // the app this panel still registers with the ConsoleManager
            ConsoleManager.getInstance().registerListener(this, []);
        }                
    }
}