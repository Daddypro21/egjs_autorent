package cg.egjs.autorent.dao;

import cg.egjs.autorent.config.DatabaseConnection;
import cg.egjs.autorent.model.Penalite;

import java.sql.*;
import java.util.Optional;

public class PenaliteDAO {

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    public int sauvegarder(Penalite p) throws SQLException {
        String sql = """
            INSERT INTO penalite (joursRetard, tauxJour, montant, idContrat)
            VALUES (?, ?, ?, ?)
            """;
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1,    p.getJoursRetard());
            ps.setDouble(2, p.getTauxJour());
            ps.setDouble(3, p.getMontant());
            ps.setInt(4,    p.getIdContrat());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public Optional<Penalite> findByContrat(int idContrat) throws SQLException {
        String sql = "SELECT * FROM penalite WHERE idContrat = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, idContrat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapper(rs));
            }
        }
        return Optional.empty();
    }

    public void marquerRegle(int idPenalite) throws SQLException {
        String sql = "UPDATE penalite SET regle = 1 WHERE idPenalite = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, idPenalite);
            ps.executeUpdate();
        }
    }

    private Penalite mapper(ResultSet rs) throws SQLException {
        Penalite p = new Penalite();
        p.setIdPenalite(rs.getInt("idPenalite"));
        p.setJoursRetard(rs.getInt("joursRetard"));
        p.setTauxJour(rs.getDouble("tauxJour"));
        p.setMontant(rs.getDouble("montant"));
        p.setRegle(rs.getBoolean("regle"));
        p.setIdContrat(rs.getInt("idContrat"));
        Timestamp ts = rs.getTimestamp("dateCalcul");
        if (ts != null) p.setDateCalcul(ts.toLocalDateTime());
        return p;
    }

    /** Récupère toutes les pénalités (pour la vue journal/stats) */
    public java.util.List<Penalite> findAll() throws java.sql.SQLException {
        java.util.List<Penalite> liste = new java.util.ArrayList<>();
        String sql = "SELECT * FROM penalite ORDER BY dateCalcul DESC";
        try (java.sql.PreparedStatement ps = getConn().prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
            while (rs.next()) liste.add(mapper(rs));
        }
        return liste;
    }
}
