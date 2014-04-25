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

/**
 * Code to read and write Base64-encoded text.
 * Fairly special-purpose, not quite ready for
 * general streaming as they don't let you
 * drain less than everything that is currently
 * available.
 * 
 * @exclude
 */
public class Hex
{
    private static final char digits[] =
            {
                '0', '1', '2', '3', '4', '5', '6', '7',
                '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
            };
    /**
    * @exclude
    */
    public static class Decoder
    {
        private int filled = 0;
        private byte data[];
        private int work[] = {0, 0};


        public Decoder()
        {
            data = new byte[256];
        }

        public void decode(String encoded)
        {

            int estimate = 1 + encoded.length() / 2;

            if (filled + estimate > data.length)
            {
                int length = data.length * 2;
                while (length < filled + estimate)
                {
                    length *= 2;
                }
                byte[] newdata = new byte[length];

                System.arraycopy(data, 0, newdata, 0, filled);
                data = newdata;
            }

            for (int i = 0; i < encoded.length(); ++i)
            {
                work[0] = Character.digit(encoded.charAt(i), 16);
                i++;
                work[1] = Character.digit(encoded.charAt(i), 16);
                data[filled++] = (byte) (((work[0] << 4) | (work[1])) & 0xff);
            }
        }

        public byte[] drain()
        {
            byte[] r = new byte[filled];
            System.arraycopy(data, 0, r, 0, filled);
            filled = 0;
            return r;
        }

        public byte[] flush() throws IllegalStateException
        {
            return drain();
        }

        public void reset()
        {
            filled = 0;
        }

    }

    /**
    * @exclude
    */
    public static class Encoder
    {
        private StringBuffer output;

        public Encoder(int size)
        {
            output = new StringBuffer(size * 2);
        }

        private void encodeBlock(byte work)
        {
            output.append(digits[(work & 0xF0) >>> 4]);
            output.append(digits[(work & 0x0F)]);
        }

        public void encode(byte[] data)
        {
            encode(data, 0, data.length);
        }

        public void encode(byte[] data, int offset, int length)
        {
            int plainIndex = offset;

            while (plainIndex < (offset + length))
            {
                encodeBlock(data[plainIndex]);
                plainIndex++;
            }
        }

        public String drain()
        {
            String r = output.toString();
            output.setLength(0);
            return r;
        }

        public String flush()
        {
            return drain();
        }
    }
}