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
 
package mx.utils.tests
{

import flexunit.framework.*;

import flash.utils.ByteArray;
import mx.controls.Alert;
import mx.utils.*;

public class UIDUtilTest extends TestCase
{
    public static function suite():TestSuite
    {
        var suite : TestSuite = new TestSuite();
        suite.addTest(new UIDUtilTest("testIsUID"));
        suite.addTest(new UIDUtilTest("testUIDToByteArray"));
        suite.addTest(new UIDUtilTest("testUIDFromByteArray"));
        return suite;
    }

    public function UIDUtilTest(name:String)
    {
        super(name);
    }

    public function testIsUID():void
    {
        // Randomly generated
        var uid:String = UIDUtil.createUID();
        var result:Boolean = UIDUtil.isUID(uid);
        assertEquals(true, result);

        // Pre-determined normal UID
        uid = "8653177A-930D-F5DC-6FAA-E1E6F557504E";
        result = UIDUtil.isUID(uid);
        assertEquals(true, result);

        // All numbers
        uid = "06531779-9303-5532-6123-617685575043";
        result = UIDUtil.isUID(uid);
        assertEquals(true, result);

        // All letters
        uid = "FEABCDEE-FFCC-FCED-DAAB-BCDEFFABCDAE";
        result = UIDUtil.isUID(uid);
        assertEquals(true, result);

        // All Fs
        uid = "FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF";
        result = UIDUtil.isUID(uid);
        assertEquals(true, result);

        // All 0s
        uid = "00000000-0000-0000-0000-000000000000";
        result = UIDUtil.isUID(uid);
        assertEquals(true, result);

        // Invalid: Too long
        uid = "8653177A-930D-F5DC-6FAA-E1E6F557504EA";
        result = UIDUtil.isUID(uid);
        assertEquals(false, result);

        // Invalid: Too short
        uid = "8653177A-930D-F5DC-6FAA-E1E6F557504";
        result = UIDUtil.isUID(uid);
        assertEquals(false, result);

        // Invalid: all hyphens
        uid = "------------------------------------";
        result = UIDUtil.isUID(uid);
        assertEquals(false, result);

        // Invalid: No hyphens
        uid = "8653177A0930D0F5DC06FAA0E1E6F557504E";
        result = UIDUtil.isUID(uid);
        assertEquals(false, result);

        // Invalid: non-hex char at end
        uid = "8653177A-930D-F5DC-6FAA-E1E6F557504Z";
        result = UIDUtil.isUID(uid);
        assertEquals(false, result);

        // Invalid: non-hex char at start
        uid = "G653177A-930D-F5DC-6FAA-E1E6F557504E";
        result = UIDUtil.isUID(uid);
        assertEquals(false, result);

        // Invalid: null-char
        uid = "8653177A-930D-F5DC-6FAA-E1E6F557504\u0000";
        result = UIDUtil.isUID(uid);
        assertEquals(false, result);

        // Invalid: random word
        uid = "Cacophony";
        result = UIDUtil.isUID(uid);
        assertEquals(false, result);

        // Invalid: empty string
        uid = "";
        result = UIDUtil.isUID(uid);
        assertEquals(false, result);

        // Invalid: null
        uid = null;
        result = UIDUtil.isUID(uid);
        assertEquals(false, result);
    }

    public function testUIDToByteArray():void
    {
        // Randomly generated
        var uid:String = UIDUtil.createUID();
        var result:ByteArray = UIDUtil.toByteArray(uid);
        assertEquals(true, result.length == 16);

        // Pre-determined normal UID
        uid = "8FEB51AC-1443-CA4C-D3A7-1AC1C5DC517B";
        result = UIDUtil.toByteArray(uid);
        assertEquals(true, result.length == 16);

        // Invalid UID
        uid = "8FEB51AC-1443-CA4C-D3A7-1AC1C5DC517Z";
        result = UIDUtil.toByteArray(uid);
        assertEquals(null, result);
    }

    public function testUIDFromByteArray():void
    {
        // Randomly generated
        var uid1:String = UIDUtil.createUID();
        var ba:ByteArray = UIDUtil.toByteArray(uid1);
        var uid2:String = UIDUtil.fromByteArray(ba);
        assertEquals(uid1, uid2);

        // Pre-determined normal UID
        uid1 = "86531839-0109-8B18-BB3A-CF8F546D0399";
        ba = UIDUtil.toByteArray(uid1);
        uid2 = UIDUtil.fromByteArray(ba);
        assertEquals(uid1, uid2);

        // Invalid: ByteArray position not 0
        uid1 = "86531839-0109-8B18-BB3A-CF8F546D0399";
        ba = UIDUtil.toByteArray(uid1);
        ba.position = 15;
        uid2 = UIDUtil.fromByteArray(ba);
        assertEquals(null, uid2);

        // Invalid: ByteArray too short
        ba = new ByteArray();
        ba.writeByte(19);
        ba.position = 0;
        uid2 = UIDUtil.fromByteArray(ba);
        assertEquals(null, uid2);

        // Invalid: null ByteArray
        ba = null;
        uid2 = UIDUtil.fromByteArray(ba);
        assertEquals(null, uid2);
    }
}

}