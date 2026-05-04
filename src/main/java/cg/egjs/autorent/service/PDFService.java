package cg.egjs.autorent.service;

import cg.egjs.autorent.model.Contrat;
import cg.egjs.autorent.util.FormatUtil;

import java.io.File;
import java.time.format.DateTimeFormatter;

/**
 * Service de génération de contrats PDF.
 * Utilise la bibliothèque iText 7 (à ajouter dans lib/).
 *
 * DÉPENDANCE : itext7-core-7.x.x.jar dans le classpath.
 */
public class PDFService {

    private static final String PDF_DIR = "contrats/";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Génère le PDF du contrat et retourne le chemin du fichier.
     */
    public String genererContrat(Contrat contrat) throws Exception {
        // Créer le répertoire si besoin
        new File(PDF_DIR).mkdirs();

        String nomFichier = PDF_DIR + "contrat_" + contrat.getIdContrat() + ".pdf";

        /*
         * Implémentation iText 7 :
         *
         * PdfWriter writer = new PdfWriter(nomFichier);
         * PdfDocument pdf = new PdfDocument(writer);
         * Document document = new Document(pdf);
         *
         * // En-tête agence
         * document.add(new Paragraph("EGJS AutoRent")
         *     .setFontSize(18).setBold());
         * document.add(new Paragraph("Avenue de France, Brazzaville, Congo"));
         * document.add(new Paragraph("Tél : +242 06 123 4567"));
         *
         * // Titre contrat
         * document.add(new Paragraph("CONTRAT DE LOCATION N° " + contrat.getIdContrat())
         *     .setFontSize(14).setBold().setTextAlignment(TextAlignment.CENTER));
         *
         * // Informations client
         * document.add(new Paragraph("Client : " + contrat.getClient().getNomComplet()));
         * document.add(new Paragraph("Permis : " + contrat.getClient().getNumPermis()));
         *
         * // Informations véhicule
         * document.add(new Paragraph("Véhicule : " + contrat.getVehicule().getDesignation()));
         * document.add(new Paragraph("Immatriculation : " + contrat.getVehicule().getImmatriculation()));
         *
         * // Période et montant
         * document.add(new Paragraph("Du : " + contrat.getDateDebut().format(FMT)));
         * document.add(new Paragraph("Au : " + contrat.getDateFin().format(FMT)));
         * document.add(new Paragraph("Durée : " + contrat.getNbJoursLocation() + " jour(s)"));
         * document.add(new Paragraph("Montant total : "
         *     + FormatUtil.formatXAF(contrat.getMontantTotal())).setBold());
         *
         * document.close();
         */

        // Placeholder en attendant iText — crée un fichier vide pour les tests
        new File(nomFichier).createNewFile();

        return nomFichier;
    }
}
