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

import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MetricsDatabase extends AbstractDatabase
{
    private Properties properties;

    private static final String DEFAULT_HOST = "10.1.144.66";
    private static final String DEFAULT_PORT = "1433";
    private static final String DEFAULT_DRIVER = "sun.jdbc.odbc.JdbcOdbcDriver";
    private static final String DEFAULT_URLBASE = "jdbc:odbc:";
    private static final String DEFAULT_DATASOURCE = "master";

    public MetricsDatabase()
    {
        init(null);
    }

    public MetricsDatabase(Properties props)
    {
        properties = props;
        init(null);
    }

    public MetricsDatabase(File f)
    {
        init(f);
    }

    private void init(File f)
    {
        if (f != null)
            loadProperties(f);

        if (properties == null)
            properties = new Properties();

        try
        {
            Class.forName(getDriver()).newInstance();
            connection = getConnection();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException
    {
        if (connection == null)
            connection = DriverManager.getConnection(getConnectionURL(), getConnectionProperties());

        return connection;
    }

    public void dispose()
    {
        try
        {
            connection.close();
            connection = null;
        }
        catch (Exception ex)
        {
        }
    }

    public void loadProperties(File f)
    {
        try
        {
            FileInputStream fis = new FileInputStream(f);
            properties = new Properties();
            properties.load(fis);
        }
        catch (FileNotFoundException ex)
        {
            System.err.println();
            System.err.println("Could not find database properties file " + f.getAbsolutePath());
            System.err.println("\t" + ex.getMessage());
        }
        catch (IOException ioe)
        {
            System.err.println();
            System.err.println("Error reading database properties file " + f.getAbsolutePath());
            System.err.println("\t" + ioe.getMessage());
        }

    }

    String getDriver()
    {
        String driver = properties.getProperty("driver");
        if (driver == null)
            driver = DEFAULT_DRIVER;
        else
            driver = driver.trim();

        return driver;
    }

    String getHost()
    {
        String host = properties.getProperty("host");
        if (host == null)
            host = DEFAULT_HOST;
        else
            host = host.trim();

        return host;
    }

    String getPort()
    {
        String port = properties.getProperty("port");
        if (port == null)
            port = DEFAULT_PORT;
        else
            port = port.trim();

        return port;
    }

    String getAccount()
    {
        String account = properties.getProperty("account");
        if (account != null)
            account = account.trim();

        return account;
    }

    String getPassword()
    {
        String pass = properties.getProperty("password");
        if (pass != null)
            pass = pass.trim();

        return pass;
    }


    String getDatabase()
    {
        String db = properties.getProperty("database");
        if (db != null)
            db = db.trim();

        return db;
    }

    String getDatasource()
    {
        String ds = properties.getProperty("datasource");
        if (ds != null)
            ds = ds.trim();

        return ds;
    }

    String getURLBase()
    {
        String urlb = properties.getProperty("urlbase");
        if (urlb == null)
            urlb = DEFAULT_URLBASE;

        return urlb;
    }

    String getConnectionURL()
    {
        StringBuffer sb = new StringBuffer();
        String urlb = getURLBase();

        if (DEFAULT_URLBASE.equals(urlb))
        {
            String odbc = getDatasource();
            if (odbc == null)
                odbc = DEFAULT_DATASOURCE;
            else
                odbc = odbc.trim();

            sb.append(urlb);

            if (!urlb.endsWith(":"))
                sb.append(":");

            sb.append(odbc);
        }
        else
        {
            sb.append(urlb).append(getHost()).append(":").append(getPort());
        }

        return sb.toString();
    }

    private Properties getConnectionProperties()
    {
        Properties connectionProps = new Properties();

        if (getDatabase() != null)
            connectionProps.setProperty("databaseName", getDatabase());

        connectionProps.setProperty("user", getAccount());
        connectionProps.setProperty("password", getPassword());

        return connectionProps;
    }
}

