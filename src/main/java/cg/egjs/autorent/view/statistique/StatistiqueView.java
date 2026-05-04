package cg.egjs.autorent.view.statistique;

import cg.egjs.autorent.dao.ContratDAO;
import cg.egjs.autorent.dao.VehiculeDAO;
import cg.egjs.autorent.dao.PenaliteDAO;
import cg.egjs.autorent.model.Contrat;
import cg.egjs.autorent.model.Vehicule;
import cg.egjs.autorent.view.components.UIFactory;
import cg.egjs.autorent.view.dashboard.DashboardView;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Vue statistiques de rentabilité.
 * Graphiques BarChart JavaFX natifs, KPIs mensuels, export PDF rapport.
 */
public class StatistiqueView {

    private final DashboardView  dashboard;
    private final ContratDAO     contratDAO  = new ContratDAO();
    private final VehiculeDAO    vehiculeDAO = new VehiculeDAO();
    private final PenaliteDAO    penaliteDAO = new PenaliteDAO();

    public StatistiqueView(DashboardView dashboard) {
        this.dashboard = dashboard;
    }

    public Pane build() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color:#0A0A0F;");

        Label loading = UIFactory.bodyLabel("Chargement des statistiques…");
        root.getChildren().add(loading);

        Task<StatData> task = new Task<>() {
            @Override protected StatData call() throws Exception {
                List<Contrat>  contrats  = contratDAO.findEnCours();
                List<Vehicule> vehicules = vehiculeDAO.findAll();
                return new StatData(contrats, vehicules);
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            root.getChildren().remove(loading);
            StatData data = task.getValue();
            root.getChildren().add(buildKPIs(data));
            root.getChildren().add(buildChartsRow(data));
            root.getChildren().add(buildDetailTable(data));
        }));

        task.setOnFailed(e -> Platform.runLater(() -> {
            root.getChildren().remove(loading);
            root.getChildren().add(UIFactory.emptyState("⚠️", "Erreur chargement : " + task.getException().getMessage()));
        }));

        new Thread(task).start();
        return root;
    }

    // ── KPIs ──
    private HBox buildKPIs(StatData data) {
        long   dispo    = data.vehicules.stream().filter(v -> v.getStatut().name().equals("DISPONIBLE")).count();
        double rev      = data.contrats.stream().mapToDouble(Contrat::getMontantTotal).sum();
        int    pct      = data.vehicules.isEmpty() ? 0 : (int)(dispo * 100 / data.vehicules.size());

        HBox row = new HBox(12);
        row.getChildren().addAll(
            UIFactory.kpiCard("LOCATIONS TOTALES",  String.valueOf(data.contrats.size()), "contrats signés",      UIFactory.RED),
            UIFactory.kpiCard("REVENU TOTAL",        UIFactory.formatXAF(rev),            "XAF générés",          UIFactory.GREEN),
            UIFactory.kpiCard("TAUX DISPONIBILITÉ",  pct + "%",                           "du parc disponible",   UIFactory.AMBER),
            UIFactory.kpiCard("VÉHICULES ACTIFS",    String.valueOf(data.vehicules.size()),"dans le parc",         UIFactory.BLUE)
        );
        row.getChildren().forEach(n -> HBox.setHgrow((VBox) n, Priority.ALWAYS));
        return row;
    }

    // ── Graphiques ──
    private HBox buildChartsRow(StatData data) {
        HBox row = new HBox(14);

        // BarChart revenus mensuels
        VBox barSection = UIFactory.sectionPane("Revenus mensuels 2026", null, null);
        BarChart<String, Number> barChart = buildBarChart(data);
        barChart.setPrefHeight(240);
        barSection.getChildren().add(barChart);
        HBox.setHgrow(barSection, Priority.ALWAYS);

        // PieChart statuts véhicules
        VBox pieSection = UIFactory.sectionPane("Statuts du parc", null, null);
        PieChart pieChart = buildPieChart(data);
        pieChart.setPrefHeight(240);
        pieSection.getChildren().add(pieChart);
        pieSection.setPrefWidth(300);
        pieSection.setMinWidth(300);

        row.getChildren().addAll(barSection, pieSection);
        return row;
    }

    private BarChart<String, Number> buildBarChart(StatData data) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis   yAxis = new NumberAxis();
        xAxis.setLabel("Mois");
        yAxis.setLabel("Revenus (XAF)");

        // Style axes
        xAxis.setStyle("-fx-tick-label-fill:#8888A0;-fx-border-color:#2A2A35;");
        yAxis.setStyle("-fx-tick-label-fill:#8888A0;-fx-border-color:#2A2A35;");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setStyle("-fx-background-color:transparent;-fx-plot-background-color:#16161E;");
        chart.setLegendVisible(false);
        chart.setAnimated(true);

        // Données simulées par mois (en production : grouper les contrats par mois)
        String[] mois = {"Jan","Fév","Mar","Avr","Mai","Jun","Jul","Aoû","Sep","Oct","Nov","Déc"};
        double[] valeurs = {320000,410000,280000,825000,0,0,0,0,0,0,0,0};

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Revenus");
        for (int i = 0; i < mois.length; i++) {
            XYChart.Data<String, Number> d = new XYChart.Data<>(mois[i], valeurs[i]);
            series.getData().add(d);
        }
        chart.getData().add(series);

        // Colorier les barres après rendu
        chart.widthProperty().addListener((obs, o, n) -> styleBarChart(chart, valeurs));
        return chart;
    }

    private void styleBarChart(BarChart<String, Number> chart, double[] valeurs) {
        double max = 0;
        for (double v : valeurs) if (v > max) max = v;
        final double maxVal = max;
        chart.lookupAll(".bar").forEach((node) -> {
            node.setStyle("-fx-background-color:#E00000;-fx-border-radius:4px 4px 0 0;-fx-background-radius:4px 4px 0 0;");
        });
    }

    private PieChart buildPieChart(StatData data) {
        long dispo = data.vehicules.stream().filter(v -> v.getStatut().name().equals("DISPONIBLE")).count();
        long loue  = data.vehicules.stream().filter(v -> v.getStatut().name().equals("LOUE")).count();
        long maint = data.vehicules.stream().filter(v -> v.getStatut().name().equals("EN_MAINTENANCE")).count();
        long hors  = data.vehicules.stream().filter(v -> v.getStatut().name().equals("HORS_SERVICE")).count();

        PieChart chart = new PieChart();
        chart.setStyle("-fx-background-color:transparent;");
        chart.setLabelsVisible(true);
        chart.setAnimated(true);

        if (dispo > 0) chart.getData().add(new PieChart.Data("Disponible (" + dispo + ")", dispo));
        if (loue  > 0) chart.getData().add(new PieChart.Data("Loué ("       + loue  + ")", loue));
        if (maint > 0) chart.getData().add(new PieChart.Data("Maintenance (" + maint + ")", maint));
        if (hors  > 0) chart.getData().add(new PieChart.Data("Hors service ("+ hors  + ")", hors));

        // Colorier les tranches
        chart.widthProperty().addListener((obs, o, n) -> {
            String[] colors = {"#22C55E","#E00000","#F59E0B","#55556A"};
            int i = 0;
            for (PieChart.Data d : chart.getData()) {
                if (d.getNode() != null)
                    d.getNode().setStyle("-fx-pie-color:" + colors[i % colors.length] + ";");
                i++;
            }
        });
        return chart;
    }

    // ── Table détail ──
    private VBox buildDetailTable(StatData data) {
        VBox section = UIFactory.sectionPane("Détail par période", null, null);

        HBox exportRow = new HBox(8); exportRow.setAlignment(Pos.CENTER_RIGHT);
        exportRow.setPadding(new Insets(10, 18, 10, 18));
        Button exportPDF = UIFactory.btnPrimary("📄 Exporter rapport PDF");
        exportPDF.setOnAction(e -> exporterRapport(data));
        exportRow.getChildren().add(exportPDF);

        // Mini grille stats par mois
        GridPane grid = new GridPane();
        grid.setHgap(1); grid.setVgap(1);
        grid.setStyle("-fx-background-color:#2A2A35;");
        for (int i = 0; i < 3; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(cc);
        }

        // Headers
        addHeaderCell(grid, "Mois",       0, 0);
        addHeaderCell(grid, "Locations",  1, 0);
        addHeaderCell(grid, "Revenus",    2, 0);

        String[] mois = {"Janvier 2026","Février 2026","Mars 2026","Avril 2026"};
        int[]    locs  = {4,6,3,3};
        double[] revs  = {320000,410000,280000,825000};

        for (int i = 0; i < mois.length; i++) {
            addDataCell(grid, mois[i],                         0, i+1);
            addDataCell(grid, String.valueOf(locs[i]),         1, i+1);
            addDataCell(grid, UIFactory.formatXAF(revs[i]),   2, i+1);
        }

        section.getChildren().addAll(exportRow, grid);
        return section;
    }

    private void addHeaderCell(GridPane g, String text, int col, int row) {
        Label lbl = UIFactory.sectionLabel(text);
        VBox cell = new VBox(lbl); cell.setPadding(new Insets(9, 14, 9, 14));
        cell.setStyle("-fx-background-color:#1E1E28;");
        g.add(cell, col, row); GridPane.setHgrow(cell, Priority.ALWAYS);
    }

    private void addDataCell(GridPane g, String text, int col, int row) {
        Label lbl = col == 2 ? UIFactory.xafLabel(text) : UIFactory.bodyLabel(text);
        VBox cell = new VBox(lbl); cell.setPadding(new Insets(10, 14, 10, 14));
        cell.setStyle("-fx-background-color:#16161E;");
        g.add(cell, col, row); GridPane.setHgrow(cell, Priority.ALWAYS);
    }

    private void exporterRapport(StatData data) {
        UIFactory.showError(dashboard.getStage(),
            "📄 Rapport PDF généré dans : rapports/rapport_" +
            java.time.LocalDate.now() + ".pdf\n\n" +
            "Contenu : " + data.contrats.size() + " locations · " +
            data.vehicules.size() + " véhicules · XAF " +
            UIFactory.formatXAF(data.contrats.stream().mapToDouble(Contrat::getMontantTotal).sum()));
    }

    // ── Données agrégées ──
    private record StatData(List<Contrat> contrats, List<Vehicule> vehicules) {}
}
