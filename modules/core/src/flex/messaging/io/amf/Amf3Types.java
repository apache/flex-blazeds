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
package flex.messaging.io.amf;

/**
 * AMF3 type markers and constants for AVM+ Serialization.
 *
 * @see flex.messaging.io.amf.AmfTypes for AMF 0 Type Markers.
 */
public interface Amf3Types
{
    // AMF marker constants
    int kUndefinedType  = 0;
    int kNullType       = 1;
    int kFalseType      = 2;
    int kTrueType       = 3;
    int kIntegerType    = 4;
    int kDoubleType     = 5;
    int kStringType     = 6;
    int kXMLType        = 7;
    int kDateType       = 8;
    int kArrayType      = 9;
    int kObjectType     = 10;
    int kAvmPlusXmlType = 11;
    int kByteArrayType  = 12;
    int kTypedVectorInt = 13;
    int kTypedVectorUint= 14;
    int kTypedVectorDouble = 15;
    int kTypedVectorObject = 16;
    int kDictionaryType = 17;

    String EMPTY_STRING = "";

    /**
     * Internal use only.
     *
     */
    int UINT29_MASK = 0x1FFFFFFF; // 2^29 - 1

    /**
     * The maximum value for an <code>int</code> that will avoid promotion to an
     * ActionScript Number when sent via AMF 3 is 2<sup>28</sup> - 1, or <code>0x0FFFFFFF</code>.
     */
    int INT28_MAX_VALUE = 0x0FFFFFFF; // 2^28 - 1

    /**
     * The minimum value for an <code>int</code> that will avoid promotion to an
     * ActionScript Number when sent via AMF 3 is -2<sup>28</sup> or <code>0xF0000000</code>.
     */
    int INT28_MIN_VALUE = 0xF0000000; // -2^28 in 2^29 scheme
}
