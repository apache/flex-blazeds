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

import java.io.IOException;
import java.io.InputStream;

/**
 * @exclude
 */
public class FileUtils
{
    public static final String UTF_8 = "UTF-8";
    public static final String UTF_16 = "UTF-16";

    /**
     * Sets a mark in the InputStream for 3 bytes to check for a BOM. If the BOM
     * stands for UTF-8 encoded content then the stream will not be reset, otherwise
     * for UTF-16 with a BOM or any other encoding situation the stream is reset to the
     * mark (as for UTF-16 the parser will handle the BOM).
     *
     * @param in InputStream containing BOM and must support mark().
     * @param default_encoding The default character set encoding. null or "" => system default
     * @return The file character set encoding.
     * @throws IOException
     */
    public static final String consumeBOM(InputStream in, String default_encoding) throws IOException
    {
        in.mark(3);

        // Determine file encoding...
        // ASCII - no header (use the supplied encoding)
        // UTF8  - EF BB BF
        // UTF16 - FF FE or FE FF (decoder chooses endian-ness)
        if (in.read() == 0xef && in.read() == 0xbb && in.read() == 0xbf)
        {
            // UTF-8 reader does not consume BOM, so do not reset
            if (System.getProperty("flex.platform.CLR") != null)
            {
                return "UTF8";
            }
            else
            {
                return UTF_8;
            }
        }
        else
        {
            in.reset();
            int b0 = in.read();
            int b1 = in.read();
            if (b0 == 0xff && b1 == 0xfe || b0 == 0xfe && b1 == 0xff)
            {
                in.reset();
                // UTF-16 reader will consume BOM
                if (System.getProperty("flex.platform.CLR") != null)
                {
                    return "UTF16";
                }
                else
                {
                    return UTF_16;
                }
            }
            else
            {
                // no BOM found
                in.reset();
                if (default_encoding != null && default_encoding.length() != 0)
                {
                    return default_encoding;
                }
                else
                {
                    return System.getProperty("file.encoding");
                }
            }
        }
    }

}
