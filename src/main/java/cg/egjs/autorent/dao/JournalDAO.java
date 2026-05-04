package cg.egjs.autorent.dao;

import cg.egjs.autorent.config.DatabaseConnection;
import cg.egjs.autorent.model.JournalAction;

import java.sql.*;

public class JournalDAO {

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    /** Enregistre une action dans le journal */
    public void enregistrer(JournalAction journal) throws SQLException {
        String sql = """
            INSERT INTO journal_action (action, details, adresseIP, idUtilisateur)
            VALUES (?, ?, ?, ?)
            """;
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, journal.getAction());
            ps.setString(2, journal.getDetails());
            ps.setString(3, journal.getAdresseIP());
            if (journal.getIdUtilisateur() != null)
                ps.setInt(4, journal.getIdUtilisateur());
            else
                ps.setNull(4, Types.INTEGER);
            ps.executeUpdate();
        }
    }

    /** Raccourci pour journaliser rapidement */
    public void log(String action, String details, Integer idUtilisateur) throws SQLException {
        enregistrer(new JournalAction(action, details, idUtilisateur));
    }

    /** Récupère les N dernières entrées du journal */
    public java.util.List<JournalAction> findRecent(int limit) throws java.sql.SQLException {
        java.util.List<JournalAction> liste = new java.util.ArrayList<>();
        String sql = "SELECT * FROM journal_action ORDER BY dateHeure DESC LIMIT ?";
        try (java.sql.PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    JournalAction j = new JournalAction();
                    j.setIdLog(rs.getInt("idLog"));
                    j.setAction(rs.getString("action"));
                    j.setDetails(rs.getString("details"));
                    j.setAdresseIP(rs.getString("adresseIP"));
                    java.sql.Timestamp ts = rs.getTimestamp("dateHeure");
                    if (ts != null) j.setDateHeure(ts.toLocalDateTime());
                    int idU = rs.getInt("idUtilisateur");
                    if (!rs.wasNull()) j.setIdUtilisateur(idU);
                    liste.add(j);
                }
            }
        }
        return liste;
    }
}
