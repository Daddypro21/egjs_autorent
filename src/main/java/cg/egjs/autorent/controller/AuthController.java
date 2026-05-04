package cg.egjs.autorent.controller;

import cg.egjs.autorent.dao.JournalDAO;
import cg.egjs.autorent.dao.UtilisateurDAO;
import cg.egjs.autorent.model.Utilisateur;
import cg.egjs.autorent.util.PasswordUtil;
import cg.egjs.autorent.util.SessionManager;

import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Contrôleur d'authentification.
 * Gère la connexion, le blocage après 3 tentatives,
 * la journalisation et la session. (v2 corrigé)
 */
public class AuthController {

    private static final Logger        LOGGER   = Logger.getLogger(AuthController.class.getName());
    private static final int           MAX_TENT = 3;

    private final UtilisateurDAO       utilisateurDAO = new UtilisateurDAO();
    private final JournalDAO           journalDAO     = new JournalDAO();
    private final SessionManager       session        = SessionManager.getInstance();

    /**
     * Authentifie un utilisateur.
     * Ordre corrigé (audit v2) : session ouverte AVANT journalisation.
     *
     * @throws AuthException si les identifiants sont invalides, le compte désactivé
     *                       ou bloqué après trop de tentatives
     */
    public Utilisateur authentifier(String email, String motDePasse) throws AuthException, SQLException {

        Optional<Utilisateur> optUser = utilisateurDAO.findByEmail(email);

        // Email inconnu
        if (optUser.isEmpty()) {
            throw new AuthException("IDENTIFIANTS_INVALIDES",
                "Email ou mot de passe incorrect.");
        }

        Utilisateur user = optUser.get();

        // Compte désactivé (audit v2 : cas séparé)
        if (!user.estActif()) {
            journalDAO.log("TENTATIVE_COMPTE_BLOQUE",
                "Tentative sur compte désactivé : " + email, null);
            throw new AuthException("COMPTE_DESACTIVE",
                "Votre compte a été désactivé. Contactez l'agence.");
        }

        // Mot de passe incorrect
        if (!PasswordUtil.verifier(motDePasse, user.getMotDePasseHash())) {
            utilisateurDAO.incrementerTentatives(email);
            int tentatives = user.getTentativesConnexion() + 1;

            // Blocage après MAX_TENT tentatives
            if (tentatives >= MAX_TENT) {
                utilisateurDAO.bloquerCompte(email);
                journalDAO.log("COMPTE_BLOQUE",
                    "Compte bloqué après " + MAX_TENT + " tentatives : " + email, null);
                throw new AuthException("COMPTE_BLOQUE",
                    "Compte bloqué après " + MAX_TENT + " tentatives. Contactez l'agence.");
            }

            throw new AuthException("IDENTIFIANTS_INVALIDES",
                "Email ou mot de passe incorrect. Tentative " + tentatives + "/" + MAX_TENT);
        }

        // ✅ Connexion réussie — ORDRE CORRIGÉ : session AVANT journal
        session.ouvrirSession(user);
        utilisateurDAO.resetTentatives(user.getIdUtilisateur());
        journalDAO.log("CONNEXION", "Connexion réussie : " + email, user.getIdUtilisateur());

        LOGGER.info("Utilisateur connecté : " + user.getNomComplet() + " (" + user.getRole() + ")");
        return user;
    }

    /** Déconnecte l'utilisateur courant */
    public void deconnecter() throws SQLException {
        if (session.estConnecte()) {
            Utilisateur u = session.getUtilisateurConnecte();
            journalDAO.log("DECONNEXION", "Déconnexion : " + u.getEmail(), u.getIdUtilisateur());
            session.fermerSession();
        }
    }

    /** Exception métier d'authentification */
    public static class AuthException extends Exception {
        private final String code;
        public AuthException(String code, String message) {
            super(message);
            this.code = code;
        }
        public String getCode() { return code; }
    }
}
