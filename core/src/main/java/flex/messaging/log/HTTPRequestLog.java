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

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Enumeration;


/**
 * Static class to log HTTP request data when it is wrapped by our LoggingHttpServletRequestWrapper.
 */
public class HTTPRequestLog {
    /**
     * Request attribute for storing error info.
     */
    public static final String HTTP_ERROR_INFO = "org.apache.flex.blazeds.internal._exception_info";

    // Default file name
    private static String filename;

    /**
     * Called to set up filename for HTTP request logging.
     * If the init parameter <b>HttpErrorLog</b> is set in the servlet context, its value is set as the output filename
     *
     * @param context the ServletContext object
     * @return true if request logging is enabled.
     */
    public static boolean init(ServletContext context) {
        // Get the HttpRequest log file information.
        String logfile = context.getInitParameter("HttpErrorLog");
        if (logfile == null || logfile.length() == 0) {
            return false;
        }
        filename = logfile;
        return true;
    }

    /**
     * Change the output file name.
     *
     * @param fileName the file name
     */
    public static void setFileName(String fileName) {
        if (fileName != null) {
            filename = fileName;
        }
    }

    /**
     * Get the file name.
     *
     * @return String the file name
     */
    public static String getFileName() {
        return filename;
    }

    /**
     * Log the HTTP request if logging turned on and we have a filename.
     *
     * @param header  the header name
     * @param httpReq the HttpServletRequest
     */
    public static synchronized void outputRequest(String header, HttpServletRequest httpReq) {
        // If things aren't set up, do nothing.
        if (!(httpReq instanceof LoggingHttpServletRequestWrapper) || filename == null) {
            return;
        }

        LoggingHttpServletRequestWrapper req = (LoggingHttpServletRequestWrapper) httpReq;
        FileWriter fw = null;
        try {
            // Open the file
            fw = new FileWriter(filename, true);
            fw.write("#===== Request Client Infomation =====#" + "\n");
            if (header != null) {
                fw.write("Error             : " + header + "\n");
            }
            fw.write("Timestamp         : " + new Date(System.currentTimeMillis()).toString() + "\n");
            fw.write("Client IP Address : " + req.getRemoteAddr() + "\n");
            fw.write("Client FQDN       : " + req.getRemoteHost() + "\n");
            fw.write("Body size         : " + req.getContentLength() + "\n");

            fw.write("#===== HTTP Headers =====#" + "\n");
            outputHeaders(fw, req);

            fw.write("#===== HTTP Body =====#" + "\n");
            outputBody(fw, req);

            fw.close();
        } catch (IOException ex) {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e) { /* ignore */ }
            }
            System.out.println("Unable to write HTTP request data to file " + filename + ": " + ex.toString());
        }
    }

    /**
     * Output 1 line message (Auto file open).
     *
     * @param message the message to print
     */
    public static void outputPrint(String message) {
        FileWriter fw = null;
        try {
            fw = new FileWriter(filename, true);
            fw.write(message + "\n");
            fw.close();
        } catch (IOException ex) {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e) { /* ignore */ }
            }
            System.out.println("Unable to log message '" + message + "' to file " + filename + " : " + ex.toString());
        }
    }


    /**
     * Display the request header.
     */
    private static void outputHeaders(FileWriter fw, LoggingHttpServletRequestWrapper req) throws IOException {
        // Get the header
        Enumeration reqHeaderNum = req.getHeaderNames();

        // Do nothing if there is no header
        if (reqHeaderNum == null) {
            fw.write("No headers" + "\n");
            return;
        }

        // Repeat the header element
        while (reqHeaderNum.hasMoreElements()) {
            // Get the key
            String key = (String) reqHeaderNum.nextElement();

            // Output the header information
            outputHeaderElements(fw, req, key);
        }
    }

    /**
     * Output header information.
     */
    private static void outputHeaderElements(FileWriter fw, LoggingHttpServletRequestWrapper req, String key) throws IOException {
        // Output the header information
        Enumeration e = req.getHeaders(key);
        String keyname = key;

        while (e.hasMoreElements()) {
            fw.write(keyname + " : " + e.nextElement() + "\n");
            // Output key name only for the first time
            keyname = "        ";
        }
    }

    /**
     * Output the body information.
     */
    private static void outputBody(FileWriter fw, LoggingHttpServletRequestWrapper req) throws IOException {
        // Get the body size
        int leng = req.getContentLength();
        // Do nothing if there is no body
        if (leng <= 0) return;

        // Buffer to read
        byte rbuf[] = new byte[leng];

        // Put data
        InputStream in = req.getInputStream();
        if (in.read(rbuf, 0, leng) > 0) {
            // Output data
            outputBinary(fw, rbuf);
        }
    }

    /**
     * Output body information.
     */
    private static void outputBinary(FileWriter fw, byte buf[]) throws IOException {

        int adrs = 0;

        // Do every 16 bytes
        for (int j = 0; j < buf.length; j += 16) {

            // Change the number to hex.
            String adrsStr = "00000000" + Integer.toHexString(adrs);
            adrsStr = adrsStr.substring(adrsStr.length() - 8, adrsStr.length());
            fw.write("\n" + adrsStr + " : ");
            // Add address by 16
            adrs += 16;

            // Output in hex.
            for (int i = 0; i < 16; i++) {

                // If it is out of the limit, display in white space
                if (i + j >= buf.length) {
                    fw.write("   ");
                } else {
                    int n = (int) buf[i + j];
                    if (n < 0) n += 256;
                    // Change the data into hex
                    String s = "00" + Integer.toHexString(n);
                    s = s.substring(s.length() - 2, s.length());
                    fw.write(s + " ");
                }
            }

            // Output in string
            fw.write("    ");
            for (int i = 0; i < 16; i++) {

                // If it is out of limit, break.
                if (i + j >= buf.length) break;

                // If it is a special character, output "."
                if (buf[i + j] < 0x20 ||
                        (buf[i + j] & 0xff) > 0xde ||
                        (buf[i + j] > 0x7e && (buf[i + j] & 0xff) < 0xa1)) {
                    fw.write(".");
                }
                // Output string
                else {
                    String s = String.valueOf((char) buf[i + j]);
                    fw.write(s);
                }
            }
        }
        fw.write("\n");
    }
}