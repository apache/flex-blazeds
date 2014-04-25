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

package macromedia.qa.metrics;

import macromedia.util.UnitTrace;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Metric extends Persistable
{
    public static final String TABLE_NAME = "Metric";

    public final String name;
    public String units;
    public String description;

    private Map clause;
    private String[] tables = new String[]{TABLE_NAME};

    Metric(String name)
    {
        this.name = name;
    }

    public void load(MetricsDatabase database)
    {
        PreparedStatement statement = null;
        ResultSet rs = null;

        try
        {
            statement = get(database);
            rs = statement.executeQuery();
            rs.first(); //move to first row

            Object i = rs.getObject("id");
            if (i != null)
            {
                id = MetricsDatabase.getId(i);
                units = rs.getString("units");
                description = rs.getString("description");
            }
            else
            {
                id = -1;
            }
        }
        catch (SQLException ex)
        {
            if (UnitTrace.errors)
                System.err.println("Error loading " + getIdentity() + ". " + ex == null ? "" : ex.getMessage());
        }
        finally
        {
            closeResultSet(rs);
            closeStatement(statement);
        }
    }

    public String getIdentity()
    {
        return TABLE_NAME + ": " + name + ", id: " + id;
    }

    public String getTableName()
    {
        return TABLE_NAME;
    }

    protected String[] getTables()
    {
        return tables;
    }

    protected Map getInserts()
    {
        Map inserts = getUpdates();
        inserts.put("name", name);
        return inserts;
    }

    protected Map getUpdates()
    {
        Map updates = new HashMap();

        if (units != null)
            updates.put("units", units);

        if (description != null)
            updates.put("description", description);

        return updates;
    }

    protected Map getClauses()
    {
        if (clause == null)
        {
            clause = new HashMap();
            clause.put("name", name);
        }

        if (id >= 0)
            clause.put("id", new Long(id));

        return clause;
    }

}
