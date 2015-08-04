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
package console
{
    import console.ConsoleTreeDataDescriptor;
    import console.events.ManagementOperationInvokeEvent;
    import flash.events.*;
    import mx.collections.*;
    import mx.containers.*;
    import mx.controls.*;
    import mx.controls.treeClasses.*;
    import mx.events.*;
    import mx.managers.*;
    import mx.messaging.messages.*;
    import mx.messaging.management.*;
    import mx.rpc.events.*;
    import mx.rpc.remoting.*;
    import mx.utils.*;
    import mx.rpc.remoting.RemoteObject;
    import flash.display.DisplayObject;
    import console.containers.UpdateManager;
    import console.containers.UpdateListener;
    import flash.utils.Timer;
    import mx.core.LayoutContainer;
        
    public class ConsoleManager implements console.containers.UpdateManager
    {
        public static const GENERAL_SERVER:int = 1;
        public static const GENERAL_POLLABLE:int = 2;
        public static const GENERAL_OPERATION:int = 3;
    
        public static const GRAPH_BY_POLL_INTERVAL:int = 50;
    
        public static const ENDPOINT_SCALAR:int = 100;
        public static const ENDPOINT_POLLABLE:int = 101;
    
        public static const DESTINATION_GENERAL:int = 150;
        public static const DESTINATION_POLLABLE:int = 151;

        public static const TIMER_INTERVAL:int = 3000;

        ///////////////////////////
        // Private data structures
        ///////////////////////////
        /**
         * A generic object model of the MBean hierarchy, built from the flat set of object names.
         * The model is an array of top level objects. Each object has the following slots:
         * <ul>
         *   <li>label:String - required - the label for the MBean instance or category name/app name</li>
         *   <li>type:String - optional - the type value for a node that parents MBeans of similar type</li>
         *   <li>objectName:ObjectName - optional - the ObjectName instance for an MBean</li>
         *   <li>children:Array - optional - array of child objects</li>
         * </ul>
         */
        private var _mbeanModel:Array;
        public function get mbeanModel():Object
        {
            return _mbeanModel;
        }
        
        /**
        * The listeners map is keyed on the listeners and stores a property called types
        * which is an array of all of the types that the listener uses and whether or not
        * an individual type should poll.  Not all listeners are always updated however,
        * there is another map 'activeListeners' which keeps track of which listeners are actively
        * listening to the timer.  This is used because if one panel is hidden,
        * we do not want to poll all of the data it listens for if it won't be displayed yet.
        * 
        * An example listeners map then looks like this:
        * 
        * listeners:Object -+-[listener1:String]-:- { reference: listenerObject,
        *                   |                         types: [[TYPE1, POLL], 
        *                   |                                 [TYPE2, NOPOLL]]
        *                   +-[listener2:String]-:- { ... }
        */
        private var listeners:Object;
        
        
        /**
        * The activeListeners object stores all of the active listeners 
        * for polling and is keyed on the type of data.  An example is:
        * 
        * activeListeners:Object -+-[ TYPE1:int ]-:- [ l1:UpdateListener , ... ]
        *                         |
        *                         + [ TYPE2:int ]-:- [ ... , ... , ... ]
        */
        private var activeListeners:Object;
        
        /**
        * This map handles storing information about what mbeans are associated 
        * with what display types.  It is keyed on the types of data (e.g. GENERAL_SCALAR, etc.),
        * then on the application name stored as a string, then the MBean name.
        * 
        * An example displays map then looks like this:
        * 
        * displays:Object  -+-[TYPE1:int]-:[Application1]:- [mbean1] -> [attr1, attr2, attr3]
        *                   |             |                 [mbean2] -> [attr1, attr2]
        *                   |             |                 [mbean3] -> [...]
        *                   |             +[Application2]:- [mbean...]
        *                   |             |
        *                   |
        *                   +-[TYPE2:int]-:- [ ... ]
        */
        private var displays:Object;
        
        /**
        * Temporarily holds onto data pulled from the server.  Anything in the buffer is
        * always overridden by new data.
        * 
        * Is a map keyed on the data types which all reference maps keyed on mbeans which
        * store arrays of associated attributes.
        * 
        * E.g.
        * 
        * dataBuffer:Object -+-[TYPE1:int]-+--[mbeanName: bean1] -:- [ attr1:String , attr2:String ] 
        *                    |             +--[mbeanName: bean2] -:- [ attr1:String , attr2:String ] 
        *                    |
        *                    +-[TYPE2:int]----[mbeanName: bean3] ...
        */
        private var dataBuffer:Object;
        
        // is this necessary!? can any reference to types[] be replaced with the keyset of displays?
        private var types:Array;
        
        
        ///////////////////////////
        // Private variables
        ///////////////////////////
        
        private var minfo:MBeanInfo;
        private var isInitialized:Boolean = false;
        
        /** 
        * Map of ints, keeps track of how many mbeans of each type
        * we've received from the server when processing getting
        * data for any given type.
        */
        private var callbackCounters:Object;


        private var _displayTypesObject:Object;
         
        /**
         * Dictionary of callback handlers for remote call results
         * keyed by message correlationId.
         */
        private var callbackMap:Object;
        protected var callbacks:Object;

        // Keeps track whether a mbeanServer related fault is already handled .
        private var faultAlreadyHandled:Boolean = false;
        
        private var updateTimer:Object;

        private var hiddenDomains:Array = [ "flex.runtime.Console" ];

        ///////////////////////////
        // Public console instance vars
        ///////////////////////////
        
        private var _currentApp:String;
        public function set currentApp(name:String):void
        {
            _currentApp = name;
           
            for (var listener:* in listeners)
            {
                // Todo: Update listeners so that they can clear their data buffers.
                (listeners[listener].reference as UpdateListener).appChanged(name);
            }
        }
        public function get currentApp():String
        {
            if (_currentApp == null)
            {
                if ((mbeanModel[0] != null) && (mbeanModel[0].label != null))
                    _currentApp = mbeanModel[0].label
                else
                    return null
                    
            }
            return _currentApp;
        }

        /**
         * Handle to the MBean Server remote object.
         */
        private var _mbeanServer:RemoteObject;
        public function get mbeanServer():RemoteObject
        {
            return _mbeanServer;
        }

        /**
         * The MBeanName for the currently selected MBean.
         */
        private var _currentMBeanName:String;  
        public function get currentMBeanName():String
        {
            return _currentMBeanName;
        }
        public function set currentMBeanName(s:String):void
        {
            _currentMBeanName = s;
        }

        /**
         * The MBeanInfo for the currently selected MBean.
         */
        private var _currentMBeanInfo:MBeanInfo;    
        public function get currentMBeanInfo():MBeanInfo
        {
            return _currentMBeanInfo;
        }
        public function set currentMBeanInfo(i:MBeanInfo):void
        {
            _currentMBeanInfo = i;
        }
        
        protected var _parent:DisplayObject
        public function set parent(p:DisplayObject):void
        {
            _parent = p;
        }
        
        public function set timerInterval(interval:int):void
        {            
            if (interval > 0)
            {
                updateTimer.timer.stop();
                updateTimer.timer = new Timer(interval, 0);
                updateTimer.timer.addEventListener(TimerEvent.TIMER, pollServer);
                updateTimer.timer.start();
            }            
        }
        
        protected static var privateCall:Boolean;
        protected static var instance:ConsoleManager;


        ////////////////////////////
        // Console methods
        ////////////////////////////
        
        public static function getInstance():ConsoleManager 
        {
            if (instance == null) 
            {
                privateCall = true;
                ConsoleManager.instance = new ConsoleManager();
                privateCall = false;
            }
            
            return ConsoleManager.instance;
        }

        /**
         * Initializes the console, and requests the names of
         * registered Flex MBeans.
         */
        public function ConsoleManager():void
        {
            if (!privateCall)
                throw new Error ("This is a Singleton Class!");
                
            // Create data structures used:
            _mbeanModel = [];
            callbacks = {};
            displays = {};
            listeners = {};
            dataBuffer = {};
            activeListeners = {}; 
            _displayTypesObject = {};         
            callbackCounters = {};
            callbackCounters.dataFetch = {};
            callbackCounters.typeCount = 0;
            callbackCounters.appCount = 0;
            
            _mbeanServer = new RemoteObject("RuntimeManagement");
            _mbeanServer.addEventListener("result", handleResult);
            _mbeanServer.addEventListener("fault", handleFault);
            
            var token:Object = _mbeanServer.getFlexMBeanObjectNames();
            callbacks[token.message.messageId] = handleMBeanObjectNames;
        }
        
        
        public function setAttribute(mbean:String, attribute:String, newValue:Object):void
        {
            var attr:Attribute = new Attribute;
            attr.name = attribute;
            attr.value = newValue;
            mx.controls.Alert.show(attr.toString());
            _mbeanServer.setAttribute(mbean, attr);
        }
        
        /**
        * Register a display panel that implements the UpdateListener interface, passing
        * in an array, 'regtypes' which contains a list of every display type and
        * whether or not the type should be polled.
        */
        public function registerListener(listener:UpdateListener, regtypes:Array):void
        {
            listeners[listener.name] = {reference: listener, types: regtypes};
        }
        
        public function unregisterListener(listener:UpdateListener):void
        {
            if (listeners[listener.name])
            {
                deactivateListener(listener);
                delete listeners[listener.name];
            }         
        }
        
        /**
        * When a listener is activated using this method, the types are found in the listeners
        * map and for each type:
        * 
        * If polling is enabled, we add the listener to the activeListeners map for the types
        * it listens to.
        * 
        * If polling is not enabled, we just poll the server once.
        */
        public function activateListener(listener:UpdateListener):void
        {
            var typesForListener:Array;
            try
            {
                typesForListener = listeners[listener.name].types;
            }
            catch (error:Error)
            {
                // nothing to do for this listener!
            }

            if (typesForListener == null)
                return;

            for (var i:int = 0; i < typesForListener.length; i++)
            {
                var type:int = typesForListener[i].type;
                getDataForType(type, listener);
                
                if (typesForListener[i].poll)
                {
                    if (!activeListeners[type])
                    {
                        updateTimer.activeTypesCounter++;
                        activeListeners[type] = new ArrayCollection;
                    }
                    
                    (activeListeners[type] as ArrayCollection).addItem(listener);
                    
                    if (!updateTimer.timer.running)
                        updateTimer.timer.start();
                }
            }
        }
        
        public function deactivateListener(listener:UpdateListener):void
        {
            // can't deactivate a non-registered listener!
            if (!listeners[listener.name])
                return;

            var typesForListener:Array = listeners[listener.name].types;
            if (typesForListener == null)
                return;

            for (var i:int = 0; i < typesForListener.length; i++)
            {
                var type:int = typesForListener[i].type;
                if (typesForListener[i].poll)
                {
                    if (activeListeners[type])
                    {
                        var indexOfListener:int = (activeListeners[type] as ArrayCollection).getItemIndex(listener);
                        if (indexOfListener != -1)
                            (activeListeners[type] as ArrayCollection).removeItemAt(indexOfListener);
                        
                        if ((activeListeners[type] as ArrayCollection).length == 0)
                        {
                            delete activeListeners[type];
                            
                            if (--updateTimer.activeTypesCounter == 0)
                                updateTimer.timer.stop();
                        }
                    }
                }
            }
        }
        
        public function addCallback(id:String, o:Object, f:Function):void
        {
            callbacks[id] = {object:o, callbackFunction:f};
        }
        
         
         public function getMBeanNamesForType(type:int):Array
         {
             if ((displays[type] == null) || (displays[type][_currentApp] == null))
                return new Array;
                
             return (displays[type][_currentApp] as Array);
         }

        
        public function getMBeanInfo(name:String, obj:Object, callback:Function):void
        {
            try
            {
                var token:Object = this.mbeanServer.getMBeanInfo(name);
                this.addCallback(token.message.messageId,this,callback);
            }
            catch (e:Error)
            {
                // Ignore runtime error - we get a Fault back from the RPC operation.
            }
        }

        /**
         * Invoke an operation on an MBean.
         */
        public function invokeOperation(e:ManagementOperationInvokeEvent, callback:Function = null):void
        {
            if (callback == null)
            {
                callback = handleInvoke
            }
            
            try
            {
                var token:Object = _mbeanServer.invoke(e.mbean, e.name, e.values, e.signature);
                callbacks[token.message.messageId] = callback;
            }
            catch (e:Error)
            {
                // Ignore runtime error - we get a Fault back from the RPC operation.
            }
        }
        
        /**
         * Request the current attribute values for an MBean.
         */
        public function getAllAttributes(name:String, info:MBeanInfo, obj:Object, callback:Function):void
        {

            // Fetch attribute values.
            var attribNames:Array = [];
            var n:int = info.attributes.length;
            for (var i:int = 0; i < n; ++i)
            {
                var attribInfo:MBeanAttributeInfo = info.attributes[i];
                // Ignore attributes that are MBean ObjectNames as all MBeans
                // are rendered in the global tree and don't need to be repeated
                // in the attributes this. Also ignore non-readable attributes.
                if (attribInfo.readable && (attribInfo.type.indexOf("ObjectName") == -1))
                {
                    attribNames.push(attribInfo.name);
                }
            }
            if (attribNames.length)
            {
                getAttributes(name, attribNames, obj, callback);
            }

        }
        
        public function getAttributes(name:String, attribs:Array, obj:Object, callback:Function):void
        {
            try
            {
                var token:Object = _mbeanServer.getAttributes(name, attribs);
                this.addCallback(token.message.messageId, obj, callback);
            }
            catch (e:Error)
            {
                // Ignore runtime error - we get a Fault back from the RPC operation.
            }
        }

        
        public function getChildTrees(...children):Array
        {
            var ret:Array = new Array;
            
            for (var i:int = 0; i < children.length; i++)
            {
                if (children[i] is String)
                {
                    (getChildTree(children[i]) as Array)
                        .forEach(function(it:*, i:int, a:Array):void { ret.push(it); });
                }
            }
            return ret;
        }
        
        public function getChildTree(childName:String):Array
        {
            var appTree:Array;
            for each (var app:Object in _mbeanModel)
            {
                if (app.label == currentApp)
                {
                    // The only child of an application is the MessageBroker _class_ which could
                    // have multiple messagebrokerX _instances_ beneath it, but the
                    // first is selected. (Thus the multiple [0].children references.)
                    try 
                    {
                        appTree = ((app.children as Array)[0].children as Array)[0].children as Array
                    }
                    catch (e:Error)
                    {
                        return new Array;
                    }
                }
            }
            
            for each (var item:Object in appTree)
            {
                if (item.label == childName)
                    return item.children as Array;
            }
            
            return new Array;
        }
        
        public function getTypesForAttribute(mbean:String, attr:String):Array
        {
            var types:Array = [];
            for (var type:* in displays)
            {
                if (displays[type][_currentApp] != null)
                {
                    for (var b:String in displays[type][_currentApp])
                    {
                        if (mbean == b)
                        {
                            if ((displays[type][_currentApp][mbean] as Array).indexOf(attr) > -1)
                                    types.push(type);
                        }
                    }
                }
            }
            
            return types;
        }
        
        public function checkTypeForAttribute(type:int, mbean:String, attr:String):Boolean
        {
            if ((displays[type as String]) && (displays[type][_currentApp]) && (displays[type][_currentApp][mbean]))
                    if ((displays[type][_currentApp][mbean] as Array).indexOf(attr) > -1)
                        return true;
                        
            return false;
        }
        
        public function updateData(listener:UpdateListener):void
        {
            var listenerObject:Object = listeners[listener.name];
            if (!listenerObject)
                return;
                
            for (var i:int = 0; i < (listenerObject.types as Array).length; i++)
                getDataForType((listenerObject.types as Array)[i].type, listener);
        }

        private function getDataForType(type:int, singleListener:UpdateListener = null):void
        {
            if (!isInitialized)
            {
                // if we don't have the display data yet, we'll re-query the server and get the mbean object names
                var objectnamesToken:Object = _mbeanServer.getFlexMBeanObjectNames();
                callbacks[objectnamesToken.message.messageId] = handleMBeanObjectNames;

                return;
            }
            
                        
            if ((displays[type] == null) || (displays[type][_currentApp] == null))
                return
            
            // Dictionary of arrays keyed on mbeanName:the name of the 
            // mbean, and the associated array is the attributes to query
            var mbeans:Object = new Object;
            var queryLength:int = 0;

            for (var mbean:String in displays[type][_currentApp])
                ++queryLength;
            
            for (var mbeanName:String in displays[type][_currentApp])
            {
                var token:Object = mbeanServer.getAttributes(currentApp+":"+mbeanName, 
                    (displays[type][_currentApp][mbeanName] as ArrayCollection).toArray());
                    
                token.type = type;
                token.mbeanName = mbeanName;
                token.queryLength = queryLength;
                token.singleListener = singleListener;
                callbacks[token.message.messageId] = handleDataForType;
            }
        }
        
        private function handleDataForType(e:ResultEvent):void
        {
            var type:int = e.token.type as int;
            var mbeanName:String = e.token.mbeanName as String;
            var singleListener:UpdateListener = e.token.singleListener;
            
            if (!dataBuffer.hasOwnProperty(type))
                dataBuffer[type] = new Object;
            
            // Put the data for the mbean for the specific type of data in the proper place in the array
            if (dataBuffer[type].hasOwnProperty(mbeanName))
                delete dataBuffer[type][mbeanName];
                
            dataBuffer[type][mbeanName] = e.result as Array;
            
            if (!callbackCounters.dataFetch.hasOwnProperty(type)) callbackCounters.dataFetch[type] = 0;
            
            if (++callbackCounters.dataFetch[type] == e.token.queryLength)
            {
                callbackCounters.dataFetch[type] = 0;
                
                if (!singleListener)
                {
                    var listenersToUpdate:ArrayCollection = activeListeners[type] as ArrayCollection;
                    
                    if (!listenersToUpdate)
                        return;
                        
                    for (var i:int = 0; i < listenersToUpdate.length; i++)
                    {
                        (listenersToUpdate[i] as UpdateListener).dataUpdate(type, dataBuffer[type]);
                    }
                }
                else
                {
                    singleListener.dataUpdate(type,  dataBuffer[type]);
                }
            }
        }
      
        /**
         * Handle any results from the MBean Server remote object by
         * delegating to the associated callback based on correlationId.
         */
        protected function handleResult(e:ResultEvent):void
        {
            var msg:AsyncMessage = AsyncMessage(e.message);
            if (msg.correlationId in callbacks)
            {
                var callback:Object = callbacks[msg.correlationId];
                delete callbacks[msg.correlationId];
                if (callback is Function)
                {
                    callback(e);
                }
                else
                {
                    if ((callback.hasOwnProperty("object")) && (callback.hasOwnProperty("callbackFunction")))
                    {
                        var f:Function = callback.callbackFunction;
                        f.call(callback.object, e);
                    }
                }
            }
        }

        /**
         * Handle any faults from the MBean Server remote object by
         * displaying an alert to the user.
         */
        protected function handleFault(e:FaultEvent):void
        {
            if (!faultAlreadyHandled)
            {
                mx.controls.Alert.show("Please restart the application since polling has stopped due to the following error: " + e.toString());
                if (updateTimer != null)
                {
                    updateTimer.activeTypesCounter = 0;
                    updateTimer.timer.stop();            
                }
                faultAlreadyHandled = true;
            }
        }

        /**
         * Handles the result of a getFlexMBeanObjectNames() call.
         */
        protected function handleMBeanObjectNames(e:ResultEvent):void
        {
            var i:int;
            // Build the model for our MBeans.
            var mbeanNames:Array = e.result as Array;
            var domainNames:Array = [];
            var domainMBeanNames:Object = {};
            var mbeanName:ObjectName;
            var n:int = mbeanNames.length;
            if (n)
            {
                for (i = 0; i < n; ++i)
                {
                    mbeanName = mbeanNames[i];
                    if (hiddenDomains.indexOf(mbeanName.domain) >= 0)
                    {

                    }
                    else if (mbeanName.domain in domainMBeanNames)
                    {
                        domainMBeanNames[mbeanName.domain].push(mbeanName);
                    }
                    else
                    {
                        domainNames.push(mbeanName.domain);
                        domainMBeanNames[mbeanName.domain] = [mbeanName];
                    }
                }
                _mbeanModel = [];
                domainNames.sort();
                callbackCounters.appCount = domainNames.length;
                var n2:int = domainNames.length;
                for (i = 0; i < n2; ++i)
                {
                    buildDomain(domainNames[i], domainMBeanNames[domainNames[i]]);
                    
                    if (_displayTypesObject[domainNames[i]] != null)      
                    {
                        var token:Object = _mbeanServer.getAttributes(
                            _displayTypesObject[domainNames[i]].canonicalName, ["SupportedTypes"]);
                        
                        token.beanName = _displayTypesObject[domainNames[i]].canonicalName;
                        addCallback(token.message.messageId, this, handleSupportedTypesMBean);
                    }
                }
                
            }
        }
        
        /**
        * Process the display types that the server has registered.  Another function
        * handles querying the MBean server for all of the actual attributes supported
        * by the given display type.
        */
         private function handleSupportedTypesMBean(e:ResultEvent):void
         {
             if ((e.result as Array)[0].name != "SupportedTypes")
             {
                 return;
             }
             
             types = (e.result as Array)[0].value;
             callbackCounters.typeCount = types.length;
             for (var i:int = 0; i < types.length; i++)
             {
                 var invokeEvent:ManagementOperationInvokeEvent = new ManagementOperationInvokeEvent(
                    e.token.beanName, 
                    "listForType", 
                    [types[i]], 
                    ["int"]);
                    
                 invokeOperation(invokeEvent, getTypesHandler);
             }
         }
         
        /**
        * Handles the attribute _names_ for a given type which the server is sending
        */
         private function getTypesHandler(e:ResultEvent):void
         {
             --callbackCounters.typeCount;
             
             // Get the defined TYPE from the token
             var type:int = e.token.message.body[2][0];

             for each (var str:String in (e.result as Array))
             {
                 var mbeanAttr:Array = str.split(":");
                    
                 if (!displays.hasOwnProperty(type))
                    displays[type] = new Object;
                    
                 if (!displays[type].hasOwnProperty(mbeanAttr[0]))
                    displays[type][mbeanAttr[0]] = new Object;
                    
                 var exposedObjectInfo:Object = new Object;

                 if (!displays[type][mbeanAttr[0]].hasOwnProperty(mbeanAttr[1]))
                    displays[type][mbeanAttr[0]][mbeanAttr[1]] = new ArrayCollection();
                     
                 (displays[type][mbeanAttr[0]][mbeanAttr[1]] as ArrayCollection).addItem(mbeanAttr[2]);
             }
             
             if (callbackCounters.typeCount == 0)
             {
                 initialize();
             }
         }

        /**
        * Function for handling what to do after all of the necessary mbea 
        * data has been polled for the first time.
        */
        private function initialize():void
        {
             // now we can poll for data based on a given display type
            isInitialized = true;
             
            // We've received and processed all of the display type information
            // so we can notify the listeners
            for each (var listener:Object in listeners)
            {
                (listener.reference as UpdateListener).mbeanModelUpdate(mbeanModel);
            }
            
            updateTimer = new Object;
            
            updateTimer.timer = new Timer(TIMER_INTERVAL);
            updateTimer.activeTypesCounter = 0;
            updateTimer.timer.addEventListener(TimerEvent.TIMER, pollServer);
        }
               
        private function pollServer(e:Event):void
        {            
            for (var type:* in activeListeners)
            {
                getDataForType(type);
            }
        }
        
        /**
         * Builds a top level domain node containing all MBeans
         * within the domain.
         */
        private function buildDomain(domain:String, objectNames:Array):void
        {
            var domainNode:Object = {label: domain, children: []};
            // Process all of the MBean names in this domain.
            // Put them in sorted order to optimize tree construction.
            objectNames.sort(compareObjectNames);
            var n:int = objectNames.length;
            for (var i:int = 0; i < n; ++i)
            {
                buildMBeanNode(domainNode, objectNames[i]);
            }
            _mbeanModel.push(domainNode);
        }

        /**
         * Comparator function for sorting MBean object names.
         */
        private function compareObjectNames(first:ObjectName, second:ObjectName):int
        {
            var firstType:String = first.getKeyProperty("type").toLowerCase();
            var secondType:String = second.getKeyProperty("type").toLowerCase();
            if (firstType > secondType)
                return 1;
            else if (firstType < secondType)
                return -1;
            else // Types match - compare on id.
            {
                var firstId:String = first.getKeyProperty("id").toLowerCase();
                var secondId:String = second.getKeyProperty("id").toLowerCase();
                return (firstId > secondId) ? 1 : (firstId < secondId) ? -1 : 0;
            }
        }

        /**
         * Builds and adds a tree node based on the MBean object name.
         */
        private function buildMBeanNode(parent:Object, name:ObjectName):void
        {
            var types:Array = name.getKeyProperty("type").split('.');
            var mbeanId:String = name.getKeyProperty("id") as String;
            // Sink to our insertion point in the type hierarchy, adding type nodes
            // along the way when necessary.
            var n:int = types.length;
            for (var i:int = 0; i < n; ++i)
            {
                if (types[i] == "AdminConsoleDisplay")
                {
                    var app:String = ((name.canonicalName as String).split(":") as Array)[0];
                    _displayTypesObject[app] = name;
                    return;
                }
            }
            
            for (i = 0; i < n; ++i)
            {
                var createChildType:Boolean = true;
                if (parent.children.length)
                {
                    // Search for a child matching on current type. This is a linear search,
                    // but there generally aren't enough child nodes to warrant anything more performant.
                    var children:Array = parent.children;
                    var node:Object;
                    var n2:int = children.length;
                    for (var j:int = 0; j < n2; ++j)
                    {
                        node = children[j];
                        if (node.hasOwnProperty("type") && node.type == types[i])
                        {
                            parent = children[j];
                            createChildType = false;
                            // Check whether the parent type (container) node contains a child MBean node
                            // that matches on id. This is the ancestor MBean instance, out of all ancestors
                            // of this current type, that we're parented under.
                            // Again, a linear search because there generally aren't enough siblings of a
                            // given type to warrant anything more performant.
                            if (parent.children.length)
                            {
                                children = parent.children;
                                var n3:int = children.length;
                                for (var k:int = 0; k < n3; ++k)
                                {
                                    node = children[k];
                                    if (node.hasOwnProperty("objectName") &&
                                         (node.objectName.getKeyProperty("id") == name.getKeyProperty(types[i])))
                                     {
                                        parent = node;
                                     }
                                }
                            }
                            break;
                        }
                    }
                }
                if (createChildType)
                {
                    node = {label: types[i], type: types[i], children: []};
                    parent.children.push(node);
                    parent = node;
                }
            }
            // We're done generating the ancestor hierarchy.
            // Insert the MBean node.
            if (parent.children.length)
            {
                // Simple linear search for insertion point among siblings for now.
                var siblings:Array = parent.children;
                var id:String = mbeanId.toLowerCase();
                node;
                n = siblings.length;
                for (i = 0; i < n; ++i)
                {
                    node = siblings[i];
                    if (node.hasOwnProperty("objectName") &&
                        (node.objectName.getKeyProperty("id").toLowerCase() > id))
                    {
                        mbeanNode = {label: mbeanId, objectName: name, children: []};
                        if (i == siblings.length)
                            siblings.push(mbeanNode);
                        else
                            siblings.splice(i+1, 0, mbeanNode);
                        return;
                    }
                }
            }
            // Add the MBean node without specifying an index; no insertion point was found for it.
            var mbeanNode:Object = {label: mbeanId, objectName: name, children: []};
            parent.children.push(mbeanNode);
        }
        

        
        /**
         * Handles the result of invoke().
         */
        private function handleInvoke(e:ResultEvent):void
        {
            if (_parent)
            {
                var resultWindow:ConsoleResultWindow = ConsoleResultWindow(PopUpManager.createPopUp(_parent, ConsoleResultWindow, false));
                PopUpManager.centerPopUp(resultWindow);
                if (e.result != null)
                {
                    resultWindow.showResult(e.result);
                }
                else // A null result indicates success with a void return type.
                {
                    resultWindow.showSuccess();
                }
            }
        }

    }
}