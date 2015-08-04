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
function FDMSLibrary() {}

// matches with mx.messaging.ChannelSet
function ChannelSet()
{
    this.fb_instance_id = FDMSLibrary.create(this.typeName).value;    
}
   
// matches with mx.messaging.Consumer
function Consumer()
{
    this.fb_instance_id = FDMSLibrary.create(this.typeName).value;
}

// matches with mx.messaging.Producer
function Producer()
{
    this.fb_instance_id = FDMSLibrary.create(this.typeName).value;
}

// matches with mx.messaging.channels.AMFChannel
function AMFChannel(id, uri)
{
    this.fb_instance_id = FDMSLibrary.create(this.typeName, id, uri).value;    
}

// matches with mx.messaging.channels.HTTPChannel
function HTTPChannel(id, uri)
{
    this.fb_instance_id = FDMSLibrary.create(this.typeName, id, uri).value;    
}

// matches with mx.messaging.channels.SecureAMFChannel
function SecureAMFChannel(id, uri)
{
    this.fb_instance_id = FDMSLibrary.create(this.typeName, id, uri).value;    
}

// matches with mx.messaging.channels.SecureHTTPChannel
function SecureHTTPChannel(id, uri)
{
    this.fb_instance_id = FDMSLibrary.create(this.typeName, id, uri).value;    
}

// matches with mx.messaging.channels.SecureStreamingAMFChannel
function SecureStreamingAMFChannel(id, uri)
{
    this.fb_instance_id = FDMSLibrary.create(this.typeName, id, uri).value;    
}

// matches with mx.messaging.channels.SecureStreamingHTTPChannel
function SecureStreamingHTTPChannel(id, uri)
{
    this.fb_instance_id = FDMSLibrary.create(this.typeName, id, uri).value;    
}

// matches with mx.messaging.channels.StreamingAMFChannel
function StreamingAMFChannel(id, uri)
{
    this.fb_instance_id = FDMSLibrary.create(this.typeName, id, uri).value;    
}

// matches with mx.messaging.channels.StreamingHTTPChannel
function StreamingHTTPChannel(id, uri)
{
    this.fb_instance_id = FDMSLibrary.create(this.typeName, id, uri).value;    
}

// matches with mx.messaging.messages.AsyncMessage
function AsyncMessage()
{
    this.fb_instance_id = FDMSLibrary.create(this.typeName).value;
}

// matches with mx.collections.ArrayCollection
function ArrayCollection()
{
    if (arguments.length > 0)
    {
        this.fb_instance_id = arguments[0];
    }
    else
    {
        this.fb_instance_id = FDMSLibrary.create(this.typeName).value;
    }
}

// matches with mx.collections.Sort
function Sort()
{
    this.fb_instance_id = FDMSLibrary.create(this.typeName).value;
}

// matches with mx.collections.SortField
function SortField(name, caseInsensitive, descending, numeric)
{
    name = name || null;
    caseInsensitive = caseInsensitive || false;
    descending = descending || false;
    numeric = numeric || false;
    this.fb_instance_id = FDMSLibrary.create(this.typeName, name, caseInsensitive, descending, numeric).value;
}

// matches with mx.rpc.AsyncResponder
function AsyncResponder(result, fault, token)
{
    token = token || null;
    this.fb_instance_id = FDMSLibrary.create(this.typeName, result, fault, token).value;    
}

function FDMSLibrary_initialized(typeData, bridgeName)
{
    this.fdsBridge = FABridge[bridgeName];
    for (var i = 0; i < typeData.length; i++)
    {
        fdsBridge.addTypeDataToCache(typeData[i]);
    }
    FDMSLibrary["bridge"] = fdsBridge;

    // setup ArrayCollection, and other types that will be used natively from JS 
    // e.g. var cs = new Consumer()
    AsyncResponder.prototype = fdsBridge.getTypeFromName("mx.rpc::AsyncResponder");
    ChannelSet.prototype = fdsBridge.getTypeFromName("mx.messaging::ChannelSet");    
    Producer.prototype = fdsBridge.getTypeFromName("mx.messaging::Producer");
    Consumer.prototype = fdsBridge.getTypeFromName("mx.messaging::Consumer");
    AMFChannel.prototype = fdsBridge.getTypeFromName("mx.messaging.channels::AMFChannel");    
    HTTPChannel.prototype = fdsBridge.getTypeFromName("mx.messaging.channels::HTTPChannel");    
    SecureAMFChannel.prototype = fdsBridge.getTypeFromName("mx.messaging.channels::SecureAMFChannel");    
    SecureHTTPChannel.prototype = fdsBridge.getTypeFromName("mx.messaging.channels::SecureHTTPChannel");    
    SecureStreamingAMFChannel.prototype = fdsBridge.getTypeFromName("mx.messaging.channels::SecureStreamingAMFChannel");    
    SecureStreamingHTTPChannel.prototype = fdsBridge.getTypeFromName("mx.messaging.channels::SecureStreamingHTTPChannel");    
    StreamingAMFChannel.prototype = fdsBridge.getTypeFromName("mx.messaging.channels::StreamingAMFChannel");    
    StreamingHTTPChannel.prototype = fdsBridge.getTypeFromName("mx.messaging.channels::StreamingHTTPChannel");    
    AsyncMessage.prototype = fdsBridge.getTypeFromName("mx.messaging.messages::AsyncMessage");
    ArrayCollection.prototype = fdsBridge.getTypeFromName("mx.collections::ArrayCollection");
    Sort.prototype = fdsBridge.getTypeFromName("mx.collections::Sort");
    SortField.prototype = fdsBridge.getTypeFromName("mx.collections::SortField");

    // notify the page that the FDMSLibrary has completed initializations and can be used
    // if you do not wait for this call in your page it is possible to encounter errors
    FDMSLibrary_ready = true;
    FDMSLibrary.notifyFDMSLibraryReady("flash");
}

// create an instance of the specified class
FDMSLibrary.create = function(className)
{
    var args = FABridge.argsToArray(arguments);
    args.shift(); // remove className

    if (FABridge.refCount > 0)
	{
	    throw new Error("You are trying to call recursively into the Flash Player which is not allowed. In most cases the JavaScript setTimeout function, can be used as a workaround.");
    }
    else
    {
        FABridge.refCount++;
        retVal = FDMSLibrary["bridge"].target.createObject(className, FDMSLibrary["bridge"].serialize(args));
        FABridge.refCount--;
        return retVal;
    }
}
// destroy an existing AS object by removing all references to it from the cache maps
FDMSLibrary.destroyObject = function(value)
{
	var retVal = fdsBridge.releaseNamedASObject(value);

}
// decrement the reference count for the object passed as parameter
FDMSLibrary.release = function(value)
{
	FDMSLibrary["bridge"].release(value);
}
// increment the reference count for the object passed as parameter
FDMSLibrary.addRef = function(value)
{
	FDMSLibrary["bridge"].addRef(value);
}
// load the swf with the fdmsbridge.as class and create the code to add it to the page.
FDMSLibrary.load = function(path, callback)
{

    var result = "<object id='_fesLib' classid='clsid:D27CDB6E-AE6D-11cf-96B8-444553540000' \
                          codebase='http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=8,5,0,0' \
                          height='1' width='1'> \
                     <param name='flashvars' value='bridgeName=flash'/> \
                     <param name='AllowScriptAccess' value='always'/> \
                     <param name='src' value='"+ (path != undefined ? path : "") + "'/> \
                     <param name='wmode' value='transparent' /> \
                     <embed name='_fesLib' pluginspage='http://www.macromedia.com/go/getflashplayer' allowScriptAccess='always'\
                     src='" + (path != undefined ? path : "") + "' height='1' width='1' flashvars='bridgeName=flash'/> \
                  </object>";
    document.write(result);


    // todo:need a callback and variable here so you can keep track that both the fabridge and the fdmsbridge are available
    FDMSLibrary.addInitializationCallback("flash", callback);

    // register for call back when the FABridge has completed initialization
    FABridge.addInitializationCallback("flash", FABridge_ready);
}

/**
 * Indicates to the FDMSLibrary that the FABridge is now connected.
 * The FDSMLibrary will notify the client when the other required initialization is complete.
 */
function FABridge_ready()
{
    FABridge_ready = true;
    FDMSLibrary.notifyFDMSLibraryReady("flash");
}

var FABridge_ready;
var FDMSLibrary_ready;
FDMSLibrary.initCallbacks = {}
/**
 *  Attach a callback to the underlying FABridge to get notified when the initialization is done
 */
FDMSLibrary.addInitializationCallback = function(bridgeName, callback)
{
    if (FABridge_ready != undefined && FDMSLibrary_ready != undefined)
    {
        callback.call(inst);
        return;
    }
    var callbackList = FDMSLibrary.initCallbacks[bridgeName];
    if(callbackList == null)
        FDMSLibrary.initCallbacks[bridgeName] = callbackList = [];

    callbackList.push(callback);
}
/**
 * Call the JS functions attached as callbacks on the library initialization and clear them from the list
 * once their are done executing
 */
FDMSLibrary.notifyFDMSLibraryReady = function(bridgeName)
{
    if (FABridge_ready != undefined && FDMSLibrary_ready != undefined)
    {
        var callbacks = FDMSLibrary.initCallbacks[bridgeName];
        if(callbacks == null)
            return;
        for(var i=0;i<callbacks.length;i++)
            callbacks[i].call();
        delete FDMSLibrary.initCallbacks[bridgeName]
    }
}
