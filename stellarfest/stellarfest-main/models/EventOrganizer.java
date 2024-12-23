package models;

import java.util.List;

public class EventOrganizer extends User{
	private List<String> events_created;
	
	public EventOrganizer(String user_id, String user_email, String user_name, String user_password, String user_role) {
		super(user_id, user_email, user_name, user_password, user_role);
	}

}
