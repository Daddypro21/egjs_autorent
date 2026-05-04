package cg.egjs.autorent.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utilitaire pour le hashage et la vérification des mots de passe.
 * Utilise BCrypt avec un facteur de coût de 12.
 */
public class PasswordUtil {

    private static final int BCRYPT_COST = 12;

    private PasswordUtil() {}

    /** Hash un mot de passe en clair avec BCrypt */
    public static String hasher(String motDePasseClair) {
        if (motDePasseClair == null || motDePasseClair.isBlank())
            throw new IllegalArgumentException("Le mot de passe ne peut pas être vide.");
        return BCrypt.hashpw(motDePasseClair, BCrypt.gensalt(BCRYPT_COST));
    }

    /** Vérifie un mot de passe en clair contre un hash BCrypt */
    public static boolean verifier(String motDePasseClair, String hash) {
        if (motDePasseClair == null || hash == null) return false;
        return BCrypt.checkpw(motDePasseClair, hash);
    }
}
