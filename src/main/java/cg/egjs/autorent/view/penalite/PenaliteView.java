package cg.egjs.autorent.view.penalite;

import cg.egjs.autorent.dao.ContratDAO;
import cg.egjs.autorent.dao.PenaliteDAO;
import cg.egjs.autorent.model.Contrat;
import cg.egjs.autorent.model.Penalite;
import cg.egjs.autorent.view.components.UIFactory;
import cg.egjs.autorent.view.dashboard.DashboardView;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * Vue complète de gestion des pénalités de retard.
 * Liste, détail, marquage réglée, reçu PDF.
 */
public class PenaliteView {

    private final DashboardView dashboard;
    private final PenaliteDAO   penaliteDAO = new PenaliteDAO();
    private final ContratDAO    contratDAO  = new ContratDAO();

    public PenaliteView(DashboardView dashboard) {
        this.dashboard = dashboard;
    }

    public Pane build() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color:#0A0A0F;");

        Label loading = UIFactory.bodyLabel("Chargement des pénalités…");
        root.getChildren().add(loading);

        Task<List<Penalite>> task = new Task<>() {
            @Override protected List<Penalite> call() throws Exception {
                // Charger pénalités depuis tous les contrats en retard
                List<Contrat> retards = contratDAO.findEnCours().stream()
                    .filter(c -> c.getStatut().name().equals("EN_RETARD"))
                    .toList();
                List<Penalite> all = new ArrayList<>();
                for (Contrat c : retards) {
                    penaliteDAO.findByContrat(c.getIdContrat()).ifPresent(all::add);
                }
                return all;
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            root.getChildren().remove(loading);
            List<Penalite> penalites = task.getValue();
            root.getChildren().add(buildKPIs(penalites));
            root.getChildren().add(buildSection(penalites));
        }));

        task.setOnFailed(e -> Platform.runLater(() -> {
            root.getChildren().remove(loading);
            root.getChildren().add(UIFactory.emptyState("⚠️", "Erreur : " + task.getException().getMessage()));
        }));

        new Thread(task).start();
        return root;
    }

    private HBox buildKPIs(List<Penalite> penalites) {
        double total   = penalites.stream().mapToDouble(Penalite::getMontant).sum();
        double nonRegle= penalites.stream().filter(p -> !p.isRegle()).mapToDouble(Penalite::getMontant).sum();
        long   nbNonRg = penalites.stream().filter(p -> !p.isRegle()).count();

        HBox row = new HBox(12);
        row.getChildren().addAll(
            UIFactory.kpiCard("PÉNALITÉS TOTALES", String.valueOf(penalites.size()), "contrats en retard",         UIFactory.RED),
            UIFactory.kpiCard("MONTANT TOTAL",      UIFactory.formatXAF(total),      "XAF calculés",               UIFactory.RED),
            UIFactory.kpiCard("NON RÉGLÉES",        String.valueOf(nbNonRg),         UIFactory.formatXAF(nonRegle) + " à percevoir", UIFactory.AMBER),
            UIFactory.kpiCard("RÉGLÉES",            String.valueOf(penalites.size()-nbNonRg), "pénalités soldées",  UIFactory.GREEN)
        );
        row.getChildren().forEach(n -> HBox.setHgrow((VBox) n, Priority.ALWAYS));
        return row;
    }

    private VBox buildSection(List<Penalite> penalites) {
        double total = penalites.stream().mapToDouble(Penalite::getMontant).sum();
        VBox section = UIFactory.sectionPane("Pénalités de retard", null, null);

        // Total dans le header
        HBox head = (HBox) section.getChildren().get(0);
        Label totalLbl = UIFactory.xafLabel(UIFactory.formatXAF(total));
        head.getChildren().add(totalLbl);

        if (penalites.isEmpty()) {
            section.getChildren().add(UIFactory.emptyState("✅", "Aucune pénalité active — tous les véhicules ont été restitués à temps"));
            return section;
        }

        // Cards pénalités
        VBox cards = new VBox(10);
        cards.setPadding(new Insets(14));
        penalites.forEach(p -> cards.getChildren().add(buildCard(p)));
        section.getChildren().add(cards);
        return section;
    }

    private HBox buildCard(Penalite p) {
        HBox card = new HBox(14);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(14, 16, 14, 16));
        String borderCol = p.isRegle() ? "#2A2A35" : "rgba(224,0,0,0.3)";
        card.setStyle(String.format(
            "-fx-background-color:#1E1E28;-fx-border-color:%s;-fx-border-width:1.5px;-fx-border-radius:10px;-fx-background-radius:10px;",
            borderCol));
        card.setOnMouseEntered(e -> card.setStyle(String.format(
            "-fx-background-color:%s;-fx-border-color:%s;-fx-border-width:1.5px;-fx-border-radius:10px;-fx-background-radius:10px;",
            p.isRegle() ? "#1E1E28" : "rgba(224,0,0,0.05)", p.isRegle() ? "#3A3A48" : "#E00000")));
        card.setOnMouseExited(e -> card.setStyle(String.format(
            "-fx-background-color:#1E1E28;-fx-border-color:%s;-fx-border-width:1.5px;-fx-border-radius:10px;-fx-background-radius:10px;",
            borderCol)));

        // Infos gauche
        VBox left = new VBox(5); HBox.setHgrow(left, Priority.ALWAYS);

        Label title = UIFactory.boldLabel("Contrat #00" + p.getIdContrat());
        Label detail = UIFactory.monoLabel(
            p.getJoursRetard() + " jour(s) × " + UIFactory.formatXAF(p.getTauxJour()) + "/j"
            + "   ·   Calculé le " + (p.getDateCalcul() != null ? p.getDateCalcul().toLocalDate() : "—"));

        HBox badges = new HBox(8);
        badges.setAlignment(Pos.CENTER_LEFT);
        Label statut = p.isRegle()
            ? UIFactory.badge("● RÉGLÉE",     "rgba(34,197,94,0.1)",  UIFactory.GREEN, "rgba(34,197,94,0.2)")
            : UIFactory.badge("● NON RÉGLÉE", "rgba(224,0,0,0.15)",   "#FF2020",       "rgba(224,0,0,0.3)");
        badges.getChildren().add(statut);

        if (!p.isRegle()) {
            Button regler = UIFactory.btnSuccess("✔ Marquer réglée");
            regler.setOnAction(e -> marquerRegle(p));
            badges.getChildren().add(regler);
        }

        Button pdf = new Button("📄 Reçu PDF");
        pdf.setStyle(UIFactory.btnSecondary("").getStyle());
        pdf.setOnAction(e -> openRecu(p));
        badges.getChildren().add(pdf);

        Button detail2 = UIFactory.btnSecondary("Détail");
        detail2.setOnAction(e -> openDetail(p));
        badges.getChildren().add(detail2);

        left.getChildren().addAll(title, detail, badges);

        // Montant droite
        VBox right = new VBox(3); right.setAlignment(Pos.CENTER_RIGHT);
        Label montant = new Label(UIFactory.formatXAF(p.getMontant()));
        montant.setStyle("-fx-font-family:'Roboto Condensed';-fx-font-size:24px;-fx-font-weight:700;-fx-text-fill:#E00000;");
        Label sub = UIFactory.bodyLabel("pénalité");
        right.getChildren().addAll(montant, sub);

        card.getChildren().addAll(left, right);
        return card;
    }

    private void marquerRegle(Penalite p) {
        boolean confirm = UIFactory.showConfirm(dashboard.getStage(),
            "Confirmer le règlement",
            "Marquer la pénalité de " + UIFactory.formatXAF(p.getMontant()) + " comme réglée ?");
        if (confirm) {
            try {
                penaliteDAO.marquerRegle(p.getIdPenalite());
                dashboard.refreshView();
            } catch (Exception ex) {
                UIFactory.showError(dashboard.getStage(), "Erreur : " + ex.getMessage());
            }
        }
    }

    private void openDetail(Penalite p) {
        VBox content = new VBox(0);

        // Header rouge
        VBox head = new VBox(4); head.setPadding(new Insets(18));
        head.setStyle("-fx-background-color:linear-gradient(from 0% 0% to 100% 100%,rgba(224,0,0,0.12),#1E1E28);");
        Label t = new Label("Pénalité — Contrat #00" + p.getIdContrat());
        t.setStyle("-fx-font-family:'Roboto Condensed';-fx-font-size:18px;-fx-font-weight:700;-fx-text-fill:#F2F2F7;");
        Label s = UIFactory.bodyLabel("Calculée le " + (p.getDateCalcul() != null ? p.getDateCalcul().toLocalDate() : "—"));
        head.getChildren().addAll(t, s);
        content.getChildren().addAll(head, UIFactory.hsep());

        // Montant preview
        Label mpVal = new Label(UIFactory.formatXAF(p.getMontant()));
        mpVal.setStyle("-fx-font-family:'Roboto Condensed';-fx-font-size:26px;-fx-font-weight:700;-fx-text-fill:#E00000;");
        HBox mp = UIFactory.montantPreview(mpVal);
        mp.setStyle(mp.getStyle() + "-fx-margin:14px;");
        VBox mpWrap = new VBox(mp); mpWrap.setPadding(new Insets(14, 18, 14, 18));
        content.getChildren().add(mpWrap);
        content.getChildren().add(UIFactory.hsep());

        // Grille détail
        GridPane grid = new GridPane();
        grid.setHgap(1); grid.setVgap(1);
        grid.setStyle("-fx-background-color:#2A2A35;");
        addCell(grid, "Jours de retard", p.getJoursRetard() + " jours", 0, 0);
        addCell(grid, "Taux journalier",  UIFactory.formatXAF(p.getTauxJour()),  0, 1);
        addCell(grid, "Statut paiement",  p.isRegle() ? "RÉGLÉE" : "NON RÉGLÉE", 1, 0);
        addCell(grid, "Date calcul",      p.getDateCalcul() != null ? p.getDateCalcul().toLocalDate().toString() : "—", 1, 1);
        content.getChildren().add(grid);
        content.getChildren().add(UIFactory.hsep());

        // Actions
        HBox acts = new HBox(8); acts.setPadding(new Insets(14, 18, 14, 18));
        if (!p.isRegle()) {
            Button regler = UIFactory.btnPrimary("✔ Marquer réglée");
            regler.setOnAction(e -> marquerRegle(p));
            acts.getChildren().add(regler);
        }
        Button pdf = UIFactory.btnSecondary("📄 Générer reçu PDF");
        pdf.setOnAction(e -> openRecu(p));
        acts.getChildren().add(pdf);
        content.getChildren().add(acts);

        Stage modal = UIFactory.createModal(dashboard.getStage(),
            "Pénalité — Contrat #00" + p.getIdContrat(), content, 500);
        modal.show();
    }

    private void openRecu(Penalite p) {
        VBox content = new VBox(14);
        content.setPadding(new Insets(22)); content.setAlignment(Pos.CENTER);
        Label icon = new Label("🧾"); icon.setStyle("-fx-font-size:44px;");
        Label title = new Label("REÇU DE PÉNALITÉ — CONTRAT #00" + p.getIdContrat());
        title.setStyle("-fx-font-family:'Roboto Condensed';-fx-font-size:17px;-fx-font-weight:700;-fx-text-fill:#F2F2F7;");
        VBox infos = new VBox(4); infos.setPadding(new Insets(13,18,13,18));
        infos.setStyle("-fx-background-color:#1E1E28;-fx-border-color:#2A2A35;-fx-border-width:1.5px;-fx-border-radius:8px;-fx-background-radius:8px;");
        infos.setMaxWidth(Double.MAX_VALUE);
        for (String line : new String[]{
            "📍  EGJS AutoRent — Avenue de France, Brazzaville",
            "🗓  Pénalité : " + p.getJoursRetard() + " jour(s) × " + UIFactory.formatXAF(p.getTauxJour()),
            "💰  Montant total : " + UIFactory.formatXAF(p.getMontant()),
            "✅  Statut : " + (p.isRegle() ? "RÉGLÉE" : "NON RÉGLÉE")
        }) {
            Label l = new Label(line);
            l.setStyle("-fx-font-family:'Roboto Mono';-fx-font-size:11px;-fx-text-fill:#55556A;");
            infos.getChildren().add(l);
        }
        Button dl = UIFactory.btnPrimary("⬇  Télécharger reçu_penalite_" + p.getIdContrat() + ".pdf");
        dl.setMaxWidth(Double.MAX_VALUE);
        content.getChildren().addAll(icon, title, infos, dl);
        Stage modal = UIFactory.createModal(dashboard.getStage(), "Reçu PDF — Pénalité #" + p.getIdPenalite(), content, 480);
        modal.show();
    }

    private void addCell(GridPane g, String label, String value, int row, int col) {
        VBox cell = new VBox(3); cell.setPadding(new Insets(11,16,11,16));
        cell.setStyle("-fx-background-color:#16161E;");
        cell.getChildren().addAll(UIFactory.sectionLabel(label), UIFactory.boldLabel(value));
        g.add(cell, col, row); GridPane.setHgrow(cell, Priority.ALWAYS);
    }
}
