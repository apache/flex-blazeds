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
package qa.messaging.flook;

/**
 * Created by IntelliJ IDEA.
 * User: truggles
 * Date: Jul 28, 2005
 * Time: 7:16:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class FLookEmail implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public String to;
    public String from;
    public String subject;
    public String message;
    public Object[] attachments;

    public FLookEmail() {
        this.attachments = new Object[0];
    }

    public FLookEmail(String to, String from,  String subject, String message, Object[] attachments) {

        this.to = to;
        this.from = from;
        this.subject = subject;
        this.message = message;

        if (attachments != null)
            this.attachments = attachments;
        else
            this.attachments = new Object[0];
    }

    public String toString() {
        return  "To:       " + to + "\n" +
                "From:     " + from + "\n" +
                "Subject:  " + subject + "\n" +
                "Attachments:" + attachments.length;
    }

    public String completeToString() {
        return  "===========================================\n" +
                this.toString() + "\n" +
                "-------------------------------------------\n" +
                message + "\n" +
                "===========================================";
    }
}
