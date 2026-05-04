package cg.egjs.autorent.util;

import cg.egjs.autorent.model.Utilisateur;

/**
 * Gestionnaire de session utilisateur (Singleton).
 * Stocke l'utilisateur connecté pour toute la durée de la session.
 */
public class SessionManager {

    private static SessionManager instance;
    private Utilisateur utilisateurConnecte;

    private SessionManager() {}

    public static synchronized SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public void ouvrirSession(Utilisateur utilisateur) {
        this.utilisateurConnecte = utilisateur;
    }

    public void fermerSession() {
        this.utilisateurConnecte = null;
    }

    public Utilisateur getUtilisateurConnecte() {
        return utilisateurConnecte;
    }

    public boolean estConnecte() {
        return utilisateurConnecte != null;
    }

    public boolean estAdmin() {
        return estConnecte() && utilisateurConnecte.estAdmin();
    }

    public boolean estGestionnaire() {
        return estConnecte() && utilisateurConnecte.estGestionnaire();
    }
}
