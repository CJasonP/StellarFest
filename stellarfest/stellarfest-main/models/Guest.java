package models;

import java.util.List;

public class Guest extends User {
	private List<String> accepted_invitations;
	private boolean selected;

	public Guest(String user_id, String user_email, String user_name, String user_password, String user_role) {
		super(user_id, user_email, user_name, user_password, user_role);
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
}
