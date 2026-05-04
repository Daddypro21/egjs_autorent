package cg.egjs.autorent.view.journal;

import cg.egjs.autorent.dao.JournalDAO;
import cg.egjs.autorent.model.JournalAction;
import cg.egjs.autorent.view.components.UIFactory;
import cg.egjs.autorent.view.dashboard.DashboardView;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Vue journal des actions.
 * Liste paginée, filtres par type/utilisateur/date, export.
 */
public class JournalView {

    private final DashboardView dashboard;
    private final JournalDAO    journalDAO = new JournalDAO();

    // Couleurs par type d'action
    private static final java.util.Map<String, String[]> ACTION_STYLE = java.util.Map.of(
        "CONNEXION",          new String[]{"rgba(96,165,250,0.12)",  "#60A5FA"},
        "CREATION_CONTRAT",   new String[]{"rgba(34,197,94,0.12)",   "#22C55E"},
        "RETOUR_VEHICULE",    new String[]{"rgba(245,158,11,0.12)",  "#F59E0B"},
        "PENALITE",           new String[]{"rgba(224,0,0,0.12)",     "#E00000"},
        "AJOUT_VEHICULE",     new String[]{"rgba(167,139,250,0.12)", "#A78BFA"},
        "MODIFICATION",       new String[]{"rgba(96,165,250,0.12)",  "#60A5FA"},
        "SUPPRESSION",        new String[]{"rgba(224,0,0,0.15)",     "#FF2020"},
        "DECONNEXION",        new String[]{"rgba(85,85,106,0.15)",   "#8888A0"},
        "COMPTE_BLOQUE",      new String[]{"rgba(224,0,0,0.15)",     "#FF2020"}
    );

    public JournalView(DashboardView dashboard) {
        this.dashboard = dashboard;
    }

    public Pane build() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color:#0A0A0F;");

        // Barre de filtres
        HBox filterBar = buildFilterBar(root);
        root.getChildren().add(filterBar);

        // Zone de contenu
        VBox contentArea = new VBox();
        root.getChildren().add(contentArea);

        // Chargement initial
        loadJournal(contentArea, null, null);
        return root;
    }

    private HBox buildFilterBar(VBox root) {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(14, 18, 14, 18));
        bar.setStyle(String.format(
            "-fx-background-color:%s;-fx-border-color:%s;-fx-border-width:1.5px;-fx-border-radius:12px;-fx-background-radius:12px;",
            UIFactory.S1, UIFactory.B1));

        Label filterLbl = UIFactory.sectionLabel("Filtrer par");

        // Filtre type d'action
        ComboBox<String> typeFilter = new ComboBox<>();
        typeFilter.getItems().addAll(
            "Toutes les actions",
            "CONNEXION", "CREATION_CONTRAT", "RETOUR_VEHICULE",
            "PENALITE", "AJOUT_VEHICULE", "MODIFICATION",
            "SUPPRESSION", "DECONNEXION", "COMPTE_BLOQUE"
        );
        typeFilter.getSelectionModel().selectFirst();
        styleCombo(typeFilter);
        typeFilter.setPrefWidth(200);

        // Filtre utilisateur
        TextField userFilter = new TextField();
        userFilter.setPromptText("🔍  Rechercher un utilisateur…");
        userFilter.setPrefWidth(220);
        userFilter.setStyle(String.format(
            "-fx-background-color:%s;-fx-border-color:%s;-fx-border-width:1.5px;-fx-border-radius:8px;-fx-background-radius:8px;-fx-text-fill:%s;-fx-prompt-text-fill:%s;-fx-padding:8 12 8 12;-fx-font-size:12.5px;",
            UIFactory.S2, UIFactory.B1, UIFactory.TX, UIFactory.TX4));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Bouton export
        Button exportBtn = UIFactory.btnSecondary("📄 Exporter");
        exportBtn.setOnAction(e -> UIFactory.showError(dashboard.getStage(),
            "Export journal généré dans : exports/journal_" + java.time.LocalDate.now() + ".csv"));

        Button appliquerBtn = UIFactory.btnPrimary("Appliquer");
        appliquerBtn.setOnAction(e -> {
            String type  = typeFilter.getValue().equals("Toutes les actions") ? null : typeFilter.getValue();
            String user  = userFilter.getText().trim().isEmpty() ? null : userFilter.getText().trim();
            VBox contentArea = (VBox) ((VBox) appliquerBtn.getParent().getParent().getParent()).getChildren().get(1);
            loadJournal(contentArea, type, user);
        });

        bar.getChildren().addAll(filterLbl, typeFilter, userFilter, spacer, exportBtn, appliquerBtn);
        return bar;
    }

    private void loadJournal(VBox contentArea, String typeFilter, String userFilter) {
        contentArea.getChildren().clear();
        Label loading = UIFactory.bodyLabel("Chargement du journal…");
        contentArea.getChildren().add(loading);

        Task<List<JournalAction>> task = new Task<>() {
            @Override protected List<JournalAction> call() throws Exception {
                // En production : appel DAO avec filtres SQL
                // Pour l'instant : données simulées
                return getSimulatedData();
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            contentArea.getChildren().remove(loading);
            List<JournalAction> entries = task.getValue();

            // Filtrer en mémoire
            if (typeFilter != null)
                entries = entries.stream().filter(j -> j.getAction().equals(typeFilter)).collect(Collectors.toList());
            if (userFilter != null)
                entries = entries.stream().filter(j -> j.getDetails() != null &&
                    j.getDetails().toLowerCase().contains(userFilter.toLowerCase())).collect(Collectors.toList());

            final List<JournalAction> filtered = entries;

            // KPIs
            HBox kpis = buildKPIs(filtered);
            contentArea.getChildren().add(kpis);

            // Section journal
            VBox section = buildJournalSection(filtered);
            contentArea.getChildren().add(section);
        }));

        task.setOnFailed(e -> Platform.runLater(() -> {
            contentArea.getChildren().remove(loading);
            contentArea.getChildren().add(UIFactory.emptyState("⚠️", "Erreur : " + task.getException().getMessage()));
        }));

        new Thread(task).start();
    }

    private HBox buildKPIs(List<JournalAction> entries) {
        long connexions = entries.stream().filter(j -> j.getAction().equals("CONNEXION")).count();
        long contrats   = entries.stream().filter(j -> j.getAction().equals("CREATION_CONTRAT")).count();
        long retours    = entries.stream().filter(j -> j.getAction().equals("RETOUR_VEHICULE")).count();
        long alertes    = entries.stream().filter(j ->
            j.getAction().equals("PENALITE") || j.getAction().equals("COMPTE_BLOQUE")).count();

        HBox row = new HBox(12);
        row.getChildren().addAll(
            UIFactory.kpiCard("TOTAL ACTIONS",   String.valueOf(entries.size()), "entrées dans le journal", UIFactory.BLUE),
            UIFactory.kpiCard("CONNEXIONS",       String.valueOf(connexions),     "sessions ouvertes",       UIFactory.GREEN),
            UIFactory.kpiCard("CONTRATS",         String.valueOf(contrats),       "créations enregistrées",  UIFactory.AMBER),
            UIFactory.kpiCard("ALERTES SÉCURITÉ", String.valueOf(alertes),        "pénalités + blocages",    UIFactory.RED)
        );
        row.getChildren().forEach(n -> HBox.setHgrow((VBox) n, Priority.ALWAYS));
        return row;
    }

    private VBox buildJournalSection(List<JournalAction> entries) {
        VBox section = UIFactory.sectionPane("Journal des actions", null, null);

        // Compteur dans le header
        HBox head = (HBox) section.getChildren().get(0);
        Label count = UIFactory.bodyLabel(entries.size() + " entrée(s)");
        count.setStyle(count.getStyle() + String.format(";-fx-text-fill:%s;", UIFactory.TX4));
        head.getChildren().add(count);

        if (entries.isEmpty()) {
            section.getChildren().add(UIFactory.emptyState("📋", "Aucune action enregistrée pour ces critères"));
            return section;
        }

        // En-têtes de colonne
        HBox header = new HBox(0);
        header.setStyle(String.format("-fx-background-color:%s;-fx-border-color:%s;-fx-border-width:0 0 1.5 0;", UIFactory.S2, UIFactory.B1));
        header.setPadding(new Insets(9, 0, 9, 0));
        addColHeader(header, "Action",      160);
        addColHeader(header, "Détails",     300);
        addColHeader(header, "Utilisateur", 140);
        addColHeader(header, "Date / Heure",150);
        section.getChildren().add(header);

        // Lignes
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background:transparent;-fx-background-color:transparent;-fx-border-color:transparent;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setPrefHeight(420);

        VBox list = new VBox(0);
        entries.forEach(j -> list.getChildren().add(buildRow(j)));
        scroll.setContent(list);
        section.getChildren().add(scroll);

        return section;
    }

    private void addColHeader(HBox row, String text, double width) {
        Label lbl = UIFactory.sectionLabel(text);
        lbl.setPrefWidth(width); lbl.setMinWidth(width);
        lbl.setPadding(new Insets(0, 14, 0, 14));
        row.getChildren().add(lbl);
    }

    private HBox buildRow(JournalAction j) {
        HBox row = new HBox(0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(0));
        row.setStyle(String.format("-fx-border-color:%s;-fx-border-width:0 0 1 0;", UIFactory.B1));
        row.setOnMouseEntered(e -> row.setStyle(String.format(
            "-fx-background-color:%s;-fx-border-color:%s;-fx-border-width:0 0 1 0;", UIFactory.S2, UIFactory.B1)));
        row.setOnMouseExited(e -> row.setStyle(String.format(
            "-fx-border-color:%s;-fx-border-width:0 0 1 0;", UIFactory.B1)));

        // Badge action
        String[] style = ACTION_STYLE.getOrDefault(j.getAction(),
            new String[]{"rgba(85,85,106,0.12)", UIFactory.TX3});
        Label actionBadge = new Label(j.getAction());
        actionBadge.setStyle(String.format(
            "-fx-background-color:%s;-fx-text-fill:%s;-fx-font-family:'Roboto Mono';-fx-font-size:10px;-fx-font-weight:700;-fx-padding:3 8 3 8;-fx-background-radius:4px;",
            style[0], style[1]));
        HBox actionCell = new HBox(actionBadge);
        actionCell.setAlignment(Pos.CENTER_LEFT);
        actionCell.setPrefWidth(160); actionCell.setMinWidth(160);
        actionCell.setPadding(new Insets(10, 14, 10, 14));

        // Détails
        Label details = UIFactory.bodyLabel(j.getDetails() != null ? j.getDetails() : "—");
        details.setWrapText(true);
        details.setPrefWidth(300); details.setMinWidth(300);
        details.setPadding(new Insets(10, 14, 10, 14));

        // IP / utilisateur
        String userInfo = j.getIdUtilisateur() != null
            ? "👤 User #" + j.getIdUtilisateur()
            : "⚙ Système";
        if (j.getAdresseIP() != null) userInfo += "\n🌐 " + j.getAdresseIP();
        Label user = UIFactory.monoLabel(userInfo);
        user.setPrefWidth(140); user.setMinWidth(140);
        user.setPadding(new Insets(10, 14, 10, 14));
        user.setWrapText(true);

        // Date/heure
        String timeStr = j.getDateHeure() != null
            ? j.getDateHeure().toLocalDate() + "\n" + j.getDateHeure().toLocalTime().toString().substring(0,5)
            : "—";
        Label time = UIFactory.monoLabel(timeStr);
        time.setStyle(time.getStyle() + String.format(";-fx-text-fill:%s;", UIFactory.TX4));
        time.setPrefWidth(150); time.setMinWidth(150);
        time.setPadding(new Insets(10, 14, 10, 14));

        row.getChildren().addAll(actionCell, details, user, time);
        return row;
    }

    private void styleCombo(ComboBox<String> combo) {
        combo.setStyle(String.format(
            "-fx-background-color:%s;-fx-border-color:%s;-fx-border-width:1.5px;-fx-border-radius:8px;-fx-background-radius:8px;-fx-font-size:13px;",
            UIFactory.S2, UIFactory.B1));
    }

    // Données simulées en attendant la connexion BDD réelle
    private List<JournalAction> getSimulatedData() {
        List<JournalAction> list = new java.util.ArrayList<>();
        String[][] data = {
            {"RETOUR_VEHICULE",  "Retour contrat #003 — pénalité 15 000 XAF appliquée", "2",  "127.0.0.1"},
            {"CREATION_CONTRAT", "Contrat #002 créé — Osseke M-C. / Mercedes Cl. E",    "2",  "127.0.0.1"},
            {"CREATION_CONTRAT", "Contrat #001 créé — Moukala J-P. / Toyota Fortuner",  "2",  "127.0.0.1"},
            {"AJOUT_VEHICULE",   "Véhicule ajouté — Toyota LC300 BZV-2023-001",          "2",  "127.0.0.1"},
            {"CONNEXION",        "Connexion réussie — gestionnaire@egjs-autorent.cg",    "2",  "127.0.0.1"},
            {"CONNEXION",        "Connexion réussie — admin@egjs-autorent.cg",           "1",  "127.0.0.1"},
            {"PENALITE",         "Pénalité #1 calculée — Contrat #003 — 15 000 XAF",     "2",  "127.0.0.1"},
            {"COMPTE_BLOQUE",    "Compte bloqué après 3 tentatives : test@test.com",     null, "192.168.1.5"},
        };
        for (String[] d : data) {
            JournalAction j = new JournalAction(d[0], d[1], d[2] != null ? Integer.parseInt(d[2]) : null);
            j.setAdresseIP(d[3]);
            j.setDateHeure(java.time.LocalDateTime.now().minusDays((long)(Math.random()*30)));
            list.add(j);
        }
        return list;
    }
}
