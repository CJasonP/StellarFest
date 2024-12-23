package database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Invitation;

public class InvitationDAO {
	private Connect connect = Connect.getInstance();
	private Map<String, ArrayList<String>> userAcceptedInvitationsMap = new HashMap<>();
	
	public void createInvitation(Invitation invitation) {
		String query = "INSERT INTO invitations (Invitation_ID, Event_ID, User_ID, Invitation_Status, Invitation_Role) VALUES (?, ?, ?, ?, ?)";

		try (PreparedStatement ps = connect.preparedStatement(query)) {
			ps.setString(1, invitation.getInvitation_id());
			ps.setString(2, invitation.getEvent_id());
			ps.setString(3, invitation.getUser_id());
			ps.setString(4, invitation.getInvitation_status());
			ps.setString(5, invitation.getInvitation_role());

			ps.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public String generateNewInvitationID() {
		String query = "SELECT Invitation_ID FROM Invitations ORDER BY Invitation_ID DESC LIMIT 1";
		PreparedStatement ps = connect.preparedStatement(query);

		try {
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				String lastID = rs.getString("Invitation_ID");
				int newID = Integer.parseInt(lastID.substring(3)) + 1;
				return "IN" + String.format("%03d", newID);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "IN001";
	}

	public List<String> getInvitedVendorIds(String eventId) {
		String query = "SELECT User_ID \r\n" + "        FROM Invitations\r\n"
				+ "        WHERE Event_ID = ? AND invitation_role = 'Vendor'";
		List<String> invitedVendorIds = new ArrayList<>();

		try (PreparedStatement ps = connect.preparedStatement(query)) {
			ps.setString(1, eventId);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				invitedVendorIds.add(rs.getString("User_ID"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return invitedVendorIds;
	}

	public List<String> getInvitedGuestIds(String eventId) {
		String query = "SELECT User_ID \r\n" + "        FROM Invitations\r\n"
				+ "        WHERE Event_ID = ? AND invitation_role = 'Guest'";
		List<String> invitedGuestIds = new ArrayList<>();

		try (PreparedStatement ps = connect.preparedStatement(query)) {
			ps.setString(1, eventId);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				invitedGuestIds.add(rs.getString("User_ID"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return invitedGuestIds;
	}

	public List<Invitation> getInvitationsByUserId(String userId) {
		List<Invitation> invitations = new ArrayList<>();
		String query = "SELECT Invitation_ID, Event_ID, User_ID, Invitation_Status, Invitation_Role FROM invitations WHERE User_ID = ?";

		try (PreparedStatement ps = connect.preparedStatement(query)) {
			ps.setString(1, userId);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				Invitation invitation = new Invitation(rs.getString("Invitation_ID"), rs.getString("Event_ID"),
						rs.getString("User_ID"), rs.getString("Invitation_Status"), rs.getString("Invitation_Role"));
				invitations.add(invitation);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return invitations;
	}

	public boolean updateInvitationStatus(String invitationId, String newStatus, String userId) {
		String updateInvitationQuery = "UPDATE invitations SET Invitation_Status = ? WHERE Invitation_ID = ?";
	    String getAcceptedInvitationsQuery = "SELECT accepted_invitations FROM users WHERE user_id = ?";
	    String updateAcceptedInvitationsQuery = "UPDATE users SET accepted_invitations = ? WHERE user_id = ?";

	    try (PreparedStatement ps = connect.preparedStatement(updateInvitationQuery);
	         PreparedStatement getPs = connect.preparedStatement(getAcceptedInvitationsQuery);
	         PreparedStatement updatePs = connect.preparedStatement(updateAcceptedInvitationsQuery)) {
	        
	        // Step 1: Update the invitation status
	        ps.setString(1, newStatus);
	        ps.setString(2, invitationId);
	        int affectedRows = ps.executeUpdate();

	        if (affectedRows > 0 && "Accepted".equalsIgnoreCase(newStatus)) {
	            // Step 2: Retrieve current accepted invitations for the user
	            getPs.setString(1, userId);
	            ResultSet rs = getPs.executeQuery();

	            String currentAcceptedInvitations = "";
	            if (rs.next()) {
	                currentAcceptedInvitations = rs.getString("accepted_invitations");
	            }

	            // Step 3: Append the new invitation ID
	            ArrayList<String> acceptedList = userAcceptedInvitationsMap
	                .computeIfAbsent(userId, k -> new ArrayList<>());

	            if (currentAcceptedInvitations != null && !currentAcceptedInvitations.isEmpty()) {
	                acceptedList.addAll(List.of(currentAcceptedInvitations.split(",")));
	            }
	            if (!acceptedList.contains(invitationId)) {
	                acceptedList.add(invitationId);
	            }

	            String updatedAcceptedInvitations = String.join(",", acceptedList);

	            // Step 4: Update the user's accepted_invitations column
	            updatePs.setString(1, updatedAcceptedInvitations);
	            updatePs.setString(2, userId);
	            updatePs.executeUpdate();

	            // Clear the local cache for the user
	            userAcceptedInvitationsMap.get(userId).clear();
	            userAcceptedInvitationsMap.put(userId, new ArrayList<>(acceptedList));

	            return true;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false;
	}

	public List<Invitation> getInvitationsByStatus(String userId, String status) {
		List<Invitation> invitations = new ArrayList<>();
		String query = "SELECT * FROM invitations WHERE User_ID = ? AND Invitation_Status = ?";

		try (PreparedStatement ps = connect.preparedStatement(query)) {
			ps.setString(1, userId);
			ps.setString(2, status);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				Invitation invitation = new Invitation(
					rs.getString("Invitation_ID"),
					rs.getString("Event_ID"),
					rs.getString("User_ID"),
					rs.getString("Invitation_Status"),
					rs.getString("Invitation_Role")
				);
				invitations.add(invitation);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return invitations;
	}

}
