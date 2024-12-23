package database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import models.User;
import models.Vendor;
import models.Event;

public class EventDAO {
	private Connect connect = Connect.getInstance();
	private Map<String, ArrayList<Event>> userEventsMap = new HashMap<>();

	public ArrayList<Event> getAllEvents() {
		ArrayList<Event> eventList = new ArrayList<>();
		String query = "SELECT * FROM Events";
		ResultSet rs = connect.execQuery(query);

		try {
			while (rs.next()) {
				String ID = rs.getString("Event_ID");
				String Name = rs.getString("Event_Name");
				String Date = rs.getString("Event_Date");
				String Location = rs.getString("Event_Location");
				String Description = rs.getString("Event_Description");
				String OrganizerID = rs.getString("Organizer_ID");
				eventList.add(new Event(ID, Name, Date, Location, Description, OrganizerID));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return eventList;
	}
	
	public ArrayList<Event> getAllEventsByOrganizer(String ID) {
	    ArrayList<Event> eventList = new ArrayList<>();
	    String query = "SELECT * FROM Events WHERE Organizer_ID = ?";
	    try (PreparedStatement ps = connect.preparedStatement(query)) {
	        ps.setString(1, ID); // Set the ID parameter

	        ResultSet rs = ps.executeQuery();

	        while (rs.next()) {
	            String eventID = rs.getString("Event_ID");
	            String eventName = rs.getString("Event_Name");
	            String eventDate = rs.getString("Event_Date");
	            String eventLocation = rs.getString("Event_Location");
	            String eventDescription = rs.getString("Event_Description");
	            String organizerID = rs.getString("Organizer_ID");

	            eventList.add(new Event(eventID, eventName, eventDate, eventLocation, eventDescription, organizerID));
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return eventList;
	}


	public String generateNewEventID() {
	    String query = "SELECT Event_ID FROM Events ORDER BY Event_ID DESC LIMIT 1";
	    PreparedStatement ps = connect.preparedStatement(query);
	    
	    try {
	        ResultSet rs = ps.executeQuery();
	        if (rs.next()) {
	            String lastID = rs.getString("Event_ID");
	            int newID = Integer.parseInt(lastID.substring(3)) + 1; // Extract number and increment it
	            return "EV" + String.format("%03d", newID); // Pad with leading zeros
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return "EV001"; // Default to "USR001" if no records exist
	}
	
	public void insertEvent(Event event) {
		String query = "INSERT INTO Events VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connect.preparedStatement(query)) {
            ps.setString(1, event.getEvent_id());
            ps.setString(2, event.getEvent_name());
            ps.setString(3, event.getEvent_date());
            ps.setString(4, event.getEvent_location());
            ps.setString(5, event.getEvent_description());
            ps.setString(6, event.getOrganizer_id());
            ps.execute();

            // Track the event for the specific organizer
            userEventsMap.computeIfAbsent(event.getOrganizer_id(), k -> new ArrayList<>()).add(event);

            // Update the events_created table
            updateEventsCreated(event.getOrganizer_id(), event.getEvent_id());
        } catch (SQLException e) {
            e.printStackTrace();
        }
	}
	
	private void updateEventsCreated(String organizerId, String newEventId) {
		String getCurrentQuery = "SELECT events_created FROM users WHERE user_id = ?";
	    String updateQuery = "UPDATE users SET events_created = ? WHERE user_id = ?";

	    try (PreparedStatement getPs = connect.preparedStatement(getCurrentQuery);
	         PreparedStatement updatePs = connect.preparedStatement(updateQuery)) {
	        
	        // Step 1: Retrieve the current value
	        getPs.setString(1, organizerId);
	        ResultSet rs = getPs.executeQuery();

	        String currentEvents = "";
	        if (rs.next()) {
	            currentEvents = rs.getString("events_created"); // Get current events
	        }

	        // Step 2: Append the new event ID
	        if (currentEvents != null && !currentEvents.isEmpty()) {
	            currentEvents += "," + newEventId;
	        } else {
	            currentEvents = newEventId; // First event for the user
	        }

	        // Step 3: Update the column
	        updatePs.setString(1, currentEvents);
	        updatePs.setString(2, organizerId);
	        updatePs.executeUpdate();

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

    }
	//buat update list untuk tabel user created_event
	
	public void deleteEvent(String eventID) {
		String query = "DELETE FROM Events WHERE Event_ID = ?";
		PreparedStatement ps = connect.preparedStatement(query);

		try {
			ps.setString(1, eventID);
			ps.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<Event> viewOrganizedEvents(String organizerID){
		String query = "SELECT * FROM events WHERE organizer_id = ?";
		ArrayList<Event> organizedEvents = new ArrayList<>();
		
		try (PreparedStatement ps = connect.preparedStatement(query)) {
			ps.setString(1, organizerID);  
			
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					String eventID = rs.getString("Event_ID");
					String eventName = rs.getString("Event_Name");
					String eventDate = rs.getString("Event_Date");
					String eventLocation = rs.getString("Event_Location");
					String eventDescription = rs.getString("Event_Description");
					String eventOrganizerID = rs.getString("Organizer_ID");

					organizedEvents.add(new Event(eventID, eventName, eventDate, eventLocation, eventDescription, eventOrganizerID));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return organizedEvents;
	}

	public Event getEventByEventId(String eventID) {
		String query = "SELECT * FROM events WHERE event_id = ?";
		Event event = null;

		try (PreparedStatement ps = connect.preparedStatement(query)) {
			ps.setString(1, eventID);  
			
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					String eventIDValue = rs.getString("Event_ID");
					String eventName = rs.getString("Event_Name");
					String eventDate = rs.getString("Event_Date");
					String eventLocation = rs.getString("Event_Location");
					String eventDescription = rs.getString("Event_Description");
					String organizerID = rs.getString("Organizer_ID");

					event = new Event(eventIDValue, eventName, eventDate, eventLocation, eventDescription, organizerID);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return event; 
	}

	public Event getEventDetails(String eventID) {
		String query = "SELECT * FROM events WHERE event_id = ?";
		try (PreparedStatement ps = connect.preparedStatement(query)) {
			ps.setString(1, eventID);
			
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					String eventIDValue = rs.getString("Event_ID");
					String eventName = rs.getString("Event_Name");
					String eventDate = rs.getString("Event_Date");
					String eventLocation = rs.getString("Event_Location");
					String eventDescription = rs.getString("Event_Description");
					String organizerID = rs.getString("Organizer_ID");
					
					return new Event(eventIDValue, eventName, eventDate, eventLocation, eventDescription, organizerID);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void editEventName(String eventID, String eventName){
		String query = "UPDATE events SET event_name = ? where event_id = ?";
		PreparedStatement ps = connect.preparedStatement(query);

		try{
			ps.setString(1, eventName);
			ps.setString(2, eventID);

			ps.execute();
		} catch(SQLException e) {
			e.printStackTrace();
		}

	}




}

