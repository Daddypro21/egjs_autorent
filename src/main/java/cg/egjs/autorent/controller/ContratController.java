package cg.egjs.autorent.controller;

import cg.egjs.autorent.dao.*;
import cg.egjs.autorent.model.*;
import cg.egjs.autorent.service.PDFService;
import cg.egjs.autorent.service.PenaliteService;
import cg.egjs.autorent.util.SessionManager;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Contrôleur des contrats de location.
 * Gère la création, le retour, et les pénalités. (v2 corrigé)
 */
public class ContratController {

    private static final Logger  LOGGER        = Logger.getLogger(ContratController.class.getName());

    private final ContratDAO     contratDAO    = new ContratDAO();
    private final VehiculeDAO    vehiculeDAO   = new VehiculeDAO();
    private final UtilisateurDAO utilisateurDAO= new UtilisateurDAO();
    private final JournalDAO     journalDAO    = new JournalDAO();
    private final PDFService     pdfService    = new PDFService();
    private final PenaliteService penaliteService = new PenaliteService();

    /**
     * Crée un nouveau contrat de location.
     * calculerMontant() est délégué au modèle Contrat (correction audit v2).
     */
    public Contrat creerContrat(int idClient, int idVehicule,
                                LocalDate dateDebut, LocalDate dateFin) throws Exception {

        // 1. Vérifier disponibilité via VehiculeDAO
        boolean dispo = vehiculeDAO.estDisponibleSurPeriode(idVehicule, dateDebut, dateFin);
        if (!dispo) throw new IllegalStateException(
            "VEHICULE_INDISPONIBLE : Ce véhicule n'est pas disponible sur la période sélectionnée.");

        // 2. Charger le véhicule
        Vehicule vehicule = vehiculeDAO.findById(idVehicule)
            .orElseThrow(() -> new IllegalArgumentException("Véhicule introuvable."));

        // 3. Créer le contrat et calculerMontant() sur le MODÈLE (correction audit v2)
        Contrat contrat = new Contrat(dateDebut, dateFin, idClient, idVehicule,
            SessionManager.getInstance().getUtilisateurConnecte().getIdAgence());
        contrat.calculerMontant(vehicule.getPrixJour()); // délégué au modèle

        // 4. Changer statut véhicule → LOUE via VehiculeDAO
        vehiculeDAO.changerStatut(idVehicule, StatutVehicule.LOUE);

        // 5. Enregistrer le contrat en BDD
        int idContrat = contratDAO.enregistrer(contrat);
        contrat.setIdContrat(idContrat);

        // 6. Charger client pour le PDF
        Utilisateur client = utilisateurDAO.findById(idClient)
            .orElseThrow(() -> new IllegalArgumentException("Client introuvable."));
        contrat.setClient(client);
        contrat.setVehicule(vehicule);

        // 7. Générer le PDF
        String cheminPDF = pdfService.genererContrat(contrat);
        contratDAO.mettreAJourCheminPDF(idContrat, cheminPDF);
        contrat.setCheminPDF(cheminPDF);

        // 8. Journaliser
        int idUser = SessionManager.getInstance().getUtilisateurConnecte().getIdUtilisateur();
        journalDAO.log("CREATION_CONTRAT",
            "Contrat #" + idContrat + " créé — Client: " + idClient + " — Véhicule: " + idVehicule,
            idUser);

        LOGGER.info("Contrat #" + idContrat + " créé avec succès.");
        return contrat;
    }

    /**
     * Enregistre le retour d'un véhicule.
     * changerStatut() toujours via VehiculeDAO (correction audit v2).
     */
    public Optional<Penalite> traiterRetour(int idContrat, LocalDate dateRetour) throws Exception {

        Contrat contrat = contratDAO.findById(idContrat)
            .orElseThrow(() -> new IllegalArgumentException("Contrat introuvable."));

        contrat.setDateRetourReelle(dateRetour);
        Optional<Penalite> penaliteOpt = Optional.empty();

        // Vérifier retard via le MODÈLE
        if (contrat.verifierRetard(dateRetour)) {
            // Calculer et sauvegarder la pénalité
            penaliteOpt = Optional.of(
                penaliteService.calculerEtSauvegarder(contrat, dateRetour));
            contratDAO.enregistrerRetour(idContrat, dateRetour, StatutContrat.EN_RETARD);
        } else {
            contratDAO.enregistrerRetour(idContrat, dateRetour, StatutContrat.TERMINE);
        }

        // Libérer le véhicule via VehiculeDAO UNIQUEMENT (correction audit v2)
        vehiculeDAO.changerStatut(contrat.getIdVehicule(), StatutVehicule.DISPONIBLE);

        // Journaliser
        int idUser = SessionManager.getInstance().getUtilisateurConnecte().getIdUtilisateur();
        String detail = "Retour véhicule contrat #" + idContrat + " le " + dateRetour;
        penaliteOpt.ifPresent(p -> LOGGER.info("Pénalité : " + p));
        journalDAO.log("RETOUR_VEHICULE", detail, idUser);

        return penaliteOpt;
    }

    public List<Contrat> getContratsEnCours() throws SQLException {
        return contratDAO.findEnCours();
    }

    public List<Contrat> getContratsDuClient(int idClient) throws SQLException {
        return contratDAO.findByClient(idClient);
    }
}
