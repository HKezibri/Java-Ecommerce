package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.User;
import service.AuthentificationService;

public class LoginController {

    @FXML
    private TextField LoginUsernameField;

    @FXML
    private PasswordField LoginpasswordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label errorLabel;
    
    @FXML
    private Hyperlink RegisterPage;

    private final AuthentificationService authService = new AuthentificationService();

    /**
     * Handles login attempts by validating inputs and authenticating the user.
     */
    @FXML
    void handleLogin(ActionEvent event) {
        // Reset the error label and its color
        resetErrorLabel();

        // Retrieve input from text fields
        String username = LoginUsernameField.getText().trim();
        String password = LoginpasswordField.getText().trim();

        // Validate input
        if (username.isEmpty() || password.isEmpty()) {
            setError("Please enter both username and password.");
            return;
        }

        try {
            // Authenticate the user
            User authenticatedUser = authService.authenticate(username, password);

            // Determine the FXML to load based on the user's role
            FXMLLoader loader;
            String fxmlPath;
            if (authenticatedUser instanceof model.Admin) {
                fxmlPath = "/view/AdminPage.fxml";
            } else if (authenticatedUser instanceof model.Client) {
                fxmlPath = "/view/ClientShoppingPage.fxml";
            } else {
                setError("Unrecognized user role.");
                return;
            }

            // Load the FXML
            loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load(); // Load FXML first before accessing the controller

            // Get the controller and set the logged-in user
            Object controller = loader.getController();
            if (controller instanceof AdminPageController) {
                ((AdminPageController) controller).setLoggedInUser(authenticatedUser);
            } else if (controller instanceof ClientShoppingController) {
                ((ClientShoppingController) controller).setLoggedInUser(authenticatedUser);
            }

            // Navigate to the appropriate page
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(authenticatedUser instanceof model.Admin ? "Admin Dashboard" : "Client Shopping");
            stage.show();

        } catch (AuthentificationService.AuthenticationException e) {
            setError("Login failed: " + e.getMessage());
        
        } catch (Exception e) {
            setError("An unexpected error occurred.");
            e.printStackTrace(); // Log detailed stack trace for debugging
        }
    }


    /**
     * Navigates to the registration page.
     */
    @FXML
    void handleRegisterPage(ActionEvent event) {
        try {
            // Load the Register.fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Register.fxml"));
            Parent root = loader.load();

            // Get the current stage
            Stage currentStage = (Stage) RegisterPage.getScene().getWindow();

            // Set the new scene and title
            currentStage.setScene(new Scene(root));
            currentStage.setTitle("Register");

            // Show the registration page
            currentStage.show();
        } catch (Exception e) {
            setError("Failed to navigate to the register page.");
            e.printStackTrace(); // Log stack trace for debugging
        }
    }

    /**
     * Resets the error label to its default state.
     */
    private void resetErrorLabel() {
        errorLabel.setText("");
        errorLabel.setStyle("");
    }

    /**
     * Sets an error message to the error label with red text color.
     *
     * @param message The error message to display.
     */
    private void setError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: red;"); // Style for error
    }

    /**
     * Sets a success message to the error label with green text color.
     *
     * @param message The success message to display.
     */
    private void setSuccess(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: green;"); // Style for success
    }
}
