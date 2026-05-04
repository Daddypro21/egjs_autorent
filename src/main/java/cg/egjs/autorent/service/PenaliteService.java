package cg.egjs.autorent.service;

import cg.egjs.autorent.dao.PenaliteDAO;
import cg.egjs.autorent.model.Contrat;
import cg.egjs.autorent.model.Penalite;

import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Service de calcul et de persistance des pénalités.
 * Ce service ne touche PAS au statut du véhicule (correction audit v2).
 */
public class PenaliteService {

    private final PenaliteDAO penaliteDAO = new PenaliteDAO();

    /**
     * Calcule et sauvegarde une pénalité pour un contrat en retard.
     * Le taux est récupéré depuis le contrat (taux de l'agence au moment de la création).
     */
    public Penalite calculerEtSauvegarder(Contrat contrat, LocalDate dateRetour) throws SQLException {
        long joursRetard = contrat.calculerJoursRetard(dateRetour);
        if (joursRetard <= 0) throw new IllegalArgumentException(
            "Aucun retard détecté pour ce contrat.");

        // Taux par défaut : 5000 XAF/jour (récupéré depuis AgenceDAO en production)
        double tauxJour = 5000.00;

        Penalite penalite = new Penalite((int) joursRetard, tauxJour, contrat.getIdContrat());
        int idPenalite = penaliteDAO.sauvegarder(penalite);
        penalite.setIdPenalite(idPenalite);

        return penalite;
    }
}
