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
package flex.samples;

import java.sql.Connection;
import java.sql.SQLException;

import flex.messaging.config.ConfigMap;
import flex.messaging.services.AbstractBootstrapService;

public class DatabaseCheckService extends AbstractBootstrapService
{
    public void initialize(String id, ConfigMap properties)
    {
    	Connection c = null;
    	try 
    	{	
    		// Check that the database is running...
    		c = ConnectionHelper.getConnection();
    		// ... if yes return
    		return;
    	}
    	catch (SQLException e)
    	{
    		System.out.println("******************************************************************************");
    		System.out.println("*                                                                            *");
    		System.out.println("*  Unable to connect to the samples database.                                *");
    		System.out.println("*  You must start the samples database before you can run the samples.       *");
    		System.out.println("*  To start the samples database:                                            *");
    		System.out.println("*    1. Open a command prompt and go to the {install-dir}/sampledb dir       *");
    		System.out.println("*    2. Run startdb.bat (Windows) or startdb.sh (Unix-based systems)         *");
    		System.out.println("*                                                                            *");
    		System.out.println("******************************************************************************");
    	} 
    	finally
    	{
    		ConnectionHelper.close(c);
    	}
    	
    }

    public void start()
    {
    }


    public void stop()
    {
    }

}
