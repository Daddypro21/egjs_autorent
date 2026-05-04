package cg.egjs.autorent.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import cg.egjs.autorent.config.DatabaseConnection;

/**
 * Utilitaire d'initialisation des mots de passe.
 * À exécuter UNE SEULE FOIS après import du script SQL
 * pour hasher les mots de passe de test avec BCrypt.
 *
 * Lancer : java -cp ... cg.egjs.autorent.util.HashPasswordUtil
 */
public class HashPasswordUtil {

    public static void main(String[] args) {
        System.out.println("=== EGJS AutoRent — Initialisation mots de passe ===\n");

        String[][] comptes = {
            {"admin@egjs-autorent.cg",         "Admin@2026"},
            {"gestionnaire@egjs-autorent.cg",   "Gest@2026"},
            {"jp.moukala@gmail.com",             "Client@2026"},
            {"mc.osseke@gmail.com",              "Client@2026"},
            {"r.bouanga@yahoo.fr",               "Client@2026"},
        };

        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String sql = "UPDATE utilisateur SET motDePasseHash = ? WHERE email = ?";

            for (String[] compte : comptes) {
                String email = compte[0];
                String mdpClair = compte[1];
                String hash = PasswordUtil.hasher(mdpClair);

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, hash);
                    ps.setString(2, email);
                    int rows = ps.executeUpdate();
                    if (rows > 0) {
                        System.out.println("✅ " + email + " → hashé avec succès");
                    } else {
                        System.out.println("⚠️  " + email + " → email non trouvé en BDD");
                    }
                }
            }

            System.out.println("\n✅ Tous les mots de passe ont été hashés avec BCrypt (coût 12).");
            System.out.println("Vous pouvez maintenant vous connecter avec les identifiants de test.");

        } catch (Exception e) {
            System.err.println("❌ Erreur : " + e.getMessage());
            System.err.println("Vérifiez database.properties et assurez-vous que MySQL est démarré.");
        } finally {
            DatabaseConnection.getInstance().closeConnection();
        }
    }
}
