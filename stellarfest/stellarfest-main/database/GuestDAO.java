package database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import models.Guest;
import models.Vendor;
import models.User;

public class GuestDAO {
	private Connect connect = Connect.getInstance();

	public ArrayList<Guest> getAllGuest() {
	    ArrayList<Guest> guestList = new ArrayList<>();
	    String query = "SELECT User_ID, User_name, User_Email, User_Password, User_Role FROM Users WHERE User_Role = 'Guest'";  // SQL query to filter by 'Vendor' role and select specific columns
	    
	    try (PreparedStatement ps = connect.preparedStatement(query);
	         ResultSet rs = ps.executeQuery()) { 
	        while (rs.next()) {
	            String ID = rs.getString("User_ID");
	            String Username = rs.getString("User_name");
	            String Email = rs.getString("User_Email");
	            String Password = rs.getString("User_Password");
	            String Role = rs.getString("User_Role");

	            guestList.add(new Guest(ID, Username, Email, Password, Role));
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return guestList;
	}

	public static ArrayList<User> getGuestsByTransactionID(String eventID){
		ArrayList<User> guestList = new ArrayList<>();
		String query = "SELECT amogus";
		return guestList;
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
