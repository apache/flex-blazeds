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
package flex.messaging.io.amfx;

/**
 * AMFX is an XML representation of AMF 3.
 */
public interface AmfxTypes {
    // AMFX Packet Structure
    String AMFX_TYPE = "amfx";
    String HEADER_TYPE = "header";
    String BODY_TYPE = "body";

    // AMFX ActionScript types
    String UNDEFINED_TYPE = "undefined";
    String NULL_TYPE = "null";
    String FALSE_TYPE = "false";
    String TRUE_TYPE = "true";
    String INTEGER_TYPE = "int";
    String ITEM_TYPE = "item";
    String DOUBLE_TYPE = "double";
    String STRING_TYPE = "string";
    String XML_TYPE = "xmldocument";
    String DATE_TYPE = "date";
    String DICTIONARY_TYPE = "dictionary";
    String ARRAY_TYPE = "array";
    String OBJECT_TYPE = "object";
    String AVM_PLUS_XML_TYPE = "xml";
    String BYTE_ARRAY_TYPE = "bytearray";

    // Special metadata types
    String REF_TYPE = "ref";
    String TRAITS_TYPE = "traits";


    // AMFX special tag constants
    String EMPTY_STRING_TAG = "<string/>";
    String FALSE_TAG = "<false/>";
    String NULL_TAG = "<null/>";
    String TRUE_TAG = "<true/>";
    String UNDEFINED_TAG = "<undefined/>";

    String EMPTY_TRAITS_TAG = "<traits/>";

    // AMFX tag constants
    String AMFX_OPEN_TAG = "<amfx>";
    String AMFX_CLOSE_TAG = "</amfx>";
    String HEADER_OPEN_TAG = "<header>";
    String HEADER_CLOSE_TAG = "</header>";
    String BODY_OPEN_TAG = "<body>";
    String BODY_CLOSE_TAG = "</body>";


    String ARRAY_OPEN_TAG = "<array>";
    String ARRAY_CLOSE_TAG = "</array>";
    String BYTE_ARRAY_OPEN_TAG = "<bytearray>";
    String BYTE_ARRAY_CLOSE_TAG = "</bytearray>";
    String DATE_OPEN_TAG = "<date>";
    String DATE_CLOSE_TAG = "</date>";
    String DICTIONARY_CLOSE_TAG = "</dictionary>";
    String DOUBLE_OPEN_TAG = "<double>";
    String DOUBLE_CLOSE_TAG = "</double>";
    String INTEGER_OPEN_TAG = "<int>";
    String INTEGER_CLOSE_TAG = "</int>";
    String ITEM_OPEN_TAG = "<item>";
    String ITEM_CLOSE_TAG = "</item>";
    String OBJECT_OPEN_TAG = "<object>";
    String OBJECT_CLOSE_TAG = "</object>";
    String STRING_OPEN_TAG = "<string>";
    String STRING_CLOSE_TAG = "</string>";
    String XML_OPEN_TAG = "<xml>";
    String XML_CLOSE_TAG = "</xml>";
    String XML_DOC_OPEN_TAG = "<xmldocument>";
    String XML_DOC_CLOSE_TAG = "</xmldocument>";

    String TRAITS_OPEN_TAG = "<traits>";
    String TRAITS_CLOSE_TAG = "</traits>";

    String TRAITS_EXTERNALIZALBE_TAG = "<traits externalizable=\"true\" />";

    // AMFX Strings always use UTF-8
    String UTF_8 = "UTF-8";

}
