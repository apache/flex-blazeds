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

public class Run extends Persistable
{
    public static final String TABLE_NAME = "Run";
    public final Build build;
    public String description;
    public long time;

    private Map clauses;
    private String[] tables = new String[]{TABLE_NAME};

    Run(Build build)
    {
        this.build = build;
    }

    public void load(MetricsDatabase database)
    {
        PreparedStatement statement = null;
        ResultSet rs = null;

        try
        {
            if (time > 0)
            {
                getClauses();
                clauses.put("runtime", new Long(time));
            }

            statement = get(database);

            if (time > 0)
                clauses.remove("runtime");

            rs = statement.executeQuery();
            rs.first(); //move to first row

            Object i = rs.getObject("id");
            if (i != null)
            {
                id = MetricsDatabase.getId(i);
                description = rs.getString("description");

                java.sql.Date sqlDate = rs.getDate("runtime");
                if (sqlDate != null)
                {
                    time = sqlDate.getTime();
                }
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
        return TABLE_NAME + ": " + build.getIdentity() + ", id: " + id;
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
        inserts.put("build_id", build);
        return inserts;
    }

    protected Map getUpdates()
    {
        HashMap updates = new HashMap();

        if (description != null)
            updates.put("description", description);

        if (time > 0)
            updates.put("runtime", new Long(time));

        return updates;
    }

    protected Map getClauses()
    {
        if (clauses == null)
        {
            clauses = new HashMap();
            clauses.put("build_id", build);
        }

        if (id >= 0)
            clauses.put("id", new Long(id));

        return clauses;
    }

}
