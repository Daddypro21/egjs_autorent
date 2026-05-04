package cg.egjs.autorent.controller;

import cg.egjs.autorent.dao.JournalDAO;
import cg.egjs.autorent.dao.VehiculeDAO;
import cg.egjs.autorent.model.StatutVehicule;
import cg.egjs.autorent.model.Vehicule;
import cg.egjs.autorent.util.SessionManager;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class VehiculeController {

    private final VehiculeDAO vehiculeDAO = new VehiculeDAO();
    private final JournalDAO  journalDAO  = new JournalDAO();

    public List<Vehicule> getTousLesVehicules() throws SQLException {
        return vehiculeDAO.findAll();
    }

    public List<Vehicule> getVehiculesDisponibles() throws SQLException {
        return vehiculeDAO.findDisponibles();
    }

    public Optional<Vehicule> getVehiculeById(int id) throws SQLException {
        return vehiculeDAO.findById(id);
    }

    public Vehicule ajouterVehicule(Vehicule v) throws SQLException {
        int id = vehiculeDAO.creer(v);
        v.setIdVehicule(id);
        int idUser = SessionManager.getInstance().getUtilisateurConnecte().getIdUtilisateur();
        journalDAO.log("AJOUT_VEHICULE",
            "Véhicule ajouté : " + v.getDesignation() + " — " + v.getImmatriculation(), idUser);
        return v;
    }

    public void modifierVehicule(Vehicule v) throws SQLException {
        vehiculeDAO.modifier(v);
        int idUser = SessionManager.getInstance().getUtilisateurConnecte().getIdUtilisateur();
        journalDAO.log("MODIFICATION_VEHICULE",
            "Véhicule modifié : #" + v.getIdVehicule(), idUser);
    }

    public void supprimerVehicule(int id) throws SQLException {
        vehiculeDAO.supprimer(id);
        int idUser = SessionManager.getInstance().getUtilisateurConnecte().getIdUtilisateur();
        journalDAO.log("SUPPRESSION_VEHICULE", "Véhicule supprimé : #" + id, idUser);
    }

    public boolean verifierDisponibilite(int idVehicule,
                                          LocalDate debut, LocalDate fin) throws SQLException {
        return vehiculeDAO.estDisponibleSurPeriode(idVehicule, debut, fin);
    }
}
