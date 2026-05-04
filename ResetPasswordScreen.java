import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

public class ResetPasswordScreen extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("SmartVote — Reset Password");
        stage.setWidth(420);
        stage.setHeight(460);
        stage.initModality(Modality.APPLICATION_MODAL);

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #0f172a;");

        // Header
        VBox header = new VBox(6);
        header.setStyle("-fx-background-color: #1e293b; -fx-padding: 24 28; "
                + "-fx-border-width: 0 0 1 0; -fx-border-color: #334155;");
        Label title = new Label("Reset Password");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; "
                + "-fx-text-fill: #f1f5f9; -fx-font-family: 'Segoe UI';");
        Label sub = new Label("Enter your student ID and current password");
        sub.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; "
                + "-fx-font-family: 'Segoe UI';");
        header.getChildren().addAll(title, sub);

        // Form
        VBox form = new VBox(14);
        form.setStyle("-fx-padding: 28;");

        TextField idField = LoginScreen.formField("Student ID");
        PasswordField oldPassField = (PasswordField) LoginScreen.formFieldPassword("Current Password");
        PasswordField newPassField = (PasswordField) LoginScreen.formFieldPassword("New Password");
        PasswordField confirmField = (PasswordField) LoginScreen.formFieldPassword("Confirm New Password");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #f87171; -fx-font-size: 12px;");
        errorLabel.setVisible(false);

        Button resetBtn = LoginScreen.primaryButton("Update Password");
        resetBtn.setMaxWidth(Double.MAX_VALUE);

        resetBtn.setOnAction(e -> {
            String idText = idField.getText().trim();
            String oldPass = oldPassField.getText();
            String newPass = newPassField.getText();
            String confirm = confirmField.getText();

            if (idText.isEmpty() || oldPass.isEmpty() || newPass.isEmpty()) {
                errorLabel.setText("All fields are required.");
                errorLabel.setVisible(true); return;
            }
            if (!newPass.equals(confirm)) {
                errorLabel.setText("New passwords do not match.");
                errorLabel.setVisible(true); return;
            }
            if (newPass.length() < 4) {
                errorLabel.setText("Password must be at least 4 characters.");
                errorLabel.setVisible(true); return;
            }
            try {
                int id = Integer.parseInt(idText);
                boolean ok = Main.resetPassword(id, oldPass, newPass);
                if (ok) {
                    Alert a = new Alert(Alert.AlertType.INFORMATION);
                    a.setHeaderText(null);
                    a.setContentText("Password updated successfully!");
                    a.getDialogPane().setStyle("-fx-background-color: #1e293b;");
                    a.showAndWait();
                    stage.close();
                } else {
                    errorLabel.setText("Incorrect student ID or current password.");
                    errorLabel.setVisible(true);
                }
            } catch (NumberFormatException ex) {
                errorLabel.setText("Student ID must be a number.");
                errorLabel.setVisible(true);
            }
        });

        Button cancelBtn = LoginScreen.secondaryButton("Cancel");
        cancelBtn.setMaxWidth(Double.MAX_VALUE);
        cancelBtn.setOnAction(e -> stage.close());

        form.getChildren().addAll(
            LoginScreen.fieldLabel("Student ID"), idField,
            LoginScreen.fieldLabel("Current Password"), oldPassField,
            LoginScreen.fieldLabel("New Password"), newPassField,
            LoginScreen.fieldLabel("Confirm New Password"), confirmField,
            errorLabel, resetBtn, cancelBtn
        );

        root.getChildren().addAll(header, form);
        stage.setScene(new Scene(root));
        stage.show();
    }
}
