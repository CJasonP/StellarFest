package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.ArrayList;

import controller.UserController;
import database.UserDAO;
import models.User;

public class RegisterPage {
    private final UserDAO userDAO = new UserDAO();
    private final UserController userController = new UserController();

    public Scene createRegisterScene(Stage stage, Scene loginScene) {
        GridPane registerPane = new GridPane();
        registerPane.setPadding(new Insets(10));
        registerPane.setVgap(8);
        registerPane.setHgap(10);
        registerPane.setAlignment(Pos.CENTER);

        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        registerPane.add(usernameLabel, 0, 0);
        registerPane.add(usernameField, 1, 0);

        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField();
        registerPane.add(emailLabel, 0, 1);
        registerPane.add(emailField, 1, 1);

        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        registerPane.add(passwordLabel, 0, 2);
        registerPane.add(passwordField, 1, 2);

        Label roleLabel = new Label("Role:");
        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("Vendor", "Event Organizer", "Guest");
        registerPane.add(roleLabel, 0, 3);
        registerPane.add(roleComboBox, 1, 3);

        Button registerButton = new Button("Register");
        registerButton.setOnAction(e -> {
            String username = usernameField.getText();
            String email = emailField.getText();
            String password = passwordField.getText();
            String role = roleComboBox.getValue();
            
            String error = userController.checkRegisterInput(username, email, password, role);
            
            if (error == null) {
            	String newID = userDAO.generateNewUserID();
                User newUser = new User(newID, username, email, password, role);
                userDAO.insertUser(newUser);
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Registration successful!", ButtonType.OK);
                alert.showAndWait();
                stage.setScene(loginScene);
            }
            else {
                showAlert(error);
            }
        });
        registerPane.add(registerButton, 1, 4);

        Button goToLoginButton = new Button("Already have an account? Login here!");
        goToLoginButton.setOnAction(e -> stage.setScene(loginScene));
        registerPane.add(goToLoginButton, 1, 5);

        return new Scene(registerPane, 1000, 600);
    }
    
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.showAndWait();
    }
}