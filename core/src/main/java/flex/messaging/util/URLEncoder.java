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

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

/**
 * Utility class for URL encoding.
 */
public final class URLEncoder {
    public static String charset = "UTF8";

    private URLEncoder() {
    }

    public static String encode(String s) {
        try {
            return encode(s, charset);
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalArgumentException(charset);
        }
    }

    public static String encode(String s, String enc) throws UnsupportedEncodingException {
        if (!needsEncoding(s)) {
            return s;
        }

        int length = s.length();

        StringBuffer out = new StringBuffer(length);

        ByteArrayOutputStream buf = new ByteArrayOutputStream(10); // why 10? w3c says so.

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(buf, enc));

        for (int i = 0; i < length; i++) {
            int c = (int) s.charAt(i);
            if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c == ' ') {
                if (c == ' ') {
                    c = '+';
                }

                toHex(out, buf.toByteArray());
                buf.reset();

                out.append((char) c);
            } else {
                try {
                    writer.write(c);

                    if (c >= 0xD800 && c <= 0xDBFF && i < length - 1) {
                        int d = (int) s.charAt(i + 1);
                        if (d >= 0xDC00 && d <= 0xDFFF) {
                            writer.write(d);
                            i++;
                        }
                    }

                    writer.flush();
                } catch (IOException ex) {
                    throw new IllegalArgumentException(s);
                }
            }
        }
        try {
            writer.close();
        } catch (IOException ioe) {
            // Ignore exceptions on close.
        }

        toHex(out, buf.toByteArray());

        return out.toString();
    }

    private static void toHex(StringBuffer buffer, byte[] b) {
        for (int i = 0; i < b.length; i++) {
            buffer.append('%');

            char ch = Character.forDigit((b[i] >> 4) & 0xF, 16);
            if (Character.isLetter(ch)) {
                ch -= 32;
            }
            buffer.append(ch);

            ch = Character.forDigit(b[i] & 0xF, 16);
            if (Character.isLetter(ch)) {
                ch -= 32;
            }
            buffer.append(ch);
        }
    }

    private static boolean needsEncoding(String s) {
        if (s == null)
            return false;

        for (int i = 0; i < s.length(); i++) {
            int c = (int) s.charAt(i);
            if (!(c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9'))
                return true;
            // Otherwise, keep going
        }

        return false;
    }
}
