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

package flex.messaging.services.remoting;

import java.sql.SQLException;
import java.util.Map;

import javax.sql.RowSet;

import flex.messaging.FlexContext;
import flex.messaging.FlexSession;
import flex.messaging.io.PageableRowSet;
import flex.messaging.io.PagedRowSet;

/**
 * A special RemotingService destination that caches a PageableRowSet
 * in the current FlexSession to provide legacy paging functionality for 
 * the deprecated client RecordSet API.
 * 
 * To create a PageableRowSet, a javax.sql.RowSet must be provided instead
 * of a java.sql.ResultSet because the data has to be disconnected and 
 * cacheable.
 * 
 * The RemotingService's JavaAdapter is expected to manage this instance.
 *
 * @see flex.messaging.io.PageableRowSet
 */
public class PageableRowSetCache
{
    private static final int DEFAULT_PAGE_SIZE = 25;
    
    /**
     * A default constructor is required for a RemotingService source.
     */
    public PageableRowSetCache()
    {
    }

    /**
     * Converts a RowSet into a PageableRowSet, stores the PageableRowSet
     * result in the cache and then returns the PageableRowSet. A unique
     * id will be created for the PagedRowSet.
     * 
     * @param rowset RowSet to be cached for paged access.
     * @param pageSize Size of the first page to be sent to the client. Default 
     * is 25 if pageSize is set to 0 or less. If all records should be sent at once
     * consider not caching the RowSet or specify Integer.MAX_VALUE.
     * 
     * @return resulting <tt>PageableRowSet</tt> of the <tt>RowSet</tt> conversion 
     */
    public static PageableRowSet cacheRowSet(RowSet rowset, int pageSize)
    {
        if (pageSize <= 0)
            pageSize = DEFAULT_PAGE_SIZE;

        PageableRowSet prs = new PagedRowSet(rowset, pageSize, true);
        cachePageableRowSet(prs);
        return prs;
    }

    /**
     * Stores the PageableRowSet result in the session to act as a cache
     * for the legacy client RecordSet paging feature.
     * 
     * @param rowset PageableRowSet to be cached for paged access.
     */
    public static void cachePageableRowSet(PageableRowSet rowset)
    {
        if (rowset != null)
        {
            FlexSession session = FlexContext.getFlexSession();
            session.setAttribute(rowset.getID(), rowset);
        }
    }

    /**
     * Get a subset of records that are cached for the given PageableRowSet id.
     *
     * @param id    The PageableRowSet's id, used to locate it in the current session.
     * @param startIndex    The absolute position for the record set cursor.
     * @param count    The size of the page of results to return.
     * @return Map    The resulting sub-set of data or 'page' requested.
     * @see PageableRowSet#getRecords(int, int)
     * 
     * @throws SQLException if an exception occurs while reading the <tt>RowSet</tt>
     */
    public Map getRecords(String id, int startIndex, int count) throws SQLException
    {
        Map page = null;
        FlexSession session = FlexContext.getFlexSession();

        if (session != null)
        {
            Object o = session.getAttribute(id);

            if (o != null && o instanceof PageableRowSet)
            {
                PageableRowSet rs = (PageableRowSet) o;
                page = rs.getRecords(startIndex, count);
            }
        }

        return page;
    }

    /**
     * Remove a PageableRowSet from the current session.
     *
     * @param id The id of the PageableRowSet to remove from the current session.
     */
    public void release(String id)
    {
        FlexSession session = FlexContext.getFlexSession();

        if (session != null)
        {
            session.removeAttribute(id);
        }
    }
}
