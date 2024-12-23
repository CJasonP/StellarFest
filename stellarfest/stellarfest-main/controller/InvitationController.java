package controller;

import java.util.ArrayList;
import java.util.List;

import database.EventDAO;
import database.InvitationDAO;
import database.UserDAO;
import models.Event;
import models.Invitation;

public class InvitationController {
    private final InvitationDAO invitationDAO;
    private final EventDAO eventDAO;
    private final UserDAO userDAO;

    public InvitationController(){
        this.invitationDAO = new InvitationDAO();
        this.eventDAO = new EventDAO();
        this.userDAO = new UserDAO();
    }

    public void sendInvitation(String event_id, String user_id) {
        String invitation_id = invitationDAO.generateNewInvitationID();
        String invitation_status = "Pending";
        String invitation_role = userDAO.getUserByUserId(user_id).getUser_role();

        System.out.println(invitation_id + invitation_status + invitation_role);

        Invitation invitation = new Invitation(invitation_id, event_id, user_id, invitation_status, invitation_role);

        invitationDAO.createInvitation(invitation);
    }

    public boolean acceptInvitation(String invitationId, String userID) {
        return invitationDAO.updateInvitationStatus(invitationId, "Accepted", userID);
    }

    public List<Invitation> getInvitationList(String userId){
        return invitationDAO.getInvitationsByUserId(userId);
    }
}
