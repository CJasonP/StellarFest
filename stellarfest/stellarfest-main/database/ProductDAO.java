package database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import models.Product;

public class ProductDAO {
	private Connect connect = Connect.getInstance();
	
	public ArrayList<Product> getAllProducts(String userId) {
        ArrayList<Product> productList = new ArrayList<>();
        String query = "SELECT Product_ID, Product_Name, Product_Description FROM Products WHERE User_ID = ?";
        
        try (PreparedStatement pstmt = connect.preparedStatement(query)) {
            pstmt.setString(1, userId);  // Set the userId parameter
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String productId = rs.getString("Product_ID");
                String productName = rs.getString("Product_Name");
                String productDescription = rs.getString("Product_Description");
                
                productList.add(new Product(productId, userId, productName, productDescription));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return productList;
    }
	
	public String generateNewProductID() {
	    String query = "SELECT Product_ID FROM Products ORDER BY Product_ID DESC LIMIT 1";
	    PreparedStatement ps = connect.preparedStatement(query);
	    
	    try {
	        ResultSet rs = ps.executeQuery();
	        if (rs.next()) {
	            String lastID = rs.getString("Product_ID");
	            int newID = Integer.parseInt(lastID.substring(3)) + 1;
	            return "PR" + String.format("%03d", newID);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return "PR001";
	}

	public void createProduct(String productID, String userID, String productName, String productDescription) {
		String query = "INSERT INTO Products VALUES (?, ?, ?, ?)";
	    System.out.println("ProductID" + productID);
	    System.out.println("userID" + userID);
	    System.out.println("ProductName" + productName);
	    System.out.println("ProductDescription" + productDescription);
	    try (PreparedStatement ps = connect.preparedStatement(query)) {
	        ps.setString(1, productID);
	        ps.setString(2, userID);
	        ps.setString(3, productName);
	        ps.setString(4, productDescription);
	        
	        int rowsAffected = ps.executeUpdate();
	        if (rowsAffected > 0) {
	            System.out.println("Product created successfully!");
	        } else {
	            System.out.println("Failed to create product.");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public void updateProduct(String productID, String productName, String productDescription) {
	    String query = "UPDATE Products SET Product_Name = ?, Product_Description = ? WHERE Product_ID = ?";
	    
	    try (PreparedStatement ps = connect.preparedStatement(query)) {
	        ps.setString(1, productName);
	        ps.setString(2, productDescription);
	        ps.setString(3, productID);
	        
	        int rowsAffected = ps.executeUpdate();
	        if (rowsAffected > 0) {
	            System.out.println("Product updated successfully!");
	        } else {
	            System.out.println("No product found with the given ID.");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public void deleteProduct(String productID) {
	    String query = "DELETE FROM Products WHERE Product_ID = ?";
	    
	    try (PreparedStatement ps = connect.preparedStatement(query)) {
	        ps.setString(1, productID);
	        
	        int rowsAffected = ps.executeUpdate();
	        if (rowsAffected > 0) {
	            System.out.println("Product deleted successfully!");
	        } else {
	            System.out.println("No product found with the given ID.");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
}
