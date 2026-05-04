import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.stage.*;
import java.sql.*;
import java.util.*;

public class AdminDashboard extends Application {

    private VBox contentArea;

    @Override
    public void start(Stage stage) {
        stage.setTitle("SmartVote — Admin Dashboard");
        stage.setWidth(1100);
        stage.setHeight(700);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #0f172a;");

        // ── Sidebar ───────────────────────────────────────────────
        VBox sidebar = buildSidebar(stage);
        root.setLeft(sidebar);

        // ── Main content ──────────────────────────────────────────
        contentArea = new VBox();
        contentArea.setStyle("-fx-background-color: #0f172a;");
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        // Top bar
        HBox topBar = buildTopBar(stage);
        contentArea.getChildren().add(topBar);

        // Default view: overview
        showOverview();

        ScrollPane scrollPane = new ScrollPane(contentArea);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setFitToWidth(true);

        root.setCenter(scrollPane);
        stage.setScene(new Scene(root));
        stage.show();
    }

    // ── Sidebar ────────────────────────────────────────────────────
    private VBox buildSidebar(Stage stage) {
        VBox sidebar = new VBox(4);
        sidebar.setPrefWidth(220);
        sidebar.setStyle("-fx-background-color: #1e293b; -fx-padding: 24 16;");

        Label logo = new Label("🗳  SmartVote");
        logo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; "
                + "-fx-text-fill: #6366f1; -fx-font-family: 'Segoe UI'; "
                + "-fx-padding: 0 0 20 8;");

        Label adminBadge = new Label("ADMIN");
        adminBadge.setStyle("-fx-background-color: #7c3aed30; -fx-text-fill: #a78bfa; "
                + "-fx-font-size: 10px; -fx-font-weight: bold; "
                + "-fx-background-radius: 4; -fx-padding: 3 8; -fx-margin: 0 0 16 8;");

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #334155;");

        Button[] navBtns = {
            navButton("📊", "Overview"),
            navButton("👥", "Candidates"),
            navButton("➕", "Add Candidate"),
            navButton("🔄", "Reset Votes"),
        };

        navBtns[0].setOnAction(e -> { setActiveNav(navBtns, navBtns[0]); showOverview(); });
        navBtns[1].setOnAction(e -> { setActiveNav(navBtns, navBtns[1]); showCandidates(); });
        navBtns[2].setOnAction(e -> { setActiveNav(navBtns, navBtns[2]); showAddCandidate(); });
        navBtns[3].setOnAction(e -> { setActiveNav(navBtns, navBtns[3]); showResetVotes(stage); });

        setActiveNav(navBtns, navBtns[0]);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = new Button("⬅  Logout");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #f87171; "
                + "-fx-border-color: #f8717140; -fx-border-radius: 8; "
                + "-fx-background-radius: 8; -fx-padding: 10 14; "
                + "-fx-alignment: CENTER_LEFT; -fx-font-size: 13px; "
                + "-fx-font-family: 'Segoe UI'; -fx-cursor: hand;");
        logoutBtn.setOnAction(e -> {
            new LoginScreen().start(new Stage());
            stage.close();
        });

        sidebar.getChildren().addAll(logo, adminBadge, sep, new Spacer(0, 8));
        sidebar.getChildren().addAll(navBtns);
        sidebar.getChildren().addAll(spacer, logoutBtn);
        return sidebar;
    }

    private Button navButton(String icon, String label) {
        Button btn = new Button(icon + "  " + label);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; "
                + "-fx-alignment: CENTER_LEFT; -fx-padding: 10 14; "
                + "-fx-background-radius: 8; -fx-font-size: 13px; "
                + "-fx-font-family: 'Segoe UI'; -fx-cursor: hand;");
        return btn;
    }

    private void setActiveNav(Button[] all, Button active) {
        for (Button b : all) {
            b.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; "
                    + "-fx-alignment: CENTER_LEFT; -fx-padding: 10 14; "
                    + "-fx-background-radius: 8; -fx-font-size: 13px; "
                    + "-fx-font-family: 'Segoe UI'; -fx-cursor: hand;");
        }
        active.setStyle("-fx-background-color: #6366f120; -fx-text-fill: #818cf8; "
                + "-fx-alignment: CENTER_LEFT; -fx-padding: 10 14; "
                + "-fx-background-radius: 8; -fx-font-size: 13px; "
                + "-fx-font-weight: bold; -fx-font-family: 'Segoe UI'; -fx-cursor: hand;");
    }

    // ── Top bar ────────────────────────────────────────────────────
    private HBox buildTopBar(Stage stage) {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color: #1e293b; -fx-padding: 14 28; "
                + "-fx-border-width: 0 0 1 0; -fx-border-color: #334155;");
        Label title = new Label("Admin Dashboard");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; "
                + "-fx-text-fill: #f1f5f9; -fx-font-family: 'Segoe UI';");
        bar.getChildren().add(title);
        return bar;
    }

    // ── Overview screen ────────────────────────────────────────────
    private void showOverview() {
        refreshContent();

        VBox page = new VBox(24);
        page.setStyle("-fx-padding: 32;");

        Label heading = pageHeading("Overview");

        // Stats row
        HBox statsRow = new HBox(16);
        statsRow.getChildren().addAll(
            statCard("Total Candidates", countCandidates()),
            statCard("Votes Cast", countTotalVotes()),
            statCard("Leading Candidate", getLeader())
        );

        // Vote bar chart
        Label chartHeading = sectionLabel("Live Vote Tally");
        VBox chartBox = buildVoteChart();

        page.getChildren().addAll(heading, statsRow, chartHeading, chartBox);
        contentArea.getChildren().add(page);
    }

    private VBox statCard(String label, String value) {
        VBox card = new VBox(6);
        card.setPrefWidth(200);
        card.setStyle("-fx-background-color: #1e293b; -fx-border-color: #334155; "
                + "-fx-border-radius: 12; -fx-background-radius: 12; "
                + "-fx-padding: 20 22;");

        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; "
                + "-fx-font-family: 'Segoe UI';");
        Label val = new Label(value);
        val.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; "
                + "-fx-text-fill: #f1f5f9; -fx-font-family: 'Segoe UI';");

        card.getChildren().addAll(lbl, val);
        return card;
    }

    private VBox buildVoteChart() {
        VBox box = new VBox(12);
        box.setStyle("-fx-background-color: #1e293b; -fx-border-color: #334155; "
                + "-fx-border-radius: 12; -fx-background-radius: 12; "
                + "-fx-padding: 20;");

        try {
            ResultSet rs = Main.getCandidates();
            if (rs == null) { box.getChildren().add(new Label("No data")); return box; }

            // First pass: collect data
            List<String> names = new ArrayList<>();
            List<Integer> votes = new ArrayList<>();
            int maxVotes = 0;
            while (rs.next()) {
                names.add(rs.getString("name"));
                int v = rs.getInt("votes");
                votes.add(v);
                if (v > maxVotes) maxVotes = v;
            }

            for (int i = 0; i < names.size(); i++) {
                HBox row = new HBox(12);
                row.setAlignment(Pos.CENTER_LEFT);

                Label name = new Label(names.get(i));
                name.setPrefWidth(160);
                name.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8; "
                        + "-fx-font-family: 'Segoe UI';");

                // Bar
                double ratio = maxVotes > 0 ? (double) votes.get(i) / maxVotes : 0;
                HBox barBg = new HBox();
                barBg.setPrefHeight(24);
                barBg.setPrefWidth(400);
                barBg.setStyle("-fx-background-color: #0f172a; "
                        + "-fx-background-radius: 4;");

                HBox bar = new HBox();
                bar.setPrefHeight(24);
                bar.setPrefWidth(ratio * 400);
                bar.setStyle("-fx-background-color: #6366f1; "
                        + "-fx-background-radius: 4;");

                StackPane barContainer = new StackPane(barBg, bar);
                StackPane.setAlignment(bar, Pos.CENTER_LEFT);
                barContainer.setPrefWidth(400);

                Label voteCount = new Label(votes.get(i) + " votes");
                voteCount.setStyle("-fx-font-size: 13px; -fx-text-fill: #6366f1; "
                        + "-fx-font-family: 'Segoe UI'; -fx-font-weight: bold;");

                row.getChildren().addAll(name, barContainer, voteCount);
                box.getChildren().add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return box;
    }

    // ── Candidates screen ──────────────────────────────────────────
    private void showCandidates() {
        refreshContent();
        VBox page = new VBox(20);
        page.setStyle("-fx-padding: 32;");
        Label heading = pageHeading("Manage Candidates");

        // Header row
        HBox header = new HBox();
        header.setStyle("-fx-background-color: #334155; -fx-padding: 12 16; "
                + "-fx-background-radius: 8 8 0 0;");
        for (String h : new String[]{"ID", "Name", "Description", "Votes"}) {
            Label lbl = new Label(h);
            lbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px; "
                    + "-fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
            HBox.setHgrow(lbl, Priority.ALWAYS);
            lbl.setPrefWidth(h.equals("Description") ? 300 : 100);
            header.getChildren().add(lbl);
        }

        VBox listBox = new VBox(0);
        listBox.setStyle("-fx-background-color: #1e293b; -fx-border-color: #334155; "
                + "-fx-border-radius: 0 0 8 8; -fx-background-radius: 0 0 8 8;");

        // Track selected
        final int[] selectedId = {-1};
        final String[] selectedName = {""};
        final String[] selectedDesc = {""};
        final String[] selectedImg = {""};

        try {
            ResultSet rs = Main.getCandidates();
            boolean[] alternate = {false};
            while (rs != null && rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String desc = rs.getString("description");
                int votes = rs.getInt("votes");
                String imagePath = rs.getString("image");

                HBox row = new HBox();
                row.setStyle("-fx-padding: 14 16; -fx-background-color: "
                        + (alternate[0] ? "#273449" : "#1e293b") + "; -fx-cursor: hand;");
                alternate[0] = !alternate[0];

                for (String val : new String[]{
                        String.valueOf(id), name,
                        desc != null ? desc : "", String.valueOf(votes)}) {
                    Label lbl = new Label(val);
                    lbl.setStyle("-fx-text-fill: #f1f5f9; -fx-font-size: 13px; "
                            + "-fx-font-family: 'Segoe UI';");
                    lbl.setPrefWidth(val.equals(desc) ? 300 : 100);
                    lbl.setWrapText(true);
                    row.getChildren().add(lbl);
                }

                row.setOnMouseClicked(e -> {
                    // Reset all rows
                    for (javafx.scene.Node n : listBox.getChildren()) {
                        ((HBox) n).setStyle(((HBox) n).getStyle()
                                .replace("-fx-border-color: #6366f1; -fx-border-width: 1;", ""));
                    }
                    row.setStyle(row.getStyle()
                            + "-fx-border-color: #6366f1; -fx-border-width: 1;");
                    selectedId[0] = id;
                    selectedName[0] = name;
                    selectedDesc[0] = desc != null ? desc : "";
                    selectedImg[0] = imagePath != null ? imagePath : "";
                });

                listBox.getChildren().add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Buttons
        HBox actions = new HBox(12);
        Button editBtn = LoginScreen.secondaryButton("Edit Selected");
        editBtn.setOnAction(e -> {
            if (selectedId[0] == -1) { alert("Select a candidate first."); return; }
            showEditCandidate(selectedId[0], selectedName[0], selectedDesc[0], selectedImg[0]);
        });

        Button deleteBtn = new Button("Delete Selected");
        deleteBtn.setStyle("-fx-background-color: #7f1d1d; -fx-text-fill: #fca5a5; "
                + "-fx-font-size: 13px; -fx-cursor: hand; "
                + "-fx-background-radius: 8; -fx-padding: 10 20; "
                + "-fx-font-family: 'Segoe UI';");
        deleteBtn.setOnAction(e -> {
            if (selectedId[0] == -1) { alert("Select a candidate first."); return; }
            Alert confirm = confirmAlert("Delete \"" + selectedName[0] + "\"?");
            confirm.showAndWait().ifPresent(r -> {
                if (r == ButtonType.OK) {
                    Main.deleteCandidate(selectedId[0]);
                    showCandidates();
                }
            });
        });
        editBtn.setOnMouseEntered(e2 -> editBtn.setStyle(
                "-fx-background-color: #475569; -fx-text-fill: #f1f5f9; "
                        + "-fx-font-size: 13px; -fx-cursor: hand; "
                        + "-fx-background-radius: 8; -fx-padding: 10 20; "
                        + "-fx-font-family: 'Segoe UI';"));
        editBtn.setOnMouseExited(e2 -> editBtn.setStyle(
                "-fx-background-color: #334155; -fx-text-fill: #94a3b8; "
                        + "-fx-font-size: 13px; -fx-cursor: hand; "
                        + "-fx-background-radius: 8; -fx-padding: 10 20; "
                        + "-fx-font-family: 'Segoe UI';"));

        deleteBtn.setOnMouseEntered(e2 -> deleteBtn.setStyle(
                "-fx-background-color: #dc2626; -fx-text-fill: white; "
                        + "-fx-font-size: 13px; -fx-cursor: hand; "
                        + "-fx-background-radius: 8; -fx-padding: 10 20; "
                        + "-fx-font-family: 'Segoe UI';"));
        deleteBtn.setOnMouseExited(e2 -> deleteBtn.setStyle(
                "-fx-background-color: #7f1d1d; -fx-text-fill: #fca5a5; "
                        + "-fx-font-size: 13px; -fx-cursor: hand; "
                        + "-fx-background-radius: 8; -fx-padding: 10 20; "
                        + "-fx-font-family: 'Segoe UI';"));

        actions.getChildren().addAll(editBtn, deleteBtn);
        page.getChildren().addAll(heading, header, listBox, actions);
        contentArea.getChildren().add(page);
    }

    private void loadCandidateTable(TableView<CandidateRow> table) {
        table.getItems().clear();
        try {
            ResultSet rs = Main.getCandidates();
            if (rs == null) return;
            while (rs.next()) {
                table.getItems().add(new CandidateRow(
                    rs.getInt("id"), rs.getString("name"),
                    rs.getString("description"), rs.getInt("votes")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private <T> void styleColumn(TableColumn<CandidateRow, T> col) {
        col.setCellFactory(tc -> {
            javafx.scene.control.TableCell<CandidateRow, T> cell = new javafx.scene.control.TableCell<>() {
                @Override
                protected void updateItem(T item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.toString());
                    }
                }
            };
            cell.setStyle("-fx-text-fill: #f1f5f9; -fx-font-size: 13px; "
                    + "-fx-font-family: 'Segoe UI'; -fx-padding: 8 12; "
                    + "-fx-background-color: transparent;");
            return cell;
        });
    }
    // ── Add Candidate screen ───────────────────────────────────────
    private void showAddCandidate() {
        refreshContent();

        VBox page = new VBox(20);
        page.setStyle("-fx-padding: 32;");
        page.setMaxWidth(500);

        Label heading = pageHeading("Add Candidate");

        VBox form = buildCandidateForm(null, "", "", "");
        page.getChildren().addAll(heading, form);
        contentArea.getChildren().add(page);
    }

    private void showEditCandidate(int id, String name, String desc, String img) {
        refreshContent();
        VBox page = new VBox(20);
        page.setStyle("-fx-padding: 32;");
        page.setMaxWidth(500);
        Label heading = pageHeading("Edit Candidate");
        VBox form = buildCandidateForm(id, name, desc, img);
        page.getChildren().addAll(heading, form);
        contentArea.getChildren().add(page);
    }

    private VBox buildCandidateForm(Integer editId, String name, String desc, String img) {
        VBox form = new VBox(14);
        form.setMaxWidth(460);
        form.setStyle("-fx-background-color: #1e293b; -fx-border-color: #334155; "
                + "-fx-border-radius: 14; -fx-background-radius: 14; -fx-padding: 28;");

        TextField nameField = LoginScreen.formField("Full name");
        nameField.setText(name);

        TextArea descField = new TextArea(desc);
        descField.setPromptText("Short description / party / department");
        descField.setStyle("-fx-background-color: #0f172a; -fx-control-inner-background: #0f172a; -fx-border-color: #334155; "
                + "-fx-border-radius: 8; -fx-background-radius: 8; "
                + "-fx-text-fill: #f1f5f9; -fx-prompt-text-fill: #475569; "
                + "-fx-padding: 10 14; -fx-font-size: 13px; -fx-font-family: 'Segoe UI';");
        descField.setPrefHeight(80);
        descField.setWrapText(true);

        TextField imgField = LoginScreen.formField("Image path (optional)");
        imgField.setText(img);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #f87171; -fx-font-size: 12px;");
        errorLabel.setVisible(false);

        String btnLabel = editId == null ? "Add Candidate" : "Save Changes";
        Button submitBtn = LoginScreen.primaryButton(btnLabel);
        submitBtn.setMaxWidth(Double.MAX_VALUE);

        submitBtn.setOnAction(e -> {
            String n = nameField.getText().trim();
            String d = descField.getText().trim();
            String i = imgField.getText().trim();
            if (n.isEmpty()) {
                errorLabel.setText("Candidate name is required.");
                errorLabel.setVisible(true);
                return;
            }
            boolean ok;
            if (editId == null) {
                ok = Main.addCandidate(n, d, i);
            } else {
                ok = Main.updateCandidate(editId, n, d, i);
            }
            if (ok) {
                alert(editId == null ? "Candidate added successfully!" : "Candidate updated!");
                showCandidates();
            } else {
                errorLabel.setText("Database error. Please try again.");
                errorLabel.setVisible(true);
            }
        });

        form.getChildren().addAll(
            LoginScreen.fieldLabel("Full Name"), nameField,
            LoginScreen.fieldLabel("Description"), descField,
            LoginScreen.fieldLabel("Image Path"), imgField,
            errorLabel, submitBtn
        );
        return form;
    }

    // ── Reset votes screen ─────────────────────────────────────────
    private void showResetVotes(Stage stage) {
        refreshContent();

        VBox page = new VBox(20);
        page.setStyle("-fx-padding: 32;");
        page.setMaxWidth(480);

        Label heading = pageHeading("Reset Votes");

        VBox warningCard = new VBox(14);
        warningCard.setStyle("-fx-background-color: #7f1d1d30; -fx-border-color: #dc262640; "
                + "-fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 24;");

        Label warningTitle = new Label("⚠  Danger Zone");
        warningTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; "
                + "-fx-text-fill: #fca5a5; -fx-font-family: 'Segoe UI';");

        Label warningText = new Label(
            "This action will:\n• Reset ALL candidate vote counts to 0\n"
            + "• Mark ALL students as 'not voted'\n\n"
            + "This cannot be undone. Use only when starting a new election cycle.");
        warningText.setStyle("-fx-font-size: 13px; -fx-text-fill: #fca5a5; "
                + "-fx-font-family: 'Segoe UI'; -fx-wrap-text: true;");
        warningText.setWrapText(true);

        Button resetBtn = new Button("Reset All Votes");
        resetBtn.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; "
                + "-fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; "
                + "-fx-background-radius: 8; -fx-padding: 11 24; "
                + "-fx-font-family: 'Segoe UI';");
        resetBtn.setOnAction(e -> {
            Alert confirm = confirmAlert("Reset ALL votes and mark all students as not voted?\n\nThis is irreversible.");
            confirm.showAndWait().ifPresent(r -> {
                if (r == ButtonType.OK) {
                    boolean ok = Main.resetVotes();
                    alert(ok ? "All votes have been reset." : "Reset failed. Check database.");
                    showOverview();
                }
            });
        });

        warningCard.getChildren().addAll(warningTitle, warningText, resetBtn);
        page.getChildren().addAll(heading, warningCard);
        contentArea.getChildren().add(page);
    }

    // ── Helpers ────────────────────────────────────────────────────
    private void refreshContent() {
        if (contentArea.getChildren().size() > 1) {
            contentArea.getChildren().remove(1, contentArea.getChildren().size());
        }
    }

    private Label pageHeading(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; "
                + "-fx-text-fill: #f1f5f9; -fx-font-family: 'Segoe UI';");
        return lbl;
    }

    private Label sectionLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; "
                + "-fx-text-fill: #94a3b8; -fx-font-family: 'Segoe UI';");
        return lbl;
    }

    private String countCandidates() {
        try {
            ResultSet rs = Main.getCandidates();
            int count = 0;
            while (rs != null && rs.next()) count++;
            return String.valueOf(count);
        } catch (Exception e) { return "–"; }
    }

    private String countTotalVotes() {
        try {
            ResultSet rs = Main.getCandidates();
            int total = 0;
            while (rs != null && rs.next()) total += rs.getInt("votes");
            return String.valueOf(total);
        } catch (Exception e) { return "–"; }
    }

    private String getLeader() {
        try {
            ResultSet rs = Main.getCandidates();
            String leader = "None";
            int max = 0;  // changed from -1 to 0
            while (rs != null && rs.next()) {
                int v = rs.getInt("votes");
                if (v > max) {
                    max = v;
                    leader = rs.getString("name");
                }
            }
            return leader;
        } catch (Exception e) { return "–"; }
    }

    private void alert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.getDialogPane().setStyle("-fx-background-color: #1e293b;");
        a.getDialogPane().lookup(".content.label").setStyle(
                "-fx-text-fill: #f1f5f9; -fx-font-family: 'Segoe UI'; -fx-font-size: 13px;");
        a.showAndWait();
    }

    private Alert confirmAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.getDialogPane().setStyle("-fx-background-color: #1e293b;");
        a.getDialogPane().lookup(".content.label").setStyle(
                "-fx-text-fill: #f1f5f9; -fx-font-family: 'Segoe UI'; -fx-font-size: 13px;");
        return a;
    }
}

// ── Data model for TableView ───────────────────────────────────────
class CandidateRow {
    private int id;
    private String name;
    private String description;
    private int votes;

    public CandidateRow(int id, String name, String description, int votes) {
        this.id = id; this.name = name;
        this.description = description; this.votes = votes;
    }
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getVotes() { return votes; }
}
