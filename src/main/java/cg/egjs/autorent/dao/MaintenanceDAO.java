package cg.egjs.autorent.dao;

import cg.egjs.autorent.config.DatabaseConnection;
import cg.egjs.autorent.model.Maintenance;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MaintenanceDAO {

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    public int creer(Maintenance m) throws SQLException {
        String sql = """
            INSERT INTO maintenance (type, dateEntree, cout, description, idVehicule)
            VALUES (?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, m.getType());
            ps.setDate(2,   Date.valueOf(m.getDateEntree()));
            ps.setDouble(3, m.getCout());
            ps.setString(4, m.getDescription());
            ps.setInt(5,    m.getIdVehicule());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public List<Maintenance> findByVehicule(int idVehicule) throws SQLException {
        List<Maintenance> liste = new ArrayList<>();
        String sql = "SELECT * FROM maintenance WHERE idVehicule = ? ORDER BY dateEntree DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, idVehicule);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapper(rs));
            }
        }
        return liste;
    }

    public void cloturer(int idMaintenance, java.time.LocalDate dateSortie) throws SQLException {
        String sql = "UPDATE maintenance SET dateSortie = ? WHERE idMaintenance = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(dateSortie));
            ps.setInt(2, idMaintenance);
            ps.executeUpdate();
        }
    }

    private Maintenance mapper(ResultSet rs) throws SQLException {
        Maintenance m = new Maintenance();
        m.setIdMaintenance(rs.getInt("idMaintenance"));
        m.setType(rs.getString("type"));
        m.setDateEntree(rs.getDate("dateEntree").toLocalDate());
        Date ds = rs.getDate("dateSortie");
        if (ds != null) m.setDateSortie(ds.toLocalDate());
        m.setCout(rs.getDouble("cout"));
        m.setDescription(rs.getString("description"));
        m.setIdVehicule(rs.getInt("idVehicule"));
        return m;
    }
}
