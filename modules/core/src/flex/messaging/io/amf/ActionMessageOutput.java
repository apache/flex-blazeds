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

import java.io.IOException;
import java.io.OutputStream;
import java.io.ObjectOutput;

/**
 * Entry point for serializing an individual ActionMessage
 * response message for AMF, AMFX or RTMP Channels.
 *
 * @author Peter Farland
 *
 * @see flex.messaging.io.amf.Amf0Input Classic Version 0 Deserializer
 * @see flex.messaging.io.amf.Amf3Input AVM+ Version 3 Deserializer
 *
 * @see flex.messaging.io.amf.ActionMessageOutput The serialization equivalent
 */
public interface ActionMessageOutput extends ObjectOutput
{
    // java.io.ObjectOutput METHODS

    /**
     * A convenient entry point for writing out any Object for conversion to ActionScript.
     * The Java class type of the Object will determine the corresponding ActionScript type
     * that will be specified in the AMF stream.
     * <p>
     * The following is a guide to the Java to ActionScript type mappings:
     * <table cellpadding="2" cellspacing="2" border="1">
     * <tr><td width="34%"><b>Java Type</b></td><td width="33%"><b>ActionScript Type (AMF 0)</b></td><td width="33%"><b>ActionScript Type (AMF 3 / AMFX)</b></td></tr>
     * <tr><td>java.lang.String</td><td>String</td><td>String</td></tr>
     * <tr><td>java.lang.Boolean</td><td>Boolean</td><td>Boolean</td></tr>
     * <tr><td>java.lang.Integer<br />java.lang.Short<br />java.lang.Byte</td><td>Number</td><td>int<sup>(a)</sup></td></tr>
     * <tr><td>java.lang.Double<br />java.lang.Long<br />java.lang.Float</td><td>Number</td><td>Number</td></tr>
     * <tr><td>java.util.Calendar<br />java.util.Date</td><td>Date<sup>(b)</sup></td><td>Date<sup>(b)</sup></td></tr>
     * <tr><td>java.lang.Character<br />java.lang.Character[]</td><td>String</td><td>String</td></tr>
     * <tr><td>java.lang.Byte[]</td><td>Array (of Numbers)</td><td>ByteArray</td></tr>
     * <tr><td>java.util.Collection</td><td>mx.collections.ArrayCollection<sup>(c)</sup></td><td>mx.collections.ArrayCollection<sup>(c)</sup></td></tr>
     * <tr><td>java.lang.Object[] (Native Array)</td><td>Array</td><td>Array</td></tr>
     * <tr><td>java.util.Map<br />java.util.Dictionary</td><td>Object<sup>(d)</sup></td><td>Object<sup>(d)</sup></td></tr>
     * <tr><td>java.lang.Object (Other than the above)</td><td>Typed Object<sup>(e)</sup></td><td>Typed Object<sup>(e)</sup></td></tr>
     * <tr><td>null</td><td>null</td><td>null</td></tr>
     * </table>
     * </p>
     * <p>
     * (a) - For AMF 3 ints, taking sign into consideration, if i &lt; 0xF0000000 || i &gt; 0x0FFFFFFF, then value is promoted to Number.
     * <br/>
     * (b) - Dates are sent in the UTC timezone. Clients and servers must adjust time accordingly for timezones.
     * <br/>
     * (c) - Channel serialization configuration can be set to support legacy Collection to ActionScript Array conversion.
     * <br/>
     * (d) - Channel serialization configuration can be set to support legacy Map to ActionScript Array (associative) conversion.
     * <br/>
     * (e) - Objects are serialized using Java Bean introspection rules. Fields that are static, transient or non-public are excluded.
     * </p>
     *
     * @param object the Object to be written
     * @throws IOException  if an I/O error occurs
     */
    void writeObject(Object object) throws IOException;

    /**
     * Internal use only.
     * @exclude
     */
    void writeObjectTraits(TraitsInfo traits) throws IOException;

    /**
     * Internal use only.
     * @exclude
     */
    void writeObjectProperty(String name, Object value) throws IOException;

    /**
     * Internal use only.
     * @exclude
     */
    void writeObjectEnd() throws IOException;


    //
    // INITIALIZATION UTILITIES
    //

    /**
     * Internal use only.
     * @exclude
     */
    void setDebugTrace(AmfTrace debugBuffer);

    /**
     * Internal use only.
     * @exclude
     */
    void setOutputStream(OutputStream out);

    /**
     * Internal use only.
     * @exclude
     */
    void reset();

}
