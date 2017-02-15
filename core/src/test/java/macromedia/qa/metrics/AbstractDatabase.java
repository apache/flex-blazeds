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

import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;

import java.util.Set;
import java.util.Iterator;
import java.util.Map;
import java.util.Calendar;

public abstract class AbstractDatabase
{
    protected AbstractDatabase()
    {
    }

    protected Connection connection;

    public PreparedStatement select(String[] columns, String[] tables, Map clauses, String other) throws SQLException
    {
        StringBuffer sb = new StringBuffer(256);
        sb.append("SELECT ");

        if (columns != null && columns.length > 0)
        {
            for (int i = 0; i < columns.length; i++)
            {
                sb.append(" ").append(columns[i]);

                if (i < columns.length - 1)
                    sb.append(", ");
            }

            sb.append("\r\n");
        }
        else
        {
            sb.append(" * \r\n");
        }

        if (tables != null && tables.length > 0)
        {
            sb.append("\tFROM ");

            for (int i = 0; i < tables.length; i++)
            {
                sb.append(" \"").append(tables[i]).append("\"");

                if (i < tables.length - 1)
                    sb.append(", ");
            }

            sb.append("\r\n");
        }

        if (clauses != null && clauses.size() > 0)
        {
            sb.append("\tWHERE \r\n");

            Set keys = clauses.keySet();
            Iterator kit = keys.iterator();
            int i = 0;

            while (kit.hasNext())
            {
                String key = (String)kit.next();

                if (i > 0)
                    sb.append("\t\tAND ");
                else
                    sb.append("\t\t");

                sb.append(" ").append(key).append(" =  ? \r\n");

                i++;
            }
        }

        if (other != null)
            sb.append(other);

        PreparedStatement statement = connection.prepareStatement(sb.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

        //WHERE (VALUES)
        if (clauses != null && clauses.size() > 0)
        {
            Set keys = clauses.keySet();
            Iterator kit = keys.iterator();
            int i = 1;

            while (kit.hasNext())
            {
                String key = (String)kit.next();
                Object value = clauses.get(key);
                addParameter(statement, i, value);
                i++;
            }
        }

        return statement;
    }


    /**
     * Creates a <code>PreparedStatement</code> for a SQL INSERT.
     *
     * e.g.
     * <pre>
     * INSERT INTO myTable ( field1, field2 )
     *     VALUES ( 'string1', 'string2' )
     *     WHERE field3 = 'string3'
     *         AND field4 = 'string4'
     * </pre>
     *
     * @param table   The table name to insert
     * @param values A collection of key-value pairs to insert, with column aliases appropriately formatted
     * @param clauses A collection of key-value pairs for the WHERE clause; the key must be a string and the value can be any valid ODBC object
     * @return A PreparedStatement that can be subsequently executed.
     * @throws SQLException
     */
    public PreparedStatement insert(String table, Map values, Map clauses, String other) throws SQLException
    {
        StringBuffer sb = new StringBuffer(256);
        sb.append("INSERT INTO ");

        //TABLE
        sb.append(table).append(" ( ");

        //COLUMNS (can include aliases if correctly formatted)
        if (values != null && values.size() > 0)
        {
            Set keys = values.keySet();
            Iterator kit = keys.iterator();
            int i = 0;

            while (kit.hasNext())
            {
                sb.append(" ").append(kit.next());

                if (i < values.size() - 1)
                    sb.append(", ");

                i++;
            }

            sb.append("\r\n");
        }

        sb.append(" ) \r\n");


        //VALUES
        if (values != null && values.size() > 0)
        {
            sb.append("\tVALUES ( ");

            Set keys = values.keySet();
            Iterator kit = keys.iterator();
            int i = 0;

            while (kit.hasNext())
            {
                kit.next();
                sb.append(" ? ");

                if (i < values.size() - 1)
                    sb.append(", ");

                i++;
            }

            sb.append(" ) \r\n");
        }

        //WHERE (KEYS)
        if (clauses != null && clauses.size() > 0)
        {
            sb.append("\tWHERE \r\n");

            Set keys = clauses.keySet();
            Iterator kit = keys.iterator();
            int i = 0;

            while (kit.hasNext())
            {
                String key = (String)kit.next();

                if (i > 0)
                    sb.append("\t\tAND ");
                else
                    sb.append("\t\t");

                sb.append(" ").append(key).append(" =  ? \r\n");

                i++;
            }
        }

        //ANY OTHER SQL COMMANDS TO ADD TO THE END OF THE STATEMENT
        if (other != null)
            sb.append(other);

        PreparedStatement statement = connection.prepareStatement(sb.toString());

        //VALUES (AS SQL PARAMETERS)
        if (values != null && values.size() > 0)
        {
            Set keys = values.keySet();
            Iterator kit = keys.iterator();
            int i = 1;

            while (kit.hasNext())
            {
                String key = (String)kit.next();
                Object value = values.get(key);
                addParameter(statement, i, value);
                i++;
            }
        }

        //WHERE (VALUES)
        if (clauses != null && clauses.size() > 0)
        {
            Set keys = clauses.keySet();
            Iterator kit = keys.iterator();
            int i = 1;

            while (kit.hasNext())
            {
                String key = (String)kit.next();
                Object value = clauses.get(key);
                addParameter(statement, i, value);
                i++;
            }
        }

        return statement;
    }

    /**
     * Creates a <code>PreparedStatement</code> for a SQL UPDATE.
     *
     * e.g.
     * <pre>
     * UPDATE Job
     *     SET Position = 'Manager'
     *     WHERE Employee_ID = 12345678
     *
     * </pre>
     *
     * @param table   The table name to update
     * @param updates A list of column names to select, with column aliases appropriately formatted
     * @param clauses A collection of key-value pairs, the key must be a string and the value can be any valid ODBC object
     * @return A PreparedStatement that can be subsequently executed.
     * @throws SQLException
     */
    public PreparedStatement update(String table, Map updates, Map clauses, String other) throws SQLException
    {
        StringBuffer sb = new StringBuffer(256);
        sb.append("UPDATE ");

        //TABLE
        sb.append(table).append(" \r\n");

        //SET (KEYS)
        if (updates != null && updates.size() > 0)
        {
            sb.append("\tSET \r\n");

            Set keys = updates.keySet();
            Iterator kit = keys.iterator();
            int i = 0;

            while (kit.hasNext())
            {
                String key = (String)kit.next();

                if (i > 0)
                    sb.append("\t\tAND ");
                else
                    sb.append("\t\t");

                sb.append(" ").append(key).append(" =  ? \r\n");

                i++;
            }
        }

        //WHERE (KEYS)
        if (clauses != null && clauses.size() > 0)
        {
            sb.append("\tWHERE \r\n");

            Set keys = clauses.keySet();
            Iterator kit = keys.iterator();
            int i = 0;

            while (kit.hasNext())
            {
                String key = (String)kit.next();

                if (i > 0)
                    sb.append("\t\tAND ");
                else
                    sb.append("\t\t");

                sb.append(" ").append(key).append(" =  ? \r\n");

                i++;
            }
        }


        //ANY OTHER SQL COMMANDS TO ADD TO THE END OF THE STATEMENT
        if (other != null)
            sb.append(other);

        PreparedStatement statement = connection.prepareStatement(sb.toString());

        //SET (VALUES)
        if (updates != null && updates.size() > 0)
        {
            Set keys = updates.keySet();
            Iterator kit = keys.iterator();
            int i = 1;

            while (kit.hasNext())
            {
                String key = (String)kit.next();
                Object value = updates.get(key);
                addParameter(statement, i, value);
                i++;
            }
        }

        //WHERE (VALUES)
        if (clauses != null && clauses.size() > 0)
        {
            Set keys = clauses.keySet();
            Iterator kit = keys.iterator();
            int i = 1;

            while (kit.hasNext())
            {
                String key = (String)kit.next();
                Object value = clauses.get(key);
                addParameter(statement, i, value);
                i++;
            }
        }

        return statement;
    }

    /**
     * Creates a <code>PreparedStatement</code> for a SQL DELETE.
     *
     * e.g.
     * <pre>
     * DELETE FROM Employee
     *     WHERE First_Name = 'John'
     *         AND Last_Name = 'Smith'
     * </pre>
     *
     * @param table   The name of the table from which to delete rows
     * @param clauses A collection of key-value pairs, the key must be a string and the value can be any valid ODBC object
     * @return A PreparedStatement that can be subsequently executed.
     * @throws SQLException
     */
    public PreparedStatement delete(String table, Map clauses, String other) throws SQLException
    {
        StringBuffer sb = new StringBuffer(256);
        sb.append("DELETE FROM ");

        //TABLE
        sb.append(table).append(" \r\n");

        //WHERE (KEYS)
        if (clauses != null && clauses.size() > 0)
        {
            sb.append("\tWHERE \r\n");

            Set keys = clauses.keySet();
            Iterator kit = keys.iterator();
            int i = 0;

            while (kit.hasNext())
            {
                String key = (String)kit.next();

                if (i > 0)
                    sb.append("\t\tAND ");
                else
                    sb.append("\t\t");

                sb.append(" ").append(key).append(" =  ? \r\n");

                i++;
            }
        }


        //ANY OTHER SQL COMMANDS TO ADD TO THE END OF THE STATEMENT
        if (other != null)
            sb.append(other);

        PreparedStatement statement = connection.prepareStatement(sb.toString());

        //WHERE (VALUES)
        if (clauses != null && clauses.size() > 0)
        {
            Set keys = clauses.keySet();
            Iterator kit = keys.iterator();
            int i = 1;

            while (kit.hasNext())
            {
                String key = (String)kit.next();
                Object value = clauses.get(key);
                addParameter(statement, i, value);
                i++;
            }
        }

        return statement;
    }

    public static long getId(Object val)
    {
        long id = -1;

        if (val != null)
        {
            if (val instanceof Number)
            {
                id = ((Number)val).longValue();
            }
            else
            {
                id = Long.parseLong(val.toString());
            }
        }

        return id;
    }


    public static int countRecords(ResultSet resultSet)
    {
        int rowCount = 0;

        //Determine rs size
        if (resultSet != null)
        {
            try
            {
                int currentIndex = resultSet.getRow();

                //Go to the end and get that row number
                if (resultSet.last())
                {
                    rowCount = resultSet.getRow();
                }

                //Put the cursor back
                if (currentIndex > 0)
                {
                    resultSet.absolute(currentIndex);
                }
                else
                {
                    resultSet.beforeFirst();
                }
            }
            catch (SQLException ex)
            {
                //TODO: Decide whether if absolute() not be supported, try first() as a last resort??
                try
                {
                    resultSet.first();
                }
                catch (SQLException se)
                {
                    //we won't try anymore.
                }
            }
        }

        return rowCount;
    }

    private void addParameter(PreparedStatement statement, int i, Object value) throws SQLException
    {
        if (value == null)
            statement.setObject(i, value);
        else if (value instanceof String)
            statement.setString(i, value.toString());
        else if (value instanceof Persistable)
            statement.setLong(i, ((Persistable)value).id);
        else if (value instanceof Boolean)
            statement.setBoolean(i, ((Boolean)value).booleanValue());
        else if (value instanceof Integer)
            statement.setInt(i, ((Integer)value).intValue());
        else if (value instanceof Double)
            statement.setDouble(i, ((Double)value).doubleValue());
        else if (value instanceof Float)
            statement.setFloat(i, ((Float)value).floatValue());
        else if (value instanceof Short)
            statement.setShort(i, ((Short)value).shortValue());
        else if (value instanceof Long)
            statement.setLong(i, ((Long)value).longValue());
        else if (value instanceof Calendar)
            statement.setDate(i, new java.sql.Date(((Calendar)value).getTime().getTime()));
        else if (value instanceof java.util.Date)
            statement.setDate(i, new java.sql.Date(((java.util.Date)value).getTime()));
        else if (value instanceof java.sql.Date)
            statement.setDate(i, ((java.sql.Date)value));
        else if (value instanceof java.sql.Timestamp)
            statement.setTimestamp(i, ((java.sql.Timestamp)value));
        else if (value instanceof java.sql.Time)
            statement.setTime(i, ((java.sql.Time)value));
        else
            statement.setObject(i, value);
    }

}
