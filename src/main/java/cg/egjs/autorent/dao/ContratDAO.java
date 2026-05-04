package cg.egjs.autorent.dao;

import cg.egjs.autorent.config.DatabaseConnection;
import cg.egjs.autorent.model.Contrat;
import cg.egjs.autorent.model.StatutContrat;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ContratDAO {

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    public int enregistrer(Contrat c) throws SQLException {
        String sql = """
            INSERT INTO contrat
            (dateDebut, dateFin, montantTotal, statut, idClient, idVehicule, idAgence)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDate(1,   Date.valueOf(c.getDateDebut()));
            ps.setDate(2,   Date.valueOf(c.getDateFin()));
            ps.setDouble(3, c.getMontantTotal());
            ps.setString(4, c.getStatut().name());
            ps.setInt(5,    c.getIdClient());
            ps.setInt(6,    c.getIdVehicule());
            ps.setInt(7,    c.getIdAgence());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public Optional<Contrat> findById(int id) throws SQLException {
        String sql = "SELECT * FROM contrat WHERE idContrat = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapper(rs));
            }
        }
        return Optional.empty();
    }

    public List<Contrat> findEnCours() throws SQLException {
        List<Contrat> liste = new ArrayList<>();
        String sql = "SELECT * FROM contrat WHERE statut IN ('EN_COURS','EN_RETARD') ORDER BY dateFin";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) liste.add(mapper(rs));
        }
        return liste;
    }

    public List<Contrat> findByClient(int idClient) throws SQLException {
        List<Contrat> liste = new ArrayList<>();
        String sql = "SELECT * FROM contrat WHERE idClient = ? ORDER BY dateCreation DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, idClient);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapper(rs));
            }
        }
        return liste;
    }

    public void mettreAJourStatut(int idContrat, StatutContrat statut) throws SQLException {
        String sql = "UPDATE contrat SET statut = ? WHERE idContrat = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, statut.name());
            ps.setInt(2, idContrat);
            ps.executeUpdate();
        }
    }

    public void enregistrerRetour(int idContrat, java.time.LocalDate dateRetour,
                                   StatutContrat statut) throws SQLException {
        String sql = "UPDATE contrat SET dateRetourReelle = ?, statut = ? WHERE idContrat = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setDate(1,   Date.valueOf(dateRetour));
            ps.setString(2, statut.name());
            ps.setInt(3,    idContrat);
            ps.executeUpdate();
        }
    }

    public void mettreAJourCheminPDF(int idContrat, String chemin) throws SQLException {
        String sql = "UPDATE contrat SET cheminPDF = ? WHERE idContrat = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, chemin);
            ps.setInt(2, idContrat);
            ps.executeUpdate();
        }
    }

    private Contrat mapper(ResultSet rs) throws SQLException {
        Contrat c = new Contrat();
        c.setIdContrat(rs.getInt("idContrat"));
        c.setDateDebut(rs.getDate("dateDebut").toLocalDate());
        c.setDateFin(rs.getDate("dateFin").toLocalDate());
        Date dr = rs.getDate("dateRetourReelle");
        if (dr != null) c.setDateRetourReelle(dr.toLocalDate());
        c.setMontantTotal(rs.getDouble("montantTotal"));
        c.setStatut(StatutContrat.valueOf(rs.getString("statut")));
        c.setCheminPDF(rs.getString("cheminPDF"));
        c.setIdClient(rs.getInt("idClient"));
        c.setIdVehicule(rs.getInt("idVehicule"));
        c.setIdAgence(rs.getInt("idAgence"));
        Timestamp ts = rs.getTimestamp("dateCreation");
        if (ts != null) c.setDateCreation(ts.toLocalDateTime());
        return c;
    }
}
