package database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;

import models.Vendor;

public class VendorDAO {
	private Connect connect = Connect.getInstance();

	public ArrayList<Vendor> getAllVendor() {
	    ArrayList<Vendor> vendorList = new ArrayList<>();
	    String query = "SELECT User_ID, User_name, User_Email, User_Password, User_Role FROM Users WHERE User_Role = 'Vendor'";  // SQL query to filter by 'Vendor' role and select specific columns
	    
	    try (PreparedStatement ps = connect.preparedStatement(query);
	         ResultSet rs = ps.executeQuery()) {  // Ensure ResultSet is also auto-closed
	        while (rs.next()) {
	            String ID = rs.getString("User_ID");
	            String Username = rs.getString("User_name");
	            String Email = rs.getString("User_Email");
	            String Password = rs.getString("User_Password");
	            String Role = rs.getString("User_Role");

	            // Assuming you have a Vendor constructor that takes these values
	            vendorList.add(new Vendor(ID, Username, Email, Password, Role));  // Create a Vendor object and add to the list
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	        // You could log the error or throw a custom exception here
	    }
	    return vendorList;  // Return the list of vendors
	}

	public boolean acceptInvitation(int eventId) {
		String query = "UPDATE invitation "
						+ "SET Status = 'Accepted' "
						+ "WHERE EventId = ? AND UserId = ?";
		
		try(PreparedStatement ps = connect.preparedStatement(query)) {
			// Use UserSession to get the logged-in user's ID
			ps.setInt(1, eventId);
			ps.setString(2, UserSession.getUserId());  

			int rowsUpdated = ps.executeUpdate();
			
			return rowsUpdated > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return false;
	}

	




	
}
