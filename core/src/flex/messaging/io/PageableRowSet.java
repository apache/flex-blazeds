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
package flex.messaging.io;

import java.sql.SQLException;
import java.util.Map;

/**
 * Implementations of this class are recoginized by the serialization filter
 * as result sets that are pageable. A pageable result set is a server side
 * cache of a query result (that implements java.sql.ResultSet) and is
 * typically stored in the session object. Users can request a subset of
 * data from the object given that they know it's id.
 *
 * @see javax.sql.RowSet
 */
public interface PageableRowSet
{
    /**
     * Constants for getRecords map keys.
     */
    String PAGE = "Page";
    String CURSOR = "Cursor";

    /**
     * List the column names of the result set.
     *
     * @return String[] An array of the column names as strings, as ordered
     *         by the result set provider's column number assignment.
     */
    String[] getColumnNames() throws SQLException;

    /**
     * Use this method to get a subset of records.
     * A map is returned with two fields, the first being the
     * row number the data page started from, and the second
     * being the array of arrays for the actual data page.
     * @param startIndex the start index of the records
     * @param count the total count
     * @return Map Contains two fields, the page's row index and the actual data array.
     */
    Map getRecords(int startIndex, int count) throws SQLException;

    /**
     * Get the total number of rows in the result set
     * @return int The total number of rows in the result set.
     */
    int getRowCount();

    /**
     * If this function returns a number &gt;= the total number of records in the recordset,
     * then the recordset should be simply returned to the client in full. However,
     * if it is &lt; the total size, then this object itself is saved in Session data,
     * and tagged with a unique ID.
     * @return int the initial download count
     */
    int getInitialDownloadCount();


    /**
     * Get the paged result ID.
     * @return String This paged result's (universally unique) id.
     */
    String getID();

    /**
     * Get the name of the service that manages the pages
     * @return String The name of the service that will manage this paged result.
     */
    String getServiceName();

    /**
     * Set the name of the service that manages the pages.
     * @param serviceName Update the name of the service that manages the pages for this query.
     */
    void setServicename(String serviceName);
}
