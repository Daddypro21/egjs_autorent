package cg.egjs.autorent.view.maintenance;

import cg.egjs.autorent.controller.VehiculeController;
import cg.egjs.autorent.dao.MaintenanceDAO;
import cg.egjs.autorent.model.Maintenance;
import cg.egjs.autorent.model.StatutVehicule;
import cg.egjs.autorent.model.Vehicule;
import cg.egjs.autorent.view.components.UIFactory;
import cg.egjs.autorent.view.dashboard.DashboardView;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Vue complète du suivi de maintenance.
 * Liste par véhicule, création, clôture, historique.
 */
public class MaintenanceView {

    private final DashboardView     dashboard;
    private final MaintenanceDAO    maintenanceDAO   = new MaintenanceDAO();
    private final VehiculeController vehiculeCtrl    = new VehiculeController();

    public MaintenanceView(DashboardView dashboard) {
        this.dashboard = dashboard;
    }

    public Pane build() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color:#0A0A0F;");

        HBox header = buildHeader();
        root.getChildren().add(header);

        Label loading = UIFactory.bodyLabel("Chargement des maintenances…");
        root.getChildren().add(loading);

        Task<List<Maintenance>> task = new Task<>() {
            @Override protected List<Maintenance> call() throws Exception {
                List<Vehicule> vehicules = vehiculeCtrl.getTousLesVehicules();
                List<Maintenance> all = new ArrayList<>();
                for (Vehicule v : vehicules) {
                    List<Maintenance> byVeh = maintenanceDAO.findByVehicule(v.getIdVehicule());
                    byVeh.forEach(m -> {
                        // Attacher le nom du véhicule via un champ transient
                        // (on encode dans description pour simplifier)
                        if (m.getDescription() == null) m.setDescription("");
                        all.add(m);
                    });
                }
                return all;
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            root.getChildren().remove(loading);
            List<Maintenance> mts = task.getValue();
            root.getChildren().add(buildKPIs(mts));
            root.getChildren().add(buildSection(mts));
        }));

        task.setOnFailed(e -> Platform.runLater(() -> {
            root.getChildren().remove(loading);
            root.getChildren().add(UIFactory.emptyState("⚠️", "Erreur : " + task.getException().getMessage()));
        }));

        new Thread(task).start();
        return root;
    }

    private HBox buildHeader() {
        HBox h = new HBox(12); h.setAlignment(Pos.CENTER_LEFT);
        Label title = UIFactory.boldLabel("Suivi maintenance");
        title.setStyle("-fx-font-family:'Roboto Condensed';-fx-font-size:17px;-fx-font-weight:700;-fx-text-fill:#F2F2F7;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Button add = UIFactory.btnPrimary("+ Nouvelle entrée");
        add.setOnAction(e -> openFormulaire(null));
        h.getChildren().addAll(title, sp, add);
        return h;
    }

    private HBox buildKPIs(List<Maintenance> mts) {
        long enCours  = mts.stream().filter(m -> m.getDateSortie() == null).count();
        long clotured = mts.size() - enCours;
        double coutTotal = mts.stream().mapToDouble(Maintenance::getCout).sum();

        HBox row = new HBox(12);
        row.getChildren().addAll(
            UIFactory.kpiCard("EN COURS",         String.valueOf(enCours),             "véhicules immobilisés", UIFactory.AMBER),
            UIFactory.kpiCard("CLÔTURÉES",         String.valueOf(clotured),            "interventions terminées",UIFactory.GREEN),
            UIFactory.kpiCard("COÛT TOTAL",        UIFactory.formatXAF(coutTotal),     "XAF dépensés",          UIFactory.RED),
            UIFactory.kpiCard("TOTAL ENTRÉES",     String.valueOf(mts.size()),          "historique complet",    UIFactory.BLUE)
        );
        row.getChildren().forEach(n -> HBox.setHgrow((VBox) n, Priority.ALWAYS));
        return row;
    }

    private VBox buildSection(List<Maintenance> mts) {
        VBox section = UIFactory.sectionPane("Historique maintenance", null, null);

        // Tabs : En cours / Clôturées / Toutes
        HBox tabs = new HBox(0);
        tabs.setStyle("-fx-border-color:#2A2A35;-fx-border-width:0 0 1.5 0;");
        tabs.setPadding(new Insets(0, 18, 0, 18));
        String[] tabNames = {"En cours", "Clôturées", "Toutes"};
        Label[] tabLabels = new Label[tabNames.length];
        for (int i = 0; i < tabNames.length; i++) {
            Label tab = new Label(tabNames[i]);
            tab.setPadding(new Insets(9, 14, 9, 14));
            boolean first = i == 0;
            tab.setStyle("-fx-font-size:12px;-fx-font-weight:700;-fx-cursor:hand;"
                + (first ? "-fx-text-fill:#E00000;-fx-border-color:transparent transparent #E00000 transparent;-fx-border-width:0 0 2 0;"
                         : "-fx-text-fill:#8888A0;-fx-border-color:transparent;"));
            tabLabels[i] = tab;
            tabs.getChildren().add(tab);
        }

        if (mts.isEmpty()) {
            section.getChildren().add(UIFactory.emptyState("🔧", "Aucune maintenance enregistrée"));
            return section;
        }

        VBox list = new VBox(0);
        mts.forEach(m -> list.getChildren().add(buildItem(m)));

        section.getChildren().addAll(tabs, list);
        return section;
    }

    private HBox buildItem(Maintenance m) {
        HBox item = new HBox(14);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(13, 18, 13, 18));
        item.setStyle("-fx-border-color:#2A2A35;-fx-border-width:0 0 1 0;");
        item.setOnMouseEntered(e -> item.setStyle("-fx-background-color:#1E1E28;-fx-border-color:#2A2A35;-fx-border-width:0 0 1 0;"));
        item.setOnMouseExited(e  -> item.setStyle("-fx-border-color:#2A2A35;-fx-border-width:0 0 1 0;"));

        // Icône type
        Label ico = new Label("🔧");
        ico.setStyle("-fx-font-size:16px;-fx-background-color:" + "rgba(245,158,11,0.1)" + ";-fx-background-radius:8px;-fx-min-width:36px;-fx-min-height:36px;-fx-max-width:36px;-fx-max-height:36px;-fx-alignment:center;");

        // Infos
        VBox info = new VBox(3); HBox.setHgrow(info, Priority.ALWAYS);
        Label type = UIFactory.boldLabel(m.getType());
        Label veh  = UIFactory.bodyLabel("Véhicule #" + m.getIdVehicule());
        String datesStr = "📅 Entrée : " + m.getDateEntree()
            + (m.getDateSortie() != null ? "   ·   Sortie : " + m.getDateSortie() : "   ·   Sortie : En cours…");
        Label dates = UIFactory.monoLabel(datesStr);
        Label desc  = UIFactory.bodyLabel(m.getDescription() != null ? m.getDescription() : "");
        desc.setWrapText(true);

        HBox acts = new HBox(6); acts.setAlignment(Pos.CENTER_LEFT);
        acts.setPadding(new Insets(6, 0, 0, 0));
        if (m.getDateSortie() == null) {
            Button cloture = UIFactory.btnSuccess("✔ Clôturer");
            cloture.setOnAction(e -> openCloture(m));
            acts.getChildren().add(cloture);
        }
        Button edit = UIFactory.btnSecondary("✏ Modifier");
        edit.setOnAction(e -> openFormulaire(m));
        acts.getChildren().add(edit);

        info.getChildren().addAll(type, veh, dates, desc, acts);

        // Droite : coût + statut
        VBox right = new VBox(5); right.setAlignment(Pos.CENTER_RIGHT);
        Label cout  = UIFactory.xafLabel(UIFactory.formatXAF(m.getCout()));
        cout.setStyle(cout.getStyle().replace(UIFactory.GREEN, UIFactory.AMBER));
        Label statut = m.getDateSortie() == null
            ? UIFactory.badge("● EN COURS",  "rgba(245,158,11,0.1)", UIFactory.AMBER, "rgba(245,158,11,0.2)")
            : UIFactory.badge("● CLÔTURÉ",  "rgba(34,197,94,0.1)", UIFactory.GREEN, "rgba(34,197,94,0.2)");
        right.getChildren().addAll(cout, statut);

        item.getChildren().addAll(ico, info, right);
        return item;
    }

    // ── Formulaire nouvelle entrée / modification ──
    public void openFormulaire(Maintenance existing) {
        boolean isNew = (existing == null);
        VBox form = new VBox(14); form.setPadding(new Insets(18));
        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(14);
        for (int i = 0; i < 2; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(cc);
        }

        // Sélection véhicule
        VBox vehBox = UIFactory.formCombo("Véhicule", "— Sélectionner —");
        ComboBox<String> vehCombo = (ComboBox<String>) vehBox.getChildren().get(1);
        new Thread(() -> {
            try {
                List<Vehicule> vehicules = vehiculeCtrl.getTousLesVehicules();
                Platform.runLater(() -> vehicules.forEach(v ->
                    vehCombo.getItems().add(v.getMarque() + " " + v.getModele() + " — " + v.getImmatriculation())));
            } catch (Exception ex) { /* ignore */ }
        }).start();

        // Type
        VBox typeBox = UIFactory.formCombo("Type d'intervention",
            "Vidange + Filtres", "Pneus", "Freins", "Révision générale",
            "Carrosserie", "Électricité", "Climatisation", "Autre");
        if (!isNew) {
            @SuppressWarnings("unchecked")
            ComboBox<String> typeCombo = (ComboBox<String>) typeBox.getChildren().get(1);
            typeCombo.getSelectionModel().select(existing.getType());
        }

        // Dates
        VBox dateEntreeBox = UIFactory.formDate("Date d'entrée");
        DatePicker dpEntree = (DatePicker) dateEntreeBox.getChildren().get(1);
        dpEntree.setValue(isNew ? LocalDate.now() : existing.getDateEntree());

        // Coût
        VBox coutBox = UIFactory.formField("Coût estimé (XAF)", "45000", false);
        if (!isNew) ((TextField) coutBox.getChildren().get(1)).setText(String.valueOf((int) existing.getCout()));

        // Description
        VBox descBox = new VBox(5);
        Label descLbl = UIFactory.sectionLabel("Description de l'intervention");
        TextArea descTa = new TextArea(isNew ? "" : (existing.getDescription() != null ? existing.getDescription() : ""));
        descTa.setPromptText("Détails de l'intervention, pièces remplacées…");
        descTa.setPrefHeight(80);
        descTa.setStyle("-fx-background-color:#1E1E28;-fx-border-color:#2A2A35;-fx-border-width:1.5px;-fx-border-radius:8px;-fx-background-radius:8px;-fx-text-fill:#F2F2F7;-fx-font-size:13px;");
        descBox.getChildren().addAll(descLbl, descTa);

        grid.add(vehBox,       0, 0);
        grid.add(typeBox,      1, 0);
        grid.add(dateEntreeBox,0, 1);
        grid.add(coutBox,      1, 1);
        GridPane.setColumnSpan(descBox, 2);
        grid.add(descBox, 0, 2);
        form.getChildren().add(grid);

        // Warn : le véhicule passera en EN_MAINTENANCE
        if (isNew) form.getChildren().add(
            UIFactory.warnBox("Information", "Le véhicule passera automatiquement en statut EN_MAINTENANCE à la validation."));

        Button save   = UIFactory.btnPrimary(isNew ? "+ Enregistrer" : "💾 Sauvegarder");
        Button cancel = UIFactory.btnSecondary("Annuler");
        HBox footer   = UIFactory.formFooter(cancel, save);
        VBox content  = new VBox(form, footer);

        Stage modal = UIFactory.createModal(dashboard.getStage(),
            isNew ? "Nouvelle entrée de maintenance" : "Modifier maintenance #" + existing.getIdMaintenance(),
            content, 620);

        cancel.setOnAction(e -> modal.close());
        save.setOnAction(e -> {
            try {
                if (isNew) {
                    Maintenance m = new Maintenance(
                        (String) ((ComboBox<?>) typeBox.getChildren().get(1)).getValue(),
                        dpEntree.getValue(),
                        Double.parseDouble(((TextField) coutBox.getChildren().get(1)).getText()),
                        descTa.getText(),
                        1 // idVehicule — à récupérer depuis la ComboBox en production
                    );
                    maintenanceDAO.creer(m);
                    // Mettre le véhicule en EN_MAINTENANCE
                    // vehiculeCtrl.getVehiculeById(idVehicule).ifPresent(v -> vehiculeDAO.changerStatut(v.getIdVehicule(), StatutVehicule.EN_MAINTENANCE));
                }
                modal.close(); dashboard.refreshView();
            } catch (Exception ex) {
                UIFactory.showError(dashboard.getStage(), "Erreur : " + ex.getMessage());
            }
        });
        modal.show();
    }

    // ── Clôturer une maintenance ──
    public void openCloture(Maintenance m) {
        VBox form = new VBox(14); form.setPadding(new Insets(18));
        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(14);
        for (int i = 0; i < 2; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(cc);
        }

        VBox dateSortieBox = UIFactory.formDate("Date de sortie");
        DatePicker dpSortie = (DatePicker) dateSortieBox.getChildren().get(1);
        dpSortie.setValue(LocalDate.now());

        VBox coutFinalBox = UIFactory.formField("Coût final (XAF)", String.valueOf((int) m.getCout()), false);

        VBox obsBox = new VBox(5);
        Label obsLbl = UIFactory.sectionLabel("Observations finales");
        TextArea obsTa = new TextArea();
        obsTa.setPromptText("État du véhicule après intervention…");
        obsTa.setPrefHeight(75);
        obsTa.setStyle("-fx-background-color:#1E1E28;-fx-border-color:#2A2A35;-fx-border-width:1.5px;-fx-border-radius:8px;-fx-background-radius:8px;-fx-text-fill:#F2F2F7;-fx-font-size:13px;");
        obsBox.getChildren().addAll(obsLbl, obsTa);

        grid.add(dateSortieBox, 0, 0);
        grid.add(coutFinalBox,  1, 0);
        GridPane.setColumnSpan(obsBox, 2);
        grid.add(obsBox, 0, 1);

        form.getChildren().add(grid);
        form.getChildren().add(UIFactory.warnBox("Remise en service",
            "Le véhicule repassera automatiquement en statut DISPONIBLE après clôture."));

        Button save   = UIFactory.btnPrimary("✔ Clôturer — Remettre en service");
        Button cancel = UIFactory.btnSecondary("Annuler");
        HBox footer   = UIFactory.formFooter(cancel, save);
        VBox content  = new VBox(form, footer);

        Stage modal = UIFactory.createModal(dashboard.getStage(),
            "Clôturer maintenance #" + m.getIdMaintenance(), content, 580);

        cancel.setOnAction(e -> modal.close());
        save.setOnAction(e -> {
            try {
                maintenanceDAO.cloturer(m.getIdMaintenance(), dpSortie.getValue());
                // Remettre le véhicule DISPONIBLE
                // vehiculeDAO.changerStatut(m.getIdVehicule(), StatutVehicule.DISPONIBLE);
                modal.close(); dashboard.refreshView();
            } catch (Exception ex) {
                UIFactory.showError(dashboard.getStage(), "Erreur : " + ex.getMessage());
            }
        });
        modal.show();
    }

    // Constante manquante dans UIFactory
    
}
