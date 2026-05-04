package cg.egjs.autorent.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Singleton de connexion à la base de données MySQL.
 * Les paramètres sont chargés depuis database.properties.
 */
public class DatabaseConnection {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    private static DatabaseConnection instance;
    private Connection connection;

    private String url;
    private String user;
    private String password;

    private DatabaseConnection() {
        loadProperties();
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("cg/egjs/autorent/database.properties")) {
            if (input == null) {
                throw new RuntimeException("Fichier database.properties introuvable.");
            }
            Properties props = new Properties();
            props.load(input);
            this.url      = props.getProperty("db.url");
            this.user     = props.getProperty("db.user");
            this.password = props.getProperty("db.password");
        } catch (IOException e) {
            throw new RuntimeException("Erreur chargement configuration BDD : " + e.getMessage(), e);
        }
    }

    public Connection getConnection() throws SQLException {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(url, user, password);
                LOGGER.info("Connexion BDD établie.");
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MySQL introuvable : " + e.getMessage(), e);
        }
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                LOGGER.info("Connexion BDD fermée.");
            }
        } catch (SQLException e) {
            LOGGER.warning("Erreur fermeture connexion : " + e.getMessage());
        }
    }
}
