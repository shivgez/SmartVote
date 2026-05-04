import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.stage.*;
import java.io.File;
import java.sql.*;

public class VotingScreen extends Application {

    private int studentId;
    private int selectedCandidateId = -1;
    private VBox selectedCard = null;

    public VotingScreen(int studentId) {
        this.studentId = studentId;
    }

    public VotingScreen() {}  // needed for Application.launch()

    @Override
    public void start(Stage stage) {
        stage.setTitle("SmartVote — Cast Your Vote");
        stage.setWidth(960);
        stage.setHeight(680);

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #0f172a;");

        // ── Top bar ───────────────────────────────────────────────
        HBox topBar = buildTopBar(stage);

        // ── Subtitle ──────────────────────────────────────────────
        VBox header = new VBox(6);
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-padding: 28 40 16 40;");

        Label title = new Label("Choose Your Candidate");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; "
                + "-fx-text-fill: #f1f5f9; -fx-font-family: 'Segoe UI';");

        Label subtitle = new Label("Select one candidate below, then click Cast Vote. You can only vote once.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; "
                + "-fx-font-family: 'Segoe UI';");

        header.getChildren().addAll(title, subtitle);

        // ── Candidates grid ───────────────────────────────────────
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        FlowPane candidatesPane = new FlowPane();
        candidatesPane.setHgap(20);
        candidatesPane.setVgap(20);
        candidatesPane.setPadding(new Insets(20, 40, 20, 40));
        candidatesPane.setAlignment(Pos.CENTER);
        candidatesPane.setStyle("-fx-background-color: transparent;");

        loadCandidates(candidatesPane, stage);
        scrollPane.setContent(candidatesPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // ── Bottom bar ────────────────────────────────────────────
        HBox bottomBar = buildBottomBar(stage, candidatesPane);

        root.getChildren().addAll(topBar, header, scrollPane, bottomBar);
        stage.setScene(new Scene(root));
        stage.show();
    }

    private HBox buildTopBar(Stage stage) {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color: #1e293b; -fx-padding: 14 24; "
                + "-fx-border-width: 0 0 1 0; -fx-border-color: #334155;");

        // Logo
        Label logo = new Label("🗳  SmartVote");
        logo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; "
                + "-fx-text-fill: #6366f1; -fx-font-family: 'Segoe UI';");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label studentInfo = new Label("Student ID: " + studentId);
        studentInfo.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; "
                + "-fx-font-family: 'Segoe UI';");

        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #f87171; "
                + "-fx-border-color: #f87171; -fx-border-radius: 6; "
                + "-fx-background-radius: 6; -fx-font-size: 12px; "
                + "-fx-cursor: hand; -fx-padding: 6 14;");
        logoutBtn.setOnAction(e -> {
            new LoginScreen().start(new Stage());
            stage.close();
        });

        bar.getChildren().addAll(logo, spacer, studentInfo, new Spacer(16, 0), logoutBtn);
        return bar;
    }

    private void loadCandidates(FlowPane pane, Stage stage) {
        try {
            ResultSet rs = Main.getCandidates();
            if (rs == null) {
                showNoData(pane);
                return;
            }
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String desc = rs.getString("description");
                String imagePath = rs.getString("image");

                VBox card = buildCandidateCard(id, name, desc, imagePath, pane, stage);
                pane.getChildren().add(card);
            }
            if (!hasData) showNoData(pane);
        } catch (Exception e) {
            e.printStackTrace();
            showNoData(pane);
        }
    }

    private VBox buildCandidateCard(int id, String name, String desc,
            String imagePath, FlowPane pane, Stage stage) {
        VBox card = new VBox(14);
        card.setPrefWidth(230);
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle("-fx-background-color: #1e293b; -fx-border-color: #334155; "
                + "-fx-border-radius: 14; -fx-background-radius: 14; "
                + "-fx-padding: 22 18; -fx-cursor: hand;");

        // Avatar / image
        StackPane avatar = buildAvatar(name, imagePath);

        // Name
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; "
                + "-fx-text-fill: #f1f5f9; -fx-font-family: 'Segoe UI'; "
                + "-fx-wrap-text: true; -fx-text-alignment: center;");
        nameLabel.setMaxWidth(190);
        nameLabel.setTextAlignment(TextAlignment.CENTER);

        // Description
        Label descLabel = new Label(desc != null ? desc : "");
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; "
                + "-fx-font-family: 'Segoe UI'; -fx-wrap-text: true; "
                + "-fx-text-alignment: center;");
        descLabel.setMaxWidth(190);
        descLabel.setTextAlignment(TextAlignment.CENTER);
        descLabel.setWrapText(true);

        // Selection indicator
        Label selectedBadge = new Label("✓  Selected");
        selectedBadge.setStyle("-fx-background-color: #22c55e20; -fx-text-fill: #22c55e; "
                + "-fx-font-size: 12px; -fx-font-weight: bold; "
                + "-fx-background-radius: 20; -fx-padding: 4 12;");
        selectedBadge.setVisible(false);

        card.getChildren().addAll(avatar, nameLabel, descLabel, selectedBadge);

        // Click to select
        card.setOnMouseClicked(e -> {
            // Deselect previous
            if (selectedCard != null) {
                selectedCard.setStyle("-fx-background-color: #1e293b; -fx-border-color: #334155; "
                        + "-fx-border-radius: 14; -fx-background-radius: 14; "
                        + "-fx-padding: 22 18; -fx-cursor: hand;");
                // hide old badge
                for (javafx.scene.Node n : selectedCard.getChildren()) {
                    if (n instanceof Label && ((Label) n).getText().contains("Selected")) {
                        n.setVisible(false);
                    }
                }
            }

            selectedCandidateId = id;
            selectedCard = card;

            card.setStyle("-fx-background-color: #1e293b; "
                    + "-fx-border-color: #6366f1; -fx-border-width: 2; "
                    + "-fx-border-radius: 14; -fx-background-radius: 14; "
                    + "-fx-padding: 22 18; -fx-cursor: hand; "
                    + "-fx-effect: dropshadow(gaussian, #6366f180, 16, 0, 0, 0);");
            selectedBadge.setVisible(true);
        });

        // Hover effect
        card.setOnMouseEntered(e -> {
            if (card != selectedCard) {
                card.setStyle("-fx-background-color: #273449; -fx-border-color: #475569; "
                        + "-fx-border-radius: 14; -fx-background-radius: 14; "
                        + "-fx-padding: 22 18; -fx-cursor: hand;");
            }
        });
        card.setOnMouseExited(e -> {
            if (card != selectedCard) {
                card.setStyle("-fx-background-color: #1e293b; -fx-border-color: #334155; "
                        + "-fx-border-radius: 14; -fx-background-radius: 14; "
                        + "-fx-padding: 22 18; -fx-cursor: hand;");
            }
        });

        return card;
    }

    private StackPane buildAvatar(String name, String imagePath) {
        StackPane sp = new StackPane();
        sp.setPrefSize(88, 88);
        sp.setMaxSize(88, 88);
        sp.setMinSize(88, 88);

        // Try to load image
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                File f = new File(imagePath);
                if (f.exists()) {
                    Image img = new Image(f.toURI().toString(), 88, 88, false, true);
                    ImageView iv = new ImageView(img);
                    iv.setFitWidth(88);
                    iv.setFitHeight(88);
                    Circle clip = new Circle(44, 44, 44);
                    iv.setClip(clip);
                    sp.getChildren().add(iv);
                    return sp;
                }
            } catch (Exception ignored) {}
        }

        // Fallback: initials circle
        Circle bg = new Circle(44);
        bg.setFill(Color.web("#6366f1"));

        String[] parts = name.split(" ");
        String initials = parts.length >= 2
                ? String.valueOf(parts[0].charAt(0)).toUpperCase()
                + String.valueOf(parts[1].charAt(0)).toUpperCase()
                : name.substring(0, Math.min(2, name.length())).toUpperCase();

        Label init = new Label(initials);
        init.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; "
                + "-fx-text-fill: white; -fx-font-family: 'Segoe UI';");

        sp.getChildren().addAll(bg, init);
        return sp;
    }

    private HBox buildBottomBar(Stage stage, FlowPane pane) {
        HBox bar = new HBox(16);
        bar.setAlignment(Pos.CENTER_RIGHT);
        bar.setStyle("-fx-background-color: #1e293b; -fx-padding: 18 40; "
                + "-fx-border-width: 1 0 0 0; -fx-border-color: #334155;");

        Label hint = new Label("Select a candidate to enable voting");
        hint.setStyle("-fx-font-size: 13px; -fx-text-fill: #475569; "
                + "-fx-font-family: 'Segoe UI';");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button castBtn = new Button("Cast Vote  ✓");
        castBtn.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; "
                + "-fx-font-size: 14px; -fx-font-weight: bold; "
                + "-fx-background-radius: 8; -fx-padding: 11 28; "
                + "-fx-font-family: 'Segoe UI'; -fx-cursor: hand;");
        castBtn.setOnMouseEntered(e -> castBtn.setStyle(
                "-fx-background-color: #4f46e5; -fx-text-fill: white; "
                        + "-fx-font-size: 14px; -fx-font-weight: bold; "
                        + "-fx-background-radius: 8; -fx-padding: 11 28; "
                        + "-fx-font-family: 'Segoe UI'; -fx-cursor: hand;"));
        castBtn.setOnMouseExited(e -> castBtn.setStyle(
                "-fx-background-color: #6366f1; -fx-text-fill: white; "
                        + "-fx-font-size: 14px; -fx-font-weight: bold; "
                        + "-fx-background-radius: 8; -fx-padding: 11 28; "
                        + "-fx-font-family: 'Segoe UI'; -fx-cursor: hand;"));

        castBtn.setOnAction(e -> {
            if (selectedCandidateId == -1) {
                showAlert(Alert.AlertType.WARNING, "No Selection",
                        "Please select a candidate before casting your vote.");
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Vote");
            confirm.setHeaderText("Are you sure?");
            confirm.setContentText("Your vote is final and cannot be changed.");
            styleAlert(confirm);
            confirm.showAndWait().ifPresent(res -> {
                if (res == ButtonType.OK) {
                    boolean ok = Main.castVote(studentId, selectedCandidateId);
                    if (ok) {
                        showSuccess(stage);
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error",
                                "Failed to record vote. Please try again.");
                    }
                }
            });
        });

        bar.getChildren().addAll(hint, spacer, castBtn);
        return bar;
    }

    private void showSuccess(Stage stage) {
        Stage successStage = new Stage();
        successStage.initModality(Modality.APPLICATION_MODAL);
        successStage.setTitle("Vote Recorded");
        successStage.setWidth(400);
        successStage.setHeight(320);

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #0f172a; -fx-padding: 50;");

        Label checkmark = new Label("✓");
        checkmark.setStyle("-fx-font-size: 60px; -fx-text-fill: #22c55e;");

        Label title = new Label("Vote Cast Successfully!");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; "
                + "-fx-text-fill: #f1f5f9; -fx-font-family: 'Segoe UI';");

        Label msg = new Label("Your vote has been recorded.\nThank you for participating.");
        msg.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; "
                + "-fx-text-alignment: center; -fx-font-family: 'Segoe UI';");
        msg.setTextAlignment(TextAlignment.CENTER);

        Button doneBtn = LoginScreen.primaryButton("Done");
        doneBtn.setOnAction(e -> {
            successStage.close();
            new LoginScreen().start(new Stage());
            stage.close();
        });

        root.getChildren().addAll(checkmark, title, msg, doneBtn);
        successStage.setScene(new Scene(root));
        successStage.show();
    }

    private void showNoData(FlowPane pane) {
        Label msg = new Label("No candidates found. Contact the admin.");
        msg.setStyle("-fx-font-size: 14px; -fx-text-fill: #475569; "
                + "-fx-font-family: 'Segoe UI';");
        pane.getChildren().add(msg);
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        styleAlert(a);
        a.showAndWait();
    }

    private void styleAlert(Alert a) {
        a.getDialogPane().setStyle(
            "-fx-background-color: #1e293b; -fx-border-color: #334155;");
        a.getDialogPane().lookup(".content.label").setStyle(
            "-fx-text-fill: #f1f5f9; -fx-font-family: 'Segoe UI';");
    }
}
