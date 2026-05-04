package cg.egjs.autorent.view.auth;

import cg.egjs.autorent.app.MainApp;
import cg.egjs.autorent.controller.AuthController;
import cg.egjs.autorent.model.Utilisateur;
import cg.egjs.autorent.util.ThemeManager;
import cg.egjs.autorent.view.dashboard.DashboardView;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Écran de connexion EGJS AutoRent.
 * Design fidèle au prototype HTML v2 — fond sombre, rouge EGJS.
 */
public class LoginView {

    private final Stage stage;
    private final AuthController authController = new AuthController();

    private TextField emailField;
    private PasswordField passwordField;
    private Label errorLabel;
    private Button loginButton;

    public LoginView(Stage stage) {
        this.stage = stage;
    }

    public Scene buildScene() {
        // ── Root ──
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #0A0A0F;");

        // ── Titlebar custom ──
        HBox titlebar = buildTitlebar();
        root.setTop(titlebar);

        // ── Centre : formulaire de connexion ──
        VBox center = buildLoginForm();
        root.setCenter(center);

        Scene scene = new Scene(root, 1280, 800);
        return scene;
    }

    private HBox buildTitlebar() {
        HBox bar = new HBox(8);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(0, 14, 0, 14));
        bar.setPrefHeight(36);
        bar.setStyle("-fx-background-color: #080810; -fx-border-color: #2A2A35; -fx-border-width: 0 0 1 0;");

        // Dots macOS style
        Circle c1 = dot("#FF5F57"); Circle c2 = dot("#FEBC2E"); Circle c3 = dot("#28C840");
        c1.setOnMouseClicked(e -> stage.close());

        // Rendre la fenêtre draggable depuis la titlebar
        final double[] dragDelta = {0, 0};
        bar.setOnMousePressed(e -> { dragDelta[0] = e.getSceneX(); dragDelta[1] = e.getSceneY(); });
        bar.setOnMouseDragged(e -> {
            stage.setX(e.getScreenX() - dragDelta[0]);
            stage.setY(e.getScreenY() - dragDelta[1]);
        });

        Label title = new Label("EGJS AUTORENT · SYSTÈME DE GESTION · BRAZZAVILLE");
        title.setStyle("-fx-font-family: 'Roboto'; -fx-font-size: 12px; -fx-font-weight: 700; -fx-text-fill: #55556A; -fx-letter-spacing: 3px;");
        HBox.setHgrow(title, Priority.ALWAYS);
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);

        Label ver = new Label("v1.0.0");
        ver.setStyle("-fx-background-color: #26262F; -fx-text-fill: #55556A; -fx-font-size: 9px; -fx-font-family: 'Roboto Mono'; -fx-padding: 2 7 2 7; -fx-background-radius: 10px; -fx-border-color: #2A2A35; -fx-border-radius: 10px;");

        bar.getChildren().addAll(c1, c2, c3, title, ver);
        return bar;
    }

    private Circle dot(String color) {
        Circle c = new Circle(5.5);
        c.setFill(Color.web(color));
        c.setCursor(Cursor.HAND);
        return c;
    }

    private VBox buildLoginForm() {
        VBox outer = new VBox();
        outer.setAlignment(Pos.CENTER);
        outer.setStyle("-fx-background-color: #0A0A0F;");

        // Card centrale
        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(400);
        card.setMaxWidth(400);
        card.setPadding(new Insets(36, 40, 36, 40));
        card.setStyle("""
            -fx-background-color: #16161E;
            -fx-border-color: #2A2A35;
            -fx-border-width: 1.5px;
            -fx-border-radius: 16px;
            -fx-background-radius: 16px;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 24, 0, 0, 8);
            """);

        // Logo
        Label logo = new Label("EGJS");
        logo.setStyle("-fx-font-family: 'Roboto Condensed'; -fx-font-size: 40px; -fx-font-weight: 900; -fx-text-fill: #E00000; -fx-letter-spacing: 6px;");

        Label subtitle = new Label("AutoRent · Brazzaville");
        subtitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #55556A; -fx-letter-spacing: 3px;");

        Label titleLabel = new Label("Connexion");
        titleLabel.setStyle("-fx-font-family: 'Roboto Condensed'; -fx-font-size: 22px; -fx-font-weight: 700; -fx-text-fill: #F2F2F7; -fx-padding: 10 0 0 0;");

        // Champs
        VBox emailBox = buildFormField("📧  EMAIL", "gestionnaire@egjs-autorent.cg", false);
        emailField = (TextField) ((VBox) emailBox).getChildren().get(1);

        VBox passBox = buildFormField("🔐  MOT DE PASSE", "••••••••", true);
        passwordField = (PasswordField) ((VBox) passBox).getChildren().get(1);

        // Erreur
        errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #FF2020; -fx-font-size: 11.5px; -fx-font-style: italic;");
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(320);

        // Bouton connexion
        loginButton = new Button("Se connecter");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.getStyleClass().add("btn-primary");
        loginButton.setStyle("""
            -fx-background-color: #E00000;
            -fx-text-fill: white;
            -fx-font-weight: 700;
            -fx-font-size: 13.5px;
            -fx-padding: 11 0 11 0;
            -fx-background-radius: 8px;
            -fx-cursor: hand;
            -fx-effect: dropshadow(gaussian, rgba(224,0,0,0.35), 10, 0, 0, 4);
            """);
        loginButton.setOnAction(e -> attemptLogin());
        passwordField.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) attemptLogin(); });

        // Toggle thème
        HBox themeRow = new HBox(8);
        themeRow.setAlignment(Pos.CENTER_RIGHT);
        Label themeLabel = new Label("Mode jour ☀️");
        themeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #55556A; -fx-cursor: hand;");
        themeLabel.setOnMouseClicked(e -> {
            ThemeManager.getInstance().toggleTheme(stage.getScene());
            boolean isDark = ThemeManager.getInstance().isDark();
            themeLabel.setText(isDark ? "Mode jour ☀️" : "Mode nuit 🌙");
        });
        themeRow.getChildren().add(themeLabel);

        card.getChildren().addAll(logo, subtitle, titleLabel, emailBox, passBox, errorLabel, loginButton, themeRow);
        outer.getChildren().add(card);

        return outer;
    }

    private VBox buildFormField(String labelText, String prompt, boolean isPassword) {
        VBox box = new VBox(5);
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-font-size: 10px; -fx-font-weight: 700; -fx-text-fill: #8888A0; -fx-letter-spacing: 0.5px;");

        Control field = isPassword ? new PasswordField() : new TextField();
        if (field instanceof TextField tf) tf.setPromptText(prompt);
        else if (field instanceof PasswordField pf) pf.setPromptText(prompt);
        field.setPrefHeight(40);
        field.setStyle("""
            -fx-background-color: #1E1E28;
            -fx-control-inner-background: #1E1E28;
            -fx-border-color: #2A2A35;
            -fx-border-width: 1.5px;
            -fx-border-radius: 8px;
            -fx-background-radius: 8px;
            -fx-text-fill: #F2F2F7;
            -fx-prompt-text-fill: #55556A;
            -fx-highlight-fill: #E00000;
            -fx-highlight-text-fill: #FFFFFF;
            -fx-padding: 8 12 8 12;
            -fx-font-size: 13px;
            """);
        field.focusedProperty().addListener((obs, old, focused) -> {
            if (focused) field.setStyle(field.getStyle() + "-fx-border-color: #E00000; -fx-effect: dropshadow(gaussian, rgba(224,0,0,0.13), 6, 0, 0, 0);");
            else field.setStyle(field.getStyle().replace("-fx-border-color: #E00000; -fx-effect: dropshadow(gaussian, rgba(224,0,0,0.13), 6, 0, 0, 0);", ""));
        });

        box.getChildren().addAll(lbl, field);
        return box;
    }

    private void attemptLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Veuillez remplir tous les champs.");
            return;
        }

        loginButton.setDisable(true);
        loginButton.setText("Connexion en cours…");
        errorLabel.setText("");

        Task<Utilisateur> task = new Task<>() {
            @Override
            protected Utilisateur call() throws Exception {
                return authController.authentifier(email, password);
            }
        };

        task.setOnSucceeded(e -> {
            Utilisateur user = task.getValue();
            // Charger le dashboard
            DashboardView dashboard = new DashboardView(stage, user);
            Scene scene = dashboard.buildScene();
            ThemeManager.getInstance().applyTheme(scene, ThemeManager.getInstance().getCurrentTheme());
            stage.setScene(scene);
        });

        task.setOnFailed(e -> {
            Throwable err = task.getException();
            String msg = err.getCause() != null ? err.getCause().getMessage() : err.getMessage();
            errorLabel.setText(msg != null ? msg : "Erreur de connexion.");
            loginButton.setDisable(false);
            loginButton.setText("Se connecter");
        });

        new Thread(task).start();
    }
}
