package cg.egjs.autorent.dao;

import cg.egjs.autorent.config.DatabaseConnection;
import cg.egjs.autorent.model.RoleUtilisateur;
import cg.egjs.autorent.model.Utilisateur;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class UtilisateurDAO {

    private static final Logger LOGGER = Logger.getLogger(UtilisateurDAO.class.getName());

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    /** Recherche un utilisateur par email */
    public Optional<Utilisateur> findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM utilisateur WHERE email = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapper(rs));
            }
        }
        return Optional.empty();
    }

    /** Recherche un utilisateur par ID */
    public Optional<Utilisateur> findById(int id) throws SQLException {
        String sql = "SELECT * FROM utilisateur WHERE idUtilisateur = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapper(rs));
            }
        }
        return Optional.empty();
    }

    /** Liste tous les clients */
    public List<Utilisateur> findAllClients() throws SQLException {
        List<Utilisateur> liste = new ArrayList<>();
        String sql = "SELECT * FROM utilisateur WHERE role = 'CLIENT' ORDER BY nom, prenom";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) liste.add(mapper(rs));
        }
        return liste;
    }

    /** Crée un nouvel utilisateur */
    public int creer(Utilisateur u) throws SQLException {
        String sql = """
            INSERT INTO utilisateur
            (nom, prenom, email, motDePasseHash, role, telephone, adresse, numPermis, idAgence)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getMotDePasseHash());
            ps.setString(5, u.getRole().name());
            ps.setString(6, u.getTelephone());
            ps.setString(7, u.getAdresse());
            ps.setString(8, u.getNumPermis());
            ps.setInt(9,    u.getIdAgence());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public void mettreAJour(Utilisateur u) throws SQLException {
        String sql = """
            UPDATE utilisateur SET nom=?, prenom=?, email=?, telephone=?, adresse=?, numPermis=?
            WHERE idUtilisateur=?
            """;
        try (java.sql.PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getTelephone());
            ps.setString(5, u.getAdresse());
            ps.setString(6, u.getNumPermis());
            ps.setInt(7,    u.getIdUtilisateur());
            ps.executeUpdate();
        }
    }

    /** Incrémente le compteur de tentatives échouées */
    public void incrementerTentatives(String email) throws SQLException {
        String sql = "UPDATE utilisateur SET tentativesConnexion = tentativesConnexion + 1 WHERE email = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, email);
            ps.executeUpdate();
        }
    }

    /** Remet à zéro le compteur après connexion réussie */
    public void resetTentatives(int idUtilisateur) throws SQLException {
        String sql = "UPDATE utilisateur SET tentativesConnexion = 0 WHERE idUtilisateur = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, idUtilisateur);
            ps.executeUpdate();
        }
    }

    /** Bloque un compte (actif = 0) */
    public void bloquerCompte(String email) throws SQLException {
        String sql = "UPDATE utilisateur SET actif = 0 WHERE email = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, email);
            ps.executeUpdate();
            LOGGER.warning("Compte bloqué pour : " + email);
        }
    }

    /** Mappe un ResultSet vers un objet Utilisateur */
    private Utilisateur mapper(ResultSet rs) throws SQLException {
        Utilisateur u = new Utilisateur();
        u.setIdUtilisateur(rs.getInt("idUtilisateur"));
        u.setNom(rs.getString("nom"));
        u.setPrenom(rs.getString("prenom"));
        u.setEmail(rs.getString("email"));
        u.setMotDePasseHash(rs.getString("motDePasseHash"));
        u.setRole(RoleUtilisateur.valueOf(rs.getString("role")));
        u.setTelephone(rs.getString("telephone"));
        u.setAdresse(rs.getString("adresse"));
        u.setNumPermis(rs.getString("numPermis"));
        u.setActif(rs.getBoolean("actif"));
        u.setTentativesConnexion(rs.getInt("tentativesConnexion"));
        u.setIdAgence(rs.getInt("idAgence"));
        Timestamp ts = rs.getTimestamp("dateCreation");
        if (ts != null) u.setDateCreation(ts.toLocalDateTime());
        return u;
    }
}
