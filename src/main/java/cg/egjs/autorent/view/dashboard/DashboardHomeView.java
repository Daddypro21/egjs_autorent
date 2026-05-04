package cg.egjs.autorent.view.dashboard;

import cg.egjs.autorent.dao.ContratDAO;
import cg.egjs.autorent.dao.VehiculeDAO;
import cg.egjs.autorent.dao.PenaliteDAO;
import cg.egjs.autorent.dao.JournalDAO;
import cg.egjs.autorent.model.*;
import cg.egjs.autorent.view.components.UIFactory;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

/**
 * Vue tableau de bord principal.
 * KPIs, contrats actifs, alertes, journal récent, mini-stats.
 */
public class DashboardHomeView {

    private final DashboardView dashboard;
    private final VehiculeDAO vehiculeDAO = new VehiculeDAO();
    private final ContratDAO  contratDAO  = new ContratDAO();
    private final PenaliteDAO penaliteDAO = new PenaliteDAO();
    private final JournalDAO  journalDAO  = new JournalDAO();

    public DashboardHomeView(DashboardView dashboard) {
        this.dashboard = dashboard;
    }

    public Pane build() {
        VBox root = new VBox(14);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #0A0A0F;");

        // Placeholders
        HBox kpiRow  = new HBox(12);
        HBox midRow  = new HBox(12);
        VBox statRow = new VBox();
        root.getChildren().addAll(kpiRow, midRow, statRow);
        HBox.setHgrow(midRow, Priority.ALWAYS);

        // Chargement asynchrone
        Task<Void> task = new Task<>() {
            List<Vehicule>    vehicules;
            List<Contrat>     contrats;
            List<Penalite>    penalites;
            List<JournalAction> journal;

            @Override protected Void call() throws Exception {
                vehicules = vehiculeDAO.findAll();
                contrats  = contratDAO.findEnCours();
                // Charger tous contrats pour stats
                penalites = penaliteDAO.findAll();
                journal   = new java.util.ArrayList<>();
                return null;
            }

            @Override protected void succeeded() {
                Platform.runLater(() -> buildContent(root, kpiRow, midRow, statRow, vehicules, contrats));
            }

            @Override protected void failed() {
                Platform.runLater(() -> buildFallback(root, kpiRow, midRow, statRow));
            }
        };
        new Thread(task).start();

        return root;
    }

    private void buildContent(VBox root, HBox kpiRow, HBox midRow, VBox statRow,
                              List<Vehicule> vehicules, List<Contrat> contrats) {
        long dispo = vehicules.stream().filter(v -> v.getStatut() == StatutVehicule.DISPONIBLE).count();
        long loues = vehicules.stream().filter(v -> v.getStatut() == StatutVehicule.LOUE).count();
        long maint = vehicules.stream().filter(v -> v.getStatut() == StatutVehicule.EN_MAINTENANCE).count();
        long retards = contrats.stream().filter(c -> c.getStatut() == StatutContrat.EN_RETARD).count();

        // ── KPIs ──
        kpiRow.getChildren().clear();
        VBox k1 = UIFactory.kpiCard("VÉHICULES LOUÉS",    String.valueOf(loues),
            dispo + " disponibles sur " + vehicules.size(), UIFactory.RED);
        VBox k2 = UIFactory.kpiCard("REVENUS CUMULÉS",    "825K",
            "XAF — total contrats", UIFactory.GREEN);
        VBox k3 = UIFactory.kpiCard("EN MAINTENANCE",     String.valueOf(maint),
            maint > 0 ? "Véhicule(s) immobilisé(s)" : "Parc opérationnel", UIFactory.AMBER);
        VBox k4 = UIFactory.kpiCard("CONTRATS ACTIFS",    String.valueOf(contrats.size()),
            retards > 0 ? "⚠ " + retards + " en retard" : "Aucun retard", UIFactory.BLUE);
        for (VBox k : new VBox[]{k1,k2,k3,k4}) {
            HBox.setHgrow(k, Priority.ALWAYS);
            kpiRow.getChildren().add(k);
        }

        // ── Rangée milieu : table contrats + side ──
        midRow.getChildren().clear();

        // Table contrats actifs
        VBox tableSection = buildContratsSection(contrats);
        HBox.setHgrow(tableSection, Priority.ALWAYS);

        // Side : alertes + journal
        VBox side = buildSidePanel(vehicules, contrats, retards, maint);
        side.setPrefWidth(310);
        side.setMinWidth(310);
        side.setMaxWidth(310);

        midRow.getChildren().addAll(tableSection, side);

        // ── Mini stats ──
        statRow.getChildren().clear();
        statRow.getChildren().add(buildMiniStats(vehicules, contrats, dispo));
    }

    private void buildFallback(VBox root, HBox kpiRow, HBox midRow, VBox statRow) {
        kpiRow.getChildren().addAll(
            UIFactory.kpiCard("VÉHICULES LOUÉS",  "—", "Connexion BDD requise", UIFactory.RED),
            UIFactory.kpiCard("REVENUS CUMULÉS",  "—", "Connexion BDD requise", UIFactory.GREEN),
            UIFactory.kpiCard("EN MAINTENANCE",   "—", "Connexion BDD requise", UIFactory.AMBER),
            UIFactory.kpiCard("CONTRATS ACTIFS",  "—", "Connexion BDD requise", UIFactory.BLUE)
        );
        kpiRow.getChildren().forEach(k -> HBox.setHgrow((VBox)k, Priority.ALWAYS));
        midRow.getChildren().add(UIFactory.emptyState("⚠️", "Impossible de charger les données. Vérifiez database.properties"));
    }

    private VBox buildContratsSection(List<Contrat> contrats) {
        VBox section = UIFactory.sectionPane("Contrats actifs", "Voir tout →",
            () -> dashboard.navigateTo("contrats"));

        // Tabs
        HBox tabs = new HBox(0);
        tabs.setStyle("-fx-border-color: #2A2A35; -fx-border-width: 0 0 1.5 0;");
        tabs.setPadding(new Insets(0, 18, 0, 18));
        for (String t : new String[]{"Tous","En cours","Retards"}) {
            Label tab = new Label(t);
            boolean first = t.equals("Tous");
            tab.setPadding(new Insets(9, 14, 9, 14));
            tab.setStyle("-fx-font-size: 12px; -fx-font-weight: 700; -fx-cursor: hand;" +
                (first ? "-fx-text-fill: #E00000; -fx-border-color: transparent transparent #E00000 transparent; -fx-border-width: 0 0 2 0;"
                       : "-fx-text-fill: #8888A0; -fx-border-color: transparent;"));
            tabs.getChildren().add(tab);
        }
        section.getChildren().add(tabs);

        // Table
        TableView<Contrat> table = buildContratsTable(contrats);
        VBox.setVgrow(table, Priority.ALWAYS);
        section.getChildren().add(table);
        VBox.setVgrow(section, Priority.ALWAYS);
        return section;
    }

    private TableView<Contrat> buildContratsTable(List<Contrat> contrats) {
        TableView<Contrat> table = new TableView<>();
        table.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(220);

        TableColumn<Contrat, String> colId = col("#", 50);
        colId.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
            "#00" + d.getValue().getIdContrat()));

        TableColumn<Contrat, String> colVeh = col("Véhicule", 140);
        colVeh.setCellValueFactory(d -> {
            Vehicule v = d.getValue().getVehicule();
            return new javafx.beans.property.SimpleStringProperty(
                v != null ? v.getMarque() + " " + v.getModele() : "—");
        });

        TableColumn<Contrat, String> colMt = col("Montant", 130);
        colMt.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
            UIFactory.formatXAF(d.getValue().getMontantTotal())));

        TableColumn<Contrat, String> colSt = col("Statut", 110);
        colSt.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
            d.getValue().getStatut().name()));

        TableColumn<Contrat, Void> colActs = new TableColumn<>("Actions");
        colActs.setPrefWidth(130);
        colActs.setCellFactory(c -> new TableCell<>() {
            final Button retour = UIFactory.btnSecondary("✔ Retour");
            final Button pdf    = new Button("📄 PDF");
            { pdf.setStyle(retour.getStyle()); }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                Contrat ctr = getTableView().getItems().get(getIndex());
                HBox box = new HBox(5);
                if (ctr.getStatut() == StatutContrat.EN_COURS) box.getChildren().add(retour);
                if (ctr.getStatut() == StatutContrat.EN_RETARD) {
                    Button pen = UIFactory.btnDanger("⚠ Pénalité");
                    box.getChildren().add(pen);
                }
                box.getChildren().add(pdf);
                setGraphic(box);
            }
        });

        table.getColumns().addAll(colId, colVeh, colMt, colSt, colActs);
        table.getItems().addAll(contrats);
        if (contrats.isEmpty()) table.setPlaceholder(UIFactory.emptyState("📄", "Aucun contrat actif"));
        styleTable(table);
        return table;
    }

    private VBox buildSidePanel(List<Vehicule> vehicules, List<Contrat> contrats, long retards, long maint) {
        VBox side = new VBox(12);

        // Alertes
        VBox alertes = UIFactory.sectionPane("Alertes", null, null);
        HBox alertHead = (HBox) alertes.getChildren().get(0);
        Label alertCount = new Label(retards + maint + " active(s)");
        alertCount.setStyle("-fx-font-size: 10.5px; -fx-text-fill: #E00000; -fx-font-weight: 700;");
        alertHead.getChildren().add(alertCount);

        if (retards > 0) {
            alertes.getChildren().add(buildAlertItem("⚠️", "Contrat(s) en retard",
                retards + " contrat(s) — pénalité calculée", "#E00000"));
        }
        if (maint > 0) {
            alertes.getChildren().add(buildAlertItem("🔧", "Véhicule(s) en maintenance",
                maint + " véhicule(s) immobilisé(s)", UIFactory.AMBER));
        }
        if (retards + maint == 0) {
            alertes.getChildren().add(UIFactory.emptyState("✅", "Aucune alerte active"));
        }

        // Journal récent
        VBox journal = UIFactory.sectionPane("Journal récent", null, null);
        for (String[] entry : new String[][]{
            {"g", "Contrat #002 créé — Osseke / Mercedes", "20/04/2026 09:14"},
            {"r", "Pénalité — Bouanga / Contrat #003",     "16/03/2026 14:22"},
            {"b", "Connexion — Nganga J.",                  "01/05/2026 08:01"},
        }) {
            journal.getChildren().add(buildJournalItem(entry[0], entry[1], entry[2]));
        }

        side.getChildren().addAll(alertes, journal);
        return side;
    }

    private HBox buildAlertItem(String icon, String title, String sub, String valueColor) {
        HBox item = new HBox(11);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(10, 18, 10, 18));
        item.setStyle("-fx-border-color: #2A2A35; -fx-border-width: 0 0 1 0;");

        Label ic = new Label(icon);
        ic.setStyle("-fx-font-size: 16px;");

        VBox txt = new VBox(2);
        Label t = UIFactory.boldLabel(title);
        t.setStyle(t.getStyle().replace("#F2F2F7", "#C8C8D4"));
        Label s = UIFactory.bodyLabel(sub);
        txt.getChildren().addAll(t, s);
        HBox.setHgrow(txt, Priority.ALWAYS);

        item.getChildren().addAll(ic, txt);
        return item;
    }

    private HBox buildJournalItem(String dotColor, String text, String time) {
        HBox item = new HBox(10);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(9, 18, 9, 18));
        item.setStyle("-fx-border-color: #2A2A35; -fx-border-width: 0 0 1 0; -fx-cursor: hand;");
        item.setOnMouseEntered(e -> item.setStyle("-fx-background-color: #1E1E28; -fx-border-color: #2A2A35; -fx-border-width: 0 0 1 0; -fx-cursor: hand;"));
        item.setOnMouseExited(e -> item.setStyle("-fx-border-color: #2A2A35; -fx-border-width: 0 0 1 0; -fx-cursor: hand;"));

        String col = switch(dotColor) {
            case "r" -> UIFactory.RED;
            case "g" -> UIFactory.GREEN;
            case "a" -> UIFactory.AMBER;
            default  -> UIFactory.BLUE;
        };
        Label dot = new Label("●");
        dot.setStyle("-fx-font-size: 8px; -fx-text-fill: " + col + ";");

        VBox info = new VBox(2);
        Label txt = UIFactory.bodyLabel(text);
        txt.setWrapText(true);
        Label t   = new Label(time);
        t.setStyle("-fx-font-family: 'Roboto Mono'; -fx-font-size: 10px; -fx-text-fill: #55556A;");
        info.getChildren().addAll(txt, t);
        HBox.setHgrow(info, Priority.ALWAYS);

        item.getChildren().addAll(dot, info);
        return item;
    }

    private VBox buildMiniStats(List<Vehicule> vehicules, List<Contrat> contrats, long dispo) {
        VBox section = UIFactory.sectionPane("Stats — Avril 2026", "Rapport complet →",
            () -> dashboard.navigateTo("stats"));

        HBox grid = new HBox(0);
        grid.setStyle("-fx-background-color: #2A2A35;");

        double rev = contrats.stream().mapToDouble(Contrat::getMontantTotal).sum();
        int pct = vehicules.isEmpty() ? 0 : (int)(dispo * 100 / vehicules.size());

        grid.getChildren().addAll(
            miniStat("Locations",         String.valueOf(contrats.size()), "r"),
            miniStat("Revenu total",       UIFactory.formatXAF(rev),       "g"),
            miniStat("Taux disponibilité", pct + "%",                      "a"),
            miniStat("Pénalités",          "15 000 XAF",                   "r")
        );
        grid.getChildren().forEach(n -> HBox.setHgrow((VBox)n, Priority.ALWAYS));
        section.getChildren().add(grid);
        return section;
    }

    private VBox miniStat(String label, String value, String color) {
        VBox box = new VBox(4);
        box.setPadding(new Insets(13, 16, 13, 16));
        box.setStyle("-fx-background-color: #16161E;");
        Label lbl = UIFactory.sectionLabel(label);
        Label val = new Label(value);
        String col = switch(color) {
            case "r" -> UIFactory.RED;
            case "g" -> UIFactory.GREEN;
            case "a" -> UIFactory.AMBER;
            default  -> UIFactory.BLUE;
        };
        val.setStyle("-fx-font-family: 'Roboto Condensed'; -fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: " + col + ";");
        box.getChildren().addAll(lbl, val);
        return box;
    }

    private <T> TableColumn<T, String> col(String title, double width) {
        TableColumn<T, String> c = new TableColumn<>(title);
        c.setPrefWidth(width);
        return c;
    }

    private void styleTable(TableView<?> t) {
        t.setStyle("""
            -fx-background-color: transparent;
            -fx-border-color: transparent;
            -fx-table-cell-border-color: #2A2A35;
            """);
    }
}
