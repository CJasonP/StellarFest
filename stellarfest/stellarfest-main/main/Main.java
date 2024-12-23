package main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import view.LoginPage;
import view.RegisterPage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        LoginPage loginPage = new LoginPage();
        RegisterPage registerPage = new RegisterPage();
        Scene registerScene;
        Scene loginScene;

        loginScene = loginPage.createLoginScene(primaryStage, null);
        registerScene = registerPage.createRegisterScene(primaryStage, loginScene);
        loginScene = loginPage.createLoginScene(primaryStage, registerScene);
        
        primaryStage.setScene(loginScene);
        primaryStage.setTitle("User Authentication System");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
