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

import java.io.UnsupportedEncodingException;


/**
 * Utility class for URL decoding.
 * 
 *@exclude
 */
public final class URLDecoder
{
    public static String decode(String s)
    {
        try
        {
            return decode(s, "UTF8");
        }
        catch (UnsupportedEncodingException ex)
        {
            throw new IllegalArgumentException("UTF8");
        }
    }

    public static String decode(String s, String enc) throws UnsupportedEncodingException
    {
        if (!needsDecoding(s))
        {
            return s;
        }

        int length = s.length();
        byte[] bytes = new byte[length];
        // FIXME: This needs to specify a character encoding (ASCII?)
        s.getBytes(0, length, bytes, 0);
        int k = 0;
        length = bytes.length;
        for (int i = 0; i < length; i++)
        {
            if (bytes[i] == '%')
            {
                while (bytes[i + 1] == '%')
                {
                    i++;
                }
                if (i < length - 2)
                {
                    bytes[k] = x2c(bytes, i);
                    i += 2;
                }
                else
                {
                    throw new IllegalArgumentException(s);
                }
            }
            else if (bytes[i] == '+')
            {
                bytes[k] = (byte)' ';
            }
            else
            {
                bytes[k] = bytes[i];
            }
            k++;
        }

        return new String(bytes, 0, k, enc);
    }

    private static boolean needsDecoding(String s)
    {
        if (s == null)
        {
            return false;
        }

        int length = s.length();

        for (int i = 0; i < length; i++)
        {
            int c = (int)s.charAt(i);
            if (c == '+' || c == '%')
            {
                return true;
            }
        }

        return false;

    }

    private static byte x2c(byte[] b, int i)
    {
        int result;
        byte b1 = b[i + 1];
        byte b2 = b[i + 2];

        // return Byte.parseByte("" + (char) b1 + (char) b2, 16);

        if (b1 < '0' || (b1 > 'F' && b1 < 'a') || b1 > 'f' ||
                b2 < '0' || (b2 > 'F' && b2 < 'a') || b2 > 'f')
        {
            throw new IllegalArgumentException("%" + (char)b1 + (char)b2);
        }

        result = b1 >= 'A' ? (b1 & 0xdf) - 'A' + 10 : b1 - '0';
        result *= 16;
        result += b2 >= 'A' ? (b2 & 0xdf) - 'A' + 10 : b2 - '0';
        return (byte)result;
    }
}
