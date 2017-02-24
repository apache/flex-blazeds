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

package flex.messaging.util;


import org.junit.Test;

public class Basae64Test {
    /**
     * Try encoding and decoding 10,000 random combinations of bytes.
     */
    @Test
    public void testEncodingAndDecoding() {
        int randomLimit = 500;
        boolean success = true;

        byte raw[] = new byte[(int) (Math.random() * randomLimit)];

        for (int i = 0; i < raw.length; ++i) {
            if ((i % 1024) < 256)
                raw[i] = (byte) (i % 1024);
            else
                raw[i] = (byte) ((int) (Math.random() * 255) - 128);
        }
        Base64.Encoder encoder = new Base64.Encoder(100);
        encoder.encode(raw);

        String encoded = encoder.drain();

        Base64.Decoder decoder = new Base64.Decoder();
        decoder.decode(encoded);
        byte check[] = decoder.flush();

        if (check.length != raw.length) {
            success = false;
        } else {
            for (int i = 0; i < check.length; ++i) {
                if (check[i] != raw[i]) {
                    success = false;
                    break;
                }
            }
        }

        org.junit.Assert.assertTrue(success);
    }
}
