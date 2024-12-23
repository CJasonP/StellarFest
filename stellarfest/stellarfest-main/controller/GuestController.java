package controller;

import java.util.ArrayList;
import java.util.List;

import database.EventDAO;
import database.GuestDAO;
import database.InvitationDAO;
import models.Event;
import models.Invitation;

public class GuestController {
	private GuestDAO guestDAO;
    private InvitationDAO invitationDAO;
    private EventDAO eventDAO;

	public GuestController(){
		this.guestDAO = new GuestDAO();
        this.invitationDAO = new InvitationDAO();
        this.eventDAO = new EventDAO();
	}

    public boolean acceptInvitation(int eventID) {
		return guestDAO.acceptInvitation(eventID);
	}

	public List<Event> viewAcceptedEvents(String userId) {
		List<Invitation> acceptedInvitations = invitationDAO.getInvitationsByStatus(userId, "Accepted");
		List<Event> acceptedEvents = new ArrayList<>();
		for (Invitation invitation : acceptedInvitations) {
			Event event = eventDAO.getEventByEventId(invitation.getEvent_id());
			if (event != null) {
				acceptedEvents.add(event);
			}
		}

		return acceptedEvents;
	}

}
