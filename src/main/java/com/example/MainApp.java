package com.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class MainApp extends Application {
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/login_schema";
    private static final String USER = "root";
    private static final String PASSWORD = "ricky";

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Login Form");
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(8);
        grid.setHgap(10);

        Label usernameLabel = new Label("Username:");
        GridPane.setConstraints(usernameLabel, 0, 0);
        TextField usernameField = new TextField();
        GridPane.setConstraints(usernameField, 1, 0);

        Label passwordLabel = new Label("Password:");
        GridPane.setConstraints(passwordLabel, 0, 1);
        PasswordField passwordField = new PasswordField();
        GridPane.setConstraints(passwordField, 1, 1);

        Button loginButton = new Button("Login");
        GridPane.setConstraints(loginButton, 1, 2);
        loginButton.setOnAction(e -> performLogin(usernameField, passwordField));

        Button registerButton = new Button("Register");
        GridPane.setConstraints(registerButton, 1, 3);
        registerButton.setOnAction(e -> showRegistrationDialog());

        grid.getChildren().addAll(usernameLabel, usernameField, passwordLabel, passwordField, loginButton, registerButton);

        Scene scene = new Scene(grid, 300, 200);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void performLogin(TextField usernameField, PasswordField passwordField) {
        String usernameInput = usernameField.getText();
        String passwordInput = passwordField.getText();
        if (authenticateUser(usernameInput, passwordInput)) {
            showAlert(Alert.AlertType.INFORMATION, "Login Successful", "Login successful!");
        } else {
            showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid username or password.");
        }
    }

    private boolean authenticateUser(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showRegistrationDialog() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Register");
        dialog.setHeaderText("Register New User");
        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        PasswordField confirmPasswordField = new PasswordField();
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(new Label("Confirm Password:"), 0, 2);
        grid.add(confirmPasswordField, 1, 2);
        dialog.getDialogPane().setContent(grid);
        ButtonType okButtonType = new ButtonType("Register", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return new Pair<>(usernameField.getText(), passwordField.getText());
            }
            return null;
        });
        Optional<Pair<String, String>> result = dialog.showAndWait();
        if (result.isPresent()) {
            String usernameInput = result.get().getKey();
            String passwordInput = result.get().getValue();
            String confirmPasswordInput = confirmPasswordField.getText();

            if (passwordInput.equals(confirmPasswordInput)) {
                if (addUserToDatabase(usernameInput, passwordInput)) {
                    showAlert(Alert.AlertType.INFORMATION, "Registration Successful", "User registered successfully!");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Registration Failed", "User registration failed. Maybe the username already exists.");
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Registration Failed", "Passwords do not match.");
            }
        }
    }

    private boolean addUserToDatabase(String username, String password) {
        String query = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            statement.setString(2, password);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.verbose", "true");
        launch(args);
    }
}
