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

import flash.net.*;
import mx.collections.*;



use namespace bean_namespace;

public class Bean
{
    public function Bean()
    {
        registerClassAlias("dev.echoservice.Bean", Bean);
    }

    public var foo:String = "bar";

    // Instance Properties

    // Read Only
    public function get publicReadOnlyProperty():String
    {
        return _publicReadOnlyProperty;
    }

    // TODO: Uncomment this when mxmlc is fixed for byte code generation of != in E4X
    // Write Only

    public function set publicWriteOnlyProperty(value:String):void
    {
        _publicWriteOnlyProperty = value;
    }


    // Read-Write
    public function get publicProperty():String
    {
        return _publicProperty;
    }

    public function set publicProperty(value:String):void
    {
        _publicProperty = value;
    }

    private function get privateProperty():String
    {
        return _privateProperty;
    }

    private function set privateProperty(value:String):void
    {
        _privateProperty = value;
    }

    internal function get internalProperty():String
    {
        return _internalProperty;
    }

    internal function set internalProperty(value:String):void
    {
        _internalProperty = value;
    }

    bean_namespace function get namespaceProperty():String
    {
        return _namespaceProperty;
    }

    bean_namespace function set namespaceProperty(value:String):void
    {
        _namespaceProperty = value;
    }


    // Static Properties

    // Read Only
    public function get publicStaticReadOnlyProperty():String
    {
        return _publicStaticReadOnlyProperty;
    }

    // Write Only

    public function set publicStaticWriteOnlyProperty(value:String):void
    {
        _publicStaticWriteOnlyProperty = value;
    }


    // Read-Write
    public static function get publicStaticProperty():String
    {
        return _publicStaticProperty;
    }

    public static function set publicStaticProperty(value:String):void
    {
        _publicStaticProperty = value;
    }

    private static function get privateStaticProperty():String
    {
        return _privateStaticProperty;
    }

    private static function set privateStaticProperty(value:String):void
    {
        _privateStaticProperty = value;
    }

    internal static function get internalStaticProperty():String
    {
        return _internalStaticProperty;
    }

    internal static function set internalStaticProperty(value:String):void
    {
        _internalStaticProperty = value;
    }

    bean_namespace static function get namespaceStaticProperty():String
    {
        return _namespaceStaticProperty;
    }

    bean_namespace static function set namespaceStaticProperty(value:String):void
    {
        _namespaceStaticProperty = value;
    }


    // ---------------------------------------------------------------
    //
    // Variables (Fields)
    //
    // ---------------------------------------------------------------

    // Instance Fields
    public var publicField:String = "publicFieldValue";
    private var privateField:String = "privateFieldValue";
    internal var internalField:String = "internalFieldValue";
    bean_namespace var namespaceField:String = "namespaceFieldValue";

    // Static Fields
    public static var publicStaticField:String = "publicStaticFieldValue";
    private static var privateStaticField:String = "privateStaticFieldValue";
    internal static var internalStaticField:String = "internalStaticFieldValue";
    bean_namespace static var namespaceStaticField:String = "namespaceStaticFieldValue";

    // Static Const Fields
    public static const publicStaticConstField:String = "publicStaticConstFieldValue";
    private static const privateStaticConstField:String = "privateStaticConstFieldValue";
    internal static const internalStaticConstField:String = "internalStaticConstFieldValue";
    bean_namespace static const namespaceStaticConstField:String = "namespaceStaticConstFieldValue";

    // Instance Const Fields
    public const publicConstField:String = "publicConstFieldValue";
    private const privateConstField:String = "privateConstFieldValue";
    internal const internalConstField:String = "internalConstFieldValue";
    bean_namespace const namespaceConstField:String = "namespaceConstFieldValue";


    // Private property backing fields

    private var _publicProperty:String = "publicPropertyValue";
    private var _privateProperty:String = "privatePropertyValue";
    private var _internalProperty:String = "internalPropertyValue";
    private var _namespaceProperty:String = "namespacePropertyValue";

    private static var _publicStaticProperty:String = "publicStaticPropertyValue";
    private static var _privateStaticProperty:String = "privateStaticPropertyValue";
    private static var _internalStaticProperty:String = "internalStaticPropertyValue";
    private static var _namespaceStaticProperty:String = "namespaceStaticPropertyValue";

    private var _publicReadOnlyProperty:String = "publicReadOnlyPropertyValue";

    private var _publicWriteOnlyProperty:String = "publicWriteOnlyPropertyValue";

    private static var _publicStaticReadOnlyProperty:String = "publicStaticReadOnlyPropertyValue";

    private static var _publicStaticWriteOnlyProperty:String = "publicStaticWriteOnlyPropertyValue";
}

}