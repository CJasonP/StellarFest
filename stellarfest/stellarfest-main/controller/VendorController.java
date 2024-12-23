package controller;

import java.util.ArrayList;
import java.util.List;

import database.EventDAO;
import database.InvitationDAO;
import database.ProductDAO;
import database.VendorDAO;
import models.Event;
import models.Invitation;
import models.Product;

public class VendorController {
	private VendorDAO vendorDAO;
	private InvitationDAO invitationDAO;
	private EventDAO eventDAO;
	private ProductDAO productDAO;

	public VendorController(){
        this.vendorDAO = new VendorDAO();
        this.invitationDAO = new InvitationDAO();
        this.eventDAO = new EventDAO();
        this.productDAO = new ProductDAO();
    }

	public boolean acceptInvitation(int eventID) {
		return vendorDAO.acceptInvitation(eventID);
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

	public void manageVendor(String productID, String userID, String productName, String productDescription) {
		productDAO.createProduct(productID, userID, productName, productDescription);
	}
	
    public String checkManageVendorInput(String productID, String productName, String productDescription) {
    	if (productName == null || productName.isEmpty()) {
            return "Product Name cannot be empty."; 
        }

        if (productDescription == null || productDescription.isEmpty()) {
            return "Product Description cannot be empty."; 
        }

        if (productDescription.length() > 200) {
            return "Product Description cannot exceed 200 characters.";
        }

        return null;
    }

    // Method tambahan untuk Products
	public ArrayList<Product> getAllProducts(String userId) {
		return productDAO.getAllProducts(userId);
	}

	public String generateProductID() {
		return productDAO.generateNewProductID();
	}
	
	public void updateProduct(String productID, String productName, String productDescription) {
		productDAO.updateProduct(productID, productName, productDescription);
	}
	
	public void deleteProduct(String productID) {
		productDAO.deleteProduct(productID);
	}
	
}
