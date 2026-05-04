package cg.egjs.autorent.dao;

import cg.egjs.autorent.config.DatabaseConnection;
import cg.egjs.autorent.model.StatutVehicule;
import cg.egjs.autorent.model.Vehicule;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VehiculeDAO {

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    public List<Vehicule> findAll() throws SQLException {
        List<Vehicule> liste = new ArrayList<>();
        String sql = "SELECT * FROM vehicule ORDER BY marque, modele";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) liste.add(mapper(rs));
        }
        return liste;
    }

    public List<Vehicule> findDisponibles() throws SQLException {
        List<Vehicule> liste = new ArrayList<>();
        String sql = "SELECT * FROM vehicule WHERE statut = 'DISPONIBLE' ORDER BY marque";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) liste.add(mapper(rs));
        }
        return liste;
    }

    public Optional<Vehicule> findById(int id) throws SQLException {
        String sql = "SELECT * FROM vehicule WHERE idVehicule = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapper(rs));
            }
        }
        return Optional.empty();
    }

    public int creer(Vehicule v) throws SQLException {
        String sql = """
            INSERT INTO vehicule
            (marque, modele, annee, immatriculation, prixJour, statut, kilometrage, photoPath, idAgence)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, v.getMarque());
            ps.setString(2, v.getModele());
            ps.setInt(3,    v.getAnnee());
            ps.setString(4, v.getImmatriculation());
            ps.setDouble(5, v.getPrixJour());
            ps.setString(6, v.getStatut().name());
            ps.setInt(7,    v.getKilometrage());
            ps.setString(8, v.getPhotoPath());
            ps.setInt(9,    v.getIdAgence());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public void modifier(Vehicule v) throws SQLException {
        String sql = """
            UPDATE vehicule SET marque=?, modele=?, annee=?, immatriculation=?,
            prixJour=?, kilometrage=?, photoPath=? WHERE idVehicule=?
            """;
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, v.getMarque());
            ps.setString(2, v.getModele());
            ps.setInt(3,    v.getAnnee());
            ps.setString(4, v.getImmatriculation());
            ps.setDouble(5, v.getPrixJour());
            ps.setInt(6,    v.getKilometrage());
            ps.setString(7, v.getPhotoPath());
            ps.setInt(8,    v.getIdVehicule());
            ps.executeUpdate();
        }
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM vehicule WHERE idVehicule = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * Change le statut d'un véhicule.
     * TOUJOURS appelé depuis VehiculeDAO — jamais depuis un autre service.
     * (correction audit v2)
     */
    public void changerStatut(int idVehicule, StatutVehicule statut) throws SQLException {
        String sql = "UPDATE vehicule SET statut = ? WHERE idVehicule = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, statut.name());
            ps.setInt(2, idVehicule);
            ps.executeUpdate();
        }
    }

    /**
     * Vérifie la disponibilité d'un véhicule sur une période donnée.
     * Un véhicule est indisponible si un contrat EN_COURS chevauche les dates demandées.
     */
    public boolean estDisponibleSurPeriode(int idVehicule,
                                           java.time.LocalDate debut,
                                           java.time.LocalDate fin) throws SQLException {
        String sql = """
            SELECT COUNT(*) FROM contrat
            WHERE idVehicule = ?
            AND statut = 'EN_COURS'
            AND NOT (dateFin <= ? OR dateDebut >= ?)
            """;
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, idVehicule);
            ps.setDate(2, Date.valueOf(debut));
            ps.setDate(3, Date.valueOf(fin));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) == 0;
            }
        }
        return false;
    }

    private Vehicule mapper(ResultSet rs) throws SQLException {
        Vehicule v = new Vehicule();
        v.setIdVehicule(rs.getInt("idVehicule"));
        v.setMarque(rs.getString("marque"));
        v.setModele(rs.getString("modele"));
        v.setAnnee(rs.getInt("annee"));
        v.setImmatriculation(rs.getString("immatriculation"));
        v.setPrixJour(rs.getDouble("prixJour"));
        v.setStatut(StatutVehicule.valueOf(rs.getString("statut")));
        v.setKilometrage(rs.getInt("kilometrage"));
        v.setPhotoPath(rs.getString("photoPath"));
        v.setIdAgence(rs.getInt("idAgence"));
        Timestamp ts = rs.getTimestamp("dateAjout");
        if (ts != null) v.setDateAjout(ts.toLocalDateTime());
        return v;
    }
}
