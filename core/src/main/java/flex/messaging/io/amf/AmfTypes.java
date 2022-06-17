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
 * The amf/rtmp data encoding format constants.
 */
public interface AmfTypes {
    // AMF marker constants
    int kNumberType = 0;
    int kBooleanType = 1;
    int kStringType = 2;
    int kObjectType = 3;
    int kMovieClipType = 4;
    int kNullType = 5;
    int kUndefinedType = 6;
    int kReferenceType = 7;
    int kECMAArrayType = 8;
    int kObjectEndType = 9;
    int kStrictArrayType = 10;
    int kDateType = 11;
    int kLongStringType = 12;
    int kUnsupportedType = 13;
    int kRecordsetType = 14;
    int kXMLObjectType = 15;
    int kTypedObjectType = 16;
    int kAvmPlusObjectType = 17;
}


