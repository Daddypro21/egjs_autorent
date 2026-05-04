package cg.egjs.autorent.view.dashboard;

import cg.egjs.autorent.controller.AuthController;
import cg.egjs.autorent.model.Utilisateur;
import cg.egjs.autorent.util.ThemeManager;
import cg.egjs.autorent.view.auth.LoginView;
import cg.egjs.autorent.view.vehicule.VehiculeView;
import cg.egjs.autorent.view.contrat.ContratView;
import cg.egjs.autorent.view.client.ClientView;
import cg.egjs.autorent.view.penalite.PenaliteView;
import cg.egjs.autorent.view.maintenance.MaintenanceView;
import cg.egjs.autorent.view.statistique.StatistiqueView;
import cg.egjs.autorent.view.journal.JournalView;
import cg.egjs.autorent.view.components.UIFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

/**
 * Tableau de bord principal EGJS AutoRent.
 * Sidebar + topbar + zone de contenu dynamique.
 * S'adapte selon le rôle de l'utilisateur connecté.
 */
public class DashboardView {

    private final Stage stage;
    private final Utilisateur user;
    private final AuthController authController = new AuthController();

    private BorderPane contentArea;
    private Label viewTitleLabel;
    private Button mainActionButton;
    private String currentView = "dashboard";
    private Runnable mainActionHandler;

    public DashboardView(Stage stage, Utilisateur user) {
        this.stage = stage;
        this.user  = user;
    }

    public Scene buildScene() {
        BorderPane root = new BorderPane();

        // ── Titlebar custom draggable ──
        HBox titlebar = buildTitlebar(root);
        root.setTop(titlebar);

        // ── Layout principal : sidebar + main ──
        HBox body = new HBox();
        VBox sidebar = buildSidebar();
        VBox main    = buildMain();
        HBox.setHgrow(main, Priority.ALWAYS);
        body.getChildren().addAll(sidebar, main);
        root.setCenter(body);

        // Charger le dashboard par défaut
        loadView("dashboard");

        Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
        return scene;
    }

    // ──────────────────────────────────────────────
    // TITLEBAR
    // ──────────────────────────────────────────────
    private HBox buildTitlebar(BorderPane root) {
        HBox bar = new HBox(8);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(0, 14, 0, 14));
        bar.setPrefHeight(36);
        bar.setStyle("-fx-background-color: #080810; -fx-border-color: #2A2A35; -fx-border-width: 0 0 1 0;");

        Circle c1 = dot("#FF5F57"); Circle c2 = dot("#FEBC2E"); Circle c3 = dot("#28C840");
        c1.setOnMouseClicked(e -> stage.close());
        c2.setOnMouseClicked(e -> stage.setIconified(true));
        c3.setOnMouseClicked(e -> stage.setMaximized(!stage.isMaximized()));

        final double[] drag = {0, 0};
        bar.setOnMousePressed(e -> { drag[0] = e.getSceneX(); drag[1] = e.getSceneY(); });
        bar.setOnMouseDragged(e -> {
            if (!stage.isMaximized()) {
                stage.setX(e.getScreenX() - drag[0]);
                stage.setY(e.getScreenY() - drag[1]);
            }
        });

        Label title = new Label("EGJS AUTORENT · SYSTÈME DE GESTION · BRAZZAVILLE");
        title.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: #55556A; -fx-letter-spacing: 2px;");
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

    // ──────────────────────────────────────────────
    // SIDEBAR
    // ──────────────────────────────────────────────
    private VBox buildSidebar() {
        VBox sb = new VBox();
        sb.setPrefWidth(212);
        sb.setMinWidth(212);
        sb.setStyle("-fx-background-color: #0D0D14; -fx-border-color: #2A2A35; -fx-border-width: 0 1 0 0;");

        // Logo
        VBox logoBox = new VBox(2);
        logoBox.setPadding(new Insets(18, 16, 14, 16));
        logoBox.setStyle("-fx-border-color: #2A2A35; -fx-border-width: 0 0 1 0;");
        Label logoLabel = new Label("EGJS");
        logoLabel.setStyle("-fx-font-family: 'Roboto Condensed'; -fx-font-size: 28px; -fx-font-weight: 900; -fx-text-fill: #E00000; -fx-letter-spacing: 4px;");
        Label logoSub = new Label("AutoRent · Brazzaville, Congo");
        logoSub.setStyle("-fx-font-size: 9px; -fx-text-fill: #55556A; -fx-letter-spacing: 2px;");
        logoBox.getChildren().addAll(logoLabel, logoSub);

        // User box
        HBox userBox = buildUserBox();

        // Nav
        ScrollPane navScroll = new ScrollPane();
        navScroll.setFitToWidth(true);
        navScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        navScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        navScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        VBox nav = buildNav();
        navScroll.setContent(nav);
        VBox.setVgrow(navScroll, Priority.ALWAYS);

        // Logout
        VBox bottomBox = new VBox();
        bottomBox.setPadding(new Insets(10));
        bottomBox.setStyle("-fx-border-color: #2A2A35; -fx-border-width: 1 0 0 0;");
        Button logoutBtn = new Button("🚪   Se déconnecter");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.getStyleClass().add("btn-logout");
        logoutBtn.setStyle("""
            -fx-background-color: #1E1E28; -fx-border-color: #2A2A35;
            -fx-border-width: 1.5px; -fx-border-radius: 8px; -fx-background-radius: 8px;
            -fx-text-fill: #8888A0; -fx-font-size: 12.5px; -fx-padding: 8 12 8 12; -fx-cursor: hand;
            """);
        logoutBtn.setOnMouseEntered(e -> logoutBtn.setStyle(logoutBtn.getStyle()
            .replace("#1E1E28", "rgba(224,0,0,0.08)").replace("#2A2A35", "#E00000")
            .replace("#8888A0", "#E00000")));
        logoutBtn.setOnMouseExited(e -> logoutBtn.setStyle("""
            -fx-background-color: #1E1E28; -fx-border-color: #2A2A35;
            -fx-border-width: 1.5px; -fx-border-radius: 8px; -fx-background-radius: 8px;
            -fx-text-fill: #8888A0; -fx-font-size: 12.5px; -fx-padding: 8 12 8 12; -fx-cursor: hand;
            """));
        logoutBtn.setOnAction(e -> logout());
        bottomBox.getChildren().add(logoutBtn);

        sb.getChildren().addAll(logoBox, userBox, navScroll, bottomBox);
        return sb;
    }

    private HBox buildUserBox() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(12, 15, 12, 15));
        box.setStyle("-fx-border-color: #2A2A35; -fx-border-width: 0 0 1 0; -fx-cursor: hand;");
        box.setOnMouseEntered(e -> box.setStyle("-fx-background-color: #1E1E28; -fx-border-color: #2A2A35; -fx-border-width: 0 0 1 0; -fx-cursor: hand;"));
        box.setOnMouseExited(e -> box.setStyle("-fx-border-color: #2A2A35; -fx-border-width: 0 0 1 0; -fx-cursor: hand;"));

        // Avatar
        Label avatar = new Label(getInitials());
        avatar.setStyle("""
            -fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #E00000, #A00000);
            -fx-background-radius: 17px; -fx-text-fill: white;
            -fx-font-weight: 700; -fx-font-size: 13px;
            -fx-min-width: 34px; -fx-min-height: 34px;
            -fx-max-width: 34px; -fx-max-height: 34px;
            -fx-alignment: center;
            -fx-effect: dropshadow(gaussian, rgba(224,0,0,0.3), 6, 0, 0, 2);
            """);

        VBox info = new VBox(1);
        Label name = new Label(user.getNomComplet());
        name.setStyle("-fx-font-size: 12.5px; -fx-font-weight: 700; -fx-text-fill: #F2F2F7;");
        Label role = new Label(user.getRole().name());
        role.setStyle("-fx-font-size: 10px; -fx-text-fill: #E00000; -fx-font-weight: 600;");
        info.getChildren().addAll(name, role);

        box.getChildren().addAll(avatar, info);
        return box;
    }

    private VBox buildNav() {
        VBox nav = new VBox(0);
        nav.setStyle("-fx-background-color: transparent;");

        addNavSection(nav, "PRINCIPAL");
        addNavItem(nav, "⊞", "Tableau de bord", "dashboard", true);

        addNavSection(nav, "GESTION");
        addNavItem(nav, "🚗", "Véhicules", "vehicules", false);
        addNavItem(nav, "📄", "Contrats", "contrats", false);
        addNavItem(nav, "👤", "Clients", "clients", false);
        addNavItem(nav, "⚠", "Pénalités", "penalites", false);
        addNavItem(nav, "🔧", "Maintenance", "maintenance", false);

        addNavSection(nav, "ANALYSE");
        addNavItem(nav, "📊", "Statistiques", "stats", false);
        addNavItem(nav, "📋", "Journal", "journal", false);

        return nav;
    }

    private void addNavSection(VBox nav, String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 9px; -fx-font-weight: 700; -fx-text-fill: #55556A; -fx-letter-spacing: 2px; -fx-padding: 12 15 4 15;");
        nav.getChildren().add(lbl);
    }

    private void addNavItem(VBox nav, String icon, String text, String viewId, boolean active) {
        Button btn = new Button(icon + "   " + text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setCursor(Cursor.HAND);
        applyNavStyle(btn, active);

        btn.setOnAction(e -> {
            // Désactiver tous les boutons nav
            nav.getChildren().stream()
                .filter(n -> n instanceof Button)
                .forEach(n -> applyNavStyle((Button) n, false));
            applyNavStyle(btn, true);
            loadView(viewId);
        });

        nav.getChildren().add(btn);
    }

    private void applyNavStyle(Button btn, boolean active) {
        if (active) {
            btn.setStyle("""
                -fx-background-color: rgba(224,0,0,0.13);
                -fx-text-fill: #F2F2F7; -fx-font-weight: 700;
                -fx-font-size: 13px; -fx-alignment: CENTER_LEFT;
                -fx-padding: 9 15 9 15; -fx-pref-width: 212px;
                -fx-cursor: hand; -fx-background-radius: 0;
                -fx-border-color: #E00000 transparent #E00000 #E00000;
                -fx-border-width: 0 0 0 2;
                """);
        } else {
            btn.setStyle("""
                -fx-background-color: transparent;
                -fx-text-fill: #8888A0; -fx-font-weight: 500;
                -fx-font-size: 13px; -fx-alignment: CENTER_LEFT;
                -fx-padding: 9 15 9 15; -fx-pref-width: 212px;
                -fx-cursor: hand; -fx-background-radius: 0;
                -fx-border-color: transparent; -fx-border-width: 0 0 0 2;
                """);
            btn.setOnMouseEntered(ev -> btn.setStyle("""
                -fx-background-color: #1E1E28;
                -fx-text-fill: #C8C8D4; -fx-font-weight: 500;
                -fx-font-size: 13px; -fx-alignment: CENTER_LEFT;
                -fx-padding: 9 15 9 15; -fx-pref-width: 212px;
                -fx-cursor: hand; -fx-background-radius: 0;
                -fx-border-color: #3A3A48; -fx-border-width: 0 0 0 2;
                """));
            btn.setOnMouseExited(ev -> applyNavStyle(btn, false));
        }
    }

    // ──────────────────────────────────────────────
    // MAIN (topbar + content)
    // ──────────────────────────────────────────────
    private VBox buildMain() {
        VBox main = new VBox();

        // Topbar
        HBox topbar = buildTopbar();
        main.getChildren().add(topbar);

        // Content area
        contentArea = new BorderPane();
        contentArea.setStyle("-fx-background-color: #0A0A0F;");
        VBox.setVgrow(contentArea, Priority.ALWAYS);
        main.getChildren().add(contentArea);

        return main;
    }

    private HBox buildTopbar() {
        HBox bar = new HBox(14);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(0, 22, 0, 22));
        bar.setPrefHeight(52);
        bar.setMinHeight(52);
        bar.setStyle("""
            -fx-background-color: #16161E;
            -fx-border-color: #2A2A35; -fx-border-width: 0 0 1 0;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 2);
            """);

        viewTitleLabel = new Label("Tableau de Bord");
        viewTitleLabel.setStyle("-fx-font-family: 'Roboto Condensed'; -fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #F2F2F7;");

        // Séparateur
        Region sep = new Region();
        sep.setPrefWidth(1); sep.setPrefHeight(20);
        sep.setStyle("-fx-background-color: #2A2A35;");

        // Search
        TextField search = new TextField();
        search.setPromptText("Rechercher un véhicule, client, contrat…");
        search.setPrefWidth(280);
        search.setStyle("""
            -fx-background-color: #1E1E28; -fx-border-color: #2A2A35;
            -fx-border-width: 1.5px; -fx-border-radius: 8px; -fx-background-radius: 8px;
            -fx-text-fill: #F2F2F7; -fx-prompt-text-fill: #55556A;
            -fx-padding: 7 12 7 12; -fx-font-size: 12.5px;
            """);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Toggle thème
        Button themeBtn = new Button("🌙");
        themeBtn.setStyle("""
            -fx-background-color: #1E1E28; -fx-border-color: #2A2A35;
            -fx-border-width: 1.5px; -fx-border-radius: 8px; -fx-background-radius: 8px;
            -fx-min-width: 34px; -fx-min-height: 34px; -fx-max-width: 34px; -fx-max-height: 34px;
            -fx-cursor: hand; -fx-font-size: 14px;
            """);
        themeBtn.setOnAction(e -> {
            ThemeManager.getInstance().toggleTheme(stage.getScene());
            themeBtn.setText(ThemeManager.getInstance().isDark() ? "🌙" : "☀️");
        });

        Button notifBtn = new Button("🔔");
        notifBtn.setStyle(themeBtn.getStyle());

        Button settingsBtn = new Button("⚙");
        settingsBtn.setStyle(themeBtn.getStyle());

        // Bouton action principal
        mainActionButton = new Button("+ Nouveau contrat");
        mainActionButton.setStyle("""
            -fx-background-color: #E00000; -fx-text-fill: white;
            -fx-font-weight: 700; -fx-font-size: 12.5px;
            -fx-padding: 8 16 8 16; -fx-background-radius: 8px; -fx-cursor: hand;
            -fx-effect: dropshadow(gaussian, rgba(224,0,0,0.3), 8, 0, 0, 3);
            """);
        mainActionButton.setOnMouseEntered(e -> mainActionButton.setStyle("""
            -fx-background-color: #FF2020; -fx-text-fill: white;
            -fx-font-weight: 700; -fx-font-size: 12.5px;
            -fx-padding: 8 16 8 16; -fx-background-radius: 8px; -fx-cursor: hand;
            -fx-translate-y: -1;
            -fx-effect: dropshadow(gaussian, rgba(224,0,0,0.45), 12, 0, 0, 4);
            """));
        mainActionButton.setOnMouseExited(e -> mainActionButton.setStyle("""
            -fx-background-color: #E00000; -fx-text-fill: white;
            -fx-font-weight: 700; -fx-font-size: 12.5px;
            -fx-padding: 8 16 8 16; -fx-background-radius: 8px; -fx-cursor: hand;
            -fx-effect: dropshadow(gaussian, rgba(224,0,0,0.3), 8, 0, 0, 3);
            """));

        mainActionButton.setOnAction(e -> { if (mainActionHandler != null) mainActionHandler.run(); });

        bar.getChildren().addAll(viewTitleLabel, sep, search, spacer, themeBtn, notifBtn, settingsBtn, mainActionButton);
        return bar;
    }

    // ──────────────────────────────────────────────
    // CHARGEMENT DES VUES
    // ──────────────────────────────────────────────
    private void loadView(String viewId) {
        currentView = viewId;
        Pane view;
        String title;
        String btnText;

        switch (viewId) {
            case "vehicules"   -> {
                VehiculeView vv = new VehiculeView(this); view = vv.build();
                title = "Parc Véhicules"; btnText = "+ Ajouter véhicule";
                mainActionHandler = vv::openFormulaireAjout;
            }
            case "contrats"    -> {
                ContratView cv = new ContratView(this); view = cv.build();
                title = "Gestion des Contrats"; btnText = "+ Nouveau contrat";
                mainActionHandler = cv::openNouveauContrat;
            }
            case "clients"     -> {
                ClientView clv = new ClientView(this); view = clv.build();
                title = "Gestion des Clients"; btnText = "+ Nouveau client";
                mainActionHandler = () -> clv.openFormulaire(null);
            }
            case "penalites"   -> {
                view = new PenaliteView(this).build();
                title = "Pénalités de Retard"; btnText = "Recalculer tout";
                mainActionHandler = null;
            }
            case "maintenance" -> {
                MaintenanceView mv = new MaintenanceView(this); view = mv.build();
                title = "Suivi Maintenance"; btnText = "+ Nouvelle entrée";
                mainActionHandler = () -> mv.openFormulaire(null);
            }
            case "stats"       -> {
                view = new StatistiqueView(this).build();
                title = "Statistiques Rentabilité"; btnText = "📄 Exporter PDF";
                mainActionHandler = null;
            }
            case "journal"     -> {
                view = new JournalView(this).build();
                title = "Journal des Actions"; btnText = "🔍 Filtrer";
                mainActionHandler = null;
            }
            default            -> {
                view = new DashboardHomeView(this).build();
                title = "Tableau de Bord"; btnText = "+ Nouveau contrat";
                mainActionHandler = () -> { navigateTo("contrats"); };
            }
        }

        viewTitleLabel.setText(title);
        mainActionButton.setText(btnText);

        ScrollPane scroll = new ScrollPane(view);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        contentArea.setCenter(scroll);
    }

    public void refreshView() { loadView(currentView); }
    public void navigateTo(String viewId) { loadView(viewId); }
    public Utilisateur getUser() { return user; }
    public Stage getStage() { return stage; }

    private String getInitials() {
        String p = user.getPrenom() != null && !user.getPrenom().isEmpty() ? String.valueOf(user.getPrenom().charAt(0)) : "";
        String n = user.getNom() != null && !user.getNom().isEmpty() ? String.valueOf(user.getNom().charAt(0)) : "";
        return p + n;
    }

    private void logout() {
        try {
            authController.deconnecter();
            LoginView loginView = new LoginView(stage);
            Scene scene = loginView.buildScene();
            ThemeManager.getInstance().applyTheme(scene, ThemeManager.getInstance().getCurrentTheme());
            stage.setScene(scene);
        } catch (Exception e) {
            UIFactory.showError(stage, "Erreur déconnexion : " + e.getMessage());
        }
    }
}
