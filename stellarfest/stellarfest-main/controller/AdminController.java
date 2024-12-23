package controller;

import java.util.ArrayList;
import java.util.List;

import database.UserDAO;
import database.EventDAO;
import database.InvitationDAO;
import models.User;
import models.Event;

public class AdminController {
	private UserDAO userDAO;
	private EventDAO eventDAO;
	private InvitationDAO invitationDAO;

	public AdminController() {
		this.userDAO = new UserDAO();
		this.eventDAO = new EventDAO();
		this.invitationDAO = new InvitationDAO();
	}

	public ArrayList<Event> viewAllEvents() {
		return eventDAO.getAllEvents();
	}

	public String viewEventDetails(Event selectedEvent, List<String> acceptedVendor, List<String> acceptedGuest) {
		String eventDetailsText = "Event Details:\n\n" +
                "ID: " + selectedEvent.getEvent_id() + "\n" +
                "Name: " + selectedEvent.getEvent_name() + "\n" +
                "Date: " + selectedEvent.getEvent_date() + "\n" +
                "Location: " + selectedEvent.getEvent_location() + "\n" +
                "Description: " + selectedEvent.getEvent_description() + "\n" +
                "Organizer ID: " + selectedEvent.getOrganizer_id() + "\n\n";

        if (acceptedVendor.isEmpty()) {
            eventDetailsText += "Accepted Vendors: No vendors\n\n";
        } else {
            eventDetailsText += "Accepted Vendors:\n";
            for (String vendor : acceptedVendor) {
                eventDetailsText += "- " + vendor + "\n";
            }
            eventDetailsText += "\n";
        }

        // Add accepted guests to event details, with a validation check
        if (acceptedGuest.isEmpty()) {
            eventDetailsText += "Accepted Guests: No guests\n\n";
        } else {
            eventDetailsText += "Accepted Guests:\n";
            for (String guest : acceptedGuest) {
                eventDetailsText += "- " + guest + "\n";
            }
            eventDetailsText += "\n";
        }
        
        return eventDetailsText;
	}

	public void deleteEvent(String id) {
		eventDAO.deleteEvent(id);
	}

	public void deleteUser(String id) {
		userDAO.deleteUser(id);
	}

	public ArrayList<User> getAllUsers() {
		return userDAO.getAllUsers();
	}

	// getAllEvents sudah di viewAllEvents
	
	public List<String> getGuestsByTransactionID(String eventId) {
		return invitationDAO.getInvitedGuestIds(eventId);
	}

	public List<String> getVendorsByTransactionID(String eventId) {
		return invitationDAO.getInvitedVendorIds(eventId);
	}
	
}
