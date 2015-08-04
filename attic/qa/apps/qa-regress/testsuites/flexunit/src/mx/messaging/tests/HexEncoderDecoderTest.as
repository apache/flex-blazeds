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

package mx.messaging.tests
{

import flexunit.framework.*;

import flash.utils.ByteArray;
import mx.controls.Alert;
import mx.utils.*;
 /**
  * Test encoding and decoding of base16 or hexadecimal encoded byte arrays.
  */   
public class HexEncoderDecoderTest extends TestCase
{
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////   
    public function HexEncoderDecoderTest(name : String)
    {
        super(name);
    }
    ////////////////////////////////////////////////////////////////////////////
    //
    // TestSuite
    //
    ////////////////////////////////////////////////////////////////////////////  
    public static function suite() : TestSuite
    {
        var suite : TestSuite = new TestSuite();
        suite.addTest(new HexEncoderDecoderTest("testEncodeDecode"));
        return suite;
    } 
    ////////////////////////////////////////////////////////////////////////////
    //
    // Tests
    //
    ////////////////////////////////////////////////////////////////////////////  
    /**
     * Test encoding and decoding of base16 or hexadecimal encoded byte arrays using the 
     * HexEncoder and HexDecoder classes
     */     
    public function testEncodeDecode() :void
    {
        var randomLimit:int = 500;

        var success:Boolean = true;

        for (var myCount:int = 0; myCount < 100; myCount++)
        {

            var randomLength:Number = Math.random() * randomLimit;
            var rawArray:ByteArray = new ByteArray();
            for (var i:int = 0; i < randomLength; ++i)
            {
                if ((i % 1024) < 256)
                {
                    rawArray.writeByte(i % 1024);
                }
                else
                {
                    rawArray.writeByte(new int((Math.random()) * 255) - 128);
                }
            }
        
            rawArray.position = 0;
            var encoder : HexEncoder = new HexEncoder();
            encoder.encode(rawArray);

            var encoded:String = encoder.drain();

            var decoder : HexDecoder = new HexDecoder();
            decoder.decode(encoded);
            var decoded:ByteArray = decoder.flush();
                
            rawArray.position = 0;
            decoded.position = 0;        
            if (decoded.bytesAvailable != rawArray.bytesAvailable)
            {
                success = false;
            }
            else
            {
                while (decoded.bytesAvailable)
                {
                    if (decoded.readByte() != rawArray.readByte())
                    {
                        success = false;
                        break;
                    }
                }
            }
        
            if (!success)
            {
                break;
            }        
        }
        
        assertEquals(true, success);
    }
}
}