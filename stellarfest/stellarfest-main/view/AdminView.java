package view;

import controller.AdminController;
import controller.UserController;
import database.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import models.User;
import models.Vendor;
import models.Event;
import models.Guest;


public class AdminView {
    private TableView<User> userTable;
    private ArrayList<User> userList;
    private AdminController adminController;
    private UserController userController;
    private String selectedUserId = null;
    private BorderPane root;
    private MenuBar menuBar;
    private Menu usersMenu, eventsMenu, profileMenu;
    private Label welcome;
    private String currentUserId;
    private User currentUser;
    
    public void initialize()
    {
    	welcome = new Label("Welcome, Admin!");
    	adminController = new AdminController();
        userController = new UserController();
    	root = new BorderPane();
    	currentUserId = UserSession.getUserId();

    	menuBar = new MenuBar();
        usersMenu = new Menu("Users");
        eventsMenu = new Menu("Event");
        profileMenu = new Menu("Profile");

        MenuItem usersItem = new MenuItem("Manage Users");
        usersItem.setOnAction(e -> displayUsers());
        MenuItem eventsItem = new MenuItem("Manage Events");
        eventsItem.setOnAction(e -> displayEvent());
        MenuItem profileItem = new MenuItem("Manage Profile");
        profileItem.setOnAction(e -> displayProfile());

        usersMenu.getItems().add(usersItem);
        eventsMenu.getItems().add(eventsItem);
        profileMenu.getItems().add(profileItem);

        menuBar.getMenus().addAll(usersMenu, eventsMenu, profileMenu);
    }
    
    public void layout()
    {
        root.setTop(menuBar);
        root.setCenter(welcome);
    }
    
    public Scene createAdminViewScene(Stage stage) {
        initialize();
        layout();

        return new Scene(root, 1000, 600);
    }

    private void displayUsers() {
        userTable = new TableView<>();
        setupUserTable();

        Button deleteButton = new Button("Delete");
        deleteButton.setDisable(true);
        deleteButton.setOnAction(e -> deleteUser());

        VBox tableBox = new VBox(10, userTable, deleteButton);
        tableBox.setAlignment(Pos.CENTER);
        tableBox.setPadding(new javafx.geometry.Insets(10));
        root.setCenter(tableBox);

        refreshUserTable();
        setTableSelectionHandler(deleteButton);
    }

    private void setupUserTable() {
        TableColumn<User, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("user_id"));

        TableColumn<User, String> usernameColumn = new TableColumn<>("Username");
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("user_name"));

        TableColumn<User, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("user_email"));

        TableColumn<User, String> roleColumn = new TableColumn<>("Role");
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("user_role"));

        userTable.getColumns().addAll(idColumn, usernameColumn, emailColumn, roleColumn);
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void refreshUserTable() {
        userList = adminController.getAllUsers();
        ObservableList<User> userObsList = FXCollections.observableArrayList(userList);
        userTable.setItems(userObsList);
    }

    private void deleteUser() {
        if (selectedUserId != null) {
            adminController.deleteUser(selectedUserId);
            refreshUserTable();
            selectedUserId = null;
        }
    }

    private void setTableSelectionHandler(Button deleteButton) {
        userTable.setOnMouseClicked((MouseEvent event) -> {
            User selectedUser = userTable.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                selectedUserId = selectedUser.getUser_id();
                deleteButton.setDisable(false);
            } else {
                deleteButton.setDisable(true);
            }
        });
    }

    private void displayEvent() {
        TableView<Event> eventTable = new TableView<>();
        setupEventTable(eventTable);

        Label instructionLabel = new Label("Double-click an event to view its details.");
        Button deleteEventButton = new Button("Delete Selected Event");
        deleteEventButton.setOnAction(e -> {
            Event selectedEvent = eventTable.getSelectionModel().getSelectedItem();
            if (selectedEvent != null) {
                boolean confirmation = confirmDeletion("Are you sure you want to delete this event?");
                if (confirmation) {
                    adminController.deleteEvent(selectedEvent.getEvent_id());
                    refreshEventTable(eventTable);
                }
            } else {
                showAlert("No Event Selected", "Please select an event to delete.");
            }
        });


        VBox tableBox = new VBox(10, instructionLabel, eventTable, deleteEventButton);
        tableBox.setAlignment(Pos.CENTER);
        tableBox.setPadding(new javafx.geometry.Insets(10));
        root.setCenter(tableBox);

        refreshEventTable(eventTable);
        setEventTableSelectionHandler(eventTable);
    }

    private void setupEventTable(TableView<Event> eventTable) {
        TableColumn<Event, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("event_id"));

        TableColumn<Event, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("event_name"));

        TableColumn<Event, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("event_date"));

        eventTable.getColumns().addAll(idColumn, nameColumn, dateColumn);
        eventTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void refreshEventTable(TableView<Event> eventTable) {
        ArrayList<Event> eventList = adminController.viewAllEvents();
        ObservableList<Event> eventObsList = FXCollections.observableArrayList(eventList);
        eventTable.setItems(eventObsList);
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
        
        eventDetailStage.setWidth(500); 
	    eventDetailStage.setHeight(500); 
	    
        Event selectedEvent = adminController.viewAllEvents()
                .stream()
                .filter(event -> event.getEvent_id().equals(eventId))
                .findFirst()
                .orElse(null);

        if (selectedEvent != null) {
            // Get accepted vendor and guest IDs
            List<String> acceptedVendor = adminController.getVendorsByTransactionID(eventId);
            List<String> acceptedGuest = adminController.getGuestsByTransactionID(eventId);

            String eventDetailsText = adminController.viewEventDetails(selectedEvent, acceptedVendor, acceptedGuest);

            Label eventDetails = new Label(eventDetailsText);
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

    private boolean confirmDeletion(String message) {
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        confirmationAlert.setTitle("Confirm Deletion");
        confirmationAlert.setHeaderText(null);

        ButtonType result = confirmationAlert.showAndWait().orElse(ButtonType.NO);
        return result == ButtonType.YES;
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

            String validationMessage = userController.checkChangeProfileInput(currentUser, newName, newEmail, oldPassword, newPassword);

            if (validationMessage != null) {
                showAlert("Invalid Input", validationMessage);
                return;
            }

            boolean isUpdated = userController.updateUserProfile(currentUserId, newName, newEmail, oldPassword, newPassword);

            if (isUpdated) {
            	showAlert("Success", "Profile updated successfully!");
                currentUser = userController.getUserById(currentUserId);
                oldPasswordField.clear();
                newPasswordField.clear();
            } else {
                showAlert("Update Failed", "Old password is incorrect or update failed.");
            }
        });

        VBox formBox = new VBox(10,
                new Label("Update Profile"),
                new Label("Name:"), nameField,
                new Label("Email:"), emailField,
                new Label("Old Password:"), oldPasswordField,
                new Label("New Password:"), newPasswordField,
                submitButton
        );

        formBox.setAlignment(Pos.CENTER);
        formBox.setPadding(new javafx.geometry.Insets(20));

        root.setCenter(formBox);
    }

    

}
