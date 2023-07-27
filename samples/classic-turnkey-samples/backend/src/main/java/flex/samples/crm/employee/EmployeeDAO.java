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
package flex.samples.crm.employee;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import flex.samples.ConnectionHelper;
import flex.samples.DAOException;
import flex.samples.crm.ConcurrencyException;
import flex.samples.crm.company.Company;

public class EmployeeDAO
{
	public List getEmployees() throws DAOException
	{
		List list = new ArrayList();
		Connection c = null;
		try
		{
			c = ConnectionHelper.getConnection();
			Statement s = c.createStatement();
			ResultSet rs = s.executeQuery("SELECT * FROM employee ORDER BY last_name");
			Employee employee;
			while (rs.next())
			{
				employee = new Employee();
				employee.setEmployeeId(rs.getInt("employee_id"));
				employee.setFirstName(rs.getString("first_name"));
				employee.setLastName(rs.getString("last_name"));
				employee.setTitle(rs.getString("title"));
				employee.setEmail(rs.getString("email"));
				employee.setPhone(rs.getString("phone"));
                Company company = new Company();
                company.setCompanyId(rs.getInt("company_id"));
                employee.setCompany(company);
				list.add(employee);
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new DAOException(e);
		}
		finally
		{
			ConnectionHelper.close(c);
		}
		return list;
	}

	public List findEmployeesByCompany(Integer companyId) throws DAOException
	{
		List list = new ArrayList();
		Connection c = null;
		try
		{
            Company company = new Company();
            company.setCompanyId(companyId.intValue());
			c = ConnectionHelper.getConnection();
			PreparedStatement ps = c.prepareStatement("SELECT * FROM employee WHERE company_id = ? ORDER BY last_name");
		    ps.setInt(1, companyId.intValue());
            ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				Employee employee = new Employee();
				employee.setEmployeeId(rs.getInt("employee_id"));
				employee.setFirstName(rs.getString("first_name"));
				employee.setLastName(rs.getString("last_name"));
				employee.setTitle(rs.getString("title"));
				employee.setEmail(rs.getString("email"));
				employee.setPhone(rs.getString("phone"));
                employee.setCompany(company);
				list.add(employee);
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new DAOException(e);
		}
		finally
		{
			ConnectionHelper.close(c);
		}
		return list;
	}

    public List findEmployeesByName(String name) throws DAOException
    {
        List list = new ArrayList();
        Connection c = null;
        
        try
        {
            c = ConnectionHelper.getConnection();
            PreparedStatement ps = c.prepareStatement("SELECT * FROM employee WHERE first_name LIKE ? OR last_name LIKE ? ORDER BY last_name");
            ps.setString(1, "%" + name + "%");
            ps.setString(2, "%" + name + "%");
            ResultSet rs = ps.executeQuery();

            Employee employee;
            while (rs.next())
            {
                employee = new Employee();
                employee.setEmployeeId(rs.getInt("employee_id"));
                employee.setFirstName(rs.getString("first_name"));
                employee.setLastName(rs.getString("last_name"));
                employee.setTitle(rs.getString("title"));
                employee.setEmail(rs.getString("email"));
                employee.setPhone(rs.getString("phone"));
                Company company = new Company();
                company.setCompanyId(rs.getInt("company_id"));

                list.add(employee);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            throw new DAOException(e);
        }
        finally
        {
            ConnectionHelper.close(c);
        }
        return list;
    }

	public Employee getEmployee(int employeeId) throws DAOException
	{
		Employee employee = null;
		Connection c = null;
        
		try
		{
			c = ConnectionHelper.getConnection();
            PreparedStatement ps = c.prepareStatement("SELECT * FROM employee WHERE employee_id= ?");
            ps.setInt(1, employeeId);
            ResultSet rs = ps.executeQuery();
            
			if (rs.next())
			{
				employee = new Employee();
				employee.setEmployeeId(rs.getInt("employee_id"));
				employee.setFirstName(rs.getString("first_name"));
				employee.setLastName(rs.getString("last_name"));
				employee.setTitle(rs.getString("title"));
				employee.setEmail(rs.getString("email"));
				employee.setPhone(rs.getString("phone"));
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new DAOException(e.getMessage());
		}
		finally
		{
			ConnectionHelper.close(c);
		}
		return employee;
	}

	public Employee createEmployee(Employee employee) throws DAOException
	{
		Connection c = null;
        PreparedStatement ps = null;
		try
		{
			c = ConnectionHelper.getConnection();
			ps = c.prepareStatement("INSERT INTO employee (first_name, last_name, title, email, phone, company_id) VALUES (?, ?, ?, ?, ?, ?)");
			ps.setString(1, employee.getFirstName());
			ps.setString(2, employee.getLastName());
			ps.setString(3, employee.getTitle());
			ps.setString(4, employee.getEmail());
			ps.setString(5, employee.getPhone());
            if (employee.getCompany() != null)
                ps.setInt(6, employee.getCompany().getCompanyId());
            else
            	ps.setNull(6, Types.INTEGER);                
			ps.execute();
            ps.close();
			Statement s = c.createStatement();
			// HSQLDB Syntax to get the identity (employee_id) of inserted row
			ResultSet rs = s.executeQuery("CALL IDENTITY()");
			rs.next();
            // Update the id in the returned object.  This is important as this
            // value must get returned to the client.
			employee.setEmployeeId(rs.getInt(1));
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new DAOException(e);
		}
		finally
		{
			ConnectionHelper.close(c);
		}
		return employee;
	}


	public void updateEmployee(Employee newVersion, Employee previousVersion, List changes) throws DAOException, ConcurrencyException
	{
		Connection c = null;
		try
		{
			c = ConnectionHelper.getConnection();
            PreparedStatement ps = c.prepareStatement("UPDATE employee SET first_name=?, last_name=?, title=?, email=?, phone=?, company_id=? WHERE employee_id=? AND first_name=? AND last_name=? AND title=? AND email=? AND phone=? AND company_id=?");
			ps.setString(1, newVersion.getFirstName());
			ps.setString(2, newVersion.getLastName());
			ps.setString(3, newVersion.getTitle());
			ps.setString(4, newVersion.getEmail());
			ps.setString(5, newVersion.getPhone());			
            if (newVersion.getCompany() != null)
                ps.setInt(6, newVersion.getCompany().getCompanyId());
            else
            	ps.setNull(6,Types.INTEGER);                
            ps.setInt(7, newVersion.getEmployeeId());
			ps.setString(8, previousVersion.getFirstName());
			ps.setString(9, previousVersion.getLastName());
			ps.setString(10, previousVersion.getTitle());
			ps.setString(11, previousVersion.getEmail());
			ps.setString(12, previousVersion.getPhone());
            if (previousVersion.getCompany() != null)
                ps.setInt(13, previousVersion.getCompany().getCompanyId());
            else
            	ps.setNull(13, Types.INTEGER);                
			if (ps.executeUpdate() == 0)
			{
				throw new ConcurrencyException("Item not found");
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new DAOException(e.getMessage());
		}
		finally
		{
			ConnectionHelper.close(c);
		}
	}

	public void deleteEmployee(Employee employee) throws DAOException, ConcurrencyException
	{
		Connection c = null;
		try
		{
			c = ConnectionHelper.getConnection();
			PreparedStatement ps = c.prepareStatement("DELETE FROM employee WHERE employee_id=? AND first_name=? AND last_name=? AND title=? AND email=? AND phone=? AND company_id=?");
			ps.setInt(1, employee.getEmployeeId());
			ps.setString(2, employee.getFirstName());
			ps.setString(3, employee.getLastName());
			ps.setString(4, employee.getTitle());
			ps.setString(5, employee.getEmail());
			ps.setString(6, employee.getPhone());
            if (employee.getCompany() != null)
                ps.setInt(7, employee.getCompany().getCompanyId());
            else
            	ps.setNull(7, Types.INTEGER);                
			if (ps.executeUpdate() == 0)
			{
				throw new ConcurrencyException("Item not found");
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new DAOException(e.getMessage());
		}
		finally
		{
			ConnectionHelper.close(c);
		}
	}
	
}
