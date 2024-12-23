package view;

import java.util.List;

import controller.EventController;
import controller.InvitationController;
import controller.UserController;
import controller.VendorController;
import database.UserSession;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Event;
import models.Invitation;
import models.Product;
import models.User;
import javafx.scene.control.cell.PropertyValueFactory;

public class VendorView {
	private BorderPane borderPane;
	private Scene scene;
	private MenuBar menuBar;
	private Menu eventMenu, invitationMenu, profileMenu, productMenu;
	private MenuItem viewAcceptedEvents, viewEventInvitations, profileItem, productItem;
	private Label welcome;

	private TableView<Invitation> invitationTable;
	private TableView<Event> acceptedEventsTable;
	private TableView<Product> productTable;
	private InvitationController invitationController;
	private EventController eventController;
	private UserController userController;
	private VendorController vendorController;
	private User currentUser;
	private String currentUserId;

	public void initialize() {
		currentUserId = UserSession.getUserId();
		invitationController = new InvitationController();
		eventController = new EventController();
		userController = new UserController();
		vendorController = new VendorController();

		welcome = new Label("Welcome, Vendor! View your events and invitations through the navigation bar.");
		menuBar = new MenuBar();

		invitationMenu = new Menu("Invitation");
		eventMenu = new Menu("Event");
		profileMenu = new Menu("Profile");
		productMenu = new Menu("Product");

		viewEventInvitations = new MenuItem("View Event Invitations");
		viewAcceptedEvents = new MenuItem("View Accepted Events");
		profileItem = new MenuItem("Manage Profile");
		productItem = new MenuItem("Manage Item");

		invitationMenu.getItems().add(viewEventInvitations);
		eventMenu.getItems().add(viewAcceptedEvents);
		profileMenu.getItems().add(profileItem);
		productMenu.getItems().add(productItem);
		menuBar.getMenus().addAll(eventMenu, invitationMenu, profileMenu, productMenu);

		borderPane = new BorderPane();
		scene = new Scene(borderPane, 1000, 600);
	}

	public void layout() {
		borderPane.setTop(menuBar);
		borderPane.setCenter(welcome);
	}

	private void setEventHandler(String currentID) {
		viewAcceptedEvents.setOnAction(e -> displayAcceptedEvent(currentID));
		viewEventInvitations.setOnAction(e -> displayInvitations(currentID));
		profileItem.setOnAction(e -> displayProfile());
		productItem.setOnAction(e -> displayProduct());
		
	}

	public Scene createVendorViewScene(Stage stage) {
		initialize();
		layout();
		String currentUserId = UserSession.getUserId();
		setEventHandler(currentUserId);
		return scene;
	}

	/////// METHODS ///////////
	
	private void displayProduct() {
	    productTable = new TableView<>();
	    setupProductTable();

	    List<Product> productList = vendorController.getAllProducts(currentUserId);
	    productTable.getItems().addAll(productList);

	    // Buttons for create, update, and delete actions
	    Button createButton = new Button("Create Product");
	    createButton.setOnAction(e -> handleCreateProduct());

	    Button updateButton = new Button("Update Product");
	    updateButton.setOnAction(e -> handleUpdateProduct());

	    Button deleteButton = new Button("Delete Product");
	    deleteButton.setOnAction(e -> handleDeleteProduct());

	    VBox buttonBox = new VBox(10, createButton, updateButton, deleteButton);
	    buttonBox.setAlignment(Pos.CENTER);

	    VBox layout = new VBox(10, productTable, buttonBox);
	    layout.setPadding(new javafx.geometry.Insets(10));
	    borderPane.setCenter(layout);
	}

	private void setupProductTable() {
	    TableColumn<Product, String> productIdColumn = new TableColumn<>("Product ID");
	    productIdColumn.setCellValueFactory(new PropertyValueFactory<>("product_id"));

	    TableColumn<Product, String> nameColumn = new TableColumn<>("Product Name");
	    nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

	    TableColumn<Product, String> descriptionColumn = new TableColumn<>("Description");
	    descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

	    productTable.getColumns().addAll(productIdColumn, nameColumn, descriptionColumn);
	    productTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
	}

	private void showAlert(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
	
	private void handleCreateProduct() {
	    TextField productNameField = new TextField();
	    productNameField.setPromptText("Enter Product Name");

	    TextField productDescriptionField = new TextField();
	    productDescriptionField.setPromptText("Enter Product Description");

	    // Submit button
	    Button submitButton = new Button("Submit");
	    submitButton.setOnAction(e -> {
	        String productName = productNameField.getText();
	        String productDescription = productDescriptionField.getText();
	        String productID = vendorController.generateProductID();
			String error = vendorController.checkManageVendorInput(productID, productName, productDescription);

			if(error != null){
				showAlert("Validation Error! ", error);
			}
			else {
				vendorController.manageVendor(productID, currentUserId, productName, productDescription);
				displayProduct();
				productNameField.clear();
		        productDescriptionField.clear();
			}

	    });
	    
	    GridPane inputLayout = new GridPane();
	    inputLayout.setVgap(15); 
	    inputLayout.setHgap(10);  
	    inputLayout.setPadding(new Insets(20)); 
	    inputLayout.setAlignment(Pos.CENTER_LEFT); 

	    inputLayout.add(new Label("Product Name:"), 0, 0);
	    inputLayout.add(productNameField, 1, 0);
	    inputLayout.add(new Label("Product Description:"), 0, 3);
	    inputLayout.add(productDescriptionField, 1, 3);

	    inputLayout.add(submitButton, 1, 4);
	    GridPane.setHalignment(submitButton, HPos.CENTER);
	    borderPane.setCenter(inputLayout);
	}

	private void handleUpdateProduct() {
		Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
	    if (selectedProduct != null) {
	        TextField productNameField = new TextField(selectedProduct.getName());
	        TextField productDescriptionField = new TextField(selectedProduct.getDescription());

	        // Button to submit the update
	        Button updateButton = new Button("Update Product");
	        updateButton.setOnAction(e -> {
	            String updatedName = productNameField.getText();
	            String updatedDescription = productDescriptionField.getText();
	            String error = vendorController.checkManageVendorInput(selectedProduct.getProduct_id(), updatedName, updatedDescription);
	            if(error != null){
					showAlert("Validation Error", error);
				}
				else {
					vendorController.updateProduct(selectedProduct.getProduct_id(), updatedName, updatedDescription);
					displayProduct();
				}
	            
	            
	        });

	        // Layout for the form
	        GridPane updateLayout = new GridPane();
	        updateLayout.setVgap(15);
	        updateLayout.setHgap(10);
	        updateLayout.setPadding(new Insets(20));
	        updateLayout.setAlignment(Pos.CENTER_LEFT);

	        // Labels and input fields
	        updateLayout.add(new Label("Product Name:"), 0, 0);
	        updateLayout.add(productNameField, 1, 0);
	        updateLayout.add(new Label("Product Description:"), 0, 1);
	        updateLayout.add(productDescriptionField, 1, 1);
	        updateLayout.add(updateButton, 1, 2);

	        // Center the layout on the BorderPane
	        borderPane.setCenter(updateLayout);
	    } else {
	        showAlert("No Product Selected", "Please select a product to update.");
	    }
	}

	private void handleDeleteProduct() {
	    Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
	    if (selectedProduct != null) {
	        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Do you want to delete this product?",
	                ButtonType.YES, ButtonType.NO);
	        confirm.showAndWait().ifPresent(response -> {
	            if (response == ButtonType.YES) {
	                vendorController.deleteProduct(selectedProduct.getProduct_id());

	                displayProduct();
	            }
	        });
	    } else {
	        showAlert("No Product Selected", "Please select a product to delete.");
	    }
	}
	
	private void displayProfile() {

		currentUser = userController.getUserById(currentUserId);

		if (currentUser == null) {
			showAlert("User Not Found", "The user could not be found in the database.");
			return;
		}

		TextField nameField = new TextField(currentUser.getUser_name());
		nameField.setPromptText("New Name");
		TextField emailField = new TextField(currentUser.getUser_email());
		emailField.setPromptText("New Email");
		PasswordField oldPasswordField = new PasswordField();
		oldPasswordField.setPromptText("Old Password");
		PasswordField newPasswordField = new PasswordField();
		newPasswordField.setPromptText("New Password");

		Button submitButton = new Button("Save Changes");

		submitButton.setOnAction(e -> {
			String newName = nameField.getText().trim();
			String newEmail = emailField.getText().trim();
			String oldPassword = oldPasswordField.getText().trim();
			String newPassword = newPasswordField.getText().trim();

			String validationMessage = userController.checkChangeProfileInput(currentUser, newName, newEmail,
					oldPassword, newPassword);

			if (validationMessage != null) {
				showAlert("Invalid Input", validationMessage);
				return;
			}

			boolean isUpdated = userController.updateUserProfile(currentUserId, newName, newEmail, oldPassword,
					newPassword);

			if (isUpdated) {
				showAlert("Success", "Profile updated successfully!");
				currentUser = userController.getUserById(currentUserId);
				oldPasswordField.clear();
				newPasswordField.clear();
			} else {
				showAlert("Update Failed", "Old password is incorrect or update failed.");
			}
		});

		VBox formBox = new VBox(10, new Label("Update Profile"), new Label("Name:"), nameField, new Label("Email:"),
				emailField, new Label("Old Password:"), oldPasswordField, new Label("New Password:"), newPasswordField,
				submitButton);

		formBox.setAlignment(Pos.CENTER);
		formBox.setPadding(new javafx.geometry.Insets(20));

		borderPane.setCenter(formBox);
	}
	
	private void displayInvitations(String userId) {
		invitationTable = new TableView<>();
		setupInvitationTable();
		List<Invitation> invitationList = invitationController.getInvitationList(userId);
		invitationTable.getItems().addAll(invitationList);
		borderPane.setCenter(invitationTable);
	}

	private void setupInvitationTable() {
		TableColumn<Invitation, String> invitationIdColumn = new TableColumn<>("Invitation ID");
		invitationIdColumn.setCellValueFactory(new PropertyValueFactory<>("invitation_id"));

		TableColumn<Invitation, String> eventIdColumn = new TableColumn<>("Event ID");
		eventIdColumn.setCellValueFactory(new PropertyValueFactory<>("event_id"));

		TableColumn<Invitation, String> invitationStatusColumn = new TableColumn<>("Invitation Status");
		invitationStatusColumn.setCellValueFactory(new PropertyValueFactory<>("Invitation_status"));

		// Kolom untuk Accept button
		TableColumn<Invitation, Void> actionColumn = new TableColumn<>("Actions");
		actionColumn.setCellFactory(col -> new TableCell<>() {
			private final Button acceptButton = new Button("Accept");

			{
				acceptButton.setOnAction(e -> {
					Invitation invitation = getTableView().getItems().get(getIndex());
					Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Do you want to accept this invitation?",
							ButtonType.YES, ButtonType.NO);
					confirm.showAndWait().ifPresent(response -> {
						if (response == ButtonType.YES) {
							handleAcceptInvitation(invitation);
						}
					});
				});
			}

			@Override
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);
				if (empty) {
					setGraphic(null);
				} else {
					setGraphic(acceptButton);
				}
			}
		});

		invitationTable.getColumns().addAll(invitationIdColumn, eventIdColumn, invitationStatusColumn, actionColumn);
		invitationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
	}

	private void handleAcceptInvitation(Invitation invitation) {
		boolean success = invitationController.acceptInvitation(invitation.getInvitation_id(), currentUserId);
		if (success) {
			String currentUserId = UserSession.getUserId();
			displayInvitations(currentUserId);
		} else {
			Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to accept the invitation.", ButtonType.OK);
			alert.showAndWait();
		}
	}

	// Starting point Accepted Event subpage
	private void displayAcceptedEvent(String userId) {
		acceptedEventsTable = new TableView<>();
		setupAcceptedEventTable();
		List<Event> acceptedEventList = vendorController.viewAcceptedEvents(userId);
		acceptedEventsTable.getItems().addAll(acceptedEventList);
    	setEventTableSelectionHandler(acceptedEventsTable); // Set double-click handler
		Label instructionLabel = new Label("Double-click an event to view its details.");
		instructionLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10;");
		VBox layout = new VBox(10, instructionLabel, acceptedEventsTable);
		layout.setPadding(new javafx.geometry.Insets(10));
		borderPane.setCenter(layout);
	}

	private void setupAcceptedEventTable() {
		TableColumn<Event, String> eventIdColumn = new TableColumn<>("Event ID");
		eventIdColumn.setCellValueFactory(new PropertyValueFactory<>("event_id"));

		TableColumn<Event, String> eventNameColumn = new TableColumn<>("Event Name");
		eventNameColumn.setCellValueFactory(new PropertyValueFactory<>("event_name"));

		TableColumn<Event, String> eventDateColumn = new TableColumn<>("Event Date");
		eventDateColumn.setCellValueFactory(new PropertyValueFactory<>("event_date"));

		TableColumn<Event, String> eventLocationColumn = new TableColumn<>("Event Location");
		eventLocationColumn.setCellValueFactory(new PropertyValueFactory<>("event_location"));

		acceptedEventsTable.getColumns().addAll(eventIdColumn, eventNameColumn, eventDateColumn, eventLocationColumn);
		acceptedEventsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
	}

	private void setEventTableSelectionHandler(TableView<Event> eventTable) {
		eventTable.setOnMouseClicked(event -> {
			if (event.getClickCount() == 2) { // Detect double-click
				Event selectedEvent = eventTable.getSelectionModel().getSelectedItem();
				if (selectedEvent != null) {
					viewEventDetails(selectedEvent.getEvent_id());
				}
			}
		});
	}
	
	private void viewEventDetails(String eventId) {
		Stage eventDetailStage = new Stage();
		BorderPane detailRoot = new BorderPane();

		Event selectedEvent = eventController.getEventByEventId(eventId);

		if (selectedEvent != null) {
			Label eventDetails = new Label(
				"Event Details:\n\n" +
				"ID: " + selectedEvent.getEvent_id() + "\n" +
				"Name: " + selectedEvent.getEvent_name() + "\n" +
				"Date: " + selectedEvent.getEvent_date() + "\n" +
				"Location: " + selectedEvent.getEvent_location() + "\n" +
				"Description: " + selectedEvent.getEvent_description() + "\n" +
				"Organizer ID: " + selectedEvent.getOrganizer_id()
			);

			VBox detailBox = new VBox(10, eventDetails);
			detailBox.setAlignment(Pos.CENTER);
			detailBox.setPadding(new javafx.geometry.Insets(10));
			detailRoot.setCenter(detailBox);
		}

		Scene detailScene = new Scene(detailRoot, 400, 300);
		eventDetailStage.setScene(detailScene);
		eventDetailStage.setTitle("Event Details");
		eventDetailStage.show();
	}

}
