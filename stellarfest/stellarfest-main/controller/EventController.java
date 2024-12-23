package controller;

import java.util.ArrayList;
import database.EventDAO;
import models.Event;
import models.User;

public class EventController {
    private EventDAO eventDAO;
    
    public EventController(){
    	this.eventDAO = new EventDAO();
    }
    
    public ArrayList<Event> getAllEvents(){
        return eventDAO.getAllEvents();
    }
    
    public ArrayList<Event> getEventsByOrganizer(String ID){
        return eventDAO.getAllEventsByOrganizer(ID);
    }
    
    public void createEvent(String EventID, String name, String date, String location, String description, String organizerID) {
		Event event = new Event(EventID, name, date, location, description, organizerID);
		eventDAO.insertEvent(event);
	}
    
    public Event getEventByEventId(String eventId) {
        return eventDAO.getEventByEventId(eventId);
    }
    
    public String generateNewEventID()
    {
    	return eventDAO.generateNewEventID();
    }
    
    public void editEventName(String eventID, String eventName)
    {
    	eventDAO.editEventName(eventID, eventName);
    }
}
