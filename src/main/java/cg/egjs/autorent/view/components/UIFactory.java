package cg.egjs.autorent.view.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Fabrique de composants UI réutilisables.
 * Centralise la création des éléments récurrents
 * (KPI cards, badges, boutons, formulaires, modales).
 */
public class UIFactory {

    // ── COULEURS ──
    public static final String RED    = "#E00000";
    public static final String GREEN  = "#22C55E";
    public static final String AMBER  = "#F59E0B";
    public static final String BLUE   = "#60A5FA";
    public static final String PURPLE = "#A78BFA";
    public static final String S1     = "#16161E";
    public static final String S2     = "#1E1E28";
    public static final String S3     = "#26262F";
    public static final String B1     = "#2A2A35";
    public static final String B2     = "#3A3A48";
    public static final String TX     = "#F2F2F7";
    public static final String TX2    = "#C8C8D4";
    public static final String TX3    = "#8888A0";
    public static final String TX4    = "#55556A";
    public static final String AMBER_BG = "rgba(245,158,11,0.1)";

    private UIFactory() {}

    // ──────────────────────────────────────────────
    // KPI CARD
    // ──────────────────────────────────────────────
    public static VBox kpiCard(String label, String value, String sub, String accentColor) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(16, 18, 16, 18));
        card.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-border-color: %s;
            -fx-border-width: 1.5px;
            -fx-border-radius: 12px;
            -fx-background-radius: 12px;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 2);
            """, S1, B1));

        // Barre couleur en haut
        Region topBar = new Region();
        topBar.setPrefHeight(3);
        topBar.setStyle(String.format("-fx-background-color: %s; -fx-background-radius: 2px;", accentColor));

        Label lbl = new Label(label);
        lbl.setStyle(String.format("-fx-font-size: 9.5px; -fx-font-weight: 700; -fx-text-fill: %s; -fx-letter-spacing: 1.2px;", TX3));

        Label val = new Label(value);
        val.setStyle(String.format("-fx-font-family: 'Roboto Condensed'; -fx-font-size: 34px; -fx-font-weight: 700; -fx-text-fill: %s;", TX));

        Label subLbl = new Label(sub);
        subLbl.setStyle(String.format("-fx-font-size: 11px; -fx-text-fill: %s;", TX4));

        card.getChildren().addAll(topBar, lbl, val, subLbl);

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-border-color: %s;
            -fx-border-width: 1.5px;
            -fx-border-radius: 12px;
            -fx-background-radius: 12px;
            -fx-translate-y: -3;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 16, 0, 0, 4);
            """, S1, B2)));
        card.setOnMouseExited(e -> card.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-border-color: %s;
            -fx-border-width: 1.5px;
            -fx-border-radius: 12px;
            -fx-background-radius: 12px;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 2);
            """, S1, B1)));

        return card;
    }

    // ──────────────────────────────────────────────
    // SECTION PANE
    // ──────────────────────────────────────────────
    public static VBox sectionPane(String title, String linkText, Runnable linkAction) {
        VBox pane = new VBox();
        pane.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-border-color: %s;
            -fx-border-width: 1.5px;
            -fx-border-radius: 12px;
            -fx-background-radius: 12px;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 2);
            """, S1, B1));

        HBox head = new HBox(10);
        head.setAlignment(Pos.CENTER_LEFT);
        head.setPadding(new Insets(12, 18, 12, 18));
        head.setStyle(String.format("-fx-border-color: %s; -fx-border-width: 0 0 1.5 0;", B1));

        Label titleLbl = new Label(title);
        titleLbl.setStyle(String.format("-fx-font-family: 'Roboto Condensed'; -fx-font-size: 15px; -fx-font-weight: 700; -fx-text-fill: %s;", TX));
        HBox.setHgrow(titleLbl, Priority.ALWAYS);

        head.getChildren().add(titleLbl);

        if (linkText != null) {
            Label link = new Label(linkText);
            link.setStyle(String.format("-fx-font-size: 11.5px; -fx-text-fill: %s; -fx-cursor: hand;", RED));
            if (linkAction != null) link.setOnMouseClicked(e -> linkAction.run());
            head.getChildren().add(link);
        }

        pane.getChildren().add(head);
        return pane;
    }

    // ──────────────────────────────────────────────
    // BADGE
    // ──────────────────────────────────────────────
    public static Label badge(String text, String bgColor, String textColor, String borderColor) {
        Label lbl = new Label(text);
        lbl.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-text-fill: %s;
            -fx-border-color: %s;
            -fx-border-width: 1px;
            -fx-border-radius: 20px;
            -fx-background-radius: 20px;
            -fx-font-size: 10px;
            -fx-font-weight: 700;
            -fx-padding: 3 9 3 9;
            """, bgColor, textColor, borderColor));
        return lbl;
    }

    public static Label badgeStatutVehicule(String statut) {
        return switch (statut) {
            case "DISPONIBLE"     -> badge("● DISPONIBLE",  "rgba(34,197,94,0.1)",  GREEN,  "rgba(34,197,94,0.2)");
            case "LOUE"           -> badge("● LOUÉ",        "rgba(224,0,0,0.1)",    RED,    "rgba(224,0,0,0.2)");
            case "EN_MAINTENANCE" -> badge("● MAINTENANCE", "rgba(245,158,11,0.1)", AMBER,  "rgba(245,158,11,0.2)");
            case "HORS_SERVICE"   -> badge("● HORS SERVICE",S3,                    TX4,    B1);
            default               -> badge(statut,          S3,                    TX4,    B1);
        };
    }

    public static Label badgeStatutContrat(String statut) {
        return switch (statut) {
            case "EN_COURS"   -> badge("● EN COURS",  "rgba(96,165,250,0.1)",  BLUE,   "rgba(96,165,250,0.2)");
            case "TERMINE"    -> badge("● TERMINÉ",   "rgba(34,197,94,0.1)",   GREEN,  "rgba(34,197,94,0.2)");
            case "EN_RETARD"  -> badge("● EN RETARD", "rgba(224,0,0,0.15)",    "#FF2020","rgba(224,0,0,0.3)");
            case "ANNULE"     -> badge("● ANNULÉ",    S3,                      TX4,    B1);
            default           -> badge(statut,        S3,                      TX4,    B1);
        };
    }

    // ──────────────────────────────────────────────
    // BOUTONS
    // ──────────────────────────────────────────────
    public static Button btnPrimary(String text) {
        Button btn = new Button(text);
        btn.setStyle(String.format("""
            -fx-background-color: %s; -fx-text-fill: white;
            -fx-font-weight: 700; -fx-font-size: 12.5px;
            -fx-padding: 8 16 8 16; -fx-background-radius: 8px; -fx-cursor: hand;
            -fx-effect: dropshadow(gaussian, rgba(224,0,0,0.3), 8, 0, 0, 3);
            """, RED));
        btn.setOnMouseEntered(e -> btn.setStyle("""
            -fx-background-color: #FF2020; -fx-text-fill: white;
            -fx-font-weight: 700; -fx-font-size: 12.5px;
            -fx-padding: 8 16 8 16; -fx-background-radius: 8px; -fx-cursor: hand;
            -fx-translate-y: -1;
            -fx-effect: dropshadow(gaussian, rgba(224,0,0,0.45), 12, 0, 0, 4);
            """));
        btn.setOnMouseExited(e -> btn.setStyle(String.format("""
            -fx-background-color: %s; -fx-text-fill: white;
            -fx-font-weight: 700; -fx-font-size: 12.5px;
            -fx-padding: 8 16 8 16; -fx-background-radius: 8px; -fx-cursor: hand;
            -fx-effect: dropshadow(gaussian, rgba(224,0,0,0.3), 8, 0, 0, 3);
            """, RED)));
        return btn;
    }

    public static Button btnSecondary(String text) {
        Button btn = new Button(text);
        btn.setStyle(String.format("""
            -fx-background-color: %s; -fx-border-color: %s;
            -fx-border-width: 1.5px; -fx-border-radius: 8px; -fx-background-radius: 8px;
            -fx-text-fill: %s; -fx-font-size: 12.5px;
            -fx-padding: 7 13 7 13; -fx-cursor: hand;
            """, S2, B1, TX3));
        btn.setOnMouseEntered(e -> btn.setStyle(String.format("""
            -fx-background-color: %s; -fx-border-color: %s;
            -fx-border-width: 1.5px; -fx-border-radius: 8px; -fx-background-radius: 8px;
            -fx-text-fill: %s; -fx-font-size: 12.5px;
            -fx-padding: 7 13 7 13; -fx-cursor: hand;
            """, S3, B2, TX)));
        btn.setOnMouseExited(e -> btn.setStyle(String.format("""
            -fx-background-color: %s; -fx-border-color: %s;
            -fx-border-width: 1.5px; -fx-border-radius: 8px; -fx-background-radius: 8px;
            -fx-text-fill: %s; -fx-font-size: 12.5px;
            -fx-padding: 7 13 7 13; -fx-cursor: hand;
            """, S2, B1, TX3)));
        return btn;
    }

    public static Button btnDanger(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: rgba(224,0,0,0.1); -fx-text-fill: #E00000; -fx-border-color: rgba(224,0,0,0.2); -fx-border-width: 1px; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-font-size: 11.5px; -fx-font-weight: 700; -fx-padding: 4 10 4 10; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #E00000; -fx-text-fill: white; -fx-border-color: #E00000; -fx-border-width: 1px; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-font-size: 11.5px; -fx-font-weight: 700; -fx-padding: 4 10 4 10; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: rgba(224,0,0,0.1); -fx-text-fill: #E00000; -fx-border-color: rgba(224,0,0,0.2); -fx-border-width: 1px; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-font-size: 11.5px; -fx-font-weight: 700; -fx-padding: 4 10 4 10; -fx-cursor: hand;"));
        return btn;
    }

    public static Button btnSuccess(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: rgba(34,197,94,0.1); -fx-text-fill: #22C55E; -fx-border-color: rgba(34,197,94,0.2); -fx-border-width: 1px; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-font-size: 11.5px; -fx-font-weight: 700; -fx-padding: 4 10 4 10; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #22C55E; -fx-text-fill: white; -fx-border-color: #22C55E; -fx-border-width: 1px; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-font-size: 11.5px; -fx-font-weight: 700; -fx-padding: 4 10 4 10; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: rgba(34,197,94,0.1); -fx-text-fill: #22C55E; -fx-border-color: rgba(34,197,94,0.2); -fx-border-width: 1px; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-font-size: 11.5px; -fx-font-weight: 700; -fx-padding: 4 10 4 10; -fx-cursor: hand;"));
        return btn;
    }

    // ──────────────────────────────────────────────
    // FORM FIELD
    // ──────────────────────────────────────────────
    public static VBox formField(String labelText, String prompt, boolean isPassword) {
        VBox box = new VBox(5);
        Label lbl = new Label(labelText.toUpperCase());
        lbl.setStyle(String.format("-fx-font-size: 10.5px; -fx-font-weight: 700; -fx-text-fill: %s; -fx-letter-spacing: 0.3px;", TX3));

        Control field = isPassword ? new PasswordField() : new TextField();
        if (field instanceof TextField tf) tf.setPromptText(prompt);
        else if (field instanceof PasswordField pf) pf.setPromptText(prompt);
        field.setPrefHeight(38);
        styleFormControl(field);

        box.getChildren().addAll(lbl, field);
        return box;
    }

    public static VBox formCombo(String labelText, String... options) {
        VBox box = new VBox(5);
        Label lbl = new Label(labelText.toUpperCase());
        lbl.setStyle(String.format("-fx-font-size: 10.5px; -fx-font-weight: 700; -fx-text-fill: %s;", TX3));

        ComboBox<String> combo = new ComboBox<>();
        combo.getItems().addAll(options);
        combo.setPrefHeight(38);
        combo.setMaxWidth(Double.MAX_VALUE);
        combo.setStyle(String.format("""
            -fx-background-color: %s; -fx-border-color: %s;
            -fx-border-width: 1.5px; -fx-border-radius: 8px; -fx-background-radius: 8px;
            -fx-font-size: 13px;
            """, S2, B1));

        box.getChildren().addAll(lbl, combo);
        return box;
    }

    public static VBox formDate(String labelText) {
        VBox box = new VBox(5);
        Label lbl = new Label(labelText.toUpperCase());
        lbl.setStyle(String.format("-fx-font-size: 10.5px; -fx-font-weight: 700; -fx-text-fill: %s;", TX3));

        DatePicker dp = new DatePicker();
        dp.setPrefHeight(38);
        dp.setMaxWidth(Double.MAX_VALUE);
        dp.setStyle(String.format("""
            -fx-background-color: %s; -fx-border-color: %s;
            -fx-border-width: 1.5px; -fx-border-radius: 8px; -fx-background-radius: 8px;
            -fx-font-size: 13px;
            """, S2, B1));

        box.getChildren().addAll(lbl, dp);
        return box;
    }

    private static void styleFormControl(Control field) {
        String base = String.format("""
            -fx-background-color: %s;
            -fx-control-inner-background: %s;
            -fx-border-color: %s;
            -fx-border-width: 1.5px; -fx-border-radius: 8px; -fx-background-radius: 8px;
            -fx-text-fill: %s; -fx-prompt-text-fill: %s;
            -fx-highlight-fill: %s; -fx-highlight-text-fill: #FFFFFF;
            -fx-padding: 8 12 8 12; -fx-font-size: 13px;
            """, S2, S2, B1, TX, TX4, RED);
        field.setStyle(base);
        field.focusedProperty().addListener((obs, old, focused) -> {
            if (focused) field.setStyle(base + String.format(
                "-fx-border-color: %s; -fx-effect: dropshadow(gaussian, rgba(224,0,0,0.13), 6, 0, 0, 0);", RED));
            else         field.setStyle(base);
        });
    }

    // ──────────────────────────────────────────────
    // MONTANT PREVIEW
    // ──────────────────────────────────────────────
    public static HBox montantPreview(Label valueLabel) {
        HBox box = new HBox();
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(14, 18, 14, 18));
        box.setStyle(String.format("""
            -fx-background-color: %s; -fx-border-color: %s;
            -fx-border-width: 1.5px; -fx-border-radius: 8px; -fx-background-radius: 8px;
            """, S3, B1));

        VBox left = new VBox(3);
        Label lbl = new Label("Montant total calculé automatiquement");
        lbl.setStyle(String.format("-fx-font-size: 11.5px; -fx-text-fill: %s;", TX3));
        Label hint = new Label("Prix/jour × nombre de jours");
        hint.setStyle(String.format("-fx-font-size: 10px; -fx-text-fill: %s;", TX4));
        left.getChildren().addAll(lbl, hint);
        HBox.setHgrow(left, Priority.ALWAYS);

        valueLabel.setStyle(String.format("-fx-font-family: 'Roboto Condensed'; -fx-font-size: 26px; -fx-font-weight: 700; -fx-text-fill: %s;", GREEN));

        box.getChildren().addAll(left, valueLabel);
        return box;
    }

    // ──────────────────────────────────────────────
    // WARN BOX
    // ──────────────────────────────────────────────
    public static HBox warnBox(String title, String detail) {
        HBox box = new HBox(11);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(11, 14, 11, 14));
        box.setStyle("""
            -fx-background-color: rgba(224,0,0,0.08);
            -fx-border-color: rgba(224,0,0,0.25);
            -fx-border-width: 1.5px;
            -fx-border-radius: 8px;
            -fx-background-radius: 8px;
            """);

        Label icon = new Label("⚠️");
        icon.setStyle("-fx-font-size: 18px;");

        VBox text = new VBox(2);
        Label t = new Label(title);
        t.setStyle(String.format("-fx-font-size: 12px; -fx-font-weight: 700; -fx-text-fill: %s;", RED));
        Label d = new Label(detail);
        d.setStyle(String.format("-fx-font-size: 11.5px; -fx-text-fill: %s;", TX3));
        text.getChildren().addAll(t, d);

        box.getChildren().addAll(icon, text);
        return box;
    }

    // ──────────────────────────────────────────────
    // FORM FOOTER
    // ──────────────────────────────────────────────
    public static HBox formFooter(Button... buttons) {
        HBox footer = new HBox(8);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(13, 18, 13, 18));
        footer.setStyle(String.format("-fx-background-color: %s; -fx-border-color: %s; -fx-border-width: 1.5 0 0 0;", S2, B1));
        footer.getChildren().addAll(buttons);
        return footer;
    }

    // ──────────────────────────────────────────────
    // DIALOGS
    // ──────────────────────────────────────────────
    public static void showError(Stage owner, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur — EGJS AutoRent");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(owner);
        alert.showAndWait();
    }

    public static boolean showConfirm(Stage owner, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(owner);
        return alert.showAndWait().map(r -> r == ButtonType.OK).orElse(false);
    }

    // ──────────────────────────────────────────────
    // MODAL STAGE
    // ──────────────────────────────────────────────
    public static Stage createModal(Stage owner, String title, Pane content, double width) {
        Stage modal = new Stage();
        modal.initOwner(owner);
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.initStyle(StageStyle.UNDECORATED);
        modal.setTitle(title);

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setStyle(String.format("-fx-background-color: %s; -fx-border-color: %s; -fx-border-width: 0 0 1.5 0;", S1, B1));

        Label titleLbl = new Label(title);
        titleLbl.setStyle(String.format("-fx-font-family: 'Roboto Condensed'; -fx-font-size: 17px; -fx-font-weight: 700; -fx-text-fill: %s;", TX));
        HBox.setHgrow(titleLbl, Priority.ALWAYS);

        Button closeBtn = new Button("✕");
        closeBtn.setStyle(String.format("""
            -fx-background-color: %s; -fx-border-color: %s;
            -fx-border-width: 1.5px; -fx-border-radius: 6px; -fx-background-radius: 6px;
            -fx-text-fill: %s; -fx-font-size: 13px;
            -fx-min-width: 28px; -fx-min-height: 28px; -fx-max-width: 28px; -fx-max-height: 28px;
            -fx-cursor: hand;
            """, S2, B1, TX3));
        closeBtn.setOnMouseEntered(e -> closeBtn.setStyle(closeBtn.getStyle().replace(S2, RED).replace(B1, RED).replace(TX3, "white")));
        closeBtn.setOnMouseExited(e -> closeBtn.setStyle(String.format("""
            -fx-background-color: %s; -fx-border-color: %s;
            -fx-border-width: 1.5px; -fx-border-radius: 6px; -fx-background-radius: 6px;
            -fx-text-fill: %s; -fx-font-size: 13px;
            -fx-min-width: 28px; -fx-min-height: 28px; -fx-max-width: 28px; -fx-max-height: 28px;
            -fx-cursor: hand;
            """, S2, B1, TX3)));
        closeBtn.setOnAction(e -> modal.close());

        header.getChildren().addAll(titleLbl, closeBtn);

        VBox root = new VBox(header, content);
        root.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-border-color: %s;
            -fx-border-width: 1.5px;
            -fx-border-radius: 16px;
            -fx-background-radius: 16px;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 24, 0, 0, 8);
            """, S1, B2));

        Scene scene = new Scene(root, width, -1);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        modal.setScene(scene);
        modal.centerOnScreen();
        return modal;
    }

    // ──────────────────────────────────────────────
    // EMPTY STATE
    // ──────────────────────────────────────────────
    public static VBox emptyState(String icon, String text) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40));

        Label ic = new Label(icon);
        ic.setStyle("-fx-font-size: 36px; -fx-opacity: 0.3;");

        Label txt = new Label(text);
        txt.setStyle(String.format("-fx-font-size: 13px; -fx-text-fill: %s;", TX4));

        box.getChildren().addAll(ic, txt);
        return box;
    }

    // ──────────────────────────────────────────────
    // SEPARATEUR
    // ──────────────────────────────────────────────
    public static Region hsep() {
        Region r = new Region();
        r.setPrefHeight(1);
        r.setMaxWidth(Double.MAX_VALUE);
        r.setStyle(String.format("-fx-background-color: %s;", B1));
        return r;
    }

    public static Label sectionLabel(String text) {
        Label lbl = new Label(text.toUpperCase());
        lbl.setStyle(String.format("-fx-font-size: 10px; -fx-font-weight: 700; -fx-text-fill: %s; -fx-letter-spacing: 1.5px;", TX4));
        return lbl;
    }

    public static Label bodyLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle(String.format("-fx-font-size: 12.5px; -fx-text-fill: %s;", TX3));
        return lbl;
    }

    public static Label boldLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle(String.format("-fx-font-size: 13px; -fx-font-weight: 700; -fx-text-fill: %s;", TX));
        return lbl;
    }

    public static Label monoLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle(String.format("-fx-font-family: 'Roboto Mono'; -fx-font-size: 11.5px; -fx-text-fill: %s;", TX3));
        return lbl;
    }

    public static Label xafLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle(String.format("-fx-font-family: 'Roboto Mono'; -fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: %s;", GREEN));
        return lbl;
    }

    // ──────────────────────────────────────────────
    // FORMAT
    // ──────────────────────────────────────────────
    public static String formatXAF(double montant) {
        return String.format("%,.0f XAF", montant).replace(",", " ");
    }
}
