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
package flex.samples.dcd.product;

import java.util.ArrayList;
import java.util.List;
import java.sql.*;
import flex.samples.ConnectionHelper;
import flex.samples.DAOException;
import java.util.Iterator;

public class ProductService {

    public Product[] getProducts() throws DAOException {

        List list = new ArrayList();
        Connection c = null;

        try {
            c = ConnectionHelper.getConnection();
            Statement s = c.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM product ORDER BY name");
            while (rs.next()) {
                list.add(new Product(rs.getInt("product_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("image"), 
                        rs.getString("category"), 
                        rs.getDouble("price"),
                        rs.getInt("qty_in_stock")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DAOException(e);
        } finally {
            ConnectionHelper.close(c);
        }
        Product[] products = new Product[list.size()];
        Iterator i = list.iterator(); 
        int index = 0;
        while(i.hasNext()) {
            products[index] = (Product)i.next();
            index++;
        }
        return products;
    }

    public List getProductsByName(String name) throws DAOException {
        
        List list = new ArrayList();
        Connection c = null;
        
        try {
            c = ConnectionHelper.getConnection();
            PreparedStatement ps = c.prepareStatement("SELECT * FROM product WHERE UPPER(name) LIKE ? ORDER BY name");
            ps.setString(1, "%" + name.toUpperCase() + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Product(rs.getInt("product_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("image"), 
                        rs.getString("category"), 
                        rs.getDouble("price"),
                        rs.getInt("qty_in_stock")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DAOException(e);
        } finally {
            ConnectionHelper.close(c);
        }
        return list;
        
    }
    
    public Product getProduct(int productId) throws DAOException {

        Product product = new Product();
        Connection c = null;

        try {
            c = ConnectionHelper.getConnection();
            PreparedStatement ps = c.prepareStatement("SELECT * FROM product WHERE product_id=?");
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                product = new Product();
                product.setProductId(rs.getInt("product_id"));
                product.setName(rs.getString("name"));
                product.setDescription(rs.getString("description"));
                product.setImage(rs.getString("image")); 
                product.setCategory(rs.getString("category")); 
                product.setPrice(rs.getDouble("price"));
                product.setQtyInStock(rs.getInt("qty_in_stock"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new DAOException(e);
        } finally {
            ConnectionHelper.close(c);
        }
        return product;
    }

    public Product createProduct(Product product) throws DAOException {
        
        Connection c = null;
        PreparedStatement ps = null;
        try {
            c = ConnectionHelper.getConnection();
            ps = c.prepareStatement("INSERT INTO product (name, description, image, category, price, qty_in_stock) VALUES (?, ?, ?, ?, ?, ?)");
            ps.setString(1, product.getName());
            ps.setString(2, product.getDescription());
            ps.setString(3, product.getImage());
            ps.setString(4, product.getCategory());
            ps.setDouble(5, product.getPrice());
            ps.setInt(6, product.getQtyInStock());
            ps.executeUpdate();
            Statement s = c.createStatement();
            // HSQLDB Syntax to get the identity (company_id) of inserted row
            ResultSet rs = s.executeQuery("CALL IDENTITY()");
            // MySQL Syntax to get the identity (product_id) of inserted row
            // ResultSet rs = s.executeQuery("SELECT LAST_INSERT_ID()");
            rs.next();
            // Update the id in the returned object. This is important as this value must get returned to the client.
            product.setProductId(rs.getInt(1));
        } catch (Exception e) {
            e.printStackTrace();
            throw new DAOException(e);
        } finally {
            ConnectionHelper.close(c);
        }
        return product;
    }

    public void updateProduct(Product product) throws DAOException {

        Connection c = null;

        try {
            c = ConnectionHelper.getConnection();
            PreparedStatement ps = c.prepareStatement("UPDATE product SET name=?, description=?, image=?, category=?, price=?, qty_in_stock=? WHERE product_id=?");
            ps.setString(1, product.getName());
            ps.setString(2, product.getDescription());
            ps.setString(3, product.getImage());
            ps.setString(4, product.getCategory());
            ps.setDouble(5, product.getPrice());
            ps.setInt(6, product.getQtyInStock());
            ps.setInt(7, product.getProductId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DAOException(e);
        } finally {
            ConnectionHelper.close(c);
        }

    }

    private boolean remove(Product product) throws DAOException {
        
        Connection c = null;
        
        try {
            c = ConnectionHelper.getConnection();
            PreparedStatement ps = c.prepareStatement("DELETE FROM product WHERE product_id=?");
            ps.setInt(1, product.getProductId());
            int count = ps.executeUpdate();
            return (count == 1);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DAOException(e);
        } finally {
            ConnectionHelper.close(c);
        }
    }

    public boolean deleteProduct(Product product) throws DAOException {
        return remove(product);
    }

}