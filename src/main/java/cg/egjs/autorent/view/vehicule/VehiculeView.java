package cg.egjs.autorent.view.vehicule;

import cg.egjs.autorent.controller.VehiculeController;
import cg.egjs.autorent.model.Vehicule;
import cg.egjs.autorent.view.components.UIFactory;
import cg.egjs.autorent.view.dashboard.DashboardView;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;

/**
 * Vue complète du parc véhicules.
 * Affiche toutes les voitures en grille avec statut, prix XAF,
 * et actions CRUD.
 */
public class VehiculeView {

    private final DashboardView dashboard;
    private final VehiculeController controller = new VehiculeController();

    public VehiculeView(DashboardView dashboard) {
        this.dashboard = dashboard;
    }

    public Pane build() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #0A0A0F;");

        // En-tête avec compteurs
        HBox header = buildHeader();
        root.getChildren().add(header);

        // Grille des véhicules (chargement async)
        FlowPane grid = new FlowPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPrefWrapLength(900);

        Label loading = UIFactory.bodyLabel("Chargement du parc véhicules…");
        root.getChildren().add(loading);

        Task<List<Vehicule>> task = new Task<>() {
            @Override protected List<Vehicule> call() throws Exception {
                return controller.getTousLesVehicules();
            }
        };

        task.setOnSucceeded(e -> {
            root.getChildren().remove(loading);
            List<Vehicule> vehicules = task.getValue();

            if (vehicules.isEmpty()) {
                root.getChildren().add(UIFactory.emptyState("🚗", "Aucun véhicule dans le parc"));
                return;
            }

            // Compteurs
            long dispo = vehicules.stream().filter(v -> v.getStatut().name().equals("DISPONIBLE")).count();
            long loue  = vehicules.stream().filter(v -> v.getStatut().name().equals("LOUE")).count();
            long maint = vehicules.stream().filter(v -> v.getStatut().name().equals("EN_MAINTENANCE")).count();

            // Mettre à jour l'en-tête
            HBox badges = (HBox) header.getChildren().get(1);
            badges.getChildren().clear();
            badges.getChildren().addAll(
                UIFactory.badge("● " + dispo + " disponible(s)", "rgba(34,197,94,0.1)", UIFactory.GREEN, "rgba(34,197,94,0.2)"),
                UIFactory.badge("● " + loue + " loué(s)",        "rgba(224,0,0,0.1)",   UIFactory.RED,   "rgba(224,0,0,0.2)"),
                UIFactory.badge("● " + maint + " maintenance",   "rgba(245,158,11,0.1)",UIFactory.AMBER, "rgba(245,158,11,0.2)")
            );

            vehicules.forEach(v -> grid.getChildren().add(buildVehiculeCard(v)));
            root.getChildren().add(grid);
        });

        task.setOnFailed(e -> {
            root.getChildren().remove(loading);
            root.getChildren().add(UIFactory.emptyState("⚠️", "Erreur : " + task.getException().getMessage()));
        });

        new Thread(task).start();
        return root;
    }

    private HBox buildHeader() {
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = UIFactory.boldLabel("Parc véhicules");
        title.setStyle("-fx-font-family: 'Roboto Condensed'; -fx-font-size: 17px; -fx-font-weight: 700; -fx-text-fill: #F2F2F7;");

        HBox badges = new HBox(8);
        badges.setAlignment(Pos.CENTER_LEFT);

        HBox.setHgrow(badges, Priority.ALWAYS);

        Button addBtn = UIFactory.btnPrimary("+ Ajouter véhicule");
        addBtn.setOnAction(e -> openFormulaireAjout());

        header.getChildren().addAll(title, badges, addBtn);
        return header;
    }

    private VBox buildVehiculeCard(Vehicule v) {
        VBox card = new VBox(8);
        card.setPrefWidth(240);
        card.setMaxWidth(240);
        card.setPadding(new Insets(12));
        card.setStyle("""
            -fx-background-color: #1E1E28;
            -fx-border-color: #2A2A35;
            -fx-border-width: 1.5px;
            -fx-border-radius: 10px;
            -fx-background-radius: 10px;
            -fx-cursor: hand;
            """);

        card.setOnMouseEntered(e -> card.setStyle("""
            -fx-background-color: #1E1E28;
            -fx-border-color: #E00000;
            -fx-border-width: 1.5px;
            -fx-border-radius: 10px;
            -fx-background-radius: 10px;
            -fx-translate-y: -3;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 12, 0, 0, 4);
            -fx-cursor: hand;
            """));
        card.setOnMouseExited(e -> card.setStyle("""
            -fx-background-color: #1E1E28;
            -fx-border-color: #2A2A35;
            -fx-border-width: 1.5px;
            -fx-border-radius: 10px;
            -fx-background-radius: 10px;
            -fx-cursor: hand;
            """));

        // Icône
        String emoji = v.getModele().contains("Hilux") || v.getModele().contains("Ranger")
                       || v.getModele().contains("Fortuner") ? "🛻" : "🚗";
        Label icon = new Label(emoji);
        icon.setStyle("-fx-font-size: 32px; -fx-alignment: center;");
        icon.setMaxWidth(Double.MAX_VALUE);
        icon.setAlignment(Pos.CENTER);
        HBox imgBox = new HBox(icon);
        imgBox.setAlignment(Pos.CENTER);
        imgBox.setPrefHeight(70);
        imgBox.setStyle("-fx-background-color: #26262F; -fx-background-radius: 6px;");

        // Infos
        Label name = UIFactory.boldLabel(v.getMarque() + " " + v.getModele());
        name.setWrapText(true);
        Label immat = UIFactory.monoLabel(v.getImmatriculation() + " · " + v.getAnnee());
        Label km    = UIFactory.bodyLabel("📍 " + String.format("%,d", v.getKilometrage()).replace(",", " ") + " km");

        HBox footer = new HBox(8);
        footer.setAlignment(Pos.CENTER_LEFT);
        Label price = UIFactory.xafLabel(UIFactory.formatXAF(v.getPrixJour()) + "/j");
        HBox.setHgrow(price, Priority.ALWAYS);
        Label statusBadge = UIFactory.badgeStatutVehicule(v.getStatut().name());
        footer.getChildren().addAll(price, statusBadge);

        // Actions
        HBox actions = new HBox(6);
        Button editBtn = UIFactory.btnSecondary("✏");
        editBtn.setOnAction(e -> openFormulaireModif(v));

        if (v.getStatut().name().equals("DISPONIBLE")) {
            Button louerBtn = UIFactory.btnSuccess("+ Louer");
            louerBtn.setOnAction(e -> dashboard.navigateTo("contrats"));
            Button maintBtn = new Button("🔧");
            maintBtn.setStyle(editBtn.getStyle());
            maintBtn.setOnAction(e -> dashboard.navigateTo("maintenance"));
            actions.getChildren().addAll(editBtn, louerBtn, maintBtn);
        } else {
            actions.getChildren().add(editBtn);
        }

        card.setOnMouseClicked(e -> openDetail(v));
        card.getChildren().addAll(imgBox, name, immat, km, footer, actions);
        return card;
    }

    private void openDetail(Vehicule v) {
        VBox content = new VBox(0);
        // Header dégradé
        VBox dpHead = new VBox(4);
        dpHead.setPadding(new Insets(18));
        dpHead.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, rgba(224,0,0,0.1), #1E1E28);");
        Label dpName  = UIFactory.boldLabel(v.getMarque() + " " + v.getModele());
        dpName.setStyle("-fx-font-family: 'Roboto Condensed'; -fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #F2F2F7;");
        Label dpImmat = UIFactory.monoLabel(v.getImmatriculation() + " · Année " + v.getAnnee());
        dpHead.getChildren().addAll(dpName, dpImmat);

        // Grid infos
        GridPane grid = new GridPane();
        grid.setHgap(1); grid.setVgap(1);
        grid.setStyle("-fx-background-color: #2A2A35;");
        addDpCell(grid, "Prix / jour", UIFactory.formatXAF(v.getPrixJour()), 0, 0, true);
        addDpCell(grid, "Statut", v.getStatut().name(), 0, 1, false);
        addDpCell(grid, "Kilométrage", String.format("%,d", v.getKilometrage()).replace(",", " ") + " km", 1, 0, false);
        addDpCell(grid, "Immatriculation", v.getImmatriculation(), 1, 1, false);

        HBox btns = new HBox(8);
        btns.setPadding(new Insets(14, 18, 14, 18));
        Button louer  = UIFactory.btnPrimary("+ Créer contrat");
        louer.setOnAction(e -> dashboard.navigateTo("contrats"));
        Button edit   = UIFactory.btnSecondary("✏ Modifier");
        edit.setOnAction(e -> openFormulaireModif(v));
        Button suppr  = UIFactory.btnDanger("🗑 Supprimer");
        btns.getChildren().addAll(louer, edit, suppr);

        content.getChildren().addAll(dpHead, UIFactory.hsep(), grid, btns);
        Stage modal = UIFactory.createModal(dashboard.getStage(), v.getMarque() + " " + v.getModele(), content, 520);
        modal.show();
    }

    private void addDpCell(GridPane g, String label, String value, int row, int col, boolean isXaf) {
        VBox cell = new VBox(3);
        cell.setPadding(new Insets(11, 16, 11, 16));
        cell.setStyle("-fx-background-color: #16161E;");
        Label lbl = UIFactory.sectionLabel(label);
        Label val = isXaf ? UIFactory.xafLabel(value) : UIFactory.boldLabel(value);
        cell.getChildren().addAll(lbl, val);
        g.add(cell, col, row);
        GridPane.setHgrow(cell, Priority.ALWAYS);
    }

    public void openFormulaireAjout() {
        VBox form = new VBox(14);
        form.setPadding(new Insets(18));
        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(14);
        VBox marqueF  = UIFactory.formField("Marque", "Toyota, Mercedes…", false);
        VBox modeleF  = UIFactory.formField("Modèle", "Land Cruiser 300", false);
        VBox anneeF   = UIFactory.formField("Année", "2023", false);
        VBox immatF   = UIFactory.formField("Immatriculation", "BZV-2026-001", false);
        VBox prixF    = UIFactory.formField("Prix / jour (XAF)", "50000", false);
        VBox kmF      = UIFactory.formField("Kilométrage", "0", false);
        VBox photoField = UIFactory.formField("Chemin photo (optionnel)", "/photos/vehicule.jpg", false);

        grid.add(marqueF, 0, 0); grid.add(modeleF, 1, 0); grid.add(anneeF, 2, 0);
        grid.add(immatF,  0, 1); grid.add(prixF,   1, 1); grid.add(kmF,    2, 1);
        GridPane.setColumnSpan(photoField, 3);
        grid.add(photoField, 0, 2);
        for (int i = 0; i < 3; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(cc);
        }

        Label errLabel = new Label("");
        errLabel.setStyle("-fx-text-fill:#FF4444;-fx-font-size:11px;");
        form.getChildren().addAll(grid, errLabel);

        Button save = UIFactory.btnPrimary("+ Ajouter au parc");
        Button cancel = UIFactory.btnSecondary("Annuler");

        HBox footer = UIFactory.formFooter(cancel, save);
        VBox content = new VBox(form, footer);
        Stage modal = UIFactory.createModal(dashboard.getStage(), "Ajouter un véhicule", content, 680);
        cancel.setOnAction(e -> modal.close());
        save.setOnAction(e -> {
            String marque = ((TextField) marqueF.getChildren().get(1)).getText().trim();
            String modele = ((TextField) modeleF.getChildren().get(1)).getText().trim();
            String anneeS = ((TextField) anneeF.getChildren().get(1)).getText().trim();
            String immat  = ((TextField) immatF.getChildren().get(1)).getText().trim();
            String prixS  = ((TextField) prixF.getChildren().get(1)).getText().trim();
            String kmS    = ((TextField) kmF.getChildren().get(1)).getText().trim();
            String photo  = ((TextField) photoField.getChildren().get(1)).getText().trim();

            if (marque.isEmpty() || modele.isEmpty() || immat.isEmpty()) {
                errLabel.setText("Marque, modèle et immatriculation sont obligatoires.");
                return;
            }
            try {
                int annee = anneeS.isEmpty() ? 2024 : Integer.parseInt(anneeS);
                double prix = prixS.isEmpty() ? 0 : Double.parseDouble(prixS.replace(" ", "").replace(",", "."));
                int km = kmS.isEmpty() ? 0 : Integer.parseInt(kmS.replace(" ", ""));

                Vehicule v = new Vehicule();
                v.setMarque(marque);
                v.setModele(modele);
                v.setAnnee(annee);
                v.setImmatriculation(immat);
                v.setPrixJour(prix);
                v.setKilometrage(km);
                v.setStatut(cg.egjs.autorent.model.StatutVehicule.DISPONIBLE);
                v.setIdAgence(dashboard.getUser().getIdAgence());

                controller.ajouterVehicule(v);
                modal.close();
                dashboard.refreshView();
            } catch (NumberFormatException ex) {
                errLabel.setText("Année, prix et kilométrage doivent être des nombres.");
            } catch (Exception ex) {
                errLabel.setText("Erreur : " + ex.getMessage());
            }
        });
        modal.show();
    }

    private void openFormulaireModif(Vehicule v) {
        VBox form = new VBox(14);
        form.setPadding(new Insets(18));
        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(14);
        VBox marqueF = addEditField(grid, "Marque", v.getMarque(), 0, 0);
        VBox modeleF = addEditField(grid, "Modèle", v.getModele(), 1, 0);
        VBox anneeF  = addEditField(grid, "Année", String.valueOf(v.getAnnee()), 2, 0);
        VBox immatF  = addEditField(grid, "Immatriculation", v.getImmatriculation(), 0, 1);
        VBox prixF   = addEditField(grid, "Prix / jour (XAF)", String.valueOf((int)v.getPrixJour()), 1, 1);
        VBox kmF     = addEditField(grid, "Kilométrage", String.valueOf(v.getKilometrage()), 2, 1);
        for (int i = 0; i < 3; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(cc);
        }

        Label errLabel = new Label("");
        errLabel.setStyle("-fx-text-fill:#FF4444;-fx-font-size:11px;");
        form.getChildren().addAll(grid, errLabel);

        Button save = UIFactory.btnPrimary("💾 Enregistrer");
        Button cancel = UIFactory.btnSecondary("Annuler");
        HBox footer = UIFactory.formFooter(cancel, save);
        VBox content = new VBox(form, footer);
        Stage modal = UIFactory.createModal(dashboard.getStage(), "Modifier — " + v.getMarque() + " " + v.getModele(), content, 680);
        cancel.setOnAction(e -> modal.close());
        save.setOnAction(e -> {
            try {
                v.setMarque(((TextField) marqueF.getChildren().get(1)).getText().trim());
                v.setModele(((TextField) modeleF.getChildren().get(1)).getText().trim());
                v.setAnnee(Integer.parseInt(((TextField) anneeF.getChildren().get(1)).getText().trim()));
                v.setImmatriculation(((TextField) immatF.getChildren().get(1)).getText().trim());
                v.setPrixJour(Double.parseDouble(((TextField) prixF.getChildren().get(1)).getText().trim().replace(" ", "").replace(",", ".")));
                v.setKilometrage(Integer.parseInt(((TextField) kmF.getChildren().get(1)).getText().trim().replace(" ", "")));
                controller.modifierVehicule(v);
                modal.close();
                dashboard.refreshView();
            } catch (NumberFormatException ex) {
                errLabel.setText("Année, prix et kilométrage doivent être des nombres.");
            } catch (Exception ex) {
                errLabel.setText("Erreur : " + ex.getMessage());
            }
        });
        modal.show();
    }

    private VBox addEditField(GridPane g, String label, String value, int col, int row) {
        VBox f = UIFactory.formField(label, value, false);
        ((TextField) f.getChildren().get(1)).setText(value);
        g.add(f, col, row);
        return f;
    }
}
