package cg.egjs.autorent.util;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.File;

public class GenerateDocPDF {

    // Palette de couleurs
    static final DeviceRgb ROUGE      = new DeviceRgb(224, 0, 0);
    static final DeviceRgb ROUGE_DARK = new DeviceRgb(160, 0, 0);
    static final DeviceRgb DARK_BG    = new DeviceRgb(10, 10, 15);
    static final DeviceRgb DARK_CARD  = new DeviceRgb(22, 22, 30);
    static final DeviceRgb GRIS_CLAIR = new DeviceRgb(200, 200, 210);
    static final DeviceRgb GRIS_MED   = new DeviceRgb(130, 130, 150);
    static final DeviceRgb BLANC      = new DeviceRgb(255, 255, 255);
    static final DeviceRgb VERT       = new DeviceRgb(34, 197, 94);
    static final DeviceRgb AMBER      = new DeviceRgb(245, 158, 11);
    static final DeviceRgb BLEU       = new DeviceRgb(96, 165, 250);

    static PdfFont BOLD;
    static PdfFont REGULAR;
    static PdfFont ITALIC;

    public static void main(String[] args) throws Exception {
        BOLD    = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        REGULAR = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        ITALIC  = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);

        String outPath = "EGJS_AutoRent_Documentation.pdf";
        PdfDocument pdf = new PdfDocument(new PdfWriter(outPath));
        Document doc = new Document(pdf, PageSize.A4);
        doc.setMargins(0, 0, 0, 0);

        buildCoverPage(doc, pdf);
        doc.add(new AreaBreak());

        doc.setMargins(45, 50, 45, 50);

        buildTableOfContents(doc);
        doc.add(new AreaBreak());

        buildSection1Prerequisites(doc);
        doc.add(new AreaBreak());

        buildSection2Launch(doc);
        doc.add(new AreaBreak());

        buildSection3Features(doc);
        doc.add(new AreaBreak());

        buildSection4Technologies(doc);
        doc.add(new AreaBreak());

        buildSection5Credentials(doc);

        doc.close();

        System.out.println("Documentation generee : " + new File(outPath).getAbsolutePath());
    }

    // ─────────────────────────────────────────────────────
    // PAGE DE COUVERTURE
    // ─────────────────────────────────────────────────────
    static void buildCoverPage(Document doc, PdfDocument pdf) throws Exception {
        // Fond noir pleine page
        com.itextpdf.kernel.pdf.canvas.PdfCanvas canvas =
            new com.itextpdf.kernel.pdf.canvas.PdfCanvas(pdf.addNewPage());
        canvas.setFillColor(DARK_BG);
        canvas.rectangle(0, 0, PageSize.A4.getWidth(), PageSize.A4.getHeight());
        canvas.fill();

        // Bande rouge en haut
        canvas.setFillColor(ROUGE);
        canvas.rectangle(0, PageSize.A4.getHeight() - 8, PageSize.A4.getWidth(), 8);
        canvas.fill();

        // Bande rouge en bas
        canvas.setFillColor(ROUGE);
        canvas.rectangle(0, 0, PageSize.A4.getWidth(), 8);
        canvas.fill();

        doc.setMargins(0, 60, 0, 60);

        // Espaceur haut
        doc.add(spacer(120));

        // Badge
        Paragraph badge = new Paragraph("DOCUMENTATION OFFICIELLE")
            .setFont(BOLD).setFontSize(8).setFontColor(ROUGE)
            .setCharacterSpacing(3)
            .setTextAlignment(TextAlignment.CENTER);
        doc.add(badge);

        doc.add(spacer(18));

        // Titre principal
        Paragraph titre = new Paragraph("EGJS AutoRent")
            .setFont(BOLD).setFontSize(52).setFontColor(BLANC)
            .setTextAlignment(TextAlignment.CENTER);
        doc.add(titre);

        // Ligne rouge décorative
        doc.add(spacer(6));
        Table ligne = new Table(UnitValue.createPercentArray(new float[]{1}))
            .setWidth(UnitValue.createPercentValue(20))
            .setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);
        Cell c = new Cell().setHeight(3).setBackgroundColor(ROUGE).setBorder(Border.NO_BORDER);
        ligne.addCell(c);
        doc.add(ligne);
        doc.add(spacer(16));

        Paragraph sousTitre = new Paragraph("Logiciel de Gestion de Location de Vehicules")
            .setFont(ITALIC).setFontSize(16).setFontColor(GRIS_CLAIR)
            .setTextAlignment(TextAlignment.CENTER);
        doc.add(sousTitre);

        doc.add(spacer(8));

        Paragraph localisation = new Paragraph("Brazzaville, Republique du Congo")
            .setFont(REGULAR).setFontSize(12).setFontColor(GRIS_MED)
            .setTextAlignment(TextAlignment.CENTER);
        doc.add(localisation);

        doc.add(spacer(80));

        // Encadré infos
        Table infoBox = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
            .setWidth(UnitValue.createPercentValue(80))
            .setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);

        infoBox.addCell(infoCell("Version", "1.0.0"));
        infoBox.addCell(infoCell("Annee", "2026"));
        infoBox.addCell(infoCell("Developpeur", "EGJS"));
        infoBox.addCell(infoCell("Plateforme", "Windows / Linux / macOS"));
        doc.add(infoBox);

        doc.add(spacer(80));

        Paragraph footer = new Paragraph("EGJS — Tous droits reserves  |  2026")
            .setFont(REGULAR).setFontSize(9).setFontColor(GRIS_MED)
            .setTextAlignment(TextAlignment.CENTER);
        doc.add(footer);
    }

    static Cell infoCell(String label, String value) {
        Paragraph p = new Paragraph()
            .add(new Text(label + "\n").setFont(REGULAR).setFontSize(9).setFontColor(GRIS_MED))
            .add(new Text(value).setFont(BOLD).setFontSize(12).setFontColor(BLANC));
        return new Cell()
            .add(p)
            .setBackgroundColor(DARK_CARD)
            .setBorder(new SolidBorder(ROUGE, 1))
            .setPadding(12)
            .setTextAlignment(TextAlignment.CENTER);
    }

    // ─────────────────────────────────────────────────────
    // TABLE DES MATIERES
    // ─────────────────────────────────────────────────────
    static void buildTableOfContents(Document doc) throws Exception {
        sectionTitle(doc, "Table des matieres");

        String[][] chapitres = {
            {"1", "Prerequis — Logiciels a installer",         "3"},
            {"2", "Lancement de l'application en local",       "4"},
            {"3", "Fonctionnalites du logiciel",               "5"},
            {"   3.1", "Connexion et securite",                "5"},
            {"   3.2", "Tableau de bord",                      "5"},
            {"   3.3", "Gestion des vehicules",                "5"},
            {"   3.4", "Gestion des clients",                  "6"},
            {"   3.5", "Contrats de location",                 "6"},
            {"   3.6", "Penalites de retard",                  "6"},
            {"   3.7", "Maintenance des vehicules",            "7"},
            {"   3.8", "Journal des actions",                  "7"},
            {"   3.9", "Statistiques",                         "7"},
            {"4", "Technologies utilisees",                    "8"},
            {"5", "Identifiants de connexion",                 "9"},
        };

        for (String[] ch : chapitres) {
            boolean isSub = ch[0].startsWith(" ");
            Table row = new Table(UnitValue.createPercentArray(new float[]{0.5f, 8, 0.5f}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(4);

            row.addCell(new Cell().add(new Paragraph(ch[0].trim())
                .setFont(isSub ? REGULAR : BOLD).setFontSize(isSub ? 10 : 11)
                .setFontColor(isSub ? GRIS_MED : BLANC))
                .setBorder(Border.NO_BORDER).setPaddingLeft(isSub ? 16 : 0));

            row.addCell(new Cell().add(new Paragraph(ch[1])
                .setFont(isSub ? REGULAR : BOLD).setFontSize(isSub ? 10 : 11)
                .setFontColor(isSub ? GRIS_CLAIR : BLANC))
                .setBorder(Border.NO_BORDER).setBorderBottom(
                    new SolidBorder(isSub ? new DeviceRgb(40,40,55) : new DeviceRgb(50,50,70), 1)));

            row.addCell(new Cell().add(new Paragraph(ch[2])
                .setFont(BOLD).setFontSize(10).setFontColor(ROUGE)
                .setTextAlignment(TextAlignment.RIGHT))
                .setBorder(Border.NO_BORDER));

            doc.add(row);
        }
    }

    // ─────────────────────────────────────────────────────
    // SECTION 1 — PREREQUIS
    // ─────────────────────────────────────────────────────
    static void buildSection1Prerequisites(Document doc) throws Exception {
        chapterHeader(doc, "1", "Prerequis — Logiciels a installer");

        bodyText(doc, "Avant de pouvoir lancer EGJS AutoRent sur votre machine, vous devez "
            + "installer les logiciels suivants. Tous sont gratuits et disponibles pour "
            + "Windows, macOS et Linux.");

        doc.add(spacer(12));

        // Tableau logiciels
        String[][] logiciels = {
            {"Java JDK 17+",   "Machine virtuelle Java",      "https://adoptium.net",          "Temurin 17 LTS recommande"},
            {"Gradle 8+",      "Outil de build",              "https://gradle.org/releases",   "Ajouter au PATH systeme"},
            {"XAMPP",          "Serveur MySQL local",         "https://apachefriends.org",     "Demarrer le module MySQL"},
            {"Git (optionnel)","Gestion de version",          "https://git-scm.com",           "Pour cloner le depot"},
        };

        Table t = logicielsTable(logiciels);
        doc.add(t);
        doc.add(spacer(16));

        subTitle(doc, "Installation de Java (JDK 17)");
        bulletList(doc, new String[]{
            "Rendez-vous sur https://adoptium.net",
            "Telechargez Eclipse Temurin 17 LTS pour votre systeme",
            "Lancez l'installateur et suivez les etapes",
            "Verifiez l'installation : ouvrez un terminal et tapez  java --version",
            "Vous devez voir  java 17.x.x  dans la reponse",
        });

        doc.add(spacer(12));
        subTitle(doc, "Installation de Gradle");
        bulletList(doc, new String[]{
            "Rendez-vous sur https://gradle.org/releases",
            "Telechargez la version binaire (binary-only) la plus recente",
            "Decompressez l'archive dans un dossier (ex: C:\\Gradle\\gradle-8.x)",
            "Ajoutez  C:\\Gradle\\gradle-8.x\\bin  dans la variable d'environnement PATH",
            "Verifiez : ouvrez un terminal et tapez  gradle --version",
        });

        doc.add(spacer(12));
        subTitle(doc, "Installation de XAMPP (MySQL)");
        bulletList(doc, new String[]{
            "Rendez-vous sur https://apachefriends.org",
            "Telechargez et installez XAMPP pour votre systeme",
            "Lancez le panneau de controle XAMPP",
            "Cliquez sur  Start  en face de  MySQL",
            "Le voyant passe au vert : MySQL est pret",
        });

        doc.add(spacer(12));
        subTitle(doc, "Import de la base de donnees");
        bulletList(doc, new String[]{
            "Ouvrez un terminal dans le dossier du projet",
            "Tapez la commande :  mysql -u root -p < autorent_database_v2.sql",
            "Appuyez sur Entree (mot de passe vide par defaut avec XAMPP)",
            "La base de donnees  egjs_autorent  est maintenant creee",
        });
    }

    // ─────────────────────────────────────────────────────
    // SECTION 2 — LANCEMENT
    // ─────────────────────────────────────────────────────
    static void buildSection2Launch(Document doc) throws Exception {
        chapterHeader(doc, "2", "Lancement de l'application en local");

        bodyText(doc, "Une fois Java, Gradle et MySQL installes, suivez ces etapes pour "
            + "demarrer EGJS AutoRent sur votre machine.");

        doc.add(spacer(14));

        String[] etapes = {
            "Demarrer MySQL dans le panneau XAMPP (bouton Start en face de MySQL)",
            "Ouvrir un terminal (cmd ou PowerShell) dans le dossier du projet",
            "Initialiser les mots de passe (a faire une seule fois) :\n   gradle initPasswords",
            "Lancer l'application :\n   gradle run",
            "La fenetre de connexion s'ouvre apres quelques secondes",
        };

        for (int i = 0; i < etapes.length; i++) {
            Table step = new Table(UnitValue.createPercentArray(new float[]{0.3f, 5}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(8);

            step.addCell(new Cell().add(new Paragraph(String.valueOf(i + 1))
                .setFont(BOLD).setFontSize(14).setFontColor(ROUGE)
                .setTextAlignment(TextAlignment.CENTER))
                .setBackgroundColor(new DeviceRgb(40, 0, 0))
                .setBorder(Border.NO_BORDER).setPadding(8)
                .setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.MIDDLE));

            step.addCell(new Cell().add(new Paragraph(etapes[i])
                .setFont(REGULAR).setFontSize(11).setFontColor(GRIS_CLAIR))
                .setBackgroundColor(DARK_CARD)
                .setBorder(Border.NO_BORDER).setPadding(10));

            doc.add(step);
        }

        doc.add(spacer(16));

        // Encadre commande principale
        Table cmdBox = new Table(UnitValue.createPercentArray(new float[]{1}))
            .setWidth(UnitValue.createPercentValue(100));
        cmdBox.addCell(new Cell()
            .add(new Paragraph("Commande de lancement")
                .setFont(BOLD).setFontSize(9).setFontColor(GRIS_MED).setMarginBottom(6))
            .add(new Paragraph("gradle run")
                .setFont(BOLD).setFontSize(22).setFontColor(ROUGE))
            .setBackgroundColor(DARK_CARD)
            .setBorder(new SolidBorder(ROUGE, 2))
            .setPadding(16)
            .setTextAlignment(TextAlignment.CENTER));
        doc.add(cmdBox);

        doc.add(spacer(16));
        subTitle(doc, "Configuration de la base de donnees");
        bodyText(doc, "Si vous changez le mot de passe MySQL ou l'URL de connexion, editez ce fichier avant de lancer :");

        Table confBox = new Table(UnitValue.createPercentArray(new float[]{1}))
            .setWidth(UnitValue.createPercentValue(100))
            .setMarginTop(8);
        confBox.addCell(new Cell()
            .add(new Paragraph("src/main/resources/cg/egjs/autorent/database.properties")
                .setFont(BOLD).setFontSize(10).setFontColor(AMBER).setMarginBottom(6))
            .add(new Paragraph(
                "db.url=jdbc:mysql://localhost:3306/egjs_autorent?useSSL=false&serverTimezone=Africa/Brazzaville\n"
                + "db.user=root\n"
                + "db.password=  (vide par defaut avec XAMPP)")
                .setFont(ITALIC).setFontSize(10).setFontColor(GRIS_CLAIR))
            .setBackgroundColor(DARK_CARD)
            .setBorder(new SolidBorder(AMBER, 1))
            .setPadding(12));
        doc.add(confBox);
    }

    // ─────────────────────────────────────────────────────
    // SECTION 3 — FONCTIONNALITES
    // ─────────────────────────────────────────────────────
    static void buildSection3Features(Document doc) throws Exception {
        chapterHeader(doc, "3", "Fonctionnalites du logiciel");

        bodyText(doc, "EGJS AutoRent couvre l'ensemble du cycle de vie d'une agence de location "
            + "de vehicules : de l'accueil client jusqu'a la cloture de contrat, en passant par "
            + "la maintenance du parc et la comptabilite des penalites.");

        doc.add(spacer(12));

        // 3.1
        subTitle(doc, "3.1  Connexion et securite");
        bulletList(doc, new String[]{
            "Authentification par email et mot de passe hache avec BCrypt",
            "Blocage automatique du compte apres 3 tentatives echouees",
            "3 roles : Administrateur, Gestionnaire, Client",
            "Chaque role donne acces a un menu different",
        });

        // 3.2
        subTitle(doc, "3.2  Tableau de bord");
        bulletList(doc, new String[]{
            "Vue d'ensemble en temps reel : KPIs du parc (disponible, loue, maintenance)",
            "Contrats actifs en cours avec date de retour prevue",
            "Alertes automatiques : contrats en retard, vehicules hors service",
            "Journal des 10 dernieres actions de l'agence",
            "Graphiques de repartition des statuts vehicules et contrats",
        });

        // 3.3
        subTitle(doc, "3.3  Gestion des vehicules");
        bulletList(doc, new String[]{
            "Liste complete du parc avec statut en temps reel (Disponible / Loue / Maintenance / Hors service)",
            "Fiche detaillee : marque, modele, immatriculation, annee, kilometrage, prix/jour",
            "Ajout, modification et suppression de vehicule",
            "Filtrage par statut et recherche par marque/modele",
        });

        doc.add(new AreaBreak());

        // 3.4
        subTitle(doc, "3.4  Gestion des clients");
        bulletList(doc, new String[]{
            "Fiche client : nom, prenom, email, telephone, adresse, numero de permis",
            "Creation de compte client avec mot de passe hache",
            "Blocage / deblocage d'un compte client",
            "Historique des locations par client",
        });

        // 3.5
        subTitle(doc, "3.5  Contrats de location");
        bulletList(doc, new String[]{
            "Creation d'un contrat : choix du vehicule, du client, dates de debut et fin",
            "Calcul automatique du montant total (prix/jour x duree)",
            "Suivi du statut : En cours, Termine, En retard, Annule",
            "Cloture de contrat avec mise a jour automatique du statut vehicule",
            "Generation de contrat en PDF imprimable (iText 7)",
        });

        // 3.6
        subTitle(doc, "3.6  Penalites de retard");
        bulletList(doc, new String[]{
            "Calcul automatique des penalites en cas de retour tardif",
            "Taux journalier configurable par contrat",
            "Marquage d'une penalite comme reglee",
            "Generation de recapitulatif PDF de penalite",
            "Historique complet de toutes les penalites de l'agence",
        });

        // 3.7
        subTitle(doc, "3.7  Maintenance des vehicules");
        bulletList(doc, new String[]{
            "Enregistrement des interventions : vidange, pneus, freins, revision, carrosserie...",
            "Suivi par vehicule avec dates d'entree et de cloture",
            "Cout estime de chaque intervention",
            "Statut en cours / cloture",
        });

        // 3.8
        subTitle(doc, "3.8  Journal des actions");
        bulletList(doc, new String[]{
            "Toutes les operations (creation, modification, suppression) sont journalisees",
            "Horodatage, utilisateur auteur, type d'action et description",
            "Filtrage par date, utilisateur ou type d'action",
            "Outil d'audit pour l'administrateur",
        });

        // 3.9
        subTitle(doc, "3.9  Statistiques");
        bulletList(doc, new String[]{
            "Graphiques natifs JavaFX (pas de librairie externe)",
            "Repartition des statuts vehicules",
            "Evolution du nombre de contrats par mois",
            "Revenus generes par periode",
            "Taux d'occupation du parc",
        });
    }

    // ─────────────────────────────────────────────────────
    // SECTION 4 — TECHNOLOGIES
    // ─────────────────────────────────────────────────────
    static void buildSection4Technologies(Document doc) throws Exception {
        chapterHeader(doc, "4", "Technologies utilisees");

        bodyText(doc, "EGJS AutoRent est developpe entierement en Java, sans framework web ni ORM. "
            + "Voici les technologies et bibliotheques qui composent le logiciel.");

        doc.add(spacer(14));

        String[][] techs = {
            {"Java 17",           "Langage principal",         "LTS — Long Term Support. Base de toute l'application."},
            {"JavaFX 21",         "Interface graphique",       "Framework UI de Java. Toute l'interface est construite en code Java pur, sans fichier FXML."},
            {"Gradle 8",          "Outil de build",            "Gere les dependances, compile et lance l'application avec  gradle run."},
            {"MySQL 8.0",         "Base de donnees",           "Stockage de toutes les donnees (vehicules, clients, contrats). Acces via JDBC direct."},
            {"BCrypt (jbcrypt)",  "Securite des mots de passe","Hashage unidirectionnel des mots de passe. Cout 12 = 4096 iterations."},
            {"iText 7",           "Generation PDF",            "Production des contrats et recapitulatifs de penalites en format PDF imprimable."},
            {"SLF4J",             "Journalisation",            "Logging applicatif : demarrage, connexion BDD, erreurs."},
            {"XAMPP",             "Environnement local",       "Serveur MySQL local pour le developpement. Non requis en production."},
        };

        for (String[] tech : techs) {
            Table row = new Table(UnitValue.createPercentArray(new float[]{1.8f, 1.5f, 4.5f}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(6);

            row.addCell(new Cell()
                .add(new Paragraph(tech[0]).setFont(BOLD).setFontSize(11).setFontColor(ROUGE))
                .setBackgroundColor(new DeviceRgb(30, 0, 0))
                .setBorder(Border.NO_BORDER).setPadding(10)
                .setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.MIDDLE));

            row.addCell(new Cell()
                .add(new Paragraph(tech[1]).setFont(BOLD).setFontSize(10).setFontColor(AMBER))
                .setBackgroundColor(DARK_CARD)
                .setBorder(Border.NO_BORDER).setPadding(10)
                .setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.MIDDLE));

            row.addCell(new Cell()
                .add(new Paragraph(tech[2]).setFont(REGULAR).setFontSize(10).setFontColor(GRIS_CLAIR))
                .setBackgroundColor(DARK_CARD)
                .setBorder(Border.NO_BORDER).setPadding(10));

            doc.add(row);
        }

        doc.add(spacer(16));
        subTitle(doc, "Architecture du projet");
        bodyText(doc, "Le projet suit une architecture MVC stricte a trois couches :");

        String[][] archi = {
            {"View (vue)",       "Classes Java dans  view/  — Construction programmatique de l'interface JavaFX"},
            {"Controller",       "Classes dans  controller/  — Logique metier, orchestration des DAOs"},
            {"DAO",              "Classes dans  dao/  — Acces direct a MySQL via JDBC et PreparedStatement"},
            {"Model",            "Classes dans  model/  — Entites metier (Vehicule, Contrat, Client, etc.)"},
            {"Service",          "Classes dans  service/  — PDFService (iText 7), PenaliteService (calculs)"},
            {"Util",             "Classes dans  util/  — SessionManager, ThemeManager, FormatUtil (XAF)"},
        };

        for (String[] a : archi) {
            Table row = new Table(UnitValue.createPercentArray(new float[]{1.5f, 6f}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(5);
            row.addCell(new Cell()
                .add(new Paragraph(a[0]).setFont(BOLD).setFontSize(10).setFontColor(BLEU))
                .setBorder(Border.NO_BORDER).setBackgroundColor(new DeviceRgb(10, 20, 40)).setPadding(8));
            row.addCell(new Cell()
                .add(new Paragraph(a[1]).setFont(REGULAR).setFontSize(10).setFontColor(GRIS_CLAIR))
                .setBorder(Border.NO_BORDER).setBackgroundColor(DARK_CARD).setPadding(8));
            doc.add(row);
        }
    }

    // ─────────────────────────────────────────────────────
    // SECTION 5 — IDENTIFIANTS
    // ─────────────────────────────────────────────────────
    static void buildSection5Credentials(Document doc) throws Exception {
        chapterHeader(doc, "5", "Identifiants de connexion");

        bodyText(doc, "Ces identifiants sont precharges dans la base de donnees de test. "
            + "Ils sont actives apres avoir execute la commande  gradle initPasswords.");

        doc.add(spacer(16));

        String[][] comptes = {
            {"Administrateur", "admin@egjs-autorent.cg",       "Admin@2026",  "Acces complet a toutes les fonctionnalites"},
            {"Gestionnaire",   "gestionnaire@egjs-autorent.cg", "Gest@2026",  "Gestion vehicules, contrats, maintenance"},
            {"Client",         "jp.moukala@gmail.com",          "Client@2026", "Consultation uniquement"},
        };

        for (String[] c : comptes) {
            DeviceRgb couleur = c[0].equals("Administrateur") ? ROUGE
                              : c[0].equals("Gestionnaire")   ? AMBER : VERT;

            Table card = new Table(UnitValue.createPercentArray(new float[]{1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(12);

            // En-tete role
            card.addCell(new Cell()
                .add(new Paragraph(c[0].toUpperCase())
                    .setFont(BOLD).setFontSize(10).setFontColor(BLANC).setCharacterSpacing(2))
                .setBackgroundColor(couleur)
                .setBorder(Border.NO_BORDER).setPadding(8).setPaddingLeft(14));

            // Corps
            Table body = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}))
                .setWidth(UnitValue.createPercentValue(100));

            body.addCell(credCell("Email", c[1]));
            body.addCell(credCell("Mot de passe", c[2]));
            body.addCell(credCell("Acces", c[3]));

            card.addCell(new Cell().add(body)
                .setBackgroundColor(DARK_CARD)
                .setBorder(new SolidBorder(couleur, 1))
                .setPadding(0));

            doc.add(card);
        }

        doc.add(spacer(20));

        // Rappel commandes
        subTitle(doc, "Rappel des commandes essentielles");

        String[][] cmds = {
            {"gradle initPasswords", "A executer UNE SEULE FOIS — hash les mots de passe en base"},
            {"gradle run",           "Lance l'application (a executer a chaque demarrage)"},
            {"gradle jar",           "Compile un fichier .jar autonome avec toutes les dependances"},
        };

        for (String[] cmd : cmds) {
            Table row = new Table(UnitValue.createPercentArray(new float[]{2f, 5f}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(6);
            row.addCell(new Cell()
                .add(new Paragraph(cmd[0]).setFont(BOLD).setFontSize(11).setFontColor(ROUGE))
                .setBackgroundColor(DARK_CARD).setBorder(new SolidBorder(ROUGE, 1)).setPadding(10));
            row.addCell(new Cell()
                .add(new Paragraph(cmd[1]).setFont(REGULAR).setFontSize(10).setFontColor(GRIS_CLAIR))
                .setBackgroundColor(DARK_CARD).setBorder(Border.NO_BORDER).setPadding(10));
            doc.add(row);
        }

        doc.add(spacer(30));

        // Note securite
        Table note = new Table(UnitValue.createPercentArray(new float[]{1}))
            .setWidth(UnitValue.createPercentValue(100));
        note.addCell(new Cell()
            .add(new Paragraph("Note de securite")
                .setFont(BOLD).setFontSize(10).setFontColor(AMBER).setMarginBottom(6))
            .add(new Paragraph(
                "Ces identifiants sont reserves a un environnement de test et de developpement. "
                + "En production, changez tous les mots de passe et securisez l'acces a MySQL.")
                .setFont(REGULAR).setFontSize(10).setFontColor(GRIS_CLAIR))
            .setBackgroundColor(new DeviceRgb(40, 30, 0))
            .setBorder(new SolidBorder(AMBER, 1))
            .setPadding(14));
        doc.add(note);
    }

    // ─────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────
    static Cell credCell(String label, String value) {
        return new Cell()
            .add(new Paragraph(label).setFont(REGULAR).setFontSize(8).setFontColor(GRIS_MED).setMarginBottom(3))
            .add(new Paragraph(value).setFont(BOLD).setFontSize(11).setFontColor(BLANC))
            .setBorder(Border.NO_BORDER).setPadding(12);
    }

    static Table logicielsTable(String[][] data) {
        Table t = new Table(UnitValue.createPercentArray(new float[]{2f, 2f, 3f, 2.5f}))
            .setWidth(UnitValue.createPercentValue(100));

        String[] headers = {"Logiciel", "Role", "Site de telechargement", "Note"};
        for (String h : headers) {
            t.addHeaderCell(new Cell()
                .add(new Paragraph(h).setFont(BOLD).setFontSize(10).setFontColor(BLANC))
                .setBackgroundColor(ROUGE).setBorder(Border.NO_BORDER).setPadding(8));
        }
        boolean alt = false;
        for (String[] row : data) {
            DeviceRgb bg = alt ? new DeviceRgb(18, 18, 25) : DARK_CARD;
            for (String cell : row) {
                t.addCell(new Cell()
                    .add(new Paragraph(cell).setFont(REGULAR).setFontSize(10).setFontColor(GRIS_CLAIR))
                    .setBackgroundColor(bg).setBorder(Border.NO_BORDER).setPadding(7));
            }
            alt = !alt;
        }
        return t;
    }

    static void sectionTitle(Document doc, String title) throws Exception {
        doc.add(new Paragraph(title)
            .setFont(BOLD).setFontSize(22).setFontColor(BLANC)
            .setMarginBottom(4));
        Table sep = new Table(UnitValue.createPercentArray(new float[]{1}))
            .setWidth(UnitValue.createPercentValue(100)).setMarginBottom(20);
        sep.addCell(new Cell().setHeight(3).setBackgroundColor(ROUGE).setBorder(Border.NO_BORDER));
        doc.add(sep);
    }

    static void chapterHeader(Document doc, String num, String title) throws Exception {
        Table header = new Table(UnitValue.createPercentArray(new float[]{0.4f, 5f}))
            .setWidth(UnitValue.createPercentValue(100)).setMarginBottom(16);
        header.addCell(new Cell()
            .add(new Paragraph(num).setFont(BOLD).setFontSize(28).setFontColor(BLANC)
                .setTextAlignment(TextAlignment.CENTER))
            .setBackgroundColor(ROUGE).setBorder(Border.NO_BORDER).setPadding(10)
            .setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.MIDDLE));
        header.addCell(new Cell()
            .add(new Paragraph(title).setFont(BOLD).setFontSize(20).setFontColor(BLANC))
            .setBackgroundColor(DARK_CARD).setBorder(Border.NO_BORDER).setPadding(14)
            .setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.MIDDLE));
        doc.add(header);
    }

    static void subTitle(Document doc, String text) throws Exception {
        doc.add(new Paragraph(text)
            .setFont(BOLD).setFontSize(13).setFontColor(ROUGE)
            .setMarginTop(12).setMarginBottom(6));
    }

    static void bodyText(Document doc, String text) throws Exception {
        doc.add(new Paragraph(text)
            .setFont(REGULAR).setFontSize(11).setFontColor(GRIS_CLAIR)
            .setFixedLeading(16).setMarginBottom(6));
    }

    static void bulletList(Document doc, String[] items) throws Exception {
        for (String item : items) {
            Table row = new Table(UnitValue.createPercentArray(new float[]{0.15f, 5f}))
                .setWidth(UnitValue.createPercentValue(100)).setMarginBottom(3);
            row.addCell(new Cell()
                .add(new Paragraph("-").setFont(BOLD).setFontSize(11).setFontColor(ROUGE))
                .setBorder(Border.NO_BORDER).setPaddingLeft(8));
            row.addCell(new Cell()
                .add(new Paragraph(item).setFont(REGULAR).setFontSize(11).setFontColor(GRIS_CLAIR))
                .setBorder(Border.NO_BORDER));
            doc.add(row);
        }
    }

    static Paragraph spacer(float height) {
        return new Paragraph("").setMarginBottom(height);
    }
}
