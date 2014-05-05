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
import java.sql.Statement;
import java.util.Map;

public abstract class Persistable
{
    protected long id = -1;

    public boolean exists(MetricsDatabase database) throws SQLException
    {
        if (database != null)
        {
            PreparedStatement statement = null;
            ResultSet rs = null;

            try
            {
                int count = 0;
                statement = get(database);
                rs = statement.executeQuery();

                count = MetricsDatabase.countRecords(rs);

                if (count > 0)
                {
                    rs.first();
                    Object val = rs.getObject("id");
                    if (val != null)
                    {
                        id = MetricsDatabase.getId(val);
                        return id >= 0;
                    }
                }
            }
            catch (SQLException ex)
            {
                if (UnitTrace.errors)
                    System.err.println("Error checking if " + getIdentity() + " exists." + ex == null ? "" : ex.getMessage());

                throw ex;
            }
            finally
            {
                closeResultSet(rs);
                closeStatement(statement);
            }
        }

        return false;
    }

    protected PreparedStatement get(MetricsDatabase database) throws SQLException
    {
        PreparedStatement statement = null;
        statement = database.select(null, getTables(), getClauses(), null);
        return statement;
    }

    protected void closeResultSet(ResultSet rs)
    {
        if (rs != null)
        {
            try
            {
                rs.close();
            }
            catch (Exception ex)
            {
            }
        }
    }

    protected void closeStatement(Statement stmt)
    {
        if (stmt != null)
        {
            try
            {
                stmt.close();
            }
            catch (Exception ex)
            {
            }
        }
    }


    public void save(MetricsDatabase database) throws SQLException
    {
        PreparedStatement statement = null;

        try
        {
            if (exists(database))
            {
                statement = database.update(getTableName(), getUpdates(), getClauses(), null);
                statement.executeUpdate();
            }
            else
            {
                statement = database.insert(getTableName(), getInserts(), null, null);
                statement.executeUpdate();
                load(database);
            }
        }
        catch (SQLException ex)
        {
            if (UnitTrace.errors)
                System.err.println("Error saving " + getIdentity() + ". " + ex == null ? "" : ex.getMessage());

            throw ex;
        }
        finally
        {
            closeStatement(statement);
        }
    }

    public void insert(MetricsDatabase database) throws SQLException
    {
        PreparedStatement statement = null;

        try
        {
            statement = database.insert(getTableName(), getInserts(), null, null);
            statement.executeUpdate();
            load(database);
        }
        catch (SQLException ex)
        {
            if (UnitTrace.errors)
                System.err.println("Error saving " + getIdentity() + ". " + ex == null ? "" : ex.getMessage());

            throw ex;
        }
        finally
        {
            closeStatement(statement);
        }
    }

    public abstract void load(MetricsDatabase database);

    public abstract String getTableName();

    public abstract String getIdentity();

    protected abstract String[] getTables();

    protected abstract Map getInserts();

    protected abstract Map getUpdates();

    protected abstract Map getClauses();
}
