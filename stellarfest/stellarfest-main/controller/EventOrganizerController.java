package controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import database.EventDAO;
import database.GuestDAO;
import database.InvitationDAO;
import database.VendorDAO;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import models.Event;
import models.Guest;
import models.Vendor;

public class EventOrganizerController {

	private EventDAO eventDAO;
	private VendorDAO vendorDAO;
	private GuestDAO guestDAO;
	private InvitationDAO invitationDAO;

	public EventOrganizerController() {
		this.eventDAO = new EventDAO();
		this.vendorDAO = new VendorDAO();
		this.guestDAO = new GuestDAO();
		this.invitationDAO = new InvitationDAO();
	}

	public void createEvent(Event event) {
		eventDAO.insertEvent(event);
	}

	public ArrayList<Event> viewOrganizedEvents(String userID) {
		return eventDAO.viewOrganizedEvents(userID);
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

	public ArrayList<Guest> getAllGuest() {
		return guestDAO.getAllGuest();
	}

	public ArrayList<Vendor> getAllVendor() {
		return vendorDAO.getAllVendor();
	}

	public List<String> getGuestsByTransactionID(String eventId) {
		return invitationDAO.getInvitedGuestIds(eventId);
	}

	public List<String> getVendorsByTransactionID(String eventId) {
		return invitationDAO.getInvitedVendorIds(eventId);
	}

	public String checkCreateEventInput(Event event) {
		if (event.getEvent_name() == null || event.getEvent_name().trim().isEmpty()) {
			return "Event Name cannot be empty.";
		}

		if (event.getEvent_date() == null) {	
			return "Event Date cannot be empty.";
		}
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			LocalDate eventLocalDate = LocalDate.parse(event.getEvent_date(), formatter);

			// Ensure the event date is in the future
			if (!eventLocalDate.isAfter(LocalDate.now())) {
				return "Event Date must be in the future.";
			}
		} catch (Exception e) {
			return "Invalid Event Date format. Please use yyyy-MM-dd.";
		}
		
		if (event.getEvent_location() == null || event.getEvent_location().trim().isEmpty()) {
			return "Event Location cannot be empty.";
		}
		if (event.getEvent_location().length() < 5) {
			return "Event Location must be at least 5 characters long.";
		}

		if (event.getEvent_description() == null || event.getEvent_description().trim().isEmpty()) {
			return "Event Description cannot be empty.";
		}
		if (event.getEvent_description().length() > 200) {
			return "Event Description must be a maximum of 200 characters long.";
		}

		return null;
	}

    public String checkAddVendorInput(ObservableList<Vendor> selectedVendor) {
    	if (selectedVendor.isEmpty()) { 
			return "No vendors selected!";
		}
        return null;
    }

    public String checkAddGuestInput(ObservableList<Guest> selectedGuest) {
        if (selectedGuest.isEmpty()) { 
			return "No guests selected!";
		}
        return null;
    }

    public void editEventName(String eventID, String eventName) {
		eventDAO.editEventName(eventID, eventName);
	}

}
