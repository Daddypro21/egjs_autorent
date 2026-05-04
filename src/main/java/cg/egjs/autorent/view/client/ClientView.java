package cg.egjs.autorent.view.client;

import cg.egjs.autorent.dao.UtilisateurDAO;
import cg.egjs.autorent.model.RoleUtilisateur;
import cg.egjs.autorent.model.Utilisateur;
import cg.egjs.autorent.util.PasswordUtil;
import cg.egjs.autorent.view.components.UIFactory;
import cg.egjs.autorent.view.dashboard.DashboardView;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;

/**
 * Vue complète de gestion des clients.
 * Liste en grille, fiche détail, création, désactivation.
 */
public class ClientView {

    private final DashboardView  dashboard;
    private final UtilisateurDAO dao = new UtilisateurDAO();

    public ClientView(DashboardView dashboard) {
        this.dashboard = dashboard;
    }

    public Pane build() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #0A0A0F;");

        HBox header = buildHeader();
        root.getChildren().add(header);

        Label loading = UIFactory.bodyLabel("Chargement des clients…");
        root.getChildren().add(loading);

        Task<List<Utilisateur>> task = new Task<>() {
            @Override protected List<Utilisateur> call() throws Exception {
                return dao.findAllClients();
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            root.getChildren().remove(loading);
            List<Utilisateur> clients = task.getValue();
            if (clients.isEmpty()) {
                root.getChildren().add(UIFactory.emptyState("👤", "Aucun client enregistré"));
            } else {
                root.getChildren().add(buildStats(clients));
                root.getChildren().add(buildGrid(clients));
            }
        }));

        task.setOnFailed(e -> Platform.runLater(() -> {
            root.getChildren().remove(loading);
            root.getChildren().add(UIFactory.emptyState("⚠️", "Erreur : " + task.getException().getMessage()));
        }));

        new Thread(task).start();
        return root;
    }

    private HBox buildHeader() {
        HBox h = new HBox(12);
        h.setAlignment(Pos.CENTER_LEFT);
        Label title = UIFactory.boldLabel("Gestion des clients");
        title.setStyle("-fx-font-family:'Roboto Condensed';-fx-font-size:17px;-fx-font-weight:700;-fx-text-fill:#F2F2F7;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Button add = UIFactory.btnPrimary("+ Nouveau client");
        add.setOnAction(e -> openFormulaire(null));
        h.getChildren().addAll(title, sp, add);
        return h;
    }

    private HBox buildStats(List<Utilisateur> clients) {
        long actifs    = clients.stream().filter(Utilisateur::estActif).count();
        long inactifs  = clients.size() - actifs;
        HBox row = new HBox(12);
        row.getChildren().addAll(
            UIFactory.kpiCard("TOTAL CLIENTS",   String.valueOf(clients.size()), "enregistrés", UIFactory.BLUE),
            UIFactory.kpiCard("COMPTES ACTIFS",  String.valueOf(actifs),         "en règle",    UIFactory.GREEN),
            UIFactory.kpiCard("COMPTES BLOQUÉS", String.valueOf(inactifs),       "désactivés",  UIFactory.RED)
        );
        row.getChildren().forEach(n -> HBox.setHgrow((VBox) n, Priority.ALWAYS));
        return row;
    }

    private FlowPane buildGrid(List<Utilisateur> clients) {
        FlowPane grid = new FlowPane();
        grid.setHgap(12); grid.setVgap(12);
        clients.forEach(c -> grid.getChildren().add(buildCard(c)));
        return grid;
    }

    private VBox buildCard(Utilisateur c) {
        VBox card = new VBox(8);
        card.setPrefWidth(280); card.setMaxWidth(280);
        card.setPadding(new Insets(14));
        String border = c.estActif() ? "#2A2A35" : "rgba(224,0,0,0.3)";
        card.setStyle(String.format("-fx-background-color:#1E1E28;-fx-border-color:%s;-fx-border-width:1.5px;-fx-border-radius:10px;-fx-background-radius:10px;-fx-cursor:hand;", border));
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color:#1E1E28;-fx-border-color:#60A5FA;-fx-border-width:1.5px;-fx-border-radius:10px;-fx-background-radius:10px;-fx-translate-y:-2;-fx-cursor:hand;"));
        card.setOnMouseExited(e  -> card.setStyle(String.format("-fx-background-color:#1E1E28;-fx-border-color:%s;-fx-border-width:1.5px;-fx-border-radius:10px;-fx-background-radius:10px;-fx-cursor:hand;", border)));
        card.setOnMouseClicked(e -> openDetail(c));

        // Avatar + nom
        HBox top = new HBox(10); top.setAlignment(Pos.CENTER_LEFT);
        String initials = (c.getPrenom() != null && !c.getPrenom().isEmpty() ? c.getPrenom().substring(0,1) : "")
                        + (c.getNom()    != null && !c.getNom().isEmpty()    ? c.getNom().substring(0,1)    : "");
        Label av = new Label(initials);
        av.setStyle("-fx-background-color:linear-gradient(from 0% 0% to 100% 100%,#26262F,#1E1E28);-fx-background-radius:19px;-fx-border-color:#3A3A48;-fx-border-width:2px;-fx-border-radius:19px;-fx-min-width:38px;-fx-min-height:38px;-fx-max-width:38px;-fx-max-height:38px;-fx-alignment:center;-fx-font-weight:700;-fx-font-size:14px;-fx-text-fill:#E00000;");
        VBox info = new VBox(2);
        Label nm = UIFactory.boldLabel(c.getPrenom() + " " + c.getNom());
        Label em = UIFactory.bodyLabel(c.getEmail());
        em.setStyle(em.getStyle() + ";-fx-max-width:190px;");
        info.getChildren().addAll(nm, em);
        top.getChildren().addAll(av, info);

        // Permis
        Label pm = UIFactory.monoLabel("🪪 " + (c.getNumPermis() != null ? c.getNumPermis() : "—"));

        // Stats + statut
        HBox foot = new HBox(8); foot.setAlignment(Pos.CENTER_LEFT);
        Label statut = c.estActif()
            ? UIFactory.badge("● ACTIF",    "rgba(34,197,94,0.1)",  UIFactory.GREEN, "rgba(34,197,94,0.2)")
            : UIFactory.badge("● BLOQUÉ",   "rgba(224,0,0,0.1)",    UIFactory.RED,   "rgba(224,0,0,0.2)");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Button det = UIFactory.btnSecondary("Voir →");
        det.setOnAction(e -> openDetail(c));
        foot.getChildren().addAll(statut, sp, det);

        card.getChildren().addAll(top, pm, foot);
        return card;
    }

    // ── Fiche détail ──
    private void openDetail(Utilisateur c) {
        VBox content = new VBox(0);

        // Header
        HBox head = new HBox(14); head.setAlignment(Pos.CENTER_LEFT);
        head.setPadding(new Insets(18));
        head.setStyle("-fx-background-color:linear-gradient(from 0% 0% to 100% 100%,rgba(96,165,250,0.1),#1E1E28);");
        String ini = (c.getPrenom()!=null&&!c.getPrenom().isEmpty()?c.getPrenom().substring(0,1):"")
                   + (c.getNom()!=null&&!c.getNom().isEmpty()?c.getNom().substring(0,1):"");
        Label av = new Label(ini);
        av.setStyle("-fx-background-color:linear-gradient(from 0% 0% to 100% 100%,#26262F,#1E1E28);-fx-background-radius:24px;-fx-border-color:#3A3A48;-fx-border-width:2px;-fx-border-radius:24px;-fx-min-width:48px;-fx-min-height:48px;-fx-max-width:48px;-fx-max-height:48px;-fx-alignment:center;-fx-font-weight:700;-fx-font-size:18px;-fx-text-fill:#E00000;");
        VBox inf = new VBox(3);
        Label nm = new Label(c.getPrenom()+" "+c.getNom());
        nm.setStyle("-fx-font-size:17px;-fx-font-weight:700;-fx-text-fill:#F2F2F7;");
        Label em = UIFactory.bodyLabel(c.getEmail());
        inf.getChildren().addAll(nm, em);
        head.getChildren().addAll(av, inf);

        content.getChildren().add(head);
        content.getChildren().add(UIFactory.hsep());

        // Grille infos
        GridPane grid = new GridPane();
        grid.setHgap(1); grid.setVgap(1);
        grid.setStyle("-fx-background-color:#2A2A35;");
        addCell(grid, "Téléphone",  c.getTelephone()  != null ? c.getTelephone()  : "—", 0, 0);
        addCell(grid, "N° Permis",  c.getNumPermis()  != null ? c.getNumPermis()  : "—", 0, 1);
        addCell(grid, "Adresse",    c.getAdresse()    != null ? c.getAdresse()    : "—", 1, 0);
        addCell(grid, "Statut",     c.estActif() ? "ACTIF" : "BLOQUÉ",                   1, 1);
        content.getChildren().add(grid);
        content.getChildren().add(UIFactory.hsep());

        // Actions
        HBox acts = new HBox(8); acts.setPadding(new Insets(14,18,14,18));
        Button newCtr = UIFactory.btnPrimary("+ Nouveau contrat");
        newCtr.setOnAction(e -> dashboard.navigateTo("contrats"));
        Button edit = UIFactory.btnSecondary("✏ Modifier");
        edit.setOnAction(evt -> { /* fermer et ouvrir formulaire */ openFormulaire(c); });
        Button block = UIFactory.btnDanger(c.estActif() ? "🔒 Désactiver" : "🔓 Réactiver");
        acts.getChildren().addAll(newCtr, edit, block);
        content.getChildren().add(acts);

        Stage modal = UIFactory.createModal(dashboard.getStage(),
            "Fiche client — " + c.getPrenom() + " " + c.getNom(), content, 520);
        modal.show();
    }

    // ── Formulaire création / modification ──
    public void openFormulaire(Utilisateur existing) {
        boolean isNew = (existing == null);
        VBox form = new VBox(14); form.setPadding(new Insets(18));
        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(14);
        for (int i = 0; i < 2; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(cc);
        }

        VBox nomF    = UIFactory.formField("Nom",      isNew?"MOUKALA" : existing.getNom(),    false);
        VBox prenomF = UIFactory.formField("Prénom",   isNew?"Jean-Pierre":existing.getPrenom(),false);
        VBox emailF  = UIFactory.formField("Email",    isNew?"client@gmail.com":existing.getEmail(),false);
        VBox telF    = UIFactory.formField("Téléphone",isNew?"+242 06 XXX XXXX":(existing.getTelephone()!=null?existing.getTelephone():""),false);
        VBox adrF    = UIFactory.formField("Adresse",  isNew?"Bacongo, Brazzaville":(existing.getAdresse()!=null?existing.getAdresse():""),false);
        VBox pmF     = UIFactory.formField("N° Permis de conduire","CG-BZV-2024-XXXXX",false);
        VBox mdpF    = UIFactory.formField("Mot de passe","Minimum 8 caractères",true);

        if (!isNew && existing.getNumPermis()!=null)
            ((TextField)pmF.getChildren().get(1)).setText(existing.getNumPermis());

        // hint permis
        Label hintPm = new Label("⚠ Obligatoire pour toute location");
        hintPm.setStyle("-fx-font-size:10px;-fx-text-fill:#55556A;");
        pmF.getChildren().add(hintPm);

        Label hintMdp = new Label("🔐 Sera hashé avec BCrypt avant enregistrement");
        hintMdp.setStyle("-fx-font-size:10px;-fx-text-fill:#55556A;");
        mdpF.getChildren().add(hintMdp);

        grid.add(nomF,    0, 0); grid.add(prenomF, 1, 0);
        grid.add(emailF,  0, 1); grid.add(telF,    1, 1);
        GridPane.setColumnSpan(adrF, 2); grid.add(adrF, 0, 2);
        GridPane.setColumnSpan(pmF,  2); grid.add(pmF,  0, 3);
        if (isNew) { GridPane.setColumnSpan(mdpF,2); grid.add(mdpF,0,4); }

        form.getChildren().add(grid);

        Button save   = UIFactory.btnPrimary(isNew ? "+ Enregistrer le client" : "💾 Sauvegarder");
        Button cancel = UIFactory.btnSecondary("Annuler");
        HBox footer   = UIFactory.formFooter(cancel, save);
        VBox content  = new VBox(form, footer);

        Stage modal = UIFactory.createModal(dashboard.getStage(),
            isNew ? "Nouveau client" : "Modifier — "+existing.getPrenom()+" "+existing.getNom(),
            content, 580);

        // Récupérer les TextField directement depuis les VBox
        TextField nomTf    = (TextField) nomF.getChildren().get(1);
        TextField prenomTf = (TextField) prenomF.getChildren().get(1);
        TextField emailTf  = (TextField) emailF.getChildren().get(1);
        TextField telTf    = (TextField) telF.getChildren().get(1);
        TextField adrTf    = (TextField) adrF.getChildren().get(1);
        TextField pmTf     = (TextField) pmF.getChildren().get(1);

        Label errLabel = new Label("");
        errLabel.setStyle("-fx-text-fill:#FF4444;-fx-font-size:11px;");
        form.getChildren().add(errLabel);

        cancel.setOnAction(e -> modal.close());
        save.setOnAction(e -> {
            String nom    = nomTf.getText().trim();
            String prenom = prenomTf.getText().trim();
            String email  = emailTf.getText().trim();
            String tel    = telTf.getText().trim();
            String adr    = adrTf.getText().trim();
            String permis = pmTf.getText().trim();

            if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty()) {
                errLabel.setText("Nom, prénom et email sont obligatoires.");
                return;
            }

            try {
                if (isNew) {
                    // Récupérer le mot de passe depuis le PasswordField
                    PasswordField mdpPf = (PasswordField) mdpF.getChildren().get(1);
                    String mdp = mdpPf.getText().trim();
                    if (mdp.length() < 8) { errLabel.setText("Mot de passe : 8 caractères minimum."); return; }

                    Utilisateur u = new Utilisateur();
                    u.setNom(nom);
                    u.setPrenom(prenom);
                    u.setEmail(email);
                    u.setTelephone(tel);
                    u.setAdresse(adr);
                    u.setNumPermis(permis);
                    u.setMotDePasseHash(PasswordUtil.hasher(mdp));
                    u.setRole(RoleUtilisateur.CLIENT);
                    u.setIdAgence(dashboard.getUser().getIdAgence());
                    dao.creer(u);
                } else {
                    existing.setNom(nom);
                    existing.setPrenom(prenom);
                    existing.setEmail(email);
                    existing.setTelephone(tel);
                    existing.setAdresse(adr);
                    existing.setNumPermis(permis);
                    dao.mettreAJour(existing);
                }
                modal.close();
                dashboard.refreshView();
            } catch (Exception ex) {
                errLabel.setText("Erreur : " + ex.getMessage());
            }
        });
        modal.show();
    }

    private void addCell(GridPane g, String label, String value, int row, int col) {
        VBox cell = new VBox(3); cell.setPadding(new Insets(11,16,11,16));
        cell.setStyle("-fx-background-color:#16161E;");
        cell.getChildren().addAll(UIFactory.sectionLabel(label), UIFactory.boldLabel(value));
        g.add(cell, col, row); GridPane.setHgrow(cell, Priority.ALWAYS);
    }
}
