import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.stage.*;

public class LoginScreen extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("SmartVote — Login");
        stage.setWidth(900);
        stage.setHeight(600);
        stage.setResizable(false);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #0f172a;");

        // ── Left panel (branding) ──────────────────────────────────
        VBox leftPanel = new VBox(20);
        leftPanel.setPrefWidth(400);
        leftPanel.setAlignment(Pos.CENTER);
        leftPanel.setStyle("-fx-background-color: #1e293b; -fx-padding: 60;");

        // Ballot icon (SVG-like using shapes)
        StackPane icon = buildBallotIcon();

        Label appTitle = new Label("SmartVote");
        appTitle.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; "
                + "-fx-text-fill: #f8fafc; -fx-font-family: 'Segoe UI';");

        Label tagline = new Label("Secure Digital Voting\nfor Your Campus");
        tagline.setStyle("-fx-font-size: 14px; -fx-text-fill: #94a3b8; "
                + "-fx-text-alignment: center; -fx-font-family: 'Segoe UI';");
        tagline.setTextAlignment(TextAlignment.CENTER);

        // Decorative separator
        Rectangle sep = new Rectangle(60, 3);
        sep.setFill(Color.web("#6366f1"));
        sep.setArcWidth(3);
        sep.setArcHeight(3);

        // Feature bullets
        VBox features = new VBox(12);
        features.setAlignment(Pos.CENTER_LEFT);
        features.getChildren().addAll(
            featureBullet("✦", "One student, one vote — guaranteed"),
            featureBullet("✦", "Real-time results & analytics"),
            featureBullet("✦", "Admin-controlled candidate management")
        );

        leftPanel.getChildren().addAll(icon, appTitle, sep, tagline, features);

        // ── Right panel (login form) ───────────────────────────────
        VBox rightPanel = new VBox(0);
        rightPanel.setAlignment(Pos.CENTER);
        rightPanel.setStyle("-fx-background-color: #0f172a; -fx-padding: 60;");

        // Tab toggle (Student / Admin)
        HBox tabBar = new HBox(0);
        tabBar.setAlignment(Pos.CENTER);
        tabBar.setMaxWidth(340);

        Button[] tabs = {
            styledTab("Student Login", true),
            styledTab("Admin Login", false)
        };

        tabBar.getChildren().addAll(tabs[0], tabs[1]);

        // Form card
        VBox formCard = new VBox(18);
        formCard.setMaxWidth(340);
        formCard.setStyle("-fx-background-color: #1e293b; -fx-padding: 32; "
                + "-fx-border-radius: 16; -fx-background-radius: 16;");

        // ── Student form ──────────────────────────────────────────
        VBox studentForm = buildStudentForm(stage);
        VBox adminForm = buildAdminForm(stage);
        adminForm.setVisible(false);
        adminForm.setManaged(false);

        formCard.getChildren().addAll(studentForm, adminForm);

        // Tab switch logic
        tabs[0].setOnAction(e -> {
            setActiveTab(tabs[0], tabs[1]);
            studentForm.setVisible(true); studentForm.setManaged(true);
            adminForm.setVisible(false); adminForm.setManaged(false);
        });
        tabs[1].setOnAction(e -> {
            setActiveTab(tabs[1], tabs[0]);
            adminForm.setVisible(true); adminForm.setManaged(true);
            studentForm.setVisible(false); studentForm.setManaged(false);
        });

        rightPanel.getChildren().addAll(tabBar, new Spacer(0, 12), formCard);
        root.setLeft(leftPanel);
        root.setCenter(rightPanel);

        stage.setScene(new Scene(root));
        stage.show();
    }

    // ── Student login form ─────────────────────────────────────────
    private VBox buildStudentForm(Stage stage) {
        VBox form = new VBox(14);

        Label heading = new Label("Student Portal");
        heading.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; "
                + "-fx-text-fill: #f1f5f9; -fx-font-family: 'Segoe UI';");

        Label subtext = new Label("Enter your student ID and password to vote");
        subtext.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; "
                + "-fx-font-family: 'Segoe UI'; -fx-wrap-text: true;");

        TextField idField = formField("Student ID (e.g. 2024001)");
        PasswordField passField = (PasswordField) formFieldPassword("Password");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #f87171; -fx-font-size: 12px;");
        errorLabel.setVisible(false);

        Button loginBtn = primaryButton("Login & Vote  →");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        passField.setOnAction(e -> loginBtn.fire());
        idField.setOnAction(e -> loginBtn.fire());

        loginBtn.setOnAction(e -> {
            String idText = idField.getText().trim();
            String pass = passField.getText();
            if (idText.isEmpty() || pass.isEmpty()) {
                errorLabel.setText("Please enter your Student ID and password.");
                errorLabel.setVisible(true);
                return;
            }
            try {
                int id = Integer.parseInt(idText);
                String status = Main.studentLoginStatus(id, pass);
                switch (status) {
                    case "FIRST_LOGIN":
                        new FirstLoginPasswordScreen(id, stage).start(new Stage());
                        break;
                    case "OK":
                        new VotingScreen(id).start(new Stage());
                        stage.close();
                        break;
                    case "ALREADY_VOTED":
                        errorLabel.setText("You have already cast your vote.");
                        errorLabel.setVisible(true);
                        break;
                    default:
                        errorLabel.setText("Invalid Student ID or password.");
                        errorLabel.setVisible(true);
                }
            } catch (NumberFormatException ex) {
                errorLabel.setText("Student ID must be a number.");
                errorLabel.setVisible(true);
            } catch (Exception ex) {
                errorLabel.setText("Connection error. Check database.");
                errorLabel.setVisible(true);
            }
        });

        // Reset password link
        Hyperlink resetLink = new Hyperlink("Forgot password?");
        resetLink.setStyle("-fx-text-fill: #6366f1; -fx-border-color: transparent; "
                + "-fx-font-size: 12px;");
        resetLink.setOnAction(e -> {
            new ResetPasswordScreen().start(new Stage());
        });

        form.getChildren().addAll(heading, subtext, new Spacer(0, 4),
                fieldLabel("Student ID"), idField,
                fieldLabel("Password"), passField,
                errorLabel, loginBtn, resetLink);
        return form;
    }

    // ── Admin login form ───────────────────────────────────────────
    private VBox buildAdminForm(Stage stage) {
        VBox form = new VBox(14);

        Label heading = new Label("Admin Portal");
        heading.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; "
                + "-fx-text-fill: #f1f5f9; -fx-font-family: 'Segoe UI';");

        Label subtext = new Label("Administrator access only");
        subtext.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; "
                + "-fx-font-family: 'Segoe UI';");

        TextField userField = formField("Username");
        PasswordField passField = (PasswordField) formFieldPassword("Password");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #f87171; -fx-font-size: 12px;");
        errorLabel.setVisible(false);

        Button loginBtn = primaryButton("Access Dashboard  →");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setStyle(loginBtn.getStyle() + "-fx-background-color: #7c3aed;");
        passField.setOnAction(e -> loginBtn.fire());
        userField.setOnAction(e -> loginBtn.fire());

        loginBtn.setOnAction(e -> {
            String user = userField.getText().trim();
            String pass = passField.getText();
            if (user.isEmpty() || pass.isEmpty()) {
                errorLabel.setText("Please fill in both fields.");
                errorLabel.setVisible(true);
                return;
            }
            try {
                boolean ok = Main.adminLogin(user, pass);
                if (ok) {
                    new AdminDashboard().start(new Stage());
                    stage.close();
                } else {
                    errorLabel.setText("Invalid admin credentials.");
                    errorLabel.setVisible(true);
                }
            } catch (Exception ex) {
                errorLabel.setText("Connection error. Check database.");
                errorLabel.setVisible(true);
            }
        });

        form.getChildren().addAll(heading, subtext, new Spacer(0, 4),
                fieldLabel("Username"), userField,
                fieldLabel("Password"), passField,
                errorLabel, loginBtn);
        return form;
    }

    // ── Helpers ────────────────────────────────────────────────────
    private StackPane buildBallotIcon() {
        StackPane sp = new StackPane();
        Rectangle box = new Rectangle(64, 64);
        box.setArcWidth(16); box.setArcHeight(16);
        box.setFill(Color.web("#6366f1"));
        Label icon = new Label("🗳");
        icon.setStyle("-fx-font-size: 32px;");
        sp.getChildren().addAll(box, icon);
        DropShadow glow = new DropShadow(20, Color.web("#6366f180"));
        sp.setEffect(glow);
        return sp;
    }

    private HBox featureBullet(String bullet, String text) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        Label b = new Label(bullet);
        b.setStyle("-fx-text-fill: #6366f1; -fx-font-size: 10px;");
        Label t = new Label(text);
        t.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px; -fx-font-family: 'Segoe UI';");
        row.getChildren().addAll(b, t);
        return row;
    }

    private Button styledTab(String text, boolean active) {
        Button btn = new Button(text);
        String base = "-fx-font-family: 'Segoe UI'; -fx-font-size: 13px; "
                + "-fx-cursor: hand; -fx-padding: 10 24; -fx-border-width: 0 0 2 0; ";
        if (active) {
            btn.setStyle(base + "-fx-background-color: transparent; "
                    + "-fx-text-fill: #6366f1; -fx-border-color: #6366f1;");
        } else {
            btn.setStyle(base + "-fx-background-color: transparent; "
                    + "-fx-text-fill: #64748b; -fx-border-color: transparent;");
        }
        return btn;
    }

    private void setActiveTab(Button active, Button inactive) {
        String base = "-fx-font-family: 'Segoe UI'; -fx-font-size: 13px; "
                + "-fx-cursor: hand; -fx-padding: 10 24; -fx-border-width: 0 0 2 0; ";
        active.setStyle(base + "-fx-background-color: transparent; "
                + "-fx-text-fill: #6366f1; -fx-border-color: #6366f1;");
        inactive.setStyle(base + "-fx-background-color: transparent; "
                + "-fx-text-fill: #64748b; -fx-border-color: transparent;");
    }

    static TextField formField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle("-fx-background-color: #0f172a; -fx-border-color: #334155; "
                + "-fx-border-radius: 8; -fx-background-radius: 8; "
                + "-fx-text-fill: #f1f5f9; -fx-prompt-text-fill: #475569; "
                + "-fx-padding: 10 14; -fx-font-size: 13px; -fx-font-family: 'Segoe UI';");
        return tf;
    }

    static Control formFieldPassword(String prompt) {
        PasswordField pf = new PasswordField();
        pf.setPromptText(prompt);
        pf.setStyle("-fx-background-color: #0f172a; -fx-border-color: #334155; "
                + "-fx-border-radius: 8; -fx-background-radius: 8; "
                + "-fx-text-fill: #f1f5f9; -fx-prompt-text-fill: #475569; "
                + "-fx-padding: 10 14; -fx-font-size: 13px; -fx-font-family: 'Segoe UI';");
        return pf;
    }

    static Label fieldLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; "
                + "-fx-text-fill: #94a3b8; -fx-font-family: 'Segoe UI';");
        return lbl;
    }

    static Button primaryButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; "
                + "-fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; "
                + "-fx-background-radius: 8; -fx-padding: 11 20; "
                + "-fx-font-family: 'Segoe UI';");
        btn.setOnMouseEntered(e -> btn.setOpacity(0.9));
        btn.setOnMouseExited(e -> btn.setOpacity(1.0));
        return btn;
    }

    static Button secondaryButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: #334155; -fx-text-fill: #94a3b8; "
                + "-fx-font-size: 13px; -fx-cursor: hand; "
                + "-fx-background-radius: 8; -fx-padding: 10 20; "
                + "-fx-font-family: 'Segoe UI';");
        return btn;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

// Tiny spacer helper
class Spacer extends Region {
    Spacer(double w, double h) {
        setMinSize(w, h);
        setPrefSize(w, h);
    }
}
