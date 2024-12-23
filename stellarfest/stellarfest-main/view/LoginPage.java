package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import database.UserSession;

import java.util.Optional;

import controller.UserController;
import models.User;

public class LoginPage {
	private final UserController userController = new UserController();

	public Scene createLoginScene(Stage stage, Scene registerScene) {
		GridPane loginPane = new GridPane();
		loginPane.setPadding(new Insets(10));
		loginPane.setVgap(8);
		loginPane.setHgap(10);
		loginPane.setAlignment(Pos.CENTER);

		Label emailLabel = new Label("Email:");
		TextField emailField = new TextField();
		loginPane.add(emailLabel, 0, 0);
		loginPane.add(emailField, 1, 0);

		Label passwordLabel = new Label("Password:");
		PasswordField passwordField = new PasswordField();
		loginPane.add(passwordLabel, 0, 1);
		loginPane.add(passwordField, 1, 1);

		Button loginButton = new Button("Login");
		loginButton.setOnAction(e -> {
			String email = emailField.getText().trim();
			String password = passwordField.getText().trim();

			String validateMessage = userController.checkLoginInput(email, password);

			if (validateMessage != null) {
				Alert alert = new Alert(Alert.AlertType.WARNING, validateMessage, ButtonType.OK);
				alert.showAndWait();
				return;
			} else {
				Optional<User> user = userController.login(email, password);
				if (user.isPresent()) {
					Alert alert = new Alert(Alert.AlertType.INFORMATION, "Login successful!", ButtonType.OK);
					alert.showAndWait();

					String currentID = UserSession.getUserId();
					String role = user.get().getUser_role(); // Assume User has a getRole() method
					navigateToRoleView(stage, role);
				} else {
					Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid email or password!", ButtonType.OK);
					alert.showAndWait();
				}
			}

		});
		loginPane.add(loginButton, 1, 2);

		Button goToRegisterButton = new Button("Don't have an account? Register here!");
		goToRegisterButton.setOnAction(e -> stage.setScene(registerScene));
		loginPane.add(goToRegisterButton, 1, 3);

		return new Scene(loginPane, 1000, 600);
	}

	private void navigateToRoleView(Stage stage, String role) {
		Scene roleScene;
		switch (role) {
		case "Admin":
			AdminView adminView = new AdminView();
			roleScene = adminView.createAdminViewScene(stage);
			break;
		case "Vendor":
			VendorView vendorView = new VendorView();
			roleScene = vendorView.createVendorViewScene(stage);
			break;
		case "Event Organizer":
			EventOrganizerView eoView = new EventOrganizerView();
			roleScene = eoView.createEventOrganizerViewScene(stage);
			break;
		case "Guest":
			GuestView guestView = new GuestView();
			roleScene = guestView.createGuestViewScene(stage);
			break;
		default:
			Alert alert = new Alert(Alert.AlertType.ERROR, "Unknown role!", ButtonType.OK);
			alert.showAndWait();
			return;
		}
		stage.setScene(roleScene);
	}

}