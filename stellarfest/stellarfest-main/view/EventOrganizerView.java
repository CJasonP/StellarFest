package view;

import javafx.scene.Scene;
import javafx.stage.Stage;
import models.Event;
import models.Guest;
import models.User;
import models.Vendor;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import controller.EventController;
import controller.EventOrganizerController;
import controller.InvitationController;
import controller.UserController;
import database.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

public class EventOrganizerView {
	private MenuBar menuBar;
	private Menu viewOrganizedEvent, viewOrganizedEventDetails, editEventName, createEvent, profileMenu;
	private MenuItem viewEvent, viewEventDetails, invitation, addEvent, viewProfile;

	private BorderPane borderPane;
	private Scene scene;
	private Label welcome;
	private EventController eventController;
	private EventOrganizerController EOController;
	private InvitationController invitationController;
	private TableView<Event> eventTable;
	private ArrayList<Event> eventList;
	private TableView<Vendor> vendorTable;
	private ArrayList<Vendor> vendorList;
	private TableView<Guest> guestTable;
	private ArrayList<Guest> guestList;
	private ObservableList<Vendor> vendorObsList;
	private ObservableList<Guest> guestObsList;
	private ObservableList<Event> eventObsList;
	private Stage eventDetailStage;
	private BorderPane detailRoot;
	private String currentUserId;
	private UserController userController;
	private User currentUser;

	private Button inviteVendorButton = new Button("Invite Selected Vendors");
	private Button inviteGuestButton = new Button("Invite Selected Guests");

	public Scene createEventOrganizerViewScene(Stage primaryStage) {
		initialize();
		layout();
		setEventHandler(currentUserId);

		primaryStage.setTitle("Event Management");
		primaryStage.setScene(scene);
		primaryStage.show();

		return scene;
	}

	public void initialize() {

		currentUserId = UserSession.getUserId();
		userController = new UserController();
		menuBar = new MenuBar();
		welcome = new Label("Welcome, Event Organizer!");
		eventController = new EventController();
		EOController = new EventOrganizerController();
		invitationController = new InvitationController();
		eventTable = new TableView<>();

		viewOrganizedEvent = new Menu("View Organized Event");
		createEvent = new Menu("Create Event");
		profileMenu = new Menu("Profile");
		eventTable = new TableView<>();

		viewEvent = new MenuItem("View Events");
		addEvent = new MenuItem("Create Events");
		viewProfile = new MenuItem("Manage Profile");

		viewOrganizedEvent.getItems().add(viewEvent);
		createEvent.getItems().add(addEvent);
		profileMenu.getItems().add(viewProfile);

		menuBar.getMenus().addAll(viewOrganizedEvent, createEvent, profileMenu);
		borderPane = new BorderPane();
		scene = new Scene(borderPane, 1000, 600);
	}

	private void showAlert(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
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

	private void displayEvent() {
		setupEventTable(eventTable);
		
		Label instructionLabel = new Label("Double-click an event to view its details.");
		VBox tableBox = new VBox(10, instructionLabel,eventTable);
		tableBox.setAlignment(Pos.CENTER);
		tableBox.setPadding(new javafx.geometry.Insets(10));
		borderPane.setCenter(tableBox);

		setEventTableSelectionHandler(eventTable);
		refreshEventTable(eventTable);
	}

	private void displayVendor(Event selectedEvent) {
		TableView<Vendor> vendorTable = new TableView<>();
		setupVendorTable(vendorTable, selectedEvent);

		VBox tableBox = new VBox(10, vendorTable, inviteVendorButton);
		tableBox.setAlignment(Pos.CENTER);
		tableBox.setPadding(new javafx.geometry.Insets(10));
		borderPane.setCenter(tableBox);

		refreshVendorTable(vendorTable, selectedEvent);
	}

	private void displayGuest(Event selectedEvent) {
		TableView<Guest> guestTable = new TableView<>();
		setupGuestTable(guestTable, selectedEvent);

		VBox tableBox = new VBox(10, guestTable, inviteGuestButton);
		tableBox.setAlignment(Pos.CENTER);
		tableBox.setPadding(new javafx.geometry.Insets(10));
		borderPane.setCenter(tableBox);

		refreshGuestTable(guestTable, selectedEvent);
	}

	private void setEventTableSelectionHandler(TableView<Event> eventTable) {
		eventTable.setOnMouseClicked(event -> {
			if (event.getClickCount() == 2) { // Detect double-click
				Event selectedEvent = eventTable.getSelectionModel().getSelectedItem();
				if (selectedEvent != null) {
					System.out.println("Selected Event ID: " + selectedEvent.getEvent_id());
					viewEventDetails(selectedEvent.getEvent_id());
				}
			}
		});
	}

	private void viewEventDetails(String eventId) {
		eventDetailStage = new Stage();
		detailRoot = new BorderPane();
	    
		 eventDetailStage.setWidth(500); 
		 eventDetailStage.setHeight(500); 
		    
		Event selectedEvent = eventController.getEventsByOrganizer(currentUserId).stream()
				.filter(event -> event.getEvent_id().equals(eventId)).findFirst().orElse(null);

		if (selectedEvent != null) {
			List<String> acceptedVendor = EOController.getVendorsByTransactionID(eventId);
            List<String> acceptedGuest = EOController.getGuestsByTransactionID(eventId);
            String eventDetailsText = EOController.viewEventDetails(selectedEvent, acceptedVendor, acceptedGuest);

			TextField eventNameField = new TextField(selectedEvent.getEvent_name());
			eventNameField.setPromptText("Update event name");

			Button updateButton = new Button("Update Event Name");
			updateButton.setOnAction(e -> {
				String updatedName = eventNameField.getText();
				if (!updatedName.isEmpty()) {
					selectedEvent.setEvent_name(updatedName);
					eventController.editEventName(eventId, updatedName);
					Alert alert = new Alert(Alert.AlertType.INFORMATION, "Event name updated successfully!");
					alert.show();
					refreshEventTable(eventTable);
				} else {
					Alert alert = new Alert(Alert.AlertType.WARNING, "Event name cannot be empty.");
					alert.show();
				}
			});
			
			Label eventDetails = new Label(eventDetailsText);
			VBox detailBox = new VBox(10, eventDetails, eventNameField, updateButton);
			detailBox.setAlignment(Pos.CENTER);
			detailBox.setPadding(new javafx.geometry.Insets(10));
			detailRoot.setCenter(detailBox);
		}

		Scene detailScene = new Scene(detailRoot, 400, 300);
		eventDetailStage.setScene(detailScene);
		eventDetailStage.setTitle("Event Details");
		eventDetailStage.show();
	}

	private void setupEventTable(TableView<Event> eventTable) {

		eventTable.getColumns().clear();
		TableColumn<Event, String> idColumn = new TableColumn<>("Event_ID");
		idColumn.setCellValueFactory(new PropertyValueFactory<>("event_id"));

		TableColumn<Event, String> usernameColumn = new TableColumn<>("Event_Name");
		usernameColumn.setCellValueFactory(new PropertyValueFactory<>("event_name"));

		TableColumn<Event, String> emailColumn = new TableColumn<>("Event_Date");
		emailColumn.setCellValueFactory(new PropertyValueFactory<>("event_date"));

		TableColumn<Event, String> roleColumn = new TableColumn<>("Event_Location");
		roleColumn.setCellValueFactory(new PropertyValueFactory<>("event_location"));

		TableColumn<Event, Void> addVendorButtonColumn = new TableColumn<>("Add Vendor");
		addVendorButtonColumn.setCellFactory(col -> {
			TableCell<Event, Void> cell = new TableCell<Event, Void>() {
				private final Button addVendorButton = new Button("Add Vendor");

				{
					addVendorButton.setOnAction(event -> {
						Event selectedEvent = getTableView().getItems().get(getIndex());
						displayVendor(selectedEvent);
					});
				}

				@Override
				public void updateItem(Void item, boolean empty) {
					super.updateItem(item, empty);
					if (empty) {
						setGraphic(null);
					} else {
						setGraphic(addVendorButton);
					}
				}
			};
			return cell;
		});

		TableColumn<Event, Void> addGuestButtonColumn = new TableColumn<>("Add Guest");
		addGuestButtonColumn.setCellFactory(col -> {
			TableCell<Event, Void> cell = new TableCell<Event, Void>() {
				private final Button addGuestButton = new Button("Add Guest");

				{
					addGuestButton.setOnAction(event -> {
						Event selectedEvent = getTableView().getItems().get(getIndex());
						displayGuest(selectedEvent);
					});
				}

				@Override
				public void updateItem(Void item, boolean empty) {
					super.updateItem(item, empty);
					if (empty) {
						setGraphic(null);
					} else {
						setGraphic(addGuestButton);
					}
				}
			};
			return cell;
		});

		eventTable.getColumns().addAll(idColumn, usernameColumn, emailColumn, roleColumn, addVendorButtonColumn,
				addGuestButtonColumn);
		eventTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
	}

	private void setupVendorTable(TableView<Vendor> vendorTable, Event selectedEvent) {

		TableColumn<Vendor, String> idColumn = new TableColumn<>("Vendor_ID");
		idColumn.setCellValueFactory(new PropertyValueFactory<>("user_id"));

		TableColumn<Vendor, String> usernameColumn = new TableColumn<>("Vendor_Name");
		usernameColumn.setCellValueFactory(new PropertyValueFactory<>("user_name"));

		TableColumn<Vendor, String> emailColumn = new TableColumn<>("Vendor_Email");
		emailColumn.setCellValueFactory(new PropertyValueFactory<>("user_email"));

		TableColumn<Vendor, String> roleColumn = new TableColumn<>("Vendor_Role");
		roleColumn.setCellValueFactory(new PropertyValueFactory<>("user_role"));

		TableColumn<Vendor, Boolean> selectColumn = new TableColumn<>("Select");
		selectColumn.setCellFactory(column -> new TableCell<>() {
			private final CheckBox checkBox = new CheckBox();

			{
				checkBox.setOnAction(event -> {
					Vendor vendor = getTableView().getItems().get(getIndex());
					vendor.setSelected(checkBox.isSelected());
				});
			}

			@Override
			protected void updateItem(Boolean item, boolean empty) {
				super.updateItem(item, empty);
				if (empty) {
					setGraphic(null);
				} else {
					setGraphic(checkBox);
					Vendor vendor = getTableView().getItems().get(getIndex());
					checkBox.setSelected(vendor.isSelected());
				}
			}
		});

		vendorTable.getColumns().addAll(idColumn, usernameColumn, emailColumn, roleColumn, selectColumn);
		vendorTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		List<String> invitedVendorIds = EOController.getVendorsByTransactionID(selectedEvent.getEvent_id());
		List<Vendor> allVendors = EOController.getAllVendor();

		List<Vendor> availableVendors = allVendors.stream()
				.filter(vendor -> !invitedVendorIds.contains(vendor.getUser_id())).collect(Collectors.toList());

		vendorObsList = FXCollections.observableArrayList(availableVendors);

		vendorTable.setItems(vendorObsList);

		inviteVendorButton.setOnAction(event -> {
			ObservableList<Vendor> selectedVendors = vendorTable.getItems().filtered(Vendor::isSelected);
			String validateError  = EOController.checkAddVendorInput(selectedVendors);
			if (validateError != null) {
				Alert alert = new Alert(Alert.AlertType.WARNING, validateError);
				alert.show();
			} else {

				for (Vendor vendor : selectedVendors) {
					System.out.println(selectedEvent.getEvent_id() + vendor.getUser_id());
					invitationController.sendInvitation(selectedEvent.getEvent_id(), vendor.getUser_id());
				}

				Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Vendors invited successfully!");
				successAlert.show();

				refreshVendorTable(vendorTable, selectedEvent);
			}
		});
	}

	private void setupGuestTable(TableView<Guest> guestTable, Event selectedEvent) {

		TableColumn<Guest, String> idColumn = new TableColumn<>("Guest_ID");
		idColumn.setCellValueFactory(new PropertyValueFactory<>("user_id"));

		TableColumn<Guest, String> usernameColumn = new TableColumn<>("Guest_Name");
		usernameColumn.setCellValueFactory(new PropertyValueFactory<>("user_name"));

		TableColumn<Guest, String> emailColumn = new TableColumn<>("Guest_Email");
		emailColumn.setCellValueFactory(new PropertyValueFactory<>("user_email"));

		TableColumn<Guest, String> roleColumn = new TableColumn<>("Guest_Role");
		roleColumn.setCellValueFactory(new PropertyValueFactory<>("user_role"));

		TableColumn<Guest, Boolean> selectColumn = new TableColumn<>("Select");
		selectColumn.setCellFactory(column -> new TableCell<>() {
			private final CheckBox checkBox = new CheckBox();

			{
				checkBox.setOnAction(event -> {
					Guest guest = getTableView().getItems().get(getIndex());
					guest.setSelected(checkBox.isSelected());
				});
			}

			@Override
			protected void updateItem(Boolean item, boolean empty) {
				super.updateItem(item, empty);
				if (empty) {
					setGraphic(null);
				} else {
					setGraphic(checkBox);
					Guest guest = getTableView().getItems().get(getIndex());
					checkBox.setSelected(guest.isSelected());
				}
			}
		});

		guestTable.getColumns().addAll(idColumn, usernameColumn, emailColumn, roleColumn, selectColumn);
		guestTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		List<String> invitedGuestIds = EOController.getGuestsByTransactionID(selectedEvent.getEvent_id());
		List<Guest> allGuest = EOController.getAllGuest();

		List<Guest> availableGuests = allGuest.stream().filter(guest -> !invitedGuestIds.contains(guest.getUser_id()))
				.collect(Collectors.toList());

		guestObsList = FXCollections.observableArrayList(availableGuests);

		guestTable.setItems(guestObsList);

		inviteGuestButton.setOnAction(event -> {
			ObservableList<Guest> selectedGuests = guestTable.getItems().filtered(Guest::isSelected);
			String validateError = EOController.checkAddGuestInput(selectedGuests);
			
			
			if (validateError != null) { 
				Alert alert = new Alert(Alert.AlertType.WARNING, validateError);
				alert.show();
			} else {

				for (Guest guest : selectedGuests) {
					System.out.println(selectedEvent.getEvent_id() + guest.getUser_id());
					invitationController.sendInvitation(selectedEvent.getEvent_id(), guest.getUser_id());
				}

				Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Guests invited successfully!");
				successAlert.show();

				refreshGuestTable(guestTable, selectedEvent);
			}
		});
	}

	private void refreshEventTable(TableView<Event> eventTable) {
		eventList = eventController.getEventsByOrganizer(currentUserId);

		eventObsList = FXCollections.observableArrayList(eventList);
		eventTable.setItems(eventObsList);
		eventTable.refresh();
	}

	private void refreshVendorTable(TableView<Vendor> vendorTable, Event selectedEvent) {
		List<String> invitedVendorIds = EOController.getVendorsByTransactionID(selectedEvent.getEvent_id());
		List<Vendor> allVendors = EOController.getAllVendor();
		List<Vendor> availableVendors = allVendors.stream()
				.filter(vendor -> !invitedVendorIds.contains(vendor.getUser_id())).collect(Collectors.toList());

		if (vendorList == null) {
			System.out.println("Vendor list is null!");
			vendorList = new ArrayList<>();
		}

		vendorObsList = FXCollections.observableArrayList(availableVendors);
		vendorTable.setItems(vendorObsList);
		vendorTable.refresh();
	}

	private void refreshGuestTable(TableView<Guest> guestTable, Event selectedEvent) {
		List<String> invitedGuestIds = EOController.getGuestsByTransactionID(selectedEvent.getEvent_id());
		List<Guest> allGuests = EOController.getAllGuest();
		List<Guest> availableGuests = allGuests.stream().filter(guest -> !invitedGuestIds.contains(guest.getUser_id()))
				.collect(Collectors.toList());
		guestList = EOController.getAllGuest();

		if (guestList == null) {
			System.out.println("Guest list is null!");
			guestList = new ArrayList<>();
		}

		guestObsList = FXCollections.observableArrayList(availableGuests);
		guestTable.setItems(guestObsList);
		guestTable.refresh();
	}

	public void layout() {
		borderPane.setTop(menuBar);
		borderPane.setCenter(welcome);
	}

	private void handleViewOrganizedEvent() {
		displayEvent();
	}

	private void handleCreateEvent(String currentID) {
		TextField eventNameField = new TextField();
		eventNameField.setPromptText("Enter Event Name");
		DatePicker eventDatePicker = new DatePicker();
		eventDatePicker.setPromptText("Select Event Date");
		TextField eventLocationField = new TextField();
		eventLocationField.setPromptText("Enter Event Location");
		TextField eventDescriptionField = new TextField();
		eventDescriptionField.setPromptText("Enter Event Description");

		Button submitButton = new Button("Submit");
		submitButton.setOnAction(e -> {
			String eventName = eventNameField.getText();
			String eventLocation = eventLocationField.getText();
			String eventDescription = eventDescriptionField.getText();
			String eventDate = (eventDatePicker.getValue() != null) ? eventDatePicker.getValue().toString()
					: "No date selected";
			String eventID = eventController.generateNewEventID();

			Event event = new Event(eventID, eventName, eventDate, eventLocation, eventDescription, currentID);
			String validationMessage = EOController.checkCreateEventInput(event);

			if (validationMessage != null) {
				showAlert("Validation Error! ", validationMessage);
			} else {
				EOController.createEvent(event);
				eventNameField.clear();
				eventDatePicker.setValue(null);
				eventLocationField.clear();
				eventDescriptionField.clear();	
				showAlert("Event Created", "The event has been successfully created!");
			}

		});

		GridPane inputLayout = new GridPane();
		inputLayout.setVgap(15);
		inputLayout.setHgap(10);
		inputLayout.setPadding(new Insets(20));
		inputLayout.setAlignment(Pos.CENTER_LEFT);

		inputLayout.add(new Label("Event Name:"), 0, 0);
		inputLayout.add(eventNameField, 1, 0);
		inputLayout.add(new Label("Event Date:"), 0, 1);
		inputLayout.add(eventDatePicker, 1, 1);
		inputLayout.add(new Label("Event Location:"), 0, 2);
		inputLayout.add(eventLocationField, 1, 2);
		inputLayout.add(new Label("Event Description:"), 0, 3);
		inputLayout.add(eventDescriptionField, 1, 3);
		inputLayout.add(submitButton, 1, 4);

		borderPane.setCenter(inputLayout);
	}

	private void setEventHandler(String currentID) {
		viewEvent.setOnAction(e -> handleViewOrganizedEvent());
		addEvent.setOnAction(e -> handleCreateEvent(currentID));
		viewProfile.setOnAction(e -> displayProfile());
	}

}
