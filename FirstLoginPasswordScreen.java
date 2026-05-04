import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.*;

public class FirstLoginPasswordScreen extends Application {

    private int studentId;
    private Stage loginStage;

    public FirstLoginPasswordScreen(int studentId, Stage loginStage) {
        this.studentId = studentId;
        this.loginStage = loginStage;
    }

    public FirstLoginPasswordScreen() {}

    @Override
    public void start(Stage stage) {
        stage.setTitle("SmartVote — Set New Password");
        stage.setWidth(440);
        stage.setHeight(650);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #0f172a;");

        // ── Header ────────────────────────────────────────────────
        VBox header = new VBox(8);
        header.setStyle("-fx-background-color: #1e293b; -fx-padding: 28 30; "
                + "-fx-border-width: 0 0 1 0; -fx-border-color: #334155;");

        // Warning badge
        Label badge = new Label("⚠  First Login Detected");
        badge.setStyle("-fx-background-color: #92400e30; -fx-text-fill: #fbbf24; "
                + "-fx-font-size: 11px; -fx-font-weight: bold; "
                + "-fx-background-radius: 6; -fx-padding: 4 10;");

        Label title = new Label("Set Your Password");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; "
                + "-fx-text-fill: #f1f5f9; -fx-font-family: 'Segoe UI';");

        Label sub = new Label("You must change your default password before you can vote.\nThis is required for account security.");
        sub.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; "
                + "-fx-font-family: 'Segoe UI'; -fx-wrap-text: true;");
        sub.setWrapText(true);

        header.getChildren().addAll(badge, title, sub);

        // ── Form ──────────────────────────────────────────────────
        VBox form = new VBox(14);
        form.setStyle("-fx-padding: 28 30;");

        // Student ID display (read-only info)
        HBox idRow = new HBox(10);
        idRow.setAlignment(Pos.CENTER_LEFT);
        idRow.setStyle("-fx-background-color: #1e293b; -fx-border-color: #334155; "
                + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10 14;");
        Label idIcon = new Label("🎓");
        idIcon.setStyle("-fx-font-size: 14px;");
        Label idLabel = new Label("Student ID: " + studentId);
        idLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8; "
                + "-fx-font-family: 'Segoe UI';");
        idRow.getChildren().addAll(idIcon, idLabel);

        PasswordField currentPassField = (PasswordField) LoginScreen.formFieldPassword("Current (default) password");
        PasswordField newPassField = (PasswordField) LoginScreen.formFieldPassword("New password");
        PasswordField confirmPassField = (PasswordField) LoginScreen.formFieldPassword("Confirm new password");

        // Password strength indicator
        Label strengthLabel = new Label("");
        strengthLabel.setStyle("-fx-font-size: 11px; -fx-font-family: 'Segoe UI';");

        newPassField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty()) {
                strengthLabel.setText("");
            } else if (newVal.length() < 4) {
                strengthLabel.setText("● Weak");
                strengthLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #f87171; "
                        + "-fx-font-family: 'Segoe UI';");
            } else if (newVal.length() < 8) {
                strengthLabel.setText("●● Fair");
                strengthLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #fbbf24; "
                        + "-fx-font-family: 'Segoe UI';");
            } else {
                strengthLabel.setText("●●● Strong");
                strengthLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #22c55e; "
                        + "-fx-font-family: 'Segoe UI';");
            }
        });

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #f87171; -fx-font-size: 12px; "
                + "-fx-wrap-text: true; -fx-font-family: 'Segoe UI';");
        errorLabel.setWrapText(true);
        errorLabel.setVisible(false);

        Button submitBtn = LoginScreen.primaryButton("Set Password & Continue  →");
        submitBtn.setMaxWidth(Double.MAX_VALUE);

        submitBtn.setOnAction(e -> {
            String currentPass = currentPassField.getText();
            String newPass = newPassField.getText();
            String confirmPass = confirmPassField.getText();

            // Validation
            if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                showError(errorLabel, "All fields are required.");
                return;
            }
            if (!newPass.equals(confirmPass)) {
                showError(errorLabel, "New passwords do not match.");
                return;
            }
            if (newPass.length() < 4) {
                showError(errorLabel, "New password must be at least 4 characters.");
                return;
            }
            if (newPass.equals(currentPass)) {
                showError(errorLabel, "New password must be different from current password.");
                return;
            }

            // Reset password using existing Main method
            boolean resetOk = Main.resetPassword(studentId, currentPass, newPass);
            if (!resetOk) {
                showError(errorLabel, "Current password is incorrect. Please try again.");
                return;
            }

            // Mark first login as done
            Main.clearFirstLogin(studentId);

            stage.close();

            // Show success then go to voting
            showSuccessAndProceed(loginStage);
        });

        form.getChildren().addAll(
            LoginScreen.fieldLabel("Logged in as"),
            idRow,
            LoginScreen.fieldLabel("Current Password"),
            currentPassField,
            LoginScreen.fieldLabel("New Password"),
            newPassField,
            strengthLabel,
            LoginScreen.fieldLabel("Confirm New Password"),
            confirmPassField,
            errorLabel,
            submitBtn
        );

        root.getChildren().addAll(header, form);
        stage.setScene(new Scene(root));
        stage.show();
    }

    private void showError(Label label, String msg) {
        label.setText(msg);
        label.setVisible(true);
    }

    private void showSuccessAndProceed(Stage loginStage) {
        Stage successStage = new Stage();
        successStage.setTitle("Password Updated");
        successStage.setWidth(380);
        successStage.setHeight(280);
        successStage.initModality(Modality.APPLICATION_MODAL);

        VBox root = new VBox(16);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #0f172a; -fx-padding: 40;");

        Label check = new Label("✓");
        check.setStyle("-fx-font-size: 52px; -fx-text-fill: #22c55e;");

        Label title = new Label("Password Updated!");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; "
                + "-fx-text-fill: #f1f5f9; -fx-font-family: 'Segoe UI';");

        Label msg = new Label("Your password has been set.\nYou can now cast your vote.");
        msg.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; "
                + "-fx-text-alignment: center; -fx-font-family: 'Segoe UI';");
        msg.setTextAlignment(TextAlignment.CENTER);

        Button proceedBtn = LoginScreen.primaryButton("Proceed to Vote  →");
        proceedBtn.setOnAction(e -> {
            successStage.close();
            try {
                new VotingScreen(studentId).start(new Stage());
                if (loginStage != null) loginStage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        root.getChildren().addAll(check, title, msg, proceedBtn);
        successStage.setScene(new Scene(root));
        successStage.show();
    }
}
