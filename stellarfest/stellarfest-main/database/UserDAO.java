package database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;

import models.User;

public class UserDAO {
	private Connect connect = Connect.getInstance();

	public ArrayList<User> getAllUsers() {
		ArrayList<User> userList = new ArrayList<>();
		String query = "SELECT User_ID, User_name, User_Email, User_Password, User_Role FROM Users";
		ResultSet rs = connect.execQuery(query);

		try {
			while (rs.next()) {
				String ID = rs.getString("User_ID");
				String Username = rs.getString("User_name");
				String Email = rs.getString("User_Email");
				String Password = rs.getString("User_Password");
				String Role = rs.getString("User_Role");

				userList.add(new User(ID, Username, Email, Password, Role));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return userList;
	}

	public String generateNewUserID() {
	    String query = "SELECT User_ID FROM Users ORDER BY User_ID DESC LIMIT 1";
	    PreparedStatement ps = connect.preparedStatement(query);
	    
	    try {
	        ResultSet rs = ps.executeQuery();
	        if (rs.next()) {
	            String lastID = rs.getString("User_ID");
	            // Assuming the ID is of the format "US001", "US002", etc.
	            int newID = Integer.parseInt(lastID.substring(3)) + 1; // Extract number and increment it
	            return "US" + String.format("%03d", newID); // Pad with leading zeros
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return "US001"; // Default to "US001" if no records exist
	}
	
	public void insertUser(User user) {
		String query = "INSERT INTO Users (user_id, user_name, user_email, user_password, user_role) VALUES (?, ?, ?, ?, ?)";
		PreparedStatement ps = connect.preparedStatement(query);
		
		try {
			ps.setString(1, user.getUser_id());
			ps.setString(2, user.getUser_name());
			ps.setString(3, user.getUser_email());
			ps.setString(4, user.getUser_password());
			ps.setString(5, user.getUser_role());
			ps.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	public boolean updateUser(User user) {
		String query = "UPDATE Users SET User_name = ?, User_Email = ?, User_Password = ? WHERE User_ID = ?";
		try (PreparedStatement ps = connect.preparedStatement(query)) {
			// Set parameters in the correct order
			ps.setString(1, user.getUser_name());
			ps.setString(2, user.getUser_email());
			ps.setString(3, user.getUser_password());
			ps.setString(4, user.getUser_id());

			int rowsAffected = ps.executeUpdate(); // Execute the update and get affected rows
			return rowsAffected > 0; // Return true if the update was successful
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false; // Return false if the update fails
	}


	public void deleteUser(String userID) {
		String query = "DELETE FROM Users WHERE User_ID = ?";
		PreparedStatement ps = connect.preparedStatement(query);

		try {
			ps.setString(1, userID);
			ps.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	public Optional<User> authenticateUser(String email, String password) {
        String query = "SELECT * FROM Users WHERE User_Email = ? AND User_Password = ?";
        PreparedStatement ps = connect.preparedStatement(query);

        try {
            ps.setString(1, email);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String ID = rs.getString("User_ID");
                String Username = rs.getString("User_name");
                String Role = rs.getString("User_Role");
                
                UserSession.setUserId(ID);
                
                return Optional.of(new User(ID, Username, email, password, Role));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

	public boolean isEmailUnique(String email) {
        String query = "SELECT * FROM Users WHERE User_Email = ?";
        PreparedStatement ps = connect.preparedStatement(query);

        try {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            return !rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isUsernameUnique(String username) {
        String query = "SELECT * FROM Users WHERE User_name = ?";
        PreparedStatement ps = connect.preparedStatement(query);

        try {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            return !rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

	public User getUserByEmail(String email){
		String query = "SELECT * FROM Users WHERE User_Email = ?";
		PreparedStatement ps = connect.preparedStatement(query);
	
		try {
			ps.setString(1, email);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				String ID = rs.getString("User_ID");
				String Username = rs.getString("User_name");
				String Password = rs.getString("User_Password");
				String Role = rs.getString("User_Role");
				return new User(ID, Username, email, Password, Role);
			}
		} catch (SQLException e){
			e.printStackTrace();
		}
	
		return null;
	}
	
	public User getUserByUsername(String username){
		String query = "SELECT * FROM Users WHERE User_name = ?";
		PreparedStatement ps = connect.preparedStatement(query);
	
		try {
			ps.setString(1, username);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				String ID = rs.getString("User_ID");
				String Email = rs.getString("User_Email");
				String Password = rs.getString("User_Password");
				String Role = rs.getString("User_Role");
				return new User(ID, username, Email, Password, Role);
			}
		} catch (SQLException e){
			e.printStackTrace();
		}
	
		return null;
	}

	public User getUserByUserId(String id) {
		String query = "SELECT * FROM Users WHERE User_ID = ?";
		try (PreparedStatement ps = connect.preparedStatement(query)) {
			ps.setString(1, id);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				String ID = rs.getString("User_ID");
				String Username = rs.getString("User_name");
				String Email = rs.getString("User_Email");
				String Password = rs.getString("User_Password");
				String Role = rs.getString("User_Role");
				return new User(ID, Username, Email, Password, Role);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

}
