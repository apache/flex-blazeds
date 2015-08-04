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
package
{

import bridge.FABridge;
import flash.display.DisplayObject;
import flash.display.MovieClip;
import flash.events.Event;
import flash.external.ExternalInterface;
import flash.system.ApplicationDomain;
import flash.utils.describeType;
import mx.collections.ArrayCollection;
import mx.collections.IViewCursor;
import mx.collections.Sort;
import mx.collections.SortField;
import mx.messaging.config.LoaderConfig;
import mx.managers.SystemManager;
import mx.messaging.ChannelSet;
import mx.messaging.Consumer;
import mx.messaging.Producer;
import mx.messaging.channels.AMFChannel;
import mx.messaging.channels.HTTPChannel;
import mx.messaging.channels.SecureAMFChannel;
import mx.messaging.channels.SecureHTTPChannel;
import mx.messaging.channels.SecureStreamingAMFChannel;
import mx.messaging.channels.SecureStreamingHTTPChannel
import mx.messaging.channels.StreamingAMFChannel;
import mx.messaging.channels.StreamingHTTPChannel;
import mx.messaging.messages.AsyncMessage;
import mx.rpc.AsyncResponder;
import mx.utils.object_proxy;
import mx.utils.UIDUtil;
import mx.core.mx_internal;

use namespace object_proxy;

[Frame(extraClass="FakeFlexInit")]
public class FDMSBridge extends FDMSBase
{

    private var gateway:FABridge;

    public function FDMSBridge()
    {
        super();

        if (ExternalInterface.available == false)
        {
            return;
        }

        gateway = new FABridge();
        gateway.rootObject = this;
        gateway.addEventListener(FABridge.INITIALIZED, gatewayInitialized);

        ExternalInterface.addCallback("createObject", createObject);
    }


    /**
     * The createObject function is the callback used by the bridge to construct
     * new ActionScript objects.  As is, it supports constructors with up to four
     * arguments.  If more than that are required, please update this function
     * and recompile with your appliation.
     */
    private function createObject(className:String, cArgs:Array):Object
    {
        var c:Class = Class(ApplicationDomain.currentDomain.getDefinition(className));
        var instance:Object;

        cArgs = gateway.deserialize(cArgs);

        // used to pass constructor arguments
        switch (cArgs.length)
        {
            case 0:
                instance = new c();
            break;

            case 1:
                instance = new c(cArgs[0]);
            break;

            case 2:
                instance = new c(cArgs[0], cArgs[1]);
            break;

            case 3:
                instance = new c(cArgs[0], cArgs[1], cArgs[2]);
            break;

            case 4:
                instance = new c(cArgs[0], cArgs[1], cArgs[2], cArgs[3]);
            break;

            default:
                throw new Error("Too many arguments. Please update 'createObject' for your use case.");
        }
        //
        // make sure the reference is known
        var objRef:Number = gateway.getRef(instance, true);
        gateway.incRef(objRef);

        return gateway.serialize(instance);
    }

    //initialize the gateway and load the type descriptions for supported objects from AS
    private function gatewayInitialized(event:Event):void
    {
        var typeDesc:Array = [];

        var acRef:ArrayCollection;
        var asyncResponderRef:AsyncResponder;
        var csRef:ChannelSet;  
        var pRef:Producer;
        var cRef:Consumer;      
        var amfRef:AMFChannel;
        var httpRef:HTTPChannel;
        var samfRef:SecureAMFChannel;
        var shttpRef:SecureHTTPChannel;
        var streamAmfRef:StreamingAMFChannel;
        var streamHttpRef:StreamingHTTPChannel;
        var sstreamAmfRef:SecureStreamingAMFChannel;
        var sstreamHttpRef:SecureStreamingHTTPChannel;
        var asmRef:AsyncMessage;
        var lRef:IViewCursor;
        var sortRef:Sort;
        var sortFieldRef:SortField;

        typeDesc.push(gateway.retrieveCachedTypeDescription("mx.collections.ArrayCollection", true));
        typeDesc.push(gateway.retrieveCachedTypeDescription("mx.collections.IViewCursor", true));
        typeDesc.push(gateway.retrieveCachedTypeDescription("mx.rpc.AsyncResponder", true));
        typeDesc.push(gateway.retrieveCachedTypeDescription("mx.messaging.ChannelSet", true));
        typeDesc.push(gateway.retrieveCachedTypeDescription("mx.messaging.Producer", true));
        typeDesc.push(gateway.retrieveCachedTypeDescription("mx.messaging.Consumer", true));
        typeDesc.push(gateway.retrieveCachedTypeDescription("mx.messaging.channels.AMFChannel", true));
        typeDesc.push(gateway.retrieveCachedTypeDescription("mx.messaging.channels.HTTPChannel", true));
        typeDesc.push(gateway.retrieveCachedTypeDescription("mx.messaging.channels.SecureAMFChannel", true));
        typeDesc.push(gateway.retrieveCachedTypeDescription("mx.messaging.channels.SecureHTTPChannel", true));
        typeDesc.push(gateway.retrieveCachedTypeDescription("mx.messaging.channels.SecureStreamingAMFChannel", true));
        typeDesc.push(gateway.retrieveCachedTypeDescription("mx.messaging.channels.SecureStreamingHTTPChannel", true));
        typeDesc.push(gateway.retrieveCachedTypeDescription("mx.messaging.channels.StreamingAMFChannel", true));
        typeDesc.push(gateway.retrieveCachedTypeDescription("mx.messaging.channels.StreamingHTTPChannel", true));
        typeDesc.push(gateway.retrieveCachedTypeDescription("mx.messaging.messages.AsyncMessage", true));
        typeDesc.push(gateway.retrieveCachedTypeDescription("mx.collections.Sort", true));
        typeDesc.push(gateway.retrieveCachedTypeDescription("mx.collections.SortField", true));

        //the ListCollectionView is now serialized natively accross the bridge; however if you want to access an object over the bridge and is not serialized correctly
        // this is the way to add custom serializer
        //see below for the actual implementation
        //gateway.addCustomSerialization("ListCollectionViewCursor", serializeIViewCursor);
        try
        {
            mx.messaging.config.LoaderConfig.mx_internal::_url = loaderInfo.loaderURL;
            mx.messaging.config.LoaderConfig.mx_internal::_parameters = loaderInfo.parameters;
        }
        catch(e:Error)
        {
            trace("could not set initial URL, connections will probably not work !");
        }

        ExternalInterface.call("FDMSLibrary_initialized", typeDesc, gateway.bridgeName);
    }

    //the ListCollectionView is already serialized natively accross the bridge
    // but if yo uneed to you can define a custom serializer and use an interface or base class as starting point
    // this is for informative purposes
    // any ListCollectionViewCursor will be serialized by its interface IViewCursor
    private function serializeIViewCursor(value:*):*
    {
        var result:* = {};
        result.newTypes = [];
        result.newRefs = {};

        // serialize a class
        result.type = FABridge.TYPE_ASINSTANCE;

        // make sure the type info is available
        var className:String = "mx.collections::IViewCursor";

        if (gateway.retrieveCachedTypeDescription(className, false) == null)
        {
            var desc:Object = gateway.retrieveCachedTypeDescription(className, true);
            result.newTypes.push(desc);
        }

        // make sure the reference is known
        var objRef:Number = gateway.getRef(IViewCursor(value), false);
        if (isNaN(objRef))
        {
            objRef = gateway.getRef(IViewCursor(value), true);
            result.newRefs[objRef] = className;
        }
        result.value = objRef;

        return result;
    }
}
}
