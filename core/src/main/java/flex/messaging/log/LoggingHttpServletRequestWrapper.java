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

package flex.messaging.log;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Theis class wraps the servlet request so we can capture the body bytes
 * and log them if an unexpected error occurs in the request. 
 */
public class LoggingHttpServletRequestWrapper extends HttpServletRequestWrapper
{
    /**
     * Body data.
     */
    private byte[] _bodyInfoData;

    /**
     * Constructor.
     * @param parent the parent HttpServletRequest to wrap
     */
    public LoggingHttpServletRequestWrapper(HttpServletRequest parent)
    {
        super(parent);
    }

    /**
     * Read the body and store it.
     *
     * @throws IOException if there is a problem reading the request data
     */
    public void doReadBody() throws IOException
    {

        // Get length of the content
        int length = super.getContentLength();

        if (length > 0)
        {
            // Instantiate ByteArrayOutputStream
            ByteArrayOutputStream out = new ByteArrayOutputStream(length);
            InputStream in = super.getInputStream();
            // Create for body of the message
            byte[] bodyByte = new byte[length];

            // -------------------------------
            // To process the body message
            // -------------------------------
            int remain = length;
            while (remain > 0)
            {
                // Keep the data
                int readLen = in.read(bodyByte);
                if (readLen <= 0)
                {
                    break;
                }
                // Write the body information
                out.write(bodyByte, 0, readLen);
                remain -= readLen;
            }

            // Store the data
            this._bodyInfoData = out.toByteArray();

            // Release
            out.close();
        }
    }

    /**
     * Returns an input stream backed by the saved body data.
     * @return ServletInputStream the Servlet input stream object
     * @throws IOException if we can not get the Servlet input stream
     */
    public ServletInputStream getInputStream() throws IOException
    {
        if (this._bodyInfoData != null)
        {
            return new ExServletInputStream(new ByteArrayInputStream(this._bodyInfoData));
        }
        return super.getInputStream();
    }


    /**
     * An extension of the <tt>ServletInputStream</tt> that is backed by an input stream
     * provided at construction.
     * <p>
     * Used to allow the servlet request wrapper to return a stream backed by the already consumed body data.</p>
     */
    private static class ExServletInputStream extends ServletInputStream
    {
        /**
         * InputStream Object.
         */
        private InputStream _inputStream;

        /**
         * Constructor.
         * @param inputStream the input stream
         */
        ExServletInputStream(InputStream inputStream)
        {
            this._inputStream = inputStream;
        }


        // --------------------------------
        // The following methods are overridden.
        // --------------------------------
        public int readLine(byte[] b, int off, int len) throws IOException
        {
            throw new UnsupportedOperationException("This method is not extended");
        }

        public int read(byte[] b, int off, int len) throws IOException
        {
            return this._inputStream.read(b, off, len);
        }

        public int read(byte[] b) throws IOException
        {
            return this._inputStream.read(b);
        }

        public void mark(int readlimit)
        {
            this._inputStream.mark(readlimit);
        }

        public long skip(long n) throws IOException
        {
            return this._inputStream.skip(n);
        }

        public void reset() throws IOException
        {
            this._inputStream.reset();
        }

        public int read() throws IOException
        {
            return this._inputStream.read();
        }

        public boolean markSupported()
        {
            return _inputStream.markSupported();
        }

        public void close() throws IOException
        {
            this._inputStream.close();
        }

        public int available() throws IOException
        {
            return this._inputStream.available();
        }
        // --------------------------------
        // This is the end of the modification.
        // --------------------------------
    } // end inner class

} // end class
