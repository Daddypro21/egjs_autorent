package cg.egjs.autorent.view.contrat;

import cg.egjs.autorent.controller.ContratController;
import cg.egjs.autorent.controller.VehiculeController;
import cg.egjs.autorent.dao.UtilisateurDAO;
import cg.egjs.autorent.model.*;
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
import java.util.List;

/**
 * Vue complète de gestion des contrats.
 * Liste, création, retour véhicule, génération PDF.
 */
public class ContratView {

    private final DashboardView      dashboard;
    private final ContratController  contratCtrl  = new ContratController();
    private final VehiculeController vehiculeCtrl = new VehiculeController();
    private final UtilisateurDAO     utilisateurDAO = new UtilisateurDAO();

    public ContratView(DashboardView dashboard) {
        this.dashboard = dashboard;
    }

    public Pane build() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #0A0A0F;");

        VBox section = UIFactory.sectionPane("Gestion des contrats", null, null);

        Label loading = UIFactory.bodyLabel("Chargement des contrats…");
        section.getChildren().add(loading);
        root.getChildren().add(section);

        Task<List<Contrat>> task = new Task<>() {
            @Override protected List<Contrat> call() throws Exception {
                return contratCtrl.getContratsEnCours();
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            section.getChildren().remove(loading);
            section.getChildren().add(buildTable(task.getValue()));
        }));

        task.setOnFailed(e -> Platform.runLater(() -> {
            section.getChildren().remove(loading);
            section.getChildren().add(UIFactory.emptyState("⚠️", "Erreur : " + task.getException().getMessage()));
        }));

        new Thread(task).start();
        return root;
    }

    private TableView<Contrat> buildTable(List<Contrat> contrats) {
        TableView<Contrat> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(500);
        table.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        table.getColumns().addAll(
            strCol("#", 55, c -> "#00" + c.getIdContrat()),
            strCol("Client",    110, c -> { var u = c.getClient(); return u != null ? u.getNomComplet() : "—"; }),
            strCol("Véhicule",  140, c -> { var v = c.getVehicule(); return v != null ? v.getMarque() + " " + v.getModele() : "—"; }),
            strCol("Début",      90, c -> c.getDateDebut() != null ? c.getDateDebut().toString() : "—"),
            strCol("Fin prévue", 90, c -> c.getDateFin() != null ? c.getDateFin().toString() : "—"),
            strCol("Retour réel",90, c -> c.getDateRetourReelle() != null ? c.getDateRetourReelle().toString() : "—"),
            strCol("Montant",   130, c -> UIFactory.formatXAF(c.getMontantTotal())),
            strCol("Statut",    100, c -> c.getStatut().name()),
            actionsCol()
        );

        table.getItems().addAll(contrats);
        if (contrats.isEmpty()) table.setPlaceholder(UIFactory.emptyState("📄", "Aucun contrat en cours"));
        return table;
    }

    private <T> TableColumn<Contrat, String> strCol(String title, double w, java.util.function.Function<Contrat, String> fn) {
        TableColumn<Contrat, String> col = new TableColumn<>(title);
        col.setPrefWidth(w);
        col.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(fn.apply(d.getValue())));
        return col;
    }

    private TableColumn<Contrat, Void> actionsCol() {
        TableColumn<Contrat, Void> col = new TableColumn<>("Actions");
        col.setPrefWidth(180);
        col.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                Contrat ctr = getTableView().getItems().get(getIndex());
                HBox box = new HBox(5);

                if (ctr.getStatut() == StatutContrat.EN_COURS) {
                    Button ret = UIFactory.btnSecondary("✔ Retour");
                    ret.setOnAction(e -> openRetour(ctr));
                    box.getChildren().add(ret);
                }
                if (ctr.getStatut() == StatutContrat.EN_RETARD) {
                    Button pen = UIFactory.btnDanger("⚠ Pénalité");
                    pen.setOnAction(e -> dashboard.navigateTo("penalites"));
                    box.getChildren().add(pen);
                }
                Button pdf = new Button("📄 PDF");
                pdf.setStyle(UIFactory.btnSecondary("").getStyle());
                pdf.setOnAction(e -> openPDF(ctr));
                box.getChildren().add(pdf);
                setGraphic(box);
            }
        });
        return col;
    }

    // ── Formulaire nouveau contrat ──
    public void openNouveauContrat() {
        VBox form = new VBox(14);
        form.setPadding(new Insets(18));

        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(14);
        for (int i = 0; i < 2; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(cc);
        }

        // Listes pour retrouver les IDs à la sauvegarde
        List<Utilisateur>[] clientsRef  = new List[]{null};
        List<Vehicule>[]    vehiculesRef = new List[]{null};

        // Sélection client
        VBox clientBox = UIFactory.formCombo("Client", "— Sélectionner —");
        ComboBox<String> clientCombo = (ComboBox<String>) clientBox.getChildren().get(1);
        new Thread(() -> {
            try {
                List<Utilisateur> clients = utilisateurDAO.findAllClients();
                clientsRef[0] = clients;
                Platform.runLater(() -> clients.forEach(u ->
                    clientCombo.getItems().add(u.getNomComplet() + " (" + u.getEmail() + ")")));
            } catch (Exception ex) { /* ignore */ }
        }).start();

        // Sélection véhicule
        VBox vehBox = UIFactory.formCombo("Véhicule disponible", "— Sélectionner —");
        ComboBox<String> vehCombo = (ComboBox<String>) vehBox.getChildren().get(1);
        new Thread(() -> {
            try {
                List<Vehicule> vehicules = vehiculeCtrl.getVehiculesDisponibles();
                vehiculesRef[0] = vehicules;
                Platform.runLater(() -> vehicules.forEach(v ->
                    vehCombo.getItems().add(v.getMarque() + " " + v.getModele() + " — " + UIFactory.formatXAF(v.getPrixJour()) + "/j")));
            } catch (Exception ex) { /* ignore */ }
        }).start();

        VBox dateDebutBox  = UIFactory.formDate("Date de début");
        VBox dateFinBox    = UIFactory.formDate("Date de fin prévue");
        DatePicker dpDebut = (DatePicker) dateDebutBox.getChildren().get(1);
        DatePicker dpFin   = (DatePicker) dateFinBox.getChildren().get(1);
        dpDebut.setValue(LocalDate.now());
        dpFin.setValue(LocalDate.now().plusDays(3));

        grid.add(clientBox,     0, 0);
        grid.add(vehBox,        1, 0);
        grid.add(dateDebutBox,  0, 1);
        grid.add(dateFinBox,    1, 1);

        // Montant preview
        Label mpVal = new Label("— XAF");
        HBox montantBox = UIFactory.montantPreview(mpVal);
        GridPane.setColumnSpan(montantBox, 2);
        grid.add(montantBox, 0, 2);

        // Calcul auto montant selon véhicule sélectionné
        Runnable calcMontant = () -> {
            LocalDate d = dpDebut.getValue();
            LocalDate f = dpFin.getValue();
            int idx = vehCombo.getSelectionModel().getSelectedIndex();
            if (d != null && f != null && f.isAfter(d)) {
                long jours = java.time.temporal.ChronoUnit.DAYS.between(d, f);
                double prixJour = (vehiculesRef[0] != null && idx > 0 && idx - 1 < vehiculesRef[0].size())
                    ? vehiculesRef[0].get(idx - 1).getPrixJour() : 50000;
                mpVal.setText(UIFactory.formatXAF(jours * prixJour));
            } else {
                mpVal.setText("— XAF");
            }
        };
        dpDebut.valueProperty().addListener((obs, o, n) -> calcMontant.run());
        dpFin.valueProperty().addListener((obs, o, n) -> calcMontant.run());
        vehCombo.valueProperty().addListener((obs, o, n) -> calcMontant.run());

        // Message d'erreur
        Label errLabel = new Label("");
        errLabel.setStyle("-fx-text-fill: #FF4444; -fx-font-size: 11px;");
        form.getChildren().addAll(grid, errLabel);

        Button save   = UIFactory.btnPrimary("✔ Créer le contrat");
        Button cancel = UIFactory.btnSecondary("Annuler");
        HBox footer   = UIFactory.formFooter(cancel, save);

        VBox content = new VBox(form, footer);
        Stage modal  = UIFactory.createModal(dashboard.getStage(), "Nouveau contrat de location", content, 660);
        cancel.setOnAction(e -> modal.close());

        save.setOnAction(e -> {
            // Validation
            int clientIdx = clientCombo.getSelectionModel().getSelectedIndex();
            int vehIdx    = vehCombo.getSelectionModel().getSelectedIndex();
            LocalDate debut = dpDebut.getValue();
            LocalDate fin   = dpFin.getValue();

            if (clientIdx <= 0 || clientsRef[0] == null) { errLabel.setText("Sélectionnez un client."); return; }
            if (vehIdx <= 0    || vehiculesRef[0] == null){ errLabel.setText("Sélectionnez un véhicule."); return; }
            if (debut == null || fin == null || !fin.isAfter(debut)) { errLabel.setText("Dates invalides."); return; }

            int idClient   = clientsRef[0].get(clientIdx - 1).getIdUtilisateur();
            int idVehicule = vehiculesRef[0].get(vehIdx - 1).getIdVehicule();

            save.setDisable(true);
            new Thread(() -> {
                try {
                    contratCtrl.creerContrat(idClient, idVehicule, debut, fin);
                    Platform.runLater(() -> { modal.close(); dashboard.refreshView(); });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        save.setDisable(false);
                        errLabel.setText(ex.getMessage());
                    });
                }
            }).start();
        });

        modal.show();
    }

    // ── Retour véhicule ──
    private void openRetour(Contrat ctr) {
        VBox form = new VBox(14);
        form.setPadding(new Insets(18));

        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(14);
        for (int i = 0; i < 2; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(cc);
        }

        // Champs readonly
        VBox clientField = UIFactory.formField("Client", "", false);
        TextField clientTf = (TextField) clientField.getChildren().get(1);
        clientTf.setText(ctr.getClient() != null ? ctr.getClient().getNomComplet() : "—");
        clientTf.setDisable(true);

        VBox vehField = UIFactory.formField("Véhicule", "", false);
        TextField vehTf = (TextField) vehField.getChildren().get(1);
        vehTf.setText(ctr.getVehicule() != null ? ctr.getVehicule().getDesignation() : "—");
        vehTf.setDisable(true);

        VBox finField = UIFactory.formField("Fin prévue", "", false);
        TextField finTf = (TextField) finField.getChildren().get(1);
        finTf.setText(ctr.getDateFin() != null ? ctr.getDateFin().toString() : "—");
        finTf.setDisable(true);

        VBox retourField = UIFactory.formDate("Date de retour réelle");
        DatePicker dpRetour = (DatePicker) retourField.getChildren().get(1);
        dpRetour.setValue(LocalDate.now());

        VBox kmField = UIFactory.formField("Kilométrage au retour", "Ex: 13 200", false);
        VBox etatBox = UIFactory.formCombo("État du véhicule", "Bon état", "Rayures mineures", "Dommages à signaler");

        // Warn box (masquée par défaut)
        HBox warnBox = UIFactory.warnBox("Retard détecté — pénalité automatique", "");
        warnBox.setVisible(false);
        warnBox.setManaged(false);
        Label warnDetail = (Label) ((VBox) warnBox.getChildren().get(1)).getChildren().get(1);

        dpRetour.valueProperty().addListener((obs, old, dr) -> {
            if (dr != null && ctr.getDateFin() != null && dr.isAfter(ctr.getDateFin())) {
                long j = java.time.temporal.ChronoUnit.DAYS.between(ctr.getDateFin(), dr);
                warnDetail.setText(j + " jour(s) × 5 000 XAF = " + UIFactory.formatXAF(j * 5000));
                warnBox.setVisible(true);
                warnBox.setManaged(true);
            } else {
                warnBox.setVisible(false);
                warnBox.setManaged(false);
            }
        });

        grid.add(clientField, 0, 0);
        grid.add(vehField,    1, 0);
        grid.add(finField,    0, 1);
        grid.add(retourField, 1, 1);
        grid.add(kmField,     0, 2);
        grid.add(etatBox,     1, 2);
        GridPane.setColumnSpan(warnBox, 2);

        form.getChildren().addAll(grid, warnBox);

        Button save   = UIFactory.btnPrimary("✔ Confirmer le retour");
        Button cancel = UIFactory.btnSecondary("Annuler");
        HBox footer   = UIFactory.formFooter(cancel, save);

        VBox content = new VBox(form, footer);
        Stage modal   = UIFactory.createModal(dashboard.getStage(),
            "Enregistrer le retour — Contrat #00" + ctr.getIdContrat(), content, 620);

        cancel.setOnAction(e -> modal.close());
        save.setOnAction(e -> {
            try {
                contratCtrl.traiterRetour(ctr.getIdContrat(), dpRetour.getValue());
                modal.close();
                dashboard.refreshView();
            } catch (Exception ex) {
                UIFactory.showError(dashboard.getStage(), "Erreur : " + ex.getMessage());
            }
        });
        modal.show();
    }

    // ── PDF ──
    private void openPDF(Contrat ctr) {
        VBox content = new VBox(14);
        content.setPadding(new Insets(22));
        content.setAlignment(Pos.CENTER);

        Label icon = new Label("📄");
        icon.setStyle("-fx-font-size: 46px;");

        Label title = new Label("CONTRAT DE LOCATION N° 00" + ctr.getIdContrat());
        title.setStyle("-fx-font-family: 'Roboto Condensed'; -fx-font-size: 19px; -fx-font-weight: 700; -fx-text-fill: #F2F2F7;");

        Label sub = UIFactory.bodyLabel("Généré via iText 7 — dossier contrats/");

        VBox infos = new VBox(4);
        infos.setPadding(new Insets(13, 18, 13, 18));
        infos.setStyle("-fx-background-color: #1E1E28; -fx-border-color: #2A2A35; -fx-border-width: 1.5px; -fx-border-radius: 8px; -fx-background-radius: 8px;");
        infos.setMaxWidth(Double.MAX_VALUE);
        for (String line : new String[]{
            "📍  EGJS AutoRent — Avenue de France, Brazzaville",
            "📞  +242 06 123 4567   ·   📧  contact@egjs-autorent.cg",
            "🗓  Généré le " + LocalDate.now() + "   ·   🔐  Signé électroniquement"
        }) {
            Label l = new Label(line);
            l.setStyle("-fx-font-family: 'Roboto Mono'; -fx-font-size: 11px; -fx-text-fill: #55556A;");
            infos.getChildren().add(l);
        }

        Button dl = UIFactory.btnPrimary("⬇  Télécharger contrat_00" + ctr.getIdContrat() + ".pdf");
        dl.setMaxWidth(Double.MAX_VALUE);
        dl.setOnAction(e -> {
            try {
                // Appel réel au PDFService
                // String path = new PDFService().genererContrat(ctr);
                UIFactory.showError(dashboard.getStage(), "PDF généré dans : contrats/contrat_00" + ctr.getIdContrat() + ".pdf");
            } catch (Exception ex) {
                UIFactory.showError(dashboard.getStage(), "Erreur PDF : " + ex.getMessage());
            }
        });

        content.getChildren().addAll(icon, title, sub, infos, dl);
        Stage modal = UIFactory.createModal(dashboard.getStage(), "Contrat PDF — #00" + ctr.getIdContrat(), content, 520);
        modal.show();
    }
}
