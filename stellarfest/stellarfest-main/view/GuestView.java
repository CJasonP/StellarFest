package view;

import java.util.List;

import controller.EventController;
import controller.GuestController;
import controller.InvitationController;
import controller.UserController;
import database.UserSession;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.stage.Stage;
import models.Event;
import models.Invitation;
import models.User;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class GuestView{
	private BorderPane borderPane;
	private Scene scene;
	private MenuBar menuBar;
	private Menu eventMenu, invitationMenu, profileMenu;
	private MenuItem viewAcceptedEvents, viewEventInvitations, profileItem;
	private Label welcome;

	private TableView<Invitation> invitationTable;
	private TableView<Event> acceptedEventsTable;
	private InvitationController invitationController;
	private EventController eventController;
	private UserController userController;
	private GuestController guestController;
	private User currentUser;
	private String currentUserId;

	public void initialize() {
		currentUserId = UserSession.getUserId();
		invitationController = new InvitationController();
		eventController = new EventController();
		userController = new UserController();
		guestController = new GuestController();

		welcome = new Label("Welcome, Guest! View your events and invitations through the navigation bar.");
		menuBar = new MenuBar();

		invitationMenu = new Menu("Invitation");
		eventMenu = new Menu("Event");
		profileMenu = new Menu("Profile");

		viewEventInvitations = new MenuItem("View Event Invitations");
		viewAcceptedEvents = new MenuItem("View Accepted Events");
		profileItem = new MenuItem("Manage Profile");

		invitationMenu.getItems().add(viewEventInvitations);
		eventMenu.getItems().add(viewAcceptedEvents);
		profileMenu.getItems().add(profileItem);
		menuBar.getMenus().addAll(eventMenu, invitationMenu, profileMenu);

		borderPane = new BorderPane();
		scene = new Scene(borderPane, 1000, 600);
	}

	public void layout() {
		borderPane.setTop(menuBar);
		borderPane.setCenter(welcome);
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
	
	private void setEventHandler(String currentID) {
		viewAcceptedEvents.setOnAction(e -> displayAcceptedEvent(currentID));
		viewEventInvitations.setOnAction(e -> displayInvitations(currentID));
		profileItem.setOnAction(e -> displayProfile());

	}

	public Scene createGuestViewScene(Stage stage) {
		initialize();
		layout();
		String currentUserId = UserSession.getUserId();
		setEventHandler(currentUserId);
		return scene;
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
		List<Event> acceptedEventList = guestController.viewAcceptedEvents(userId);
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
